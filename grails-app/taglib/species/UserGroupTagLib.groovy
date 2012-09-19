package species

import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import species.auth.Role;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;

class UserGroupTagLib {
	static namespace = "uGroup";

	def springSecurityService
	def userGroupService;

	def userGroups = { attrs, body ->
		def userInstance = attrs.model?.userInstance
		if (!userInstance) {
			userInstance = springSecurityService.currentUser;
		}
		def userGroupsList = userGroupService.getUserGroups(userInstance);
		out << render(template:"/common/userGroup/selectUsersGroupsTemplate", model:['userGroupsList':userGroupsList]);
	}

	def founders = {attrs, body ->
		def userGroupInstance = attrs.model?.userGroupInstance
		def founders = [];
		founders.each {
			out << g.createLink(controller:"sUser", action:"show", id:"${it.id}");
		}
	}

	def members = {attrs, body ->
		def userGroupInstance = attrs.model?.userGroupInstance
		userGroupInstance.members.each {
			out << g.link(controller:"sUser", action:"show", id:"${it.id}");
		}
	}

	def showSnippet = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupSnippetTemplate", model:attrs.model);
		}
	}

	def showSnippetTablet = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupSnippetTabletTemplate", model:attrs.model);
		}
	}


	def showStory = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupStoryTemplate", model:attrs.model);
		}
	}

	def addFlag= {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/addFlagTemplate", model:attrs.model);
		}
	}

	def showStoryTablet = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupStoryTabletTemplate", model:attrs.model);
		}
	}

	def showUserGroupInfo = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupInfoTemplate", model:attrs.model);
		}
	}

	def showRelatedStory = {attrs, body->
		out << render(template:"/common/userGroup/showUserGroupRelatedStoryTemplate", model:attrs.model);
	}

	def showRating = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupRatingTemplate", model:attrs.model);
		}
	}

	def showTagsSummary = {attrs, body->
		if(attrs.model.userGroupInstance) {
			def tags = userGroupService.getRelatedTagsFromObservation(attrs.model.userGroupInstance)
			out << render(template:"/common/userGroup/showTagsSummaryTemplate", model:[tags:tags]);
		}
	}

	//	def showTags = {attrs, body->
	//		if(attrs.model.userGroupInstance) {
	//			out << render(template:"/common/userGroup/showUserGroupTagsTemplate", model:attrs.model);
	//		}
	//	}

	def showObvStats = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupStatsTemplate", model:attrs.model);
		}
	}

	// this will call showTagsList and showTagsCloud
	def showAllTags = {attrs, body->
		def count = attrs.model.count;
		def tags = attrs.model.tags
		if(tags == null) {
			def tagFilterBy = attrs.model.tagFilterByProperty


			if(tagFilterBy == "Related"){
				def relatedParams = attrs.model.relatedObvParams
				tags = userGroupService.getAllRelatedUserGroupTags(relatedParams)
				count = tags.size()
			}
			else if(tagFilterBy == "User"){
				def userId = attrs.model.tagFilterByPropertyValue.toLong();
				tags = userGroupService.getAllTagsOfUser(userId)
				count = tags.size()
			} else if(tagFilterBy == "UserGroup") {
				def userGroupInstance = attrs.model.tagFilterByPropertyValue
				tags = userGroupService.getRelatedTagsFromUserGroup(userGroupInstance)
				count = tags.size()
			} else {
				tags = userGroupService.getFilteredTags(attrs.model.params);
				count = tags.size();
			}
		}
		//log.debug "==== tags " + tags
		out << render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:tags, isAjaxLoad:attrs.model.isAjaxLoad]);
	}

	def showTagsList = {attrs, body->
		out << render(template:"/common/userGroup/showTagsListTemplate", model:[tags:attrs.model.tags, isAjaxLoad:attrs.model.isAjaxLoad]);
	}


	def showTagsCloud = {attrs, body->
		out << render(template:"/common/userGroup/showTagsCloudTemplate", model:[tags:attrs.model.tags, isAjaxLoad:attrs.model.isAjaxLoad]);
	}


	def showGroupList = {attrs, body->
		out << render(template:"/common/userGroup/showGroupListTemplate", model:attrs.model);
	}

	def showUserGroupFilterMessage = {attrs, body->
		out << render(template:"/common/userGroup/showUserGroupFilterMsgTemplate", model:attrs.model);
	}

	def showLocation = {attrs, body->
		if(attrs.model.userGroupInstance) {
			out << render(template:"/common/userGroup/showUserGroupLocationTemplate", model:attrs.model);
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Tag List added by specific User ///////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	def showNoOfTagsOfUser = {attrs, body->
		def tags = userGroupService.getAllTagsOfUser(attrs.model.userId.toLong());
		out << tags.size()
	}

	def showUserGroupsList = {attrs, body->
		out << render(template:"/common/userGroup/showUserGroupListTemplate", model:attrs.model);
	}

	def showUserGroupsListWrapper = {attrs, body->
		out << render(template:"/common/userGroup/showUserGroupListWrapperTemplate", model:attrs.model);
	}

	def identificationByEmail = {attrs, body->
		def emailInfoModel = userGroupService.getIdentificationEmailInfo(attrs.model, attrs.model.requestObject, "");
		out << render(template:"/common/userGroup/identificationByEmailTemplate",model:emailInfoModel);
	}

	def showFooter = {attrs, body->
		out << render(template:"/common/userGroup/showUserGroupStoryFooterTemplate", model:attrs.model);
	}

	def showHeader = {attrs, body->
		out << render(template:"/common/userGroup/headerTemplate", model:attrs.model);
	}

	def showSidebar = {attrs, body->
		out << render(template:"/common/userGroup/sidebarTemplate", model:attrs.model);
	}

	def getCurrentUserUserGroups = {attrs, body ->
		def user = springSecurityService.getCurrentUser();
		def userGroups = userGroupService.getUserGroups(user);
		def result = [:]
		if(attrs.model?.observationInstance && attrs.model.observationInstance.userGroups) {
			//check if the obv already belongs to userGroup and disable the control for it not to submit again
			def obvInUserGroups = attrs.model.observationInstance.userGroups.intersect(userGroups)

			userGroups.removeAll(obvInUserGroups);
			obvInUserGroups.each {
				result[it] = true;
			}
		}

		userGroups.each {
			result[it] = false;
		}
		out << render(template:"/common/userGroup/showCurrentUserUserGroupsTemplate", model:[userGroups:result]);
	}

	def getCurrentUserUserGroupsSidebar = {attrs, body ->
		def user = springSecurityService.getCurrentUser();
		Set userGroups = userGroupService.getUserGroups(user);
		def subList= []
		int i=0;
		userGroups.each {
			if(i++<5) {
				subList.push(it)
				return;
			}
		}

		out << render(template:"/common/userGroup/showCurrentUserUserGroupsSidebarTemplate", model:['userGroups':subList]);
	}

	def showSuggestedUserGroups = {attrs, body ->
		def user = springSecurityService.getCurrentUser();
		Set userGroups = userGroupService.getSuggestedUserGroups(user);
		def subList= []
		int i=0;
		userGroups.each {
			if(i++<5) {
				subList.push(it)
				return;
			}
		}

		out << render(template:"/common/userGroup/showSuggestedUserGroupsTemplate", model:['userGroups':subList]);
	}
	
	def isUserGroupMember = { attrs, body->
		def user = springSecurityService.getCurrentUser();
		//TODO:optimize count
		if(user.getUserGroups().size() > 0) {
			out<< body();
		}
	}

	def isNotAMember = { attrs, body->
		def user = springSecurityService.getCurrentUser();
		def userGroupInstance = attrs.model?.userGroupInstance;
		if(!user || !userGroupInstance || !user.isUserGroupMember(userGroupInstance)) {
			out<< body();
		}
	}

	def isAMember = { attrs, body->
		def user = springSecurityService.getCurrentUser();
		def userGroupInstance = attrs.model?.userGroupInstance;
		if(user && userGroupInstance && user.isUserGroupMember(userGroupInstance)) {
			out<< body();
		}
	}

	def aclUtilService
	def gormUserDetailsService
	def perm = { attrs, body ->
		println aclUtilService.hasPermission(gormUserDetailsService.loadUserByUsername(attrs.model.user.email, true), attrs.model.userGroupInstance, attrs.model.permission)
		//println aclUtilService.readAcl(attrs.model.userGroupInstance);
		println "====="
	}

	def showActivityOnMap = {attrs, body ->
		def userGroupInstance = attrs.model?.userGroupInstance;
		def result = userGroupService.getUserGroupObservations(userGroupInstance, [:], -1, -1, true)
		def model = ['observationInstanceList':result?.observationInstanceList]
		out<<render(template:"/common/observation/showObservationMultipleLocationTemplate", model:model);
	}

	def showNoOfUserGroupsOfUser = {attrs, body->
		def noOfGroups = userGroupService.getNoOfUserUserGroups(attrs.model.user);
		out << noOfGroups
	}

	def showUserUserGroups  = {attrs, body->
		def userInstance = attrs.model?.userInstance;
		def result = userGroupService.getUserUserGroups(userInstance, -1, -1);

		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())

		result.each {
			switch(it.key.id) {
				case founderRole.id :
					out << render(template:"/common/userGroup/showUserGroupsTemplate", model:['title':'Founded', 'userGroupInstanceList':it.value.collect{it.userGroup}]);
					break;
				case memberRole.id :
					out << render(template:"/common/userGroup/showUserGroupsTemplate", model:['title':'Member of', 'userGroupInstanceList':it.value.collect{it.userGroup}]);
					break;
				default :
					log.error it
			}
		}


	}

	def showActionsHeaderTemplate = {attrs, body ->
		out<<render(template:"/common/userGroup/actionsHeaderTemplate", model:attrs.model);
	}

	def joinLeaveGroupTemplate = {attrs, body ->
		out<<render(template:"/common/userGroup/joinLeaveGroupTemplate", model:attrs.model);
	}

	def interestedSpeciesGroups = {attrs, body ->
		out<<render(template:"/common/userGroup/interestedSpeciesGroupsTemplate", model:attrs.model);
	}

	def interestedHabitats = {attrs, body ->
		out<<render(template:"/common/userGroup/interestedHabitatsTemplate", model:attrs.model);
	}

	def locationSelector = {attrs, body ->
		out<<render(template:"/common/userGroup/locationSelectorTemplate", model:attrs.model);
	}

	def showUserGroupSignature = {attrs, body ->
		out<<render(template:"/common/userGroup/showUserGroupSignatureTemplate", model:attrs.model);
	}
	
	def showSubmenuTemplate = {attrs, body->
		out << render(template:"/userGroup/userGroupSubmenuTemplate", model:attrs.model);
	}
}
