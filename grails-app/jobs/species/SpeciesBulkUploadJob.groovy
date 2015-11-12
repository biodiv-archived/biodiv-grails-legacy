package species

import java.util.logging.Logger;
import species.participation.SpeciesBulkUpload.Status
import species.participation.SpeciesBulkUpload

class SpeciesBulkUploadJob {
	
	def speciesUploadService
	def utilsService
	
    static triggers = {
      simple startDelay: 1000l, repeatInterval: 5000l // starts after 1 second  and execute job once in 5 seconds 
    }

    def execute() {
		List scheduledTaskList = getJobs()
		if(!scheduledTaskList){
			return
		}
		
		scheduledTaskList.each { SpeciesBulkUpload dl ->
			try{
				log.debug "starting task $dl"
				speciesUploadService.upload(dl)
				//dl.updateStatus(Status.SUCCESS)
				//utilsService.sendNotificationMail(utilsService.DOWNLOAD_REQUEST, dl, null, null, null);
			}catch (Exception e) {
				log.debug " Error while running task $dl"
				e.printStackTrace()
				dl.updateStatus(Status.FAILED)
			}
		}
    }
	
	

	private synchronized getJobs(){
		List scheduledTaskList = SpeciesBulkUpload.findAllByStatus(Status.SCHEDULED, [sort: "id", order: "asc", max:5])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { SpeciesBulkUpload dl ->
			dl.updateStatus(Status.RUNNING)
		}
		
		return scheduledTaskList
	}
}
