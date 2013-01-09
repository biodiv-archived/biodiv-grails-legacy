package species

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Resource;
import species.Resource.ResourceType;
import species.groups.SpeciesGroup;
import species.utils.ImageType;
import species.utils.ImageUtils;

class Species {

	String title; 
	String guid; 
	TaxonomyDefinition taxonConcept;
	Resource reprImage;
	Float percentOfInfo; 
	Date updatedOn;
	Date createdOn = new Date();
	Date dateCreated;
	Date lastUpdated;
	
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
		updatedOn(nullable:true);
	}

	static mapping = {
		id generator:'species.utils.PrefillableUUIDHexGenerator'
		fields sort : 'field'
	}

	Resource mainImage() {  
		if(!reprImage) {
			def images = this.getImages();
			this.reprImage = images ? images[0]:null;
			if(reprImage) {
				log.debug " Saving representative image for species ===  $reprImage.fileName" ;
				if(!this.save(flush:true)) {
					this.errors.each { log.error it }
				}
			}			
		}
		
		if(reprImage && (new File(grailsApplication.config.speciesPortal.resources.rootDir+reprImage.fileName.trim())).exists()) {
			return reprImage;
		} else {
			return null;
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
	Resource fetchSpeciesGroupIcon(ImageType type) {
		SpeciesGroup group = fetchSpeciesGroup();
		return group.icon(type);
	}
	
	Map<Classification, List<TaxonomyDefinition>> fetchTaxonomyRegistry() {
		return this.taxonConcept.parentTaxonRegistry();
	}
	
	def classifications() {
		def classifications = new HashSet();
		def combinedHierarchy = Classification.findByName(grailsApplication.config.speciesPortal.fields.COMBINED_TAXONOMIC_HIERARCHY);
		classifications.add(combinedHierarchy);
		def reg = TaxonomyRegistry.findAllByTaxonDefinition(this.taxonConcept);
		reg.each {
			if(it.path.split('_').length >= 6) {
				classifications.add(it.classification);
			}
		}
		//Ordering has to figured out. Sort is a vague criteria. 
		//Added just to get Author contributed as first result if present 
		classifications = classifications.sort {it.name};
		
		return classifications;
	}

}