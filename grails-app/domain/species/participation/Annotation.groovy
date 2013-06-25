package species.participation

class Annotation {
	
	String key;
	String value;
	
	//source of annotation in case of checklist source type will be Checklist.class 
	String sourceType
	//order of annotation in source required for checklist
	int columnOrder
	
	// can be enabled on giving facility to add annotation by some user
	//author
	//description
	//dateCreated
	//sourceId
	
	static belongsTo = [observation:Observation];

	static constraints = {
		value nullable:true;
		sourceType nullable:true;
		columnOrder nullable:true;
	}

	static mapping = {
		version : false;
		value type:'text';
	}	

}
