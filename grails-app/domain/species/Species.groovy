package species

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Resource.ResourceType;

class Species {

	String title; 
	String guid;
	TaxonomyDefinition taxonConcept;
	
	def fieldsConfig = ConfigurationHolder.config.speciesPortal.fields
	
	static hasMany = [fields: SpeciesField, globalDistributionEntities:GeographicEntity, globalEndemicityEntities:GeographicEntity, taxonomyRegistry:TaxonomyRegistry, resources:Resource];

	static constraints = {
		guid(blank: false, unique: true);
	}

	static mapping = { 
		fields sort : 'field'
		commonNames sort:'language'
		taxonomyRegistry sort:'taxonDefinition'
	}

	Resource mainImage() {  
		def images = getImages();
		return images?images[0]:null;
	}

	List<Resource> getImages() { 
		List<Resource> images = new ArrayList<Resource>();
		resources.each { resource ->
			if(resource.type == species.Resource.ResourceType.IMAGE) {
				images.add(resource);
			}
		};
		return images;
	}

	List<Resource> getIcons() {
		def icons = new ArrayList<Resource>();
		resources.each {
			if(it?.type == species.Resource.ResourceType.ICON) {
				images.add(it);
			}
		}
		return icons;
	}

	String findSummary() {
		def f = this.fields.find { speciesField ->
			Field field = speciesField.field;
			field.concept.equalsIgnoreCase(fieldsConfig.OVERVIEW) && field.category.equalsIgnoreCase(fieldsConfig.SUMMARY)
		}
		if(!f) {
			f = this.fields.find { speciesField ->
				Field field = speciesField.field;
				field.concept.equalsIgnoreCase(fieldsConfig.OVERVIEW) && field.category.equalsIgnoreCase(fieldsConfig.BRIEF)
			}
		}
		return f?.description;
	}
	
}
