package species.participation

import species.participation.Observation
import species.auth.SUser

class Flag extends AbstractAction {
	
	def activityFeedService
    
    String notes;

 
	//static belongsTo = [author:SUser, observation:Observation]
	
	public enum FlagType {
		DETAILS_INAPPROPRIATE("Inappropriate details"),
		LOCATION_INAPPROPRIATE("Inappropriate location"),
		DATE_INAPPROPRIATE("Inappropriate date")
		
		private String value;

		FlagType(String value) {
			this.value = value;
		}
		
		static list() {
			return [DETAILS_INAPPROPRIATE, LOCATION_INAPPROPRIATE, DATE_INAPPROPRIATE];
		}
		
		static FlagType getEnum(value){
			if(!value) return null
			
			if(value instanceof FlagType)
				return value
			
			value = value.toUpperCase().trim()
            println value
			switch(value){
				case 'DETAILS INAPPROPRIATE':
					return FlagType.DETAILS_INAPPROPRIATE
				case 'LOCATION INAPPROPRIATE':
					return FlagType.LOCATION_INAPPROPRIATE
				case 'DATE_INAPPROPRIATE':
					return FlagType.DATE_INAPPROPRIATE
				default:
					return null	
			}
		}

		String value() {
			return this.value;
		}
	}
    
	static constraints = {
		author(unique:['objectId','objectType'])
		notes nullable:true, blank: true
		createdOn nullable:false
        flag nullable:false
		notes (size:0..400)
    }
	
	static mapping = { 
		notes type:'text';
	}

	
	FlagType flag;
		
}
