package species

class SpeciesField {

	public enum Status {
		UNDER_CREATION("Under Creation"),
		CREATION_COMPLETE("Creation Complete"),
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
	
	
	static hasMany = [contributors:Contributor, licenses:License, audienceTypes:AudienceType, resources:Resource, references:Reference, attributors:Contributor];
	static belongsTo = [species:Species];
	
	static mapping = {
		description type:"text";
		tablePerHierarchy true		
	}
	
	static constraints = {
	
		contributors validator : { val, obj ->
			if(!val) {
				println "++++++${obj}"
				obj.addToContributors(Contributor.findByName('dummy'));
				//return ['species.field.empty', 'contributor', obj.field.category, obj.field.subCategory, obj.species.taxonConcept.name]
				return true;
			}
		}
		licenses validator : { val, obj ->
			if(!val) {
				return ['species.field.empty', 'licenses', obj.field.category, obj.field.subCategory, obj.species.taxonConcept.name]
			}
		}
	}

	
}
