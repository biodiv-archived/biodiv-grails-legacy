package species.auth

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;

class OpenIDAuthenticationFilter extends
		org.springframework.security.openid.OpenIDAuthenticationFilter {

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
			   def target = request.getParameter(AbstractAuthenticationTargetUrlRequestHandler.DEFAULT_TARGET_PARAMETER);
			   if (StringUtils.hasText(target)) {
				   return_to += "?"+AbstractAuthenticationTargetUrlRequestHandler.DEFAULT_TARGET_PARAMETER+"="+target
			   }
			   return return_to;
		   }
}
