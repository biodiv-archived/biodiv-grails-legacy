package species

class Classification {

	String name;
    String citation;
    Language language;

	static constraints = {
		name(blank:false, unique:true);
		citation(nullable:true);
		language(nullable:false);
    }
	
	static mapping = {
		version : false;
		sort name:"asc"
	}
}
