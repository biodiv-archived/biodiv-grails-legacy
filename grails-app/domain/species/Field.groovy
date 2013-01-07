package species

class Field {

	String concept;
	String category;
	String subCategory;
	String description;
	int displayOrder;
	String urlIdentifier
	
	static mapping = {
		description type:'text';
		version : false;
	}
	
    static constraints = {
		concept (blank : false);
		category(nullable:true);
		subCategory(nullable:true);
		description(nullable:true);
		description (blank : true);
		urlIdentifier(blank : true, nullable:true);
    }
}
