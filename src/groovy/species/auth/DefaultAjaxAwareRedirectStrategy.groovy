package species.auth

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.util.UrlUtils;
import grails.plugin.springsecurity.ReflectionUtils;

import species.utils.Utils;


class DefaultAjaxAwareRedirectStrategy extends DefaultRedirectStrategy {
	
    private boolean contextRelative;

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
		String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
		String ajaxHeaderName = (String)ReflectionUtils.getConfigProperty("ajaxHeader");
		
        def ajaxHeader = request.getHeader(ajaxHeaderName)
		if(ajaxHeader && ajaxHeader != '') { //  && request.getMethod() == 'POST' .. request is the currentRequest & not the savedRequest
			logger.debug ("Request is an ajax request. Ading extra parameter ajax_login_error to handle")
			if(url.lastIndexOf('?') != -1) {
				redirectUrl += "&ajax_login_error=1"
			} else {
				redirectUrl += "?ajax_login_error=1"
			}
		}
		redirectUrl = response.encodeRedirectURL(redirectUrl);

		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting to '" + redirectUrl + "'");
		}

		response.sendRedirect(redirectUrl);
    }
	
	private String calculateRedirectUrl(String contextPath, String url) {
        
        println "+++++++++++++++++++++++++++++++++++++++"
        println url
        url =  java.net.URLDecoder.decode(url, "UTF-8"); 
		if (!UrlUtils.isAbsoluteUrl(url)) {
			if (contextRelative) {
				return url;
			} else {
				return contextPath + url;
			} 
		}

		// Full URL, including http(s)://
println contextRelative;
		if (!contextRelative) {
			return url;
		}

		// Calculate the relative URL from the fully qualified URL, minus the scheme and base context.
		url = url.substring(url.indexOf("://") + 3); // strip off scheme
		url = url.substring(url.indexOf(contextPath) + contextPath.length());

		if (url.length() > 1 && url.charAt(0) == '/') {
			url = url.substring(1);
		}

		return url;
	}
	
	/**
	* If <tt>true</tt>, causes any redirection URLs to be calculated minus the protocol
	* and context path (defaults to <tt>false</tt>).
	*/
   public void setContextRelative(boolean useRelativeContext) {
	   this.contextRelative = useRelativeContext;
	   super.setContextRelative(this.contextRelative);
   }

}
