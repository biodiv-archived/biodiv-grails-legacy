import javax.sql.DataSource
import groovy.sql.Sql

import species.participation.Comment
import species.participation.Observation
import species.participation.ActivityFeedService
import species.participation.ActivityFeed

import speciespage.UserGroupService
import species.auth.SUser
import species.groups.UserGroup

/*
def feedService = ctx.getBean("activityFeedService");
def userGroupService = ctx.getBean("userGroupService");

def wgpGroup = UserGroup.read(1)
def wgpUserDate = new Date(112, 7, 8)
*/

def migrate(){
	migrateCommentAsFeeds()
	migrateUserToWGPGroup()
	migreateObvToTWGPGroup()
}


def migrateCommentAsFeeds(){
	def feedService = ctx.getBean("activityFeedService");
	Comment.list().each { Comment c ->
		def rootHolder = feedService.getDomainObject(c.rootHolderType, c.rootHolderId)
		def af = feedService.addActivityFeed(rootHolder, c,  c.author, feedService.COMMENT_ADDED)
		
		if(!af){
			println "=========== error while save "
		}
		updateTime(af, c)
		println "=== done comment " + af
	}
}

def migrateUserToWGPGroup(){
	def wgpUserDate = new Date(112, 7, 8)
	def wgpGroup = UserGroup.read(1)
	def feedService = ctx.getBean("activityFeedService");

	SUser.findAllByDateCreatedGreaterThanEquals(wgpUserDate).each{ user ->
		wgpGroup.addMember(user)
		println "== done user addition " + user
	}
	
	ActivityFeed.findAllByActivityType(feedService.MEMBER_JOINED).each{ af ->
		def user = feedService.getDomainObject(af.activityHolderType, af.activityHolderId)
		updateTime(af, user)
		println "== done updating user time addition " + user
	}
}

def migreateObvToTWGPGroup(){
	def wgpGroup = UserGroup.read(1)
	def userGroupService = ctx.getBean("userGroupService");
	def feedService = ctx.getBean("activityFeedService");
	
	def obvs = getAllWgpObvs()
	obvs.each{ obv ->
		userGroupService.postObservationToUserGroup(obv, wgpGroup)
		println "done post of obv " + obv
	}
	
	ActivityFeed.findAllByActivityType(feedService.OBSERVATION_POSTED_ON_GROUP).each{ af ->
		def obv = feedService.getDomainObject(af.rootHolderType, af.rootHolderId)
		af.dateCreated = obv.createdOn 
		af.lastUpdated = obv.createdOn
		if(!af.save(flush:true)){
			af.errors.allErrors.each { println  it }
		}
		println "== done updating user time addition " + obv
	}
}

def getAllWgpObvs(){
	def dataSource =  ctx.getBean("dataSource");
	println "======== data osurce  $dataSource"
	def sql =  Sql.newInstance(dataSource);
	String query = "select obv.id from observation_locations as obv, wg_bounds as wg where ST_Contains(wg.geom, obv.st_geomfromtext);"
	String query1 = "select obv.id as id from observation as obv"
	def retList = []
	sql.eachRow(query){ row ->
		println "=== id " + row.id
		retList.add(Observation.read(row.id))
	}
	return retList
}

def updateTime(af, c){
	af.dateCreated = c.dateCreated 
	af.lastUpdated = c.dateCreated
	if(!af.save(flush:true)){
		af.errors.allErrors.each { println  it }
	}
}

migrate()

/*
//please run following command on database
//biodiv=#  update activity_feed set last_updated = date_created;
//becasue last updated is managed by grails so manully setting its time to same as date_created
*/
