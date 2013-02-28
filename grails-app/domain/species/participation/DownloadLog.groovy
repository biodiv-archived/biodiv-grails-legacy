package species.participation

import java.util.Date;

import species.auth.SUser;
import speciespage.ObvUtilService;

class DownloadLog {
	
	public enum DownloadType {
		CSV("CSV"),
		KML("KML")
		
		private String value;

		DownloadType(String value) {
			this.value = value;
		}
		
		static list() {
			return [CSV, KML];
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
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		notes nullable:true, blank: true, size:0..400
		filePath nullable:true
    }
	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static createLog(SUser author, String filterUrl, String downloadTypeString, String notes){
		return createLog(author, null, filterUrl, downloadTypeString, notes, new Date(),  ObvUtilService.SCHEDULED)
	}
	
	static createLog(SUser author, String filePath, String filterUrl, String downloadTypeString, String notes, Date createdOn, String status){
		DownloadLog dl = new DownloadLog (author:author, filePath:filePath, filterUrl:filterUrl, type:getType(downloadTypeString), notes:notes, createdOn:createdOn, status:status)
		if(!dl.save(flush:true)){
			dl.errors.allErrors.each { println it }
			return null
		}else{
			return dl
		}
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
}
