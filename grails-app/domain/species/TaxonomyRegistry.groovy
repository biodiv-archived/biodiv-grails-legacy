package species

import java.util.List;

class TaxonomyRegistry extends NamesSorucedata {
	//to store level of the node requried for bread first search
	//int level
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
