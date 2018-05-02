package species.auth;

import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.RestOauthService;
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.profile.OAuth20Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UserDetails;
import org.scribe.model.Token;

class MyOauthService extends RestOauthService {

    @Override
    String storeAuthentication(String provider, WebContext context) {
        super.storeAuthentication(provider, context);
        /*
        BaseOAuthClient client = getClient(provider)
        Credentials credentials 
        try {
//        credentials = client.getCredentials context
            log.debug "Querying provider to fetch User ID"

            final String[] accessTokens = context.getRequest().getParameterValues('access_token');
            if(!accessTokens) {
                throw new OAuthCredentialsException("No access token");
            }
            Token token = new Token(accessTokens[0], "");

            //this.service.signRequest(accessToken, request);

            CommonProfile profile = client.retrieveUserProfileFromToken(token);

            log.debug "User's ID: ${profile.id}"

            def providerConfig = grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
            List defaultRoles = providerConfig.defaultRoles.collect { new SimpleGrantedAuthority(it) }
            UserDetails userDetails = oauthUserDetailsService.loadUserDetailsByUserProfile(profile, defaultRoles)

            String tokenValue = tokenGenerator.generateToken()
            log.debug "Generated REST authentication token: ${tokenValue}"

            log.debug "Storing token on the token storage"
            tokenStorageService.storeToken(tokenValue, userDetails)
            Authentication authenticationResult = new RestAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, tokenValue)
            SecurityContextHolder.context.setAuthentication(authenticationResult)

            return tokenValue+"&id="+userDetails.id;
        } catch(e) {
            e.printStackTrace();
            throw e;
        }
*/
    }

}
