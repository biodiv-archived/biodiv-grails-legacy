package species

class Reference {

	String title;
	String url;

	static belongsTo = [speciesField:SpeciesField];
		
    static constraints = {
		url(url:true, nullable:true);
		title(nullable:true);
    }
	
	static mapping = {
		title type:"text"
	}
}
