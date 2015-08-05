package species.groups

import java.util.Map;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.plugin.springsecurity.ui.RegistrationCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.grails.taggable.*
import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;

//import species.DigestJob;
import species.auth.Role;
import species.participation.Digest;
import species.auth.SUser;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.participation.UserToken;
import species.utils.ImageUtils;
import species.utils.Utils;
import grails.converters.JSON;
import grails.converters.XML;
import grails.plugin.springsecurity.annotation.Secured;
import groovy.text.SimpleTemplateEngine
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import static org.springframework.http.HttpStatus.*;

class UserGroupController {

	def springSecurityService;
	def userGroupService;

	def mailService;
	def aclUtilService;
	def utilsService;
	def observationService;
	def emailConfirmationService;
	def namesIndexerService;
	def activityFeedService;
    def digestService;
	def customFieldService;
	
    def messageSource;
	
    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}

	def activity = {
		def userGroupInstance = findInstance(params.id, params.webaddress);
		if (userGroupInstance) {
			[userGroupInstance: userGroupInstance]
		}
	}
	
	def list() {
		Map model = getUserGroupList(params);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = 'group'
            model['obvListHtml'] =  g.render(template:"/common/userGroup/showUserGroupListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

       /*     model['filteredTags'] = userGroupService.getTagsFromUserGroup(model.totalUserGroupInstanceList.collect{it.id})
            model['tagsHtml'] = g.render(template:"/common/observation/showAllTagsTemplate", model:[tags:model.filteredTags, isAjaxLoad:true]);
            model['mapViewHtml'] = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[userGroupInstanceList:model.totalUserGroupInstanceList]);
            */
        }
 
        model = utilsService.getSuccessModel("Success in executing ${actionName} of ${params.controller}", null, OK.value(), model);

        withFormat {
            html { 
                if(!params.isGalleryUpdate?.toBoolean()){
                    render (view:"list", model:model.model)
                } else{
                    return
                }
            }
            json {
                model.model.remove('totalUserGroupInstanceList');
                render model as JSON
            }
            xml {
                model.model.remove('totalUserGroupInstanceList');
                render model as XML
            }
        }
	}

	def filteredList = {
		def result;
		//TODO: Dirty hack to feed results through solr if the request is from search
		if(params.action == 'search') {
			result = userGroupService.getUserGroupFromSearch(params)
		} else {
			result = getUserGroupList(params);
		}
		render (template:"/common/userGroup/showUserGroupListTemplate", model:result);
	}

	protected Map getUserGroupList(params) {
		def max = Math.min(params.max ? params.int('max') : 24, 100)
		def offset = params.offset ? params.int('offset') : 0

		def filteredUserGroup = userGroupService.getFilteredUserGroups(params, max, offset, false)

		def userGroupInstanceList = filteredUserGroup.userGroupInstanceList
		def queryParams = filteredUserGroup.queryParams
		def activeFilters = filteredUserGroup.activeFilters
		activeFilters.put("append", true);//needed for adding new page userGroup ids into existing session["uGroup_ids_list"]

		def totalUserGroupInstanceList = userGroupService.getFilteredUserGroups(params, -1, -1, true).userGroupInstanceList
		def count = totalUserGroupInstanceList.size()

		
		if(params.action == 'search' && !params.query) {
			count = 0
			totalUserGroupInstanceList = 0
			userGroupInstanceList = [:]
		}
		
		//storing this filtered obvs ids list in session for next and prev links
		//http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/1.8.2/org/codehaus/groovy/runtime/DefaultGroovyMethods.java
		//returns an arraylist and invalidates prev listing result
		if(params.append && session["uGroup_ids_list"]) {
			session["uGroup_ids_list"].addAll(userGroupInstanceList.collect {it.id});
		} else {
			session["uGroup_ids_list_params"] = params.clone();
			session["uGroup_ids_list"] = userGroupInstanceList.collect {it.id};
		}

		log.debug "Storing all userGroup ids list in session ${session['uGroup_ids_list']} for params ${params}";
		return [totalUserGroupInstanceList:totalUserGroupInstanceList, userGroupInstanceList: userGroupInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, 'resultType':'user group']
	}

	def listRelated = {
		switch(params.filterProperty) {
			case 'featuredObservations':
				redirect  url: uGroup.createLink(mapping: 'userGroup', action:'observation', params:['webaddress':params.webaddress]);
				break;
			case 'featuredMembers':
				redirect  url: uGroup.createLink(mapping: 'userGroup', action:'user',params:['webaddress':params.webaddress]);
				break;
			case 'obvRelatedUserGroups':
				redirect  url: uGroup.createLink(mapping: 'userGroupGeneric', action:'list', params:[observation:params.id]);
				break;
			case 'userUserGroups':
				redirect  url: uGroup.createLink(mapping: 'userGroupGeneric', action:'list', params:[user:params.id]);
				break;
			default:
				//flash.message "Invalid command"
				redirect  url: uGroup.createLink(mapping: 'userGroupGeneric', action:'list')
		}
		return
	}

	@Secured(['ROLE_USER'])
	def create() {
		def userGroupInstance = new UserGroup()
		userGroupInstance.properties = params
		return [userGroupInstance: userGroupInstance, currentUser:springSecurityService.currentUser]
	}

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def save() {
		params.domain = Utils.getDomainName(request)
		params.locale_language = utilsService.getCurrentLanguage(request);
		def userGroupInstance = userGroupService.create(params);
		if (userGroupInstance.hasErrors()) {
			userGroupInstance.errors.allErrors.each { log.error it }
			render(view: "create", model: [userGroupInstance: userGroupInstance])
		}
		else {
			log.debug "Successfully created usergroup : "+userGroupInstance
			activityFeedService.addActivityFeed(userGroupInstance, null, springSecurityService.currentUser, activityFeedService.USERGROUP_CREATED);
			customFieldService.addToGroup(params.customFieldMapList, userGroupInstance)
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label'), userGroupInstance.webaddress])}"
			redirect  url: uGroup.createLink(mapping: 'userGroup', action: "show", params:['webaddress': userGroupInstance.webaddress])
		}
	}

	def show() {
		def userGroupInstance = findInstance(params.id, params.webaddress);
		if (userGroupInstance) {
			userGroupInstance.incrementPageVisit();
            def model = utilsService.getSuccessModel("", userGroupInstance, OK.value())
            withFormat {
                html{
                    def userLanguage = utilsService.getCurrentLanguage(request);
                    if(params.pos) {
                        int pos = params.int('pos');
                        def prevNext = getPrevNextUserGroups(pos);
                        if(prevNext) {
                            return [userGroupInstance: userGroupInstance, prevUserGroupId:prevNext.prevUserGroup, nextUserGroupId:prevNext.nextUserGroupId, lastListParams:prevNext.lastListParams, userLanguage:userLanguage]
                        } else {
                            return [userGroupInstance: userGroupInstance, userLanguage:userLanguage]
                        }
                    } else {
                        return [userGroupInstance: userGroupInstance, userLanguage:userLanguage]
                    }
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		} 
        /*else {
            def model = utilsService.getErrorModel(messageSource.getMessage("id.required", ['Valid id'] as Object[], RCU.getLocale(request)), userGroupInstance, NOT_FOUND.value())
            withFormat {
                html {
                    //flash.message = model.msg
                    //redirect(action: "list")
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }*/

	}

	/**
	 *
	 * @param pos
	 * @return
	 */
	private def getPrevNextUserGroups(int pos) {
		def lastListParams = session["uGroup_ids_list_params"]?.clone();
		if(lastListParams) {
			if(!session["uGroup_ids_list"]) {
				log.debug "Fetching userGroups list as its not present in session "
				runLastListQuery(lastListParams);
			}

			log.debug "Current ids list in session ${session['uGroup_ids_list']} and position ${pos}";
			def nextId = (pos+1 < session["uGroup_ids_list"].size()) ? session["uGroup_ids_list"][pos+1] : null;
			if(nextId == null) {
				lastListParams.put("append", true);
				def max = Math.min(lastListParams.max ? lastListParams.int('max') : 9, 100)
				def offset = lastListParams.offset ? lastListParams.int('offset') : 0
				lastListParams.offset = offset + max;
				log.debug "Fetching new page of userGroups using params ${lastListParams}";
				runLastListQuery(lastListParams);
				nextId = (pos+1 < session["uGroup_ids_list"].size()) ? session["uGroup_ids_list"][pos+1] : null;
			}
			def prevId = pos > 0 ? session["uGroup_ids_list"][pos-1] : null;

			lastListParams.isGalleryUpdate = false;
			return ['prevObservationId':prevId, 'nextObservationId':nextId, 'lastListParams':lastListParams];
		}
	}

	private void runLastListQuery(Map params) {
		log.debug params;
		if(params.action == 'search') {
			userGroupService.getUserGroupsFromSearch(params);
		} else {
			getUserGroupList(params);
		}
	}

	@Secured(['ROLE_USER'])
	def edit() {
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (!userGroupInstance) {
			//flash.message = "${message(code: 'userGroup.default.not.found.message', args: [params.webaddress])}"
			//redirect(action: "list")
		} else if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION)) {
			render(view: "create", model: [userGroupInstance: userGroupInstance, 'springSecurityService':springSecurityService])
		} else {
			flash.message = "${message(code: 'default.not.permitted.message', args: [params.action, message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.name])}"
			redirect  url: uGroup.createLink(mapping: 'userGroupGeneric', action: "list")
		}
	}

	@Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def update() {
		log.debug params;
		params.locale_language = utilsService.getCurrentLanguage(request);
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (userGroupInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (userGroupInstance.version > version) {

					userGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'userGroup.label', default: 'UserGroup')]
					as Object[], "Another user has updated this UserGroup while you were editing")
					render(view: "edit", model: [userGroupInstance: userGroupInstance])
					return
				}
			}
			params.domain = Utils.getDomainName(request)
			userGroupService.update(userGroupInstance, params)
			if (userGroupInstance.hasErrors()) {
				userGroupInstance.errors.allErrors.each { log.error it }
				render(view: "create", model: [userGroupInstance: userGroupInstance])
			}
			else {
				log.debug "Successfully updated usergroup : "+userGroupInstance
				activityFeedService.addActivityFeed(userGroupInstance, null, springSecurityService.currentUser, activityFeedService.USERGROUP_UPDATED);
				customFieldService.addToGroup(params.customFieldMapList, userGroupInstance)
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.name])}"
				if(params.founders) {
					def messagesourcearg = new Object[1];
                messagesourcearg[0] = params.founders;
					flash.message += messageSource.getMessage("info.email.join.founders", messagesourcearg, RCU.getLocale(request))
				}
				redirect  url: uGroup.createLink(mapping: 'userGroup', action: "show", userGroup:userGroupInstance)
			}
		}
	
	}

	@Secured(['ROLE_USER'])
	def delete() {
		log.debug params;
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (userGroupInstance) {
			try {
				userGroupService.delete(userGroupInstance)
				activityFeedService.deleteFeed(userGroupInstance);
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.webaddress])}"
				redirect url: uGroup.createLink(mapping: 'userGroupGeneric',action: "list")
				return
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.webaddress])}"
				redirect url: uGroup.createLink(mapping: 'userGroup',action: "show", params:['webaddress': params.webaddress])
				return;
			}
		}
	}

	private UserGroup findInstance(id=null, String webaddress='', boolean redirectToList=true) {
		def userGroup
		
		if(id) {
			if(id instanceof String) {
                try {
				    id = Long.parseLong(id);
                } catch(e) {
                    id = null;
                    e.printStackTrace();
                }
			}
            if(id)
			    userGroup = UserGroup.get(id)
		} 
		if(webaddress) {
			userGroup = UserGroup.findByWebaddress(webaddress)
		}
		
		if (!userGroup && redirectToList) {
            def model = utilsService.getErrorModel( "${message(code: 'default.not.found.message', args: ['UserGroup', id?:webaddress])}", userGroup, NOT_FOUND.value())
                withFormat {
                    html {
                        flash.message = model.msg
                        redirect url: uGroup.createLink(controller:'userGroup', action:'list')
                        return;
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
                return;
		}
		return userGroup
	}

	def user() {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
        println "11111111111111111111111"
		if (!userGroupInstance) return
println "2222222222222222222"
		params.max = Math.min(params.max ? params.int('max') : 12, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def allMembers;
		if(params.onlyMembers?.toBoolean()) {
			allMembers = userGroupInstance.getMembers(params.max, params.offset, params.sort);
		} else {
			allMembers = userGroupInstance.getAllMembers(params.max, params.offset, params.sort);
		}
		
	        
        def instanceTotal = userGroupInstance.getAllMembersCount();
        def model = ['userGroupInstance':userGroupInstance, 'userInstanceList':allMembers, 'instanceTotal':instanceTotal, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'membersTotalCount':instanceTotal, 'expertsTotalCount': userGroupInstance.getExpertsCount()]

        model = prepareRenderUserModel(params, model);

        withFormat {
            html {
                renderUsersModel(params, model);
                return;
            }
            json { render model as JSON }
            xml { render model as XML }
        }
	}

    private prepareRenderUserModel(params, model) {
        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model['resultType'] = 'user'
            model['obvListHtml'] =  g.render(template:"/common/suser/showUserListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

            //			def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
            //			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
            //			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);
        }
        model = utilsService.getSuccessModel('', null, OK.value(), model);
        return model;
    }

	private renderUsersModel (params,  model) {

        if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/common/suser/showUserListTemplate", model:model.model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render( view:'user', model: model.model);
            return;
		} else {
			params.remove('isGalleryUpdate');
			return;
		}
	}
	
	def founders() {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return

		params.max = Math.min(params.max ? params.int('max') : 12, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def founders = userGroupInstance.getFounders(params.max, params.offset);

		def instanceTotal = userGroupInstance.getFoundersCount();
		def model = ['userGroupInstance':userGroupInstance, 'userInstanceList':founders, 'instanceTotal':instanceTotal, 'foundersTotalCount':instanceTotal, 'membersTotalCount':userGroupInstance.getAllMembersCount(), 'expertsTotalCount':userGroupInstance.getExpertsCount()]
        
        model = prepareRenderUserModel(params, model);
        
		withFormat {
            html{
                renderUsersModel(params, model);
                return;
            }
            json { 
                render model as JSON 
            }
            xml { render model as XML }
        }
	}

	def moderators () {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return

		params.max = Math.min(params.max ? params.int('max') : 12, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def experts = userGroupInstance.getExperts(params.max, params.offset);

		def instanceTotal = userGroupInstance.getExpertsCount();
		def model = ['userGroupInstance':userGroupInstance, 'userInstanceList':experts, 'instanceTotal':instanceTotal, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'membersTotalCount':userGroupInstance.getAllMembersCount(), 'expertsTotalCount':instanceTotal]

        model = prepareRenderUserModel(params, model);


    	withFormat {
            html{
                renderUsersModel(params, model);
                return;
            }
            json { render model as JSON }
            xml { render model as XML }
        }

	}

	def observation() {
		def model = getUserGroupObservationsList(params);
        def model2 = utilsService.getSuccessModel("", null, OK.value(), model)
        withFormat {
            html{
                if(params.loadMore?.toBoolean()){
                    render(template:"/common/observation/showObservationListTemplate", model:model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    render(view:"observation", model:model);
                    return;
                } else {
                    def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
                    def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
                    def tagsHtml = "";
                    if(model.showTags) {
                        //	def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
                        //	tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true, 'userGroup':model.userGroup]);
                    }
                    //def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model2.observationInstanceList, 'userGroup':model2.userGroup]);

                    def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, instanceTotal: model.instanceTotal]
                    render result as JSON
                    return;
                }
                json { render model2 as JSON }
                xml { render model2 as XML }
            }

        }
    }

	def getUserGroupObservationsList(params) {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return

		params.max = Math.min(params.max ? params.int('max') : 24, 100)
		params.offset = params.offset ? params.int('offset') : 0
		
		def model = observationService.getUserGroupObservations(userGroupInstance, params, params.max, params.offset);
		
        def observationInstanceList = model.observationInstanceList
        def checklistCount =  model.checklistCount
		def allObservationCount =  model.allObservationCount
		model['checklistCount'] = checklistCount
		model['instanceTotal'] = allObservationCount
		model['observationCount'] = allObservationCount-checklistCount
		//model['totalObservationInstanceList'] = model2.observationInstanceList;
		model['userGroupInstance'] = userGroupInstance;
		//model2['userGroup'] = userGroupInstance;
		

		if(params.append?.toBoolean()) {
            def groupObvList = session[userGroupInstance.webaddress+"obv_ids_list"]
            if(!groupObvList){
                session[userGroupInstance.webaddress+"obv_ids_list"] = []
            }
			    session[userGroupInstance.webaddress+"obv_ids_list"].addAll(model.observationInstanceList.collect {it.id});
		} else {
			session[userGroupInstance.webaddress+"obv_ids_list_params"] = params.clone();
			session[userGroupInstance.webaddress+"obv_ids_list"] = model.observationInstanceList.collect {it.id};
		}
		
		log.debug "Storing all observations ids list in session ${session[userGroupInstance.webaddress+'obv_ids_list']} for params ${params}";
		return model;
	}
	
	def filteredMapBasedObservationsList = {
		def model = getUserGroupObservationsList(params);
        def model2 = utilsService.getSuccessModel("", null, OK.value(), model)
        withFormat {
            html{
		        render (template:"/common/observation/showObservationListTemplate", model:model);
            }
            json {render model2 as JSON}
            xml {render model2 as XML}
        }
	}

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def settings() {
		log.debug params
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (!userGroupInstance) return

			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION)) {
				userGroupInstance.homePage = params.homePage ?: userGroupInstance.homePage 
				userGroupInstance.theme = params.theme ?: userGroupInstance.theme
				
				if(!userGroupInstance.save(flush:true)){
					userGroupInstance.errors.allErrors.each { log.error it }
				}
				return ['userGroupInstance':userGroupInstance]
			}
		return;
    }

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
    def joinUs() {
        def msg
        def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
        if (!userGroupInstance) {
            msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
            def model = utilsService.getErrorModel(msg, null, OK.value())
            withFormat {
                html {
                    flash.message = msg;
                    render (['success':true,'statusComplete':false, 'msg':msg] as JSON);
                }
                json { render model as JSON }
                xml { render model as XML }
            }
            return;
        }

        def user = springSecurityService.currentUser;
        if(user) {
            if(userGroupInstance.isMember(user)) {
                msg = messageSource.getMessage("default.already.user", ['member'] as Object[], RCU.getLocale(request))

                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
                        flash.message = msg;
                        render ([success:true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg]as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }


            } else {
                if(userGroupInstance.addMember(user)) {

                    msg = messageSource.getMessage("userGroup.joined.to.contribution", [userGroupInstance.name] as Object[], RCU.getLocale(request))
                    def model = utilsService.getSuccessModel(msg, null, OK.value())
                    model['shortMsg'] = messageSource.getMessage("info.joined", null, RCU.getLocale(request));
                    withFormat {
                        html {
                            flash.message = msg;
                            render ([success:true, 'statusComplete':true, 'shortMsg':model.shortMsg, 'msg':msg]as JSON);
                        } 
                        json { render model as JSON }
                        xml { render model as XML }
                    }

                    return;
                }
            }

            msg = messageSource.getMessage("default.not.able.process", null, RCU.getLocale(request))
            def model = utilsService.getErrorModel(msg, null, OK.value())
            model['shortMsg'] = 'Cannot process now';
            withFormat {
                html {
                    flash.error = msg;
                    render ([success:true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg]as JSON);
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    } 

	@Secured(['ROLE_USER'])
	def inviteMembers() {
		def msg
		List members = Utils.getUsersList(params.memberUserIds);

		if(members) {
			def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
			if (!userGroupInstance) {
				msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
				        render (['success':true, 'statusComplete':false, 'shortMsg':msg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
				return;
			}

			int membersCount = members.size();
			userGroupService.sendMemberInvitation(userGroupInstance, members, Utils.getDomainName(request), params.message);
			msg = messageSource.getMessage("default.usergroup.send.invite.success", [members.size()] as Object[], RCU.getLocale(request))
			if(membersCount > members.size()) {
				int alreadyMembersCount = membersCount-members.size();

				msg += messageSource.getMessage("default.usergroup.send.invite.success.contd", [alreadyMembersCount] as Object[], RCU.getLocale(request))	
            }
            def model = utilsService.getSuccessModel(msg, null, OK.value())
            model['shortMsg'] = 'Sent request';
            withFormat {
                html {
                    render (['success':true, 'statusComplete':true, 'shortMsg':model.shortMsg, 'msg':msg] as JSON)
                } 
                json { render model as JSON }
                xml { render model as XML }
            }
			return
		}
		msg = messageSource.getMessage("default.provide.details.invite", null, RCU.getLocale(request))
        def model = utilsService.getErrorModel(msg, null, OK.value())
        model['shortMsg'] = 'Please provide details';
        withFormat {
            html {
                flash.error = msg;
		        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON)
            }
            json { render model as JSON }
            xml { render model as XML }
        }
	}
	
	@Secured(['ROLE_USER'])
	def inviteExperts() {
		List members = Utils.getUsersList(params.expertUserIds);
		def msg

		if(members) {
			def userGroupInstance = findInstance(params.id, params.webaddress)
			if (!userGroupInstance) {
				msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
				        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }

				return;
			}
			
			int membersCount = members.size();
			
			def expertRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
			def groupExperts = UserGroupMemberRole.findAllByUserGroupAndRole(userGroupInstance, expertRole).collect {it.sUser};
			def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
			def groupFounders = UserGroupMemberRole.findAllByUserGroupAndRole(userGroupInstance, founderRole).collect {it.sUser};
			groupExperts.addAll(groupFounders)
			members.removeAll(groupExperts);
			
			userGroupService.sendExpertInvitation(userGroupInstance, members, params.message, Utils.getDomainName(request));
			msg = messageSource.getMessage("default.usergroup.send.invite.success", [members.size()] as Object[], RCU.getLocale(request))			
			if(membersCount > members.size()) {
				int alreadyMembersCount = membersCount-members.size();

				msg += messageSource.getMessage("default.usergroup.send.invite.success.contd", [alreadyMembersCount] as Object[], RCU.getLocale(request))
			}
            
            def model = utilsService.getSuccessModel(msg, null, OK.value())
            model['shortMsg'] = 'Sent request';
            withFormat {
                html {
			        render (['success':true, 'statusComplete':true, 'shortMsg':model.shortMsg, 'msg':msg] as JSON)
                } 
                json { render model as JSON }
                xml { render model as XML }
            }

			return
		}
		msg = messageSource.getMessage("default.provide.details.invite", null, RCU.getLocale(request))
        def model = utilsService.getErrorModel(msg, null, OK.value())
        model['shortMsg'] = 'Please provide details';
        withFormat {
            html {
		        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON)
            }
            json { render model as JSON }
            xml { render model as XML }
        }
	}


	@Secured(['ROLE_USER'])
	def requestMembership() {
		def user = springSecurityService.currentUser;
		def msg;
		if(user) {
			def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
			if (!userGroupInstance) {
				msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
				        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }


				return;
			}

			if(userGroupInstance.isMember(user)) {
				msg = messageSource.getMessage("default.already.user", ['member'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
                        render (['success':true, 'statusComplete':false, 'shortMsg':msg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }


				return;
			}
			
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			def founders = userGroupInstance.getFounders(userGroupInstance.getFoundersCount() as int, 0L);
            founders.addAll(userGroupInstance.getExperts(userGroupInstance.getExpertsCount() as int, 0L));
            msg = messageSource.getMessage("default.confirm.membership",['membership'] as Object[], RCU.getLocale(request))
			founders.each { founder ->
				log.debug "Sending email to  founder ${founder}"
				def userToken = new UserToken(username: user."$usernameFieldName", controller:'userGroupGeneric', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':user.id.toString(), 'role':UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value()]);
				userToken.save(flush: true)
				def userLanguage = utilsService.getCurrentLanguage();
				emailConfirmationService.sendConfirmation(founder.email,
						msg ,  [founder:founder, user: user, userGroupInstance:userGroupInstance,domain:Utils.getDomainName(request), view:'/emailtemplates/'+userLanguage.threeLetterCode+'/requestMembership'], userToken.token);
			}
			msg = messageSource.getMessage("default.sent.request.admin", null, RCU.getLocale(request))
            def model = utilsService.getSuccessModel(msg, null, OK.value())
            model['shortMsg'] = 'Sent request';
            withFormat {
                html {
			        render (['success':true, 'statusComplete':true, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                } 
                json { render model as JSON }
                xml { render model as XML }
            }
			return;
		}
		msg = messageSource.getMessage("default.login.confirm", null, RCU.getLocale(request))
        def model = utilsService.getErrorModel(msg, null, OK.value())
        model['shortMsg'] = 'Please login';
        withFormat {
            html {
		        render (['success':true,'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
            }
            json { render model as JSON }
            xml { render model as XML }
        }

	}
	
	@Secured(['ROLE_USER'])
	def requestModeratorship() {
		def user = springSecurityService.currentUser;
		def msg;
		if(user) {
			def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
			if (!userGroupInstance) {
				msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = msg;
                withFormat {
                    html {
				        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
				return;
			}

			if(userGroupInstance.isExpert(user)) {
				msg = messageSource.getMessage("default.already.user", ['moderator'] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value())
                model['shortMsg'] = 'Already a moderator';
                withFormat {
                    html {
				        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
				return;
			}
			
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			def founders = userGroupInstance.getFounders(userGroupInstance.getFoundersCount().toInteger(), 0L);
			founders.addAll(userGroupInstance.getExperts(userGroupInstance.getExpertsCount().toInteger(), 0L));
			msg = messageSource.getMessage("default.confirm.membership", ['moderator'] as Object[], RCU.getLocale(request))
			founders.each { founder ->
				log.debug "Sending email to  founder or expert ${founder}"
				def userToken = new UserToken(username: user."$usernameFieldName", controller:'userGroupGeneric', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':user.id.toString(), 'role':UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value()]);
				userToken.save(flush: true)
				def userLanguage = utilsService.getCurrentLanguage();
				emailConfirmationService.sendConfirmation(founder.email,
						msg,  [founder:founder, message:params.message, user: user, userGroupInstance:userGroupInstance,domain:Utils.getDomainName(request), view:'/emailtemplates/'+userLanguage.threeLetterCode+'/requestModeratorship'], userToken.token);
			}
			msg = messageSource.getMessage("default.sent.request.admin", null, RCU.getLocale(request))
            def model = utilsService.getSuccessModel(msg, null, OK.value())
            model['shortMsg'] = 'Sent request';
            withFormat {
                html {
			        render (['success':true, 'statusComplete':true, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                } 
                json { render model as JSON }
                xml { render model as XML }
            }
			return;
		}
		msg = messageSource.getMessage("default.login.confirm", null, RCU.getLocale(request))
        def model = utilsService.getErrorModel(msg, null, OK.value())
        model['shortMsg'] = 'Please login';
        withFormat {
            html {
		        render (['success':true,'statusComplete':false, 'shortMsg': model.shortMsg, 'msg':msg] as JSON);
            }
            json { render model as JSON }
            xml { render model as XML }
        }

	}
	
	private String generateLink( String controller, String action, linkAttrs, linkParams, request) {
		linkAttrs.controller = controller
		linkAttrs.action = action
		linkAttrs.absolute = true
		linkAttrs.params = linkParams
		uGroup.createLink(linkAttrs)
	}
	
//	private String generateLink( String controller, String action, linkParams, request) {
//		uGroup.createLink(base: Utils.getDomainServerUrl(request),
//				controller:controller, action: action,
//				params: linkParams)
//	}

    private boolean findDesignation (user, ug, role) {
    	def msg
        switch(role) {
            case UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value():
                if(ug.isFounder(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'founder'] as Object[], RCU.getLocale(request))
                    return true
                }
                else if (ug.isExpert(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'moderator'] as Object[], RCU.getLocale(request))
                    return true
                }
                else if( ug.isMember(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'member'] as Object[], RCU.getLocale(request))
                    return true
                }
                else {
                    return false
                }
                break;
            case UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value():
                if(ug.isFounder(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'founder'] as Object[], RCU.getLocale(request))
                    return true
                }
                else if (ug.isExpert(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'moderator'] as Object[], RCU.getLocale(request))
                    return true
                }
                else {
                    return false
                }
                break;
            case UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value():
                if(ug.isFounder(user)) {
                    flash.message=messageSource.getMessage("default.usergroup.already.label", [user,'founder'] as Object[], RCU.getLocale(request))
                    return true
                }
                else {
                    return false
                }
                break;
            	default: log.error "No proper role type defined"
        }
    }

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def confirmMembershipRequest() {
		log.debug params;
		def msg;
		if(params.userId && params.userGroupInstanceId) {
			def user;
			if(params.userId == 'register') {
				user = springSecurityService.currentUser
			} else {
				user = SUser.read(params.userId.toLong())
			}
			def userGroupInstance = UserGroup.read(params.userGroupInstanceId.toLong());
            //Founder can approve for all 3 roles here
            //Expert can only approve for member and expert
            //expert is not getting a mail to confirm for founder so it works fine
			//Same func also used to accept invitation--important
            if(user && userGroupInstance && (user.id.toLong() == springSecurityService.currentUser.id || userGroupInstance.isFounder() || userGroupInstance.isExpert())) {
				switch(params.role) {
					case UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value():
					    boolean b = findDesignation(user, userGroupInstance, params.role)
                        if(b) {
                            break;
                        }
                        if(userGroupInstance.addMember(user)) {
                            flash.message=messageSource.getMessage("default.addresource.success", [user,'member'] as Object[], RCU.getLocale(request))
						}
						break;
					case UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value():
						boolean b = findDesignation(user, userGroupInstance, params.role)
                        if(b) {
                            break;
                        }
                        if(userGroupInstance.addFounder(user)) {
							flash.message=messageSource.getMessage("default.addresource.success", [user,'founder'] as Object[], RCU.getLocale(request))
						}
						break;
					case UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value():
						boolean b = findDesignation(user, userGroupInstance, params.role)
                        if(b) {
                            break;
                        }
                        if(userGroupInstance.addExpert(user)) {
							flash.message=messageSource.getMessage("default.addresource.success", [user,'moderator'] as Object[], RCU.getLocale(request))
						}
						break;
					default: log.error "No proper role type is specified."
				}
				def conf = PendingEmailConfirmation.findByConfirmationToken(params.confirmationToken);
				if(conf) {
					log.debug "Deleting confirmation code and usertoken params";
					conf.delete();
					UserToken.get(params.tokenId.toLong())?.delete();
				}
			} else {
				if(user && userGroupInstance) {
					flash.error=messageSource.getMessage("default.userCouldntaddedToGroup.permission", null, RCU.getLocale(request))				} else {
					flash.error=messageSource.getMessage("default.userCouldntaddedToGroup.permission", null, RCU.getLocale(request))
				}
			}
			redirect url: uGroup.createLink(mapping: 'userGroup', action:"show", 'userGroup':userGroupInstance);
			return
		}
		flash.error=messageSource.getMessage("default.userPermission.to.confirmation", null, RCU.getLocale(request))
		redirect url: uGroup.createLink(mapping: 'userGroupGeneric', action:"list");
	}

	@Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def leaveUs() {
		def msg;
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) {
			msg = messageSource.getMessage("default.not.selected", ['userGroup'] as Object[], RCU.getLocale(request))
            def model = utilsService.getErrorModel(msg, null, OK.value())
            model['shortMsg'] = msg;
            withFormat {
                html {
			        flash.error = msg
			        render (['success':true, 'statusComplete':false, 'shortMsg':model.shortMsg, 'msg':msg] as JSON);
                } 
                json { render model as JSON }
                xml { render model as XML }
            }
			return;
		}

		def user = springSecurityService.currentUser;
		if(user && userGroupInstance.deleteMember(user)) {
			activityFeedService.addActivityFeed(userGroupInstance, user, user, activityFeedService.MEMBER_LEFT);
			msg = messageSource.getMessage("default.thankYouForBeingWithUS", null, RCU.getLocale(request))
            def model = utilsService.getSuccessModel(msg, null, OK.value())
            model['shortMsg'] = 'Thank you';
            withFormat {
                html {
                    flash.message = msg
			        render (['msg':msg, 'shortMsg':model.shortMsg, 'success':true, 'statusComplete':true] as JSON);
                } 
                json { render model as JSON }
                xml { render model as XML }
            }
			return;
		}
		msg = messageSource.getMessage("default.YourPresenceImportant", null, RCU.getLocale(request))
        def model = utilsService.getErrorModel(msg, null, OK.value())
        model['shortMsg'] = 'Cannot let you leave';
        withFormat {
            html {
                flash.error = msg
                render (['msg':msg, 'shortMsg':model.shortMsg, 'success':true, 'statusComplete':false]as JSON);
            } 
            json { render model as JSON }
            xml { render model as XML }
        }
	}
   

	def about() {
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (!userGroupInstance) return;
		def userLanguage = utilsService.getCurrentLanguage(request);
		return ['userGroupInstance':userGroupInstance, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'expertsTotalCount':userGroupInstance.getExpertsCount(), 'membersTotalCount':userGroupInstance.getAllMembersCount(),userLanguage:userLanguage]
	}

	def getRelatedUserGroups() {
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		if(!params.id) return;

		def observationInstance = Observation.get(params.long('id'))
		if (!observationInstance) {
			flash.message = messageSource.getMessage("default.Not.Founded", ['Observation',params.id] as Object[], RCU.getLocale(request))
			return;
		}

		def userGroups = userGroupService.getObservationUserGroups(observationInstance, max, offset);

		def result = [];
		userGroups.each {
			result.add(['observation':it, 'title':it.name]);
		}

		def r = ["observations":result, "count":userGroupService.getNoOfObservationUserGroups(observationInstance)]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations, '');
		}
		render r as JSON
	}

	def getFeaturedUserGroups() {
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		def userGroups = userGroupService.getSuggestedUserGroups(null);

		def result = [];
		userGroups.each {
			result.add(['observation':it, 'title':it.name]);
		}

		def r = ["observations":result, "count":result.size()]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations, '');
		}
		render r as JSON
	}

	@Secured(['ROLE_USER'])
	def upload_resource() {
		log.debug params;

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				def rs = [:]
				Utils.populateHttpServletRequestParams(request, rs);
				def resourcesInfo = [];
				def rootDir = grailsApplication.config.speciesPortal.userGroups.rootDir
				File userGroupDir;
				def message;

				if(!params.resources) {
					message = g.message(code: 'no.file.attached', default:'No file is attached')
				}

				params.resources.each { f ->
					log.debug "Saving userGroup logo file ${f.originalFilename}"

					// List of OK mime-types
					//TODO Move to config
					def okcontents = [
						'image/png',
						'image/jpeg',
						'image/pjpeg',
						'image/gif',
						'image/jpg'
					]

					if (! okcontents.contains(f.contentType)) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							okcontents,
							f.originalFilename
						])
					}
					else if(f.size > grailsApplication.config.speciesPortal.userGroups.logo.MAX_IMAGE_SIZE) {
						message = g.message(code: 'resource.file.invalid.max.message', args: [
							grailsApplication.config.speciesPortal.userGroups.logo.MAX_IMAGE_SIZE/1024,
							f.originalFilename,
							((int)f.size/1024)+'KB'
						], default:"File size cannot exceed ${grailsApplication.config.speciesPortal.userGroups.logo.MAX_IMAGE_SIZE/1024}KB");
					}
					else if(f.empty) {
						message = g.message(code: 'file.empty.message', default:'File cannot be empty');
					}
					else {
						if(!userGroupDir) {
							if(!params.dir) {
								userGroupDir = new File(rootDir);
								if(!userGroupDir.exists()) {
									userGroupDir.mkdir();
								}
								userGroupDir = new File(userGroupDir, UUID.randomUUID().toString()+File.separator+"resources");
								userGroupDir.mkdirs();
							} else {
								userGroupDir = new File(rootDir, params.dir);
								userGroupDir.mkdir();
							}
						}

						File file = utilsService.getUniqueFile(userGroupDir, Utils.generateSafeFileName(f.originalFilename));
						f.transferTo( file );
						ImageUtils.createScaledImages(file, userGroupDir);
						resourcesInfo.add([fileName:file.name, size:f.size]);
					}
				}
				log.debug resourcesInfo
				// render some XML markup to the response
				if(userGroupDir && resourcesInfo) {
					render(contentType:"text/xml") {
						userGroup {
							dir(userGroupDir.absolutePath.replace(rootDir, ""))
							resources {
								for(r in resourcesInfo) {
									image('fileName':r.fileName, 'size':r.size){}
								}
							}
						}
					}
				} else {
					response.setStatus(500)
					message = [error:message]
					render message as JSON
				}
			} else {
				response.setStatus(500)
				def message = [error:g.message(code: 'no.file.attached', default:'No file is attached')]
				render message as JSON
			}
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			def message = [error:g.message(code: 'file.upload.fail', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	def getFeaturedObservations() {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return;

		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0
		params.sort = "visitCount";
		def model = observationService.getUserGroupObservations(userGroupInstance, params, max, offset);

		def result = [];
		model.observationInstanceList.each {
			result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
		}

		def r = ["observations":result, "count":model.observationInstanceTotal]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations);
		}
		render r as JSON
	}

	def getFeaturedMembers() {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return;

		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		params.sort = "activity";
		//TODO:sort on activity
		def members = UserGroupMemberRole.findAllByUserGroup(userGroupInstance, [max:max, offset:offset]).collect { it.sUser};

		def result = [];
		members.each {
			result.add(['observation':it, 'title':it.name]);
		}

		def r = ["observations":result, "count":userGroupInstance.getAllMembersCount()]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations, "");
		}
		render r as JSON
	}

	def getUserUserGroups() {
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		def userInstance = SUser.get(params.long('id'))
		if (!userInstance) {
			flash.message = messageSource.getMessage("default.not.found", ['SUser',params.id] as Object[], RCU.getLocale(request))
            def model = utilsService.getErrorModel(flash.message, null, OK.value())
            render model as JSON
			return;
		}

		def userGroups = userGroupService.getUserUserGroups(userInstance, max, offset);
		def result = [];
		userGroups.each {key, value ->
            value.each {
			    result.add(['observation':it.userGroup, 'title':it.userGroup.name]);
            }
		}

		def r = ["observations":result, "count":userGroupService.getNoOfUserUserGroups(userInstance)]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations, '');
		}
	
        def model = utilsService.getSuccessModel("", null, OK.value(), r);
	    render model as JSON
	}
	
	def actionsHeader() {
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (!userGroupInstance) return;
		render (template:"/common/userGroup/actionsHeaderTemplate", model:['userGroupInstance':userGroupInstance]);
	}

	def pages() {
		def userGroupInstance = findInstance(null, params.webaddress, false)
		//if (!userGroupInstance) return;
		def newsletters = userGroupService.getNewsLetters(userGroupInstance, params.max, params.offset, params.sort, params.order);
		render (view:"pages", model:['userGroupInstance':userGroupInstance, 'newsletters':newsletters])
	} 
	
	def page() {
		def userGroupInstance = findInstance(null, params.webaddress, false)
		//if (!userGroupInstance) return;
		render (view:'page', model:['userGroupInstance':userGroupInstance, 'newsletterId':params.id])
	} 
	
	def pageCreate() {
		def userGroupInstance = findInstance(null, params.webaddress, false)
		//if (!userGroupInstance) return;
		render (view:'pageCreate', model:['userGroupInstance':userGroupInstance])
	}
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search() {
		def model = getUserGroupList(params);
		if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"search", model:model)
		} else{
			def userGroupListHtml =  g.render(template:"/common/userGroup/showUserGroupListTemplate", model:model);
			def userGroupFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def filteredTags = userGroupService.getTagsFromUserGroup(model.totalUserGroupInstanceList.collect{it.id})
			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[tags:filteredTags, isAjaxLoad:true]);
			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[userGroupInstanceList:model.totalUserGroupInstanceList]);

			def result = [obvListHtml:userGroupListHtml, obvFilterMsgHtml:userGroupFilterMsgHtml, tagsHtml:tagsHtml, mapViewHtml:mapViewHtml]
			render result as JSON
		}
	}
	/**
	* Ajax call used by autocomplete textfield.
	*/
   def nameTerms() {
	   log.debug params

	   params.max = Math.min(params.max ? params.int('max') : 5, 10);
	   
	   def jsonData = []

	   def namesLookupResults = namesIndexerService.suggest(params)
	   jsonData.addAll(namesLookupResults);

	   jsonData.addAll(userGroupService.getUserGroupSuggestions(params));
	   render jsonData as JSON
   }
      
   def allGroups() {
		def userGroupInstance = findInstance(params.id, params.webaddress)
		if (!userGroupInstance) return;
		render (view:'allGroups', model:['userGroupInstance':userGroupInstance])
   }
   
   def myGroups() {
		def userGroupInstance = findInstance(params.id, params.webaddress, !params.format?.equalsIgnoreCase('json'))
		if (!userGroupInstance) return;
		render (view:'myGroups', model:['userGroupInstance':userGroupInstance])
   }
   
   def suggestedGroups() {
	   log.debug params;	   
	   def gList = userGroupService.getSuggestedUserGroups(null,params?.offset)
	   def res =[suggestedGroupsHtml: g.render(template:"/common/userGroup/showSuggestedUserGroupsListTemplate", model:['userGroups':gList])];
	   render res as JSON
   }

