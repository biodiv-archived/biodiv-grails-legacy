package species

import species.participation.Checklist;

class Reference {

	String title;
	String url;

	static belongsTo = [speciesField:SpeciesField, checklist:Checklist];
		
    static constraints = {
		url(url:true, nullable:true);
		title(nullable:true);
    }
	
	static mapping = {
		title type:"text"
	}
}
