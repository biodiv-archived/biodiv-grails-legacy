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

/////////////////////////////////////////STAT NAMELIST ////////////////////////////////////////////
PLEASE REFER namelist.sql do to the namelist_wikiwio migration.
////////////////////////////////////// ENDS NAMELIST ///////////////////////////////////////////////


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


/** 9th Jan 2015
    After restoring and th1 creation, removing invalid images and resource mappings and row entries
    Dropping reprImage column in species. No longer used.
 **/
ALTER TABLE species DROP COLUMN repr_image_id ;
ALTER TABLE species DROP constraint fk8849413c32f2eca9 ;

/** Then run script crop.groovy **/

/** 16th Jan 2015
    FilePicker security
    1. switch on security for biodiv account on filepicker
    2. generate secret key
    3. put it in additional-config file like this - 
    -----------------
    speciesPortal {
        observations {
            filePicker.key = 'AXCVl73JWSwe7mTPb2kXdz'
            filePicker.secret = '4UCJGK6GLVDTRDAHETOCHGPGIY'
        }
    }
    ----------------
**/

/** 27th Jan 2015
    Adding new column has_media in species
    to apply filter on species having media
 **/
ALTER TABLE species ADD COLUMN has_media boolean ;
update species set has_media = false;
update species set has_media = true where id in (select distinct(species_resources_id) from species_resource);
update species set has_media = true where id in (select distinct(sf.species_id) from species_field_resources sfr, species_field sf where sfr.species_field_id = sf.id);

/** 28th Jan 2015
    Updating observations which were not marked deleted when its checklist was deleted
 **/
update observation set is_deleted = true where source_id in (select id from observation where is_checklist = true and is_deleted = true) and is_deleted = false;



//2nd feb 2015
alter table featured add column expire_time timestamp without time zone ;

#added by sathish for add references
update species_field SET description = 'dummy' where field_id = 81 and description = '';



# 2nd march 2015
# Observation enhancement
ALTER TABLE observation ADD COLUMN location_scale character varying(255);
ALTER TABLE custom_field ADD COLUMN allowed_participation boolean;


# 6th may 2015
# Observation enhancement
update observation set location_scale = 'APPROXIMATE' where  location_scale is null;
alter table observation  alter column location_scale set not null;


# 22 june 2015
////////////////////////////// redundant table drop //////////////
drop table un_curated_scientific_names_un_curated_common_names;
drop table un_curated_votes;
drop table un_curated_common_names;
drop table un_curated_scientific_names;


#23 june 2015 activity feed correction for species page
update activity_feed set activity_descrption = activity_type where activity_type like 'Added hierarchy%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Updated hierarchy%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Deleted hierarchy%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Added common name%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Updated common name%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Deleted common name%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Added synonym%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Updated synonym%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Deleted synonym%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Updated species field%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Added species field%';
update activity_feed set activity_descrption = activity_type where activity_type like 'Deleted species field%';


update activity_feed set activity_type = 'Added hierarchy' where activity_type like 'Added hierarchy%';
update activity_feed set activity_type = 'Updated hierarchy' where activity_type like 'Updated hierarchy%';
update activity_feed set activity_type = 'Deleted hierarchy' where activity_type like 'Deleted hierarchy%';
update activity_feed set activity_type = 'Added common name' where activity_type like 'Added common name%';
update activity_feed set activity_type = 'Updated common name'  where activity_type like 'Updated common name%';
update activity_feed set activity_type = 'Deleted common name' where activity_type like 'Deleted common name%';
update activity_feed set activity_type = 'Added synonym' where activity_type like 'Added synonym%';
update activity_feed set activity_type =  'Updated synonym' where activity_type like 'Updated synonym%';
update activity_feed set activity_type = 'Deleted synonym' where activity_type like 'Deleted synonym%';
update activity_feed set activity_type = 'Updated species field' where activity_type like 'Updated species field%';
update activity_feed set activity_type = 'Added species field' where activity_type like 'Added species field%';
update activity_feed set activity_type = 'Deleted species field' where activity_type like 'Deleted species field%';


