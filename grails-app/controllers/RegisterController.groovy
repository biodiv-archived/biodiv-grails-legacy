import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH

import species.utils.Utils;

import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;

class RegisterController extends grails.plugins.springsecurity.ui.RegisterController {

	def SUserService;
	def facebookAuthService;
	def springSecurityService;
	def openIDAuthenticationFilter;
	
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
		def config = SpringSecurityUtils.securityConfig
		if(flash.chainedParams?.command) {
			log.debug("Registration with openId command: $flash.chainedParams");
			['command': flash.chainedParams?.command, 
				openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
				daoPostUrl:    "${request.contextPath}${config.apf.filterProcessesUrl}",
				persistentRememberMe: config.rememberMe.persistent,
				rememberMeParameter: config.rememberMe.parameter,
				openidIdentifier: config.openid.claimedIdentityFieldName
				]
		} else {
			log.debug("Registration with params : $flash.chainedParams");
			def copy = [:] + (flash.chainedParams ?: [:])
			if(flash.chainedParams?.timezone) {
				copy.timezone = params.float(copy.timezone)
			}
			copy.remove 'controller'
			copy.remove 'action'
			['command': new CustomRegisterCommand(copy), 
				openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
					daoPostUrl:    "${request.contextPath}${config.apf.filterProcessesUrl}",
					persistentRememberMe: config.rememberMe.persistent,
					rememberMeParameter: config.rememberMe.parameter,
					openidIdentifier: config.openid.claimedIdentityFieldName]
		}
	}
	
	def register = { CustomRegisterCommand command ->

		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
		def conf = SpringSecurityUtils.securityConfig
		if (command.hasErrors()) {
			render view: 'index', model: [command: command]
			return
		}

		def user = SUserService.create(command.properties);
		
		if(command.openId) {
			log.debug("Is an openId registration");
			user.accountLocked = false;
			user.addToOpenIds(url: command.openId);
			user.password = "openIdPassword"
			
			SUserService.save(user);
			
			if(command.facebookUser) {
				log.debug "registering facebook user"
				def token = session["LAST_FACEBOOK_USER"]
				facebookAuthService.registerFacebookUser token, user
			}
			
			SUserService.assignRoles(user);
		} else {
			log.debug("Is an local account registration");
			user.accountLocked = true;
			SUserService.save(user);
		}
		
		if (user == null || user.hasErrors()) {
			flash.error = message(code: 'spring.security.ui.register.miscError')
			flash.chainedParams = params
			redirect action: 'index'
			return
		}

		if(command.openId) {
			flash.message = message(code: 'spring.security.ui.register.complete')
			authenticateAndRedirect user.email
			return	
		} else {
			registerAndEmail user.username, user.email, request			
			render view: 'index', model: [emailSent: true]
			return
		}

	}

	def verifyRegistration = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
		def conf = SpringSecurityUtils.securityConfig
		String defaultTargetUrl = conf.successHandler.defaultTargetUrl
		String usernamePropertyName = conf.userLookup.usernamePropertyName

		String token = params.t

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			flash.error = message(code: 'spring.security.ui.register.badCode')
			redirect uri: defaultTargetUrl
			return
		}

		def user
		RegistrationCode.withTransaction { status ->
			user = lookupUserClass().findWhere((usernamePropertyName): registrationCode.username)

			if (!user) {
				return
			}
			user.accountLocked = false
			user.save(flush:true)
			SUserService.assignRoles(user);
			registrationCode.delete()
		}

		if (!user) {
			flash.error = message(code: 'spring.security.ui.register.badCode')
			redirect uri: defaultTargetUrl
			return
		}

		springSecurityService.reauthenticate user."$usernamePropertyName"

		flash.message = message(code: 'spring.security.ui.register.complete')
		redirect uri: conf.ui.register.postRegisterUrl ?: defaultTargetUrl
	}

	def forgotPassword = {
		if (!request.post) {
			// show the form
			return
		}
		
		String username = params.username
		if (!username) {
			flash.error = message(code: 'spring.security.ui.forgotPassword.username.missing')
			redirect action: 'forgotPassword'
			return
		}
		
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		def user = lookupUserClass().findWhere((usernameFieldName): username)
		if (!user) {
			flash.error = message(code: 'spring.security.ui.forgotPassword.user.notFound')
			redirect action: 'forgotPassword'
			return
		}
		
		def registrationCode = new RegistrationCode(username: user."$usernameFieldName")
		registrationCode.save(flush: true)
		
		String url = generateLink('resetPassword', [t: registrationCode.token], request)
		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.forgotPassword.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: user.name.capitalize(), url: url])
		}
		
		mailService.sendMail {
			to user.email
			from conf.ui.forgotPassword.emailFrom
			subject conf.ui.forgotPassword.emailSubject
			html body.toString()
		}
		
		[emailSent: true]
	}
	
	def resetPassword = { ResetPasswordCommand command ->
		log.debug params
		String token = params.t

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}

		if (!request.post) {
			return [token: token, command: new ResetPasswordCommand()]
		}

		String usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		def command2 = new ResetPasswordCommand(username:registrationCode.username, password:command.password, password2:command.password2);
		command2.validate()

		if (command2.hasErrors()) {
			return [token: token, command: command]
		}

		String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
		RegistrationCode.withTransaction { status ->
			def user = lookupUserClass().findWhere((usernamePropertyName): registrationCode.username)
			user.password = springSecurityUiService.encodePassword(command.password, salt)
			user.save()
			registrationCode.delete()
		}

		springSecurityService.reauthenticate registrationCode.username

		flash.message = message(code: 'spring.security.ui.resetPassword.success')

		def conf = SpringSecurityUtils.securityConfig
		String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
		redirect uri: postResetUrl
	}

	protected String generateLink(String action, linkParams, request) {
		createLink(base: Utils.getDomainServerUrl(request),
				controller: 'register', action: action,
				params: linkParams)
	}

	static myPasswordValidator = { String password, command ->
		String usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		if (command."$usernamePropertyName" && command."$usernamePropertyName".equals(password)) {
			return "command.password.error.email"
		}

		if (!checkPasswordMinLength(password, command) ||
		!checkPasswordMaxLength(password, command) ||
		!checkPasswordRegex(password, command)) {
			return 'command.password.error.strength'
		}
	}

	protected void registerAndEmail(String username, String email, request) {
		RegistrationCode registrationCode = SUserService.register(email)

		if (registrationCode == null || registrationCode.hasErrors()) {
			flash.error = message(code: 'spring.security.ui.register.miscError')
			flash.chainedParams = params
			redirect action: 'index'
			return
		}

		String url = generateLink('verifyRegistration', [t: registrationCode.token], request)

		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.register.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: username.capitalize(), url: url])
		}
		def sub = conf.ui.register.emailSubject
		println sub
		if (sub.contains('$')) {
			sub = evaluate(sub, [domain: Utils.getDomainName(request)])
		}
		println sub;
		mailService.sendMail {
			to email
			from conf.ui.register.emailFrom
			subject sub.toString()
			html body.toString()
		}
		clearRegistrationInfoFromSession()
	}
	
	
	/**
	 * Authenticate the user for real now that the account exists/is linked and redirect
	 * to the originally-requested uri if there's a SavedRequest.
	 *
	 * @param username the user's login name
	 */
	protected void authenticateAndRedirect(String username) {
		clearRegistrationInfoFromSession()
		
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

	private void clearRegistrationInfoFromSession() {
		session.removeAttribute OIAFH.LAST_OPENID_USERNAME
		session.removeAttribute OIAFH.LAST_OPENID_ATTRIBUTES
		session.removeAttribute "LAST_FACEBOOK_USER"
	}
}


class CustomRegisterCommand {
	String username
	String email
	String password
	String password2
	String name
	String website
	float timezone=0
	String aboutMe;
	String location;
	String profilePic;
	String openId;
	boolean facebookUser;

	def grailsApplication

	static constraints = {
		email email: true, blank: false, nullable: false, validator: { value, command ->
			if (value) {
				def User = command.grailsApplication.getDomainClass(
						SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
				if (User.findByEmail(value)) {
					return 'registerCommand.email.unique'
				}
			}
		}
		password blank: false, nullable: false, validator: RegisterController.myPasswordValidator
		password2 validator: RegisterController.password2Validator
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.properties.toString();
	}
}


class ResetPasswordCommand {
	String username
	String password
	String password2

	static constraints = {
		username nullable: false, email: true
		password blank: false, nullable: false, validator: grails.plugins.springsecurity.ui.RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}
