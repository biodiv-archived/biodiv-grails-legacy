package species.auth

class FacebookAuthToken extends com.the6hours.grails.springsecurity.facebook.FacebookAuthToken {
		
		String domain;
		
		@Override
		String toString() {
			return "Domain: $domain, Principal: $principal, uid: $uid, roles: ${authorities.collect { it.authority}}"
		}
}
