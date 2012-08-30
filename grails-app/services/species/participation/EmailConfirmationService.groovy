package species.participation

import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;

class EmailConfirmationService extends com.grailsrocks.emailconfirmation.EmailConfirmationService {

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
