package speciespage

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.exception.ConstraintViolationException;

import species.CommonNames;
import species.Synonyms;
import species.TaxonomyDefinition;
import species.participation.Recommendation;
import species.utils.Utils;

class RecommendationService {

	def grailsApplication
	def sessionFactory

	def namesIndexerService;

	static transactional = false
	static int BATCH_SIZE = 20

	/**
	 * TODO:Bind this call to recommendation domain object save
	 * as a recommendation can be saved with out being indexed
	 * @param reco
	 * @return
	 */
	boolean save(Recommendation reco) {
		def dupReco = searchReco(reco.name, reco.isScientificName, reco.languageId, reco.taxonConcept)
		if(dupReco){
			log.debug "Same reco found in database so igonoring save $reco"
			return true
		}
		
		def flushImmediately  = grailsApplication.config.speciesPortal.flushImmediately
		if(reco.save(flush:flushImmediately)) {
			log.debug "createing new recommendation $reco"
			//XXX uncomment this
			namesIndexerService.add(reco);
			return true;
		}
		log.error "Error saving recommendation"
		reco.errors.allErrors.each { log.error it }
		return false;	
	}

	/**
	 *
	 * @param recos
	 * @return
	 */
	int save(List<Recommendation> recos) {
		log.info "Saving recos : "+recos.size()

		int noOfRecords = 0;
		def startTime = System.currentTimeMillis()
		recos.eachWithIndex { Recommendation reco, index ->
			def dupReco = searchReco(reco.name, reco.isScientificName, reco.languageId, reco.taxonConcept)
			if(dupReco){
				log.debug "Same reco found in database so igonoring save $reco"
			}else{
				if(reco.save()) {
					noOfRecords++;
					namesIndexerService.add(reco);
				} else {
					reco.errors.allErrors.each { log.error it }
					log.error "Coundn't save the recommendation : "+reco				
				}
				if (index % BATCH_SIZE == 0) {
					log.debug "Persisted ${index} recommendations"
					cleanUpGorm();
				}
			}
		}
		if(noOfRecords) {
			def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
			namesIndexerService.store(indexStoreDir);
			cleanUpGorm();
		}
		log.info "Time taken to save : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"
		log.info "Persisted ${noOfRecords} recommendations in total"
		return noOfRecords;
	}

	/**
	 * 
	 * @param oldReco
	 * @param reco
	 * @return
	 */
	boolean update(Recommendation oldReco, Recommendation reco) {
		if(reco.save(flush:true)) {
			namesIndexerService.update(oldReco, reco);
			return true;
		}
		log.error "Error updating recomendation"
		reco.errors.allErrors.each { log.error it }
		return false;
	}

	/**
	 * 
	 * @param reco
	 * @return
	 */
	boolean delete(Recommendation reco) {
		if(reco.delete(flush:true)) {
			namesIndexerService.delete(reco);
			return true;
		}
		log.error "Error saving recomendation"
		reco.errors.allErrors.each { log.error it }
		return false;
	}

	/**
	 * 
	 * @return
	 */
	def deleteAll() {
		namesIndexerService.setDirty(true);
		return Recommendation.executeUpdate("delete Recommendation r");
	}

	/**
	 * 
	 */
	private void cleanUpGorm() {
		def hibSession = sessionFactory?.getCurrentSession()
		if(hibSession) {
			log.debug "Flushing and clearing session"
			try {
				hibSession.flush()
			} catch(ConstraintViolationException e) {
				e.printStackTrace()
			}
			hibSession.clear()
		}
	}
	
	
	public Recommendation getRecoForScientificName(String recoName, String canonicalName, Recommendation commonNameReco){
		def reco, taxonConcept;
		//
		
		//first searching by canonical name. this name is present if user select from auto suggest
		if(canonicalName && (canonicalName.trim() != "")){
			return findReco(canonicalName, true, null, taxonConcept);
		}
		
		//searching on whatever user typed in scientific name text box
		if(recoName) {
			return findReco(recoName, true, null, taxonConcept);
		}
		
		//it may possible certain common name may point to species id in that case getting the SN for it
		if(commonNameReco && commonNameReco.taxonConcept){
			TaxonomyDefinition taxOnConcept = commonNameReco.taxonConcept
			return findReco(taxOnConcept.canonicalForm, true, null, taxOnConcept)
		}
		
		return null;
	}
	
	public Recommendation findReco(name, isScientificName, languageId, taxonConceptForNewReco){
		if(name){
			// if sn then only sending to names parser for common name only cleaning
			//XXX setting language id null for scientific name
			if(isScientificName){
				name = Utils.getCanonicalForm(name);
				languageId = null
			}else{
				//converting common name to title case
				name = Utils.getTitleCase(Utils.cleanName(name));
			}
			def reco = searchReco(name, isScientificName, languageId, taxonConceptForNewReco)
			if(!reco) {
				reco = new Recommendation(name:name, taxonConcept:taxonConceptForNewReco, isScientificName:isScientificName, languageId:languageId);
				if(!save(reco)) {
					reco = null;
				}
			}
			return reco;
		}
		return null;
	}
	
	private Recommendation searchReco(name, isScientificName, languageId, taxonConcept){
		def c = Recommendation.createCriteria();
		def recoList = c.list {
			ilike('name', name);
			eq('isScientificName', isScientificName);
			(languageId) ? eq('languageId', languageId) : isNull('languageId');
			if(taxonConcept){
				eq('taxonConcept', taxonConcept)
			}
		}
		
		if(!recoList || recoList.isEmpty()){
			return null
		}
		
		// giving priority to reco which has taxon concept
		recoList.each { Recommendation r ->
			if(r.taxonConcept){
				return r
			}
		}
		
		//no reco with taxon concept found so returning first one
		return recoList[0]
	}
}
