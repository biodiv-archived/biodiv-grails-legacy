import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.TaxonomyRegistry
import species.TaxonomyDefinition
import species.SpeciesController

import species.namelist.Utils
import species.Classification
import species.ScientificName
import species.ScientificName.TaxonomyRank;
import species.SynonymsMerged
import species.SpeciesField
import species.Synonyms
import species.Species
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
import species.Language;
import species.participation.ActivityFeedService;

nSer = ctx.getBean("namelistService");
utilsService = ctx.getBean("utilsService");
speciesUploadService = ctx.getBean("speciesUploadService");
speciesSearchService = ctx.getBean("speciesSearchService");
taxonService = ctx.getBean("taxonService");

//////////FOR MIGRATION////////////////

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
    def taxCan = TaxonomyDefinition.get(taxonId.toLong()).canonicalForm.replaceAll(' ', '_')
    println "=====READING THIS FILE ====== " + taxCan+'.xml'
	List colData = nSer.processColData(new File(domainSourceDir, taxCan+'.xml'));
    if(colData) {
        ScientificName sciName = TaxonomyDefinition.get(taxonId);
        nSer.curateName(sciName, colData);
    } else {
        ScientificName sciName = TaxonomyDefinition.get(taxonId.toLong())
	sciName.noOfCOLMatches = 0;
        sciName.position = NamesMetadata.NamePosition.RAW;
        sciName.dirtyListReason = "NO XML - NO COL DATA"
        println "=======NO COL MATCHES==== " + sciName.noOfCOLMatches
	if(sciName.save(flush:true) && !sciName.hasErrors()) {
		println "saved====="
        } else {
            sciName.errors.allErrors.each { log.error it }
        }
        println "=====NO COL DATA === " 
    }
}

File domainSourceDir = new File("/home/rahulk/col_8May/TaxonomyDefinition");
//migrate()
//migrateFromDir(domainSourceDir);
//curateName(1273, domainSourceDir);



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

boolean createThisSynonym(acc, canonicalForm, authorYear, normalizedForm){
    List synFamily = [];
    if(acc.status == NamesMetadata.NameStatus.SYNONYM){
        def res = acc.fetchAcceptedNames();
        acc = res[0];
    }
    synFamily.add(acc);
    def synList = acc.fetchSynonyms();
    synFamily.addAll(synList);
    def canMatches = []
    synFamily.each {
        if(it.canonicalForm == canonicalForm) {
            canMatches.add(it)
        }
    }
    ////CANONICAL ZERO MATCH - SO ADD
    if(canMatches.size() == 0 ){
        println "CANONICAL ZERO MATCH - SO ADD"
        return true
    } else {
        //CANONICAL MATCH MULTIPLE BUT NO AUTHOR YEAR - SO DROP
        if(authorYear == null || authorYear == '' || authorYear == ' '){
            println "CANONICAL MATCH MULTIPLE BUT NO AUTHOR YEAR - SO DROP"
            return false; 
        } 
        else {
            int noOfMatches = 0;
            canMatches.each {
                if(it.normalizedForm == normalizedForm) {
                    noOfMatches++;
                }
            }
            //NORMALISED ZERO MATCH - SO ADD
            if(noOfMatches == 0){
                println "NORMALISED ZERO MATCH - SO ADD"
                return true;
            } else {
                //NORMALISED MULTI MATCH - SO DROP
                println "NORMALISED MULTI MATCH - SO DROP"
                return false;
            }
        }
    }
}

