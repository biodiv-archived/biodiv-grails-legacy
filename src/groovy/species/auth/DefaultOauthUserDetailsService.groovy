package species.auth;

import com.odobo.grails.plugin.springsecurity.rest.oauth.DefaultOauthUserDetailsService;
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser;
import org.springframework.security.core.userdetails .UserDetails;
import speciespage.SUserService;

class  DefaultOauthUserDetailsService extends DefaultOauthUserDetailsService {
    UserDetails loadUserDetailsByUserProfile(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles)
        throws UsernameNotFoundException {
        UserDetails userDetails
        OauthUser oauthUser

        try {
            log.debug "Trying to fetch user details for user profile: ${userProfile}"
            userDetails = userDetailsService.loadUserByUsername userProfile.email
            //Collection<GrantedAuthority> allRoles = userDetails.authorities + defaultRoles
            //oauthUser = new OauthUser(userDetails.username, userDetails.password, allRoles, userProfile)
            return userDetails;
        } catch (UsernameNotFoundException unfe) {
            log.debug "User not found. Creating a new one with default roles: ${defaultRoles}"
	
	def user = SUserService.create([username:userProfile.getUsername(), name:userProfiler.getDisplayName(), password:'openIdPassword', email:userProfile.getEmail(), profilePic:userProfile.getPictureUrl(),location:userProfile.getLocation(),  accountLocked:false, enabled:true]);
			
			SUserService.save(user);
		
			SUserService.assignRoles(user);
            return user;
        }
    }
}

