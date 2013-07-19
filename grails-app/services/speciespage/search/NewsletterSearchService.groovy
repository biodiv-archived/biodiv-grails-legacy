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
import utils.Newsletter;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer

class NewsletterSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 50;

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to newsletters search index"
		
		//TODO: change limit
		int limit = Newsletter.count()+1, offset = 0;
		
		def newsletters;
		def startTime = System.currentTimeMillis()
		while(true) {
			newsletters = Newsletter.list(max:limit, offset:offset);
			if(!newsletters) break;
			publishSearchIndex(newsletters, true);
			newsletters.clear();
			offset += limit;
		}
		
		log.info "Time taken to publish newsletter search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(Newsletter obv, boolean commit) {
		return publishSearchIndex([obv], commit);
	}
	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Newsletter> obvs, boolean commit) {
		if(!obvs) return;
		log.info "Initializing publishing to newsletters search index : "+obvs.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		obvs.each { obv ->
			log.debug "Reading Newsletter : "+obv.id;

				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, obv.id.toString());
				doc.addField(searchFieldsConfig.NAME, obv.title);
				doc.addField(searchFieldsConfig.UPLOADED_ON, obv.date);
				doc.addField(searchFieldsConfig.UPDATED_ON, obv.date);
				if(obv.userGroup) {
					doc.addField(searchFieldsConfig.USER_GROUP, obv.userGroup.id);
					doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, obv.userGroup.webaddress);
				}
				if(obv.newsitem) {
					
					doc.addField(searchFieldsConfig.MESSAGE, obv.newsitem);
				}
				
//				obv.tags.each { tag ->
//					doc.addField(searchFieldsConfig.TAG, tag);
//				}
					
				docs.add(doc);
			
		}

		//log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
                if(solrServer instanceof StreamingUpdateSolrServer)
    				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to newsletters solr core"
			}
		} catch(SolrServerException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def search(query) {
		def params = SolrParams.toSolrParams(query);
		log.info "Running newsletter search query : "+params
		return solrServer.query( params );
	}

	/**
	* delete requires an immediate commit
	* @return
	*/
   def delete(long id) {
	   log.info "Deleting newsletter from search index"
	   solrServer.deleteByQuery("id:${id}");
	   solrServer.commit();
   }
   
	/**
	 * 
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting newsletter search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 * 
	 * @return
	 */
	def optimize() {
		log.info "Optimizing newsletter search index"
		solrServer.optimize();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def terms(query, field, limit) {
		field = field?:"autocomplete";
		SolrParams q = new SolrQuery().setQueryType("/terms")
				.set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
				.set(TermsParams.TERMS_LOWER, query)
				.set(TermsParams.TERMS_LOWER_INCLUSIVE, true)
				.set(TermsParams.TERMS_REGEXP_STR, query+".*")
				.set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
				.set(TermsParams.TERMS_LIMIT, limit)
				.set(TermsParams.TERMS_RAW, true);
		log.info "Running newsletter search query : "+q
		return solrServer.query( q );
	}
}
