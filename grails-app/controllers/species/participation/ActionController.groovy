package species.participation

import java.util.List;
import java.util.Map;

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder;
import groovy.xml.StreamingMarkupBuilder;
import groovy.xml.XmlUtil;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.converters.XML;

import species.participation.Flag.FlagType
import species.participation.Follow
import grails.plugins.springsecurity.Secured
import grails.util.Environment;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.Flag.FlagType
import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.Habitat;
import species.Species;
import species.Resource;
import species.BlockedMails;
import species.Resource.ResourceType;
import species.auth.SUser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import species.participation.ActivityFeedService

class ActionController {
    
    def grailsApplication;
	def observationService;
	def springSecurityService;
	def mailService;
	def observationsSearchService;
    def speciesSearchService;
    def documentSearchService;
	def namesIndexerService;
	def userGroupService;
	def activityFeedService;
	def SUserService;
	def obvUtilService;
    def chartService;

	static allowedMethods = [save:"POST", update: "POST", delete: "POST"]

    def index = { }
    
    def searchIndex(type,obv){
        if(type == "species.participation.Observation"){
            observationsSearchService.publishSearchIndex(obv, true);
        }
        else if(type == "species.participation.Species"){
             speciesSearchService.publishSearchIndex(obv, true);
        }
        else if(type == "content.eml.Document") {
            documentSearchService.publishSearchIndex(obv, true);

        }
    } 
   
