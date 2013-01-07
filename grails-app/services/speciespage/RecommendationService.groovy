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
		def flushImmediately  = grailsApplication.config.speciesPortal.flushImmediately
		if(reco.save(flush:flushImmediately)) {
			//XXX uncomment this
			//namesIndexerService.add(reco);
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
}
