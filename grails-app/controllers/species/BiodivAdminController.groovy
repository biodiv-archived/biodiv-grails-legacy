package species

import java.util.Date;
import java.lang.Float;
import species.NamesParser;
import species.Synonyms;

import grails.plugin.springsecurity.SpringSecurityUtils;

import species.auth.SUser;

import grails.plugin.springsecurity.annotation.Secured;

@Secured(['ROLE_ADMIN'])
class BiodivAdminController {

    def setupService;
    def speciesService;
    def speciesUploadService;
    def taxonService;
    def speciesSearchService;
    def observationsSearchService;
    def SUserSearchService;
    def documentSearchService;
    def namesLoaderService;
    def namesIndexerService;
    def groupHandlerService;
    def sessionFactory;
    def externalLinksService;
    def biodivSearchService;
    def messageSource;
    def msg;

    /**
     * 
     */
    def index = {
    }

    /**
     * 
     */
    def setup = {
        try {
            setupService.setupDefs();
            flash.message = messageSource.getMessage("default.success.loaded", null, request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def loadData = {
        int noOfInsertions = 0;
        try {
            noOfInsertions = speciesUploadService.loadData();
            flash.message = messageSource.getMessage("default.addNo.records", [noOfInsertions] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = messageSource.getMessage("default.insert.record.error", [noOfInsertions,e.getMessage()] as Object[], request.locale)
        }
        redirect(action: "index")
    }

    def loadNames = {
        try {
            taxonService.loadTaxon(true);
            flash.message = messageSource.getMessage("default.admin.finished.loading", null, request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = messageSource.getMessage("default.admin.error", [e.getMessage()] as Object[], request.locale)
        }

        redirect (action:"index");
    }

    def reloadNames = {
        try {
            log.debug "Syncing names into recommendations"
            namesLoaderService.syncNamesAndRecos(false);
            flash.message = messageSource.getMessage("default.admin.success.loaded.name", null, request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }

        redirect(action: "index")
    }

    def reloadSpeciesSearchIndex = {
        try {
            //speciesSearchService.deleteIndex();
            speciesSearchService.publishSearchIndex();
            speciesSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['species'] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadObservationsSearchIndex = {
        try {
            //observationsSearchService.deleteIndex();
            observationsSearchService.publishSearchIndex();
            observationsSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['observations'] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadUsersSearchIndex = {
        try {
            //SUserSearchService.deleteIndex();
            SUserSearchService.publishSearchIndex();
            SUserSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['users'] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        } 
        redirect(action: "index")
    }

    def reloadDocumentSearchIndex = {
        try {
            //documentSearchService.deleteIndex();
            documentSearchService.publishSearchIndex();
            documentSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['documents'] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadNamesIndex = {
        try {
            namesIndexerService.rebuild();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['names'] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def updateGroups = {
        int noOfUpdations = 0;
        try {
            noOfUpdations = groupHandlerService.updateGroups();
            flash.message = messageSource.getMessage("default.admin.success.updated.group", ['associations',noOfUpdations] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def updateExternalLinks = {
        try {
            int noOfUpdations = externalLinksService.updateExternalLinks();
            flash.message = messageSource.getMessage("default.admin.success.updated.group", ['externalLinks',noOfUpdations] as Object[], request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def recomputeInfoRichness = {
        try {
            speciesService.computeInfoRichness();
            flash.message = messageSource.getMessage("default.admin.success.updated.richness", null, request.locale)
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    /*
    def loadUsers() {
    def defaultRoleNames = ['ROLE_USER']

    new File("/tmp/users.tsv").splitEachLine("\\t") {
    def fields = it;
    def user = new SUser (
username : fields[1],
name : fields[1],
password : fields[2],
enabled : true,
accountExpired : false,
accountLocked : false,
passwordExpired : false,
email : fields[3],
dateCreated : new Date(Long.parseLong(fields[9])),
lastLoginDate : new Date(Long.parseLong(fields[11])),
profilePic:fields[15]);

if(fields[13]) {
user.timezone = Float.parseFloat(fields[13])
}

SUser.withTransaction {
if(!user.save(flush: true) ){
user.errors.each { println it; }
} else {

def securityConf = SpringSecurityUtils.securityConfig
Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz
PersonRole.withTransaction { status ->
defaultRoleNames.each { String roleName ->
String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
def auth = Authority."findBy${findByField}"(roleName)
if (auth) {
PersonRole.create(user, auth)
} else {
println "Can't find authority for name '$roleName'"
}
}
}
}
}

    }
    }

    def parseSynonyms = {
    NamesParser namesParser = new NamesParser();
    Synonyms.withTransaction {
    Synonyms.list().eachWithIndex { syn, index ->
    def parsedNames = namesParser.parse([syn.name]);
    if(parsedNames[0]?.canonicalForm) {
    syn.canonicalForm = parsedNames[0].canonicalForm;
    syn.normalizedForm = parsedNames[0].normalizedForm;;
    syn.italicisedForm = parsedNames[0].italicisedForm;;
    syn.binomialForm = parsedNames[0].binomialForm;;
    if(!syn.save(flush:true, insert:true)) {
    syn.errors.each {println it}
    }
    }
    };
    }
    namesLoaderService.syncNamesAndRecos(true);
    }
     */	
def user = {
    String actionId = params.id ?: "list"
    log.debug actionId
    render (template:"/admin/user/$actionId");
}

    def reloadBiodivSearchIndex = {
        try {
            biodivSearchService.deleteIndex();
            biodivSearchService.publishSearchIndex();
            biodivSearchService.optimize();
            flash.message = "Successfully created biodiv search index"
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

}
