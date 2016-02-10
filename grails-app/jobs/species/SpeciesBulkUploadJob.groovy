package species

import java.util.logging.Logger;
import species.participation.SpeciesBulkUpload.Status
import species.participation.SpeciesBulkUpload

class SpeciesBulkUploadJob {
	
	def speciesUploadService
	def utilsService
	def dataSource
	
	private static volatile boolean JOB_RUNNING = false
	
    static triggers = {
      simple startDelay: 1000l, repeatInterval: 10*1000l // starts after 1 second  and execute job once in 10 seconds 
    }

    def execute() {
		List scheduledTaskList = getJobs()
		if(!scheduledTaskList){
			return
		}
		
		int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(500);
		
		try{
			scheduledTaskList.each { SpeciesBulkUpload dl ->
				try{
					println "starting task $dl and sleeping"
					speciesUploadService.upload(dl)
					dl.updateStatus(Status.SUCCESS)
					//utilsService.sendNotificationMail(utilsService.DOWNLOAD_REQUEST, dl, null, null, null);
				}catch (Exception e) {
					log.debug " Error while running task $dl"
					e.printStackTrace()
					dl.updateStatus(Status.FAILED)
				}
			}
		}catch(e){
			e.printStackTrace()
		}
		
		log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
		dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
		
		JOB_RUNNING = false
    }
	
	

	private synchronized getJobs(){
		if(JOB_RUNNING){
			return null
		}
		
		List scheduledTaskList = SpeciesBulkUpload.findAllByStatus(Status.SCHEDULED, [sort: "id", order: "asc", max:1])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { SpeciesBulkUpload dl ->
			dl.updateStatus(Status.RUNNING)
		}
		
		JOB_RUNNING = true
		return scheduledTaskList
	}
}
