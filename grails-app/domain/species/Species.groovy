package species

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Resource;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.utils.ImageType;
import species.utils.ImageUtils;
import species.participation.Follow;
import groovy.sql.Sql;
import grails.util.GrailsNameUtils;
import org.grails.rateable.*

class Species implements Rateable {
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
	def springSecurityService;
	def dataSource
	def activityFeedService
	
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

    /** 
    * Ordering resources basing on rating
    **/
	List<Resource> getImages() { 
        def params = [:]
        def clazz = Resource.class;
        def type = GrailsNameUtils.getPropertyName(clazz);

		def sql =  Sql.newInstance(dataSource);
        params['cache'] = true;
        params['type'] = type;
        def results = sql.rows("select resource_id, species_resources_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from species_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='$type' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref, resource r where resource_id = r.id and r.type !='"+ResourceType.ICON+"' and species_resources_id=:id order by avg desc, resource_id asc", [id:this.id]);
        
        def idList = results.collect { it[0] }

        if(idList) {
            def instances = Resource.withCriteria {  
                inList 'id', idList 
                cache params.cache
            }
            results.collect {  r-> instances.find { i -> r[0] == i.id } }                           
        } else {
            []
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

	List<Resource> getVideos() {
		def res = new ArrayList<Resource>();
		resources.each {
			if(it?.type == species.Resource.ResourceType.VIDEO) {
				res.add(it);
			}
		}
		return res;
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

	def boolean fetchIsFollowing(SUser user=springSecurityService.currentUser){
		return Follow.fetchIsFollowing(this, user)
	}
	
	def fetchOccurrence(){
		def query = "select count(*) from Observation obv where obv.maxVotedReco.taxonConcept.id = :taxOnConceptId and obv.isDeleted = false "
		return Species.executeQuery(query, ['taxOnConceptId':taxonConcept.id])[0]
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}
	
}
