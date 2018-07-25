package species

import java.util.Date;
import java.lang.Float;
import species.NamesParser;
import species.Synonyms;
import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import grails.util.Environment;
import grails.plugin.springsecurity.SpringSecurityUtils;
import species.auth.SUser;

import grails.plugin.springsecurity.annotation.Secured;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;

import species.Resource;
import species.auth.SUser;
import groovy.sql.Sql;

import java.util.Date;
import species.utils.ImageUtils;
import java.util.*;
import java.io.*;


@Secured(['ROLE_ADMIN'])
class BiodivAdminController {

    def setupService;
    def speciesService;
    def speciesUploadService;
    def taxonService;
    def speciesSearchService;
    def observationsSearchService;
    def SUserSearchService;
    def resourceSearchService;
    def newsletterSearchService;
    def documentSearchService;
    def projectSearchService;

    def userGroupSearchService;
    def namesLoaderService;
    def namesIndexerService;
    def groupHandlerService;
    def sessionFactory;
    def externalLinksService;
    def biodivSearchService;
    def messageSource;
    def msg;
    def utilsService;
    def banner;
    Map bannerMessageMap;

    def dataSource;
    def grailsApplication;

    /**
     *
     */
    def index = {
         render(view:"index")
    }

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

    def syncRecosFromTaxonConcepts = {
        try {
            log.debug "Syncing names into recommendations"
            List taxonConcepts = [];
            params.id?.split(',').each {
                def s = Species.read(Integer.parseInt(it));
                if(s) taxonConcepts << s.taxonConcept;
            }
            render namesLoaderService.syncRecosFromTaxonConcepts(taxonConcepts, true);
            //flash.message = messageSource.getMessage("default.admin.success.loaded.name", null, RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }

        //redirect(action: "index")
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
    def reloadResourceSearchIndex = {
        try {
            if(params.deleteIndex)
                resourceSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                resourceSearchService.INDEX_DOCS = indexDocs
            }

            resourceSearchService.publishSearchIndex();
            resourceSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['resources'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }
    def reloadNewsletterSearchIndex = {
        try {
            if(params.deleteIndex)
                newsletterSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                newsletterSearchService.INDEX_DOCS = indexDocs
            }

            newsletterSearchService.publishSearchIndex();
            newsletterSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['newsletter'] as Object[], RCU.getLocale(request))
        } catch(e) {
            e.printStackTrace();
            flash.message = e.getMessage()
        }
        redirect(action: "index")
    }
    def reloadProjectSearchIndex = {
        try {
            if(params.deleteIndex)
                projectSearchService.deleteIndex();
            int indexDocs = params.indexDocs?params.int('indexDocs'):-1
            if(indexDocs > -1) {
                projectSearchService.INDEX_DOCS = indexDocs
            }

            projectSearchService.publishSearchIndex();
            projectSearchService.optimize();
            flash.message = messageSource.getMessage("default.admin.success.createdSearchIndex", ['project'] as Object[], RCU.getLocale(request))
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
            if(params.taxonId) {
            noOfUpdations = groupHandlerService.updateGroup(TaxonomyDefinition.get(Long.parseLong(params.taxonId)));
            flash.message = noOfUpdations +" taxon updated"
            } else if(params.speciesId) {
            noOfUpdations = groupHandlerService.updateGroups([Species.get(Long.parseLong(params.speciesId))], true);
            flash.message = noOfUpdations +" species updated"
            } else {
            noOfUpdations = groupHandlerService.updateGroups(params.runForSynonyms?params.runForSynonyms.toBoolean():false, params.updateWhereNoGroup?params.updateWhereNoGroup.toBoolean():false);
            flash.message = messageSource.getMessage("default.admin.success.updated.group", ['associations',noOfUpdations] as Object[], RCU.getLocale(request))
            }
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

/**
 *
 */
def contentupdate(){
      String content = params.content?.trim()
    String group = params.groupName?.trim()

    try {
        def bannerMessageFile = new File(grailsApplication.config.speciesPortal.bannerFilePath);
        bannerMessageFile.append('\n'+group+"_"+utilsService.getCurrentLanguage(request).threeLetterCode+"-"+content);
        utilsService.loadBannerMessageMap();
        flash.message = "Updated banner message content successfully!"
        redirect(action: "index")
    }
    catch(ex){
        ex.printStackTrace();
        flash.error = "Error in Updating"
        redirect(action: "index")
    }

}

def getMessage(){
    String groupName=params.groupId;
    def bMessage=utilsService.getBannerMessage(groupName);
    render (view:"index" , model:[getMessage:bMessage,getGroup:groupName])
}

def clearCache() {
    render utilsService.clearCache(params.name);
}

private List geUserResoruceId(){
    def result = []
    SUser.findAllByIconIsNotNull().each { user ->
        result << [id:user.id, fileName:user.icon]
    }
    return result
}

private void _doCrop(resourceList, relativePath, sql, dataSoruce){
    def resSize = resourceList.size()
    def counter = 0;
    def missingCount = 0; 
    def fails = 0;
    def resIds = []
    println "==============Resource List Size==================" + resSize
    //HashMap hm = new HashMap();
    resourceList.each { res->
        counter = counter + 1
        if((counter%100) == 0) {
            //println "=======COUNTER==== " + counter
        }
        //println "------------------------------------------------------------------ " + res.id
        String fileName = relativePath + "/" + res.fileName;
        File file = new File(fileName);

        String name = file.getName();
        String parent = file.getParent();
        String inName = name;
        String ext = ".jpg"
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex != -1) {
            inName = name.substring(0, lastIndex);
            ext = name.substring(lastIndex, name.size());
            if(ext == '.tif' || ext == '.tiff') {
                println "=======TIF FILES===== " + file
                println "------------------------------------------------------------------ " + res.id
                ext = '.jpg'
            }
        }

        String outName = inName + "_th2" + ext;
        if(!file.exists()) return;	
        //println file;
        File dir = new File(parent);
        File outImg = new File(dir,outName);
        //println outImg;

        //_th1 image already exists so return;
        if(outImg.exists()) {
            //println "=======TH1 exists======"
            return;
        }
        missingCount = missingCount + 1;
        //println "========NOT FOUND TH1 -- CREATING ====="
        //println "======THIS IMAGE ==== " + outImg
        try{
            ImageUtils.doResize(file, outImg, 320, 320);
        }catch (Exception e) {
            fails += 1;
            //println "==============RahulImageException===== " + e.getMessage()
            resIds.add(res.id.toLong());
            /*println "===DELETING THIS FILE === " + file +"======RES ID==== " + res.id
            sql.executeUpdate('DELETE from species_resource where resource_id = ?', [res.id.toLong()]);
            sql.executeUpdate('DELETE from species_field_resources where resource_id = ?', [res.id.toLong()]);
            sql.executeUpdate('DELETE from resource_license where resource_licenses_id = ?', [res.id.toLong()]);
            sql.executeUpdate('DELETE from resource_contributor where resource_contributors_id = ? or resource_attributors_id = ?', [res.id.toLong(), res.id.toLong()]);
            sql.executeUpdate('DELETE from observation_resource where resource_id = ?', [res.id.toLong()]);
            sql.executeUpdate('DELETE from resource where id = ?', [res.id.toLong()]);
            file.delete();           
             */
        }
    }
    println "=============MISSING COUNT ============= " + missingCount
    println "=============FAILS ============= " + fails
    println "==========RES IDS ===== " + resIds
}

private Set getResoruceId(query, sql){
    def result = [] as Set
    sql.rows(query).each{
        def res = Resource.read(it.getProperty("id"));
        if(res.type == Resource.ResourceType.IMAGE){
            result << res
        }
    }
    return result
}


def doCrop(){
    Date startDate = new Date();

    //def dataSoruce = ctx.getBean("dataSource");
    //def grailsApplication = ctx.getBean("grailsApplication");

    def sql =  Sql.newInstance(dataSoruce);
    def query, result

    //gettting all resource for species
    query = "select distinct(resource_id) as id from species_resource order by resource_id";
    result = getResoruceId(query, sql)
    query = "select distinct(resource_id) as id from species_field_resources order by resource_id";
    result.addAll(getResoruceId(query, sql))
    _doCrop(result, grailsApplication.config.speciesPortal.resources.rootDir, sql, dataSoruce)

    println "----------------DONE SPECIES-------------------------------------------------- "

    //getting all resources for observations 
    //query = "select distinct(resource_id) as id from observation_resource  where resource_id > 291108 order by resource_id ";
    query = "select distinct(resource_id) as id from observation_resource order by resource_id ";
    result = getResoruceId(query, sql)
    _doCrop(result, grailsApplication.config.speciesPortal.observations.rootDir)

    println "----------------DONE OBSERVATION-------------------------------------------------- "

    //getting all resources for users
    //result = geUserResoruceId()
    //_doCrop(result, grailsApplication.config.speciesPortal.users.rootDir)

    //println "----------------DONE USERS-------------------------------------------------- "

    println "============= Start  Time " + startDate  + "          end time " + new Date()
}

}
