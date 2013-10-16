import species.Resource;
import species.auth.SUser;
import species.participation.*;
import groovy.sql.Sql;

import java.util.Date;
import species.utils.ImageUtils;
import java.util.*;

def migrateFeedForFlag() {
    def s = ctx.getBean("activityFeedService")
    def flagObj
    def f = ActivityFeed.FindAllWhere(activityType:"Observation flagged")
    ActivityFeed.withTransaction() {
        f.each{ act ->

            flagObj = s.getDomainObject(act.activityHolderType, act.activityHolderId)
            def activityNotes = flagObj.flag.value() + ( flagObj.notes ? " \n" + flagObj.notes : "")
            act.activityDescrption = activityNotes
            act.activityType = "Flagged"
            if(!act.save(flush:true)) {
                act.errors.allErrors.each { log.error it }
            }
        }
    }

}


//After migration drop table and remove observationflag.groovy

def migrateFlag() {
    Flag.withTransaction{
        Flag.list.each { f ->
            f.objectId = f.observation.id
            f.objectType = f.observation.class.getCanonicalName()
            if(!f.save(flush:true)) {
                f.errors.allErrors.each { log.error it }
            }
        }
    }
}


migrateFlag()
migrateFeedForFlag()

//SQL COMMANDS
//ALTER TABLE follow RENAME COLUMN user_id TO author_id;
//ALTER TABLE observation_flag RENAME TO flag;
//UPDATE activity_feed SET activity_holder_type = 'species.participation.Flag' WHERE activity_holder_type = 'species.participation.ObservationFlag'


