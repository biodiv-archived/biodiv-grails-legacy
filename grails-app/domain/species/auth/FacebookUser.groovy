package species.auth

import species.auth.SUser

class FacebookUser {

	long uid
    String accessToken

	static belongsTo = [user: SUser]

	static constraints = {
		uid unique: true
	}
	
}
