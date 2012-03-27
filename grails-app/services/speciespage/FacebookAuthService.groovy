package speciespage


import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.ImageType;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.facebook.connect.FacebookServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.Spring30OAuth2RequestFactory;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import species.auth.SUser;

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
			if(fbProfile.email) {
				log.info "Found existing user with same emailId $fbProfile.email"
				appUser = UserDomainClass.findByEmail(fbProfile.email)
				log.info "Merging fb details with existing account $appUser"
			}

			if(!appUser) {
				if (!UserDomainClass) {
					log.error("Can't find user domain: $userDomainClassName")
					return null
				}
				appUser = UserDomainClass.newInstance()
			}


			if(!appUser[securityConf.userLookup.usernamePropertyName] && fbProfile.username) {
				appUser[securityConf.userLookup.usernamePropertyName] = fbProfile.username
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
}
