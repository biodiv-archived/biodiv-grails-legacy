import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationFailureHandler;
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler;
import grails.plugin.springsecurity.userdetails.DefaultPostAuthenticationChecks;
import grails.plugin.springsecurity.userdetails.DefaultPreAuthenticationChecks;
import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.plugin.springsecurity.openid.userdetails.OpenIdUserDetailsService;
import species.auth.DefaultAjaxAwareRedirectStrategy;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

import com.the6hours.grails.springsecurity.facebook.DefaultFacebookAuthDao;

import javax.servlet.http.HttpServletResponse
import species.auth.ConsumerManager;
import species.auth.FacebookAuthCookieFilter;
import species.auth.FacebookAuthCookieLogoutHandler;
import species.auth.FacebookAuthProvider;
import species.auth.FacebookAuthUtils;
import species.auth.OpenIDAuthenticationFilter;
import species.auth.OpenIDAuthenticationProvider;
import species.auth.OpenIdAuthenticationFailureHandler;

import species.participation.EmailConfirmationService;
import speciespage.FacebookAuthService;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import grails.util.Environment

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH 
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import species.auth.DefaultOauthUserDetailsService;
import species.auth.MyOauthService;
import species.utils.marshallers.*;
import species.auth.RestAuthenticationFailureHandler;
import species.auth.BiodivRestAuthenticationTokenJsonRenderer;
import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationSuccessHandler;
import grails.plugin.mail.MailMessageContentRenderer;
import grails.rest.render.json.JsonRenderer;
import org.codehaus.groovy.grails.web.mime.MimeType;
import species.participation.Comment;
import species.auth.RestTokenValidationFilter;
import species.MyEntityInterceptor;
import species.auth.AjaxAwareAuthenticationEntryPoint;
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
    
    // default 'authenticationEntryPoint'
    //overriding entry point defined in rest plugin to redirect to login page on accessdenied exception. Workd only when anonymous auth is present in the session
    authenticationEntryPoint(AjaxAwareAuthenticationEntryPoint, conf.auth.loginFormUrl) { // '/login/auth'
        ajaxLoginFormUrl = conf.auth.ajaxLoginFormUrl // '/login/authAjax'
        forceHttps = conf.auth.forceHttps // false
        useForward = conf.auth.useForward // false
        portMapper = ref('portMapper')
        portResolver = ref('portResolver')
        userGroupService = ref('userGroupService') 
    }

    /** securityContextRepository */
    securityContextRepository(HttpSessionSecurityContextRepository) {
        allowSessionCreation = conf.scr.allowSessionCreation // true
        disableUrlRewriting = conf.scr.disableUrlRewriting // true
        springSecurityContextKey = conf.scr.springSecurityContextKey // SPRING_SECURITY_CONTEXT
    }

          
    requestCache(HttpSessionRequestCache) {
        portResolver = ref('portResolver')
        createSessionAllowed = conf.requestCache.createSession // true
        requestMatcher = ref('requestMatcher')
    }


    userDetailsService(OpenIdUserDetailsService) { grailsApplication = ref('grailsApplication') }

    def configRoot = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search

    if (Environment.current == Environment.DEVELOPMENT) {
        // In development we use messageLocalService as implementation
        // of MessageService.
	//File home = new File("${configRoot.speciesPortal.app.rootDir}/solr" );
        //File f = new File( home, "solr.xml" );
        CoreContainer container = new CoreContainer("${configRoot.speciesPortal.app.rootDir}/solr");
        container.load() 
            
        
//        speciesSolrServer(EmbeddedSolrServer, container, "biodiv" )
//        observationsSolrServer(EmbeddedSolrServer, container, "biodiv" );
//        newsletterSolrServer(EmbeddedSolrServer, container, "biodiv" );
            projectSolrServer(EmbeddedSolrServer, container, "projects" );
        //checklistSolrServer(EmbeddedSolrServer, container, "checklists" );
//        documentSolrServer(EmbeddedSolrServer, container, "biodiv" );
//        usersSolrServer(EmbeddedSolrServer, container, "biodiv" );
//        userGroupSolrServer(EmbeddedSolrServer, container, "biodiv" );
        biodivSolrServer(EmbeddedSolrServer, container, "biodiv" );

    } else {
/*        speciesSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/species"
        }

        observationsSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/observations"
        }

        newsletterSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/newsletters"
        }
*/
        projectSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL +"/projects", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/projects"
        }
