package species.participation

import java.text.SimpleDateFormat
import org.hibernate.Hibernate;

import species.auth.SUser;
import species.groups.UserGroup;
import species.Species;
import species.SpeciesField

class ActivityFeedService {
	
	static final String COMMENT_ADDED = "Added a comment"
	static final String COMMENT_IN_REPLY_TO = "In reply to"
	
	
	//checklist related
	static final String CHECKLIST_CREATED = "Checklist created"
	static final String CHECKLIST_UPDATED = "Checklist updated"
	//static final String CHECKLIST_POSTED_ON_GROUP = "Posted checklist to group"
	//static final String CHECKLIST_REMOVED_FROM_GROUP = "Removed checklist from group"
	
	
	//observation related
	static final String OBSERVATION_CREATED = "Observation created"
	static final String OBSERVATION_UPDATED = "Observation updated"
	static final String OBSERVATION_FLAGGED = "Observation flagged"
	static final String OBSERVATION_FLAG_DELETED = "Observation flag deleted"
	static final String OBSERVATION_DELETED = "Deleted observation"
	
	static final String SPECIES_RECOMMENDED = "Suggested species name"
	static final String SPECIES_AGREED_ON = "Agreed on species name"
	static final String RECOMMENDATION_REMOVED = "Suggestion removed"
	//static final String OBSERVATION_POSTED_ON_GROUP = "Posted observation to group"
	//static final String OBSERVATION_REMOVED_FROM_GROUP = "Removed observation from group"
	
	
	//group related
	static final String USERGROUP_CREATED = "Group created"
	static final String USERGROUP_UPDATED = "Group updated"
	static final String MEMBER_JOINED = "Joined group"
	static final String MEMBER_LEFT = "Left Group"
	static final String MEMBER_ROLE_UPDATED = "Role updated"
	static final String USER_REGISTERED = "Registered to portal"
	static final String RESOURCE_POSTED_ON_GROUP = "Posted resource"
	static final String RESOURCE_REMOVED_FROM_GROUP = "Removed resoruce"
	//static final String RESOURCE_BULK_POST = "Bulk posting"
	//static final String RESOURCE_BULK_REMOVE = "Bulk removal"
	
	
	//document related
	static final String DOCUMENT_CREATED = "Document created"
	static final String DOCUMENT_UPDATED = "Document updated"
	//static final String DOCUMENT_POSTED_ON_GROUP = "Posted document to group"
	//static final String DOCUMENT_REMOVED_FROM_GROUP = "Removed document from group"
	
	//species related
	//static final String SPECIES_POSTED_ON_GROUP = "Posted species to group"
	//static final String SPECIES_REMOVED_FROM_GROUP = "Removed species from group"
	
	
	static final String OLDER = "older"
	static final String NEWER = "newer"
	
	static final String READ_ONLY = "readOnly"
	static final String EDITABLE = "editable"
	
	static final String AUTO = "auto"
	static final String MANUAL= "manual"
	
	static final String LATEST_FIRST = "latestFirst"
	static final String OLDEST_FIRST = "oldestFirst"
	
	static final String GENERIC = "Generic"
	static final String SPECIFIC = "Specific"
	static final String SELECTED = "Selected"
	static final String GROUP_SPECIFIC = "GroupSpecific"
	static final String MY_FEEDS = "MyFeeds"
	static final String USER = "User"
	
	
	static final String ALL = "All"
	static final String OTHER = "other"
	
	//classes for non-db object this should not use as root holder object in any manner 
	//these should be used only for comment with proper root holder object(some domain class)
	static final String SPECIES_SYNONYMS = "species_Synonyms"
	static final String SPECIES_COMMON_NAMES = "species_Common Names"
	static final String SPECIES_MAPS = "species_Occurrence Records"
	static final String SPECIES_TAXON_RECORD_NAME = "species_Taxon Record Name"
	
	private static DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	
	
	static transactional = false
	
