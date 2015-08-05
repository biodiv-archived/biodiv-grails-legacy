package species.participation

import org.apache.commons.logging.LogFactory;

import species.SpeciesPermission
import species.AcceptedSynonym
import species.ScientificName
import species.TaxonomyDefinition;
import species.SynonymsMerged;
import species.Synonyms;
import species.NamesMetadata;
import species.TaxonomyRegistry
import species.Classification;
import species.Species;
import species.SpeciesField;
import species.ScientificName.TaxonomyRank
import species.Synonyms;
import species.CommonNames;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.COLNameStatus;
import species.NamesMetadata.NamePosition;
import species.auth.SUser;
import content.eml.DocSciName
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.sql.Sql
import groovy.util.XmlParser
import grails.converters.JSON;
import wslite.soap.*
import species.NamesParser;
import species.sourcehandler.XMLConverter;
import species.participation.Recommendation;
import speciespage.SpeciesUploadService;

import species.namelist.Utils

class NamelistUtilService {
	
	private static final String LOG_PREFIX = "- NAME LIST - "

	def dataSource
    def groupHandlerService
    def springSecurityService;
    def taxonService;
    def grailsApplication;
    def speciesService;
    def utilsService;
	def sessionFactory;
    def activityFeedService;
	def namelistService;
	
	
	
	public downloadColXml(List<ScientificName> taxons){
		Utils.saveFiles(new File(grailsApplication.config.speciesPortal.namelist.rootDir), taxons, [])
	}
	
	public Map getColDataFromColId(String colId){
		return namelistService.processColData(colId)?.get(0)
	}
	
	public updateIBPNameWithCol(ScientificName sn, String colId){
		if(!sn || !colId)
			return
	
		Map match = getColDataFromXml(sn, colId)
		if(match){
			log.debug LOG_PREFIX + " Found match for " + sn.canonicalForm + "  col Id " + colId
			namelistService.processDataForMigration(sn, match, 1)
		}else{
			log.debug LOG_PREFIX + " Either match not found or name is already in working list " + "------- for " + sn.canonicalForm + "  col Id " + colId
		}			
	}
	
	public createDuplicateNameWithNewColId(String colId){
		Map match = namelistService.processColData(colId)?.get(0)
		if(match){
			log.debug LOG_PREFIX + " Found match for  col Id " + colId
			//log.debug match
			namelistService.processDataForMigration(new TaxonomyDefinition(), match, 1, true)
		}else{
			log.debug LOG_PREFIX + " Either match not found or name is already in working list for col Id " + colId
		}
	}
	
	private Map getColDataFromXml(ScientificName sn, String colId){
		List colResultList = getColDataFromXml(sn)
		Map acceptedMatch = null;
		colResultList.each { colMatch ->
			if(colMatch.externalId == colId){
				acceptedMatch = colMatch
			}
		}
		
		if(acceptedMatch){
			//log.debug LOG_PREFIX + " Match foud based on given colId " + acceptedMatch
		}
		
		return acceptedMatch
	}
	
	private List getColDataFromXml(ScientificName sn){
		String taxCan = sn.canonicalForm.replaceAll(' ', '_');
		return namelistService.processColData(new File(new File(grailsApplication.config.speciesPortal.namelist.rootDir), taxCan +'.xml'), sn);
	}

