//set context of resources accordingly
UPDATE resource SET context ='SPECIES' where id in (select resource_id from species_resource);
UPDATE resource SET context ='OBSERVATION' where id in (select resource_id from observation_resource);

//add value false to all observation for isLocked
UPDATE observation SET is_locked = false;
