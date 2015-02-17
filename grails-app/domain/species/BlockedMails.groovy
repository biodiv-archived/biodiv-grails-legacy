package species

class BlockedMails {
	String email;
	
    static constraints = {
		email email: true, blank: false, unique: true, nullable:false
    }
	static mapping = {
        version: false;
    }
}