# 30th Jun 2015
alter table doc_sci_name add column taxon_concept_id bigint;

#7th July 2015
alter table taxonomy_registry add column parent_taxon_definition_id bigint;
alter table taxonomy_registry add constraint td_fk foreign key (parent_taxon_definition_id) references taxonomy_definition(id);
update taxonomy_registry set parent_taxon_definition_id=t1.taxon_definition_id from taxonomy_registry t1 where taxonomy_registry.parent_taxon_id=t1.id;

///////////////////////////// 7th aug 2015 ////////////////////////
alter table user_group add column send_digest_mail boolean;
update user_group set send_digest_mail = false; 
update user_group set send_digest_mail = true where id in (select user_group_id from digest);
alter table user_group alter column send_digest_mail set not NULL;

alter table user_group  add column stat_start_date timestamp without time zone;
update user_group set stat_start_date = founded_on;
alter table user_group alter column stat_start_date set not NULL;

alter table digest drop column start_date_stats;

#13th Aug 2015
alter table download_log add column offset_param bigint;
update download_log set offset_param=0;
alter table download_log alter column offset_param set not null;


#21 Oct 2015
ALTER TABLE species_bulk_upload ADD COLUMN  upload_type varchar(255);

#16th Nov 2015 
drop index if exists last_updated_comment_idx, root_holder_type_comment_idx, root_holder_id_comment_idx;

#16th Nov 2015
ALTER TABLE newsletter ADD language_id bigint;
update newsletter set language_id=205;
alter table newsletter alter column language_id set not null;
ALTER TABLE newsletter ADD parent_id bigint;
update newsletter set parent_id=0;
alter table newsletter alter column parent_id set not null;



#25 Nov 2015
#Please stop app before running these queries
alter table document add column visit_count integer not null default 0;
alter table document add column rating integer not null default 0;
alter table document add column is_deleted boolean not null default 'false';

alter table observation add column protocol varchar(255);
update observation set protocol='SINGLE_OBSERVATION';
alter table observation alter column protocol set  not null;

alter table observation add column basis_of_record varchar(255);
update observation set basis_of_record='HUMAN_OBSERVATION';
alter table observation alter column basis_of_record set  not null;

insert into license(id,name) values (828,'UNSPECIFIED');

select max(id) from activity_feed;
alter sequence hibernate_sequence restart with ;

alter table dataset add column type varchar(255);
update dataset set type='OBSERVATIONS';
alter table dataset alter column type set not null;

ALTER TABLE dataset ALTER COLUMN rights type text;
ALTER TABLE dataset ALTER COLUMN purpose type text;
ALTER TABLE dataset ALTER COLUMN additional_info type text;
ALTER TABLE dataset ALTER COLUMN description type text;
ALTER TABLE datasource ALTER COLUMN description type text;

#28th Dec 2015

alter table observation alter column place_name drop not null;
alter table observation alter column reverse_geocoded_name drop not null ;
alter table observation alter column place_name type text;
alter table observation alter column reverse_geocoded_name type text;



#20thJan2016
#Please stop app before running these queries
create index external_id_idx on observation(external_id);

drop view observation_locations ;
drop view checklist_species_locations;
drop view checklist_species_view;
ALTER TABLE recommendation ALTER COLUMN name type text;


create view observation_locations as  SELECT obs.id,
    'observation:'::text || obs.id AS source,
        r.name AS species_name,
            obs.topology,
                obs.last_revised
                   FROM observation obs,
                    recommendation r
                      WHERE obs.max_voted_reco_id = r.id AND obs.is_deleted = false AND (obs.is_showable = true OR obs.external_id is not null);

create view checklist_species_view as SELECT obs.source_id AS id,
    r.name AS species_name
       FROM observation obs,
        recommendation r
          WHERE obs.max_voted_reco_id = r.id AND obs.is_deleted = false AND obs.is_showable = false
          GROUP BY obs.source_id, r.name;


