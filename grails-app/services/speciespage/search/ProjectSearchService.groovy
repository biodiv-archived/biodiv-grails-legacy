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

import content.Project

class ProjectSearchService {

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
		int limit = Project.count()+1, offset = 0;
		
		def projects;
		def startTime = System.currentTimeMillis()
		while(true) {
			projects = Project.list(max:limit, offset:offset);
			if(!projects) break;
			publishSearchIndex(projects, true);
			projects.clear();
			offset += limit;
		}
		
		log.info "Time taken to publish projects search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(Project proj, boolean commit) {
		return publishSearchIndex([proj], commit);
	}

	/**
	 * 
	 * @param projects
	 * @param commit
	 * @return
	 */
	def publishSearchIndex(List<Project> projects, boolean commit) {
		if(!projects) return;
		log.info "Initializing publishing to projects search index : "+projects.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		projects.each { proj ->
			log.debug "Reading Project : "+proj.id;

				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, proj.id.toString());
				doc.addField(searchFieldsConfig.TITLE, proj.title);
				doc.addField(searchFieldsConfig.GRANTEE_ORGANIZATION, proj.granteeOrganization);
				
				proj.locations.each { location ->
					doc.addField(searchFieldsConfig.SITENAME, location.siteName);				
					doc.addField(searchFieldsConfig.CORRIDOR, location.corridor);
				}				

				
				proj.tags.each { tag ->
					doc.addField(searchFieldsConfig.TAG, tag);
				}
					
				
				proj.userGroups.each { userGroup ->
					doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
					doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
				}
				docs.add(doc);
			
		}

		log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to project solr core"
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
		log.info "Running project search query : "+params
		return solrServer.query( params );
	}

	/**
	* delete requires an immediate commit
	* @param id
	* @return
	*/
   def delete(long id) {
	   log.info "Deleting project from search index"
	   solrServer.deleteByQuery("id:${id}");
	   solrServer.commit();
   }
   
	/**
	 *
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting project search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 *
	 * @return
	 */
	def optimize() {
		log.info "Optimizing project search index"
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
		log.info "Running project search query : "+q
		return solrServer.query( q );
	}
}
