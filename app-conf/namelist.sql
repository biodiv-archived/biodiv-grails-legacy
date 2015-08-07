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
    Delete col hierarchies
 **/

delete from taxonomy_registry_suser where taxonomy_registry_contributors_id in (select id from taxonomy_registry where classification_id = 821);
update taxonomy_registry set parent_taxon_id  = null where classification_id = 821;
delete from taxonomy_registry where id in (select id from taxonomy_registry where classification_id = 821 limit 1000);


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

2. Trinomials rank update using updateRanks() in namelist.groovy, sheet provided by Thomas (trinomialsFinal.csv).

3. Add IBP hierarchy using addIBPTaxonHie() in namelist.groovy.

4. PreProcess by Sravanthi
	Whatever code change required, Please revert back after done.

5. Download COL XML if required using Utils.downloadColXml("file directory path") in colReport.groovy, can mention from what id to do or all. 

6. curateAllNames() in namelist.groovy, check what is the last id in tax_def and use that.

7. curateWithManualIds() in namelist.groovy, sheet provided by Thomas (dirtynamesMatchedtoCoLpipesep.csv)

8. addSynonymsFromCOL() in namelist.groovy

9. migrateSynonyms() in namelist.groovy

10. change back code in addIBPHierarchyFromCol() in NamelistService.groovy  before deployment //line number 1207 uncomment, 1213 comment

11. Delete/Merge by Sandeep
	mergeAcceptedName() in checklistObvPost.groovy

12. Names Sync by Sandeep

update recommendation set lowercase_name = lower(name); 
update common_names set lowercase_name = lower(name); 
update taxonomy_definition set lowercase_match_name = lower(canonical_form);

	sync() in checklistObvPost.groovy
	
12.a Rebuilding auto suggestion tree on pamba
    buildTree() in checklistObvPost.groovy
	
13. Snapping of hierarchy by Sravanthi

/**------ Post process
Old Synonyms table and its related stuff can be dropped, as now new table is created SynonymsMerged inherited from Taxonomy Definition
Just check references of synonyms table everywhere

////// STATS

1. Tax def sheet generation and joint with species information
select s.id as speciesID, s.percent_of_info,s.title as species_title, t.* from  taxonomy_definition t left join species s on s.taxon_concept_id = t.id;

2. Hierarchy script
In Namelist.groovy functions - 
    a. incompleteHierarchy()
    b. speciesDetails()

3. Col Multiple Results sheet
In colReport.groovy
    a. Utils.generateColStats("/apps/git/biodiv/col_8May")
*/

/*
 * After Migration clean up steps
 */
1. Add constrain for deltetion in taxonomy_registry.
ALTER TABLE taxonomy_registry DROP CONSTRAINT fk9ded596b7e532be5,
ADD CONSTRAINT fk9ded596b7e532be5 FOREIGN KEY (parent_taxon_id) REFERENCES taxonomy_registry(id) ON DELETE CASCADE;

ALTER TABLE taxonomy_registry_suser DROP CONSTRAINT fk87a93aea76e99a2e,
ADD CONSTRAINT fk87a93aea76e99a2e FOREIGN KEY (taxonomy_registry_contributors_id) REFERENCES taxonomy_registry(id) ON DELETE CASCADE;

2. Drop hir for all raw names. In checklistObvPost.groovy run dropRawHir()

3. Delete names created because of snapping
delete from taxonomy_registry where taxon_definition_id > 420871;
delete from taxonomy_definition_suser where taxonomy_definition_contributors_id > 420871;
delete from accepted_synonym where  synonym_id > 420871;
delete from taxonomy_definition_year where taxonomy_definition_id > 420871;
delete from taxonomy_definition_author where taxonomy_definition_id > 420871;
delete from doc_sci_name where taxon_concept_id > 420871;
delete from taxonomy_definition where id > 420871;

4. Adding/updating hir for 33 accepted working name. 
Copy '/home/sandeept/git/biodiv/app-conf/col-xmls/TaxonomyDefinition' to kk for latest XML
In checklistObvPost.groovy run addColhir()

5. Create new working name with different col id and split the children. In checklistObvPost.groovy run createDuplicateName()

