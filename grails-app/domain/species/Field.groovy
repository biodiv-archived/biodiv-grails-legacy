package species
import species.Language;

class Field {

	String concept;
	String category;
	String subCategory;
	String description;
	int displayOrder;
	String urlIdentifier
	// Language
    Language language;
    int connection;

	static mapping = {
		description type:'text';
		version : false;
	}
	
    static constraints = {
    	language (nullable:false);
		concept (blank : false);
		category(nullable:true);
		subCategory(nullable:true);
		description(nullable:true);
		description (blank : true);
		urlIdentifier(blank : true, nullable:true);
    }

	@Override
    String toString() {
        return concept+" > "+category+(subCategory?" > "+subCategory:'');
    }
}
