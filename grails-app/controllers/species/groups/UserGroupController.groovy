package species.groups

import java.util.Map;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;

import species.auth.Role;
import species.auth.SUser;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.participation.UserToken;
import species.utils.ImageUtils;
import species.utils.Utils;
import grails.converters.JSON;
import grails.plugins.springsecurity.Secured;

class UserGroupController {

	def springSecurityService;
	def userGroupService;

	def mailService;
	def aclUtilService;
	def observationService;
	def emailConfirmationService;
	def namesIndexerService;
	def activityFeedService;
	static allowedMethods = [save: "POST", update: "POST",]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		def model = getUserGroupList(params);
		if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
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

	protected def getUserGroupList(params) {
		def max = Math.min(params.max ? params.int('max') : 9, 100)
		def offset = params.offset ? params.int('offset') : 0

		def filteredUserGroup = userGroupService.getFilteredUserGroups(params, max, offset, false)

		def userGroupInstanceList = filteredUserGroup.userGroupInstanceList
		def queryParams = filteredUserGroup.queryParams
		def activeFilters = filteredUserGroup.activeFilters
		activeFilters.put("append", true);//needed for adding new page userGroup ids into existing session["uGroup_ids_list"]

		def totalUserGroupInstanceList = userGroupService.getFilteredUserGroups(params, -1, -1, true).userGroupInstanceList
		def count = totalUserGroupInstanceList.size()

		//storing this filtered obvs ids list in session for next and prev links
		//http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/1.8.2/org/codehaus/groovy/runtime/DefaultGroovyMethods.java
		//returns an arraylist and invalidates prev listing result
		if(params.append) {
			session["uGroup_ids_list"].addAll(userGroupInstanceList.collect {it.id});
		} else {
			session["uGroup_ids_list_params"] = params.clone();
			session["uGroup_ids_list"] = userGroupInstanceList.collect {it.id};
		}

		log.debug "Storing all userGroup ids list in session ${session['uGroup_ids_list']} for params ${params}";
		return [totalUserGroupInstanceList:totalUserGroupInstanceList, userGroupInstanceList: userGroupInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}

	def listRelated = {
		log.debug params

		switch(params.filterProperty) {
			case 'featuredObservations':
				redirect(action:'observations', id:params.id);
				break;
			case 'featuredMembers':
				redirect(action:'members', id:params.id);
				break;
			case 'obvRelatedUserGroups':
				redirect(action:'list', params:[observation:params.id]);
				break;
			case 'userUserGroups':
				redirect(action:'list', params:[user:params.id]);
				break;
			default:
				flash:message "Invalid command"
				redirect(action:list, id:params.id)
		}
		return
	}

	@Secured(['ROLE_USER'])
	def create = {
		log.debug params
		def userGroupInstance = new UserGroup()
		userGroupInstance.properties = params
		return [userGroupInstance: userGroupInstance, currentUser:springSecurityService.currentUser]
	}

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params;
		params.domain = Utils.getDomainName(request)
		def userGroupInstance = userGroupService.create(params);
		if (userGroupInstance.hasErrors()) {
			userGroupInstance.errors.allErrors.each { log.error it }
			render(view: "create", model: [userGroupInstance: userGroupInstance])
		}
		else {
			log.debug "Successfully created usergroup : "+userGroupInstance
			activityFeedService.addActivityFeed(userGroupInstance, null, springSecurityService.currentUser, activityFeedService.USERGROUP_CREATED);
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.id])}"
			redirect(action: "show", id: userGroupInstance.id)
		}
	}

	def show = {
		def userGroupInstance = findInstance();
		if (!userGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
		else {
			userGroupInstance.incrementPageVisit();
			if(params.pos) {
				int pos = params.int('pos');
				def prevNext = getPrevNextUserGroups(pos);
				if(prevNext) {
					[userGroupInstance: userGroupInstance, prevUserGroupId:prevNext.prevUserGroup, nextUserGroupId:prevNext.nextUserGroupId, lastListParams:prevNext.lastListParams]
				} else {
					[userGroupInstance: userGroupInstance]
				}
			} else {
				[userGroupInstance: userGroupInstance]
			}
		}
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
	def edit = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		} else if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION)) {
			render(view: "create", model: [userGroupInstance: userGroupInstance, 'springSecurityService':springSecurityService])
		} else {
			flash.message = "${message(code: 'default.not.permitted.message', args: [params.action, message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.name])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def update = {
		log.debug params;
		
		def userGroupInstance = findInstance()
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
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.name])}"
				if(params.founders) {
					flash.message += ". Sent email invitation to ${params.founders} to join as founders"
				}
				redirect(action: "show", id: userGroupInstance.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def delete = {
		log.debug params;
		def userGroupInstance = findInstance()
		if (userGroupInstance) {
			try {
				userGroupService.delete(userGroupInstance)
				activityFeedService.deleteFeed(userGroupInstance);
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "list")
				return
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "show", id: params.id)
				return;
			}
		}
		
		flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
		redirect(action: "list")
		
	}

	private UserGroup findInstance() {
		if(!params.id) return;

		def userGroup = userGroupService.get(params.long('id'))
		if (!userGroup) {
			flash.message = "UserGroup not found with id $params.id"
			redirect action: list
		}
		userGroup
	}

	def members = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			params.max = Math.min(params.max ? params.int('max') : 9, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def allMembers;
		if(params.onlyMembers) {
			allMembers = userGroupInstance.getMembers(params.max, params.offset);
		} else {
			allMembers = userGroupInstance.getAllMembers(params.max, params.offset);
		}
		if(params.isAjaxLoad?.toBoolean()) {
			def membersJSON = []
			for(m in allMembers) {
				membersJSON << ['id':m.id, 'name':m.name, 'icon':m.icon()]
			}
			render ([result:membersJSON] as JSON);
			return;
		}
		['userGroupInstance':userGroupInstance, 'members':allMembers, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'membersTotalCount':userGroupInstance.getAllMembersCount(), 'expertsTotalCount':0]
	}

	def founders = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			params.max = Math.min(params.max ? params.int('max') : 9, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def founders = userGroupInstance.getFounders(params.max, params.offset);

		if(params.isAjaxLoad?.toBoolean()) {
			def foundersJSON = []
			for(m in founders) {
				foundersJSON << ['id':m.id, 'name':m.name, 'icon':m.icon()]
			}
			render ([result:foundersJSON] as JSON);
			return;
		}
		render(view:"members", model:['userGroupInstance':userGroupInstance, 'founders':founders, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'membersTotalCount':userGroupInstance.getAllMembersCount(), 'expertsTotalCount':0]);
	}

	def experts = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			params.max = Math.min(params.max ? params.int('max') : 9, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def experts = [];//userGroupInstance.getExperts(params.max, params.offset);
		render(view:"members", model:['userGroupInstance':userGroupInstance, 'experts':experts, 'foundersTotalCount':userGroupInstance.getFoundersCount(), 'membersTotalCount':userGroupInstance.getAllMembersCount(), 'expertsTotalCount':0]);
	}

	def observations = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			params.max = Math.min(params.max ? params.int('max') : 9, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def model = userGroupService.getUserGroupObservations(userGroupInstance, params, params.max, params.offset);
		def model2 = userGroupService.getUserGroupObservations(userGroupInstance, params, -1, -1, true);
		def observationInstanceTotal = model2.observationInstanceList.size();
		model['instanceTotal'] = observationInstanceTotal
		model['totalObservationInstanceList'] = model2.observationInstanceList;

		if(params.loadMore?.toBoolean()){
			render(template:"/common/observation/showObservationListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render(view:"observations", model:model);
			return;
		} else {
			def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
			def tagsHtml = "";
			if(model.showTags) {
				def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
				tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			}
			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model2.observationInstanceList]);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, mapViewHtml:mapViewHtml]
			render result as JSON
			return;
		}

	}

	def filteredMapBasedObservationsList = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			params.max = Math.min(params.max ? params.int('max') : 9, 100)
		params.offset = params.offset ? params.int('offset') : 0

		def model = userGroupService.getUserGroupObservations(userGroupInstance, params, params.max, params.offset);
		def model2 = userGroupService.getUserGroupObservations(userGroupInstance, params, -1, -1, true);
		def totalCount = model2.observationInstanceList.size();
		model['observationInstanceTotal'] = totalCount

		render (template:"/common/observation/showObservationListTemplate", model:model);
	}

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def settings = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return

			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION)) {
				return ['userGroupInstance':userGroupInstance]
			}
		return;
	}

	@Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def joinUs = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) {
			render (['success':true,'statusComplete':false, 'msg':'No userGroup is selected.'] as JSON);
			return;
		}

		def user = springSecurityService.currentUser;
		if(user) {
			if(userGroupInstance.isMember(user)) {
				flash.message = 'Already a member.';
				render ([success:true, 'statusComplete':false, 'msg':'Already a member.']as JSON);
			} else {
				if(userGroupInstance.addMember(user)) {
					flash.message = "You have joined ${userGroupInstance} group. We look forward for your contribution.";
					render ([success:true, 'statusComplete':true, 'msg':"You have joined ${userGroupInstance} group. We look forward for your contribution."]as JSON);
					return;
				}
			}
		}
		flash.error = 'We are extremely sorry as we are not able to process your request now. Please try again.';
		render ([success:true, 'statusComplete':false, 'msg':'We are extremely sorry as we are not able to process your request now. Please try again.']as JSON);
	}

	@Secured(['ROLE_USER'])
	def inviteMembers = {
		List members = Utils.getUsersList(params.memberUserIds);
		log.debug members;

		if(members) {
			def userGroupInstance = findInstance()
			if (!userGroupInstance) {
				render (['success':true, 'statusComplete':false, 'msg':'No userGroup selected.'] as JSON);
				return;
			}

			int membersCount = members.size();
			userGroupService.sendMemberInvitation(userGroupInstance, members, Utils.getDomainName(request));
			String msg = "Successfully sent invitation messsage to ${members.size()} member(s)"
			if(membersCount > members.size()) {
				int alreadyMembersCount = membersCount-members.size();

				msg += " as other "+alreadyMembersCount+" member(s) were already found to be members of this group."
			}
			render (['success':true, 'statusComplete':true, 'msg':msg] as JSON)
			return
		}
		render (['success':true, 'statusComplete':false, 'msg':'Please provide details of people you want to invite to join this group.'] as JSON)
	}

	@Secured(['ROLE_USER'])
	def requestMembership = {
		def user = springSecurityService.currentUser;
		if(user) {
			def userGroupInstance = findInstance()
			if (!userGroupInstance) {
				render (['success':true, 'statusComplete':false, 'msg':'No userGroup selected.'] as JSON);
				return;
			}

			if(userGroupInstance.isMember(user)) {
				render (['success':true, 'statusComplete':false, 'msg':'Already a member.'] as JSON);
				return;
			}
			
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			def founders = userGroupInstance.getFounders(userGroupInstance.getFoundersCount(), 0);

			founders.each { founder ->
				log.debug "Sending email to  founder ${founder}"
				def userToken = new UserToken(username: user."$usernameFieldName", controller:'userGroup', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':user.id.toString(), 'role':UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value()]);
				userToken.save(flush: true)
				emailConfirmationService.sendConfirmation(founder.email,
						"Please confirm users membership",  [founder:founder, user: user, userGroupInstance:userGroupInstance,domain:Utils.getDomainName(request), view:'/emailtemplates/requestMembership'], userToken.token);
			}
			render (['success':true, 'statusComplete':true, 'msg':'Sent request to all founders for confirmation.'] as JSON);
			return;
		}
		render (['success':true,'statusComplete':false, 'msg':'Please login to request membership.'] as JSON);

	}

	private String generateLink( String controller, String action, linkParams, request) {
		createLink(base: Utils.getDomainServerUrl(request),
				controller:controller, action: action,
				params: linkParams)
	}

	@Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def confirmMembershipRequest = {
		log.debug params;
		
		if(params.userId && params.userGroupInstanceId) {
			def user;
			if(params.userId == 'register') {
				user = springSecurityService.currentUser
			} else {
				user = SUser.read(params.userId.toLong())
			}
			def userGroupInstance = UserGroup.read(params.userGroupInstanceId.toLong());
			if(user && userGroupInstance && (user.id.toLong() == springSecurityService.currentUser.id || userGroupInstance.isFounder())) {
				switch(params.role) {
					case UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value():
						if(userGroupInstance.addMember(user)) {
							flash.message="Successfully added ${user} to this group as member"
						}
						break;
					case UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value():
						if(userGroupInstance.addFounder(user)) {
							flash.message="Successfully added ${user} to this group as founder"
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
					flash.error="Couldn't add user to the group as the currently logged in user doesn't have required permissions."
				} else {
					flash.error="Couldn't add user to the group because of missing information."
				}
			}
			redirect (action:"show", id:userGroupInstance.id);
			return;
		}
		flash.error="There seems to be some problem. You are not the user to whom this confirmation request is sent as per our records."
		redirect (action:"list");
	}

	@Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
	def leaveUs = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) {
			flash.error = 'No userGroup selected.'
			render (['success':true, 'statusComplete':false, 'msg':'No userGroup selected.'] as JSON);
			return;
		}

		def user = springSecurityService.currentUser;
		if(user && userGroupInstance.deleteMember(user)) {
			activityFeedService.addActivityFeed(userGroupInstance, user, user, activityFeedService.MEMBER_LEFT);
			flash.message = 'Thank you for being with us.'
			render (['msg':'Thank you for being with us.', 'success':true, 'statusComplete':true] as JSON);
			return;
		}
		flash.error = 'Your presence is important to us. Cannot let you leave at present.'
		render (['msg':'Your presence is important to us. Cannot let you leave at present.','success':true, 'statusComplete':false]as JSON);
	}

	def aboutUs = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;

		return ['userGroupInstance':userGroupInstance]
	}

	def getRelatedUserGroups = {
		log.debug params;
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		if(!params.id) return;

		def observationInstance = Observation.get(params.long('id'))
		if (!observationInstance) {
			flash.message = "Observation not found with id $params.id"
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

	@Secured(['ROLE_USER'])
	def upload_resource = {
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
							f.size/1024
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

						File file = observationService.getUniqueFile(userGroupDir, Utils.cleanFileName(f.originalFilename));
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

	def getFeaturedObservations = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;

		log.debug params;
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0
		params.sort = "visitCount";
		def model = userGroupService.getUserGroupObservations(userGroupInstance, params, max, offset);

		def result = [];
		model.observationInstanceList.each {
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
		}

		def r = ["observations":result, "count":model.observationInstanceTotal]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations);
		}
		render r as JSON
	}

	def getFeaturedMembers = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;

		log.debug params;
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

	def getUserUserGroups = {
		log.debug params;
		def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		if(!params.id) return;

		def userInstance = SUser.get(params.long('id'))
		if (!userInstance) {
			flash.message = "SUser not found with id $params.id"
			return;
		}

		def userGroups = userGroupService.getUserUserGroups(userInstance, max, offset);

		def result = [];
		userGroups.each {
			result.add(['observation':it, 'title':it.name]);
		}

		def r = ["observations":result, "count":userGroupService.getNoOfUserUserGroups(userInstance)]
		if(r.observations) {
			r.observations = observationService.createUrlList2(r.observations, '');
		}
		render r as JSON
	}
	
	def actionsHeader = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (template:"/common/userGroup/actionsHeaderTemplate", model:['userGroupInstance':userGroupInstance]);
	}

	def pages = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (view:"pages", model:['userGroupInstance':userGroupInstance])
	}
	
	def page = {
		log.debug params;
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (view:'page', model:['userGroupInstance':userGroupInstance, 'newsletterId':params.newsletterId])
	}
	
	def pageCreate = {
		log.debug params;
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (view:'pageCreate', model:['userGroupInstance':userGroupInstance])
	}
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search = {
		log.debug params;
		
		def model = getUserGroupList(params);
		if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
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
   def nameTerms = {
	   log.debug params

	   params.max = Math.min(params.max ? params.int('max') : 5, 10);
	   
	   def jsonData = []

	   def namesLookupResults = namesIndexerService.suggest(params)
	   jsonData.addAll(namesLookupResults);

	   jsonData.addAll(getUserGroupSuggestions(params));
	   render jsonData as JSON
   }
   
   /**
	*
	*/
   def terms = {
	   log.debug params;
	   params.max = Math.min(params.max ? params.int('max') : 5, 10);
	   render getUserGroupSuggestions(params) as JSON;
   }

   private getUserGroupSuggestions(params){
	   def jsonData = []
	   String name = params.term
	   
	   String usernameFieldName = 'name';//SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
	   String userId = 'id';


	   def results = UserGroup.executeQuery(
			   "SELECT DISTINCT u.$usernameFieldName, u.$userId " +
			   "FROM UserGroup u " +
			   "WHERE LOWER(u.$usernameFieldName) LIKE :name " +
			   "ORDER BY u.$usernameFieldName",
			   [name: "${name.toLowerCase()}%"],
			   [max: params.max])

	   for (result in results) {
		   jsonData << [value: result[0], label:result[0] , userId:result[1] , "category":"User Groups"]
	   }
	   
	   return jsonData;
   }
   
   def allGroups= {
	   log.debug params;
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (view:'allGroups', model:['userGroupInstance':userGroupInstance])
   }
   
   def myGroups= {
	   log.debug params;
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return;
		render (view:'myGroups', model:['userGroupInstance':userGroupInstance])
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