5c. Deleted unnecessary  hierarchies.
select * from taxonomy_registry where parent_taxon_id in (select id from taxonomy_registry where path in ('872_20218_47444_52192_52194_280012','124658_125488_125504_125531_125540_278688','872_874_876_66985_69492_277801','2998_33364_107311_112729_30643_277376','94899_94901_95676_95692_95694_280083','872_874_876_66985_67041_277802','94899_94901_95736_95738_95833_280088','123350','2998_33364_33366_118713_3055_277291','872_78725_79245_79747_276733_79956_280738','2998_33364_33366_3554_30698_279037','872_76313_76340_76418_76538_279950','94899_97001_98845_99000_99324_280115','94899_94901_95120_95146_166380_280266','872_78725_79245_79747_276733_79956_280738'));
delete from taxonomy_registry where path in ('872_20218_47444_52192_52194_280012','124658_125488_125504_125531_125540_278688','872_874_876_66985_69492_277801','2998_33364_107311_112729_30643_277376','94899_94901_95676_95692_95694_280083','872_874_876_66985_67041_277802','94899_94901_95736_95738_95833_280088','123350','2998_33364_33366_118713_3055_277291','872_78725_79245_79747_276733_79956_280738','2998_33364_33366_3554_30698_279037','872_76313_76340_76418_76538_279950','94899_97001_98845_99000_99324_280115','94899_94901_95120_95146_166380_280266','872_78725_79245_79747_276733_79956_280738');

6. Delete duplicates.
delete from common_names_suser where common_names_contributors_id in (select id from common_names where taxon_concept_id = 113098); 
delete from common_names where taxon_concept_id = 113098;

In checklistObvPost.groovy run mergeAcceptedName()

7. updating col id for working names
update taxonomy_definition set match_id = 'cbe288fbc700b394526d6aabf07f3fbe' where id = 278329; 
update taxonomy_definition set match_id = '0db9547d1db256a759d1e3436d9e0c5a' where id = 278330; 
update taxonomy_definition set match_id = 'd26f3a70c8ddb7ee45f38dbbfb307331' where id = 277470; 
update taxonomy_definition set match_id = '6a24ded7009e1be4e8d0d63ae8c95b9e' where id = 278944; 
update taxonomy_definition set match_id = '5dae19da1534c9b5104960f259772955' where id = 279018; 
update taxonomy_definition set match_id = '8efabbc738df6fea09ce18141b4fae0c' where id = 280475; 
update taxonomy_definition set match_id = 'ef9e5fbbfb2b09301c00a8cb6b44bcd0' where id = 281524;

7a. In checklistObvPost.groovy run updateColId()

8. For verification :  In checklistObvPost.groovy run createInputFile()


/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// 29th july 2015 ////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////


ALTER TABLE synonyms_suser DROP CONSTRAINT fkc2e9df97c09419c5,
ADD CONSTRAINT fkc2e9df97c09419c5 FOREIGN KEY (synonyms_contributors_id) REFERENCES synonyms(id) ON DELETE CASCADE;

ALTER TABLE common_names_suser DROP CONSTRAINT fka5241eb35d2d07c2,
ADD CONSTRAINT fka5241eb35d2d07c2 FOREIGN KEY (common_names_contributors_id) REFERENCES common_names(id) ON DELETE CASCADE;

update taxonomy_definition set is_deleted = false where id = 276110;

delete from taxonomy_registry where taxon_definition_id = 39867;
delete from taxonomy_definition_suser where taxonomy_definition_contributors_id = 39867;
delete from accepted_synonym where  accepted_id = 39867;
delete from taxonomy_definition_year where taxonomy_definition_id = 39867;
delete from taxonomy_definition_author where taxonomy_definition_id = 39867;
delete from doc_sci_name where taxon_concept_id = 39867;
delete from common_names where taxon_concept_id = 39867;
delete from synonyms where taxon_concept_id = 39867;
delete from recommendation where taxon_concept_id = 39867;
delete from taxonomy_definition where id = 39867;;

0. In checklistObvPost.groovy run updateColId()
1. In checklistObvPost.groovy run mergeAcceptedName()
2. In checklistObvPost.groovy run mergeSynonym()
3. In checklistObvPost.groovy run createDuplicateName()
4. Remove accepted raw name marked as deleted 
	In checklistObvPost.groovy run removeIsDeletedRawName()
5. In checklistObvPost.groovy run addIBPHirToRawNames()
6. Remove col hier for all working names and copy IBP hir as col hir.
	In checklistObvPost.groovy run copyIbpHir()

	
	
////////////////////// 6th Aug 2015 ///////////////

1. In checklistObvPost.groovy run deleteName()
2. In checklistObvPost.groovy run updateNameAndCreateIbpHir()
3. In checklistObvPost.groovy run createNameAndAddIBPHir()
4. In checklistObvPost.groovy run migSyn() 