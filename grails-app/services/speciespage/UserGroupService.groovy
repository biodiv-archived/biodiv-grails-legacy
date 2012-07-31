package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import grails.plugins.springsecurity.Secured;
import groovy.sql.Sql;

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.transaction.annotation.Transactional

import species.Resource;
import species.Resource.ResourceType;
import species.auth.Role;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.utils.ImageUtils;
import species.utils.Utils;

class UserGroupService {

	static transactional = false

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService
	def dataSource;
	def observationService;
	def grailsApplication;

	void addPermission(UserGroup userGroup, SUser user, int permission) {
		addPermission userGroup, user, aclPermissionFactory.buildFromMask(permission)
	}

	//@PreAuthorize("hasPermission(#userGroup, admin) or hasRole('ROLE_RUN_AS_ACL_USERGROUP_FOUNDER')")
	@Transactional
	void addPermission(UserGroup userGroup, SUser user, Permission permission) {
		aclUtilService.addPermission userGroup, user.email, permission
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	UserGroup create(params) {
		UserGroup userGroup = new UserGroup();
		userGroup.properties = params;
		userGroup.name = userGroup.name?.capitalize();
		List founders = Utils.getUsersList(params.founderUserIds);
		//List members = Utils.getUsersList(params.memberUserIds);
		userGroup.icon = getUserGroupIcon(params.icon);

		if(userGroup.save()) {
			userGroup.setFounders(founders);
			//userGroup.setMembers(members);
		}
		return userGroup
	}

	//@PreAuthorize("hasPermission(#id, 'species.groups.UserGroup', read) or hasPermission(#id, 'species.groups.UserGroup', admin)")
	UserGroup get(long id) {
		UserGroup.get id
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	//@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<UserGroup> list(Map params) {
		UserGroup.list params
	}

	int count() {
		UserGroup.count()
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write) or hasPermission(#userGroup, admin)")
	void update(UserGroup userGroup, params) {
		userGroup.properties = params
		userGroup.name = userGroup.name?.capitalize();
		List founders = Utils.getUsersList(params.founderUserIds);
		//List members = Utils.getUsersList(params.memberUserIds);
		userGroup.icon = getUserGroupIcon(params.icon);
		userGroup.setFounders(founders);
		//userGroup.setMembers(members);

	}

	private String getUserGroupIcon(String icon) {
		if(!icon) return;

		def resource = null;
		def rootDir = grailsApplication.config.speciesPortal.userGroups.rootDir

		File iconFile = new File(rootDir , Utils.cleanFileName(icon));
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

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, delete) or hasPermission(#userGroup, admin)")
	void delete(UserGroup userGroup) {
		userGroup.delete()
		// Delete the ACL information as well
		aclUtilService.deleteAcl userGroup
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, admin)")
	void deletePermission(UserGroup userGroup, SUser user, Permission permission) {
		def acl = aclUtilService.readAcl(userGroup)
		// Remove all permissions associated with this particular recipient (string equality to KISS)
		acl.entries.eachWithIndex { entry, i ->
			if (entry.sid.equals(user) && entry.permission.equals(permission)) {
				acl.deleteAce i
			}
		}
		aclService.updateAcl acl
	}

	def getUserGroups(SUser userInstance) {
		return userInstance.groups;
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

	protected Map getTagsFromUserGroup(uGroupIds){
		int tagsLimit = 30;
		LinkedHashMap tags = [:]
		if(!uGroupIds){
			return tags
		}

		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as ug_count from tag_links as tl, tags as t, user_group ug where tl.tag_ref in " + getIdList(uGroupIds)  + " and tl.tag_ref = ug.id  and tl.type = 'userGroup' and ug.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("ug_count");
		};
		return tags;
	}

	Map getFilteredTags(params){
		def userGroupInstanceList = getFilteredUserGroups(params, -1, -1, true).userGroupInstanceList
		println userGroupInstanceList;
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
		//params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		//params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		//params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;

		def query = "select uGroup from UserGroup uGroup where uGroup.isDeleted = :isDeleted "
		//def mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv where obv.isDeleted = :isDeleted "
		def queryParams = [isDeleted : false]
		def filterQuery = ""
		def activeFilters = [:]

		//	   if(params.sGroup){
		//		   params.sGroup = params.sGroup.toLong()
		//		   def groupId = getSpeciesGroupIds(params.sGroup)
		//		   if(!groupId){
		//			   log.debug("No groups for id " + params.sGroup)
		//		   }else{
		//			   filterQuery += " and obv.group.id = :groupId "
		//			   queryParams["groupId"] = groupId
		//			   activeFilters["sGroup"] = groupId
		//		   }
		//	   }

		if(params.tag){
			query = "select uGroup from UserGroup uGroup,  TagLink tagLink where uGroup.isDeleted = :isDeleted "
			//mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv, TagLink tagLink where obv.isDeleted = :isDeleted "
			filterQuery +=  " and uGroup.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'userGroup';
			activeFilters["tag"] = params.tag
		}


		//	   if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
		//		   filterQuery += " and obv.habitat.id = :habitat "
		//		   queryParams["habitat"] = params.habitat
		//		   activeFilters["habitat"] = params.habitat
		//	   }
		//
		//	   if(params.user){
		//		   filterQuery += " and obv.author.id = :user "
		//		   queryParams["user"] = params.user.toLong()
		//		   activeFilters["user"] = params.user.toLong()
		//	   }
		//
		//	   if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
		//		   filterQuery += " and obv.maxVotedSpeciesName = :speciesName "
		//		   queryParams["speciesName"] = params.speciesName
		//		   activeFilters["speciesName"] = params.speciesName
		//	   }
		//
		//	   if(params.isFlagged && params.isFlagged.toBoolean()){
		//		   filterQuery += " and obv.flagCount > 0 "
		//	   }
		//
		//	   if(params.bounds){
		//		   def bounds = params.bounds.split(",")
		//
		//		   def swLat = bounds[0]
		//		   def swLon = bounds[1]
		//		   def neLat = bounds[2]
		//		   def neLon = bounds[3]
		//
		//		   filterQuery += " and obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon
		//		   activeFilters["bounds"] = params.bounds
		//	   }
		def sortOption = "foundedOn";//(params.sort ? params.sort : "foundedOn");
		def orderByClause = " order by uGroup." + sortOption +  " desc"
		//
		//	   if(isMapView) {
		//		   query = mapViewQuery + filterQuery + orderByClause
		//	   } else {
		query += filterQuery + orderByClause
		if(max != -1)
			queryParams["max"] = max
		if(offset != -1)
			queryParams["offset"] = offset
		//	   }

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
	void postObservationtoUserGroups(Observation observation, List userGroupIds) {
		log.debug "Posting ${observation} to userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					postObservationToUserGroup(observation, userGroup)
				}
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void postObservationToUserGroup(Observation observation, UserGroup userGroup) {
		userGroup.addToObservations(observation);
		if(!userGroup.save()) {
			log.error "Could not add ${observation} to ${usergroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
			log.debug "Added ${observation} to userGroup ${userGroup}"
		}
	}

	void removeObservationFromUserGroups(Observation observation, List userGroupIds) {
		log.debug "Removing ${observation} from userGroups ${userGroupIds}"
		userGroupIds.each {
			if(it) {
				def userGroup = UserGroup.read(Long.parseLong(it));
				if(userGroup) {
					removeObservationFromUserGroup(observation, userGroup)
				}
			}
		}
	}


	@Transactional
	@PreAuthorize("hasPermission(#userGroup, write)")
	void removeObservationFromUserGroup(Observation observation, UserGroup userGroup) {
		userGroup.observations.remove(observation);
		if(!userGroup.save()) {
			log.error "Could not remove ${observation} from ${usergroup}"
			log.error  userGroup.errors.allErrors.each { log.error it }
		} else {
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
		def count = UserGroup.executeQuery(countQuery, [obvIsDeleted:false, userGroupIsDeleted:false])
		return count[0]
	}

	def getUserGroupObservations(UserGroup userGroupInstance, params, int max, long offset, boolean isMapView=false) {

		if(!userGroupInstance) return;

		def queryParts = observationService.getFilteredObservationsFilterQuery(params);
		queryParts.queryParams['userGroup'] = userGroupInstance
		queryParts.queryParams['isDeleted'] = false;

		String query = queryParts.query;
		String userGroupQuery = " join obv.userGroups userGroup "
		queryParts.filterQuery += " and userGroup=:userGroup "
		if(isMapView) {
			query = queryParts.mapViewQuery + userGroupQuery + queryParts.filterQuery + queryParts.orderByClause
		} else {
			query += userGroupQuery + queryParts.filterQuery + queryParts.orderByClause
			if(max != -1)
				queryParts.queryParams["max"] = max
			if(offset != -1)
				queryParts.queryParams["offset"] = offset
		}

		//		String countQuery = "select count(*) from Observation observation " +
		//			"join observation.userGroups userGroup " +
		//			"where userGroup=:userGroup and observation.isDeleted=:obvIsDeleted	";
		//
		//		def countParams = queryParts.queryParams.clone();
		//		countParams.remove("max");
		//		println countParams;
		//		println queryParts.mapViewQuery
		//		def totalObservationInstanceList = Observation.executeQuery(queryParts.mapViewQuery, countParams)
		//		def count = totalObservationInstanceList.size()

		//		String query = "select observation from Observation observation " +
		//			"join observation.userGroups userGroup " +
		//			"where userGroup=:userGroup and observation.isDeleted=:obvIsDeleted	";
		log.debug query;

		def result=['userGroupInstance':userGroupInstance, 'observationInstanceList': Observation.executeQuery(query, queryParts.queryParams), 'queryParams':queryParts.queryParams, 'activeFilters':queryParts.activeFilters, 'showTags':false];

		return result;
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
	void addMember(UserGroup userGroup, SUser user, Role role, Permission... permissions) {
		def userMemberRole = UserGroupMemberRole.findBySUserAndUserGroup(user, userGroup);
		if(!userMemberRole) {
			userMemberRole = UserGroupMemberRole.create(userGroup, user, role);
		}

		if(userMemberRole.role.id != role.id) {
			def prevRole = userMemberRole.role;
			userMemberRole.role = role
			if(!userMemberRole.save()) {
				log.error userMemberRole.errors.allErrors.each { log.error it }
			} else {
				deletePermissionsAsPerRole(userGroup, user, prevRole);
			}
		}

		permissions.each { permission ->
			addPermission userGroup, user, permission
		}

		//TODO:send invitation requesting for confirmation
		//sendFounderInvitation(user);
	}

	void addMember(UserGroup userGroup, SUser user, Role role, List<Permission> permissions) {
		addMember userGroup, user, role, permissions as Permission[]
	}

	@Transactional
	void deleteMember(UserGroup userGroup, SUser user, Role role) {
		UserGroupMemberRole.remove(userGroup, user, role);
		deletePermissionsAsPerRole(userGroup, user, role);

		///////////////////IMPORTANT//////////////////////////
		//TODO:delete obvs posted by him in this group
		/////////////////////////////////////////////////////
	}


	//TODO:need to make this better by providing a mapping between this role and associated permissions
	private deletePermissionsAsPerRole(UserGroup userGroup, SUser user, Role role) {
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		switch(role.id) {
			case founderRole.id :
				deletePermission userGroup, user, BasePermission.ADMINISTRATION
				deletePermission userGroup, user, BasePermission.WRITE
				break;
			case memberRole.id :
				deletePermission userGroup, user, BasePermission.WRITE
				break;
			default :
				log.error "Prev rle is invalid ${role}"
		}
	}

	////////////////////USERS RELATED/////////////////////////

	int getNoOfUserUserGroups(SUser user) {
		return UserGroupMemberRole.countBySUser(user);
	}
	
	def getUserUserGroups(SUser user, int max, long offset) {
		return UserGroupMemberRole.findAllBySUser(user,[max:max, offset:offset]).collect { it.userGroup};
	}
	
}
