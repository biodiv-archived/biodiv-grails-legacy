package species.participation

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.auth.SUser
import species.groups.UserGroup;
import species.participation.ActivityFeedService

class ActivityFeed {
	def activityFeedService;
	
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
	
	//subroot : this is to support aggregation on commnet(ie. thread) on group or obv
	//it will be null for all except the commnet activity where is will store main comment thread
	Long subRootHolderId;
	String subRootHolderType;
	
	static belongsTo = [author:SUser];

	static constraints = {
		activityRootType nullable:true;
		rootHolderId nullable:true;
		rootHolderType nullable:true;
		activityType nullable:true;
		activityDescrption nullable:true;
		activityHolderId nullable:true;
		activityHolderType nullable:true;
		subRootHolderId nullable:true;
		subRootHolderType nullable:true;
	}

	static mapping = {
		version : false;
		
		//fething this right away
		author fetch: 'join'
		
		rootHolderId index: 'rootHolderId_Index'
		rootHolderType index: 'rootHolderType_Index'
		lastUpdated index: 'lastUpdated_Index'
		
		subRootHolderId index: 'subRootHolderId_Index'
		subRootHolderType index: 'subRootHolderType_Index'
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
				if(params.isCommentThread){
					eq('subRootHolderType', params.subRootHolderType)
					eq('subRootHolderId', params.subRootHolderId)
				}else{
					switch (feedType) {
						case ActivityFeedService.GENERIC:
							eq('rootHolderType', params.rootHolderType)
							break
						case ActivityFeedService.SPECIFIC:
							eq('rootHolderType', params.rootHolderType)
							eq('rootHolderId', params.rootHolderId.toLong())
							break
						case [ActivityFeedService.GROUP_SPECIFIC, ActivityFeedService.MY_FEEDS]:
							or{
								params.typeToIdFilterMap.each{key, value ->
									if(!value.isEmpty()){ 
										and{
											eq('rootHolderType', key)
											'in'('rootHolderId', value)
										}
									}
								}
								if(params.showUserFeed){
									and{
										eq('author', params.author)
										eq('rootHolderType', Observation.class.getCanonicalName())
									}
								}
							}
						default:
							break
					}
				}
				if(params.feedCategory && params.feedCategory.trim() != ActivityFeedService.ALL){
					eq('rootHolderType', params.feedCategory)
				}
				if(params.feedClass){
					eq('activityHolderType', params.feedClass)
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
				if(params.isCommentThread != null && params.isCommentThread.toBoolean()){
					eq('subRootHolderType', params.subRootHolderType)
					eq('subRootHolderId', params.subRootHolderId)
				}else{
					switch (feedType) {
						case ActivityFeedService.GENERIC:
							eq('rootHolderType', params.rootHolderType)
							break
						case [ActivityFeedService.SPECIFIC, ActivityFeedService.GROUP_SPECIFIC]:
							eq('rootHolderType', params.rootHolderType)
							eq('rootHolderId', params.rootHolderId.toLong())
							break
						default:
							break
					}
				}
				(params.timeLine == ActivityFeedService.OLDER) ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
			}
		}[0]
	}

	private static validateParams(params){
		params.feedType = params.feedType ?: ActivityFeedService.ALL
		params.checkFeed = (params.checkFeed != null)? params.checkFeed.toBoolean() : false
		params.isCommentThread = (params.isCommentThread != null)? params.isCommentThread.toBoolean() : false
		
		if(params.isCommentThread){
			params.subRootHolderId = params.subRootHolderId.toLong()
		}
		
		switch (params.feedType) {
			//to handle complete list (ie groups, obvs, species)
			case ActivityFeedService.GENERIC:
				if(!params.rootHolderType || params.rootHolderType.trim() == ""){
					return false
				}
				break
			//to search for only specific object	
			case ActivityFeedService.SPECIFIC:
				if(!params.rootHolderType || params.rootHolderType.trim() == "" || !params.rootHolderId || params.rootHolderId.trim() == ""){
					return false
				}
				break
			
			case ActivityFeedService.GROUP_SPECIFIC:
				if(!params.rootHolderType || params.rootHolderType.trim() != UserGroup.class.getCanonicalName()){ 
					return false
				}
				def groups = [UserGroup.read(params.rootHolderId.toLong())]
				params.typeToIdFilterMap = getGroupAndObsevations(groups)
				break
			
			case ActivityFeedService.MY_FEEDS:
				if(!params.author){
					return false
				}
				def groups = params.author.getUserGroups()
				params.showUserFeed = true
				params.typeToIdFilterMap = getGroupAndObsevations(groups)
				break
				
			default:
				break
		}
		
		params.timeLine = params.timeLine?:ActivityFeedService.OLDER
		params.refTime = params.refTime?:new Date().time.toString()
		
		params.max = params.max ?: ((params.timeLine == ActivityFeedService.OLDER) ? ((params.feedType == ActivityFeedService.SPECIFIC) ? 2 : 5)  :null)
		
		return true
	}
	
	def isMainThreadComment(){
		return (activityHolderId == subRootHolderId && activityHolderType == subRootHolderType)
	}
	
	def fetchMainCommentFeed(){
		return ActivityFeed.findByActivityHolderTypeAndActivityHolderId(subRootHolderType, subRootHolderId)
	}
	
	def showComment(){
		return (rootHolderType == Observation.class.getCanonicalName() || !isMainThreadComment())
	}
	
	def fetchUserGroup(){
		if(rootHolderType == UserGroup.class.getCanonicalName()){
			return activityFeedService.getDomainObject( rootHolderType, rootHolderId)
		}
		return null
	}
	
	private static getGroupAndObsevations(groups){
		def m = [:]
		m[Observation.class.getCanonicalName()] = getObvIds(groups)
		m[UserGroup.class.getCanonicalName()] = groups.collect{ it.id}
		return m
	}
	
	private static getObvIds(groups){
		def obvIds = []
		groups.each{ it ->
			obvIds.addAll(it.observations.collect{it.id})
		}
		return obvIds
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
					and{
						eq('subRootHolderType', type)
						eq('subRootHolderId', id)
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
