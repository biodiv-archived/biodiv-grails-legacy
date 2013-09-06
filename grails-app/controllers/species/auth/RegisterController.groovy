package species.auth;

import grails.util.Environment;

import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import species.auth.SUser;
import species.participation.Observation;
import species.utils.Utils;
import org.springframework.security.web.WebAttributes;
import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;

class RegisterController extends grails.plugins.springsecurity.ui.RegisterController {
	
	
	def SUserService;
	def facebookAuthService;
	def springSecurityService;
	def openIDAuthenticationFilter;
	def jcaptchaService;
	def activityFeedService;
	//def recaptchaService;	
	
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
		def savedRequest = request.getSession()?.getAttribute(WebAttributes.SAVED_REQUEST)
		if(savedRequest != null) {
			if(Utils.isAjax(savedRequest)) {
				request.getSession()?.removeAttribute(WebAttributes.SAVED_REQUEST)
			}
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
		def config = SpringSecurityUtils.securityConfig
		
		def redirectModel = [openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
					daoPostUrl:    "${request.contextPath}${config.apf.filterProcessesUrl}",
					persistentRememberMe: config.rememberMe.persistent,
					rememberMeParameter: config.rememberMe.parameter,
					openidIdentifier: config.openid.claimedIdentityFieldName]
		
		log.debug "Registering user $command"
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
		def conf = SpringSecurityUtils.securityConfig
		if (command.hasErrors()) {
			redirectModel.command = command
			render view: 'index', model: redirectModel
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

		//recaptchaService.cleanUp(session)
		
		def userProfileUrl = generateLink("SUser", "show", ["id": user.id], request)
		activityFeedService.addActivityFeed(user, user, user, activityFeedService.USER_REGISTERED);
		SUserService.sendNotificationMail(SUserService.NEW_USER, user, request, userProfileUrl);
		
		if(command.openId) {
			authenticateAndRedirect user.email
			return	
		} else {
			registerAndEmail user.username, user.email, request		
			redirectModel.emailSent = true
			render view: 'index', model: redirectModel
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
		redirect url: conf.ui.register.postRegisterUrl 
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
		
		String url = generateLink('register', 'resetPassword', [t: registrationCode.token], request)
		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.forgotPassword.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: user.name.capitalize(), url: url])
		}
		
		try {
			mailService.sendMail {
				to user.email
				from conf.ui.forgotPassword.emailFrom
				subject conf.ui.forgotPassword.emailSubject
				html body.toString()
			}
		
			[emailSent: true]
		}catch(all)  {
		      log.error all.getMessage()
		      [emailSent:false]
		}
	}

    def resetPassword = { ResetPasswordCommand2 command ->
        log.debug params
        String token = params.t

        def registrationCode = token ? RegistrationCode.findByToken(token) : null
        if (!registrationCode) {
            flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
            return
        }

        if (!request.post) {
            return [token: token, command: new ResetPasswordCommand2()]
        }

        String usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
        def command2 = new ResetPasswordCommand2(username:registrationCode.username, password:command.password, password2:command.password2);
        command2.validate()

        if (command2.hasErrors()) {
            command2.errors.allErrors.each {
                log.error it
            }
            return [token: token, command: command]
        }
        String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
        RegistrationCode.withTransaction { status ->
            def user = lookupUserClass().findWhere((usernamePropertyName): registrationCode.username)
            user.password = springSecurityUiService.encodePassword(command.password, salt)
            user.accountLocked = false;
            user.save()
            registrationCode.delete()
            SUserService.assignRoles(user);
        }

        springSecurityService.reauthenticate registrationCode.username

        flash.message = message(code: 'spring.security.ui.resetPassword.success')

        def conf = SpringSecurityUtils.securityConfig
        String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
        redirect uri: postResetUrl
    }

	protected String generateLink(String controller, String action, linkParams, request) {
		uGroup.createLink(base: Utils.getDomainServerUrl(request),
				controller: controller, action: action, 'userGroupWebaddress':params.webaddress,
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

		String url = generateLink('register', 'verifyRegistration', [t: registrationCode.token], request)
		sendVerificationMail(username,email,url,request)
	}
	
	def resend = {
		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]?.decodeHTML()
		def registrationCode = RegistrationCode.findByUsername(username)
		String url = generateLink('register', 'verifyRegistration', [t: registrationCode.token], request)
		sendVerificationMail(username,username,url,request)
	}

	protected void sendVerificationMail(String username, String email, String url, request)  {

		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.register.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [username: username.capitalize(), url: url])
		}
		def sub = conf.ui.register.emailSubject
		
		if (sub.contains('$')) {
			sub = evaluate(sub, [domain: Utils.getDomainName(request)])
		}
		
		try {
			mailService.sendMail {
				to email
				from conf.ui.register.emailFrom
				subject sub.toString()
				html body.toString()
			}
			clearRegistrationInfoFromSession()
		}catch(all)  {
		    log.error all.getMessage()
		}
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
			flash.message = message(code: 'spring.security.ui.register.completeSimple')
			redirect url: savedRequest.redirectUrl
		}
		else {
			flash.message = message(code: 'spring.security.ui.register.complete')
			redirect url: config.ui.register.postRegisterUrl
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
	//String recaptcha_response_field;
	//String recaptcha_challenge_field;
	String captcha_response;
	
	def grailsApplication
	def jcaptchaService;
	//def recaptchaService;
	
		
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
		captcha_response blank:false, nullable:false, validator: { value, command ->
			def session = RCH.requestAttributes.session
			def request = RCH.requestAttributes.request
			try{
				if (!command.jcaptchaService.validateResponse("imageCaptcha", session.id, command.captcha_response)) {
					//if(!command.recaptchaService.verifyAnswer(session, request.getRemoteAddr(), command)) {
					return 'reCaptcha.invalid.message'
				}
			}catch (com.octo.captcha.service.CaptchaServiceException e) {
				// TODO: handle exception
				e.printStackTrace()
				return 'reCaptcha.invalid.message'
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.properties.toString();
	}
}


class ResetPasswordCommand2 {
    String username
    String password
    String password2

    static constraints = {
        username nullable: false, email: true
        password blank: false, nullable: false, validator: grails.plugins.springsecurity.ui.RegisterController.passwordValidator
        password2 validator: RegisterController.password2Validator
    }
}
