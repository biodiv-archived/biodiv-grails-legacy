package species.auth

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.oauth2.Spring30OAuth2RequestFactory;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.util.StringUtils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;

import species.auth.DefaultAjaxAwareRedirectStrategy;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.utils.Utils;
import species.auth.CustomRegisterCommand;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import grails.plugin.springsecurity.oauth.OAuthToken;

/**
 * Manages associating OpenIDs with application users, both by creating a new local user
 * associated with an OpenID and also by associating a new OpenID to an existing account.
 */
class OpenIdController {

	/** Dependency injection for daoAuthenticationProvider. */
	def daoAuthenticationProvider

	/** Dependency injection for OpenIDAuthenticationFilter. */
	def openIDAuthenticationFilter

	/** Dependency injection for the springSecurityService. */
	def springSecurityService

	def facebookAuthService
	def SUserService
	def portResolver
	def messageSource
	static defaultAction = 'auth'

	def checkauth = { log.debug "inside check auth " + params }

	/**
	 * Shows the login page. The user has the choice between using an OpenID and a username
	 * and password for a local account. If an OpenID authentication is successful but there
	 * is no corresponding local account, they'll be redirected to createAccount to create
	 * a new account, or click through to linkAccount to associate the OpenID with an
	 * existing local account.
	 */
 	def auth = {

		def config = SpringSecurityUtils.securityConfig

 		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}
		
		

		def targetUrl = "";
        def requestCache = new HttpSessionRequestCache();
		def savedRequest = requestCache.getRequest(request, response);
        // request.getSession()?.getAttribute(WebAttributes.SAVED_REQUEST)
		if(savedRequest == null) {
			if(params.login_error) {
				targetUrl = request.getSession().getAttribute("LOGIN_REFERRER");
			} else {
				targetUrl = request.getHeader("Referer")?:"";
				if (StringUtils.hasText(targetUrl)) {
					try {
						targetUrl = URLEncoder.encode(targetUrl, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						def msg = messageSource.getMessage("utf.shouldnt.support", null, RCU.getLocale(request))
						throw new IllegalStateException(msg);
					}
				}
				//params["spring-security-redirect"] = targetUrl
			}
			request.getSession().setAttribute("LOGIN_REFERRER", targetUrl);
			log.debug "Passing targetUrlParameter for redirect: " + targetUrl;
		} else {
			if(Utils.isAjax(savedRequest)) {
		        requestCache.removeRequest(request, response);
			}
		}
		render (view:'auth', model:[openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
					daoPostUrl:"${request.contextPath}${config.apf.filterProcessesUrl}",
					persistentRememberMe: config.rememberMe.persistent,
					rememberMeParameter: config.rememberMe.parameter,
					openidIdentifier: config.openid.claimedIdentityFieldName,
					targetUrl:targetUrl]);
	}

	/**
	 * Initially we're redirected here after a UserNotFoundException with a valid OpenID
	 * authentication. This action is specified by the openid.registration.createAccountUri
	 * attribute.
	 * <p/>
	 * The GSP displays the OpenID that was received by the external provider and keeps it
	 * in the session rather than passing it between submits so the user has no opportunity
	 * to change it.
	 */
	def createAccount = {

		String openId = session[OIAFH.LAST_OPENID_USERNAME]
		List attributes = session[OIAFH.LAST_OPENID_ATTRIBUTES] ?: []
        OAuthToken oAuthToken = session[SpringSecurityOAuthController.SPRING_SECURITY_OAUTH_TOKEN]
            
 		if (!openId && !oAuthToken) {
			flash.error = messageSource.getMessage("login.errors.openid.found", null, RCU.getLocale(request))
			return
		}

		log.debug "Processing OpenId authentication in createAccount/merge"
		String email
        if(openId) {
           emailAttribute = attributes.find { l ->
                if(l.name == 'email') {
                    email = l.values[0]
                }
            }
        } else if(oAuthToken) {
            email = oAuthToken.email
        }

		if (!email) {
			flash.error = messageSource.getMessage("login.errors.emailId.necessary", null, RCU.getLocale(request))
			return
		}

		def user;
		if(email) {
			user = SUser.findByEmail(email)
		}

		if(user) {
			log.info "Found existing user with same emailId $email"
			log.info "Merging details with existing account $user"
            if(openId) {
		    	registerAccountOpenId user.email, openId
            } else if(oAuthToken) {
		    	registerAccountOAuth user.email, oAuthToken
            }
			def usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			authenticateAndRedirect user."$usernamePropertyName", request, response
		} else {
			log.info "Redirecting to register"
			CustomRegisterCommand command = new CustomRegisterCommand();
            if(openId) {
    			copyFromAttributeExchange command
            } else if(oAuthToken) {
                command.email = oAuthToken.email
            }
			command.openId = openId?:oAuthToken.email;
			log.debug "register command : $command"
			flash.chainedParams = [command: command]
			chain ( controller:"register");
		}
	}

