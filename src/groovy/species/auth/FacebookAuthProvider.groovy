package species.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.User
import org.apache.log4j.Logger
import grails.plugin.springsecurity.userdetails.DefaultPostAuthenticationChecks;
import grails.plugin.springsecurity.userdetails.DefaultPreAuthenticationChecks;
import grails.plugin.springsecurity.userdetails.GrailsUser;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.the6hours.grails.springsecurity.facebook.FacebookAuthDao;
import species.auth.FacebookAuthToken;

public class FacebookAuthProvider extends com.the6hours.grails.springsecurity.facebook.FacebookAuthProvider {

	private static def log = Logger.getLogger(this)

	UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
	UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

	public Authentication authenticate(Authentication authentication) {
       
		FacebookAuthToken token = authentication
        //HACK to accomodate registering new user from facebookCreateAccount
        if(token.uid > 0) {
            def user = facebookAuthDao.findUser(token.uid as Long)
            if (user == null && token.user != null && token.user.accessToken != null) {
                throw new AuthenticationServiceException("Registering from Facebook ${token.user}");
            }
        }

/*
		def user = facebookAuthDao.findUser(token.uid as Long)
		if (user == null) {
			//log.debug "New person $token.uid"
            if(token.user != null) {
					throw new AuthenticationServiceException("Registering from Facebook");
            }
            else if (createNew) {
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
			token.principal = userDetails//facebookAuthDao.getPrincipal(user)
			token.authorities = userDetails.getAuthorities()

			try {
				preAuthenticationChecks.check(userDetails);
			} catch (AuthenticationException exception) {
				throw exception;
			}

			postAuthenticationChecks.check(userDetails);
		} else {
			token.authenticated = false
		}*/

        super.authenticate(authentication);
        token.details = token.principal

        if(token.details) {
			try {
				preAuthenticationChecks.check(token.details);
			} catch (AuthenticationException exception) {
				throw exception;
			}

			postAuthenticationChecks.check(token.details);
        }

		log.debug "returning fb token : $token"
		return token
	}

	public boolean supports(Class<? extends Object> authentication) {
		return FacebookAuthToken.isAssignableFrom(authentication);
	}

	protected UserDetails createUserDetails(Object fbUser, String secret) {
		
		def user = fbUser.user;
		Collection<GrantedAuthority> roles = facebookAuthDao.getRoles(user)
		
		def conf = SpringSecurityUtils.securityConfig
		String usernamePropertyName = conf.userLookup.usernamePropertyName
		String passwordPropertyName = conf.userLookup.passwordPropertyName
		String enabledPropertyName = conf.userLookup.enabledPropertyName
		String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
		String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
		String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName
		
		String username = user."$usernamePropertyName";//fbUser.uid.toString()
		String password = secret
		boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
		boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
		boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
		boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false

		new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
				!accountLocked, roles, user.id)

	}

}
