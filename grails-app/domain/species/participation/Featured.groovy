package species.participation

import species.groups.UserGroup
import species.auth.SUser
import species.participation.Observation
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import species.auth.Role;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.utils.Utils;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;


class Featured extends AbstractAction {

    def springSecurityService
    def userGroupService;
    def activityFeedService;

    String notes;
    UserGroup userGroup;


    static constraints = {
        author(unique: ['objectId', 'objectType', 'userGroup'])
        userGroup nullable:true
        createdOn nullable:false
        notes nullable:false, blank: false
        notes (size:0..400)
    }



    static boolean deleteFeatureOnObv(object, SUser user, UserGroup ug = null) {
        if(!user) return false;
        boolean isAuthor = (user == object.author)
        boolean isAdmin = SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") 

        if(isAuthor || isAdmin || ug.isFounder(user) || ug.isExpert(user) ) {
            def f = Featured.findWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
            if(f!= null){
                if(!f.delete(flush: true)){
                    println "Error deleting the featured object when Observation got deleted"
                    f.errors.allErrors.each { println it }
                    return null
                }
            }
        }
    }

    /**
    //returns list of groups in which this resource is featured
     */
    static List isFeaturedIn(object){
        def f = Featured.findAllWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        def ugList = []
        f.each{ 
            if(it.notes == null) {
                ugList.add(['userGroup':it.userGroup,'notes':"No Notes!!"])
            }
            else  {
                ugList.add(['userGroup':it.userGroup,'notes':it.notes])
            }
        } 
        return ugList;
    } 

    //ugId null becoz of IBP group
    //returns true if resource featured in particular group
    static String isFeaturedInGroup(object, ugId = null) {
        UserGroup ug
        if(ugId) {
            ug = UserGroup.read(ugId)
        }
        def f = Featured.findWhere(objectType: object.class.getCanonicalName(), objectId: object.id, userGroup: ug)
        if(f) {
            return f.notes
        }
        else {
            return ''
        }
    }

    //returns true if featured in any group
    static boolean isFeaturedAnyWhere(object) {
        def f = Featured.findWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        if(f) {
            return true 
        }
        else {
            return false
        }
    }
}
