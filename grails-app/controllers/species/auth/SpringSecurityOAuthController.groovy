/*
 * Copyright 2012 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package species.auth

import grails.plugin.springsecurity.oauth.OAuthToken
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import species.auth.SUser
import species.auth.Role
import species.auth.SUserRole
import species.auth.OAuthID
import species.utils.Utils;
import org.scribe.model.Token;
import grails.converters.JSON;
import org.grails.plugin.springsecurity.oauth.GoogleApi20;
/**
 * Simple helper controller for handling OAuth authentication and integrating it
 * into Spring Security.
 */
class SpringSecurityOAuthController {

    public static final String SPRING_SECURITY_OAUTH_TOKEN = 'springSecurityOAuthToken'

    def grailsApplication
    def oauthService
    def springSecurityService
    def googleSpringSecurityOAuthService;

    /**
     * This can be used as a callback for a successful OAuth authentication
     * attempt. It logs the associated user in if he or she has an internal
     * Spring Security account and redirects to <tt>targetUri</tt> (provided as a URL
     * parameter or in the session). Otherwise it redirects to a URL for
     * linking OAuth identities to Spring Security accounts. The application must implement
     * the page and provide the associated URL via the <tt>oauth.registration.askToLinkOrCreateAccountUri</tt>
     * configuration setting.
     */
    def onSuccess = {
        // Validate the 'provider' URL. Any errors here are either misconfiguration
        // or web crawlers (or malicious users).
        if (!params.provider) {
            renderError 400, "The Spring Security OAuth callback URL must include the 'provider' URL parameter."
            return
        }

            def requestCache = new HttpSessionRequestCache();
		    def defaultSavedRequest = requestCache.getRequest(request, response);
			//def defaultSavedRequest = request.getSession()?.getAttribute(WebAttributes.SAVED_REQUEST)

        OAuthToken oAuthToken;
        boolean isAjaxRequest = request.xhr;
        if(isAjaxRequest) {
            Token accessToken = new GoogleApi20().getAccessTokenExtractor().extract(params.response);
            oAuthToken = googleSpringSecurityOAuthService.createAuthToken(accessToken)
            oAuthToken = createAuthToken2(params.provider, oAuthToken);
        } else {
            def sessionKey = oauthService.findSessionKeyForAccessToken(params.provider)
            if (!session[sessionKey]) {
                renderError 500, "No OAuth token in the session for provider '${params.provider}'!"
                return
            }
            
            // Create the relevant authentication token and attempt to log in.
            oAuthToken = createAuthToken(params.provider, session[sessionKey])
        }
        if (oAuthToken.principal instanceof GrailsUser || isAjaxRequest) { //isAjaxRequest is a hack
            if(isAjaxRequest) {
                SecurityContextHolder.context.authentication = oAuthToken
                if(defaultSavedRequest) {
                    (new DefaultAjaxAwareRedirectStrategy()).sendRedirect(request, response, defaultSavedRequest.getRedirectUrl());
                    return
                } else { 
                    render ([success:true] as JSON)
                    return;
                }
            } else {
                authenticateAndRedirect(oAuthToken, defaultTargetUrl)
            }
        } else {
            // This OAuth account hasn't been registered against an internal
            // account yet. Give the oAuthID the opportunity to create a new
            // internal account or link to an existing one.
            session[SPRING_SECURITY_OAUTH_TOKEN] = oAuthToken

            def redirectUrl = SpringSecurityUtils.securityConfig.oauth.registration.askToLinkOrCreateAccountUri
            assert redirectUrl, "grails.plugin.springsecurity.oauth.registration.askToLinkOrCreateAccountUri" +
                    " configuration option must be set!"
            log.debug "Redirecting to askToLinkOrCreateAccountUri: ${redirectUrl}"
            redirect(redirectUrl instanceof Map ? redirectUrl : [uri: redirectUrl])
        }
    }

    def onFailure = {
        authenticateAndRedirect(null, defaultTargetUrl)
    }

