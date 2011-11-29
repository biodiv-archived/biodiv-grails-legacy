package species.participation

import species.TaxonomyDefinition;

class Recommendation {
	
	String name;
	TaxonomyDefinition taxonConcept;
	Date lastModified = new Date();

	static constraints = {
		name(blank:false, unique:['taxonConcept']);
		taxonConcept nullable:true;
	}

	static mapping = { 
		version false; 
	}
	
	
}
