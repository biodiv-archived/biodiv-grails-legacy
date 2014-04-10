import java.util.List;
import utils.Newsletter;
import species.groups.UserGroup;


//set context of resources accordingly
UPDATE resource SET context ='SPECIES' where id in (select resource_id from species_resource);
UPDATE resource SET context ='OBSERVATION' where id in (select resource_id from observation_resource);

//add value false to all observation for isLocked
UPDATE observation SET is_locked = false;


//initialize all display order in newsletter to zero
UPDATE newsletter SET display_order = 0;

//set displayOrder for all news letter user group specific 
// it should start from 0 and increase with date ascending
//then latest created with have max displayOrder

//this is for parent portal

def res1 = Newsletter.executeQuery ('''
    from Newsletter nl where nl.userGroup is null order by nl.date asc''', []);

    println "====================res1 ============== " + res1
    def counter1 = 0
    res1.each{
        it.displayOrder = counter
        counter1++
        if(!it.save(flush:true)){
            it.errors.allErrors.each { log.error it }        
        }
    }

//for rest of the groups

def ugList = UserGroup.list()
println "======ALL UG LIST ======= " + ugList
ugList.each{
    def res = Newsletter.executeQuery ('''
    from Newsletter nl where nl.userGroup = :ug  order by nl.date asc''', ['ug' : it]);

    println "====================res ============== " + res
    def counter = 0
    res.each{
        it.displayOrder = counter
        counter++
        if(!it.save(flush:true)){
            it.errors.allErrors.each { log.error it }        
        }
    }
}

