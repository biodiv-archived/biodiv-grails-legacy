


class RegisterController extends grails.plugins.springsecurity.ui.RegisterController {
	
	def index = {
		def copy = [:] + (flash.chainedParams ?: [:])
		copy.remove 'controller'
		copy.remove 'action'
		[command: new RegisterCommand(copy)]
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
		username blank: false, nullable: false, validator: { value, command ->
			if (value) {
				def User = command.grailsApplication.getDomainClass(
					SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
				if (User.findByUsername(value)) {
					return 'registerCommand.username.unique'
				}
			}
		}
		email blank: false, nullable: false, email: true
		password blank: false, nullable: false, validator: RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}