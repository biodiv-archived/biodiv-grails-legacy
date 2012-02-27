package species.auth.drupal
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.GenericFilterBean
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.context.ApplicationEventPublisher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Cookie
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AuthenticationManager
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException

class DrupalAuthCookieFilter extends GenericFilterBean implements ApplicationEventPublisherAware {
	
	private static final log = LogFactory.getLog(this);
	
	ApplicationEventPublisher applicationEventPublisher
	DrupalAuthUtils drupalAuthUtils
	AuthenticationManager authenticationManager
	String logoutUrl = '/j_spring_security_logout'

	void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
		HttpServletRequest request = servletRequest
		HttpServletResponse response = servletResponse

		log.debug "Cookies from filter: "
		request.cookies.each { log.debug it.name+" : "+it.value }
		String url = request.requestURI.substring(request.contextPath.length())
		logger.debug("Processing url: $url")

		if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {
			logger.debug("Applying drupal auth filter")
			assert drupalAuthUtils != null
			Cookie cookie = drupalAuthUtils.getAuthCookie(request)
			if (cookie != null) {
				try {
					DrupalAuthToken token = drupalAuthUtils.build(request)
					if (token != null) {
						Authentication authentication = authenticationManager.authenticate(token);
						// Store to SecurityContextHolder
						SecurityContextHolder.context.authentication = authentication;

						if (logger.isDebugEnabled()) {
							logger.debug("SecurityContextHolder populated with DrupalAuthToken: '"
									+ SecurityContextHolder.context.authentication + "'");
						}
						try {
							chain.doFilter(request, response);
						} finally {
							SecurityContextHolder.context.authentication = null;
						}
						return
					}
				} catch (BadCredentialsException e) {
					logger.info("Invalid cookie, skip. Message was: $e.message")
				}
			} else {
				logger.debug("No auth cookie")
			}
		} else {
			logger.debug("SecurityContextHolder not populated with DrupalAuthToken token, as it already contained: $SecurityContextHolder.context.authentication");
		}

		//when not authenticated, dont have auth cookie or bad credentials
		chain.doFilter(request, response)
	}

}
