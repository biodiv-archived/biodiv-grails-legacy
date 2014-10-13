package species

import grails.converters.JSON;
import grails.converters.XML;

class NamelistController {

    def index() { }
	
	
	/**
	 * input : taxon id of ibp 
	 * @return A map which contain keys as dirty, clean and working list. Values of this key is again a LIST of maps with key as name and id
	 * 
	 */
	def getNamesFromTaxon(){
        //input in params.taxonId
        println "====CALL HERE ====== " + params
		def res = [dirtyList:[[name:'aa', id:11], [name:'bb', id:22]], workingList:[[name:'aa', id:11], [name:'bb', id:22]]]
        render res as JSON
	}
	
	/**
	 * input : taxon id of ibp
	 * @return All detail like kingdom, order etc
	 */
	def getNameDetails(){
        //input in params.taxonId
		//[name:'aa', kingdom:'kk', .....]
	    println "====CALL HERE NAME DETAILS====== " + params
		def res = [name:'rahul', kingdom:'kk',phylum:'ph', authorString:'author', rank:'order', source:'COL', superfamily:'rerfef', nameStatus:'acceptedName']
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
        def res = [[name:'aa', nameStatus:'st', colId:34, rank:'genus', group:'plant', sourceDatabase:'sb'], [name:'bb', nameStatus:'st', colId:34, rank:'family', group:'animal', sourceDatabase:'sb']]
        render res as JSON
    }
    /**
     * input : id & dbName
     * @return same as api getNameDetails
     */
    def getExternalDbDetails(){
        //same getNameDetails
        println "====EXTERNAL DB DETAILS====== " + params
        def res = [name:'rahul', kingdom:'kk',phylum:'ph', authorString:'author', rank:'order', source:'COL', superfamily:'rerfef', nameStatus:'acceptedName']
        render res as JSON
    }
}
