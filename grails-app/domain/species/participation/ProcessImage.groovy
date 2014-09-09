package species.participation

import java.util.Date;
import grails.converters.JSON;

import speciespage.ObvUtilService;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory

class ProcessImage {

	private static log = LogFactory.getLog(this);
    
    Date createdOn;
	String filePath;
    String directory;
    String status;

    static constraints = {
    }
	
    static createLog(String filePath, String directory) {
	    ProcessImage pi = new ProcessImage (createdOn: new Date(), filePath:filePath, directory:directory, status:ObvUtilService.SCHEDULED)
		if(!pi.save(flush:true)){
			pi.errors.allErrors.each { println it }
			return null
		}else {
			return pi
		}
    }
}
