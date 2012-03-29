package species

class SUserTagLib {
	static namespace = "sUser"
	
	def springSecurityService
	
	def renderProfileLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << showUserSnippet(model:['userInstance':currentUser]);
		}
	}
	
	def showUserSnippet = { attrs ->
		out << render(template:"/common/showUserTemplate", model:attrs.model);
	}
	
	/**
	* Renders the body if the authenticated user owns this page.
	*/
   def ifOwns = { attrs, body ->
	   if (springSecurityService.isLoggedIn() && springSecurityService.currentUser?.id == attrs.model.user.id) {
		   out << body()
	   }
   }
   
   def externalAuthProviders = {attrs, body ->
		out << render(template:"/common/auth/externalProvidersTemplate", model:attrs.model);
	}
	
}