	public addExistingHir(ScientificName sn, String colId, String path){
		TaxonomyDefinition nTd = TaxonomyDefinition.findByMatchId(colId)
		log.debug LOG_PREFIX + "Assigning existing hir for col id " + colId + "  new name id " + nTd.id + "  old id " + sn.id
		log.debug LOG_PREFIX + " path  " + path
		
		def oldId = sn.id
		def saveTrList = []
		
		//updating taxon def
		def updatedOldId = oldId
		def trList = TaxonomyRegistry.findAllByPath(path)
		trList.each { tr ->
			log.debug LOG_PREFIX + " path  " + tr.path + " updating taxondef " + tr.id 
			tr.taxonDefinition = nTd
			println "=== must come here " + path
			def idList = tr.path.split("_").collect{it}
			updatedOldId = idList.last()
			idList.remove(updatedOldId)
			idList.add(nTd.id)
			tr.path = idList.join("_")
			println "===== new path " + tr.path
			saveTrList << tr
		}
		saveTrList.each { tr ->
			if(!tr.save(flush:true)){
				tr.errors.allErrors.each { log.error it }
			}
			
		}
		
		//updating path
		saveTrList.clear()
		trList = TaxonomyRegistry.findAllByPathLike(path + "_%")
		log.debug LOG_PREFIX + "child tr List " + trList.size()
		trList.each { tr ->
			log.debug LOG_PREFIX + " path  " + tr.path + " tr id " + tr.id
			println " updating child path "
			tr.path = tr.path.replace('_' +  updatedOldId + '_',  '_' + nTd.id + '_')
			println "===== child new path " + tr.path
			saveTrList << tr
		}
		saveTrList.each { tr ->
			if(!tr.save(flush:true)){
				tr.errors.allErrors.each { log.error it }
			}
			
		}
		
		//updating parent taxon def
		saveTrList.clear()
		TaxonomyRegistry.findAllByTaxonDefinition(nTd).each { tr ->
		TaxonomyRegistry.findAllByParentTaxon(tr).each { ntr ->
			ntr.parentTaxonDefinition = nTd
			saveTrList << ntr
			println "===== saving parent taxon def in tr " + ntr
		}
		}
		saveTrList.each { tr ->
			if(!tr.save(flush:true)){
				tr.errors.allErrors.each { log.error it }
			}
			
		}
		
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////  NAMES STATS Check  ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public void generateStatsInput(File f){
		def oldTimeOut = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(50000);
		def sql =  Sql.newInstance(dataSource);
		def query  = "select id from taxonomy_definition where status = 'ACCEPTED' and is_deleted = false and position = 'WORKING' order by rank,id asc "
		int i = 0
		int offset = 0
		int limit = 1000
		def ibpHier = Classification.findByName('IBP Taxonomy Hierarchy')
		f.withWriter { out ->
			out.println "id|name|rank|colId|paths|colIdPath"
			while(true){
				String q = query + " limit " + limit + " offset " + offset 
				def resList = sql.rows(q)
				if(resList.isEmpty())
					break
					
				resList.each{
					i++
					if(i%100 == 0){
						println " count " + i + " time " + new Date()
					}
					TaxonomyDefinition tdf = TaxonomyDefinition.get(it.getProperty("id"))
					def colIdPaths = []
					def idPaths = []
					def trs = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(tdf, ibpHier)
					trs.each { tr ->
						colIdPaths << (tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).matchId}.join("_"))
						idPaths << tr.path
					}
					out.println tdf.id + "|" + tdf.name + "|" + tdf.rank + "|" + tdf.matchId + "|" + idPaths.join('#') + "|" + colIdPaths.join('#')
				}
				utilsService.cleanUpGorm()
				offset += limit
				println " new offset " + offset
			}
			
		}
		dataSource.setUnreturnedConnectionTimeout(oldTimeOut);
	}
	
	
	public void verifyAcceptedNamesAndColPath(File inputFile, File outputFile){
		def oldTimeOut = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(50000);
		def startTime = new Date()
		List lines = inputFile.readLines();
		int i=0;
		List failed = []
		lines.each { line ->
			i++
			if(i == 1) return;
			if(i%100 == 0){
				println ">>>>>>>>>>>>>  count " + i +  " failed size " + failed.size()  + " time " + new Date()
			}
			
			try{
				def r = line.split('\\|')
				println  "Starting for " + r
				if(r.length < 6){
					def failReason = "| No hirachy stored in db for this"
					failed << (line + failReason)
					return
				}
				def id = Long.parseLong(r[0])
				def colId = r[3]
				def colIdPath = r[5].split("#").collect{it}
				
				if(!verify(id, colId, colIdPath, line, failed)){
					println "=== failed for line " + line
				}
			}catch(e){
				def failReason = "| Exception came while verification " + e.message
				failed << (line + failReason)
				e.printStackTrace()
			}
		}
		outputFile.withWriter { out ->
			out.println "id|name|rank|colId|paths|colIdPath|failReason|ibppath|correct_col_path"
			failed.each { l ->
				out.println l
			}
			//out.println "Total failed " + failed.size() + "  start time " + startTime + "  end time " + new Date()
		}
		dataSource.setUnreturnedConnectionTimeout(oldTimeOut);
		
	}
	
	private boolean verify(id, String colId, List colIdPath, String line, List failed){
		def td = TaxonomyDefinition.read(id)
		def res = getColDataFromXml(td, colId)
		if(!res){
			println " col id in xml not found.  " + colId + " getting from the col website now"
			res = getColDataFromColId(colId)
		}
		if(!res){
			def failReason = "| No result found from col "
			failed << (line + failReason)
			return false
		}
		
		boolean ret = true
		colIdPath.each { p ->
			String colXmlPath = res["colIdPath"]
			colXmlPath = (colXmlPath == "")? colId : colXmlPath + "_" + colId
			String colNamePath = res["colNamePath"] + "->" + td.canonicalForm
			if(colXmlPath != p){
				ret = false
				def failReason = "|" +  (colIdPath.size()>1 ? "MULTIPLE PATH":"")  + " Database " + p + " does NOT match XML " + colXmlPath	+ getDetailPathInfo(p) + "|" + colNamePath 
				failed << (line + failReason)
			}
		}
		
		return ret
		
	}
	
	private String getDetailPathInfo(p){
		String ret = "|"
		ret += p.split("_").collect{TaxonomyDefinition.findByMatchId(it).name}.join("->")
	}
	
	
	
	
	public copyIBPHirAsCOL(){
		def oldTimeOut = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(50000);
		def sql =  Sql.newInstance(dataSource);
		def query  = ''' select tr.id as trId from taxonomy_registry as tr, taxonomy_definition as t where t.status = 'ACCEPTED' and t.position = 'WORKING' and t.is_deleted = false and t.id = tr.taxon_definition_id  and tr.classification_id = 265799 and rank > -1 order by t.rank, t.id '''
		int i = 0
		int offset = 0
		int limit = 1000
		def colHir = Classification.findByName('Catalogue of Life Taxonomy Hierarchy')
		Map pMap = [:]
		while(true){
			String q = query + " limit " + limit + " offset " + offset
			def resList = sql.rows(q)
			if(resList.isEmpty())
				break
			
			TaxonomyRegistry.withNewTransaction {
				resList.each{
					i++
					if(i%100 == 0){
						println " count " + i + " time " + new Date()
					}
					TaxonomyRegistry ibpTr = TaxonomyRegistry.get(it.getProperty("trId"))
					TaxonomyRegistry colTr = new TaxonomyRegistry()
					colTr.properties = ibpTr.properties
					colTr.classification = colHir
					
					def pTdf = ibpTr.parentTaxonDefinition
					
					if(pTdf){
						colTr.parentTaxon = pMap.get(pTdf)
					}
					colTr.contributors = null;
					
					if(!colTr.save(flush:true)){
						colTr.errors.allErrors.each { log.error it }
					}else{
						pMap.put(colTr.taxonDefinition, colTr)
					}
				}
			}
			utilsService.cleanUpGorm()
			pMap.clear()
			offset += limit
			println "map size " + pMap.size()  + " new offset " + offset
		}
			
		dataSource.setUnreturnedConnectionTimeout(oldTimeOut); 
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////  MERGE ACCEPTED NAME  ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public mergeSynonym(long oldId, long newId, boolean fullDelete = false){
		SynonymsMerged oldName = SynonymsMerged.get(oldId)
		SynonymsMerged newName = SynonymsMerged.get(newId)
		
		
		if(!oldName ||  !newName){
			log.debug "One of name is not exist in the system " + oldId + "  " +  newId
			return
		}
		
		//moving synonym
		def oldEntries = AcceptedSynonym.findAllBySynonym(oldName);
		oldEntries.each { e ->
			TaxonomyDefinition acName = e.accepted
			acName.removeSynonym(oldName)
			acName.addSynonym(newName)
		}
		
		if(fullDelete){
			println "========= old name " + oldName
			if(oldName)
				oldName.delete(flush:true)
		}else{
			oldName.isDeleted = true
			println "======= for delete " + oldName
			if(!oldName.save(flush:true)){
				oldName.errors.allErrors.each { log.error it }
			}
		}
	}
	
	
	public mergeAcceptedName(long oldId, long newId, boolean fullDelete = false){
		TaxonomyDefinition oldName = TaxonomyDefinition.get(oldId)
		TaxonomyDefinition newName = TaxonomyDefinition.get(newId)
		
		
		if(!oldName ||  !newName){
			log.debug "One of name is not exist in the system " + oldId + "  " +  newId
			return
		}
		
		
		
		def oldTrList = TaxonomyRegistry.findAllByTaxonDefinition(oldName)
		println "=== findbytaxondef " + oldTrList
		
		//update all paths for this taxon defintion
		List trList = TaxonomyRegistry.findAllByPathLike('%_' + oldId + '_%')
		trList.addAll(TaxonomyRegistry.findAllByPathLike(oldId + '_%'))
		trList.addAll(TaxonomyRegistry.findAllByPath(oldId))
		trList.addAll(oldTrList)
		trList.unique()
		
		println " complete tr list ------------ " + trList
		
		List oldTrToBeDeleted = []
		List updateTrList = []
		
		Map updateTrMap = [:]
		Map deleteToParentTaxonMap = [:]
		Species.withTransaction {
		println "================ starting things now "
		trList.each { TaxonomyRegistry tr ->
			String newPath = tr.path
			newPath = newPath.replaceAll('_' + oldId + '_', '_' + newId + '_')
			
			if(newPath.startsWith(oldId + '_'))
				newPath.replaceFirst(oldId + '_', newId + '_')
			
			if(newPath == ('' + oldId) )
				newPath = '' + newId
			
			if(newPath.endsWith('_' + oldId))
				newPath = newPath.substring(0, newPath.lastIndexOf('_') + 1) +  newId
				
			if(isDuplicateTr(tr, newPath, newName, deleteToParentTaxonMap)){
				oldTrToBeDeleted << tr
			}else{
				updateTrMap.put(tr, newPath)
			}
		}
		
		trList.clear()
		updateTrMap.each { k, v ->
			k.path = v
			updateTrList << k
		}
		updateTrMap.clear()
		
		oldTrList.removeAll(oldTrToBeDeleted)
		//trList.removeAll(oldTrToBeDeleted)
		
		println "------------ updateTrList " + updateTrList
		updateTrList.each {TaxonomyRegistry tr ->
			println "================ updating tr " + tr
			if(tr.parentTaxonDefinition == oldName){
				tr.parentTaxonDefinition = newName
			}
			if(!tr.save(flush:true)){
				tr.errors.allErrors.each { log.error it }
			}
		}
		println "------------ oldTrToBeDeleted " + oldTrToBeDeleted
		oldTrToBeDeleted.each {TaxonomyRegistry tr ->
			println "================ deleting tr " + tr
			TaxonomyRegistry tmpTr = deleteToParentTaxonMap.get(tr)
			TaxonomyRegistry.findAllByParentTaxon(tr).each{
				it.parentTaxon = tmpTr
				it.parentTaxonDefinition = tmpTr.taxonDefinition
				if(!tmpTr.save(flush:true)){
					tmpTr.errors.allErrors.each { log.error it }
				}
			}
			tr.delete(flush:true)
		}
		
		
		println "------------ oldTrList " + oldTrList
		//updating taxon def so that new hirarchy should be shown
		oldTrList.each {TaxonomyRegistry tr ->
			println "================ taxon def for tr " + tr
			tr.taxonDefinition = newName
			
		}
		
		
		
		//moving synonym
		oldName.fetchSynonyms().each {
			println "================ synonym move " + it
			newName.addSynonym(it)
			oldName.removeSynonym(it)
		}
		
		//moving common names
		List cns = CommonNames.findAllByTaxonConcept(oldName)
		cns.each { cn ->
			println "================ common name update " + cn
			cn.taxonConcept = newName
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
		
		//moving docsciname
		List dcs = DocSciName.findAllByTaxonConcept(oldName)
		dcs.each {DocSciName  cn ->
			cn.taxonConcept = newName
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
		
		Species oldSpecies = Species.findByTaxonConcept(oldName)
		Species newSpecies = Species.findByTaxonConcept(newName)
		
		//updating species for names
		if(!newSpecies && oldSpecies){
			oldSpecies.taxonConcept = newName
			if(!oldSpecies.save(flush:true)){
				oldSpecies.errors.allErrors.each { log.error it }
			}
			log.debug "  new speices not available"
		}
		
		if(newSpecies && oldSpecies){
			//move sfield
			SpeciesField.findAllBySpecies(oldSpecies).each {sf ->
				sf.species = newSpecies
				if(!sf.save(flush:true)){
					sf.errors.allErrors.each { log.error it }
				}
			}
			
			//move resources
			def ress = oldSpecies.resources.collect { it}
			ress.each { res ->
				log.debug "Removing resource " + res
				newSpecies.addToResources(res)
				oldSpecies.removeFromResources(res)
			}
			
			//add hyper link for redirect
			log.debug "================= old species id " + oldSpecies + " ............ " + newSpecies
			ResourceRedirect.addLink(oldSpecies, newSpecies)
			
			//saving new species
			if(!newSpecies.save(flush:true)){
				newSpecies.errors.allErrors.each { log.error it }
			}
			
			//deleting speices
			//oldSpecies.taxonConcept = null
			oldSpecies.deleteSpecies(SUser.read(1))
		}
		
		//setting delete flag true on name
		if(fullDelete){
			def newReco = Recommendation.findByTaxonConcept(newName)
			def reco = Recommendation.findByTaxonConcept(oldName)
			if(reco){
				RecommendationVote.findAllByRecommendationOrCommonNameReco(reco, reco).each { r ->
					println " saving reco vote  " + r
					if(r.recommendation == reco){
						r.recommendation = newReco
					}
					if(r.commonNameReco == reco){
						r.commonNameReco = newReco
					}
					if(!r.save(flush:true)){
						r.errors.allErrors.each { log.error it }
					}
				}
				Observation.findAllByMaxVotedReco(reco).each { obv ->
					obv.maxVotedReco = newReco
					if(!obv.save(flush:true)){
						obv.errors.allErrors.each { log.error it }
					}
					
				}
				
				println "========= deleting reco " + reco
				reco.delete(flush:true)
			}
			//XXX: remove entry from old synonym table after table drop remove this code
			Synonyms.findAllByTaxonConcept(oldName).each {s ->
			   		s.delete(flush:true)
		    }
			SpeciesPermission.findAllByTaxonConcept(oldName).each { sp ->
				sp.delete(flush:true)
			}
			println "========= old name " + oldName
			if(oldName)
				oldName.delete(flush:true)
		}else{
			oldName.isDeleted = true
			println "======= for delete " + oldName
			if(!oldName.save(flush:true)){
				oldName.errors.allErrors.each { log.error it }
			}
		}
		}
		
	}
	
	
	private boolean isDuplicateTr(tr, newPath, newName, deleteToParentTaxonMap ){
		//update all paths for this taxon defintion
		def id = newName.id
		List trList = TaxonomyRegistry.findAllByPathLike('%_' + id + '_%')
		trList.addAll(TaxonomyRegistry.findAllByPathLike(id + '_%'))
		trList.addAll(TaxonomyRegistry.findAllByPath(id))
		trList.addAll(TaxonomyRegistry.findAllByTaxonDefinition(newName))
		trList.unique()
		
		boolean isDuplicate = false
		
		trList.each { nTr ->
			
				if((nTr.classification == tr.classification) && (nTr.path == newPath) ){
					isDuplicate = true
					println "----------- duplicate tr " + nTr
					deleteToParentTaxonMap.put(tr, nTr)
				}
				
		}
		return isDuplicate
	}	
	
	/////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// SNAPPING RAW NAMES /////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	
	public addIBPHirToRawNames() {
		int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(5000);
		
		Date startDate = new Date();
		List hirList = [Classification.findByName('IUCN Taxonomy Hierarchy (2010)'), Classification.findByName("Author Contributed Taxonomy Hierarchy"), Classification.findByName("FishBase Taxonomy Hierarchy"), Classification.findByName("GBIF Taxonomy Hierarchy")]
		def ibp_classifi = Classification.findByName("IBP Taxonomy Hierarchy");
		def admin = SUser.read(1L);
		
		def taxons;
		def query  = ''' select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW' and is_deleted = false order by rank, id '''
		def sql =  Sql.newInstance(dataSource);
		
		int failCount = 0
		int i = 0
		int offset = 0
		int limit = 100
		while(true){
			if(i%20 == 0){
				println "-- Count  " + i + " failed Count " + failCount
			}
			
			String q = query + " limit " + limit + " offset " + offset
			taxons = sql.rows(q)
			if(taxons.isEmpty())
				break
			
			TaxonomyRegistry.withNewTransaction {	
			taxons.each { t ->
				i++
				TaxonomyDefinition td = TaxonomyDefinition.get(t.id)
				boolean isSnapped = td.snapToIBPHir(hirList, ibp_classifi)
				if(!isSnapped){
					failCount ++
					println "failed for " +  td 
				}
			}
			}
			utilsService.cleanUpGorm()
			offset = offset + limit;
			
		}
		dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
		println "Failed count " + failCount   + "  Total time  " + ((new Date()).getTime() - startDate.getTime())/1000;
	}

	
}
