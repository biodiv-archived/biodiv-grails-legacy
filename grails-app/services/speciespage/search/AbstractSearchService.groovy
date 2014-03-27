package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.hibernate.SessionFactory;

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer

abstract class AbstractSearchService {

    static transactional = false

    def grailsApplication;

    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SolrServer solrServer;
	SessionFactory sessionFactory;
    int BATCH_SIZE = 50;

    /**
     * 
     */
    def abstract publishSearchIndex();

    def publishSearchIndex(def obj, boolean commit) {
        return publishSearchIndex([obj], commit);
    }

    /**
     * 
     * @param species
     * @return
     */
    def abstract publishSearchIndex(List objs, boolean commit);

    /**
    *
    */
    protected boolean commitDocs(List<SolrInputDocument> docs, boolean commit = true) {
        if(docs) {
            try {
                solrServer.add(docs);
                if(commit) {
                    //commit ...server is configured to do an autocommit after 10000 docs or 1hr
                    if(solrServer instanceof ConcurrentUpdateSolrServer) {
                        solrServer.blockUntilFinished();
                    }
                    solrServer.commit();
                    log.info "Finished committing to ${this.getClass().getName()} solr core"
                    return true;
                }
            } catch(SolrServerException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 
     * @param query
     * @return
     */
    def search(query) {
        def params = SolrParams.toSolrParams(query);
        log.info "Running ${this.getClass().getName()} search query : "+params
        def result;
        try {
            result = solrServer.query( params );
			println "===============res " + result
        } catch(SolrException e) {
            log.error "Error: ${e.getMessage()}"
        }
        return result;
    }


    /**
     * delete requires an immediate commit
     * @param id
     * @return
     */
    def delete(long id) {
        log.info "Deleting ${this.getClass().getName()} from search index"
        try {
            solrServer.deleteByQuery("id:${id}");
            solrServer.commit();
        } catch(SolrException e) {
            log.error "Error: ${e.getMessage()}"
        }

    }

    /**
     * @return
     */
    def deleteIndex() {
        log.info "Deleting  ${this.getClass().getName()} search index"
        solrServer.deleteByQuery("*:*")
        solrServer.commit();
    }

    /**
     * @return
     */
    def optimize() {
        log.info "Optimizing ${this.getClass().getName()} search index"
        solrServer.optimize();
    }

    /**
     * @param query
     * @return
     */
    def terms(query, field, limit) {
        field = field ?: "autocomplete";
        SolrParams q = new SolrQuery().setQueryType("/terms")
        .set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
        .set(TermsParams.TERMS_LOWER, query)
        .set(TermsParams.TERMS_LOWER_INCLUSIVE, true)
        .set(TermsParams.TERMS_REGEXP_STR, query+".*")
        .set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
        .set(TermsParams.TERMS_LIMIT, limit)
        .set(TermsParams.TERMS_RAW, true);
        log.info "Running observation search query : "+q
        def result;
        try{
            result = solrServer.query( q );
        } catch(SolrException e) {
            log.error "Error: ${e.getMessage()}"
        }
        return result;
    }

    /**
     *
     */
    protected void cleanUpGorm() {

        def hibSession = sessionFactory?.getCurrentSession();

        if(hibSession) {
            log.debug "Flushing and clearing session"
            try {
                hibSession.flush()
            } catch(e) {
                e.printStackTrace()
            }
            hibSession.clear()
        }
    }
}
