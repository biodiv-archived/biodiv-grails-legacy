package species.participation

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
import grails.util.GrailsNameUtils;
import org.grails.rateable.*
import content.eml.Coverage;
import species.Metadata;

class Observation extends Metadata implements Taggable, Rateable {
	
	def dataSource
	def grailsApplication;
	def commentService;
	def activityFeedService;
	def springSecurityService;
    def resourceService;
	def observationsSearchService;
	
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
	String searchText;
	Recommendation maxVotedReco;
	boolean agreeTerms = false;
	
	//if true observation comes on view otherwise not
	boolean isShowable;
	//if observation representing checklist then this flag is true
	boolean isChecklist = false;
	//observation generated from checklist will have source as checklist other will point to themself
	Long sourceId;
    
	static hasMany = [resource:Resource, recommendationVote:RecommendationVote, obvFlags:ObservationFlag, userGroups:UserGroup, annotations:Annotation];
	static belongsTo = [SUser, UserGroup, Checklists]

	static constraints = {
		notes nullable:true
		searchText nullable:true
		maxVotedReco nullable:true
		//to insert observation without db exception
		sourceId nullable:true
		//resource validator : { val, obj -> val && val.size() > 0 }
		fromDate validator : {val -> val < new Date()}
		latitude validator : { val, obj -> 
            if(!val && !areas) {
                return ['default.blank.message', 'Latitude']
            }
			if(Float.isNaN(val)) {
				return ['typeMismatch.java.lang.Integer','Latitude']
			}
			if( val < 6.74678 || val > 35.51769) {
				return ['value.not.in.range', 'Latitude', '6.74678', '35.51769']
			}
		}
		longitude validator : { val, obj ->  
            if(!val && !areas) {
                return ['default.blank.message', 'Longitude']
            }
			if(Float.isNaN(val)) { 
				return ['typeMismatch.java.lang.Integer', 'Longitude']
			}
			if( val < 68.03215 || val > 97.40238) {
				return ['value.not.in.range', 'Longitude', '68.03215', '97.40238']
			}
		}
        placeName blank:false
		agreeTerms nullable:true
	}

	static mapping = {
		version : false;
		notes type:'text'
		searchText type:'text'
		autoTimestamp false
		tablePerHierarchy false
		//XXX uncomment this only for metachecklist migration to reatain id in old url
		//id generator:'assigned'
	}

	/**
	 * TODO: return resources in rating order and choose first
	 * @return
	 */
	Resource mainImage() {
		def res = listResourcesByRating(1);
        if(res) 
            return res[0]
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
		return RecommendationVote.find(query, [observation:this, recoIds:recoIds]).recommendation
	}

	void calculateMaxVotedSpeciesName(){
		maxVotedReco = findMaxRepeatedReco();
		lastRevised = new Date();
		saveConcurrently();
	}

	String fetchSuggestedCommonNames(){
		if(!maxVotedReco){
			return "";
		}else{
			return fetchSuggestedCommonNames(maxVotedReco.id, false);
		}
	}


	private String fetchSuggestedCommonNames(recoId, boolean addLanguage){
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
					nameList.add(cnReco.name)
				}
			}
		}

		return getFormattedCommonNames(langToCommonName, addLanguage)
	}

	private String getFormattedCommonNames(Map langToCommonName, boolean addLanguage){
		if(langToCommonName.isEmpty()){
			return ""
		}

		def englishId = Language.getLanguage(null).id
		def englishNames = langToCommonName.remove(englishId)

		def cnList = []

		langToCommonName.keySet().each{ key ->
			def lanSuffix = langToCommonName.get(key).join(", ")
			if(addLanguage){
				lanSuffix = Language.read(key).name + ": " + lanSuffix
			}
			cnList << lanSuffix
		}

		//adding english names in front if its availabel
		def engNamesString = null
		if(englishNames){
			engNamesString = englishNames.join(", ")
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
			String cNames = fetchSuggestedCommonNames(reco.id, true)
			map.put("commonNames", (cNames == "")?"":"(" + cNames + ")");
			map.put("disAgree", (currentUser in map.authors));
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
			lastRevised = new Date();
		}
	}
	
	def beforeInsert(){
		updateIsShowable()
	}
	
	def afterInsert(){
		sourceId = sourceId ?:id
	}
	
	def getPageVisitCount(){
		return visitCount;
	}

	public static int getCountForGroup(groupId){
		return Observation.executeQuery("select count(*) from Observation obv where obv.group.id = :groupId ", [groupId: groupId])[0]
	}

	List fetchAllFlags(){
		return ObservationFlag.findAllWhere(observation:this);
	}

	private updateObservationTimeStamp(){
		lastRevised = new Date();
		saveConcurrently();
		observationsSearchService.publishSearchIndex(this, true);
	}
	
	private updateIsShowable(){
		isShowable = (isChecklist || (resource && !resource.isEmpty())) ? true : false
	}
	
	String fetchSpeciesCall(){
		return maxVotedReco ? maxVotedReco.name : "Unknown"
	}
	
	String title() {
		String title = fetchSpeciesCall() 
		if(!title || title.equalsIgnoreCase('Unknown')) {
			title = 'Help Identify'
		}
		return title;
	}

	def fetchCommentCount(){
		return commentService.getCount(null, this, null, null)
	}
	
	def onAddComment(comment){
		updateObservationTimeStamp()
	}
	
	def afterDelete(){
		activityFeedService.deleteFeed(this)
	}
	
	def Map fetchExportableValue(){
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
		
		
		res[ObvUtilService.LOCATION] =placeName
		res[ObvUtilService.LONGITUDE] = "" + longitude
		res[ObvUtilService.LATITUDE] = "" + latitude
		res[ObvUtilService.NOTES] = notes
		
		
		res[ObvUtilService.TAGS] =this.tags.join(", ")
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
	
	def saveConcurrently(f = {}){
		try{
			f()
			if(!save(flush:true)){
				errors.allErrors.each { log.error it }
			}
		}catch(org.hibernate.StaleObjectStateException e){
			attach()
			def m = merge()
			//refresh()
			//f()
			if(!m.save(flush:true)){
				m.errors.allErrors.each { log.error it }
			}
		}
	}
	
	def boolean fetchIsFollowing(SUser user=springSecurityService.currentUser){
		return Follow.fetchIsFollowing(this, user)
	}

    def listResourcesByRating(max = -1) {
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

	def fetchResourceCount(){
        def result = Observation.executeQuery ('''
            select r.type, count(*) from Observation obv join obv.resource r where obv.id=:obvId group by r.type order by r.type
            ''', [obvId:this.id]);
        return result;
	}
	
	def fetchRecoVoteOwnerCount(){
		return RecommendationVote.countByObservation(this)
	}
	
	def fetchChecklistAnnotation(){
		return Annotation.findAllBySourceTypeAndObservation(Checklists.class.getCanonicalName(), this, [sort:'columnOrder', order:'asc'])
	}
//	
//	def fetchSourceChecklistTitle(){
//		activityFeedService.getDomainObject(sourceType, sourceId).title
//	}
}
