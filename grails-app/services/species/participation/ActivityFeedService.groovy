package species.participation

import java.text.SimpleDateFormat

class ActivityFeedService {
	
	static final String COMMENT_ADDED = "Comment added" 
	
	//observation related
	static final String OBSERVATION_CREATED = "Observation created"
	static final String OBSERVATION_UPDATED = "Observation updated"
	static final String OBSERVATION_FLAGGED = "Observation flagged"
	static final String SPECIES_RECOMMENDED = "Species recommended"
	static final String SPECIES_AGREED_ON = "Species agreed on"
	
	//group related
	static final String USERGROUP_CREATED = "User group created"
	static final String USERGROUP_UPDATED = "User group updated"
	static final String OBSERVATION_POSTED_ON_GROUP = "Observation posted on group"
	static final String OBSERVATION_REMOVED_FROM_GROUP = "Observation removed from group"
	static final String MEMBER_JOINED = "New member joined"
	static final String MEMBER_ROLE_UPDATED = "Member role updated"
	static final String MEMBER_LEFT = "Member left"
	
	static final String OLDER = "older"
	static final String NEWER = "newer"
	
	static final String READ_ONLY = "readOnly"
	static final String EDITABLE = "editable"
	
	static final String AUTO = "auto"
	static final String MANUAL= "manual"
	
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
		def feeds = ActivityFeed.fetchFeeds(params)
		return aggregateFeeds(feeds, params)
	}
	
	def getCount(params){
		return ActivityFeed.fetchCount(params)
	}
	
	def addActivityFeed(rootHolder, activityHolder, author, activityType){
		ActivityFeed af = new ActivityFeed(author:author, activityHolderId:activityHolder?.id, \
						activityHolderType:activityHolder?.class?.getCanonicalName(), \
						rootHolderId:rootHolder?.id, rootHolderType:rootHolder?.class?.getCanonicalName(), \
						activityType:activityType);
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
	
	//XXX needs to handle this on query level
	private aggregateFeeds(List feeds, params){
		if(params.feedType == SPECIFIC || params.checkFeed){
			return feeds
		}
		
		// aggregating only observation object
		if(params.feedType == GROUP_SPECIFIC){
			Set feedSet = new HashSet()
			def retList = []
			feeds.each { it ->
				if(it.rootHolderType != Observation.class.getCanonicalName()){
					retList.add(it)
				}else{
					def feedKey = it.rootHolderType + it.rootHolderId;
					if(!feedSet.contains(feedKey)){
						retList.add(it)
						feedSet.add(feedKey)
					}
				}
			}
			return retList
		}
		
		//aggregating all the object
		Set feedSet = new HashSet()
		def retList = []
		feeds.each { it ->
			def feedKey = it.rootHolderType + it.rootHolderId;
			if(!feedSet.contains(feedKey)){
				retList.add(it)
				feedSet.add(feedKey)
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
