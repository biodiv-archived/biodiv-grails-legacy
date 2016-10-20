package species

import groovy.sql.Sql
import java.util.logging.Logger;
import species.participation.UploadLog.Status
import species.participation.SpeciesBulkUpload
import species.participation.NamelistService

class SpeciesBulkUploadJob {
	
	
	def speciesUploadService
	def utilsService
	def dataSource
	
	private static volatile boolean JOB_RUNNING = false
	
    static triggers = {
//      simple startDelay: 1000l, repeatInterval: 10*1000l // starts after 1 second  and execute job once in 10 seconds 
    }

    def execute() {
/*		List scheduledTaskList = getJobs()
		if(!scheduledTaskList){
			return
		}
		
		int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(100000);
		
		try{
			scheduledTaskList.each { SpeciesBulkUpload dl ->
				try{
					println "starting task $dl and sleeping"
					TaxonomyDefinition.UPDATE_SQL_LIST.clear();
					speciesUploadService.upload(dl)
					dl.updateStatus(Status.SUCCESS)
					utilsService.clearCache("defaultCache")
					excuteSql()
				}catch (Exception e) {
					log.debug " Error while running task $dl"
					e.printStackTrace()
					dl.updateStatus(Status.FAILED)
				}finally{
					TaxonomyDefinition.UPDATE_SQL_LIST.clear();
					NamelistService.clearSessionNewNames();
				}
			}
		}catch(e){
			e.printStackTrace()
		}
		
		log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
		dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
		
		JOB_RUNNING = false
  */  }
	
	
/*
	private synchronized getJobs(){
		if(JOB_RUNNING){
			return null
		}
		
		List scheduledTaskList = SpeciesBulkUpload.findAllByStatus(Status.SCHEDULED, [sort: "id", order: "asc", max:1, cache:false])
		if(scheduledTaskList.isEmpty()){
			return null
		}
		scheduledTaskList.each { SpeciesBulkUpload dl ->
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
    */
}
