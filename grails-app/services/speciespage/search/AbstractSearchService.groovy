package speciespage.search

// import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.hibernate.SessionFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import groovyx.net.http.HTTPBuilder

import groovyx.net.http.ContentType
import static groovyx.net.http.Method.POST
import static groovyx.net.http.Method.GET
abstract class AbstractSearchService {

    static transactional = false

    def grailsApplication;
   // def utilsServiceBean;
    def utilsService;

  //  @Autowired
  //  ApplicationContext applicationContext

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	SessionFactory sessionFactory;
    int BATCH_SIZE = 10;
    int INDEX_DOCS = -1;
/*
    def getUtilsServiceBean() {
        if(!utilsServiceBean) {
            utilsServiceBean = applicationContext.getBean("utilsService");
        }
        return utilsServiceBean;
    }
*/
    /**
     *
     */
    public def abstract publishSearchIndex();

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
    public void postToElastic(List doc,String index){
      if(!doc) return;
        def searchConfig = grailsApplication.config.speciesPortal
        def URL=searchConfig.search.nakshaURL;
        def http = new HTTPBuilder(URL)
        http.request(POST,ContentType.JSON) {
            uri.path = "/naksha/services/bulk-upload/${index}/${index}";
            body = doc
            response.success = { resp, reader ->
                log.debug "Successfully posted observation  to elastic"
            }
            response.'404' = {
              println 'Not found';
              log.debug "Error in posting observation to elastic : Not found";
            }
        }
    }


    /**
     *
     * @param query
     * @return
     */
    def search(params) {

        def searchConfig = grailsApplication.config.speciesPortal
        def URL=searchConfig.search.biodivApiURL;
        log.info "######Running ${this.getClass().getName()} search query : "+params
        def result = [];
        def queryResponse;
        def http = new HTTPBuilder()

              http.request( URL, GET, ContentType.JSON ) { req ->
                uri.path = '/biodiv-api/search/all'
                uri.query = [ query:params.query,object_type:(params.aq?.object_type?:params.object_type),name:params.aq?params.aq.name:null,text:params.aq?params.aq.text:null,
                location:params.aq?.location,contributor:params.aq?.contributor,license:params.aq?.license,member:params.aq?.member,user:params.aq?.user,
                attribution:params.aq?.attribution,from:params?.offset]
                headers.Accept = 'application/json'

                response.success = { resp, json ->
                  assert resp.statusLine.statusCode == 200
                  println "Got response: ${resp.statusLine}"
                  println resp
                  println json;
                  queryResponse = json;
                }

                response.'404' = {
                  println 'Not found'
                }
              }
        return queryResponse

    }


    /**
     * delete requires an immediate commit
     * @param id
     * @return
     */
    def delete(String id) {
        log.info "Deleting ${this.getClass().getName()} from search index"
        try {
            //solrServer.deleteByQuery("id:${id}");
            //solrServer.commit();
        } catch(Exception e) {
            log.error "Error: ${e.getMessage()}"
        }

    }

    /**
     * @return
     */
    def deleteIndex() {
        log.info "Deleting  ${this.getClass().getName()} search index"
        //solrServer.deleteByQuery("*:*")
        //solrServer.commit();
    }

    /**
     * @param query
     * @return
     */
    def terms(query, field, limit) {
        field = field ?: "autocomplete";
/*        SolrParams q = new SolrQuery().setQueryType("/terms")
        .set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
        .set(TermsParams.TERMS_LOWER, query)
        .set(TermsParams.TERMS_LOWER_INCLUSIVE, true)
        .set(TermsParams.TERMS_REGEXP_STR, query+".*")
        .set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
        .set(TermsParams.TERMS_LIMIT, limit)
        .set(TermsParams.TERMS_RAW, true);
        log.info "Running observation terms query : "+q
*/        def result;
        try{
            //result = solrServer.query( q );
        } catch(Exception e) {
            log.error "Query: ${query} - Error: ${e.getMessage()}"
        }
        return result;
    }

    /**
     *
     */
     void cleanUpGorm() {

        def hibSession = sessionFactory?.getCurrentSession();

        if(hibSession) {
            log.debug "Flushing and clearing session"
            try {
                //hibSession.flush()
            } catch(e) {
                e.printStackTrace()
            }
            hibSession.clear()
        }
    }
}
