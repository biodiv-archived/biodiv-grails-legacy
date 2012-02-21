import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

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
	drupalAuthUtils(DrupalAuthUtils);
	drupalAuthCookieFilter(DrupalAuthCookieFilter) {
		authenticationManager = ref('authenticationManager')
		drupalAuthUtils = ref('drupalAuthUtils')
		logoutUrl =  SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
	
	drupalAuthDao(species.auth.drupal.DrupalAuthDao)
	
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