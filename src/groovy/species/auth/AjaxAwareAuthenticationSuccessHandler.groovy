package species.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;

import species.utils.Utils;

class AjaxAwareAuthenticationSuccessHandler
extends
org.codehaus.groovy.grails.plugin.springsecurity.AjaxAwareAuthenticationSuccessHandler {

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler#onAuthenticationSuccess(
	 * 	javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * 	org.springframework.security.core.Authentication)
	 */
	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
	final Authentication authentication) throws ServletException, IOException {

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

		
		super.onAuthenticationSuccess(request, response, authentication);
		//removing login referrer
		request.getSession().removeAttribute("LOGIN_REFERRER");
		
	}

	private getLoginStatusCookie(HttpServletRequest request) {
		String cookieName = "login"
		return request.cookies.find { Cookie it ->
			return it.name == cookieName
		}
	}

}
