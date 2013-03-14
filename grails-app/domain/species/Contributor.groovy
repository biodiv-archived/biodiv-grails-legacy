package species

class Contributor {

	String name;
		
    static constraints = {
		name (blank : false, unique:true);
    }
	
	static mapping = {
		name type:"text";
		version false;
	}
	
}
