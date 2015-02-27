package species

import species.ScientificName.TaxonomyRank

class CommonNames extends NamesMetadata {

	String name;
	Language language;
	TaxonomyDefinition taxonConcept;

	//i.e. aam
	String transliteration;
	
    static constraints = {
		name(blank:false, nullable:false, unique:['language','taxonConcept']);
		language(nullable:true);
		transliteration(nullable:true);
    }

	static mapping = {
		version false;
        language sort:'name asc'
	}

    static fetchMode = [language:'eager']

    Map fetchGeneralInfo(){
        def rank = this.taxonConcept.rank;
	    return [name:name, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId ]
   }
}
