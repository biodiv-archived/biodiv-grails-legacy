package species.auth


class FacebookUser {

	long uid
	String accessToken
	boolean isFirstLogin = true;
	

	static belongsTo = [user: SUser]

	static constraints = { 
		uid unique: true
	 
	}
}
