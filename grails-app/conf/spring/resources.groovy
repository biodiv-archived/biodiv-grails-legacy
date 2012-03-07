import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationSuccessHandler;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdUserDetailsService;

import species.auth.drupal.DrupalAuthCookieFilter;
import species.auth.drupal.DrupalAuthUtils;

// Place your Spring DSL code here
beans = {
	//userDetailsService(species.auth.drupal.DrupalUserDetailsService);
	drupalAuthentiactionProvider(species.auth.drupal.DrupalAuthenticationProvider) {
		//userDetailsService = ref("userDetailsService");
		drupalAuthDao = ref('drupalAuthDao')
	}
	//springSecurityUiService(com.strandls.avadis.auth.DummySpringSecurityUiService);
//	drupalAuthDirectFilter(species.auth.drupal.DrupalAuthDirectFilter, '/j_drupal_spring_security_check') {
//		authenticationManager = ref('authenticationManager')
//		sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
//		authenticationSuccessHandler = ref('authenticationSuccessHandler')
//		authenticationFailureHandler = ref('authenticationFailureHandler')
//		rememberMeServices = ref('rememberMeServices')
//		authenticationDetailsSource = ref('authenticationDetailsSource')
//	}
	
	def conf = SpringSecurityUtils.securityConfig;
	
	authenticationSuccessHandler(AjaxAwareAuthenticationSuccessHandler) {
		requestCache = ref('requestCache')
		defaultTargetUrl = conf.successHandler.defaultTargetUrl // '/'
		alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault // false
		targetUrlParameter = conf.successHandler.targetUrlParameter // 'spring-security-redirect'
		ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl // '/login/ajaxSuccess'
		useReferer = true // false
		redirectStrategy = ref('redirectStrategy')
	}

	drupalAuthUtils(DrupalAuthUtils);
	drupalAuthCookieFilter(DrupalAuthCookieFilter) {
		authenticationManager = ref('authenticationManager')
		drupalAuthUtils = ref('drupalAuthUtils')
		sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
		authenticationSuccessHandler = ref('authenticationSuccessHandler')
		authenticationFailureHandler = ref('authenticationFailureHandler')
//		rememberMeServices = ref('rememberMeServices')
//		authenticationDetailsSource = ref('authenticationDetailsSource')
//		filterProcessesUrl = conf.apf.filterProcessesUrl // '/j_spring_security_check'
//		usernameParameter = conf.apf.usernameParameter // j_username
//		passwordParameter = conf.apf.passwordParameter // j_password
		continueChainBeforeSuccessfulAuthentication = SpringSecurityUtils.securityConfig.apf.continueChainBeforeSuccessfulAuthentication // false
		allowSessionCreation = SpringSecurityUtils.securityConfig.apf.allowSessionCreation // true
//		postOnly = conf.apf.postOnly // true
		logoutUrl =  SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
	
	drupalAuthDao(species.auth.drupal.DrupalAuthDao)
	
	userDetailsService(OpenIdUserDetailsService) {
		grailsApplication = ref('grailsApplication')
	}

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
	
}