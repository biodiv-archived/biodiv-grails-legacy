package species

import java.util.List;

class TaxonomyRegistry extends NamesSorucedata {
	TaxonomyDefinition taxonDefinition;
	TaxonomyRegistry parentTaxon;
	TaxonomyDefinition parentTaxonDefinition;
	String path;
	Classification classification;

	static constraints = {
		parentTaxon(nullable:true);
		parentTaxonDefinition(nullable:true);
		taxonDefinition(unique:['classification', 'path'])
	}

    static mapping = {
    }
	
	def updateLevel(){
		//update this node
		//update all child node recursively
	}
	
//	def beforeInsert(){
//		level = path.split("_").size()
//	}
}
