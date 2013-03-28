package species

import species.participation.ChecklistRowData;
import species.participation.Comment

class ActivityFeedTagLib {
	static namespace = "feed"
	
	def activityFeedService
	
	def showActivityFeed = {attrs, body->
		def model = attrs.model
		//model.feedPermission = model.feedPermission?:activityFeedService.READ_ONLY
		out << render(template:"/common/activityfeed/showActivityFeedTemplate", model:attrs.model);
	}
	
	def showActivityFeedList = {attrs, body->
		out << render(template:"/common/activityfeed/showActivityFeedListTemplate", model:attrs.model);
	}
	
	def showActivityFeedContext = {attrs, body->
		def model = attrs.model
		model.feedParentInstance = activityFeedService.getDomainObject( model.feedInstance.rootHolderType, model.feedInstance.rootHolderId)
		out << render(template:"/common/activityfeed/showActivityFeedContextTemplate", model:attrs.model);
	}
	
	def showAggregateFeed = {attrs, body->
		out << render(template:"/common/activityfeed/showAggregateFeedTemplate", model:attrs.model);
	}
	
	def showSpecificFeed = {attrs, body->
		out << render(template:"/common/activityfeed/showSpecificFeedTemplate", model:attrs.model);
	}
	
	def showFeedWithFilter = {attrs, body->
		def model = attrs.model
		model.feedType = model.feedType ?: activityFeedService.ALL
		model.feedCategory = model.feedCategory ?: activityFeedService.ALL
		out << render(template:"/common/activityfeed/showFeedWithFilterTemplate", model:attrs.model);
	}
	
	def showFeedFilter = {attrs, body->
		out << render(template:"/common/activityfeed/showFeedFilterTemplate", model:attrs.model);
	}
	
	def showAllActivityFeeds = {attrs, body->
		def model = attrs.model
		
		def refTime = new Date().time.toString()
		model.newerTimeRef = model.olderTimeRef = refTime
		model.feedOrder = model.feedOrder?:activityFeedService.OLDEST_FIRST
		model.feedPermission = model.feedPermission?:activityFeedService.READ_ONLY
		model.refreshType = model.refreshType ?:activityFeedService.AUTO
		if(model.refreshType == activityFeedService.MANUAL){
			preloadFeeds(model)
		}else{
			model.feeds = []
		}
		out << render(template:"/common/activityfeed/showAllActivityFeedTemplate", model:model);
	}
	
	private preloadFeeds(model){
		def newParams = new HashMap(model)
		
		newParams["rootHolderType"] = model.rootHolder?.class?.getCanonicalName()
		newParams["rootHolderId"] = "" + model.rootHolder?.id
		newParams["max"] = 2
		
		model.feeds = activityFeedService.getActivityFeeds(newParams);
		if(model.feeds){
			model.newerTimeRef = (model.feedOrder == activityFeedService.LATEST_FIRST) ? model.feeds.first().lastUpdated.time.toString() :  model.feeds.last().lastUpdated.time.toString()
			model.olderTimeRef = (model.feedOrder == activityFeedService.LATEST_FIRST) ? model.feeds.last().lastUpdated.time.toString() :  model.feeds.first().lastUpdated.time.toString() 
		}else{
			def refTime = new Date().time.toString()
			model.newerTimeRef = model.olderTimeRef = refTime
		}
		newParams.refTime = model.olderTimeRef
		model.remainingFeedCount = activityFeedService.getCount(newParams);
	}
	
	def showActivity = {attrs, body->
		def model = attrs.model
		def activityDomainObj = activityFeedService.getDomainObject( model.feedInstance.activityHolderType, model.feedInstance.activityHolderId)
		def result = activityFeedService.getContextInfo(model.feedInstance, params)
		model.activityInstance = activityDomainObj
		model.feedText = result.text
		model.activityTitle = result.activityTitle
		out << render(template:"/common/activityfeed/showActivityTemplate", model:attrs.model);
	}
	
	def follow = {attrs, body->
		out << render(template:"/common/followTemplate", model:attrs.model);
	}
		
}