create view checklist_species_locations as SELECT csv.id,
'checklist:'::text || csv.id AS source,
    cls.title,
        csv.species_name,
            obs.topology
                FROM checklist_species_view csv,
                observation obs,
                    checklists cls
                        WHERE csv.id = obs.id AND obs.id = cls.id;

drop sequence document_id_seq; drop sequence observation_id_seq; drop sequence species_id_seq; drop sequence suser_id_seq;
select max(id) from document; select max(id) from observation; select max(id) from species; select max(id) from suser;
create sequence document_id_seq start ;
create sequence observation_id_seq start ;
create  sequence species_id_seq start ; 
create sequence suser_id_seq start ;

#1st Feb 2016
#Please stop app before running these queries
# Upload gbif data before this
ALTER TABLE observation DISABLE TRIGGER ALL ;
alter table observation add constraint obv_dataset_id_fk foreign key (dataset_id) references dataset(id);

alter table observation add column no_of_images integer not null default 0, add column no_of_videos integer not null default 0,  add column no_of_audio integer not null default 0, add column no_of_identifications integer not null default 0;

update observation set no_of_images = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='IMAGE' group by observation_id) g where g.observation_id = id;
update observation set no_of_videos = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='VIDEO' group by observation_id) g where g.observation_id = id;
update observation set no_of_audio = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='AUDIO' group by observation_id) g where g.observation_id = id;

create table tmp as select observation_id, count(*) as count from recommendation_vote group by observation_id;

update observation set no_of_identifications = g.count from (select * from tmp) g where g.observation_id=id;

drop table tmp;

create table tmp as select resource_id, observation_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from observation_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='resource' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref order by observation_id asc, avg desc, resource_id asc;

update observation set repr_image_id = g.resource_id from (select b.observation_id,b.resource_id from (select observation_id, max(avg) as max_avg from tmp group by observation_id) a inner join tmp b on a.observation_id=b.observation_id where b.avg=a.max_avg) g where g.observation_id=id;

drop table tmp;

create index on observation(external_id);
create index on observation(dataset_id);
create index on observation(group_id);
create index on observation(max_voted_reco_id);
create index on recommendation(taxon_concept_id);
create index on observation(is_checklist, is_deleted, is_showable);
create index on observation(last_revised desc, id asc);
CREATE INDEX observation_topology_gist ON observation USING GIST (topology);
ANALYZE observation;
VACUUM ANALYZE observation;

ALTER TABLE observation ENABLE TRIGGER ALL ;

#8th Feb 2016
#Please stop app before running these queries
alter table taxonomy_definition add column species_id bigint;
alter table taxonomy_definition add foreign key(species_id) references species(id);
update taxonomy_definition set species_id = s.sid from (select taxon_concept_id, id as sid from species) s  where s.taxon_concept_id = id;

#adding defaultHierarchy json to taxon_definition table
alter table taxonomy_definition alter column default_hierarchy type text;
update taxonomy_definition set default_hierarchy = g.dh from (select x.lid, json_agg(x) dh from (select s.lid, t.id, t.name, t.canonical_form, t.rank from taxonomy_definition t, (select taxon_definition_id as lid, regexp_split_to_table(path,'_')::integer as tid from taxonomy_registry tr where tr.classification_id = 265799 order by tr.id) s where s.tid=t.id order by lid, t.rank) x group by x.lid) g where g.lid=id;

#7th feb 2016
CREATE INDEX normalized_form_idx ON taxonomy_definition(normalized_form);
CREATE INDEX status_idx ON taxonomy_definition(status);
CREATE INDEX rank_idx ON taxonomy_definition(rank);
CREATE INDEX position_idx ON taxonomy_definition(position);
CREATE INDEX match_id_idx ON taxonomy_definition(match_id);

#10thFeb 2016
#creating single license for resource instead of multiple
alter table resource add column license_id bigint;
alter table resource add foreign key (license_id) references license(id);
update resource set license_id = g.license_id from (select license_id,resource_licenses_id from resource_license group by resource_licenses_id, license_id) g where g.resource_licenses_id=id;
update resource set license_id=822 where license_id is null;
alter table resource alter column license_id set not null;

