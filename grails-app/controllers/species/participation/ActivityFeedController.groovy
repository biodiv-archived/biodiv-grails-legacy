package species.participation

import grails.converters.JSON
import grails.plugin.springsecurity.Secured
import species.participation.Follow
import species.auth.SUser

class ActivityFeedController {

	def activityFeedService;
	def springSecurityService;
	
	def getFeeds = {
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
	
	def getServerTime = {
		log.debug params
		render ("" + new Date().getTime()) as JSON
	}
	
	def index = {
		redirect(action:list, params:params)
	}
	
	def list = {
		log.debug params
		['feedType':params.feedType, 'feedCategory':params.feedCategory]
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Follow Related////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Secured(['ROLE_USER'])
	def follow = {
		log.debug params
		def author = springSecurityService.currentUser;
		def domainObj = activityFeedService.getDomainObject(params.className, params.id);
        def msg
		if(params.follow.toBoolean()){
			Follow.addFollower(domainObj, author)
			msg = "Followed..."
			if(!author.sendNotification){
				msg += " Please turn on notification mail from your profile page."
			}
		}else{
			Follow.deleteFollower(domainObj, author)
			msg = "Unfollowed..."
		}
		def r = [status:'success']
		r['msg']= msg 
		render r as JSON
	}
}
