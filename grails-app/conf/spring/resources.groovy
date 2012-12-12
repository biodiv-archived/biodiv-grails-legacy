import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationFailureHandler;
import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationSuccessHandler;
import org.codehaus.groovy.grails.plugins.springsecurity.DefaultPostAuthenticationChecks;
import org.codehaus.groovy.grails.plugins.springsecurity.DefaultPreAuthenticationChecks;
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
import species.auth.OpenIDAuthenticationProvider;
import species.auth.OpenIdAuthenticationFailureHandler;

import species.auth.drupal.DrupalAuthCookieFilter;
import species.auth.drupal.DrupalAuthUtils;
import species.participation.EmailConfirmationService;
import speciespage.FacebookAuthService;

// Place your Spring DSL code here
beans = {
	def conf = SpringSecurityUtils.securityConfig;
	
	authenticationSuccessHandler(species.auth.AjaxAwareAuthenticationSuccessHandler) {
		requestCache = ref('requestCache')
		defaultTargetUrl = conf.successHandler.defaultTargetUrl // '/'
		alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault // false
		targetUrlParameter = conf.successHandler.targetUrlParameter // 'spring-security-redirect'
		ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl // '/login/ajaxSuccess'
		useReferer = true // false
		redirectStrategy = ref('redirectStrategy')
	}

	userDetailsService(OpenIdUserDetailsService) {
		grailsApplication = ref('grailsApplication')
	}

	def configRoot = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search
	speciesSolrServer(org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer,config.serverURL+"/species", config.queueSize, config.threadCount ) { 
		setSoTimeout(config.soTimeout);
		setConnectionTimeout(config.connectionTimeout);
		setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
		setMaxTotalConnections(config.maxTotalConnections);
		setFollowRedirects(config.followRedirects);
		setAllowCompression(config.allowCompression);
		setMaxRetries(config.maxRetries);
		//setParser(new XMLResponseParser()); // binary parser is used by default
		log.debug "Initialized search server to "+config.serverURL+"/species"
	}

	speciesSearchService(speciespage.search.SpeciesSearchService) { 
		solrServer = ref('speciesSolrServer');		
	}

	observationsSolrServer(org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer,config.serverURL+"/observations", config.queueSize, config.threadCount ) {
		setSoTimeout(config.soTimeout);
		setConnectionTimeout(config.connectionTimeout);
		setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
		setMaxTotalConnections(config.maxTotalConnections);
		setFollowRedirects(config.followRedirects);
		setAllowCompression(config.allowCompression);
		setMaxRetries(config.maxRetries);
		//setParser(new XMLResponseParser()); // binary parser is used by default
		log.debug "Initialized search server to "+config.serverURL+"/observations"
	}

	observationsSearchService(speciespage.search.ObservationsSearchService) {
		solrServer = ref('observationsSolrServer');
	}

	newsletterSolrServer(org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer,config.serverURL+"/newsletters", config.queueSize, config.threadCount ) {
		setSoTimeout(config.soTimeout);
		setConnectionTimeout(config.connectionTimeout);
		setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
		setMaxTotalConnections(config.maxTotalConnections);
		setFollowRedirects(config.followRedirects);
		setAllowCompression(config.allowCompression);
		setMaxRetries(config.maxRetries);
		//setParser(new XMLResponseParser()); // binary parser is used by default
		log.debug "Initialized search server to "+config.serverURL+"/newsletters"
	}

	newsletterSearchService(speciespage.search.NewsletterSearchService) {
		solrServer = ref('newsletterSolrServer');
	}
	
	preAuthenticationChecks(DefaultPreAuthenticationChecks)
	postAuthenticationChecks(DefaultPostAuthenticationChecks)
	
	facebookAuthUtils(FacebookAuthUtils) {
		grailsApplication = ref('grailsApplication')
	}

	facebookAuthCookieLogout(FacebookAuthCookieLogoutHandler) {
		facebookAuthUtils = ref('facebookAuthUtils')
	}
	SpringSecurityUtils.registerLogoutHandler('facebookAuthCookieLogout')
	
	fbAuthenticationFailureHandler(AjaxAwareAuthenticationFailureHandler) {
		redirectStrategy = ref('redirectStrategy')
		defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
		useForward = conf.failureHandler.useForward // false
		ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
		exceptionMappings = conf.failureHandler.exceptionMappings // [:]
	}

	facebookAuthCookieFilter(FacebookAuthCookieFilter) {
		grailsApplication = ref('grailsApplication')
		authenticationManager = ref('authenticationManager')
		facebookAuthUtils = ref('facebookAuthUtils')
		logoutUrl = 'j_spring_security_logout'
		createAccountUrl = '/login/facebookCreateAccount'
		registerUrl = '/register'
		authenticationFailureHandler = ref('fbAuthenticationFailureHandler')
	}

	facebookAuthService(FacebookAuthService) {
		grailsApplication = ref('grailsApplication')
		userDomainClassName = conf.userLookup.userDomainClassName
	}
	
	

	facebookAuthProvider(FacebookAuthProvider) {
		facebookAuthDao = ref('facebookAuthDao')
		facebookAuthUtils = ref('facebookAuthUtils')
		preAuthenticationChecks = ref('preAuthenticationChecks')
		postAuthenticationChecks = ref('postAuthenticationChecks')
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
	
	

	openIDAuthProvider(OpenIDAuthenticationProvider) {
		userDetailsService = ref('userDetailsService')
		preAuthenticationChecks = ref('preAuthenticationChecks')
		postAuthenticationChecks = ref('postAuthenticationChecks')
	}

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
	
	emailConfirmationService(EmailConfirmationService) {
		mailService = ref('mailService')
	}
}
