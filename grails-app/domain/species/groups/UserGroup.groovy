package species.groups

import org.grails.taggable.Taggable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

import species.Habitat;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.Role;
import species.auth.SUser;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.utils.ImageType;
import species.utils.ImageUtils;

class UserGroup implements Taggable {

	String name;
	String description;
	String aboutUs;
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
	
	def grailsApplication;
	def aclUtilService
	def gormUserDetailsService;
	def springSecurityService;
	def userGroupService;

	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat, observations:Observation]

	static constraints = {
		name nullable: false, blank:false, unique:true
		webaddress nullable: false, blank:false, unique:true
		description nullable: false, blank:false
		//contactEmail nullable:false, blank:false, email:true
		icon nullable:false
		allowObvCrossPosting nullable:false
		allowNonMembersToComment nullable:false
		allowUsersToJoin nullable:false
	}

	static mapping = {
		version  false;
		description type:'text';
		aboutUs type:'text';
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
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.userGroups.rootDir+this.icon)).exists()
		if(!iconPresent) {
			return new Resource(fileName:grailsApplication.config.speciesPortal.resources.serverURL+"/no-image.jpg", type:ResourceType.ICON, title:"");
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
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		return UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole, [max:max, offset:offset]).collect { it.sUser};
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
	
	def getMembers(int max, long offset) {
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return UserGroupMemberRole.findAllByUserGroupAndRole(this, role, [max:max, offset:offset]).collect { it.sUser};
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
			userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
			true;
		}
	}

	boolean deleteMember(SUser member) {
		if(member) {
			def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
			userGroupService.deleteMember(this, member, memberRole);
			true;
		}
	}

	def getAllMembers(int max, long offset) {
		return UserGroupMemberRole.findAllByUserGroup(this, [max:max, offset:offset]).collect { it.sUser};
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
		return UserGroupMemberRole.countByUserGroup(this);
	}

	//TODO:remove
	boolean hasPermission(SUser user, Permission permission) {
		return aclUtilService.hasPermission(gormUserDetailsService.loadUserByUsername(user.email, true), this, permission)
	}

}
