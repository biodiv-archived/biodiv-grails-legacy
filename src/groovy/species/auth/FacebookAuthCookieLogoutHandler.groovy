package species.auth

import org.springframework.security.web.authentication.logout.LogoutHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import javax.servlet.http.Cookie
import org.apache.log4j.Logger

import species.utils.Utils;

class FacebookAuthCookieLogoutHandler extends com.the6hours.grails.springsecurity.facebook.FacebookAuthCookieLogoutHandler {

	private static final Logger logger = Logger.getLogger(this)

	void logout(HttpServletRequest httpServletRequest,
	    HttpServletResponse httpServletResponse,
	    Authentication authentication) {
        
		super.logout(httpServletRequest, httpServletResponse, authentication);

		facebookAuthUtils.logout(httpServletRequest, httpServletResponse);
	}

	
}
