import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import species.Field;
import species.UserGroupTagLib;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.groups.SpeciesGroup;
import species.groups.UserGroupController;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.UserToken;
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

class BootStrap {

	private static final log = LogFactory.getLog(this);
	
	def grailsApplication
	def setupService;
	def namesIndexerService
	def navigationService
	def springSecurityService
	def emailConfirmationService
	def userGroupService
	/**
	 * 
	 */
	def init = { servletContext ->
		//grailsApplication.config.'grails.web.disable.multipart' = true
		initDefs();
		initUsers();
		//initGroups();
		initNames();
		initFilters();
		initEmailConfirmationService();
	}

	def initDefs() {
		if(Field.count() == 0) {
			log.debug ("Initializing db.")
			setupService.setupDefs()
		}
	}
	
	/**
	 * 
	 * @return
	 */
	def initUsers() {
		createOrUpdateUser('admin@strandls.com', 'admin', true);
//		createOrUpdateUser('sravanthi', 'sra123', true);
//		createOrUpdateUser('janaki', 'janaki', false);
//		createOrUpdateUser('prabha', 'prabha', false);
//		createOrUpdateUser('rahool', 'rahool', false);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param isAdmin
	 */
	private void createOrUpdateUser(email, password, boolean isAdmin) {
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(flush:true, failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(flush:true, failOnError: true)
		def fbRole = Role.findByAuthority('ROLE_FACEBOOK') ?: new Role(authority: 'ROLE_FACEBOOK').save(flush:true, failOnError: true)
		def drupalAdminRole = Role.findByAuthority('ROLE_DRUPAL_ADMIN') ?: new Role(authority: 'ROLE_DRUPAL_ADMIN').save(flush:true, failOnError: true)

		def cepfAdminRole = Role.findByAuthority('ROLE_CEPF_ADMIN') ?: new Role(authority: 'ROLE_CEPF_ADMIN').save(flush:true, failOnError: true)
		
		
		def user = SUser.findByEmail(email) ?: new SUser(
				email: email,
				password: password,
				enabled: true).save(failOnError: true)

		if (!user.authorities.contains(userRole)) {
			SUserRole.create user, userRole
		}

		if(isAdmin) {
			if (!user.authorities.contains(adminRole)) {
				SUserRole.create user, adminRole
			}
		}
		
		UserGroupMemberRoleType.each { it ->
			Role.findByAuthority(it.value()) ?: new Role(authority: it.value()).save(flush:true, failOnError: true)
		}
	}

	/**
	 * 	
	 * @return
	 */
	def initGroups() {
		def groups = SpeciesGroup.list();
		def subItems = [];
		def allGroup;
		groups.eachWithIndex { SpeciesGroup group, index ->
			subItems.add([controller:'speciesGroup', title:group.name, order:index, action:'show', params:[id:group.id], path:'show/'+group.id]);
			if(group.name.equalsIgnoreCase("All")) {
				allGroup = group;
			}
		}
		navigationService.registerItem('dashboard', [controller:'speciesGroup', order:30, title:'Groups', action:'list', path:(allGroup)?'show/'+allGroup.id:'list', subItems:subItems])
		navigationService.updated()
	}

	/**
	 * 
	 * @return
	 */
	def initNames() {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		namesIndexerService.load(indexStoreDir);
	}

	/**
	 * 
	 */
	def initFilters() {
		if(grailsApplication.config.checkin.drupal) {
			SpringSecurityUtils.clientRegisterFilter('drupalAuthCookieFilter', SecurityFilterPosition.CAS_FILTER.order + 1);
		}
	}

	def initEmailConfirmationService() {
		emailConfirmationService.onConfirmation = { email, uid, confirmationToken ->
			log.info("User with id $uid has confirmed their email address $email")
			def userToken = UserToken.findByToken(uid);
			if(userToken) {
				userToken.params.tokenId = userToken.id.toString();
				userToken.params.confirmationToken = confirmationToken;
				def userGroupController = new UserGroupController();
				def userGroup = userGroupController.findInstance(Long.parseLong(userToken.params.userGroupInstanceId), null, false);
				println "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
				println userGroup
				println userToken.params
				println userToken.controller
				println userToken.action
				println userGroupService.userGroupBasedLink(mapping: 'userGroupGeneric', controller:userToken.controller, action:userToken.action, userGroup:userGroup, params:userToken.params)
				return [url: userGroupService.userGroupBasedLink(mapping: 'userGroupGeneric', controller:userToken.controller, action:userToken.action, userGroup:userGroup, params:userToken.params)]
			} else {
				//TODO
			}
		  }
		  emailConfirmationService.onInvalid = { uid ->
			log.warn("User with id $uid failed to confirm email address after 30 days")
		  }
		  emailConfirmationService.onTimeout = { email, uid ->
			 log.warn("User with id $uid failed to confirm email address after 30 days")
		  }
	}
	
	/**
	 * 
	 */
	def destroy = {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		//namesIndexerService.store(indexStoreDir);
	}
	
}
