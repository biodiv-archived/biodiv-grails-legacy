package species.participation

import species.auth.SUser

class ObservationFlag {
	
	def activityFeedService
	
	public enum FlagType {
		OBV_INAPPROPRIATE("Inappropriate observation"),
		LOCATION_INAPPROPRIATE("Inappropriate location")
		
		private String value;

		FlagType(String value) {
			this.value = value;
		}
		
		static list() {
			return [OBV_INAPPROPRIATE, LOCATION_INAPPROPRIATE];
		}
		
		String value() {
			return this.value;
		}
	}
    
	static constraints = {
		author(unique:['observation'])
		notes nullable:true, blank: true
		flag nullable:false
		notes (size:0..400)
    }
	
	static mapping = {
		version : false;
		notes type:'text';
	}

	
	Date createdOn = new Date();
	String notes;
	FlagType flag;
	
	static belongsTo = [author:SUser, observation:Observation]
	
	def afterDelete(){
		activityFeedService.deleteFeed(this)
	}
}
