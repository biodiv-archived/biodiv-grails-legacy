package species.participation

import java.util.Date;
import grails.converters.JSON;

import species.auth.SUser;
import speciespage.ObvUtilService;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory

class DownloadLog {
	private static log = LogFactory.getLog(this);
	
	public enum DownloadType {
		CSV("CSV"),
		KML("KML"),
		PDF("PDF"),
		ZIP("ZIP"),
		TAR("TAR"),
		DWCA("DWCA")
		private String value;

		DownloadType(String value) {
			this.value = value;
		}
		
		static list() {
			return [CSV, KML, PDF];
		}
		
		String value() {
			return this.value;
		}
	}
	
	Date createdOn;
	String filePath;
	String filterUrl;
	DownloadType type;
	String notes;
	String status;
	String paramsMapAsText
	String sourceType
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		notes nullable:true, blank: true, size:0..400
		paramsMapAsText nullable:true, blank: true
		filePath nullable:true
    }

	static mapping = {
		version : false;
		notes type:'text';
		paramsMapAsText type:'text';
    }
	
	static createLog(SUser author, String filterUrl, String downloadTypeString, String notes, String sourceType, params){
		return createLog(author, null, filterUrl, downloadTypeString, notes, sourceType, new Date(),  ObvUtilService.SCHEDULED, params)
	}
	
	static createLog(SUser author, String filePath, String filterUrl, String downloadTypeString, String notes,  String sourceType, Date createdOn, String status, params){
		def paramsMapAsText = getTextFromMap(params)
		log.debug "params in download log "+ paramsMapAsText
		DownloadLog dl = new DownloadLog (author:author, filePath:filePath, filterUrl:filterUrl, type:getType(downloadTypeString), notes:notes, createdOn:createdOn, status:status, sourceType:sourceType, paramsMapAsText:paramsMapAsText)
		if(!dl.save(flush:true)){
			dl.errors.allErrors.each { println it }
	 	}
		return dl
	 }
	
	static DownloadType getType(String dType){
		if(!dType) return null;
		for(DownloadType type : DownloadType) {
			if(type.name().equals(dType)) {
				return type;
			}
		}
		return null;
	}
	
	private static String  getTextFromMap(params){
		Map newMap = new HashMap(params)
		
		newMap.remove("action")
		newMap.remove("controller")
		newMap.remove("max")
		newMap.remove("offset")
		newMap.remove("filterUrl")
		newMap.remove("notes")
		newMap.remove("downloadType")
		
		return newMap as JSON
	}
	
	def fetchMapFromText(){
		return JSON.parse(paramsMapAsText)
	}
	
	
}
