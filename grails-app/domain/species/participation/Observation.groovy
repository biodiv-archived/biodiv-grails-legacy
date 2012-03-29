package species.participation

import org.grails.taggable.*
import groovy.sql.Sql;
import grails.converters.JSON;
import grails.plugins.springsecurity.Secured



import species.Habitat
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

	static hasMany = [resource:Resource, recommendationVote:RecommendationVote];

	static constraints = {
		notes nullable:true
		maxVotedSpeciesName nullable:true
		resource validator : { val, obj -> val && val.size() > 0 }
		observedOn validator : {val -> val < new Date()}
		notes (size:0..400)
	}

	static mapping = {
		version : false;
		notes type:'text'
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

	/**
	 * 
	 * @return
	 */
	List<String> getSpecies() {
		def speciesList = [];
		this.recommendationVote.each{speciesList << it.recommendation.name}
		return getMaxRepeatedElementsFromList(speciesList)
	}

	void calculateMaxVotedSpeciesName(){
		List speciesList = getSpecies(); 
		if(speciesList.isEmpty()){
			maxVotedSpeciesName = "Unknown";
		}else if(speciesList.size() == 1){
			maxVotedSpeciesName = speciesList[0];
		}else{
			String query = "from RecommendationVote as recoVote where recoVote.recommendation.name in (:speciesList) order by recoVote.votedOn desc "
			maxVotedSpeciesName = RecommendationVote.find(query, [speciesList:speciesList]).recommendation.name
		}
		
		if(!save(flush:true)){
			errors.allErrors.each { log.error it }
		}
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	private List getMaxRepeatedElementsFromList(list){
		list.sort()
		def max = 1
		def currentElement = list[0]
		def maxElement = []
		def currentCounter = 0
		for(spe in list) {
			if(spe == currentElement) {
				currentCounter++
			} else {
				if(currentCounter > max) {
					maxElement.clear();
					maxElement.add(currentElement)
				} else if (currentCounter == max) {
					maxElement.add(currentElement)
				}
				currentElement = spe
				currentCounter = 1
			}
		}

		if(currentCounter > max) {
			maxElement.clear();
			maxElement.add(currentElement)
		} else if (currentCounter == max) {
			maxElement.add(currentElement)
		}

		return maxElement
	}

	/**
	 * 
	 * @return
	 */
	def getRecommendationVotes(int limit, long offset) {
		if(limit <= 0) limit = 3;

		def sql =  Sql.newInstance(dataSource);
		
		 def recoVoteCount = sql.rows("select recoVote.recommendation_id as recoId, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoVote.recommendation_id order by votecount desc limit :max offset :offset", [obvId:this.id, max:limit, offset:offset])
		 	
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
			def map = [:];
			map.put("recoId", reco.id);
			if(reco?.taxonConcept) {
				map.put("speciesId", reco?.taxonConcept?.findSpeciesId());
				map.put("canonicalForm", reco?.taxonConcept?.canonicalForm)
			} else {
				map.put("name", reco?.name)
			}
			def recos = RecommendationVote.withCriteria {
				eq('recommendation', reco)
				eq('observation', this)
				min('votedOn')				
			}
			
			map.put("authors", recos.collect{it.author})
			map.put("votedOn", recos.collect{it.votedOn})
			
			map.put("noOfVotes", recoVote[1]);
			result.add(map);
		}
		return ['recoVotes':result, 'totalVotes':this.recommendationVote.size()];
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
	
	
}
