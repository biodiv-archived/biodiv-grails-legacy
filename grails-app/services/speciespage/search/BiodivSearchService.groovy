package speciespage.search

import species.participation.Observation
import species.Species
import content.eml.Document
import species.auth.SUser;
import utils.Newsletter;
import content.Project
import species.groups.UserGroup;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
import org.springframework.context.ApplicationContext


class BiodivSearchService extends AbstractSearchService {
    
    def observationsSearchServiceBean
    def speciesSearchServiceBean
    def documentSearchServiceBean
    def SUserSearchServiceBean
    //def newsletterSearchServiceBean
    def userGroupSearchServiceBean
    //ApplicationContext applicationContext

    int BATCH_SIZE = 10;


/*
    def getObservationsSearchServiceBean() {
        println "=======HELLO==========="
        if(!observationsSearchServiceBean) { 
            println "=================="
            observationsSearchServiceBean = applicationContext.getBean("observationsSearchService");
        }
        return observationsSearchServiceBean;
    }
    
    def getSpeciesSearchServiceBean() {
        if(!speciesSearchServiceBean) { 
            speciesSearchServiceBean = applicationContext.getBean("speciesSearchService");
        }
        return speciesSearchServiceBean;
    
    }
    
    def getDocumetSearchServiceBean() {
        if(!documentSearchServiceBean) { 
            documentSearchServiceBean = applicationContext.getBean("documentSearchService");
        }
        return documentSearchServiceBean;
    
    }
    
    def getSUserSearchServiceBean() {
        if(!SUserSearchServiceBean) { 
            SUserSearchServiceBean = applicationContext.getBean("SUserSearchService");
        }
        return SuserSearchServiceBean;
    
    }
    
    def getNewsletterSearchServiceBean() {
        if(!newsletterSearchServiceBean) { 
            newsletterSearchServiceBean = applicationContext.getBean("newsletterSearchService");
        }
        return newsletterSearchServiceBean;
    }
    
    def getUserGroupSearchServiceBean() {
        if(!userGroupSearchServiceBean) { 
            userGroupSearchServiceBean = applicationContext.getBean("userGroupSearchService");
        }
        return userGroupSearchServiceBean;
    }
  */
    /**
     * 
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to biodiv search index"

        //TODO: change limit
        int limit = BATCH_SIZE//Observation.count()+1, 
        int offset = 0;

        def modules = [:];
        def startTime = System.currentTimeMillis()
        //while(true) {
        modules["observations"] = Observation.findAllByIsShowableAndIsChecklist(true,false, [max:limit, offset:offset, sort:'id', order: 'desc']);
        modules["species"] = Species.list(max: limit);
        modules["document"] = Document.list(max:limit);
        modules["users"] = SUser.findAll("from SUser as u where u.accountLocked =:ae and u.accountExpired =:al and u.enabled=:en", [ae:false, al:false, en:true], [max:limit, offset:offset, sort: "id"]);
        modules["projects"] = Project.list(max:limit, offset:offset);
        modules["newsletters"] = Newsletter.list(max:limit, offset:offset);
        modules["userGroups"] = UserGroup.list(max:limit, offset:offset);
        if(!publishSearchIndex(modules, true)) {
            log.error "FAILED to publish biodiv search index"
            return
        }
        modules.clear();
        cleanUpGorm()
        //}

        log.info "Time taken to publish biodiv search index is ${System.currentTimeMillis()-startTime}(msec)";
    }

    def publishSearchIndex(List modules, boolean commit){
        println "to do"
    }
    
    def publishSearchIndex(HashMap modules, boolean commit) {
        println "=======STARTING======== "
        if(!modules) return;
        log.info "Initializing publishing to biodiv search index : "+modules.size();

        //def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields

        //Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        //Map names = [:];
        //Map docsMap = [:]
        def obvs = modules["observations"];
        def sps = modules["species"];
        def documents = modules["document"];
        def users = modules["users"];
        //def projects = modules["projects"];
        def newsletters = modules["newsletters"];
        def userGroups = modules["userGroups"];
        //getObservationsSearchServiceBean()
        println "===INDEXING OBV======== "
        def f1 = observationsSearchServiceBean.publishSearchIndex(obvs, commit);
        println "===INDEXING SP========"
        def f2 = speciesSearchServiceBean.publishSearchIndex(sps, commit);
        println "===INDEXING DOC========"
        def f3 = documentSearchServiceBean.publishSearchIndex(documents, commit)
        println "===INDEXING USER========"
        def f4 = SUserSearchServiceBean.publishSearchIndex(users, commit)
        //println "===NEWSLETTER========"
        //def f5 = newsletterSearchServiceBean.publishSearchIndex(newsletters, commit)
        println "=====INDEXING USER GROUP==== "
        def f6 = userGroupSearchServiceBean.publishSearchIndex(userGroups, commit)

        println "===DONE========"
        /*obvs.each { obv ->
            log.debug "Reading observation : "+obv.id;
            List ds = observationsSearchService.getSolrDocument(mod);
            docs.addAll(ds);
        }
        sps.each { sp ->
            log.debug "Reading species : "+sp.id;
            List ds = speciesSearchService.getSolrDocument(mod);
            docs.addAll(ds);
        }
        docs.each { doc ->
            log.debug "Reading document : "+doc.id;
            List ds = documentSearchService.getSolrDocument(mod);
            docs.addAll(ds);
        }*/
        println "=======RES===== " + f1.booleanValue() +"==== "+ f2.booleanValue() + "======= "+ f3.booleanValue() + "========== " + f4.booleanValue() + "========== " + f6.booleanValue()
        return (f1 && f2 && f3 && f4 && f6);

        return (f1 && f2 && f3 && f4 && f6);
    }


}
