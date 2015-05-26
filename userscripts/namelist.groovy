import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.TaxonomyRegistry
import species.TaxonomyDefinition

import species.namelist.Utils
import species.Classification
import species.ScientificName
import species.SynonymsMerged
import species.SpeciesField
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
import species.NamesMetadata.NamePosition;
import groovy.io.FileType
import java.nio.file.*

nSer = ctx.getBean("namelistService");
utilsService = ctx.getBean("utilsService");

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
        ScientificName sciName = TaxonomyDefinition.get(taxonId);
        sciName.noOfCOLMatches = 0;
        sciName.position = NamesMetadata.NamePosition.DIRTY;
        sciName.dirtyListReason = "NO XML - NO COL DATA"
        if(!sciName.hasErrors() && sciName.save(flush:true)) {
        } else {
            sciName.errors.allErrors.each { log.error it }
        }
        println "=====NO COL DATA === " 
    }
}

//File domainSourceDir = new File("/home/rahulk/git/biodiv/col_27mar/TaxonomyDefinition");
//File domainSourceDir = new File("/apps/git/biodiv/col_27mar/TaxonomyDefinition");
//File domainSourceDir = new File("/apps/git/biodiv/col_21April_2015checklist/TaxonomyDefinition");
//File domainSourceDir = new File("/home/rahulk/col_8May/TaxonomyDefinition");
File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition");
//migrate()
//migrateFromDir(domainSourceDir);
//curateName(156829, domainSourceDir);

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
    cl.name = "IBP Taxonomy Hierarchy";
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
        def oldSynList = Synonyms.list (max: limit , offset:offset, , sort: "id", order: "asc");
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
                    synMer.oldId = "syn_"+oldSyn.id.toString();
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
    int limit = 67010, offset = 0;
    int counter = 0;
    List curatingThese = [];
    File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition");
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def taxDefList;
        TaxonomyDefinition.withNewTransaction {
            def c = TaxonomyDefinition.createCriteria()
            taxDefList = c.list (max: limit , offset:offset) {
                and {
                    lt('id', 275703L)
                    //eq('position', NamesMetadata.NamePosition.WORKING)
                    //isNull('position')
                }
                order('rank','asc')
                order('id','asc')                    
            }
        }
        for(taxDef in taxDefList) {
		    TaxonomyDefinition.withNewSession {
                println "###############################################################################################"
                println "#"
                println "=====WORKING ON THIS TAX DEF============== " + taxDef + " =========COUNTER ====== " + counter;
                counter++;
                //File domainSourceDir = new File("/apps/git/biodiv/col_27mar/TaxonomyDefinition");
                //File domainSourceDir = new File("/apps/git/biodiv/col_21April_2015checklist/TaxonomyDefinition");
                //File domainSourceDir = new File("/home/rahulk/col_8May/TaxonomyDefinition");
                //curatingThese.add(taxDef.id);
                curateName(taxDef.id, domainSourceDir);
            }
        }

        offset = offset + limit; 
        utilsService.cleanUpGorm(true); 
        if(!taxDefList) break;  
    }
    println "======NUM OF TIMES SEARCH IBP CALLED==== " + nSer.SEARCH_IBP_COUNTER;
    println "======CAN_ZERO==== " + nSer.CAN_ZERO;
    println "======CAN_SINGLE==== " + nSer.CAN_SINGLE;
    println "======CAN_MULTIPLE==== " + nSer.CAN_MULTIPLE;
    println "======AFTER_CAN_MULTI_ZERO==== " + nSer.AFTER_CAN_MULTI_ZERO;
    println "======AFTER_CAN_MULTI_SINGLE==== " + nSer.AFTER_CAN_MULTI_SINGLE;
    println "======AFTER_CAN_MULTI_MULTI==== " + nSer.AFTER_CAN_MULTI_MULTI;
    //println "======== CURATING THESE ===== " + curatingThese
    //println "======== CURATING THESE ===== " + curatingThese.size()
    //println "======== NAMES IN WKG ===== " + nSer.namesInWKG
    //println "========SIZE NAMES IN WKG ===== " + nSer.namesInWKG.size()

    //println "===========NAMES BEFORE === " + nSer.namesBeforeSave
    //println "===========NAMES AFTER === " + nSer.namesAfterSave
    def fiRes = []
    nSer.namesBeforeSave.each { key, value ->
        if(value != nSer.namesAfterSave[key]) {
            println "NAMES AFTER SAVE ----------- " + nSer.namesAfterSave[key]
            fiRes.add(key)
        }
    }

    println "=======FI RES ===== " + fiRes
    println "=======FI RES size ===== " + fiRes.size()

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

def speciesDetails() {
    int limit = 50, offset = 0;
    int counter = 0;
    spUpSer = ctx.getBean("speciesUploadService");
    def columns  = spUpSer.getDataColumnsMap(Language.read(205L));

    File reportFile = new File("/home/rahulk/species_details.csv")
    if(reportFile.exists()){
        reportFile.delete()
        reportFile.createNewFile()
    }
    String str = "Species Id|Species Title|Species Percent Info|Classification";
    columns.each { key,value ->
        str = str + "|" + value;
    }
    str = str + "\n";
    reportFile << str;
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def spList;
        Species.withNewTransaction {
            spList = Species.list (max: limit , offset:offset, sort: 'id', order: "asc")
        }
        for(sp in spList) {
            println "=====WORKING ON THIS SPECIES============== " + sp + " =========COUNTER ====== " + counter;
            counter++;
            StringBuilder sb = new StringBuilder();
            sb.append(sp.id + "|");
            sb.append(sp.title + "|");
            sb.append(sp.percentOfInfo + "|");
            def path = "";
            def authClass = Classification.read(817L);
            def reg = sp.taxonConcept.longestParentTaxonRegistry(authClass);
            def taxonList = reg.authClass
            if(taxonList.size() > 0) {
                taxonList.each{
                    path += it.name + " -> "
                }
            } else {
                def iucnClass = Classification.read(819L);
                reg = sp.taxonConcept.longestParentTaxonRegistry(iucnClass);
                taxonList = reg.iucnClass
                if(taxonList.size() > 0) {
                    taxonList.each{
                        path += it.name + " -> "
                    }
                } else {
                    def gbifClass = Classification.read(818L);
                    reg = sp.taxonConcept.longestParentTaxonRegistry(gbifClass);
                    taxonList = reg.gbifClass;
                    if(taxonList.size() > 0) {
                        taxonList.each{
                            path += it.name + " -> "
                        }
                    }
                }
            }
            sb.append(path + "|");
            columns.each { key, value ->
                def c = SpeciesField.createCriteria().count {
                    and {
                        eq("species", sp)
                        eq("field", Field.read(key))
                    }
                }
                sb.append(c + "|");
            }
		    reportFile << sb.toString() + "\n";
        }

        offset = offset + limit; 
        if(!spList) break;  
    }
}