#creating single contributor instead of multiple for resource

#11th Feb 2016
alter table recommendation alter column is_scientific_name set not null;
alter table recommendation_vote add column given_sci_name text, add column given_common_name text;
update recommendation_vote set given_sci_name=reco.name from recommendation reco where reco.is_scientific_name='t' and reco.id=recommendation_id;
update recommendation_vote set given_common_name=reco.name from recommendation reco where reco.is_scientific_name='f' and reco.id=common_name_reco_id;


#15 Feb
update recommendation set accepted_name_id = taxon_concept_id;

#run gbifMigration + colReport userscripts before gbifupload

# 22 Feb 2016
alter table observation alter column longitude type double precision;
alter table observation alter column latitude type double precision;

create view reco_vote_details as  SELECT rv.id as reco_vote_id, rv.common_name_reco_id, rv.author_id, rv.voted_on, rv.comment, rv.original_author, rv.given_sci_name, rv.given_common_name, r.id as reco_id, r.name, r.is_scientific_name, r.language_id, t.id as taxon_concept_id, t.canonical_form, t.species_id, o.id as observation_id, o.is_locked, o.max_voted_reco_id FROM recommendation_vote rv inner join recommendation r on rv.recommendation_id=r.id inner join observation o on rv.observation_id=o.id left outer join taxonomy_definition t on t.id = r.taxon_concept_id;

#24 feb 2016
ALTER TABLE recommendation DROP CONSTRAINT recommendation_taxon_concept_id_key; 
ALTER TABLE recommendation ADD CONSTRAINT recommendation_taxon_concept_id_key UNIQUE (taxon_concept_id, accepted_name_id, name, language_id);
 

#3rd march update representative image for species
DROP TABLE IF EXISTS  tmp;
DROP TABLE IF EXISTS  tmp1;
create table tmp as ((select resource_id, species_resources_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from species_resource o left outer join (select rl.rating_ref, avg(r.stars), count(r.stars) from rating_link rl, rating r where rl.type='resource' and rl.rating_id = r.id  group by rl.rating_ref) c on o.resource_id =  c.rating_ref, resource r where resource_id = r.id  and r.type = 'IMAGE' )
union (select resource_id, sf.species_id as species_resources_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from species_field sf join species_field_resources o on species_field_id= sf.id left outer join (select rl.rating_ref, avg(r.stars), count(r.stars) from rating_link rl, rating r where rl.type='resource' and rl.rating_id = r.id group by rl.rating_ref) c on o.resource_id = c.rating_ref, resource r where resource_id = r.id and r.type = 'IMAGE' ));
create table tmp1 as select species_resources_id, max(avg) as avg from tmp  x  group by x.species_resources_id order by avg, x.species_resources_id;
update species set repr_image_id = g.resource_id from (select tmp.species_resources_id, tmp.resource_id from tmp, tmp1 where tmp.species_resources_id = tmp1.species_resources_id and tmp.avg = tmp1.avg) g where g.species_resources_id = id;
DROP TABLE IF EXISTS  tmp;
DROP TABLE IF EXISTS  tmp1;

#4thMar datasource and dataset seq
create sequence datasource_id_seq start 1;
create sequence dataset_id_seq start 1;
alter table resource alter column access_rights type varchar(2055);

#8 March
update observation set protocol='LIST' where is_checklist = true or (id != source_id);

#16 March
drop view reco_vote_details;
create view reco_vote_details as  SELECT rv.id as reco_vote_id, rv.common_name_reco_id, rv.author_id, rv.voted_on, rv.comment, rv.original_author, rv.given_sci_name, rv.given_common_name, r.id as reco_id, r.name, r.is_scientific_name, r.language_id, t.id as taxon_concept_id, t.normalized_form,t.status, t.species_id, o.id as observation_id, o.is_locked, o.max_voted_reco_id FROM recommendation_vote rv inner join recommendation r on rv.recommendation_id=r.id inner join observation o on rv.observation_id=o.id left outer join taxonomy_definition t on t.id = r.taxon_concept_id;

