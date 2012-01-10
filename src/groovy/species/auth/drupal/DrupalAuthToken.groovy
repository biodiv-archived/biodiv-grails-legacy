package species.auth.drupal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.AbstractAuthenticationToken

public class DrupalAuthToken extends AbstractAuthenticationToken implements Authentication {
	
	long uid
	String code = "password"
    Object principal
	
	Collection<GrantedAuthority> authorities
	
	def DrupalAuthToken() {
		super([] as Collection<GrantedAuthority>);
	}	

	public Object getCredentials() {
		return uid;
	}

    String toString() {
        return "Principal: $principal, uid: $uid, roles: ${authorities.collect { it.authority}}"
    }

}