package species

import java.util.logging.Logger;

import species.participation.Digest;

class DigestJob {

    def digestService
	def dataSource
	
    static triggers = {
        println "==========================Setting trigger for DigestJob";
        cron name:'cronTriggerForDigest', startDelay:600l, cronExpression: '0 0 5 * * ?'
    }

    def execute() {
		int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
		dataSource.setUnreturnedConnectionTimeout(1000);
		try{
	        println "============SENDING DIGEST MAIL STARTED==================="
	        digestService.sendDigestAction();
	        println "============SENDING DIGEST MAIL FINISHED=================="
		}catch(e){
			e.printStackTrace()
		}finally{
			dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
		}
    } 

}
