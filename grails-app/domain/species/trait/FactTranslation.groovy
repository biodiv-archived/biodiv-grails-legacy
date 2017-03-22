package species.trait;
import species.Language;

class FactTranslation {
	
	Language language;
	Fact fact;
	String toValue;
	String attribution;


	static constraints = {
		language nullable:false;
		fact nullable:false;
		toValue(nullable:true);
		attribution(nullable:true);				
	}
}