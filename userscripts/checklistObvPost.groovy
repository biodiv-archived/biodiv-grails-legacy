
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
	File file = new File("/home/sandeept/namesync/isFlaggedToDelete.csv");
	//File file = new File("/apps/git/biodiv/isFlaggedToDelete.csv");
	
	def lines = file.readLines();
	println "============ started =========="
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		def toDelete = Long.parseLong(arr[1].trim())
        def toMove = Long.parseLong(arr[2].trim())
		println "====================  " + toDelete + "   toMove " + toMove
		try {
				ns.mergeAcceptedName(toDelete, toMove)
		}catch(e){
			println e.printStackTrace()
		}
	}
}

//mergeAcceptedName()

def sync(){
	def startDate = new Date()
	println "============ strated "
	def s = ctx.getBean("namesLoaderService");
	s.syncNamesAndRecos(false, false)
	println "========done=== start date " + startDate + "  " + new Date()
}

//sync()


def buildTree(){
	def startDate = new Date()
	def s = ctx.getBean("namesIndexerService");
	s.rebuild()
	println "========done=== start date " + startDate + "  " + new Date()
}
buildTree()

 