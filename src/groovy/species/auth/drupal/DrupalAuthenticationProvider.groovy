package species.auth.drupal

import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.User
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser;

import species.auth.SUser;

public class DrupalAuthenticationProvider implements AuthenticationProvider {

	private static def log = Logger.getLogger(this)

	DrupalAuthDao drupalAuthDao

	boolean createNew = true

	public Authentication authenticate(Authentication authentication) {
		log.debug "Authenticating drupal user using token : "+authentication;
		
		if(authentication instanceof DrupalAuthToken) {
			DrupalAuthToken authRequest = (DrupalAuthToken) authentication

			SUser user = drupalAuthDao.findUser(authRequest.uid)

			if (user == null) {
				//log.debug "New person $authRequest.uid"
				if (createNew) {
					log.info "Create new drupal user with uid $authRequest.uid"
					user = drupalAuthDao.create(authRequest)
				} else {
					log.error "User $authRequest.uid not exists - not authenticated"
				}
			}
			if (user != null) {
				UserDetails userDetails = createUserDetails(user, authRequest.code)

				authRequest.details = userDetails
				authRequest.principal = userDetails
				authRequest.authorities = userDetails.getAuthorities()
			} else {
				authRequest.authenticated = false
			}
			return authRequest
		}
		return null;
	}

	public boolean supports(Class<? extends Object> authentication) {
		return DrupalAuthToken.isAssignableFrom(authentication);
	}

	protected UserDetails createUserDetails(SUser user, String secret) {
		return new GrailsUser(user.username, user.password, user.enabled, !user.accountExpired, !user.passwordExpired,
		!user.accountLocked, drupalAuthDao.getRoles(user), user.id);
	}

}