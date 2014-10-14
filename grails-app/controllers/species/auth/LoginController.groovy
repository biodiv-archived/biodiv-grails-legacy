package species.auth

import grails.converters.JSON

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import grails.plugin.springsecurity.ui.RegistrationCode;
import species.utils.Utils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import species.auth.DefaultAjaxAwareRedirectStrategy;
import org.springframework.context.i18n.LocaleContextHolder as LCH;

class LoginController {

	/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	def grailsApplicaiton

	def messageSource;

	/**
	 * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
	 */
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
		else {
			redirect url:uGroup.createLink(action:'auth', controller:"login", 'userGroupWebaddress':params.webaddress, params:params)
			//redirect action: 'auth', params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def auth = {
		def config = SpringSecurityUtils.securityConfig
		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}
		String view = 'auth'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		render view: view, model: [postUrl: postUrl,
					rememberMeParameter: config.rememberMe.parameter]
	}

	def authSuccess = {
		if(params.uid) {
			def targetUrl = request.getParameter(SpringSecurityUtils.DEFAULT_TARGET_PARAMETER);
			if (StringUtils.hasText(targetUrl)) {
				try {
					targetUrl = URLDecoder.decode(targetUrl, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException("UTF-8 not supported. Shouldn't be possible");
				}
				
				log.debug "Redirecting to target : $targetUrl";
				(new DefaultAjaxAwareRedirectStrategy()).sendRedirect(request, response, targetUrl);
				return;
			}

            def requestCache = new HttpSessionRequestCache();
		    def defaultSavedRequest = requestCache.getRequest(request, response);
			//def defaultSavedRequest = request.getSession()?.getAttribute(WebAttributes.SAVED_REQUEST)
			log.debug "Redirecting to DefaultSavedRequest : $defaultSavedRequest";
			if(defaultSavedRequest) {
				(new DefaultAjaxAwareRedirectStrategy()).sendRedirect(request, response, defaultSavedRequest.getRedirectUrl());
				return
			} else {
				redirect uri:"/";
				return;
			}
		} else {
            params.remove('action');
            params.remove('controller');
            if(!params.token) {
                params.remove('token');
            } 
            
            if(params.id) {
                params.id = Long.parseLong(params.id);
            }
            render params as JSON
        }
	}

	/**
	 * The redirect action for Ajax requests.
	 */
	def authAjax = {
		response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
		response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}

	/**
	 * Show denied page.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
		authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			//redirect action: 'full', params: params
			redirect url:uGroup.createLink(action:'full', controller:"login", 'userGroupWebaddress':params.webaddress, params:params)
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render controller:'openId', view: 'auth', params: params,
				model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
					postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def authfail = {

		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		log.debug exception

		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = messageSource.getMessage("springSecurity.errors.login.expired", null, LCH.getLocale())
				
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = messageSource.getMessage("springSecurity.errors.login.passwordExpired", null, LCH.getLocale())
			}
			else if (exception instanceof DisabledException) {
				msg = messageSource.getMessage("springSecurity.errors.login.disabled", null, LCH.getLocale())				
			}
			else if (exception instanceof LockedException) {
				//check if the email has been verified and give option to resend the email
				def registerationCode = RegistrationCode.findAllByUsername(username.decodeHTML()) 
				if(registerationCode) {
					def url = Utils.getDomainServerUrl(request) + "/register/resend?email="+username	
					msg = messageSource.getMessage("email.verified.resend.url", [url] as Object[], LCH.getLocale())
				}else 
					msg = messageSource.getMessage("springSecurity.errors.login.locked", null, LCH.getLocale())
			}
			else {
				msg = messageSource.getMessage("springSecurity.errors.login.fail", null, LCH.getLocale())
                msg += " ("+exception.message+")"
			}
		}
		
        if (springSecurityService.isAjax(request)) {
			render([error: msg] as JSON)
		}
		else {
			flash.error = msg
			//redirect action: 'auth', params: params
			redirect url:uGroup.createLink(action:'auth', controller:"login", 'userGroupWebaddress':params.webaddress, params:params)
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		render([error: 'access denied'] as JSON)
	}

	def authFromDrupal = {
		def config = SpringSecurityUtils.securityConfig

		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}
		//		//String postUrl = "/${grailsApplication.metadata['app.name']}${config.apf.filterProcessesUrl}"
		//		def urlpath = grailsApplication.config.speciesPortal.drupal.getAuthentication;
		//		request.cookies.each {
		//			response.addCookie(it)
		//		}
		//		def qStr = "";
		//		def reqParams = ['spring-security-redirect':request.getHeader('referer')]
		//		reqParams.each { k,v -> qStr += "$k=${v.encodeAsURL()}&" }
		//		String host = request.getRequestURL();
		//		//TODO : not a clean way to construct drupal host url
		//		host = host.substring(0,host.indexOf(':8080') );
		//
		//		//redirect (url: host+urlpath + "?" + qStr);
		def openIdProvider = request.cookies.find { Cookie c ->
			return c.name == ('openid_provider');
		}
		if(grailsApplication.config.checkin.drupal) {
			response.setHeader 'Location', request.getHeader('referer')
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		} else {
			redirect url:uGroup.createLink(action:'auth', controller:"login", 'userGroupWebaddress':params.webaddress)
			//redirect (action:'auth');
		}
	}
}
