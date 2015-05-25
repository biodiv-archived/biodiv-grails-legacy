package species.participation

import org.apache.commons.logging.LogFactory;

import species.ScientificName
import species.TaxonomyDefinition;
import species.SynonymsMerged;
import species.Synonyms;
import species.NamesMetadata;
import species.TaxonomyRegistry
import species.Classification;
import species.Species;
import species.ScientificName.TaxonomyRank
import species.Synonyms;
import species.CommonNames;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.COLNameStatus;
import species.NamesMetadata.NamePosition;
import species.auth.SUser;

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.sql.Sql
import groovy.util.XmlParser
import grails.converters.JSON;
import wslite.soap.*
import species.NamesParser;
import species.sourcehandler.XMLConverter;
import species.participation.Recommendation;

class NamelistService {
   
    private static final String COL_SITE = 'http://www.catalogueoflife.org'
	private static final String COL_URI = '/annual-checklist/2015/webservice'
    
    private static final String GBIF_SITE = 'http://api.gbif.org'
	private static final String GBIF_URI = '/v1/species'
    
    private static final String TNRS_SITE = 'http://tnrs.iplantc.org'
    private static final String TNRS_URI = '/tnrsm-svc/matchNames'
	
    private static final String EOL_SITE = 'http://eol.org'
    private static final String EOL_URI = '/api/search/1.0.json'
    private static final String EOL_URI_ID = '/api/hierarchy_entries/1.0/'
    
    private static final String WORMS_SITE = 'http://www.marinespecies.org/'
    private static final String WORMS_URI = 'aphia.php'

    private static final int BATCH_SIZE = 100
	private static final log = LogFactory.getLog(this);

	private static final String ACCEPTED_NAME = "accepted name"
	private static final String SYNONYM = "synonym"
	private static final String PROV_ACCEPTED_NAME = "provisionally accepted name"
	private static final String COMMON_NAME = "common name"
	private static final String AMBI_SYN_NAME = "ambiguous synonym"
	private static final String MIS_APP_NAME = "misapplied name"

    private static long SEARCH_IBP_COUNTER = 0;
    private static long CAN_ZERO = 0;
    private static long CAN_SINGLE = 0;
    private static long CAN_MULTIPLE = 0;
    private static long AFTER_CAN_MULTI_ZERO = 0;
    private static long AFTER_CAN_MULTI_SINGLE = 0;
    private static long AFTER_CAN_MULTI_MULTI = 0;
    
    public static Set namesInWKG = [];

    public static Map namesBeforeSave = [:];
    public static Map namesAfterSave = [:];

	def dataSource
    def groupHandlerService
    def springSecurityService;
    def taxonService;
    def grailsApplication;
    def speciesService;
    def utilsService;
	def sessionFactory;
    def activityFeedService;

