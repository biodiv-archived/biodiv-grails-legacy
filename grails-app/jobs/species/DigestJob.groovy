package species

import java.util.logging.Logger;

import species.participation.Digest;

class DigestJob {

    def digestService

    static triggers = {
        cron name:'cronTrigger', startDelay:600l, cronExpression: '0 0 5 ? * *'
    }

    def execute() {
        digestService.sendDigestWrapper(dig)
    }
}
