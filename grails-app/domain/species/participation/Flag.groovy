package species.participation

import species.participation.Observation
import species.auth.SUser

class Flag {
	
	def activityFeedService
    
    SUser author
    long objectId
    String objectType
    Date createdOn = new Date();
    String notes;

    static belongsTo = [author:SUser];
 
	
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
		flag nullable:false
		notes (size:0..400)
    }
	
	static mapping = {
        version : false;
		notes type:'text';
	}

	
	FlagType flag;
		
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}
}
