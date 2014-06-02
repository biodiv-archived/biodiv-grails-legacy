package species.auth;

import com.odobo.grails.plugin.springsecurity.rest.OauthService;
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.client.BaseOAuthClient
import org.pac4j.oauth.profile.OAuth20Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken;
import org.springframework.security.core.userdetails .UserDetails;

class MyOauthService extends OauthService {

    @Override
    String storeAuthentication(String provider, WebContext context) {
        BaseOAuthClient client = getClient(provider)
        Credentials credentials = client.getCredentials context

        log.debug "Querying provider to fetch User ID"
        CommonProfile profile = client.getUserProfile credentials, null
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

        return tokenValue
    }

}
