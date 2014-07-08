package species.auth


class FacebookUser {

	long uid
	String accessToken
    Date accessTokenExpires

	boolean isFirstLogin = true;
	

	static belongsTo = [user: SUser]

	static constraints = { 
		uid unique: true
	 
	}
	
	static void removeAll(SUser sUser) {
		executeUpdate 'DELETE FROM FacebookUser WHERE user=:sUser', [sUser: sUser]
	}
}
