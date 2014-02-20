package species

import species.auth.SUser

class Contributor {

	String name;
	SUser user;
		
    static constraints = {
		name (blank : false, unique:true);
		user (nullable :true)
    }
	
	static mapping = {
		name type:"text";
		version false;
	}
	
}
