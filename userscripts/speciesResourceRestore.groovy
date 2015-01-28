import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.*
import species.Resource
import groovy.sql.Sql;

import species.participation.*
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import species.auth.*

private void cleanUpGorm(boolean clearSession) {
	def sessionFactory = ctx.getBean("sessionFactory");
	def hibSession = sessionFactory?.getCurrentSession();

	if(hibSession) {
		println "Flushing and clearing session"
		try {
			hibSession.flush()
		} catch(Exception e) {
			e.printStackTrace()
		}
		if(clearSession){
			hibSession.clear()
		}
	}
}
private List getAllResourceFromFolder(String sName, id, l=[]){
	def rootDir = ConfigurationHolder.config.speciesPortal.resources.rootDir
	File sDir = new File(rootDir, sName)
	File sDir1 = new File(rootDir, sName.replaceAll(" ", "_"))
	sDir = sDir.exists() ? sDir : sDir1
	if(!sDir.exists()){
		return []
	}
	def resList = []
	sDir.listFiles().toList()*.name.each { f ->
		try{
			def s = f.split("\\.")[-2].toString().toLowerCase()
			if(!s.endsWith("_o") && !s.endsWith("_gall") && !s.endsWith("_th") && !s.endsWith("_th1")){
				def res = Resource.findByFileName("/" + sDir.getName() + "/" + f)
				if(res){
					resList << res.id
				}else{
						l << new File(sDir, f)
				}
			}
		}catch(e){
			//println sName + " , " + f +	", Exception"
		}
	}
	return resList
}

def writeJson(obj, String path){
	File f = new File(path)
	if(f.exists()){
		f.delete()
	}
	
	f.createNewFile()
	f.write(JsonOutput.toJson(obj))
}


def readJson(String path){
	return new JsonSlurper().parse(new FileReader(new File(path)))
}


def _restoreSR(String path){
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	Map finalMap = readJson(path)
	
	println "=============================== final map "
	println finalMap.size()
	println "================================== content "
	println finalMap
	println "=========================================="
	
	int count = 0
	finalMap.each { sid, newResources ->
		count++
		
		if(count%100 == 0){
			println " processed  " + count
		}
		
		newResources.each { rid ->
			sql.executeUpdate('insert into species_resource values(?, ?)', [sid.toLong(), rid.toLong()]);
			sql.executeUpdate(''' update resource set  context = 'SPECIES' where id = ? ''', [rid.toLong()]);
		}
	}
}

def createRawMap(String path){
	def sContext =  Resource.ResourceContext.toList()[1]
	boolean run = true
	int BATCH_SIZE = 2000
	long offset = 0
	def rMap = [:]
	while(run){
		Species.withTransaction([readOnly:true]){
			def sList = Species.list(max: BATCH_SIZE, offset: offset, sort: "id", order: "asc")
			if(sList.isEmpty())
				run = false

			sList.each { Species s ->
				List resList = getAllResourceFromFolder(s.taxonConcept.canonicalForm, s.id)
				if(!resList.isEmpty())
					rMap[s.id] = resList
			}
			offset += BATCH_SIZE
			println "Done for " + offset
		}
	}
	cleanUpGorm(true)
	writeJson(rMap, path)
}

def createFinalMap(String rawPath, finalPath){
	def rMap = readJson(rawPath)
	Map finalMap = [:]
	int count = 0
	rMap.each { sId, resList ->
		count++
		if(count%5000 == 0){
			println "========== count " + count
			cleanUpGorm(true)
		}
		if(count%100 == 0){
			println " processed " + count
		}
		def s = Species.get(sId)
		List sfResoruces = []
		s.fields.each { sf ->
			sf.resources.each {
				sfResoruces << it.id
			}
		}
		s.resources.each {sfResoruces << it.id }
		
		List resToAdded = resList.minus(sfResoruces)
		
		if(!resToAdded.isEmpty()){
			println "Adding resources " + resToAdded + "  in to species " + s
			finalMap[s.id] = resToAdded
		}
	}
	cleanUpGorm(true)
	writeJson(finalMap, finalPath)

}

def restoreSpeciesResources(){
	def startDate = new Date()
	String rawPath = "/tmp/slink_raw.txt"
	createRawMap(rawPath)
	String finalPath = "/tmp/slink_final.txt"
	createFinalMap(rawPath, finalPath)
	println "Finished ------------------------- start date " + startDate + "  end date " + new Date()
}



