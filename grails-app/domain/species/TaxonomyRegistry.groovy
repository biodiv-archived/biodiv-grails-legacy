package species

class TaxonomyRegistry {

	TaxonomyDefinition taxonDefinition;
	TaxonomyRegistry parentTaxon;
	String path;
	Classification classification;

	//static belongsTo = [species:Species];
	
	static constraints = {
		parentTaxon(nullable:true);
		//species(nullable:true);
	}

	static mappings = {
		version : false
		taxonDefinition(unique:['classification', 'path'])
	}
	
}
