import java.sql.Timestamp;

import javax.sql.DataSource
import groovy.sql.Sql

import species.participation.Comment
import species.participation.Observation
import species.participation.ActivityFeedService
import species.participation.ActivityFeed

import speciespage.UserGroupService
import species.auth.SUser
import species.groups.UserGroup
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import content.eml.*;
/*
def feedService = ctx.getBean("activityFeedService");
def userGroupService = ctx.getBean("userGroupService");

def wgpGroup = UserGroup.read(1)
def wgpUserDate = new Date(112, 7, 8)
*/



def migrate(){
	/*
	def userGroupService = ctx.getBean("userGroupService");
	userGroupService.migrateUserPermission()
	println "=== done "
	*/
	
	//migrateUserToWGPGroup()
	//migreateObvToTWGPGroup()
	//migrateCommentAsFeeds()
	migratePermission()
}


def migratePermission(){
	
	def userGroupService = ctx.getBean("userGroupService");
	userGroupService.migrateUserPermission()
	
}

def migrateCommentAsFeeds(){
	def feedService = ctx.getBean("activityFeedService");
	Comment.list().each { Comment c ->
		def rootHolder = feedService.getDomainObject(c.rootHolderType, c.rootHolderId)
		def af = feedService.addActivityFeed(rootHolder, c,  c.author, feedService.COMMENT_ADDED)
		
		if(!af.save(flush:true)){
			println "=========== error while save "
		}
		updateTime(af, c)
		println "=== done comment " + af
	}
}

def migrateUserToWGPGroup(){
	def wgpUserDate = new Date(111, 7, 8)
	def wgpGroup = UserGroup.read(1)
	def feedService = ctx.getBean("activityFeedService");

	SUser.findAllByDateCreatedGreaterThanEquals(wgpUserDate).each{ user ->
		if(!wgpGroup.isMember(user) && !wgpGroup.isFounder(user)){
			wgpGroup.addMember(user)
			println "== done user addition " + user
		}
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
	
	
	println "new date " + af.dateCreated
	
	if(!af.save(flush:true)){
		af.errors.allErrors.each { println  it }
	}
	println "after save " +  af.dateCreated
}


def correctObvActivityOrder(){
	def feedService = ctx.getBean("activityFeedService");
	
	ActivityFeed.withTransaction(){
		ActivityFeed.findAllByActivityType(feedService.OBSERVATION_CREATED).each{ af ->
			def createdTime = af.dateCreated
			
			ActivityFeed.findAllWhere(activityType:feedService.OBSERVATION_POSTED_ON_GROUP, rootHolderType:af.rootHolderType, rootHolderId:af.rootHolderId).each{ af1 ->
				if(createdTime >=  af1.dateCreated){
					createdTime = new Timestamp(af1.dateCreated.getTime() - 1)
				}
			}
			
			if(createdTime < af.dateCreated){
				af.dateCreated = createdTime
				if(!af.save()){
					af.errors.allErrors.each { println  it }
				}
				println "========== updating " + af
			}
		}
	}
	
	
	
	//biodiv=#  update activity_feed set last_updated = date_created where activity_type = 'Observation created';
}

def addFeedForObvCreate(){
	def feedService = ctx.getBean("activityFeedService");
	Observation.findAllWhere(isDeleted:false).each { obv ->
		def af1 = ActivityFeed.findWhere(activityType:feedService.OBSERVATION_CREATED, rootHolderType:Observation.class.getCanonicalName(), rootHolderId:obv.id)
		if(!af1){
			ActivityFeed newaf = feedService.addActivityFeed(obv, null, obv.author, feedService.OBSERVATION_CREATED);
			println "saved feed"
		}
	}
	ActivityFeed.withTransaction(){
	ActivityFeed.findAllByActivityType(feedService.OBSERVATION_CREATED).each{ af ->
		def obv = feedService.getDomainObject(af.rootHolderType, af.rootHolderId)
		af.dateCreated = obv.createdOn
		af.lastUpdated = obv.createdOn
		if(!af.save()){
			af.errors.allErrors.each { println  it }
		}
		println "== done updating user time addition " + af.dateCreated
	}
	}
}

def addUserRegistrationFeed(){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	def m = GrailsDomainBinder.getMapping(ActivityFeed.class)
	m.autoTimestamp = false
	
	SUser.withTransaction(){
		SUser.list().each { user ->
			println user
			checklistUtilService.addActivityFeed(user, user, user, ActivityFeedService.USER_REGISTERED, user.dateCreated);
		}
	}
	m.autoTimestamp = true
}


def addDocumentPostFeed(){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	def m = GrailsDomainBinder.getMapping(ActivityFeed.class)
	def desc = "Posted document to group"
	m.autoTimestamp = false
	Document.withTransaction(){
		Document.list().each { doc ->
			println doc
			doc.userGroups.each { ug ->
				checklistUtilService.addActivityFeed(doc, ug, doc.author, ActivityFeedService.RESOURCE_POSTED_ON_GROUP, new Date(doc.dateCreated.getTime() + 2) , desc);
			}
		}
	}
	m.autoTimestamp = true
}



addDocumentPostFeed()

//addUserRegistrationFeed()

//addFeedForObvCreate()
//correctObvActivityOrder()
//migrate()

/*
//please run following command on database
//biodiv=#  update activity_feed set last_updated = date_created;
//becasue last updated is managed by grails so manully setting its time to same as date_created
*/
