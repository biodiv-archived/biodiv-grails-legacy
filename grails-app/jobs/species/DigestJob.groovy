package species

import java.util.logging.Logger;

import species.participation.Digest;

class DigestJob {

    def digestService

    static triggers = {
        cron name:'cronTriggerForDigest', startDelay:600l, cronExpression: '0 0 5 * * ?'
        //'0 55 11 * * ?'
        //'0 0/5 * * * ?'
        //'0 35 11 ? * *'
    }

    def execute() {
        println "============SENDING DIGEST MAIL STARTED==================="
        //digestService.sendDigestAction();
        println "============SENDING DIGEST MAIL FINISHED=================="
    } 

}
