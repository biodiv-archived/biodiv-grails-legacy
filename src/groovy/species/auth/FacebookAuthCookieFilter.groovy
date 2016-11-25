package species.auth

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean
import species.auth.FacebookAuthToken

import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.utils.Utils;


import org.springframework.social.facebook.api.FacebookProfile;


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
    def facebookService

    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, javax.servlet.FilterChain chain) {
        HttpServletRequest request = servletRequest
        HttpServletResponse response = servletResponse
        String url = request.requestURI.substring(request.contextPath.length())
        logger.debug("Processing url: $url with params : ${request.getParameterMap()} with method : ${request.getMethod()}")
        //logger.debug("SecurityContext authentication : ${SecurityContextHolder.context.authentication }");
        if (url != logoutUrl && SecurityContextHolder.context.authentication == null) {
            //logger.debug("Applying facebook auth filter")
            assert facebookAuthUtils != null
            Cookie cookie = facebookAuthUtils.getAuthCookie(request)
            Cookie fbLoginCookie = facebookAuthUtils.getFBLoginCookie(request);
            if (cookie != null && fbLoginCookie != null) {
                //logger.debug("Found fbsr & fb_login cookie");
                FacebookAuthToken token;
                try {
                    token = facebookAuthUtils.build(cookie.value)
                    if (token != null) {
                        //logger.debug("Got fbAuthToken $token");
                        token.user = request.getSession().getAttribute("LAST_FACEBOOK_USER");
                        Authentication authentication = null
                        authentication = authenticationManager.authenticate(token);
                        if (authentication && authentication.authenticated) {


                            // Store to SecurityContextHolder
                            SecurityContextHolder.context.authentication = authentication;

                            // Fire event only if its the authSuccess url
                            if (this.eventPublisher != null && (url == '/login/authSuccess' || url == '/oauth/google/success')) {
                                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));
                            }

                            if (logger.isDebugEnabled()) {
                                //logger.debug("SecurityContextHolder populated with FacebookAuthToken: '"
                                //+ SecurityContextHolder.context.authentication + "'");
                            }
                            try {
                                chain.doFilter(request, response);
                            } finally {
                                SecurityContextHolder.context.authentication = null;
                            }
                            return
                        }
                    }
                } catch(UsernameNotFoundException e) {
                    logger.info("UsernameNotFoundException: $e.message")
                    def referer = request.getHeader("referer");
                    if(url == '/login/authSuccess' || url == '/oauth/google/success') {
                        handleAuthSuccess(request, response, token, e);
                        return;
                    }
                } catch (BadCredentialsException e) {
                    logger.info("Invalid cookie, skip. Message was: $e.message")
                    unsuccessfulAuthentication(request, response, e);
                    return;
                } catch (AuthenticationServiceException e) {
                    logger.info("Message : $e.message")
                    if(url == '/login/authSuccess' || url == '/oauth/google/success') {
                        handleAuthSuccess(request, response, token, e);
                        return;
                    }
                    //		unsuccessfulAuthentication(request, response, e);
                    //        return;
                } catch(AuthenticationException e) {
                    logger.info("Auth exception. Message was: $e.message")
                    unsuccessfulAuthentication(request, response, e);
                    return;
                }
            } else {
                if(!cookie) {
                    //logger.debug("No auth cookie");
                }
                if(!fbLoginCookie) {
                    //logger.debug("No fb_login cookie");
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

    private void handleAuthSuccess(request, response, token, e) {
        if(SpringSecurityUtils.isAjax(request)) {
            logger.error "Unsuccessful ajax authentication:  $e.message";
            unsuccessfulAuthentication(request, response, e);
        } else {
            logger.error "Unsuccessful authentication:  $e.message";
            request.getSession().setAttribute("LAST_FACEBOOK_USER", token);
            logger.debug "Redirecting to $createAccountUrl"
            (new DefaultRedirectStrategy()).sendRedirect(request, response, createAccountUrl);
        }
        return
    }
}
