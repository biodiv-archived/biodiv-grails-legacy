package species.participation

class OccurrenceRecord {

	float latitude;
	float longitude;
	String location;
	boolean hidePrecise;
	String notes;
	
    static constraints = {
    }
	
	static mapping = {
		version : false;
		notes type:"text";
	}
}