#20thMar2016
alter table dataset add column attribution text;
update dataset set attribution='';
alter table dataset alter column attribution set  not null;


#6thApr2016
update field set sub_category ='Local Endemicity Geographic Entity' where id=64;
update field set sub_category ='Local Distribution Geographic Entity' where id=61;

#22ndApr2016
alter table doc_sci_name add column primary_name integer not null default 0;
alter table doc_sci_name add column is_deleted boolean not null default 'false';

#02May2016
create view ibp_taxonomy_registry as
with recursive cte as
(   
    select
    *,
    cast(0 as text) as level
    from taxonomy_registry
    where parent_taxon_id is null and classification_id=265799
    union all
    select
    t.*,
    level || '.' || t.parent_taxon_definition_id AS level
    from taxonomy_registry t 
    inner join cte i on i.id = t.parent_taxon_id
    where t.classification_id = 265799
)
select * from cte;

#4th may
#alter table common_names add column is_deleted boolean not null default 'false';
ALTER TABLE common_names DROP constraint common_names_taxon_concept_id_key ;
ALTER TABLE common_names ADD CONSTRAINT common_names_taxon_concept_id_key UNIQUE (taxon_concept_id, language_id, name, is_deleted);

create index on common_names(is_deleted);
create index on taxonomy_definition(is_deleted);


#16th may
alter table species add column is_deleted boolean not null default 'false';
create index on species(is_deleted);


#17 may 
update document set external_url =uri where uri !='';
alter table document drop column uri;

#26 may
ALTER TABLE document ALTER COLUMN latitude TYPE double precision;
ALTER TABLE document ALTER COLUMN longitude TYPE double precision;

#8 Apr
ALTER TABLE suser ADD COLUMN latitude double precision;
ALTER TABLE suser ADD COLUMN longitude double precision;

#21Sep2016
alter table taxonomy_definition add column traits bigint[][];
alter table taxonomy_definition alter column traits  type bigint[][] using traits::bigint[][];
CREATE AGGREGATE array_agg_custom(anyarray)
(
        SFUNC = array_cat,
            STYPE = anyarray
        );
update taxonomy_definition set traits = g.item from (
             select x.page_taxon_id, array_agg_custom(ARRAY[ARRAY[x.tid, x.tvid]]) as item from (select f.page_taxon_id, t.id as tid, tv.id as tvid, tv.value from fact f, trait t, trait_value tv where f.trait_id = t.id and f.trait_value_id = tv.id ) x group by x.page_taxon_id
) g where g.page_taxon_id=id;


CREATE INDEX taxonomy_definition_traits ON taxonomy_definition using gin(traits);

alter table trait add column is_deleted boolean not null default false;
alter table fact add column is_deleted boolean not null default false;


#3rd Oct
alter table fact drop constraint fact_object_id_page_taxon_id_trait_id_key;
alter table fact add constraint  fact_object_id_page_taxon_id_trait_id_trait_value_id_key unique(object_id, page_taxon_id, trait_id, trait_value_id);
delete from fact;
delete from trait_value;
delete from trait;
create sequence trait_id_seq start 1;
create sequence trait_value_id_seq start 1;
create sequence fact_id_seq start 1;

#18thOct2016
alter table resource add column gbifID bigint;

#20th Oct 2016   for merging uploadjob and species bulk upload
insert into upload_log(id,version, author_id, end_date, file_path, notes, start_date, status, error_file_path, images_dir, species_created, species_updated, stubs_created, upload_type, log_file_path, class) select id,version, author_id, end_date, file_path, notes, start_date, status, error_file_path, images_dir, species_created, species_updated, stubs_created, upload_type, log_file_path, 'species.participation.SpeciesBulkUpload' as class from species_bulk_upload ;

update upload_log set upload_type = 'species bulk upload';

#27thOct2016
alter table trait_value add column is_deleted boolean not null default false;