/*
        documentSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/documents"
         }
        
        userGroupSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            log.debug "Initialized user groups search server to "+config.serverURL+"/biodiv"
        }
*/
        biodivSolrServer(org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer,config.serverURL+"/biodiv", config.queueSize, config.threadCount ) {
            setSoTimeout(config.soTimeout);
            setConnectionTimeout(config.connectionTimeout);
            setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
            setMaxTotalConnections(config.maxTotalConnections);
            setFollowRedirects(config.followRedirects);
            setAllowCompression(config.allowCompression);
            setMaxRetries(config.maxRetries);
            //setParser(new XMLResponseParser()); // binary parser is used by default
            println "Initialized search server to "+config.serverURL+"/biodiv"
         }
    }//end of initializing solr Server
    
    resourceSearchService(speciespage.search.ResourceSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
    }

    speciesSearchService(speciespage.search.SpeciesSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
        resourceSearchService = ref('resourceSearchService');
    }
    observationsSearchService(speciespage.search.ObservationsSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
        resourceSearchService = ref('resourceSearchService');
     }
    //checklistSearchService(speciespage.search.ChecklistSearchService) {
    //    solrServer = ref('checklistSolrServer');
    //}
    newsletterSearchService(speciespage.search.NewsletterSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
    }
    projectSearchService(speciespage.search.ProjectSearchService) {
        solrServer = ref('projectSolrServer');
		sessionFactory = ref("sessionFactory");
    }
    documentSearchService(speciespage.search.DocumentSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
    }
    SUserSearchService(speciespage.search.SUserSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
    }
    userGroupSearchService(speciespage.search.UserGroupSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
    }

    biodivSearchService(speciespage.search.BiodivSearchService) {
        solrServer = ref('biodivSolrServer');
		sessionFactory = ref("sessionFactory");
        grailsApplication = ref('grailsApplication');
        observationsSearchService = ref('observationsSearchService');
        speciesSearchService = ref('speciesSearchService');
        documentSearchService = ref('documentSearchService');
        SUserSearchService = ref('SUserSearchService');
        userGroupSearchService = ref('userGroupSearchService');
    }

    preAuthenticationChecks(DefaultPreAuthenticationChecks)
    postAuthenticationChecks(DefaultPostAuthenticationChecks)

    SpringSecurityUtils.loadSecondaryConfig 'DefaultFacebookSecurityConfig'
    // have to get again after overlaying DefaultFacebookecurityConfig
    def dbConf = SpringSecurityUtils.securityConfig

    dbConf.facebook.bean.dao = 'facebookAuthDao'
    facebookAuthDao(DefaultFacebookAuthDao) {
        domainClassName = dbConf.facebook.domain.classname
        appUserConnectionPropertyName = dbConf.facebook.domain.appUserConnectionPropertyName
        userDomainClassName = dbConf.userLookup.userDomainClassName
        rolesPropertyName = dbConf.userLookup.authoritiesPropertyName
        coreUserDetailsService = ref('userDetailsService')
        defaultRoleNames = ['ROLE_USER']

    }

    facebookAuthUtils(FacebookAuthUtils) { 
        grailsApplication = ref('grailsApplication') 
        apiKey = conf.facebook.apiKey
        secret = conf.facebook.secret
        applicationId = conf.facebook.appId
        filterTypes = ['cookie', 'transparent']
        requiredPermissions = ['email']

    }

    facebookAuthCookieLogout(FacebookAuthCookieLogoutHandler) { 
        facebookAuthUtils = ref('facebookAuthUtils') 
        facebookAuthDao = ref('facebookAuthDao')
    }
    SpringSecurityUtils.registerLogoutHandler('facebookAuthCookieLogout')

    fbAuthenticationFailureHandler(AjaxAwareAuthenticationFailureHandler) {
        redirectStrategy = ref('redirectStrategy')
        defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
        useForward = conf.failureHandler.useForward // false
        ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
        exceptionMappings = conf.failureHandler.exceptionMappings // [:]
    }

    facebookAuthCookieTransparentFilter(FacebookAuthCookieFilter) {
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
        facebookAuthDao = ref('facebookAuthDao')
    }



    facebookAuthProvider(FacebookAuthProvider) {
        facebookAuthDao = ref('facebookAuthDao')
        facebookAuthUtils = ref('facebookAuthUtils')
        preAuthenticationChecks = ref('preAuthenticationChecks')
        postAuthenticationChecks = ref('postAuthenticationChecks')
    }

    openIDConsumerManager(ConsumerManager) { nonceVerifier = ref('openIDNonceVerifier') }

    openIdAuthenticationFailureHandler(OpenIdAuthenticationFailureHandler) {
        redirectStrategy = ref('redirectStrategy')
        defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
        useForward = conf.failureHandler.useForward // false
        ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
        exceptionMappings = conf.failureHandler.exceptionMappings // [:]
    }



    openIDAuthProvider(OpenIDAuthenticationProvider) {
        userDetailsService = ref('userDetailsService')
        preAuthenticationChecks 	= ref('preAuthenticationChecks')
        postAuthenticationChecks = ref('postAuthenticationChecks')
    }

    openIDAuthenticationFilter(OpenIDAuthenticationFilter) {
        //claimedIdentityFieldName = conf.openid.claimedIdentityFieldName // openid_identifier
        consumer = ref('openIDConsumer')
        rememberMeServices = ref('rememberMeServices')
        authenticationManager = ref('authenticationManager')
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('openIdAuthenticationFailureHandler')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
        filterProcessesUrl = '/j_spring_openid_security_check' // not configurable
    }

    emailConfirmationService(EmailConfirmationService) { mailService = ref('mailService') }
    redirectStrategy(DefaultAjaxAwareRedirectStrategy) {
        contextRelative = conf.redirectStrategy.contextRelative // false
    }

    dataSource(ComboPooledDataSource) { bean ->
        bean.destroyMethod = 'close'
        user = CH.config.dataSource.username
        password = CH.config.dataSource.password
        driverClass = CH.config.dataSource.driverClassName
        jdbcUrl = CH.config.dataSource.url
        unreturnedConnectionTimeout = 90 // seconds
		maxConnectionAge = 1800 // seconds (30 minutes)
        debugUnreturnedConnectionStackTraces = true
     } 

    /*if (Environment.current == Environment.DEVELOPMENT) {
    log4jConfigurer(org.springframework.beans.factory.config.MethodInvokingFactoryBean) {
        targetClass = "org.springframework.util.Log4jConfigurer"
        targetMethod = "initLogging"
        arguments = ["file:/home/sravanthi/git/biodiv/lib/log4j.properties"]
    }
    } else {
    log4jConfigurer(org.springframework.beans.factory.config.MethodInvokingFactoryBean) {
        targetClass = "org.springframework.util.Log4jConfigurer"
        targetMethod = "initLogging"
        arguments = ["classpath:log4j.properties"]
    }
    }*/

    /* oauthUserDetailsService */
    oauthUserDetailsService(DefaultOauthUserDetailsService) {
        userDetailsService = ref('userDetailsService')
        userService = ref('SUserService')
    }
    
    restOauthService(MyOauthService) {
        tokenGenerator = ref('tokenGenerator')
        tokenStorageService = ref('tokenStorageService')
        userDetailsService = ref('userDetailsService')
        grailsApplication = ref('grailsApplication')
        grailsLinkGenerator = ref('grailsLinkGenerator')
        oauthUserDetailsService = ref('oauthUserDetailsService')
    }

    customObjectMarshallers( CustomObjectMarshallers ) {
        grailsApplication = ref('grailsApplication') 
        userGroupService = ref('userGroupService') 

        marshallers = [
            new ObservationMarshaller(),
            new SpeciesMarshaller(),
            new DocumentMarshaller()
        ]
    }

    restAuthenticationFailureHandler(species.auth.RestAuthenticationFailureHandler) {
        statusCode = conf.rest.login.failureStatusCode?:HttpServletResponse.SC_FORBIDDEN
    }

    restAuthenticationTokenJsonRenderer(BiodivRestAuthenticationTokenJsonRenderer)
    restAuthenticationSuccessHandler(RestAuthenticationSuccessHandler) { 
        renderer = ref('restAuthenticationTokenJsonRenderer')
    }

    /* restAuthenticationFilter */
    /*
    restAuthenticationFilter(RestAuthenticationFilter) {
        authenticationManager = ref('authenticationManager')
        authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
        authenticationFailureHandler = ref('restAuthenticationFailureHandler')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        credentialsExtractor = ref('credentialsExtractor')
        endpointUrls = conf.rest.login.endpointUrls
        tokenGenerator = ref('tokenGenerator')
        tokenStorageService = ref('tokenStorageService')
    }
*/
/*
    final API_MIME_TYPE = "application/vnd.biodiv.app.api+json";
    final v1_MIME_TYPE = new MimeType(API_MIME_TYPE, [v: '1.0']);
    final v2_MIME_TYPE = new MimeType(API_MIME_TYPE, [v: '2.0']);
    commentV1Renderer(CommentRenderer, Comment, v1_MIME_TYPE) {
    }
 */
    

    restTokenValidationFilter(RestTokenValidationFilter) {
        grailsApplication = ref('grailsApplication') 
        webInvocationPrivilegeEvaluator = ref('webInvocationPrivilegeEvaluator') 
        headerName = conf.rest.token.validation.headerName
        validationEndpointUrl = conf.rest.token.validation.endpointUrl
        active = conf.rest.token.validation.active
        tokenReader = ref('tokenReader')
        enableAnonymousAccess = conf.rest.token.validation.enableAnonymousAccess
        authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
        authenticationFailureHandler = ref('restAuthenticationFailureHandler')
        restAuthenticationProvider = ref('restAuthenticationProvider')
    }

    webCacheKeyGenerator(species.utils.CustomCacheKeyGenerator)
    entityInterceptor(species.MyEntityInterceptor);

}


