package species.participation

import grails.converters.JSON

class ActivityFeedController {

	def activityFeedService;
	def springSecurityService;
	
	def getFeeds = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		
		def feeds = activityFeedService.getActivityFeeds(params);
		if(!feeds.isEmpty()){
			if(params.checkFeed){
				def m = [feedAvailable:true]
				render m as JSON;
			}
			else{
			def showFeedListHtml = g.render(template:"/common/activityfeed/showActivityFeedListTemplate", model:[feeds:feeds, feedType:params.feedType, feedPermission:params.feedPermission]);
			def olderTimeRef = feeds.last().lastUpdated.time.toString()
			def newerTimeRef = feeds.first().lastUpdated.time.toString()
			def result = [showFeedListHtml:showFeedListHtml, olderTimeRef:olderTimeRef, newerTimeRef:newerTimeRef, currentTime:new Date().getTime()]
			if(params.refreshType == activityFeedService.MANUAL){
				params.refTime = feeds.last().lastUpdated.time.toString()
				result["remainingFeedCount"] = activityFeedService.getCount(params);
			}
			render result as JSON
			}
		}else{
			render [:] as JSON
		}
	}
	
	def getServerTime = {
		render ("" + new Date().getTime()) as JSON
	}
	
	def index = {
		redirect(action:list, params:['feedType':params.feedType, 'feedCategory':params.feedCategory])
	}
	
	def list = {
		log.debug params
		['feedType':params.feedType, 'feedCategory':params.feedCategory]
	}
}
