package species.participation

import grails.plugin.springsecurity.SpringSecurityUtils;

import species.DataObject;
import species.utils.ImageType;
import species.utils.Utils
import org.grails.taggable.*
import groovy.sql.Sql;
import java.text.SimpleDateFormat
import species.Habitat
import species.Language;
import species.License;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import speciespage.ObvUtilService;
import grails.converters.JSON
import grails.util.GrailsNameUtils;
import org.grails.rateable.*
import com.vividsolutions.jts.geom.Geometry
import content.eml.Coverage;
import speciespage.ObservationService;
import species.Species;
import species.dataset.Dataset;
import au.com.bytecode.opencsv.CSVReader;
import java.io.InputStream;
import species.trait.Fact;


class Observation extends DataObject {
	
	def dataSource
	def commentService;
	def springSecurityService;
    def resourceService;
	def observationsSearchService;
    def observationService;
    def userGroupService;
    def traitService;

	public enum OccurrenceStatus {
		ABSENT ("Absent"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#absent
		CASUAL ("Casual"),	// http://rs.gbif.org/terms/1.0/occurrenceStatus#casual
		COMMON	("Common"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#common
		DOUBTFUL ("Doubtful"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#doubtful
		FAIRLYCOMMON ("FairlyCommon"),	//http://rs.gbif.org/terms/1.0/occurrenceStatus#fairlyCommon
		IRREGULAR ("Irregular"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#irregular
		PRESENT	("Present"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#present
		RARE	("Rare"), //http://rs.gbif.org/terms/1.0/occurrenceStatus#rare
		UNCOMMON("Uncommon")

		private String value;

		OccurrenceStatus(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}
	}
    
	public enum BasisOfRecord {
		PRESERVED_SPECIMEN ("Preserved Specimen"),
		FOSSIL_SPECIMEN ("Fossil Specimen"),
		LIVING_SPECIMEN ("Living Specimen"),
		HUMAN_OBSERVATION ("Human Observation"),
		MACHINE_OBSERVATION ("Machine Observation"),
        MATERIAL_SAMPLE("Material Sample"),
        OBSERVATION("Observation"),
        UNKNOWN("Unknown")
		
		private String value;

		BasisOfRecord(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}
		
		static BasisOfRecord getEnum(value){
			if(!value) return null
			
			if(value instanceof BasisOfRecord)
				return value
			
			value = value.toUpperCase().trim()
			switch(value){
				case 'PRESERVED_SPECIMEN':
					return BasisOfRecord.PRESERVED_SPECIMEN
				case 'FOSSIL_SPECIMEN':
					return BasisOfRecord.FOSSIL_SPECIMEN
				case 'LIVING_SPECIMEN':
					return BasisOfRecord.LIVING_SPECIMEN
				case 'HUMAN_OBSERVATION':
					return BasisOfRecord.HUMAN_OBSERVATION
				case 'MACHINE_OBSERVATION':
					return BasisOfRecord.MACHINE_OBSERVATION
				case 'MATERIAL_SAMPLE':
					return BasisOfRecord.MATERIAL_SAMPLE
				case 'OBSERVATION':
					return BasisOfRecord.OBSERVATION
	
				default:
					return BasisOfRecord.UNKNOWN	
			}
		}
	}

	public enum ProtocolType {
        //https://github.com/gbif/gbif-api/blob/master/src/main/java/org/gbif/api/vocabulary/EndpointType.java
        DWC_ARCHIVE,//observations as dwc archive
        TEXT,//CSV upload ... for datasets mainly
        LIST,//Checklist
        SINGLE_OBSERVATION,//Single observation through UI
        MULTI_OBSERVATION,//Multiple observations thru UI
        BULK_UPLOAD,//XLS File upload of observations along with images
        MOBILE,//obvs uploaded thru mobile interface
        OTHER

		private String value;

		String value() {
			return this.value;
		}
		
		static ProtocolType getEnum(value){
			if(!value) return null
			
			if(value instanceof ProtocolType)
				return value
			
			value = value.toUpperCase().trim()
			switch(value){
				case 'DWC_ARCHIVE':
					return ProtocolType.DWC_ARCHIVE
				case 'TEXT':
					return ProtocolType.TEXT
				case 'LIST':
					return ProtocolType.LIST
				case 'SINGLE_OBSERVATION':
					return ProtocolType.SINGLE_OBSERVATION
				case 'MULTI_OBSERVATION':
					return ProtocolType.MULTI_OBSERVATION
				case 'MOBILE':
					return ProtocolType.MOBILE
				case 'OTHER':
					return ProtocolType.OTHER
				default:
					return null	
			}
		}
	}

	String notes;
    //boolean isDeleted = false;
    String searchText;
    //if observation locked due to pulling of images in species
    boolean isLocked = false;
	Recommendation maxVotedReco;
	boolean agreeTerms = false;
	
	//if true observation comes on view otherwise not
	boolean isShowable;
	//if observation representing checklist then this flag is true
	boolean isChecklist = false;
	//observation generated from checklist will have source as checklist other will point to themself
	Long sourceId;
	
	//column to store checklist key value pair in serialized object
	String checklistAnnotations;
    BasisOfRecord basisOfRecord = BasisOfRecord.HUMAN_OBSERVATION;
    ProtocolType protocol = ProtocolType.SINGLE_OBSERVATION;
    String externalDatasetKey;
    Date lastCrawled;
    String catalogNumber;
    String publishingCountry = 'IN';
    String accessRights;
    String informationWithheld;

    Resource reprImage;

    int noOfImages=0;
    int noOfVideos=0;
    int noOfAudio=0;
    int noOfIdentifications=0;

	static hasMany = [userGroups:UserGroup, resource:Resource, recommendationVote:RecommendationVote, annotations:Annotation];
	static belongsTo = [SUser, UserGroup, Checklists, Dataset]
    static List eagerFetchProperties = ['author','maxVotedReco', 'reprImage', 'resource', 'maxVotedReco.taxonConcept', 'dataset', 'dataset.datasource'];

 	static constraints = {
		notes nullable:true
		searchText nullable:true
		maxVotedReco nullable:true
		//to insert observation without db exception
		sourceId nullable:true
		resource validator : { val, obj ->
			//XXX ignoring validator for checklist and its child observation. 
			//all the observation generated from checklist will have source id in advance based on that we are ignoring validation.
			// Genuine observation will not have source id and checklist as false
			if(!obj.sourceId && !obj.isChecklist && !obj.externalId) 
				val && val.size() > 0 
		}
		latitude nullable: false
		longitude nullable:false
		topology nullable:false
        fromDate nullable:false
		agreeTerms nullable:true
		checklistAnnotations nullable:true
		locationScale nullable:true
        externalDatasetKey nullable:true
        lastCrawled nullable:true
        catalogNumber nullable:true
        publishingCountry nullable:true
        accessRights nullable:true
        informationWithheld nullable:true
        reprImage nullable:true
    }

	static mapping = {
		//version false
		notes type:'text'
		searchText type:'text'
		checklistAnnotations type:'text'
		autoTimestamp false
		tablePerHierarchy false
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "observation_id_seq"] 
	 } 

	/**
	 * TODO: return resources in rating order and choose first
	 * @return
	 */
	Resource mainImage() {
		def res = reprImage ? [reprImage] : null;//:listResourcesByRating(1);
        if(res && res.fileName[0].size() > 1)  {
            return res[0]
        } else {
			return group?.icon(ImageType.ORIGINAL)
        }
	}

	/**
	 * 
	 * @return
	 */
	RecommendationVote fetchOwnerRecoVote(){
		return RecommendationVote.findByAuthorAndObservation(author, this);
	}

	private Recommendation findMaxRepeatedReco(){
        /*def recoVotes = RecommendationVote.findAllByObservation(this);
        def votesCounts = [:];
        //aggregating votes across all recommendation accepted names
        recoVotes.each { recoVote ->
            def name = recoVote.recommendation.taxonConcept ? recoVote.recommendation.taxonConcept.canonicalForm:recoVote.recommendation.name
            if(!votesCounts[name]) {
                votesCounts[name] = 0;
            }
            votesCounts[name]++;
        }
        int maxVotes = 0;
        String maxVoteName;
        votesCounts.each { k,v ->
            if(v > maxVotes) {
                maxVotes = v;
                maxVoteName = k;
            }
        }
        return Recommendation.findByName(maxVoteName);
        */


		//getting list of max repeated recoIds
		def sql =  Sql.newInstance(dataSource);
		def query = "select recoVote.recommendation_id as recoid, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoId order by votecount desc;"
		def res = sql.rows(query, [obvId:id])

		if(!res || res.isEmpty()){
			return null
		}
		def recoIds = []
        
        this.noOfIdentifications = 0;

		int maxCount = res[0]["votecount"]
		res.each{ reco ->
			if(reco["votecount"] == maxCount){
				recoIds << reco["recoid"]
			}
            this.noOfIdentifications += reco["votecount"];
		}

		if(recoIds.size() == 1){
			return Recommendation.read(recoIds[0])
		}

		//getting latest recoVote
		query = "from RecommendationVote as recoVote where recoVote.observation = :observation and recoVote.recommendation.id in (:recoIds) order by recoVote.votedOn desc "
		def recoVote = RecommendationVote.find(query, [observation:this, recoIds:recoIds])
		updateChecklistAnnotation(recoVote)
		return recoVote.recommendation
        
	}

	void calculateMaxVotedSpeciesName(){
		this.maxVotedReco = findMaxRepeatedReco();
	}

	String fetchSuggestedCommonNames(){
		if(!maxVotedReco){
			return "";
		}else{
			def langToCommonName =  suggestedCommonNames(maxVotedReco.id);
            return  getFormattedCommonNames(langToCommonName, false)
		}
	}

	Map suggestedCommonNames(recoId) {
        return observationService.suggestedCommonNames(this, recoId);
    }

	static String getFormattedCommonNames(Map langToCommonName, boolean addLanguage){
		if(langToCommonName.isEmpty()){
			return ""
		}

		def englishId = Language.getLanguage(null).id
		def englishNames = langToCommonName.remove(englishId)

		def cnList = []

		langToCommonName.keySet().each{ key ->
			def lanSuffix = langToCommonName.get(key)*.name.join(", ")
			if(addLanguage){
				lanSuffix = Language.read(key).name + ": " + lanSuffix
			}
			cnList << lanSuffix
		}

		//adding english names in front if its availabel
		def engNamesString = null
		if(englishNames){
			engNamesString = englishNames*.name.join(", ")
			if(addLanguage){
				engNamesString = Language.read(englishId).name + ": " + engNamesString
			}
		}

		if(engNamesString){
			cnList.add(0, engNamesString);
		}

		return cnList.join("; ")

	}

	/**
	 * 
	 * @return
	 */
	def getRecommendationVotes(int limit, long offset) {
        return observationService.getRecommendationVotes(this, limit, offset);
	}

	def getRecommendationCount(){
		Sql sql =  Sql.newInstance(dataSource);
		def result = sql.rows("select count(distinct(recoVote.recommendation_id)) from recommendation_vote as recoVote where recoVote.observation_id = :obvId", [obvId:id])
		return result[0]["count"]
	}

	//XXX: comment this method before checklist migration
	def beforeUpdate() {
        log.debug 'Observation beforeUpdate'
		if(isDirty() && !isDirty('visitCount') && !isDirty('version')){
            updateResources();
			updateIsShowable()
			
			if(isDirty('topology')) {
				updateLatLong()
			}
			lastRevised = new Date();
		}
	}
	
	def beforeInsert() {
        println 'Observation beforeInsert'
		updateLocationScale()
		updateLatLong()
        updateResources();
		updateIsShowable()
	}
	
	def afterInsert() {
        println 'Observation afterInsert'
		sourceId = sourceId ?:id
	}
	
	def afterUpdate(){
        println 'Observation afterUpdate'
		//XXX uncomment this method when u actully abt to change isShowable variable
		// (ie. if media added to obv of checklist then this method should be uncommented)
		//activityFeedService.updateIsShowable(this)
    }
	
	def getPageVisitCount(){
		return visitCount;
	}
	
	public static int getCountForGroup(groupId){
		return Observation.executeQuery("select count(*) from Observation obv where obv.group.id = :groupId ", [groupId: groupId])[0]
	}
 
    List fetchAllFlags(){
        def fList = Flag.findAllWhere(objectId:this.id,objectType:this.class.getCanonicalName());
        return fList;
	}

    //Should be called after updateResource
	private void updateIsShowable(){
//		//Suppressing all checklist generated observation even if they have media
//		boolean isChecklistObs = (id && sourceId != id) ||  (!id && sourceId)
//		isShowable = (isChecklist || (!isChecklistObs && resource && !resource.isEmpty())) ? true : false
//		
		
		//showing all observation those have media
		isShowable = (isChecklist || (noOfImages || noOfVideos || noOfAudio)) ? true : false

		//updating protocol
        if(isChecklist || (sourceId  && (id != sourceId))) 
            this.protocol = ProtocolType.LIST;
	}
	
	private void updateResources() {
        log.debug "Observation updateResources";
		noOfImages = noOfVideos = noOfAudio = 0;
        resource.each {
            if(it.type == ResourceType.IMAGE) noOfImages++;
            else if(it.type == ResourceType.VIDEO) noOfVideos++;
            else if(it.type == ResourceType.AUDIO) noOfAudio++;
        }
        utilsService.evictInCache('resources', 'observation-'+this.id);
        updateReprImage();
	}

	private void updateReprImage() {
        println "Observation updateReprImage"
        println this.resource
        if(!this.resource) return;
        Resource highestRatedResource = null;
        this.resource.each { r ->
            if(r.id && r.isAttached()) {
                if(!highestRatedResource || r.averageRating > highestRatedResource.averageRating) {
                    highestRatedResource = r;
                }
            }
            println r.fileName
            println r.rating
            println r.totalRatings
            println r.averageRating
        }
        def res = highestRatedResource;//listResourcesByRating(1)
        if(res && !res.fileName.equals('i')) 
            res = res
		else 
			res = null;//group?.icon(ImageType.ORIGINAL)
        println "Updating reprImage to ${res} ${res.fileName} ${res.url}" 
        this.reprImage = res;
    }

	private updateChecklistAnnotation(recoVote){
		def m = fetchChecklistAnnotation()
		if(!m){
			return
		}
		
        if(sourceId && !datasetId) {
            Checklists cl = Checklists.read(sourceId)
            if(cl.sciNameColumn && recoVote.recommendation.isScientificName){
                m[cl.sciNameColumn] = recoVote.recommendation.name
            }
            if(cl.commonNameColumn && recoVote.commonNameReco){
                m[cl.commonNameColumn] = recoVote.commonNameReco.name
            }

            checklistAnnotations = m as JSON
        }
	}
	
	private updateLocationScale(){
		locationScale = locationScale?:Metadata.LocationScale.APPROXIMATE
	}
	
	String fetchFormattedSpeciesCall() {
        if(!maxVotedReco) return "Unknown"

        //if(maxVotedReco.taxonConcept) //sciname from clean list
        //    return maxVotedReco.name; //maxVotedReco.taxonConcept.italicisedForm
        if(maxVotedReco.taxonConcept && maxVotedReco.isScientificName) { //sciname from dirty list

            if(maxVotedReco.taxonConcept.status=="SYNONYM" && maxVotedReco.isScientificName) {
           		return '<i>'+maxVotedReco.taxonConcept.name+'</i> - <small>Synonym of</small> <i>'+this.maxVotedReco.taxonConcept.italicisedForm+'</i>';
           	}           
            return '<i>'+maxVotedReco.taxonConcept.italicisedForm+'</i>'
        } else //common name
		    return maxVotedReco.name
	}
	
	String fetchSpeciesCall() {
		if(maxVotedReco?.taxonConcept && maxVotedReco?.isScientificName) { //sciname from dirty list

            if(maxVotedReco.taxonConcept.status=="SYNONYM" && maxVotedReco.isScientificName) {
           		return maxVotedReco.taxonConcept.name+'- Synonym of '+this.maxVotedReco.taxonConcept.normalizedForm;
           	}           
            return maxVotedReco.taxonConcept.normalizedForm
        } else //common name
	        return maxVotedReco ? maxVotedReco?.name : "Unknown"
    }

	String title() {
		String title = fetchSpeciesCall() 
		if(!title || title.equalsIgnoreCase('Unknown')) {
			title = 'Help Identify'
		}
		return title;
	}

    String notes(Language userLanguage=null) {
        return this.notes
    }

    String summary(Language userLanguage=null) {
        String authorUrl = userGroupService.userGroupBasedLink('controller':'user', 'action':'show', 'id':this.author.id);
		String desc = "Observed by <b><a href='"+authorUrl+"'>"+this.author.name.capitalize() +'</a></b>'
        desc += " at <b>'" + (this.placeName?.trim()?this.placeName?.trim():this.reverseGeocodedName) +"'</b>" + (this.fromDate ?  (" on <b>" +  this.fromDate.format('MMMM dd, yyyy')+'</b>') : "")+".";
        return desc
    }

	def fetchCommentCount(){
		return commentService.getCount(null, this, null, null)
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}
	
	def Map fetchExportableValue(SUser reqUser=null){
		Map res = [:]
		res[ObvUtilService.OBSERVATION_ID] = "" + id
		res[ObvUtilService.OBSERVATION_URL] = utilsService.createHardLink('observation', 'show', this.id)
		res[ObvUtilService.IMAGE_PATH] = fetchImageUrlList().join(", ")
		
		
		def snName = ""
		def cnName = ""
		def totalVotes, maxVotedRecoCount
		if(maxVotedReco){
			if(maxVotedReco.isScientificName){
				snName = maxVotedReco.name
				cnName = fetchSuggestedCommonNames()
			}else{
				cnName = maxVotedReco.name
			}
			totalVotes = RecommendationVote.countByObservation(this)
			maxVotedRecoCount = RecommendationVote.findAllByRecommendationAndObservation(maxVotedReco, this).size()
		}
		
		res[ObvUtilService.SN] =snName
		res[ObvUtilService.CN] =cnName
		res[ObvUtilService.NUM_IDENTIFICATION_AGREEMENT] = maxVotedRecoCount ? "" + maxVotedRecoCount : "0"
		res[ObvUtilService.NUM_IDENTIFICATION_DISAGREEMENT] = totalVotes ? "" + (totalVotes - maxVotedRecoCount) : "0"
		res[ObvUtilService.HELP_IDENTIFY] = maxVotedReco ? "NO" : "YES"
		
		
		res[ObvUtilService.SPECIES_GROUP] = group.name
		res[ObvUtilService.HABITAT] = habitat.name
		res[ObvUtilService.OBSERVED_ON] = new SimpleDateFormat("dd/MM/yyyy").format(fromDate)
		
				
		res[ObvUtilService.LOCATION] = placeName
		res[ObvUtilService.LOCATION_SCALE] = locationScale?.value()
		
		def geoPrivacyAdjust = fetchGeoPrivacyAdjustment(reqUser)
		res[ObvUtilService.LONGITUDE] = "" + (this.longitude + geoPrivacyAdjust)
		res[ObvUtilService.LATITUDE] = "" + (this.latitude + geoPrivacyAdjust)
		res[ObvUtilService.GEO_PRIVACY] = "" + geoPrivacy
		
		res[ObvUtilService.NOTES] = notes
		
		//XXX: During download of large number of observation some time following exception coming
		//an assertion failure occured (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session)
		try{
			res[ObvUtilService.TAGS] =this.tags.join(", ")
		}catch(e){
			log.debug e.printStackTrace()
			Observation.withNewSession {
				res[ObvUtilService.TAGS] = this.tags.join(", ")
			}
		}
		def ugList = []
		
		this.userGroups.each{ ug ->
			ugList.add(ug.name)
		}
		
		res[ObvUtilService.USER_GROUPS] = ugList.join(", ")
		
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		//def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
		//def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
		res[ObvUtilService.AUTHOR_NAME] = author.name
		res[ObvUtilService.AUTHOR_URL] = utilsService.createHardLink('user', 'show', author.id) 
		
		res[ObvUtilService.CREATED_ON] = new SimpleDateFormat("dd/MM/yyyy").format(createdOn)
		res[ObvUtilService.UPDATED_ON] = new SimpleDateFormat("dd/MM/yyyy").format(lastRevised)

		return res 
	}
	
	private fetchImageUrlList(){
		
		def res = []
		
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String base = config.speciesPortal.observations.serverURL
		
		Iterator iterator = resource?.iterator();
		while(iterator.hasNext()) {
			def reprImage = iterator.next();
			if(reprImage && (new File(grailsApplication.config.speciesPortal.observations.rootDir+reprImage.fileName.trim())).exists()) {
				res.add(base + reprImage.fileName.trim());
			}
		}
		return res
	}

	def getOwner() {
		return author;
	}

	def boolean fetchIsFollowing(SUser user=springSecurityService.currentUser){
		return Follow.fetchIsFollowing(this, user)
	}

    def listResourcesByRating(int max = -1) {
        def params = [:]
        def clazz = Resource.class;
        def type = GrailsNameUtils.getPropertyName(clazz);

        def sql =  Sql.newInstance(dataSource);
        params['cache'] = true;
        params['type'] = type;

        def queryParams = [:];
        queryParams['obvId'] = this.id;
        
        def query = "select resource_id, observation_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from observation_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='$type' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref where observation_id=:obvId order by avg desc, resource_id asc";
        if(max && max > 0) {
            query += " limit :max"
            queryParams['max'] = max;
        }

        def results = sql.rows(query, queryParams);
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

	List fetchResourceCount(){
/*      def result = Observation.executeQuery ('''
            select r.type, count(*) from Observation obv join obv.resource r where obv.id=:obvId group by r.type order by r.type
            ''', [obvId:this.id]);
        return result;
*/
        return [[ResourceType.IMAGE, noOfImages], [ResourceType.VIDEO, noOfVideos], [ResourceType.AUDIO,noOfAudio]];
	}
	
	def fetchRecoVoteOwnerCount(){
		return noOfIdentifications;//RecommendationVote.countByObservation(this)
	}
	
	def fetchChecklistAnnotation(){
		def res = [:]
		Checklists cl = Checklists.read(sourceId)
		if(cl && checklistAnnotations){
			def m = JSON.parse(checklistAnnotations)
			cl.fetchColumnNames().each { name ->
				res.put(name, m[name])
			}
		} else if(checklistAnnotations) {
            res = JSON.parse(checklistAnnotations);
            
            //read dwcObvMapping
            InputStream dwcObvMappingFile = this.class.classLoader.getResourceAsStream('species/dwcObservationMapping.tsv')
            Map dwcObvMapping = [:];
            int l=0;
            dwcObvMappingFile.eachLine { line ->
                if(l++>0) { 
                    String[] parts = line.split(/\t/)
                    if(parts.size()>=8 && (parts[6] || parts[7])) {
                        dwcObvMapping[parts[2].replace('1','').toLowerCase()] = ['name':parts[0], 'url':parts[1], 'field':parts[7], 'order':Float.parseFloat(parts[6])];
                    }
                    else if(parts.size()>=7 && parts[6]) {
                        dwcObvMapping[parts[2].replace('1','').toLowerCase()] = ['name':parts[0], 'url':parts[1], 'field':'', 'order':Float.parseFloat(parts[6])];
                    }
                }
            }

            res = res.sort { dwcObvMapping[it.key.toLowerCase()]?dwcObvMapping[it.key.toLowerCase()].order:1000 }
            def m = [:];
            res.each {
                if(it.value) {
                    m[it.key] = ['value':it.value, 'url':dwcObvMapping[it.key]?.url?:'']
                }
            }
            res = m;
        }
		return res
	}


//	
//	def fetchSourceChecklistTitle(){
//		activityFeedService.getDomainObject(sourceType, sourceId).title
//	}

    def getObservationFeatures() {
        return observationService.getObservationFeatures(this);
    }
	
	def fetchList(params, max, offset, action){
		return observationService.getObservationList(params, max, offset, action)
	}

    static long countObservations() {
        def c = Observation.createCriteria();
        def observationCount = c.count {
            eq ('isDeleted', false);
            //eq ('isShowable', true);
            eq ('isChecklist', false);
        }
        return observationCount;
    }

    static long countChecklists() {
        def c = Observation.createCriteria();
        def observationCount = c.count {
            eq ('isDeleted', false);
            eq ('isShowable', true);
            eq ('isChecklist', true);
        }
        return observationCount;
    }

    private void removeResourcesFromSpecies(){
        def obvRes = this.resource
        if(!this.maxVotedReco) {
            return
        }
        def taxCon = this.maxVotedReco?.taxonConcept
        if(!taxCon){return}
        def speWithThisTaxon = Species.findAllByTaxonConcept(taxCon)
        speWithThisTaxon.each{ sp ->   
            obvRes.each{ obres ->
                sp.removeFromResources(obres)
            }
            if(!sp.save(flush:true)){
                sp.errors.allErrors.each { log.error it } 
            }
        }
    }

    private deleteFromChecklist() {
        if(isObvFromChecklist()) {
            def ckl = Checklists.get(sourceId)
            ckl.deleteObservation(this)
        }
    }

    SpeciesGroup fetchSpeciesGroup() {
        return this.group?:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS); 
	}
	
	def boolean isObvFromChecklist(){
		return (id != sourceId)
	}

    Map getTraits() {
        def factList = Fact.findAllByObjectIdAndObjectType(this.id, this.class.getCanonicalName())
        def traitList = traitService.getFilteredList(['sGroup':this.group.id, 'isNotObservationTrait':false,'isParticipatory':true, 'showInObservation':true], -1, -1).instanceList;
        Map traitFactMap = [:]
        Map queryParams = ['trait':[:]];
        //def conRef = []
        factList.each { fact ->
            if(!traitFactMap[fact.trait.id]) {
                traitFactMap[fact.trait.id] = []
                queryParams['trait'][fact.trait.id] = '';
                traitFactMap['fact'] = []
            }
            traitFactMap[fact.trait.id] << fact.traitValue
            traitFactMap['fact'] << fact.id
            queryParams['trait'][fact.trait.id] += fact.traitValue.id+',';
        }
        queryParams.trait.each {k,v->
            queryParams.trait[k] = v[0..-2];
        }
        return ['traitList':traitList, 'traitFactMap':traitFactMap, 'queryParams':queryParams];

    }
}
