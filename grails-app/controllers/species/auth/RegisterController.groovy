package species.auth;

import grails.util.Environment;

import grails.plugin.springsecurity.annotation.Secured;
import grails.plugin.springsecurity.authentication.dao.NullSaltSource;
import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.plugin.springsecurity.ui.RegistrationCode;
import grails.plugin.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import species.auth.SUser;
import species.participation.Observation;
import species.utils.Utils;
import org.springframework.security.web.WebAttributes;
import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import species.groups.UserGroup;
import grails.converters.JSON;

class RegisterController extends grails.plugin.springsecurity.ui.RegisterController {
	
	
	def SUserService;
	def facebookAuthService;
	def springSecurityService;
	def openIDAuthenticationFilter;
	def jcaptchaService;
	def activityFeedService;
	//def recaptchaService;	
    //def grailsApplication

	static allowedMethods = [user:"POST", register:"POST", 'forgotPasswordMobile':'POST']

	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return;
		}
		
        def requestCache = new HttpSessionRequestCache();
        def savedRequest = requestCache.getRequest(request, response);
		//def savedRequest = request.getSession()?.getAttribute(WebAttributes.SAVED_REQUEST)
		if(savedRequest != null) {
			if(Utils.isAjax(savedRequest)) {
                requestCache.removeRequest(request,response);
		//		request.getSession()?.removeAttribute(WebAttributes.SAVED_REQUEST)
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
			} else {
			    SUserService.assignRoles(user);
            }
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
        if(params.webaddress) {
            //trigger joinUs 
            def userGroupInstance = UserGroup.findByWebaddress(params.webaddress);
            if(userGroupInstance) {
                if(userGroupInstance.allowUsersToJoin) {
                    def founder = userGroupInstance.getFounders(1,0)[0]; 
                    log.debug "Adding ${user} to the group ${userGroupInstance} using founder ${founder} authorities ";
                    SpringSecurityUtils.doWithAuth(founder.email, {
                        if(userGroupInstance.addMember(user)) {
                            flash.message = "You have joined ${userGroupInstance.name} group. We look forward for your contribution.";
                        }
                    });
                }
            } else {
                log.error "Cannot find usergroup with webaddress : "+params.webaddress;
            }
        }


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

    def user( CustomRegisterCommand2 command ) {
		def config = SpringSecurityUtils.securityConfig
		
		log.debug "Registering user $command"
		if (springSecurityService.isLoggedIn()) {
            render(['success':false, 'msg':'Already logged in'] as JSON) 
			return;
		}
		
		def conf = SpringSecurityUtils.securityConfig
        if (command.hasErrors()) {
            def errors = [];
            for (int i = 0; i < command.errors.allErrors.size(); i++) {
                def formattedMessage = g.message(code: command.errors.getFieldError(command.errors.allErrors.get(i).field).code)
                errors << [field: command.errors.allErrors.get(i).field, message: formattedMessage]
            }
            render(['success':false, 'msg':"Failed to register the user because of the following errors: ${errors}"] as JSON) 
			return
		}

        if(!command.username) command.username = command.name;

		def user = SUserService.create(command.properties);
		
			log.debug("Is an local account registration");
			user.accountLocked = true;
			SUserService.save(user);
		
		if (user == null || user.hasErrors()) {
            def errors = [];
            if(user) {
            for (int i = 0; i < user.errors.allErrors.size(); i++) {
                def formattedMessage = g.message(code: command.errors.getFieldError(command.errors.allErrors.get(i).field).code)
                errors << [field: command.errors.allErrors.get(i).field, message: formattedMessage]
            }
            } else {
                errors << "User is null."
            }
            

            render(['success':false, 'msg':"Failed to register the user because of the following errors: ${errors}"] as JSON) 
			return
		}

		def userProfileUrl = generateLink("SUser", "show", ["id": user.id], request)
		activityFeedService.addActivityFeed(user, user, user, activityFeedService.USER_REGISTERED);
		SUserService.sendNotificationMail(SUserService.NEW_USER, user, request, userProfileUrl);

        def registrationCode = registerAndEmail user.username, user.email, request, false	
        
		if (registrationCode == null || registrationCode.hasErrors()) {
            render(['success':false, 'msg':"Successfully registered new user '${user}' but there was an error while sending verification code. As a result your account will be locked. Please try to do a forgot password to unlock your account."] as JSON) 
        }
        render(['success':true, 'msg':"Welcome user ${user}. A verification link has been sent to ${user.email}. Please click on the verification link in the email to activate your account."] as JSON) 
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

	def forgotPasswordMobile () {
        params.isMobileApp = true;
        forgotPassword();
    }

	def forgotPassword () {
		String username = params.username?:params.email
		if (!username) {
            flash.error = message(code: 'spring.security.ui.forgotPassword.username.missing')
            if(request.getHeader('X-Auth-Token') || params.isMobileApp) {
                render (['success':false, 'msg':flash.error] as JSON);
                return;
            } else {
                render view: 'forgotPassword'
                return
            }
		}
		
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		def user = lookupUserClass().findWhere((usernameFieldName): username)
		if (!user) {
			flash.error = message(code: 'spring.security.ui.forgotPassword.user.notFound')
            if(request.getHeader('X-Auth-Token') || params.isMobileApp) {
                render (['success':false, 'msg':flash.error] as JSON);
                return;
            } else {
			    render view: 'forgotPassword'
			    return
            }
		}
        flash.error = '';
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
				from grailsApplication.config.grails.mail.default.from
				subject conf.ui.forgotPassword.emailSubject
				html body.toString()
			}
            if(request.getHeader('X-Auth-Token') || params.isMobileApp) {
                render (['success':true, 'msg':"An email has been sent to ${user.email}. Please click on the link in the email."] as JSON);
                return;
            } else {
			    [emailSent: true]
            }
		} catch(all)  {
            all.printStackTrace();
            if(request.getHeader('X-Auth-Token') || params.isMobileApp) {
		        log.error all.getMessage()
                render (['success':false, 'msg':"Error while generating token. ${all.getMessage()}"] as JSON);
                return;
            } else {
		      log.error all.getMessage()
		      [emailSent:false]
            }
		}
	}

    def resetPassword = { ResetPasswordCommand2 command ->
        String token = params.t

        def registrationCode = token ? RegistrationCode.findByToken(token) : null
        if (!registrationCode) {
            flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
            return
        }
        flash.error = '';

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
            user.password = command.password;//springSecurityUiService.encodePassword(command.password, salt)
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

	protected RegistrationCode registerAndEmail(String username, String email, request, boolean redirect=true) {
		RegistrationCode registrationCode = SUserService.register(email)
		if (registrationCode == null || registrationCode.hasErrors()) {
			flash.error = message(code: 'spring.security.ui.register.miscError')
			flash.chainedParams = params
            if(redirect) {
			redirect action: 'index'
            }
			return registrationCode
		}

		String url = generateLink('register', 'verifyRegistration', [t: registrationCode.token], request)
		sendVerificationMail(username,email,url,request)
        return registrationCode;
	}
	
	def resend() {
		def username = params.email
        if(username) {
    		def registrationCode = RegistrationCode.findByUsername(username)
            if(registrationCode) {
                String url = generateLink('register', 'verifyRegistration', [t: registrationCode.token], request)
                SUser user = SUser.findByEmail(username);
                sendVerificationMail(user.name, username, url, request)

                render ([success:true, 'msg':"Successfully sent verification email to ${username}. Please check your inbox."] as JSON)
                return;
            } else {
                log.error "registration code for ${username} is not present"
                render ([success:false, 'msg':"Registration code for the email address ${username} is not found"] as JSON)
                return;
            }
        } else {
            log.error "username is null"
            render ([success:false, 'msg':'Please provide a valid email address'] as JSON)
        }
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
				from grailsApplication.config.grails.mail.default.from
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

		def savedRequest = session['SPRING_SECURITY_SAVED_REQUEST_KEY']
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

class CustomRegisterCommand2 {
	String username
	String email
	String password
	String password2
	String name
	String website
	float timezone=0
	String aboutMe;
	String location;
	
	def grailsApplication
	
	static constraints = {
		name blank: false, nullable: false
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



class ResetPasswordCommand2 {
    String username
    String password
    String password2

    static constraints = {
        username nullable: false, email: true
        password blank: false, nullable: false, validator: grails.plugin.springsecurity.ui.RegisterController.passwordValidator
        password2 validator: RegisterController.password2Validator
    }
}
