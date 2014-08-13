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
		//XXX:remove this once all migration is done remove constrain from db as well and make it nullable false
		//level(nullable:true);
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