	/**
	 * The registration page has a link to this action so an existing user who successfully
	 * authenticated with an OpenID can associate it with their account for future logins.
	 */
	def linkAccount = { OpenIdLinkAccountCommand command ->

		String openId = session[OIAFH.LAST_OPENID_USERNAME]
		if (!openId) {
			flash.error = messageSource.getMessage("login.errors.openid.found", null, RCU.getLocale(request))
			return [command: command]
		}

		if (!request.post) {
			// show the form
			command.clearErrors()
			return [command: command, openId: openId]
		}

		if (command.hasErrors()) {
			return [command: command, openId: openId]
		}

		try {
			registerAccountOpenId command.username, command.password, openId
		}
		catch (AuthenticationException e) {
			flash.error = messageSource.getMessage("login.errors.validate.msg", null, RCU.getLocale(request))
			return [command: command, openId: openId]
		}

		def usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		authenticateAndRedirect user."$usernamePropertyName", request, response

	}

	def createFacebookAccount = {

		def token = session["LAST_FACEBOOK_USER"]

		if (!token) {
			flash.error = messageSource.getMessage("login.errors.facebook.fetch.accessToken", null, RCU.getLocale(request))
			return
		}

		log.debug "Processing facebook registration in createAccount"
		FacebookTemplate facebook = new FacebookTemplate(token.accessToken.accessToken);
		facebook.setRequestFactory(new Spring30OAuth2RequestFactory(ClientHttpRequestFactorySelector.getRequestFactory(), token.accessToken.accessToken, facebook.getOAuth2Version()));
		FacebookProfile fbProfile = facebook.userOperations().getUserProfile();

		//TODO: if there are multiple email accounts available choose among them
		String email = fbProfile.email;

		if (!email) {
			flash.error = messageSource.getMessage("login.errors.emailId.necessary", null, RCU.getLocale(request))
			return
		}

		def user;
		if(email) {
			user = SUser.findByEmail(email)
		}

		if(user) {
			log.info "Found existing user with same emailId $email"
			log.info "Merging details with existing account $user"
			facebookAuthService.mergeFacebookUserDetails user, fbProfile
			registerAccountOpenId user.email, fbProfile.link
			facebookAuthService.registerFacebookUser token, user

			def usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			authenticateAndRedirect user."$usernamePropertyName", request, response
		} else {
			log.info "Redirecting to register"
			CustomRegisterCommand command = new CustomRegisterCommand();
			facebookAuthService.copyFromFacebookProfile command, fbProfile
			command.openId = fbProfile.link
			command.facebookUser = true;
			log.debug "register command : $command"
			flash.chainedParams = [command: command]
			chain ( controller:"register");
		}
	}