//speciesDetails();


def moveFiles() {
    def s = "/home/rahulk/col_8May/TaxonomyDefinition"
    def d = "/home/rahulk/col_8May/TaxonomyDefinitionNew"
    int maxNum = 275702;
    File fd = new File(d)
    if(!fd.exists()){
        fd.mkdirs()
    }
    int counter = 1;
    File domainSourceDir = new File(s);
    domainSourceDir.eachFileRecurse (FileType.FILES) { file ->
        def fileNum = file.name.replace('.xml','').toInteger()
        if(fileNum < maxNum) {
            println "========COUNTER ===== " + counter 
            counter ++
            //copy it to a new directory
            Path sp = Paths.get(s+"/"+ file.name);
            Path dp = Paths.get(d+"/"+ file.name);
            Files.copy(sp,dp); 
        }
    }
}

//moveFiles()

def checkzz() {
List s1 = [872, 874, 876, 878, 880, 882, 884, 886, 888, 895, 897, 901, 904, 907, 910, 912, 918, 920, 924, 928, 930, 933, 935, 937, 942, 947, 951, 953, 958, 962, 964, 967, 970, 974, 981, 983, 985, 988, 990, 993, 996, 1000, 1003, 1007, 1011, 1013, 1019, 1023, 1026, 1029, 1033, 1037, 1039, 1043, 1045, 1050, 1052, 1059, 1061, 1070, 1072, 1074, 1077, 1079, 1082, 1084, 1086, 1089, 1093, 1096, 1099, 1101, 1105, 1108, 1113, 1118, 1120, 1123, 1125, 1127, 1130, 1132, 1135, 1137, 1142, 1144, 1147, 1149, 1154, 1157, 1162, 1168, 1171, 1174, 1177, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1203, 1206, 1209, 1212, 1216, 1219, 1222, 1225, 1228, 1231, 1234, 1240, 1243, 1245, 1248, 1251, 1258, 1260, 1264, 1273, 1279, 1283, 1286, 1289, 1291, 1295, 1297, 1300, 1310, 1312, 2998, 3000, 3002, 3004, 3006, 3008, 3010, 3035, 3037, 3039, 3041, 3053, 3055, 3057, 3059, 3061, 3063, 3065, 3067, 3071, 3075, 3077, 3079, 3085, 3087, 3089, 3091, 3093, 3099, 3101, 3103, 3107, 3109, 3112, 3118, 3126, 3132, 3136, 3142, 3144, 3151, 3154, 3156, 3158, 3169, 3171, 3173, 3175, 3177, 3179, 3181, 3183, 3185, 3203, 3205, 3207, 3209, 3218, 3220, 3223, 3230, 3234, 3236, 3239, 3241, 3243, 3252, 3254, 3256, 3258, 3264, 3266, 3268, 3270, 3276, 3279, 3281, 3297, 3299, 3301, 3303, 3305, 3307, 3314, 3326, 3328, 3330, 3332, 3334, 3338, 3341, 3344, 3352, 3354, 3356, 3358, 3360, 3376, 3378, 3387, 3397, 3405, 3407, 3414, 3419, 3421, 3423, 3427, 3429, 3432, 3434, 3437, 3440, 3442, 3444, 3446, 3452, 3454, 3464, 3466, 3468, 3475, 3477, 3485, 3487, 3489, 3491, 3494, 3496, 3498, 3500, 3507, 3509, 3511, 3514, 3516, 3518, 3520, 3542, 3544, 3546, 3550, 3552, 3554, 3556, 3558, 3560, 3568, 3570, 3581, 3587, 3601, 3603, 3605, 3618, 3620, 3628, 3631, 3635, 3637, 3649, 3651, 3677, 3679, 3681, 3683, 3696, 3698, 3701, 3704, 3706, 3711, 3713, 3726, 3736, 3738, 3740, 3742, 3748, 3753, 3762, 3767, 3770]
def s = [ 3772, 3774, 3776, 3783, 3785, 3801, 3803, 3806, 3808, 3812, 3815, 3821, 3823, 3825, 3827, 3829, 3834, 3836, 3838, 3840, 3852, 3854, 3865, 3868, 3876, 3878, 3883, 3885, 3887, 3889, 3899, 3901, 3903, 3905, 3908, 3910, 3913, 3915, 3922, 3924, 3926, 3928, 3931, 3935, 3937, 3939, 3941, 3943, 3946, 3948, 3962, 3964, 3966, 3968, 3970, 3972, 3975, 3977, 3979, 3981, 3984, 3987, 3989, 3992, 3994, 4002, 4004, 4018, 4020, 4022, 4025, 4027, 4029, 4031, 4033, 4035, 4037, 4050, 4052, 4055, 4058, 4060, 4062, 4069, 4073, 4081, 4085, 4089, 4099, 4110, 4115, 4119, 4124, 4129, 4131, 4135, 4140, 4144, 4149, 4153, 4156, 4164, 4180, 4183, 4185, 4187, 4190, 4192, 4194, 4196, 4204, 4206, 4208, 4215, 4217, 4223, 4226, 4231, 4234, 4237, 4242, 4245, 4247, 4258, 4265, 4274, 4276, 4278, 4280, 4282, 4291, 4293, 4311, 4330, 4335, 4337, 4339, 4343, 4345, 4347, 4349, 4352, 4355, 4357, 4359, 4361, 4363, 4365, 4367, 4369, 4371, 4374, 4376, 4378, 4380, 4385, 4389, 4391, 4393, 4395, 4400, 4402, 4406, 4408, 4410, 4414, 4416, 4423, 4427, 4435, 4446, 4454, 4470, 4477, 4483, 4489, 4499, 4506, 4508, 4519, 4521, 4526, 4528, 4549, 4563, 4579, 4585, 4589, 4592, 4597, 4606, 4608, 4611, 4613, 4619, 4625, 4627, 4633, 4635, 4637, 4642, 4644, 4646, 4648, 4650, 4652, 4663, 4665, 4667, 4671, 4673, 4675, 4686, 4688, 4691, 4693, 4695, 4698, 4701, 4703, 4711, 4713, 4715, 4717, 4719, 4721, 4731, 4733, 4739, 4741, 4744, 4746, 4748, 4750, 4752, 4754, 4756, 4764, 4766, 4787, 4792, 4795, 4799, 4803, 4805, 4807, 4817, 4819, 4827, 4829, 4832, 4836, 4839, 4856, 4867, 4876, 4881, 4883, 4886, 4888, 4893, 4895, 4901, 4903, 4907, 4909, 4919, 4923, 4939, 4941, 4953, 4955, 4957, 4959, 4962, 4964, 4966, 4969, 4974, 4976, 4979, 4981, 4986, 4989, 4994, 4996, 4998, 5001, 5003, 5018, 5020, 5031, 5033, 5035, 5037, 5039, 5042, 5045, 5047, 5049, 5051, 5053, 5055, 5057, 5059, 5061, 5063, 5066, 5075, 5077, 5081, 5083, 5087, 5092, 5094, 5096, 5098, 5100, 5103, 5105, 5107, 5110, 5113, 5115, 5117, 5119, 5121, 5136, 5138, 5148, 5161, 5163, 5174, 5176, 5185, 5187, 5189, 5194, 5196, 5198, 5201, 5203, 5240, 5244, 5246, 5249, 5251, 5262, 5264, 5273, 5275, 5289, 5291, 5298, 5300, 5302, 5307, 5309, 5312, 5314, 5317, 5320, 5322, 5333, 5335, 5337, 5345, 5347, 5349, 5360, 5365, 5367, 5369, 5372, 5374, 5376, 5378, 5380, 5382, 5384, 5400, 5402, 5404, 5408, 5410, 5439, 5441, 5443, 5463, 5465, 5467, 5469, 5471, 5473, 5475, 5477, 5479, 5481, 5483, 5486, 5490, 5492, 5494]
def s2 = [5496, 5504, 5506, 5519, 5523, 5525, 5527, 5530, 5532, 5540, 5542, 5553, 5560, 5572, 5574, 5576, 5578, 5580, 5582, 5585, 5587, 5594, 5596, 5607, 5609, 5611, 5613, 5615, 5620, 5622, 5626, 5628, 5632, 5635, 5639, 5642, 5644, 5651, 5656, 5658, 5664, 5666, 5668, 5670, 5683, 5688, 5690, 5693, 5695, 5707, 5709, 5712, 5714, 5716, 5718, 5720, 5722, 5743, 5745, 5747, 5749, 5755, 5757, 5769, 5771, 5774, 5785, 5790, 5792, 5797, 5799, 5801, 5808, 5810, 5812, 5815, 5817, 5819, 5821, 5823, 5825, 5829, 5831, 5833, 5841, 5843, 5846, 5848, 5856, 5858, 5860, 5863, 5867, 5871, 5873, 5879, 5881, 5890, 5892, 5899, 5920, 5925, 5927, 5929, 5931, 5933, 5935, 5937, 5940, 5942, 5945, 5947, 5950, 5952, 5965, 5967, 5969, 5971, 5974, 5977, 5979, 5981, 5987, 5991, 5996, 5998, 6007, 6009, 6011, 6016, 6018, 6029, 6031, 6033, 6035, 6042, 6044, 6050, 6052, 6054, 6058, 6060, 6062, 6070, 6072, 6078, 6080, 6082, 6086, 6088, 6091, 6101, 6104, 6109, 6118, 6120, 6136, 6138, 6140, 6142, 6148, 6150, 6166, 6168, 6173, 6177, 6182, 6184, 6188, 6190, 6207, 6209, 6221, 6223, 6248, 6250, 6252, 6257, 6259, 6263, 6265, 6267, 6269, 6275, 6277, 6282, 6287, 6289, 6291, 6294, 6301, 6305, 6311, 6313, 6317, 6326, 6341, 6351, 6356, 6367, 6380, 6386, 6391, 6396, 6399, 6403, 6410, 6418, 6420, 6427, 6429, 6432, 6443, 6445, 6452, 6454, 6456, 6479, 6522, 6534, 6536, 6541, 6543, 6545, 6555, 6557, 6576, 6578, 6600, 6602, 6606, 6609, 6611, 6615, 6617, 6619, 6631, 6636, 6638, 6640, 6642, 6650, 6656, 6658, 6698, 6700, 6702, 6707, 6709, 6711, 6713, 6723, 6728, 6730, 6732, 6734, 6738, 6740, 6744, 6752, 6754, 6765, 6773, 6775, 6785, 6787, 6799, 6801, 6803, 6810, 6812, 6817, 6819, 6825, 6827, 20218, 20220, 20222, 20224, 20226, 20228, 20234]


def x1 =[4400, 4406, 4408, 4414, 4385, 4389, 4391, 4393, 6576, 6578, 6543, 6541, 4378, 4376, 6534, 6555, 4355, 4352, 4359, 6557, 4357, 6545, 6636, 6638, 4477, 6631, 6650, 95736, 6640, 6642, 6606, 6602, 6600, 4423, 6619, 6617, 4416, 6615, 6611, 4427, 6609, 6432, 4528, 6443, 4521, 6452, 6454, 4526, 6456, 4519, 4506, 4508, 4499, 6427, 4483, 6429, 118713, 4606, 4592, 4589, 6522, 6479, 4563, 4549, 4131, 4129, 4135, 4144, 30359, 4149, 6305, 4153, 4156, 4099, 6301, 6289, 6291, 6294, 4115, 6287, 4119, 6275, 4124, 4194, 4192, 4206, 4204, 4215, 35021, 4223, 4217, 4164, 4180, 173682, 4190, 4185, 4187, 4265, 4258, 6207, 6177, 4280, 6182, 4274, 6184, 4278, 6188, 4276, 4234, 173738, 4237, 6166, 6168, 149892, 4226, 6173, 4231, 6148, 6150, 4242, 4245, 4247, 6263, 4335, 6257, 6267, 6265, 4349, 4347, 4345, 6252, 4339, 6250, 4337, 6248, 4293, 4291, 6209, 6221, 141288, 6223, 4311, 4886, 4883, 4881, 4895, 4888, 4867, 4876, 4901, 4903, 121943, 4909, 121949, 4907, 107311, 4955, 4953, 4957, 4939, 4941, 4976, 4979, 134832, 4981, 4986, 4989, 4962, 4966, 4969, 30154, 4974, 5020, 5018, 20222, 20220, 20218, 5003, 5001, 4998, 4996, 4994, 5055, 5047, 5042, 5037, 5033, 5031, 5083, 5081, 5087, 5075, 5077, 5066, 5059, 5057, 5063, 5061, 5113, 5117, 175427, 5119, 5110, 5096, 5100, 5103, 5092, 4613, 6812, 4611, 6810, 6801, 6803, 4619, 3004, 4625, 4627, 2998, 6785, 4633, 6787, 4635, 4646, 4644, 4642, 95929, 4650, 4648, 4663, 6827, 6825, 138949, 4671, 4667, 6817, 4665, 4673, 880, 4675, 884, 3055, 3041, 895, 4686, 4688, 144477, 3065, 4691, 4693, 3071, 4695, 872, 3057, 874, 4698, 3059, 876, 3061, 4701, 878, 3063, 4703, 20234, 20226, 4715, 3010, 20224, 4713, 3008, 4719, 20228, 4717, 3035, 4721, 3039, 3037, 4731, 33364, 33366, 4733, 113497, 4748, 958, 4750, 4744, 953, 4746, 4741, 951, 947, 4739, 4764, 942, 4766, 6656, 6658, 933, 4756, 928, 930, 4754, 6711, 924, 6709, 6707, 920, 918, 912, 6713, 4799, 910, 907, 904, 4792, 6702, 901, 6700, 4787, 6698, 897, 6738, 1019, 1023, 117899, 1011, 4803, 1013, 4805, 1000, 6723, 117919, 1003, 4827, 4829, 1007, 993, 4817, 6728, 4819, 6730, 6732, 996, 148341, 985, 990, 988, 6773, 4832, 4839, 4836, 981, 6754, 970, 6752, 4856, 974, 962, 95846, 967, 6765, 964, 1101, 1096, 5496, 3281, 1099, 5492, 1093, 119450, 5494, 1089, 5490, 3268, 1118, 3270, 5486, 1113, 5481, 3264, 5483, 3266, 5477, 1108, 3276, 3279, 5479, 1105, 5473, 5475, 5471, 1132, 5469, 1130, 5467, 1127, 5463, 1125, 1123, 1120, 3303, 3301, 1149, 3299, 1147, 1144, 3307, 5443, 3305, 5441, 1033, 3218, 3220, 1037, 1039, 5439, 1026, 1029, 3230, 1050, 3205, 1052, 3207, 3209, 5408, 1043, 1045, 5402, 5400, 1070, 3254, 3252, 5404, 1059, 3256, 1061, 1082, 3234, 5384, 3239, 1086, 1084, 293749, 3236, 293754, 3243, 1074, 5378, 293752, 3241, 293758, 1079, 293759, 5382, 1077, 293756, 5380, 5620, 1222, 5622, 1216, 293769, 1219, 293764, 1228, 3156, 293765, 5628, 293766, 3158, 293760, 1225, 3154, 293763, 5626, 3151, 5607, 1245, 5613, 293783, 3142, 293782, 5615, 1240, 3136, 5609, 293779, 293778, 1243, 1251, 5587, 5585, 1248, 1260, 1258, 3185, 5574, 3183, 148569, 3181, 3179, 1264, 3177, 5582, 3175, 5580, 3173, 5578, 3171, 5576, 1273, 3169, 139917, 3099, 1154, 1157, 3101, 3103, 3089, 5560, 3091, 3093, 1168, 29443, 1171, 5540, 1174, 5542, 121564, 1177, 1180, 3077, 1183, 3079, 5523, 5527, 1189, 5525, 3132, 1195, 5530, 1192, 1198, 3126, 5532, 5506, 1203, 3112, 5504, 1206, 3118, 3107, 1209, 121594, 5519, 1212, 3109, 119686, 3520, 3542, 5246, 5244, 3546, 3544, 3556, 5196, 3554, 5194, 5189, 5185, 3568, 3570, 211190, 3581, 44678, 5201, 5203, 5163, 5161, 1310, 265670, 1297, 3464, 1300, 3475, 1291, 1289, 1295, 3477, 115425, 1286, 5174, 3487, 3485, 3489, 3491, 3494, 5121, 3496, 3498, 3507, 3509, 3511, 1312, 5136, 3514, 5138, 36000, 3518, 3407, 3405, 5347, 5345, 3397, 3423, 5367, 3421, 5365, 3419, 3414, 5374, 5372, 5369, 3437, 3432, 5312, 121780, 3434, 5314, 5320, 5322, 3427, 5333, 3452, 5335, 113923, 3454, 113925, 3444, 113931, 3446, 5337, 3442, 3338, 3341, 3330, 5291, 5289, 3334, 3332, 3354, 5298, 3358, 3356, 5300, 3344, 5249, 5251, 3360, 5262, 5264, 3387, 5273, 3376, 5275, 3827, 5979, 3825, 5977, 3829, 5981, 3834, 5971, 3838, 3836, 3808, 3815, 5967, 5965, 3812, 5952, 3821, 6009, 6011, 3801, 3803, 3806, 3776, 5996, 3783, 3785, 118924, 5987, 5991, 3767, 3762, 3774, 3772, 3748, 5899, 5890, 3753, 5950, 5945, 3740, 5940, 3742, 5942, 5937, 3738, 5933, 5935, 5929, 5931, 3726, 5920, 3704, 3711, 3698, 6104, 3696, 6109, 3701, 6082, 6080, 6086, 6088, 3681, 6136, 6138, 6140, 44335, 6142, 6118, 6120, 3649, 3651, 6033, 3637, 3635, 3631, 6018, 6016, 6031, 6029, 3618, 6070, 3605, 6078, 3601, 3603, 6052, 6054, 6050, 6060, 6062, 6058, 3587, 5707, 5709, 4073, 5720, 4081, 5722, 4085, 4089, 5714, 5716, 5718, 4035, 4033, 5743, 4037, 4050, 5755, 4055, 4052, 5757, 4058, 5747, 5745, 4062, 4060, 5749, 4004, 5644, 4002, 5642, 5639, 5635, 4020, 4022, 5656, 4018, 5658, 4029, 4031, 4025, 4027, 5651, 3975, 3970, 5670, 3981, 94899, 5668, 3979, 94901, 5666, 3977, 5664, 5695, 3989, 5693, 5690, 3987, 3984, 5688, 3994, 5683, 3992, 5825, 3946, 3948, 5829, 5831, 3937, 3941, 3943, 5841, 5843, 3966, 5846, 5848, 5858, 3915, 5856, 3913, 5863, 5860, 5867, 3905, 5871, 3931, 3928, 3935, 5879, 3922, 5881, 3926, 3924, 3885, 3887, 3883, 3876, 5774, 5769, 5771, 3901, 3903, 3899, 36384, 46567, 36397, 5790, 3889, 5785, 3854, 5799, 3852, 5797, 66985, 3840, 5801, 5815, 3868, 5812, 5810, 3865, 5808, 67001, 5823, 5821, 5819, 5817]


def x2 =[4400, 4406, 4408, 4414, 4385, 4389, 4391, 4393, 6576, 6578, 6543, 6541, 4378, 4376, 6534, 6555, 4355, 4352, 4359, 6557, 4357, 6545, 6636, 6638, 4477, 6631, 6650, 95736, 6640, 6642, 6606, 6602, 6600, 4423, 6619, 6617, 4416, 6615, 6611, 4427, 6609, 6432, 4528, 6443, 4521, 6452, 6454, 4526, 6456, 4519, 4506, 4508, 4499, 6427, 4483, 6429, 118713, 4606, 4592, 4589, 6522, 6479, 4563, 4549, 4131, 4129, 4135, 4144, 30359, 4149, 6305, 4153, 4156, 4099, 6301, 6289, 6291, 6294, 4115, 6287, 4119, 6275, 4124, 4194, 4192, 4206, 4204, 4215, 35021, 4223, 4217, 4164, 4180, 173682, 4190, 4185, 4187, 4265, 4258, 6207, 6177, 4280, 6182, 4274, 6184, 4278, 6188, 4276, 4234, 173738, 4237, 6166, 6168, 149892, 4226, 6173, 4231, 6148, 6150, 4242, 4245, 4247, 6263, 4335, 6257, 6267, 6265, 4349, 4347, 4345, 6252, 4339, 6250, 4337, 6248, 4293, 4291, 6209, 6221, 141288, 6223, 4311, 4886, 4883, 4881, 4895, 4888, 4867, 4876, 4901, 4903, 121943, 4909, 121949, 4907, 107311, 4955, 4953, 4957, 4939, 4941, 4976, 4979, 134832, 4981, 4986, 4989, 4962, 4966, 4969, 30154, 4974, 5020, 5018, 20222, 20220, 20218, 5003, 5001, 4998, 4996, 4994, 5055, 5047, 5042, 5037, 5033, 5031, 5083, 5081, 5087, 5075, 5077, 5066, 5059, 5057, 5063, 5061, 5113, 5117, 175427, 5119, 5110, 5096, 5100, 5103, 5092, 4613, 6812, 4611, 6810, 6801, 6803, 4619, 3004, 4625, 4627, 2998, 6785, 4633, 6787, 4635, 4646, 4644, 4642, 95929, 4650, 4648, 4663, 6827, 6825, 138949, 4671, 4667, 6817, 4665, 4673, 880, 4675, 884, 3055, 3041, 895, 4686, 4688, 144477, 3065, 4691, 4693, 3071, 4695, 872, 3057, 874, 4698, 3059, 876, 3061, 4701, 878, 3063, 4703, 20234, 20226, 4715, 3010, 20224, 4713, 3008, 4719, 20228, 4717, 3035, 4721, 3039, 3037, 4731, 33364, 33366, 4733, 113497, 4748, 958, 4750, 4744, 953, 4746, 4741, 951, 947, 4739, 4764, 942, 4766, 6656, 6658, 933, 4756, 928, 930, 4754, 6711, 924, 6709, 6707, 920, 918, 912, 6713, 4799, 910, 907, 904, 4792, 6702, 901, 6700, 4787, 6698, 897, 6738, 1019, 1023, 117899, 1011, 4803, 1013, 4805, 1000, 6723, 117919, 1003, 4827, 4829, 1007, 993, 4817, 6728, 4819, 6730, 6732, 996, 148341, 985, 990, 988, 6773, 4832, 4839, 4836, 981, 6754, 970, 6752, 4856, 974, 962, 95846, 967, 6765, 964, 1101, 1096, 5496, 3281, 1099, 5492, 1093, 119450, 5494, 1089, 5490, 3268, 3270, 1118, 5486, 1113, 5481, 3264, 5483, 3266, 5477, 3276, 1108, 3279, 5479, 1105, 5473, 5475, 5471, 1132, 5469, 1130, 5467, 1127, 5463, 1125, 1123, 1120, 3303, 3301, 1149, 3299, 1147, 1144, 3307, 5443, 3305, 5441, 1033, 3218, 3220, 1037, 1039, 5439, 1026, 1029, 3230, 1050, 3205, 1052, 3207, 3209, 5408, 1043, 1045, 5402, 5400, 1070, 3254, 3252, 5404, 1059, 3256, 1061, 1082, 3234, 5384, 293750, 3239, 1086, 293751, 1084, 293749, 3236, 293754, 3243, 1074, 293755, 5378, 293752, 3241, 293753, 293758, 1079, 293759, 5382, 1077, 293756, 293757, 5380, 293772, 5620, 293773, 293774, 1222, 5622, 293775, 293768, 1216, 293769, 1219, 293770, 293771, 293764, 1228, 3156, 293765, 5628, 293766, 3158, 293767, 1225, 293760, 293761, 293762, 3154, 293763, 5626, 3151, 5607, 293785, 293784, 293786, 293781, 293780, 1245, 5613, 293783, 3142, 293782, 5615, 293777, 1240, 3136, 293776, 5609, 293779, 293778, 1243, 1251, 5587, 5585, 1248, 1260, 1258, 3185, 5574, 3183, 148569, 3181, 3179, 1264, 3177, 5582, 3175, 5580, 3173, 5578, 3171, 5576, 1273, 3169, 139917, 3099, 1154, 1157, 3101, 3103, 3089, 5560, 3091, 3093, 1168, 29443, 1171, 5540, 1174, 5542, 121564, 1177, 1180, 3077, 1183, 3079, 5523, 5527, 1189, 5525, 3132, 1195, 5530, 1192, 1198, 3126, 5532, 5506, 1203, 3112, 5504, 1206, 3118, 3107, 1209, 121594, 5519, 1212, 3109, 119686, 3520, 3542, 5246, 5244, 3546, 3544, 3556, 5196, 3554, 5194, 5189, 5185, 3568, 3570, 211190, 3581, 44678, 5201, 5203, 5163, 5161, 1310, 265670, 1297, 3464, 1300, 3475, 1291, 1289, 1295, 3477, 115425, 1286, 5174, 3487, 3485, 3489, 3491, 3494, 5121, 3496, 3498, 3507, 3509, 3511, 1312, 5136, 3514, 5138, 36000, 3518, 3407, 3405, 5347, 5345, 3397, 3423, 5367, 3421, 5365, 3419, 3414, 5374, 5372, 5369, 3437, 3432, 5312, 121780, 3434, 5314, 5320, 5322, 3427, 5333, 3452, 5335, 113923, 3454, 113925, 3444, 113931, 3446, 5337, 3442, 3338, 3341, 3330, 5291, 5289, 3334, 3332, 3354, 5298, 3358, 3356, 5300, 3344, 5249, 5251, 3360, 5262, 5264, 3387, 5273, 3376, 5275, 3827, 5979, 3825, 5977, 3829, 5981, 3834, 5971, 3838, 3836, 3808, 3815, 5967, 5965, 3812, 5952, 3821, 6009, 6011, 3801, 3803, 3806, 3776, 5996, 3783, 3785, 118924, 5987, 5991, 3767, 3762, 3774, 3772, 3748, 5899, 5890, 3753, 5950, 5945, 3740, 5940, 3742, 5942, 5937, 3738, 5933, 5935, 5929, 5931, 3726, 5920, 3704, 3711, 3698, 6104, 3696, 6109, 3701, 6082, 6080, 6086, 6088, 3681, 6136, 6138, 6140, 44335, 6142, 6118, 6120, 3649, 3651, 6033, 3637, 3635, 3631, 6018, 6016, 6031, 6029, 3618, 6070, 3605, 6078, 3601, 3603, 6052, 6054, 6050, 6060, 6062, 6058, 3587, 5707, 5709, 4073, 5720, 4081, 5722, 4085, 4089, 5714, 5716, 5718, 4035, 4033, 5743, 4037, 4050, 5755, 4055, 4052, 5757, 4058, 5747, 5745, 4062, 4060, 5749, 4004, 5644, 4002, 5642, 5639, 5635, 4020, 4022, 5656, 4018, 5658, 4029, 4031, 4025, 4027, 5651, 3975, 3970, 5670, 3981, 94899, 5668, 3979, 94901, 5666, 3977, 5664, 5695, 3989, 5693, 5690, 3987, 3984, 5688, 3994, 5683, 3992, 5825, 3946, 3948, 5829, 5831, 3937, 3941, 3943, 5841, 5843, 3966, 5846, 5848, 5858, 3915, 5856, 3913, 5863, 5860, 5867, 3905, 5871, 3931, 3928, 3935, 5879, 3922, 5881, 3926, 3924, 3885, 3887, 3883, 3876, 5774, 5769, 5771, 3901, 3903, 3899, 36384, 46567, 36397, 5790, 3889, 5785, 3854, 5799, 3852, 5797, 66985, 3840, 5801, 5815, 3868, 5812, 5810, 3865, 5808, 67001, 5823, 5821, 5819, 5817]


//on id run
// Total - [872, 874, 876, 878, 880, 882, 884, 886, 888, 895, 897, 901, 904, 907, 910, 912, 918, 920, 924, 928, 930, 933, 935, 937, 942, 947, 951, 953, 958, 962, 964, 967, 970, 974, 981, 983, 985, 988, 990, 993, 996, 1000, 1003, 1007, 1011, 1013, 1019, 1023, 1026, 1029, 1033, 1037, 1039, 1043, 1045, 1050, 1052, 1059, 1061, 1070, 1072, 1074, 1077, 1079, 1082, 1084, 1086, 1089, 1093, 1096, 1099, 1101, 1105, 1108, 1113, 1118, 1120, 1123, 1125, 1127, 1130, 1132, 1135, 1137, 1142, 1144, 1147, 1149, 1154, 1157, 1162, 1168, 1171, 1174, 1177, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1203, 1206, 1209, 1212, 1216, 1219, 1222, 1225, 1228, 1231, 1234, 1240, 1243, 1245, 1248, 1251, 1258, 1260, 1264, 1273, 1279, 1283, 1286, 1289, 1291, 1295, 1297, 1300, 1310, 1312, 2998, 3000, 3002, 3004, 3006, 3008, 3010, 3035, 3037, 3039, 3041, 3053, 3055, 3057, 3059, 3061, 3063, 3065, 3067, 3071, 3075, 3077, 3079, 3085, 3087, 3089, 3091, 3093, 3099, 3101, 3103, 3107, 3109, 3112, 3118, 3126, 3132, 3136, 3142, 3144, 3151, 3154, 3156, 3158, 3169, 3171, 3173, 3175, 3177, 3179, 3181, 3183, 3185, 3203, 3205, 3207, 3209, 3218, 3220, 3223, 3230, 3234, 3236, 3239, 3241, 3243, 3252, 3254, 3256, 3258, 3264, 3266, 3268, 3270, 3276, 3279, 3281, 3297, 3299, 3301, 3303, 3305, 3307, 3314, 3326, 3328, 3330, 3332, 3334, 3338, 3341, 3344, 3352, 3354, 3356, 3358, 3360, 3376, 3378, 3387, 3397, 3405, 3407, 3414, 3419, 3421, 3423, 3427, 3429, 3432, 3434, 3437, 3440, 3442, 3444, 3446, 3452, 3454, 3464, 3466, 3468, 3475, 3477, 3485, 3487, 3489, 3491, 3494, 3496, 3498, 3500, 3507, 3509, 3511, 3514, 3516, 3518, 3520, 3542, 3544, 3546, 3550, 3552, 3554, 3556, 3558, 3560, 3568, 3570, 3581, 3587, 3601, 3603, 3605, 3618, 3620, 3628, 3631, 3635, 3637, 3649, 3651, 3677, 3679, 3681, 3683, 3696, 3698, 3701, 3704, 3706, 3711, 3713, 3726, 3736, 3738, 3740, 3742, 3748, 3753, 3762, 3767, 3770, 3772, 3774, 3776, 3783, 3785, 3801, 3803, 3806, 3808, 3812, 3815, 3821, 3823, 3825, 3827, 3829, 3834, 3836, 3838, 3840, 3852, 3854, 3865, 3868, 3876, 3878, 3883, 3885, 3887, 3889, 3899, 3901, 3903, 3905, 3908, 3910, 3913, 3915, 3922, 3924, 3926, 3928, 3931, 3935, 3937, 3939, 3941, 3943, 3946, 3948, 3962, 3964, 3966, 3968, 3970, 3972, 3975, 3977, 3979, 3981, 3984, 3987, 3989, 3992, 3994, 4002, 4004, 4018, 4020, 4022, 4025, 4027, 4029, 4031, 4033, 4035, 4037, 4050, 4052, 4055, 4058, 4060, 4062, 4069, 4073, 4081, 4085, 4089, 4099, 4110, 4115, 4119, 4124, 4129, 4131, 4135, 4140, 4144, 4149, 4153, 4156, 4164, 4180, 4183, 4185, 4187, 4190, 4192, 4194, 4196, 4204, 4206, 4208, 4215, 4217, 4223, 4226, 4231, 4234, 4237, 4242, 4245, 4247, 4258, 4265, 4274, 4276, 4278, 4280, 4282, 4291, 4293, 4311, 4330, 4335, 4337, 4339, 4343, 4345, 4347, 4349, 4352, 4355, 4357, 4359, 4361, 4363, 4365, 4367, 4369, 4371, 4374, 4376, 4378, 4380, 4385, 4389, 4391, 4393, 4395, 4400, 4402, 4406, 4408, 4410, 4414, 4416, 4423, 4427, 4435, 4446, 4454, 4470, 4477, 4483, 4489, 4499, 4506, 4508, 4519, 4521, 4526, 4528, 4549, 4563, 4579, 4585, 4589, 4592, 4597, 4606, 4608, 4611, 4613, 4619, 4625, 4627, 4633, 4635, 4637, 4642, 4644, 4646, 4648, 4650, 4652, 4663, 4665, 4667, 4671, 4673, 4675, 4686, 4688, 4691, 4693, 4695, 4698, 4701, 4703, 4711, 4713, 4715, 4717, 4719, 4721, 4731, 4733, 4739, 4741, 4744, 4746, 4748, 4750, 4752, 4754, 4756, 4764, 4766, 4787, 4792, 4795, 4799, 4803, 4805, 4807, 4817, 4819, 4827, 4829, 4832, 4836, 4839, 4856, 4867, 4876, 4881, 4883, 4886, 4888, 4893, 4895, 4901, 4903, 4907, 4909, 4919, 4923, 4939, 4941, 4953, 4955, 4957, 4959, 4962, 4964, 4966, 4969, 4974, 4976, 4979, 4981, 4986, 4989, 4994, 4996, 4998, 5001, 5003, 5018, 5020, 5031, 5033, 5035, 5037, 5039, 5042, 5045, 5047, 5049, 5051, 5053, 5055, 5057, 5059, 5061, 5063, 5066, 5075, 5077, 5081, 5083, 5087, 5092, 5094, 5096, 5098, 5100, 5103, 5105, 5107, 5110, 5113, 5115, 5117, 5119, 5121, 5136, 5138, 5148, 5161, 5163, 5174, 5176, 5185, 5187, 5189, 5194, 5196, 5198, 5201, 5203, 5240, 5244, 5246, 5249, 5251, 5262, 5264, 5273, 5275, 5289, 5291, 5298, 5300, 5302, 5307, 5309, 5312, 5314, 5317, 5320, 5322, 5333, 5335, 5337, 5345, 5347, 5349, 5360, 5365, 5367, 5369, 5372, 5374, 5376, 5378, 5380, 5382, 5384, 5400, 5402, 5404, 5408, 5410, 5439, 5441, 5443, 5463, 5465, 5467, 5469, 5471, 5473, 5475, 5477, 5479, 5481, 5483, 5486, 5490, 5492, 5494, 5496, 5504, 5506, 5519, 5523, 5525, 5527, 5530, 5532, 5540, 5542, 5553, 5560, 5572, 5574, 5576, 5578, 5580, 5582, 5585, 5587, 5594, 5596, 5607, 5609, 5611, 5613, 5615, 5620, 5622, 5626, 5628, 5632, 5635, 5639, 5642, 5644, 5651, 5656, 5658, 5664, 5666, 5668, 5670, 5683, 5688, 5690, 5693, 5695, 5707, 5709, 5712, 5714, 5716, 5718, 5720, 5722, 5743, 5745, 5747, 5749, 5755, 5757, 5769, 5771, 5774, 5785, 5790, 5792, 5797, 5799, 5801, 5808, 5810, 5812, 5815, 5817, 5819, 5821, 5823, 5825, 5829, 5831, 5833, 5841, 5843, 5846, 5848, 5856, 5858, 5860, 5863, 5867, 5871, 5873, 5879, 5881, 5890, 5892, 5899, 5920, 5925, 5927, 5929, 5931, 5933, 5935, 5937, 5940, 5942, 5945, 5947, 5950, 5952, 5965, 5967, 5969, 5971, 5974, 5977, 5979, 5981, 5987, 5991, 5996, 5998, 6007, 6009, 6011, 6016, 6018, 6029, 6031, 6033, 6035, 6042, 6044, 6050, 6052, 6054, 6058, 6060, 6062, 6070, 6072, 6078, 6080, 6082, 6086, 6088, 6091, 6101, 6104, 6109, 6118, 6120, 6136, 6138, 6140, 6142, 6148, 6150, 6166, 6168, 6173, 6177, 6182, 6184, 6188, 6190, 6207, 6209, 6221, 6223, 6248, 6250, 6252, 6257, 6259, 6263, 6265, 6267, 6269, 6275, 6277, 6282, 6287, 6289, 6291, 6294, 6301, 6305, 6311, 6313, 6317, 6326, 6341, 6351, 6356, 6367, 6380, 6386, 6391, 6396, 6399, 6403, 6410, 6418, 6420, 6427, 6429, 6432, 6443, 6445, 6452, 6454, 6456, 6479, 6522, 6534, 6536, 6541, 6543, 6545, 6555, 6557, 6576, 6578, 6600, 6602, 6606, 6609, 6611, 6615, 6617, 6619, 6631, 6636, 6638, 6640, 6642, 6650, 6656, 6658, 6698, 6700, 6702, 6707, 6709, 6711, 6713, 6723, 6728, 6730, 6732, 6734, 6738, 6740, 6744, 6752, 6754, 6765, 6773, 6775, 6785, 6787, 6799, 6801, 6803, 6810, 6812, 6817, 6819, 6825, 6827, 20218, 20220, 20222, 20224, 20226, 20228, 20234]

//List -[4400, 4406, 4408, 4414, 4385, 4389, 4391, 4393, 6576, 6578, 6543, 6541, 4378, 4376, 6534, 6555, 4355, 4352, 4359, 6557, 4357, 6545, 6636, 6638, 4477, 6631, 6650, 95736, 6640, 6642, 6606, 6602, 6600, 4423, 6619, 6617, 4416, 6615, 6611, 4427, 6609, 6432, 4528, 6443, 4521, 6452, 6454, 4526, 6456, 4519, 4506, 4508, 4499, 6427, 4483, 6429, 118713, 4606, 4592, 4589, 6522, 6479, 4563, 4549, 4131, 4129, 4135, 4144, 30359, 4149, 6305, 4153, 4156, 4099, 6301, 6289, 6291, 6294, 4115, 6287, 4119, 6275, 4124, 4194, 4192, 4206, 4204, 4215, 35021, 4223, 4217, 4164, 4180, 173682, 4190, 4185, 4187, 4265, 4258, 6207, 6177, 4280, 6182, 4274, 6184, 4278, 6188, 4276, 4234, 173738, 4237, 6166, 6168, 149892, 4226, 6173, 4231, 6148, 6150, 4242, 4245, 4247, 6263, 4335, 6257, 6267, 6265, 4349, 4347, 4345, 6252, 4339, 6250, 4337, 6248, 4293, 4291, 6209, 6221, 141288, 6223, 4311, 4886, 4883, 4881, 4895, 4888, 4867, 4876, 4901, 4903, 121943, 4909, 121949, 4907, 107311, 4955, 4953, 4957, 4939, 4941, 4976, 4979, 134832, 4981, 4986, 4989, 4962, 4966, 4969, 30154, 4974, 5020, 5018, 20222, 20220, 20218, 5003, 5001, 4998, 4996, 4994, 5055, 5047, 5042, 5037, 5033, 5031, 5083, 5081, 5087, 5075, 5077, 5066, 5059, 5057, 5063, 5061, 5113, 5117, 175427, 5119, 5110, 5096, 5100, 5103, 5092, 4613, 6812, 4611, 6810, 6801, 6803, 4619, 3004, 4625, 4627, 2998, 6785, 4633, 6787, 4635, 4646, 4644, 4642, 95929, 4650, 4648, 4663, 6827, 6825, 138949, 4671, 4667, 6817, 4665, 4673, 880, 4675, 884, 3055, 3041, 895, 4686, 4688, 144477, 3065, 4691, 4693, 3071, 4695, 872, 3057, 874, 4698, 3059, 876, 3061, 4701, 878, 3063, 4703, 20234, 20226, 4715, 3010, 20224, 4713, 3008, 4719, 20228, 4717, 3035, 4721, 3039, 3037, 4731, 33364, 33366, 4733, 113497, 4748, 958, 4750, 4744, 953, 4746, 4741, 951, 947, 4739, 4764, 942, 4766, 6656, 6658, 933, 4756, 928, 930, 4754, 6711, 924, 6709, 6707, 920, 918, 912, 6713, 4799, 910, 907, 904, 4792, 6702, 901, 6700, 4787, 6698, 897, 6738, 1019, 1023, 117899, 1011, 4803, 1013, 4805, 1000, 6723, 117919, 1003, 4827, 4829, 1007, 993, 4817, 6728, 4819, 6730, 6732, 996, 148341, 985, 990, 988, 6773, 4832, 4839, 4836, 981, 6754, 970, 6752, 4856, 974, 962, 95846, 967, 6765, 964, 1101, 1096, 5496, 3281, 1099, 5492, 1093, 119450, 5494, 1089, 5490, 3268, 1118, 3270, 5486, 1113, 5481, 3264, 5483, 3266, 5477, 1108, 3276, 3279, 5479, 1105, 5473, 5475, 5471, 1132, 5469, 1130, 5467, 1127, 5463, 1125, 1123, 1120, 3303, 3301, 1149, 3299, 1147, 1144, 3307, 5443, 3305, 5441, 1033, 3218, 3220, 1037, 1039, 5439, 1026, 1029, 3230, 1050, 3205, 1052, 3207, 3209, 5408, 1043, 1045, 5402, 5400, 1070, 3254, 3252, 5404, 1059, 3256, 1061, 1082, 3234, 5384, 3239, 1086, 1084, 293749, 3236, 293754, 3243, 1074, 5378, 293752, 3241, 293758, 1079, 293759, 5382, 1077, 293756, 5380, 5620, 1222, 5622, 1216, 293769, 1219, 293764, 1228, 3156, 293765, 5628, 293766, 3158, 293760, 1225, 3154, 293763, 5626, 3151, 5607, 1245, 5613, 293783, 3142, 293782, 5615, 1240, 3136, 5609, 293779, 293778, 1243, 1251, 5587, 5585, 1248, 1260, 1258, 3185, 5574, 3183, 148569, 3181, 3179, 1264, 3177, 5582, 3175, 5580, 3173, 5578, 3171, 5576, 1273, 3169, 139917, 3099, 1154, 1157, 3101, 3103, 3089, 5560, 3091, 3093, 1168, 29443, 1171, 5540, 1174, 5542, 121564, 1177, 1180, 3077, 1183, 3079, 5523, 5527, 1189, 5525, 3132, 1195, 5530, 1192, 1198, 3126, 5532, 5506, 1203, 3112, 5504, 1206, 3118, 3107, 1209, 121594, 5519, 1212, 3109, 119686, 3520, 3542, 5246, 5244, 3546, 3544, 3556, 5196, 3554, 5194, 5189, 5185, 3568, 3570, 211190, 3581, 44678, 5201, 5203, 5163, 5161, 1310, 265670, 1297, 3464, 1300, 3475, 1291, 1289, 1295, 3477, 115425, 1286, 5174, 3487, 3485, 3489, 3491, 3494, 5121, 3496, 3498, 3507, 3509, 3511, 1312, 5136, 3514, 5138, 36000, 3518, 3407, 3405, 5347, 5345, 3397, 3423, 5367, 3421, 5365, 3419, 3414, 5374, 5372, 5369, 3437, 3432, 5312, 121780, 3434, 5314, 5320, 5322, 3427, 5333, 3452, 5335, 113923, 3454, 113925, 3444, 113931, 3446, 5337, 3442, 3338, 3341, 3330, 5291, 5289, 3334, 3332, 3354, 5298, 3358, 3356, 5300, 3344, 5249, 5251, 3360, 5262, 5264, 3387, 5273, 3376, 5275, 3827, 5979, 3825, 5977, 3829, 5981, 3834, 5971, 3838, 3836, 3808, 3815, 5967, 5965, 3812, 5952, 3821, 6009, 6011, 3801, 3803, 3806, 3776, 5996, 3783, 3785, 118924, 5987, 5991, 3767, 3762, 3774, 3772, 3748, 5899, 5890, 3753, 5950, 5945, 3740, 5940, 3742, 5942, 5937, 3738, 5933, 5935, 5929, 5931, 3726, 5920, 3704, 3711, 3698, 6104, 3696, 6109, 3701, 6082, 6080, 6086, 6088, 3681, 6136, 6138, 6140, 44335, 6142, 6118, 6120, 3649, 3651, 6033, 3637, 3635, 3631, 6018, 6016, 6031, 6029, 3618, 6070, 3605, 6078, 3601, 3603, 6052, 6054, 6050, 6060, 6062, 6058, 3587, 5707, 5709, 4073, 5720, 4081, 5722, 4085, 4089, 5714, 5716, 5718, 4035, 4033, 5743, 4037, 4050, 5755, 4055, 4052, 5757, 4058, 5747, 5745, 4062, 4060, 5749, 4004, 5644, 4002, 5642, 5639, 5635, 4020, 4022, 5656, 4018, 5658, 4029, 4031, 4025, 4027, 5651, 3975, 3970, 5670, 3981, 94899, 5668, 3979, 94901, 5666, 3977, 5664, 5695, 3989, 5693, 5690, 3987, 3984, 5688, 3994, 5683, 3992, 5825, 3946, 3948, 5829, 5831, 3937, 3941, 3943, 5841, 5843, 3966, 5846, 5848, 5858, 3915, 5856, 3913, 5863, 5860, 5867, 3905, 5871, 3931, 3928, 3935, 5879, 3922, 5881, 3926, 3924, 3885, 3887, 3883, 3876, 5774, 5769, 5771, 3901, 3903, 3899, 36384, 46567, 36397, 5790, 3889, 5785, 3854, 5799, 3852, 5797, 66985, 3840, 5801, 5815, 3868, 5812, 5810, 3865, 5808, 67001, 5823, 5821, 5819, 5817]




s1.addAll(s)
s1.addAll(s2)

def z = x2-x1
def s5 = nSer.checkzz1();
def x = s5 - s1

println "====s1 == " + s1.size()
println "====s5 == " + s5.size()


println "===X ==  " + x;
println "===X SIZE== "+ x.size()

println "===Z ==  " + z;
println "===Z SIZE== "+ z.size()

println "defined";

}

//checkzz();


def updateRanks() {
    int counter  = 0;
    def ranks = []
    new File("/home/rahulk/Desktop/trinomialsFinal.csv").splitEachLine(",") {fields ->
        if(fields[0] != 'ID') {   
            println "========COUNTER === " + counter
            println "=====WORKING ON ==== " + fields[0]
            counter++
            TaxonomyDefinition.withNewTransaction {
                def td = TaxonomyDefinition.get(fields[0].toLong());
                if(td.rank == 9) {
                    println "=====================RANK 9======================="
                    ranks.add(td);
                }
                td.rank = 10;
                if(!td.save(flush:true)){
                    println "failed update===="
                }
            }
        }
    }
    println "=======RANKS ==== " + ranks
}


//updateRanks()
