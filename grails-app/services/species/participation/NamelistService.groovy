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
            temp['externalId'] = r?.id?.text()
            temp['matchDatabaseName'] = "COL"
            temp['canonicalForm'] = r?.name?.text(); 
            temp['name'] = r?.name?.text() 
            if(searchBy == 'name') {
                temp['name'] += " " +r?.author?.text()
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
            temp['authorString'] = r?.author?.text()
            temp['sourceDatabase'] = r?.source_database?.text()

            temp['group'] = (r?.classification?.taxon[0]?.name?.text())?r?.classification?.taxon[0]?.name?.text():''
            println "==========NAME STATUS========= " + temp['nameStatus']
            if(temp['nameStatus'] == "synonym") {
                def aList = []
                r.accepted_name.each {
                    def m = [:]
                    m['id'] = it.id.text()
                    m['name'] = it.name.text() + " " + it.author.text();
                    m['canonicalForm'] = it.name.text();
                    m['nameStatus'] = it.name_status.text()?.tokenize(' ')[0];
                    m['rank'] = it.rank?.text()?.toLowerCase();
                    m['authorString'] = it.author.text();
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
                r.classification.taxon.each { t ->
                //println t.rank.text() + " == " + t.name.text()
                temp[t?.rank?.text()?.toLowerCase()] = t?.name?.text()
                id_details[t?.name?.text()] = t?.id?.text()
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

    //Searches IBP accepted and synonym
    List<ScientificName> searchIBP(String canonicalForm, String authorYear, NameStatus status, int rank) {  
        //utilsService.cleanUpGorm(true);
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
        println "====SEARCHING ON IBP IN CLASS ====== " + clazz
        clazz.withNewSession{
            //FINDING BY CANONICAL
            res = clazz.findAllWhere(canonicalForm:canonicalForm, status: status);

            //CANONICAL ZERO MATCH OR SINGLE MATCH
            if(res.size() < 2) { 
                if(res.size() == 0 ) {
                    println "====CANONICAL - ZERO MATCH ====== "
                } else {
                    println "====CANONICAL - SINGLE MATCH ====== "
                }
                return res;
            }
            //CANONICAL MULTIPLE MATCH
            else {
                //FINDING BY VERBATIM
                if(!authorYear) authorYear = '';
                res = clazz.findAllWhere(name: (canonicalForm + " " + authorYear), status: status);

                //VERBATIM SINGLE MATCH
                if(res.size() == 1) {
                    println "====VERBATIM - SINGLE MATCH ====== "
                    return res;
                }

                //VERBATIM ZERO MATCH/MULTIPLE MATCH
                else if(res.size() > 2 || res.size() == 0) {
                    //FINDING BY VERBATIM + RANK
                    res = clazz.findAllWhere(name:(canonicalForm + " " + authorYear),rank: rank, status: status);

                    //VERBATIM + RANK ZERO MATCH
                    if(res.size() == 0) {
                        if(authorYear) {
                            println "====TRYING VERBATIM + RANK - ZERO MATCH AND ALSO HAS AUTHOR YEAR"
                            return res;
                        }
                        else {
                            //FINDING BY CANONICAL + RANK
                            println "====TRYING VERBATIM + RANK - ZERO MATCH & NO AUTHOR YEAR - SO MATCHED ON CANONICAL + RANK"
                            res = clazz.findAllWhere(canonicalForm:canonicalForm ,rank: rank, status: status);
                            return res;
                        }
                    }
                    //VERBATIM + RANK SINGLE MATCH
                    if(res.size() == 1) {
                        println "====TRYING VERBATIM + RANK - SINGLE MATCH"
                        return res;
                    }

                    //VERBATIM + RANK MULTIPLE MATCH
                    else {
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
        println "================LIST OF COL DATA=========================== " + colData
        log.debug "Curating name ${sciName} with col data ${colData}"
        def acceptedMatch;

        if(!colData) return;

        //check if this is a single direct match
        if(colData.size() == 1 ) {
            //Reject all (IBP)scientific name -> (CoL) common name matches (leave for curation).
            if(sciName.status != NameStatus.COMMON && colData['nameStatus'] == NamesMetadata.COLNameStatus.COMMON.value()) {
                //reject ... position remains DIRTY
                log.debug "[REJECTING AS NAME IS COMMON NAME] ${sciName} is a sciname but it is common name as per COL. So leaving this name for curation"
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
                if(colMatch.authorString) {
                    colMatchVerbatim = colMatch.name + " " + colMatch.authorString
                }
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
                    println colMatch
                    println sciName.rank
                    if(colMatch.parsedName.normalizedForm == sciName.normalizedForm && colMatch.parsedRank == sciName.rank) {
                        noOfMatches++;
                        acceptedMatch = colMatch;
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
                    } else {
                        //comparing Canonical + rank
                        log.debug "Comparing now with canonical + rank"
                        noOfMatches = 0;
                        colNames[sciName.normalizedForm].each { colMatch ->
                            //If no match exists with Verbatim+rank and there is no author year info then match with canonical+rank.
                            if(colMatch.parsedName.canonicalForm == sciName.canonicalForm && colMatch.parsedRank == sciName.rank) {
                                noOfMatches++;
                                acceptedMatch = colMatch;

                            }
                        }
                        if(noOfMatches == 1) {
                            //acceptMatch
                            log.debug "[CANONICAL+RANK : SINGLE MATCH] Canonical ${sciName.canonicalForm} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                        } else {
                            acceptedMatch = null;
                            log.debug "[CANONICAL+RANK : NO MATCH] No single match on canonical+rank... leaving name for manual curation"
                        }
                    }
                } else if (noOfMatches > 1) {
                    acceptedMatch = null;
                    log.debug "[VERBATIM+RANK: MULTIPLE MATCHES] Multiple matches even on verbatim + rank. So leaving name for manual curation"
                }

            }
        }
       
        if(acceptedMatch) {
            println "================ACCEPTED MATCH=========================== " + acceptedMatch
            log.debug "There is an acceptedMatch ${acceptedMatch} for ${sciName}. Updating status, rank and hieirarchy"
            processDataForMigration(sciName, acceptedMatch);            
        } else {
            log.debug "[NO MATCH] No accepted match in colData. So leaving name in dirty list for manual curation"
        }
    }

    def processDataForMigration(ScientificName sciName, Map acceptedMatch) {
        sciName.tempActivityDescription = "";
        sciName = updateAttributes(sciName, acceptedMatch);
        //if sciName_status != colData[nameStatus] -> update status
        sciName = updateStatus(sciName, acceptedMatch).sciName;
        println "=======AFTER STATUS======== " + sciName.status +"==== "+  acceptedMatch.parsedRank
        updateRank(sciName, acceptedMatch.parsedRank);            
        //WHY required here??
        //addIBPHierarchyFromCol(sciName, acceptedMatch);
        updatePosition(sciName, NamesMetadata.NamePosition.WORKING);
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
            def feedInstance = activityFeedService.addActivityFeed(sciName, sciName, springSecurityService.currentUser?:SUser.read(1L), ActivityFeedService.TAXON_NAME_UPDATED, sciName.tempActivityDescription);
            sciName.tempActivityDescription = "";
            utilsService.cleanUpGorm(true);
        } else {
            sciName.errors.allErrors.each { log.error it }
        }

    }
   
    def processDataFromUI(ScientificName sciName, Map acceptedMatch) {
        sciName.tempActivityDescription = "";
        sciName = updateAttributes(sciName, acceptedMatch);
        //if sciName_status != colData[nameStatus] -> update status
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
            sciName.tempActivityDescription += createNameActivityDescription("Rank ", sciName.rank, rank);
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
        log.debug "Updating position from ${sciName.position} to ${position}"
        sciName.tempActivityDescription += createNameActivityDescription("Position", sciName.position.value(), position.value());
        sciName.position = position;
    }

    List processColData(File f) {
        if(!f.exists()){
            log.debug "File not found skipping now..."
            return
        }
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
    }

    private Map fetchTaxonRegistryData(Map m) {
        println "=======MAP M ========= " + m
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
        result['taxonRegistry.9'] = res['9'] = m['species']

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
        http.request( EOL_SITE, GET, TEXT ) { req ->
            if(searchBy == 'name') {
                uri.path = EOL_URI;
            } else {
                uri.path = EOL_URI;
            }
            uri.query = [ exact:'true', q :input]
            headers.Accept = '*/*'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                println "========TNRS RESULT====== " + xmlText
                return responseFromEOLAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }

    List responseFromEOLAsMap(String xmlText , String searchBy) {
        def allResults = JSON.parse(xmlText).results
        println "============RESULT=============== " + allResults
        def finalResult = []
        allResults.each { result ->
            Map temp = new HashMap()
            temp['externalId'] = "" 
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
            sciName.flaggingReason = sciName.flaggingReason + " ### " + flaggingReason;
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
        sciName.tempActivityDescription += createNameActivityDescription("COL Name Status", sciName.colNameStatus.value(), colMatch.colNameStatus);
	    sciName.colNameStatus = getCOLNameStatus(colMatch.colNameStatus);
        sciName.tempActivityDescription += createNameActivityDescription("Match Id", sciName.matchId, colMatch.externalId);
        sciName.matchId = colMatch.externalId;
        sciName.tempActivityDescription += createNameActivityDescription("Match Database Name", sciName.matchDatabaseName, colMatch.matchDatabaseName);
        sciName.matchDatabaseName = colMatch.matchDatabaseName;
        sciName.tempActivityDescription += createNameActivityDescription("Source Database", sciName.viaDatasource, colMatch.sourceDatabase);
        sciName.viaDatasource = colMatch.sourceDatabase;
        sciName = sciName.merge();
        if(!sciName.save()) {
            sciName.errors.allErrors.each { log.error it }
        }
        println "=========DONE UPDATING ATTRIBUTES ========"
        return sciName
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
        String desc = "";
        if(oldValue.toLowerCase() != newValue.toLowerCase()) {
            if(oldValue == "") oldValue = "-"
            if(newValue == "") newValue = "-"
            desc = fieldName + " changed from " + oldValue + " to " + newValue +" .";
        }
        return desc;
    }

}
