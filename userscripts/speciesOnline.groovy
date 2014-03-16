import species.*;
import species.auth.SUser;
import species.Contributor;
import species.formatReader.*;
import groovy.sql.Sql

def addNewField(){
	def f = new Field(concept:'Information Listing',category:'Images', description:'Place holder for images', displayOrder:83)
	f.save(flush:true)
}

def addMetadataField(){
	def f = new Field(concept:'Meta data', category:'Meta data', description:'Place holder for marking meta data', displayOrder:84)
	f.save(flush:true)
}


def getContNotPresent(){
    def allCont = []
    def contList = Contributor.list()
    contList.each { cl->
        allCont.add(cl.name)
    }
    
    println "==ALL CONT ======= " + allCont

    def suList = SUser.findAllByUsernameInList(allCont)
    def contPresent = []
    suList.each{ su-> 
        contPresent.add(su.username)
    }
    println "======CONT PRESENT ==== " + contPresent
    def contNotPresent = allCont - contPresent

    println "======CONT NOT PRESNET ====== " + contNotPresent
    File file = new File("/home/rahulk/Documents/new.tsv")
    contNotPresent.each {
        file << ("${it}\n")
    } 
}

def makeFieldGeneric(){
	def fList = Field.findAllBySubCategory('Indian Distribution Geographic Entity')
	Field.withTransaction { 
		fList.each {
			println 'changing for field ' + it 
			it.subCategory = 'Local Distribution Geographic Entity'
			it.save(flush:true)
		}
	}
}


def mySave(obj){
	if(!obj.save(flush:true)){
		obj.errors.allErrors.each { println it }
	}
}


//1. create user

//2. map user in contributor
def populateUserInContributor(){
	Contributor.withTransaction { 
		Contributor.list(sort:'id', order:'asc').each { cont ->
			def u = SUser.findByUsername(cont.name)
			if(u){
				println " saving user for contributor  " + cont
				cont.user = u
				mySave(cont)
			}
		}
	}
	//populateUserInContributor1()
}

def populateUserInContributor1(){
	def m = [:]
	def dMap = [:]
	Contributor.withTransaction {
		new File("/home/sandeept/git/biodiv/contributormap.csv").splitEachLine(",") {
			println "data   " + it
			def fields = it;
			def contId = fields[0].trim().toLong()
			def userIds = fields[1].trim().split("\\|").collect{it.trim().toLong()}
			m.put(contId, userIds)
		
			def cont = 	Contributor.get(contId)
			if(userIds.size() == 1){
				cont.user = SUser.read(userIds[0])
				println "adding single user " + cont.user + "   list " + userIds
				mySave(cont)
			}else{
				dMap.put(contId, userIds)
				userIds.each { userId ->
					def user = SUser.read(userId)
					if(!Contributor.findByUser(user)){
						def nCont = new Contributor(name:user.name, user:user)
						println "createing new contributor for user  " + cont.user + "   list " + userIds
						mySave(nCont)
					}
				}
			}
		}
		
	}
	//println dMap
	//populateSfieldContributor(dMap)
	
}
 

//3. populate sField to contributor table
def populateSfieldContributor(){
	def dMap = [:]
	new File("/home/sandeept/git/biodiv/contributormap.csv").splitEachLine(",") {
		println "data   " + it
		def fields = it;
		def contId = fields[0].trim().toLong()
		def userIds = fields[1].trim().split("\\|").collect{it.trim().toLong()}
		if(userIds.size() > 1){
			dMap.put(contId, userIds)
		}
	}
	println dMap
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	
	
	dMap.each { k, v ->
		int i = 0
		sql.eachRow("select sfc.species_field_contributors_id as sfid from species_field_contributor sfc where sfc.contributor_id = ? and sfc.species_field_contributors_id is not null order by species_field_contributors_id", [k]) { sfc ->
			for(uid in v){
				sql.executeUpdate('insert into species_field_suser values(?, ?, ?)', [sfc.sfid, uid, i++])
		 	}
		}
	}
	
	
	def query = "select sfc.species_field_contributors_id as sfid, c.user_id as uid from species_field_contributor sfc, contributor c where c.id = sfc.contributor_id and c.user_id is not null and species_field_contributors_id is not null order by species_field_contributors_id"
	int j = 0
	sql.rows(query).each{
		sql.executeUpdate( 'insert into species_field_suser values(?, ?, ?)',  [it.sfid, it.uid, j++])
	 }
}

//4. delete redudant rows
//delete from species_field_contributor where species_field_contributors_id is not null; { later...

//5 change code in species upload + resource crate + checklist attribution { by sra

//6 change code in species show done



//---------------------------

def updateObservationResource(){
	int i = 0
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	def query = "select o.author_id as aid, o.created_on as cdate, r.resource_id as rid from observation o, observation_resource r where o.id = r.observation_id"
	sql.rows(query).each{
		println " updating resource " + it.rid + " count " + i++ 
		sql.executeUpdate( 'UPDATE resource set uploader_id = ?, upload_time = ?  where id = ?',  [it.aid, it.cdate, it.rid])
	 }
}



def updateNameContributor(){
	int i = 0
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	['common_names', 'synonyms', 'taxonomy_definition', 'taxonomy_registry'].each { t -> 
		def query = "select id as id from " + t + " order by id "
		sql.rows(query).each{
			println " adding contri " + it.id + " count " + i++
			sql.executeUpdate( 'insert into ' + t + '_suser values(?, 1)',  [it.id])
		}
	}
}


//makeFieldGeneric()
//addMetadataField()
//addNewField()

//populateUserInContributor()
//populateUserInContributor1()
//populateSfieldContributor()


/*
update species_field_suser set contributors_idx = 0;
delete from species_field_contributor where species_field_contributors_id is not null;
alter table species_field_contributor drop column species_field_contributors_id;
drop table species_taxonomy_registry ;
*/


//adding soure info
//updateObservationResource()
//update species_field set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
//update resource set uploader_id = 1, upload_time = '1970-01-01 00:00:00' where uploader_id is null;



//adding namessourceinfo {have to add contributors}
//update synonyms set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
//update taxonomy_definition set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
//update common_names set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
//update taxonomy_registry set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
//updateNameContributor()


//speciesbulkupload drop constrain
//ALTER TABLE  species_bulk_upload  ALTER COLUMN file_path drop not NULL;
//ALTER TABLE  species_bulk_upload  ALTER COLUMN end_date drop not NULL;
