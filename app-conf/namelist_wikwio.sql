/* sql for namelist changes added on 19th nov */

ALTER TABLE taxonomy_definition ALTER COLUMN canonical_form SET NOT NULL;

//adding place for super-family rank
update taxonomy_definition set rank = 9 where rank = 8 ;
update taxonomy_definition set rank = 8 where rank = 7 ;
update taxonomy_definition set rank = 7 where rank = 6 ;
update taxonomy_definition set rank = 6 where rank = 5 ;
update taxonomy_definition set rank = 5 where rank = 4 ;

//add columns to common name, synonyms and taxon def
ALTER TABLE common_names ADD COLUMN  transliteration varchar(255);
ALTER TABLE common_names ADD COLUMN  status varchar(255);
ALTER TABLE common_names ADD COLUMN  position varchar(255);
ALTER TABLE common_names ADD COLUMN  author_year varchar(255);
ALTER TABLE common_names ADD COLUMN  match_database_name varchar(255);
ALTER TABLE common_names ADD COLUMN  match_id varchar(255);
ALTER TABLE common_names ADD COLUMN  ibp_source varchar(255);
ALTER TABLE common_names ADD COLUMN  via_datasource varchar(255);

update common_names set status = 'COMMON';
update  common_names set position = 'RAW';


ALTER TABLE taxonomy_definition ADD COLUMN  status varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  position varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  author_year varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  match_database_name varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  match_id varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  ibp_source varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  via_datasource varchar(255);

update  taxonomy_definition set status = 'ACCEPTED';
update  taxonomy_definition set position = 'RAW';


ALTER TABLE synonyms ADD COLUMN  status varchar(255);
ALTER TABLE synonyms ADD COLUMN  position varchar(255);
ALTER TABLE synonyms ADD COLUMN  author_year varchar(255);
ALTER TABLE synonyms ADD COLUMN  match_database_name varchar(255);
ALTER TABLE synonyms ADD COLUMN  match_id varchar(255);
ALTER TABLE synonyms ADD COLUMN  ibp_source varchar(255);
ALTER TABLE synonyms ADD COLUMN  via_datasource varchar(255);

update  synonyms set status = 'SYNONYM';
update  synonyms set position = 'RAW';

//added on 25th Feb 2015
ALTER TABLE taxonomy_definition add column is_flagged boolean;
update taxonomy_definition set is_flagged = false;

////////////////**SYNONYM Migration**//////////////

RUN-APP to create SynonymsMerged table;
then run these sqls

//12th March 2015
//Synonyms migration to new table
ALTER TABLE taxonomy_definition ADD COLUMN class varchar(255);
update taxonomy_definition set class = 'species.TaxonomyDefinition';
alter table taxonomy_definition alter column class set not null;

/**if tax_def table does not have relationship column**/
ALTER TABLE taxonomy_definition ADD COLUMN relationship varchar(255);

/**Drop unique constraint**/
ALTER TABLE taxonomy_definition DROP CONSTRAINT taxonomy_definition_rank_key;

////////////////**SYNONYM Migration**//////////////

/**Adding flagging reason column**/
ALTER TABLE taxonomy_definition DROP COLUMN flagging_reason;
ALTER TABLE taxonomy_definition ADD COLUMN flagging_reason varchar(1500);

//////////////////**OBSERVATION RECOMMENDATION**///////////////////
//added on 8th april 2015
ALTER TABLE recommendation add column is_flagged boolean;
update recommendation set is_flagged = false;
ALTER TABLE recommendation ALTER COLUMN flagging_reason type varchar(1500);
alter table activity_feed alter column activity_descrption type varchar(2000);

ALTER TABLE taxonomy_definition ADD COLUMN no_ofcolmatches int;

ALTER TABLE taxonomy_definition add column is_deleted boolean;
update taxonomy_definition set is_deleted = false;

alter table taxonomy_definition drop column dirty_list_reason;
alter table taxonomy_definition add column dirty_list_reason  varchar(1000);



alter table synonyms add column drop_reason  varchar(500);

update taxonomy_definition set no_ofcolmatches = -99;
update taxonomy_definition set position = NULL;

/**
 *  Adding optimized column for case insenstive match
 */
ALTER TABLE taxonomy_definition ADD COLUMN  lowercase_match_name varchar(255);
ALTER TABLE recommendation  ADD COLUMN  lowercase_name varchar(255);
ALTER TABLE common_names  ADD COLUMN  lowercase_name varchar(255);

update recommendation set lowercase_name = lower(name); 
update common_names set lowercase_name = lower(name); 
update taxonomy_definition set lowercase_match_name = lower(canonical_form); 

CREATE INDEX taxonomy_definition_lowercase_match_name ON taxonomy_definition(lowercase_match_name);
CREATE INDEX recommendation_lowercase_name ON recommendation(lowercase_name);
CREATE INDEX common_names_lowercase_name ON common_names(lowercase_name);


////////////////////////////////////// ENDS NAMELIST ///////////////////////////////////////////////

//All files under /apps/git/biodiv/namelist
//All logs under /apps/git/biodiv/namelist/logs

1. Add IBP and col hierarchy using addIBPTaxonHie() in namelist_wikwio.groovy.

2. Download COL XML if required using Utils.downloadColXml("file directory path") in colReport.groovy, can mention from what id to do or all. 

3. curateAllNames() in namelist_wikwio.groovy

4. addSynonymsFromCOL() in namelist_wikwio.groovy

5. migrateSynonyms() in namelist_wikwio.groovy

update recommendation set lowercase_name = lower(name); 
update common_names set lowercase_name = lower(name); 
update taxonomy_definition set lowercase_match_name = lower(canonical_form);

/*
 * After Migration clean up steps
 */
1. Add constrain for deltetion in taxonomy_registry.
ALTER TABLE taxonomy_registry DROP CONSTRAINT fk9ded596b7e532be5,
ADD CONSTRAINT fk9ded596b7e532be5 FOREIGN KEY (parent_taxon_id) REFERENCES taxonomy_registry(id) ON DELETE CASCADE;

ALTER TABLE taxonomy_registry_suser DROP CONSTRAINT fk87a93aea76e99a2e,
ADD CONSTRAINT fk87a93aea76e99a2e FOREIGN KEY (taxonomy_registry_contributors_id) REFERENCES taxonomy_registry(id) ON DELETE CASCADE;

2. Drop hir for all raw names. In checklistObvPost.groovy run dropRawHir()


/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// 29th july 2015 ////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////


ALTER TABLE synonyms_suser DROP CONSTRAINT fkc2e9df97c09419c5,
ADD CONSTRAINT fkc2e9df97c09419c5 FOREIGN KEY (synonyms_contributors_id) REFERENCES synonyms(id) ON DELETE CASCADE;

ALTER TABLE common_names_suser DROP CONSTRAINT fka5241eb35d2d07c2,
ADD CONSTRAINT fka5241eb35d2d07c2 FOREIGN KEY (common_names_contributors_id) REFERENCES common_names(id) ON DELETE CASCADE;

/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// 29 OCT 2015 ////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////// FOR WIKWIO AND BHUTAN //////////////////////////////
1. copyIbpHir() in checklistObvPost.groovy
2. addIBPHirToRawNames()  in checklistObvPost.groovy