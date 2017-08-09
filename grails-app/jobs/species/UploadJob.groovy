package species

import java.util.logging.Logger;

import species.participation.UploadLog;
import species.participation.UploadLog.Status
import org.apache.log4j.Level;
import groovy.sql.Sql
import java.util.logging.Logger;
import species.participation.SpeciesBulkUpload
import species.participation.NamelistService

class UploadJob {
	
	private final static String OBSERVATION_LIST = "observations";
	private final static String CHECKLIST = "checklist";
	private final static String SPECIES = "species";
	private final static String UNIQUE_SPECIES = "unique species";
	private final static String TAXONOMY_DEFINITION = "taxonomydefinition";
	private final static String TRAIT = "trait";
	private final static String FACT = "fact";
	public final static String SPECIES_BULK_UPLOAD = "species bulk upload";
	
	def obvUtilService
    def utilsService;
	def checklistService
	def speciesService
    def namelistService;
    def traitService;
	def factService;
    def speciesUploadService
	def dataSource
	
    protected static volatile boolean JOB_RUNNING = false

    static triggers = {
        println "==========================Setting trigger for UploadJob";
        simple name:'UploadJob', startDelay: 600l, repeatInterval: 5000l // starts after 5 minutes and execute job once in 5 seconds 
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
                    case SPECIES_BULK_UPLOAD:
                    int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
                    dataSource.setUnreturnedConnectionTimeout(100000);
                    try{
                        println "starting task $dl and sleeping"
                        TaxonomyDefinition.UPDATE_SQL_LIST.clear();
                        speciesUploadService.upload(dl)
                        dl.updateStatus(Status.SUCCESS)
                        result = ['success':true];
                        utilsService.clearCache("defaultCache")
                        excuteSql()
                    }catch (Exception e) {
                        log.debug " Error while running task $dl"
                        e.printStackTrace()
                        result = ['success':false];
                        dl.updateStatus(Status.FAILED)
                    }finally{
                        TaxonomyDefinition.UPDATE_SQL_LIST.clear();
                        NamelistService.clearSessionNewNames();
                    }
                    log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
                    dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
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

    private void excuteSql(){
		if(!TaxonomyDefinition.UPDATE_SQL_LIST)
			return

		Sql sql = new Sql(dataSource)
		TaxonomyDefinition.UPDATE_SQL_LIST.each { String s ->
			log.debug " Path update query " + s
			try{
				int updateCount = sql.executeUpdate(s);
				log.debug " updated path count  " + 	updateCount
				}catch(e){
					e.printStackTrace()
				}
		}
		TaxonomyDefinition.UPDATE_SQL_LIST.clear();
		String defHirUpdateSql = """ update taxonomy_definition set default_hierarchy = g.dh from (select x.lid, json_agg(x) dh from (select s.lid, t.id, t.name, t.canonical_form, t.rank from taxonomy_definition t, (select taxon_definition_id as lid, regexp_split_to_table(path,'_')::integer as tid from taxonomy_registry tr where tr.classification_id = 265799 order by tr.id) s where s.tid=t.id order by lid, t.rank) x group by x.lid) g where g.lid=id; """
		int hirupdateCount = sql.executeUpdate(defHirUpdateSql);
		log.debug " Default hir update count " + hirupdateCount
	}

}
