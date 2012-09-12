package species.participation

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.auth.SUser
import species.participation.ActivityFeedService

class ActivityFeed {
	//activity basic info
	Date dateCreated;
	Date lastUpdated;

	//root holder(i.e observation, group)
	String activityRootType; //i.e domain or none
	Long rootHolderId;
	String rootHolderType;

	String activityType; // fileuplaod, join/unjoin
	String activityDescrption;
	
	//activity holder (i.e recoVote, image)
	Long activityHolderId; 
	String activityHolderType;

	static belongsTo = [author:SUser];

	static constraints = {
		activityRootType nullable:true;
		rootHolderId nullable:true;
		rootHolderType nullable:true;
		activityType nullable:true;
		activityDescrption nullable:true;
		activityHolderId nullable:true;
		activityHolderType nullable:true;
	}

	static mapping = {
		version : false;
	}

	static fetchFeeds(params){
		if(validateParams(params)){
			return fetchRequiredFeeds(params)
		}
		return Collections.EMPTY_LIST
	}
	
	static fetchRequiredFeeds(params){
		def feedType = params.feedType
		
		def refTime = getDate(params.refTime)
		if(!refTime){
			return Collections.EMPTY_LIST
		}
		
		return ActivityFeed.withCriteria(){
			and{
				switch (feedType) {
					case ActivityFeedService.GENERIC:
						eq('rootHolderType', params.rootHolderType)
						break
					case ActivityFeedService.SPECIFIC:
						eq('rootHolderType', params.rootHolderType)
						eq('rootHolderId', params.rootHolderId.toLong())
						break
					default:
						break
				}
				(params.timeLine == ActivityFeedService.OLDER) ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
			}
			if(params.max){
				maxResults params.max
			}
			order 'lastUpdated', 'desc'
		}
	}
	
	//XXX: right now its only usable for specific type. Needs to handle it for aggregate(i.e generic, all) type
	static int fetchCount(params){
		def feedType = params.feedType
		
		def refTime = getDate(params.refTime)
		if(!refTime){
			return 0
		}
		return ActivityFeed.withCriteria(){
			projections {
				count('id')
			}
			and{
				switch (feedType) {
					case ActivityFeedService.GENERIC:
						eq('rootHolderType', params.rootHolderType)
						break
					case ActivityFeedService.SPECIFIC:
						eq('rootHolderType', params.rootHolderType)
						eq('rootHolderId', params.rootHolderId.toLong())
						break
					default:
						break
				}
				(params.timeLine == ActivityFeedService.OLDER) ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
			}
		}[0]
	}

	private static validateParams(params){
		params.feedType = params.feedType ?: ActivityFeedService.ALL
		switch (params.feedType) {
			case ActivityFeedService.GENERIC:
				if(!params.rootHolderType || params.rootHolderType.trim() == ""){
					return false
				}
				break
			case ActivityFeedService.SPECIFIC:
				if(!params.rootHolderType || params.rootHolderType.trim() == "" || !params.rootHolderId || params.rootHolderId.trim() == ""){
					return false
				}
				break
			default:
				break
		}
		
		params.timeLine = params.timeLine?:ActivityFeedService.OLDER
		params.refTime = params.refTime?:((params.timeLine == ActivityFeedService.OLDER) ?  new Date().time.toString(): new Date().previous().time.toString())
		
		params.max = params.max ?: ((params.timeLine == ActivityFeedService.OLDER) ? ((params.feedType == ActivityFeedService.SPECIFIC) ? 2 : 5)  :null)
		
		return true
	}
	
	private static getDate(String timeIn){
		if(!timeIn){
			return null
		}
		
		try{
			return new Date(timeIn.toLong())
		}catch (Exception e) {
			e.printStackTrace()
		}
		return null
	}
		
	
	static deleteFeed(obj){
		def type = obj.class.getCanonicalName()
		def id = obj.id
		List<ActivityFeed> feeds = ActivityFeed.withCriteria(){
				or{
					and{
						eq('rootHolderType', type)
						eq('rootHolderId', id)
					}
					and{
						eq('activityHolderType', type)
						eq('activityHolderId', id)
					}
				}
			}
		
		feeds.each{ ActivityFeed af ->
			try{
				ActivityFeed.withNewSession {
					af.delete(flush:true)
				}
			}catch(Exception e){
				e.printStackTrace()
			}
		}
	}
}
