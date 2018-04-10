package species.groups
import org.grails.taggable.Taggable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

import groovy.sql.Sql;

import species.Habitat;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.Role;
import species.auth.SUser;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.Observation;
import species.utils.ImageType;
import species.utils.ImageUtils;
import content.eml.Document;
import content.Project;
import species.Species
import species.Language;
import species.participation.Discussion;
import species.participation.Digest;
import utils.Newsletter;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.grails.web.json.JSONArray;
import com.vividsolutions.jts.geom.GeometryFactory
import content.eml.Coverage;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import species.dataset.Dataset1;
import species.dataset.DataTable;


class UserGroup implements Taggable {
	
	def dataSource;
	def activityFeedService
	
	String name;
	String description;
	//String aboutUs;
	//String contactEmail;
	String webaddress;
	Date foundedOn = new Date();
	boolean isDeleted = false;
	long visitCount = 0;
	String icon;
	boolean allowObvCrossPosting=true;
	boolean allowNonMembersToComment=true;
	boolean allowUsersToJoin=true;
	boolean allowMembersToMakeSpeciesCall=true;
    float sw_latitude;
    float sw_longitude;
    float ne_latitude;
    float ne_longitude;
	String homePage;
	String theme;
	String domainName;
    String filterRule;

	def grailsApplication;
	def aclUtilService
	def gormUserDetailsService;
	def springSecurityService;
	def userGroupService;
	// Language
    Language language;

	
	boolean sendDigestMail=false;
	//when sending digest mail to count no. of days for calulating top contributors
	Date statStartDate = new Date();

	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat, observations:Observation, newsletters:Newsletter, documents:Document, projects:Project, species:Species, discussions:Discussion, dataTables:DataTable, datasets:Dataset1]

	static constraints = {
		name nullable: false, blank:false, unique:true
		webaddress nullable: false, blank:false, unique:true
		description nullable: false, blank:false
		//contactEmail nullable:false, blank:false, email:true
		icon nullable:true
		allowObvCrossPosting nullable:false
		allowNonMembersToComment nullable:false
		allowUsersToJoin nullable:false
            
        sw_latitude nullable:false
        sw_longitude nullable:false
        ne_latitude nullable:false
        ne_longitude nullable:false
		homePage nullable:true
		theme nullable:true
		domainName nullable:true
		language nullable:false
        filterRule nullable:true
	}

	static mapping = {
		version  false;
		description type:'text';
		//aboutUs type:'text';
		sort name:"asc"
        cache true
		filterRule type:'text';
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
		+ ((webaddress == null) ? 0 : webaddress.hashCode());
		result = prime * result
		+ ((id == null) ? 0 : id.hashCode());

		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this.is(obj))
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserGroup))
			return false;

		UserGroup other = (UserGroup) obj;
		
		//reading complete object again
		try {
			other = UserGroup.get(other.id)
			//log.debug other.name
		}catch(e){
			log.error e.getMessage()
            e.printStackTrace();
			UserGroup.withNewSession{
				other = UserGroup.get(other.id)
				//log.debug other.name
			}
		}
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (webaddress == null) {
			if (other.webaddress != null)
				return false;
		} else if (!webaddress.equals(other.webaddress))
			return false;

		if(other.id != id) return false;

		return true;
	}

	Resource icon(ImageType type) {
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.userGroups.rootDir.toString()+this.icon)).exists()
		if(!iconPresent) {
            log.warn "Couldn't find logo at "+grailsApplication.config.speciesPortal.userGroups.rootDir.toString()+this.icon
			return new Resource(fileName:grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg", type:ResourceType.ICON, title:"");
		}
		return new Resource(fileName:grailsApplication.config.speciesPortal.userGroups.serverURL+this.icon, type:ResourceType.ICON, title:this.name);
	}

	Resource mainImage() {
		return icon(ImageType.NORMAL);
	}

    String title() {
        return this.name;
    }

    String notes(Language userLangauge = null) {
        return this.description;
    }

    String summary(Language userLangauge = null) {
        return this.description;
    }

    SpeciesGroup fetchSpeciesGroup() {
        return null;
    }

	def incrementPageVisit(){
		visitCount++;

		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}

	def getPageVisitCount(){
		return visitCount;
	}

	def getFounders(int max, long offset) {
		UserGroupMemberRole.withTransaction {
			def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
			return UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole, [max:max, offset:offset]).collect { it.sUser};
		}
	}
	
	def getExperts(int max, long offset) {
		UserGroupMemberRole.withTransaction {
			def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
			return UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole, [max:max, offset:offset]).collect { it.sUser};
		}
	}


	void setFounders(List<SUser> founders) {
		founders.add(springSecurityService.currentUser);
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		def groupFounders = UserGroupMemberRole.findAllByUserGroupAndRole(this, founderRole).collect {it.sUser};
		def commons = founders.intersect(groupFounders);
		groupFounders.removeAll(commons);
		founders.removeAll(commons);


		log.debug "Adding new founders ${founders}"
		log.debug "Removing as founders ${groupFounders}"
		if(founders) {
			founders.each { founder ->
                addFounder(founder);
			}
		}

		if(groupFounders) {
			groupFounders.each { founder ->
				userGroupService.deleteMember (this, founder, founderRole);
			}
		}
	}

	boolean addFounder(SUser founder) {
		if(founder) {
            boolean success = true;
			if(isMember(founder)){
				success = deleteMember(founder)	
			}
            if(success) {
			    def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
			    return userGroupService.addMember(this, founder, founderRole, [BasePermission.ADMINISTRATION,BasePermission.WRITE]);
            }
		}
		return false;

	}

	def getMembers(int max, long offset, String sortBy) {
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return getUserList(max, offset, sortBy, role.id)
	}
	

	void setMembers(List<SUser> members) {
		if(members) {
			def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
			members.each { member ->
				userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
			}
		}
	}

	void setExperts(List<SUser> members) {
		if(members) {
			members.each { member ->
				userGroupService.addExpert(member);
			}
		}
	}

	
	boolean addMember(SUser member) {
		if(member) {
			def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
			return userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
		}
		return false;
	}
	
	boolean addExpert(SUser member) {
		if(member) {
            boolean success = true;
			if(isMember(member)){
				success = deleteMember(member)	
			}
            if(success) {
			    def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
			    return userGroupService.addMember(this, member, memberRole, BasePermission.WRITE);
            }
		}
		return false;
	}

	boolean deleteMember(SUser member) {
		if(member) {
			def role = getRole(member);
			if(role.authority == UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value() && getFoundersCount() <= 1) {
				return false;
			} else {
				return userGroupService.deleteMember(this, member, role);
			}
		}
	}

	Role getRole(SUser member) {
		return UserGroupMemberRole.findByUserGroupAndSUser(this, member)?.role;
	}

	def getAllMembers(int max, long offset, String sortBy) {
		return getUserList(max, offset, sortBy, null)
	}

	def getMembersCount() {
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value())
		return UserGroupMemberRole.countByUserGroupAndRole(this, memberRole);
	}

	def getExpertsCount() {
		def memberRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value())
		return UserGroupMemberRole.countByUserGroupAndRole(this, memberRole);
	}

	def getFoundersCount() {
		def founderRole = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value())
		return UserGroupMemberRole.countByUserGroupAndRole(this, founderRole);
	}

	def getAllMembersCount() {
        if(id) {
		def c = UserGroupMemberRole.createCriteria()
		def memberCount = c.get {
			eq('userGroup', this)
			projections {
				countDistinct "sUser"
			}
		}
		return memberCount;
        } else {
            return SUser.count();
        }
	}

	boolean isFounder(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value());
		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
			return true;
		return false
	}

	boolean isExpert(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value());
		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
			return true;
		return false
	}

	boolean isMember(SUser user) {
		if(!user) user = springSecurityService.currentUser;
		def role = getRole(user);
		if(role) return true;
//		def role = Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_MEMBER.value());
//		if(UserGroupMemberRole.find("from UserGroupMemberRole umr where umr.userGroup=:userGroup and umr.sUser=:sUser and umr.role=:role", [sUser:user, userGroup:this, role:role]))
//			return true;
		return false
	}

	//TODO:remove
	boolean hasPermission(permission) {
		return userGroupService.hasPermission(this, permission);
	}

	private getUserList(int max, long offset, String sortBy, roleId){
		if(!sortBy || sortBy.trim().equalsIgnoreCase("Activity")){
			def res = []
			def groupId = this.id
			String query = "select umg.s_user_id as user, count(*) as activitycount from user_group_member_role as umg left outer join activity_feed as af on(umg.s_user_id = af.author_id) where umg.user_group_id = $groupId and af.is_showable = true ";
        	query += (roleId) ? " and umg.role_id $roleId" : ""
			query += " group by umg.s_user_id  order by activitycount desc limit $max offset $offset"
			
			//log.debug "Getting users list : $query"
			def sql =  Sql.newInstance(dataSource);
			sql.rows(query).each{
				res.add(SUser.read(it.getProperty("user")));
			};
			return res;
		}
		
		String sortOrder = sortBy.trim().equalsIgnoreCase("name") ? "asc" : "desc"
		String query = "from UserGroupMemberRole as umr where umr.userGroup = :userGroup"
		query += (roleId) ? " and umg.role.id $roleId" : ""
		query += " order by umr.sUser.$sortBy $sortOrder"
		log.debug "Getting users list : $query"
		return UserGroupMemberRole.findAll(query, [userGroup:this, max:max, offset:offset]).collect{it.sUser}
	}

	def getPages(){
		return userGroupService.getNewsLetters(this, null, null, null, null);
	}
	
	def getThemes(){
		return userGroupService.getGroupThemes()
	}
	
	def fetchHomePageTitle(){
		return userGroupService.fetchHomePageTitle(this)
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this)
	}

    def noOfObservations() {
        return userGroupService.getCountByGroup(Observation.simpleName, this.id?this:null);
    }

    def noOfSpecies() {
        return userGroupService.getCountByGroup(Species.simpleName, this.id?this:null);
    }

    def noOfDocuments() {
        return userGroupService.getCountByGroup(Document.simpleName, this.id?this:null);
    }
	
	def noOfDiscussions() {
		return userGroupService.getCountByGroup(Discussion.simpleName, this.id?this:null);
	}
    
    static UserGroup findByWebaddress(webaddress){
    	if(webaddress){
            return UserGroup.createCriteria().get {
                eq 'webaddress', webaddress
                cache true
            }
    	}
    }

    static List<UserGroup> list() { 
        return UserGroup.createCriteria().list {
            order('name', 'asc')
            cache true
        }
    }

    void addFilterRule(String fieldName, String ruleName, List<String> ruleValues) {
        def fRJson = this.filterRule?JSON.parse(this.filterRule):new JSONArray();
        def ruleValuesJSONArray = new JSONArray();
        ruleValuesJSONArray.addAll(ruleValues);
        fRJson.add(['fieldName':fieldName, 'ruleName':ruleName, 'ruleValues':ruleValuesJSONArray]);
        this.filterRule = fRJson.toString();
    }
    
    void setFilterRules(JSONObject filterRules) {
        this.filterRule = filterRules.toString();
    }

    List<FilterRule> getFilterRules() {
        List<FilterRule> filterRules = [];
        if(this.filterRule) {
            JSON.parse(this.filterRule).each {
                def rule = it;
                if(rule.fieldName.equalsIgnoreCase('topology') && rule.ruleName.equalsIgnoreCase('dwithin')) {
                    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
                    if(rule.ruleValues[0]) {
                        WKTReader wkt = new WKTReader(geometryFactory);
                        try {
                            Geometry geom = wkt.read(rule.ruleValues[0]);
                            rule.ruleValues = [geom];
                        } catch(ParseException e) {
                            e.printStackTrace();
                            rule.rulesValues = [];
                        }
                    }
                }
                filterRules << new FilterRule(rule.fieldName, rule.ruleName, rule.ruleValues);
            }
        }
        return filterRules;
    }

    void clearFilterRules() {
        this.filterRule = new JSONArray();
    }

    String getTopologyFilterRuleValue() {
        String topologyFilterRule;
        if(this.filterRule) {
            JSON.parse(this.filterRule).each {
                def rule = it;
                if(rule.fieldName.equalsIgnoreCase('topology') && rule.ruleName.equalsIgnoreCase('dwithin')) {
                    topologyFilterRule = rule.ruleValues[0];
                    return;
               }
            }
        }
        return topologyFilterRule;
    }

    public class FilterRule {
        String fieldName;
        String ruleName;
        List<String> ruleValues;

        FilterRule(String fieldName, String ruleName,  ruleValues) {
            this.fieldName = fieldName;
            this.ruleName = ruleName;
            this.ruleValues = ruleValues;
        }
        
    }
}
