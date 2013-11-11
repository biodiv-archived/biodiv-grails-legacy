//Notify all the users who ahve registered but not verified their email address
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;
def mailService = ctx.getBean("mailService");

def users = RegistrationCode.getAll()

users.each()  { 
	String domain = (grailsApplication.config.ibp.domain).trim()
	
	if (domain.getAt(domain.size()-1).equals("/")) {
		domain = domain.reverse().minus("/").reverse()
	}

	String url = "${domain}/register/verifyRegistration?t=${it.token}"
	def toUser = it.username
	try {	
		mailService.sendMail {
			to toUser 
			from grailsApplication.config.grails.mail.default.from 
			subject "Please activite your account on ${domain}"
			html "Hello here, <br /> we have noticed that you haven\'t yet verified your email address on ${domain} <br /> Please <a href=\"${url}\">click here</a> to activate to activate it now"
		}
	}catch(all)  {
		all.printStackTrace();	
	}
}

