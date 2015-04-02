package species

import grails.converters.JSON;
import grails.converters.XML;
import species.TaxonomyRegistry;
import species.Classification;
import species.TaxonomyDefinition;
import grails.plugin.springsecurity.annotation.Secured;
import species.NamesParser;
import species.sourcehandler.XMLConverter;

class NamelistController {
    
	@Secured(['ROLE_USER'])
    def index() { }

    def namelistService
	
	/**
	 * input : taxon id ,classification id of ibp 
	 * @return A map which contain keys as dirty, clean and working list. Values of this key is again a LIST of maps with key as name and id
	 * 
	 */
	def getNamesFromTaxon(){
        //input in params.taxonId
		println "=====PARAMS========= " + params
        def res = namelistService.getNamesFromTaxon(params)
        println "====CALL HERE ====== " +  res  //namelistService.getNamesFromTaxon(params)
		println "=================================="
		//def res = [dirtyList:[[name:'aa', id:11, classificationId:params.classificationId], [name:'bb', id:29585, classificationId:params.classificationId]], workingList:[[name:'aa', id:11, classificationId:params.classificationId], [name:'bb', id:22, classificationId:params.classificationId]]]
        render res as JSON
	}
	
	/**
	 * input : taxon id, classification id of ibp
	 * @return All detail like kingdom, order etc
	 */
	def getNameDetails(){
        //input in params.taxonId
		//[name:'aa', kingdom:'kk', .....]
        def res = namelistService.getNameDetails(params);
	    println "====CALL HERE NAME DETAILS====== " + res
		println "========================================="
		//fetch registry using taxon id and classification id
        /*def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()),TaxonomyDefinition.read(params.taxonId.toLong())); 
        println "=========TAXON REG========= " + taxonReg
        def res
        if(taxonReg) {
            res = [name:'rahul', kingdom:'Plantae',phylum:'Magnoliophyta', authorString:'author', rank:'super-family', source:'COL', superfamily:'Ydfvsdv',family:'Menispermaceae', 'class':'Equisetopsida', order:'Ranunculales',genus:'Albertisia',species:'Albertisia mecistophylla','sub-genus':'Subsdfsdf','sub-family':'SubFfsad', nameStatus:'accepted', via:'xx', id:'123', taxonReg:taxonReg.id?.toString()]
        } else {
            println "======TAXON REGISTRY NULL====="
        }*/
        render res as JSON
	}
	
	/**
     * input : string name and dbName
     * @return list of map where each map represent one result
     */
    def searchExternalDb(){
        //[[name:'aa', nameStatus:'st', colId:34, rank:4, group:'plant', sourceDatabase:'sb'], [name:'bb', nameStatus:'st', colId:34, rank:4, group:'plant', sourceDatabase:'sb']]
        println "====SEARCH COL====== " + params.name+"====== "+ params.dbName
        //SWITCH CASE BASED ON DB NAME [col,gbif,ubio,tnrs,gni,eol,worms] and if value "databaseName" - means no database selected to query
        List<String> givenNames = [params.name]
        NamesParser namesParser = new NamesParser();
        List<TaxonomyDefinition> parsedNames = namesParser.parse(givenNames);
        println "===========PARSED NAMES========= " + parsedNames[0].canonicalForm
        params.name = parsedNames[0].canonicalForm
        def dbName = params.dbName
        List res = []
        switch (dbName) {
            case "col":
                res = namelistService.searchCOL(params.name, 'name');
            break

            case "gbif":
                res = namelistService.searchGBIF(params.name, 'name');
            break

            case "tnrs":
                res = namelistService.searchTNRS(params.name, 'name');
            break

            case "eol":
                res = namelistService.searchEOL(params.name, 'name');
            break

            case "worms":
                res = namelistService.searchWORMS(params.name, 'name'); 
            break

            default:
                log.debug "INVALID EXTERNAL DATABASE"
            break

        }
        
        //def res = [[name:'aa', nameStatus:'st', externalId:34, rank:'genus', group:'plant', sourceDatabase:'sb'], [name:'bb', nameStatus:'st', externalId:34, rank:'family', group:'animal', sourceDatabase:'sb']]

        println "========RES ======= " + res
        render res as JSON
    }
    /**
     * input : externalId & dbName
     * @return same as api getNameDetails
     */
    def getExternalDbDetails(){
        //same getNameDetails
        println "====EXTERNAL DB DETAILS====== " + params
        //SWITCH CASE BASED ON DB NAME [col,gbif,ubio,tnrs,gni,eol,worms] and if value "databaseName" - means no database selected to query
        def dbName = params.dbName
        List res = []
        switch (dbName) {
            case "col":
                res = namelistService.searchCOL(params.externalId, 'id');
            break

            case "gbif":
                res = namelistService.searchGBIF(params.externalId, 'id');
            break

            case "tnrs":
                //external id not present, name will be used
                //externalId has name inside
                res = namelistService.searchTNRS(params.externalId, 'id')
            break

            case "eol":
                res = namelistService.searchEOL(params.externalId, 'id')
            break
        }
        //def res = [name:'rahul', kingdom:'kk',phylum:'ph', authorString:'author', rank:'order', source:'COL', superfamily:'rerfef', nameStatus:'acceptedName']
        println "========RES DETAILS====== " + res[0]  
        //Sending 0th index as only one result as its queried on id
        render res[0] as JSON
    }
    
    def searchIBP() {
        List<String> givenNames = [params.name]
        NamesParser namesParser = new NamesParser();
        List<TaxonomyDefinition> parsedNames = namesParser.parse(givenNames);
        println "===========PARSED NAMES========= " + parsedNames[0].canonicalForm
        params.name = parsedNames[0].canonicalForm
        List res = []
        res = namelistService.searchIBP(params.name); 
        println "========RES ======= " + res
        render res as JSON
    }
   
    def curateName() {
        def acceptedMatch = JSON.parse(params.acceptedMatch);
        acceptedMatch.parsedRank =  XMLConverter.getTaxonRank(acceptedMatch.rank);
        println "=============ACCEPTED MATCH=========== " + acceptedMatch
        
        ScientificName sciName = TaxonomyDefinition.get(acceptedMatch.taxonId.toLong());
        println "=============SCIENTIFIC NAME ========= " + sciName;
        def res = namelistService.processDataFromUI(sciName, acceptedMatch)
        render res as JSON
    }
}
