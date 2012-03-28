package species.auth

import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.logging.LogFactory;

class SUserRole implements Serializable {

	SUser sUser
	Role role
	
	private static final log = LogFactory.getLog(this);

	boolean equals(other) {
		if (!(other instanceof SUserRole)) {
			return false
		}

		other.sUser?.id == sUser?.id &&
			other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (sUser) builder.append(sUser.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static SUserRole get(long sUserId, long roleId) {
		find 'from SUserRole where sUser.id=:sUserId and role.id=:roleId',
			[sUserId: sUserId, roleId: roleId]
	}

	static SUserRole create(SUser sUser, Role role, boolean flush = false) {
		//new SUserRole(sUser: sUser, role: role).save(flush: flush, insert: true)
		def sUserRole = new SUserRole(sUser: sUser, role: role); 
		if(!sUserRole.save(flush: flush)) {
			log.error sUserRole.errors.allErrors.each { log.error it }
		}
	}

	static boolean remove(SUser sUser, Role role, boolean flush = false) {
		SUserRole instance = SUserRole.findBySUserAndRole(sUser, role)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(SUser sUser) {
		executeUpdate 'DELETE FROM SUserRole WHERE sUser=:sUser', [sUser: sUser]
	}

	static void removeAll(Role role) {
		executeUpdate 'DELETE FROM SUserRole WHERE role=:role', [role: role]
	}

	static mapping = {
		id composite: ['role', 'sUser']
		version false
	}
}
