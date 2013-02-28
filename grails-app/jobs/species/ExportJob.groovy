package species

import java.util.logging.Logger;

import speciespage.ObvUtilService;
import species.participation.DownloadLog;

class ExportJob {
	
	def obvUtilService
	def observationService
	
    static triggers = {
      simple startDelay: 1000l, repeatInterval: 5000l // starts after 5 minutes and execute job once in 5 seconds 
    }

    def execute() {
		List scheduledTaskList = DownloadLog.findAllByStatus(ObvUtilService.SCHEDULED, [sort: "createdOn", order: "asc"])
		if(scheduledTaskList.isEmpty()){
			return
		}
		
		scheduledTaskList.each { DownloadLog dl ->
			try{
				log.debug "strating task $dl"
				setStatus(dl, ObvUtilService.EXECUTING)
				
				File f = obvUtilService.export(getParamsMap(dl.filterUrl))
				if(f){
					dl.filePath = f.getAbsolutePath()
					setStatus(dl, ObvUtilService.SUCCESS)
					log.debug "finish task $dl"
					observationService.sendNotificationMail(observationService.DOWNLOAD_REQUEST, dl, null, null, null);
				}else{
					setStatus(dl, ObvUtilService.FAILED)
					log.debug "Error $dl"
				}
			}catch (Exception e) {
				log.debug " Error while running task $dl"
				e.printStackTrace()
				setStatus(dl, ObvUtilService.FAILED)
			}
		}
    }
	
	private setStatus(task, status){
		task.status = status
		if(!task.save(flush:true)){
			task.errors.allErrors.each { log.debug it }
		}
	}
	
	private Map getParamsMap(String urlString){
		def u = new URL(urlString)
		def m = [:]
		URLDecoder.decode(u.getQuery(), "UTF-8").split("&").each { token ->
			def pair = token.split("=")
			m[pair[0]] = pair[1]
		}
		return m
	}
}
