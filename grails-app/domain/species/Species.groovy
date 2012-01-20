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
	Float percentOfInfo;
	
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
			 
			def images = this.getImages();
			this.reprImage = images ? images[0]:null;
			if(reprImage) {
				log.debug "Saving representative image for species";
				if(!this.save(flush:true)) {
					this.errors.each { log.error it }
				}
			}			
		}
		
		if(reprImage && (new File(grailsApplication.config.speciesPortal.resources.rootDir+reprImage.fileName.trim())).exists()) {
			return reprImage;			
		} else {
			fetchSpeciesGroupIcon();			
		}
	}

	List<Resource> getImages() { 
		List<Resource> images = new ArrayList<Resource>();
		
		if(reprImage) {
			images.add(reprImage);
		}
		resources.each { resource ->
			if(resource.type == species.Resource.ResourceType.IMAGE && resource.id != reprImage?.id) {
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
	
	//TODO:remove this function after getting icons for all groups
	Resource fetchSpeciesGroupIcon() {
		SpeciesGroup group = fetchSpeciesGroup();
		String name = group.name?.trim()?.replaceAll(/ /, '_')?.plus('.png');
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/group_icons/${name?.trim()}")).exists()
		if(!iconPresent) {
			name = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).name?.trim()?.replaceAll(/ /, '_')?.plus('.png');
		}
		return new Resource(fileName:"group_icons/${name}", type:ResourceType.ICON, title:"You can contribute!!!");
		
	}
	
	Map<Classification, List<TaxonomyRegistry>> fetchTaxonomyRegistry() {
		return this.taxonConcept.parentTaxonRegistry();
	}
	
}
