package species.auth.drupal

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication
import org.apache.commons.lang.StringUtils
import org.apache.commons.codec.digest.*
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityRequestHolder;


class DrupalAuthDirectFilter extends AbstractAuthenticationProcessingFilter {

	private static def log = Logger.getLogger(this)

	def DrupalAuthDirectFilter(String url) {
		super(url)
	}
	
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		log.debug request
		request.getHeaderNames().each {
			println it;
			println request.getHeader(it)
		}
		
		println request.getRemoteUser();
		println request.requestURI;
		
		log.debug request.getParameterNames();
		Map params = [:]
		request.getParameterNames().each {
			println it;
			params[it] = request.getParameter(it)
		}
		
		log.debug params;
		
		if (params.uid == null) {
			throw new AuthenticationServiceException("Uid cannot be null");
		}

		DrupalAuthToken authRequest = new DrupalAuthToken (
				uid: Long.parseLong(params.uid),
				code: params.code
		);
		log.debug authRequest;
		
//		HttpSession session = request.getSession(false);
//		if (session != null || getAllowSessionCreation()) {
//			request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextEscapeUtils.escapeEntities(username));
//		}

		// Allow subclasses to set the "details" property
		//setDetails(request, authRequest);
		return this.getAuthenticationManager().authenticate(authRequest);

	}
}