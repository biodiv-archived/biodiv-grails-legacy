import java.util.Date;
import java.util.List;
import java.lang.Float;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import species.auth.SUser;

//SUser.executeUpdate("delete SUser s");
def defaultRoleNames = ['ROLE_USER']

new File("/tmp/users.tsv").splitEachLine("\\t") {
	def fields = it;
	def email = fields[0].trim()
	def username = email.split("@")[0].trim()
	def user = new SUser (
			username : username,
			name : username,
			password : username,
			enabled : true,
			accountExpired : false,
			accountLocked : false,
			passwordExpired : false,
			email : email
			);
		
		if(fields[13] && fields[13] != '\\N') {			
			user.timezone = Float.parseFloat(fields[13].trim())
		}

	SUser.withTransaction {
		if(!user.save(flush: true) ){
			user.errors.each { println it; }
		} else {

			def securityConf = SpringSecurityUtils.securityConfig
			Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
			Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz
			PersonRole.withTransaction { status ->
				defaultRoleNames.each { String roleName ->
					String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
					def auth = Authority."findBy${findByField}"(roleName)
					if (auth) {
						PersonRole.create(user, auth)
					} else {
						println "Can't find authority for name '$roleName'"
					}
				}
			}
		}
	}

}


