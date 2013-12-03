package species.auth.drupal

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugin.springsecurity.GrailsUser
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

import species.auth.SUser

public class DrupalAuthenticationProvider implements AuthenticationProvider {

	private static def log = Logger.getLogger(this)

	DrupalAuthDao drupalAuthDao

	boolean createNew = true

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.debug "Authenticating drupal user using token : "+authentication;

		if(authentication instanceof DrupalAuthToken) {
			DrupalAuthToken authRequest = (DrupalAuthToken) authentication

			if(!authRequest.uid) {
				throw new BadCredentialsException("Drupal UID is missing");
			}

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
				UserDetails userDetails = createUserDetails(user, authRequest.getCredentials().toString());
				return new DrupalAuthToken(userDetails, authRequest.getCredentials(), userDetails.getAuthorities()) 
			} 
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