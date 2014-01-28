package species.participation

import java.util.Map;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;

class EmailConfirmationService extends com.grailsrocks.emailconfirmation.EmailConfirmationService {

	def makeURL(token, userGroupInstance) {
		//@todo this needs to change to do a reverse mapping lookup
		//@todo also if uri already exists in binding, append token to it
		def grailsApplication = ApplicationHolder.application
		def uGroup = grailsApplication.mainContext.getBean('species.UserGroupTagLib');
		return uGroup.createLink(controller:'emailConfirmation', action:'index', id:token.encodeAsURL(), userGroup:userGroupInstance, absolute:true);		
	}
	
	def sendConfirmation(String emailAddress, String thesubject,
		Map binding = null, String userToken = null) {
		def conf = new PendingEmailConfirmation(emailAddress:emailAddress, userToken:userToken)
		conf.makeToken()
		if (!conf.save()) {
			throw new IllegalArgumentException( "Unable to save pending confirmation: ${conf.errors}")
		}
		
		binding = binding ? new HashMap(binding) : [:]
	
		binding['uri'] = makeURL(conf.confirmationToken, binding.userGroupInstance)

		if (log.infoEnabled) {
			log.info( "Sending email confirmation mail to $emailAddress - confirmation link is: ${binding.uri}")
		}
		
		def defaultView = binding.view == null
		def viewName = defaultView ? "/emailconfirmation/mail/confirmationRequest" : binding.view
		def pluginName = defaultView ? "email-confirmation" : binding.plugin

		try {
			mailService.sendMail {
				to emailAddress
				from binding.from ?: ConfigurationHolder.config.emailConfirmation.from
				subject thesubject
				def bodyArgs = [view:viewName, model:binding]
				if (pluginName) {
					bodyArgs.plugin = pluginName
				}
				body( bodyArgs)
			}
		} catch (Throwable t) {
			if (Environment.current == Environment.DEVELOPMENT) {
				log.warn "Mail sending failed but you're in development mode so I'm ignoring this fact, you can confirm using the link shown in the previous log output"
				log.error "Mail send failed", t
			} else {
				throw t
			}
		}
		return conf
	}

	
	def checkConfirmation(String confirmationToken) {
		if (log.traceEnabled) log.trace("checkConfirmation looking for confirmation token: $confirmationToken")
		def conf = PendingEmailConfirmation.findByConfirmationToken(confirmationToken)
		// 100% double check that the token in the found object matches exactly. Some lame databases
		// are case insensitive for searches, which reduces the possible token space
		if (conf && (conf.confirmationToken == confirmationToken)) {
			if (log.debugEnabled) {
				log.debug( "Notifying application of valid email confirmation for user token ${conf.userToken}, email ${conf.emailAddress}")
			}
			// Tell application it's ok
			// @todo auto sense number of args
			def result = onConfirmation?.clone().call(conf.emailAddress, conf.userToken, confirmationToken)
			//conf.delete()
			return [valid: true, action:result, email: conf.emailAddress, token:conf.userToken]
		} else {
			if (log.traceEnabled) log.trace("checkConfirmation did not find confirmation token: $confirmationToken")
			def result = onInvalid?.clone().call(confirmationToken)
			return [valid:false, action:result]
		}
	}
}
