package species.participation

import java.util.List;
import java.util.Map;
import species.TaxonomyDefinition
import species.auth.SUser
import species.participation.NamePermission.Permission
import species.NamesMetadata.NamePosition

class NamePermissionService {

	static transactional = false
	
	
	def boolean hasPermission(params){
		Map m = populateMap(params)
		Permission perm = getRequiredPermission(m)
		boolean retVal = NamePermission.hasPermission(m.user, m.node, perm)
		log.debug "Checking permission for User ${m.user} ::: Node  ${m.node}  ::: Permission ${perm}   ::: RESULT >> ${retVal}"
		return retVal
	}
	
	def NamePermission addPermission(params){
		Map m = populateMap(params)
		return NamePermission.add(m.user, m.node, m.permission)
	}
	
	
	def boolean removePermission(params){
		Map m = populateMap(params)
		return NamePermission.remove(m.user, m.node, m.permission)
	}
	
	def List getAllPermissions(params){
		Map m = populateMap(params)
		return NamePermission.getAllPermissions(m.node, m.user)
	}
	
	private Permission getRequiredPermission(Map m){
		//staring with basic permission
		Permission perm = Permission.EDITOR
		
		//upgrading to CURATOR if name is moving to clean list or name is in cleanlist
		if(m.moveToClean || (m.node.position == NamePosition.CLEAN)){
			perm = Permission.CURATOR
		}
		
		//if input permission is at admin level then checking admin permission
		if(m.Permission == Permission.ADMIN){
			perm = Permission.ADMIN
		}
		
		return perm
	}
	
	private Map populateMap(params){
		//println "========= params " + params
		SUser u = SUser.read(params.user?.toLong())
		TaxonomyDefinition taxon = TaxonomyDefinition.read(params.taxon?.toLong())
		Permission perm = Permission.getPermissionFromStr(params.permission)
		boolean moveToClean = params.moveToClean ? params.moveToClean.toBoolean() : false  
		Map m = ['user':u, 'node':taxon, 'permission':perm, 'moveToClean':moveToClean]
		println "============= populated map  === " + m
		return m
	}

}
		