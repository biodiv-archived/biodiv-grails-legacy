package species.participation

import org.grails.taggable.*
import groovy.sql.Sql;
import grails.converters.JSON;
import grails.plugins.springsecurity.Secured



import species.Habitat
import species.Language;
import species.VisitCounter;
import species.Contributor;
import species.Resource;
import species.auth.SUser;
import species.groups.SpeciesGroup;

class Observation implements Taggable{

	/**
	 * 
	 */
	def dataSource
	
	def grailsApplication;
	
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
	Date observedOn;
	Date createdOn = new Date();
	Date lastRevised = createdOn;
	String notes;
	SpeciesGroup group;
	int rating;
	String placeName;
	String reverseGeocodedName
	String location;
	float latitude;
	float longitude;
	boolean geoPrivacy = false;
	String locationAccuracy;
	Habitat habitat;
	long visitCount = 0;
	String maxVotedSpeciesName;
	boolean isDeleted = false;
	int flagCount = 0;
	String searchText;
	
	static hasMany = [resource:Resource, recommendationVote:RecommendationVote];

	static constraints = {
		notes nullable:true
		searchText nullable:true;
		maxVotedSpeciesName nullable:true
		resource validator : { val, obj -> val && val.size() > 0 }
		observedOn validator : {val -> val < new Date()}
		
	}

	static mapping = {
		version : false;
		notes type:'text'
		searchText type:'text'
		autoTimestamp false
	}

	/**
	 * TODO: return resources in rating order and choose first
	 * @return
	 */
	Resource mainImage() {
		def reprImage;		
		Iterator iterator = resource?.iterator();
		if(iterator.hasNext()) {
			reprImage = iterator.next();
		}
		
		if(reprImage && (new File(grailsApplication.config.speciesPortal.observations.rootDir+reprImage.fileName.trim())).exists()) {
			return reprImage;
		} else {
			return null;			
		}
	}
	
	/**
	 * 
	 * @return
	 */
	RecommendationVote fetchOwnerRecoVote(){
		return RecommendationVote.findByAuthorAndObservation(author, this);
	}

	Long findMaxRepeatedRecoId(){
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
			return recoIds[0]
		}
		
		//getting latest recoVote
		query = "from RecommendationVote as recoVote where recoVote.observation = :observation and recoVote.recommendation.id in (:recoIds) order by recoVote.votedOn desc "
		return RecommendationVote.find(query, [observation:this, recoIds:recoIds]).recommendation.id
	}
	
	void calculateMaxVotedSpeciesName(){
		Long recoId = findMaxRepeatedRecoId(); 
		if(!recoId){
			maxVotedSpeciesName = "Unknown";
		}else{
			maxVotedSpeciesName = Recommendation.get(recoId).name
		}
		
		if(!save(flush:true)){
			errors.allErrors.each { log.error it }
		}
		
	}
	
	String fetchSuggestedCommonNames(){
		Long recoId = findMaxRepeatedRecoId();
		if(!recoId){
			return "";
		}else{
			return fetchSuggestedCommonNames(recoId);
		}
	}
	
	
	private String fetchSuggestedCommonNames(recoId){
		def englistId = Language.getLanguage(null)
		Set engCommonNameList = []
		Set otherCNList = []
		this.recommendationVote.each{ rv ->
			if(rv.recommendation.id == recoId){
				def cnReco = rv.commonNameReco
				if(cnReco){
					if(cnReco.languageId == englistId){
						engCommonNameList << cnReco.name
					}else{
						otherCNList << cnReco.name
					}
				}
			}
			
		}
		return engCommonNameList.join(", ") +  otherCNList.join(", ")
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
		 	
//		def recoVoteCount = RecommendationVote.executeQuery("select recoVote.recommendation.id as recoId, count(*) as votecount from RecommendationVote as recoVote where recoVote.observation.id = :obvId group by recoVote.recommendation", [obvId:this.id]);
		
//		def recoVoteCount = RecommendationVote.createCriteria().list {
//			projections {
//				groupProperty("recommendation")
//				count 'id', 'voteCount'
//			}
//			eq('observation', this)
//			order 'voteCount', 'desc'
//		}
		
		def result = [];
		recoVoteCount.each { recoVote ->
            def reco = Recommendation.read(recoVote[0]);
			def map = reco.getRecommendationDetails(this);
			map.put("noOfVotes", recoVote[1]);
			map.put("obvId", this.id);
			String cNames = fetchSuggestedCommonNames(reco.id)
			map.put("commonNames", (cNames == "")?:"(" + cNames + ")");
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
		visitCount++;
		
		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
	def beforeUpdate(){
		if(isDirty() && !isDirty('visitCount')){
			lastRevised = new Date();
		}
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
	
	def updateObservationTimeStamp(){
		lastRevised = new Date();
		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
}
