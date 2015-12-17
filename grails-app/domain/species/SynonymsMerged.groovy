package species

import species.ScientificName.RelationShip
import species.ScientificName.TaxonomyRank
import species.NamesMetadata.NameStatus;

//merged becoz now it extends taxonomy definition
class SynonymsMerged extends TaxonomyDefinition {
	
	def sessionFactory;

    RelationShip relationship;
    NamesMetadata.NameStatus status = NamesMetadata.NameStatus.SYNONYM;
    
    static constraints = {
	    relationship(nullable:true);
    }

    static mapping = {
        sort id:'asc'
        version false;
    }

    Map fetchGeneralInfo() {
         return [name:name, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId ]
    }

    Map fetchLimitInfo(){
        return [id:id,name:name,canonicalForm:canonicalForm,italicisedForm:italicisedForm,rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(),nameStatus:status.toString().toLowerCase(),sourceDatabase:matchDatabaseName,group:group,relationship:relationship?.value(),position:position]
    }

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
	
	boolean changeToAcceptedName(){
		removeAsSynonym()
		
		this.status = NameStatus.ACCEPTED
		this.relationship = null
		if(!save()) {
			this.errors.allErrors.each { log.error it }
		}
		
		String query = "update taxonomy_definition set class = :newClass where id = :id";
		def sql = sessionFactory.getCurrentSession().createSQLQuery(query)
		sql.setProperties([id:id, newClass:TaxonomyDefinition.class.canonicalName]).executeUpdate()
	   
		//SynonymsMerged.executeUpdate("update TaxonomyDefinition set dirtyListReason = :newClass where id = :id ", [newClass:TaxonomyDefinition.class.canonicalName, id : id])
		//XXX: Not giving hir to accepted name because this method is called from getTaxonHir where we create hir at the end
		return true
	}
}
