package species.participation

import grails.plugin.springsecurity.ui.RegistrationCode;

class UserToken extends RegistrationCode {

	String controller
	String action
	Map params
	
    static constraints = {
    }
}
