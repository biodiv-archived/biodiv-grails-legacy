package species.trait;
import species.Language;

class TraitTranslation {
	String description;
	String name;
	String source;
	Language language;
	Trait trait;


	static constraints = {
		description(nullable:true);
		name(nullable:true);
		source(nullable:true);
		language nullable:false;
		trait nullable:false;		
	}

	static mapping = {
	   description type: 'text'
	}
}