package species.auth

import javax.servlet.http.HttpServletRequest;

class OpenIDAuthenticationFilter extends
		org.springframework.security.openid.OpenIDAuthenticationFilter {

			def grailsApplication;
			
			/**
			* Builds the <tt>return_to</tt> URL that will be sent to the OpenID service provider.
			* By default returns the URL of the current request.
			*
			* @param request the current request which is being processed by this filter
			* @return The <tt>return_to</tt> URL.
			*/
		   protected String buildReturnToUrl(HttpServletRequest request) {
			   String return_to = super.buildReturnToUrl(request)
			   return_to = return_to.replace(":8080", '');
			   return return_to;
		   }
}
