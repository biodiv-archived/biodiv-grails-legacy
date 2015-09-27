package species

import species.ScientificName.RelationShip
import species.ScientificName.TaxonomyRank

//merged becoz now it extends taxonomy definition
class SynonymsMerged extends TaxonomyDefinition {

    RelationShip relationship;
    NamesMetadata.NameStatus status = NamesMetadata.NameStatus.SYNONYM;
    
    static constraints = {
	    relationship(nullable:true);
    }

    static mapping = {
        sort id:'asc'
        version false;
    }

/*    Map fetchGeneralInfo() {
         return [name:name, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId ]
    }
*/
    def addAcceptedName(TaxonomyDefinition accepted) {
        AcceptedSynonym.createEntry(accepted, this);
        return;
    }

    List<TaxonomyDefinition> fetchAcceptedNames() {
        return AcceptedSynonym.fetchAcceptedNames(this);
    }

    def removeAcceptedName(TaxonomyDefinition accepted) {
        AcceptedSynonym.removeEntry(accepted, this);
        return;
    }
    
    //Removes as synonym from all accepted names
    def removeAsSynonym() {
        def acceptedNames = this.fetchAcceptedNames();
        acceptedNames.each { acc ->
            //this.removeAcceptedName(acc);
            acc.removeSynonym(this);
        }
    }
	
	def beforeInsert(){
		super.beforeInsert()
	}
}
