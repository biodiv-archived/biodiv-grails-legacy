package species.auth;

import grails.plugin.springsecurity.SpringSecurityUtils
import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert
import com.odobo.grails.plugin.springsecurity.rest.token.rendering.DefaultRestAuthenticationTokenJsonRenderer;
import com.odobo.grails.plugin.springsecurity.rest.token.rendering.RestAuthenticationTokenJsonRenderer;

class BiodivRestAuthenticationTokenJsonRenderer  implements RestAuthenticationTokenJsonRenderer {//extends DefaultRestAuthenticationTokenJsonRenderer {

    @Override
    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal

        def conf = SpringSecurityUtils.securityConfig

        String usernameProperty = conf.rest.token.rendering.usernamePropertyName
        String tokenProperty = conf.rest.token.rendering.tokenPropertyName
        String authoritiesProperty = conf.rest.token.rendering.authoritiesPropertyName

        def result = [:]
        result["id"] = userDetails.id
        result["$usernameProperty"] = userDetails.username
        result["$tokenProperty"] = restAuthenticationToken.tokenValue
        result["$authoritiesProperty"] = userDetails.authorities.collect {GrantedAuthority role -> role.authority }

        if (userDetails instanceof OauthUser) {
            CommonProfile profile = (userDetails as OauthUser).userProfile
            result.with {
                email = profile.email
                displayName = profile.displayName
            }
        }

        def model = [success:true, status : 200, msg:'Successfully logged in', model:result];
        def jsonResult = model as JSON

        log.debug "Generated JSON:\n ${jsonResult.toString(true)}"

        return jsonResult.toString()

    }
            

}
