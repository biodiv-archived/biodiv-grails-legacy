package species.participation

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.grails.tagcloud.TagCloudUtil
import groovy.sql.Sql

import species.groups.SpeciesGroup;
import species.Habitat

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;

import species.participation.Observation;
import species.utils.Utils;
import species.License
import species.License.LicenseType
import content.Project

import species.sourcehandler.XMLConverter
import species.AbstractObjectService;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import static speciespage.ObvUtilService.*;
import java.nio.file.Files;
import species.formatReader.SpreadsheetReader;
import species.auth.SUser;
import species.groups.UserGroup;
import static java.nio.file.StandardCopyOption.*
import java.nio.file.Paths;

class DiscussionService extends AbstractObjectService {

	static transactional = false
	
	private static final int BATCH_SIZE = 1
	
	def discussionSearchService
	def userGroupService
    def sessionFactory
	def activityFeedService
	def checklistUtilService
	def dataSource
	
	Discussion createDiscussion(params) {
		def discussion = new Discussion()
		updateDiscussion(discussion, params)
		return discussion
	}


	def updateDiscussion(Discussion discussion, params) {
		discussion.properties = params
		discussion.language = params.locale_language 
		
		discussion.subject = params.subject? params.subject.trim() :null
		discussion.body = params.body? params.body.trim() :null
	}


