package species.auth.drupal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import species.auth.Role;
import species.auth.SUser;
import species.auth.SUserRole;

class DrupalAuthDao {

	static final List DEFAULT_ROLES = [new GrantedAuthorityImpl('ROLE_USER')]

    SUser findUser(long uid) {
        def user = null
        SUser.withTransaction { status ->
            user = SUser.get(uid)
        }
        return user
    }

    SUser create(DrupalAuthToken token) {
        SUser user = SUser.newInstance()
		user.username = token.uid.toString();
        user.password = token.code
        user.id = token.uid
		boolean success = false;
		boolean created = SUser.withTransaction { status ->
			if (!user.save()) {
				success = true;
			}

			for (String roleName in getRoles(user)) {
				SUserRole.create user, Role.findByAuthority(roleName.getAuthority())
			}
		}
		if(success)
        	return user
    }

    Object getPrincipal(SUser user) {
        return user.uid
    }

    Collection<GrantedAuthority> getRoles(SUser user) {
        return DEFAULT_ROLES;
    }
}
