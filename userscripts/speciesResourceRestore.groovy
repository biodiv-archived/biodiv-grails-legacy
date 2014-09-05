import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import species.*

def rootDir = ConfigurationHolder.config.speciesPortal.resources.rootDir


def List getAllResourceFromFolder(String sName){
	File sDir = new File(rootDir, sName)
	if(!sDir.exists()){
		return []
	}
	def resList = []
	sDir.listFiles.toList()*.name.each { f ->
		def s = f.split(".")[-2].toString().toLowerCase()
		if(!s.endsWith("_gall") && !s.endsWith("_gall_th") && !s.endsWith("_th1")){
			def res = Resource.findByFileName("/" + sName + "/" + f)
			if(res){
				resList << res
			}
		}
	}
	println resList
	return resList
}

def restoreSpeciesResources(){
	int BATCH_SIZE = 100
	long offset = 0
	while(true){
		Species.withTransaction{
			Species.list(max: BATCH_SIZE, offset: offset, sort: "id", order: "asc").each { Species s ->
				List resList = getAllResourceFromFolder(s.taxonConcept.canonicalForm)
//				List resToAdded = resList.minus(s.resources.collect { it})
//				
//				println "Adding resources " + resToAdded + "  in to species " + s
//				resToAdded.each {
//					s.addToResources(it)
//				}
//				if(!s.save(flush:true)){
//					s.errors.allErrors.each { println  it }
				}
			}
			offset += offset
			println "Done for " + offset
		}
	}
}