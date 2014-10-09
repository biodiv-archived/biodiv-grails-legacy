package species

import org.grails.rateable.*
import species.auth.SUser
import species.NamesSorucedata;
import species.Language;

import org.springframework.context.MessageSourceResolvable;
class SpeciesField extends NamesSorucedata implements Rateable {

	def activityFeedService
	
	public enum Status implements org.springframework.context.MessageSourceResolvable{
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

		static def toList() {
			return [
				UNDER_CREATION,
				PUBLISHED,
				UNDER_VALIDATION,
				VALIDATED 
            ]
		}
	Object[] getArguments() { [] as Object[] }

        String[] getCodes() {
            
            println "${getClass().name}.${name()}"
            ["${getClass().name}.${name()}"] as String[]
        }   

        String getDefaultMessage() { value() }
	}
	
	public enum AudienceType implements org.springframework.context.MessageSourceResolvable{
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

        static def toList() {
			return [
	            CHILDREN,
                GENERAL_PUBLIC,
                EXPERT_USERS
            ]
        }
	Object[] getArguments() { [] as Object[] }

        String[] getCodes() {
            
            println "${getClass().name}.${name()}"
            ["${getClass().name}.${name()}"] as String[]
        }   

        String getDefaultMessage() { value() }
	}
	
	Status status = Status.UNDER_CREATION;
	Field field;	
	String description;
	Date dateCreated
	Date lastUpdated
    List<Contributor> attributors;
	// Language
    Language language;

	static hasMany = [licenses:License, audienceTypes:AudienceType, resources:Resource, references:Reference, attributors:Contributor];
	static belongsTo = [species:Species];
	
	static mapping = {
		description type:"text";
//		tablePerHierarchy true;
//		tablePerHierarchy false
		references cascade: "all-delete-orphan"
//        discriminator value:"species.SpeciesField"
	}
	
	static constraints = {
        description blank:false, nullable:false
        language nullable:false
		contributors validator : { val, obj ->
			if(!val) {
				//obj.addToContributors(SUser.findByUsername('pearlsravanthi'));
				return ['species.field.empty', 'contributor',  obj.field.concept, obj.field.category, obj.field.subCategory, obj.species.taxonConcept.name]
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
