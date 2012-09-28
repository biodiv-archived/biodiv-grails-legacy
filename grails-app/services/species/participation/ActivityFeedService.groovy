package species.participation

import java.text.SimpleDateFormat

import species.groups.UserGroup;

class ActivityFeedService {
	
	static final String COMMENT_ADDED = "Comment added" 
	
	//observation related
	static final String OBSERVATION_CREATED = "Observation created"
	static final String OBSERVATION_UPDATED = "Observation updated"
	static final String OBSERVATION_FLAGGED = "Observation flagged"
	static final String SPECIES_RECOMMENDED = "Species recommended"
	static final String SPECIES_AGREED_ON = "Species agreed on"
	static final String OBSERVATION_POSTED_ON_GROUP = "Observation posted on group"
	static final String OBSERVATION_REMOVED_FROM_GROUP = "Observation removed from group"
	
	//group related
	static final String USERGROUP_CREATED = "User group created"
	static final String USERGROUP_UPDATED = "User group updated"
	static final String MEMBER_JOINED = "New member joined"
	static final String MEMBER_ROLE_UPDATED = "Member role updated"
	static final String MEMBER_LEFT = "Member left"
	
	static final String OLDER = "older"
	static final String NEWER = "newer"
	
	static final String READ_ONLY = "readOnly"
	static final String EDITABLE = "editable"
	
	static final String AUTO = "auto"
	static final String MANUAL= "manual"
	
	static final String LATEST_FIRST = "latestFirst"
	static final String OLDEST_FIRST = "oldestFirst"
	
	static final String GENERIC = "Generic"
	static final String SPECIFIC = "Specific"
	static final String SELECTED = "Selected"
	static final String GROUP_SPECIFIC = "GroupSpecific"
	static final String MY_FEEDS = "MyFeeds"
	
	
	static final String ALL = "All"
	static final String OTHER = "other"
	
	private static DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	
	
	static transactional = false
	
	def grailsApplication
	
	def getActivityFeeds(params){
		log.debug params;
		def feeds = ActivityFeed.fetchFeeds(params)
		if(params.feedOrder == OLDEST_FIRST){
			feeds = feeds.reverse()
		}
		return aggregateFeeds(feeds, params)
	}
	
	def getCount(params){
		return ActivityFeed.fetchCount(params)
	}
	
	def addActivityFeed(rootHolder, activityHolder, author, activityType){
		//to support discussion on comment thread
		def subRootHolderType = rootHolder?.class?.getCanonicalName()
		def subRootHolderId = rootHolder?.id
		if(activityHolder?.class?.getCanonicalName() == Comment.class.getCanonicalName()){
			subRootHolderType = activityHolder.class.getCanonicalName()
			subRootHolderId = (activityHolder.isMainThread())? activityHolder.id : activityHolder.fetchMainThread().id
		}
			
		ActivityFeed af = new ActivityFeed(author:author, activityHolderId:activityHolder?.id, \
						activityHolderType:activityHolder?.class?.getCanonicalName(), \
						rootHolderId:rootHolder?.id, rootHolderType:rootHolder?.class?.getCanonicalName(), \
						activityType:activityType, subRootHolderType:subRootHolderType, subRootHolderId:subRootHolderId);
					
		ActivityFeed.withNewSession {
			if(!af.save(flush:true)){
				af.errors.allErrors.each { log.error it }
				return null
			}else{
				return af
			}
		}
	}
	
	def getDomainObject(className, id){
		if(!className || className.trim() == ""){
			return null
		}
		
		id = id.toLong()
		return grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
	}
	
	private aggregateFeeds(List feeds, params){
		if(params.feedType == SPECIFIC || params.checkFeed){
			return feeds
		}
		
		// aggregating object based on feed type
		Set obvFeedSet = new HashSet()
		Set commentFeedSet = new HashSet()
		Set otherFeedSet = new HashSet()
		def retList = []
		feeds.each { it ->
			if(it.rootHolderType == Observation.class.getCanonicalName()){
				//aggregating observation object
				def feedKey = it.rootHolderType + it.rootHolderId;
				if(!obvFeedSet.contains(feedKey)){
					retList.add(it)
					obvFeedSet.add(feedKey)
				}
			}else if(it.rootHolderType == UserGroup.class.getCanonicalName() && it.subRootHolderType == Comment.class.getCanonicalName()){
				//aggregating comment
				def feedKey = it.subRootHolderType + it.subRootHolderId;
				if(!commentFeedSet.contains(feedKey)){
					retList.add(it)
					commentFeedSet.add(feedKey)
				}
			}else if(params.feedType == GROUP_SPECIFIC){
				//adding object as it is if group specific object	
				retList.add(it)
			}else{
				//aggregating other object as well if its not specific to group(ie. myfeeds )
				def feedKey = it.rootHolderType + it.rootHolderId;
				if(!otherFeedSet.contains(feedKey)){
					retList.add(it)
					otherFeedSet.add(feedKey)
				}
			}
		}
		return retList
	}
	
	def deleteFeed(obj){
		ActivityFeed.deleteFeed(obj);
	}
	
//	static getDateInISO(date){
//		return date.getTime()//DATE_FORMAT.format(date)
//	}
}
