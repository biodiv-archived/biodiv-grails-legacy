package species.participation

import groovy.sql.Sql;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import content.eml.Document;

import species.Species;
import species.auth.SUser
import species.groups.UserGroup;
import species.participation.ActivityFeedService

class ActivityFeed {
	def activityFeedService;
	def dataSource
	
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
	
	boolean isShowable = true;
	
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
			return execuateQuery(params, false)
		}
		return Collections.EMPTY_LIST
	}
	
	//XXX: right now its only usable for specific type. Needs to handle it for aggregate(i.e generic, all) type
	static int fetchCount(params){
		if(validateParams(params)){
			return execuateQuery(params, true)
		}
		return 0
	}

	
	static execuateQuery(params, boolean isCountQuery){
 		def feedType = params.feedType
		
		def refTime = getDate(params.refTime)
		if(!refTime){
			return isCountQuery?0:Collections.EMPTY_LIST
		}
		
		
		def map = [:]
		String selectClause  = isCountQuery? "select count(*) as c " :"select af.id as id, af.last_updated as updatetime "
		String fromClause = " from activity_feed af "
		String whereClause = " where af.root_holder_type != '" + SUser.class.getCanonicalName() + "' "
		String orderClause = isCountQuery?"":" order by af.last_updated desc limit " + params.max 
		
		if(params.isShowableOnly){
			whereClause += " and af.is_showable = :isShowable "
			map['isShowable'] = params.isShowableOnly
		}
		
		if(params.feedCategory && params.feedCategory.trim() != ActivityFeedService.ALL){
			whereClause += " and af.root_holder_type = :rootHolderType "
			map['rootHolderType'] = params.feedCategory
		}
		
		if(params.feedClass){
			whereClause += " and af.activity_holder_type = :activityHolderType "
			map['activityHolderType'] = params.feedClass
		}
		
		
		if(params.timeLine == ActivityFeedService.OLDER){
			whereClause += " and af.last_updated < :lastUpdated "
		}else{
			whereClause += " and af.last_updated > :lastUpdated "
		}
		map['lastUpdated'] = refTime
		
		List queryList
		
		if(params.isCommentThread){
			whereClause += " and af.sub_root_holder_type = :subRootHolderType "
			whereClause += " and af.sub_root_holder_id = :subRootHolderId "
			map['subRootHolderType'] = params.subRootHolderType
			map['subRootHolderId'] = params.subRootHolderId
		}else{
			switch (feedType) {
				case ActivityFeedService.GENERIC:
					whereClause += " and af.root_holder_type = :rootHolderType "
					map['rootHolderType'] = params.rootHolderType
					break
				case ActivityFeedService.SPECIFIC:
					whereClause += " and af.root_holder_type = :rootHolderType "
					whereClause += " and af.root_holder_id = :rootHolderId "
					map['rootHolderType'] = params.rootHolderType
					map['rootHolderId'] = params.rootHolderId.toLong()
					break
				case [ActivityFeedService.USER, ActivityFeedService.GROUP_SPECIFIC, ActivityFeedService.MY_FEEDS]:
					if(feedType == ActivityFeedService.USER){
						whereClause += " and af.author_id = :authorId "
						map['authorId'] = params.user.toLong()
					}
					if(params.userGroupList){
						queryList = getUserGroupFeedQuery(params.userGroupList, selectClause, fromClause, whereClause, orderClause)
					}
					break
		
				default:
					break
			}
		}
		
		//if query without group 
		queryList = queryList?:[selectClause + fromClause + whereClause + orderClause]
		
		def result =  new ActivityFeed().getResult(queryList, map, isCountQuery, params.max)
		println "=============== final reustls ===================================" 
		println result
		println "=============== final reustls ==================================="
		return result 
	} 
	
	private getResult(List queryList, Map m,  boolean isCountQuery, max){
		def result = []
		def sql =  Sql.newInstance(dataSource)
		queryList.each { q ->
			//println "================= Query =========== " 
			//println q
			//println m
			//println "================= Query =========== "
			result.addAll(sql.rows(q, m))
		}
		
		//println "====== raw result  = " + result
		
		if(isCountQuery){
			int c = 0
			result.each { r ->
 				c += r.c
			}
			return c
		}
		
		Collections.sort(result , new Comparator(){
			public int compare(s1, s2){
				s2.updatetime - s1.updatetime
			}});
		
		
		def limitedResult = []
		int i = 0;
		result.each {
			if(++i <= max){
				limitedResult << ActivityFeed.read(it.id)
			}
		}
			
		return limitedResult
	}
	
	

	private static validateParams(params){
		params.feedType = params.feedType ?: ActivityFeedService.ALL
		params.checkFeed = (params.checkFeed != null)? params.checkFeed.toBoolean() : false
		params.isCommentThread = (params.isCommentThread != null)? params.isCommentThread.toBoolean() : false
		// in default showing only feeds having isShowable = true 
		params.isShowableOnly = true
		
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
				//in this perticular case showing all feeds
				params.isShowableOnly = false
				break
			
			case ActivityFeedService.GROUP_SPECIFIC:
				if(!params.rootHolderType || params.rootHolderType.trim() != UserGroup.class.getCanonicalName()){ 
					return false
				}
				params.userGroupList = [UserGroup.read(params.rootHolderId.toLong())]
				break
			
			case ActivityFeedService.MY_FEEDS:
				if(!params.author){
					return false
				}
				params.userGroupList = params.author.getUserGroups()
				params.showUserFeed = true
				break
				
            case ActivityFeedService.USER:
                if(params.userGroupFromUserProfile){
                    params.userGroupList = [UserGroup.read(params.userGroupFromUserProfile.toLong())]
                }
                if(!params.user){
					return false
				}
				break
			default:
				break
		}
		
		params.timeLine = params.timeLine?:ActivityFeedService.OLDER
		params.refTime = params.refTime?:((params.timeLine == ActivityFeedService.OLDER) ?  new Date().time.toString(): new Date().previous().time.toString())
		
		params.max = params.max ?: ((params.timeLine == ActivityFeedService.OLDER) ? ((params.feedType == ActivityFeedService.SPECIFIC) ? 5 : 5)  :null)
		
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
		m[Species.class.getCanonicalName()] = getSpeciesIds(groups)
		m[Document.class.getCanonicalName()] = getDocIds(groups)
		return m
	}
	
	private static getObvIds(groups){
		def obvIds = []
		groups.each{ it ->
			obvIds.addAll(it.observations.collect{it.id})
		}
		return obvIds
	}
	
	private static getSpeciesIds(groups){
		def obvIds = []
		groups.each{ it ->
			obvIds.addAll(it.species.collect{it.id})
		}
		return obvIds
	}
	
	private static getDocIds(groups){
		def obvIds = []
		groups.each{ it ->
			obvIds.addAll(it.documents.collect{it.id})
		}
		return obvIds
	}
	
	
	private static getDate(String timeIn){
		if(!timeIn){
			return null
		}
		
		try{
			return new java.sql.Date(timeIn.toLong())
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
	
	
	static updateIsDeleted(obj){
		if(!obj || !obj.hasProperty('isDeleted')){
			return
		}
		setShowable(obj, !obj.isDeleted)
	}

	
	static updateIsShowable(obj){
		if(!obj || !obj.hasProperty('isShowable')){
			return
		}
		setShowable(obj, obj.isShowable)
	}
	
	private static setShowable(obj, boolean isShowable){
		def type = obj.class.getCanonicalName()
		def id = obj.id
		ActivityFeed.withTransaction {
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
			feeds.each{ af ->
					af.isShowable = isShowable
					af.save(flush:true)
			}
		}
	}

    def contextInfo(params=null) {
        return activityFeedService.getContextInfo(this,params);
    }
	
	private static List getUserGroupFeedQuery(List ugs, String selectClause, String  fromClause, String whereClause, String orderClause){
		List queryList = []
		ugs.each { ug ->
			getUserGroupFeedQuery(ug, fromClause, whereClause).each { query ->
				queryList.add(selectClause + query + orderClause)
			}
		}
		return queryList
	}
	
	private static List getUserGroupFeedQuery(UserGroup ug, String fromClause, String whereClause ){
		//def uid = ug.id
		String obvQuery = " ${fromClause}, user_group_observations ugo ${whereClause} and ((ugo.user_group_id = ${ug.id} and af.root_holder_type = 'species.participation.Observation' and af.root_holder_id = ugo.observation_id))" 
		String spQuery = " ${fromClause}, user_group_species ugs ${whereClause} and ((ugs.user_group_id = ${ug.id} and af.root_holder_type = 'species.Species' and af.root_holder_id = ugs.species_id)) "
		String docQuery = " ${fromClause}, user_group_documents ugd ${whereClause} and ((ugd.user_group_id =  ${ug.id} and af.root_holder_type = 'content.eml.Document' and af.root_holder_id = ugd.document_id))"
		String groupQuery = " ${fromClause} ${whereClause} and ((af.root_holder_type = 'species.groups.UserGroup' and af.root_holder_id = ${ug.id})) "

		return [spQuery, obvQuery,docQuery, groupQuery]
	}
	
	public static int getActivityCount(Date startDate, Date endDate, UserGroup ug, feedType=null){
		
		def map = [:]
		String selectClause  = "select count(*) as c "
		String fromClause = " from activity_feed af "
		String whereClause = " where af.is_showable = :isShowable "
		map['isShowable'] = true
		
		if(feedType){
			whereClause += " and af.activity_type = :activityType "
			map['activityType'] = feedType
		}
		
		
		if(startDate && endDate){
			whereClause += " and af.last_updated between :startDate and :endDate "
		}
		map['startDate'] = new java.sql.Date(startDate.getTime())
		map['endDate'] = new java.sql.Date(endDate.getTime())
		
		
		List queryList
		if(ug){
			queryList = getUserGroupFeedQuery(ug, fromClause, whereClause).collect { selectClause + it }
		}else{
			queryList = [selectClause + fromClause + whereClause]
		}
		
		return new ActivityFeed().getResult(queryList, map, true, null)
	}

}
