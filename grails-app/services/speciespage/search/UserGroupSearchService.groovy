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

    def observationService;
	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to usergroup search index"
		
		//TODO: change limit
		int limit = UserGroup.count()+1, offset = 0;
		
		def userGroups;
		def startTime = System.currentTimeMillis()
		while(true) {
			userGroups = UserGroup.list(max:limit, offset:offset);
			if(!userGroups) break;
			publishSearchIndex(userGroups, true);
			userGroups.clear();
			offset += limit;
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
                println "=====ID======== " + ug.class.simpleName +"_"+ug.id.toString()
				doc.addField(searchFieldsConfig.ID, ug.class.simpleName +"_"+ ug.id.toString());
			    doc.addField(searchFieldsConfig.OBJECT_TYPE, ug.class.simpleName);
				doc.addField(searchFieldsConfig.TITLE, ug.name);
				//Location
                //doc.addField(searchFieldsConfig.UPLOADED_ON, obv.date);
				//Pages
                def allPages = ""
                ug.newsletters.each {
                    allPages += it.title + " "
                }
                println "===ALL PAGES===== " + allPages 
                doc.addField(searchFieldsConfig.PAGES, allPages);

                String members = ""
                List allMembers = observationService.getParticipants(ug)
                allMembers.each { mem ->
                    members += mem.name + " "
                }
                doc.addField(searchFieldsConfig.MEMBERS, members);
                
                docs.add(doc);
			
		}

        return commitDocs(docs, commit);
	}


}
