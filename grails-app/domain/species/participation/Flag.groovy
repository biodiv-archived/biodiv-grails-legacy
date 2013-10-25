package species.participation

import species.participation.Observation
import species.auth.SUser

class Flag extends AbstractAction {
	
	def activityFeedService
    
    String notes;

 
	//static belongsTo = [author:SUser, observation:Observation]
	
	public enum FlagType {
		DETAILS_INAPPROPRIATE("Inappropriate details"),
		LOCATION_INAPPROPRIATE("Inappropriate location")
		
		private String value;

		FlagType(String value) {
			this.value = value;
		}
		
		static list() {
			return [DETAILS_INAPPROPRIATE, LOCATION_INAPPROPRIATE];
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
