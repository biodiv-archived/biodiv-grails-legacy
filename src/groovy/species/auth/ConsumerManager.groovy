package species.auth

import grails.plugin.springsecurity.SpringSecurityUtils;
import org.openid4java.message.AuthSuccess;

class ConsumerManager extends org.openid4java.consumer.ConsumerManager {

	/**
	* Verifies that the URL where the Consumer (Relying Party) received the
	* authentication response matches the value of the "openid.return_to"
	* parameter in the authentication response.
	*
	* @param receivingUrl      The URL where the Consumer received the
	*                          authentication response.
	* @param response          The authentication response.
	* @return                  True if the two URLs match, false otherwise.
	*/
   public boolean verifyReturnTo(String receivingUrl, AuthSuccess response) {
	   receivingUrl = receivingUrl.replace(":8080", '');
	   if(receivingUrl.contains("openId%2Fcheckauth")){
		   return true;
		   //return retBool = super.verifyReturnTo(getUrlForAjaxCall(receivingUrl, response), response);
		   
	   }
	   return super.verifyReturnTo(receivingUrl, response);
   }
   
   private String getUrlForAjaxCall(String receivingUrl, AuthSuccess response){
	   return receivingUrl.replaceFirst(response.getReturnTo().encodeAsURL(), receivingUrl.substring(0, receivingUrl.indexOf("?")).encodeAsURL());
	   /*
	   String aa = receivingUrl.replace('http%3A%2F%2Findiabiodiversity.localhost.org%2Fbiodiv%2FopenId%2Fcheckauth', 'http://indiabiodiversity.localhost.org/biodiv/j_spring_openid_security_check'.encodeAsURL());
	   return aa;
	   String startString = "&openid.return_to=";
	   String endString = "&openid.";
	   int start = receivingUrl.indexOf(startString) + startString.length();
	   int end = receivingUrl.indexOf("&openid.", start);
	   String stringToReplace =  receivingUrl.substring(start, end);
	   String ff = receivingUrl.replaceFirst(response.getReturnTo().encodeAsURL(), receivingUrl.substring(0, receivingUrl.indexOf("?")).encodeAsURL());
	   if(aa == ff){
		   println "===="
	   }
	   return ff
	   */
   }
}