	/**
	 * Authenticate the user for real now that the account exists/is linked and redirect
	 * to the originally-requested uri if there's a SavedRequest.
	 *
	 * @param username the user's login name
	 */
	private void authenticateAndRedirect(String username, final HttpServletRequest request, final HttpServletResponse response) {
		session.removeAttribute OIAFH.LAST_OPENID_USERNAME
		session.removeAttribute OIAFH.LAST_OPENID_ATTRIBUTES
		session.removeAttribute "LAST_FACEBOOK_USER"
		session.removeAttribute SpringSecurityOAuthController.SPRING_SECURITY_OAUTH_TOKEN

		springSecurityService.reauthenticate username

		def config = SpringSecurityUtils.securityConfig

		def savedRequest = session['SPRING_SECURITY_SAVED_REQUEST_KEY']
		if (savedRequest && !config.successHandler.alwaysUseDefault) {
			(new DefaultAjaxAwareRedirectStrategy()).sendRedirect(request, response, savedRequest.getRedirectUrl());
		}
		else {
			redirect uri: config.successHandler.defaultTargetUrl
		}
	}

    /**
	 * Associates an OpenID with an existing account. Needs the user's password to ensure
	 * that the user owns that account, and authenticates to verify before linking.
	 * @param username  the username
	 * @param password  the password
	 * @param openId  the associated OpenID
	 */
	private void registerAccountOpenId(String username, String password, String openId) {
		// check that the user exists, password is valid, etc. - doesn't actually log in or log out,
		// just checks that user exists, password is valid, account not locked, etc.
		daoAuthenticationProvider.authenticate(
				new UsernamePasswordAuthenticationToken(username, password))

		def user = SUser.findByUsername(username)
		registerAccountOpenId(user.email, openId);
	}

	private void registerAccountOpenId(String email, String openId) {
		SUser.withTransaction { status ->
			def user = SUser.findByEmail(email);
		
			user.addToOpenIds(url: openId)
			if (!user.save(flush:true, failOnError:true)) {
				log.error "Coudn't save user openIds"
				status.setRollbackOnly()
			}

			SUserService.assignRoles(user);
		}

	}


    private void registerAccountOAuth(String email, OAuthToken oAuthToken) {
        SUser.withTransaction { status ->
            SUser user = SUser.findByEmail(email)
            if (user) {
                user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)
                if (user.validate() && user.save()) {
                    SUserService.assignRoles(user);
                    return true
                }
            } 
            status.setRollbackOnly()
            return false
        }
    }

	/**
	 * For the initial form display, copy any registered AX values into the command.
	 * @param command  the command
	 */
	private void copyFromAttributeExchange(CustomRegisterCommand command) {
		List attributes = session[OIAFH.LAST_OPENID_ATTRIBUTES] ?: []
		String firstName, lastName="";
		for (attribute in attributes) {

			String name = attribute.name
			if(name == "firstname") {
				firstName = attribute.values[0]
			} else if(name == "lastname") {
				lastName = attribute.values[0]
			}

			if (command.hasProperty(name)) {
				command."$name" = attribute.values[0]
			}
		}

		if(firstName) {
			command.name = firstName + " "+ lastName
			command.name = command.name.trim();
		}
	}

}

class OpenIdRegisterCommand {

	String username = ""
	String password = ""
	String password2 = ""

	static constraints = {
		username blank: false, validator: { String username, command ->
			SUser.withNewSession { session ->
				if (username && SUser.countByUsername(username)) {
					return 'openIdRegisterCommand.username.error.unique'
				}
			}
		}
		password blank: false, minSize: 8, maxSize: 64, validator: { password, command ->
			if (command.username && command.username.equals(password)) {
				return 'openIdRegisterCommand.password.error.username'
			}

			if (password && password.length() >= 8 && password.length() <= 64 &&
			(!password.matches('^.*\\p{Alpha}.*$') ||
			!password.matches('^.*\\p{Digit}.*$') ||
			!password.matches('^.*[!@#$%^&].*$'))) {
				return 'openIdRegisterCommand.password.error.strength'
			}
		}
		password2 validator: { password2, command ->
			if (command.password != password2) {
				return 'openIdRegisterCommand.password2.error.mismatch'
			}
		}
	}
}

class OpenIdLinkAccountCommand {

	String username = ""
	String password = ""

	static constraints = {
		username blank: false
		password blank: false
	}
}
