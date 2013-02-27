package species.participation

import java.util.Date;

import species.auth.SUser;


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
	
	Date dateCreated;
	String filePath;
	String filterUrl;
	DownloadType type;
	String notes;
	
	static belongsTo = [author:SUser];
	
    static constraints = {
		notes nullable:true, blank: true, size:0..400
    }
	static mapping = {
		version : false;
		notes type:'text';
    }
	
	static createLog(SUser author, String filePath, String filterUrl, String downloadTypeString, String notes){
		DownloadLog dl = new DownloadLog (author:author, filePath:filePath, filterUrl:filterUrl, type:getType(downloadTypeString), notes:notes)
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
