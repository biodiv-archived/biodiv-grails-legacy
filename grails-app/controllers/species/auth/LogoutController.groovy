package species.auth

import grails.plugin.springsecurity.SpringSecurityUtils

class LogoutController {

    def grailsLinkGenerator;

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO put any pre-logout code here
		redirect uri: request.scheme+"://"+request.serverName+request.contextPath+SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
