package species.trait;
import species.Language;

class TraitValueTranslation {
	String description;
	String value;
	String source;
	Language language;
	TraitValue traitValue;


	static constraints = {
		description(nullable:true);
		value(nullable:true);
		source(nullable:true);
		language nullable:false;
		traitValue nullable:false;		
	}

	static mapping = {
	   description type: 'text'
	}
}