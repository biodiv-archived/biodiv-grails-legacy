package species

class SUserTagLib {
	static namespace = "sUser"

	def springSecurityService

	/**
	 * 
	 */
	def renderProfileLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << showUserTemplate(model:['userInstance':currentUser]);
		}
	}


	/**
	 * Renders the body if the authenticated user owns this page.
	 */
	def ifOwns = { attrs, body ->
		if (springSecurityService.isLoggedIn() && springSecurityService.currentUser?.id == attrs.model.user.id) {
			out << body()
		}
	}

	/**
	 * 
	 */
	def externalAuthProviders = {attrs, body ->
		out << render(template:"/common/auth/externalProvidersTemplate", model:attrs.model);
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
}
