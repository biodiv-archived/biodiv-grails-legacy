import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.TaxonomyRegistry
import species.TaxonomyDefinition

import species.namelist.Utils
import species.Classification
import species.ScientificName
import species.SynonymsMerged
import species.Synonyms
import species.participation.Recommendation;
import species.participation.TestA;
import species.participation.TestC;
import species.participation.TestB;
import groovy.sql.Sql;
import species.NamesParser;
import species.auth.SUser;
import species.NamesMetadata;
import species.ScientificName.RelationShip

import groovy.io.FileType

nSer = ctx.getBean("namelistService");
import species.Language;

def migrate(){
    nSer.populateInfoFromCol(new File('col_feb24'));
	println "done "
}

def migrateFromDir(domainSourceDir) {
    domainSourceDir.eachFileRecurse (FileType.FILES) { file ->
        curateName(file.name.replace('.xml','').toLong())
    }
}

def curateName(taxonId, domainSourceDir) {
    List colData = nSer.processColData(new File(domainSourceDir, taxonId+'.xml'));
    if(colData) {
        ScientificName sciName = TaxonomyDefinition.get(taxonId);
        nSer.curateName(sciName, colData);
    } else {
        println "=====NO COL DATA === " 
    }
    
}

File domainSourceDir = new File("/home/rahulk/git/biodiv/col_27mar/TaxonomyDefinition");
//File domainSourceDir = new File("/apps/git/biodiv/col_27mar/TaxonomyDefinition");
//migrate()
//migrateFromDir(domainSourceDir);
curateName(182038, domainSourceDir);

def updatePosition(){
    println "====update status called=";
    ScientificName sciName = TaxonomyDefinition.get(7L)
    //nSer.updatePosition(sciName, NamesMetadata.NamePosition.WORKING);
    sciName.position = NamesMetadata.NamePosition.DIRTY
    sciName.save(flush:true)
    println "===pos=== " + sciName.position ; 
}
//updatePosition()

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

def addIBPTaxonHie() {
    println "====ADDING IBP TAXON HIERARCHY======"
    def cl = new Classification();
    cl.name = "Test Hierarchy";
    cl.language = Language.read(205L);
    if(!cl.save(flush:true)) {
        cl.errors.allErrors.each { println it }
    }
    println "====DONE======"
}

//addIBPTaxonHie();

/*
#alter table classification alter column language_id drop not null;
#UPDATE classification set language_id = 205 where id = (whatever id it gets);
#alter table classification alter column language_id set not null;
*/

def createMapping(xid, yid) {
    def utilsService = ctx.getBean("utilsService");
    utilsService.cleanUpGorm(true);
    def x = TestA.get(xid);
    def y = TestA.get(yid);
    y.addToTestas(x)
    if(!y.save(flush:true)){
        y.errors.allErrors.each { println  it }
    }
}

def createTestEntry(){
    println "=====START=="
    /*def x, y;
    //x = TestA.read(3L);
    x = new TestA(name:'Strand77');
    //x.id = 100L ;
    if(!x.save(flush:true)){
        x.errors.allErrors.each { println  it }
    }
    def dataSoruce = ctx.getBean("dataSource");
	def sql =  Sql.newInstance(dataSoruce);
    sql.execute("update testa set class = 'species.participation.TestB' where id = 7");
    //x.refresh();
    def utilsService = ctx.getBean("utilsService");
    utilsService.cleanUpGorm(true); 
    
    println "===new instance== " + TestB.read(7L) //+ " ==== "+TestB.read(4L).class + " =====" +TestB.read(4L).address;
    //x = new TestA(name:'Strand77');
    //x.id = 100L ;
    /*if(!x.save(flush:true)){
        x.errors.allErrors.each { println  it }
    }*/
    
    /*y = new TestB(id:100L, name:'heaven55', address:'WSWS55');
    if(!y.save(flush:true)){
        y.errors.allErrors.each { println  it }
    }
    */
    //def a = TestA.get(x.id);
    /*println "====A=== " + x
    //def b = TestB.get(y.id)
    println "====B=== " + y
    def c = new TestC(a:x, b:y)
    if(!c.save(flush:true)){
        c.errors.allErrors.each { println  it }
    }
    println "===C==" + c
    c.getAs(y);
    */
    println "=============== " + TaxonomyDefinition.read(286782L)
    println "====END =="
}

