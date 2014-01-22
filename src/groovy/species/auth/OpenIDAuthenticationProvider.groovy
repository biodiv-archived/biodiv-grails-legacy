package species.auth

import grails.plugin.springsecurity.userdetails.DefaultPostAuthenticationChecks;
import grails.plugin.springsecurity.userdetails.DefaultPreAuthenticationChecks;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

class OpenIDAuthenticationProvider extends
org.springframework.security.openid.OpenIDAuthenticationProvider {

	UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
	UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		Authentication openIdAuthToken = super.authenticate(authentication);
		def user = openIdAuthToken.principal;
		Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");

		try {
			preAuthenticationChecks.check(user);
		} catch (AuthenticationException exception) {
			throw exception;
		}

		postAuthenticationChecks.check(user);
		return openIdAuthToken
	}
}