    List searchCOL(String input, String searchBy) {
        //http://www.catalogueoflife.org/col/webservice?name=Tara+spinosa

        def http = new HTTPBuilder()
        http.request( COL_SITE, GET, TEXT ) { req ->
            uri.path = COL_URI
            if(searchBy == 'name') {
                uri.query = [ name:input, response:'full', format:'xml']
            } else if(searchBy == 'id') {
                uri.query = [ id:input, response:'full', format:'xml']
            }
            //headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            headers.Accept = 'text/xml'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                //println xmlText
                def result = responseAsMap(xmlText, searchBy);
                return result;
            }
            response.'404' = { println 'Not found' }
        }
    }

    List searchGBIF(String input, String searchBy){
        //http://api.gbif.org/v1/species/match?verbose=true&name=Mangifera

        def http = new HTTPBuilder()
        println "========GBIF SITE===== " + GBIF_SITE
        http.request( GBIF_SITE, GET, TEXT ) { req ->
            if(searchBy == 'name') {
                uri.path = GBIF_URI + '/match';
            } else {
                uri.path = GBIF_URI + '/' + input;
            }
            if(searchBy == 'name') {
                uri.query = [ name:input]
            }
            /*else if(searchBy == 'id') {
                uri.query = [ id:input, format:'xml']
            }*/
            //headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            headers.Accept = '*/*'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                return responseFromGBIFAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }

    List responseAsMap(String xmlText, String searchBy) {
        def results = new XmlParser().parseText(xmlText)
        return responseAsMap(results, searchBy)
    }

    List responseAsMap(results, String searchBy) {
        List finalResult = []
        //println results.'@total_number_of_results'
        //println results.'@number_of_results_returned'
        //println results.'@error_message'
        //println results.'@version'

        int i = 0
        results.result.each { r ->
            Map temp = new HashMap();
            Map id_details = new HashMap();
            temp['externalId'] = r?.id?.text()//+"'"
            temp['matchDatabaseName'] = "COL"
            temp['canonicalForm'] = r?.name?.text(); 
            temp['name'] = r?.name?.text() 
            if(searchBy == 'name' || searchBy == 'id') {
                temp['name'] += " " +r?.author?.text().capitalize();
            }
            temp['rank'] = r?.rank?.text()?.toLowerCase()
            temp[r?.rank?.text()?.toLowerCase()] = r?.name?.text()
            id_details[r?.name?.text()] = r?.id?.text();
            def cs = r?.name_status?.text()?.tokenize(' ')[0]
            if(cs == 'provisionally' || cs == 'accepted') {
                temp['nameStatus'] = 'accepted'
            } else if (cs == 'misapplied' || cs == 'ambiguous' || cs == 'synonym') {
                temp['nameStatus'] = 'synonym'
            } else {
                temp['nameStatus'] = cs
            }
            temp['colNameStatus'] = r?.name_status?.text()?.tokenize(' ')[0]
            temp['authorString'] = r?.author?.text().capitalize();
            temp['sourceDatabase'] = r?.source_database?.text()

            temp['group'] = (r?.classification?.taxon[0]?.name?.text())?r?.classification?.taxon[0]?.name?.text():''
            println "==========NAME STATUS========= " + temp['nameStatus']
            if(temp['nameStatus'] == "synonym") {
                def aList = []
                r.accepted_name.each {
                    def m = [:]
                    m['id'] = it.id.text()
                    m['name'] = it.name.text() + " " + it.author.text().capitalize();;
                    m['canonicalForm'] = it.name.text();
                    m['nameStatus'] = it.name_status.text()?.tokenize(' ')[0];
                    m['rank'] = it.rank?.text()?.toLowerCase();
                    m['authorString'] = it.author.text().capitalize();;
                    m['source'] = "COL"
                    aList.add(m);
                }
                println "======A LIST======== " + aList;
                temp['acceptedNamesList'] = aList;
            }
            if(searchBy == 'id' || searchBy == 'name') {
                //println "============= references  "
                r.references.reference.each { ref ->
                //println ref.author.text()
                //println ref.source.text()
                }

                println "============= higher taxon  "
                int maxRank = -1;
                r.classification.taxon.each { t ->
                    //println t.rank.text() + " == " + t.name.text()
                    temp[t?.rank?.text()?.toLowerCase()] = t?.name?.text()
                    id_details[t?.name?.text()] = t?.id?.text()
                    int currRank = XMLConverter.getTaxonRank(t?.rank?.text()?.toLowerCase());
                    if(currRank > maxRank) {
                        temp['parentTaxon'] =  t?.name?.text()
                        maxRank = currRank
                    }
                }

                println "============= child taxon  "
                r.child_taxa.taxon.each { t ->
                // println t.name.text()
                // println t.author.text()
                }

                println "============= synonyms  "
                r.synonyms.synonym.each { s ->
                //println s.rank.text() + " == " + s.name.text()
                //println "============= references  "
                s.references.reference.each { ref ->
                //println ref.author.text()
                //println ref.source.text()
                }
                }
                /*
                println "==========NAME STATUS========= " + temp['nameStatus']
                if(temp['nameStatus'] == "synonym") {
                    def aList = []
                    r.accepted_name.each {
                        def m = [:]
                        m['id'] = it.id.text()
                        m['name'] = it.name.text()
                        m['source'] = "COL"
                        aList.add(m);
                    }
                    println "======A LIST======== " + aList;
                    temp['acceptedNamesList'] = aList;
                }
                */
            }
            
            temp['id_details'] = id_details
            finalResult.add(temp);
        }
        return finalResult
    }

    List responseFromGBIFAsMap(String xmlText , String searchBy) {
        def result = JSON.parse(xmlText)
        def finalResult = []
        Map temp = new HashMap()
        temp['externalId'] = result['usageKey'];
        temp['name'] = result['scientificName'];
        temp['rank'] = result['rank']?.toLowerCase();
        temp['nameStatus'] = '';
        temp['sourceDatabase'] = '';
        temp['group'] = result['kingdom'];
        if(searchBy == 'id') {
            temp['name'] = result['canonicalName'];
            temp['externalId'] = result['key'];
            temp['kingdom'] = result['kingdom']; 
            temp['phylum'] = result['phylum']; 
            temp['order'] = result['order']; 
            temp['family'] = result['family']; 
            temp['class'] = result['class']; 
            temp['genus'] = result['genus']; 
            temp['species'] = result['species']; 
            temp['nameStatus'] = result['taxonomicStatus']?.toLowerCase();
            temp['sourceDatabase'] = result['accordingTo'];
            temp['authorString'] = result['authorship'];
        }
        finalResult.add(temp);
        println "===========PARSED RESULT ======== " + finalResult
        return finalResult;
    }

    def getNamesFromTaxon(params){
        log.debug params
        def sql = new Sql(dataSource)
        def sqlStr, rs
        def classSystem = params.classificationId.toLong()
        def parentId = params.parentId
        def limit = params.limit ? params.limit.toInteger() : 1000
        def offset = params.offset ? params.limit.toLong() : 0
        if(!parentId) {
            sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name, s.path as path, t.is_flagged as isflagged, t.flagging_reason as flaggingreason, ${classSystem} as classificationid, position as position \
                from taxonomy_registry s, \
                taxonomy_definition t \
                where \
                s.taxon_definition_id = t.id and "+
                (classSystem?"s.classification_id = :classSystem and ":"")+
                "t.rank = 0";
            rs = sql.rows(sqlStr, [classSystem:classSystem])
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def classification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
            def cl = Classification.read(classSystem.toLong());
            if(cl == classification) {
                def authorClass = Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
                rs.addAll(sql.rows(sqlStr, [classSystem:authorClass.id]));
            }
        } else {
            sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name,  s.path as path ,t.is_flagged as isflagged, t.flagging_reason as flaggingreason, ${classSystem} as classificationid, position as position \
                from taxonomy_registry s, \
                taxonomy_definition t \
                where \
                s.taxon_definition_id = t.id and "+
                (classSystem?"s.classification_id = :classSystem and ":"")+
                "s.path like '"+parentId+"%' " +
                "order by t.rank, t.name asc limit :limit offset :offset";
            rs = sql.rows(sqlStr, [classSystem:classSystem, limit:limit, offset:offset])
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def classification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
            def cl = Classification.read(classSystem.toLong());
            if(cl == classification) {
                def authorClass = Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
                rs.addAll(sql.rows(sqlStr, [classSystem:authorClass.id, limit:limit, offset:offset]));
            }
        }

        println "total result size === " + rs.size()
        
        def dirtyList = [:]
        def workingList = [:]
        def cleanList = [:]
        
        def accDL = [], accWL = [], accCL = []
        def synDL = [], synWL = [], synCL = []
        def comDL = [], comWL = [], comCL = []


        ///////////////////////////////
        rs.each {
            //NOT SENDING PATH
            //SENDING IDS as taxonid for synonyms and common names
            //def s1 = "select s.id as taxonid, ${it.rank} as rank, s.name as name , ${classSystem} as classificationid, s.position as position \
                //from synonyms s where s.taxon_concept_id = :taxonId";
            
            def s1 = "select s.id as taxonid, s.rank as rank, s.name as name ,s.is_flagged as isflagged, s.flagging_reason as flaggingreason, ${classSystem} as classificationid, s.position as position \
                from taxonomy_definition s, accepted_synonym acsy where s.id = acsy.synonym_id and acsy.accepted_id = :taxonId";

            def q1 = sql.rows(s1, [taxonId:it.taxonid])
            q1.each {
                println "==========TAXA IDS======= " + it.taxonid
                if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.DIRTY.value())){
                    synDL << it
                }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                    synWL << it
                }else{
                    synCL << it
                }
            }
            
            def s2 = "select c.id as taxonid, ${it.rank} as rank, c.name as name , ${classSystem} as classificationid, position as position \
                from common_names c where c.taxon_concept_id = :taxonId";

            def q2 = sql.rows(s2, [taxonId:it.taxonid])
            q2.each {
                if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.DIRTY.value())){
                    comDL << it
                }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                    comWL << it
                }else{
                    comCL << it
                }
            }
        }

        println "==========SYN DL============= " + synDL
        println "==========COM DL============= " + comDL
        ///////////////////////////////
        
        rs.each {
            if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.DIRTY.value())){
                accDL << it
            }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                accWL << it
            }else{
                accCL << it
            }
        }
        dirtyList['accDL'] = accDL
        dirtyList['synDL'] = synDL
        dirtyList['comDL'] = comDL
        workingList['accWL'] = accWL
        workingList['synWL'] = synWL
        workingList['comWL'] = comWL
        cleanList['accCL'] = accCL
        cleanList['synCL'] = synCL
        cleanList['comCL'] = comCL
        return [dirtyList:dirtyList, workingList:workingList, cleanList:cleanList]	
    }

    def getNameDetails(params){
        log.debug params
        if(params.nameType == '1') {
            def taxonDef = TaxonomyDefinition.read(params.taxonId.toLong())
            def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()), taxonDef);
            def result = taxonDef.fetchGeneralInfo()
            result['taxonId'] = params.taxonId;

            if(taxonReg) {
                result['taxonRegId'] = taxonReg.id?.toString()
                taxonReg.path.tokenize('_').each { taxonDefinitionId ->
                    def td = TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId));
                    result.put(TaxonomyRank.getTRFromInt(td.rank).value().toLowerCase(), td.name);
                }
            }
            result['synonymsList'] = getSynonymsOfTaxon(taxonDef);
            result['commonNamesList'] = getCommonNamesOfTaxon(taxonDef);
            /*def counts = getObvCKLCountsOfTaxon(taxonDef);
            result['countObv'] = counts['countObv'];
            result['countCKL'] = counts['countCKL'];
            result['countSp'] = getSpeciesCountOfTaxon(taxonDef);
            println "=========COUNTS============= " + counts
            */return result
        }else if(params.nameType == '2') {
            if(params.choosenName && params.choosenName != '') {
                //taxonId here is id of synonyms table
                def syn = SynonymsMerged.read(params.taxonId.toLong());
                def result = syn.fetchGeneralInfo();
                result[result['rank']] = params.choosenName;
                result['acceptedNamesList'] = getAcceptedNamesOfSynonym(syn);
                println "========SYNONYMS NAME DETAILS ===== " + result
                return result
            }    
        }else if(params.nameType == '3') {
            if(params.choosenName && params.choosenName != '') {
                //taxonId here is id of common names table
                def com = CommonNames.read(params.taxonId.toLong());
                def result = com.fetchGeneralInfo()
                result[result['rank']] = params.choosenName;
                result['acceptedNamesList'] = getAcceptedNamesOfCommonNames(params.choosenName);
                println "========SYNONYMS NAME DETAILS ===== " + result
                return result
            }    
        }
    }

    //Searches IBP accepted and synonym only in WORKING AND DIRTY LIST and NULL list
    List<ScientificName> searchIBP(String canonicalForm, String authorYear, NameStatus status, int rank, boolean searchInNull = false) {  
        //utilsService.cleanUpGorm(true);
        SEARCH_IBP_COUNTER ++;
        println "========SEARCH IBP CALLED======="
        println "======PARAMS FOR SEARCH IBP ===== " + canonicalForm +"--- "+authorYear +"--- "+ status + "=--- "+ rank;
        //Decide in what class to search TaxonomyDefinition/SynonymsMerged
        def res = [];
        def clazz;
        if(status == NameStatus.ACCEPTED) {
            clazz = TaxonomyDefinition.class;
        } else {
            clazz = SynonymsMerged.class; 
        }
        def res1 = [];
        println "====SEARCHING ON IBP IN CLASS ====== " + clazz
        clazz.withNewSession{
            //FINDING BY CANONICAL
            res1 = clazz.findAllWhere(canonicalForm:canonicalForm, status: status);
            if(searchInNull) {
                res = res1;
            } else {
                res1.each{
                    if(it.position != null) {
                        res.add(it)
                    }
                }
            }
            //CANONICAL ZERO MATCH OR SINGLE MATCH
            if(res.size() < 2) { 
                if(res.size() == 0 ) {
                    CAN_ZERO ++;
                    println "====CANONICAL - ZERO MATCH ====== "
                } else {
                    CAN_SINGLE ++;
                    println "====CANONICAL - SINGLE MATCH ====== "
                }
                return res;
            }
            //CANONICAL MULTIPLE MATCH
            else {
                //COUNTER INCREASE FOR CANONICAL MULTIPLE
                CAN_MULTIPLE ++;
                //FINDING BY VERBATIM
                if(!authorYear) authorYear = '';
                res1 = clazz.findAllWhere(name: (canonicalForm + " " + authorYear), status: status);
                if(searchInNull) {
                    res = res1;
                } else {
                    res1.each{
                        if(it.position != null) {
                            res.add(it)
                        }
                    }
                }
                //VERBATIM SINGLE MATCH
                if(res.size() == 1) {
                    //COUNTER INCREASE FOR prune to 1 result
                    AFTER_CAN_MULTI_SINGLE ++;
                    println "====VERBATIM - SINGLE MATCH ====== "
                    return res;
                }

                //VERBATIM ZERO MATCH/MULTIPLE MATCH
                else if(res.size() > 2 || res.size() == 0) {
                    //FINDING BY VERBATIM + RANK
                    res1 = clazz.findAllWhere(name:(canonicalForm + " " + authorYear),rank: rank, status: status);
                    if(searchInNull) {
                        res = res1;
                    } else {
                        res1.each{
                            if(it.position != null) {
                                res.add(it)
                            }
                        }
                    }
                    //VERBATIM + RANK ZERO MATCH
                    if(res.size() == 0) {
                        if(authorYear) {
                            println "====TRYING VERBATIM + RANK - ZERO MATCH AND ALSO HAS AUTHOR YEAR"
                            AFTER_CAN_MULTI_ZERO ++;
                            return res;
                        }
                        else {
                            //FINDING BY CANONICAL + RANK
                            println "====TRYING VERBATIM + RANK - ZERO MATCH & NO AUTHOR YEAR - SO MATCHED ON CANONICAL + RANK"
                            res1 = clazz.findAllWhere(canonicalForm:canonicalForm ,rank: rank, status: status);
                            if(searchInNull) {
                                res = res1;
                            } else {
                                res1.each{
                                    if(it.position != null) {
                                        res.add(it)
                                    }
                                }
                            }
                            res1.each{
                                if(it.position != null) {
                                    res.add(it)
                                }
                            }
                            if(res.size() == 0) {AFTER_CAN_MULTI_ZERO ++;}
                            else if(res.size() == 1) {AFTER_CAN_MULTI_SINGLE ++;}
                            else {AFTER_CAN_MULTI_MULTI ++;}
                            
                            return res;
                        }
                    }
                    //VERBATIM + RANK SINGLE MATCH
                    if(res.size() == 1) {
                        //COUNTER INCREASE FOR prune to 1 result
                        AFTER_CAN_MULTI_SINGLE ++;
                        println "====TRYING VERBATIM + RANK - SINGLE MATCH"
                        return res;
                    }

                    //VERBATIM + RANK MULTIPLE MATCH
                    else {
                        AFTER_CAN_MULTI_MULTI ++;
                        println "====TRYING VERBATIM + RANK - MULTIPLE MATCH"
                        return res;
                    }
                }
            }
            return res;
        }
    }
    
    List searchIBPResults(String canonicalForm, String authorYear, NameStatus status, rank) {
        def res = searchIBP(canonicalForm, authorYear, status, rank) //TaxonomyDefinition.findAllByCanonicalForm(canonicalForm);
        def finalResult = []
        res.each { 
            def taxonConcept = TaxonomyDefinition.get(it.id.toLong());
            def temp = [:]
            temp['taxonId'] = it.id
            temp['externalId'] = it.id
            temp['name'] = it.canonicalForm
            temp['rank'] = TaxonomyRank.getTRFromInt(it.rank).value().toLowerCase()
            temp['nameStatus'] = it.status.value().toLowerCase()
            temp['group'] = groupHandlerService.getGroupByHierarchy(taxonConcept, taxonConcept.parentTaxon()).name
            temp['sourceDatabase'] = it.viaDatasource?it.viaDatasource:''
            finalResult.add(temp);
        }
        println "====RESULT FROM IBP==== " + finalResult
        return finalResult 
    }


	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// COL Migration related /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
    def populateInfoFromCol(File sourceDir){
        if(!sourceDir.exists()){
            log.debug "Source dir does not exist. ${sourceDir} Aborting now..." 
            return
        }

        curateName(new File(sourceDir, TaxonomyDefinition.class.simpleName), TaxonomyDefinition.class)
        //curateName(new File(sourceDir, Synon//TaxonomyDefinition.findAllByCanonicalForm(canonicalForm)yms.class.simpleName), Synonyms.class)
    }

    void curateName(File domainSourceDir, domainClass){
        if(!domainSourceDir.exists()){
            log.debug "Source dir does not exist. ${domainSourceDir} Aborting now..."
            return
        }

        long offset = 0
        int i = 0
        while(true && (offset == 0)){
            List tds = domainClass.list(max: BATCH_SIZE, offset: offset, sort: "rank", order: "desc")
            tds.each {
                log.debug  it.rank +  "    " + it.id + "   " +  it.canonicalForm
            }
            if(tds.isEmpty()){
                break
            }
            offset += BATCH_SIZE
            tds.each {
                curateName(it, domainSourceDir);
            }
        }
    }

    void curateName (ScientificName sciName, File domainSourceDir) {
        File f = new File(domainSourceDir, "" + sciName.id + ".xml")
        log.debug  "===== starting " + f
        List colData = processColData( f );
        curateName(sciName, colData);
    }

    void curateName (ScientificName sciName) {
        List res = searchCOL(sciName.canonicalForm, 'name');
        curateName(sciName, res);
    }

    void curateName (ScientificName sciName, List colData) {
        //println "================LIST OF COL DATA=========================== " + colData
        log.debug "=========== Curating name ${sciName} with col data ${colData}"
        def acceptedMatch;
        int colDataSize = colData.size();
        String dirtyListReason;
        if(!colData) return;

        //check if this is a single direct match
        if(colData.size() == 1 ) {
            //Reject all (IBP)scientific name -> (CoL) common name matches (leave for curation).
            if(sciName.status != NameStatus.COMMON && colData[0].nameStatus == NamesMetadata.COLNameStatus.COMMON.value()) {
                //reject ... position remains DIRTY
                dirtyListReason = "[REJECTING AS NAME IS COMMON NAME]. So leaving this name for curation"
                log.debug "[REJECTING AS NAME IS COMMON NAME] ${sciName} is a sciname but it is common name as per COL. So leaving this name for curation"
                sciName.noOfCOLMatches = colDataSize;
                sciName.position = NamesMetadata.NamePosition.DIRTY;
                sciName.dirtyListReason = dirtyListReason;
                if(!sciName.hasErrors() && sciName.save(flush:true)) {
                } else {
                    sciName.errors.allErrors.each { log.error it }
                }
                return;
            } else {
                log.debug "[CANONICAL : SINGLE MATCH] There is only a single match on col for this name. So accepting name match"
                acceptedMatch = colData[0]
                def  colMatchVerbatim = acceptedMatch.name + " " + acceptedMatch.authorString
                NamesParser namesParser = new NamesParser();
                def parsedNames = namesParser.parse([colMatchVerbatim]);
                colMatchVerbatim = parsedNames[0].normalizedForm;
                acceptedMatch['parsedName'] = parsedNames[0];
                acceptedMatch['parsedRank'] = XMLConverter.getTaxonRank(acceptedMatch.rank);
                println "============ACCEPTED MATCH ======= " + acceptedMatch
            }
        } else {
            if(sciName.status != NameStatus.COMMON) {
                log.debug "[CANONICAL : MULTIPLE MATCHES] Removing names with status as common name if IBP name is not a common name"
                def colDataTemp = [];
                colData.each {
                    if(it['nameStatus'] != NamesMetadata.COLNameStatus.COMMON.value()) {
                        colDataTemp.add(it);
                    }
                }
                colData = colDataTemp
            }
            log.debug "[CANONICAL : MULTIPLE MATCHES] There are multiple matches on COL for this name. Trying to filter out."
            //multiple match case
            Map colNames = [:];
            NamesParser namesParser = new NamesParser();
            colData.each { colMatch ->
                def colMatchVerbatim = colMatch.name;
                /*if(colMatch.authorString) {
                    colMatchVerbatim = colMatch.name + " " + colMatch.authorString
                }*/
                def parsedNames = namesParser.parse([colMatchVerbatim]);
                colMatchVerbatim = parsedNames[0].normalizedForm;
                colMatch['parsedName'] = parsedNames[0];
                colMatch['parsedRank'] = XMLConverter.getTaxonRank(colMatch.rank);
                if(!colNames[colMatchVerbatim]) {
                    colNames[colMatchVerbatim] = [];
                }
                colNames[colMatchVerbatim] << colMatch;
            }
            if(!colNames[sciName.normalizedForm]) {
                log.debug "[VERBATIM : NO MATCH] No verbatim match for ${sciName.name}"
                int noOfMatches = 0;
                log.debug "Comparing now with CANONICAL + RANK"
                def multiMatches = [];
                colData.each { colMatch ->
                    if(colMatch.canonicalForm == sciName.canonicalForm && colMatch.parsedRank == sciName.rank) {
                        noOfMatches++;
                        acceptedMatch = colMatch;
                        multiMatches.add(colMatch)
                    }
                }
                if(noOfMatches != 1) {
                    log.debug "[CANONICAL+RANK : NO MATCH] No single match on canonical+rank... leaving name for manual curation"
                    dirtyListReason = "[CANONICAL+RANK : NO MATCH] No single match on canonical+rank... leaving name for manual curation"
                    acceptedMatch = null;
                    //
                    if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                        log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                        noOfMatches = 0
                        List parentTaxons = sciName.immediateParentTaxonCanonicals() ;
                        println "-==IMMEDIATE TAXONS == " + parentTaxons
                        multiMatches.each { colMatch ->
                            println "COL MATCH PARENT TAXON == " + colMatch.parentTaxon
                            if(parentTaxons.contains(colMatch.parentTaxon)){
                                noOfMatches++;
                                acceptedMatch = colMatch
                            }
                        }
                        if(noOfMatches == 1) {
                            log.debug "[PARENT TAXON MATCH : SINGLE MATCH]  Accepting ${acceptedMatch}"
                        } else {
                            acceptedMatch = null;
                            dirtyListReason = "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                        }
                    }
                } else {
                    log.debug "[CANONICAL+RANK : SINGLE MATCH] Canonical ${sciName.canonicalForm} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                }
            }
            else if(colNames[sciName.normalizedForm].size() == 1) {
                //generate and compare verbatim. If verbatim matches with a single match accept. 
                acceptedMatch = colNames[sciName.normalizedForm][0]
                log.debug "[VERBATIM : SINGLE MATCH] Verbatim ${sciName.name} matches single entry in col matches. Accepting ${acceptedMatch}"
            } else {
                //checking only inside all matches of verbatim
                log.debug "[VERBATIM: MULTIPLE MATCHES] There are multiple col matches with canonical and just verbatim .. so checking with verbatim + rank ${sciName.rank}"
                int noOfMatches = 0;
                colNames[sciName.normalizedForm].each { colMatch ->
                    //If Verbatims match with multiple matches, then match with verbatim+rank.
                    //println colMatch
                    //println sciName.rank
                    def multiMatches2 = []
                    if(colMatch.parsedName.normalizedForm == sciName.normalizedForm && colMatch.parsedRank == sciName.rank) {
                        noOfMatches++;
                        acceptedMatch = colMatch;
                        multiMatches2.add(colMatch)
                    }
                }
                if(noOfMatches == 1) {
                    //acceptMatch
                    log.debug "[VERBATIM+RANK : SINGLE MATCH] Verbatim ${sciName.name} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                } else if(noOfMatches == 0) {
                    log.debug "[VERBATIM+RANK : NO MATCH] No match on verbatim + rank"
                    acceptedMatch = null;
                    //If verbatim shows no match, and the original has no author year, compare Canonical+ rank.  If matched with single match exists accept match. 
                    if(sciName.authorYear) {
                    
                        //If original has author year and no match exists, leave for curation (if author info exists and only canonical+rank match is considered, errors may occur eg: Aq matched with Ax, Ay and Az)(comparing hierarchies to further match will not help as a single name on IBP can have multiple hierarchies).
                        log.debug "As there is author year info .. leaving name for manual curation"
                        dirtyListReason = "[VERBATIM+RANK : NO MATCH] - As there is author year info .. leaving name for manual curation"
                    } else {
                        //comparing Canonical + rank
                        log.debug "Comparing now with canonical + rank"
                        noOfMatches = 0;
                        def multiMatches = [];
                        colNames[sciName.normalizedForm].each { colMatch ->
                            //If no match exists with Verbatim+rank and there is no author year info then match with canonical+rank.
                            if(colMatch.parsedName.canonicalForm == sciName.canonicalForm && colMatch.parsedRank == sciName.rank) {
                                noOfMatches++;
                                acceptedMatch = colMatch;
                                multiMatches.add(colMatch);
                            }
                        }
                        if(noOfMatches == 1) {
                            //acceptMatch
                            log.debug "[CANONICAL+RANK : SINGLE MATCH] Canonical ${sciName.canonicalForm} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                        } else {
                            acceptedMatch = null;
                            if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                                log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                                noOfMatches = 0
                                List parentTaxons = sciName.immediateParentTaxonCanonicals() ;
                                println "-==IMMEDIATE TAXONS == " + parentTaxons
                                multiMatches.each { colMatch ->
                                    println "COL MATCH PARENT TAXON == " + colMatch.parentTaxon
                                    if(parentTaxons.contains(colMatch.parentTaxon)){
                                        noOfMatches++;
                                        acceptedMatch = colMatch
                                    }
                                }
                                if(noOfMatches == 1) {
                                    log.debug "[PARENT TAXON MATCH : SINGLE MATCH]  Accepting ${acceptedMatch}"
                                } else {
                                    acceptedMatch = null;
                                    dirtyListReason = "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                                }
                            }

                        }
                    }
                } else if (noOfMatches > 1) {
                    acceptedMatch = null;
                    log.debug "[VERBATIM+RANK: MULTIPLE MATCHES] Multiple matches even on verbatim + rank. So leaving name for manual curation"
                    dirtyListReason = "[VERBATIM+RANK: MULTIPLE MATCHES] Multiple matches even on verbatim + rank. So leaving name for manual curation"
                    if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                        log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                        noOfMatches = 0
                        List parentTaxons = sciName.immediateParentTaxonCanonicals() ;
                        println "-==IMMEDIATE TAXONS == " + parentTaxons
                        multiMatches2.each { colMatch ->
                            println "COL MATCH PARENT TAXON == " + colMatch.parentTaxon
                            if(parentTaxons.contains(colMatch.parentTaxon)){
                                noOfMatches++;
                                acceptedMatch = colMatch
                            }
                        }
                        if(noOfMatches == 1) {
                            log.debug "[PARENT TAXON MATCH : SINGLE MATCH]  Accepting ${acceptedMatch}"
                        } else {
                            acceptedMatch = null;
                            dirtyListReason = "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                        }
                    }
                }

            }
        }
       
        if(acceptedMatch) {
            println "================ACCEPTED MATCH=========================== " + acceptedMatch
            if(acceptedMatch.parsedRank != sciName.rank) {
                log.debug "There is an acceptedMatch ${acceptedMatch} for ${sciName}. But REJECTED AS RANK WAS CHANGING"
                sciName.noOfCOLMatches = colDataSize;
                sciName.position = NamesMetadata.NamePosition.DIRTY;
                sciName.dirtyListReason = "REJECTED AS RANK WAS CHANGING"
                if(!sciName.hasErrors() && sciName.save(flush:true)) {
                } else {
                    sciName.errors.allErrors.each { log.error it }
                }
                return;
            }
            log.debug "There is an acceptedMatch ${acceptedMatch} for ${sciName}. Updating status, rank and hieirarchy"
            processDataForMigration(sciName, acceptedMatch, colDataSize);            
        } else {
            log.debug "[NO MATCH] No accepted match in colData. So leaving name in dirty list for manual curation"
            sciName.noOfCOLMatches = colDataSize;
            sciName.position = NamesMetadata.NamePosition.DIRTY;
            sciName.dirtyListReason = dirtyListReason;
            if(!sciName.hasErrors() && sciName.save(flush:true)) {
            } else {
                sciName.errors.allErrors.each { log.error it }
            }
        }
    }

    def processDataForMigration(ScientificName sciName, Map acceptedMatch, colDataSize) {
        sciName.tempActivityDescription = "";
        /*def upAt = updateAttributes(sciName, acceptedMatch);
        println  "====UP AT == " + upAt 
        if(upAt.isDeleted) {
            log.debug "MARKED AS DELETED ${sciName}"
            return;
        }*/
        sciName = updateAttributes(sciName, acceptedMatch);
        sciName = updateStatus(sciName, acceptedMatch).sciName;
        println "========THE SCI NAME======== " + sciName
        println "=======AFTER STATUS======== " + sciName.status +"==== "+  acceptedMatch.parsedRank
        updateRank(sciName, acceptedMatch.parsedRank);            
        //WHY required here??
        //addIBPHierarchyFromCol(sciName, acceptedMatch);

        //already updated in update attributes
        //updatePosition(sciName, NamesMetadata.NamePosition.WORKING);
        
        sciName.noOfCOLMatches = colDataSize;
        /*else if(sciName.status == NameStatus.ACCEPTED) {
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def cl = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
            def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(cl, sciName);
            taxonService.moveToWKG([taxonReg]);
        }*/
        println "=======SCI NAME POSITION ========== " + sciName.position
        println "=====SCI NAME ==== " + sciName
        sciName = sciName.merge();
        if(!sciName.hasErrors() && sciName.save(flush:true)) {
            println sciName.position
            log.debug "Saved sciname ${sciName}"
            if(sciName.tempActivityDescription != "") {
                def feedInstance = activityFeedService.addActivityFeed(sciName, sciName, springSecurityService.currentUser?:SUser.read(1L), ActivityFeedService.TAXON_NAME_UPDATED, sciName.tempActivityDescription);
                sciName.tempActivityDescription = "";
            }
            namesAfterSave[sciName.id] = sciName.position.value();
            utilsService.cleanUpGorm(true);
        } else {
            sciName.errors.allErrors.each { log.error it }
        }

    }
   
    def processDataFromUI(ScientificName sciName, Map acceptedMatch) {
        sciName.tempActivityDescription = "";
        /*def upAt = updateAttributes(sciName, acceptedMatch);
        println  "====UP AT == " + upAt 
        if(upAt.isDeleted) {
            log.debug "MARKED AS DELETED ${sciName}"
            return;
        }*/
        sciName = updateAttributes(sciName, acceptedMatch);
        def result =  updateStatus(sciName, acceptedMatch);
        sciName = result.sciName;
        println "=======AFTER STATUS======== " + sciName.status +"==== "+  acceptedMatch.parsedRank
        updateRank(sciName, acceptedMatch.parsedRank);            
        //WHY required here??
        //addIBPHierarchyFromCol(sciName, acceptedMatch);            
        updatePosition(sciName, NamesMetadata.NamePosition.WORKING);    
        //taxonService.moveToWKG([taxonReg]);
        println "=======SCI NAME POSITION ========== " + sciName.position
        println "=====SCI NAME ==== " + sciName
        sciName = sciName.merge();
        if(!sciName.hasErrors() && sciName.save(flush:true)) {
            log.debug "Saved sciname ${sciName}"        
            def feedInstance = activityFeedService.addActivityFeed(sciName, sciName, springSecurityService.currentUser?:SUser.read(1L), ActivityFeedService.TAXON_NAME_UPDATED, sciName.tempActivityDescription);
            sciName.tempActivityDescription = "";
            //utilsService.cleanUpGorm(true);
        } else {
            sciName.errors.allErrors.each { log.error it }
        }
        return result;
    }

    //Handles name moving from accepted to synonym & vice versa
    //also updates IBP Hierarchy if no status change and its accepted name
    def updateStatus(ScientificName sciName, Map colMatch) {
        println "===========SCIENTIFIC NAME === " + sciName
        println "===========COL STATUS === " + colMatch.nameStatus
        def result = [:];
        if(!sciName.status.value().equalsIgnoreCase(colMatch.nameStatus)) {
            log.debug "Changing status from ${sciName.status} to ${colMatch.nameStatus}"
            //NOW WILL BE FLAGGING IT
            //check if there is another taxon with same name and rank and changed status
            /*
            boolean duplicateExists = checkForDuplicateSciNameOnStatusAndRank(sciName, getNewNameStatus(colMatch.nameStatus), colMatch.parsedRank);
            if(duplicateExists) {
                log.debug "Changing status is resulting in a duplicate name with same status and rank... so leaving name for curation"
                return sciName;
            }
            */
            //changing status
            sciName.tempActivityDescription += createNameActivityDescription("IBP status", sciName.status.value(), colMatch.nameStatus);
            def newStatus = getNewNameStatus(colMatch.nameStatus);
            println "===========NEW STATUS  === " + newStatus
            switch(newStatus) {
                case NameStatus.ACCEPTED :
                    result = changeSynonymToAccepted(sciName, colMatch);
                    sciName = result.lastTaxonInIBPHierarchy;        //changeSynonymToAccepted(sciName, colMatch);
                    result.sciName = sciName
                    /*    
                    def result = speciesService.deleteSynonym(sciName.id);
                    if(!result.success) {
                        log.debug "Error in deleting synonym ${sciName}. Not updating status."
                    }
                    sciName = saveAcceptedName(colMatch);
                    */
                    break;
                case NameStatus.SYNONYM :                     
                    sciName = changeAcceptedToSynonym(sciName, colMatch);
                    result.sciName = sciName
                    /*//delete the name from taxonDefinition table and add it to synonyms table
                    taxonService.deleteTaxon(sciName);
                    def synonym;
                    //if the changed status is Synonym and its accepted name doesn't exist create it
                    colMatch.acceptedNamesList.each { colAcceptedNameData ->
                        ScientificName acceptedName = saveAcceptedName(colAcceptedNameData);
                       //update acceptedName property for this synonym  
                        synonym = saveSynonym(sciName, acceptedName);
                    }*/
                    break;
            }
            //handling inside the cases only
            //sciName.status = newStatus;
        } else {        //Do when status does not change and its accepted
            println "=========STATUS SAME  === "
            if(sciName.status == NameStatus.ACCEPTED) {
                colMatch.curatingTaxonId = sciName.id;
                //sciName = updateAttributes(sciName, colMatch)
                result = addIBPHierarchyFromCol(colMatch);
                sciName = result.lastTaxonInIBPHierarchy; 
                println "======STATUS MEIN SCINAME==== " + sciName
                result.sciName = sciName

            } else {
                //sciName = updateAttributes(sciName, colMatch)
                //For synonym work on its accepted name
                colMatch.acceptedNamesList.each { colAcceptedNameData ->
                    println "======SAVING THIS ACCEPTED NAME ==== " + colAcceptedNameData;
                    //TODO Pass on id information of last nodesciName.errors.allErrors.each { log.error it }
                    colAcceptedNameData.curatingTaxonId = sciName.id;
                    ScientificName acceptedName = saveAcceptedName(colAcceptedNameData);
                    acceptedName.addSynonym(sciName);
                    println "======SAVED THIS ACCEPTED NAME & added synonym also ==== " + acceptedName;
                    //add old synonyms to this new accepted name
                }
                result.sciName = sciName
            }
        }
		result.remove("taxonRegistry");
        return result;
    }

    private boolean checkForDuplicateSciNameOnStatusAndRank(ScientificName sciName, NameStatus nameStatus, int rank) {
        def res = searchIBP(sciName.canonicalForm, sciName.authorYear, nameStatus, rank)
        println "========RESULT FROM SEARCH IBP============ " + res;
        /*def taxonConcept = sciName.class.withCriteria() {
            ne ('id', sciName.id)
            eq ('status', nameStatus)
            eq ('rank', rank)
        }
        if(taxonConcept)  return true;
        return false;
        */
        if(res.size() == 0) return false;
        else return true;
    }

    private NameStatus getNewNameStatus(String nameStatus) {
        if(!nameStatus) return null;
		for(NameStatus s : NameStatus){
			if(s.value().equalsIgnoreCase(nameStatus))
				return s
		}
        return null;
    }
    
    public COLNameStatus getCOLNameStatus(String colNameStatus) {
        if(!colNameStatus) return null;
		for(COLNameStatus s : COLNameStatus){
			if(s.value().equalsIgnoreCase(colNameStatus))
				return s
		}
        return null;
    }
    
    private List<ScientificName> checkIfSciNameExists(Map colAcceptedNameData) {
        NameStatus status = getNewNameStatus(colAcceptedNameData.nameStatus);
        def rank = XMLConverter.getTaxonRank(colAcceptedNameData.rank);
        println "======PARAMS FOR SEARCH IBP ===== " + colAcceptedNameData.canonicalForm +" =--- "+colAcceptedNameData.authorString +" =--- "+ status + " =--- "+ rank;
        def res = searchIBP(colAcceptedNameData.canonicalForm, colAcceptedNameData.authorString, status , rank);
        println "========RESULTS of SEARCH IBP ===== " + res
        return res;
    }
    
    ScientificName saveAcceptedName(Map colAcceptedNameData) {
        //List<ScientificName> acceptedNameList = checkIfSciNameExists(colAcceptedNameData);
        ScientificName acceptedName = null;
        //if(acceptedNameList.size() == 0) {
            //create acceptedName
            log.debug "Creating/Updating accepted name of this synonym"
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def result = addIBPHierarchyFromCol(colAcceptedNameData);
            acceptedName = result.lastTaxonInIBPHierarchy;
        //}
        return acceptedName;
    }

    ScientificName saveSynonym(ScientificName sciName, ScientificName acceptedName) {
        //check if another synonym exists with same name relationship and for same acceptedName
        def synonyms = Synonym.withCriteria() {
            eq('name', sciName.canonicalForm)
            eq('relationship', ScientificName.SYNONYM)
            eq('taxonConcept', acceptedName)
        }

        if(!synonyms) {
            def result = speciesService.updateSynonym(null, null, ScientificName.RelationShip.SYNONYM, sciName.name, ['taxonId':acceptedName.id]);  
            return result.dataInstance;
        } else {
            log.debug "Already a synonym exists with same name for this acceptedName"
            return synonyms;
        }
    }

    private void updateRank(ScientificName sciName, int rank) {
        if(sciName.rank != rank) {
            log.debug "Updating rank from ${sciName.rank} to ${rank}"
            sciName.tempActivityDescription += createNameActivityDescription("Rank ", TaxonomyRank.getTRFromInt(sciName.rank).value(), TaxonomyRank.getTRFromInt(rank).value());
            sciName.rank = rank;
        }
    }
        
    //A scientific name was also passed to this function but not used - so removed
    private def  addIBPHierarchyFromCol(Map colAcceptedNameData) {
        //  Because - not complete details of accepted name coming
        //  but its id is present - so searching COL based on ID
        //  Might happen when name changes from accepeted to synonym
        if(!colAcceptedNameData.kingdom && colAcceptedNameData.id) {
            def temp = colAcceptedNameData.curatingTaxonId
            colAcceptedNameData = searchCOL(colAcceptedNameData.id, 'id')[0];
            colAcceptedNameData.curatingTaxonId = temp;
        }
        def fieldsConfig = grailsApplication.config.speciesPortal.fields
        def classification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
        Map taxonRegistryNamesTemp = fetchTaxonRegistryData(colAcceptedNameData).taxonRegistry;
        println "======USE THIS ==== " + taxonRegistryNamesTemp
        List taxonRegistryNames = [];
        taxonRegistryNamesTemp.each { key, value ->
            taxonRegistryNames[Integer.parseInt(key)] = value;
        }

        log.debug "Adding ${classification} ${taxonRegistryNames}"
        SUser contributor = springSecurityService.currentUser?:SUser.read(1L) //findByName('admin');
        //to match the input format
        //getTaxonHierarchy() XMLConverter
        def metadata1 = [:]
		metadata1['authorString'] = colAcceptedNameData['authorString']
		metadata1['source'] = colAcceptedNameData['source'] //col
		metadata1['via'] = colAcceptedNameData['sourceDatabase']
        colAcceptedNameData['metadata'] = metadata1
        println "=====T R N======= " + taxonRegistryNames
        println colAcceptedNameData.abortOnNewName;
        println colAcceptedNameData.fromCOL;
        println colAcceptedNameData.spellCheck
        //From UI
        //def result = taxonService.addTaxonHierarchy(colAcceptedNameData.name, taxonRegistryNames, classification, contributor, null, colAcceptedNameData.abortOnNewName, colAcceptedNameData.fromCOL.toBoolean(), colAcceptedNameData);
        //From migration script
        def result = taxonService.addTaxonHierarchy(colAcceptedNameData.name, taxonRegistryNames, classification, contributor, null, false, true, colAcceptedNameData);
        println result
        return result;
    }

    boolean updatePosition(ScientificName sciName, NamePosition position) {
        namesInWKG.add(sciName.id)
        namesBeforeSave[sciName.id] = "Working"
        log.debug "Updating position from ${sciName.position} to ${position}"
        sciName.tempActivityDescription += createNameActivityDescription("Position", sciName.position?.value(), position.value());
        sciName.position = position;
    }

    List processColData(File f) {
        if(!f.exists()){
            log.debug "File not found skipping now..."
            return
        }
        try{
            def results = new XmlParser().parse(f)

            String errMsg = results.'@error_message'
            int resCount = Integer.parseInt((results.'@total_number_of_results').toString()) 
            if(errMsg != ""){
                log.debug "Error in col response " + errMsg
                return
            }

            /*if(resCount != 1 ){
              log.debug "Multiple result found [${resCount}]. so skipping this ${f.name} for manual curation"
              return
              }*/

            //Every thing is fine so now populating CoL info
            List res = responseAsMap(results, "id")

            log.debug "================   Response map   =================="
            log.debug res
            /*log.debug "=========ui map ==========="
            def newRes = fetchTaxonRegistryData(res[0])
            //newRes['nameDbInstance'] = sciName
            log.debug newRes
            log.debug "================   Response map   =================="
             */
            return res
        } catch(Exception e) {
            return;
        }
    }

    private Map fetchTaxonRegistryData(Map m) {
        def result = [:]
        def res = [:]

        result['taxonRegistry.0'] = res['0'] = m['kingdom']
        result['taxonRegistry.1'] = res['1'] = m['phylum']
        result['taxonRegistry.2'] = res['2'] = m['class']
        result['taxonRegistry.3'] = res['3'] = m['order']
        result['taxonRegistry.4'] = res['4'] = m['superfamily']
        result['taxonRegistry.5'] = res['5'] = m['family']
        result['taxonRegistry.6'] = res['6'] = m['subfamily']
        result['taxonRegistry.7'] = res['7'] = m['genus']
        result['taxonRegistry.8'] = res['8'] = m['subgenus']
        if(m['rank'] == 'species'){
            result['taxonRegistry.9'] = res['9'] = m['species'] + " " + m['authorString']
        } else {
            result['taxonRegistry.9'] = res['9'] = m['species'];    
        }
        if(m['rank'] == 'infraspecies'){
            def authStr = searchCOL(m.id_details[m['species']], "id")[0].authorString;
            result['taxonRegistry.9'] = res['9'] = m['genus'] + " " +m['species'] + " " + authStr;    
            m.id_details[m['genus'] + " " +m['species']] = m.id_details[m['species']]
            result['taxonRegistry.10'] = res['10'] = m['infraspecies'] + " " + m['authorString'];
        } else {
            result['taxonRegistry.10'] = res['10'] = m['infraspecies'];     
        }
        result['taxonRegistry'] = res;
        result['reg'] = m["taxonRegId"]          //$('#taxaHierarchy option:selected').val();
        result['classification'] = 817; //for author contributed



        def metadata1 = [:]
        metadata1['name'] = m['name']
        metadata1['rank'] = m['rank']
        metadata1['authorString'] = m['authorString']
        metadata1['nameStatus'] = m['nameStatus']
        metadata1['source'] = m['source'] //col
        metadata1['via'] = m['sourceDatabase']
        metadata1['id'] = m['externalId']
        result['metadata'] = metadata1;

        return result;
    }

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
    def getSynonymsOfTaxon(TaxonomyDefinition taxonConcept) {
        def res = taxonConcept.fetchSynonyms(); //Synonyms.findAllByTaxonConcept(taxonConcept);
        def result = []
        res.each {
            def temp = [:]
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }
    
    def getAcceptedNamesOfSynonym(SynonymsMerged syn) {
        //def r = Synonyms.findAllByName(synName);
        def res = syn.fetchAcceptedNames();  //[]
        def result = []
        res.each {
            def temp = [:]
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }

    def getAcceptedNamesOfCommonNames(String comName) {
        def r = CommonNames.findAllByName(comName);
        def res = []
        r.each {
            res.add(it.taxonConcept);
        }
        def result = []
        res.each {
            def temp = [:]
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }

    def getCommonNamesOfTaxon(TaxonomyDefinition taxonConcept) {
        def res = CommonNames.findAllByTaxonConcept(taxonConcept);
        def result = []
        res.each {
            def temp = [:]
            println "======TEMP ==== " +temp
            temp['id'] = it.id.toString();
            temp['name'] = it.name;
            temp['source'] = it.viaDatasource;
            temp['language'] = it.language?it.language.name:'English';
            String contri = '';
            it.contributors.each {
                contri += it.name + ", "
            }
            if(contri != '') {
                contri = contri.substring(0,contri.lastIndexOf(','));
            }
            temp['contributors'] = contri; 
            println "======TEMP ==== " +temp
            result.add(temp);
        }
        return result
    }

    def getObvCKLCountsOfTaxon(TaxonomyDefinition taxonConcept) {
        def sql = new Sql(dataSource)
        def sqlStr;
        def countObv = 0, countCKL = 0;
        sqlStr = "select count(distinct o.id) from recommendation rec, recommendation_vote rv, observation o where rec.id = rv.recommendation_id and rv.observation_id = o.id and rec.taxon_concept_id ="+ taxonConcept.id.toString() +" and o.is_checklist = false and o.id = o.source_id";

        countObv =  sql.rows(sqlStr)[0].count
        println "=======COUNT OBV======== " + countObv

        sqlStr = "select count(distinct o.id) from recommendation rec, recommendation_vote rv, observation o where rec.id = rv.recommendation_id and rv.observation_id = o.id and rec.taxon_concept_id ="+ taxonConcept.id.toString() +" and o.is_checklist = true and o.id != o.source_id";

        countCKL =  sql.rows(sqlStr)[0].count
        println "=======COUNT CKL======== " + countCKL
        println "=====COUNTS=== " +countObv +"==== " + countCKL
        return ['countObv': countObv, 'countCKL': countCKL];
    }

    def getSpeciesCountOfTaxon(TaxonomyDefinition taxonConcept) {
        def taxonId = taxonConcept.id.toString();
        def sql = new Sql(dataSource)
        String sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '%!_"""+taxonId+"' escape '!'";
        
        def res1 = sql.rows(sqlStr)
        sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '%!_"""+taxonId+"!_%"+"' escape '!'";
        
        def res2 = sql.rows(sqlStr);

        sqlStr = """select * 
        from taxonomy_registry
        where 
        path like '"""+taxonId+"!_%"+"' escape '!'";
        
        def res3 = sql.rows(sqlStr);

        def taxonConcepts = res1.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())};
        taxonConcepts.add(res2.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())});
        taxonConcepts.add(res3.collect {TaxonomyDefinition.read(it.taxon_definition_id.toLong())});
        def speciesCount = Species.findAllByTaxonConceptInList(taxonConcepts).size();
        return speciesCount;
    }

    List searchTNRS(String input, String searchBy) {
        //http://tnrs.iplantc.org/tnrsm-svc/matchNames?retrieve=best&names=Mangifera

        def http = new HTTPBuilder()
        println "========TNRS SITE===== " + TNRS_SITE
        http.request( TNRS_SITE, GET, TEXT ) { req ->
            if(searchBy == 'name') {
                uri.path = TNRS_URI;
            } else {
                uri.path = TNRS_URI;
            }
            uri.query = [ retrieve:'best', names:input]
            headers.Accept = '*/*'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                println "========TNRS RESULT====== " + xmlText
                return responseFromTNRSAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }

    List responseFromTNRSAsMap(String xmlText , String searchBy) {
        def allResults = JSON.parse(xmlText).items
        println "============RESULT=============== " + allResults
        def finalResult = []
        allResults.each { result ->
            Map temp = new HashMap()
            temp['externalId'] = "" 
            temp['name'] = result['nameScientific'];
            if(searchBy == 'name') {
                temp['name'] = temp['name'] + " " + result['acceptedAuthor'];
            }
            temp['rank'] = result['rank']?result['rank'].toLowerCase() : "";
            temp['nameStatus'] = "";
            temp['sourceDatabase'] = result['url']? result['url'] : "";
            temp['group'] = result['kingdom']? result['kingdom']:"";
            //if(searchBy == 'id') {
            temp['kingdom'] = result['kingdom']; 
            temp['phylum'] = result['phylum']; 
            temp['order'] = result['order']; 
            temp['family'] = result['family']; 
            temp['class'] = result['class']; 
            temp['genus'] = result['genus']; 
            temp['species'] = result['species']; 
            temp['authorString'] = result['acceptedAuthor'];
            //} 
            finalResult.add(temp);
        }
        println "===========PARSED RESULT ======== " + finalResult
        return finalResult;
    }

    List searchEOL(String input, String searchBy) {
        //http://eol.org/api/search/1.0.json?q=Mangifera+indica&page=1&exact=true
        
        def http = new HTTPBuilder()
        println "========EOL SITE===== " + EOL_SITE
        println "========INPUT===== " + EOL_URI_ID + input+'.json'
        http.request( EOL_SITE, GET, TEXT ) { req ->
            if(searchBy == 'name') {
                uri.path = EOL_URI;
                uri.query = [ exact:'true', q :input]
            } else {
                uri.path = EOL_URI_ID + input+'.json';
                uri.query = [common_names :false, synonyms :false]
            }
            headers.Accept = '*/*'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                println "========TNRS RESULT====== " + xmlText
                return responseFromEOLAsMap(xmlText, searchBy);
            }
            response.'404' = { println '404 - Not found' }
        }
    }

    List responseFromEOLAsMap(String xmlText , String searchBy) {
        def allResults = JSON.parse(xmlText).results
        println "============RESULT=============== " + allResults
        def finalResult = []
        allResults.each { result ->
            Map temp = new HashMap()
            temp['externalId'] = result['id'] 
            temp['name'] = result['title'];
            temp['rank'] = result['rank']?result['rank'].toLowerCase() : "";
            temp['nameStatus'] = "";
            temp['sourceDatabase'] = result['link']? result['link'] : "";
            temp['group'] = result['kingdom']? result['kingdom']:"";
            //if(searchBy == 'id') {
            temp['kingdom'] = result['kingdom']; 
            temp['phylum'] = result['phylum']; 
            temp['order'] = result['order']; 
            temp['family'] = result['family']; 
            temp['class'] = result['class']; 
            temp['genus'] = result['genus']; 
            temp['species'] = result['species']; 
            temp['authorString'] = result['acceptedAuthor']?result['acceptedAuthor']:"" ;
            //} 
            finalResult.add(temp);
        }
        println "===========PARSED RESULT ======== " + finalResult
        return finalResult;
    }


    List searchWORMS(String input, String searchBy) {
        //http://www.marinespecies.org/aphia.php?p=taxlist&tName=Solea solea
        /*
        def soapClient = new SOAPClient("http://www.marinespecies.org/aphia.php?p=soap");
        def response = soapClient.send(SOAPAction:"matchAphiaRecordsByNames") {
            body {
                matchAphiaRecordsByNames(xmlns:"http://www.marinespecies.org") {
                    scientificnames("Solea solea")
                }
            }
        }
        println "======RESPONSE======== " + response
        */
    }

    public def changeSynonymToAccepted(ScientificName sciName,  Map colMatch) {
        //Remove as synonym from all accepted names
        sciName.removeAsSynonym();
        //sciName = updateAttributes(sciName, colMatch)
        colMatch.curatingTaxonId = sciName.id;
        colMatch.curatingTaxonStatus = sciName.status;
        //Change status and class for this row entry in database
        sciName = updateStatusAndClass(sciName, NameStatus.ACCEPTED)
        //Add IBP Hierarchy to this name
        //TODO Pass on id information of last node
        def result = addIBPHierarchyFromCol(colMatch)
        //sciName = result.lastTaxonInIBPHierarchy;
        return result;
    }
    
    public ScientificName changeAcceptedToSynonym(ScientificName sciName,  Map colMatch) {
        println "======PROCESS START ACCEPTED TO SYNONYM===="
        //Attach its synonyms to the new accepted suggested by COL
        def oldSynonyms = sciName.fetchSynonyms();
        println "======OLD SYNONYMS OF THAT ==== " + oldSynonyms
        //Remove as accepted name from all synonyms 
        sciName.removeAsAcceptedName();
        println "======REMOVED AS ACCEPTED NAME ==== "
        
        //sciName = updateAttributes(sciName, colMatch)
        //Change status and class for this row entry in database
        sciName = updateStatusAndClass(sciName, NameStatus.SYNONYM)
        println "======CHANGED STATUS AND CLASS ==== " + sciName.status +" ===== " + sciName.class
        oldSynonyms.add(sciName);
        //Save all the new accepted names or update its hierarchy
        colMatch.acceptedNamesList.each { colAcceptedNameData ->
            println "======SAVING THIS ACCEPTED NAME ==== " + colAcceptedNameData;
            colAcceptedNameData.curatingTaxonId = sciName.id;
            colAcceptedNameData.curatingTaxonStatus = sciName.status;
            //TODO Pass on id information of last nodesciName.errors.allErrors.each { log.error it }
            ScientificName acceptedName = saveAcceptedName(colAcceptedNameData);
            println "======SAVED THIS ACCEPTED NAME ==== " + acceptedName;
            //add old synonyms to this new accepted name
            oldSynonyms.each {
                acceptedName.addSynonym(it);
            }
            println "======ADDED OLD SYNONYMS ==== " + acceptedName.fetchSynonyms();
        }
        println "======Returning this NEW SYNONYM ==== " + sciName;
        return sciName;
    }

    ScientificName updateStatusAndClass(ScientificName sciName, NameStatus status) {
       	sciName = sciName.merge();
	//def sql =  Sql.newInstance(dataSource);
        String query = "";
        println "=======RUNNING SQL TO UPDATE CLASS=========="
        if(status == NameStatus.ACCEPTED) {
        	println "=======MAKING IT ACCEPTED=========="
            sciName.relationship = null;
            query = "update taxonomy_definition set class = 'species.TaxonomyDefinition' where id = " + sciName.id.toString();
		def session = sessionFactory.getCurrentSession()
def sql= session.createSQLQuery(query)
            sql.executeUpdate();
            println " ========executed query =="
            utilsService.cleanUpGorm(true);
            sciName = TaxonomyDefinition.get(sciName.id.toLong())
        } else {
        	println "=======MAKING IT SYNONYM=========="
query = "update taxonomy_definition set class = 'species.SynonymsMerged' where id = " + sciName.id.toString();
            def session = sessionFactory.getCurrentSession()
def sql= session.createSQLQuery(query)
            sql.executeUpdate();
            println " ========executed query =="
            utilsService.cleanUpGorm(true);
            sciName = SynonymsMerged.get(sciName.id.toLong())
            sciName.relationship = ScientificName.RelationShip.SYNONYM;
        }
        sciName.status = status;
        return sciName;
    }

    ScientificName updateAttributes(ScientificName sciName, Map colMatch) {
        TaxonomyDefinition.withNewSession {
            println "=========UPDATING ATTRIBUTES ========"
            NamesParser namesParser = new NamesParser();
            def name = sciName.canonicalForm + " " + colMatch.authorString
            def res1 = searchIBP(sciName.canonicalForm, colMatch.authorString, NameStatus.ACCEPTED , sciName.rank);
            def res2 = searchIBP(sciName.canonicalForm, colMatch.authorString, NameStatus.SYNONYM , sciName.rank);
            res2.addAll(res1);
            if((res2.size() > 1) || (res2.size() == 1 && res2[0].id != sciName.id)) {
                sciName.isFlagged = true;
                String flaggingReason = "The name clashes with an existing name on the portal.IDs- ";
                res2.each {
                    flaggingReason = flaggingReason + it.id.toString() + ", ";
                }
                println "########### Flagging becoz of Udating attributes ============== " + sciName
                res2.each {
                    if(it != sciName && it.isFlagged) {
                        it.flaggingReason = it.flaggingReason + " ### " + flaggingReason;
                        if(!it.save(flush:true)) {
                            it.errors.allErrors.each { log.error it }
                        }
                    }
                }
                sciName.flaggingReason = sciName.flaggingReason + " ### " + flaggingReason;
                if(!sciName.findSpeciesId()) {
                    sciName.isDeleted = true;
                }
            }
            def parsedNames = namesParser.parse([name]);
            println "=============PARSING THIS ========== " + name
            def pn = parsedNames[0];
            if(pn.canonicalForm) {
                println "============= " + pn.canonicalForm +"============= "+ pn.name
                sciName.tempActivityDescription += createNameActivityDescription("Canonical Name", sciName.canonicalForm, pn.canonicalForm);
                sciName.canonicalForm = pn.canonicalForm
                sciName.tempActivityDescription += createNameActivityDescription("Binomial Name", sciName.binomialForm, pn.binomialForm);
                sciName.binomialForm = pn.binomialForm
                sciName.tempActivityDescription += createNameActivityDescription("Normalized Name", sciName.normalizedForm, pn.normalizedForm);
                sciName.normalizedForm = pn.normalizedForm
                sciName.tempActivityDescription += createNameActivityDescription("Italicised Name", sciName.italicisedForm, pn.italicisedForm);
                sciName.italicisedForm = pn.italicisedForm
                sciName.tempActivityDescription += createNameActivityDescription("Verbatim Name", sciName.name, pn.name);
                sciName.name = pn.name
            }
            sciName.tempActivityDescription += createNameActivityDescription("Author Year", sciName.authorYear, colMatch.authorString);
            sciName.authorYear = colMatch.authorString;
            sciName.tempActivityDescription += createNameActivityDescription("COL Name Status", sciName.colNameStatus?.value(), colMatch.colNameStatus);
            sciName.colNameStatus = getCOLNameStatus(colMatch.colNameStatus);
            sciName.tempActivityDescription += createNameActivityDescription("Match Id", sciName.matchId, colMatch.externalId);
            sciName.matchId = colMatch.externalId;
            sciName.tempActivityDescription += createNameActivityDescription("Match Database Name", sciName.matchDatabaseName, colMatch.matchDatabaseName);
            sciName.matchDatabaseName = colMatch.matchDatabaseName;
            sciName.tempActivityDescription += createNameActivityDescription("Source Database", sciName.viaDatasource, colMatch.sourceDatabase);
            sciName.viaDatasource = colMatch.sourceDatabase;
            sciName.tempActivityDescription += createNameActivityDescription("Position", sciName.position?.value(), NamePosition.WORKING.value());
            sciName.position = NamePosition.WORKING;
            sciName = sciName.merge();
            println "==========SCI NAME AFTER MERGE ======== " + sciName
            if(!sciName.save(flush:true)) {
                sciName.errors.allErrors.each { log.error it }
            }
            println "=========DONE UPDATING ATTRIBUTES ========"
            return sciName //:sciName,isDeleted:false];
        }
    }

