package species.auth



class OpenID {

	String url

	static belongsTo = [user: SUser]

	static constraints = {
		url unique: true
	}
}
