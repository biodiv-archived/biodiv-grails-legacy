package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import content.eml.Document
import grails.plugin.springsecurity.annotation.Secured;
import groovy.sql.Sql;
import groovy.util.Eval;

import org.apache.solr.common.SolrException;
import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.plugin.springsecurity.acl.AclEntry
import grails.plugin.springsecurity.acl.AclSid
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid
import org.springframework.security.core.Authentication
import grails.plugin.springsecurity.acl.AclSid;
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import grails.plugin.springsecurity.acl.AclObjectIdentity
import org.apache.commons.logging.LogFactory;

import species.Habitat;
import species.Resource;
import species.ResourceFetcher
import species.Resource.ResourceType;
import species.Species;
import species.auth.Role;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Discussion
import species.participation.Observation;
import species.participation.UserToken;
import species.utils.ImageUtils;
import species.utils.Utils;
import utils.Newsletter;
import content.eml.Document
import content.Project
import species.participation.Checklists
import species.participation.Featured
import species.formatReader.SpreadsheetReader

class UserGroupService {

	static transactional = false

    def utilsService
	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService
	def dataSource;
	def grailsApplication;
	def emailConfirmationService;
	def sessionFactory
	def activityFeedService;

	private void addPermission(UserGroup userGroup, SUser user, int permission) {
		addPermission userGroup, user, aclPermissionFactory.buildFromMask(permission)
	}

