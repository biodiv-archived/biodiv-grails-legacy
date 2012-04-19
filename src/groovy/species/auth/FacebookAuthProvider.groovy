package species.auth

import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.User
import org.apache.log4j.Logger

import com.the6hours.grails.springsecurity.facebook.FacebookAuthDao;
import com.the6hours.grails.springsecurity.facebook.FacebookAuthToken;

public class FacebookAuthProvider implements AuthenticationProvider {

	private static def log = Logger.getLogger(this)

	FacebookAuthDao facebookAuthDao
	FacebookAuthUtils facebookAuthUtils

	boolean createNew = true

	public Authentication authenticate(Authentication authentication) {
		FacebookAuthToken token = authentication

		def user = facebookAuthDao.findUser(token.uid as Long)

		if (user == null) {
			//log.debug "New person $token.uid"
			if (createNew) {
				log.info "Create new facebook user with uid $token.uid"
				log.info "Setting domain specific applicationId and secret"
				String applicationId = facebookAuthUtils.getFacebookAppIdForDomain(token.domain); 
				String secret = facebookAuthUtils.getFacebookAppSecretForDomain(token.domain)
				token.accessToken = facebookAuthUtils.getAccessToken(applicationId, secret, token.code)
				if(token.accessToken) {
					throw new UsernameNotFoundException("No user. Please register", token);
					//	user = facebookAuthDao.create(token)
				} else {
					throw new BadCredentialsException("Can't read data from Facebook");
				}
			} else {
				log.error "User $token.uid not exists - not authenticated"
			}
		}
		if (user != null) {
			UserDetails userDetails = createUserDetails(user, token.code)

			token.details = userDetails
			token.principal = facebookAuthDao.getPrincipal(user)
			token.authorities = userDetails.getAuthorities()
		} else {
			token.authenticated = false
		}
		log.debug "returning fb token : $token"
		return token
	}

	public boolean supports(Class<? extends Object> authentication) {
		return FacebookAuthToken.isAssignableFrom(authentication);
	}

	protected UserDetails createUserDetails(Object user, String secret) {
		Collection<GrantedAuthority> roles = facebookAuthDao.getRoles(user)
		new User(user.uid.toString(), secret, true,
		true, true, true, roles)
	}

}