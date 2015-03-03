package species

import species.ScientificName.RelationShip
import species.ScientificName.TaxonomyRank

class Synonyms extends ScientificName {

	String name;
	RelationShip relationship;
	TaxonomyDefinition taxonConcept;
	
    NamesMetadata.NameStatus status = NamesMetadata.NameStatus.ACCEPTED;
	static constraints = {
		name(blank : false);
		canonicalForm(blank : false, nullable:false, unique:['relationship', 'taxonConcept']);
		relationship(nullable:true);
    }
	
	static mapping = {
        sort id:'asc'
		version false;
	}

    Map fetchGeneralInfo(){
        def rank = this.taxonConcept.rank;
        return [name:name, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId ]
    }
	
}
