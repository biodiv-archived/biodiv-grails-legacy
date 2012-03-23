package species.auth

import species.Resource;
import species.Resource.ResourceType;

class SUser {

	transient springSecurityService
	
	def grailsApplication;
	
	String username
	String name;
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	String email
	Date dateCreated
	Date lastLoginDate = new Date();
	String profilePic
	String website;
	float timezone=0;//offset
	String aboutMe;
	String location;
	
    static hasMany = [openIds: OpenID]

	static constraints = {
		username blank: false, unique: true
		password blank: false
		email email: true, blank: false, unique: true
		profilePic nullable:true
		website nullable:true
		timezone nullable:true
		aboutMe nullable:true
		location nullable:true
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
	}

	Set<Role> getAuthorities() {
		SUserRole.findAllBySUser(this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
	
	def icon() {
		if(profilePic) {
			return profilePic;
		} 
		return grailsApplication.config.speciesPortal.resources.serverURL+"/users/user_small.png"
		
	}
	
	@Override
	String toString() {
		return username;
	}
}
