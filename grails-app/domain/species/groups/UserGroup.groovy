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
	String contactEmail;
	String webaddress;
	Date foundedOn = new Date();
	boolean isDeleted = false;
	long visitCount = 0;

	def grailsApplication;
	def aclUtilService
	def gormUserDetailsService;
	def springSecurityService;
	def userGroupService;

	static hasMany = [sGroups:SpeciesGroup, habitats:Habitat, observations:Observation]

	static constraints = {
		name nullable: false, blank:false, unique:true
		webaddress nullable: false, blank:false, unique:true
		description nullable: false, blank:false
		contactEmail nullable:false, blank:false, email:true
	}

	static mapping = {
		version  false;
		description type:'text';
		aboutUs type:'text';
	}

	Resource icon(ImageType type) {
		String name = this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
		name = ImageUtils.getFileName(name, type, '.png');

		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/group_icons/groups/${name}")).exists()
		if(!iconPresent) {
			name = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
			name = ImageUtils.getFileName(name, type, '.png');
		}

		return new Resource(fileName:"group_icons/groups/${name}", type:ResourceType.ICON, title:"You can contribute!!!");
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

	def getFounders() {
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		return UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole).collect { it.sUser};
	}

	void setFounders(List<SUser> founders) {
		founders.add(springSecurityService.currentUser);
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def groupFounders = UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole).collect {it.sUser};
		def commons = founders.intersect(groupFounders);
		groupFounders.removeAll(commons);
		founders.removeAll(commons);

		
		log.debug "Adding new founders ${founders}"
		log.debug "Romoving as founders ${groupFounders}"
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

	def getMembers() {
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return UserGroupMemberRole.findAllByUserGroupAndRole(this, role).collect { it.sUser};
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
	
	def getAllMembers(int max, int offset) {
		return UserGroupMemberRole.findAllByUserGroup(this, [max:max, offset:offset]).collect { it.sUser};
	}

	def getMembersCount() {
		return UserGroupMemberRole.countByUserGroup(this);
	}

	//TODO:remove
	boolean hasPermission(SUser user, Permission permission) {
		return aclUtilService.hasPermission(gormUserDetailsService.loadUserByUsername(user.email, true), this, permission)
	}

}