///////////////////OBV RECO NAMES/////////////////////////

    def curateRecoName(Recommendation reco , List colData) {      
        log.debug "Curating reco name ${reco.name} id ${reco.id} with col data ${colData}"
        def acceptedMatch = null;

        if(!colData) return;

        //check if this is a single direct match
        if(colData.size() == 1 ) {
            //Reject all (IBP)scientific name -> (CoL) common name matches (leave for curation).
            if(colData['nameStatus'] == NamesMetadata.COLNameStatus.COMMON.value()) {
                //reject ... position remains DIRTY
                log.debug "[REJECTING AS NAME IS COMMON NAME] ${reco.name} is a sciname but it is common name as per COL. So leaving this name for curation"
                return;
            } else {
                log.debug "[CANONICAL : SINGLE MATCH] There is only a single match on col for this name. So accepting name match"
                acceptedMatch = colData[0]
                def  colMatchVerbatim = acceptedMatch.name + " " + acceptedMatch.authorString
                NamesParser namesParser = new NamesParser();
                def parsedNames = namesParser.parse([colMatchVerbatim]);
                colMatchVerbatim = parsedNames[0].normalizedForm;
                acceptedMatch['parsedName'] = parsedNames[0];
                acceptedMatch['parsedRank'] = XMLConverter.getTaxonRank(acceptedMatch.rank);
                println "============ACCEPTED MATCH ======= " + acceptedMatch
            }
        }
        if(acceptedMatch) {
            println "================ACCEPTED MATCH=========================== " + acceptedMatch
            log.debug "There is an acceptedMatch ${acceptedMatch} for recommendation ${reco.name}. Updating link"
            //processRecoName(reco, acceptedMatch);
            ScientificName sciName;
            //Search on IBP that name with status
            NameStatus nameStatus = getNewNameStatus(acceptedMatch.nameStatus);
            int rank = acceptedMatch['parsedRank'];
            List res = searchIBP(acceptedMatch.canonicalForm, acceptedMatch.authorString, nameStatus, rank)
            if(res.size() == 0) {
                if(nameStatus == NameStatus.SYNONYM){
                    def acceptedNames = [];
                    acceptedMatch.acceptedNamesList.each { colAcceptedNameData ->
                        println "======SAVING THIS ACCEPTED NAME ==== " + colAcceptedNameData;
                        //TODO Pass on id information of last nodesciName.errors.allErrors.each { log.error it }
                        //colAcceptedNameData.curatingTaxonId = sciName.id;
                        ScientificName acceptedName;
                        if(acceptedMatch.fromUI) {
                            acceptedName = ScientificName.get(colAcceptedNameData.acceptedNameId)
                        } else {
                            acceptedName = saveAcceptedName(colAcceptedNameData);
                        }
                        acceptedNames.add(acceptedName)
                        println "======SAVED THIS ACCEPTED NAME ==== " + acceptedName;
                        //acceptedName.addSynonym(synonym);
                    }
                    def otherParams = ['taxonId':acceptedNames[0].id]
                    def result = speciesService.updateSynonym(null, null,ScientificName.RelationShip.SYNONYM.value(),acceptedMatch.canonicalForm +" "+ acceptedMatch.authorString , otherParams);
                    sciName = result.dataInstance;
                    acceptedNames.each { acceptedName ->
                        if(acceptedName != acceptedNames[0]){
                            acceptedName.addSynonym(sciName);
                        }
                    }
                } else {
                    sciName = saveAcceptedName(acceptedMatch);
                }
            } else if(res.size() == 1) {
                sciName = res[0]
            } else {
                //picking one by default
                sciName = res[0]
                //flagging recommendation
                reco.isFlagged = true;
                String flaggingReason = "The name clashes with an existing name on the portal.IDs- ";
                res.each {
                    flaggingReason = flaggingReason + it.id.toString() + ", ";
                }
                println "########### Flagging reco ============== "
                reco.flaggingReason = reco.flaggingReason + " ### " + flaggingReason;
            } 
            //processDataForMigration(sciName, acceptedMatch);            
            syncReco(reco, sciName);
            
        } else {
            log.debug "[NO MATCH] No accepted match in colData. So leaving name in dirty list for manual curation"
        }
    }

    def syncReco(Recommendation reco, TaxonomyDefinition taxDef) {
        reco.taxonConcept = taxDef;
        if(!reco.save()) {
            reco.errors.allErrors.each { log.error it }
        }
    }

    def getOrphanRecoNames() {
        def query = "from Recommendation as r where r.isScientificName = true and r.taxonConcept = null order by r.id"
        def recoList = Recommendation.findAll(query);
        def result = [];
        recoList.each{
            result.add([taxonid:it.id, name:it.name]);
        }
        return result;
    }

    def processRecoName(Recommendation reco, Map acceptedMatch) {
        ScientificName sciName;
        //Search on IBP that name with status
        NameStatus nameStatus = getNewNameStatus(acceptedMatch.nameStatus);
        int rank = acceptedMatch['parsedRank'];
        List res = searchIBP(acceptedMatch.canonicalForm, acceptedMatch.authorString, nameStatus, rank)
        if(res.size() == 0) {
            if(nameStatus == NameStatus.SYNONYM){
                def acceptedNames = [];
                acceptedMatch.acceptedNamesList.each { colAcceptedNameData ->
                    println "======SAVING THIS ACCEPTED NAME ==== " + colAcceptedNameData;
                    //TODO Pass on id information of last nodesciName.errors.allErrors.each { log.error it }
                    //colAcceptedNameData.curatingTaxonId = sciName.id;
                    ScientificName acceptedName = saveAcceptedName(colAcceptedNameData);
                    acceptedNames.add(acceptedName)
                    println "======SAVED THIS ACCEPTED NAME ==== " + acceptedName;
                    //acceptedName.addSynonym(synonym);
                }
                def otherParams = ['taxonId':acceptedNames[0].id]
                def result = speciesService.updateSynonym(null, null,ScientificName.RelationShip.SYNONYM.value(),acceptedMatch.canonicalForm +" "+ acceptedMatch.authorString , otherParams);
                sciName = result.dataInstance;
                acceptedNames.each { acceptedName ->
                    if(acceptedName != acceptedNames[0]){
                        acceptedName.addSynonym(sciName);
                    }
                }
            } else {
                sciName = saveAcceptedName(acceptedMatch);
            }
        } else if(res.size() == 1) {
            sciName = res[0]
        } else {
            //picking one by default
            sciName = res[0]
            //flagging recommendation
            reco.isFlagged = true;
            String flaggingReason = "The name clashes with an existing name on the portal.IDs- ";
            res.each {
                flaggingReason = flaggingReason + it.id.toString() + ", ";
            }
            println "########### Flagging reco ============== "
            reco.flaggingReason = reco.flaggingReason + " ### " + flaggingReason;
        } 
        syncReco(reco, sciName);
    }

    public String createNameActivityDescription(String fieldName, String oldValue, String newValue) {
        if(oldValue == "" || oldValue == null) oldValue = "-";
        if(newValue == "" || newValue == null) newValue = "-";
        String desc = "";
        if(oldValue?.toLowerCase() != newValue?.toLowerCase()) {
            desc = fieldName + " changed from " + oldValue + " to " + newValue +" .";
        }
        return desc;
    }

    //suggest names from IBP and COL
    public Map nameMapper(List<String> names) {
        Map finalResult = [:]
        NamesParser namesParser = new NamesParser();
        def parsedNames = namesParser.parse(names);
        int speciesRank = TaxonomyRank.SPECIES.ordinal();
        int i = 0
        parsedNames.each { pn ->
            def res = searchIBP(pn.canonicalForm, pn.authorYear, NameStatus.ACCEPTED, speciesRank);
            def res1 = searchIBP(pn.canonicalForm, pn.authorYear, NameStatus.SYNONYM, speciesRank);
            res.addAll(res1);
            if(res.size() == 0){
                //COL results
                def r = searchCOL(pn.canonicalForm, "name");
                List temp = []
                r.each {
                    temp.add(['name':it.name, 'id':it.externalId, 'status':it.nameStatus])
                }
                if(r.size() == 0) temp = null;
                finalResult[names[i]] = ['COL' : temp, 'IBP': null]; 
            } else {
                List temp = []
                res.each {
                    temp.add(['name':it.name, 'id':it.id, 'status': it.status.value()])
                }
                finalResult[names[i]] = ['IBP' : temp, 'COL' : null]; 
            }
            i++;
        }
        return finalResult;
    }

    def checkzz1() {
        List s2 =[872, 874, 876, 878, 880, 882, 884, 886, 888, 895, 897, 901, 904, 907, 910, 912, 918, 920, 924, 928, 930, 933, 935, 937, 942, 947, 951, 953, 958, 962, 964, 967, 970, 974, 981, 983, 985, 988, 990, 993, 996, 1000, 1003, 1007, 1011, 1013, 1019, 1023, 1026, 1029, 1033, 1037, 1039, 1043, 1045, 1050, 1052, 1059, 1061, 1070, 1072, 1074, 1077, 1079, 1082, 1084, 1086, 1089, 1093, 1096, 1099, 1101, 1105, 1108, 1113, 1118, 1120, 1123, 1125, 1127, 1130, 1132, 1135, 1137, 1142, 1144, 1147, 1149, 1154, 1157, 1162, 1168, 1171, 1174, 1177, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1203, 1206, 1209, 1212, 1216, 1219, 1222, 1225, 1228, 1231, 1234, 1240, 1243, 1245, 1248, 1251, 1258, 1260, 1264, 1273, 1279, 1283, 1286, 1289, 1291, 1295, 1297, 1300, 1310, 1312, 2998, 3000, 3002, 3004, 3006, 3008, 3010, 3035, 3037, 3039, 3041, 3053, 3055, 3057, 3059, 3061, 3063, 3065, 3067, 3071, 3075, 3077, 3079, 3085, 3087, 3089, 3091, 3093, 3099, 3101, 3103, 3107, 3109, 3112, 3118, 3126, 3132, 3136, 3142, 3144, 3151, 3154, 3156, 3158, 3169, 3171, 3173, 3175, 3177, 3179, 3181, 3183, 3185, 3203, 3205, 3207, 3209, 3218, 3220, 3223, 3230, 3234, 3236, 3239, 3241, 3243, 3252, 3254, 3256, 3258, 3264, 3266, 3268, 3270, 3276, 3279, 3281, 3297, 3299, 3301, 3303, 3305, 3307, 3314, 3326, 3328, 3330, 3332, 3334, 3338, 3341, 3344, 3352, 3354, 3356, 3358, 3360, 3376, 3378, 3387, 3397, 3405, 3407, 3414, 3419, 3421, 3423, 3427, 3429, 3432, 3434, 3437, 3440, 3442, 3444, 3446, 3452, 3454, 3464, 3466, 3468, 3475, 3477, 3485, 3487, 3489, 3491, 3494, 3496, 3498, 3500, 3507, 3509, 3511, 3514, 3516, 3518, 3520, 3542, 3544, 3546, 3550, 3552, 3554, 3556, 3558, 3560, 3568, 3570, 3581, 3587, 3601, 3603, 3605, 3618, 3620, 3628, 3631, 3635, 3637, 3649, 3651, 3677, 3679, 3681, 3683, 3696, 3698, 3701, 3704, 3706, 3711, 3713, 3726, 3736, 3738, 3740, 3742, 3748, 3753, 3762, 3767, 3770, 3772, 3774, 3776, 3783, 3785, 3801, 3803, 3806, 3808, 3812, 3815, 3821, 3823, 3825, 3827, 3829, 3834, 3836, 3838, 3840, 3852, 3854, 3865, 3868, 3876, 3878, 3883, 3885, 3887, 3889, 3899, 3901, 3903, 3905, 3908, 3910, 3913, 3915, 3922, 3924, 3926, 3928, 3931, 3935, 3937, 3939, 3941, 3943, 3946, 3948, 3962, 3964, 3966, 3968, 3970, 3972, 3975, 3977, 3979, 3981, 3984, 3987, 3989, 3992, 3994, 4002, 4004, 4018, 4020, 4022, 4025, 4027, 4029, 4031, 4033, 4035, 4037, 4050, 4052, 4055, 4058, 4060, 4062, 4069, 4073, 4081, 4085, 4089, 4099, 4110, 4115, 4119, 4124, 4129, 4131, 4135, 4140, 4144, 4149, 4153, 4156, 4164, 4180, 4183, 4185, 4187, 4190, 4192, 4194, 4196, 4204, 4206, 4208, 4215, 4217, 4223, 4226, 4231, 4234, 4237, 4242, 4245, 4247, 4258, 4265, 4274, 4276, 4278, 4280, 4282, 4291, 4293, 4311, 4330, 4335, 4337, 4339, 4343, 4345, 4347, 4349, 4352, 4355, 4357, 4359, 4361, 4363, 4365, 4367, 4369, 4371, 4374, 4376, 4378, 4380, 4385, 4389, 4391, 4393, 4395, 4400, 4402, 4406, 4408, 4410, 4414, 4416, 4423, 4427, 4435, 4446, 4454, 4470, 4477, 4483, 4489, 4499, 4506, 4508, 4519, 4521, 4526, 4528, 4549, 4563, 4579, 4585, 4589, 4592]
        def s = [4597, 4606, 4608, 4611, 4613, 4619, 4625, 4627, 4633, 4635, 4637, 4642, 4644, 4646, 4648, 4650, 4652, 4663, 4665, 4667, 4671, 4673, 4675, 4686, 4688, 4691, 4693, 4695, 4698, 4701, 4703, 4711, 4713, 4715, 4717, 4719, 4721, 4731, 4733, 4739, 4741, 4744, 4746, 4748, 4750, 4752, 4754, 4756, 4764, 4766, 4787, 4792, 4795, 4799, 4803, 4805, 4807, 4817, 4819, 4827, 4829, 4832, 4836, 4839, 4856, 4867, 4876, 4881, 4883, 4886, 4888, 4893, 4895, 4901, 4903, 4907, 4909, 4919, 4923, 4939, 4941, 4953, 4955, 4957, 4959, 4962, 4964, 4966, 4969, 4974, 4976, 4979, 4981, 4986, 4989, 4994, 4996, 4998, 5001, 5003, 5018, 5020, 5031, 5033, 5035, 5037, 5039, 5042, 5045, 5047, 5049, 5051, 5053, 5055, 5057, 5059, 5061, 5063, 5066, 5075, 5077, 5081, 5083, 5087, 5092, 5094, 5096, 5098, 5100, 5103, 5105, 5107, 5110, 5113, 5115, 5117, 5119, 5121, 5136, 5138, 5148, 5161, 5163, 5174, 5176, 5185, 5187, 5189, 5194, 5196, 5198, 5201, 5203, 5240, 5244, 5246, 5249, 5251, 5262, 5264, 5273, 5275, 5289, 5291, 5298, 5300, 5302, 5307, 5309, 5312, 5314, 5317, 5320, 5322, 5333, 5335, 5337, 5345, 5347, 5349, 5360, 5365, 5367, 5369, 5372, 5374, 5376, 5378, 5380, 5382, 5384, 5400, 5402, 5404, 5408, 5410, 5439, 5441, 5443, 5463, 5465, 5467, 5469, 5471, 5473, 5475, 5477, 5479, 5481, 5483, 5486, 5490, 5492, 5494, 5496, 5504, 5506, 5519, 5523, 5525, 5527, 5530, 5532, 5540, 5542, 5553, 5560, 5572, 5574, 5576, 5578, 5580, 5582, 5585, 5587, 5594, 5596, 5607, 5609, 5611, 5613, 5615, 5620, 5622, 5626, 5628, 5632, 5635, 5639, 5642, 5644, 5651, 5656, 5658, 5664, 5666, 5668, 5670, 5683, 5688, 5690, 5693, 5695, 5707, 5709, 5712, 5714, 5716, 5718, 5720, 5722, 5743, 5745, 5747, 5749, 5755, 5757, 5769, 5771, 5774, 5785, 5790, 5792, 5797, 5799, 5801, 5808, 5810, 5812, 5815, 5817, 5819, 5821, 5823, 5825, 5829, 5831, 5833, 5841, 5843, 5846, 5848, 5856, 5858, 5860, 5863, 5867, 5871, 5873, 5879, 5881, 5890, 5892, 5899, 5920, 5925, 5927, 5929, 5931, 5933, 5935, 5937, 5940, 5942, 5945, 5947, 5950, 5952, 5965, 5967, 5969, 5971, 5974, 5977, 5979, 5981, 5987, 5991, 5996, 5998, 6007, 6009, 6011, 6016, 6018, 6029, 6031, 6033, 6035, 6042, 6044, 6050, 6052, 6054, 6058, 6060, 6062, 6070, 6072, 6078, 6080, 6082, 6086, 6088, 6091, 6101, 6104, 6109, 6118, 6120, 6136, 6138, 6140, 6142, 6148, 6150, 6166, 6168, 6173, 6177, 6182, 6184, 6188, 6190, 6207, 6209, 6221, 6223, 6248, 6250, 6252, 6257, 6259, 6263, 6265, 6267, 6269, 6275, 6277, 6282, 6287, 6289, 6291, 6294, 6301, 6305, 6311, 6313, 6317, 6326, 6341, 6351, 6356, 6367, 6380, 6386, 6391, 6396, 6399, 6403, 6410, 6418, 6420, 6427, 6429, 6432, 6443, 6445, 6452, 6454, 6456, 6479, 6522, 6534, 6536, 6541, 6543, 6545, 6555, 6557, 6576, 6578, 6600, 6602, 6606, 6609, 6611, 6615, 6617, 6619, 6631, 6636, 6638, 6640, 6642, 6650, 6656, 6658, 6698, 6700, 6702, 6707, 6709, 6711, 6713, 6723, 6728, 6730, 6732, 6734, 6738, 6740, 6744, 6752, 6754, 6765, 6773, 6775, 6785, 6787, 6799, 6801, 6803, 6810, 6812, 6817, 6819, 6825, 6827, 20218, 20220, 20222, 20224, 20226, 20228, 20234]

         s2.addAll(s);
        //s2.addAll(s1);
        return s2
    }
    
    }
