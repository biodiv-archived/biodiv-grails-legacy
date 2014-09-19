package species.participation

import grails.plugin.springsecurity.SpringSecurityUtils;

import species.utils.ImageType;
import species.utils.Utils
import org.grails.taggable.*
import groovy.sql.Sql;
import java.text.SimpleDateFormat
import species.Habitat
import species.Language;
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
import species.Metadata;
import speciespage.ObservationService;
import species.Species;

class Observation extends Metadata implements Taggable, Rateable {
	
	def dataSource
	def commentService;
	def springSecurityService;
    def resourceService;
	def observationsSearchService;
	def SUserService;
    def observationService;
    def userGroupService;

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

    SUser author;
	String notes;
	int rating;
	long visitCount = 0;
    boolean isDeleted = false;
	int flagCount = 0;
	int featureCount = 0;
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

	// Language 
	Language language;
    
	static hasMany = [resource:Resource, recommendationVote:RecommendationVote, userGroups:UserGroup, annotations:Annotation];
	static belongsTo = [SUser, UserGroup, Checklists]

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
			if(!obj.sourceId && !obj.isChecklist) 
				val && val.size() > 0 
		}
		language nullable:false
        featureCount nullable:false
		latitude nullable: false
		longitude nullable:false
		topology nullable:false
		fromDate nullable:false
		placeName blank:false
		agreeTerms nullable:true
		checklistAnnotations nullable:true
	}

	static mapping = {
		version : false;
		notes type:'text'
		searchText type:'text'
		checklistAnnotations type:'text'
		autoTimestamp false
		tablePerHierarchy false
	}

	/**
	 * TODO: return resources in rating order and choose first
	 * @return
	 */
	Resource mainImage() {
		def res = listResourcesByRating(1);
        if(res) 
            return res[0]
		else
			return group?.icon(ImageType.ORIGINAL)
	}

	/**
	 * 
	 * @return
	 */
	RecommendationVote fetchOwnerRecoVote(){
		return RecommendationVote.findByAuthorAndObservation(author, this);
	}

	private Recommendation findMaxRepeatedReco(){
		//getting list of max repeated recoIds
		def sql =  Sql.newInstance(dataSource);
		def query = "select recoVote.recommendation_id as recoid, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoId order by votecount desc;"
		def res = sql.rows(query, [obvId:id])

		if(!res || res.isEmpty()){
			return null
		}
		def recoIds = []

		int maxCount = res[0]["votecount"]
		res.each{ reco ->
			if(reco["votecount"] == maxCount){
				recoIds << reco["recoid"]
			}
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
		maxVotedReco = findMaxRepeatedReco();
	}

	String fetchSuggestedCommonNames(){
		if(!maxVotedReco){
			return "";
		}else{
			def langToCommonName =  suggestedCommonNames(maxVotedReco.id);
            return  getFormattedCommonNames(langToCommonName, false)
		}
	}

	private Map suggestedCommonNames(recoId) {
		def englistId = Language.getLanguage(null).id
		Map langToCommonName = new HashMap()
		this.recommendationVote.each{ rv ->
			if(rv.recommendation.id == recoId){
				def cnReco = rv.commonNameReco
				if(cnReco){
					def cnLangId = (cnReco.languageId != null)?(cnReco.languageId):englistId
					def nameList = langToCommonName.get(cnLangId)
					if(!nameList){
						nameList = new HashSet()
						langToCommonName[cnLangId] = nameList
					}
					nameList.add(cnReco)
				}
			}
		}
        return langToCommonName;
    }

	private String getFormattedCommonNames(Map langToCommonName, boolean addLanguage){
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
		if(limit == 0) limit = 3;

		def sql =  Sql.newInstance(dataSource);

		def recoVoteCount;
		if(limit == -1) {
			recoVoteCount = sql.rows("select recoVote.recommendation_id as recoId, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoVote.recommendation_id order by votecount desc", [obvId:this.id])
		} else {
			recoVoteCount = sql.rows("select recoVote.recommendation_id as recoId, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoVote.recommendation_id order by votecount desc limit :max offset :offset", [obvId:this.id, max:limit, offset:offset])
		}

		def currentUser = springSecurityService.currentUser;
		def result = [];
		recoVoteCount.each { recoVote ->
			def reco = Recommendation.read(recoVote[0]);
			def map = reco.getRecommendationDetails(this);
			map.put("noOfVotes", recoVote[1]);
			map.put("obvId", this.id);
            map.put("isLocked", this.isLocked);
			def langToCommonName =  suggestedCommonNames(reco.id);
            String cNames = getFormattedCommonNames(langToCommonName, true)

			//String cNames = suggestedCommonNames(reco.id, true)
			map.put("commonNames", (cNames == "")?"":"(" + cNames + ")");
			map.put("disAgree", (currentUser in map.authors));
            if(this.isLocked == false){
                map.put("showLock", true);
            }
            else{
                def recVo = RecommendationVote.findWhere(recommendation: reco, observation:this);
                if(recVo && recVo.recommendation == this.maxVotedReco){
                    map.put("showLock", false);                   
                }
                else{
                    map.put("showLock", true);
                }
            }
			result.add(map);
		}
		return ['recoVotes':result, 'totalVotes':this.recommendationVote.size(), 'uniqueVotes':getRecommendationCount()];
	}

	def getRecommendationCount(){
		Sql sql =  Sql.newInstance(dataSource);
		def result = sql.rows("select count(distinct(recoVote.recommendation_id)) from recommendation_vote as recoVote where recoVote.observation_id = :obvId", [obvId:id])
		return result[0]["count"]
	}

	
	def incrementPageVisit(){
		visitCount++
	}

	//XXX: comment this method before checklist migration
	def beforeUpdate(){
		if(isDirty() && !isDirty('visitCount')){
			updateIsShowable()
			
			if(isDirty('topology')){
				updateLatLong()
			}
			lastRevised = new Date();
		}
	}
	
	def beforeInsert(){
		updateIsShowable()
		updateLatLong()
	}
	
	def afterInsert(){
		sourceId = sourceId ?:id
	}
	
	def afterUpdate(){
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
	
	private updateIsShowable(){
//		//Suppressing all checklist generated observation even if they have media
//		boolean isChecklistObs = (id && sourceId != id) ||  (!id && sourceId)
//		isShowable = (isChecklist || (!isChecklistObs && resource && !resource.isEmpty())) ? true : false
//		
		
		//showing all observation those have media
		isShowable = (isChecklist || (resource && !resource.isEmpty())) ? true : false
	}
	
	private updateChecklistAnnotation(recoVote){
		def m = fetchChecklistAnnotation()
		if(!m){
			return
		}
		
		Checklists cl = Checklists.read(sourceId)
		if(cl.sciNameColumn && recoVote.recommendation.isScientificName){
			m[cl.sciNameColumn] = recoVote.recommendation.name
		}
		if(cl.commonNameColumn && recoVote.commonNameReco){
			m[cl.commonNameColumn] = recoVote.commonNameReco.name
		}
		
		checklistAnnotations = m as JSON
	}
	
	String fetchFormattedSpeciesCall() {
        if(!maxVotedReco) return "Unknown"

        if(maxVotedReco.taxonConcept) //sciname from clean list
            return maxVotedReco.taxonConcept.italicisedForm
        else if(maxVotedReco.isScientificName) //sciname from dirty list
            return '<i>'+maxVotedReco.name+'</i>'
        else //common name
		    return maxVotedReco.name
	}
	
	String fetchSpeciesCall() {
        return maxVotedReco ? maxVotedReco.name : "Unknown"
    }

	String title() {
		String title = fetchSpeciesCall() 
		if(!title || title.equalsIgnoreCase('Unknown')) {
			title = 'Help Identify'
		}
		return title;
	}

    String notes() {
        return this.notes
    }

    String summary() {
        String authorUrl = userGroupService.userGroupBasedLink('controller':'user', 'action':'show', 'id':this.author.id);
		String desc = "Observed by <b><a href='"+authorUrl+"'>"+this.author.name.capitalize() +'</a></b>'
        desc += " at <b>'" + (this.placeName.trim()?:this.reverseGeocodedName) +"'</b>" + (this.fromDate ?  (" on <b>" +  this.fromDate.format('MMMM dd, yyyy')+'</b>') : "")+".";
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
		
		res[ObvUtilService.IMAGE_PATH] = fetchImageUrlList().join(", ")
		
		res[ObvUtilService.SPECIES_GROUP] = group.name
		res[ObvUtilService.HABITAT] = habitat.name
		res[ObvUtilService.OBSERVED_ON] = new SimpleDateFormat("dd/MM/yyyy").format(fromDate)
		
		def snName = ""
		def cnName = ""
		if(maxVotedReco){
			if(maxVotedReco.isScientificName){
				snName = maxVotedReco.name
				cnName = fetchSuggestedCommonNames()
			}else{
				cnName = maxVotedReco.name
			}
		}
		res[ObvUtilService.CN] =cnName
		res[ObvUtilService.SN] =snName
		
		res[ObvUtilService.GEO_PRIVACY] = "" + geoPrivacy
		res[ObvUtilService.LOCATION] = placeName
		def geoPrivacyAdjust = fetchGeoPrivacyAdjustment(reqUser)
		res[ObvUtilService.LONGITUDE] = "" + (this.longitude + geoPrivacyAdjust)
		res[ObvUtilService.LATITUDE] = "" + (this.latitude + geoPrivacyAdjust)
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
		String base = config.speciesPortal.observations.serverURL
		res[ObvUtilService.AUTHOR_URL] = ObvUtilService.createHardLink('user', 'show', author.id) 
		res[ObvUtilService.AUTHOR_NAME] = author.name
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
        def result = Observation.executeQuery ('''
            select r.type, count(*) from Observation obv join obv.resource r where obv.id=:obvId group by r.type order by r.type
            ''', [obvId:this.id]);
        return result;
	}
	
	def fetchRecoVoteOwnerCount(){
		return RecommendationVote.countByObservation(this)
	}
	
	def fetchChecklistAnnotation(){
		def res = [:]
		Checklists cl = Checklists.read(sourceId)
		if(cl && checklistAnnotations){
			def m = JSON.parse(checklistAnnotations)
			cl.fetchColumnNames().each { name ->
				res.put(name, m[name])
			}
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
            eq ('isShowable', true);
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
}
