package species

class ExternalLinks {

	String eolId;
	String gbifId;
	String iucnId;
	String itisId;
	String ncbiId;
	String colId
	
	Date eolFetchDate;
	Integer noOfDataObjects;
	
	static belongsTo = [taxonConcept:TaxonomyDefinition];
	
    static constraints = {
		eolId(nullable:true);
		gbifId(nullable:true);
		iucnId(nullable:true);
		itisId(nullable:true);
		ncbiId(nullable:true);
		colId(nullable:true)
		eolFetchDate(nullable:true);
		noOfDataObjects(nullable:true);
    }
	
	
	static mapping = {
		version  false;
	}
}
