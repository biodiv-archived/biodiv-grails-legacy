package species.participation

import java.util.Date;

import species.auth.SUser;

class DownloadLogs {
	
	Date dateCreated;
	String filePath;
	String requestUrl;
	
	static belongsTo = [author:SUser];
	
    static constraints = {
    }
	static mapping = {
		version : false;
    }
	
	static createLog(SUser author, String filePath, String requestUrl){
		DownloadLogs dl = new DownloadLogs (author:author, filePath:filePath, requestUrl:requestUrl)
		if(!dl.save(flush:true)){
			//dl.errors.allErrors.each { log.error it }
			return null
		}else{
			return dl
		}
	}
}
