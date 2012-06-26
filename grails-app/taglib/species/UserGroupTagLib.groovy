package species

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
			}
			else {
				tags =  userGroupService.getFilteredTags(attrs.model.params);
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

}
