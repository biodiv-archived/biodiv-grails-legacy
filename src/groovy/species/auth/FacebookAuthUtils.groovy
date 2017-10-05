package species.auth

import species.utils.Utils;
import org.apache.log4j.Logger
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.apache.commons.codec.binary.Base64
import org.springframework.security.authentication.BadCredentialsException
import grails.converters.JSON
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import species.utils.Utils;
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentEncoding.Type.GZIP;
import java.util.concurrent.TimeUnit
import org.codehaus.groovy.grails.web.json.JSONException
import com.the6hours.grails.springsecurity.facebook.FacebookAccessToken
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import species.auth.JwtAuthToken;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;

class FacebookAuthUtils extends com.the6hours.grails.springsecurity.facebook.FacebookAuthUtils {

	private static def log = Logger.getLogger(this);

	def grailsApplication;
    def userDetailsService;

    /*private static final String AUTH_URL = "https://www.facebook.com/dialog/oauth"
    private static final String API_URL = "https://graph.facebook.com"

    static def getLoginUrl() {
        def params = [
                response_type: "code",
                client_id    : apiKey,
                scope        : "email,user_about_me,user_location,user_hometown,user_website"
        ]
        def url = "$AUTH_URL?" + params.collect { k, v -> "$k=$v" }.join('&')
        return url
    }*/


    //HACK required to populate user in facebookauthtoken... for new user registration
    FacebookAuthToken build(String signedRequest) {
        com.the6hours.grails.springsecurity.facebook.FacebookAuthToken t = super.build(signedRequest);
        FacebookAuthToken token = new FacebookAuthToken(t);
        return token;
    }

    public getFBLoginCookie(HttpServletRequest request) {
		String cookieName = "fb_login"
		//log.debug "looking for cookie named $cookieName";
		return request.cookies.find { Cookie it ->
			//log.debug("Cookie $it.name, expected $cookieName")
			return it.name == cookieName
		}
	}

    FacebookAccessToken requestAccessToken(String authUrl) {
        try {
            Map data;
            def httpBuilder = new HTTPBuilder(authUrl);
            httpBuilder.contentEncoding = GZIP

            httpBuilder.request(GET) { req ->

                response.success = { resp, json ->
                    log.debug "$authUrl request was successful"
                    data = json;
                }

                response.failure = { resp ->
                    log.debug "$authUrl request failed"
                    throw new IOException(resp.getData().toString());
                }
            }

            println "AccessToken response: $data"
            /*println responseStr
            Map data = [:]
            response.split('&').each {
                println it;
                String[] kv = it.split('=')
                if (kv.length != 2) {
                    log.warn("Invalid response part: $it")
                } else {
                    data[kv[0]] = kv[1]
                }
            }*/
            FacebookAccessToken token = new FacebookAccessToken()
            if (data.access_token) {
                token.accessToken = data.access_token
            } else {
                log.error("No access_token in response: $data")
            }
            if (data.expires_in) {
                if (data.expires_in =~ /^\d+$/) {
                    token.expireAt = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.parseLong(data.expires_in+'')))
                } else {
                    log.warn("Invalid 'expires' value: $data.expires_in")
                }
            } else {
              log.error("No expires in response: $data")
            }
            //log.debug("Got AccessToken: $token")
            return token
        } catch (IOException e) {
            log.error("Can't read data from Facebook", e)
            return null
        }
    }

    void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Cookie cookie2 = this.getFBLoginCookie(httpServletRequest)
		if (cookie2 != null) {
            cookie2.value = false
			cookie2.maxAge = 0
			cookie2.path = '/'
			cookie2.domain = "."+Utils.getIBPServerCookieDomain();
			httpServletResponse.addCookie(cookie2)
		}
	}

    Cookie getJwtTokenCookie(HttpServletRequest request) {
        String cookieName = "BAToken";
        request.cookies?.find { Cookie it ->
            FacebookAuthUtils.log.debug("Cookie $it.name, expected $cookieName")
            it.name == cookieName
        }
    }
    
    JwtAuthToken buildJwtToken(String jwtToken) {
        return new JwtAuthToken(jwtToken);
    }

    def authenticate(String jwtToken) {
        def authenticator =  new JwtAuthenticator(new org.pac4j.jwt.config.signature.SecretSignatureConfiguration(JWT_SALT));
        CommonProfile profile = authenticator.validateToken(jwtToken);
        println "******************************"
        println "******************************"
        println profile;
        println "******************************"
        println "******************************"
        def token = new JwtAuthToken();
        if(profile) {
            def user = SUser.findByEmail(profile.email);
            token.details = null
            token.principal = userDetailsService.createUserDetails(user);
            token.authenticated = true
            token.authorities = (Collection<GrantedAuthority>)((UserDetails)principal).authorities;
        } else {
            token.authenticated = false
        }
        return token;
    }

    void logoutJwtAuth(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Cookie cookie2 = this.getJwtTokenCookie(httpServletRequest)
		if (cookie2 != null) {
            cookie2.value = false
			cookie2.maxAge = 0
			cookie2.path = '/'
			cookie2.domain = "."+Utils.getIBPServerCookieDomain();
			httpServletResponse.addCookie(cookie2)
		}
	}
}

