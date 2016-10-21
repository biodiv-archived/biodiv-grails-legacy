package species

import java.util.logging.Logger;
import species.participation.NamesReportGenerator;
import species.participation.UploadLog.Status

class NameValidationJob {
	
	def speciesUploadService
	def utilsService
	
    static triggers = {
        println "==========================Setting trigger for NameValidationJob";
      simple startDelay: 1000l, repeatInterval: 5000l // starts after 1 second  and execute job once in 5 seconds 
    }

    def execute() {
		//println "NameValidationJob started -----------"
		List scheduledTaskList = getValidationJob()
		if(!scheduledTaskList){
			return
		}
		
		scheduledTaskList.each { NamesReportGenerator dl ->
			try{
				log.debug "starting task $dl"
				speciesUploadService.runNamesMapper(dl)
				//dl.updateStatus(Status.SUCCESS)
				//utilsService.sendNotificationMail(utilsService.DOWNLOAD_REQUEST, dl, null, null, null);
			}catch (Exception e) {
				log.debug " Error while running task $dl"
				e.printStackTrace()
				dl.updateStatus(Status.FAILED)
			}
		}
    }
	
	

	private synchronized getValidationJob(){
		List scheduledTaskList = NamesReportGenerator.findAllByStatus(Status.SCHEDULED, [sort: "id", order: "asc", max:5, cache:false])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { NamesReportGenerator dl ->
			dl.updateStatus(Status.RUNNING)
		}
		
		return scheduledTaskList
	}
}
