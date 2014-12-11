import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Species
import species.Resource
import groovy.sql.Sql;

import species.participation.*

private void cleanUpGorm(boolean clearSession) {
	def sessionFactory = ctx.getBean("sessionFactory");
	def hibSession = sessionFactory?.getCurrentSession();

	if(hibSession) {
		//log.debug "Flushing and clearing session"
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
private List getAllResourceFromFolder(String sName, id, l){
	def rootDir = ConfigurationHolder.config.speciesPortal.resources.rootDir
	File sDir = new File(rootDir, sName)
	File sDir1 = new File(rootDir, sName.replaceAll(" ", "_"))
	sDir = sDir.exists() ? sDir : sDir1
	if(!sDir.exists()){
		//println "Dir not exsist " + sDir.getAbsolutePath()
		return []
	}
	def resList = []
	//println "Starting for  " + sDir.getAbsolutePath()
	sDir.listFiles().toList()*.name.each { f ->
		try{
			def s = f.split("\\.")[-2].toString().toLowerCase()
			if(!s.endsWith("_o") && !s.endsWith("_gall") && !s.endsWith("_th") && !s.endsWith("_th1")){
				//println "file name "+ f
				def res = Resource.findByFileName("/" + sDir.getName() + "/" + f)
				if(res){
					resList << res
				}else{
						//println "" + id + ", " + sName + ", " + f + ", " + sDir.getAbsolutePath()
						l << new File(sDir, f)
						
				}
			}
		}catch(e){
			//println sName + " , " + f +	", Exception"
		}
	}
	//println "resource List " + resList
	return resList
}

def restoreSpeciesResources(){
	def startDate = new Date()
	boolean run = true
	int BATCH_SIZE = 2000
	long offset = 0
	while(run){
	Species.withNewTransaction([readOnly:true]){//		Species.withTransaction{
		def sList = Species.list(max: BATCH_SIZE, offset: offset, sort: "id", order: "asc")
		if(sList.isEmpty())	
			run = false

		sList.each { Species s ->
			List resList = getAllResourceFromFolder(s.taxonConcept.canonicalForm, s.id)
			
//			List sfResoruces = []
//			s.fields.each { sf ->
//				sf.resources.each {
//					sfResoruces << it
//				}
//				
//			}
//			s.resources.each {sfResoruces << it }
//			
//			List resToAdded = resList.minus(sfResoruces)
//			
//			if(!resToAdded.isEmpty()){
//				println "Adding resources " + resToAdded + "  in to species " + s
//				resToAdded.each { s.addToResources(it) }
//				if(!s.save()){
//					s.errors.allErrors.each { println  it }
//				}
//			}
		}
	}
//		cleanUpGorm(true)
		offset += BATCH_SIZE
		//println "Done for " + offset
		//		}
	}

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
		if(del && f.exists()){
			f.deleteOnExit()
		}
	}
}


def restoreObvContributor(){	
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
		
		def contributor = Contributor.findByName(obv.author.);
		if(!contributor && createNew && contributorName != null) {
			contributor = new Contributor(name:contributorName);
			if(!contributor.save(flush:true)) {
				contributor.errors.each { log.error it }
			}
		}
		
	}
	
}


deleteRedundantFile(true)

//restoreSpeciesResources()