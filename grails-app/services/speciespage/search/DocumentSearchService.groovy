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

        //Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

        List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();

        Map names = [:];
        Map docsMap = [:]
        documents.each { document ->
            log.debug "Reading Document : "+document.id;

          //  SolrInputDocument doc = new SolrInputDocument();
              Map<String,Object> doc=new HashMap<String,Object>();
          //  doc.setDocumentBoost(1.5);
            doc.put(searchFieldsConfig.ID, document.id.toString());
            doc.put(searchFieldsConfig.OBJECT_TYPE, document.class.simpleName);
            doc.put(searchFieldsConfig.TITLE, document.title);
            doc.put(searchFieldsConfig.DESCRIPTION, document.notes);
            doc.put(searchFieldsConfig.TYPE, document.type.value());
            doc.put(searchFieldsConfig.UPLOADED_ON, document.createdOn);
            if(document.license) {
                doc.put(searchFieldsConfig.LICENSE, document.license.name.name());
            }

            if(document.attribution){
                doc.put(searchFieldsConfig.ATTRIBUTION, document.attribution);
            }

            if(document.contributors){
                document.contributors.each { contributor ->
                    /*String userInfo = ""
                    if(contributor.user) {
                        userInfo = " ### "+contributor.user.email+" "+contributor.user.username+" "+contributor.user.id.toString()
                    }*/
                    doc.put(searchFieldsConfig.CONTRIBUTOR, contributor);
                }
            }

            document.tags.each { tag ->
                doc.put(searchFieldsConfig.TAG, tag);
            }

            document.userGroups.each { userGroup ->
                doc.put(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.put(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }

            String memberInfo = ""
            List allMembers = utilsServiceBean.getParticipants(document)
            allMembers.each { mem ->
                if(mem == null) return;
                memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
                doc.put(searchFieldsConfig.MEMBERS, memberInfo);
            }

            doc.put(searchFieldsConfig.DOC_TYPE, document.type);
          
            eDocs.add(doc);

        }

		postToElastic(eDocs,"document")
        //return commitDocs(docs, commit);
	}

    def delete(long id) {
        super.delete(Document.simpleName +"_"+id.toString());
    }
}
