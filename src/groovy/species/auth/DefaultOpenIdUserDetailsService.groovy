package species.auth;


import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GormUserDetailsService

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Extends the core plugin's implementation to add in searching by username and
 * the collection of OpenIDs to allow login via linked OpenIDs.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class OpenIdUserDetailsService extends GormUserDetailsService {

	@Override
	UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        username = username.toLowerCase();
        return super.loadUserByUsername(username, loadRoles);
    }

}
