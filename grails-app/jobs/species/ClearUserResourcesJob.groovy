package species

import java.util.logging.Logger;

import species.Resource;


class ClearUserResourcesJob {

    def resourcesService

    static triggers = {
        cron name:'cronTrigger', startDelay:600l, cronExpression: '0 0 2 ? * *'
    }

    def execute() {
        //resourcesService.deleteUsersResources();
    } 

}