    @Secured(['ROLE_USER'])
    def featureIt = { 
        log.debug params;
        params.author = springSecurityService.currentUser;
        def obv = activityFeedService.getDomainObject(params.type,params.id); 
        def ugParam = params['userGroup']
        def ugParamLen = ugParam.length()
        List splitGroups = ugParam.split(",")
        if(ugParamLen != 0){
            if(ugParam[ugParamLen-1] == ',') {
                splitGroups.add("")
            }
        }
        List groups = splitGroups.collect {
            if(it == ""){ 
                null
            }
            else { 
                UserGroup.read(Long.parseLong(it))
            }
        }
        def featuredInstance
        UserGroup.withTransaction(){
            groups.each { ug ->
                featuredInstance = Featured.findWhere(author: params.author,objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                 if(!featuredInstance){
                     try{
                        featuredInstance = new Featured(author:params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug, notes: params.notes)
                        featuredInstance.save(flush: true)
                        if(!featuredInstance.save(flush:true)){
                            featuredInstance.errors.allErrors.each { log.error it }
                          }
                         else{
                            println featuredInstance.id
                        }
                        def act = activityFeedService.addActivityFeed(obv, ug? ug : obv, featuredInstance.author, activityFeedService.FEATURED, featuredInstance.notes);
                        println "====================="
                        searchIndex(params.type,obv)
                        Follow.addFollower(obv, params.author)
                        observationService.sendNotificationMail(act.activityType, obv, null, null, act)
                    }catch (Exception e) {
                        e.printStackTrace()
                     }

                 }
                else {
                    flash.message  = "${message(code: 'featured.already', default:'Already featured')}"
                }
                 }
            } 

        def r = ["success":true]
        def freshUGListHTML = g.render(template:"/common/showFeaturedTemplate" ,model:['observationInstance':obv])
        r["freshUGListHTML"] = freshUGListHTML
	    render r as JSON
    }

    @Secured(['ROLE_USER'])
    def unfeatureIt = {
        log.debug params;
        params.author = springSecurityService.currentUser;
        def obv = activityFeedService.getDomainObject(params.type, params.id);
        def ugParam = params['userGroup']
        def ugParamLen = ugParam.length()
        List splitGroups = ugParam.split(",")
        if(ugParamLen != 0){
            if(ugParam[ugParamLen-1] == ',') {
                splitGroups.add("")
            }
        }
        List groups = splitGroups.collect {
		     if(it == ""){
                null
            }
            else {
                UserGroup.read(Long.parseLong(it))
            }
		}
        def featuredInstance
        UserGroup.withTransaction() {
            groups.each { ug ->
                featuredInstance = Featured.findWhere(author: params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                if(!featuredInstance) {
                    return
                }
                try {
                    //featuredInstance.delete(flush: true)
                        if(!featuredInstance.delete(flush:true)){
                            featuredInstance.errors.allErrors.each { log.error it }
                    }

                    //String actDesc = activityFeedService.getDescriptionForFeature(obv, ug, false) 
                    //println "======ACT DESC ===== " + actDesc
                    def act = activityFeedService.addActivityFeed(obv, ug? ug : obv, featuredInstance.author, activityFeedService.UNFEATURED, featuredInstance.notes);
                    searchIndex(params.type,obv)
                    observationService.sendNotificationMail(act.activityType, obv, null, null, act)
                    return
                }catch (org.springframework.dao.DataIntegrityViolationException e) {
				    flash.message = "${message(code: 'featured.delete.error', default: 'Error while unfeaturing')}"
		        }
            }
        }
        def r = ["success":true]
        def freshUGListHTML = g.render(template:"/common/showFeaturedTemplate" ,model:['observationInstance':obv])
        r["freshUGListHTML"] = freshUGListHTML
	    render r as JSON

    }

    
    

	@Secured(['ROLE_USER'])
	def flagIt = { 
       	log.debug params;
		params.author = springSecurityService.currentUser;
		def obv = activityFeedService.getDomainObject(params.type,params.id);     //GEt object instance ??
		FlagType flag = observationService.getObservationFlagType(params.obvFlag?:FlagType.OBV_INAPPROPRIATE.name());    //flag nikalne ki function observationservice mein hai ??
		def FlagInstance = Flag.findWhere(author: params.author,objectId: params.id.toLong(),objectType: params.type);
		if (!FlagInstance) {
			try {
				FlagInstance = new Flag(objectId: params.id.toLong(),objectType: params.type, author: params.author, flag:flag, notes:params.notes)
				FlagInstance.save(flush: true)
				if(!FlagInstance.save(flush:true)){
                    FlagInstance.errors.allErrors.each { println it }
			        return null
		        }

                obv.flagCount++
				obv.save(flush:true)
				activityFeedService.addActivityFeed(obv, FlagInstance, FlagInstance.author, activityFeedService.OBSERVATION_FLAGGED); //add activity
                searchIndex(params.type,obv)				
				observationService.sendNotificationMail(observationService.OBSERVATION_FLAGGED, obv, request, params.webaddress) //???
				flash.message = "${message(code: 'flag.added', default: 'Observation flag added')}"
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'flag.error', default: 'Error during addition of flag')}"   ///change message
			}
		}
		else {
			flash.message  = "${message(code: 'flag.duplicate', default:'Already flagged')}"    ///change message
		}
        def r = ["success":true]
        def observationInstance = activityFeedService.getDomainObject(params.type,params.id); 
        def flagListUsersHTML = g.render(template:"/common/observation/flagListUsersTemplate" ,model:['observationInstance':observationInstance])
	    
        r['flagListUsersHTML'] = flagListUsersHTML
	    render r as JSON
	}

	@Secured(['ROLE_USER'])
	def deleteFlag  = {
		log.debug params;
        params.author = springSecurityService.currentUser;
		def FlagInstance = Flag.findWhere(id: params.id.toLong());  //kaun si kiski id hai???
		def obv = activityFeedService.getDomainObject(FlagInstance.objectType,FlagInstance.objectId);    ///observation nikali hai...species bhi ho sakta hai
        

		if(!FlagInstance){
			render obv.flagCount;
			return
		}
		try {
			FlagInstance.delete(flush: true);
            
            println "=FLAG COUNT=== " +	obv.flagCount	
            obv.flagCount--;
			obv.save(flush:true)
            println "=FLAG COUNT=== " +	obv.flagCount
			searchIndex(params.type,obv);    //observation ke liye only
            def message = [:]
            message['flagCount'] = obv.flagCount 
            println "=FLAG COUNT=== " + message['flagCount']
			render message as JSON;
			return;
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			def message = [error: g.message(code: 'flag.error.onDelete', default:'Error on deleting flag')];
			
            render message as JSON
		} 
	}
}
