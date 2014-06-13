package species

import java.util.List;

class TaxonomyRegistry extends NamesSorucedata {

	TaxonomyDefinition taxonDefinition;
	TaxonomyRegistry parentTaxon;
	String path;
	Classification classification;

	static constraints = {
		parentTaxon(nullable:true);
		taxonDefinition(unique:['classification', 'path'])
	}

    static mapping = {
    }
}
