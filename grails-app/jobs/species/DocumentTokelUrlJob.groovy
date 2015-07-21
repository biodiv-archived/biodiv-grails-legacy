package species

import java.util.logging.Logger;

import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import speciespage.ObvUtilService;
import content.eml.DocumentTokenUrl;
import content.eml.DocumentService;
import content.eml.DocSciName;

class DocumentTokelUrlJob {
    static triggers = {
      simple repeatInterval: 1000l // execute job once in 5 seconds
    }
    def documentService;

    def execute() {
        // execute job
        List scheduledTaskList = getDocumentTokelUrl()
        if(!scheduledTaskList){
			return
		}
		scheduledTaskList.each { DocumentTokenUrl tu ->
			try{
				log.debug "starting task $tu"
                println "===========PROCESSING ============== " + tu
                Map gnrdNames = documentService.getGnrdScientificNames(tu.tokenUrl);
                setStatus(tu,gnrdNames.status)
                List docSciNameId = DocSciName.findAllByDocument(tu.doc)
                docSciNameId.each { 
                	it.delete();
                }
                docSciNameId.clear();
                Map offsetReturnedValues = gnrdNames.offsetMap
                Map parsedNameSetValues = gnrdNames.parsedNameSetMap
                int mapSize = gnrdNames.names.size()
                //println "mapsize------- "+ mapSize
                gnrdNames.names.each { sciName, freq ->
                	def docSciNameInstance = new DocSciName()
                	docSciNameInstance.document = tu.doc
                	docSciNameInstance.scientificName = sciName
                	docSciNameInstance.frequency = freq
                	def stringOffsets = offsetReturnedValues[sciName].join(",")
                	docSciNameInstance.offsetValues = stringOffsets
                    docSciNameInstance.canonicalForm = parsedNameSetValues[sciName]
                    docSciNameInstance.displayOrder = mapSize
                    mapSize--;
                    if(docSciNameInstance.canonicalForm) {
                        docSciNameInstance.taxonConcept = TaxonomyDefinition.findByCanonicalForm(docSciNameInstance.canonicalForm);
                    }
                   // println "mapsize----in loop--- "+ mapSize
                	if (!docSciNameInstance.save(flush: true)) {
   					    docSciNameInstance.errors.each {
     					  // println "=======it========"+it
   					    }
					}
                }
                

                }catch (Exception e) {
				log.debug " Error while running task $tu"
				e.printStackTrace()
				setStatus(tu, ObvUtilService.FAILED)
			}
		}
    }

    private setStatus(task, status){
		task.status = status
        task.merge();
		if(!task.save(flush:true)){
			task.errors.allErrors.each { log.debug it }
		}
	}

	private synchronized getDocumentTokelUrl(){
		List scheduledTaskList = DocumentTokenUrl.findAllByStatus(ObvUtilService.SCHEDULED, [sort: "createdOn", order: "asc", max:5])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { DocumentTokenUrl tu ->
			setStatus(tu, ObvUtilService.EXECUTING)
		}
		
		return scheduledTaskList
	}

}
