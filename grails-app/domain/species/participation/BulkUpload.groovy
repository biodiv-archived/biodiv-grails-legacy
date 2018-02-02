package species.participation

import java.util.Date;
import grails.util.Holders;
import grails.converters.JSON;
import species.Species;
import species.auth.SUser;
import species.dataset.DataTable;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory
import species.UploadJob;
import species.participation.UploadLog.Status;


class BulkUpload extends UploadLog {

	String imagesDir;
	int noCreated = 0;
	int noUpdated = 0;
    DataTable dataTable;
	
    static constraints = {
        importFrom UploadLog

		imagesDir nullable:true
    }

	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static transients = [ "logFile" ]
	
	
	static BulkUpload create(SUser author, Date startDate, Date endDate, String filePath, String imagesDir, String notes=null, String uploadType=null, Status status = Status.VALIDATION, DataTable dataTable=null){
        if(!uploadType) uploadType = UploadJob.BULK_UPLOAD; 
		BulkUpload sbu = new BulkUpload (author:author, filePath:filePath, startDate:startDate, endDate:endDate, imagesDir:imagesDir, status:status, notes:notes, uploadType:uploadType, dataTable:dataTable)
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
			updateCount()
		}
		
		if(!this.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
	private void updateCount(){
		def res = dataTable?.fetchSummary()
		noCreated = res.noCreated
		noUpdated = res.noUpdated
	}
/*	
	def writeLog(String content, boolean isNameValidation = false){
		if(!logFilePath){
			String contentRootDir = Holders.config.speciesPortal.content.rootDir
			String tmpFileName = isNameValidation ? "logFile.txt" : "logFile.csv"
			logFile = utilsService.createFile(tmpFileName , uploadType, contentRootDir)
			logFilePath = logFile.getAbsolutePath();
			if(!this.save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
			println "----------------------- logFile path " + logFilePath
		}
		
		def ln = System.getProperty('line.separator')
		logFile << "$ln$content"
	}
*/	
}
