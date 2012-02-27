package species.auth.drupal

import grails.converters.JSON

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException

import species.utils.Utils;


class DrupalAuthUtils {

	private static def log = Logger.getLogger(this)

	String apiKey
	String secret
	String applicationId

	DrupalAuthToken build(request) {
		Map params = [:];
		Utils.populateHttpServletRequestParams(request, params);
		
		log.debug "Params : "+params;
		if (params.uid == null) {
			throw new BadCredentialsException("Uid cannot be null");
		}

		DrupalAuthToken authRequest = new DrupalAuthToken (
		uid: Long.parseLong(params.uid),
		username:params.name,
		code: params.code
		);
		return authRequest;
		/*if (!signedRequest) {
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
		 if (!verifySign(signedRequestParts[0], signedRequestParts[1])) {
		 throw new BadCredentialsException("Invalid signature")
		 } else {
		 log.debug "Signature is ok"
		 }
		 String code = json.code?.toString()
		 DrupalAuthToken token = new DrupalAuthToken (
		 uid: Long.parseLong(json.user_id.toString()),
		 code: code
		 )
		 token.authenticated = true
		 return token
		 */
	}

	public Cookie getAuthCookie(HttpServletRequest request) {
		//String cookieName = "drsr_" + applicationId
		return request.cookies.find { Cookie it ->
			//DrupalAuthUtils.log.debug("Cookie $it.name, expected $cookieName")
			return it.name.startsWith('SESS');
		}
	}



}
