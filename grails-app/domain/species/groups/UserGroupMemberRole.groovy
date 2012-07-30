package species.groups

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.LogFactory;

import species.auth.Role;
import species.auth.SUser;

class UserGroupMemberRole implements Serializable {

	UserGroup userGroup
	SUser sUser
	Role role

	private static final log = LogFactory.getLog(this);
	
	public enum UserGroupMemberRoleType {
		ROLE_USERGROUP_FOUNDER("ROLE_USERGROUP_FOUNDER"),
		ROLE_USERGROUP_EXPERT("ROLE_USERGROUP_EXPERT"),
		ROLE_USERGROUP_MEMBER("ROLE_USERGROUP_MEMBER");

		private String value;

		UserGroupMemberRoleType(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}

		static def toList() {
			return [ ROLE_USERGROUP_FOUNDER, ROLE_USERGROUP_EXPERT, ROLE_USERGROUP_MEMBER ]
		}

		public String toString() {
			return this.value();
		}
	
	}
	boolean equals(other) {
		if (!(other instanceof UserGroupMemberRole)) {
			return false
		}
		other.userGroup?.id == userGroup?.id &&
		other.sUser?.id == sUser?.id &&
				other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (userGroup) builder.append(userGroup.id)
		if (sUser) builder.append(sUser.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static UserGroupMemberRole get(long userGroupId, long sUserId, long roleId) {
		find 'from UserGroupMemberRole where userGroup.id=:userGroupId and sUser.id=:sUserId and role.id=:roleId',
				[userGroupId:userGroupId, sUserId: sUserId, roleId: roleId]
	}

	static UserGroupMemberRole create(UserGroup userGroup, SUser sUser, Role role, boolean flush = false) {
		//new UserGroupMemberRole(sUser: sUser, role: role).save(flush: flush, insert: true)
		def sUserRole = new UserGroupMemberRole(userGroup:userGroup, sUser: sUser, role: role);
		if(!sUserRole.save(flush: flush)) {
			log.error sUserRole.errors.allErrors.each { log.error it }
		} else {
			return sUserRole;
		}
	}

	static boolean remove(UserGroup userGroup, SUser sUser, Role role, boolean flush = false) {
		UserGroupMemberRole instance = UserGroupMemberRole.get(userGroup.id, sUser.id, role.id)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(UserGroup userGroup, SUser sUser) {
		executeUpdate 'DELETE FROM UserGroupMemberRole WHERE userGroup=:userGroup and sUser=:sUser', [userGroup:userGroup, sUser: sUser]
	}

	static void removeAll(Role role) {
		executeUpdate 'DELETE FROM UserGroupMemberRole WHERE role=:role', [role: role]
	}

	static mapping = {
		id composite: ['role', 'sUser', 'userGroup']
		version false
	}
}
