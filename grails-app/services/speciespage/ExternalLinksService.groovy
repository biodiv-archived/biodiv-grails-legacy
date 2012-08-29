package speciespage

import org.hibernate.exception.ConstraintViolationException;

import species.ExternalLinks;
import species.Species;
import species.TaxonomyDefinition.TaxonomyRank;
import grails.converters.JSON;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.ContentType;
import groovyx.net.http.Method;
import species.TaxonomyDefinition;

class ExternalLinksService {

	static transactional = false

	static int BATCH_SIZE = 20;

	def sessionFactory;

	def updateExternalLinks() {
		int limit = BATCH_SIZE;
		int offset = 0;
		int noOfUpdations = 0;
		int noOfFailures = 0;
		
		while(true) {

			def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonomyDefinition where taxonomyDefinition.rank = :speciesTaxonRank  order by taxonomyDefinition.id",[speciesTaxonRank:TaxonomyRank.SPECIES.ordinal()],[max:limit, offset:offset]);
			
			if(!taxonConcepts) break;
			
			taxonConcepts.eachWithIndex { taxonConcept, index ->
				if(!taxonConcept.externalLinks?.eolId && updateExternalLinks(taxonConcept)) {
					noOfUpdations ++;
				} else {
					noOfFailures++;
				}	
			}
			log.info "Updated external links for taxonConcepts ${noOfUpdations}"
			cleanUpGorm();
			offset += limit;
		}
		if(noOfUpdations) {
			cleanUpGorm();
		}
		log.info "Updated external links for taxonConcepts ${noOfUpdations} in total"
		return noOfUpdations;
	}

	/**
	 * 
	 * @param taxonConcept
	 * @return
	 */
	boolean updateExternalLinks(TaxonomyDefinition taxonConcept) {
		def http = new HTTPBuilder();

		updateEOLId(http, taxonConcept);
		if(taxonConcept.externalLinks?.eolId) {
			updateOtherIdsFromEOL(http, taxonConcept.externalLinks?.eolId, taxonConcept);
		}

		taxonConcept = taxonConcept.merge();
		if(!taxonConcept.save()) {
			taxonConcept.errors.each { log.error it};
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param taxonConcept
	 * @param idType
	 * @param idVal
	 * @param persist
	 * @return
	 */
	def updateExternalLink(TaxonomyDefinition taxonConcept, String idType, String idVal, boolean persist) {
		return 	updateExternalLink(taxonConcept, idType, idVal, persist, null);
	}

	/**
	 * 
	 * @param taxonConcept
	 * @param idType
	 * @param idVal
	 * @param persist
	 * @param eolFetchDate
	 * @return
	 */
	def updateExternalLink(TaxonomyDefinition taxonConcept, String idType, String idVal, boolean persist, Date eolFetchDate) {

		if(!taxonConcept.externalLinks) {
			taxonConcept.externalLinks = new ExternalLinks(noOfDataObjects : 0);
		}

		taxonConcept.externalLinks.eolFetchDate = eolFetchDate;

		switch (idType) {
			case "eol" :
				log.debug "Setting eol link id to : "+idVal
				taxonConcept.externalLinks.eolId = idVal;
				break;
			case "iucn" :
				log.debug "Setting iucn link id to : "+idVal
				taxonConcept.externalLinks.iucnId = idVal;
				break;
			case "gbif" :
				log.debug "Setting gbif link id to : "+idVal
				taxonConcept.externalLinks.gbifId = idVal;
				break;
			case "col" :
				log.debug "Setting col link id to : "+idVal
				taxonConcept.externalLinks.colId = idVal;
				break;
			case "itis" :
				log.debug "Setting itis link id to : "+idVal
				taxonConcept.externalLinks.itisId = idVal;
				break;
			case "ncbi" :
				log.debug "Setting ncbi link id to : "+idVal
				taxonConcept.externalLinks.ncbiId = idVal;
				break;
		}


		if(persist && !taxonConcept.save()) {
			taxonConcept.errors.each { log.error it};
		}
	}

	/**
	 * TODO:optimize withAsync HTTPBuilder and threads
	 * @param http
	 * @param taxonConcept
	 * @return
	 */
	private boolean updateEOLId(HTTPBuilder http, TaxonomyDefinition taxonConcept) {
		log.debug "Fetching EOL ID for taxon : "+taxonConcept

		http.request( "http://eol.org/api/search/1.0" , Method.GET, ContentType.JSON) {
			uri.path = taxonConcept.canonicalForm+'.json'
			uri.query = [ exact:1 ]
			response.success = { resp, json ->
				if(resp.isSuccess()) {
					log.debug "EOL search result for : "+json
					if(json.results) {
						updateExternalLink(taxonConcept, "eol", String.valueOf(json.results[0].id), false, new Date());
					}
				}
			}
			response.failure = { resp ->  log.error 'EOL search request failed for taxon : '+taxonConcept }
		}

		return (taxonConcept.externalLinks?.eolId)
	}


	/**
	 *
	 * @param eolId
	 * @return
	 */
	private void updateOtherIdsFromEOL(HTTPBuilder http, String eolId, TaxonomyDefinition taxonConcept) {
		log.debug "Fetching EOL ID for taxon : "+taxonConcept


		http.request( "http://eol.org/api/pages/1.0" , Method.GET, ContentType.JSON) {
			uri.path = eolId + '.json'
			uri.query = [ common_names:1, details:1, subjects:'all', text:2 ]
			response.success = { resp, json ->
				if(resp.isSuccess()) {
					json.taxonConcepts.each { r ->
						log.debug r;
						switch(r.nameAccordingTo) {
							case "Species 2000 & ITIS Catalogue of Life: Annual Checklist 2010":
								updateExternalLink(taxonConcept, "col", r.sourceIdentfier, false, new Date());
								break;
							case "Integrated Taxonomic Information System (ITIS)":
								updateExternalLink(taxonConcept, "itis", r.sourceIdentfier, false, new Date());
								break;
							case "IUCN Red List (Species Assessed for Global Conservation)":
								if(!taxonConcept.externalLinks?.iucnId)
									updateExternalLink(taxonConcept, "iucn", r.sourceIdentfier, false, new Date());
								break;
							case "NCBI Taxonomy":
								updateExternalLink(taxonConcept, "ncbi", r.sourceIdentfier, false, new Date());
								break;
						}
					}
					log.debug "No of data objects  : "+json.dataObjects.size();
					taxonConcept.externalLinks.noOfDataObjects = json.dataObjects.size();
				}
			}
			response.failure = { resp ->  log.error 'EOL page request failed for eolId : '+eolId }
		}
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
