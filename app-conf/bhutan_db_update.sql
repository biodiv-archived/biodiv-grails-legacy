/*
 *  All the sql commands are specific to Postgres database only 
 */

alter table field add column connection bigint;
alter table field drop column version;

ALTER TABLE observation ADD language_id bigint;
alter table observation add constraint language_id foreign key (language_id) references language(id) match full;
update observation set language_id = 1;
alter table observation alter column language_id set not null;

ALTER TABLE document ADD language_id bigint;
alter table document add constraint language_id foreign key (language_id) references language(id) match full;
update document set language_id = 1;
alter table document alter column language_id set not null;

ALTER TABLE suser ADD language_id bigint;
alter table suser add constraint language_id foreign key (language_id) references language(id) match full;
update suser set language_id = 1;
alter table suser alter column language_id set not null;

ALTER TABLE user_group ADD language_id bigint;
alter table user_group add constraint language_id foreign key (language_id) references language(id) match full;
update user_group set language_id = 1;	
alter table user_group alter column language_id set not null;

ALTER TABLE resource ADD language_id bigint;
alter table resource add constraint language_id foreign key (language_id) references language(id) match full;
update resource set language_id = 1;
alter table resource alter column language_id set not null;

ALTER TABLE comment ADD language_id bigint;
alter table comment add constraint language_id foreign key (language_id) references language(id) match full;
update comment set language_id = 1;
alter table comment alter column language_id set not null;

alter table classification add column language_id bigint;
alter table classification add constraint language_id foreign key (language_id) references language(id) match full;
update classification set language_id = 1;
alter table classification alter column language_id set not null;


alter table species_field add column language_id bigint;
alter table species_field add constraint language_id foreign key (language_id) references language(id) match full;
update species_field set language_id = 1;
 alter table species_field alter column language_id set not null;

alter table field add column language_id bigint;
alter table field add constraint language_id foreign key (language_id) references language(id) match full;
update field set language_id = 1;
alter table field alter column language_id set not null;


update field set connection = display_order;
alter table field alter column connection set not null;

alter table featured add column language_id bigint;
alter table featured add constraint language_id foreign key (language_id) references language(id) match full;
update featured set language_id = 1;
alter table featured alter column language_id set not null;


alter table facebook_user  add column access_token_expires timestamp without time zone ;

ALTER TABLE observation ADD license_id bigint;
alter table observation add constraint license_id foreign key (license_id) references license(id) match full;


update observation set license_id = c.license_id from checklists c where observation.source_id = c.id and  observation.is_checklist = 'f' and observation.id != observation.source_id;
update observation set license_id = c.license_id from checklists c where observation.source_id = c.id and  observation.is_checklist = 't' and observation.id = observation.source_id;
update observation set license_id = 2 where license_id is null and is_checklist='f' and id=source_id;
alter table observation alter column license_id set not null;

select count(*) from document where license_id is null;
update document set license_id = 2 where license_id is null;
alter table document alter column license_id set not null;

ALTER TABLE checklists DROP COLUMN license_id;

alter table suser add column send_digest boolean;
alter table suser add column fb_profile_pic character varying(255) ;
update suser set send_digest  = false;


alter table observation add column is_locked  boolean;
update observation set is_locked = false;


//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// AFTER RUNNING MIGRATION SCRIPT //////////////////
///////////////////////////////////////////////////////////////////////////////////////////

update species_field_contributor set attributors_idx = 0;
update taxonomy_registry_suser set contributors_idx = 0;

UPDATE resource set context = 'OBSERVATION' where id in (select resource_id from observation_resource);
UPDATE resource set context = 'SPECIES' where id in (select resource_id from species_resource);
UPDATE resource set context = 'SPECIES_FIELD' where id in (select resource_id from species_field_resources);
UPDATE resource set context = 'USER' where id in (select res_id from users_resource);

delete from species_field_contributor where species_field_contributors_id is not null;
alter table species_field_contributor drop column species_field_contributors_id;
drop table species_taxonomy_registry ;

update species_field set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
update resource set uploader_id = 1, upload_time = '1970-01-01 00:00:00' where uploader_id is null;

update synonyms set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
update taxonomy_definition set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
update common_names set uploader_id = 1, upload_time = '1970-01-01 00:00:00';
update taxonomy_registry set uploader_id = 1, upload_time = '1970-01-01 00:00:00';

delete from species_field_license where species_field_licenses_id in (select id from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy'));;
delete from species_field_contributor where species_field_attributors_id in (select id from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy'));
delete from species_field where field_id in (select id from field where category='Author Contributed Taxonomy Hierarchy');


/////////////////////////////on bhutanmaps database/////////////////////////////////
create view  observation_locations as SELECT obs.id,
    obs.source,
    obs.species_name,
    obs.topology
   FROM dblink('dbname=biodiv'::text, 'select id, source, species_name, topology from observation_locations'::text) obs(id bigint, source text, species_name character varying(255), topology geometry);

 create view checklist_species_locations as SELECT obs.id,
    obs.source,
    obs.title,
    obs.species_name,
    obs.topology
   FROM dblink('dbname=biodiv'::text, 'select id, source, title, species_name, topology from checklist_species_locations'::text) obs(id bigint, source text, title character varying(255), species_name character varying(255), topology geometry);
   
/////////////////////////////////////////////