    def askToLinkOrCreateAccount = {
        if (springSecurityService.loggedIn) {
            def currentUser = springSecurityService.currentUser
            OAuthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
            assert oAuthToken, "There is no auth token in the session!"
            currentUser.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: currentUser)
            if (currentUser.validate() && currentUser.save()) {
                oAuthToken = updateOAuthToken(oAuthToken, currentUser)
                authenticateAndRedirect(oAuthToken, defaultTargetUrl)
                return
            }
        }
    }

    /**
     * Associates an OAuthID with an existing account. Needs the user's password to ensure
     * that the user owns that account, and authenticates to verify before linking.
     */
    def linkAccount = { OAuthLinkAccountCommand command ->
        OAuthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        assert oAuthToken, "There is no auth token in the session!"

        if (request.post) {
            boolean linked = command.validate() && SUser.withTransaction { status ->
                SUser user = SUser.findByEmailAndPassword(
                        command.email, springSecurityService.encodePassword(command.password))
                if (user) {
                    user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)
                    if (user.validate() && user.save()) {
                        oAuthToken = updateOAuthToken(oAuthToken, user)
                        return true
                    }
                } else {
                    command.errors.rejectValue("email", "OAuthLinkAccountCommand.email.not.exists")
                }

                status.setRollbackOnly()
                return false
            }

            if (linked) {
                authenticateAndRedirect(oAuthToken, defaultTargetUrl)
                return
            }
        }

        render view: 'askToLinkOrCreateAccount', model: [linkAccountCommand: command]
        return
    }

    def createAccount = { OAuthCreateAccountCommand command ->
        OAuthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        assert oAuthToken, "There is no auth token in the session!"

        if (request.post) {
            if (!springSecurityService.loggedIn) {
                def config = SpringSecurityUtils.securityConfig

                boolean created = command.validate() && SUser.withTransaction { status ->
                    SUser user = new SUser(email: command.email, password: command.password1, enabled: true)
                    user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)

                    // updateUser(user, oAuthToken)

                    if (!user.validate() || !user.save()) {
                        status.setRollbackOnly()
                        return false
                    }

                    for (roleName in config.oauth.registration.roleNames) {
                        SUserRole.create user, Role.findByAuthority(roleName)
                    }

                    oAuthToken = updateOAuthToken(oAuthToken, user)
                    return true
                }

                if (created) {
                    authenticateAndRedirect(oAuthToken, defaultTargetUrl)
                    return
                }
            }
        }

        render view: 'askToLinkOrCreateAccount', model: [createAccountCommand: command]
    }

    // utils

    protected renderError(code, msg) {
        log.error msg + " (returning ${code})"
        render status: code, text: msg
    }

    protected OAuthToken createAuthToken(providerName, scribeToken) {
        def providerService = grailsApplication.mainContext.getBean("${providerName}SpringSecurityOAuthService")
        OAuthToken oAuthToken = providerService.createAuthToken(scribeToken)
        return createAuthToken2(providerName, oAuthToken);
    }

    protected OAuthToken createAuthToken2(providerName, OAuthToken oAuthToken) {
        def oAuthID = OAuthID.findByProviderAndAccessToken(oAuthToken.providerName, oAuthToken.socialId)
        if (oAuthID) {
            updateOAuthToken(oAuthToken, oAuthID.user)
        }

        return oAuthToken
    }

    protected OAuthToken updateOAuthToken(OAuthToken oAuthToken, SUser user) {
        def conf = SpringSecurityUtils.securityConfig

        // user

        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."${usernamePropertyName}"
        String password = user."${passwordPropertyName}"
        boolean enabled = enabledPropertyName ? user."${enabledPropertyName}" : true
        boolean accountExpired = accountExpiredPropertyName ? user."${accountExpiredPropertyName}" : false
        boolean accountLocked = accountLockedPropertyName ? user."${accountLockedPropertyName}" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."${passwordExpiredPropertyName}" : false

        // authorities

        String authoritiesPropertyName = conf.userLookup.authoritiesPropertyName
        String authorityPropertyName = conf.authority.nameField
        Collection<?> userAuthorities = user."${authoritiesPropertyName}"
        def authorities = userAuthorities.collect { new GrantedAuthorityImpl(it."${authorityPropertyName}") }

        oAuthToken.principal = new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
                !accountLocked, authorities ?: [GormUserDetailsService.NO_ROLE], user.id)
        oAuthToken.authorities = authorities
        oAuthToken.authenticated = true

        return oAuthToken
    }

