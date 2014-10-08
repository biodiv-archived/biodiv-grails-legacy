package species

import java.util.logging.Logger;

import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import speciespage.ObvUtilService;
import species.participation.ProcessImage;

class ProcessImageJob {
    static triggers = {
      simple repeatInterval: 1000l // execute job once in 5 seconds
    }

    def execute() {
        // execute job
        List scheduledTaskList = getProcessImageRequest()
        if(!scheduledTaskList){
			return
		}
		scheduledTaskList.each { ProcessImage pi ->
			try{
				log.debug "starting task $pi"
                println "===========PROCESSING ============== " + pi
			    ImageUtils.createScaledImages(new File(pi.filePath), new File(pi.directory));
                setStatus(pi, ObvUtilService.SUCCESS)
			}catch (Exception e) {
				log.debug " Error while running task $pi"
				e.printStackTrace()
				setStatus(pi, ObvUtilService.FAILED)
			}
		}
    }

    private setStatus(task, status){
		task.status = status
		if(!task.save(flush:true)){
			task.errors.allErrors.each { log.debug it }
		}
	}

	private synchronized getProcessImageRequest(){
		List scheduledTaskList = ProcessImage.findAllByStatus(ObvUtilService.SCHEDULED, [sort: "createdOn", order: "asc", max:5])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { ProcessImage pi ->
			setStatus(pi, ObvUtilService.EXECUTING)
		}
		
		return scheduledTaskList
	}

}
