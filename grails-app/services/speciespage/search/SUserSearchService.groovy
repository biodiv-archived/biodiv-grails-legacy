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

import species.auth.SUser;
//import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer

class SUserSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 50;

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to ufiles search index"
		
		//TODO: change limit
		int limit = 10, offset = 0;
		
		def susers;
		def startTime = System.currentTimeMillis()
		//while(true) {
			susers = SUser.findAllWhere(accountLocked: false, accountExpired: false, enabled: true);
			//susers = SUser.findAllByAccountLockedLikeAndAccountExpiredLikeAndEnabled(false, false, true, sort: "id");
			//if(!susers) break;
			if(susers)  {
				publishSearchIndex(susers, true);
				susers.clear();
			}
			//offset += limit;
		//}
		
		log.info "Time taken to publish users search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(SUser suser, boolean commit) {
		return publishSearchIndex([suser], commit);
	}
	
	/**	
	 *
	 * @param ufiles
	 * @param commit
	 * @return
	 */
	def publishSearchIndex(List<SUser> susers, boolean commit) {
		if(!susers) return;
		log.info "Initializing publishing to Users search index : "+susers.size();

		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		susers.each { suser ->
			log.debug "Reading User : "+suser.id;
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, suser.id.toString());
				doc.addField(searchFieldsConfig.NAME, suser.name);
				doc.addField(searchFieldsConfig.USERNAME, suser.username);
				doc.addField(searchFieldsConfig.EMAIL, suser.email);
				doc.addField(searchFieldsConfig.ABOUT_ME, suser.aboutMe);
				doc.addField(searchFieldsConfig.LAST_LOGIN, suser.lastLoginDate);
				
				docs.add(doc);
		}

		log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
                	if(solrServer instanceof ConcurrentUpdateSolrServer)
    				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to SUser solr core"
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
		log.info "Running user search query : "+params
		return solrServer.query( params );
	}

	/**
	* delete requires an immediate commit
	* @param id
	* @return
	*/
   def delete(long id) {
	   log.info "Deleting user from search index"
	   solrServer.deleteByQuery("id:${id}");
	   solrServer.commit();
   }
   
	/**
	 *
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting user search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 *
	 * @return
	 */
	def optimize() {
		log.info "Optimizing user search index"
		solrServer.optimize();
	}

	/**
	 *
	 * @param query
	 * @param field
	 * @param limit
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
		log.info "Running user search query : "+q
		return solrServer.query( q );
	}
}