//createTestEntry();

def migrateSynonyms() {
    int limit = 50, offset = 0, insert_check = 0,exist_check = 0;
    int counter = 0;
    def nonParsedSyns = [];
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit //+ " =========COUNTER ====== " + conter;    
        def oldSynList = Synonyms.list (max: limit , offset:offset);
        //def oldSynList = Synonyms.read(39985L) //(max: limit , offset:offset);
        def synMer;
        for(oldSyn in oldSynList) {
            def flag = false;
            SynonymsMerged.withNewTransaction {
                println "=====WORKING ON THIS SYNONYM============== " + oldSyn + " =========COUNTER ====== " + counter;
                counter++;
                synMer = new SynonymsMerged();
                synMer.name = oldSyn.name
                synMer.relationship = oldSyn.relationship
                //oldSyn.taxonConcept
                if (!oldSyn.canonicalForm){
                    println "======MISSING CANONICAL FORM - Parsing to fetch ====="
                    NamesParser namesParser = new NamesParser();
                    def parsedNames = namesParser.parse([oldSyn.name]);
                    if(!parsedNames[0]?.canonicalForm) {
                        nonParsedSyns.add(oldSyn.id);
                        println "===COULD NOT PARSE copying its name in other name variants==="
                        oldSyn.canonicalForm = oldSyn.name;
                        oldSyn.normalizedForm = oldSyn.name;
                        oldSyn.italicisedForm = oldSyn.name;
                        oldSyn.binomialForm = oldSyn.name;
                        flag = true;
                    }
                    if(!flag && parsedNames[0]?.canonicalForm) {
                        oldSyn.canonicalForm = parsedNames[0].canonicalForm;
                        oldSyn.normalizedForm = parsedNames[0].normalizedForm;
                        oldSyn.italicisedForm = parsedNames[0].italicisedForm;
                        oldSyn.binomialForm = parsedNames[0].binomialForm;
                    }
                }
                if(!flag) {
                    synMer.canonicalForm = oldSyn.canonicalForm
                    synMer.normalizedForm = oldSyn.normalizedForm
                    synMer.italicisedForm = oldSyn.italicisedForm
                    synMer.binomialForm = oldSyn.binomialForm
                    synMer.status = oldSyn.status
                    synMer.viaDatasource = oldSyn.viaDatasource
                    synMer.uploadTime = oldSyn.uploadTime

                    synMer.uploader = oldSyn.uploader
                    synMer.authorYear = oldSyn.authorYear
                    synMer.ibpSource = oldSyn.ibpSource
                    synMer.matchId = oldSyn.matchId
                    synMer.matchDatabaseName = oldSyn.matchDatabaseName
                    synMer.rank = oldSyn.taxonConcept.rank;
                    oldSyn.contributors.each {
                        synMer.addToContributors(it);
                    }         
                    //save new syn merged
                    if(!synMer.save(flush:true)) {
                        synMer.errors.each { println it }
                    }
                    println "========SYN MERGED ======= " + synMer
                }
            }
            if(!flag) {
                SynonymsMerged.withNewTransaction {
                    oldSyn.taxonConcept.addSynonym(synMer);
                    //oldSyn.taxonConcept.addToSynonyms(synMer);
                }
            }
        }
        offset = offset + limit; 
        //utilsService.cleanUpGorm(true); 
        if(!oldSynList) break;  
    }
    println "=======NON PARSED IDS ===== " + nonParsedSyns
}

//migrateSynonyms();

def createTaxons() {
    taxSer = ctx.getBean("taxonService");
    def classification = Classification.read(2L);
    def taxonRegistryNames = ['Animalia', 'Arthropoda', 'Insecta' ,'Coleoptera', '','Scarabaeidae','Scarabaeinae', 'Catharsius','','Catharsius granulatus (Sharp, 1875)', ''];
    def otherParams = [:]
    def metadata = ['source':'script', 'via': 'script', 'authorString':'(Sharp, 1875)'];
    otherParams.metadata = metadata;
    otherParams['nameStatus'] = 'accepted';
    SUser contributor = SUser.read(1L) //findByName('admin');
    def result = taxSer.addTaxonHierarchy(taxonRegistryNames.last() , taxonRegistryNames, classification, contributor, null, false, true, otherParams);
}

