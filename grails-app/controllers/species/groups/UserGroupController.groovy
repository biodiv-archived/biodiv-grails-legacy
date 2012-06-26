package species.groups

import java.util.Map;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import species.auth.SUser;
import species.utils.Utils;
import grails.converters.JSON;
import grails.plugins.springsecurity.Secured;

class UserGroupController {

	def springSecurityService;
	def userGroupService;
	def mailService;

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		def model = getUserGroupList(params);
		if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
		} else{
			def userGroupListHtml =  g.render(template:"/common/userGroup/showUserGroupListTemplate", model:model);
			def userGroupFilterMsgHtml = g.render(template:"/common/userGroup/showUserGroupFilterMsgTemplate", model:model);

			def filteredTags = userGroupService.getTagsFromUserGroup(model.totalUserGroupInstanceList.collect{it[0]})
			def tagsHtml = g.render(template:"/common/userGroup/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			def mapViewHtml = g.render(template:"/common/userGroup/showUserGroupMultipleLocationTemplate", model:[userGroupInstanceList:model.totalUserGroupInstanceList]);

			def result = [userGroupListHtml:userGroupListHtml, userGroupFilterMsgHtml:userGroupFilterMsgHtml, tagsHtml:tagsHtml, mapViewHtml:mapViewHtml]
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
		return [totalUserGroupInstanceList:totalUserGroupInstanceList, userGroupInstanceList: userGroupInstanceList, userGroupInstanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}

	@Secured(['ROLE_USER'])
	def create = {
		log.debug params
		def userGroupInstance = new UserGroup()
		userGroupInstance.properties = params
		return [userGroupInstance: userGroupInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params;
		List founders = Utils.getUsersList(params.founderUserIds);
		List members = Utils.getUsersList(params.memberUserIds);
		def userGroupInstance = userGroupService.create(params.name, params.webaddress, params.description, founders, members);
		if (userGroupInstance.hasErrors()) {
			userGroupInstance.errors.allErrors.each { log.error it }
			render(view: "create", model: [userGroupInstance: userGroupInstance])
		}
		else {

			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			userGroupInstance.setTags(tags);

			log.debug "Successfully created usergroup : "+userGroupInstance
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.id])}"
			redirect(action: "show", id: userGroupInstance.id)
		}
	}

	private void setFounderInvitation(SUser user, UserGroup userGroup) {
		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.userGroup.inviteFounder.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: user.name.capitalize(), groupUrl: userGroup.webaddress])
		}

		mailService.sendMail {
			to user.email
			from conf.ui.userGroup.inviteFounder.emailFrom
			subject conf.ui.userGroup.inviteFounder.emailSubject
			html body.toString()
		}
	}

	private void setMemberInvitation(SUser user) {
		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.userGroup.inviteMember.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: user.name.capitalize(), groupUrl: userGroup.webaddress])
		}

		mailService.sendMail {
			to user.email
			from conf.ui.userGroup.inviteMember.emailFrom
			subject conf.ui.userGroup.inviteMember.emailSubject
			html body.toString()
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
		}
		else {
			return [userGroupInstance: userGroupInstance]
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
			userGroupService.update(userGroupInstance, params)
			if (userGroupInstance.hasErrors()) {
				userGroupInstance.errors.allErrors.each { log.error it }
				render(view: "edit", model: [userGroupInstance: userGroupInstance])
			}
			else {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.id])}"
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
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
	}

	def grant = {
		def userGroupInstance = findInstance()
		if (!userGroupInstance) return
			if (!request.post) {
				return [userGroupInstance: userGroupInstance]
			}
		userGroupService.addPermission userGroupInstance, params.recipient, params.int('permission')
		flash.message = "Permission $params.permission granted on userGroupInstance $userGroupInstance.id to $params.recipient";
		redirect action: show, id: id
	}

	private UserGroup findInstance() {
		def userGroup = userGroupService.get(params.long('id'))
		if (!userGroup) {
			flash.message = "UserGroup not found with id $params.id"
			redirect action: list
		}
		userGroup
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