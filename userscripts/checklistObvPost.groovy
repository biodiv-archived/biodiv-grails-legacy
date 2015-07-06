
import  species.groups.UserGroup
import species.participation.Checklists
import groovy.sql.Sql
import species.*
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
	File file = new File("/home/sandeept/namesync/toDelete.csv");
	//File file = new File("/apps/git/biodiv/namelist/tobedeleted_KK.txt");
	
	def lines = file.readLines();
	println "============ started =========="
//	try {
//		ns.mergeAcceptedName(276173, 264706, true)
//	}catch(e){
//		println e.printStackTrace()
//	}
	
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		//println '--------------' + arr
		if(arr.length > 1){
		def toDelete = Long.parseLong(arr[0].trim())
		def fullDelete = (arr[1].trim() == 'Yes') ? true : false
        def toMove = Long.parseLong(arr[2].trim())
		println "====================  " + toDelete + "   toMove " + toMove + "    fullDelete " + fullDelete
		try {
				ns.mergeAcceptedName(toDelete, toMove, fullDelete)
		}catch(e){
			println e.printStackTrace()
		}
	}else{
		println '  leaving arr ' + arr
	}
	}
}

mergeAcceptedName()

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
//buildTree()

def snap(){
	def tt = new species.TaxonController()
	tt.createIBPHierarchyForDirtylist()
}

//snap()


def dmp(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select * from tmp_duplicates  where c > 1 order by rank desc, name asc"
	def taxons = sql.rows(sqlStr);
	int i = 0
	new File("/tmp/higher_rank_duplicates.csv").withWriter { out ->
		out.println "id|name|status|position|paths"
		taxons.each { t ->
			def tds = TaxonomyDefinition.findAllByNameAndRank(t.name, t.rank)
			println i++
			tds.each {  td -> 
			if(!td.isDeleted){
				def hNames = []
				def trs = TaxonomyRegistry.findAllByTaxonDefinition(td)
				trs.each { tr ->
					hNames << tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->")
				}
				out.println td.id + "|" + td.name + "|" + td.status + "|" + td.position + "|" + hNames.join('#')
			}
			}
			
		}
	}
	
}

//dmp()
 