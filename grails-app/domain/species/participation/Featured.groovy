package species.participation

import species.groups.UserGroup
import java.util.Date;

import species.auth.SUser
import species.participation.Observation

import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import species.Language;
import species.auth.Role;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.utils.Utils;
import grails.plugin.springsecurity.SpringSecurityUtils;

import org.apache.commons.logging.LogFactory;

class Featured extends AbstractAction {

    def springSecurityService
    def userGroupService;
    def activityFeedService;
	def utilsService;
	
    String notes;
    UserGroup userGroup;
    Language language;
	
	Date expireTime;
	private static final log = LogFactory.getLog(this);

    static constraints = {
        language nullable:false
        author(unique: ['objectId', 'objectType', 'userGroup'])
        userGroup nullable:true
        createdOn nullable:false
        notes nullable:false, blank: false
        notes (size:0..400)
		expireTime nullable:true
    }

    static boolean deleteFeatureOnObv(object, SUser user, UserGroup ug = null) {
        boolean isFeatured = Featured.isFeaturedInGroup(object, ug);
        if(!isFeatured) {
            log.debug "Nothing to delete as object is not featured at all"
            return true;
        }

        boolean isAuthor = (user == object.author)
        boolean isAdmin = SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") 

        if(isAuthor || isAdmin || ug.isFounder(user) || ug.isExpert(user) ) {
            int result = Featured.executeUpdate("delete Featured feat where feat.objectType=:objectType and feat.objectId=:objectId", [objectType:object.class.getCanonicalName(), objectId:object.id])
            if(result > 0) {
                return true
            } else {
                log.error "Error deleting featured entries for ${object}"
            }
        } else {
            log.error "${user} is not Author|Admin|Founder|Expert for ${object} in user group ${ug} to delete"
        }
        return false
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

    static boolean isFeaturedInGroup(object) {
        return featuredNotes(object,null) ? true:false
    }

    //returns true if resource featured in particular group
    static boolean isFeaturedInGroup(object, long ugId) {
        return featuredNotes(object,ugId) ? true:false
    }

    static boolean isFeaturedInGroup(object, UserGroup ug) {
        return featuredNotes(object,ug) ? true:false
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

    static List featuredNotes(object) {
        return featuredNotes(object, null);
    }

    static List featuredNotes(object, long ugId) {
        UserGroup ug
        if(ugId) {
            ug = UserGroup.read(ugId)
        }
        return featuredNotes(object, ug);
    }

    static List featuredNotes(object, UserGroup ug) {
        def fs;
        if(ug)
            fs = Featured.findAllWhere(objectType: object.class.getCanonicalName(), objectId: object.id, userGroup: ug)
        else
            fs = Featured.findAllWhere(objectType: object.class.getCanonicalName(), objectId: object.id)
        return fs; 
    }
	
	
	static List fetchFeature(String classType, UserGroup  ug=null, Date startTime=null, Date endTime=null){
		def res = Featured.withCriteria{
				and{
					eq('objectType', classType)
					if(ug){
						eq('userGroup', ug)
					}
					if(startTime){
						eq('createdOn', startTime)
					}
					if(endTime){
						eq('expireTime', endTime)
					}
				}
				maxResults(5)
				order('createdOn', "desc")
			}
		
		List finalResult = []
		res.each { 
			finalResult << utilsService.getDomainObject(it.objectType, it.id)
		}
		
		return finalResult
	}

}
