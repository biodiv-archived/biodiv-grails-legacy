package species

import species.ScientificName.RelationShip

class Synonyms extends ScientificName {

	String name;
	RelationShip relationship;
	TaxonomyDefinition taxonConcept;
	
	static constraints = {
		name(blank : false);
		canonicalForm(blank : false, nullable:false, unique:['relationship', 'taxonConcept']);
		relationship(nullable:true);
    }
	
	static mapping = {
        sort id:'asc'
		version false;
	}
}