/*
    private def updateUser(SUser user, OAuthToken oAuthToken) {
        if (!user.validate()) {
            return
        }

        if (oAuthToken instanceof TwitterOAuthToken) {
            TwitterOAuthToken twitterOAuthToken = (TwitterOAuthToken) oAuthToken

            if (!user.username) {
                user.username = twitterOAuthToken.twitterProfile.screenName
                if (!user.validate()) {
                    user.username = null
                }
            }

            if (!user.firstName || !user.lastName) {
                def names = twitterOAuthToken.twitterProfile.name?.split(' ')
                if (names) {
                    if (!user.lastName) {
                        user.lastName = names[0]
                        if (!user.validate()) {
                            user.lastName = null
                        }
                    }

                    if (!user.firstName) {
                        user.firstName = names[-1]
                        if (!user.validate()) {
                            user.firstName = null
                        }
                    }
                }
            }
        } else if (oAuthToken instanceof FacebookOAuthToken) {
            FacebookOAuthToken facebookOAuthToken = (FacebookOAuthToken) oAuthToken

            if (!user.username) {
                user.username = facebookOAuthToken.facebookProfile.username
                if (!user.validate()) {
                    user.username = null
                }
            }

            if (!user.email) {
                user.email = facebookOAuthToken.facebookProfile.email
                if (!user.validate()) {
                    user.email = null
                }
            }

            if (!user.lastName) {
                user.lastName = facebookOAuthToken.facebookProfile.lastName
                if (!user.validate()) {
                    user.lastName = null
                }
            }

            if (!user.firstName) {
                user.firstName = facebookOAuthToken.facebookProfile.firstName
                if (!user.validate()) {
                    user.firstName = null
                }
            }
        } else if (oAuthToken instanceof GoogleOAuthToken) {
            GoogleOAuthToken googleOAuthToken = (GoogleOAuthToken) oAuthToken

            if (!user.email) {
                user.email = googleOAuthToken.email
                if (!user.validate()) {
                    user.email = null
                }
            }
        } else if (oAuthToken instanceof YahooOAuthToken) {
            YahooOAuthToken yahooOAuthToken = (YahooOAuthToken) oAuthToken

            if (!user.username) {
                user.username = yahooOAuthToken.profile.nickname
                if (!user.validate()) {
                    user.username = null
                }
            }

            if (!user.lastName) {
                user.lastName = yahooOAuthToken.profile.familyName
                if (!user.validate()) {
                    user.lastName = null
                }
            }

            if (!user.firstName) {
                user.firstName = yahooOAuthToken.profile.givenName
                if (!user.validate()) {
                    user.firstName = null
                }
            }
        }
    }
*/

    protected Map getDefaultTargetUrl() {
        def config = SpringSecurityUtils.securityConfig
        def savedRequest = SpringSecurityUtils.getSavedRequest(session)
        def defaultUrlOnNull = '/'

        def dturl;
        if (savedRequest && !config.successHandler.alwaysUseDefault) {
            dturl = [url: (savedRequest.redirectUrl ?: defaultUrlOnNull)]
        } else {
            dturl = [uri: (config.successHandler.defaultTargetUrl ?: defaultUrlOnNull)]
        }
        return dturl;
    }

    protected void authenticateAndRedirect(OAuthToken oAuthToken, redirectUrl) {
        session.removeAttribute SPRING_SECURITY_OAUTH_TOKEN

        SecurityContextHolder.context.authentication = oAuthToken
        redirect(redirectUrl instanceof Map ? redirectUrl : [uri: redirectUrl])
    }

}

class OAuthCreateAccountCommand {

    String email
    String password1
    String password2

    static constraints = {
        email blank: false, validator: { String email, command ->
            SUser.withNewSession { session ->
                if (email && SUser.countByEmail(email)) {
                    return 'OAuthCreateAccountCommand.email.error.unique'
                }
            }
        }
        password1 blank: false, minSize: 8, maxSize: 64, validator: { password1, command ->
            if (command.email && command.email.equals(password1)) {
                return 'OAuthCreateAccountCommand.password.error.email'
            }

            if (password1 && password1.length() >= 8 && password1.length() <= 64 &&
                    (!password1.matches('^.*\\p{Alpha}.*$') ||
                     !password1.matches('^.*\\p{Digit}.*$') ||
                     !password1.matches('^.*[!@#$%^&].*$'))) {
                return 'OAuthCreateAccountCommand.password.error.strength'
            }
        }
        password2 nullable: true, blank: true, validator: { password2, command ->
            if (command.password1 != password2) {
                return 'OAuthCreateAccountCommand.password.error.mismatch'
            }
        }
    }
}

class OAuthLinkAccountCommand {

    String email
    String password

    static constraints = {
        email blank: false
        password blank: false
    }

}
