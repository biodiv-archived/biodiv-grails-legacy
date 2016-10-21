package species.participation

import java.util.Date;

import species.Species;
import species.auth.SUser;
import species.participation.UploadLog.Status

import org.apache.commons.logging.LogFactory

class NamesReportGenerator {
	private static log = LogFactory.getLog(this);
	
	Date startDate;
	Date endDate;
	Status status;
	String filePath;
	
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		filePath nullable:true
		endDate nullable:true
    }
	static mapping = {
		version : false;
    }
	
	static NamesReportGenerator create(SUser author, Date startDate, Date endDate, String filePath, Status status = Status.SCHEDULED){
		NamesReportGenerator sbu = new NamesReportGenerator (author:author, filePath:filePath, startDate:startDate, endDate:endDate, status:status)
		if(!sbu.save(flush:true)){
			sbu.errors.allErrors.each { println it }
			return null
		}else{
			log.debug "Created roll back hook ${sbu}"
			return sbu
		}
	}
	
	def updateStatus(Status status){
		refresh()
		
		this.status = status
		
		if((status == Status.FAILED) || (status == Status.SUCCESS)){
			this.endDate = new Date()
		}
		
		if(!this.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
}
