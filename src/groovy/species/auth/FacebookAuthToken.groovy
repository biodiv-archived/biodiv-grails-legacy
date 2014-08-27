package species.auth


class FacebookAuthToken extends com.the6hours.grails.springsecurity.facebook.FacebookAuthToken {
		
		String domain;
	    def user;

        FacebookAuthToken (com.the6hours.grails.springsecurity.facebook.FacebookAuthToken token) {
            this.uid = token.uid
            this.accessToken = token.accessToken
            this.code = token.code
            this.redirectUri = token.redirectUri
            this.principal = token.principal
            this.authorities = token.authorities
        }

		@Override
		String toString() {
			return "Domain: $domain, Principal: $principal, uid: $uid, roles: ${authorities.collect { it.authority}}"
		}
}
