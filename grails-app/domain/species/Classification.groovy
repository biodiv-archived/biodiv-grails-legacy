package species

class Classification {

	String name;
    String citation;
	
	static constraints = {
		name(blank:false, unique:true);
		citation(nullable:true);
    }
	
	static mapping = {
		version : false;
	}
}
