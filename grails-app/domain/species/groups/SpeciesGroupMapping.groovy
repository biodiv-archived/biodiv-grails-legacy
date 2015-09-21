package species.groups

import species.TaxonomyDefinition

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
		speciesGroup lazy: false
	}
}
