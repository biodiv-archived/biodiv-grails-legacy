package species.groups

import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;

class SpeciesGroupMapping {

	String taxonName;
	int rank;
	TaxonomyDefinition taxonConcept;
	
	static belongsTo = [speciesGroup : SpeciesGroup];
	
	static constraints = {
		taxonName(blank:false, unique:['rank']);
		taxonConcept(nullable:true);
	}
	
	static mapping = {
		version  false;
	}
}
