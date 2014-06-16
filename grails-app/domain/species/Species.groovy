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
import species.groups.UserGroup;
import groovy.sql.Sql;
import grails.util.GrailsNameUtils;
import org.grails.rateable.*
import species.participation.Flag;
import species.participation.Featured;

class Species implements Rateable { 
	String title;
	String guid; 
	TaxonomyDefinition taxonConcept;
	Resource reprImage;
	Float percentOfInfo;  
    Date updatedOn;
    int featureCount = 0;
	Date createdOn = new Date();
	Date dateCreated;
	Date lastUpdated;
	Habitat habitat;
	StringBuilder sLog;
	
	def grailsApplication; 
	def springSecurityService;
	def dataSource
	def activityFeedService
	def speciesService;
    def externalLinksService;
    def speciesUploadService;
    def speciesPermissionService;

    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	private static final log = LogFactory.getLog(this);
	
	def fieldsConfig = ConfigurationHolder.config.speciesPortal.fields
	
	static hasMany = [fields: SpeciesField,
		globalDistributionEntities:GeographicEntity, 
		globalEndemicityEntities:GeographicEntity, 
		indianDistributionEntities:GeographicEntity, 
		indianEndemicityEntities:GeographicEntity, 
		//taxonomyRegistry:TaxonomyRegistry, 
		resources:Resource,
		userGroups:UserGroup];
 

	static belongsTo = [UserGroup]

	static constraints = {
		guid(blank: false, unique: true);
		reprImage(nullable:true);
		percentOfInfo(nullable:true);
		updatedOn(nullable:true);
        featureCount nullable:false;
        habitat nullable:true;
	}

	static mapping = {
		id generator:'species.utils.PrefillableUUIDHexGenerator'
		fields sort : 'field'
	}
	
	//used for debugging
	static transients = [ "sLog" ]

    Species() {
        super();
        println "Species Initialization +++++++++++++++++++++++++++++++"
        //new Throwable("init").printStackTrace() 
    }

