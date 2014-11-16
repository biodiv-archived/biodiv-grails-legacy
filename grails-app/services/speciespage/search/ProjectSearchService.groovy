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
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer

import content.Project

class ProjectSearchService extends AbstractSearchService {

    static transactional = false
	/**
	 *
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to projects search index"

        //TODO: change limit
        int limit = BATCH_SIZE, offset = 0;
        int noIndexed = 0;

        def projects;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Project.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Project.withNewTransaction([readOnly:true]) { status ->
                projects = Project.list(max:limit, offset:offset);
                noIndexed += projects.size()
                if(!projects) return;
                publishSearchIndex(projects, true);
                //projects.clear();
                offset += limit;
            }
            if(!projects) return;
            projects.clear();

        }

        log.info "Time taken to publish projects search index is ${System.currentTimeMillis()-startTime}(msec)";
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

                doc.addField(searchFieldsConfig.UPLOADED_ON, proj.dateCreated);
                doc.addField(searchFieldsConfig.UPDATED_ON, proj.lastUpdated);

				
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

        return commitDocs(docs, commit);
	}

}
