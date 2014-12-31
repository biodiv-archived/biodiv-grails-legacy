-- Enable PostGIS (includes raster)
CREATE EXTENSION postgis;
-- Enable Topology
CREATE EXTENSION postgis_topology;
-- fuzzy matching needed for Tiger
CREATE EXTENSION fuzzystrmatch;
-- Enable US Tiger Geocoder
CREATE EXTENSION postgis_tiger_geocoder;

create extension dblink;


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

/** 19th Septembaer 2014 for language */
ALTER TABLE observation ADD language_id bigint;
alter table observation add constraint language_id foreign key (language_id) references language(id) match full;
update observation set language_id = 205;
alter table observation alter column language_id set not null;

ALTER TABLE document ADD language_id bigint;
alter table document add constraint language_id foreign key (language_id) references language(id) match full;
update document set language_id = 205;
alter table document alter column language_id set not null;

ALTER TABLE suser ADD language_id bigint;
alter table suser add constraint language_id foreign key (language_id) references language(id) match full;
update suser set language_id = 205;
alter table suser alter column language_id set not null;

ALTER TABLE user_group ADD language_id bigint;
alter table user_group add constraint language_id foreign key (language_id) references language(id) match full;
update user_group set language_id = 205;	
alter table user_group alter column language_id set not null;

ALTER TABLE resource ADD language_id bigint;
alter table resource add constraint language_id foreign key (language_id) references language(id) match full;
update resource set language_id = 205;
alter table resource alter column language_id set not null;

ALTER TABLE comment ADD language_id bigint;
alter table comment add constraint language_id foreign key (language_id) references language(id) match full;
update comment set language_id = 205;
alter table comment alter column language_id set not null;

alter table classification add column language_id bigint;
alter table classification add constraint language_id foreign key (language_id) references language(id) match full;
update classification set language_id = 205;
alter table classification alter column language_id set not null;


alter table species_field add column language_id bigint;
alter table species_field add constraint language_id foreign key (language_id) references language(id) match full;
update species_field set language_id = 205;
 alter table species_field alter column language_id set not null;

alter table field add column language_id bigint;
alter table field add constraint language_id foreign key (language_id) references language(id) match full;
update field set language_id = 205;
alter table field alter column language_id set not null;

alter table field add column connection bigint;
alter table field drop column version;
//load french defs
alter table field alter column connection set not null;


/* Added on 16 OCT */

alter table featured add column language_id bigint;
alter table featured add constraint language_id foreign key (language_id) references language(id) match full;
update featured set language_id = 205;
 alter table featured alter column language_id set not null;


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

/**
 * Before Migration script DB changes
 */
ALTER TABLE follow RENAME COLUMN user_id TO author_id;

ALTER TABLE observation_flag RENAME TO flag;
UPDATE activity_feed SET activity_holder_type = 'species.participation.Flag' WHERE activity_holder_type = 'species.participation.ObservationFlag';
update flag set flag = 'DETAILS_INAPPROPRIATE'  where flag = 'OBV_INAPPROPRIATE';

/**
 * Updating and dummy script code but no migration
 */
alter table activity_feed alter column activity_descrption type varchar(400);
update flag set object_id = 0;
update document set flag_count = 0;
update observation set feature_count = 0;
update species set feature_count = 0;
update document set feature_count = 0;

/*
 * uncomment code in flag static belongsTo = [author:SUser, observation:Observation]
 * 
 * run migration script
 * 
 * commnet back static belongsTo = [author:SUser, observation:Observation]
 */

/*
 * After migration script DB changes unc
 */
update activity_feed set last_updated = date_created;
alter table flag drop COLUMN observation_id ;
ALTER TABLE flag ADD CONSTRAINT flag_author_type_id UNIQUE (author_id, object_id, object_type);
ALTER TABLE flag ALTER COLUMN object_id SET NOT NULL;
ALTER TABLE flag ALTER COLUMN object_type SET NOT NULL;

/**
* 3rd April 2014
* Delete all hierarchy related entries from species field
* Add contributor to taxonomy registry where classification is Author contributoed hierarchy
*/

