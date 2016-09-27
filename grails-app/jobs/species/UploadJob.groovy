package species

import java.util.logging.Logger;

import species.participation.UploadLog;
import species.participation.UploadLog.Status
import org.apache.log4j.Level;

class UploadJob {
	
	private final static String OBSERVATION_LIST = "observations";
	private final static String CHECKLIST = "checklist";
	private final static String SPECIES = "species";
	private final static String UNIQUE_SPECIES = "unique species";
	private final static String TAXONOMY_DEFINITION = "taxonomydefinition";
	private final static String TRAIT = "trait";
	private final static String FACT = "fact";
	
	def obvUtilService
    def utilsService;
	def checklistService
	def speciesService
    def namelistService;
    def traitService;
	def factService;

    protected static volatile boolean JOB_RUNNING = false

    static triggers = {
        println "==========================Setting trigger for UploadJob";
        simple startDelay: 600l, repeatInterval: 5000l // starts after 5 minutes and execute job once in 5 seconds 
    }

    def execute() {
        List scheduledTaskList = getJobs()

        if(!scheduledTaskList){
            return
        }

        scheduledTaskList.each { UploadLog dl ->
            try{
                log.debug "starting task $dl"
                Map result;

                switch (dl.uploadType.toLowerCase()) {
                    case [OBSERVATION_LIST, UNIQUE_SPECIES]:
                    //f = obvUtilService.export(dl.fetchMapFromText(), dl)
                    break
                    case CHECKLIST:
                    //f = checklistService.export(dl.fetchMapFromText(), dl)
                    break
                    case SPECIES:
                    //f = speciesService.export(dl.fetchMapFromText(), dl)
                    break;
                    case TAXONOMY_DEFINITION:
                    //f = namelistService.export(dl.fetchMapFromText(), dl);
                    break;
                    case TRAIT:
                    result = traitService.upload(dl.filePath, dl.fetchMapFromText(), dl);
                    break;
                    case FACT:
                    result = factService.upload(dl.filePath, dl.fetchMapFromText(), dl);
                    break;
                    default:
                    log.debug "Invalid source Type $dl.uploadType"
                }

                //dl.filePath = result.logFile.getAbsolutePath();
                //dl.notes = result.msg;

                if(result.success) {
                    dl.updateStatus(Status.UPLOADED);
                    log.debug "finish task $dl"
                    utilsService.sendNotificationMail(utilsService.IMPORT_REQUEST, dl, null, null, null);
                } else {
                    dl.updateStatus(Status.FAILED);
                    dl.writeLog("Failed $dl", Level.ERROR);
                }
            }catch (Exception e) {
                dl.writeLog(e.getMessage(), Level.ERROR);
                e.printStackTrace()
                dl.updateStatus(Status.FAILED)
            }
        }

		JOB_RUNNING = false
    }

    protected synchronized getJobs(){
        if(JOB_RUNNING){
            return null
        }
        List scheduledTaskList = UploadLog.findAllByStatus(Status.SCHEDULED, [sort: "id", order: "asc", max:1, cache:false])
        if(scheduledTaskList.isEmpty()){
            return null
        }
        scheduledTaskList.each { UploadLog dl ->
            dl.updateStatus(Status.RUNNING)
        }

        JOB_RUNNING = true
        return scheduledTaskList
    }
}
