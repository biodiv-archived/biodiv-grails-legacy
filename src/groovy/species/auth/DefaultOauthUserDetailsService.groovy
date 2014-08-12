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
import species.auth.SUser;


class  DefaultOauthUserDetailsService extends DefaultOauthUserDetailsService {
    SUserService userService;

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
            def userProp = new HashMap();
            userProp['username'] = userProfile.getUsername();
            userProp['name']=userProfile.getDisplayName();
            userProp['password']='openIdPassword';
            userProp['email']=userProfile.getEmail();
            userProp['profilePic']=userProfile.getPictureUrl();
            userProp['location']=userProfile.getLocation();
            userProp['accountLocked']=false;
            userProp['enabled']=true;
            def user = userService.create(userProp);
            userService.save(user);
		
			userService.assignRoles(user);
            userDetails = userDetailsService.loadUserByUsername userProfile.email
            return userDetails;
        }
    }

}