//   def species() {
//	   log.debug params;
//	   def userGroupInstance = findInstance(params.id, params.webaddress)
//	   if (!userGroupInstance) return;
//	   render (view:'species', model:['userGroupInstance':userGroupInstance, params:params])
//   }

   def tags() {
	   log.debug params;
	   render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
   }
   
   def bulkPost() {
	   def r = userGroupService.updateResourceOnGroup(params)
       withFormat {
           json {
               def resObj = r.remove('resourceObj')
               if(params.pullType == 'single'){
                   r['resourceGroupHtml'] =  g.render(template:"/common/resourceInGroupsTemplate", model:['observationInstance':resObj]);
                   r['featureGroupHtml'] = uGroup.featureUserGroups([model:['observationInstance':resObj]]);
               }

               r['msg'] = "${message(code:r.remove('msgCode'))}"
               render r as JSON
           }
       }
   } 
   /////////////////////////////////////////////////////////////////////////////////////////////
   ////////////////////To create and add user to a specific group (i.e BirdRace)////////////////
   /////////////////////////////////////////////////////////////////////////////////////////////
  
   /**
   * add member can only be done when a user who has 
   * BasePermission.ADMINISTRATOR i.e., founder runs this code
   */
   @Secured(['ROLE_USER'])
   def addUserToGroup() {
	   log.debug params
	   UserGroup ug = UserGroup.read(params.groupId.toLong())
	   boolean sendMail = (params.sendMail ? params.sendMail.toBoolean() : false)
	   boolean postObs = (params.postObs ? params.postObs.toBoolean() : false)
	   def res = createFromFile("/tmp/users.tsv")
	   def oldUsers = res[0]
	   def newUsers = res[1]
	   
	   SUser.withTransaction(){
		   log.debug "adding new uesr " + user
		   newUsers.each{ user ->
			   ug.addMember(user);
			   if(sendMail){
				   sendNotificationMail(user, request, params, true, ug)
			   }
		   }
		   oldUsers.each{ user ->
			   log.debug "======== adding old user " + user
			   ug.addMember(user);
			   if(sendMail){
				   sendNotificationMail(user, request, params, false, ug)
			   }
		   } 
	   }
	   
	   SUser.withTransaction(){
		   oldUsers.each{ user ->
			   if(postObs){
				   postObvToGroup(ug, user)
			   }
		   }
	   }
	   
	   render "== done"
   }
   
   private postObvToGroup(group, SUser user){
	   user.observations.each { obv ->
		   if(!obv.isDeleted){
		   		userGroupService.postObservationToUserGroup(obv, group)
				log.debug "posting obs " + obv
		   }
	   }
   }
   
   private void sendNotificationMail(SUser user, request, params, boolean isNewUser, userGroupInstance){
	   def conf = SpringSecurityUtils.securityConfig
	   
//	   def userGroupInstance;
//	   if(params.groupId || params.webaddress) {
//		   userGroupInstance = findInstance(params.groupId, params.webaddress);
//	   }
//	   
	   def userProfileUrl = generateLink("user", "show", [userGroup:userGroupInstance], ["id": user.id], request)
	   def changePasswordUrl = generateLink("user", "resetPassword", [userGroup:userGroupInstance], ["id": user.id], request)
	   def obvModuleUrl = generateLink("observation", "list", [userGroup:userGroupInstance], [], request);
	   def bBirdUrl;
	   
	   if(userGroupInstance)
			bBirdUrl = generateLink("group", "show", [mapping:'userGroup', userGroup:userGroupInstance], [], request)
		else
			bBirdUrl = generateLink("group", "show", [base: Utils.getDomainServerUrl(request)], [], request)
	   def templateMap = [username: user.name.capitalize(), bBirdUrl:bBirdUrl, obvModuleUrl:obvModuleUrl, changePasswordUrl:changePasswordUrl, email:user.email, password:user.email.split("@")[0].trim(), userProfileUrl:userProfileUrl, domain:Utils.getDomainName(request)]
	   
	   def mailSubject = conf.ui.bBird.emailSubject
	   def body
	   
	   if(isNewUser)
	   		body = conf.ui.bBird.emailBody
	   else
	   		body = conf.ui.bBirdExistingUser.emailBody 
	   
	   if (body.contains('$')) {
		   body = evaluate(body, templateMap)
	   }
	   
	   if (mailSubject.contains('$')) {
		   mailSubject = evaluate(mailSubject, [domain: Utils.getDomainName(request)])
	   }

	   //if ( Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
		   try {
		   	mailService.sendMail {
			   to user.email
               		   bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
			   //bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com","thomas.vee@gmail.com","sandeept@strandls.com","balachandert@gmail.com"
			   from grailsApplication.config.grails.mail.default.from
			   subject mailSubject
			   html body.toString()
			}
		   }catch(all)  {
		       log.error all.getMessage()
		   }
	   //}
		//   log.debug "Sent mail for notificationType ${notificationType} to ${user.email}"
   }
   
   private String evaluate(s, binding) {
	   new SimpleTemplateEngine().createTemplate(s).make(binding)
   }
   
   private createFromFile(String fileName){
	   Set emailList = new HashSet()
	   new File(fileName).splitEachLine("\\t") {
		   def fields = it;
		   def email = fields[0].trim()
		   emailList.add(email)
	   }
	   return createUserByEmail(emailList)
	}
   
   private createUserByEmail(emailList){
	   Set oldUsers = new HashSet()
	   Set newUsers = new HashSet() 
	   def defaultRoleNames = ['ROLE_USER']
	   emailList.each { email ->
		   def user = SUser.findByEmail(email)
		   if(user){
			   oldUsers.add(user)
		   }else{
		   	   //creating new user	
			   def username = email.split("@")[0].trim()
			   user = new SUser (
					   username : username,
					   name : username,
					   password : username,
					   enabled : true,
					   accountExpired : false,
					   accountLocked : false,
					   passwordExpired : false,
					   email : email
					   );
				   
		   
			   SUser.withTransaction {
				   if(!user.save(flush: true) ){
					   user.errors.each { println it; }
				   }else {
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
					   newUsers.add(user)
				   }
			   }
		   }
	   }
	   return [oldUsers, newUsers]
   }
  
    @Secured(['ROLE_ADMIN'])
    def sendSampleDigest() {
        println "=====STARTING SENDING SAMPLE EMAIL======"
        def digest = Digest.findByUserGroup(UserGroup.get(params.userGroupId.toLong()));
        //def usersEmailList = [SUser.get(4136L)]
        def usersEmailList = [SUser.get(1426L), SUser.get(1117L), SUser.get(4136L)]
        println "==USERSMAIL LIST========= "  + usersEmailList
        def setTime = params.setTime?params.setTime.toBoolean():false
        println "=====SET TIME ===== " + setTime
        def digestContent = digestService.fetchDigestContent(digest)
        digestService.sendDigest(digest, usersEmailList, setTime, digestContent);
        println "==========SAMPLE EMAILS SENT============"
    }
    
    @Secured(['ROLE_ADMIN'])
    def sendDigest() {
        println "=====STARTING SENDING DIGEST MAIL TO ALL TREES INDIA MEMBERS======"
        /*
        def digest = Digest.get(1L)
        def setTime = params.setTime?params.setTime.toBoolean():true
        digestService.sendDigestWrapper(digest, setTime);
        */
        digestService.sendDigestAction()
        println "==========ALL DIGEST EMAILS SENT============"
    }

    @Secured(['ROLE_ADMIN'])
    def sendSampleDigestPrizeEmail() {
        println "=====STARTING SAMPLE DIGEST PRIZE EMAIL======"
        /*
        def digest = Digest.get(1L)
        def setTime = params.setTime?params.setTime.toBoolean():true
        digestService.sendDigestWrapper(digest, setTime);
        */
        def usersEmailList = [SUser.get(1426L), SUser.get(1117L), SUser.get(4136L)]
        digestService.sendSampleDigestPrizeEmail(usersEmailList)
        println "==========ALL DIGEST EMAILS SENT============"
    }

    @Secured(['ROLE_ADMIN'])
    def sendDigestPrizeEmail() {
        println "=====STARTING SENDING DIGEST PRIZE MAIL TO ALL TREES INDIA MEMBERS======"
        digestService.sendDigestPrizeEmail()
        println "==========ALL DIGEST EMAILS SENT============"
    }
	
	@Secured(['ROLE_USER'])
    def removeMemberInBulk(){
    	userGroupService.removeMemberInBulk(params)
    	render "== done"
    }

    @Secured(['ROLE_ADMIN'])
    def createDigestIns(){
        def ug = UserGroup.get(params.userGroupId.toLong())
        println "=========UG=========== " + ug
        def dig = new Digest(userGroup:ug, lastSent:new Date() - 14, forObv:true, forSp:true, forDoc:true, forUsers:true, startDateStats:new Date() - 14, sendTopContributors:true,sendTopIDProviders:true);
        if(!dig.save(flush:true)) {
            dig.errors.allErrors.each { log.error it } 
        }
        println "========== CREATED Digest instance ============="
    }

}



class UserGroupCommand {
	String name
	String webaddress
	String description
	String founderEmailIds;
	String memberEmailIds;

	static constraints = {
		name nullable: false, blank:false
		webaddress nullable: false, blank:false, validator: UserGroupController.webaddressValidator
		description nullable: false, blank:false
	}
}
