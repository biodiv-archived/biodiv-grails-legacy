package content.eml

import java.util.Date;
import grails.converters.JSON;

import speciespage.ObvUtilService;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory

class DocumentTokenUrl {

	Document doc;
	String tokenUrl;
	String status;
	Date createdOn;



    static constraints = {
    }

    static  DocumentTokenUrl createLog( Document documentInstance, String tokenUrl) {
	    DocumentTokenUrl tu = new DocumentTokenUrl (createdOn: new Date(), doc:documentInstance, tokenUrl:tokenUrl, status:ObvUtilService.SCHEDULED)
		if(!tu.save(flush:true)){
			tu.errors.allErrors.each { println it }
			return null
		}else {
			return tu
		}
    }
}
