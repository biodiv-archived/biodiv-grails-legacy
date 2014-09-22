package speciespage.search

import species.participation.Observation
import species.Species
import content.eml.Document
import species.auth.SUser;


class BiodivSearchService extends AbstractSearchService {
    
    def observationsSearchService
    def speciesSearchService
    def documentSearchService
    def SUserSearchService

    int BATCH_SIZE = 10;

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
        println "=====MODULES======== " + modules
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
        println "===OBV======== "
        def f1 = observationsSearchService.publishSearchIndex(obvs, commit);
        println "===SP========"
        def f2 = speciesSearchService.publishSearchIndex(sps, commit);
        println "===DOC========"
        def f3 = documentSearchService.publishSearchIndex(documents, commit)
        println "===USER========"
        def f4 = SUserSearchService.publishSearchIndex(users, commit)
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
        println "=======RES===== " + f1.booleanValue() +"==== "+ f2.booleanValue() + "======= "+ f3.booleanValue() + "========== " + f4.booleanValue()
        return (f1 && f2 && f3 && f4);
    }


}
