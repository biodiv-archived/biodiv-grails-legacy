package species.participation

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import species.participation.Follow
import species.auth.SUser
import species.groups.UserGroup
import species.participation.Stats

import org.springframework.web.servlet.support.RequestContextUtils as RCU;

import  org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import static org.springframework.http.HttpStatus.*;


class ActivityFeedController {
	
	static defaultAction = "list"
	
	def activityFeedService;
	def springSecurityService;
	def messageSource;
	def observationService;
	def utilsService;
	def checklistUtilService;
	
	def getFeeds(){
		//log.debug params;
		params.author = springSecurityService.currentUser;

		def feeds = activityFeedService.getActivityFeeds(params);
		def userLanguage = utilsService.getCurrentLanguage(request);
		if(!feeds.isEmpty()) {
			if(params.checkFeed){
				def m = [feedAvailable:true]
				render m as JSON;
			}
			else {
                def model = [feeds:feeds, feedType:params.feedType, feedPermission:params.feedPermission, feedHomeObject:activityFeedService.getDomainObject(params.feedHomeObjectType, params.feedHomeObjectId), userLanguage:userLanguage]
				def showFeedListHtml = g.render(template:"/common/activityfeed/showActivityFeedListTemplate", model:model);
                showFeedListHtml = showFeedListHtml.replaceAll('\\n|\\t','');
				def newerTimeRef = (params.feedOrder == activityFeedService.LATEST_FIRST) ? feeds.first().lastUpdated.time.toString() : feeds.last().lastUpdated.time.toString()
				def olderTimeRef = (params.feedOrder == activityFeedService.LATEST_FIRST) ? feeds.last().lastUpdated.time.toString() : feeds.first().lastUpdated.time.toString()
				def result = [showFeedListHtml:showFeedListHtml, olderTimeRef:olderTimeRef, newerTimeRef:newerTimeRef, currentTime:new Date().getTime()]
				if(params.refreshType == activityFeedService.MANUAL){
					params.refTime = olderTimeRef
					result["remainingFeedCount"] = activityFeedService.getCount(params);
				}
                
                model = utilsService.getSuccessModel('', null, OK.value(), model);
                model.putAll(result);
				render model as JSON
			}
		} else {
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
			msg = messageSource.getMessage("default.followed", null, RCU.getLocale(request))
			if(!author.sendNotification){
				msg += messageSource.getMessage("default.turn.notification", null, RCU.getLocale(request))
			}
		}else{
			Follow.deleteFollower(domainObj, author)
			msg = messageSource.getMessage("default.unfollowed", null, RCU.getLocale(request))
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
	
	@Secured(['ROLE_ADMIN'])
	def addUserRegistrationFeed(){
		def m = new GrailsDomainBinder().getMapping(ActivityFeed.class)
		m.autoTimestamp = false
		
		SUser.withTransaction(){
			SUser.list().each { user ->
				println "checking feed for " + user
				def feed = ActivityFeed.findByActivityTypeAndAuthor(ActivityFeedService.USER_REGISTERED, user);
				if(!feed){
					println "adding feed for user " + user
					checklistUtilService.addActivityFeed(user, user, user, ActivityFeedService.USER_REGISTERED, user.dateCreated);
				}
			}
		}
		m.autoTimestamp = true
	}
	
	def stats(){
		log.debug params
		render Stats.getStatResult(UserGroup.findByWebaddress(params.webaddress)) as JSON
	}
}
