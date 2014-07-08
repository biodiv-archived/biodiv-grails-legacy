package species.auth.drupal

import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import species.auth.Role;
import species.auth.SUser;
import species.auth.SUserRole;

class DrupalAuthDao {

	static final List DEFAULT_ROLES = [new GrantedAuthorityImpl('ROLE_USER')]
	private static final log = LogFactory.getLog(this);
	
    SUser findUser(long uid) {
        def user = null
        SUser.withTransaction { status ->
            user = SUser.get(uid)
        }
        return user
    }

    SUser create(DrupalAuthToken token) {
        SUser user = SUser.newInstance()
		user.username = token.getPrincipal().toString();
        user.password = token.getCredentials().toString();
        user.id = token.uid
		boolean success = false;
		boolean created = SUser.withTransaction { status ->
			if (user.save(flush:true)) {
				success = true;
			} else {
				user.errors.allErrors.each { log.error it }
			}
		}
		
		if(success) {
			SUserRole.withTransaction { status ->	
				for (String roleName in getRoles(user)) {
					SUserRole.create(user, Role.findByAuthority(roleName.getAuthority()));
				}
		    }
		
        	return user
		}
    }

    Object getPrincipal(SUser user) {
        return user.uid
    }

    Collection<GrantedAuthority> getRoles(SUser user) {
        return DEFAULT_ROLES;
    }
}