//createTaxons()

def createSynonyms() {
    def synMer = new SynonymsMerged();
    synMer.name = "Catharsius granulatus";
    synMer.relationship = RelationShip.SYNONYM 
    synMer.canonicalForm = "Catharsius granulatus"
    synMer.normalizedForm = "Catharsius granulatus"
    synMer.italicisedForm = "<i>Catharsius granulatus</i>"
    synMer.binomialForm = "Catharsius granulatus"
    synMer.status = NamesMetadata.NameStatus.SYNONYM
    synMer.viaDatasource = ""
    synMer.uploadTime = new Date()
    def contributor = SUser.read(1L);
    synMer.uploader = contributor
    synMer.authorYear = ""
    synMer.ibpSource = null
    synMer.matchId = ""
    synMer.matchDatabaseName = ""
    synMer.rank = 9;
    synMer.addToContributors(contributor);
    
    //create taxon
    //add it as synonym to that taxon;

    createTaxons();
    def x = TaxonomyDefinition.list(sort: "id", order: "desc").first();
    if(!synMer.save(flush:true)) {
        synMer.errors.each { println it }
    }
    x.addSynonym(synMer)
}

//createSynonyms()

def createAcceptedSynonym(){
    def x = TaxonomyDefinition.get(58L)
    def y = SynonymsMerged.get(59L)
    x.addSynonym(y);
}
//createAcceptedSynonym()

def curateAllNames() {
    int limit = 50, offset = 0;
    int counter = 0;
    while(offset < 10000){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def taxDefList;
        TaxonomyDefinition.withNewTransaction {
            def c = TaxonomyDefinition.createCriteria()
            taxDefList = c.list (max: limit , offset:offset) {
                and {
                    order('rank','asc')
                    order('id','asc')                    
                }
            }
        for(taxDef in taxDefList) {
		    TaxonomyDefinition.withNewSession {
                println "=====WORKING ON THIS TAX DEF============== " + taxDef + " =========COUNTER ====== " + counter;
                counter++;
                //File domainSourceDir = new File("/apps/git/biodiv/col_27mar/TaxonomyDefinition");
                File domainSourceDir = new File("/home/rahulk/git/biodiv/col_Mar20/TaxonomyDefinition");
                curateName(taxDef.id, domainSourceDir);
            }
        }
        offset = offset + limit; 
        //utilsService.cleanUpGorm(true); 
        if(!taxDefList) break;  
    }
    println "======NUM OF TIMES SEARCH IBP CALLED==== " + nSer.SEARCH_IBP_COUNTER;
    println "======CAN_ZERO==== " + nSer.CAN_ZERO;
    println "======CAN_SINGLE==== " + nSer.CAN_SINGLE;
    println "======CAN_MULTIPLE==== " + nSer.CAN_MULTIPLE;
    println "======AFTER_CAN_MULTI_ZERO==== " + nSer.AFTER_CAN_MULTI_ZERO;
    println "======AFTER_CAN_MULTI_SINGLE==== " + nSer.AFTER_CAN_MULTI_SINGLE;
    println "======AFTER_CAN_MULTI_MULTI==== " + nSer.AFTER_CAN_MULTI_MULTI;
}

//curateAllNames()

def curateRecoName() {
    println "=======SCRIPT FOR RECO NAMES======"
    int limit = 10, offset = 0;
    int counter = 0;
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit   
        def query = "from Recommendation as r where r.isScientificName = true and r.taxonConcept = null order by r.id"
        def recoList = Recommendation.findAll(query, [max: limit, offset: offset])
        //def recoList = Recommendation.get(316572L)
        
        for(reco in recoList) {
		    Recommendation.withNewTransaction {
                println "=====WORKING ON THIS RECO============== " + reco + " =========COUNTER ====== " + counter;
                counter++;
                def recoName = reco.name.replaceAll('&', '');
                List colData = nSer.searchCOL(recoName, "name");
                nSer.curateRecoName(reco, colData)
            }
        }
        offset = offset + limit; 
        //utilsService.cleanUpGorm(true); 
        if(!recoList) break;  
    }
}
//curateRecoName()

