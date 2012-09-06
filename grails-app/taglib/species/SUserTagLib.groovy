package species

import species.auth.FacebookAuthToken;

class SUserTagLib {
	static namespace = "sUser"

	def springSecurityService
	def SUserService;

	/**
	 * 
	 */
	def renderProfileLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << showUserTemplate(model:['userInstance':currentUser]);
		}
	}

	def renderProfileHyperLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << createLink(controller:"user", action:"show", id:springSecurityService.currentUser.id);
		}
	}

	/**
	 * Renders the body if the authenticated user owns this page.
	 */
	def ifOwns = { attrs, body ->
		if (SUserService.ifOwns(attrs.model.user)) {
			out << body()
		}
	}

	/**
	 * 
	 */
	def ifOwnsOrIsPublic = { attrs, body ->
		if (SUserService.ifOwns(attrs.model.user) || attrs.model.isPublic) {
			out << body()
		}
	}

	/**
	 * 
	 */
	def showUserTemplate = { attrs ->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserTemplate", model:attrs.model);
		}
	}

	/**
	 *
	 */
	def showUserSnippet = { attrs ->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserSnippetTemplate", model:attrs.model);
		}
	}

	/**
	 * 
	 */
	def showUserSnippetTablet = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserSnippetTabletTemplate", model:attrs.model);
		}
	}


	/**
	 *
	 */
	def showUserList = { attrs, body->
		if(attrs.model.userInstanceList) {
			out << render(template:"/common/suser/showUserListTemplate", model:attrs.model);
		}
	}

	/**
	 *
	 */
	def showUserListWrapper = { attrs, body->
		out << render(template:"/common/suser/showUserListWrapperTemplate", model:attrs.model);
	}

	/**
	 *
	 */
	def showUserStory = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserStoryTemplate", model:attrs.model);
		}
	}

	/**
	 * 
	 */
	def showUserStoryTablet = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserStoryTabletTemplate", model:attrs.model);
		}
	}
	/**
	 * 
	 */
	def showDate = {
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showDateTemplate", model:attrs.model);
		}
	}

	def userLoginBox = { attrs, body->
		out << render(template:"/common/suser/userLoginBoxTemplate", model:attrs.model);
	}

	def isFBUser = { attrs, body->
		if(springSecurityService.getAuthentication() instanceof FacebookAuthToken) {
			out<< body();
		}
	}
	
	def selectUsers = { attrs, body->
		out << render(template:"/common/suser/selectUsersTemplate", model:attrs.model);
	}

	/**
	 * Renders the body if the authenticated user owns this page.
	 */
	def isAdmin = { attrs, body ->
		if (SUserService.isAdmin(attrs.model.user?.id)) {
			out << body()
		}
	}
}
