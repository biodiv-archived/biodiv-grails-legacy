package species.auth.drupal

import java.util.Collection

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

public class DrupalAuthToken extends UsernamePasswordAuthenticationToken {
	
	long uid
	
	public DrupalAuthToken(long uid, Object principal, Object credentials) {
		super(principal, credentials);
		this.uid = uid
	}
	
	
	public DrupalAuthToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
		this.uid = uid;
	}
	
//	String username;
//	String code = "password"
//    Object principal
//	
//	Collection<GrantedAuthority> authorities
//	
//	def DrupalAuthToken() {
//		super([] as Collection<GrantedAuthority>);
//	}	
//
//	public Object getCredentials() {
//		return uid;
//	}
//
//    String toString() {
//        return "Principal: $principal, uid: $uid, roles: ${authorities.collect { it.authority}}"
//    }

}