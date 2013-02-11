package species.groups
import org.grails.taggable.Taggable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

import groovy.sql.Sql;

import species.Habitat;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.Role;
import species.auth.SUser;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.utils.ImageType;
import species.utils.ImageUtils;
import utils.Newsletter;

class UserGroup implements Taggable {
	
	def dataSource;
	
	String name;
	String description;
	//String aboutUs;
	//String contactEmail;
	String webaddress;
	Date foundedOn = new Date();
	boolean isDeleted = false;
	long visitCount = 0;
	String icon;
	boolean allowObvCrossPosting=true;
	boolean allowNonMembersToComment=true;
	boolean allowUsersToJoin=true;
	boolean allowMembersToMakeSpeciesCall=true;
    float sw_latitude;
    float sw_longitude;
    float ne_latitude;
    float ne_longitude;
	String homePage;
	String theme;
	String domainName;

	def grailsApplication;
	def aclUtilService
	def gormUserDetailsService;
	def springSecurityService;
	def userGroupService;

	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat, observations:Observation, newsletters:Newsletter]

	static constraints = {
		name nullable: false, blank:false, unique:true
		webaddress nullable: false, blank:false, unique:true
		description nullable: false, blank:false
		//contactEmail nullable:false, blank:false, email:true
		icon nullable:true
		allowObvCrossPosting nullable:false
		allowNonMembersToComment nullable:false
		allowUsersToJoin nullable:false
            
        sw_latitude nullable:false
        sw_longitude nullable:false
        ne_latitude nullable:false
        ne_longitude nullable:false
		homePage nullable:true
		theme nullable:true
		domainName nullable:true
	}

	static mapping = {
		version  false;
		description type:'text';
		//aboutUs type:'text';
		sort name:"asc"
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
		+ ((webaddress == null) ? 0 : webaddress.hashCode());
		result = prime * result
		+ ((id == null) ? 0 : id.hashCode());

		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserGroup))
			return false;

		UserGroup other = (UserGroup) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (webaddress == null) {
			if (other.webaddress != null)
				return false;
		} else if (!webaddress.equals(other.webaddress))
			return false;

		if(other.id != id) return false;

		return true;
	}

	Resource icon(ImageType type) {
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.userGroups.rootDir.toString()+this.icon)).exists()
		if(!iconPresent) {
			return new Resource(fileName:grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg", type:ResourceType.ICON, title:"");
		}
		return new Resource(fileName:grailsApplication.config.speciesPortal.userGroups.serverURL+this.icon, type:ResourceType.ICON, title:this.name);
	}

	Resource mainImage() {
		return icon(ImageType.NORMAL);
	}

	def incrementPageVisit(){
		visitCount++;

		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}

	def getPageVisitCount(){
		return visitCount;
	}

	def getFounders(int max, long offset) {
		UserGroupMemberRole.withTransaction {
			def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
			return UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole, [max:max, offset:offset]).collect { it.sUser};
		}
	}

	void setFounders(List<SUser> founders) {
		founders.add(springSecurityService.currentUser);
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def groupFounders = UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole).collect {it.sUser};
		def commons = founders.intersect(groupFounders);
		groupFounders.removeAll(commons);
		founders.removeAll(commons);


		log.debug "Adding new founders ${founders}"
		log.debug "Removing as founders ${groupFounders}"
		if(founders) {
			founders.each { founder ->
				userGroupService.addMember(this,founder, founderRole, [
					BasePermission.ADMINISTRATION,
					BasePermission.WRITE
				]);
			}
		}

		if(groupFounders) {
			groupFounders.each { founder ->
				userGroupService.deleteMember (this, founder, founderRole);
			}
		}
	}

	void addFounder(SUser founder) {
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		userGroupService.addMember(this,founder, founderRole, [
			BasePermission.ADMINISTRATION,
			BasePermission.WRITE
		]);
	}

	def getMembers(int max, long offset, String sortBy) {
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return getUserList(max, offset, sortBy, role.id)
	}
	

	void setMembers(List<SUser> members) {
		if(members) {
			def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
			members.each { member ->
				userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
			}
		}
	}

	boolean addMember(SUser member) {
		if(member) {
			def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
			return userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
		}
		return false;
	}

	boolean deleteMember(SUser member) {
		if(member) {
			def role = getRole(member);
			if(role.authority == UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value() && getFoundersCount() <= 1) {
				return false;
			} else {
				return userGroupService.deleteMember(this, member, role);
			}
		}
	}

	Role getRole(SUser member) {
		return UserGroupMemberRole.findByUserGroupAndSUser(this, member)?.role;
	}

	def getAllMembers(int max, long offset, String sortBy) {
		return getUserList(max, offset, sortBy, null)
	}

	def getMembersCount() {
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return UserGroupMemberRole.countByUserGroupAndRole(this, memberRole);
	}

	def getFoundersCount() {
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		return UserGroupMemberRole.countByUserGroupAndRole(this, founderRole);
	}

	def getAllMembersCount() {
		def c = UserGroupMemberRole.createCriteria()
		def memberCount = c.get {
			eq('userGroup', this)
			projections {
				countDistinct "sUser"
			}
		}
		return memberCount;
	}

	boolean isFounder(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value());
		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
			return true;
		return false
	}

	boolean isExpert(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value());
		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
			return true;
		return false
	}

	boolean isMember(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = getRole(user);
		if(role) return true;
//		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value());
//		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
//			return true;
		return false
	}

	//TODO:remove
	boolean hasPermission(SUser user, Permission permission) {
		return aclUtilService.hasPermission(gormUserDetailsService.loadUserByUsername(user.email, true), this, permission)
	}

	private getUserList(int max, long offset, String sortBy, roleId){
		if(!sortBy || sortBy.trim().equalsIgnoreCase("Activity")){
			def res = []
			def groupId = this.id
			def groupClass = "'" + this.class.getCanonicalName() + "'"
			
			String query = "select umg.s_user_id as user, count(*) as activitycount from user_group_member_role as umg left outer join activity_feed as af on(umg.s_user_id = af.author_id) where umg.user_group_id = $groupId and af.root_holder_id = $groupId and af.root_holder_type = $groupClass"
			query += (roleId) ? " and umg.role_id $roleId" : ""
			query += " group by umg.s_user_id  order by activitycount desc limit $max offset $offset"
			
			log.debug "Getting users list : $query"
			
			def sql =  Sql.newInstance(dataSource);
			sql.rows(query).each{
				res.add(SUser.read(it.getProperty("user")));
			};
			return res;
		}
		
		String sortOrder = sortBy.trim().equalsIgnoreCase("name") ? "asc" : "desc"
		String query = "from UserGroupMemberRole as umr where umr.userGroup = :userGroup"
		query += (roleId) ? " and umg.role.id $roleId" : ""
		query += " order by umr.sUser.$sortBy $sortOrder"
		log.debug "Getting users list : $query"
		return UserGroupMemberRole.findAll(query, [userGroup:this, max:max, offset:offset]).collect{it.sUser}
	}

	def getPages(){
		return userGroupService.getNewsLetters(this, null, null, null, null);
	}
	
	def getThemes(){
		return userGroupService.getGroupThemes()
	}
	
	def fetchHomePageTitle(){
		return userGroupService.fetchHomePageTitle(this)
	}
}
