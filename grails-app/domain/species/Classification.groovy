package species

class Classification {

	String name;
    
	static constraints = {
		name(blank:false, unique:true);
    }
	
	static mapping = {
		version : false;
	}
}
