package species.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

import species.utils.Utils;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;


class AjaxAwareAuthenticationSuccessHandler
extends
grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler {

    private final String JWT_SALT = "12345678901234567890123456789012";

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler#onAuthenticationSuccess(
	 * 	javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * 	org.springframework.security.core.Authentication)
	 */
	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
	final Authentication authentication) throws ServletException, IOException {

        boolean ajax = SpringSecurityUtils.isAjax(request)

        // GPSPRINGSECURITYCORE-240
        if (ajax) {
            requestCache.removeRequest request, response
        }

        try {
            if (ajax) {
                clearAuthenticationAttributes request
                if (logger.debugEnabled) {
                    logger.debug 'Redirecting to Ajax Success Url: ' + ajaxSuccessUrl
                }
                redirectStrategy.sendRedirect request, response, ajaxSuccessUrl
            }
            else {
                super.onAuthenticationSuccess request, response, authentication
            }
        }
        finally {
                // always remove the saved request
                requestCache.removeRequest request, response
        }
        
        println SecurityContextHolder?.context.authentication
        def jwtToken = issueToken(SecurityContextHolder?.context.authentication);

		Cookie cookie = getLoginStatusCookie(request)
		if (cookie == null) {
			cookie = new Cookie("login", "true");
		} else {
			cookie.login = true;
		}
		cookie.maxAge = 0
		cookie.path = '/'
		cookie.domain = "."+Utils.getDomain(request);
		response.addCookie(cookie)
        println "------------------"
		super.onAuthenticationSuccess(request, response, authentication);
        println "------------------"
		//removing login referrer
		request.getSession().removeAttribute("LOGIN_REFERRER");

/*      println "Setting jwtToken as cookie"
        Cookie jwtCookie = new Cookie('jwtToken', jwtToken);
        println "++++++++++++++++++++++++++++++++++++"
        println "++++++++++++++++++++++++++++++++++++"
        println "++++++++++++++++++++++++++++++++++++"
        println Utils.getDomain(request);
        println response
		response.addCookie(cookie)
*/
    }

	private getLoginStatusCookie(HttpServletRequest request) {
		String cookieName = "login"
		return request.cookies.find { Cookie it ->
			return it.name == cookieName
		}
	}

    private String issueToken(authToken) {
        def userToken = SecurityContextHolder?.context.authentication;
        println userToken;
        println userToken.details;
        println userToken.principal
        println userToken.credentials;
        def user = userToken.principal;
        final CommonProfile profile = new CommonProfile();
        profile.setId(user.getId());    
        profile.addAttribute('username', user.getUsername());
        profile.addAttribute("email", user.getUsername());
        Set roles = user.getAuthorities();
        for(def role : roles) {        
            profile.addRole(role.getAuthority());
        }
        JwtGenerator<CommonProfile> generator = new JwtGenerator<>(
            new SecretSignatureConfiguration(JWT_SALT));
        String jwtToken = generator.generate(profile);
        return jwtToken;
    }
}
