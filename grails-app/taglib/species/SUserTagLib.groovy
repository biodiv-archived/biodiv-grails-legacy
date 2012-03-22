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
	
}
