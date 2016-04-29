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
import species.Synonyms


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
			namelistService.processDataForMigration(new TaxonomyDefinition(), match, 1, true, false)
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
		def ibpHier = Classification.fetchIBPClassification()
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
	
	
	public void generateSheetMultipleIBPNameList(File f){
		def oldTimeOut = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(50000);
		def sql =  Sql.newInstance(dataSource);
		def query  = "select taxon_definition_id as id, classification_id, count(*) as c from taxonomy_registry  where  classification_id = 265799 group by taxon_definition_id, classification_id having count(*) > 1;"
		int i = 0
		int offset = 0
		int limit = 1000
		def ibpHier = Classification.fetchIBPClassification()
		f.withWriter { out ->
			out.println "id|name|rank|colId|paths|idpath"
			//while(true){
				def resList = sql.rows(query)
					
				resList.each{
					TaxonomyDefinition tdf = TaxonomyDefinition.get(it.getProperty("id"))
					def colIdPaths = []
					def idPaths = []
					def trs = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(tdf, ibpHier)
					trs.each { tr ->
						out.println tdf.id + "|" + tdf.name + "|" + tdf.rank + "|" + tdf.matchId + "|" + tr.path + "|" + (tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("_"))
						//colIdPaths << (tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("_"))
						//idPaths << tr.path
					}
					//out.println tdf.id + "|" + tdf.name + "|" + tdf.rank + "|" + tdf.matchId + "|" + idPaths.join('#') + "|" + colIdPaths.join('#')
				}
				utilsService.cleanUpGorm()
				offset += limit
				println " new offset " + offset
			//}
			
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
			
			//TaxonomyRegistry.withNewTransaction {
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
			//}
			utilsService.cleanUpGorm()
			pMap.clear()
			offset += limit
			println "map size " + pMap.size()  + " new offset " + offset
		}
			
		dataSource.setUnreturnedConnectionTimeout(oldTimeOut); 
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////  MERGE NAMES  ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean changeAccToSyn(long oldId, long newId){
		ScientificName oldName = ScientificName.get(oldId)
		ScientificName newName = ScientificName.get(newId)
		
		if(oldName && (oldName.status == NamesMetadata.NameStatus.SYNONYM)){
			log.debug "Name is already synonym... nothing to do... RETURNING " + oldId 
			return true
		}
		
		if(!oldName || !newName){
			log.debug "Null id is given for the names  old id " + oldId + " new name " + newName
			return false
		}
		
		if(newName.status != NamesMetadata.NameStatus.ACCEPTED){
			log.debug "Status of new name  is not acceepted aborting... new name id " + newId
			return false
		}
		
		_mergeAccepted(oldName, newName, false)
		//cache is cleared so reading name again
		oldName.refresh()// = ScientificName.get(oldId)
		
		updateStatusAndClass(oldName,  NamesMetadata.NameStatus.SYNONYM)
		oldName.refresh()
		
		//adding this synonym to new accepted name
		newName.refresh()// = ScientificName.get(newId)
		newName.addSynonym(oldName)
		return true
	}
	
	public boolean changeSynToAcc(long oldId, Map hirMap=null){
		ScientificName oldName = ScientificName.get(oldId)
		
		if(!oldName){
			log.debug "Null id is given for the names  old id " + oldId
			return false
		}
		
		if(oldName && (oldName.status == NamesMetadata.NameStatus.ACCEPTED)){
			log.debug "Name is already accepted... nothing to do... RETURNING " + oldId
			return true
		}
		
		//removing this name as synonym from all other accepted name
		oldName.removeAsSynonym()
		updateStatusAndClass(oldName,  NamesMetadata.NameStatus.ACCEPTED)
		
		//TODO have to add ibp hir fir this changed name
		return true
	}
	
	
	public boolean deleteName(long id){
		ScientificName name = ScientificName.get(id)
		if(!name || name.isDeleted){
			log.debug "Null id is given for the name or name is already deleted... ABORTING " + id
			return false
		}
		
		if(name.status == NamesMetadata.NameStatus.SYNONYM){
			return mergeSynonym(name, null)
		}
		
		//its an accepted name
		Classification ibpClassification = Classification.fetchIBPClassification()
		int tCount = TaxonomyRegistry.countByParentTaxonDefinitionAndClassification(name, ibpClassification)
		if(tCount > 0){
			log.debug "Given name has children in ibp classification... Aborting delete ... child count  " +  tCount
			return false
		}
		
		//every thing passed now deleting the accepted name
		return _mergeAccepted(name, null)
	} 
	
	
	public boolean mergeNames(long oldId, long newId){
		ScientificName oldName = ScientificName.get(oldId)
		ScientificName newName = ScientificName.get(newId)
		
		if(!oldName || !newName){
			log.debug "Null id is given for the names  old id " + oldId + " new name " + newName
			return false
		}
		
		
		if(oldName.status != newName.status){
			log.debug "Status is not same for  the name " + oldName.status + " new name " + newName.status
			return false
		}
		
		if(oldName.status == NamesMetadata.NameStatus.SYNONYM){
			return mergeSynonym(oldName, newName)
		}
		
		//status is accepted 
		return mergeAccepted(oldName, newName)
		
	}
	
	
	private void updateStatusAndClass(ScientificName sciName, NameStatus status) {
		Map m = [id:sciName.id]
		m['class'] = (status == NameStatus.SYNONYM) ? SynonymsMerged.class.canonicalName: TaxonomyDefinition.class.canonicalName
		m['relationship'] = (status == NameStatus.SYNONYM)? ScientificName.RelationShip.SYNONYM.toString():null
		m['status'] = status.toString()
		String query = "update taxonomy_definition set (class, status, relationship) = (:class, :status, :relationship) where id = :id";
		Sql sql = sessionFactory.getCurrentSession().createSQLQuery(query)
		sql.setProperties(m).executeUpdate()
	}
	
	private boolean mergeAccepted(oldName, newName){
		if(oldName.rank != newName.rank){
			log.debug "Rank is not same... Aborting merge " + oldName.rank + "  new name " + newName.rank
			return false
		 }
		
		
		if(oldName.fetchRootId() != newName.fetchRootId()){
			log.debug "Kingdom is not same... Aborting merge " + oldName.fetchRootId() + "  new name " + newName.fetchRootId()
			return false
		 }
		
		log.debug "All validation passed Merging names..." + oldName + " new name " + newName
		
		return _mergeAccepted(oldName, newName)
	}

	private boolean _mergeAccepted(oldName, newName, boolean flagAsDelete = true) {
		//moving synonym, common names and document sci name
		moveOtherNamesReference(oldName, newName)

		//move recommendation and observation
		moveRecoAndObv(oldName, newName)

		//move species content and resources
		moveSpeciesContent(oldName, newName)


		//flag the name as deleted
		if(flagAsDelete){
			oldName.isDeleted = true
			log.debug"======= for delete " + oldName
			if(!oldName.save(flush:true)){
				oldName.errors.allErrors.each { log.error it }
				return false
			}
		}

		//moving existing taxon reg and deleteing ibp hir
		moveTaxonReg(oldName, newName)

		utilsService.clearCache("defaultCache")

		return true
	}
	
	
	
	private boolean mergeSynonym(oldName, newName){
		//moving synonym
		def oldEntries = AcceptedSynonym.findAllBySynonym(oldName);
		oldEntries.each { e ->
			TaxonomyDefinition acName = e.accepted
			acName.removeSynonym(oldName)
			if(newName){
				acName.addSynonym(newName)
			}
		}
		
		moveSpeciesContent(oldName, newName)
		oldName.isDeleted = true
		println "======= for delete " + oldName
		if(!oldName.save(flush:true)){
			oldName.errors.allErrors.each { log.error it }
			return false
		}
		
		return true
	}


	private moveSpeciesContent(oldName, newName){
		Species oldSpecies = Species.get(oldName.findSpeciesId())
		if(!oldSpecies){
			log.debug "Species page is not there... nothing to move " + oldName
			return
		}
		
		//old species page is there but new name is not given which means want to delete species content
		if(!newName){
			oldSpecies.deleteSpecies(null)
			return
		}
		
		Species newSpecies = Species.get(newName.findSpeciesId())
		
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
			ResourceRedirect.addLink(oldSpecies, newSpecies)
			
			//saving new species
			if(!newSpecies.save(flush:true)){
				newSpecies.errors.allErrors.each { log.error it }
			}
			
			//deleting speices
			oldSpecies.deleteSpecies(null)
		}
	}
	
	
	private moveRecoAndObv(oldName, newName){
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
	}
	
	private moveOtherNamesReference(oldName, newName){
		//moving synonym
		oldName.fetchSynonyms().each {
			log.debug "================ synonym move " + it
			oldName.removeSynonym(it)
			if(newName) newName.addSynonym(it)
			
		}
		
		//moving common names
		List cns = CommonNames.findAllByTaxonConcept(oldName)
		cns.each { CommonNames cn ->
			log.debug "================ common name update " + cn
			if(newName)
				cn.taxonConcept = newName
			else
				cn.isDeleted = true
				
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
		
		//moving docsciname
		List dcs = DocSciName.findAllByTaxonConcept(oldName)
		dcs.each {DocSciName  cn ->
			log.debug "================ Doc sci name " + cn
			if(newName)
				cn.taxonConcept = newName
			else
				cn.isDeleted = true
				
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
	}
	
	//moving children and deleting ibp hir
	private moveTaxonReg(oldName, newName){
		Classification ibpClassification = Classification.fetchIBPClassification()
		TaxonomyRegistry tr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(oldName, ibpClassification)
		if(!tr){
			log.debug "Does not have ibp hir so not moving any thing " + oldName
			return
		}
		//updating new parent of all the children of old name
		if(newName){
			TaxonomyRegistry ntr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(newName, ibpClassification)
			TaxonomyRegistry.executeUpdate( "update TaxonomyRegistry set parentTaxon = :newParentTaxon, parentTaxonDefinition = :newParentTaxonDef where parentTaxon = :oldParentTaxon", [newParentTaxon:ntr, newParentTaxonDef :newName, oldParentTaxon:tr])
//			TaxonomyRegistry.findAllByParentTaxon(tr).each { TaxonomyRegistry t ->
//				t.parentTaxon = ntr
//				t.parentTaxonDefinition = newName
//				if(!t.save(flush:true)){
//					t.errors.allErrors.each { log.error it }
//			   }
//			}
		}
		
		//deleteing ibp hir of old name
		if(!tr.delete(flush:true)){
			tr.errors.allErrors.each { log.error it }
		}
		
		//moving all children to new ibp path
		if(newName){
			List sqlStrings = newName.fetchUpdateTaxonRegSql(oldName.id)
			Sql sql = new Sql(dataSource)
			sqlStrings.each { String s ->
			log.debug " Path update query " + s
				try{
					int updateCount = sql.executeUpdate(s);
					log.debug " updated path count  " + 	updateCount
				}catch(e){
					e.printStackTrace()
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
		def ibp_classifi = Classification.fetchIBPClassification()
		def admin = SUser.read(1L);
		
		def taxons;
		def query  = ''' select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW' and is_deleted = false and rank = '''
		//def query = ''' select id from taxonomy_definition where id not in (select distinct(taxon_definition_id) from taxonomy_registry where classification_id = 265799 and taxon_definition_id in (select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW')) and status = 'ACCEPTED' and position = 'RAW'  order by rank, name '''
	
		def sql =  Sql.newInstance(dataSource);
		
		int failCount = 0
		int i = 0
		for(int r = 0; r <= 10 ; r++){
			int offset = 0
			int limit = 100
			while(true){
				if(i%20 == 0){
					println "-- Count  " + i + " failed Count " + failCount
				}
				
				String q = query + r + " order by rank, id  limit " + limit + " offset " + offset
				taxons = sql.rows(q)
				if(taxons.isEmpty())
					break
				
				//TaxonomyRegistry.withNewTransaction {	
				taxons.each { t ->
					i++
					TaxonomyDefinition td = TaxonomyDefinition.get(t.id)
					println "--------------------------- starting " + td 
					boolean isSnapped = td.snapToIBPHir(hirList, ibp_classifi)
					if(!isSnapped){
						failCount ++
						println "failed for " +  td 
					}
				}
				//}
				utilsService.cleanUpGorm()
				offset = offset + limit;
				
			}
		}
		dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
		println "Failed count " + failCount   + "  Total time  " + ((new Date()).getTime() - startDate.getTime())/1000;
	}

	/////////////////////////////////////////////////////////////////////////////////
	////////////////// Create name from Spreadsheet and add IBP hir /////////////////
	/////////////////////////////////////////////////////////////////////////////////
	

	public TaxonomyDefinition createName(String name, String rank, String status, String position){
		return createName(name, rank.trim().toInteger(), NameStatus.getEnum(status), NamePosition.getEnum(position))
	}
	
	public TaxonomyDefinition createName(String name, int rank, NameStatus status, NamePosition position){
		def parsedNames =  new NamesParser().parse([name]);
		if(!parsedNames[0]?.canonicalForm) {
			println "Not able to parse " + name
			return
		}
		
		TaxonomyDefinition td = new TaxonomyDefinition()
		
		td.normalizedForm = parsedNames[0].normalizedForm;
		td.italicisedForm = parsedNames[0].italicisedForm;
		td.binomialForm = parsedNames[0].binomialForm;
		td.canonicalForm = parsedNames[0].canonicalForm
		td.name = name
		
		List<TaxonomyDefinition> tds = NamelistService.searchIBP(td.canonicalForm, null, status, rank)
		if(!tds.isEmpty()){
			println "Name alreay exist in system " + tds
			return tds[0]
		}
		
		td.status = status
		td.position = position
		td.rank = rank
		
		if(!td.save(flush:true)) {
			td.errors.each { log.debug it }
		}
		
		return td
	}
		
	public TaxonomyRegistry saveIBPHir(TaxonomyDefinition td, Long parentId){
		Classification ibpHir = Classification.fetchIBPClassification()
		TaxonomyDefinition  pTd = TaxonomyDefinition.read(parentId)
		
		if(!td || !pTd)
			return 
		
		TaxonomyRegistry ibpTr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(td, ibpHir)
		if(ibpTr){
			println "Already have on ibp hir " + ibpTr
			return ibpTr
		}
		
		ibpTr =	new TaxonomyRegistry()
		if(pTd.status == NameStatus.SYNONYM){
			pTd = AcceptedSynonym.fetchAcceptedNames(pTd)[0]
		}
		
		TaxonomyRegistry pIbpTr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(pTd, ibpHir)
		if(!pIbpTr){
			println "Parent do not have ibp hir " + pTd + "  for name " +  td
			return
		}
		
		ibpTr.properties = pIbpTr.properties
		ibpTr.parentTaxonDefinition = pIbpTr.taxonDefinition
		ibpTr.taxonDefinition = td
		ibpTr.parentTaxon = pIbpTr
		ibpTr.contributors = null;
		ibpTr.path = pIbpTr.path + "_" + td.id
		
		if(!ibpTr.save(flush:true)){
			ibpTr.errors.allErrors.each { println  it }
		}
		
		return ibpTr
	}
	
	//migrate synonyms for accepted raw names
	public migrateSynonymForRawNames(Map replaceMap=[:] ){
		NamesParser nameParser = new NamesParser()
		int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(5000);
		
		def query  = '''select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW' order by rank, id '''
		def sql =  Sql.newInstance(dataSource);
		
		int failCount = 0
		int i = 0
		def createList = []
		int offset = 0
		int limit = 100
		while(true){
			String q = query + " limit " + limit + " offset " + offset
			def res = sql.rows(q)
			if(res.isEmpty()){
				break
			}
			
			res.each { r ->
				i++
				if(i%50 == 0){
					println "============= count " + i
				}
				
				TaxonomyDefinition  pTd = TaxonomyDefinition.read(r.id)
				Synonyms.findAllByTaxonConcept(pTd).each { Synonyms syn ->
					SynonymsMerged td 
					def replaceId = replaceMap.get(syn.id)
					if(replaceId){
						println "============= reusing id give by map " + replaceId
						td = SynonymsMerged.get(replaceId)
						AcceptedSynonym.createEntry(pTd, td)
						return
					}
					
					def tds = NamelistService.searchIBP(syn.name, null, NamesMetadata.NameStatus.ACCEPTED)
					if(!tds.isEmpty()){
						println "Name is accpeted in new system leaving out " + tds
						return 
					}
					tds = NamelistService.searchIBP(syn.name, null, NamesMetadata.NameStatus.SYNONYM)
					if(!tds.isEmpty()){
						println "Name is synonym in new system so reusing " + tds
						td = tds[0]
						AcceptedSynonym.createEntry(pTd, td)
					}
					else{
						if(!syn.name){
							createList << syn.id
							println "name is null failed for synonym " + syn
							return
						}
						def parsedNames =  nameParser.parse([syn.name]);
						if(!parsedNames[0]?.canonicalForm) {
							println "Not able to parse " + syn.name
							createList << syn.id
							return
						}
						
						
						td = new SynonymsMerged()
						td.properties = syn.properties
						
						td.normalizedForm = parsedNames[0].normalizedForm;
						td.italicisedForm = parsedNames[0].italicisedForm;
						td.binomialForm = parsedNames[0].binomialForm;
						td.canonicalForm = parsedNames[0].canonicalForm
						td.name = syn.name
						
						td.status = NamesMetadata.NameStatus.SYNONYM
						td.position = NamesMetadata.NamePosition.RAW
						td.rank = pTd.rank
						td.contributors = null
						td.curators = null
		
						
						if(!td.save(flush:true)){
							td.errors.allErrors.each { println  it }
						}
						
						AcceptedSynonym.createEntry(pTd, td)
					}
				}
			
			}
			utilsService.cleanUpGorm()
			offset = offset + limit;
			
		}
		
		println "-----------------------failed id list==============" + createList.size()
		println createList.join(", ")
		println "-------------------------------"
	}
}
