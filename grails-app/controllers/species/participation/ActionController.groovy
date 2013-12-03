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
import org.codehaus.groovy.grails.plugin.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.converters.XML;

import species.participation.Flag.FlagType
import species.participation.Follow
import grails.plugin.springsecurity.Secured
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

    private boolean saveActMail(params, Featured featuredInstance , obv, UserGroup ug) {
        if(!featuredInstance.save(flush:true)){
            featuredInstance.errors.allErrors.each { log.error it }
            return false
        }
        //NOTE: Putting rootHolder & activity holder Same for IBP group case
        def act = activityFeedService.addActivityFeed(obv, ug? ug : obv, featuredInstance.author, activityFeedService.FEATURED, featuredInstance.notes);
        searchIndex(params.type,obv)
        observationService.sendNotificationMail(act.activityType, obv, null, null, act)
        return true
    }

    def inGroups= {
        log.debug params;
        boolean status = false;
        def r = [:]

        def obv = activityFeedService.getDomainObject(params.type,params.id); 
        
        def resourceGroupHtml = ""

        if(obv) {
            resourceGroupHtml =  g.render(template:"/common/resourceInGroupsTemplate", model:['observationInstance': obv]);
            status = 'success';
        }
        r["status"] = status?'success':'error'
        r["resourceGroupHtml"] = resourceGroupHtml
        render r as JSON

    }

    @Secured(['ROLE_USER'])
    def featureIt = {  
        log.debug params;
        boolean status = false;
        String msg = '';
        def r = [:]
       
        params.author = springSecurityService.currentUser;

        def obv = activityFeedService.getDomainObject(params.type,params.id); 
        
        def ugParam = params['userGroup']
        def resourceGroupHtml

        if(ugParam != null && params.notes && obv) {
            List splitGroups = [];
           println ugParam 
             if(ugParam && ugParam.length() > 0){
                splitGroups = ugParam.split(",")
                 if(ugParam[-1] == ',') {//TODO:CHECK THIS
                    splitGroups.add("")
                }
            }
            else {
                splitGroups.add("")    
            }

            List groups = splitGroups.collect { (it == '') ? null : UserGroup.read(Long.parseLong(it)) }
            def featuredInstance;
            UserGroup.withTransaction() {
                groups.each { ug ->
                    println ug
                   if(ug == null) {
                        if(SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")) {
                        }
                        else {
                            msg = "You don't have the permission!!"
                            status = false;
                            r["status"] = status?'success':'error'
                            r['msg'] = msg
                            render r as JSON
                            return
                        }
                    }
                    else {  
                        if (ug.isFounder(params.author) || ug.isExpert(params.author)) {
                        }  
                        else {
                             msg = "You don't have the permission!!" 
                             status = false;
                             r["status"] = status?'success':'error'
                             r['msg'] = msg
                             render r as JSON
                             return
                        } 
                    }
                     try{
                        featuredInstance = Featured.findWhere(objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                        if(!featuredInstance) {
                            featuredInstance = new Featured(author:params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug, notes: params.notes)
                            status = saveActMail(params, featuredInstance, obv, ug);
                            obv.featureCount++
		                    if(!obv.save(flush:true)) {
                                obv.errors.allErrors.each { log.error it }
                            }
                            if(status) msg = "Successfully featured ${obv.class.simpleName}"
                        } 
                        else {
                            if(featuredInstance.author == params.author){
                                featuredInstance.notes = params.notes
                                featuredInstance.createdOn = new Date()
                                status = saveActMail(params, featuredInstance, obv, ug) 
                                if(status) msg = "Successfully updated notes for the featued ${obv.class.simpleName}"
                            }
                            else{
                                try{
                                    featuredInstance.delete(flush:true, failOnError:true)
                                }catch (Exception e) {
                                    e.printStackTrace()
                                }
                                featuredInstance = new Featured(author:params.author, objectId: params.id.toLong(), objectType: params.type, userGroup: ug, notes: params.notes)
                                status = saveActMail(params, featuredInstance, obv, ug)
                                if(status) msg = "Successfully featued ${obv.class.simpleName} again and updated notes given previously"

                            }
                        }
                    }catch (Exception e) {
                        status = false;
                        msg = "Error: ${e.getMessage()}";
                        e.printStackTrace()
                        log.error e.getMessage();
                    }
                 }
             } 
            
            if(!status)
                msg = "Error while featuring the ${obv.class.simpleName}. ${msg}"

            //freshUGListHTML = g.render(template:"/common/showFeaturedTemplate" ,model:['observationInstance':obv])
         }

        if(obv)
            resourceGroupHtml =  g.render(template:"/common/resourceInGroupsTemplate", model:['observationInstance': obv]);
        r["status"] = status?'success':'error'
        r['msg'] = msg
        r["resourceGroupHtml"] = resourceGroupHtml
        render r as JSON
    }

    @Secured(['ROLE_USER'])
    def unfeatureIt = {
        log.debug params;
        boolean status = false;
        String msg = '';
        def r = [:]

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
                   if(ug == null) {
                        if(SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")) {
                        }
                        else {
                            msg = "You don't have the permission!!"
                            status =false;
                            r["status"] = status?'success':'error'
                            r['msg'] = msg
                            render r as JSON
                            return
                        }
                    }
                    else { 
                        if (ug.isFounder(params.author) || ugroup.isExpert(params.author)) {
                        } 
                        else {
                             msg = "You don't have the permission!!" 
                             status = false;
                             r["status"] = status?'success':'error'
                             r['msg'] = msg
                             render r as JSON
                             return
                        } 
                    }
                featuredInstance = Featured.findWhere(objectId: params.id.toLong(), objectType: params.type, userGroup: ug)
                if(!featuredInstance) {
                    return
                }
                try {
                    if(!featuredInstance.delete(flush:true)){
                        featuredInstance.errors.allErrors.each { log.error it }
                    }
                    obv.featureCount--
                    if(!obv.save(flush:true)) {
                        obv.errors.allErrors.each { log.error it }
                    }

                    def act = activityFeedService.addActivityFeed(obv, ug? ug : obv, params.author, activityFeedService.UNFEATURED, featuredInstance.notes);
                    searchIndex(params.type,obv)
                    observationService.sendNotificationMail(act.activityType, obv, null, null, act)
                    status = true
                    if(status) {
                        msg = "Successfully removed featured ${obv.class.simpleName}"
                    }
                    return
                }catch (org.springframework.dao.DataIntegrityViolationException e) {
                    status false
                    if(!status){
                        msg = "Error while featuring the ${obv.class.simpleName}"                    
                    }
                    flash.message = "${message(code: 'featured.delete.error', default: 'Error while unfeaturing')}"
                }
            }
        }
        r["status"] = status?'success':'error'
        r['msg'] = msg
        def resourceGroupHtml =  g.render(template:"/common/resourceInGroupsTemplate", model:['observationInstance': obv]);
        r["resourceGroupHtml"] = resourceGroupHtml
        render r as JSON

    } 

	@Secured(['ROLE_USER'])
	def flagIt = { 
        log.debug params;
		params.author = springSecurityService.currentUser;
		def obv = activityFeedService.getDomainObject(params.type,params.id);     
		FlagType flag = observationService.getObservationFlagType(params.obvFlag?:FlagType.OBV_INAPPROPRIATE.name());    
		def flagInstance = Flag.findWhere(author: params.author,objectId: params.id.toLong(),objectType: params.type);
		if (!flagInstance) {
			try {
				flagInstance = new Flag(objectId: params.id.toLong(),objectType: params.type, author: params.author, flag:flag, notes:params.notes)
				flagInstance.save(flush: true)
				if(!flagInstance.save(flush:true)){
                    flagInstance.errors.allErrors.each { println it }
			        return null
		        }
                def activityNotes = flagInstance.flag.value() + ( flagInstance.notes ? " \n" + flagInstance.notes : "")
                obv.flagCount++
				obv.save(flush:true)
				def act = activityFeedService.addActivityFeed(obv, flagInstance, flagInstance.author, activityFeedService.OBSERVATION_FLAGGED, activityNotes); 
                searchIndex(params.type,obv)				
				observationService.sendNotificationMail(observationService.OBSERVATION_FLAGGED, obv, request, params.webaddress, act) 
				flash.message = "${message(code: 'flag.added', default: 'Flag added')}"
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
	    def msg = "Flagged..."
        r['msg'] = msg

        r['flagListUsersHTML'] = flagListUsersHTML
	    render r as JSON
	}

	@Secured(['ROLE_USER'])
	def deleteFlag  = {
		log.debug params;
        params.author = springSecurityService.currentUser;
		def flagInstance = Flag.read(params.id.toLong());  
		if(!flagInstance){
			return
		}
		try {
            def obv = activityFeedService.getDomainObject(flagInstance.objectType, flagInstance.objectId);
            def activityNotes = flagInstance.flag.value() + ( flagInstance.notes ? " \n" + flagInstance.notes : "")
			flagInstance.delete(flush: true);
            obv.flagCount--
			obv.save(flush:true)
            activityFeedService.addActivityFeed(obv, flagInstance, params.author, activityFeedService.REMOVED_FLAG, activityNotes);
			searchIndex(params.type,obv);    //observation ke liye only
            def message = [:]
            message['flagCount'] = obv.flagCount 
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
