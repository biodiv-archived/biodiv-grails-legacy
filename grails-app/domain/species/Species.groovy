package species

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Resource;
import species.Resource.ResourceType;
import species.groups.SpeciesGroup;

class Species {

	String title;  
	String guid;
	TaxonomyDefinition taxonConcept;
	Resource reprImage;
	Integer percentOfInfo;
	
	def grailsApplication; 
	
	private static final log = LogFactory.getLog(this);
	
	def fieldsConfig = ConfigurationHolder.config.speciesPortal.fields
	
	static hasMany = [fields: SpeciesField, 
		globalDistributionEntities:GeographicEntity, 
		globalEndemicityEntities:GeographicEntity, 
		indianDistributionEntities:GeographicEntity, 
		indianEndemicityEntities:GeographicEntity, 
		taxonomyRegistry:TaxonomyRegistry, 
		resources:Resource];

	static constraints = {
		guid(blank: false, unique: true);
		reprImage(nullable:true);
		percentOfInfo(nullable:true);
	}

	static mapping = {
		fields sort : 'field'
	}

	Resource mainImage() {  
		if(!reprImage) { 
			def images = getImages();
			reprImage = images ? images[0]:null;
			if(reprImage && !reprImage.fileName.equals("no-image.jpg")) {
				if(!this.save()) {
					this.errors.each { log.error it }
				}
			}			
		}
		
		if(reprImage && (new File(grailsApplication.config.speciesPortal.resources.rootDir+reprImage.fileName.trim())).exists()) {
			return reprImage;			
		} else {
			SpeciesGroup group = fetchSpeciesGroup();
			String name = group.name?.trim()?.replaceAll(/ /, '_')
			return new Resource(fileName:"group_icons/${name ? name+'.png': '../no-image.jpg'}", type:ResourceType.IMAGE, title:"You can contribute!!!");
		}
	}

	List<Resource> getImages() { 
		List<Resource> images = new ArrayList<Resource>();
		resources.each { resource ->
			if(resource.type == species.Resource.ResourceType.IMAGE) {
				images.add(resource);
			}
		};
		if(images) {
			return images;	
		} else {
		//
			//return [new Resource(fileName:"no-image.jpg", type:ResourceType.IMAGE, title:"You can contribute!!!")];
		}
	}

	List<Resource> getIcons() {
		def icons = new ArrayList<Resource>();
		resources.each {
			if(it?.type == species.Resource.ResourceType.ICON) {
				icons.add(it);
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
	
	SpeciesGroup fetchSpeciesGroup() {
		return this.taxonConcept.group?:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS); 
	}
}
