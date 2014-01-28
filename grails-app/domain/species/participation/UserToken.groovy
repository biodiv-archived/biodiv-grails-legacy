package species.participation

import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;

class UserToken extends RegistrationCode {

	String controller
	String action
	Map params
	
    static constraints = {
    }
}
