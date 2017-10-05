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

class SUserSearchService extends AbstractSearchService {

    static transactional = false

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to ufiles search index"
		
		//TODO: change limit
		int limit = BATCH_SIZE, offset = 0, noIndexed = 0;

        def susers;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:SUser.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) { 
            SUser.withNewTransaction([readOnly:true]) { status ->
                susers = SUser.findAll("from SUser as u where u.accountLocked =:ae and u.accountExpired =:al and u.enabled=:en", [ae:false, al:false, en:true], [max:limit, offset:offset, sort: "id"]);
                noIndexed += susers.size();
                if(!susers) return;
                if(susers)  {
                    publishSearchIndex(susers, true);
                    //susers.clear();
                }
                offset += limit;
                cleanUpGorm();
            }
            if(!susers) break;
            susers.clear();
        }

		log.info "Time taken to publish users search index is ${System.currentTimeMillis()-startTime}(msec)";
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

		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		susers.each { suser ->
            suser = SUser.read(suser.id);
            log.debug "Reading User : "+suser.id;
            SolrInputDocument doc = new SolrInputDocument();
            doc.setDocumentBoost(3);
            String className = org.hibernate.Hibernate.getClass(suser).getSimpleName()  
            doc.addField(searchFieldsConfig.ID, className +"_"+suser.id.toString());
            doc.addField(searchFieldsConfig.OBJECT_TYPE, className);
            doc.addField(searchFieldsConfig.NAME, suser.name);
            doc.addField(searchFieldsConfig.USERNAME, suser.username);
            doc.addField(searchFieldsConfig.EMAIL, suser.email);
            doc.addField(searchFieldsConfig.ABOUT_ME, suser.aboutMe);
            doc.addField(searchFieldsConfig.LAST_LOGIN, suser.lastLoginDate);
            doc.addField(searchFieldsConfig.LOCATION, suser.location);

            String userInfo = suser.name + " ### " + suser.email +" "+ suser.username +" "+suser.id.toString()
            doc.addField(searchFieldsConfig.USER, userInfo);

            doc.addField(searchFieldsConfig.UPLOADED_ON, suser.dateCreated);
            doc.addField(searchFieldsConfig.UPDATED_ON, suser.lastLoginDate);

            suser.groups.each { userGroup ->
                doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }

            docs.add(doc);
		}

        return commitDocs(docs, commit);
	}

    def delete(long id) {
        String className = org.hibernate.Hibernate.getClass(suser).getSimpleName()  
        super.delete(className.simpleName +"_"+id.toString());
    }
}
