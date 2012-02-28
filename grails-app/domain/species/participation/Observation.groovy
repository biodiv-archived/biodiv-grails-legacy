package species.participation

import org.grails.taggable.*
import grails.plugins.springsecurity.Secured

import species.Contributor;
import species.Resource;
import species.auth.SUser;
import species.groups.SpeciesGroup;

class Observation implements Taggable{

	public enum OccurrenceStatus {
		ABSENT ("Aabsent"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#absent
		CASUAL ("Casual"),	// http://rs.gbif.org/terms/1.0/occurrenceStatus#casual
		COMMON	("Common"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#common
		DOUBTFUL ("Doubtful"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#doubtful
		FAIRLYCOMMON ("FairlyCommon"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#fairlyCommon
		IRREGULAR ("Irregular"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#irregular
		PRESENT	("Present"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#present
		RARE	("Rare"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#rare
		UNCOMMON("Uncommon")

		private String value;

		OccurrenceStatus(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}
	}
	
	SUser author;
	Date observedOn;
	Date createdOn = new Date();
	String notes;
	SpeciesGroup group;
	int rating;
    String placeName;
	String reverseGeocodedName
	String location;
	float latitude;
    float longitude;
	boolean geoPrivacy = false;
	String locationAccuracy;
	
	static hasMany = [resource:Resource, recommendationVote:RecommendationVote];

	static constraints = {
		notes nullable:true
		resource validator : { val, obj -> val && val.size() > 0 }
		observedOn validator : {val -> val < new Date()}
		notes (size:0..400)
	}

	static mapping = {
		version : false;
		notes type:'text'
	}
	
	Resource mainImage() {
		//TODO: return resources in rating order and choose first
		Iterator iterator = resource?.iterator();
		if(iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
}