delete from species_field_license where species_field_licenses_id in (select id from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy'));;

delete from species_field_contributor where species_field_attributors_id in (select id from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy'));

delete from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy');


/*17th jul 2014*/
alter table facebook_user  add column access_token_expires timestamp without time zone ;

/* user account merging */
 update activity_feed set author_id = 4509 where author_id=4507;
 update activity_feed set author_id = 4509  where author_id=4507;
 update observation  set author_id = 4509 where author_id=4507;
 update comment  set author_id = 4509 where author_id=4507;
 update document  set author_id = 4509 where author_id=4507;
 update download_log  set author_id = 4509 where author_id=4507;
 update featured  set author_id = 4509 where author_id=4507;
 update flag  set author_id = 4509 where author_id=4507;
 update follow  set author_id = 4509 where author_id=4507 and object_type !='species.groups.UserGroup' and object_type != 'species.auth.SUser';
 delete from follow where author_id = 4507;
 update project  set author_id = 4509 where author_id=4507;
 update recommendation_vote  set author_id = 4509 where author_id=4507;
 update species_bulk_upload  set author_id = 4509 where author_id=4507;
 update species_permission  set author_id = 4509 where author_id=4507;
 update un_curated_votes  set author_id = 4509 where author_id=4507;
 update checklists_contributor  set contributor_id = 4509 where contributor_id=4507;
 update resource_contributor  set contributor_id = 4509 where contributor_id=4507;
 update species_field_contributor  set contributor_id = 4509 where contributor_id=4507;
 update comment_suser set suser_id = 4509 where suser_id=4507;
 update common_names_suser  set suser_id = 4509 where suser_id=4507;
 update species_field_suser  set suser_id = 4509 where suser_id=4507;
 update synonyms_suser  set suser_id = 4509 where suser_id=4507;
 update taxonomy_definition_suser  set suser_id = 4509 where suser_id=4507;
 update taxonomy_registry_suser  set suser_id = 4509 where suser_id=4507;
 delete from user_group_member_role  where s_user_id = 4507;
 update user_group_member_role  set s_user_id = 4509 where s_user_id=4507;

/* added on 13th nov 2014 */

ALTER TABLE observation ADD license_id bigint;
alter table observation add constraint license_id foreign key (license_id) references license(id) match full;


//list of checklist observations 
update observation set license_id = c.license_id from checklists c where observation.source_id = c.id and  observation.is_checklist = 'f' and observation.id != observation.source_id;

update observation set license_id = c.license_id from checklists c where observation.source_id = c.id and  observation.is_checklist = 't' and observation.id = observation.source_id;

update observation set license_id = 822 where license_id is null and is_checklist='f' and id=source_id;

alter table observation alter column license_id set not null;

select count(*) from document where license_id is null;
update document set license_id = 822 where license_id is null;
alter table document alter column license_id set not null;

update solr schema.xml biodiv/conf/schema.xml 

//added on 19th nov
//Dropping license from checklists table
ALTER TABLE checklists DROP COLUMN license_id;

//5th Dec 2014
//create index taxonomy_definition_canonical_form_idx on taxonomy_definition ((lower(canonical_form)));
//create index recommendation_name_idx on recommendation ((lower(name)));
CREATE EXTENSION pg_trgm;
CREATE INDEX ON recommendation using GIST(name gist_trgm_ops);
CREATE INDEX ON taxonomy_definition using GIST(canonical_form gist_trgm_ops);


delete from recommendation where id in (select r.id from recommendation r left outer join recommendation_vote rv on r.id=rv.recommendation_id where lower(r.name) in (select lower(r.name) from recommendation as r, taxonomy_definition as t where r.name ilike t.canonical_form and r.taxon_concept_id is null and r.is_scientific_name = true) and r.taxon_concept_id is not null and rv.id is  null);

delete from recommendation where id in (select r.id from recommendation r left outer join recommendation_vote rv on r.id=rv.recommendation_id where lower(r.name) in (select lower(r.name) from recommendation as r, synonyms as s where r.name ilike s.canonical_form and r.taxon_concept_id is null and r.is_scientific_name = true) and r.taxon_concept_id is not null and rv.id is  null);

CREATE TABLE tmp_table_update_taxonconcept as ( select r.id as recoid,  t.id as taxonid, r.name as name, r.language_id as rl, c.language_id as c_lang from recommendation as r, taxonomy_definition as t, common_names as c where 
            r.name ilike c.name and 
                    ((r.language_id is null and c.language_id is null) or (r.language_id is not null and c.language_id is not null and r.language_id = c.language_id ) or (r.language_id = c.language_id )) and 
                            c.taxon_concept_id = t.id and c.taxon_concept_id is not null and
                                    r.taxon_concept_id is null and r.is_scientific_name = false);

update recommendation_vote set common_name_reco_id = 316619 where common_name_reco_id = 366025;

delete from recommendation where id in (select r.id from recommendation r left outer join recommendation_vote rv on r.id=rv.recommendation_id or r.id=rv.common_name_reco_id where r.id in (select r.id from recommendation r , tmp_table_update_taxonconcept t where lower(r.name)=lower(t.name) and ((r.language_id is not null and t.c_lang is not null and r.language_id = t.c_lang) or (r.language_id is null and t.language_id is null)) and r.is_scientific_name = false and r.taxon_concept_id = t.taxonid ));

#added by sathish for add references
update species_field SET description = 'dummy' where field_id = 81 and description = '';