package species.participation

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import species.participation.Follow
import species.auth.SUser


class ActivityFeedController {
	
	static defaultAction = "list"
	
	def activityFeedService;
	def springSecurityService;
	def messageSource;
	
	def getFeeds(){
		//log.debug params;
		params.author = springSecurityService.currentUser;
		def feeds = activityFeedService.getActivityFeeds(params);
		if(!feeds.isEmpty()){
			if(params.checkFeed){
				def m = [feedAvailable:true]
				render m as JSON;
			}
			else{
				def showFeedListHtml = g.render(template:"/common/activityfeed/showActivityFeedListTemplate", model:[feeds:feeds, feedType:params.feedType, feedPermission:params.feedPermission, feedHomeObject:activityFeedService.getDomainObject(params.feedHomeObjectType, params.feedHomeObjectId)]);
                showFeedListHtml = showFeedListHtml.replaceAll('\\n|\\t','');
				def newerTimeRef = (params.feedOrder == activityFeedService.LATEST_FIRST) ? feeds.first().lastUpdated.time.toString() : feeds.last().lastUpdated.time.toString()
				def olderTimeRef = (params.feedOrder == activityFeedService.LATEST_FIRST) ? feeds.last().lastUpdated.time.toString() : feeds.first().lastUpdated.time.toString()
				def result = [showFeedListHtml:showFeedListHtml, olderTimeRef:olderTimeRef, newerTimeRef:newerTimeRef, currentTime:new Date().getTime()]
				if(params.refreshType == activityFeedService.MANUAL){
					params.refTime = olderTimeRef
					result["remainingFeedCount"] = activityFeedService.getCount(params);
				}
				render result as JSON
				}
		}else{
			render [:] as JSON
		}
	}
	
	def getServerTime () {
		log.debug params
		render ("" + new Date().getTime()) as JSON
	}
	
	def index() {
		redirect(params:params)
	}
	
	def list() {
		log.debug params
		['feedType':params.feedType, 'feedCategory':params.feedCategory]
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Follow Related////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Secured(['ROLE_USER'])
	def follow() {
		log.debug params
		def author = springSecurityService.currentUser;
		def domainObj = activityFeedService.getDomainObject(params.className, params.id);
        def msg
		if(params.follow.toBoolean()){
			Follow.addFollower(domainObj, author)
			msg = messageSource.getMessage("default.followed", null, request.locale)
			if(!author.sendNotification){
				msg += messageSource.getMessage("default.turn.notification", null, request.locale)
			}
		}else{
			Follow.deleteFollower(domainObj, author)
			msg = messageSource.getMessage("default.unfollowed", null, request.locale)
		}
		def r = [status:'success']
		r['msg']= msg 
		render r as JSON
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// COMMENT THIS /////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Secured(['ROLE_ADMIN'])
	def migrateFeedForPost() {
		def wgpGroup = species.groups.UserGroup.read(1)
		def author = SUser.read(1426)
		def resList = wgpGroup.species.collect{it}
		ActivityFeed.withTransaction(){
			activityFeedService.addFeedOnGroupResoucePull(resList, wgpGroup, author, true, false, true, true)
		}
	}
}
