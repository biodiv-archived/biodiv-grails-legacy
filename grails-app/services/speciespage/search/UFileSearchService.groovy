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

import content.fileManager.UFile

class UFileSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 50;

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to projects search index"
		
		//TODO: change limit
		int limit = UFile.count()+1, offset = 0;
		
		def uFiles;
		def startTime = System.currentTimeMillis()
		while(true) {
			uFiles = UFile.list(max:limit, offset:offset);
			if(!uFiles) break;
			publishSearchIndex(uFIles, true);
			uFiles.clear();
			offset += limit;
		}
		
		log.info "Time taken to publish projects search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(UFile uFile, boolean commit) {
		return publishSearchIndex([uFile], commit);
	}

	/**
	 *
	 * @param projects
	 * @param commit
	 * @return
	 */
	def publishSearchIndex(List<UFile> uFiles, boolean commit) {
		if(!uFiles) return;
		log.info "Initializing publishing to UFiles search index : "+uFiles.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		uFiles.each { uFile ->
			log.debug "Reading UFile : "+uFile.id;

				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, uFile.id.toString());
				doc.addField(searchFieldsConfig.NAME, uFile.name);
				doc.addField(searchFieldsConfig.DESCRIPTION, uFile.description);
				
				
				uFile.tags.each { tag ->
					doc.addField(searchFieldsConfig.TAG, tag);
				}
					
				docs.add(doc);
			
		}

		//log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to UFile solr core"
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
		log.info "Running uFile search query : "+params
		return solrServer.query( params );
	}

	/**
	* delete requires an immediate commit
	* @param id
	* @return
	*/
   def delete(long id) {
	   log.info "Deleting uFile from search index"
	   solrServer.deleteByQuery("id:${id}");
	   solrServer.commit();
   }
   
	/**
	 *
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting uFile search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 *
	 * @return
	 */
	def optimize() {
		log.info "Optimizing uFile search index"
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
		log.info "Running uFile search query : "+q
		return solrServer.query( q );
	}
}
