package species.participation

import java.util.Date;
import grails.converters.JSON;

import species.auth.SUser;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory

class SpeciesBulkUpload {
	private static log = LogFactory.getLog(this);
	
	public enum Status {
		ROLLBACK("ROLLBACK"),
		UPLOADED("UPLOADED"),
		RUNNING("RUNNING"),
		FAILED("FAILED")
		
		private String value;

		Status(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
	}
	
	Date startDate;
	Date endDate;
	String notes;
	Status status;
	String filePath;
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		notes nullable:true, blank: true, size:0..400
    }
	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static SpeciesBulkUpload create(SUser author, Date startDate, Date endDate, String filePath, String notes=null, Status status = Status.UPLOADED){
		SpeciesBulkUpload sbu = new SpeciesBulkUpload (author:author, filePath:filePath, startDate:startDate, endDate:endDate, status:status, notes:notes)
		if(!sbu.save(flush:true)){
			sbu.errors.allErrors.each { println it }
			return null
		}else{
			log.debug "Created roll back hook ${sbu}"
			return sbu
		}
	}
	
	def updateStatus(Status status){
		this.status = status
		if(!this.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
	
}