	def setUserGroups(Discussion discussionInstance, List userGroupIds, boolean sendMail = true) {
		if(!discussionInstance) return

		def discussionInUserGroups = discussionInstance.userGroups.collect { it.id + ""}
		def toRemainInUserGroups =  discussionInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			log.debug 'removing discussion from usergroups'
			userGroupService.removeDiscussionFromUserGroups(discussionInstance, discussionInUserGroups, sendMail);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
			userGroupService.postDiscussiontoUserGroups(discussionInstance, userGroupIds, sendMail);
			discussionInUserGroups.removeAll(toRemainInUserGroups)
			userGroupService.removeDiscussionFromUserGroups(discussionInstance, discussionInUserGroups, sendMail);
		}
	}





	/**
	 * Handle the filtering on Documetns
	 * @param params
	 * @param max
	 * @param offset
	 * @return
	 */
	Map getFilteredDiscussions(params, max, offset) {
		def res = [canPullResource:userGroupService.getResourcePullPermission(params)]
		if(Utils.isSearchAction(params)){
			//returning docs from solr search
			res.putAll(search(params))
		}else{
			res.putAll(getDiscussionFromDB(params, max, offset))
		}
		return res
	}
	
	private getDiscussionFromDB(params, max, offset){
		def queryParts = getDiscussionsFilterQuery(params)
		String query = queryParts.query;


		query += queryParts.filterQuery + queryParts.orderByClause
		if(max != -1)
			queryParts.queryParams["max"] = max
		if(offset != -1)
			queryParts.queryParams["offset"] = offset

		log.debug "Discussion Query "+ query + "  params " + queryParts.queryParams

        def hqlQuery = sessionFactory.currentSession.createQuery(query)
        if(max > -1){
            hqlQuery.setMaxResults(max);
            queryParts.queryParams["max"] = max
        }
        if(offset > -1) {
            hqlQuery.setFirstResult(offset);
            queryParts.queryParams["offset"] = offset
        }

        hqlQuery.setProperties(queryParts.queryParams);
		def discussionInstanceList = hqlQuery.list();

		return [discussionInstanceList:discussionInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	/**
	 * Prepare database wuery based on paramaters
	 * @param params
	 * @return
	 */
	def getDiscussionsFilterQuery(params) {
		def query = "select discussion from Discussion discussion "
		def queryParams = [:]
		def activeFilters = [:]
		def filterQuery = "where discussion.isDeleted = false "  
        def userGroup = utilsService.getUserGroup(params);
 
        if(params.featureBy == "true"){
			query = "select discussion from Discussion discussion "
		 	if(!userGroup) {
                filterQuery += " and discussion.featureCount > 0 "                
            }
             else {
                query += ", Featured feat "
                filterQuery += " and discussion.id = feat.objectId and feat.objectType =:featType and feat.userGroup.id = :userGroupId "
                queryParams["userGroupId"] = userGroup?.id

            }
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Discussion.class.getCanonicalName();
            activeFilters["featureBy"] = params.featureBy
		}

		if(params.tag){
			query = "select discussion from Discussion discussion,  TagLink tagLink "
			filterQuery += " and discussion.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Discussion.class)
			activeFilters["tag"] = params.tag
		}
		
		if(userGroup) {
			queryParams['userGroup'] = userGroup
			query += " join discussion.userGroups userGroup "
			filterQuery += " and userGroup=:userGroup "
		}
		
		def sortBy = params.sort ? params.sort : "lastRevised "
		def orderByClause = " order by discussion." + sortBy +  " desc, discussion.id asc"
		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// migrate old comment thread to discussion ///////////////
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	def migrate(){
		def m = new GrailsDomainBinder().getMapping(ActivityFeed.class)
		m.autoTimestamp = false
		Map cToFeed = [:]
		def map = [:]
		List uniqueFeed = []
		List commentThreadList = ActivityFeed.findAllByRootHolderTypeAndSubRootHolderType(UserGroup.class.canonicalName, Comment.class.canonicalName)
		
		ActivityFeed.withTransaction {
			commentThreadList.each { ActivityFeed af ->
				Comment mainComment = utilsService.getDomainObject(af.subRootHolderType, af.subRootHolderId)
				
				//if main discussion thread
				if(af.subRootHolderType == af.activityHolderType && af.subRootHolderId == af.activityHolderId){
					
					UserGroup userGroup = utilsService.getDomainObject(af.rootHolderType, af.rootHolderId)
					
					def subject = mainComment.subject ?:(mainComment.body.substring(0, Math.min(mainComment.body.length(), 50))) 
					Discussion d = new Discussion(subject:subject, body:mainComment.body, plainText:mainComment.body, createdOn : mainComment.dateCreated, \
													lastRevised:mainComment.lastUpdated, language:mainComment.language, author:mainComment.author)
					if(!d.save(flush:true)){
						d.errors.allErrors.each { log.error it }
					}
					checklistUtilService.addActivityFeed(d, d, d.author, ActivityFeedService.DISCUSSION_CREATED, d.createdOn)
					
					userGroup.addToDiscussions(d)
					if(!userGroup.save(flush:true)) {
						log.error "Could not add ${d} to ${userGroup}"
						log.error  userGroup.errors.allErrors.each { log.error it }
					} else {
						checklistUtilService.addActivityFeed(d, userGroup, d.author, ActivityFeedService.RESOURCE_POSTED_ON_GROUP, new Date(d.createdOn.getTime() + 10 ), "Posted discussion to group")
					}
					log.debug "Saved comment " + mainComment
					map[mainComment] = d
					uniqueFeed << af
				}else{
					if(cToFeed.containsKey(mainComment)){
						cToFeed[mainComment] << af
					}else{
						cToFeed[mainComment] = [af]
					}
				}
			}
		}
		m.autoTimestamp = false
		

		println "============ ctofeed === " +	cToFeed	
		//moving feed to discussion thread
		def sql =  Sql.newInstance(dataSource);
		cToFeed.keySet().each { Comment c ->
			def d = map[c]
			cToFeed[c].each { ActivityFeed af ->
				println "updating feed >>>>>>>>>>>>>>>>> " + af 
				sql.executeUpdate('update activity_feed set root_holder_type = :rootClass where sub_root_holder_type = :subRootClass and sub_root_holder_id = :subRootId ', [rootClass:d.class.canonicalName, subRootClass:af.subRootHolderType, subRootId:af.subRootHolderId] );
				sql.executeUpdate('update activity_feed set root_holder_id = :rootClass where sub_root_holder_type = :subRootClass and sub_root_holder_id = :subRootId ', [rootClass:d.id, subRootClass:af.subRootHolderType, subRootId:af.subRootHolderId] );
			}
			
			println "updating for comment " + c
			sql.executeUpdate('update comment set root_holder_type = :rootClass where comment_holder_type = :subRootClass and main_parent_id = :subRootId ', [rootClass:d.class.canonicalName, subRootClass:c.class.canonicalName, subRootId:c.id] );
			sql.executeUpdate('update comment set root_holder_id = :rootClass where comment_holder_type = :subRootClass and main_parent_id = :subRootId ', [rootClass:d.id, subRootClass:c.class.canonicalName, subRootId:c.id] );

		}
		
		uniqueFeed.each { ActivityFeed af ->
			af.delete(flush:true)
		}
		
		sql.executeUpdate("delete from comment where root_holder_type = 'species.groups.UserGroup' and comment_holder_type = 'species.groups.UserGroup' ")
//		cToFeed.keySet().each { Comment c ->
//			c.delete(flush:true)
//		}
		
	}
}
		