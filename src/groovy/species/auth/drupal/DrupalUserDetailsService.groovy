package species.auth.drupal

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.plugin.springsecurity.GrailsUserDetailsService
import org.codehaus.groovy.grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import species.auth.SUser;

class DrupalUserDetailsService implements GrailsUserDetailsService {
//
//	private Logger _log = Logger.getLogger(getClass())
//
//	/**
//	 * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so
//	 * we give a user with no granted roles this one which gets past that restriction but
//	 * doesn't grant anything.
//	 */
//	static final List USER_ROLES = [
//		new GrantedAuthorityImpl('ROLE_USER')
//	]
//
	/** Dependency injection for the application. */
	def grailsApplication

	public UserDetails loadUserByUsername(String username)
	throws UsernameNotFoundException, DataAccessException {
		loadUserByUsername username, true
	}
//
	public UserDetails loadUserByUsername(String username, boolean loadRoles)
	throws UsernameNotFoundException, DataAccessException {
//		def conf = SpringSecurityUtils.securityConfig
//		String userClassName = conf.userLookup.userDomainClassName
//		def dc = grailsApplication.getDomainClass(userClassName)
//		if (!dc) {
//			throw new RuntimeException("The specified user domain class '$userClassName' is not a domain class")
//		}
//
//		Class<?> User = dc.clazz
//
//		User.withTransaction { status ->
//			def user = User.findWhere((conf.userLookup.usernamePropertyName): username)
//			if (!user) {
//				log.warn "User not found: $username"
//				throw new UsernameNotFoundException('User not found', username)
//			}
//
//			Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
//			createUserDetails user, authorities
//		}
	}
//
//	SUser create(DrupalAuthToken token) {
//		SUser user = SUser.newInstance()
//		user.username = token.username;
//		user.password = token.username;
//		user.id = token.uid
//		boolean success = false;
//		boolean created = SUser.withTransaction { status ->
//			if (user.save(flush:true)) {
//				success = true;
//			} else {
//				user.errors.allErrors.each { log.error it }
//			}
//		}
//		
//		if(success) {
//			SUserRole.withTransaction { status ->
//				for (String roleName in getRoles(user)) {
//					SUserRole.create(user, Role.findByAuthority(roleName.getAuthority()));
//				}
//			}
//		
//			return user
//		}
//	}
//	
//	protected Collection<GrantedAuthority> loadAuthorities(user, String username, boolean loadRoles) {
//		if (!loadRoles) {
//			return []
//		}
//
//		def conf = SpringSecurityUtils.securityConfig
//
//		String authoritiesPropertyName = conf.userLookup.authoritiesPropertyName
//		String authorityPropertyName = conf.authority.nameField
//
//		Collection<?> userAuthorities = user."$authoritiesPropertyName"
//		def authorities = userAuthorities.collect { new GrantedAuthorityImpl(it."$authorityPropertyName") }
//		authorities ?: NO_ROLES
//	}
//
//	protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {
//
//		def conf = SpringSecurityUtils.securityConfig
//
//		String usernamePropertyName = conf.userLookup.usernamePropertyName
//		String passwordPropertyName = conf.userLookup.passwordPropertyName
//		String enabledPropertyName = conf.userLookup.enabledPropertyName
//		String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
//		String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
//		String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName
//
//		String username = user."$usernamePropertyName"
//		String password = user."$passwordPropertyName"
//		boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
//		boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
//		boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
//		boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false
//
//		new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
//				!accountLocked, authorities, user.id)
//	}
//
//	protected Logger getLog() {
//		_log
//	}
}
