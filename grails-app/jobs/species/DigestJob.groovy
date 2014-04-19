package species

import java.util.logging.Logger;

import species.participation.Digest;

class DigestJob {
	
	def digestService
	
    static triggers = {
      cron name:'cronTrigger', startDelay:600l, cronExpression: '0 0 7 ? * *'
    }

    def execute() {
		/*def digestList = Digest.list()
        digestList.each{ dig ->
            digestService.sendDigest(dig)
        }*/
    } 
	
}
