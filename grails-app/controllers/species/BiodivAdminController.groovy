package species

import java.util.Date;
import java.lang.Float;
import species.NamesParser;
import species.Synonyms;

import grails.plugin.springsecurity.SpringSecurityUtils;

import species.auth.SUser;

import grails.plugin.springsecurity.annotation.Secured;
 import org.springframework.web.servlet.support.RequestContextUtils as RCU;
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
    def userGroupSearchService;
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
            flash.message = messageSource.getMessage("default.success.loaded", null, RCU.getLocale(request))
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
            flash.message = messageSource.getMessage("default.addNo.records", [noOfInsertions] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = messageSource.getMessage("default.insert.record.error", [noOfInsertions,e.getMessage()] as Object[], RCU.getLocale(request))
        }
        redirect(action: "index")
    }

    def loadNames = {
        try {
            taxonService.loadTaxon(true);
            flash.message = messageSource.getMessage("default.admin.finished.loading", null, RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = messageSource.getMessage("default.admin.error", [e.getMessage()] as Object[], RCU.getLocale(request))
        }

        redirect (action:"index");
    }

    def reloadNames = {
        try {
            log.debug "Syncing names into recommendations"
            namesLoaderService.syncNamesAndRecos(false);
            flash.message = messageSource.getMessage("default.admin.success.loaded.name", null, RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }

        redirect(action: "index")
    }

    def reloadSpeciesSearchIndex = {
        try {
            if(params.deleteIndex)
                speciesSearchService.deleteIndex();

            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                speciesSearchService.INDEX_DOCS = indexDocs
            }
 
            speciesSearchService.publishSearchIndex();
            speciesSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['species'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadObservationsSearchIndex = {
        try {
            if(params.deleteIndex)
                observationsSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                observationsSearchService.INDEX_DOCS = indexDocs
            }
 
            observationsSearchService.publishSearchIndex();
            observationsSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['observations'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadUsersSearchIndex = {
        try {
            if(params.deleteIndex)
                SUserSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                SUserSearchService.INDEX_DOCS = indexDocs
            }
 
            SUserSearchService.publishSearchIndex();
            SUserSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['users'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        } 
        redirect(action: "index")
    }

    def reloadDocumentSearchIndex = {
        try {
            if(params.deleteIndex)
                documentSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                documentSearchService.INDEX_DOCS = indexDocs
            }
 
            documentSearchService.publishSearchIndex();
            documentSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['documents'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadUserGroupSearchIndex = {
        try {
            if(params.deleteIndex)
                userGroupsSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                userGroupSearchService.INDEX_DOCS = indexDocs
            }
 
            userGroupSearchService.publishSearchIndex();
            userGroupSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['user group'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def reloadNamesIndex = {
        try {
            namesIndexerService.rebuild();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['names'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def updateGroups = {
        int noOfUpdations = 0;
        try {
            noOfUpdations = groupHandlerService.updateGroups(params.runForSynonyms?params.runForSynonyms.toBoolean():false);
            flash.message = messageSource.getMessage("default.admin.success.updated.group", ['associations',noOfUpdations] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def updateExternalLinks = {
        try {
            int noOfUpdations = externalLinksService.updateExternalLinks();
            flash.message = messageSource.getMessage("default.admin.success.updated.group", ['externalLinks',noOfUpdations] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }

    def recomputeInfoRichness = {
        try {
            speciesService.computeInfoRichness();
            flash.message = messageSource.getMessage("default.admin.success.updated.richness", null, RCU.getLocale(request))
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
            if(params.deleteIndex) 
                biodivSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                biodivSearchService.INDEX_DOCS = indexDocs
            }
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
