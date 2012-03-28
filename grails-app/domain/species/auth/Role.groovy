package species.auth

class Role {

	String authority

	static mapping = {
		cache true
		version:false;
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
