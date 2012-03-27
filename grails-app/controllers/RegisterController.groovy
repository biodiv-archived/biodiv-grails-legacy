import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;




class RegisterController extends grails.plugins.springsecurity.ui.RegisterController {
	
	def index = {
		def copy = [:] + (flash.chainedParams ?: [:])
		if(flash.chainedParams?.timezone) {
			copy.timezone = params.float(copy.timezone)
		}
		copy.remove 'controller'
		copy.remove 'action'
		[command: new RegisterCommand(copy)]
	}
	
	
	def register = { RegisterCommand command ->

		if (command.hasErrors()) {
			render view: 'index', model: [command: command]
			return
		}

		String salt = saltSource instanceof NullSaltSource ? null : command.username
		def user = lookupUserClass().newInstance(email: command.email, username: command.username,
				accountLocked: true, enabled: true)

		RegistrationCode registrationCode = springSecurityUiService.register(user, command.password, salt)
		if (registrationCode == null || registrationCode.hasErrors()) {
			// null means problem creating the user
			flash.error = message(code: 'spring.security.ui.register.miscError')
			flash.chainedParams = params
			redirect action: 'index'
			return
		}

		String url = generateLink('verifyRegistration', [t: registrationCode.token])

		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.register.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [user: user, url: url])
		}
		mailService.sendMail {
			to command.email
			from conf.ui.register.emailFrom
			subject conf.ui.register.emailSubject
			html body.toString()
		}

		render view: 'index', model: [emailSent: true]
	}
	
	protected String generateLink(String action, linkParams) {
		createLink(base: "$request.scheme://$grailsApplication.config.speciesPortal.domain$request.contextPath",
				controller: 'register', action: action,
				params: linkParams)
	}

}

class RegisterCommand {
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
		email blank: false, nullable: false, validator: { value, command ->
			if (value) {
				def User = command.grailsApplication.getDomainClass(
					SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
				if (User.findByEmail(value)) {
					return 'registerCommand.email.unique'
				}
			}
		}
		password blank: false, nullable: false, validator: RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}