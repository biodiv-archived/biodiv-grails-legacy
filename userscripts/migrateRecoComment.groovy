import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;

import species.participation.ActivityFeed;
import species.participation.ActivityFeedService;
import species.participation.Follow;
import species.participation.RecommendationVote
import species.participation.Comment

def activityFeedService = ctx.getBean("activityFeedService");

def migrate(){
	RecommendationVote.findAllByCommentIsNotNull().each { RecommendationVote rv ->
		
		def commentHolderId = rv.recommendation.id
		def commentHolderType = rv.recommendation.class.getName()
		
		def rootHolderId = rv.observation.id
		def rootHolderType = rv.observation.class.getName()
		
		def dateCreated = rv.votedOn
		def lastUpdated = dateCreated
		
		Comment c = new Comment(author:rv.author, body:rv.comment, commentHolderId:commentHolderId, \
							commentHolderType:commentHolderType, rootHolderId:rootHolderId, \
							rootHolderType:rootHolderType, dateCreated:dateCreated, lastUpdated:lastUpdated)
		
		if(!c.save(flush:true)){
			c.errors.allErrors.each { println  it }
		}
		
		println "=========== voted on " + dateCreated
		c.dateCreated = dateCreated
		c.lastUpdated = lastUpdated
		
		if(!c.save(flush:true)){
			c.errors.allErrors.each { println  it }
		}
	}
	
	//please run following command on database
	//biodiv=#  update comment set last_updated = date_created;
	//becasue last updated is managed by grails so manully setting its time to same as date_created
}


def addActivityFeed(rootHolder, activityHolder, author, activityType, description, date){
	//to support discussion on comment thread
	def subRootHolderType = rootHolder?.class?.getCanonicalName()
	def subRootHolderId = rootHolder?.id
	if(activityHolder?.class?.getCanonicalName() == Comment.class.getCanonicalName()){
		subRootHolderType = activityHolder.class.getCanonicalName()
		subRootHolderId = (activityHolder.isMainThread())? activityHolder.id : activityHolder.fetchMainThread().id
	}
	
	//date = date ?: new Date()
	ActivityFeed af = new ActivityFeed(author:author, activityHolderId:activityHolder?.id, \
					activityHolderType:activityHolder?.class?.getCanonicalName(), \
					rootHolderId:rootHolder?.id, rootHolderType:rootHolder?.class?.getCanonicalName(), \
					activityType:activityType, subRootHolderType:subRootHolderType, subRootHolderId:subRootHolderId, activityDescrption:description,
					dateCreated :date, lastUpdated:date);
				
	//ActivityFeed.withNewSession {
		if(!af.save(flush:true)){
			af.errors.allErrors.each { log.error it }
			return null
		}
	//}
	Follow.addFollower(rootHolder, author)
	return af
}


def saveFeed(List rvs, activityFeedService){
	/*
	def time = rvs[0].votedOn
	if(rvs.size() > 1){
		rvs.each { rv ->
			if(time > rv.votedOn){
				println "===================== error "
			}
		}
	}
	*/
	if(!rvs || rvs.isEmpty()) return
	println "=================================================================== recos    " + rvs.size()
	int count = 0
	rvs.each { RecommendationVote rv ->
		count++
		def obv = rv.observation
		def auth = rv.author
		if(rvs.size() > 1)
		println "=============== seaching ==========" + rv.id +"  observation " + obv.id + "  authro " + auth + "==== $rv.votedOn "
		def res = ActivityFeed.findWhere(rootHolderType:obv.class.getCanonicalName(), rootHolderId:obv.id, activityHolderType:rv.class.getCanonicalName(), activityHolderId:rv.id, author:auth)
		if(!res){
			def aType = (count > 1 ) ? ActivityFeedService.SPECIES_AGREED_ON : ActivityFeedService.SPECIES_RECOMMENDED
			if(rvs.size() > 1)
			println "=== not found for " + rv.id +"  observation " + obv.id + "  authro " + auth + "==== $rv.votedOn " + "  type " + aType
			addActivityFeed(obv, rv, rv.author, aType, null, rv.votedOn);
			if(rvs.size() > 1 && count > 1 && aType == ActivityFeedService.SPECIES_RECOMMENDED){
				println "Error   =============="
			}
		}else{
			if(rvs.size() > 1)
			println "== found"
		}
	}
	
}

def checkRecoAndFeed(activityFeedService){
	//ActivityFeed.withTransaction(){
		
		def ss = new HashSet()	
	
		def m = GrailsDomainBinder.getMapping(ActivityFeed.class)
		m.autoTimestamp = false
		
		def res = RecommendationVote.findAll("from RecommendationVote as rv order by observation, votedOn asc")
		def obvRv = []
		def currObv = res[0].observation
		def aacount = 1
		res.each { RecommendationVote rv ->
			ss.add(rv.observation)
			def obv = rv.observation
			if(currObv == obv){
				obvRv << rv
			}else{
				// obv is changing so first saving clubbed rvs
				saveFeed(obvRv, activityFeedService)
				aacount++
				obvRv = []
				obvRv << rv
				currObv = obv
			}
		}
		//saving last club
		saveFeed(obvRv, activityFeedService)
		
		m.autoTimestamp = true
	//}
		
		
	println "=================== set size " + ss.size() + "          adn " + aacount
}

checkRecoAndFeed(activityFeedService)