def deleteRedundantFile(boolean del = false){
	def startDate = new Date()
	boolean run = true
	int BATCH_SIZE = 2000
	long offset = 0
	def deleteFileList = []
	while(run){
		Species.withNewTransaction([readOnly:true]){
			def sList = Species.list(max: BATCH_SIZE, offset: offset, sort: "id", order: "asc")
			if(sList.isEmpty())
				run = false
	
			sList.each { Species s ->
				List resList = getAllResourceFromFolder(s.taxonConcept.canonicalForm, s.id, deleteFileList)
			}
	
			offset += BATCH_SIZE
		}
	}
	
	println "================== delete file list size " + deleteFileList.size()
	//deleting given file
	deleteFileList.each { File f ->
		println "Deleting file == " + f.getAbsolutePath()
		if(del && f.exists()){
			f.deleteOnExit()
		}
	}
}


def restoreObvContributor(){	
	def s = new Date()
	def dataSource = ctx.getBean("dataSource");
	def query = '''select r.id as id from resource r left outer join resource_contributor rc on r.id=rc.resource_contributors_id where rc.resource_attributors_id is null and rc.resource_contributors_id is null and rc.contributor_id is null and r.context = 'OBSERVATION' '''
	def conn = Sql.newInstance(dataSource)
	conn.rows(query).each { r ->
		def res = Resource.get(r.id)
		def obv = Observation.withCriteria(){
			and{
				resource{
					eq('id', r.id)
				}
			}
		}
		if(obv){
			obv = obv[0]
			def contributor = Contributor.findByName(obv.author.username);
			if(!contributor) {
				println "Saving contributor for user " + obv.author
				contributor = new Contributor(name:obv.author.username, user:obv.author);
				if(!contributor.save(flush:true)) {
					contributor.errors.each { println  it }
				}
			}
			println "adding contributor for res " + res + " contr " + contributor
			res.addToContributors(contributor)
			if(!res.save(flush:true)) {
				res.errors.each { println  it }
			}
		}else{
			println "obv not found for resource  " + res
		}
	}
	
	println "  start date " + s + "  end date  " + new Date()
	
}


def restoreUserContributor(){
	def s = new Date()
	def dataSource = ctx.getBean("dataSource");
	def query = '''select r.id as id from resource r left outer join resource_contributor rc on r.id=rc.resource_contributors_id where rc.resource_attributors_id is null and rc.resource_contributors_id is null and rc.contributor_id is null and r.context = 'USER' '''
	def conn = Sql.newInstance(dataSource)
	conn.rows(query).each { r ->
		def res = Resource.get(r.id)
		def contributor = Contributor.findByName(res.uploader.username);
		if(!contributor) {
			println "Saving contributor for user " + res.uploader
			contributor = new Contributor(name:res.uploader.username, user:res.uploader);
			if(!contributor.save(flush:true)) {
				contributor.errors.each { println  it }
			}
		}
		println "adding contributor for res " + res + " contr " + contributor
		res.addToContributors(contributor)
		if(!res.save(flush:true)) {
			res.errors.each { println  it }
		}
		
	}
	
	println "  start date " + s + "  end date  " + new Date()
	
}


def restoreLic(){
	def query = ''' select r.id as id from resource r left outer join resource_license rc on r.id=rc.resource_licenses_id where rc.resource_licenses_id is null'''
	def dataSource = ctx.getBean("dataSource");
	def conn = Sql.newInstance(dataSource)
	conn.rows(query).each { r ->
		def res = Resource.get(r.id)
		res.addToLicenses(License.read(822))
		if(!res.save(flush:true)) {
			res.errors.each { println  it }
		}
	}
}


def restoreSpContributor(String path){
	def m = [:]
	def ds = ctx.getBean("dataSource")
	def sql =  Sql.newInstance(ds);
	Contributor.withTransaction {
		new File(path).splitEachLine("\\|") {
			def fields = it;
//			println fields
//			if(!fields.isEmpty()){
				def resId = fields[0].trim().toLong()
				def cRaw = fields[1].trim()
				def c = Contributor.findByNameIlike(cRaw)
				if(!c){
					SUser  s = SUser.findByUsernameIlike(cRaw)
					c = new Contributor(name:cRaw, user:s)
					println "saving contributor  " + cRaw + "   user " + s
					if(!c.save(flush:true)) {
						c.errors.each { println  it }
					}
				}
				m[resId] = c.id
//			}
		}	
	}
	
	int count = 0
	m.each { r, c ->
		count++
		if(count%100 == 0){
			println " processed  " + count
		}
		sql.executeUpdate('insert into resource_contributor values(null, ?, ?)', [c, r]);
	}
}


restoreLic()
restoreObvContributor()
restoreUserContributor()
restoreSpContributor("/tmp/mcont.csv")
//restoreSpeciesResources()
//_restoreSR("/tmp/slink_final.txt")
//deleteRedundantFile(true)
