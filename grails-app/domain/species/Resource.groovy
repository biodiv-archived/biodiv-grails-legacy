package species

import species.participation.Observation;

class Resource {

	public enum ResourceType {
		ICON("Icon"),
		IMAGE("Image"),
		AUDIO("Audio"),
		VIDEO("Video");
		
		private String value;
		
		ResourceType(String value) {
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}
	}
	
	ResourceType type;
	String url; //TODO validate as url
	String fileName;
	String description;
	String mimeType; //TODO:validate
	

	static hasMany = [contributors:Contributor, attributors:Contributor, speciesFields:SpeciesField, observation:Observation, licenses:License];
	static belongsTo = [SpeciesField, Observation];
	
	static mapping = {
		description type:'text';
	}
	
    static constraints = {
		fileName(blank:false);
		url(nullable:true);
		description(nullable:true);
		mimeType(nullable:true);
		licenses  validator : { val, obj -> val && val.size() > 0 }
    }
}
