package species.dataset

import species.auth.SUser;

import grails.converters.JSON;
import species.groups.CustomField;

class DataPackage {	
	
	public enum DataTableType {
		OBSERVATIONS("Observations"),
		DOCUMENTS("Documents"),
		SPECIES("Species"),
		SPECIES_FIELDS("Species Fields"),
        TRAITS("Traits"),
        FACTS("Facts");

		private String value;

		DataTableType(String value) {
			this.value = value;
		}

		static list() {
			[
				OBSERVATIONS,
				DOCUMENTS,
				SPECIES,
				SPECIES_FIELDS,
                TRAITS,
                FACTS			
			]
		}

		String value() {
			return this.value;
		}

		String toString() {
			return this.value();
		}
	}
	
    public enum SupportingModules {
		TITLE("Title"),
		DESCRIPTION("Description"),

		ACCESS("Access"),
		PARTY("Party"),

        TAGS("Tags"),

		TEMPORAL_COVERAGE("Temporal Coverage"),
		TAXONOMIC_COVERAGE("Taxonomic Coverage"),
		GEOGRAPHICAL_COVERAGE("Geographical Coverage"),
        
		PROJECT("Project"),

		METHODS("Methods");

		private String value;

		SupportingModules(String value) {
			this.value = value;
		}

		static list() {
			[
		        TITLE,
                DESCRIPTION,
                ACCESS,
                PARTY,
                TAGS,
                TEMPORAL_COVERAGE,
                TAXONOMIC_COVERAGE,
                GEOGRAPHICAL_COVERAGE,
                PROJECT,
                METHODS
			]
		}

		String value() {
			return this.value;
		}

		String toString() {
			return this.value();
		}
	}
	
	String title;
    String description;
	
	String supportingModules;
	String allowedDataTableTypes;

    SUser uploader;
    SUser author;

    Date createdOn = new Date();
    Date lastRevised = new Date();

    boolean isDeleted = false;

	static constraints = {
		title nullable:false, blank:false;
		description nullable:false, blank:false;
		supportingModules nullable:true;
		allowedDataTableTypes nullable:false;
	}
	
	static mapping = {
        description type:'text'		
	}

    Map<SupportingModules, CustomField> supportingModules() {
        Map<SupportingModules, CustomField> s = [:];   
        if(this.supportingModules) {
            JSON.parse(this.supportingModules).each {sm, cfs ->
                List cfsList = [];
                cfs.each { m->
                    if(m.name && m.dataType){
                        def dataType = CustomField.DataType.getDataType(m.dataType) 
                        boolean allowedMultiple = (dataType == CustomField.DataType.TEXT)?m.allowedMultiple:false
                        def options = m.options? m.options.split(",").collect{it.trim()}.join(","):""
                        CustomField cf =  new CustomField(dataPackage:this, name:m.name, dataType:dataType, isMandatory:m.isMandatory, allowedParticipation:m.allowedParticipation, allowedMultiple:m.allowedMultiple, defaultValue:m.defaultValue, options:options, notes:m.description)
                        cfsList << cf;
                    }else{
                        log.debug "Either name or type is missing ${m}"
                    }
                }

                s[sm] = cfsList;
            }
        }
        return s;
    }
    
    static Map<SupportingModules, CustomField> defaultSupportingModules() {
        Map<SupportingModules, CustomField> s = [:];
        s[SupportingModules.TITLE] = new CustomField();
        s[SupportingModules.DESCRIPTION] = new  CustomField(); 
        s[SupportingModules.TEMPORAL_COVERAGE] = new  CustomField(); 
        s[SupportingModules.GEOGRAPHICAL_COVERAGE] = new  CustomField(); 
        s[SupportingModules.TAXONOMIC_COVERAGE] = new  CustomField(); 
        return s;
    }

    List<DataTableType> allowedDataTableTypes() {
        List<DataTableType> s = [];   
        if(this.allowedDataTableTypes) {
            JSON.parse(this.allowedDataTableTypes).each {
                s << DataTableType.list()[it];
            }
        }
        return s
    }
}
