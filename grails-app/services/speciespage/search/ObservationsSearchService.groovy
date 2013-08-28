package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrException
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
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;

class ObservationsSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 10;

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to observations search index"
		
		//TODO: change limit
		int limit = BATCH_SIZE//Observation.count()+1, 
		int offset = 0;
		
		def observations;
		def startTime = System.currentTimeMillis()
		while(true) {
			observations = Observation.findAllByIsShowable(true, [max:limit, offset:offset, sort:'id']);
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

		//def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
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
				doc.addField(searchFieldsConfig.AUTHOR+"_id", obv.author.id);
				doc.addField(searchFieldsConfig.CONTRIBUTOR, obv.author.name);
				
				doc.addField(searchFieldsConfig.FROM_DATE, obv.fromDate);
				doc.addField(searchFieldsConfig.TO_DATE, obv.toDate);
				
				doc.addField(searchFieldsConfig.OBSERVED_ON, obv.fromDate);
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

                def topology = obv.topology;
                doc.addField(searchFieldsConfig.LATLONG, obv.latitude+","+obv.longitude);
                
                WKTWriter wkt = new WKTWriter();
                try {
                    String geomStr = wkt.write(obv.topology);
                    doc.addField(searchFieldsConfig.TOPOLOGY, geomStr);
                } catch(Exception e) {
                    log.error "Error writing polygon wkt : ${observationInstance}"
                }
				
				doc.addField(searchFieldsConfig.IS_CHECKLIST, obv.isChecklist);
				doc.addField(searchFieldsConfig.IS_SHOWABLE, obv.isShowable);
				doc.addField(searchFieldsConfig.SOURCE_ID, obv.sourceId);
				//boolean geoPrivacy = false;
				//String locationAccuracy;
				obv.tags.each { tag ->
					doc.addField(searchFieldsConfig.TAG, tag);
				}
				
				obv.userGroups.each { userGroup ->
					doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
					doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
				}
				
				addChecklistData(obv, doc)
				
				docs.add(doc);
			}
		}

		//log.debug docs;

        if(docs) {
            try {
                solrServer.add(docs);
                if(commit) {
                    //commit ...server is configured to do an autocommit after 10000 docs or 1hr
                    if(solrServer instanceof StreamingUpdateSolrServer) {
                        solrServer.blockUntilFinished();
                    }
                    solrServer.commit();
                    log.info "Finished committing to observations solr core"
                }
            } catch(SolrServerException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
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
		def distRecoVotes = obv.recommendationVote?.unique { it.recommendation };  
		distRecoVotes.each { vote ->
			doc.addField(searchFieldsConfig.NAME, vote.recommendation.name);
			doc.addField(searchFieldsConfig.CONTRIBUTOR, vote.author.name);
			if(vote.recommendation.taxonConcept)
				doc.addField(searchFieldsConfig.CANONICAL_NAME, vote.recommendation.taxonConcept.canonicalForm);
		}
	}

	
	private addChecklistData(Observation obv, SolrInputDocument doc){
		if(!obv.isChecklist) return
		
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
		def chk = obv 
		
		doc.addField(searchFieldsConfig.TITLE, chk.title);
		doc.removeField(searchFieldsConfig.UPLOADED_ON);
		doc.addField(searchFieldsConfig.UPLOADED_ON, chk.publicationDate?:chk.createdOn);
		doc.addField(searchFieldsConfig.REFERENCE, chk.refText);
		doc.addField(searchFieldsConfig.SOURCE_TEXT, chk.sourceText);
		
		chk.contributors.each { s ->
			doc.addField(searchFieldsConfig.CONTRIBUTOR, s.name);
		}
		chk.attributions.each { s ->
			doc.addField(searchFieldsConfig.ATTRIBUTION, s.name);
		}
		
		chk.observations.each { row ->
			addNameToDoc(row, doc)
		}
		
		chk.states.each { s->
			doc.addField(searchFieldsConfig.LOCATION, s);
		}
		chk.districts.each { s->
			doc.addField(searchFieldsConfig.LOCATION, s);
		}
		chk.talukas.each { s->
			doc.addField(searchFieldsConfig.LOCATION, s);
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
        def result;
        try {
		    result = solrServer.query( params );
        } catch(SolrException e) {
            log.error "Error: ${e.getMessage()}"
        }
        return result;
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
	def terms(query, field, limit) {
		field = field ?: "autocomplete";
		SolrParams q = new SolrQuery().setQueryType("/terms")
				.set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
				.set(TermsParams.TERMS_LOWER, query)
				.set(TermsParams.TERMS_LOWER_INCLUSIVE, true)
				.set(TermsParams.TERMS_REGEXP_STR, query+".*")
				.set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
				.set(TermsParams.TERMS_LIMIT, limit)
				.set(TermsParams.TERMS_RAW, true);
		log.info "Running observation search query : "+q
        def result;
        try{
		    result = solrServer.query( q );
        } catch(SolrException e) {
            log.error "Error: ${e.getMessage()}"
        }
        return result;
	}
}
