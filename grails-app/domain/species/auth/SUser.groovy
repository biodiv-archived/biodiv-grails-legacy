package species.auth

import java.util.List;

import org.springframework.security.acls.domain.BasePermission;

import species.Resource;
import species.Resource.ResourceType;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.participation.Observation;
import species.participation.Flag;
import species.participation.RecommendationVote;
import species.participation.curation.UnCuratedVotes;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.utils.ImageType;
import species.Habitat;
import species.groups.SpeciesGroup;
import content.eml.Document
import species.utils.ImageUtils;
import species.SpeciesPermission;
import species.Language;

class SUser {

	transient springSecurityService
	def aclUtilService
	def grailsApplication;
	def commentService;
	def activityFeedService;

	String username
	String name;
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	String email
	Date dateCreated = new Date();
	Date lastLoginDate = new Date();
	String profilePic
	String icon

	String website;
	float timezone=0;//offset
	String aboutMe;
	String location;
	boolean sendNotification = true;
    boolean sendDigest = true;
	boolean hideEmailId = true;
	boolean allowIdentifactionMail = true;

	// Language
    Language language;

	static hasMany = [openIds: OpenID, flags:Flag, unCuratedVotes:UnCuratedVotes, observations:Observation, recoVotes:RecommendationVote, groups:UserGroup, speciesGroups:SpeciesGroup, habitats:Habitat, documents:Document ]
	static belongsTo = [UserGroup]
	//static hasOne = [facebookUser:FacebookUser]

	static constraints = {
		username blank: false
		name blank: false
		password blank: false
		email email: true, blank: false, unique: true, nullable:false
		profilePic nullable:true
		icon nullable:true
		website nullable:true
		timezone nullable:true
		aboutMe nullable:true
		location nullable:true
		lastLoginDate nullable:true
		language nullable:false
	}

	static mapping = {
		/*
		 * Just keep in mind that the UUIDHexGenerator is not generating globally unique identifiers, 
		 * as Java can only acquire the IP address of the machine it’s running on 
		 * and not the MAC address of the network interface. 
		 * Also you have to be careful not to run into any conditions where the external system 
		 * could create the same IDs that you generate internally.
		 */
		id generator:"species.utils.PrefillableUUIDHexGenerator"
		password column: '`password`'
		aboutMe type:"text";
		autoTimestamp false;
	}

	Set<Role> getAuthorities() {
		SUserRole.findAllBySUser(this).collect { it.role } as Set
	}

	def beforeValidate() {
		if(this.email) {
			if(!this.name) {
				this.name = this.email.substring(0, this.email.indexOf('@'));
			}

			if(!this.username) {
				this.username = this.email.substring(0, this.email.indexOf('@'));
			}
		}
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	def beforeDelete() {
		activityFeedService.deleteFeed(this)
		UserGroupMemberRole.removeAll(this);
        //SpeciesPermission.removeAll(this);
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}

	Resource mainImage() {
		return new Resource(fileName:profilePicture());
	}

	def profilePicture() {
		return profilePicture(ImageType.NORMAL);
	}

	def profilePicture(ImageType type) {
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.users.rootDir.toString()+this.icon)).exists()
		if(iconPresent) {
			def thumbnailUrl =  grailsApplication.config.speciesPortal.users.serverURL + "/" + ImageUtils.getFileName(this.icon, type, null)
			return thumbnailUrl;
		}


		if(profilePic) {
			return profilePic;
		}

		def baseUrl = grailsApplication.config.speciesPortal.users.serverURL;
		switch(type) {
			case ImageType.NORMAL :
				case ImageType.LARGE : return baseUrl+"/user_large.png"
			case ImageType.SMALL : return baseUrl+"/user.png"
			case ImageType.VERY_SMALL : return baseUrl+"/user_small.png"
		}
	}

	Set<UserGroup> getUserGroups(onlyExpertGroups=false) {
		def orderedByName = [
			compare:{ a,b ->
				a.name<=>b.name }
		] as Comparator

		def userGroups = new TreeSet(orderedByName)
		def uGroups
		if(onlyExpertGroups){
			uGroups = UserGroupMemberRole.createCriteria().list{
				and{
					eq('sUser', this)
					or{
						eq('role', Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value()))
						eq('role', Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value()))
					}
				}
			}.collect {it.userGroup}
		}else{
			uGroups = UserGroupMemberRole.findAllBySUser(this).collect{it.userGroup}
		}
		uGroups.each {
            try{
			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), it, BasePermission.WRITE)) {
				userGroups.add(it)
			}
            } catch(e) {
                e.printStackTrace()
                log.error e.getMessage();
            }
		}
		//userGroups.asList().sort(true, { a, b -> a.name <=> b.name } as Comparator)
		return userGroups;
	}

	boolean isUserGroupMember(UserGroup userGroup) {
		return UserGroupMemberRole.countBySUserAndUserGroup(this, userGroup) ?: 0
	}
	
	boolean fetchIsFounderOrExpert(){
		return UserGroupMemberRole.createCriteria().count {
			and{
				eq('sUser', this)
				or{
					eq('role', Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_FOUNDER.value()))
					eq('role', Role.findByAuthority(UserGroupMemberRoleType.ROLE_USERGROUP_EXPERT.value()))
				}
			}
		} > 0
	}

	@Override
	String toString() {
		return username;
	}

	def getWebsiteLink(){
		if(website && website.indexOf("://") == -1){
			return "http://" + website
		}else{
			return website;
		}
	}

	def fetchCommentCount(){
		return commentService.getCountByUser(this)
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((email == null) ? 0 : email.hashCode());
		result = prime * result	+ ((username == null) ? 0 : username.hashCode());
		result = prime * result	+ ((name == null) ? 0 : name.hashCode());

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
		if (!(obj instanceof SUser))
			return false;

		SUser other = (SUser) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
        if(username != other.username) return false;
        if(name != other.name) return false;

		return true;
	}

}
