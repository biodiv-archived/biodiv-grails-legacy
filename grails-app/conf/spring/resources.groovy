// Place your Spring DSL code here
beans = {
	//userDetailsService(species.auth.drupal.DrupalUserDetailsService);
	drupalAuthentiactionProvider(species.auth.drupal.DrupalAuthenticationProvider) {
		//userDetailsService = ref("userDetailsService");
		drupalAuthDao = ref('drupalAuthDao')
	}
	//springSecurityUiService(com.strandls.avadis.auth.DummySpringSecurityUiService);
	drupalAuthDirectFilter(species.auth.drupal.DrupalAuthDirectFilter, '/j_drupal_spring_security_check') {
		authenticationManager = ref('authenticationManager')
		sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
		authenticationSuccessHandler = ref('authenticationSuccessHandler')
		authenticationFailureHandler = ref('authenticationFailureHandler')
		rememberMeServices = ref('rememberMeServices')
		authenticationDetailsSource = ref('authenticationDetailsSource')
	}
	
	drupalAuthDao(species.auth.drupal.DrupalAuthDao)
	
}