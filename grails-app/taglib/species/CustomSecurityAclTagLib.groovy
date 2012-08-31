package species;

import grails.plugins.springsecurity.acl.SecurityAclTagLib;

import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl
import org.springframework.security.acls.model.ObjectIdentityGenerator
import org.springframework.security.acls.model.ObjectIdentity;

import species.auth.SUser;
import species.groups.UserGroup;


class CustomSecurityAclTagLib extends SecurityAclTagLib {

	static namespace = "customsecurity"

	def grailsApplication;
	
	private ObjectIdentityGenerator objectIdentityGenerator = new ObjectIdentityRetrievalStrategyImpl();

	def isPermittedAsPerGroups = { attrs, body ->
		if (hasPermissionAsPerGroup(attrs, 'permitted')) {
			out << body();
		}
	}

	def hasPermissionForAction = {attrs ->
		out << hasPermission(attrs, 'permitted');
	}
	
	def hasPermissionAsPerGroups = {attrs ->
		out << hasPermissionAsPerGroup(attrs, 'permitted');
	}

	//resolving permissions basing on the usergroups object belongs to in most liberal fashion
	protected boolean hasPermissionAsPerGroup(attrs, String tagName) {

		if (!springSecurityService.isLoggedIn()) {
			return false
		}

		def auth = springSecurityService.authentication
		def perm = assertAttribute('permission', attrs, tagName)
		def permissions = resolvePermissions(perm)

		def object = attrs.remove('object')
		if (!object) {

			def id = assertAttribute('id', attrs, tagName)
			String className = assertAttribute('className', attrs, tagName)
			if (!id || !className) {
				throwTagError "Tag [$tagName] requires either an object or a class name and id"
			}
			object = grailsApplication.getClassForName(className).read(id.toLong());
		}

		if(object) {
			String property = attrs.property
			String propertyCondition = attrs.propertyCondition?:'or'
			boolean isPermitted = resolvePermissionsAsPerGroups(auth, object, property, propertyCondition, permissions);
			log.debug "hasPermissionLiberally ${isPermitted}"
			return isPermitted;
		}
		return false;

	}


	protected boolean resolvePermissionsAsPerGroups(auth, object, specialProperty, propertyCondition, permissions) {
		def userGroups = getObjectUserGroups(object);
		if(!userGroups) return true;

		String userGroupClass = UserGroup.getCanonicalName();
		boolean isPermitted = false;
		userGroups.each { userGroup ->
			if(specialProperty && userGroup."${specialProperty}") isPermitted = true;
			if(specialProperty && userGroup."${specialProperty}") isPermitted = true;
			else {
				if(permissionEvaluator.hasPermission(auth, userGroup.id, userGroupClass, permissions)) isPermitted = true;
			}
			if(isPermitted)
			// returns from closure
			return ;
		}
		return isPermitted;
	}

	protected def getObjectUserGroups(object) {
		return object.getUserGroups();
	}
	
	def hasPermissionToMakeSpeciesCall = {attrs ->
		out << hasPermissionToMakeSpeciesCallAsPerGroups(attrs, 'permitted');
	}
	
	protected hasPermissionToMakeSpeciesCallAsPerGroups(attrs, String tagName) {

		if (!springSecurityService.isLoggedIn()) {
			return false
		}

		def auth = springSecurityService.authentication
		def perm = assertAttribute('permission', attrs, tagName)
		def permissions = resolvePermissions(perm)

		def object = attrs.remove('object')
		if (!object) {

			def id = assertAttribute('id', attrs, tagName)
			String className = assertAttribute('className', attrs, tagName)
			if (!id || !className) {
				throwTagError "Tag [$tagName] requires either an object or a class name and id"
			}
			object = grailsApplication.getClassForName(className).read(id.toLong());
		}

		if(object) {
			boolean isPermitted = resolvePermissionsToMakeSpeciesCallAsPerGroups(auth, object, permissions);
			log.debug "hasPermissionLiberally ${isPermitted}"
			return isPermitted;
		}
		return false;

	}
	
	protected boolean resolvePermissionsToMakeSpeciesCallAsPerGroups(auth, object, permissions) {
		def userGroups = getObjectUserGroups(object);
		if(!userGroups) return true;
		
		def user = SUser.read((long)auth.getPrincipal().id);
		
		String userGroupClass = UserGroup.getCanonicalName();
		boolean isPermitted = false;
		boolean isForExperts = false;
		userGroups.each { userGroup ->
			if(permissionEvaluator.hasPermission(auth, userGroup.id, userGroupClass, permissions)) {
				if(userGroup.allowMembersToMakeSpeciesCall) {
					isPermitted = true;
				} else {
					//chkif user is a founder or expert.
					if(userGroup.isFounder(user) || userGroup.isExpert(user))
						isPermitted = true;
				}
			}
			
			if(isPermitted)
			// returns from closure
			return ;
		}
		return isPermitted;
	}
}
