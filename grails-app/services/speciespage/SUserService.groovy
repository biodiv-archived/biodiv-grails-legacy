package speciespage

import grails.plugins.springsecurity.ui.SpringSecurityUiService;
import grails.util.Environment;
import groovy.text.SimpleTemplateEngine;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import species.auth.SUser;
import species.utils.Utils;

class SUserService extends SpringSecurityUiService {

	def grailsApplication

	def springSecurityService
	def mailService
	
	public static final String NEW_USER = "newUser";
	public static final String USER_DELETED = "deleteUser";
	
	/**
	 * 
	 */
	SUser create(Map propsMap) {
		log.debug("Creating new User");
		propsMap = propsMap ?: [:];
		
		propsMap.remove('metaClass')
		propsMap.remove('class')

		String userDomainClassName = SpringSecurityUtils.securityConfig.userLookup.userDomainClassName

		Class<?> UserDomainClass = grailsApplication.getDomainClass(userDomainClassName).clazz
		if (!UserDomainClass) {
			log.error("Can't find user domain: $userDomainClassName")
			return null
		}

		def user = UserDomainClass.newInstance(propsMap);
		user.enabled = true;
		return user;
	}

	/**
	 * 
	 * @param user
	 * @param cleartextPassword
	 * @param salt
	 * @return
	 */
	SUser save(user) {
		log.debug "Saving $user"
		if (!user.save(flush:true, failOnError:true)) {
			warnErrors user, messageSource
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
			return null
		}
		return user
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	RegistrationCode register(String username) {
		if(!username) return null;

		def registrationCode = new RegistrationCode(username: username)
		if (!registrationCode.save()) {
			warnErrors registrationCode, messageSource
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
		}

		registrationCode
	}

	/**
	 * 
	 * @param user
	 */
	void assignRoles(SUser user) {
		
		def securityConf = SpringSecurityUtils.securityConfig

		def defaultRoleNames = securityConf.ui.register.defaultRoleNames;

		Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
		Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz
		
		PersonRole.withTransaction { status ->
			defaultRoleNames.each { String roleName ->
				String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
				def auth = Authority."findBy${findByField}"(roleName)
				if (auth) {
					log.debug "Assigning role $auth to user $user"
					PersonRole.create(user, auth)
				} else {
					log.error("Can't find authority for name '$roleName'")
				}
			}
		}
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	boolean ifOwns(SUser user) {
		return springSecurityService.isLoggedIn() && (springSecurityService.currentUser?.id == user.id || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN'))	
	}
	
	boolean ifOwns(id) {
		return springSecurityService.isLoggedIn() && (springSecurityService.currentUser?.id == id || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN'))
	}
	
	boolean isAdmin(id) {
		if(!id) return false
		return SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')
	}

	public void sendNotificationMail(String notificationType, SUser user, request, String userProfileUrl){
		def conf = SpringSecurityUtils.securityConfig
		
		//def userProfileUrl = generateLink("SUser", "show", ["id": user.id], request)

		def templateMap = [username: user.name.capitalize(), email:user.email, userProfileUrl:userProfileUrl, domain:Utils.getDomainName(request)]

		def mailSubject = ""
		def body = ""

		switch ( notificationType ) {
			case NEW_USER:
				mailSubject = conf.ui.newuser.emailSubject
				body = conf.ui.newuser.emailBody
				break
			case USER_DELETED:
				mailSubject = conf.ui.userdeleted.emailSubject
				body = conf.ui.userdeleted.emailBody
				break
			default:
				log.debug "invalid notification type"
		}

		if (body.contains('$')) {
			body = evaluate(body, templateMap)
		}
		
		if (mailSubject.contains('$')) {
			mailSubject = evaluate(mailSubject, [domain: Utils.getDomainName(request)])
		}

		if ( Environment.getCurrent().getName().equalsIgnoreCase("pamba")) {
			mailService.sendMail {
				to user.email
				bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com","thomas.vee@gmail.com"
				from conf.ui.notification.emailFrom
				subject mailSubject
				html body.toString()
			}
		}
			log.debug "Sent mail for notificationType ${notificationType} to ${user.email}"
	}
	
	protected String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}
	
	def nameTerms(params) {
		return getUserSuggestions(params);
	}	
	
	def getUserSuggestions(params){
		def jsonData = []
		String username = params.term
		
		String usernameFieldName = 'name';//SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		String userId = 'id';


		def results = grailsApplication.getDomainClass(SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz.executeQuery(
				"SELECT DISTINCT u.$usernameFieldName, u.$userId " +
				"FROM ${SpringSecurityUtils.securityConfig.userLookup.userDomainClassName} u " +
				"WHERE LOWER(u.$usernameFieldName) LIKE :name " +
				"ORDER BY u.$usernameFieldName",
				[name: "${username.toLowerCase()}%"],
				[max: params.max])

		for (result in results) {
			jsonData << [value: result[0], label:result[0] , userId:result[1] , "category":"Members"]
		}
		
		return jsonData;
	}

}
