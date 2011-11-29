package species

class Language {
	String threeLetterCode;
	String twoLetterCode;
	String name;
	
    static constraints = {
		threeLetterCode(blank:false, nullable:false, unique:true);
		twoLetterCode(nullable:true);
		name(blank:false, nullable:false);
    }
	
	static mapping = {
		version false;
		sort 'name';
	}
}
