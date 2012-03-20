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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class FacebookAuthService {

	def grailsApplication;
	
//	def prepopulateAppUser(appUser, token) {
	//facebookConnectionFactory.getServiceProvider() ? facebookConnectionFactory.getServiceProvider().getApi() :
//	FacebookTemplate facebook = new FacebookTemplate(accessToken);
//	facebook.setRequestFactory(new Spring30OAuth2RequestFactory(ClientHttpRequestFactorySelector.getRequestFactory(), accessToken, facebook.getOAuth2Version()));
//	//Facebook facebook = new FacebookTemplate(accessToken)
//	FacebookProfile fbProfile = facebook.userOperations().getUserProfile();
//	
//   println ')))))))))))))))))))))))))))))))))))'
//   println accessToken;
//   
//   println fbProfile.getEmail();
//		   println fbProfile.getBio();
//
//		   println fbProfile.getName();
//		   println fbProfile.getWebsite();
//		   println fbProfile.getTimezone();
//		   println fbProfile.getAbout();
//		   println ')))))))))))))))))))))))))))))))))))'
//		   println facebook.userOperations().getUserProfileImage();
//		   println facebook.userOperations().getUserProfileImage(ImageType.SMALL);
//		def securityConf = SpringSecurityUtils.securityConfig
//		println token.accessToken;
////		FacebookConnectionFactory connectionFactory = new FacebookConnectionFactory(grailsApplication.config.grails.plugins.springsecurity.facebook.appId, grailsApplication.config.grails.plugins.springsecurity.facebook.secret);
////		System.out.println("connectionFactory authorizationCode :"+connectionFactory);
////		String redirectUrl = grailsApplication.config.grails.serverURL
////		MultiValueMap paramsMap1 = new LinkedMultiValueMap();
////		paramsMap1.set("redirect_uri", redirectUrl);
////		paramsMap1.set("grant_type", "authorization_code");
////		paramsMap1.set("scope","read_stream,user_about_me,user_birthday,user_likes,user_status,email,publish_stream,offline_access");
////		
////		AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(token.code, redirectUrl, paramsMap1);
////		String accssTokentemp = accessGrant.getAccessToken();
////		System.out.println("accssTokentemp :"+accssTokentemp);
////		String scope_p = accessGrant.getScope();
////		System.out.println("scope_p :"+scope_p);
////		
//		//FacebookServiceProvider facebookServiceProvider = new FacebookServiceProvider(grailsApplication.config.grails.plugins.springsecurity.facebook.appId, grailsApplication.config.grails.plugins.springsecurity.facebook.secret);
//		//Facebook facebook = facebookServiceProvider.getApi();//new FacebookTemplate(accssTokentemp);
//		Facebook facebook = new FacebookTemplate(token.accessToken)
//		System.out.println("facebook :"+facebook);
//		FacebookProfile fbProfile = facebook.userOperations().getUserProfile();
//		
//	   println ')))))))))))))))))))))))))))))))))))'
//	   println fbProfile;
//	   println fbProfile.getBio();
//	   println fbProfile.getEmail();
//	   println fbProfile.getName();
//	   println fbProfile.getWebsite();
//	   println fbProfile.getTimezone();
//	   println fbProfile.getAbout();
//	   println ')))))))))))))))))))))))))))))))))))'
//	   println facebook.userOperations().getUserProfileImage();
//	   println facebook.userOperations().getUserProfileImage(ImageType.SMALL);
//		
//		appUser[securityConf.userLookup.usernamePropertyName] = "facebook_$token.uid"
//		appUser[securityConf.userLookup.passwordPropertyName] = token.accessToken
//		appUser[securityConf.userLookup.enabledPropertyName] = true
//		appUser[securityConf.userLookup.accountExpiredPropertyName] = false
//		appUser[securityConf.userLookup.accountLockedPropertyName] = false
//		appUser[securityConf.userLookup.passwordExpiredPropertyName] = false
//	}
}