	private void addPermission(UserGroup userGroup, SUser user, Permission permission) {
		aclUtilService.addPermission userGroup, user.email, permission
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	UserGroup create(params) {
		UserGroup userGroup = new UserGroup();
		update(userGroup, params);
		return userGroup
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write) or hasPermission(#userGroup, admin)")
	void update(UserGroup userGroup, params) {
		userGroup.properties = params;
		userGroup.name = userGroup.name?.capitalize();
		userGroup.webaddress = URLEncoder.encode(userGroup.name.toLowerCase().replaceAll(" ", "_"), "UTF-8");
		userGroup.language = params.locale_language;

		userGroup.icon = getUserGroupIcon(params.icon);
		addInterestedSpeciesGroups(userGroup, params.speciesGroup)
		addInterestedHabitats(userGroup, params.habitat)
		updateHomePage(userGroup, params)
		
		
		if(params.sw_latitude)
			userGroup.sw_latitude = Float.parseFloat(params.sw_latitude)
		if(params.sw_longitude)
			userGroup.sw_longitude = Float.parseFloat(params.sw_longitude)
		if(params.ne_latitude)
			userGroup.ne_latitude = Float.parseFloat(params.ne_latitude)
		if(params.ne_longitude)
			userGroup.ne_longitude = Float.parseFloat(params.ne_longitude)

		if(!userGroup.hasErrors() && userGroup.save()) {
			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			userGroup.setTags(tags);

			List founders = Utils.getUsersList(params.founderUserIds);
			setUserGroupFounders(userGroup, founders, params.founderMsg, params.domain);
			
			List experts = Utils.getUsersList(params.expertUserIds);
			setUserGroupExperts(userGroup, experts, params.expertMsg, params.domain);
			params.founders = founders;
		}
	}

	//@PreAuthorize("hasPermission(#id, 'species.groups.UserGroup', read) or hasPermission(#id, 'species.groups.UserGroup', admin)")
	UserGroup get(long id) {
		UserGroup.get id
	}

	UserGroup get(String webaddress) {
		UserGroup.findByWebaddress(webaddress);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	//@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<UserGroup> list(Map params) {
		UserGroup.list params
	}

	int count() {
		UserGroup.count()
	}

	private String getUserGroupIcon(String icon) {
		if(!icon) return;

		def resource = null;
		def rootDir = grailsApplication.config.speciesPortal.userGroups.rootDir

		File iconFile = new File(rootDir , icon);
		if(!iconFile.exists()) {
			log.error "COULD NOT locate icon file ${iconFile.getAbsolutePath()}";
		}

		resource = iconFile.absolutePath.replace(rootDir, "");

		//		def res = Resource.findByFileNameAndType(path, ResourceType.ICON);
		//
		//		if(!res) {
		//			resource = new Resource(fileName:path, type:ResourceType.ICON);
		//			if(!resource.save()) {
		//				resource.errors.each { log.error it }
		//			}
		//		}
		return resource;
	}

	private void addInterestedSpeciesGroups(userGroupInstance, speciesGroups) {
		log.debug "Adding species group interests ${speciesGroups}"
		userGroupInstance.speciesGroups = []
		speciesGroups.each {key, value ->
			userGroupInstance.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}
	}

	private void addInterestedHabitats(userGroupInstance, habitats) {
		log.debug "Adding habitat interests ${habitats}"
		userGroupInstance.habitats = []
		habitats.each { key, value ->
			userGroupInstance.addToHabitats(Habitat.read(value.toLong()));
		}
	}
	
	private void updateHomePage(userGroup, params){
		//on create correcting webaddress of home page in other cases(i.e update) no need to do any thing
		if(params.homePage){
			def page = params.homePage.tokenize('/').last().trim()
			def uGroup = grailsApplication.mainContext.getBean('species.UserGroupTagLib');
			if(!page.isInteger()){
				userGroup.homePage = uGroup.createLink(mapping:'userGroup', action:page, params:['webaddress':userGroup.webaddress])
			}else{
				//if home page is newsletter then setting appropriate url and sticky bit true
				userGroup.homePage = uGroup.createLink(controller:'newsletter', action:'show', id:page.toInteger())
				if(page.isInteger()){
					def pageObj = Newsletter.read(page.toInteger())
					if(pageObj){
						pageObj.sticky = true
						if(!pageObj.save(flush:true)){
							pageObj.errors.allErrors.each { log.error it }
						}
					}
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, delete) or hasPermission(#userGroup, admin)")
	void delete(UserGroup userGroup) {
		aclUtilService.deleteAcl userGroup
		UserGroupMemberRole.removeAll(userGroup)
		userGroup.delete()
		// Delete the ACL information as well

	}

	@PreAuthorize("hasPermission(#userGroup, write) or hasPermission(#userGroup, admin)")
	private void deletePermission(UserGroup userGroup, SUser user, Permission permission) {
		aclUtilService.deletePermission(userGroup, user.email, permission);
	}

	//@Transactional
	def getUserGroups(SUser userInstance) {
		return userInstance.getUserGroups()
	}

	@Transactional
	def getSuggestedUserGroups(SUser userInstance,offset = 0) {

		def conn = new Sql(sessionFactory.currentSession.connection())

		List userGroups = [];

		String query = "";
		if(userInstance) {
			query += '''select user_group_id, count(distinct(s_user_id)) as c 
						from user_group_member_role '''
			query += " where user_group_id in (select distinct(user_group_id) from user_group_member_role where s_user_id = "+userInstance.id+") "
			query += 	''' group by user_group_id 
						order by c desc limit 20''';
//			query = '''select distinct s.user_group_id, max(s.count) as maxCount 
//						from ((select distinct u1.user_group_id, u2.count from  user_group_observations u1, 
//								(select observation_id, count(*) from user_group_observations group by observation_id) u2 
//								where u1.observation_id=u2.observation_id) 
//							union 
//							(select distinct u1.user_group_species_groups_id, u2.count from  user_group_species_group u1, 
//								(select species_group_id, count(*) from user_group_species_group group by species_group_id) u2 
//									where u1.species_group_id=u2.species_group_id) 
//							union 
//							(select distinct u1.user_group_habitats_id, u2.count from  user_group_habitat u1, 
//								(select habitat_id, count(*) from user_group_habitat group by habitat_id) u2 
//								where u1.habitat_id=u2.habitat_id)) s 
//						where s.user_group_id not in (select distinct user_group_id from user_group_member_role where s_user_id=${userInstance.id}) 
//						group by s.user_group_id order by maxCount desc;'''
		} else {
			query += '''select user_group_id, count(distinct(s_user_id)) as c
						from user_group_member_role '''
			query += 	''' group by user_group_id
						order by c desc limit 20 offset '''+offset;
//			query = '''select distinct s.user_group_id, max(s.count) as maxCount from ((select distinct u1.user_group_id, u2.count from  user_group_observations u1, (select observation_id, count(*) from user_group_observations group by observation_id) u2 where u1.observation_id=u2.observation_id) union (select distinct u1.user_group_species_groups_id, u2.count from  user_group_species_group u1, (select species_group_id, count(*) from user_group_species_group group by species_group_id) u2 where u1.species_group_id=u2.species_group_id) union (select distinct u1.user_group_habitats_id, u2.count from  user_group_habitat u1, (select habitat_id, count(*) from user_group_habitat group by habitat_id) u2 where u1.habitat_id=u2.habitat_id)) s group by s.user_group_id order by maxCount desc;'''
		}		
		//log.debug "Suggested usergroup query ${query}"
		conn.eachRow(query,
				{ row ->
					userGroups << UserGroup.read(row.user_group_id)

				}
				)
		return userGroups;

	}

	/////////////// TAGS RELATED START /////////////////
	Map getAllRelatedUserGroupTags(params){
		//XXX should handle in generic way
		params.limit = 100
		params.offset = 0
		def uGroupIds = getRelatedUserGroups(params).relatedUserGroups.userGroups.userGroup.collect{it.id}
		uGroupIds.add(params.id.toLong())
		return getTagsFromUserGroups(uGroupIds)
	}

	Map getRelatedTagsFromUserGroup(UserGroup uGroup){
		int tagsLimit = 30;
		def tagNames = uGroup.tags
		LinkedHashMap tags = [:]
		if(tagNames.isEmpty()){
			return tags
		}

		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as ug_count from tag_links as tl, tags as t, user_group ug where t.name in ('" +  tagNames.join("', '") + "') and ug.is_deleted = false and tl.tag_ref = ug.id and tl.type = 'userGroup' and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("ug_count");
		};
		return tags;
	}

	protected Map getTagsFromUserGroup(uGroups){
		int tagsLimit = 30;
		LinkedHashMap tags = [:]
		if(!uGroups){
			return tags
		}

		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as ug_count from tag_links as tl, tags as t, user_group ug where tl.tag_ref in " + getIdList(uGroups)  + " and tl.tag_ref = ug.id  and tl.type = 'userGroup' and ug.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		log.debug query;
		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("ug_count");
		};
		return tags;
	}

	Map getFilteredTags(params){
		def userGroupInstanceList = getFilteredUserGroups(params, -1, -1, true).userGroupInstanceList
		return getTagsFromUserGroup(userGroupInstanceList.collect{it.id});
	}

	/////////////// TAGS RELATED END /////////////////

	/**
	 * Filter usergroups by group, habitat, tag, user, species
	 * max: limit results to max: if max = -1 return all results
	 * offset: offset results: if offset = -1 its not passed to the
	 * executing query
	 */
	Map getFilteredUserGroups(params, max, offset, isMapView) {
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;
		params.observation = (params.observation)? params.long('observation'):null
		params.user = (params.user)? params.long('user'):null

		def query = "select uGroup from UserGroup uGroup "
		//def mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv where obv.isDeleted = :isDeleted "
		def queryParams = [isDeleted : false]
		def filterQuery = " where uGroup.isDeleted = :isDeleted "
		def activeFilters = [:]

		if(params.tag){
			query = "select uGroup from UserGroup uGroup,  TagLink tagLink  "
			//mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv, TagLink tagLink where obv.isDeleted = :isDeleted "
			filterQuery +=  " and uGroup.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'userGroup';
			activeFilters["tag"] = params.tag
		}

		if(params.query) {
			filterQuery += " and lower(uGroup.name) LIKE :name ";
			queryParams["name"] = params.query.toLowerCase()+"%"
			activeFilters["name"] = params.query
		}

		if(params.user){
			params.user = params.user.toLong()
			query += "  ,UserGroupMemberRole userGroupMemberRole "
			filterQuery += " and userGroupMemberRole.userGroup.id=uGroup.id and userGroupMemberRole.sUser.id=:user ";
			queryParams["user"] = params.user
			activeFilters["user"] = params.user
		}

		if(params.observation){
			params.observation = params.observation.toLong()
			query += " join uGroup.observations observation "
			filterQuery += " and observation.id=:observation and observation.isDeleted=:obvIsDeleted ";
			queryParams["observation"] = params.observation
			queryParams["obvIsDeleted"] = false
			activeFilters["observation"] = params.observation
		}

		if(params.sGroup){
			params.sGroup = params.sGroup.toLong()
			def groupId = utilsService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			}else{
				query += " join uGroup.speciesGroups speciesGroup "
				filterQuery += " and speciesGroup.id = :groupId"
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}

		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			query += " join uGroup.habitats habitat "
			filterQuery += " and habitat.id = :habitat "
			queryParams["habitat"] = params.habitat
			activeFilters["habitat"] = params.habitat
		}

		def sortOption = (params.sort ? ((params.sort=='score')?'name':params.sort) : "visitCount");
		def orderByClause = " order by uGroup." + sortOption +  " desc, uGroup.id asc"

		query += filterQuery + orderByClause
		if(max != -1)
			queryParams["max"] = max
		if(offset != -1)
			queryParams["offset"] = offset

		log.debug "Getting filtered usergroups $query $queryParams";
		def userGroupInstanceList = UserGroup.executeQuery(query, queryParams)

		return [userGroupInstanceList:userGroupInstanceList, queryParams:queryParams, activeFilters:activeFilters]
	}

	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private String getIdList(l){
		return l.toString().replace("[", "(").replace("]", ")")
	}

	/////////////// OBSERVATIONS RELATED /////////////////
	void postObservationtoUserGroups(Observation observation, List userGroupIds, boolean sendMail=true) {
		log.debug "Posting ${observation} to userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					postObservationToUserGroup(observation, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void postObservationToUserGroup(Observation observation, UserGroup userGroup, boolean sendMail = true) {
		List obvs = [observation]
		if(observation.instanceOf(Checklists)){
			obvs.addAll(observation.observations)
		}
		obvs.collate(ResourceUpdate.POST_BATCH_SIZE).each  { subList ->
			Observation.withNewTransaction{
				subList.each {
					userGroup.addToObservations(it);
				}
			}
		}
		if(!userGroup.save()) {
			log.error "Could not add ${obvs} to ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(observation, userGroup, observation.author, true, sendMail);
			//utilsService.sendNotificationMail(activityFeedService.OBSERVATION_POSTED_ON_GROUP, observation, null, null, activityFeed);
			log.debug "Added ${observation} to userGroup ${userGroup}"
		}
	}

	void removeObservationFromUserGroups(Observation observation, List userGroupIds, boolean sendMail=true) {
		log.debug "Removing ${observation} from userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					removeObservationFromUserGroup(observation, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void removeObservationFromUserGroup(Observation observation, UserGroup userGroup, boolean sendMail = true) {
		List obvs = [observation]
		if(observation.instanceOf(Checklists)){
			obvs.addAll(observation.observations)
		}
		obvs.collate(ResourceUpdate.POST_BATCH_SIZE).each  { subList ->
			Observation.withNewTransaction{
				subList.each {
					userGroup.observations.remove(it);
				}
			}
		}
		if(!userGroup.save()) {
			log.error "Could not remove ${obvs} from ${usergroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(observation, userGroup, observation.author, false, sendMail);
			//utilsService.sendNotificationMail(activityFeedService.OBSERVATION_REMOVED_FROM_GROUP, observation, null, null, activityFeed);
			log.debug "Removed ${observation} from userGroup ${userGroup}"
		}
	}

	def getObservationUserGroups(Observation observationInstance, int max, long offset) {
		return observationInstance.userGroups;
	}

	long getNoOfObservationUserGroups(Observation observationInstance) {
		String countQuery = "select count(*) from UserGroup userGroup " +
				"join userGroup.observations observation " +
				"where observation=:observation and observation.isDeleted=:obvIsDeleted	and userGroup.isDeleted=:userGroupIsDeleted";
		def count = UserGroup.executeQuery(countQuery, [observation:observationInstance, obvIsDeleted:false, userGroupIsDeleted:false])
		return count[0]
	}

	def long getCountByGroup(String objectType, UserGroup userGroupInstance){
        long count = 0;
        def query = '';
        def queryParams = [:]

        if(userGroupInstance)
            queryParams['userGroup'] = userGroupInstance

        switch(objectType) {
            case Observation.simpleName:
            queryParams['isDeleted'] = false;
            queryParams['isChecklist'] = false;
            //queryParams['isShowable'] = true;
            query = "select count(*) from Observation obv "
            if(userGroupInstance)
                query += "join obv.userGroups userGroup where userGroup=:userGroup and "
            query += " obv.isDeleted = :isDeleted and obv.isChecklist = :isChecklist "// and obv.isShowable = :isShowable"
            count =  Observation.executeQuery(query, queryParams, [cache:true])[0]
            break;
            case Checklists.simpleName:
            queryParams['isDeleted'] = false;
            queryParams['isChecklist'] = true;
            queryParams['isShowable'] = true;
            query = "select count(*) from Observation obv "
            if(userGroupInstance)
                query += "join obv.userGroups userGroup where userGroup=:userGroup and "
            query += " obv.isDeleted = :isDeleted and obv.isChecklist = :isChecklist and obv.isShowable = :isShowable"
            count =  Observation.executeQuery(query, queryParams, [cache:true])[0]
            break;
            case Species.simpleName :
            query = "select count(*) from Species obv "
            if(userGroupInstance)
                query += "join obv.userGroups userGroup where userGroup=:userGroup"
            count =  Species.executeQuery(query, queryParams, [cache:true])[0]
            break;
            case Document.simpleName :
            query = "select count(*) from Document obv "
            if(userGroupInstance)
                query += "join obv.userGroups userGroup where userGroup=:userGroup"
            count =  Document.executeQuery(query, queryParams, [cache:true])[0]
			
			case Discussion.simpleName :
			query = "select count(*) from Discussion obv "
			if(userGroupInstance)
				query += "join obv.userGroups userGroup where userGroup=:userGroup"
			count =  Discussion.executeQuery(query, queryParams, [cache:true])[0]
        }
        return count;
	}
	



	////////////////////MEMBERS RELATED/////////////////////////
	/**
	 *
	 * @param userGroup
	 * @param user
	 * @param Role
	 * @param role
	 * @param permission
	 */
	@Transactional
	boolean addMember(UserGroup userGroup, SUser user, Role role, Permission... permissions) {
		log.debug "Adding member ${user} with role ${role} to group ${userGroup}"
		log.debug "Granting permissions ${permissions}"
		def userMemberRole = UserGroupMemberRole.findBySUserAndUserGroup(user, userGroup);
		if(!userMemberRole) {
			userMemberRole = UserGroupMemberRole.create(userGroup, user, role);
			if(userMemberRole) {
				permissions.each { permission ->
					addPermission userGroup, user, permission
				}
				activityFeedService.addActivityFeed(userGroup, user, user, activityFeedService.MEMBER_JOINED);
			}
			return true;
		} else {
			log.debug "${user} is already a member of ${userGroup}"
			if(userMemberRole.role.id != role.id) {
				log.debug "Assigning a new role ${role}"
				def prevRole = userMemberRole.role;
				if(UserGroupMemberRole.setRole(userGroup, user, role) > 0) {
					deletePermissionsAsPerRole(userGroup, user, prevRole);
					permissions.each { permission ->
						addPermission userGroup, user, permission
					}
					log.debug "Updated permissions as per new role"
					activityFeedService.addActivityFeed(userGroup, user, user, activityFeedService.MEMBER_ROLE_UPDATED);
					return true;
				} else {
					log.error "error while updating role for ${userMemberRole}"
				}
			}
		}
		return false;
	}

	void addMember(UserGroup userGroup, SUser user, Role role, List<Permission> permissions) {
		addMember userGroup, user, role, permissions as Permission[]
	}

	@Transactional
	boolean deleteMember(UserGroup userGroup, SUser user, Role role) {
		log.debug "Deleting member ${user} with role ${role} from group ${userGroup}"
		if(UserGroupMemberRole.remove(userGroup, user, role)) {
			return deletePermissionsAsPerRole(userGroup, user, role);
		}
		return false;

		///////////////////IMPORTANT//////////////////////////
		//TODO:delete obvs posted by him in this group
		/////////////////////////////////////////////////////
	}


	//TODO:need to make this better by providing a mapping between this role and associated permissions
	private deletePermissionsAsPerRole(UserGroup userGroup, SUser user, Role role) {
		log.debug "Deleting permissions for member ${user} who had role ${role} in group ${userGroup}"
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def expertRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		switch(role.id) {
			case founderRole.id :
				log.debug "Deleting admin permission for member ${user} who had role ${role} in group ${userGroup}"
				deletePermission userGroup, user, BasePermission.ADMINISTRATION
				log.debug "Deleting write permission for member ${user} who had role ${role} in group ${userGroup}"
				deletePermission userGroup, user, BasePermission.WRITE
				break;
			case [memberRole.id, expertRole.id] :
				log.debug "Deleting write permission for member ${user} who had role ${role} in group ${userGroup}"
				deletePermission userGroup, user, BasePermission.WRITE
				break;
			default :
				log.error "Prev role is invalid ${role}"
				return false;
		}
		return true;
	}

	////////////////////USERS RELATED/////////////////////////

	int getNoOfUserUserGroups(SUser user) {
		return UserGroupMemberRole.countBySUser(user);
	}

	def getUserUserGroups(SUser user, int max, long offset) {
		if(max==-1 && offset==-1)
			return  UserGroupMemberRole.findAllBySUser(user).groupBy{ it.role };

		return UserGroupMemberRole.findAllBySUser(user,[max:max, offset:offset]).groupBy{ it.role };
	}

	//////////////////User & MAIL RELATED////////////////////

	@PreAuthorize("hasPermission(#userGroupInstance, write) or hasPermission(#userGroup, admin)")
	def setUserGroupFounders(userGroupInstance, founders, foundersMsg, domain) {
		founders.add(springSecurityService.currentUser);
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def groupFounders = UserGroupMemberRole.findAllByUserGroupAndRole(userGroupInstance, founderRole).collect {it.sUser};
		def commons = founders.intersect(groupFounders);
		groupFounders.removeAll(commons);
		founders.removeAll(commons);

		sendFounderInvitation(userGroupInstance, founders, foundersMsg, domain);

		if(groupFounders) {
			groupFounders.each { founder ->
				//deletes as founder
				userGroupInstance.deleteMember (founder);
				//adds as member
				userGroupInstance.addMember(founder);
			}
		}
	}

	@PreAuthorize("hasPermission(#userGroupInstance, write) or hasPermission(#userGroup, admin)")
	void sendFounderInvitation(userGroupInstance, founders, foundersMsg, domain) {

		log.debug "Sending invitation to ${founders}"

		String usernameFieldName = 'name'

		founders.each { founder ->
			if(founder instanceof SUser && founder?.id == springSecurityService.currentUser.id) {
				userGroupInstance.addFounder(founder);
			} else {
				String founderEmail, name, userId;
				if(founder instanceof String) {
					founderEmail = founder
					name = founder.substring(0, founder.indexOf("@"));
					userId = 'register'
				} else {
					founderEmail = founder.email;
					name = founder."$usernameFieldName".capitalize()
					userId = founder.id.toString()
				}

				def userToken = new UserToken(username: name, controller:'userGroup', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':userId, 'role':UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value()]);
				userToken.save(flush: true)
				def userLanguage = utilsService.getCurrentLanguage();
				emailConfirmationService.sendConfirmation(founderEmail,
						"Invitation to join as founder for group",  [name:name, fromUser:springSecurityService.currentUser, foundersMsg:foundersMsg, userGroupInstance:userGroupInstance,domain:domain, view:'/emailtemplates/'+userLanguage.threeLetterCode+'/founderInvitation'], userToken.token);
			}
		}
	}
	
	@PreAuthorize("hasPermission(#userGroupInstance, write) or hasPermission(#userGroup, admin)")
	def setUserGroupExperts(userGroupInstance, experts, expertsMsg, domain) {
		//experts.add(springSecurityService.currentUser);
		def expertRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
		def groupExperts = UserGroupMemberRole.findAllByUserGroupAndRole(userGroupInstance, expertRole).collect {it.sUser};
		def commons = experts.intersect(groupExperts);
		groupExperts.removeAll(commons);
		experts.removeAll(commons);

		sendExpertInvitation(userGroupInstance, experts, expertsMsg, domain);

		if(groupExperts) {
			groupExperts.each { expert ->
				//deletes as expert
				userGroupInstance.deleteMember (expert);
				//adds as member
				userGroupInstance.addMember(expert);
			}
		}
	}

	@PreAuthorize("hasPermission(#userGroupInstance, write) or hasPermission(#userGroup, admin)")
	void sendExpertInvitation(userGroupInstance, experts, expertsMsg, domain) {

		log.debug "Sending invitation to ${experts}"

		String usernameFieldName = 'name'

		experts.each { expert ->
			if(expert instanceof SUser && expert?.id == springSecurityService.currentUser.id) {
				userGroupInstance.addExpert(expert);
			} else {
				String expertEmail, name, userId;
				if(expert instanceof String) {
					expertEmail = expert
					name = expert.substring(0, expert.indexOf("@"));
					userId = 'register'
				} else {
					expertEmail = expert.email;
					name = expert."$usernameFieldName".capitalize()
					userId = expert.id.toString()
				}

				def userToken = new UserToken(username: name, controller:'userGroup', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':userId, 'role':UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value()]);
				userToken.save(flush: true)
				def userLanguage = utilsService.getCurrentLanguage();
				emailConfirmationService.sendConfirmation(expertEmail,
						"Invitation to join as moderator for group",  [name:name, fromUser:springSecurityService.currentUser, expertsMsg:expertsMsg, userGroupInstance:userGroupInstance,domain:domain, view:'/emailtemplates/'+userLanguage.threeLetterCode+'/expertInvitation'], userToken.token);
			}
		}
	}

	@PreAuthorize("hasPermission(#userGroupInstance, write)")
	void sendMemberInvitation(userGroupInstance, members, domain, message=null) {
		//find if the invited members are already part of the group and ignore sending invitation to them
		//def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		def groupMembers = UserGroupMemberRole.findAllByUserGroup(userGroupInstance).collect {it.sUser};
		def commons = members.intersect(groupMembers);
		members.removeAll(commons);
		log.debug "Sending invitation to ${members}"

		String usernameFieldName = 'name'
		members.each { member ->
			String memberEmail, name, userId;
			if(member instanceof String) {
				memberEmail = member
				name = member.substring(0, member.indexOf("@"));
				userId = 'register'
			} else {
				memberEmail = member.email;
				name = member."$usernameFieldName".capitalize()
				userId = member.id.toString()
			}
			def userToken = new UserToken(username: name, controller:'userGroup', action:'confirmMembershipRequest', params:['userGroupInstanceId':userGroupInstance.id.toString(), 'userId':userId, 'role':UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value()]);
			userToken.save(flush: true)
			def userLanguage = utilsService.getCurrentLanguage();
			emailConfirmationService.sendConfirmation(memberEmail,
					"Invitation to join as member in group",  [name:name, fromUser:springSecurityService.currentUser, memberMsg:message, userGroupInstance:userGroupInstance,domain:domain, view:'/emailtemplates/'+userLanguage.threeLetterCode+'/memberInvitation'], userToken.token);
		}
	}

	////////////////////////////// SEaRCH ///////////////////////////

	/**
	 *
	 * @param params
	 * @return
	 */
	def getUserGroupsFromSearch(params) {
		def max = Math.min(params.max ? params.int('max') : 9, 100)
		def offset = params.offset ? params.long('offset') : 0

		def model;

		try {
			model = getFilteredUserGroups(params, max, offset, false);
		} catch(SolrException e) {
			e.printStackTrace();
			//model = [params:params, observationInstanceTotal:0, observationInstanceList:[],  queryParams:[max:0], tags:[]];
		}
		return model;
	}


	def getNewsLetters(UserGroup userGroupInstance,  max,  offset, String sort, String order) {
		String query = "from Newsletter newsletter ";
		def queryParams = [:]
		if(userGroupInstance) {
			queryParams['userGroupInstance'] = userGroupInstance;
			query += " where newsletter.userGroup=:userGroupInstance"
			def author = springSecurityService.currentUser
			//if not logged in user or not a group founder then show only sticky pages
			if(!author || !userGroupInstance.isFounder(author)){
				queryParams['sticky'] = true;
				query += " and newsletter.sticky=:sticky"
			}
		} else {
			query += " where newsletter.userGroup is null"
		}
		
		if(max && max != -1) {
			queryParams['max'] = max;
		}
		if(offset && offset != -1) {
			queryParams['offset'] = offset;
		}
        if(!sort){
            sort = " displayOrder"
        }
        if(!order){
            order = " desc"
        }
		if(sort) {
			sort = sort?:"displayOrder"
			query += " order by newsletter."+sort
		}
		if(order) {
			order = order?:"desc"
			query += " "+order
		}
		log.debug query + " " + queryParams
		return Newsletter.executeQuery(query, queryParams);

	}


	def getUserGroupsStickyPages(UserGroup userGroup, int max, long offset) {


		def queryParams = [:]
		if(userGroup) {
			queryParams['userGroup'] = userGroup;
		}
		if(max != -1) {
			queryParams['max'] = max;
		}
		if(offset != -1) {
			queryParams['offset'] = offset;
		}
		queryParams['sticky'] = true;

		if(userGroup) {
			return Newsletter.executeQuery('from Newsletter newsletter where newsletter.sticky=:sticky and newsletter.userGroup=:userGroup order by newsletter.date asc',
			queryParams);
		} else {
			return Newsletter.executeQuery('from Newsletter newsletter where newsletter.sticky=:sticky and newsletter.userGroup is null order by newsletter.date asc',
			queryParams);
		}

	}

	def getGroupThemes(){
        def themes = ['default'];
        File themesFile = new File(grailsApplication.config.speciesPortal.app.rootDir+'/group-themes/themes.txt');
        if(themesFile.exists()) {
            themesFile.eachLine { theme ->
                themes << theme;
            }
        } else {
            log.error "${themesFile.getAbsolutePath()} does not exist"
        }
        return themes;
	}

	def fetchHomePageTitle(UserGroup userGroupInstance){
		if(!userGroupInstance.homePage)
			return null
		
		//if home page is news letter then getting title from newsletter
		String newsletterId = userGroupInstance.homePage.tokenize('/').last()
		if(newsletterId.isNumber()){
			def newsletter = Newsletter.read(newsletterId.toLong())
			//if news letter not deleted
			if(newsletter)
				return 	newsletter.title
			else
				return null
		}else{
			//returning one of about/activity page
			return userGroupInstance.homePage
		}	
	}
	
	def userGroupBasedLink(attrs) {
        return utilsService.userGroupBasedLink(attrs);
    }

	def nameTerms(params) {
		return getUserGroupSuggestions(params);
	}

	private getUserGroupSuggestions(params){
		def jsonData = []
		String name = params.term

        if(name) {
            String usernameFieldName = 'name';//SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
            String userId = 'id';


            def results = UserGroup.executeQuery(
                    "SELECT DISTINCT u.$usernameFieldName, u.$userId " +
                    "FROM UserGroup u " +
                    "WHERE LOWER(u.$usernameFieldName) LIKE :name " +
                    "ORDER BY u.$usernameFieldName",
                    [name: "${name.toLowerCase()}%"],
                    [max: params.max])

            for (result in results) {
                jsonData << [value: result[0], label:result[0] , userId:result[1] , "category":"Groups"]
            }
        }

		return jsonData;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// User migration to wgp ///////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	def migrateUserPermission(){
		UserGroup wgpGroup = UserGroup.read(1)
		def wgpUserDate = new Date(111, 7, 8)

		int i = 0
		SUser.withTransaction {

			SUser.findAllByDateCreatedGreaterThanEquals(wgpUserDate, [sort:"id", order:"desc"]).each{ user ->

				if(!wgpGroup.isFounder(user)){
					log.debug "user id  $user.id"
					log.debug "adding permission $user"
					addPermissionTest(user.email, 2, i++ )
				}else{
					log.debug "adding foudner permission"
					addPermissionTest(user.email, 16, i++)
				}

			}
			//adding fouder permission to ramesh br
			addPermissionTest(SUser.read(797).email, 2, i++)
			addPermissionTest(SUser.read(797).email, 16, i++)
			
			addPermissionTest(SUser.read(1117).email, 2, i++)
			addPermissionTest(SUser.read(1117).email, 16, i++)
			
			//adding dummy founder 
			addPermissionTest(SUser.read(1184).email, 16, i++)
			addPermissionTest(SUser.read(1188).email, 16, i++)
		}
	}

	def addSpecialFounder(){
		UserGroup wgpGroup = UserGroup.read(36)
		wgpGroup.addFounder(SUser.read(4105))
		log.debug "founder added "
	}

	def addRemaining(){
		UserGroup wgpGroup = UserGroup.read(1)
		def sql =  Sql.newInstance(dataSource);
		sql.eachRow("select id from suser where date_created >= '2011-08-01 00:00:00' and id not in (select s_user_id from user_group_member_role )") { row ->
			def user = SUser.read(row.id.toLong())
			log.debug "user  $user"
			wgpGroup.addMember(user);
		}


	}

	void addPermissionTest(recipient, permission, i) {
		Sid sid = createSid(recipient)

		save new AclEntry(
				aclObjectIdentity: AclObjectIdentity.read(1),
				aceOrder: i,
				sid: createOrRetrieveSid(sid, true),
				mask: permission,
				granting: true,
				auditSuccess: false,
				auditFailure: false)
		log.debug "Added permission $permission for Sid $sid"
	}

	protected AclSid createOrRetrieveSid(Sid sid, boolean allowCreate) {
		Assert.notNull sid, 'Sid required'

		String sidName
		boolean principal
		if (sid instanceof PrincipalSid) {
			sidName = sid.principal
			principal = true
		}
		else if (sid instanceof GrantedAuthoritySid) {
			sidName = sid.grantedAuthority
			principal = false
		}
		else {
			throw new IllegalArgumentException('Unsupported implementation of Sid')
		}

		AclSid aclSid = AclSid.findBySidAndPrincipal(sidName, principal)
		if (!aclSid && allowCreate) {
			aclSid = save(new AclSid(sid: sidName, principal: principal))
		}
		return aclSid
	}

	private Sid createSid(recipient) {
		if (recipient instanceof String) {
			return recipient.startsWith('ROLE_') ?
			new GrantedAuthoritySid(recipient) :
			new PrincipalSid(recipient)
		}

		if (recipient instanceof Sid) {
			return recipient
		}

		if (recipient instanceof Authentication) {
			return new PrincipalSid(recipient)
		}

		throw new IllegalArgumentException('recipient must be a String, Sid, or Authentication')
	}

	private save(bean) {
		bean.validate()
		if (bean.hasErrors()) {
			if (log.isEnabledFor(Level.WARN)) {
				def message = new StringBuilder(
						"problem creating ${bean.getClass().simpleName}: $bean")
				def locale = Locale.getDefault()
				for (fieldErrors in bean.errors) {
					for (error in fieldErrors.allErrors) {
						message.append('\n\t')
						message.append(messageSource.getMessage(error, locale))
					}
				}
				log.warn message
			}
		}
		else {
			if(!bean.save()){
				bean.errors.allErrors.each { println  it }
			}else{
				println "saved succssful"
			}
		}
		bean
	}
	
	
	
	/////////////// DOCUMENTS RELATED /////////////////
	void postDocumenttoUserGroups(Document document, List userGroupIds, boolean sendMail=true) {
		log.debug "Posting ${document} to userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					postDocumentToUserGroup(document, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void postDocumentToUserGroup(Document document, UserGroup userGroup, boolean sendMail=true) {
		userGroup.addToDocuments(document);
		if(!userGroup.save()) {
			log.error "Could not add ${document} to ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(document, userGroup, document.author, sendMail);
			log.debug "Added ${document} to userGroup ${userGroup}"
		}
	}

	void removeDocumentFromUserGroups(Document document, List userGroupIds, boolean sendMail=true) {
		log.debug "Removing ${document} from userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong("" + it));
				if(userGroup) {
					removeDocumentFromUserGroup(document, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void removeDocumentFromUserGroup(Document document, UserGroup userGroup, boolean sendMail=true) {
		userGroup.documents.remove(document);
		if(!userGroup.save()) {
			log.error "Could not remove ${document} from ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(document, userGroup, document.author, sendMail);
			log.debug "Removed ${document} from userGroup ${userGroup}"
		}
	}

	def getDocumentUserGroups(Document documentInstance, int max, long offset) {
		return documentInstance.userGroups;
	}

	long getNoOfDocumentUserGroups(Document documentInstance) {
		String countQuery = "select count(*) from UserGroup userGroup " +
				"join userGroup.documents document " +
				"where document=:document and document.isDeleted=:docIsDeleted	and userGroup.isDeleted=:userGroupIsDeleted";
		def count = UserGroup.executeQuery(countQuery, [document:documentInstance, docIsDeleted:false, userGroupIsDeleted:false])
		return count[0]
	}

	def long getDocumentCountByGroup(UserGroup userGroupInstance){
		def queryParams = [:]
		queryParams['userGroup'] = userGroupInstance
		queryParams['isDeleted'] = false;
		
		def query = "select count(*) from Document doc join doc.userGroups userGroup where doc.isDeleted = :isDeleted and userGroup=:userGroup"
		return Document.executeQuery(query, queryParams)[0]
	}
	
	/////////////// Discussion RELATED /////////////////
	void postDiscussiontoUserGroups(Discussion discussion, List userGroupIds, boolean sendMail=true) {
		log.debug "Posting ${discussion} to userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					postDiscussionToUserGroup(discussion, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void postDiscussionToUserGroup(Discussion discussion, UserGroup userGroup, boolean sendMail=true) {
		userGroup.addToDiscussions(discussion);
		if(!userGroup.save()) {
			log.error "Could not add ${discussion} to ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(discussion, userGroup, discussion.author, sendMail);
			log.debug "Added ${discussion} to userGroup ${userGroup}"
		}
	}

	void removeDiscussionFromUserGroups(Discussion discussion, List userGroupIds, boolean sendMail=true) {
		log.debug "Removing ${discussion} from userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong("" + it));
				if(userGroup) {
					removeDiscussionFromUserGroup(discussion, userGroup, sendMail)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void removeDiscussionFromUserGroup(Discussion discussion, UserGroup userGroup, boolean sendMail=true) {
		userGroup.discussions.remove(discussion);
		if(!userGroup.save()) {
			log.error "Could not remove ${discussion} from ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			activityFeedService.addFeedOnGroupResoucePull(discussion, userGroup, discussion.author, sendMail);
			log.debug "Removed ${discussion} from userGroup ${userGroup}"
		}
	}

	/////////////// PROJECTS RELATED /////////////////
	void postProjecttoUserGroups(Project project, List userGroupIds) {
		log.debug "Posting ${project} to userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					postProjectToUserGroup(project, userGroup)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void postProjectToUserGroup(Project project, UserGroup userGroup) {
		userGroup.addToProjects(project);
		if(!userGroup.save()) {
			log.error "Could not add ${project} to ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			//activityFeedService.addFeedOnGroupResoucePull(project, userGroup, project.author, true);
			log.debug "Added ${project} to userGroup ${userGroup}"
		}
	}

	void removeProjectFromUserGroups(Project project, List userGroupIds) {
		log.debug "Removing ${project} from userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong("" + it));
				if(userGroup) {
					removeProjectFromUserGroup(project, userGroup)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void removeProjectFromUserGroup(Project project, UserGroup userGroup) {
		userGroup.projects.remove(project);
		if(!userGroup.save()) {
			log.error "Could not remove ${project} from ${userGroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			//activityFeedService.addFeedOnGroupResoucePull(project, userGroup, project.author, false);
			log.debug "Removed ${project} from userGroup ${userGroup}"
		}
	}

	def getProjectUserGroups(Project projectInstance, int max, long offset) {
		//TODO
		return projectInstance.userGroups;
	}
	
	
	def boolean hasPermissionAsPerGroup(object, property, permission){
		def secTagLib = grailsApplication.mainContext.getBean('species.CustomSecurityAclTagLib');
		return secTagLib.hasPermissionAsPerGroup(['permission':permission, 'object':object, 'property':property], 'permitted')
	}
	
	//XXX same call from taglib leadind to no session errro. to avoid that puttins same checkin in service and exposing through domain object 
	def boolean hasPermission(object, permission){
		def secTagLib = grailsApplication.mainContext.getBean('species.CustomSecurityAclTagLib');
		return secTagLib.hasPermission(['permission':permission, 'object':object], 'permitted')
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Bulk posting ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	def updateResourceOnGroup(params){
		def r = [:]
		try{
			List groups = params['userGroups'].split(",").collect {
				UserGroup.read(Long.parseLong(it))
			}
			def objectIds = params['objectIds']
			def domainClass = grailsApplication.getArtefact("Domain",params.objectType)?.getClazz()
			List obvs = []
			if(objectIds && objectIds != ""){
				objectIds.split(",").each { 
					def obj = domainClass.read(Long.parseLong(it))
					obvs << obj
					if(obj.instanceOf(Checklists)){
						obvs.addAll(obj.observations)
					}
				}
			}
			r['resourceObj'] = (params.pullType == 'single')? obvs[0]:null
			
			String submitType = params.submitType
			String objectType = params.objectType
			String groupRes = ""
			String functionString = ""
			switch (objectType) {
				case [Observation.class.getCanonicalName(), Checklists.class.getCanonicalName()]:
					groupRes += 'observations'
					functionString += (submitType == 'post')? 'addToObservations' : 'removeFromObservations'
					break
				case Species.class.getCanonicalName():
					groupRes += 'species'
					functionString += (submitType == 'post')? 'addToSpecies' : 'removeFromSpecies'
					break
				case Document.class.getCanonicalName():
					groupRes += 'documents'
					functionString += (submitType == 'post')? 'addToDocuments' : 'removeFromDocuments'
					break
				case Discussion.class.getCanonicalName():
					groupRes += 'discussions'
					functionString += (submitType == 'post')? 'addToDiscussions' : 'removeFromDiscussions'
					break
				default:
					break
			}
			
			r['msgCode']= new ResourceUpdate().updateResourceOnGroup(params, groups, obvs, groupRes, functionString)
			r['success'] = true
			//r['msgCode']=  (submitType == 'post') ? 'userGroup.default.multiple.posting.success' : 'userGroup.default.multiple.unposting.success'
		}catch (Exception e) {
			e.printStackTrace()
			r['success'] = false
			r['msgCode']= 'error'
		}
		return r
	}
	
	def boolean getResourcePullPermission(params, isBulkPull=true){
		if(!springSecurityService.isLoggedIn()){
			return false
		}
		
		SUser currUser = springSecurityService.currentUser;
		//returning true for user with admin role
		if(utilsService.isAdmin(currUser?.id)){
			return true
		}
		
		int groupCount = UserGroupMemberRole.countBySUser(currUser)
		if(groupCount == 0){
			return false
		}
		
		if(!getExpertGroupsOnly(isBulkPull, params)){
			return true
		}
		//if user is founder or expert in any group then retruing true permission for bulk upload
		//on list apge of any resource (i.e. obv, species, docs)
		return currUser.fetchIsFounderOrExpert()
	}
	
	def boolean getExpertGroupsOnly(boolean isBulkPull, params){
		//resource like species or bulk post can be done only by expert or founder in his group only  
		return (isBulkPull || params.controller == 'species')
	}
	
	private class ResourceUpdate {
		public static final int POST_BATCH_SIZE = 100
		private static final log = LogFactory.getLog(this);
		
		def String updateResourceOnGroup(params, groups, allObvs, groupRes, updateFunction){
			ResourceFetcher rf
			if(params.pullType == 'bulk' && params.selectionType == 'selectAll'){
				rf = new ResourceFetcher(params.objectType, params.filterUrl, params.webaddress)
				List newList = rf.getAllResult()
				newList.removeAll(allObvs)
				allObvs = newList
			}
			
			log.debug " All Resources " +  allObvs.size()
			log.debug " All Groups " + groups
			
			def afDescriptionList = []
			def currUser = springSecurityService.currentUser?:SUser.read(params.author?.toLong()) 
			groups.each { UserGroup ug ->
				def obvs = new ArrayList(allObvs)
				boolean success = postInBatch(ug, obvs, params.submitType, updateFunction, groupRes)
				if(success){
					log.debug "Transcation complete with resource pull now adding feed and sending mail..."
					def af = activityFeedService.addFeedOnGroupResoucePull(obvs, ug, currUser, params.submitType == 'post' ? true: false, false, params.pullType == 'bulk'?true:false)
					afDescriptionList <<  getStatusMsg(af, allObvs[0].class.canonicalName, allObvs.size() - obvs.size(), params.submitType, ug)
				}
			}
			return afDescriptionList.join(" ")
		}
		
		
		private boolean postInBatch(ug, obvs, String submitType, String updateFunction, String groupRes){
			
			UserGroup.withNewTransaction(){  status ->
				if(submitType == 'post'){
					obvs.removeAll(Eval.x(ug, 'x.' + groupRes))
				}else{
					obvs.retainAll(Eval.x(ug, 'x.' + groupRes))
					obvs = getFeatureSafeList(ug, obvs)
				}
			}
			
			if(obvs.isEmpty()){
				log.debug "Nothing to update because of permissoin or not part of group"
				return false
			}
			//XXX: to avoid connection time out posting in batches
			List resSubLists = obvs.collate(POST_BATCH_SIZE)
			resSubLists.each { resList ->
				UserGroup.withNewTransaction(){  status ->
					log.debug submitType + " for group " + ug + "  resources size " +  resList.size()
					ug = ug.merge()
					resList.each { obv ->
						obv = obv.merge()
						Eval.xy(ug, obv,  'x.' + updateFunction + '(y)')
					}
					try{
						ug.save(flush:true, failOnError:true)
					}catch(Exception e){
						ug.errors.allErrors.each { log.debug it }
						status.setRollbackOnly()
						e.printStackTrace()
					} 
				}
			}
			return !ug.hasErrors()
		}
		
		private String getStatusMsg(af, resoruceClassName, remainingCount, submitType, userGroup){
			String msg = af ? (activityFeedService.getContextInfo(af).activityTitle) : ("No " + activityFeedService.getResourceDisplayName(resoruceClassName) +  ((submitType == 'post') ? " posted to group ": " removed from group ") + activityFeedService.getUserGroupHyperLink(userGroup)) 
			if(remainingCount > 0){
				msg += ( ", " + remainingCount + " were " ) + ((submitType == 'post') ? "already part of this group" : "not part of this group")
			}
			msg += "."
			return msg 
		}
		
		private List getFeatureSafeList(ug, obvs){
			SUser currUser = springSecurityService.currentUser;
			//if admin or founder or expert then can un post any featured resource
			if(utilsService.isAdmin(currUser) || ug.isFounder(currUser) || ug.isExpert(currUser)){
				log.debug "prevlidge user in the gropu " + ug + "    uesr " + currUser
				return obvs
			}
			
			def newObvs = []
			obvs.each { obv ->
				if(( obv.metaClass.hasProperty(obv, 'author') && (obv.author == currUser)) || !Featured.isFeaturedAnyWhere(obv)){
					newObvs << obv
					log.debug "User is author or obv is not featured in any group " + currUser
				}
			}
			return newObvs
		}
	}

       ///////////////////////////////// Remove user in bulk ////////////////////////////////
       def removeMemberInBulk(params){
               try{
                       List userIds = []
                       List<Map> content = SpreadsheetReader.readSpreadSheet(params.file, 0, 0);
                       for (Map row : content) {
                               userIds << row.get("userid").toLong();
                       }
                       
                       UserGroup ug = UserGroup.get(params.groupId.toLong())
                       println "user group name " + ug.name + " and userIds " + userIds 

                       
                       UserGroup.withTransaction { 
                               userIds.each { uid ->
                               		SUser u = SUser.get(uid)
                               		if(ug.isMember(u)){
										println  "Deleting user " + uid + " from group " + ug
                                       ug.deleteMember(u)
                               		}else{
                               			println " >>>>>>>>>>>>> Not a member ==== " + uid
                               		}
                                       
                                       
                               }
                       }
               }catch(Exception e){
                       log.error e.printStackTrace()
               }
       }

}
