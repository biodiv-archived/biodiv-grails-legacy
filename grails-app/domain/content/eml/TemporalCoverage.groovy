package content.eml

class TemporalCoverage {
    
    Date singleDateTime;
    
    //Temporal Coverage
	Date fromDate;
	Date toDate;

    static constraints = {
	      fromDate nullable:true, validator : {val, obj ->
	 		if(!val){
				return true
			} 
			return val < new Date()
		}

	 	toDate nullable:true, validator : {val, obj ->
	 		if(!val){
				return true
			}
			return val < new Date() && val >= obj.fromDate
		}
    }
}
