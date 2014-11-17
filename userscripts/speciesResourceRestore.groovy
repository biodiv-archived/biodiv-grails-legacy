import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.Species
import species.Resource


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
private List getAllResourceFromFolder(String sName){
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
					//println sName + " , " + f
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
	
	int BATCH_SIZE = 2000
	long offset = 0
	while(true){
		//		Species.withTransaction{
		def sList = Species.list(max: BATCH_SIZE, offset: offset, sort: "id", order: "asc")
		if(sList.isEmpty())
			break

		sList.each { Species s ->
			List resList = getAllResourceFromFolder(s.taxonConcept.canonicalForm)
			List sfResoruces = []
			s.fields.each { sf ->
				sf.resources.each {
					sfResoruces << it
				}
				
			}
			s.resources.each {sfResoruces << it }
			
			List resToAdded = resList.minus(sfResoruces)
			
			if(!resToAdded.isEmpty()){
				println "Adding resources " + resToAdded + "  in to species " + s
				resToAdded.each { s.addToResources(it) }
				if(!s.save()){
					s.errors.allErrors.each { println  it }
				}
			}
		}
		cleanUpGorm(true)
		offset += BATCH_SIZE
		println "Done for " + offset
		//		}
	}

	println "Finished ------------------------- start date " + startDate + "  end date " + new Date() 
}

restoreSpeciesResources()