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
    
   
    @Secured(['ROLE_USER'])
    def featureIt = {
        log.debug params;
        params.author = springSecurityService.currentUser;
        List groups = params['userGroup'].split(",").collect {
			UserGroup.read(Long.parseLong(it))
		}
        def featuredInstance
        UserGroup.withTransaction(){
            groups.each { ug ->
                println "@@@@@@@@@@@@@@@@@"
                println ug
                featuredInstance = Featured.findWhere(author: params.author,objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                if(!featuredInstance){
                    try{
                        println "#################################################"
                        featuredInstance = new Featured(author:params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug, notes: params.notes)
                        featuredInstance.save(flush: true)
                        if(!featuredInstance.save(flush:true)){
                            println "%%%%%%%%%%%%%%%"
				            featuredInstance.errors.allErrors.each { log.error it }
			            }
                        else{
                             println "----------------------------"

                            println featuredInstance.id
                        }


                    }catch (Exception e) {
				        e.printStackTrace()
                        //flash.message = "${message(code: 'featured.mark.error', args: [params.type], default: 'Error during Featuring')}"
			        }

                }
                else {
			        flash.message  = "${message(code: 'featured.already', default:'Already featured')}"
		        }
            }
        }
        println "FEATURE function end sending msg==================================================="
        def r = ["success":true]
        def observationInstance = activityFeedService.getDomainObject(params.type,params.id); 
        def freshUGListHTML = g.render(template:"/common/showFeaturedTemplate" ,model:['observationInstance':observationInstance])
        println "=======HTML RENDER ========== " + freshUGListHTML
        r["freshUGListHTML"] = freshUGListHTML
	    //r['msg'] = "dghgtiughvf" 
	    render r as JSON

    }

    @Secured(['ROLE_USER'])
    def unfeatureIt = {
        log.debug params;
        params.author = springSecurityService.currentUser;
        List groups = params['userGroup'].split(",").collect {
		    UserGroup.read(Long.parseLong(it))
		}

        def featuredInstance
        UserGroup.withTransaction() {
            groups.each { ug ->
                featuredInstance = Featured.findWhere(author: params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                if(!featuredInstance) {
                    return
                }
                try {
                    featuredInstance.delete(flush: true);
                    return
                }catch (org.springframework.dao.DataIntegrityViolationException e) {
				    flash.message = "${message(code: 'featured.delete.error', default: 'Error while unfeaturing')}"
		        }
            }
        }
        println "UNFEATURE function end sending msg==================================================="
        def r = ["success":true]
        def observationInstance = activityFeedService.getDomainObject(params.type,params.id); 
        def freshUGListHTML = g.render(template:"/common/showFeaturedTemplate" ,model:['observationInstance':observationInstance])
        println "=======HTML RENDER ========== " + freshUGListHTML
        r["freshUGListHTML"] = freshUGListHTML
	    render r as JSON

    }

    
    def searchIndex(type,obv){
        if(type == "species.participation.Observation"){
            println "======OBSERVATION ==========" + obv + " " + type
            observationsSearchService.publishSearchIndex(obv, true);
            println "=======DONE ==="
        }
        else if(type == "species.participation.Specie"){
             speciesSearchService.publishSearchIndex(obv, true);
        }
        else if(type == "content.eml.Document") {
            println "======DOCUMENT ==========" + obv + " " + type
            documentSearchService.publishSearchIndex(obv, true);
             println "=======DONE ==="

        }
    }

	@Secured(['ROLE_USER'])
	def flagIt = {
       	log.debug params;
        println "============%%%%%%%%%%%  FLAGGING  %%%%%%%%%%%==========="
        println params.type;
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
	            println "=======PUBLISH SEARCH START======"	
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
		println "FLAG function end sending msg==================================================="
        def r = ["success":true]
        def observationInstance = activityFeedService.getDomainObject(params.type,params.id); 
        def flagListUsersHTML = g.render(template:"/common/observation/flagListUsersTemplate" ,model:['observationInstance':observationInstance])
	    
        r['flagListUsersHTML'] = flagListUsersHTML
	    render r as JSON
	}

	@Secured(['ROLE_USER'])
	def deleteFlag = {
		log.debug params;
        params.author = springSecurityService.currentUser;
		def FlagInstance = Flag.findWhere(id: params.id.toLong());  //kaun si kiski id hai???
		def obv = activityFeedService.getDomainObject(FlagInstance.objectType,FlagInstance.objectId);    ///observation nikali hai...species bhi ho sakta hai
        

		if(!FlagInstance){
			//response.setStatus(500);
			//def message = [info: g.message(code: 'flag.alreadytDeleted', default:'Flag already deleted')];
			render obv.flagCount;
			return
		}
		try {
			FlagInstance.delete(flush: true);
            

			obv.flagCount--;
			obv.save(flush:true)
            println "=======PUBLISH SEARCH START======"
			searchIndex(params.type,obv);    //observation ke liye only
			render obv.flagCount;
			return;
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			def message = [error: g.message(code: 'flag.error.onDelete', default:'Error on deleting flag')];
			render message as JSON
		}
	}
}