	Resource mainImage() {
        def speciesGroupIcon =  this.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
		if(!reprImage || reprImage?.fileName == speciesGroupIcon.fileName) {
            def images = this.getImages();
			this.reprImage = images ? images[0]:null;
            if(reprImage) {
                log.debug " Saving representative image for species ===  $reprImage.fileName" ;

                if(!this.save(flush:true)) {
                    this.errors.each { log.error it }
                }
			}			
		}
		if(reprImage && (new File(grailsApplication.config.speciesPortal.resources.rootDir+reprImage.fileName.trim()).exists() || new File(grailsApplication.config.speciesPortal.observations.rootDir+reprImage.fileName.trim()).exists())) {
			    return reprImage;
		} else {
			return fetchSpeciesGroup().icon(ImageType.ORIGINAL)
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

        def results = sql.rows("select resource_id, species_resources_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from species_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='$type' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref, resource r where resource_id = r.id and r.type ='"+ResourceType.IMAGE+"' and species_resources_id=:id order by avg desc, resource_id asc", [id:this.id]);
        
        def res = sql.rows("select id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='$type' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.id =  c.rating_ref where o.type ='"+ResourceType.IMAGE+"' and o.id in (select resource_id from species_field_resources where species_field_id in(select id from species_field where species_id=:id)) order by avg desc, id asc", [id:this.id])

        def idList = results.collect {it[0]}

        res.each {
            if(!idList.contains(it[0])) {
                idList<<it[0]
            }
        }
        //def idList = results.collect { it[0] }
        if(idList) {
            def instances = Resource.withCriteria {  
                inList 'id', idList 
                cache params.cache
            }
            def finalRes = results.collect {  r -> 
                instances.find { i -> (r[0] == i.id) } 
            }
            return finalRes
        } else {
            []
        }
    }

    List fetchAllFlags(){
		return Flag.findAllWhere(objectId:this.id,objectType:this.class.getCanonicalName());
	}

    String fetchSpeciesCall(){
		return this.title;
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
	
	String notes() {
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

    String summary() {
        return "";
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
		def classifications = []
		//def combinedHierarchy = Classification.findByName(grailsApplication.config.speciesPortal.fields.COMBINED_TAXONOMIC_HIERARCHY);
		//classifications.add([0, combinedHierarchy);
		def reg = TaxonomyRegistry.findAllByTaxonDefinition(this.taxonConcept);
		reg.each {
			//if(it.path.split('_').length >= 6) {
				classifications.add([it.id, it.classification, it.contributors]);
			//}
		}
		//Ordering has to figured out. Sort is a vague criteria. 
		//Added just to get Author contributed as first result if present 
		classifications = classifications.sort {return it[1].name};
		
		return classifications;
	}

	def boolean fetchIsFollowing(SUser user=springSecurityService.currentUser){
		return Follow.fetchIsFollowing(this, user)
	}
	
	def fetchOccurrence(){
		def query = "select count(*) from Observation obv where obv.maxVotedReco.taxonConcept.id = :taxOnConceptId and obv.isDeleted = false "
		return Species.executeQuery(query, ['taxOnConceptId':taxonConcept.id])[0]
	}

    def beforeUpdate(){
        /*try {
            if(this.taxonConcept && externalLinksService.updateExternalLinks(this.taxonConcept)) {
                this.taxonConcept = TaxonomyDefinition.get(this.taxonConcept.id);
            }
        } catch(e) {
            this.appendLogSummary(e)
            e.printStackTrace()
        }*/
        this.percentOfInfo = speciesUploadService.calculatePercentOfInfo(this);
    }

    def afterInsert() {
		//XXX: hack bug in hiebernet and grails 1.3.7 has to use new session
		//http://jira.grails.org/browse/GRAILS-4453
		Species.withNewSession{
	        HashSet contributors = new HashSet();
	
	        //TODO:looks like this is gonna be heavy on every save ... gotta change
			this.fields?.each { contributors.addAll(it.contributors)}
	        Synonyms.findAllByTaxonConcept(this.taxonConcept)?.each { contributors.addAll(it.contributors)}
	        CommonNames.findAllByTaxonConcept(this.taxonConcept)?.each { contributors.addAll(it.contributors)}
	        
	        //Saving current user as contributor for the species
	        if(speciesPermissionService.addContributors(this, new ArrayList(contributors))) {
                log.debug "Added permissions on ${this} species and taxon ${this.taxonConcept.id} to ${contributors}"
            } else {
                log.error "Error while adding permissions on ${this} species and taxon ${this.taxonConcept.id} to ${contributors}"
            }
		}
    }

    def beforeDelete(){
        activityFeedService.deleteFeed(this)
    }

    def fetchList(params, action){
        return speciesService.getSpeciesList(params, action)
    }

    List featuredNotes() {
        return Featured.featuredNotes(this);
    }

    def fetchSpeciesImageDir(){
        if(!this.taxonConcept){
            return null
        }
        def resourcesRootDir = config.speciesPortal.resources.rootDir;
        String sname = resourcesRootDir + "/" +this.taxonConcept.canonicalForm
        File f = new File(sname)
        if(f.exists()){
            return f
        }
        else{
            if(f.mkdirs()){
                return f
            }
        }

    }

    //	def onAddActivity(af, flushImmidiatly=true){
    //		lastUpdated = new Date();
    //		saveConcurrently(null, flushImmidiatly);
    //	}
    //	
//	private saveConcurrently(f = {}, flushImmidiatly=true){
//		try{
//			if(f) f()
//			if(!save(flush:flushImmidiatly)){
//				errors.allErrors.each { log.error it }
//			}
//		}catch(org.hibernate.StaleObjectStateException e){
//			attach()
//			def m = merge()
//			if(!m.save(flush:flushImmidiatly)){
//				m.errors.allErrors.each { log.error it }
//			}
//		}
//	}
	
	public void appendLogSummary(def str){
		if(!str) return
		
		if(!sLog){
			sLog = new StringBuilder()
		}
		
		if(str instanceof Exception){
			//StringWriter errors = new StringWriter();
			//str.printStackTrace(new PrintWriter(errors));
			str = str.getMessage()//errors.toString();
		}
		sLog.append("" + str + System.getProperty("line.separator"))
	}
	
	public String fetchLogSummary(){
		return fetchSpeciesCall() + "\n" + ( sLog ?  sLog.toString() : "")
	}
	
	/**
	 * In overwrite action clearing basic content
	 * Not deleting species because it may be part of user group and activity feed.
	 * Also not touching synonyms, common names and taxon registry
	 * @return
	 */
	def clearBasicContent(){
		resources?.clear();
		fields.each { sf -> 
			removeFromFields(sf)
			def ge = GeographicEntity.read(sf.id)
			if(ge){
				s.removeFromGlobalDistributionEntities(ge)
				s.removeFromGlobalEndemicityEntities(ge)
				s.removeFromIndianDistributionEntities(ge)
				s.removeFromIndianEndemicityEntities(ge)
			}
		}
		
		if(!save()){
			errors.allErrors.each { log.error it }
		}
	}
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @return species list summary on given time period
	 */
	static Map fetchSpeciesSummary(start, end){
		def allSpeciesUpdateCount = Species.createCriteria().count {
			and{
				between("lastUpdated", start, end)
			}
		}
		
		def speciesUpdateCount = Species.createCriteria().count {
			and{
				gt('percentOfInfo', new Float(0.0))
				between("lastUpdated", start, end)
				lt("dateCreated", start)
			}
		}
		
		def speciesCreateCount = Species.createCriteria().count {
			and{
				gt('percentOfInfo', new Float(0.0))
				between("dateCreated", start, end)
			}
		}
		
		return ['allSpeciesUpdated':allSpeciesUpdateCount, 'speciesUpdated':speciesUpdateCount, 'speciesCreated':speciesCreateCount, 'stubsCreated':(allSpeciesUpdateCount - speciesUpdateCount - speciesCreateCount)]
	}
}
