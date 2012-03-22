package species

class SUserTagLib {
	static namespace = "sUser"
	
	def springSecurityService
	
	def renderProfileLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << "<a href='${createLink(controller:'SUser', action:'show', id:currentUser.id)}'>${currentUser.username}</a>"
		}
	}
	
	
}
