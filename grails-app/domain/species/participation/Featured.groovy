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
        notes nullable:true, blank: true
        notes (size:0..400)
    }
    
    static Set getObjectsUserGroups(long id, String type) {
        def object = activityFeedService.getDomainObject(type,id);
        def f = object.findAllWhere(id: id);
        return f[0].userGroups
    }

    static List getFeaturedUserGroups(long id, String type) {
        println "=============== GET FEATURES ==============="
        def f = Featured.findAllWhere(objectId: id, objectType: type);
        //println "===============F List ==============="
        //println f
        List ug = f.collect{it.userGroup}
        println "=============== FEATURED List ==============="
        println ug
        return ug

    }
    
    static List getAuthorityUserGroups() {
        def user = springSecurityService.getCurrentUser();
		def userGroups = userGroupService.getUserGroups(user);
        List authorityUG = []
        userGroups.each { ugroup ->
            if(ugroup.isFounder(user) || ugroup.isExpert(user)){
                authorityUG.add(ugroup)
            }
            
        }
        println "========== AUTHORTIY UG ============ " + authorityUG
        return authorityUG
    }
    
    static Map getListToFeatureIn(id, type) {
        println "=======FUNC START ====="
        def result = [:]
       	def ug = getFeaturedUserGroups(id, type)
        //println "==========UG from upper function============"
        //println ug
        def objUserGroup = getObjectsUserGroups(id, type)
        println "=======OBJ USER GROUP==========" + objUserGroup
        
        def authorityUG = getAuthorityUserGroup();
        def allowedUserGroups = objUserGroup.intersect(authorityUG)
        println "=======ALLOWED UG========= "  + allowedUserGroups
        def fUserGroups = allowedUserGroups.intersect(ug)

        println "=============== THIS SHOULD BE SELECTED ==============="
        println fUserGroups
        allowedUserGroups.removeAll(fUserGroups);
        fUserGroups.each {
            result[it] = true;
        } 

		allowedUserGroups.each {
			result[it] = false;
		}
        return result;
 
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
        println "======OBJECT INSTANCE=========" + object
        def f = Featured.findAllWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        def ugList = []
        f.each{
            if(it.notes == null) {
                println "==========NULL NOTES==========="
                ugList.add(['userGroup':it.userGroup,'notes':"SORRY!! NO NOTES"])
            }
            else {
                println "========== NOTES PRESENT ==========="
                ugList.add(['userGroup':it.userGroup,'notes':it.notes])
            }
        }
        println "==========UG LIST RETURNED=========" + ugList
        return ugList;
    }
}
