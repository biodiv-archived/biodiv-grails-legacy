package species.participation

import java.util.List;
import java.util.Map;
import species.TaxonomyDefinition
import species.auth.SUser
import species.participation.NamePermission.Permission
import species.NamesMetadata.NamePosition

class NamePermissionService {

	static transactional = false
	
	
	/**
	 * If only one taxon is passed then this will return boolean value.
	 * otherwise return Map<taxonId, boolean> 
	 * called should be careful while using this call	 * 
	 * @param params
	 * @return
	 */
	def hasPermission(Map m){
		//Map m = populateMap(params)
		Permission perm = getRequiredPermission(m)
		def retVal = NamePermission.hasPermission(m.user, m.node, perm)
		log.debug "Checking permission for User ${m.user} ::: Node  ${m.node}  ::: Permission ${perm}   ::: RESULT >> ${retVal}"
		return retVal
	}
	
	def NamePermission addPermission(Map m){
		//Map m = populateMap(params)		
		return NamePermission.add(m.user, m.node, m.permission)
	}
	
	def boolean removePermission(Map m){
		//Map m = populateMap(params)
		return NamePermission.remove(m.user, m.node, m.permission)
	}
	
	def List getAllPermissions(Map m){
		//Map m = populateMap(params)
		return NamePermission.getAllPermissions(m.node, m.user)
	}
	
	def List getAllPermissionsOfUser(Map m){
		//Map m = populateMap(params)
		return NamePermission.getAllPermissions(m.user)
	}
	
	def boolean hasPermissionOnAll(m){
		if(m instanceof Map){
			boolean retVal = true
			m.values().each { boolean v ->
				retVal = (retVal && v)
			}
			return retVal
		}
		
		return m
	}
	
	public Map populateMap(params){
		log.debug "========= params " + params
		SUser u = (params.user)?SUser.read(params.user?.toLong()):null;
		Permission perm = (params.permission)? Permission.getPermissionFromStr(params.permission):null;
		boolean moveToClean = params.moveToClean ? params.moveToClean.toBoolean() : true
		Map m = ['user':u, 'node':getTaxons(params), 'permission':perm, 'moveToClean':moveToClean]
		log.debug "============= populated map  === " + m
		return m
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
	
	private getTaxons(Map m){
		if(m.taxon){
			List tList = m.taxon.split(',').collect { TaxonomyDefinition.read(it.trim().toLong())}
			if(tList.size() == 1)
				return tList[0]
			else
				return tList
		}
	}
}
		