boolean migrateThisSynonym(syn) {
    def acc = syn.taxonConcept;
    if(acc.position == NamesMetadata.NamePosition.WORKING) {
        println "ACCEPTED NAME IN WORKING LIST" 
        syn.dropReason = "ACCEPTED NAME IN WORKING LIST" 
        if(!syn.save()){
            println "FAILED TO SAVE SYNONYM " + syn
        }
        return false;
    }
    NamesParser namesParser = new NamesParser();
    def parsedNames = namesParser.parse([syn.name]);
    if (!syn.canonicalForm){
        println "======MISSING CANONICAL FORM - Parsing to fetch ===== " + syn.name 
        if(!parsedNames[0]?.canonicalForm) {
            println "===COULD NOT PARSE copying its name in other name variants - syn id=== " + syn.id
            syn.canonicalForm = syn.name;
            //syn.normalizedForm = syn.name;
            //syn.italicisedForm = syn.name;
            //syn.binomialForm = syn.name;
        } else {
            syn.canonicalForm = parsedNames[0].canonicalForm;
            syn.normalizedForm = parsedNames[0].normalizedForm;
            syn.italicisedForm = parsedNames[0].italicisedForm;
            syn.binomialForm = parsedNames[0].binomialForm;
        }
    }
    if(!syn.authorYear) {
        println "=========AUTHOR YEAR ====== " + parsedNames[0].authorYear
        syn.authorYear = parsedNames[0].authorYear;
        println "=========AUTHOR YEAR ====== " + syn.authorYear
    }
    List synFamily = [];
    if(acc.status == NamesMetadata.NameStatus.SYNONYM){
        def res = acc.fetchAcceptedNames();
        acc = res[0];
    }
    synFamily.add(acc);
    def synList = acc.fetchSynonyms();
    synFamily.addAll(synList);
    def canMatches = []
    synFamily.each {
        if(it.canonicalForm == syn.canonicalForm) {
            canMatches.add(it)
        }
    }
    ////CANONICAL ZERO MATCH - SO ADD
    if(canMatches.size() == 0 ){
        println "CANONICAL ZERO MATCH - SO ADD"
        return true
    } else {
        //CANONICAL MATCH MULTIPLE BUT NO AUTHOR YEAR - SO DROP
        if(syn.authorYear == null || syn.authorYear == '' || syn.authorYear == ' '){
            println "CANONICAL MATCH MULTIPLE BUT NO AUTHOR YEAR - SO DROP"
            syn.dropReason = "CANONICAL MATCH MULTIPLE BUT NO AUTHOR YEAR - SO DROP"
            if(!syn.save(flush:true)){
                println "FAILED TO SAVE SYNONYM " + syn
            }
            return false; 
        } 
        else {
            int noOfMatches = 0;
            canMatches.each {
                if(it.normalizedForm == syn.normalizedForm) {
                    noOfMatches++;
                }
            }
            //NORMALISED ZERO MATCH - SO ADD
            if(noOfMatches == 0){
                println "NORMALISED ZERO MATCH - SO ADD"
                return true;
            } else {
                //NORMALISED MULTI MATCH - SO DROP
                println "NORMALISED MULTI MATCH - SO DROP"
                syn.dropReason = "NORMALISED MULTI MATCH - SO DROP"
                if(!syn.save()){
                    println "FAILED TO SAVE SYNONYM " + syn
                }
                return false;
            }
        }
        
    }
}

def migrateSynonyms() {
    int limit = 20000, offset = 0, insert_check = 0,exist_check = 0;
    int counter = 0;
    def nonParsedSyns = [];
    def notMigrating = [];
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit //+ " =========COUNTER ====== " + conter;    
        def oldSynList = Synonyms.list (max: limit , offset:offset, , sort: "id", order: "asc");
        //def oldSynList = Synonyms.read(218033L) //(max: limit , offset:offset);
        def synMer = null;
        int count2000 = 0;
        for(oldSyn in oldSynList) {
            count2000++;
            println "=====WORKING ON THIS SYNONYM============== " + oldSyn + " =========COUNTER ====== " + counter;
            counter++;
            boolean migrateThisSynonym = migrateThisSynonym(oldSyn);
            synMer = SynonymsMerged.findByName(oldSyn.name);
            if(synMer) {
                migrateThisSynonym = false;
                if(oldSyn.taxonConcept.status == NamesMetadata.NameStatus.ACCEPTED) {
                    oldSyn.taxonConcept.addSynonym(synMer);
                } else {
                    def accRes  = oldSyn.taxonConcept.fetchAcceptedNames();
                    def acc = accRes[0];
                    acc.addSynonym(synMer);
                }
            }
            if(migrateThisSynonym) {
                def flag = false;
                SynonymsMerged.withNewTransaction {
                    
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
                            oldSyn.authorYear = parsedNames[0].authorYear;
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
                        if(!synMer.save()) {
                            synMer.errors.each { println it }
                        }
                        println "========SYN MERGED ======= " + synMer
                    }

                    if(!flag) {
                        //SynonymsMerged.withNewTransaction {
                        //TODO: check whether its old accepted name is still accepted or changed to synonym
                        if(oldSyn.taxonConcept.status == NamesMetadata.NameStatus.ACCEPTED) {
                            oldSyn.taxonConcept.addSynonym(synMer);
                        } else {
                            def accRes  = oldSyn.taxonConcept.fetchAcceptedNames();
                            def acc = accRes[0];
                            acc.addSynonym(synMer);
                        }
                        //}
                    }
                }
            } else {
                println "======NOT MIGRATING THIS SYNONYM ====== " + oldSyn
                notMigrating.add(oldSyn.id);
            }
            if(count2000 == 2000) {
                //utilsService.cleanUpGorm(true);
                count2000 = 0;
            }
        }
        offset = offset + limit; 
        utilsService.cleanUpGorm(true); 
        if(!oldSynList) break;  
    }
    println "=======NON PARSED IDS ===== " + nonParsedSyns
    println "=======NOT MIGRATING ===== " + notMigrating
    println "=======NOT MIGRATING SIZE ===== " + notMigrating.size()
}

