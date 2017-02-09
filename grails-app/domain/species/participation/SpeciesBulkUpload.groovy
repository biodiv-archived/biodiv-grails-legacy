package species.participation

import java.util.Date;
import grails.util.Holders;
import grails.converters.JSON;
import species.Species;
import species.auth.SUser;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory
import species.UploadJob;
import species.participation.UploadLog.Status;


class SpeciesBulkUpload extends UploadLog {

	String imagesDir;
	int speciesCreated = 0;
	int speciesUpdated = 0
	int stubsCreated = 0;
	
    static constraints = {
        importFrom UploadLog

		imagesDir nullable:true
    }

	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static transients = [ "logFile" ]
	
	
	static SpeciesBulkUpload create(SUser author, Date startDate, Date endDate, String filePath, String imagesDir, String notes=null, String uploadType=null, Status status = Status.VALIDATION){
        if(!uploadType) uploadType = UploadJob.SPECIES_BULK_UPLOAD; 
		SpeciesBulkUpload sbu = new SpeciesBulkUpload (author:author, filePath:filePath, startDate:startDate, endDate:endDate, imagesDir:imagesDir, status:status, notes:notes, uploadType:uploadType)
		if(!sbu.save(flush:true)){
			sbu.errors.allErrors.each { println it }
			return null
		}else{
			log.debug "Created roll back hook ${sbu}"
			return sbu
		}
	}

    @Override
	def updateStatus(Status status){
		refresh()
		
		this.status = status
		
		if((status == Status.ABORTED) ||(status == Status.FAILED) || (status == Status.UPLOADED)){
			this.endDate = new Date()
			updateSpeciesCount()
		}
		
		if(!this.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
	private void updateSpeciesCount(){
		def res = Species.fetchSpeciesSummary(startDate, endDate)
		speciesCreated = res.speciesCreated
		speciesUpdated = res.speciesUpdated
		stubsCreated = res.stubsCreated
	}
	
	def writeLog(String content, boolean isNameValidation = false){
		if(!logFilePath){
			String contentRootDir = Holders.config.speciesPortal.content.rootDir
			String tmpFileName = isNameValidation ? "logFile.txt" : "logFile.csv"
			logFile = utilsService.createFile(tmpFileName , "species", contentRootDir)
			logFilePath = logFile.getAbsolutePath();
			if(!this.save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
			println "----------------------- logFile path " + logFilePath
			if(!isNameValidation)
				logFile << "Name|Taxon Id|Status|Position|Rank|Col Id|Hir Names|Synonyms|Common Names"
		}
		
		def ln = System.getProperty('line.separator')
		logFile << "$ln$content"
	}
	
}
