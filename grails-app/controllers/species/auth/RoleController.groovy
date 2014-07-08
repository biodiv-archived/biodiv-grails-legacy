package species.auth

import grails.plugin.springsecurity.annotation.Secured;
import grails.util.GrailsNameUtils;

import grails.plugin.springsecurity.SpringSecurityUtils;

@Secured(['ROLE_ADMIN'])
class RoleController extends grails.plugin.springsecurity.ui.RoleController {

	def edit() {

		String upperAuthorityFieldName = GrailsNameUtils.getClassName(
				SpringSecurityUtils.securityConfig.authority.nameField, null)

		def role = params.name ? lookupRoleClass()."findBy$upperAuthorityFieldName"(params.name) : null
		if (!role) role = findById()
		if (!role) return

			setIfMissing 'max', 10, 100

		def roleClassName = GrailsNameUtils.getShortName(lookupRoleClassName())
		def userField = GrailsNameUtils.getPropertyName(GrailsNameUtils.getShortName(lookupUserClassName()))

		def users = lookupUserRoleClass()."findAllBy$roleClassName"(role, params)*.sUser
		int userCount = lookupUserRoleClass()."countBy$roleClassName"(role)

		[role: role, users: users, userCount: userCount]
	}
}
