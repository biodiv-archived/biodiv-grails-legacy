import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.oauth2.Spring30OAuth2RequestFactory;
import org.springframework.social.support.ClientHttpRequestFactorySelector;

import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;

import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole

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
	
	static defaultAction = 'auth'

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

		[openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
					daoPostUrl:    "${request.contextPath}${config.apf.filterProcessesUrl}",
					persistentRememberMe: config.rememberMe.persistent,
					rememberMeParameter: config.rememberMe.parameter,
					openidIdentifier: config.openid.claimedIdentityFieldName]
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

		if (!openId) {
			flash.error = 'Sorry, an OpenID was not found'
			return
		}

		log.debug "Processing OpenId authentication in createAccount/merge"
		
		def emailAttribute = attributes.find { l ->
			if(l.name == 'email') {
				return l;
			}
		}

		//TODO: if there are multiple email accounts available choose among them
		String email = emailAttribute.values[0];

		if (!email) {
			flash.error = 'Sorry, an email id is necessary for an account'
			return
		}

		def user;
		if(email) {
			user = SUser.findByEmail(email)
		}
		
		if(user) {
			log.info "Found existing user with same emailId $email"
			log.info "Merging details with existing account $user"
			registerAccountOpenId user.email, openId
			def usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			authenticateAndRedirect user."$usernamePropertyName"
		} else {
			log.info "Redirecting to register"
			RegisterCommand command = new RegisterCommand();
			copyFromAttributeExchange command
			command.openId = openId;
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
			flash.error = 'Sorry, an OpenID was not found'
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
			flash.error = 'Sorry, no user was found with that username and password'
			return [command: command, openId: openId]
		}
		
		def usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		authenticateAndRedirect user."$usernamePropertyName"
		
	}
	
	def createFacebookAccount = {

		def token = session["LAST_FACEBOOK_USER"]

		if (!token) {
			flash.error = 'Sorry, problem fetching access token from facebook'
			return
		}

		log.debug "Processing facebook registration in createAccount"
		FacebookTemplate facebook = new FacebookTemplate(token.accessToken);
		facebook.setRequestFactory(new Spring30OAuth2RequestFactory(ClientHttpRequestFactorySelector.getRequestFactory(), token.accessToken, facebook.getOAuth2Version()));
		FacebookProfile fbProfile = facebook.userOperations().getUserProfile();

		//TODO: if there are multiple email accounts available choose among them
		String email = fbProfile.email;

		if (!email) {
			flash.error = 'Sorry, an email id is necessary for an account'
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
			authenticateAndRedirect user."$usernamePropertyName"
		} else {
			log.info "Redirecting to register"
			RegisterCommand command = new RegisterCommand();
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
	private void authenticateAndRedirect(String username) {
		session.removeAttribute OIAFH.LAST_OPENID_USERNAME
		session.removeAttribute OIAFH.LAST_OPENID_ATTRIBUTES
		session.removeAttribute "LAST_FACEBOOK_USER"
		
		springSecurityService.reauthenticate username

		def config = SpringSecurityUtils.securityConfig

		def savedRequest = session[DefaultSavedRequest.SPRING_SECURITY_SAVED_REQUEST_KEY]
		if (savedRequest && !config.successHandler.alwaysUseDefault) {
			redirect url: savedRequest.redirectUrl
		}
		else {
			redirect uri: config.successHandler.defaultTargetUrl
		}
	}

	/**
	 * Create the user instance and grant any roles that are specified in the config
	 * for new users.
	 * @param username  the username
	 * @param password  the password
	 * @param openId  the associated OpenID
	 * @return  true if successful
	 */
	private boolean createNewAccount(String username, String password, String openId) {
		boolean created = SUser.withTransaction { status ->
			def config = SpringSecurityUtils.securityConfig

			password = springSecurityService.encodePassword(password)
			def user = new SUser(username: username, password: password, enabled: true)

			user.addToOpenIds(url: openId)

			if (!user.save()) {
				return false
			}

			for (roleName in config.openid.registration.roleNames) {
				SUserRole.create user, Role.findByAuthority(roleName)
			}
			return true
		}
		return created
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
				status.setRollbackOnly()
			}
			
//			//user roles getting deleted...patch to add them again onmerge.
//			def userRole = SUserRole.findBySUser(user);
//			if(!userRole) {
//				SUserService.assignRoles(user);
//			}
		}
		
	}

	

	/**
	 * For the initial form display, copy any registered AX values into the command.
	 * @param command  the command
	 */
	private void copyFromAttributeExchange(RegisterCommand command) {
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
