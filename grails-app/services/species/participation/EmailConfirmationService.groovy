package species.participation

import java.util.Map;

import grails.util.Environment
import grails.util.Holders;
import grails.util.Holders;

import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;

class EmailConfirmationService extends com.grailsrocks.emailconfirmation.EmailConfirmationService {

	def makeURL(token, userGroupInstance) {
		//@todo this needs to change to do a reverse mapping lookup
		//@todo also if uri already exists in binding, append token to it
		def grailsApplication = Holders.getGrailsApplication()
		def uGroup = grailsApplication.mainContext.getBean('species.UserGroupTagLib');
		return uGroup.createLink(controller:'emailConfirmation', action:'index', id:token.encodeAsURL(), userGroup:userGroupInstance, absolute:true);		
	}
	
//	def sendConfirmation(String emailAddress, String thesubject, Map binding = null, String userToken = null) {
	def sendConfirmation(Map args) {

		if (log.infoEnabled) {
			log.info "Sending email confirmation mail to ${args.to}, callback events will be prefixed with " +
                     "[${args.event ? args.event+'.' : ''}] in namespace [${args.eventNamespace ?: EVENT_NAMESPACE}], user data is [${args.id}])"
		}
		def conf = new PendingEmailConfirmation(
		    emailAddress:args.to, 
		    userToken:args.id, 
		    confirmationEvent:makeConfirmationEventString(args))
        makeToken(conf)
        
        if (log.debugEnabled) {
            log.debug "Created email confirmation token [${conf.confirmationToken}] for mail to ${conf.emailAddress}"
        }

        if (!conf.save()) {
			throw new IllegalArgumentException( "Unable to save pending confirmation: ${conf.errors}")
		}
		
		def binding = args.model ? new HashMap(args.model) : [:]
	
		binding['uri'] = makeURL(conf.confirmationToken)

		if (log.infoEnabled) {
			log.info( "Sending email confirmation mail to $args.to - confirmation link is: ${binding.uri}")
		}
		
		def defaultView = args.view == null
		def viewName = defaultView ? "/emailConfirmation/mail/confirmationRequest" : args.view
        def pluginName = defaultView ? "email-confirmation" : args.plugin

		try {
    		mailService.sendMail {
    			to args.to 
    			from args.from ?: pluginConfig.from
    			subject args.subject
    			def bodyArgs = [view:viewName, model:binding]
    			if (pluginName) {
    			    bodyArgs.plugin = pluginName
    			}
    			body( bodyArgs)
    	    }
		} catch (Throwable t) {
		    if (Environment.current == Environment.DEVELOPMENT) {
		        log.warn "Mail sending failed but you're in development mode so I'm ignoring this fact, you can confirm using this link: ${binding.uri}"
                log.error "Mail send failed", t
		    } else {
	            throw t
            }
		}
		return conf


/*
		def conf = new PendingEmailConfirmation(emailAddress:emailAddress, userToken:userToken)
		makeToken(conf);
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
				from binding.from ?: grails.util.Holders.config.emailConfirmation.from
				subject thesubject
				def bodyArgs = [view:viewName, model:binding]
				if (pluginName) {
					bodyArgs.plugin = pluginName
				}
				body( bodyArgs)
			}
		} catch (Throwable t) {
			if(Environment.current == Environment.DEVELOPMENT) {
				log.warn "Mail sending failed but you're in development mode so I'm ignoring this fact, you can confirm using the link shown in the previous log output"
				log.error "Mail send failed", t
			} else {
				throw t
			}
		}
		return conf */
	}


	def checkConfirmation(String confirmationToken) {
		if (log.traceEnabled) {
            log.trace("checkConfirmation looking for confirmation token: $confirmationToken")
        }

		def conf
        if (confirmationToken) {
            conf = PendingEmailConfirmation.findByConfirmationToken(confirmationToken)
            if (conf) {
                conf = PendingEmailConfirmation.lock(conf.ident())
            }
        }

		// 100% double check that the token in the found object matches exactly. Some lame databases
		// are case insensitive for searches, which reduces the possible token space
		if (conf && (conf.confirmationToken == confirmationToken)) {
			if (log.debugEnabled) {
				log.debug( "Notifying application of valid email confirmation for user token ${conf.userToken}, email ${conf.emailAddress}")
			}
			// Tell application it's ok
            println "++++++++++++++++++++++"
			def result = fireEvent(EVENT_TYPE_CONFIRMED, conf.confirmationEvent, [email:conf.emailAddress, id:conf.userToken], {
                println onConfirmation
                println "&&&&&&&&&&&&&&&&&&&&&&&&&"
                println "======CONF DETAILS=============== " + conf.emailAddress + conf.userToken + "=======" + confirmationToken
                onConfirmation?.clone().call(conf.emailAddress, conf.userToken, confirmationToken); 
			})
			
			conf.delete()
			return [valid: true, actionToTake:result, email: conf.emailAddress, token:conf.userToken]
		} else {
			if (log.traceEnabled) {
			    log.trace("checkConfirmation did not find confirmation token: $confirmationToken")
		    }
			def result = fireEvent(EVENT_TYPE_INVALID, null, [token:confirmationToken], {
                onInvalid?.clone().call(confirmationToken)					    
			})
			
			return [valid:false, actionToTake:result]
		}
	}
}
