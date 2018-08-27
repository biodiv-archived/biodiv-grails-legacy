package species.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JwtAuthToken extends AbstractAuthenticationToken {
    String username;
    String jwtToken;
    def principal;
    Collection<GrantedAuthority> authorities = [] as Collection<GrantedAuthority>;

    JwtAuthToken(String jwtToken) {
        super([] as Collection<GrantedAuthority>);
        this.jwtToken = jwtToken;
    }
    
    def getCredentials() { 
        return jwtToken;
    }
}
