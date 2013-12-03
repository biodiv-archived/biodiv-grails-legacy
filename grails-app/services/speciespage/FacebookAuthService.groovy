package speciespage


import org.codehaus.groovy.grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.social.facebook.api.FacebookProfile
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.social.oauth2.Spring30OAuth2RequestFactory
import org.springframework.social.support.ClientHttpRequestFactorySelector

import species.auth.SUser
import species.auth.CustomRegisterCommand;

import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken

class FacebookAuthService {

	def grailsApplication;

	String userDomainClassName

	def createAppUser(user, token) {

		def appUser;

		if(token.accessToken) {
			FacebookTemplate facebook = new FacebookTemplate(token.accessToken);
			facebook.setRequestFactory(new Spring30OAuth2RequestFactory(ClientHttpRequestFactorySelector.getRequestFactory(), token.accessToken, facebook.getOAuth2Version()));
			FacebookProfile fbProfile = facebook.userOperations().getUserProfile();

			def securityConf = SpringSecurityUtils.securityConfig
			Class<?> UserDomainClass = grailsApplication.getDomainClass(userDomainClassName).clazz

			if (!UserDomainClass) {
				log.error("Can't find user domain: $userDomainClassName")
				return null
			}

			if(fbProfile.email) {
				log.info "Finding existing user based on emailId $fbProfile.email"
				appUser = UserDomainClass.findByEmail(fbProfile.email)
			}

			if(!appUser) {
				throw new UsernameNotFoundException("No user. Please register", fbProfile);
				//log.info "Creating a new user"
				//appUser = UserDomainClass.newInstance()
			} else {
				log.info "Merging details with existing account $appUser"
			}


			if(!appUser['username'] && fbProfile.username) {
				appUser['username'] = fbProfile.username
			}

			if(!appUser[securityConf.userLookup.passwordPropertyName])
				appUser[securityConf.userLookup.passwordPropertyName] = token.accessToken

			appUser[securityConf.userLookup.enabledPropertyName] = true
			appUser[securityConf.userLookup.accountExpiredPropertyName] = false
			appUser[securityConf.userLookup.accountLockedPropertyName] = false
			appUser[securityConf.userLookup.passwordExpiredPropertyName] = false

			if(!appUser['name'] && fbProfile.name) {
				appUser['name'] = fbProfile.name
			}

			if(!appUser['email'] && fbProfile.email) {
				appUser['email'] = fbProfile.email
			}

			if(!appUser['website'] && fbProfile.website) {
				appUser['website'] = fbProfile.website
			}

			if(!appUser['profilePic'] && token.uid) {
				appUser['profilePic'] = "http://graph.facebook.com/$token.uid/picture?type=large";
			}
			if(!appUser['timezone'] && fbProfile.timezone) {
				appUser['timezone'] = fbProfile.timezone;
			}

			UserDomainClass.withTransaction {
				appUser = appUser.merge();
				appUser.save(flush: true, failOnError: true)
			}
		}

		return appUser;
	}

	/**
	 * 
	 * @param token
	 * @param appUser
	 * @param openId
	 */
	void registerFacebookUser(FacebookAuthToken token, SUser appUser) {
		log.debug "Saving facebook user domain class"
		def conf =  grailsApplication.config.grails.plugin.springsecurity;
		String domainClassName = conf.facebook.domain.classname;
		String connectionPropertyName = conf.facebook.domain.connectionPropertyName
		Class<?> UserClass = grailsApplication.getDomainClass(domainClassName)?.clazz

		if (!UserClass) {
			log.error("Can't find domain: $domainClassName")
			return null
		}

		def user = grailsApplication.getDomainClass(domainClassName).newInstance()

		user.uid = token.uid

		if (user.properties.containsKey('accessToken')) {
			user.accessToken = token.accessToken
		}

		user[connectionPropertyName] = appUser

		user.save(flush: true, failOnError: true)

		log.debug "Saving facebook user domain class done $user"
	}

	/**
	 *
	 * @param appUser
	 * @param fbProfile
	 */
	void mergeFacebookUserDetails(SUser appUser, FacebookProfile fbProfile) {
		copyFromFacebookProfile(appUser, fbProfile);
	}

	/**
	 *
	 */
	void copyFromFacebookProfile(appUser, FacebookProfile fbProfile) {
		def securityConf = SpringSecurityUtils.securityConfig
		if(!appUser['username'] && fbProfile.username) {
			appUser['username'] = fbProfile.username
		}

		if(!appUser['name'] && fbProfile.name) {
			appUser['name'] = fbProfile.name
		}

		if(!appUser['email'] && fbProfile.email) {
			appUser['email'] = fbProfile.email
		}

		if(!appUser['website'] && fbProfile.website) {
			appUser['website'] = fbProfile.website
		}

		if(!appUser['profilePic'] && fbProfile.id) {
			appUser['profilePic'] = "http://graph.facebook.com/$fbProfile.id/picture?type=large";
		}

		if(!appUser['timezone'] && fbProfile.timezone) {
			appUser['timezone'] = fbProfile.timezone;
		}
	
        if(!appUser instanceof CustomRegisterCommand) {
            appUser[securityConf.userLookup.enabledPropertyName] = true
            appUser[securityConf.userLookup.accountExpiredPropertyName] = false
            appUser[securityConf.userLookup.accountLockedPropertyName] = false
            appUser[securityConf.userLookup.passwordExpiredPropertyName] = false
        }
	}
	
}
