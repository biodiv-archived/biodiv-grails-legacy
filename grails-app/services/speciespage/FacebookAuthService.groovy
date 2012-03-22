package speciespage


import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
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

class FacebookAuthService {

	def grailsApplication;

	def prepopulateAppUser(appUser, token) {
		if(token.accessToken) {
			FacebookTemplate facebook = new FacebookTemplate(token.accessToken);
			facebook.setRequestFactory(new Spring30OAuth2RequestFactory(ClientHttpRequestFactorySelector.getRequestFactory(), token.accessToken, facebook.getOAuth2Version()));
			FacebookProfile fbProfile = facebook.userOperations().getUserProfile();

			def securityConf = SpringSecurityUtils.securityConfig

			appUser[securityConf.userLookup.usernamePropertyName] = fbProfile.username
			appUser[securityConf.userLookup.passwordPropertyName] = token.accessToken
			appUser[securityConf.userLookup.enabledPropertyName] = true
			appUser[securityConf.userLookup.accountExpiredPropertyName] = false
			appUser[securityConf.userLookup.accountLockedPropertyName] = false
			appUser[securityConf.userLookup.passwordExpiredPropertyName] = false
			appUser['name'] = fbProfile.name
			appUser['email'] = fbProfile.email
			appUser['website'] = fbProfile.website
			appUser['profilePic'] = "http://graph.facebook.com/$token.uid/picture?type=large";
			appUser['timezone'] = fbProfile.timezone;
		}
	}
}
