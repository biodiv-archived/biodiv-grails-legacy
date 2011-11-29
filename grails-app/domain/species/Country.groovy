package species

class Country {

	String twoLetterCode;
	String countryName;
	
	
	static constraints = {
		twoLetterCode(blank:false, unique : true);
		countryName(blank:false);
	}
	
	static mapping = {
		version false;
		sort 'countryName';
	}
    
}
