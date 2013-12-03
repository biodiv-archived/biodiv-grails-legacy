package species.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.plugin.springsecurity.AjaxAwareAuthenticationFailureHandler;
import org.codehaus.groovy.grails.plugin.springsecurity.ReflectionUtils;
import org.codehaus.groovy.grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.plugin.springsecurity.openid.InvalidOpenidEndpoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class OpenIdAuthenticationFailureHandler extends AjaxAwareAuthenticationFailureHandler {

	/** Session key for the Open ID username/uri. */
	public static final String LAST_OPENID_USERNAME = "LAST_OPENID_USERNAME";

	/** Session key for the attributes that were returned. */
	public static final String LAST_OPENID_ATTRIBUTES = "LAST_OPENID_ATTRIBUTES";

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler#onAuthenticationFailure(
	 * 	javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * 	org.springframework.security.core.AuthenticationException)
	 */
	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException {

		if (exception.getMessage().contains("Unable to process claimed identity")) { //TODO Not the best way
			super.onAuthenticationFailure(request, response, new InvalidOpenidEndpoint(exception));
			return;
		}

		boolean createMissingUsers = Boolean.TRUE.equals(
				ReflectionUtils.getConfigProperty("openid.registration.autocreate"));

		if (!createMissingUsers || !isSuccessfulLoginUnknownUser(exception) || SpringSecurityUtils.isAjax(request)) {
			super.onAuthenticationFailure(request, response, exception);
			return;
		}

		OpenIDAuthenticationToken authentication = (OpenIDAuthenticationToken)exception.getAuthentication();
		request.getSession().setAttribute(LAST_OPENID_USERNAME, authentication.getPrincipal().toString());
		request.getSession().setAttribute(LAST_OPENID_ATTRIBUTES, extractAttrsWithValues(authentication));

		String createAccountUri = (String)ReflectionUtils.getConfigProperty("openid.registration.createAccountUri");
		getRedirectStrategy().sendRedirect(request, response, createAccountUri);
	}

	private boolean isSuccessfulLoginUnknownUser(AuthenticationException exception) {
		if (!(exception instanceof UsernameNotFoundException)) {
			return false;
		}

		Authentication authentication = exception.getAuthentication();
		if (!(authentication instanceof OpenIDAuthenticationToken)) {
			return false;
		}

		return OpenIDAuthenticationStatus.SUCCESS.equals(
				((OpenIDAuthenticationToken)authentication).getStatus());
	}

	private List<OpenIDAttribute> extractAttrsWithValues(final OpenIDAuthenticationToken authentication) {
		List<OpenIDAttribute> attributes = new ArrayList<OpenIDAttribute>();
		for (OpenIDAttribute attr : authentication.getAttributes()) {
			if (attr.getValues() == null || attr.getValues().isEmpty()) {
				continue;
			}
			if (attr.getValues().size() == 1 && attr.getValues().get(0) == null) {
				continue;
			}
			attributes.add(attr);
		}
		return attributes;
	}
}
