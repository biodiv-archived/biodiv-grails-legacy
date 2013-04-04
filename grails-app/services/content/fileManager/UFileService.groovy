package content.fileManager

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
class UFileService {

    static transactional = true

    def updateUFiles(params) {
		log.info "Updating UFiles from params: "+ params
		
		def uFiles = []
		def filesList = (params.files != null) ? Arrays.asList(params.files) : new ArrayList()		
		for(fileId in filesList) {
			def uFileInstance = UFile.get(fileId)
			if(params."${fileId}.name") {
				uFileInstance.name = params."${fileId}.name"
			}
			
			if(params."${fileId}.description") {
				uFileInstance.description = params."${fileId}.description"
			}
			
			if(params."${fileId}.contributors") {
				uFileInstance.contributors = params."${fileId}.contributors"
			}
			
			if(params."${fileId}.attribution") {
				uFileInstance.attribution = params."${fileId}.attribution"
			}
			
			if(params."${fileId}.license") {
				uFileInstance.license = params."${fileId}.license"
			}
			
			if(params."${fileId}.tags") {
				def tags = (params."${fileId}.tags" != null) ? Arrays.asList(params."${fileId}.tags") : new ArrayList();				
				uFileInstance.setTags(tags);
				
			}
			
			if(params."sourceHolderId") {
				uFileInstance.sourceHolderId = params.sourceHolderId
			}
			
			if(params."sourceHolderType") {
				uFileInstance.sourceHolderType = params.sourceHolderType
			}
 			
			
			if (uFileInstance.save(flush: true)) {
				//flash.message = "${message(code: 'default.created.message', args: [message(code: 'UFile.label', default: 'UFile'), uFileInstance.id])}"
				log.info "ufile saved" + uFileInstance.dump()
			}
			else {
				flash.message = "${message(code: 'error')}";
				uFileInstance.errors.allErrors.each { log.error it }
				def errorMsg = "Errors in saving files"
				throw new GrailsTagException(errorMsg)
				
			}
			uFiles.add(uFileInstance)
		}

		return uFiles				 				
    }
	
	
	public static String getFileSize(File file) {
		return FileUtils.byteCountToDisplaySize(file.length());
	}
	
	
}
