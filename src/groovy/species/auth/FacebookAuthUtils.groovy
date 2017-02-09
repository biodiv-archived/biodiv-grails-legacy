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
import static groovyx.net.http.ContentType.TEXT
import java.util.concurrent.TimeUnit
import org.codehaus.groovy.grails.web.json.JSONException
import com.the6hours.grails.springsecurity.facebook.FacebookAccessToken


class FacebookAuthUtils extends com.the6hours.grails.springsecurity.facebook.FacebookAuthUtils {

	private static def log = Logger.getLogger(this)

	def grailsApplication;
	
    //HACK required to populate user in facebookauthtoken... for new user registration
    FacebookAuthToken build(String signedRequest) {
        com.the6hours.grails.springsecurity.facebook.FacebookAuthToken t = super.build(signedRequest);
        FacebookAuthToken token = new FacebookAuthToken(t);
        return token;
    }
/*
	FacebookAuthToken build(HttpServletRequest request, String signedRequest) {
		if (!signedRequest) {
			return null
		}
		String[] signedRequestParts = signedRequest.split('\\.')
		if (signedRequestParts.length != 2) {
			throw new BadCredentialsException("Invalid Signed Request")
		}

		String jsonData = new String(Base64.decodeBase64(signedRequestParts[1].getBytes()), 'UTF-8')
		def json = JSON.parse(jsonData)

		if (json.algorithm != 'HMAC-SHA256') {
			throw new BadCredentialsException("Unknown hashing algorightm: $json.algorithm")
		}

		//log.debug("Payload: $jsonData")

		String secret = getFacebookAppSecretForDomain(Utils.getDomain(request));

		if (!verifySign(signedRequestParts[0], signedRequestParts[1], secret)) {
			throw new BadCredentialsException("Invalid signature")
		} else {
			//log.debug "Signature is ok"
		}

		String code = json.code?.toString()

		FacebookAuthToken token = new FacebookAuthToken(
				uid: Long.parseLong(json.user_id.toString()),
				code: code,
				domain:Utils.getDomain(request)
				);
		token.authenticated = true
		return token
	}

	public Cookie getAuthCookie(HttpServletRequest request) {
		String applicationId = getFacebookAppIdForDomain(request);
		String cookieName = "fbsr_" + applicationId
		log.debug "looking for cookie named $cookieName";
		return request.cookies.find { Cookie it ->
			//log.debug("Cookie $it.name, expected $cookieName")
			return it.name == cookieName
		}
	}
*/
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
            String responseStr;
            new HTTPBuilder(authUrl).request(GET, TEXT) { req ->

                response.success = { resp, reader ->
                    log.debug "$authUrl request was successful"
                    responseStr = reader.text;
                }

                response.failure = { resp ->
                    log.debug "$authUrl request failed"
                    throw new IOException(resp.getData().toString());
                }
            }

            String response = responseStr?:'';
            println "AccessToken response: $response"
            Map data = [:]
            response.split('&').each {
                String[] kv = it.split('=')
                if (kv.length != 2) {
                    log.warn("Invalid response part: $it")
                } else {
                    data[kv[0]] = kv[1]
                }
            }
            FacebookAccessToken token = new FacebookAccessToken()
            if (data.access_token) {
                token.accessToken = data.access_token
            } else {
                log.error("No access_token in response: $response")
            }
            if (data.expires) {
                if (data.expires =~ /^\d+$/) {
                    token.expireAt = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.parseLong(data.expires)))
                } else {
                    log.warn("Invalid 'expires' value: $data.expires")
                }
            } else {
              log.error("No expires in response: $response")
            }
            //log.debug("Got AccessToken: $token")
            return token
        } catch (IOException e) {
            log.error("Can't read data from Facebook", e)
            return null
        }
    }


/*
	String getAccessToken(String applicationId, String secret, String code) {
		try {
			String authUrl = "https://graph.facebook.com/oauth/access_token?client_id=$applicationId&redirect_uri=&client_secret=$secret&code=$code"
			URL url = new URL(authUrl)
			HttpURLConnection httpConn = (HttpURLConnection)url.openConnection()
			InputStream is;
			if (httpConn.getResponseCode() >= 400) {
				is = httpConn.getErrorStream();
				List lines = is.readLines();
				log.error "Error reading from facebook : ${lines} for url ${authUrl}"
				return null;
			} else {
				is = httpConn.getInputStream();
				List lines = is.readLines();
				log.debug "Access token lines ${lines} for url ${authUrl}"
				return lines.first().split('&').first().split('=')[1]
			}
			
			
		} catch (IOException e) {
			log.error("Can't read data from Facebook", e)
			return null
		}
	}

	public boolean verifySign(String sign, String payload, String secret) {
		String signer = 'HMACSHA256'

		try {
            //log.debug("Secret $secret")
            SecretKeySpec sks = new SecretKeySpec(secret?.getBytes(), signer)
            //log.debug("Payload1: `$payload`")
            payload = payload.replaceAll("-", "+").replaceAll("_", "/").trim()
            //log.debug("Payload2: `$payload`")
            sign = sign.replaceAll("-", "+").replaceAll("_", "/")

			Mac mac = Mac.getInstance(signer)
			mac.init(sks)
			byte[] my = mac.doFinal(payload.getBytes('UTF-8'))
			byte[] their = Base64.decodeBase64(sign.getBytes('UTF-8'))
			//log.info("My: ${new String(Base64.encodeBase64(my, false))}, their: ${new String(Base64.encodeBase64(their))} / $sign")
			return Arrays.equals(my, their)
		} catch (Exception e) {
			log.error("Can't validate signature", e);
			return false;
		}
	}

	String getFacebookAppIdForDomain(HttpServletRequest request) {
		return getFacebookAppIdForDomain(Utils.getDomain(request));
	}

	String getFacebookAppIdForDomain(String domain) {
		if(!domain) return;

		if(domain.equals(grailsApplication.config.wgp.domain)) {
			return grailsApplication.config.speciesPortal.wgp.facebook.appId;
		} else {
			return grailsApplication.config.speciesPortal.ibp.facebook.appId;
		}
		return null;
	}

	String getFacebookAppSecretForDomain(String domain) {
		if(!domain) return;

		if(domain.equals(grailsApplication.config.wgp.domain)) {
			return grailsApplication.config.speciesPortal.wgp.facebook.secret
		} else {
			return grailsApplication.config.speciesPortal.ibp.facebook.secret
		}
		return null;
	}
*/
	void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
/*		log.info("Cleanup Facebook cookies")
		Cookie cookie = this.getAuthCookie(httpServletRequest)
		if (cookie != null) {
			cookie.maxAge = 0
			cookie.path = '/'
			cookie.domain = "."+Utils.getIBPServerCookieDomain();
			httpServletResponse.addCookie(cookie)
		}
*/
		Cookie cookie2 = this.getFBLoginCookie(httpServletRequest)
		if (cookie2 != null) {
			cookie2.maxAge = 0
			cookie2.path = '/'
			cookie2.domain = "."+Utils.getIBPServerCookieDomain();
			httpServletResponse.addCookie(cookie2)
		}
	}

}

