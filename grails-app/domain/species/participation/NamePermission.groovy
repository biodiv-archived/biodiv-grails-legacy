package species.participation

import java.util.Date;

import species.Species;
import species.Classification;
import species.SynonymsMerged
import species.TaxonomyDefinition
import species.auth.SUser;

import org.apache.commons.logging.LogFactory

class NamePermission {
	private static log = LogFactory.getLog(this);
	
	public enum Permission {
		ADMIN("ADMIN"),
		CURATOR("CURATOR"),
		EDITOR("EDITOR"),
		
		private String value;

		Permission(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
		
		static Permission getPermissionFromStr(String s){
			if(!s){
				return null
			}
			Permission perm
			s = s.trim().toUpperCase()
			switch (s){
				case Permission.CURATOR.value():
					perm = Permission.CURATOR
					break
				case Permission.EDITOR.value():
					perm = Permission.EDITOR
					break
				case Permission.ADMIN.value():
					perm = Permission.ADMIN
					break
				default :
					log.error "Permission not found for given String" + s
			}
			return perm
		}
		
		static List toList() {
			return [ ADMIN, CURATOR, EDITOR]
		}
	}
		
	
	TaxonomyDefinition rootNode
	TaxonomyDefinition node
	int rank = -1
	SUser user
	Permission permission
	Date lastUpdated
	
	
	static belongsTo = [user:SUser];
	
    static constraints = {
		node nullable:true
		rootNode nullable:true
		lastUpdated nullable:true
    }
	static mapping = {
		version : false
		node index : 'node_index'
		user index : 'user_index'
		permission : 'permission_index'
    }
	
	static NamePermission add(SUser user, TaxonomyDefinition node, Permission permission = Permission.EDITOR){
		if(isAdmin(user)){
			log.info "$user is already has ADMIN permission"
			return NamePermission.findWhere(user:user, permission:Permission.ADMIN)
		}
		
		if(!node && (permission != Permission.ADMIN)){
			log.error "Node can not be null for the permission " + permission
			return null
		}
		
		NamePermission np
		if(permission == Permission.ADMIN){
			np = new NamePermission(user:user, permission:permission)
		}else{
			np = getDuplicate(node, user, permission)
			if(np){
				np.rank = node.rank
			}else{
				np = new NamePermission(user:user, rootNode:node.fetchRoot(), node:node, rank:node.rank, permission:permission)
			}
		}
		
		if(!np.save(flush:true)){
			np.errors.allErrors.each { log.error it }
			return null
		}else{
			log.debug "Created/updated permission ${np}"
			return np
		}
	}
	
	static boolean remove(SUser user, TaxonomyDefinition node=null, Permission permission = null){
		List npList = NamePermission.createCriteria().list{
			and{
				eq("user", user)
				if(node){
					eq("node", node)
				}
				if(permission){
					eq("permission", permission)
				}
			}
		}
		
		npList.each { np ->
			np.delete(flush: true)
		}
	}
	
	static boolean removeAll(List<NamePermission> npList){
		npList.each { np ->
			np.delete(flush: true)
		}
	}
	
	static boolean hasPermission(SUser user, TaxonomyDefinition node, Permission permission = Permission.EDITOR){
		if(!node){
			log.error "Node is null so not giving any permission"
			return false
		}
		
		if(isAdmin(user)){
			return true
		}
		
		if(node instanceof SynonymsMerged){
			return hasPermissionOnSynonym(user, node, permission)
		}
		
		Classification defClassi = Classification.fetchIBPClassification()
		List tds = node.parentTaxonRegistry(defClassi).get(defClassi)
		if(!tds){
			log.error "Node $node does not have ibp hirerachy so giving permission to everybody"
			return true
		}
		
		boolean hasPerm = false
		tds.each { TaxonomyDefinition td ->
			if(hasPerm) return
			
			List npList = NamePermission.findAllByNodeAndUser(td, user)
			npList.each { NamePermission np ->
				if( !hasPerm && checkValidPermission(np, td, permission)){
					hasPerm = true
				}
			}
		}
		
		return hasPerm		
	}
	

	static Map hasPermission(SUser user, List<TaxonomyDefinition> nodeList, Permission permission = Permission.EDITOR){
		Map retMap = [:]
		nodeList.each { node ->
			retMap.put(node, hasPermission(user, node, permission))
		}
		return retMap
	}

	
	static boolean isAdmin(SUser user){
		if(!user){
			return false
		}
		return (NamePermission.findWhere(user:user, permission:Permission.ADMIN) != null)
	}
	
	static List getAllPermissions(TaxonomyDefinition node, SUser user = null){
		return NamePermission.createCriteria().list{
				and{
					eq("node", node)
					if(user){
						eq("user", user)
					}
				}
			}
	}
	
	static List getAllAdmins(){
		return NamePermission.createCriteria().list{						
					eq("rank", -1)						
				}
	}
	static List getAllPermissionsOfUser(SUser user){
		return NamePermission.findAllByUser(user)
	}
	
	private static boolean hasPermissionOnSynonym(SUser user, SynonymsMerged node, Permission permission){
		List nodes = node.fetchAcceptedNames()
		for(n in nodes){
			if(hasPermission(user, n, permission))
				return true
		}
		return false
	}

	public String toString(){
		return "\n User " + user.id + " ::: Node " + node + " ::: rank " + rank + " ::: permission " + permission + "\n"
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// Private methods ///////////////////////////////////////////	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static boolean checkValidPermission(NamePermission np, TaxonomyDefinition node, Permission permission){
		return (isValidNodeState(np, node) && isPermission(np, permission))
	}
	
	private static NamePermission getDuplicate(TaxonomyDefinition node, SUser user, Permission permission){
		return NamePermission.findWhere(rootNode:node.fetchRoot(), node:node, user:user, permission:permission)
	}
	
	
	private static boolean isPermission(NamePermission np, Permission permission){
		boolean isPerm = false
		switch (permission){
			case Permission.CURATOR:
				isPerm = ((np.permission == permission) ||  (np.permission == Permission.ADMIN))
				break
			case Permission.EDITOR:
				isPerm = ((np.permission == permission) ||  (np.permission == Permission.ADMIN) || (np.permission == Permission.CURATOR))
				break
			case Permission.ADMIN:
				isPerm = (np.permission == permission)
				break
			
			default :
				log.debug "Permission not found " + permission
					
		}
		return isPerm
	}
	
	private static boolean isValidNodeState(NamePermission np, TaxonomyDefinition node){
		//status and position can be added later
		//log.debug " NamePermission " + np + "  node " + node.id  + " rank  " + node.rank  
		return ((np.node == node) && (node.rank >= np.rank) && (np.rootNode == node.fetchRoot()))
	}
	
}
