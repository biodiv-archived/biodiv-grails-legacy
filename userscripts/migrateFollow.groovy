import species.participation.Follow
import species.participation.ActivityFeed

def migrate(){
	def activityFeedService = ctx.getBean("activityFeedService");
	//ActivityFeed.withTransaction(){
		ActivityFeed.listOrderById().each{ ActivityFeed af ->
			//println " starting $af "
			def domainObj = activityFeedService.getDomainObject(af.rootHolderType, af.rootHolderId)
			//if(! domainObj instanceof Map){
				Follow.addFollower(domainObj, af.author)
				println "added $af"
			//}
		}
	//}
}

migrate()