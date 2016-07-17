import species.participation.*;
import species.auth.*;
import species.*;
import groovy.sql.Sql

import grails.util.Holders
import species.namelist.Utils


def getHir(tr){
	def res = []
	//println tr.path
	tr.path.trim().tokenize("_").each { id ->
		td = TaxonomyDefinition.get(id.toLong())
		res.add( td.name + " : " + td.rank )
	}
	return res.join(">")
}

def bbpNames(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(1000);

	def header = ['id', 'name', 'canonicalForm', 'rank',  'col-match-id'  ,'Hierarchy', 'hirName']

	def rowList = []
	rowList << header
	
	int i = 0
	TaxonomyDefinition.list(sort:"rank", order:"asc").each { td ->
		if(td.id > 10184)
			return

		println "Writing " + td + "   count " + (i++)

		TaxonomyRegistry.findAllByTaxonDefinition(td).each { tr ->
			def tRow = [td.id, td.name, td.canonicalForm, td.rank, td.matchId, getHir(tr),  tr.classification.name ]
			rowList << tRow
		}

	}

	new File("/tmp/names_1.csv").withWriter { out ->
		rowList.each {
		  out.println it.join("|")
		}
	}


}


def removeBBPHirLevel(){
	def deleteId = [10185, 6926, 10187, 10188]

	def repMap = [10185:6978, 6926:8008, 10187:16808, 10188:16809]

	def repMap1 = [10185:16807, 6926:10441, 10187:16809, 10188:16810]



	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(10500);
	Sql conn = new Sql(ds);
	conn.executeUpdate("ALTER TABLE taxonomy_registry DROP CONSTRAINT if exists taxonomy_registry_path_classification_id_taxon_definition_i_key");
	int count = 0
	deleteId.each { dId ->
		
		def query = " select id as id, path as path from taxonomy_registry where path like '%\\_" + dId + "\\_%';"
		
		def idList = conn.rows(query);
		idList.each { tid ->
			def trId = tid.id
			def trPath = tid.path
			trPath = trPath.replace("_" + dId + "_", "_")
			conn.executeUpdate("update taxonomy_registry set path = '" + trPath + "' where id = " + trId )
			count++
			if(count%10 == 0)
				println "----------------- count " + count
		
		}
		//conn.executeUpdate("update taxonomy_registry set parent_taxon_id = " + repMap.get(dId) + " where parent_taxon_id = " +  repMap1.get(dId) )

		
	}

	conn.executeUpdate("update taxonomy_registry set parent_taxon_id = 6978 where parent_taxon_id= 16807")
	conn.executeUpdate("update taxonomy_registry set parent_taxon_id = 8008 where parent_taxon_id= 10441")
	conn.executeUpdate("update taxonomy_registry set parent_taxon_id = 16809 where parent_taxon_id= 16810")
	conn.executeUpdate("update taxonomy_registry set parent_taxon_id = 16808 where parent_taxon_id= 16809")
	//deleting all taxonred at wrong rank
	conn.executeUpdate("delete from taxonomy_registry where taxon_definition_id in (10185, 6926, 10187, 10188)")
	conn.executeUpdate("update recommendation set taxon_concept_id = null where taxon_concept_id in (10185, 6926, 10187, 10188)")
	conn.executeUpdate("delete from taxonomy_definition where id in (10185, 6926, 10187, 10188)")

}

//removeBBPHirLevel()


def addIBPTaxonHie() {
	println "====ADDING IBP TAXON HIERARCHY======"
	def cl = new Classification();
	cl.name = "BBP Taxonomy Hierarchy";
	cl.language = Language.read(1L);
	if(!cl.save(flush:true)) {
		cl.errors.allErrors.each { println it }
	}
	println "====DONE======"
}


def downloadXML(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(105000);
	def config = Holders.config
	String path = config.speciesPortal.namelist.rootDir + "/../"
	Utils.downloadColXml(path)
}



def testCurate(id){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(105000);
	def nSer = ctx.getBean("namelistService");
	def config = Holders.config
	String path = config.speciesPortal.namelist.rootDir  
	nSer.curateName(TaxonomyDefinition.read(id), new File(path))
}

def curateAllAcceptedNames(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(105000);
	def nSer = ctx.getBean("namelistService");
	def config = Holders.config
	String path = config.speciesPortal.namelist.rootDir + "/../"
	nSer.populateInfoFromCol(new File(path), 10184L)
}


def addBBPHirToName(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(105000);

	long totalCount = 10184L
	long offset = 0
        while(true){
            List tds = TaxonomyDefinition.createCriteria().list(max:100, offset:offset) {
				and {
					le("id", totalCount)
					order("rank", "asc")	
					order("id", "asc")	
				}
				
            }
            if(tds.isEmpty()){
                break
            }
            offset += 100
            tds.each { td ->
            	try{
            		td.doColCuration = false
            		td.postProcess()
            	}catch(e){
            		e.printStackTrace()
            	}
            }
        }

}

def synRecos(){
	def nls = ctx.getBean("namesLoaderService");
	nls.syncNamesAndRecos(false, false)
}


def synNames(){
	def nls = ctx.getBean("namesIndexerService");
	nls.rebuild()
}

//bbpNames()
//testCurate(997L)
//addIBPTaxonHie()
//downloadXML()
//curateAllAcceptedNames()
//addBBPHirToName()
//synRecos()
synNames()