	def grailsApplication
	def springSecurityService
	def observationService
	def userGroupService
	
	def getActivityFeeds(params){
//		log.debug params;
		def feeds = ActivityFeed.fetchFeeds(params)
		if(params.feedOrder == OLDEST_FIRST){
			feeds = feeds.reverse()
		}
		return aggregateFeeds(feeds, params)
	}
	
	def getCount(params){
		return ActivityFeed.fetchCount(params)
	}
	
	def addActivityFeed(rootHolder, activityHolder, author, activityType, description=null, boolean isShowable=null, flushImmidiatly=true){
		//to support discussion on comment thread
		def subRootHolderType = rootHolder?.class?.getCanonicalName()
		def subRootHolderId = rootHolder?.id
		if(activityHolder?.class?.getCanonicalName() == Comment.class.getCanonicalName()){
			subRootHolderType = getType(activityHolder)
			subRootHolderId = (activityHolder.isMainThread())? activityHolder.id : activityHolder.fetchMainThread().id
		}
		
		//if not pass through params hiding object whose isShowable = false. In all other cases making feed showable
		isShowable= (isShowable != null) ? isShowable : (rootHolder.hasProperty('isShowable') && rootHolder.isShowable != null)? rootHolder.isShowable : true
		ActivityFeed af = new ActivityFeed(author:author, activityHolderId:activityHolder?.id, \
						activityHolderType:getType(activityHolder), \
						rootHolderId:rootHolder?.id, rootHolderType:getType(rootHolder), \
						isShowable:isShowable,\
						activityType:activityType, subRootHolderType:subRootHolderType, subRootHolderId:subRootHolderId, activityDescrption:description);
		
		ActivityFeed.withNewSession {
				if(!af.save(flush:flushImmidiatly)){
					af.errors.allErrors.each { log.error it }
					return null
				}
		}
		Follow.addFollower(rootHolder, author, flushImmidiatly)
		return af
	}
	
	def getDomainObject(className, id){
		def retObj = null
		if(!className || className.trim() == ""){
			return retObj
		}
		
		id = id.toLong()
		switch (className) {
			case [SPECIES_SYNONYMS, SPECIES_COMMON_NAMES, SPECIES_MAPS, SPECIES_TAXON_RECORD_NAME]:
				retObj = [objectType:className, id:id]
				break
			default:
				retObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
				break
		}
		return retObj
	}
	
	// this will return class of object in general used in comment framework
	static getType(obj){
		if(!obj) return null
		
		if(obj instanceof Map){
			return obj.objectType
		}
		
		return Hibernate.getClass(obj).getName();
	}
	
	private aggregateFeeds(List feeds, params){
		if(params.feedType == SPECIFIC || params.checkFeed){
			return feeds
		}
		
		// aggregating object based on feed type
		Set genericFeedSet = new HashSet()
		Set commentFeedSet = new HashSet()
		Set otherFeedSet = new HashSet()
		def retList = []
		feeds.each { it ->
			if(it.rootHolderType == Observation.class.getCanonicalName() || it.rootHolderType == Checklists.class.getCanonicalName() || it.rootHolderType == Species.class.getCanonicalName()){
				//aggregating observation object
				def feedKey = it.rootHolderType + it.rootHolderId;
				if(!genericFeedSet.contains(feedKey)){
					retList.add(it)
					genericFeedSet.add(feedKey)
				}
			}else if(it.rootHolderType == UserGroup.class.getCanonicalName() && it.subRootHolderType == Comment.class.getCanonicalName()){
				//aggregating comment
				def feedKey = it.subRootHolderType + it.subRootHolderId;
				if(!commentFeedSet.contains(feedKey)){
					retList.add(it)
					commentFeedSet.add(feedKey)
				}
			}else if(params.feedType == GROUP_SPECIFIC){
				//adding object as it is if group specific object	
				retList.add(it)
			}else{
				//aggregating other object as well if its not specific to group(ie. myfeeds )
				def feedKey = it.rootHolderType + it.rootHolderId;
				if(!otherFeedSet.contains(feedKey)){
					retList.add(it)
					otherFeedSet.add(feedKey)
				}
			}
		}
		return retList
	}
	
