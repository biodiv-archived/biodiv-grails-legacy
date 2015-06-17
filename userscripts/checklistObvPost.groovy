import java.sql.SQLClientInfoException;

import  species.groups.UserGroup
import species.participation.Checklists
import groovy.sql.Sql

def migrate(){
	def ugs = ctx.getBean("userGroupService");
	def ds = ctx.getBean('dataSource')
	Sql sql = Sql.newInstance(ds)
	String query = ''' select o.id as oid from observation as o, user_group_observations as ugo where o.id = ugo.observation_id and o.is_checklist = true and ugo.user_group_id = '''; 
	UserGroup.list(sort:"id", order:"asc").each{ UserGroup ug ->
		def objectIds = sql.rows(query + ug.id).oid.collect{'' + it}.join(',')
		def ugids = '' + ug.id
		println "================== " + ugids
		println "====================== " + objectIds
		def m = [author:"1", objectIds:objectIds, submitType:'post', userGroups:ugids, pullType :'bulk', 'objectType':Checklists.class.getCanonicalName() ]
		ugs.updateResourceOnGroup(m)
	}
}

//migrate()

def mergeAcceptedName(){
	def ns = ctx.getBean("namelistService");
    println "============ started =========="
	ns.mergeAcceptedName(46873, 173051)
}

mergeAcceptedName()