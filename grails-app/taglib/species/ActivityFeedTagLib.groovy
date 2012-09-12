package species

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
	
	def showAllActivityFeeds = {attrs, body->
		def model = attrs.model
		
		def refTime = new Date().time.toString()
		model.newerTimeRef = model.olderTimeRef = refTime
		
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
			model.olderTimeRef = model.feeds.last().lastUpdated.time.toString()
			model.newerTimeRef = model.feeds.first().lastUpdated.time.toString()
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
		def text = ""
		
		println "=============================$model.feedInstance"
		switch (activityType) {
			case activityFeedService.COMMENT_ADDED:
				text = activityDomainObj.body
				break
			case activityFeedService.SPECIES_RECOMMENDED:
				text = getSpeciesNameHtml(activityDomainObj)
				break
			case activityFeedService.SPECIES_AGREED_ON:
				text = getSpeciesNameHtml(activityDomainObj)
				break
			case activityFeedService.OBSERVATION_FLAGGED:
				text = activityDomainObj.flag.value() + ( activityDomainObj.notes ? " \n" + activityDomainObj.notes : "")
				break
			default:
				text = activityType
				break
		}
		model.activityInstance = activityDomainObj
		model.feedText = text
		out << render(template:"/common/activityfeed/showActivityTemplate", model:attrs.model);
	}
	
	private getSpeciesNameHtml(recoVote){
		def reco = recoVote.recommendation
		def speciesId = reco?.taxonConcept?.findSpeciesId();
		String sb = ""
		if(speciesId != null){
			sb = (g.link(controller:"species", action:"show", id:speciesId){"<i>$reco.name</i>"})
		}else if(reco.isScientificName){
			sb = "<i>$reco.name</i>"
		}else{
			sb = reco.name
		}
 		return "" + sb
	}
}
