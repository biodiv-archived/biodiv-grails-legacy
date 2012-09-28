package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams

import species.CommonNames
import species.Habitat;
import species.NamesParser
import species.Synonyms
import species.TaxonomyDefinition
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote.ConfidenceType;

class ObservationsSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 50;

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to observations search index"
		
		//TODO: change limit
		int limit = Observation.count()+1, offset = 0;
		
		def observations;
		def startTime = System.currentTimeMillis()
		while(true) {
			observations = Observation.list(max:limit, offset:offset);
			if(!observations) break;
			publishSearchIndex(observations, true);
			observations.clear();
			offset += limit;
		}
		
		log.info "Time taken to publish observations search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(Observation obv, boolean commit) {
		return publishSearchIndex([obv], commit);
	}
	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Observation> obvs, boolean commit) {
		if(!obvs) return;
		log.info "Initializing publishing to observations search index : "+obvs.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]

		obvs.each { obv ->
			log.debug "Reading Observation : "+obv.id;
			if(!obv.isDeleted) {
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, obv.id.toString());
				addNameToDoc(obv, doc);
	
				doc.addField(searchFieldsConfig.AUTHOR, obv.author.name);
				doc.addField(searchFieldsConfig.CONTRIBUTOR, obv.author.name);
				doc.addField(searchFieldsConfig.OBSERVED_ON, obv.observedOn);
				doc.addField(searchFieldsConfig.UPLOADED_ON, obv.createdOn);
				doc.addField(searchFieldsConfig.UPDATED_ON, obv.lastRevised);
				if(obv.notes) {
					doc.addField(searchFieldsConfig.MESSAGE, obv.notes);
				}
				
				doc.addField(searchFieldsConfig.SGROUP, obv.group.id);			
				doc.addField(searchFieldsConfig.HABITAT, obv.habitat.id);
				doc.addField(searchFieldsConfig.LOCATION, obv.placeName);
				doc.addField(searchFieldsConfig.LOCATION, obv.reverseGeocodedName);
				doc.addField(searchFieldsConfig.ISFLAGGED, (obv.flagCount > 0));
				doc.addField(searchFieldsConfig.LATLONG, obv.latitude+","+obv.longitude);
				//boolean geoPrivacy = false;
				//String locationAccuracy;
				obv.tags.each { tag ->
					doc.addField(searchFieldsConfig.TAG, tag);
				}
					
				docs.add(doc);
			}
		}

		//log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to observations solr core"
			}
		} catch(SolrServerException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param doc
	 * @param name
	 */
	private void addNameToDoc(Observation obv, SolrInputDocument doc) {
		
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
		doc.addField(searchFieldsConfig.MAX_VOTED_SPECIES_NAME, obv.fetchSpeciesCall());
		def distRecoVotes = obv.recommendationVote.unique { it.recommendation };  
		distRecoVotes.each { vote ->
			doc.addField(searchFieldsConfig.NAME, vote.recommendation.name);
			doc.addField(searchFieldsConfig.CONTRIBUTOR, vote.author.name);
			if(vote.recommendation.taxonConcept)
				doc.addField(searchFieldsConfig.CANONICAL_NAME, vote.recommendation.taxonConcept.canonicalForm);
		}
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def search(query) {
		def params = SolrParams.toSolrParams(query);
		log.info "Running observation search query : "+params
		return solrServer.query( params );
	}

	/**
	* delete requires an immediate commit
	* @return
	*/
   def delete(long id) {
	   log.info "Deleting observation from search index"
	   solrServer.deleteByQuery("id:${id}");
	   solrServer.commit();
   }
   
	/**
	 * 
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting observation search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 * 
	 * @return
	 */
	def optimize() {
		log.info "Optimizing observation search index"
		solrServer.optimize();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def terms(query) {
		def field = query?.field?:"autocomplete";
		SolrParams q = new SolrQuery().setQueryType("/terms")
				.set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
				.set(TermsParams.TERMS_LOWER, query.term)
				.set(TermsParams.TERMS_LOWER_INCLUSIVE, false)
				.set(TermsParams.TERMS_REGEXP_STR, query.term+".*")
				.set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
				.set(TermsParams.TERMS_RAW, true);
		log.info "Running observation search query : "+q
		return solrServer.query( q );
	}
}
