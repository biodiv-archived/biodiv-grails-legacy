import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationSuccessHandler;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdUserDetailsService;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

import com.the6hours.grails.springsecurity.facebook.DefaultFacebookAuthDao;

import species.auth.ConsumerManager;
import species.auth.FacebookAuthCookieFilter;
import species.auth.FacebookAuthCookieLogoutHandler;
import species.auth.FacebookAuthProvider;
import species.auth.FacebookAuthUtils;
import species.auth.OpenIDAuthenticationFilter;
import species.auth.OpenIdAuthenticationFailureHandler;

import species.auth.drupal.DrupalAuthCookieFilter;
import species.auth.drupal.DrupalAuthUtils;
import speciespage.FacebookAuthService;

// Place your Spring DSL code here
beans = {
	//userDetailsService(species.auth.drupal.DrupalUserDetailsService);
//	drupalAuthentiactionProvider(species.auth.drupal.DrupalAuthenticationProvider) {
//		//userDetailsService = ref("userDetailsService");
//		drupalAuthDao = ref('drupalAuthDao')
//	}

	def conf = SpringSecurityUtils.securityConfig;
	
//	authenticationSuccessHandler(species.auth.AjaxAwareAuthenticationSuccessHandler) {
//		requestCache = ref('requestCache')
//		defaultTargetUrl = conf.successHandler.defaultTargetUrl // '/'
//		alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault // false
//		targetUrlParameter = conf.successHandler.targetUrlParameter // 'spring-security-redirect'
//		ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl // '/login/ajaxSuccess'
//		useReferer = true // false
//		redirectStrategy = ref('redirectStrategy')
//	}
//
//	drupalAuthUtils(DrupalAuthUtils);
//	drupalAuthCookieFilter(DrupalAuthCookieFilter) {
//		authenticationManager = ref('authenticationManager')
//		drupalAuthUtils = ref('drupalAuthUtils')
//		sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
//		authenticationSuccessHandler = ref('authenticationSuccessHandler')
//		authenticationFailureHandler = ref('authenticationFailureHandler')
////		rememberMeServices = ref('rememberMeServices')
////		authenticationDetailsSource = ref('authenticationDetailsSource')
////		filterProcessesUrl = conf.apf.filterProcessesUrl // '/j_spring_security_check'
////		usernameParameter = conf.apf.usernameParameter // j_username
////		passwordParameter = conf.apf.passwordParameter // j_password
//		continueChainBeforeSuccessfulAuthentication = SpringSecurityUtils.securityConfig.apf.continueChainBeforeSuccessfulAuthentication // false
//		allowSessionCreation = SpringSecurityUtils.securityConfig.apf.allowSessionCreation // true
////		postOnly = conf.apf.postOnly // true
//		logoutUrl =  SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
//	}
//	
//	drupalAuthDao(species.auth.drupal.DrupalAuthDao)
	
	userDetailsService(OpenIdUserDetailsService) {
		grailsApplication = ref('grailsApplication')
	}

	def configRoot = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search
	solrServer(org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer,config.serverURL, config.queueSize, config.threadCount ) { 
		setSoTimeout(config.soTimeout);
		setConnectionTimeout(config.connectionTimeout);
		setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
		setMaxTotalConnections(config.maxTotalConnections);
		setFollowRedirects(config.followRedirects);
		setAllowCompression(config.allowCompression);
		setMaxRetries(config.maxRetries);
		//setParser(new XMLResponseParser()); // binary parser is used by default
		log.debug "Initialized search server to "+config.serverURL
	}

	searchService(speciespage.SearchService) { 
		solrServer = ref('solrServer');		
	}
	
	
	facebookAuthUtils(FacebookAuthUtils) {
		grailsApplication = ref('grailsApplication')
	}

	facebookAuthCookieLogout(FacebookAuthCookieLogoutHandler) {
		facebookAuthUtils = ref('facebookAuthUtils')
	}
	SpringSecurityUtils.registerLogoutHandler('facebookAuthCookieLogout')
	
	facebookAuthCookieFilter(FacebookAuthCookieFilter) {
		grailsApplication = ref('grailsApplication')
		authenticationManager = ref('authenticationManager')
		facebookAuthUtils = ref('facebookAuthUtils')
		logoutUrl = 'j_spring_security_logout'
		createAccountUrl = '/login/facebookCreateAccount'
		registerUrl = '/register'
	}

	facebookAuthService(FacebookAuthService) {
		grailsApplication = ref('grailsApplication')
		userDomainClassName = conf.userLookup.userDomainClassName
	}
	
	

	facebookAuthProvider(FacebookAuthProvider) {
		facebookAuthDao = ref('facebookAuthDao')
		facebookAuthUtils = ref('facebookAuthUtils')
	}

	openIDConsumerManager(ConsumerManager) {
		nonceVerifier = ref('openIDNonceVerifier')
	}
	
//	authenticationFailureHandler(OpenIdAuthenticationFailureHandler) {
//		grailsApplication = ref('grailsApplication')
//		userDomainClassName = conf.userLookup.userDomainClassName
//		redirectStrategy = ref('redirectStrategy')
//		defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
//		useForward = conf.failureHandler.useForward // false
//		ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
//		exceptionMappings = conf.failureHandler.exceptionMappings // [:]
//	}
	
	openIDAuthenticationFilter(OpenIDAuthenticationFilter) {
		//claimedIdentityFieldName = conf.openid.claimedIdentityFieldName // openid_identifier
		consumer = ref('openIDConsumer')
		rememberMeServices = ref('rememberMeServices')
		authenticationManager = ref('authenticationManager')
		authenticationSuccessHandler = ref('authenticationSuccessHandler')
		authenticationFailureHandler = ref('authenticationFailureHandler')
		authenticationDetailsSource = ref('authenticationDetailsSource')
		sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
		filterProcessesUrl = '/j_spring_openid_security_check' // not configurable
	}
}