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
    def f = ActivityFeed.findAllWhere(activityType:"Observation flagged")
    ActivityFeed.withTransaction() {
        f.each{ act ->
            println act
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
    Flag.withTransaction(){
        Flag.list().each { f ->
            f.objectId = f.observation.id
            f.objectType = f.observation.class.getCanonicalName()
            if(!f.save(flush:true)) {
                f.errors.allErrors.each { log.error it }
            }
        }
    }
}


migrateFlag()
//migrateFeedForFlag()

println "=========== done"

//SQL COMMANDS
//ALTER TABLE follow RENAME COLUMN user_id TO author_id;
//UPDATE activity_feed SET activity_holder_type = 'species.participation.Flag' WHERE activity_holder_type = 'species.participation.ObservationFlag';
//ALTER TABLE observation_flag RENAME TO flag;
//update flag set flag = 'DETAILS_INAPPROPRIATE'  where flag = 'OBV_INAPPROPRIATE';

//update flag set object_id = 0;


//after all migration script
//update activity_feed set last_updated = date_created;
//alter table flag drop COLUMN observation_id ;
//ALTER TABLE flag ALTER COLUMN object_id SET NOT NULL; ALTER TABLE flag ALTER COLUMN object_type SET NOT NULL; ALTER TABLE flag ADD CONSTRAINT flag_object_id_object_type UNIQUE (object_id, object_type, author_id);
//update document set flag_count = 0;

