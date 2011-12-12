package species.participation

import species.TaxonomyDefinition;
import species.auth.Role;
import species.utils.Utils;

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

	void setName(String name) {
		this.name = Utils.cleanName(name);
	}	
	
}
