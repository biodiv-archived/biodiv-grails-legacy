package species.auth


import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UsernameNotFoundException
import species.auth.SUser;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import species.auth.JwtAuthToken;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;


public class JwtTokenAuthProvider  implements AuthenticationProvider, InitializingBean, ApplicationContextAware {
   
    def coreUserDetailsService;
    ApplicationContext applicationContext
    private final String JWT_SALT = "12345678901234567890123456789012";

    Authentication authenticate(Authentication authentication) {
        JwtAuthToken token = (JwtAuthToken)authentication;
        if(token.jwtToken != null) {
            def authenticator =  new JwtAuthenticator(new org.pac4j.jwt.config.signature.SecretSignatureConfiguration(JWT_SALT));
            CommonProfile profile = authenticator.validateToken(token.jwtToken);
            println "******************************"
            println "******************************"
            println profile;
            println "******************************"
            println "******************************"

            if(profile) {
                String email = null;
                if(profile instanceof org.pac4j.oauth.profile.google2.Google2Profile) {
                    final List list = profile.getEmails();
                    println list;
                    if (list != null && !list.isEmpty()) {
                        email= list.get(0).email;
                    }
                } else {
                    email = profile.email;
                }
                def user = SUser.findByEmail(email);
                if(user) {
                    token.details = null
                    token.principal = coreUserDetailsService.loadUserByUsername(user.email, true);
                    token.authenticated = true
                    token.authorities = (Collection<GrantedAuthority>)((UserDetails)token.principal).authorities;
                } else {
					throw new UsernameNotFoundException("User not found.", profile.email);
                }
            } else {
                token.authenticated = false
            }
        } else {
            token.authenticated = false
        }
        return token;
    }

    boolean supports(Class<? extends Object> authentication) {
        JwtAuthToken.isAssignableFrom authentication
    }

    void afterPropertiesSet() {
    }

}
