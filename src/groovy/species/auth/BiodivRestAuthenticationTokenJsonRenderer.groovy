package species.auth;

import grails.util.Holders;

import grails.plugin.springsecurity.SpringSecurityUtils
import  grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert
import static org.springframework.http.HttpStatus.*;

class BiodivRestAuthenticationTokenJsonRenderer  implements AccessTokenJsonRenderer {//extends DefaultRestAuthenticationTokenJsonRenderer {
    def utilsService;
    def grailsApplication;

    @Override
    String generateJson(AccessToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal

        def conf = SpringSecurityUtils.securityConfig

        String usernameProperty = conf.rest.token.rendering.usernamePropertyName
        String tokenProperty = conf.rest.token.rendering.tokenPropertyName
        String authoritiesProperty = conf.rest.token.rendering.authoritiesPropertyName

        def result = [:]
        result["id"] = userDetails.id
        result["$usernameProperty"] = userDetails.username
        result["$tokenProperty"] = restAuthenticationToken.accessToken
        result["$authoritiesProperty"] = userDetails.authorities.collect {GrantedAuthority role -> role.authority }

        if (userDetails instanceof OauthUser) {
            CommonProfile profile = (userDetails as OauthUser).userProfile
            result.with {
                email = profile.email
                displayName = profile.displayName
            }
        }

        utilsService = grailsApplication.getMainContext().getBean("utilsService");
        def model = utilsService.getSuccessModel('Successfully logged in', null, OK.value(), result);
        def jsonResult = model as JSON

        log.debug "Generated JSON:\n ${jsonResult.toString(true)}"

        return jsonResult.toString()

    }
            

}
