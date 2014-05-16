package species.participation

import java.util.Date;
import grails.converters.JSON;

import species.Species;
import species.auth.SUser;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory


class SpeciesBulkUpload {
	private static log = LogFactory.getLog(this);
	
	public enum Status {
		SCHEDULED("SCHEDULED"),
		RUNNING("RUNNING"),
		ABORTED("ABORTED"),
		FAILED("FAILED"),
		UPLOADED("UPLOADED"),
		ROLLBACK("ROLLBACK")
		
		
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
	String errorFilePath;
	String imagesDir;
	
	int speciesCreated = 0;
	int speciesUpdated = 0
	int stubsCreated = 0;
	
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		filePath nullable:true
		errorFilePath nullable:true
		imagesDir nullable:true
		endDate nullable:true
		notes nullable:true, blank: true, size:0..400
    }
	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static SpeciesBulkUpload create(SUser author, Date startDate, Date endDate, String filePath, String imagesDir, String notes=null, Status status = Status.SCHEDULED){
		SpeciesBulkUpload sbu = new SpeciesBulkUpload (author:author, filePath:filePath, startDate:startDate, endDate:endDate, imagesDir:imagesDir, status:status, notes:notes)
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
}
