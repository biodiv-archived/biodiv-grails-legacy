
import  species.groups.UserGroup
import species.participation.Checklists
import groovy.sql.Sql
import species.*
import grails.converters.JSON
import species.NamesMetadata.NameStatus;

//
//
//def migrate(){
//	def ugs = ctx.getBean("userGroupService");
//	def ds = ctx.getBean('dataSource')
//	Sql sql = Sql.newInstance(ds)
//	String query = ''' select o.id as oid from observation as o, user_group_observations as ugo where o.id = ugo.observation_id and o.is_checklist = true and ugo.user_group_id = '''; 
//	UserGroup.list(sort:"id", order:"asc").each{ UserGroup ug ->
//		def objectIds = sql.rows(query + ug.id).oid.collect{'' + it}.join(',')
//		def ugids = '' + ug.id
//		println "================== " + ugids
//		println "====================== " + objectIds
//		def m = [author:"1", objectIds:objectIds, submitType:'post', userGroups:ugids, pullType :'bulk', 'objectType':Checklists.class.getCanonicalName() ]
//		ugs.updateResourceOnGroup(m)
//	}
//}

def mergeSynonym(){
	def ns = ctx.getBean("namelistUtilService");
	//File file = new File("/home/sandeept/namesync/thomas/duplicatecolidstomergeSynonyms.csv")
	File file = new File("/apps/git/biodiv/namelist/after-migration/duplicatecolidstomergeSynonyms.csv");
	
	def lines = file.readLines();
	println "============ started =========="
//	try {
//		ns.mergeSynonym(385677,358206, true)
//	}catch(e){
//		println e.printStackTrace()
//	}
	def failCountList = []
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println '--------------' + arr
		if(arr.length > 1){
			def toDelete = Long.parseLong(arr[0].trim())
			def fullDelete = (arr[1].trim().toLowerCase() == 'yes') ? true : false
			if(!fullDelete)
				return
	        def toMove = Long.parseLong(arr[2].trim())
			println "====================  " + toDelete + "   toMove " + toMove + "    fullDelete " + fullDelete
			try {
				ns.mergeSynonym(toDelete, toMove, fullDelete)
			}catch(e){
				failCountList << arr
				println e.printStackTrace()
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "=========== failed list " + failCountList
	println "=========== tatal failed " + failCountList.size()

}


def mergeAcceptedName(){
	def ns = ctx.getBean("namelistUtilService");
	//File file = new File("/home/sandeept/namesync/thomas/duplicatestodelete.csv")
	//File file = new File("/apps/git/biodiv/namelist/after-migration/toDelete.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/duplicatestodelete.csv");
	
	def lines = file.readLines();
	println "============ started =========="
//	try {
//		ns.mergeAcceptedName(385677,358206, true)
//	}catch(e){
//		println e.printStackTrace()
//	}
	def failCountList = []
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println '--------------' + arr
		if(arr.length > 1){
			def toDelete = Long.parseLong(arr[0].trim())
			def fullDelete = (arr[1].trim().toLowerCase() == 'yes') ? true : false
			if(!fullDelete)
				return
	        def toMove = Long.parseLong(arr[2].trim())
			println "====================  " + toDelete + "   toMove " + toMove + "    fullDelete " + fullDelete
			try {
				ns.mergeAcceptedName(toDelete, toMove, fullDelete)
			}catch(e){
				failCountList << arr
				println e.printStackTrace()
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "=========== failed list " + failCountList
	println "=========== tatal failed " + failCountList.size()

}

def sync(){
	def startDate = new Date()
	println "============ strated "
	def s = ctx.getBean("namesLoaderService");
	s.syncNamesAndRecos(false, false)
	println "========done=== start date " + startDate + "  " + new Date()
}



def buildTree(){
	def startDate = new Date()
	def s = ctx.getBean("namesIndexerService");
	s.rebuild()
	println "========done=== start date " + startDate + "  " + new Date()
}

def dmp(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select * from tmp_duplicates  where c > 1 order by rank desc, name asc"
	def taxons = sql.rows(sqlStr);
	int i = 0
	new File("/tmp/accepted_duplicates.csv").withWriter { out ->
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

def splitTreeExport(inTaxons = null, boolean filterByIbpClassi = true){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	//String sqlStr = "select taxon_definition_id  as id from tmp_mul_ibp_hier  where c > 1"
	String sqlStr = "select id from taxonomy_definition where id in (160240, 161749, 4414, 170120, 127543, 159416, 170257, 160983, 156734, 167433, 162764, 132778, 146902, 5693, 71886, 161011, 160201, 166095, 87087, 82121, 122152, 176805, 273654, 137733, 168253, 69648, 162621, 276697, 276974, 420941, 277742) "
	
	def taxons = inTaxons ? inTaxons : sql.rows(sqlStr);
	int i = 0
	int totalSizeC = 0
	int totalhier = 0
	
	new File("/tmp/raw_names_without_ibpHir.csv").withWriter { out ->
		//out.println "id|name|status|position|rank|colId|paths|nextChilds"
		taxons.each { t ->
			def tdId = (t instanceof Map) ? t.id : t
			def tdf = TaxonomyDefinition.get(tdId)
			println i++	
			//tds.each {  td ->
			//if(!td.isDeleted){
				def hNames = []
				def nextChild = []
				def trs
				if(filterByIbpClassi){
					trs = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(tdf, Classification.read(265799))
				}else{
					trs = TaxonomyRegistry.findAllByTaxonDefinition(tdf)
				}
				totalhier += trs.size()
				trs.each { tr ->
					hNames << (tr.path + "#" + tr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->"))
					def cTrs =  TaxonomyRegistry.findAllByParentTaxon(tr)
					totalSizeC += cTrs.size()
					cTrs.each { ctr ->
						nextChild << (ctr.path + "#" + ctr.path.split("_").collect{TaxonomyDefinition.read(Long.parseLong(it)).name}.join("->") + ":" + ctr.taxonDefinition.matchId+ ":" + ctr.taxonDefinition.status+ ":" + ctr.taxonDefinition.position)
					}
				}
				out.println tdf.id + "|" + tdf.name + "|" + tdf.status + "|" + tdf.position + "|"+ tdf.rank  + "|" +  tdf.matchId + "|"+  hNames.join('#') + "|" + nextChild.join('$') 
			//}
			//}
			
		}
	}
	
	println "===========================  tatal child count " + totalSizeC + "  total hier " + totalhier + "  total names " + i
	
}

def dropRawHir(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW'"
	def taxons = sql.rows(sqlStr);
	int i = 0
	def trids = [:]
	//def tridsMap = [:]
	taxons.each { t ->
			println  t.id +  "  count " + i++ 
			//sql.executeUpdate(" delete from taxonomy_registry_suser where taxonomy_registry_contributors_id = " + tr.id )
			//sql.executeUpdate(" delete from taxonomy_registry where id in (select id from taxonomy_registry where classification_id = 265799 and path like '%" + t.id + "%')")
		    sql.executeUpdate(" delete from taxonomy_registry where id in ( select id from taxonomy_registry where classification_id = 265799 and (path like '%\\_" + t.id + "\\_%' or path like '" + t.id + "\\_%' or path like '%\\_" + t.id + "'  or path like '" + t.id + "'))" )
			
			
//			String s = "select id from taxonomy_registry where classification_id = 265799 and (path like '%\\_" + t.id + "\\_%' or path like '" + t.id + "\\_%' or path like '%\\_" + t.id + "'  or path like '" + t.id + "')" 
//			def ids = sql.rows(s).collect{it.id}
//            println s
////			println ids
//			trids.putAt(t.id, ids)	
			
	}
//	println trids.size()
//	new File("/home/sandeept/name-mig/d_map.txt").withWriter { out ->
//		  out.println trids as JSON
//	  }
//	println '-------------------------------'
//	println trids
//	println '-------------------------------'
	
	
}
 
def addColhir(){
	def nlSer = ctx.getBean("namelistUtilService");
	
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String sqlStr = "select id from taxonomy_definition where  position = 'WORKING' and status = 'ACCEPTED' and is_deleted = false and id not in ( select distinct(td.id) from taxonomy_definition as td left outer join taxonomy_registry tr on td.id = tr.taxon_definition_id where td.position = 'WORKING' and td.status = 'ACCEPTED' and tr.classification_id = 265799 and tr.id is not  null)"
	def taxons = sql.rows(sqlStr);
	int i = 0
	taxons = taxons.collect { TaxonomyDefinition.get(it.id) }
	
	// to download file if not exist
	nlSer.downloadColXml(taxons)
	
	taxons.each { td ->
		nlSer.updateIBPNameWithCol(td, td.matchId)
	}
	println "============= done "
}

def createDuplicateName(){
	def nlSer = ctx.getBean("namelistUtilService");
	
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	
	//File file = new File("/home/sandeept/namesync/thomas/to_split.csv");
	//File file = new File("/home/sandeept/namesync/thomas/to-split_again.csv");
	//File file = new File("/apps/git/biodiv/namelist/after-migration/to_split.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/to-split_again.csv");
	
	def lines = file.readLines();
	int i=0;
	def ibpId = null
	boolean hasId = false
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		if(arr.length > 1){
			if(arr[0] && (arr[0].trim() != '')){
				ibpId = Long.parseLong(arr[0].trim())
				hasId = true
			}else{
				hasId = false
			}
			
			def colId = arr[1].trim()
			def path = arr[2].trim()
			
			if(!hasId){
				def td = TaxonomyDefinition.get(ibpId)
				nlSer.createDuplicateNameWithNewColId(colId)
				nlSer.addExistingHir(td, colId, path)
			}
			else{
				println "updating only col id"
				def otd = TaxonomyDefinition.get(ibpId)
				otd.matchId = colId
				if(!otd.save(flush:true)){
					otd.errors.allErrors.each { println it }
				}
			}
		}else{
			println '  leaving arr ' + arr
		}
	}
	println "============= done "
}


def updateColId(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	//File file = new File("/home/sandeept/namesync/thomas/validation_fixes.csv");
	//File file = new File("/home/sandeept/namesync/thomas/colIDreplacements.csv");
	//File file = new File("/apps/git/biodiv/namelist/after-migration/validation_fixes.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/colIDreplacements.csv");
	
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		def ibpId = arr[0]//Long.parseLong(arr[0].trim())
		def colId = arr[1]
		sql.executeUpdate(" update taxonomy_definition set match_id = '" + colId + "' where id = " + ibpId);
	}
}
	

def createInputFile(){
	def nlSer = ctx.getBean("namelistUtilService");
	//nlSer.generateStatsInput(new File("/home/sandeept/name-mig/kk_ac_names.csv"))
	nlSer.verifyAcceptedNamesAndColPath(new File("/home/sandeept/name-mig/kk_ac_names.csv"), new File("/home/sandeept/name-mig/kk_ac_in.csv"))
	
}


def copyIbpHir(){
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.copyIBPHirAsCOL()
	
}

def addIBPHirToRawNames(){
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.addIBPHirToRawNames()
	
}

def removeIsDeletedRawName(){
	String s1 = "delete from taxonomy_definition_suser where taxonomy_definition_contributors_id = "
	String s2 = "delete from accepted_synonym where accepted_id = "
	String s3 = "delete from taxonomy_definition_year where taxonomy_definition_id = "
	String s4 = "delete from taxonomy_definition_author where taxonomy_definition_id = "
	String s5 = "delete from doc_sci_name where taxon_concept_id = "
	String s6 = "delete from recommendation where taxon_concept_id = ";
	String s7 = "delete from common_names where taxon_concept_id = "
	String s8 = "delete from synonyms where taxon_concept_id = "
	String s9 = "delete from taxonomy_definition where id = "
	
	List delTableList = [s1, s2, s3, s4, s5, s6, s7, s8, s9]
	
	def nlSer = ctx.getBean("namelistUtilService");
	
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	String unMarkD = "update taxonomy_definition set is_deleted = false where id = "
	
	List delNames = []
	List retainNames = []
	List recoVoteList = []
	String sqlStr = " select id, rank from taxonomy_definition where is_deleted = true and position = 'RAW' and rank = " 
	for(int i = 10; i >= 1 ; i--){
		String selectIdQuery = sqlStr + i  + " order by rank, id"
		//println "select q  " + selectIdQuery
		def taxons = sql.rows(selectIdQuery);
		taxons.each { r ->
			def tId = r.id
			String checkQ =  "select count(*) as c from taxonomy_registry where parent_taxon_definition_id = " + tId 
			int pCount = sql.rows(checkQ)[0].c
			if(pCount == 0){
				String delQ = "delete from taxonomy_registry where taxon_definition_id = " + tId 
				sql.executeUpdate(delQ)
				try{
					delTableList.each { String s ->
						sql.executeUpdate(s + " " + tId)
					}
					delNames << tId
				}catch(e){
					println e.message
					println "--- failed reco vote " +  tId
					recoVoteList << tId
				}
			}else{
				println "--- failed children " +  tId
				retainNames << tId
				sql.executeUpdate(unMarkD + tId)
			}
		}
	}
	
	println "-------------------------del names ------- " + delNames.size() + "  retainNames " +  retainNames.size() + "  recovotefail " + recoVoteList.size()
//	splitTreeExport(retainNames, false)
//	new File("/tmp/delete_raw_reco.csv").withWriter { out ->
//		out.println "id|name"
//		recoVoteList.each { tId ->
//			out.println tId + "|" + TaxonomyDefinition.read(tId).name
//		}
//	}
	
	String ss = "update recommendation set taxon_concept_id = NULL where taxon_concept_id = "
	recoVoteList.each { tId ->
		sql.executeUpdate(ss + tId)
		delTableList.each { String s ->
			sql.executeUpdate(s + " " + tId)
		}
	}
	
	
	println "------------------------------------------"
	println "============= done "
}

def rawNamesWithNoIbpHir(){
	def ss = ''' select id from taxonomy_definition where id not in (select distinct(taxon_definition_id) from taxonomy_registry where classification_id = 265799 and taxon_definition_id in (select id from taxonomy_definition where status = 'ACCEPTED' and position = 'RAW')) and status = 'ACCEPTED' and position = 'RAW'  order by rank, name '''
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	splitTreeExport(sql.rows(ss), false)
		
}


def deleteName(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(500);
	def sql = new Sql(dataSource)
	
	
	String s1 = "delete from taxonomy_definition_suser where taxonomy_definition_contributors_id = "
	String s2 = "delete from accepted_synonym where accepted_id = "
	String s3 = "delete from taxonomy_definition_year where taxonomy_definition_id = "
	String s4 = "delete from taxonomy_definition_author where taxonomy_definition_id = "
	String s5 = "delete from doc_sci_name where taxon_concept_id = "
	String s6 = "delete from recommendation where taxon_concept_id = ";
	String s7 = "delete from common_names where taxon_concept_id = "
	String s8 = "delete from synonyms where taxon_concept_id = "
	String s9 = "delete from taxonomy_definition where id = "
	
	List delTableList = [s1, s2, s3, s4, s5, s6, s7, s8, s9]
	String ss = "update recommendation set taxon_concept_id = NULL where taxon_concept_id = "
	
	File file = new File("/apps/git/biodiv/namelist/after-migration/raw_names_without_ibpHir_todelete.csv");
	//File file = new File("/home/sandeept/namesync/thomas/raw_names_without_ibpHir_todelete.csv")
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		def tId = arr[0]//Long.parseLong(arr[0].trim())
		sql.executeUpdate(ss + tId)
		delTableList.each { String s ->
			sql.executeUpdate(s + " " + tId)
		}
	}
}


def updateNameAndCreateIbpHir(){
	def nlSer = ctx.getBean("namelistUtilService");
	def ibpHir = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
	File file = new File("/apps/git/biodiv/namelist/after-migration/raw_names_without_ibpHir_tocorrectandsnap.csv");
	//File file = new File("/home/sandeept/namesync/thomas/raw_names_without_ibpHir_tocorrectandsnap.csv")
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		def tId = Long.parseLong(arr[0].trim())
		String correctName = arr[1].trim()
		def pId = Long.parseLong(arr[3].trim())
		TaxonomyDefinition td = TaxonomyDefinition.get(tId)
		
		NamesParser namesParser = new NamesParser();
		def parsedNames = namesParser.parse([correctName]);
		if(parsedNames[0]?.canonicalForm) {
			td.normalizedForm = parsedNames[0].normalizedForm;
			td.italicisedForm = parsedNames[0].italicisedForm;
			td.binomialForm = parsedNames[0].binomialForm;
			td.canonicalForm = parsedNames[0].canonicalForm
			td.name = correctName
			if(!td.save(flush:true)) {
				td.errors.each { println it }
			}
		}
	
		nlSer.saveIBPHir(td, pId)
	}
}


def createNameAndAddIBPHir(){
	def nlSer = ctx.getBean("namelistUtilService");
	File file = new File("/apps/git/biodiv/namelist/after-migration/raw_names_without_ibpHir_tocreateandsnap.csv");
	//File file = new File("/home/sandeept/namesync/thomas/raw_names_without_ibpHir_tocreateandsnap.csv")
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		def tId = Long.parseLong(arr[0].trim())
		String correctName = arr[1].trim()
		String parentName = arr[2].trim()
		def pTd = nlSer.createName(parentName, arr[5].trim(), arr[6].trim(), arr[4].trim())
		if(!pTd){
			println "Not able to create parent " + parentName
			return
		}
		def tr = nlSer.saveIBPHir(pTd, Long.parseLong(arr[8].trim()))
		if(!tr){
			println "Not able to create tr for " + pTd
			return 
		}
		
		NamesParser namesParser = new NamesParser();
		def parsedNames = namesParser.parse([correctName]);
		if(parsedNames[0]?.canonicalForm) {
			TaxonomyDefinition td = TaxonomyDefinition.get(tId)
			td.normalizedForm = parsedNames[0].normalizedForm;
			td.italicisedForm = parsedNames[0].italicisedForm;
			td.binomialForm = parsedNames[0].binomialForm;
			td.canonicalForm = parsedNames[0].canonicalForm
			td.name = correctName
			if(!td.save(flush:true)) {
				td.errors.each { println it }
			}
			nlSer.saveIBPHir(td, pTd.id)
		}
	}	
}

def migSyn(){
	def m = [:]
	//File file = new File("/home/sandeept/namesync/thomas/synonyms_to_reuse.csv");
	File file = new File("/apps/git/biodiv/namelist/after-migration/synonyms_to_reuse.csv");
	
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split(',');
		println " arr ->>> " + arr
		m.put(Long.parseLong(arr[0].trim()), Long.parseLong(arr[1].trim()))
	}
	println " m ------------- " + m
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.migrateSynonymForRawNames(m)
}


def test(){
	List hirList = [ Classification.findByName('Catalogue of Life Taxonomy Hierarchy'), Classification.findByName('IUCN Taxonomy Hierarchy (2010)'), Classification.findByName("Author Contributed Taxonomy Hierarchy"), Classification.findByName("FishBase Taxonomy Hierarchy"), Classification.findByName("GBIF Taxonomy Hierarchy")]
	def trHir = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
	TaxonomyDefinition.get(421973).snapToIBPHir(hirList, trHir)
}


def multipleIbp(){
	def nlSer = ctx.getBean("namelistUtilService");
	nlSer.generateSheetMultipleIBPNameList(new File("/tmp/multipleibp.csv"))
}


def deleteHir(){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(50000);
	def sql = new Sql(dataSource)
	
	String ss = " delete from taxonomy_registry where classification_id = 265799 and path = "
	String ss1 = " delete from taxonomy_registry where classification_id = 265799 and path like  " 
	
	
	File file = new File("/tmp/multipleibp.csv");
	def lines = file.readLines();
	int i=0;
	lines.each { line ->
		if(i++ == 0) return;
		def arr = line.split('\\|');
		//println " arr ->>> " + arr
		boolean isDelete =  "delete".equals(arr[3]?.trim()?.toLowerCase())
		//println "isDelete ==== " + isDelete
		String pathPrefixEq = "'" +  arr[4].trim() + "'"
		String pathPrefixLike = "'" +  arr[4].trim() + "\\_%'"
 		if(isDelete){
			 String q = ss + pathPrefixEq
			 println q
			 int a = sql.executeUpdate(q)
			 println "================= delete " + a
			 q = ss1 + pathPrefixLike
			 println q
			 a = sql.executeUpdate(q)
			 println "================= delete " + a
		}
	}
}


def moveCleanNameSpeciesToGroup(groupId){
	def dataSource = ctx.getBean("dataSource");
	dataSource.setUnreturnedConnectionTimeout(50000);
	def sql = new Sql(dataSource)
	
	String q = "select species_id as id from taxonomy_definition where (rank = 9 or rank = 10) and status = 'ACCEPTED' and position = 'CLEAN' and species_id is not null;"
	
	sql.rows(q).each{
		def speciesId = it.id
		String s = "insert into user_group_species values (" + speciesId + ", " + groupId + ");"
		println s
		try{
			sql.execute(s);
		}catch(e){
			println e.message
		}
	}
	println "== done"
	
}

//dmp()
//splitTreeExport()

//dropRawHir()
//addColhir()
//createDuplicateName()
//mergeAcceptedName()
//mergeSynonym()
//updateColId()

//createInputFile()

//updateColId()
//mergeAcceptedName()
//mergeSynonym()
//createDuplicateName()
//removeIsDeletedRawName()
//addIBPHirToRawNames()
//copyIbpHir()

//rawNamesWithNoIbpHir()

//deleteName()
//updateNameAndCreateIbpHir()
//createNameAndAddIBPHir()
//migSyn()

//test()

//multipleIbp()
//deleteHir()
//moveCleanNameSpeciesToGroup(48)