	def deleteFeed(obj){
		ActivityFeed.deleteFeed(obj);
	}
	
	def updateIsShowable(obj){
		ActivityFeed.updateIsShowable(obj);
	}
	
	def getContextInfo(ActivityFeed feedInstance, params){
		
		def activityType = feedInstance.activityType
		def activityDomainObj = getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId)
		def activityRootObj = 	getDomainObject(feedInstance.rootHolderType, feedInstance.rootHolderId)
		
		def text = null
		def activityTitle = null
		
		//log.debug "=== feed === $feedInstance.id === $feedInstance.activityType"
		switch (activityType) {
			case COMMENT_ADDED:
				activityTitle = COMMENT_ADDED  + getCommentContext(activityDomainObj, params)
				text = activityDomainObj.body
				break
			case SPECIES_RECOMMENDED:
				activityTitle = SPECIES_RECOMMENDED + " " + (activityDomainObj ? getSpeciesNameHtml(activityDomainObj, params):feedInstance.activityDescrption)
				break
			case SPECIES_AGREED_ON:
				activityTitle =  SPECIES_AGREED_ON + " " + (activityDomainObj ? getSpeciesNameHtml(activityDomainObj, params):feedInstance.activityDescrption)
				break
			case OBSERVATION_FLAGGED:
				activityTitle = OBSERVATION_FLAGGED
				text = activityDomainObj.flag.value() + ( activityDomainObj.notes ? " \n" + activityDomainObj.notes : "")
				break
			case OBSERVATION_UPDATED:
				activityTitle = OBSERVATION_UPDATED
				text = "User updated the observation details"
				break
			case USERGROUP_CREATED:
				activityTitle = "Group " + getUserGroupHyperLink(activityRootObj) + " created"
				break
			case USERGROUP_UPDATED:
				activityTitle = "Group " + getUserGroupHyperLink(activityRootObj) + " updated"
				break
			case MEMBER_JOINED:
				activityTitle = "Joined group " + getUserGroupHyperLink(activityRootObj)
				break
			case MEMBER_ROLE_UPDATED:
				activityTitle = getUserHyperLink(activityDomainObj, feedInstance.fetchUserGroup()) + "'s role updated"
				break
			case MEMBER_LEFT:
				activityTitle = "Left group " + getUserGroupHyperLink(activityRootObj)
				break
			case RECOMMENDATION_REMOVED:
				activityTitle = "Removed species name " + feedInstance.activityDescrption
				break
			
			case [RESOURCE_POSTED_ON_GROUP, RESOURCE_REMOVED_FROM_GROUP]:
				activityTitle = feedInstance.activityDescrption  + " " + getUserGroupHyperLink(activityDomainObj)
				break
			
			default:
				activityTitle = activityType
				break
		}
		
