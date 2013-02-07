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
CREATE INDEX root_holder_id_idx ON comment(root_holder_id);
CREATE INDEX root_holder_type_idx ON comment(root_holder_type);
CREATE INDEX last_updated_idx ON comment(last_updated);
CREATE INDEX comment_holder_id_idx ON comment(comment_holder_id);
CREATE INDEX comment_holder_type_idx ON comment(comment_holder_type);

/*
 *  To update query planner to use available indexes
 */
ANALYZE activity_feed;
ANALYZE comment;
