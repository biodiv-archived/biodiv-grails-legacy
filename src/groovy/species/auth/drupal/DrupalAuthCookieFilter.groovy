package species.auth.drupal
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.plugin.springsecurity.RequestHolderAuthenticationFilter;
import org.codehaus.groovy.grails.plugin.springsecurity.SecurityRequestHolder;
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.util.TextEscapeUtils;

class DrupalAuthCookieFilter extends AbstractAuthenticationProcessingFilter {

	private static final log = LogFactory.getLog(this);

	DrupalAuthUtils drupalAuthUtils
	String logoutUrl = '/j_spring_security_logout'

	DrupalAuthCookieFilter() {
		super(org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.plugin.springsecurity.apf.filterProcessesUrl);	
	}
	//		void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
	//			HttpServletRequest request = servletRequest
	//			HttpServletResponse response = servletResponse
	//
	//			log.debug "Cookies from filter: "
	//			request.cookies.each { log.debug it.name+" : "+it.value }
	//			String url = request.requestURI.substring(request.contextPath.length())
	//			logger.debug("Processing url: $url")
	//
	//			if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {
	//				logger.debug("Applying drupal auth filter")
	//				assert drupalAuthUtils != null
	//				Cookie cookie = drupalAuthUtils.getAuthCookie(request)
	//				if (cookie != null) {
	//					try {
	//						DrupalAuthToken token = drupalAuthUtils.build(request)
	//						if (token != null) {
	//							Authentication authentication = getAuthenticationManager().authenticate(token);
	//							// Store to SecurityContextHolder
	//							SecurityContextHolder.context.authentication = authentication;
	//
	//							if (logger.isDebugEnabled()) {
	//								logger.debug("SecurityContextHolder populated with DrupalAuthToken: '"
	//										+ SecurityContextHolder.context.authentication + "'");
	//							}
	//							try {
	//								chain.doFilter(request, response);
	//							} finally {
	//								//SecurityContextHolder.context.authentication = null;
	//							}
	//							return
	//						}
	//					} catch (BadCredentialsException e) {
	//						logger.info("Invalid cookie, skip. Message was: $e.message")
	//					}
	//				} else {
	//					logger.debug("No auth cookie")
	//				}
	//			} else {
	//				logger.debug("SecurityContextHolder not populated with DrupalAuthToken token, as it already contained: $SecurityContextHolder.context.authentication");
	//			}
	//
	//			//when not authenticated, dont have auth cookie or bad credentials
	//			chain.doFilter(request, response)
	//		}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		SecurityRequestHolder.set((HttpServletRequest)request, (HttpServletResponse)response);
		try {
			HttpSession session = request.getSession(false);			
			if (session != null) {
				log.debug '----------------------'
				log.debug session.getId();
				log.debug session.getAttribute(WebAttributes.SAVED_REQUEST);
				def enames = session.getAttributeNames();
				while (enames.hasMoreElements()) {
				   String name = (String) enames.nextElement();
				   String value = session.getAttribute(name);
				   log.debug name+":"+value;
				}
				log.debug '----------------------'
			}
			super.doFilter(request, response, chain);
		}
		finally {
			SecurityRequestHolder.reset();
		}
	}

	/**
	 * 
	 */
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		//		if (postOnly && !request.getMethod().equals("POST")) {
		//			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		//		}

		log.debug "Attempting authentication"
		log.debug "Cookies from filter: "
		request.cookies.each { log.debug it.name+" : "+it.value }

		final DrupalAuthToken authRequest = drupalAuthUtils.build(request);

		// Place the last username attempted into HttpSession for views
		HttpSession session = request.getSession(false);
		log.debug "--------------------";
		log.debug "Request Cookies : ";
		request.cookies.each { log.debug it.name+" : "+it.value }
//		def username = obtainUsername(request);
		
		if (session != null || getAllowSessionCreation()) {
			//log.debug "Current session Id : "+session.getId();
//			request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextEscapeUtils.escapeEntities(username));
		}

		// Allow subclasses to set the "details" property
		//setDetails(request, authRequest);

		return this.getAuthenticationManager().authenticate(authRequest);
	}

	/**
	 * Overridden to provide proxying capabilities.
	 */
//	protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
//		final String requestUri = request.getRequestURI();
//		String url = request.requestURI.substring(request.contextPath.length())
//		logger.debug("Checking if requiresAuthentication for url : "+url);
//		if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {			
//			assert drupalAuthUtils != null
//			Cookie cookie = drupalAuthUtils.getAuthCookie(request)
//			if (cookie == null) {
//				return true;
//			}
//		} 
//		return super.requiresAuthentication(request, response);
//	}

}
