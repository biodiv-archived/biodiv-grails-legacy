package species.auth

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.plugins.springsecurity.ReflectionUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.Assert;
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

	private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
	private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

	def grailsApplication

	void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
		HttpServletRequest request = servletRequest
		HttpServletResponse response = servletResponse
		String url = request.requestURI.substring(request.contextPath.length())
		logger.debug("Processing url: $url with params : ${request.getParameterMap()}")
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
					logger.info("UsernameNotFoundException: $e.message")
					def referer = request.getHeader("referer");
					if(url == '/login/authSuccess') {
						if(SpringSecurityUtils.isAjax(request)) {
							logger.error "Unsuccessful ajax authentication:  $e.message";
							unsuccessfulAuthentication(request, response, e);
							return;
						} else {
							logger.error "Unsuccessful authentication:  $e.message";
							request.getSession().setAttribute("LAST_FACEBOOK_USER", e.extraInformation);
							logger.debug "Redirecting to $createAccountUrl"
							(new DefaultRedirectStrategy()).sendRedirect(request, response, createAccountUrl);
							return;
						}
					}
				} catch (BadCredentialsException e) {
					logger.info("Invalid cookie, skip. Message was: $e.message")
				} catch(AuthenticationException e) {
					logger.info("Auth exception. Message was: $e.message")
					unsuccessfulAuthentication(request, response, e);
					return;
				}
			} else {
				if(!cookie) {
					logger.warn("No auth cookie");
				}
				if(!fbLoginCookie) {
					logger.debug("No fb_login cookie");
				}
//				logger.debug("Found following cookies");
//				request.cookies.each { logger.debug it.name+":"+it.value }
			}
		} else {
			logger.debug("SecurityContextHolder not populated with FacebookAuthToken token , as it already contained: $SecurityContextHolder.context.authentication");
		}

		//when not authenticated, don't have auth cookie or bad credentials
		chain.doFilter(request, response)
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Default behaviour for unsuccessful authentication.
	 * <ol>
	 * <li>Clears the {@link SecurityContextHolder}</li>
	 * <li>Stores the exception in the session (if it exists or <tt>allowSesssionCreation</tt> is set to <tt>true</tt>)</li>
	 * <li>Informs the configured <tt>RememberMeServices</tt> of the failed login</li>
	 * <li>Delegates additional behaviour to the {@link AuthenticationFailureHandler}.</li>
	 * </ol>
	 */
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	AuthenticationException failed) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Authentication request failed: " + failed.toString());
			logger.debug("Updated SecurityContextHolder to contain null Authentication");
			logger.debug("Delegating to authentication failure handler" + failureHandler);
		}
		
		facebookAuthUtils.logout(request, response);
		//rememberMeServices.loginFail(request, response);
		failureHandler.onAuthenticationFailure(request, response, failed);
	}
	
	public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
		Assert.notNull(failureHandler, "failureHandler cannot be null");
		this.failureHandler = failureHandler;
	}
}
