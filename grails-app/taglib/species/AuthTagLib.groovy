package species

import grails.plugin.springsecurity.SpringSecurityUtils;

class AuthTagLib {
	
	static namespace = "auth"
	
	def openIDAuthenticationFilter
	
	def ajaxLogin = {attrs, body ->
		def config = SpringSecurityUtils.securityConfig
		attrs.model = [openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
					daoPostUrl:    "${request.contextPath}${config.apf.filterProcessesUrl}",
					persistentRememberMe: config.rememberMe.persistent,
					rememberMeParameter: config.rememberMe.parameter,
					openidIdentifier: config.openid.claimedIdentityFieldName]
		out << render(template:"/common/ajaxLogin", model:attrs.model);
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
  def loginForm = {attrs, body ->
	  out << render(template:"/common/auth/loginFormTemplate", model:attrs.model);
  }
}
