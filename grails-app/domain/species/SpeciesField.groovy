package species

import org.grails.rateable.*
import species.auth.SUser

class SpeciesField extends Sourcedata implements Rateable {

	def activityFeedService
	
	public enum Status {
		UNDER_CREATION("Under Creation"),
		PUBLISHED("Published"),
		UNDER_VALIDATION("Under Validation"),
		VALIDATED("Validated");
		
		private String value;
		
		Status(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
	}
	
	public enum AudienceType {
		CHILDREN("Children"), 
		GENERAL_PUBLIC("General Audience"), 
		EXPERT_USERS("Expert"),
		
		private String value;
		
		AudienceType(String value) {
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}
	}
	
	Status status = Status.UNDER_CREATION;
	Field field;	
	String description;
	Date dateCreated
	Date lastUpdated
    List<SUser> contributors;
    List<Contributor> attributors;
	
	static hasMany = [contributors:SUser, licenses:License, audienceTypes:AudienceType, resources:Resource, references:Reference, attributors:Contributor];
	static belongsTo = [species:Species];
	
	static mapping = {
		description type:"text";
		tablePerHierarchy true;
		references cascade: "all-delete-orphan"
	}
	
	static constraints = {
		contributors validator : { val, obj ->
			if(!val) {
				obj.addToContributors(SUser.findByUsername('pearlsravanthi'));
				//return ['species.field.empty', 'contributor',  obj.field.concept, obj.field.category, obj.field.subCategory, obj.species.taxonConcept.name]
				return true;
			}
		}
		licenses validator : { val, obj ->
			if(!val) {
				return ['species.field.empty', 'licenses',  obj.field.concept, obj.field.category, obj.field.subCategory, obj.species.taxonConcept.name]
			}
		}
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}
}
