package species.participation

import species.auth.SUser

class ObservationMetaData{ // implements Comparable {
	
	String key;
	String value;
	
	// if can be checklist or any other thing or null
	String source;
	//if source is checklist then rowId and columnOrderId will have posi
	int rowId;
	//to keep column order same as raw file
	int columnOrderId;
	
	// for complete ability to add meta data
//	String description;
//	SUser author;
//	Date lastUpdated;
		
	static belongsTo = [observation:Observation];

	static constraints = {
		value nullable:true;
		source nullable:true;
		rowId nullable:true;
		columnOrderId nullable:true;
	}

	static mapping = {
		version : false;
		value type:'text';
	}	
//
//	@Override
//	public int compareTo(Object o) {
//		// mainly to handle order in checklist meta data
//		int order = 0;
//		if(source && o.source && (source ==  o.source) && columnOrderId && o.columnOrderId){
//			order = columnOrderId.compareTo(o.columnOrderId);
//		}
//		return order;
//	}
}