#15th Nov 2016
alter table fact add column object_type varchar(255);
update fact set object_type = class;
alter table fact alter column object_type set not null;
alter table fact drop column class;
alter table fact drop constraint fact_trait_value_id_object_id_page_taxon_id_trait_id_key;
create index fact_trait_value_id_object_id_object_type_trait_id_key on fact (trait_value_id, object_id, object_type, trait_id);
alter table fact alter column page_taxon_id drop not null;

#16thNov2016
alter table trait add column is_not_observation_trait boolean default 'f';
alter table trait add column show_in_observation boolean default 'f';
alter table trait add column is_participatory boolean default 't';
update species_group_mapping set taxon_concept_id = g.id from (select id,name from taxonomy_definition t ) as g where  g.name = taxon_name;

#21stNov2016
insert into trait_taxonomy_definition(trait_taxon_id,taxonomy_definition_id)  select id, taxon_id from trait;
alter table trait alter column taxon_id drop not null;
alter table trait drop column taxon_id;


#27/11 custom fields migration sqls
delete from custom_field where id not in (5,6);
select * from custom_fields_group_18 where cf_4 is not null and cf_4 != '';
alter table custom_fields_group_18 drop column cf_4;
select * from custom_fields_group_13 where cf_1 is not null and cf_1 != '';
drop table custom_fields_group_13;
select * from custom_fields_group_30 where cf_7 is not null and cf_7 != '';
drop table custom_fields_group_30;
select * from custom_fields_group_33 where cf_9 is not null and cf_9 != '';
select * from custom_fields_group_33 where cf_10 is not null and cf_10 != '';
select * from custom_fields_group_33 where cf_11 is not null and cf_11 != '';
drop table custom_fields_group_33;
select * from custom_fields_group_38 where cf_8 is not null and cf_8 != '';
drop table custom_fields_group_38;
select * from custom_fields_group_7 where cf_2 is not null and cf_2 != '';
select * from custom_fields_group_7 where cf_3 is not null and cf_3 != '';
drop table custom_fields_group_7;

select id from field where concept='Natural History' and category='Reproduction';
update trait set field_id=39 where name='Sex';

#21stDec2016
alter table trait alter column icon type text;

#03Jan2017
alter table fact add column to_value varchar(255);
alter table fact alter column trait_value_id drop not null;


#9thJan2017
alter table taxonomy_definition add column traits_json json;
update taxonomy_definition set traits_json = g.item from (
     select x1.page_taxon_id, format('{%s}', string_agg(x1.item,','))::json as item from (
        (select x.page_taxon_id,  string_agg(format('"%s":{"value":%s,"to_value":%s}', to_json(x.tid), to_json(x.value), to_json(x.to_value)), ',') as item from (select f.page_taxon_id, t.id as tid, f.value::numeric as value, f.to_value::numeric as to_value from fact f, trait t where f.trait_id = t.id and (t.data_types='NUMERIC') ) x group by x.page_taxon_id)
        union
        (select x.page_taxon_id,  string_agg(format('"%s":{"from_date":%s,"to_date":%s}', to_json(x.tid), to_json(x.from_date), to_json(x.to_date)), ',') as item from (select f.page_taxon_id, t.id as tid, f.from_date as from_date, f.to_date as to_date from fact f, trait t where f.trait_id = t.id and (t.data_types='DATE') ) x group by x.page_taxon_id)
        union
        (select x.page_taxon_id,  string_agg(format('"%s":{"r":%s,"g":%s,"b":%s}', to_json(x.tid), to_json(x.value[1]::integer), to_json(x.value[2]::integer), to_json(x.value[3]::integer)), ',') as item from (select f.page_taxon_id, t.id as tid, string_to_array(substring(f.value from 5 for length(f.value)-5),',') as value from fact f, trait t where f.trait_id = t.id and (t.data_types='COLOR')) x group by x.page_taxon_id)
    ) x1 group by x1.page_taxon_id
) g where g.page_taxon_id=id;


create table calendar as (
      select date '2017-01-01' + (n || ' days')::interval calendar_date
      from generate_series(0, 365) n
)

#15th March 2017
alter table external_links add column frlht_url varchar;
