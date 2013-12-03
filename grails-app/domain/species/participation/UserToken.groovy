package species.participation

import org.codehaus.groovy.grails.plugin.springsecurity.ui.RegistrationCode;

class UserToken extends RegistrationCode {

	String controller
	String action
	Map params
	
    static constraints = {
    }
}
