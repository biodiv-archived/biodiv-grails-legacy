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

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;

import species.groups.UserGroup;

class UserGroupSearchService extends AbstractSearchService {

    static transactional = false

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to usergroup search index"
		
		//TODO: change limit
		int limit = BATCH_SIZE//UserGroup.count()+1, 
        int offset = 0, noIndexed = 0;
		
		def userGroups;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:UserGroup.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            UserGroup.withNewTransaction([readOnly:true]) { status ->
                userGroups = UserGroup.list(max:limit, offset:offset);
                noIndexed += userGroups.size();
                if(!userGroups) return;
                publishSearchIndex(userGroups, true);
                //userGroups.clear();
                offset += limit;
            }
            if(!userGroups) break;
            userGroups.clear();
        }

		log.info "Time taken to publish usergroup search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<UserGroup> ugs, boolean commit) {
		if(!ugs) return;
		log.info "Initializing publishing to usergroups search index : "+ugs.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		ugs.each { ug ->
			log.debug "Reading usergroup : "+ug.id;

				SolrInputDocument doc = new SolrInputDocument();
				doc.addField(searchFieldsConfig.ID, ug.class.simpleName +"_"+ ug.id.toString());
			    doc.addField(searchFieldsConfig.OBJECT_TYPE, ug.class.simpleName);
				doc.addField(searchFieldsConfig.TITLE, ug.name);
				//Location
                doc.addField(searchFieldsConfig.UPLOADED_ON, ug.foundedOn);
				//Pages
                def allPages = ""
                ug.newsletters.each {
                    allPages += it.title + " "
                }
                doc.addField(searchFieldsConfig.PAGES, allPages);
                
                String memberInfo = ""
                List allMembers = utilsServiceBean.getParticipants(ug)
                allMembers.each { mem ->
                    memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
                    doc.addField(searchFieldsConfig.MEMBERS, memberInfo);
                }

                docs.add(doc);
			
		}

        return commitDocs(docs, commit);
	}


}
