package species

class TaxonomyRegistry {

	TaxonomyDefinition taxonDefinition;
	TaxonomyRegistry parentTaxon;
	String path;
	Classification classification;

	static constraints = {
		parentTaxon(nullable:true);
		taxonDefinition(unique:['classification', 'path'])
	}

	static mappings = {
		version : false
	}
	
}