		return [activityTitle:activityTitle, text:text]
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Template rendering related //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	def getCommentContext(Comment comment, params){
		String result = ""
		switch (comment.commentHolderType) {
			case Observation.class.getCanonicalName():
				def rootObj = getDomainObject(comment.rootHolderType,comment.rootHolderId)
				if(rootObj.instanceOf(Checklists)){
					def obv = getDomainObject(comment.commentHolderType,comment.commentHolderId)
					result += " on " + getSpeciesNameHtmlFromReco(obv.maxVotedReco, params) + ": Row " + (rootObj.observations.indexOf(obv) + 1) 
				}
				break
			case SpeciesField.class.getCanonicalName():
				SpeciesField sf = getDomainObject(comment.commentHolderType,comment.commentHolderId)
				result += " on species field: " +  sf.field.category + (sf.field.subCategory ? ":" + sf.field.subCategory : "")
				break
			case [SPECIES_SYNONYMS, SPECIES_COMMON_NAMES, SPECIES_MAPS, SPECIES_TAXON_RECORD_NAME]:
				result += " on species field: " + comment.commentHolderType.split("_")[1]
				break
			default:
				break
		}
		return result
	}
	
	def getSpeciesNameHtml(recoVote, params){
		return getSpeciesNameHtmlFromReco(recoVote.recommendation, params)
	}
	
	def getSpeciesNameHtmlFromReco(reco, params){
		if(!reco){
			return ""
		}
		
		def speciesId = reco?.taxonConcept?.findSpeciesId();
		String sb = ""
		if(speciesId != null){
			sb =  '<a href="' + observationService.generateLink("species", "show", [id:speciesId, 'userGroupWebaddress':params?.webaddress]) + '">' + "<i>$reco.name</i>" + "</a>"
		}else if(reco.isScientificName){
			sb = "<i>$reco.name</i>"
		}else{
			sb = reco.name
		}
		 return "" + sb
	}
	
	def getUserHyperLink(user, userGroup){
		return '<a href="' +  observationService.generateLink("SUser", "show", ["id": user.id, userGroup:userGroup, 'userGroupWebaddress':userGroup?.webaddress])  + '">' + "<i>$user.name</i>" + "</a>"
	}
	
	def getUserGroupHyperLink(userGroup){
		return '<a href="' + userGroupService.userGroupBasedLink([controller:"userGroup", action:"show", mapping:"userGroup", userGroup:userGroup, userGroupWebaddress:userGroup?.webaddress]) + '">' + "<i>$userGroup.name</i>" + "</a>"
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// GROUP PULL RELATED ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	def addFeedOnGroupResoucePull(resource, ug, SUser author, boolean isPost){
		addFeedOnGroupResoucePull([resource], ug, author, isPost)
	}
	
	def addFeedOnGroupResoucePull(List resources, ug, SUser author, boolean isPost, boolean isShowable=true, isBulkPull=false){
		if(resources.isEmpty()) return
		
		log.debug "Adding feed for resources " + resources.size()
		def activityType = isPost ? RESOURCE_POSTED_ON_GROUP : RESOURCE_REMOVED_FROM_GROUP
		Map resCountMap = [:]
		resources.each { r->
			def description = getDescriptionForResourcePull(r, isPost)
			def af = addActivityFeed(r, ug, author, activityType, description, isShowable, false)
			int oldCount = resCountMap.get(r.class.canonicalName)?:0
			resCountMap.put(r.class.canonicalName, ++oldCount)
			if(isShowable){
				observationService.sendNotificationMail(activityType, r, null, null, af){
			}
		}
		if(isBulkPull){
			def description = getDescriptionForBulkResourcePull(isPost, resCountMap)
			def af = addActivityFeed(ug, ug, author, activityType, description, true)
			observationService.sendNotificationMail(activityType, ug, null, null, af){
		} 
	}
	
	private String getDescriptionForBulkResourcePull(isPost, countMap){
		def desc = isPost ? "Posted" : "Removed"
		int loopVar = 0
		countMap.each { k, v ->
			println k
			println v
			if(loopVar > 0) desc += " and"
			desc += " " + v 
			switch(k){
				case Checklists.class.canonicalName:
					desc += " checklist" + ((v > 1) ? "s" : "")
					break
				case Species.class.canonicalName:
					desc += " species" 
					break
				default:
					desc += " " + k.split("\\.")[-1] + ((v > 1) ? "s" : "")
					break
			}
			loopVar++
		}
		desc +=  isPost ? " to group" : " from group"
		return desc
	}
	
	
	private String getDescriptionForResourcePull(r, isPost){
		def desc = isPost ? "Posted" : "Removed"
		switch(r.class.canonicalName){
			case Checklists.class.canonicalName:
				desc += " checklist"
				break
			default:
				desc += " " + r.class.simpleName.toLowerCase()
				break
		}
		desc +=  isPost ? " to group" : " from group"
		return desc
	}
}
