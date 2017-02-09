package species.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;

import org.springframework.security.core.AuthenticationException;
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationEntryPoint;
import org.springframework.util.Assert;
import grails.plugin.springsecurity.SpringSecurityUtils;
import species.groups.UserGroup;
import species.utils.Utils;

class AjaxAwareAuthenticationEntryPoint
extends
grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationEntryPoint {

    def userGroupService;
    /**
     * @param loginFormUrl URL where the login page can be found. Should either be relative to the web-app context path
     * (include a leading {@code /}) or an absolute URL.
     */
    public AjaxAwareAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
	protected String determineUrlToUseForThisRequest(final HttpServletRequest request,
			final HttpServletResponse response, final AuthenticationException e) {

		if (ajaxLoginFormUrl != null && SpringSecurityUtils.isAjax(request)) {
			return ajaxLoginFormUrl;
		}

        println request;
        println request.getRequestURI();
        println request.getQueryString();
println request.getParameterMap();
        def userGroupId = request.getParameter('userGroupInstanceId');
        String url = getLoginFormUrl();
        if(userGroupId) {
            def userGroup = UserGroup.read(Long.parseLong(userGroupId));
            if(userGroup)
                url = userGroupService.userGroupBasedLink([ 'controller':'login', 'action':'auth', 'userGroup':userGroup]);
        }
		return url;
	}
}