//migrateSynonyms();

//First function to run curation for all accepted names
def curateAllNames() {
    def start = new Date();
	int limit = 70000, offset = 0;
    int counter = 0;
    List curatingThese = [];
    File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition_cononical_name/TaxonomyDefinition");
    while(offset < 68000){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def taxDefList = [];
        TaxonomyDefinition.withNewTransaction {
            def dataSoruce = ctx.getBean("dataSource");
            def sql =  Sql.newInstance(dataSoruce);
            def query  = "select id from taxonomy_definition where id <= 276064 order by rank,id asc limit 70000";
            sql.rows(query).each{
                taxDefList.add(TaxonomyDefinition.get(it.getProperty("id")));
            }   
            /*def c = TaxonomyDefinition.createCriteria()
            taxDefList = c.list (max: limit , offset:offset) {
            and {
            //lt('id', 275703L)
            lt('id', 276015L)
            //eq('position', NamesMetadata.NamePosition.WORKING)
            //isNull('position')
            }
            order('rank','asc')
            order('id','asc')                    
            }*/
            //taxDefList = TaxonomyDefinition.list(max: limit, offset: offset, sort: "rank", order: "asc")
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
	println "=======START TIME ===== " + start
println "========END TIME= ======= " + new Date()
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


def updateRanks() {
    int counter  = 0;
    def ranks = []
    new File("/apps/git/biodiv/trinomialsFinal.csv").splitEachLine(",") {fields ->
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

def curateWithManualIDs() {
    def alreadyInWorking = [];
    def start = new Date()
    int counter  = 0;
    new File("/tmp/dirtynamesMatchedtoCoLpipesep.csv").splitEachLine(",") {fields ->
        if(fields[0] != 'species ID') {
            println "========COUNTER === " + counter
            println "=====WORKING ON ==== " + fields[0]
            counter++
            def s = Species.get(fields[0].toLong())
            def td = s.taxonConcept;
            def colID = fields[-1];
            println "=====LOOKING FOR THIS COL ID ======= " + colID
            def taxonId = td.id.toString();
            def taxCan = td.canonicalForm.replaceAll(' ', '_');
            File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition_cononical_name/TaxonomyDefinition");
            //File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition");
            List colData = nSer.processColData(new File(domainSourceDir, taxCan+'.xml'));
            def colDataSize = 0
            if(colData){ 
                colDataSize = colData.size();
            }
            def acceptedMatch = null;
            colData.each { colMatch ->
                if(colMatch.externalId == colID){
                    acceptedMatch = colMatch    
                }
            }
            if(acceptedMatch){
                println "=======ACCEPTED MATCH FOUND ======= " + acceptedMatch
                if(td.position != NamesMetadata.NamePosition.WORKING){
                    nSer.processDataForMigration(td, acceptedMatch, colDataSize)
                }else {
                    alreadyInWorking.add(td.id);
                }
            } else {
                println "========COULD NOT FIND COL MATCH FOR SPECIES========= " + fields[0]
            }
        }
    }
    println "======ALREADY IN WORKING =========  "+ alreadyInWorking
    println "====ALREADY IN WORKING SIZE =====  " + alreadyInWorking.size();
    println "=======START TIME ===== " + start
    println "========END TIME= ======= " + new Date()

}

//curateWithManualIDs()

def addSynToAccName(sciName, synDetails) {
    NamesParser namesParser = new NamesParser();
    def parsedNames = namesParser.parse([synDetails.name]);

    def synMer = null; 
    synMer = SynonymsMerged.findByName(synDetails.name);
    if(!synMer) {
        synMer = new SynonymsMerged();
        synMer.name = synDetails.name;
        synMer.canonicalForm = synDetails.canonicalForm;
        synMer.relationship = RelationShip.SYNONYM 
        if(parsedNames[0]?.canonicalForm) {
            synMer.normalizedForm = parsedNames[0].normalizedForm;
            synMer.italicisedForm = parsedNames[0].italicisedForm;
            synMer.binomialForm = parsedNames[0].binomialForm;
        } 
        /*else {
          println "=====PUTTING CANONICAL AS BINOMIAL===="
          synMer.normalizedForm = synMer.canonicalForm
          synMer.italicisedForm = synMer.canonicalForm 
          synMer.binomialForm = synMer.canonicalForm;
          }*/

        synMer.status = NamesMetadata.NameStatus.SYNONYM
        synMer.viaDatasource = "CatalogueOfLife"
        synMer.uploadTime = new Date()
        def contributor = SUser.read(1L);
        synMer.uploader = contributor
        synMer.authorYear = synDetails.authorString;
        synMer.ibpSource = null
        synMer.matchId = synDetails.id
        synMer.matchDatabaseName = "CatalogueOfLife"
        synMer.position = NamesMetadata.NamePosition.WORKING
        synMer.rank = synDetails.parsedRank;
        synMer.colNameStatus = nSer.getCOLNameStatus(synDetails.colNameStatus);
        synMer.addToContributors(contributor);

        if(!synMer.save()) {
            synMer.errors.each { println it }
        }
    }
    sciName.addSynonym(synMer);
}

def addSynonymsFromCOL() {
    def start = new Date();
	int limit = 75000, offset = 0;
    int counter = 0;
    List curatingThese = [];
    //File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition");

	File domainSourceDir = new File("/apps/git/biodiv/col_8May/TaxonomyDefinition_cononical_name/TaxonomyDefinition");
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def taxDefList;
        TaxonomyDefinition.withNewTransaction {
            def c = TaxonomyDefinition.createCriteria()
            //taxDefList = TaxonomyDefinition.get(4135L);
            taxDefList = c.list (max: limit , offset:offset) {
                and {
                    gt('rank', 8)
                    eq('status', NamesMetadata.NameStatus.ACCEPTED)
                    eq('position', NamesMetadata.NamePosition.WORKING)
                    //isNull('position')
                }
                order('rank','asc')
                order('id','asc')                    
            }
        }
        int count5 = 0;
        int synCount = 0;
        Date startDate = new Date();
        for(taxDef in taxDefList) {
            count5 ++;
            println "###############################################################################################"
            println "#"
            println "=====WORKING ON THIS TAX DEF============== " + taxDef + " =========COUNTER ====== " + counter;
            def colID = taxDef.matchId
            counter++;
            List colData = nSer.processColData(new File(domainSourceDir, taxDef.canonicalForm.replaceAll(' ', '_')+'.xml'));
            def acceptedMatch = null;
            if(colData && colData.size() > 0 ) {
                for(colMatch in colData) {
                    if(colMatch.externalId == colID){
                        acceptedMatch = colMatch
                        break
                    }
                }
            } else {
                acceptedMatch = nSer.searchCOL(colID, 'id')[0]
            }
            TaxonomyDefinition.withNewTransaction {
                if(acceptedMatch){
                    println "=======ACCEPTED MATCH FROM COL ======= " + acceptedMatch
                    synCount += acceptedMatch.synList.size();
                    acceptedMatch.synList.each { synDetails ->
                        println "====ADDING THESE DETAILS AS SYNONYMS ====== " + synDetails
                        NamesParser namesParser = new NamesParser();
                        def parsedNames = namesParser.parse([synDetails.name]);
                        boolean createSynonym;
                        if(parsedNames[0]?.canonicalForm) {
                            createSynonym = createThisSynonym(taxDef, synDetails.canonicalForm, synDetails.authorYear, parsedNames[0]?.normalizedForm)
                        } else {
                            createSynonym = createThisSynonym(taxDef, synDetails.canonicalForm, synDetails.authorYear, synDetails.name)
                        }
                        if(createSynonym) {
                            addSynToAccName(taxDef, synDetails)    
                        } else {
                            println "======THIS SYNONYM FROM COL ALREADY EXISTS===="
                        }
                    }
                } else {
                    println "=========NO ACCEPTED MATCH======== "
                }
            }
            if(count5 == 4){
                utilsService.cleanUpGorm(true);
                println "=========SYN COUNT PER 5 ACCEPTED NAME ========= " + synCount + "      total time  " + ((new Date()).getTime() - startDate.getTime())/1000;
                startDate = new Date();
                synCount = 0;
                count5 = 0;
            }
        }
        offset = offset + limit; 
        utilsService.cleanUpGorm(true);
        if(!taxDefList) break;  
    }
 println "=======START TIME ===== " + start
println "========END TIME= ======= " + new Date()

}

//addSynonymsFromCOL()

def addDetailsFromGNI() {
    int limit = 71800, offset = 71799;
    int counter = 0;

    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def synMerList;
        SynonymsMerged.withNewTransaction {
            def c = SynonymsMerged.createCriteria()
            //taxDefList = TaxonomyDefinition.get(4135L);
            synMerList = c.list (max: limit , offset:offset) {
                and {
                    gt('id', 280621L)
                }
                order('rank','asc')
                order('id','asc')                    
            }
        }
        int count200 = 0;
        Date startDate = new Date();
        for(synMer in synMerList) {
            count200 ++;
            println "###############################################################################################"
            println "#"
            println "=====WORKING ON THIS SYN MER============== " + synMer + " =========COUNTER ====== " + counter;
            counter++;
            SynonymsMerged.withNewTransaction { 
                NamesParser namesParser = new NamesParser();
                def parsedNames = namesParser.parse([synMer.name]);
                if(parsedNames[0]?.canonicalForm) {
                    synMer.normalizedForm = parsedNames[0].normalizedForm;
                    synMer.italicisedForm = parsedNames[0].italicisedForm;
                    synMer.binomialForm = parsedNames[0].binomialForm;
                    if(!synMer.save()) {
                        synMer.errors.each { println it }
                    }
                }
            }
            if(count200 == 200){
                println "==== total time  " + ((new Date()).getTime() - startDate.getTime())/1000;
                utilsService.cleanUpGorm(true);
                startDate = new Date();
                count200 = 0;
            }
        }
        offset = offset + limit; 
        utilsService.cleanUpGorm(true);
        if(!synMerList) break;  
    }
}

//addDetailsFromGNI()


//IBPhierarchyDirtlistSpsWithInfo : contains names in the dirty list for at species or infra-species level for which the ccomplete hierarchy needs to be populated into the IBP TAXONOMIC HIERARCHY
def IBPhierarchyDirtlistSpsWithInfo() {
	File file = new File("/apps/git/biodiv/IBPhierarchyDirtlistSpsWithInfo.txt");
    def lines = file.readLines();
    int i=0;
    SUser admin = SUser.read(1L);
    def trr = new TaxonomyDefinition[11];
    def classifi = Classification.findByName("Author Contributed Taxonomy Hierarchy");
    lines.each { line ->
            if(i++ == 0) return;
            arr = line.split('\\t');
            println arr;
            def reg = TaxonomyRegistry.get(Long.parseLong(arr[5]));

            def result = taxonService.deleteTaxonHierarchy(reg, true, false);
            if(result.success) {
                String speciesName= (arr.size() == 17) ? arr[6+TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()]: arr[6+TaxonomyRank.SPECIES.ordinal()];
                def taxonNames= [];
                int j=0;
                arr.each {
                    if(j++<6) return;
                    if(it != 'null')
                        taxonNames << it
                    else 
                        taxonNames << null
                }
                println taxonNames
                println  taxonService.addTaxonHierarchy(speciesName, taxonNames, classifi, admin, null, false, false, null);
            } else {
                println "ERROR : "+result;
            }
    } 
}

//IBPhierarchyDirtlistSpsToDrop: contains names in the dirty list for at species or infra-species level which need to be dropped and stubs deleted.
def IBPhierarchyDirtlistSpsToDrop() {
    println "IBPhierarchyDirtlistSpsToDrop"
	File file = new File("/apps/git/biodiv/IBPhierarchyDirtlistSpsToDrop.txt");
    def lines = file.readLines();
    int i=0;
    def sc = new SpeciesController();
    SUser admin = SUser.read(1L);
    int ns = 0, nt=0;
    lines.each { line ->
        if(i++ == 0) return;
        def arr = line.split('\\t');
        //println arr;
        def speciesInstance = Species.get(Long.parseLong(arr[0]));
        if(speciesInstance) {
            //Species.withTransaction {
                try {
                    boolean success = speciesUploadService.deleteSpeciesWrapper(speciesInstance, admin);
                    if(success) {
                        ns++;
                        def reg = TaxonomyRegistry.get(Long.parseLong(arr[4]));
                        if(reg) {
                            def result = taxonService.deleteTaxonHierarchy(reg, true, false);
                        }
                       
                        def taxon = TaxonomyDefinition.get(Long.parseLong(arr[3]));
                        if (taxon) {
                            taxon.isDeleted = true;
                            if(taxon.save(flush:true)) nt++;
                        }
                    }
                } catch(e) {
                    e.printStackTrace()
                    println "ERRRRORRRR : "+e.getMessage();

                }
            //}
        }
    }
    println "deleted "+ns+" species "+ nt + " taxon ";
}


//IBPhierarchyDirtlistABOVESpsToDrop: contains names in the dirty list for above species level, which needs to be dropped only if they have no reference in other places.
def IBPhierarchyDirtlistABOVESpsToDrop() {
    println "IBPhierarchyDirtlistABOVESpsToDrop"

    def taxonService = ctx.getBean("taxonService");
	File file = new File("/home/sravanthi/git/biodiv/IBPhierarchyDirtlistABOVESpsToDrop.txt");
    def lines = file.readLines();
    int i=0;
    int no=0;
    TaxonomyDefinition.withNewSession {
        lines.each { line ->
            if(i++ == 0) return;
            def arr = line.split('\\t');
            println arr;
            TaxonomyDefinition t = TaxonomyDefinition.get(Long.parseLong(arr[3]));
            TaxonomyRegistry reg = TaxonomyRegistry.get(Long.parseLong(arr[4]));
            def r = taxonService.deleteTaxonEntries(reg, true, false);
            if(r.success && r.status != 401) {
                t.isDeleted = true;
                if(t.save(flush:true)) {
                    no++;
                } else {
		        	t.errors.each { println it }
                }
            }
        }
    }
    println "deleted "+no+" taxonNames";
}
//IBPhierarchyDirtlistSpsWithInfo() 
//IBPhierarchyDirtlistSpsToDrop();
//IBPhierarchyDirtlistABOVESpsToDrop();
//createIBPHierarchyForDirtylist();


def copyIBPHierarchyToCOLClassification() {
    def classifi = Classification.findByName("Catalogue of Life Taxonomy Hierarchy");
    def ibpClass = Classification.findByName("IBP Taxonomy Hierarchy");
    def start = new Date();
	int limit = 50, offset = 0;
    int counter = 0;
    SUser admin = SUser.read(1L);
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def taxRegList = [];
        TaxonomyRegistry.withNewTransaction {
	        def taxReg = TaxonomyRegistry.createCriteria()
            taxRegList = taxReg.list (max: limit , offset:offset) {
                and {
                    eq('classification', ibpClass)
                }
                order('id','asc')                    
            }
        }
        for(reg in taxRegList) {
            println "=====WORKING ON THIS REGISTRY============== " + reg + " =========COUNTER ====== " + counter;
            counter++;
            List taxonNames = [];
            List taxIds = reg.path.tokenize('_');
            def m = [:]
            taxIds.each {
                def tax = TaxonomyDefinition.read(it.toLong());
                m[tax.rank] = tax.name;
            }
            for (index in 0..10) {
                if(m[index]) {
                    taxonNames.add(m[index]);
                } else {
                    taxonNames.add(null);
                }
            }
            def otherParams = [:];
            otherParams['nameStatus'] = 'accepted';
            println "=====TAXON NAMES ====== " + taxonNames
            println  taxonService.addTaxonHierarchy(taxonNames.last(), taxonNames, classifi, admin, null, false, true, otherParams);
        }

        offset = offset + limit; 
        //utilsService.cleanUpGorm(true); 
        if(!taxRegList) break;  
    }
    println "=====START === " + start + "====END === " + new Date()
}

//copyIBPHierarchyToCOLClassification()


def addFeedForRawNames(){
    println "ADDING FEEDS"
    int limit = 50, offset = 0;
    int counter = 0;

    def activityFeedService = ctx.getBean("activityFeedService");
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def tdList;
        TaxonomyDefinition.withNewTransaction {
            def c = TaxonomyDefinition.createCriteria()
            tdList = c.list (max: limit , offset:offset) {
                and {
                    eq('position', NamesMetadata.NamePosition.RAW)
                    eq('status', NamesMetadata.NameStatus.ACCEPTED)
                }
                order('rank','asc')
                order('id','asc')                    
            }
        }
        for (td in tdList) {
            println "=====WORKING ON THIS TAX DEF============== " + td + " =========COUNTER ====== " + counter;
            counter++;
            TaxonomyDefinition.withNewTransaction {
                if(td.dirtyListReason && td.dirtyListReason != '') {
                    def feedInstance = activityFeedService.addActivityFeed(td, td, SUser.read(1L), ActivityFeedService.TAXON_NAME_UPDATED, td.dirtyListReason);
                }
            }
        }

        offset = offset + limit; 
        if(!tdList) break; 
    }
}


//addFeedForRawNames()




//////////// TESTING ////////
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
    //x = new TestA(name:'Strand77');println "========COUNTER === " + counter
            println "=====WORKING ON ==== " + fields[0]

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

def updatePosition(){
    println "====update status called=";
    ScientificName sciName = TaxonomyDefinition.get(7L)
    //nSer.updatePosition(sciName, NamesMetadata.NamePosition.WORKING);
    sciName.position = NamesMetadata.NamePosition.RAW
    sciName.save(flush:true)
    println "===pos=== " + sciName.position ; 
}
//updatePosition()

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





/////////// STATS ///////////////
def incompleteHierarchy() {
    int limit = 50, offset = 0;
    int counter = 0;

    File reportFile = new File("/home/rahulk/incomplete_hierarchy.csv")
    if(reportFile.exists()){
        reportFile.delete()
        reportFile.createNewFile()
    }
    String str = "Species Id|Species Title|Species Percent Info|Taxon Id|Hierarchy Id|Hierarchy";
    str = str + "\n";
    reportFile << str;
    def namesNoHie = [];
    def classifications = Classification.list();
    while(true){
        println "=====offset == "+ offset + " ===== limit == " + limit  
        def tdList;
        TaxonomyDefinition.withNewTransaction {
            def c = TaxonomyDefinition.createCriteria()
            tdList = c.list (max: limit , offset:offset) {
                and {
                    lt('id', 275703L)
                    eq('position', NamesMetadata.NamePosition.RAW)
                    //isNull('position')
                }
                order('rank','asc')
                order('id','asc')                    
            }
        }
        for (td in tdList) {
            println "=====WORKING ON THIS TAX DEF============== " + td + " =========COUNTER ====== " + counter;
            counter++;
            boolean incomplete = true;
            def longestReg = null;
            def cl = null;
            int max = 0;
            classifications.each { 
                def reg = td.longestParentTaxonRegistry(it);
                def taxonList = reg.get(it);
                if(taxonList.size() > max) {
                    longestReg = reg;
                    cl = it;
                    max = taxonList.size();
                } 
            }
            if(!longestReg){
                namesNoHie.add(td.id);
                continue;
            }
            def finalTaxonList = longestReg.get(cl);
            switch(td.rank) {
                case 0 :
                if(finalTaxonList.size() >= 1){
                    incomplete = false;
                }
                break
                case 1 :
                if(finalTaxonList.size() >= 2){
                    incomplete = false;
                }
                break
                case 2 :
                if(finalTaxonList.size() >= 3){
                    incomplete = false;
                }
                break
                case 3 :
                if(finalTaxonList.size() >= 4){
                    incomplete = false;
                }
                break
                case 4 :
                if(finalTaxonList.size() >= 5){
                    incomplete = false;
                }
                break
                case 5 :
                if(finalTaxonList.size() >= 5){
                    incomplete = false;
                }
                break
                case 6 :
                if(finalTaxonList.size() >= 6){
                    incomplete = false;
                }
                break
                case 7 :
                if(finalTaxonList.size() >= 6){
                    incomplete = false;
                }
                break
                case 8 :
                if(finalTaxonList.size() >= 7){
                    incomplete = false;
                }
                break
                case 9 :
                if(finalTaxonList.size() >= 7){
                    incomplete = false;
                }
                break
                case 10 :
                if(finalTaxonList.size() >= 8){
                    incomplete = false;
                }
                break
            }
            if(incomplete) {
                def spId = td.findSpeciesId();
                StringBuilder sb = new StringBuilder();
                 if(spId) {
                    def sp = Species.read(spId.toLong());
                    sb.append(sp.id + "|");
                    sb.append(sp.title + "|");
                    sb.append(sp.percentOfInfo + "|");
                } else {
                    sb.append("null" + "|");
                    sb.append("null" + "|");
                    sb.append("null" + "|");
                }
                sb.append(td.id + "|")
                def regId = longestReg.regId;
                sb.append(regId + "|");
                def m = [:];
                finalTaxonList.each {
                    m[it.rank] = it.name;
                }
                String hie = "";
                for ( i in 0..10 ) {
                    if(m[i]){
                        hie += m[i] + "->"
                    } else {
                        hie += "null" + "->"
                    }
                }
                sb.append(hie);
                reportFile << sb.toString() + "\n";
            }
        }
        offset = offset + limit; 
        if(!tdList) break; 
    }
    println "======NO HIE = == " + namesNoHie;
}

//incompleteHierarchy()


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


def correctSynonyms() {
    speciesService = ctx.getBean("speciesService");
    int counter  = 0;
    new File("/home/rahulk/Desktop/correctedsynonyms.csv").splitEachLine(',') {fields ->
        if(fields[0] != 'syn_id') {
            println "#"
            println "#"
            println "#"
            println "=======COUNTER=== " + counter
            counter++
            def acc = TaxonomyDefinition.get(fields[1].toLong())
            def speciesId =  acc.findSpeciesId();
            switch(fields[3]){
                case "Correct":
                println "==CORRECT VALUE====" + fields[2]
                def res = speciesService.updateSynonymOld(fields[0].toLong(), speciesId, 'SYNONYM', fields[2])
                println "========RES RESULT ===" + res.success + "=====MSG ====== "  + res.msg
                break

                case "Delete" :
                println "==DELETE===="
                def res = speciesService.deleteSynonymOld(fields[0].toLong(), speciesId)
                println "========RES RESULT ===" + res.success + "=====MSG ====== "  + res.msg

                break

                case "Create":
                println "==TO CREATE====" + fields[2]
                def res = speciesService.updateSynonymOld(null, speciesId, 'SYNONYM', fields[2])
                println "========RES RESULT ===" + res.success + "=====MSG ====== "  + res.msg
                break

                default :
                println "==============INVALID COMMAND============"
                break
            }
            
        }
    }
}

//correctSynonyms()

def testSearchIBP() {
    def res = nSer.searchIBP("Eriocaulon epedunculatum bis", "Potdar, Anil Kumar", NamesMetadata.NameStatus.ACCEPTED,10)
    println res;
}

//testSearchIBP()
