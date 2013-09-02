package species.auth

import java.util.List;

import org.springframework.security.acls.domain.BasePermission;

import species.Resource;
import species.Resource.ResourceType;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole;
import species.participation.Observation;
import species.participation.ObservationFlag;
import species.participation.RecommendationVote;
import species.participation.curation.UnCuratedVotes;
import species.utils.ImageType;
import species.Habitat;
import species.groups.SpeciesGroup;
import content.eml.Document

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
	boolean hideEmailId = true;
	boolean allowIdentifactionMail = true;


	static hasMany = [openIds: OpenID, flags:ObservationFlag, unCuratedVotes:UnCuratedVotes, observations:Observation, recoVotes:RecommendationVote, groups:UserGroup, speciesGroups:SpeciesGroup, habitats:Habitat, documents:Document ]
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
			return grailsApplication.config.speciesPortal.users.serverURL+this.icon //, type:ResourceType.ICON, title:this.name);
		}


		if(profilePic) {
			return profilePic;
		}

		def baseUrl = grailsApplication.config.speciesPortal.resources.serverURL;
		switch(type) {
			case ImageType.NORMAL :
				case ImageType.LARGE : return baseUrl+"/user_large.png"
			case ImageType.SMALL : return baseUrl+"/user.png"
			case ImageType.VERY_SMALL : return baseUrl+"/user_small.png"
		}
	}

	Set<UserGroup> getUserGroups() {
		def orderedByName = [
			compare:{ a,b ->
				a.name<=>b.name }
		] as Comparator

		def userGroups = new TreeSet(orderedByName)

		def uGroups = UserGroupMemberRole.findAllBySUser(this).collect{it.userGroup}
		uGroups.each {
			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), it, BasePermission.WRITE)) {
				userGroups.add(it)
			}
		}
		//userGroups.asList().sort(true, { a, b -> a.name <=> b.name } as Comparator)
		return userGroups;
	}

	boolean isUserGroupMember(UserGroup userGroup) {
		return UserGroupMemberRole.countBySUserAndUserGroup(this, userGroup) ?: 0
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

}
