package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import grails.plugins.springsecurity.Secured;
import groovy.sql.Sql;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.transaction.annotation.Transactional

import species.Habitat;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.Role;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.participation.UserToken;
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
	def emailConfirmationService;


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
		userGroup.webaddress = URLEncoder.encode(userGroup.name.replaceAll(" ", "_"), "UTF-8");

		userGroup.icon = getUserGroupIcon(params.icon);
		addInterestedSpeciesGroups(userGroup, params.speciesGroup)
		addInterestedHabitats(userGroup, params.habitat)

                userGroup.sw_latitude = Float.parseFloat(params.sw_latitude);
                userGroup.sw_longitude = Float.parseFloat(params.sw_longitude);
                userGroup.ne_latitude = Float.parseFloat(params.ne_latitude);
                userGroup.ne_longitude = Float.parseFloat(params.ne_longitude);

		if(!userGroup.hasErrors() && userGroup.save()) {
			def tags = (params.tags != null) ? params.tags.values() as List : new ArrayList();
			userGroup.setTags(tags);

			List founders = Utils.getUsersList(params.founderUserIds);
			setUserGroupFounders(userGroup, founders, params.founderMsg, params.domain);
			params.founders = founders;
		}
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

	private void addInterestedSpeciesGroups(userGroupInstance, speciesGroups) {
		log.debug "Adding species group interests ${speciesGroups}"
		userGroupInstance.speciesGroups?.removeAll()
		speciesGroups.each {key, value ->
			userGroupInstance.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}
	}

	private void addInterestedHabitats(userGroupInstance, habitats) {
		log.debug "Adding habitat interests ${habitats}"
		userGroupInstance.habitats?.removeAll()
		habitats.each { key, value ->
			userGroupInstance.addToHabitats(Habitat.read(value.toLong()));
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

	@PreAuthorize("hasPermission(#userGroup, admin)")
	private void deletePermission(UserGroup userGroup, SUser user, Permission permission) {
		aclUtilService.deletePermission(userGroup, user.email, permission);
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
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
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

		def sortOption = (params.sort ? params.sort : "visitCount");
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
		def count = UserGroup.executeQuery(countQuery, [observation:observationInstance, obvIsDeleted:false, userGroupIsDeleted:false])
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
	boolean addMember(UserGroup userGroup, SUser user, Role role, Permission... permissions) {
		log.debug "Adding member ${user} with role ${role} to group ${userGroup}"
		def userMemberRole = UserGroupMemberRole.findBySUserAndUserGroup(user, userGroup);
		if(!userMemberRole) {
			userMemberRole = UserGroupMemberRole.create(userGroup, user, role);
			permissions.each { permission ->
				addPermission userGroup, user, permission
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
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		switch(role.id) {
			case founderRole.id :
				log.debug "Deleting admin permission for member ${user} who had role ${role} in group ${userGroup}"
				deletePermission userGroup, user, BasePermission.ADMINISTRATION
				log.debug "Deleting write permission for member ${user} who had role ${role} in group ${userGroup}"
				deletePermission userGroup, user, BasePermission.WRITE
				break;
			case memberRole.id :
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

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

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
				emailConfirmationService.sendConfirmation(founderEmail,
						"Invitation to join as founder for group",  [name:name, fromUser:springSecurityService.currentUser, foundersMsg:foundersMsg, userGroupInstance:userGroupInstance,domain:domain, view:'/emailtemplates/founderInvitation'], userToken.token);
			}
		}
	}

	@PreAuthorize("hasPermission(#userGroupInstance, write)")
	void sendMemberInvitation(userGroupInstance, members, domain) {
		//find if the invited members are already part of the group and ignore sending invitation to them
		//def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		def groupMembers = UserGroupMemberRole.findAllByUserGroup(userGroupInstance).collect {it.sUser};
		def commons = members.intersect(groupMembers);
		members.removeAll(commons);

		log.debug "Sending invitation to ${members}"

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
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
			emailConfirmationService.sendConfirmation(memberEmail,
					"Invitation to join as member in group",  [name:name, fromUser:springSecurityService.currentUser, userGroupInstance:userGroupInstance,domain:domain, view:'/emailtemplates/memberInvitation'], userToken.token);
		}
	}
}
