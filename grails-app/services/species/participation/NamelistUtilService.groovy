package species.participation

import org.apache.commons.logging.LogFactory;

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
	
	private static final String LOG_PREFIX = "------------ NAME LIST --------- "

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
		Utils.saveFiles(new File(grailsApplication.config..speciesPortal.namelist.rootDir), taxons, [])
	}
	
	public updateIBPNameWithCol(ScientificName sn, String colId){
		if(!sn || !colId)
			return
	
		Map match = getColDataFromXml(sn, colId)
		if(match){
			namelistService.processDataForMigration(sn, match, 1)
		}else{
			log.debug LOG_PREFIX + " Either match not found or name is already in working list "
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
			log.debug LOG_PREFIX + " Match foud based on given colId " + acceptedMatch
		}
		
		return acceptedMatch
	}
	
	private List getColDataFromXml(ScientificName sn){
		String taxCan = sn.canonicalForm.replaceAll(' ', '_');
		return namelistService.processColData(new File(new File(grailsApplication.config..speciesPortal.namelist.rootDir), taxCan +'.xml'));
	}

	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////  MERGE ACCEPTED NAME  ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public mergeAcceptedName(long oldId, long newId, boolean fullDelete = false){
		TaxonomyDefinition oldName = TaxonomyDefinition.get(oldId)
		TaxonomyDefinition newName = TaxonomyDefinition.get(newId)
		
		
		if(!oldName ||  !newName){
			log.debug "One of name is not exist in the system " + oldId + "  " +  newId
			return
		}
		
		
		
		def oldTrList = TaxonomyRegistry.findAllByTaxonDefinition(oldName)
		
		
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
			if(!tr.save(flush:true)){
				tr.errors.allErrors.each { log.error it }
			}
		}
		println "------------ oldTrToBeDeleted " + oldTrToBeDeleted
		oldTrToBeDeleted.each {TaxonomyRegistry tr ->
			println "================ deleting tr " + tr
			def tmpTr = deleteToParentTaxonMap.get(tr)
			TaxonomyRegistry.findAllByParentTaxon(tr).each{
				it.parentTaxon = tmpTr
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
			println "========= deleting reco " + reco
			reco.delete(flush:true)
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
	
}
