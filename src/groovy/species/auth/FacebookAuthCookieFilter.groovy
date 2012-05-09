package species.auth

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.plugins.springsecurity.ReflectionUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.web.filter.GenericFilterBean

import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken

/**
 * TODO
 *
 * @since 14.10.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class FacebookAuthCookieFilter extends GenericFilterBean implements ApplicationEventPublisherAware {

	
    protected ApplicationEventPublisher eventPublisher
    FacebookAuthUtils facebookAuthUtils
    AuthenticationManager authenticationManager
    String logoutUrl = '/j_spring_security_logout'
	String registerUrl;
	String createAccountUrl;
	
	def grailsApplication

    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
        HttpServletRequest request = servletRequest
        HttpServletResponse response = servletResponse
        String url = request.requestURI.substring(request.contextPath.length())
        logger.debug("Processing url: $url")
        if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {
            logger.debug("Applying facebook auth filter")
            assert facebookAuthUtils != null
            Cookie cookie = facebookAuthUtils.getAuthCookie(request)
			Cookie fbLoginCookie = facebookAuthUtils.getFBLoginCookie(request);
            if (cookie != null && fbLoginCookie != null) {
				logger.debug("Found fb cookie");
				
                try {
                    FacebookAuthToken token = facebookAuthUtils.build(request, cookie.value)
                    if (token != null) {
						logger.debug("Got fbAuthToken $token");
                        Authentication authentication = authenticationManager.authenticate(token);
                        // Store to SecurityContextHolder
                        SecurityContextHolder.context.authentication = authentication;
						
						// Fire event only if its the authSuccess url
						if (this.eventPublisher != null && url == '/login/authSuccess') {
							eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));
						}
						
                        if (logger.isDebugEnabled()) {
                            logger.debug("SecurityContextHolder populated with FacebookAuthToken: '"
                                + SecurityContextHolder.context.authentication + "'");
                        }
                        try {
                            chain.doFilter(request, response);
                        } finally {
                            SecurityContextHolder.context.authentication = null;
                        }
                        return
                    }
                } catch(UsernameNotFoundException e) {
					def referer = request.getHeader("referer");
					if(url == '/login/authSuccess') {
						logger.error e.getMessage();
						request.getSession().setAttribute("LAST_FACEBOOK_USER", e.extraInformation);
						logger.debug "Redirecting to $createAccountUrl"
						(new DefaultRedirectStrategy()).sendRedirect(request, response, createAccountUrl);
						return;
					}
                } catch (BadCredentialsException e) {
                    logger.info("Invalid cookie, skip. Message was: $e.message")
                }
            } else {
                logger.debug("No auth cookie")
            }
        } else {
            logger.debug("SecurityContextHolder not populated with FacebookAuthToken token, as it already contained: $SecurityContextHolder.context.authentication");
        }

        //when not authenticated, don't have auth cookie or bad credentials
        chain.doFilter(request, response)
    }

	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
}
