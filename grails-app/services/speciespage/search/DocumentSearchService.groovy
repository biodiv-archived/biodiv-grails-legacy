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

import content.eml.Document;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer

class DocumentSearchService extends AbstractSearchService {
    
    static transactional = false
	
    /**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to ufiles search index"
		
		//TODO: change limit
		int limit = BATCH_SIZE;//Document.count()+1, 
        int offset = 0, noIndexed = 0;
		
		def documents;
		def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Document.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Document.withNewTransaction([readOnly:true]) { status ->
                documents = Document.list(max:limit, offset:offset);
                noIndexed += documents.size();
                if(!documents) return;
                publishSearchIndex(documents, true);
                //documents.clear();
                offset += limit;
                cleanUpGorm();
            }
            if(!documents) break;
            documents.clear();
        }
		
		log.info "Time taken to publish documents search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 *
	 * @param ufiles
	 * @param commit
	 * @return
	 */
	def publishSearchIndex(List<Document> documents, boolean commit) {
		if(!documents) return;
		log.info "Initializing publishing to Documents search index : "+documents.size();

        def fieldsConfig = grails.util.Holders.config.speciesPortal.fields
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        Map names = [:];
        Map docsMap = [:]
        documents.each { document ->
            log.debug "Reading Document : "+document.id;

            SolrInputDocument doc = new SolrInputDocument();
            doc.setDocumentBoost(1.5);
            doc.addField(searchFieldsConfig.ID,document.class.simpleName +"_"+ document.id.toString());
            doc.addField(searchFieldsConfig.OBJECT_TYPE, document.class.simpleName);
            doc.addField(searchFieldsConfig.TITLE, document.title);
            doc.addField(searchFieldsConfig.DESCRIPTION, document.notes);
            doc.addField(searchFieldsConfig.TYPE, document.type.value());
            doc.addField(searchFieldsConfig.UPLOADED_ON, document.createdOn);
            if(document.license) {
                doc.addField(searchFieldsConfig.LICENSE, document.license.name.name());
            }

            if(document.attribution){
                doc.addField(searchFieldsConfig.ATTRIBUTION, document.attribution);
            }

            if(document.contributors){
                document.contributors.each { contributor -> 
                    /*String userInfo = ""
                    if(contributor.user) {
                        userInfo = " ### "+contributor.user.email+" "+contributor.user.username+" "+contributor.user.id.toString()
                    }*/
                    doc.addField(searchFieldsConfig.CONTRIBUTOR, contributor);
                }
            }

            document.tags.each { tag ->
                doc.addField(searchFieldsConfig.TAG, tag);
            }

            document.userGroups.each { userGroup ->
                doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }

            String memberInfo = ""
            List allMembers = utilsServiceBean.getParticipants(document)
            allMembers.each { mem ->
                if(mem == null) return;
                memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
                doc.addField(searchFieldsConfig.MEMBERS, memberInfo);
            }

            doc.addField(searchFieldsConfig.DOC_TYPE, document.type);

            docs.add(doc);

        }

		//log.debug docs;
        return commitDocs(docs, commit);
	}

    def delete(long id) {
        super.delete(Document.simpleName +"_"+id.toString());
    }
}
