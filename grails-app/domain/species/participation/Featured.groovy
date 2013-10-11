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


class Featured {
    
    def springSecurityService
	def userGroupService;
    def activityFeedService;

    SUser author
    long objectId
    String objectType
    Date createdOn = new Date();
	String notes;
    UserGroup userGroup;

    static belongsTo = [author:SUser];

    static constraints = {
        author(unique: ['objectId', 'objectType', 'userGroup'])
        userGroup nullable:true
        notes nullable:true, blank: true
        notes (size:0..400)
    }
    
        

    static void deleteFeatureOnObv(object) {
        def f = Featured.findWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        if(f!= null){
            f.delete(flush: true);
            if(!f.delete(flush: true)){
		            println "Error deleting the featured object when Observation got deleted"
                    f.errors.allErrors.each { println it }
		            return null
                }
        }
    }

    static List isFeaturedIn(object){
        def f = Featured.findAllWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        def ugList = []
        f.each{
            if(it.notes == null) {
                ugList.add(['userGroup':it.userGroup,'notes':"No Notes!!"])
             }
            else {
                ugList.add(['userGroup':it.userGroup,'notes':it.notes])
             }
        } 
        return ugList;
    }
}
