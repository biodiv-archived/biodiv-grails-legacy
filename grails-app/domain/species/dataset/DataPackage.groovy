package species.dataset

import species.auth.SUser;

import grails.converters.JSON;
import species.groups.CustomField;

class DataPackage {	
	
	public enum DataTableType {
		OBSERVATIONS("Observations"),
		SPECIES("Species"),
        TRAITS("Traits"),
        FACTS("Facts"),
		DOCUMENTS("Documents")

		private String value;

		DataTableType(String value) {
			this.value = value;
		}

		static list() {
			[
				OBSERVATIONS,
				SPECIES,
                TRAITS,
                FACTS,	
				DOCUMENTS
			]
		}

		static DataTableType getEnum(value){
			if(!value) return null
			
			if(value instanceof DataTableType)
				return value
			
			value = value.toUpperCase().trim()
            println value
			switch(value){
				case 'OBSERVATIONS':
					return DataTableType.OBSERVATIONS
				case 'SPECIES':
					return DataTableType.SPECIES
				case 'TRAITS':
					return DataTableType.TRAITS
				case 'FACTS':
					return DataTableType.FACTS
                case 'DOCUMENTS':
					return DataTableType.DOCUMENTS
				default:
					return null	
			}
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
		SUMMARY("Summary"),
		DESCRIPTION("Description"),

		USAGE_RIGHTS("Usage Rights"),
		PARTY("Party"),

        TAGS("Tags"),

		TEMPORAL_COVERAGE("Temporal Coverage"),
		TAXONOMIC_COVERAGE("Taxonomic Coverage"),
		GEOGRAPHICAL_COVERAGE("Geographical Coverage"),
        
		PROJECT("Project"),

		METHODS("Methods"),
		
        OTHERS("Others");

		private String value;

		SupportingModules(String value) {
			this.value = value;
		}

		static list() {
			[
		        TITLE,
                SUMMARY,
                DESCRIPTION,
                USAGE_RIGHTS,
                PARTY,
                TAGS,
                TEMPORAL_COVERAGE,
                TAXONOMIC_COVERAGE,
                GEOGRAPHICAL_COVERAGE,
                PROJECT,
                METHODS,
                OTHERS
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

    SUser author;
    String uploaderIds;
    boolean hasRoleUserAllowed = false; //if false only specific users are going to get permission to add dataset

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

    Map<SupportingModules, List<CustomField>> supportingModules() {
        Map<SupportingModules, CustomField> s = [:];   
        if(this.supportingModules) {
            JSON.parse(this.supportingModules).each {sm, cfs ->
                List cfsList = [];
                cfs.each { m->
                    println m
                    println '=================='
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

                s[SupportingModules.list()[Integer.parseInt(sm)]] = cfsList;
            }
        }
        return s;
    }
    
    static Map<SupportingModules, CustomField> defaultSupportingModules() {
        Map<SupportingModules, CustomField> s = [:];
        s[SupportingModules.TITLE] = new CustomField();
        s[SupportingModules.SUMMARY] = new  CustomField(); 
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
    
    List<CustomField> fetchCustomFields(SupportingModules supportingModule) {
        return supportingModules().get(supportingModule);
    }

    List<SUser> getUploaders() {
        List<SUser> uploaders = [];
        
        if(!uploaderIds) return uploaders;

        uploaderIds.split(',').each { uId ->
            uploaders << SUser.read(Long.parseLong(uId));
        }
        return uploaders;
    }

}
