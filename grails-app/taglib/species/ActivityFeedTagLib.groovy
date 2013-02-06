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
	
	//XXX this should be formatted in better way
	def showActivity = {attrs, body->
		def model = attrs.model
		def activityType = model.feedInstance.activityType
		def activityDomainObj = activityFeedService.getDomainObject( model.feedInstance.activityHolderType, model.feedInstance.activityHolderId)
		def activityRootObj = activityFeedService.getDomainObject( model.feedInstance.rootHolderType, model.feedInstance.rootHolderId)
		
		def text = null
		def activityTitle = null
		
		println "=== feed === $model.feedInstance.id === $model.feedInstance.activityType" 
		switch (activityType) {
			case activityFeedService.COMMENT_ADDED:
				println "came here "
				activityTitle = activityFeedService.COMMENT_ADDED  + activityFeedService.getCommentContext(activityDomainObj, params)
				text = activityDomainObj.body
				break
			case activityFeedService.SPECIES_RECOMMENDED:
				activityTitle = activityFeedService.SPECIES_RECOMMENDED + " " + activityFeedService.getSpeciesNameHtml(activityDomainObj, params)
				break
			case activityFeedService.SPECIES_AGREED_ON:
				activityTitle =  activityFeedService.SPECIES_AGREED_ON + " " + activityFeedService.getSpeciesNameHtml(activityDomainObj, params)
				break
			case activityFeedService.OBSERVATION_FLAGGED:
				activityTitle = activityFeedService.OBSERVATION_FLAGGED
				text = activityDomainObj.flag.value() + ( activityDomainObj.notes ? " \n" + activityDomainObj.notes : "")
				break
			case activityFeedService.OBSERVATION_UPDATED:
				activityTitle = activityFeedService.OBSERVATION_UPDATED
				text = "User updated the observation details"
				break
			case activityFeedService.USERGROUP_CREATED:
				activityTitle = "Group " + activityFeedService.getUserGroupHyperLink(activityRootObj) + " created"
				break
			case activityFeedService.USERGROUP_UPDATED:
				activityTitle = "Group " + activityFeedService.getUserGroupHyperLink(activityRootObj) + " updated"
				break
			case activityFeedService.OBSERVATION_POSTED_ON_GROUP:
				activityTitle = activityFeedService.OBSERVATION_POSTED_ON_GROUP + " " + activityFeedService.getUserGroupHyperLink(activityDomainObj)
				break
			case activityFeedService.OBSERVATION_REMOVED_FROM_GROUP:
				activityTitle = activityFeedService.OBSERVATION_REMOVED_FROM_GROUP + " " + activityFeedService.getUserGroupHyperLink(activityDomainObj) 
				break
			case activityFeedService.MEMBER_JOINED:
				activityTitle = "Joined group " + activityFeedService.getUserGroupHyperLink(activityRootObj)
				break
			case activityFeedService.MEMBER_ROLE_UPDATED:
				activityTitle = activityFeedService.getUserHyperLink(activityDomainObj, model.feedInstance.fetchUserGroup()) + "'s role updated"
				break
			case activityFeedService.MEMBER_LEFT:
				activityTitle = "Left group " + activityFeedService.getUserGroupHyperLink(activityRootObj)
				break
			default:
				activityTitle = activityType
				break
		}
		
		model.activityInstance = activityDomainObj
		model.feedText = text
		model.activityTitle = activityTitle
		out << render(template:"/common/activityfeed/showActivityTemplate", model:attrs.model);
	}	
}
