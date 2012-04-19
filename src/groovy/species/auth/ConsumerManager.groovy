package species.auth

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
	   return super.verifyReturnTo(receivingUrl, response);
   }
}
