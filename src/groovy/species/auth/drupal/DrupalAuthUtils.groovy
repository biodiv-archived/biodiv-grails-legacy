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

	DrupalAuthToken build(request) {
		Map params = [:];
		Utils.populateHttpServletRequestParams(request, params);
		
		log.debug "Params : "+params;
		
		if (params.uid == null) {
			throw new BadCredentialsException("Uid cannot be null");
		}

		DrupalAuthToken authRequest = new DrupalAuthToken (
			Long.parseLong(params.uid),
			params.j_username,
			params.j_username
		);
		return authRequest;
	}

	public Cookie getAuthCookie(HttpServletRequest request) {
		//String cookieName = "drsr_" + applicationId
		return request.cookies.find { Cookie it ->
			//DrupalAuthUtils.log.debug("Cookie $it.name, expected $cookieName")
			return it.name.startsWith('SESS');
		}
	}

}
