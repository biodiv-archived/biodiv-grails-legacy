//set context of resources accordingly
UPDATE resource SET context ='SPECIES' where id in (select resource_id from species_resource);
UPDATE resource SET context ='OBSERVATION' where id in (select resource_id from observation_resource);

//add value false to all observation for isLocked
UPDATE observation SET is_locked = false;

//set displayOrder for all news letter user group specific 
// it should start from 0 and increase with date ascending
//then latest created with have max displayOrder

//this is for parent portal
def res = Newsletter.executeQuery ('''
                 from Newsletter nl where nl.userGroup is null order by nl.date asc''', []);

println "====================res ============== " + res
def counter = 0
res.each{
    it.displayOrder = counter
    counter++
    it.save(flush:true)
}
