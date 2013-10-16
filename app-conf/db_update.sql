/*
 *  All the sql commands are specific to Postgres database only 
 */


/*
 *  for testing index uses
 */
SELECT * FROM pg_stat_user_indexes WHERE relname = 'activity_feed';
SELECT * FROM pg_stat_user_tables WHERE relname = 'activity_feed';

/*
 indexes on activity feed table 
 */
CREATE INDEX root_holder_id_idx ON activity_feed(root_holder_id);
CREATE INDEX root_holder_type_idx ON activity_feed(root_holder_type);
CREATE INDEX last_updated_idx ON activity_feed(last_updated);
CREATE INDEX sub_root_holder_id_idx ON activity_feed(sub_root_holder_id);
CREATE INDEX sub_root_holder_type_idx ON activity_feed(sub_root_holder_type);


/*
 indexes on comment table 
 */
CREATE INDEX root_holder_id_comment_idx ON comment(root_holder_id);
CREATE INDEX root_holder_type_comment_idx ON comment(root_holder_type);
CREATE INDEX last_updated_comment_idx ON comment(last_updated);
CREATE INDEX comment_holder_id_idx ON comment(comment_holder_id);
CREATE INDEX comment_holder_type_idx ON comment(comment_holder_type);


/*
 indexes on checlist row table 
 */
CREATE INDEX rowId_checklist_data_idx ON checklist_row_data(row_id);

/*
 *  To update query planner to use available indexes
 */
ANALYZE activity_feed;
ANALYZE comment;
ANALYZE checklist_row_data;

CREATE VIEW map_layer_features AS
  SELECT f.type, f.feature, f.topology
    FROM dblink('hostaddr=10.0.0.9 port=5432 dbname=ibp user=postgres password=postgres123', 
        '(select descriptio as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_117_india_soils) union (select rain_range as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_119_india_rainfallzone) union (select temp_c as feature,  __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_162_india_temperature) union (select type_desc as feature, __mlocate__topology as topology,  __mlocate__layer_id as layer_id from lyr_118_india_foresttypes) union (select tahsil as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_115_india_tahsils)') 
        AS f(feature varchar(255), topology geometry, type varchar(255));


CREATE VIEW map_layer_features AS
  SELECT f.type, f.feature, f.topology
    FROM dblink('dbname=ibp', 
        '(select descriptio as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_117_india_soils) union (select rain_range as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_119_india_rainfallzone) union (select temp_c as feature,  __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_162_india_temperature) union (select type_desc as feature, __mlocate__topology as topology,  __mlocate__layer_id as layer_id from lyr_118_india_foresttypes) union (select tahsil as feature, __mlocate__topology as topology, __mlocate__layer_id as layer_id from lyr_115_india_tahsils)') 
        AS f(feature varchar(255), topology geometry, type varchar(255));
        
/**
 * Document refactor
 * 
 */
        
/** Before updating code */        
ALTER TABLE document RENAME COLUMN date_created TO created_on;
ALTER TABLE document RENAME COLUMN last_updated TO last_revised;
ALTER TABLE document RENAME COLUMN description TO notes;

/** After updating code */
update document set geo_privacy = false;
update document set latitude = 0.0 where latitude is null;
update document set longitude = 0.0 where longitude is null;

/**
 * Activity feed Refactor 
 */
update activity_feed set activity_descrption = activity_type where activity_type = 'Posted observation to group' or activity_type='Removed observation from group' or activity_type = 'Posted checklist to group' or activity_type = 'Removed checklist from group';
update activity_feed set activity_type = 'Posted resource' where activity_type = 'Posted observation to group' or activity_type = 'Posted checklist to group';
update activity_feed set activity_type = 'Removed resoruce' where activity_type='Removed observation from group' or activity_type = 'Removed checklist from group';

