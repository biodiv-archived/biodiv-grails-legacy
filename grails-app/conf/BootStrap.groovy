import species.SpeciesGroup;
import species.auth.Role;
import species.auth.SUser;
import species.auth.SUserRole;

class BootStrap {

	def grailsApplication
	def namesIndexerService
	def navigationService
	def springSecurityService

	/**
	 * 
	 */
	def init = { servletContext ->
		initUsers();
		initGroups();
		initNames();
	}

	/**
	 * 
	 * @return
	 */
	def initUsers() {
		createOrUpdateUser('admin', 'admin', true);
		createOrUpdateUser('sravanthi', 'sra123', true);
		createOrUpdateUser('janaki', 'janaki', false);
		createOrUpdateUser('prabha', 'prabha', false);
		createOrUpdateUser('rahool', 'rahool', false);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param isAdmin
	 */
	private void createOrUpdateUser(username, password, boolean isAdmin) {
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(flush:true, failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(flush:true, failOnError: true)

		def user = SUser.findByUsername(username) ?: new SUser(
				username: username,
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
	def destroy = {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		namesIndexerService.store(indexStoreDir);
	}
}
