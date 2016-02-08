package species

import java.util.logging.Logger;
import species.participation.SpeciesBulkUpload.Status
import species.participation.SpeciesBulkUpload

class SpeciesBulkUploadJob {
	
	def speciesUploadService
	def utilsService
	def dataSource
	
    static triggers = {
      simple startDelay: 1000l, repeatInterval: 5000l // starts after 1 second  and execute job once in 5 seconds 
    }

    def execute() {
		List scheduledTaskList = getJobs()
		if(!scheduledTaskList){
			return
		}
		
		scheduledTaskList.each { SpeciesBulkUpload dl ->
			int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
			try{
				log.debug "starting task $dl"
				dataSource.setUnreturnedConnectionTimeout(500);
				speciesUploadService.upload(dl)
				//dl.updateStatus(Status.SUCCESS)
				//utilsService.sendNotificationMail(utilsService.DOWNLOAD_REQUEST, dl, null, null, null);
			}catch (Exception e) {
				log.debug " Error while running task $dl"
				e.printStackTrace()
				dl.updateStatus(Status.FAILED)
			}finally {
            	log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
				dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
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
