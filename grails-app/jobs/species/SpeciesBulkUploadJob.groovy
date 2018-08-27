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
  
    }
	
}
