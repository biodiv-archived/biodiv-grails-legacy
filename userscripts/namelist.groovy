import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.TaxonomyRegistry

import species.namelist.Utils
import species.Classification
/*
def startDate = new Date()
println "======== started " 
species.namelist.Utils.generateColStats("/home/sandeept/col")
println "======== finish date " + new Date() + "    start date "  + startDate  
*/
/*
def testNav(){
	def tSer = ctx.getBean("taxonService")
	GrailsParameterMap m = new GrailsParameterMap([:], null)
	m.taxonId = "3004"
	m.classificationId = "817"
	println "Res === " + tSer.getNodeChildren(m)
}

ctx.getBean("taxonService").addLevelToTaxonReg()

// #---ALTER TABLE taxonomy_registry ADD COLUMN level integer;

ALTER TABLE taxonomy_definition ALTER COLUMN canonical_form SET NOT NULL;

//addin place for super-family rank
update taxonomy_definition set rank = 9 where rank = 8 ;
update taxonomy_definition set rank = 8 where rank = 7 ;
update taxonomy_definition set rank = 7 where rank = 6 ;
update taxonomy_definition set rank = 6 where rank = 5 ;
update taxonomy_definition set rank = 5 where rank = 4 ;

//moved taxonomy rank and relationship to scientific name class. changes requried to change import of this in all groovy and gsp files.

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
update  common_names set position = 'DIRTY';


ALTER TABLE taxonomy_definition ADD COLUMN  status varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  position varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  author_year varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  match_database_name varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  match_id varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  ibp_source varchar(255);
ALTER TABLE taxonomy_definition ADD COLUMN  via_datasource varchar(255);

update  taxonomy_definition set status = 'ACCEPTED';
update  taxonomy_definition set position = 'DIRTY';


ALTER TABLE synonyms ADD COLUMN  status varchar(255);
ALTER TABLE synonyms ADD COLUMN  position varchar(255);
ALTER TABLE synonyms ADD COLUMN  author_year varchar(255);
ALTER TABLE synonyms ADD COLUMN  match_database_name varchar(255);
ALTER TABLE synonyms ADD COLUMN  match_id varchar(255);
ALTER TABLE synonyms ADD COLUMN  ibp_source varchar(255);
ALTER TABLE synonyms ADD COLUMN  via_datasource varchar(255);

update  synonyms set status = 'SYNONYM';
update  synonyms set position = 'DIRTY';

*/
/*
def addIBPTaxonHie() {
    println "====ADDING IBP TAXON HIERARCHY======"
    def cl = new Classification();
    cl.name = "IBP Taxonomy Hierarchy";
    if(!cl.save(flush:true)) {
        cl.errors.allErrors.each { log.error it }
    }
    println "====DONE======"
}

addIBPTaxonHie();
*/
/*
#alter table classification alter column language_id drop not null;
#UPDATE classification set language_id = 205 where id = (whatever id it gets);
#alter table classification alter column language_id set not null;
*/