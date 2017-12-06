package species.dataset

import species.auth.SUser;

import grails.converters.JSON;

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

    List<SupportingModules> supportingModules() {
        List<SupportingModules> s = [];   
        if(this.supportingModules) {
            JSON.parse(this.supportingModules).each {
                s << SupportingModules.list()[it];
            }
        }
        return s
    }

    List<DataTableType> allowedDataTableTypes() {
        List<DataTableType> s = [];   
        JSON.parse(this.allowedDataTableTypes).each {
            s << DataTableType.list()[it];
        }
        return s
    }
}
