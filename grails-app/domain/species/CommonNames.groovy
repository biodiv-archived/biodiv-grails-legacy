package species

import species.ScientificName.TaxonomyRank
import species.auth.SUser

class CommonNames extends NamesMetadata {

	String name;
	Language language;
	TaxonomyDefinition taxonConcept;

	//i.e. aam
	String transliteration;
	
	// added this column for optimizing case insensitive sql query
	String lowercaseName;
	
	boolean isDeleted = false;
	def utilsService;
	
    static constraints = {
		name(blank:false, nullable:false, unique:['language','taxonConcept']);
		language(nullable:true);
		transliteration(nullable:true);
		lowercaseName nullable:true;
		isDeleted nullable:false;
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
	
	def beforeInsert(){
		super.beforeInsert()
		lowercaseName = name.toLowerCase()
	}
	
	def beforeUpdate(){
		super.beforeUpdate()
		if(lowercaseName != name.toLowerCase())
			lowercaseName = name.toLowerCase()
	}

	boolean isSpeciesContributor(SUser user) {
        if(!user) user = springSecurityService.currentUser;
        return utilsService.isSpeciesAdmin(user) || utilsService.isAdmin(user);
    }
}
