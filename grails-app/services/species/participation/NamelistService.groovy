package species.participation

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.ScientificName
import species.TaxonomyDefinition;
import species.SynonymsMerged;
import species.Synonyms;
import species.NamesMetadata;
import species.TaxonomyRegistry
import species.Classification;
import species.Species;
import species.SpeciesField;
import species.ScientificName.TaxonomyRank
import species.Synonyms;
import species.CommonNames;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.COLNameStatus;
import species.NamesMetadata.NamePosition;
import species.auth.SUser;
import content.eml.DocSciName
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.sql.Sql
import grails.util.Holders
import groovy.util.XmlParser
import grails.converters.JSON;
import wslite.soap.*
import species.NamesParser;
import species.sourcehandler.XMLConverter;
import species.participation.Recommendation;
import speciespage.SpeciesUploadService;
import species.namelist.NameInfo
import species.namelist.Utils;
import species.utils.Utils as UtilsUtils;
import species.AcceptedSynonym
import species.ResourceFetcher;
import species.AbstractObjectService;
import species.SpeciesPermission;
import species.ScientificName.RelationShip

class NamelistService extends AbstractObjectService {
  
    static transactional = false;
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

	private final static int EXPORT_BATCH_SIZE = 10000;
	
 	//XXX: this map is used to store new name created by session for bulk and name upload. This is needed because
	//if a user want to create new name by saying 'new' in spreadsheet then same name may be created multiple times
	//it that name appears multiple time in hir sheet 
	//this map will be cleared by every bulk upload thread to avoid any conflict
	private static Map NEW_NAME_IN_SESSION = [:]

	def dataSource
    def groupHandlerService
    def springSecurityService;
    def taxonService;
    def grailsApplication;
    def speciesService;
    def utilsService;
	def sessionFactory;
    def activityFeedService;
	//def speciesUploadService;
    def speciesPermissionService;
    
    //Searches IBP accepted and synonym only in WORKING AND RAW LIST and NULL list
    public static List<ScientificName> searchIBP(String canonicalForm, String authorYear, NameStatus status, int rank = -1, boolean searchInNull = false, String normalizedForm = null, boolean useAuthorYear = false) {  
        //println "=SEARCH IBP== canForm " + canonicalForm +"--- authorYear "+authorYear +"---status "+ status + "---rank "+ rank + " userauthoryear " + useAuthorYear + " searchInNull " + searchInNull ;
        List res = [];

        def clazz
        if(status == NameStatus.ACCEPTED || !status) {
            clazz = TaxonomyDefinition.class;
        } else {
            clazz = SynonymsMerged.class; 
        }
		if(normalizedForm || authorYear){
			String authorYearSuffix = authorYear ? (' ' +  authorYear) :''
			normalizedForm = normalizedForm ?:(canonicalForm + authorYearSuffix)
			res = clazz.withCriteria(){
				and{
					eq('isDeleted', false)
					eq('normalizedForm', normalizedForm)
					if(status) eq('status', status)
					if(rank >= 0)
						eq('rank', rank)
					if(!searchInNull){
						isNotNull('position')
					}
				}
			}
		}
		
		if(res){
			//println "== FINAL SEARCH RESULT " + res
			return res
		}
		//println  "No result in Normalized form using canonical form now"
        res = clazz.withCriteria(){
			and{
				eq('isDeleted', false)
				eq('canonicalForm', canonicalForm)
				if(status) eq('status', status)
				if(rank >= 0)
					eq('rank', rank)
				if(!searchInNull){
					isNotNull('position')
				}
				//XXX in curation while taking col hir. we want author year column to be used forcefully 
				// in other cases we will not use untill gets multiple matches 
				if(useAuthorYear && authorYear){
					eq('authorYear', authorYear)
				}
			}
        }

        //println "== FINAL SEARCH RESULT " + res
        return res;
    }
 
   
    List searchIBPResults(String canonicalForm, String authorYear, NameStatus status, rank) {
        def res = searchIBP(canonicalForm, authorYear, status, rank) //TaxonomyDefinition.findAllByCanonicalForm(canonicalForm);
        def finalResult = []
        res.each { 
            def temp = [:]
            temp['taxonId'] = it.id
            temp['externalId'] = it.id
            temp['name'] = it.canonicalForm
            temp['rank'] = TaxonomyRank.getTRFromInt(it.rank).value().toLowerCase()
            temp['nameStatus'] = it.status.value().toLowerCase()
            temp['group'] = groupHandlerService.getGroupByHierarchy(it, it.parentTaxon()).name
            temp['sourceDatabase'] = it.viaDatasource?it.viaDatasource:''
            finalResult.add(temp);
        }
        return finalResult 
    }

	
	/**
	 * Searches given name with rank in ibp and col and return all the matched result
	 * @param name
	 * @param rank
	 * @return
	 */
	public Map nameMapper(List<NameInfo> names) {
		
		Map finalResult = [:]
		NamesParser namesParser = new NamesParser();
		List<TaxonomyDefinition> parsedNames = namesParser.parse(names.collect {it.name});
		
		int i = -1
        List nameSubLists = parsedNames.collate(5)
        nameSubLists.each { nl ->
	        TaxonomyDefinition.withNewTransaction {
		        nl.each { TaxonomyDefinition name ->
					i++
					
					if(i%100 == 0){
						log.debug "---------------------------------------- current count " + i
					}
					
					List tmpRes = []
					if(!name || !name.canonicalForm) {
						log.debug "Name is not parsed by Names Parser " + name
						tmpRes << ['match':'None', 'name':'', 'rank':'', 'status': '', 'group' : '', 'position':'','id':'']
						finalResult[names[i]] = tmpRes
						// return works here as continue
						return
					}
					
					List ibpResult = searchIBP(name.canonicalForm, name.authorYear, null, names[i].rank, false, name.normalizedForm, true)
					ibpResult.each { TaxonomyDefinition t ->
		                t = TaxonomyDefinition.get(t.id)
						tmpRes << ['match':'IBP', 'name':t.name, 'rank':ScientificName.TaxonomyRank.getTRFromInt(t.rank).value(), 'status': t.status.value(), 'group' : t.group?.name, 'position':t.position.value(),'id':t.id]
					}
					
					if(!ibpResult){
						List colResult = searchCOL(name.canonicalForm, 'name');
						colResult.each { t ->
							boolean addToList = true
							int rr = XMLConverter.getTaxonRank(t.rank)
							if((names[i].rank <= 8) && (names[i].rank > 0)){
								if(rr != names[i].rank){
									addToList = false
								}
							}else{ // rank is given species or infraspecies
								int wCount = name.canonicalForm.trim().count(" ") + 1
								if((wCount == 1) && (rr > 8 )){ // if single word
									addToList = false
								}else if((wCount == 2) && (rr != 9)){
									addToList = false
								}
							}
							if(addToList){
								tmpRes << ['match':'COL', 'name':t.name, 'rank':t.rank, 'status': t.colNameStatus, 'group' : t.group, 'position':'WORKING','id':t.externalId]
							}
						}
						
						//No ibp result then searching only by canonical name and rank and excluding author year info
						//to give all match option for curation
						if(name.authorYear){
							ibpResult = searchIBP(name.canonicalForm, null, null, names[i].rank, false, null, false)
							ibpResult.each { TaxonomyDefinition t ->
								t = TaxonomyDefinition.get(t.id)
								tmpRes << ['match':'IBP', 'name':t.name, 'rank':ScientificName.TaxonomyRank.getTRFromInt(t.rank).value(), 'status': t.status.value(), 'group' : t.group?.name, 'position':t.position.value(),'id':t.id]
							}
						}
					}
					
					
					finalResult[names[i]] = tmpRes
		        }
	        }
		}
		return finalResult;
	}

	public ScientificName createNameFromColId(String colId, boolean runPostProcess = true){
		if(!colId)
			return
		
		def td = TaxonomyDefinition.findByMatchId(colId)
		if(td){
			return td
		}
		
		def colRes = searchCOL(colId, 'id')
		if(!colRes)
			return
		
		colRes = colRes[0]
		String status = colRes.nameStatus
		if(status.equalsIgnoreCase('accepted')){
			td = createAcceptedNameFromColId(colId, runPostProcess)
		}else{
			td = createSynonymFromColId(colId, runPostProcess)
		}
		addNewNameInSession(td)
		return td
	}
	
	private ScientificName createAcceptedNameFromColId(String colId, boolean runPostProcess){
		TaxonomyDefinition td = TaxonomyDefinition.findByMatchId(colId)
		if(td){
			return td
		}
		
		td = processDataForMigration(new TaxonomyDefinition(), searchCOL(colId, 'id')[0], 1, true)
		
		if(runPostProcess){
			td.postProcess()
		}
		return td
	}
	
	private ScientificName createSynonymFromColId(String colId, runPostProcess){
		def colRes = searchCOL(colId, 'id')[0]
		String accepteNameColId = colRes.acceptedNamesList[0].id
		TaxonomyDefinition td = createAcceptedNameFromColId(accepteNameColId, runPostProcess)
		def syn =  SynonymsMerged.findByMatchId(colId)
		//some times accepted name is available but synonym not got added so by this call making sure synonym must be added
		if(!syn){
			td.addSynonymFromCol(searchCOL(accepteNameColId, 'id')[0].synList)
			syn =  SynonymsMerged.findByMatchId(colId)
		}
		td.createSpeciesStub()
		println "---------------created synonym " + syn
		return syn
	}
	

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// COL Migration related /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
    def populateInfoFromCol(File sourceDir, totalCount){
        if(!sourceDir.exists()){
            log.debug "Source dir does not exist. ${sourceDir} Aborting now..." 
            return
        }

        curateName(new File(sourceDir, TaxonomyDefinition.class.simpleName), TaxonomyDefinition.class, totalCount)
        //curateName(new File(sourceDir, Synon//TaxonomyDefinition.findAllByCanonicalForm(canonicalForm)yms.class.simpleName), Synonyms.class)
    }

    void curateName(File domainSourceDir, domainClass, totalCount){
        if(!domainSourceDir.exists()){
            log.debug "Source dir does not exist. ${domainSourceDir} Aborting now..."
            return
        }

        long offset = 0
        int i = 0
        while(true){
            List tds = domainClass.createCriteria().list(max:BATCH_SIZE, offset:offset) {
				and {
					le("id", totalCount)
					order("rank", "asc")	
					order("id", "asc")	
				}
				
            }
            tds.each {
                log.debug  it.rank +  "    " + it.id + "   " +  it.canonicalForm
            }
            if(tds.isEmpty()){
                break
            }
            offset += BATCH_SIZE
            tds.each {
				if(!it.matchId && !it.dirtyListReason)
                	curateName(it, domainSourceDir);
            }
        }
    }

    void curateName (ScientificName sciName, File domainSourceDir) {
		String xmlFileName = sciName.canonicalForm.replaceAll(' ', '_') + ".xml"
        File f = new File(domainSourceDir, xmlFileName)
        log.debug  "===== starting " + f
        try{
        	List colData = processColData(f, sciName);
        	curateName(sciName, colData);
        }catch(e){
        	println "============= Failed for this taxon ============= " + sciName
        	e.printStackTrace()
        }
    }

    void curateName (ScientificName sciName) {
        List res = searchCOL(sciName.canonicalForm, 'name');
        curateName(sciName, res);
    }

	void curateName (ScientificName sciName, List colData) {
		def acceptedMatch = validateColMatch(sciName, colData)
		if(acceptedMatch)
			processDataForMigration(sciName, acceptedMatch, colData.size());
	}
    
	def validateColMatch(ScientificName sciName, List colData) {
        //println "================LIST OF COL DATA=========================== " + colData
        log.debug "=========== Curating name ${sciName} "
        def acceptedMatch;
        String dirtyListReason;
        if(!colData) return;
		
		int colDataSize = colData.size();
		//check if this is a single direct match
        if(colData.size() == 1 ) {
            //Reject all (IBP)scientific name -> (CoL) common name matches (leave for curation).
            if(sciName.status != NameStatus.COMMON && colData[0].nameStatus == NamesMetadata.COLNameStatus.COMMON.value()) {
                //reject ... position remains RAW
                dirtyListReason = "[REJECTING AS NAME IS COMMON NAME]. So leaving this name for curation"
                log.debug "[REJECTING AS NAME IS COMMON NAME] ${sciName} is a sciname but it is common name as per COL. So leaving this name for curation"
                sciName.noOfCOLMatches = colDataSize;
                sciName.position = NamesMetadata.NamePosition.RAW;
                sciName.dirtyListReason = dirtyListReason;
                if(!sciName.hasErrors() && sciName.save(flush:true)) {
                } else {
                    sciName.errors.allErrors.each { log.error it }
                }
                return;
            } else if (sciName.canonicalForm != colData[0].canonicalForm) {  //COL prefix based search
                dirtyListReason = "[REJECTING AS CANONICAL DONT MATCH EVEN ON 1 RESULT]."
                log.debug "[REJECTING AS CANONICAL DONT MATCH EVEN ON 1 RESULT]."
                sciName.noOfCOLMatches = colDataSize;
                sciName.position = NamesMetadata.NamePosition.RAW;
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
                //println "============ACCEPTED MATCH ======= " + acceptedMatch
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
                //log.debug "[VERBATIM : NO MATCH] No verbatim match for ${sciName.name}"
                int noOfMatches = 0;
                //log.debug "Comparing now with CANONICAL + RANK"
                def multiMatches = [];
                colData.each { colMatch ->
                    if(colMatch.canonicalForm == sciName.canonicalForm && colMatch.parsedRank == sciName.rank) {
                        noOfMatches++;
                        acceptedMatch = colMatch;
                        multiMatches.add(colMatch)
                    }
                }
                if(noOfMatches != 1) {
                    //log.debug "[CANONICAL+RANK : NO SINGLE MATCH] No single match on canonical+rank... leaving name for manual curation"
                    dirtyListReason = "[CANONICAL+RANK: NO SINGLE MATCH] - NO PARENT TAXON MATCH - rank >= 9"
                    acceptedMatch = null;
                    //PARENT TAXON MATCH for rank below species
                    if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                        log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                        acceptedMatch = parentTaxonMatch(sciName, multiMatches);
                        if(!acceptedMatch) {
                            dirtyListReason = "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                        } 
                    }
                } else {
                    //log.debug "[CANONICAL+RANK : SINGLE MATCH] Canonical ${sciName.canonicalForm} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                }
            }
            else if(colNames[sciName.normalizedForm].size() == 1) {
                //generate and compare verbatim. If verbatim matches with a single match accept. 
                acceptedMatch = colNames[sciName.normalizedForm][0]
                //log.debug "[VERBATIM : SINGLE MATCH] Verbatim ${sciName.name} matches single entry in col matches. Accepting ${acceptedMatch}"
            } else {
                //checking only inside all matches of verbatim
                //log.debug "[VERBATIM: MULTIPLE MATCHES] There are multiple col matches with canonical and just verbatim .. so checking with verbatim + rank ${sciName.rank}"
                int noOfMatches = 0;
                def multiMatches2 = []
                colNames[sciName.normalizedForm].each { colMatch ->
                    //If Verbatims match with multiple matches, then match with verbatim+rank.
                    //println colMatch
                    //println sciName.rank
                    if(colMatch.parsedName.normalizedForm == sciName.normalizedForm && colMatch.parsedRank == sciName.rank) {
                        noOfMatches++;
                        acceptedMatch = colMatch;
                        multiMatches2.add(colMatch)
                    }
                }
                if(noOfMatches == 1) {
                    //acceptMatch
                    //log.debug "[VERBATIM+RANK : SINGLE MATCH] Verbatim ${sciName.name} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                } else if(noOfMatches == 0) {
                    //log.debug "[VERBATIM+RANK : NO MATCH] No match on verbatim + rank"
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
                        def multiMatches = []
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
                            //log.debug "[CANONICAL+RANK : SINGLE MATCH] Canonical ${sciName.canonicalForm} and rank ${sciName.rank} matches single entry in col matches. Accepting ${acceptedMatch}"
                        } else {
                            acceptedMatch = null;
                            dirtyListReason = "[CANONICAL+RANK: MULTIPLE MATCH] Multiple matches even on canonical + rank. NO PARENT TAXON MATCH - rank >= 9"
                            //PARENT TAXON MATCH for rank below species
                            if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                                log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                                acceptedMatch = parentTaxonMatch(sciName, multiMatches);
                                if(!acceptedMatch) {
                                    dirtyListReason = "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                                } 
                            }
                        }
                    }
                } else if (noOfMatches > 1) {
                    acceptedMatch = null;
                    //log.debug "[VERBATIM+RANK: MULTIPLE MATCHES] Multiple matches even on verbatim + rank. PARENT TAXON MATCH"
                    dirtyListReason = "[VERBATIM+RANK: MULTIPLE MATCHES] Multiple matches even on verbatim + rank. NO PARENT TAXON MATCH - rank >= 9"
                    //PARENT TAXON MATCH for rank below species
                    if(noOfMatches > 1 && (sciName.rank < TaxonomyRank.SPECIES.ordinal())) {
                        log.debug "[VERBATIM+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] "
                        acceptedMatch = parentTaxonMatch(sciName, multiMatches2);
                        if(!acceptedMatch) {
                            dirtyListReason = "[VERBATIM+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
                        }
                    }
                }
            }
        }
       
        if(acceptedMatch) {
            //println "================ACCEPTED MATCH=========================== " + acceptedMatch
            if(acceptedMatch.parsedRank != sciName.rank) {
                //log.debug "There is an acceptedMatch ${acceptedMatch} for ${sciName}. But REJECTED AS RANK WAS CHANGING"
                sciName.noOfCOLMatches = colDataSize;
                sciName.position = NamesMetadata.NamePosition.RAW;
                sciName.dirtyListReason = "REJECTED AS RANK WAS CHANGING"
                if(!sciName.hasErrors() && sciName.save(flush:true)) {
                } else {
                    sciName.errors.allErrors.each { log.error it }
                }
                return;
            }
            //log.debug "There is an acceptedMatch ${acceptedMatch} for ${sciName}. Updating status, rank and hieirarchy"
			return acceptedMatch                  
        } else {
            //log.debug "[NO MATCH] No accepted match in colData. So leaving name in dirty list for manual curation"
            sciName.noOfCOLMatches = colDataSize;
            sciName.position = NamesMetadata.NamePosition.RAW;
            sciName.dirtyListReason = dirtyListReason;
            if(!sciName.hasErrors() && sciName.save(flush:true)) {
            println "=======SAVED SCI NAME==========="
		} else {
                sciName.errors.allErrors.each { log.error it }
            }
        }
    }

    public processDataForMigration(ScientificName sciName, Map acceptedMatch, colDataSize, boolean createOnlyNameFromCol = false, boolean addHir= true) {
        updateAttributes(sciName, acceptedMatch, createOnlyNameFromCol);
		
		//inside this we are adding hirarchy
		if(addHir)
        	sciName = updateStatus(sciName, acceptedMatch).sciName;
			
        //println "========THE SCI NAME======== " + sciName
        //println "=======AFTER STATUS======== " + sciName.status +"==== "+  acceptedMatch.parsedRank
        if(!acceptedMatch['parsedRank']) {
            acceptedMatch['parsedRank'] = XMLConverter.getTaxonRank(acceptedMatch.rank);
        }
        updateRank(sciName, acceptedMatch.parsedRank);            
        
        //already updated in update attributes
        sciName.dirtyListReason = null;
        sciName.noOfCOLMatches = colDataSize;
        /*else if(sciName.status == NameStatus.ACCEPTED) {
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def cl = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
            def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(cl, sciName);
            taxonService.moveToWKG([taxonReg]);
        }*/
        //println "=======SCI NAME POSITION ========== " + sciName.position
        //println "=====SCI NAME ==== " + sciName
        //sciName = sciName.merge();
        sciName.position =  NamesMetadata.NamePosition.WORKING
        if(!sciName.hasErrors() && sciName.save(flush:true)) {
            println sciName.position
            log.debug "Saved sciname ${sciName}"
			sciName.updateNameSignature()
            //namesAfterSave[sciName.id] = sciName.position.value();
            //utilsService.cleanUpGorm(true);
        } else {
            sciName.errors.allErrors.each { log.error it }
        }
		
		//WHY required here??
		//XXX: comment this once bulk migration is over
		addColHir(acceptedMatch);

		sciName.addSynonymFromCol(acceptedMatch.synList)
		return sciName
    }
   
    public def processDataFromUI(ScientificName sciName, Map acceptedMatch) {
        println "+++++++++++++++++++++++++++++++++++"
        println sciName
        println acceptedMatch
        println "+++++++++++++++++++++++++++++++++++"

        Map result = [:];
        result.errors = [];
        Map tempResult;
        try {
            boolean moveToRaw = acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.RAW.toString()):false;
            boolean moveToWKG =  acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.WORKING.toString()):false;
            boolean moveToClean =  acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.CLEAN.toString()):false;

            //boolean justMoveToAnotherList = false;
            //if(moveToRaw || moveToWKG || moveToClean) {
            //    justMoveToAnotherList = true;
            //}

            //if(!justMoveToAnotherList) {
            /*def upAt = updateAttributes(sciName, acceptedMatch);
            println  "====UP AT == " + upAt 
            if(upAt.isDeleted) {
            log.debug "MARKED AS DELETED ${sciName}"
            return;
            }*/
           

                println "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                println "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                println "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
                println "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
            tempResult = updateAttributes(sciName, acceptedMatch);
           println sciName.name 
            result.errors << tempResult.errors;
            
            println "======= RESULT FROM UpdateAttributes ${tempResult}";

            //adds IBP Hierarchy as well
            tempResult = updateStatus(sciName, acceptedMatch);
           println sciName.name 
            sciName = TaxonomyDefinition.get(sciName.id);
           println sciName.name 
            result.errors << tempResult.errors;
            println "======= RESULT FROM UpdateStatus ${tempResult}";
            /*if(r)
                sciName = r.sciName;
            println "=======AFTER STATUS======== " + sciName.status;
            */

            if(!acceptedMatch['parsedRank']) {
                acceptedMatch['parsedRank'] = XMLConverter.getTaxonRank(acceptedMatch.rank);
            }

            tempResult = updateRank(sciName, acceptedMatch.parsedRank);
            println "======= RESULT FROM UpdateRank ${tempResult}";
            result.errors << tempResult.errors;
            

            //WHY required here??
            //addColHir(sciName, acceptedMatch);
            //}

            def position;
            if(moveToClean) position = NamesMetadata.NamePosition.CLEAN;
            if(moveToWKG) position = NamesMetadata.NamePosition.WORKING;
            if(moveToRaw) position = NamesMetadata.NamePosition.RAW;

            tempResult = updatePosition(sciName, position); 
           println sciName.name 
            println "======= RESULT FROM UpdatePosition ${tempResult}";
            result.errors << tempResult.errors;
            //taxonService.moveToWKG([taxonReg]);


            println "=====SCI NAME ==== " + sciName
            println "=====SCI NAME CLASS ==== " + sciName.class
           println sciName.name 
            log.debug "Saving sciname ${sciName}"        
            if(!sciName.hasErrors() && sciName.save(flush:true)) {
                log.debug "Saved sciname ${sciName}" 
                sciName.updateNameSignature()
				//utilsService.cleanUpGorm(true);
                result.success = true;
            } else {
                result.success = false;
                result.msg = "Error while saving sciName";
                result.errors << sciName.errors.allErrors;
                sciName.errors.allErrors.each { 
                    log.error it 
                    println it;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            result.success = false;
            result.msg = e.getMessage();
        }
        return result;
    }

    def parentTaxonMatch(ScientificName sciName, List colData) {
        int noOfMatches = 0
        def acceptedMatch = null;
        List parentTaxons = sciName.immediateParentTaxonCanonicals() ;
        println "-==IMMEDIATE TAXONS == " + parentTaxons
        colData.each { colMatch ->
            println "COL MATCH PARENT TAXON == " + colMatch.parentTaxon
            if(parentTaxons.contains(colMatch.parentTaxon)){
                noOfMatches++;
                acceptedMatch = colMatch
            }
        }
        if(noOfMatches == 1) {
            //log.debug "[PARENT TAXON MATCH : SINGLE MATCH]  Accepting ${acceptedMatch}"
            return acceptedMatch;
        } else {
            log.debug "[CANONICAL+RANK : MULTIPLE MATCH TRYING PARENT TAXON MATCH] No single match on parent taxon match... leaving name for manual curation"
            acceptedMatch = null;
            return acceptedMatch;
        } 
    }

    //Handles name moving from accepted to synonym & vice versa
    //also updates IBP Hierarchy if no status change and its accepted name
    private def updateStatus(ScientificName sciName, Map colMatch) {
        //println "=======\nUPDATING STATUS of ${sciName} to ${colMatch.nameStatus} from ${colMatch}"
        
        boolean success = false;
        def errors = [];

        if(!colMatch.nameStatus) {
            errors << "Not updating name status as there is no new value"
            return [success:success, errors:errors];
        }

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
            def newStatus = getNewNameStatus(colMatch.nameStatus);
            println "===========NEW STATUS  === " + newStatus
            switch(newStatus) {
                case NameStatus.ACCEPTED :
                result = changeSynonymToAccepted(sciName, colMatch);
                sciName = result.lastTaxonInIBPHierarchy;        //changeSynonymToAccepted(sciName, colMatch);
                //result.sciName = result.lastTaxonInIBPHierarchy
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
                //changeAcceptedToSynonym(sciName, colMatch);
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
            //println "=========STATUS SAME  === "
            if(sciName.status == NameStatus.ACCEPTED) {
                colMatch.curatingTaxonId = sciName.id;
                //sciName = updateAttributes(sciName, colMatch)
                result = addColHir(colMatch);
                //sciName = result.lastTaxonInIBPHierarchy; 
                //println "======STATUS MEIN SCINAME==== " + sciName
                result.sciName = sciName //result.lastTaxonInIBPHierarchy;

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
        result.success = true;
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
        
        if(colAcceptedNameData.taxonConcept) return colAcceptedNameData.taxonConcept;

        //List<ScientificName> acceptedNameList = checkIfSciNameExists(colAcceptedNameData);
        ScientificName acceptedName = null;
        //if(acceptedNameList.size() == 0) {
            //create acceptedName
            log.debug "Creating/Updating accepted name of this synonym"
            def result = addColHir(colAcceptedNameData);
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

    private Map updateRank(ScientificName sciName, int rank) {
        //println "======= UPDATING RANK ============+"
        boolean success = false;
        List errors = [];
        if(sciName.rank != rank) {
            log.debug "Updating rank from ${sciName.rank} to ${rank}"
            sciName.rank = rank;
            //TODO: might require to add IBP hierarchy here
            if(!sciName.save(flush:true)) {
                success = false;
                errors << sciName.errors.allErrors;
                sciName.errors.allErrors.each { log.error it }
            } else {
                success = true;
            }            
        } else {
            success = false;
        }
        //println "======= UPDATING RANK DONE to ${sciName.rank}============+"
        return [success:success, errors:errors];
    }
        
    //A scientific name was also passed to this function but not used - so removed
	
    private def  addColHir(Map colAcceptedNameData) {
        //log.debug "------------------------------------------------------------------"
        //log.debug "------------------------------------------------------------------"
        //println "Adding COL hierarchy from ${colAcceptedNameData}"
        //log.debug "------------------------------------------------------------------"
        //log.debug "------------------------------------------------------------------"
        //  Because - not complete details of accepted name coming
        //  but its id is present - so searching COL based on ID
        //  Might happen when name changes from accepeted to synonym
        if(!colAcceptedNameData.kingdom && colAcceptedNameData.id) {
            def temp = colAcceptedNameData.curatingTaxonId
            println "SEARCHING COL for this accepted id"
            colAcceptedNameData = searchCOL(colAcceptedNameData.id, 'id')[0];
            colAcceptedNameData['source'] = colAcceptedNameData.matchDatabaseName;
            //println colAcceptedNameData;
            colAcceptedNameData.curatingTaxonId = temp;
        }
        def classification = Classification.fetchCOLClassification()
        Map taxonRegistryNamesTemp = fetchTaxonRegistryData(colAcceptedNameData).taxonRegistry;
        List taxonRegistryNames = [];
        taxonRegistryNamesTemp.each { key, value ->
            taxonRegistryNames[Integer.parseInt(key)] = value;
        }

        log.debug "Adding ${classification} ${taxonRegistryNames}"
        println "Adding ${classification} ${taxonRegistryNames}"
        SUser contributor = springSecurityService.currentUser?:SUser.read(1L) //findByName('admin');
        //to match the input format
        //getTaxonHierarchy() XMLConverter
        def metadata1 = [:]
		metadata1['authorString'] = colAcceptedNameData['authorString']
		metadata1['source'] = colAcceptedNameData['source']?:colAcceptedNameData['matchDatabaseName']
		metadata1['via'] = colAcceptedNameData['sourceDatabase']
        colAcceptedNameData['metadata'] = metadata1
        //println "=====T R N======= " + taxonRegistryNames
        //boolean fromCol = (colAcceptedNameData.fromCOL != null)? fromCOL : false
        //From UI uncomment
        def result;
        //TaxonomyRegistry.withNewSession {
            boolean fromCOL = false;
            println metadata1
            if('COL'.equalsIgnoreCase(metadata1['source']) || 'CatalogueOfLife'.equalsIgnoreCase(metadata1['source']) ||  'Catalogue Of Life'.equalsIgnoreCase(metadata1['source'])) {
                fromCOL = true; 
            }
            println "===============++"
            //println colAcceptedNameData
            result = taxonService.addTaxonHierarchy(colAcceptedNameData.name, taxonRegistryNames, classification, contributor, null, false, fromCOL, colAcceptedNameData);
        //}

        //From migration script
        //Also add to catalogue of life hierarchy
        //def colClassification = Classification.findByName(fieldsConfig.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY);
        //def result1 = taxonService.addTaxonHierarchy(colAcceptedNameData.name, taxonRegistryNames, colClassification, contributor, null, false, true, colAcceptedNameData);
        //def result = taxonService.addTaxonHierarchy(colAcceptedNameData.name, taxonRegistryNames, classification, contributor, null, false, true, colAcceptedNameData);
        //println result
        return result;
    }

    public Map updatePosition(ScientificName sciName, NamePosition position) {
        //println "\n============== UPDATING POSITION ========" + position
        boolean success = false;
        List errors = [];
        if(!position) {
            return [success:success, errors:['Position is empty']];
        }

        log.debug "Updating position from ${sciName.position} to ${position}"
        sciName.updatePosition(position.value())
		return [success:success, errors:errors];
    }

    List processColData(File f, ScientificName sn = null) {
		println "========= File --- " + f.absolutePath
        if(!f.exists()){
			println "File not found..."
			if(sn){
				println  "Downloading based on canonical name " + sn
				Utils.saveFiles(new File(grailsApplication.config.speciesPortal.namelist.rootDir), [sn], [])
			}
        }
		return _processColData(new XmlParser().parse(f), sn)
    }
	
	List processColData(String colId) {
		String text = Utils.getColDataAsText(colId)
		if(!text) return 
		def res = _processColData(new XmlParser().parseText(text))
		if(res){
			String taxCan = res[0].canonicalForm.replaceAll(' ', '_');
			File f = new File(new File(grailsApplication.config.speciesPortal.namelist.rootDir), taxCan + ".xml")
			if(taxCan && !f.exists()){
				f.createNewFile()
				println "-- Writing to file " + f.getAbsolutePath()
				f.write(text)
			}
		}
		return res
	}
		
	private _processColData(results, ScientificName sn = null){
        try{
            String errMsg = results.'@error_message'
            int resCount = Integer.parseInt((results.'@total_number_of_results').toString()) 
            if(errMsg != ""){
                log.debug "11Error in col response " + errMsg
                if(sn){
                	sn.dirtyListReason = "COL : " + errMsg
                	log.debug "settting error " +  sn.dirtyListReason 
                	if(!sn.save(flush:true)){
                		sn.errors.allErrors.each { log.error it }
                	}
                }
                return
            }

            /*if(resCount != 1 ){
              log.debug "Multiple result found [${resCount}]. so skipping this ${f.name} for manual curation"
              return
              }*/

            //Every thing is fine so now populating CoL info
            List res = responseAsMap(results, "id")

            log.debug "================   Response map   =================="
            //log.debug res
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
            result['taxonRegistry.9'] = res['9'] = m['species']     //TODO:check author year coming or not + " " + m['authorString']
        } else {
            result['taxonRegistry.9'] = res['9'] = m['species'];    
        }
        if(m['rank'] == 'infraspecies'){
            if(m['species']) {
				def authStr = ""
                if(m['id_details']) {
					authStr = searchCOL(m.id_details[m['species'] + "#" + "9"], "id")[0].authorString;
					m.id_details[m['genus'] + " " +m['species']+ "#" + "9"] = m.id_details[m['species']+ "#" + "9"]
                }
                result['taxonRegistry.9'] = res['9'] = m['genus'] + " " +m['species'] + " " + authStr?:"";    
            }
            result['taxonRegistry.10'] = res['10'] = m['infraspecies']      //TODO:check author year coming or not + " " + m['authorString'];
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
            result.add(temp);
        }
        return result
    }

    def getAcceptedNamesOfCommonNames(String comName) {
        def r = CommonNames.findAllByNameAndIsDeleted(comName, false);
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
            result.add(temp);
        }
        return result
    }

    def getCommonNamesOfTaxon(TaxonomyDefinition taxonConcept) {
        def res = CommonNames.findAllByTaxonConceptAndIsDeleted(taxonConcept, false);
        def result = []
        res.each {
            def temp = [:]
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
            response.'404' = { 
            println '404 - Not found' 
            return null
            }
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

    private def changeSynonymToAccepted(ScientificName sciName,  Map colMatch) {
        //Remove as synonym from all accepted names
        sciName.removeAsSynonym();
        //sciName = updateAttributes(sciName, colMatch)
        colMatch.curatingTaxonId = sciName.id;
        colMatch.curatingTaxonStatus = sciName.status;
        //Change status and class for this row entry in database
        updateStatusAndClass(sciName, NameStatus.ACCEPTED)
        sciName = TaxonomyDefinition.get(sciName.id);
        //Add IBP Hierarchy to this name
        //TODO Pass on id information of last node
        def result = addColHir(colMatch)
        //sciName = result.lastTaxonInIBPHierarchy;
        return result;
    }
    
    private ScientificName changeAcceptedToSynonym(ScientificName sciName,  Map colMatch) {
        if(colMatch.acceptedNamesList && colMatch.acceptedNamesList.size() == 0) {
            log.error "No acceptedMatch for the synonym. Ignoring status change"
            return sciName
        }
        println "======PROCESS START ACCEPTED TO SYNONYM===="
        //Attach its synonyms to the new accepted suggested by COL
        def oldSynonyms = sciName.fetchSynonyms();
        println "======OLD SYNONYMS OF THAT ==== " + oldSynonyms
        //Remove as accepted name from all synonyms 
        sciName.removeAsAcceptedName();
        println "======REMOVED AS ACCEPTED NAME ==== "
        
        //sciName = updateAttributes(sciName, colMatch)
        //Change status and class for this row entry in database
        updateStatusAndClass(sciName, NameStatus.SYNONYM)
        sciName = SynonymsMerged.get(sciName.id);
        println sciName
        println "==============+++++"
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

    private ScientificName updateStatusAndClass1(ScientificName sciName, NameStatus status) {
        sciName = sciName.merge();
        String query = "";
        println "=======RUNNING SQL TO UPDATE CLASS=========="
        if(status == NameStatus.ACCEPTED) {
            println "=======MAKING IT ACCEPTED=========="
            sciName.relationship = null;
            /*TaxonomyDefinition.executeUpdate(
                    "update TaxonomyDefinition t set t.class = :klass where t.id = :id ", 
                        [klass:'species.TaxonomyDefinition', id:sciName.id]);
            */
            //def sql =  Sql.newInstance(dataSource);
            query = "update taxonomy_definition set (class, status) = (:class, :status) where id = :id";
            def session = sessionFactory.getCurrentSession()
            def sql= session.createSQLQuery(query)
            sql.setProperties([id:sciName.id, class:'species.TaxonomyDefinition', status:status.toString()]).executeUpdate();
            println " ========executed query =="
            //utilsService.cleanUpGorm(true);
            
            //sciName.class = 'species.TaxonomyDefinition';
            //TODO: CHK: sciName = TaxonomyDefinition.get(sciName.id.toLong())
            //println "CASTING SYNONYM TO TAXONDEFINITION"
            //sciName = (TaxonomyDefinition) sciName;
            //println sciName.class
        } else {
            println "=======MAKING IT SYNONYM=========="
            
		    //def sql = new Sql(dataSource)
            query = "update taxonomy_definition set (class, status, relationship) = (:class, :status, :relationship) where id = :id";
            def session = sessionFactory.getCurrentSession()
            def sql = session.createSQLQuery(query)
            sql.setProperties([id:sciName.id, class:'species.SynonymsMerged', relationship:ScientificName.RelationShip.SYNONYM.toString(), status:status.toString()]).executeUpdate();
            println " ========executed query =="
            //utilsService.cleanUpGorm(true);
            
            /*TaxonomyDefinition.executeUpdate(
                    "update TaxonomyDefinition t set t.class = :klass where t.id = :id ", 
                        [klass:'species.SynonymsMerged', id:sciName.id]);
            */
            //sciName.class = 'species.SynonymsMerged';
            //sciName = new SynonymsMerged(sciName.properties);
            //println "=============COPYING PROPERTIES TO SynonymsMerged";
            //UtilsUtils.copyProperties(sciName, persistentSciName, false);
            //println sciName
            //sciName = sciName.merge();
            //println sciName
            //println persistentSciName
            //sciName = persistentSciName;
            //sciName.relationship = ScientificName.RelationShip.SYNONYM;
        }
        //sciName.status = status;
        //return sciName;
    }

    private Map updateAttributes(ScientificName sciName, Map colMatch, doNotSearch = false) {
        //println "\n UPDATING ATTRIBUTES ${sciName} with ${colMatch}"
        boolean success = false;
        def errors = [];
        try {
            NamesParser namesParser = new NamesParser();
            if(!colMatch.canonicalForm) colMatch.canonicalForm = colMatch.name;
            def name = colMatch.canonicalForm
            if(colMatch.authorString)
                name = name + " " + colMatch.authorString
            if(!doNotSearch){
                def res1 = searchIBP(colMatch.canonicalForm, colMatch.authorString, NameStatus.ACCEPTED , sciName.rank);
                def res2 = searchIBP(colMatch.canonicalForm, colMatch.authorString, NameStatus.SYNONYM , sciName.rank);
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
                            it = it.merge();
                            if(!it.save(/*flush:true*/)) {
                                it.errors.allErrors.each { log.error it }
                            }
                        }
                    }
                    sciName.flaggingReason = sciName.flaggingReason + " ### " + flaggingReason;
                    if(!sciName.findSpeciesId()) {
                        sciName.isDeleted = true;
                    }
                }
            }
            def parsedNames = namesParser.parse([name]);
            println "=============PARSING THIS ========== " + name
            def pn = parsedNames[0];
            if(pn.canonicalForm) {
                println "============= " + pn.canonicalForm +"============= "+ pn.name
                sciName.canonicalForm = pn.canonicalForm
                sciName.binomialForm = pn.binomialForm
                sciName.normalizedForm = pn.normalizedForm
                sciName.italicisedForm = pn.italicisedForm
                sciName.name = pn.name
                println sciName.name
                println sciName.canonicalForm
            }
            sciName.authorYear = colMatch.authorString;
            if(sciName.colNameStatus) {
                sciName.colNameStatus = getCOLNameStatus(colMatch.colNameStatus);
            }

            sciName.matchId = colMatch.externalId;
            sciName.matchDatabaseName = colMatch.matchDatabaseName;
            sciName.viaDatasource = colMatch.sourceDatabase;
            /*
            sciName.position = NamePosition.WORKING;
            sciName = sciName.merge();
            println "==========SCI NAME AFTER MERGE ======== " + sciName
             */
            if(!sciName.save(flush:true)) {
                success = false;
                errors = sciName.errors.allErrors;
                sciName.errors.allErrors.each { println it; log.error it }
            } else {
                success = true;
            }
            
            //println "=========DONE UPDATING ATTRIBUTES ========\n" + sciName

        } catch (Exception e) {
            success = false;
            errors << e.getMessage();
            e.printStackTrace();
            println "=========ERROR WHILE UPDATING ATTRIBUTES ========\n"
        }
        return [success:success, errors:errors, sciName:sciName]; 
        //:sciName,isDeleted:false];
    }

///////////////////OBV RECO NAMES/////////////////////////

    public def curateRecoName(Recommendation reco , List colData) {      
        log.debug "Curating reco name ${reco.name} id ${reco.id} with col data ${colData}"
        def acceptedMatch = null;

        if(!colData) return;

        //check if this is a single direct match
        if(colData.size() == 1 ) {
            //Reject all (IBP)scientific name -> (CoL) common name matches (leave for curation).
            if(colData['nameStatus'] == NamesMetadata.COLNameStatus.COMMON.value()) {
                //reject ... position remains RAW
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
                //println "============ACCEPTED MATCH ======= " + acceptedMatch
            }
        }
        if(acceptedMatch) {
            //println "================ACCEPTED MATCH=========================== " + acceptedMatch
            //log.debug "There is an acceptedMatch ${acceptedMatch} for recommendation ${reco.name}. Updating link"
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
                            acceptedName = TaxonomyDefinition.get(colAcceptedNameData.acceptedNameId)
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
        println oldValue + "===================================" + newValue;
        if(!oldValue.equalsIgnoreCase(newValue)) {
            desc = fieldName + " changed from " + oldValue + " to " + newValue +" .";
        }
        return desc;
    }

 
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public List searchCOL(String input, String searchBy) {
		//http://www.catalogueoflife.org/col/webservice?name=Tara+spinosa
		
		File sourceDir = new File(Holders.config.speciesPortal.namelist.rootDir)
		if(searchBy == 'name'){
			File xmlFile = new File(sourceDir,  "" + input.replaceAll(' ', '_') + ".xml")
			if(xmlFile.exists()){
				println "COL ::: XML file found"
				return responseAsMap(xmlFile.text, searchBy)
			}
		}
		
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
				def result = responseAsMap(xmlText, searchBy);
				return result;
			}
			response.'404' = { println 'Not found' }
		}
	}

	public List searchGBIF(String input, String searchBy){
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
		def results = []
		try {
			results = new XmlParser().parseText(xmlText)
			return responseAsMap(results, searchBy)
		}catch(e){
			log.debug "Error in xml text "  + xmlText
			e.printStackTrace()
		}
		//returning empty result
		return results
	}

	String generateVerbatim (colResult) {
		String verbatim = '';
		int origRank = XMLConverter.getTaxonRank(colResult.rank?.text()?.toLowerCase());
		if(origRank <= TaxonomyRank.SPECIES.ordinal()) {
			verbatim = colResult?.name?.text() + " " +colResult?.author?.text().capitalize();
		} else {
			String genus = colResult?.genus?.text() + ' ';
			String species = colResult?.species?.text() + ' ';
			String infraSpeciesMarker = '';
			if(colResult?.infraspecies_marker) {
				infraSpeciesMarker = colResult?.infraspecies_marker.text() + ' ';
			}
			String infraSpecies = '';
			if(colResult?.infraspecies) {
				infraSpecies = colResult?.infraspecies.text() + ' ';
			}
			String authorYear = '';
			if(colResult?.author) {
				authorYear = colResult?.author.text().capitalize();
			}
			verbatim = genus + species + infraSpeciesMarker + infraSpecies + authorYear;
		}
		return verbatim
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
			List colIdPath = []
			List colNamePath = []
			temp['externalId'] = r?.id?.text()//+"'"
			temp['matchDatabaseName'] = "CatalogueOfLife"
			temp['canonicalForm'] = r?.name?.text();
			temp['rank'] = r?.rank?.text()?.toLowerCase()
			temp[r?.rank?.text()?.toLowerCase()] = generateVerbatim(r);     //r?.name?.text()
			//GENERATING VERBATIM BASED ON RANK
			temp['name'] = generateVerbatim(r);
			def tmpRank =  XMLConverter.getTaxonRank(r?.rank?.text()?.toLowerCase());
			id_details[r?.name?.text() + "#" + tmpRank] = r?.id?.text();
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
			//println "==========NAME STATUS========= " + temp['nameStatus']
			if(temp['nameStatus'] == "synonym") {
				def aList = []
				r.accepted_name.each {
					def m = [:]
					m['id'] = it.id.text()
					m['name'] = generateVerbatim(it)        //it.name.text() + " " + it.author.text().capitalize();;
					m['canonicalForm'] = it.name.text();
					m['nameStatus'] = it.name_status.text()?.tokenize(' ')[0];
					m['colNameStatus'] = it.name_status?.text()?.tokenize(' ')[0]
					m['rank'] = it.rank?.text()?.toLowerCase();
					m['authorString'] = it.author.text().capitalize();;
					m['source'] = "CatalogueOfLife"
					aList.add(m);
				}
				//println "======A LIST======== " + aList;
				temp['acceptedNamesList'] = aList;
			}
			if(searchBy == 'id' || searchBy == 'name') {
				//println "============= references  "
				r.references.reference.each { ref ->
				//println ref.author.text()
				//println ref.source.text()
				}

				//println "============= higher taxon  "
				int maxRank = -1;
				r.classification.taxon.each { t ->
					//println t.rank.text() + " == " + t.name.text()
					tmpRank =  XMLConverter.getTaxonRank(t?.rank?.text()?.toLowerCase());
					temp[t?.rank?.text()?.toLowerCase()] = t?.name?.text()
					id_details[t?.name?.text() + "#" + tmpRank] = t?.id?.text()
					colIdPath << t?.id?.text()
					colNamePath << t?.name?.text()
					int currRank = XMLConverter.getTaxonRank(t?.rank?.text()?.toLowerCase());
					if(currRank > maxRank) {
						temp['parentTaxon'] =  t?.name?.text()
						maxRank = currRank
					}
				}

				//println "============= child taxon  "
				r.child_taxa.taxon.each { t ->
				// println t.name.text()
				// println t.author.text()
				}

				if(temp['nameStatus'] == "accepted") {
					def synList = []
					r.synonyms.synonym.each {
						def m = [:]
						m['id'] = it.id.text()
						m['name'] = generateVerbatim(it);        //it.name.text() + " " + it.author.text().capitalize();;
						m['canonicalForm'] = it.name.text();
						m['nameStatus'] = it.name_status.text()?.tokenize(' ')[0];
						m['colNameStatus'] = it.name_status?.text()?.tokenize(' ')[0]
						m['rank'] = it.rank?.text()?.toLowerCase();
						m['parsedRank'] = XMLConverter.getTaxonRank(m.rank);
						m['authorString'] = it.author.text().capitalize();;
						m['source'] = "CatalogueOfLife"
						synList.add(m);
					}
					//println "======A LIST======== " + aList;
					temp['synList'] = synList;
				}
				//r.synonyms.synonym.each { s ->
				//println s.rank.text() + " == " + s.name.text()
				//println "============= references  "
				//s.references.reference.each { ref ->
				//println ref.author.text()
				//println ref.source.text()
				//}
				//}
				/*
				println "==========NAME STATUS========= " + temp['nameStatus']
				if(temp['nameStatus'] == "synonym") {
					def aList = []
					r.accepted_name.each {
						def m = [:]
						m['id'] = it.id.text()
						m['name'] = it.name.text()
						m['source'] = "CatalogueOfLife"
						aList.add(m);
					}
					println "======A LIST======== " + aList;
					temp['acceptedNamesList'] = aList;
				}
				*/
			}
			
			temp['id_details'] = id_details
			temp['colIdPath'] = colIdPath.join("_").trim()
			temp['colNamePath'] = colNamePath.join("->").trim()
			finalResult.add(temp);
		}
		return finalResult
	}

	List responseFromGBIFAsMap(String xmlText , String searchBy) {
		def result = JSON.parse(xmlText)
		println "=====RESULT ===== " + result
		def finalResult = []
		if(!result['usageKey']) {
			return finalResult
		}
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

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
    public def getNamesFromTaxon(params){
    	println "===================== params " + params 
        def sql = new Sql(dataSource)
        def sqlStr, rs
        def classSystem = params.classificationId.toLong()
        def parentId = params.parentId;
        def limit = params.limit ? params.limit.toInteger() : 100
        def offset = params.offset ? params.offset.toLong() : 0
        def parentTaxon = TaxonomyDefinition.read(parentId.tokenize('_')[-1].toLong());
        def nextPrimaryRank = TaxonomyRank.nextPrimaryRank(parentTaxon.rank)
        List ranksToFetch = params.ranksToFetch ? getRanksToFetch(parentTaxon.rank, params.ranksToFetch.split(',')):[parentTaxon.rank, nextPrimaryRank];
        List statusToFetch = params.statusToFetch ? (params.statusToFetch).split(','):[NameStatus.ACCEPTED.value().toUpperCase(), NameStatus.SYNONYM.value().toUpperCase()];
        List positionsToFetch = params.positionsToFetch ? (params.positionsToFetch).split(','):[NamePosition.RAW.value().toUpperCase(), NamePosition.WORKING.value().toUpperCase(), NamePosition.CLEAN.value().toUpperCase()];
        String exportFields = "t.id as id, t.id as taxonid, t.rank as rank, t.name as name, t.italicised_form as italicisedform, t.is_flagged as isflagged, t.flagging_reason as flaggingreason, t.position as position, t.status as status, ${classSystem} as classificationid";
        String synExportFields = "";
        List reqExportFields = params.exportFields
        if(reqExportFields) {
            exportFields = "";
            TaxonomyDefinition.fetchExportableFields(grailsApplication).each {
                if(reqExportFields.contains(it.field)) {
                    if(it.dbField) 
                        exportFields += "t."+it.dbField +' as "'+it.name+'", ';
                }
            }
            exportFields = exportFields[0..-3];
            synExportFields = exportFields;
        } else {
            synExportFields = exportFields.replace('t.id as id,', '');
            synExportFields += ", concat(acsy.id, '_', t.id) as id ";
            exportFields += ", s.path as path  ";
        }
        List namesList = []
        //Map dirtyList = [:]
        //Map workingList = [:]
        //Map cleanList = [:]

        int instanceTotal = 0;
        int dirtyListCount = 0, workingListCount = 0, cleanListCount=0;
        int acceptedCount = 0, synonymCount = 0, commonNameCount=0;
        //List accDL = [], accWL = [], accCL = []
        //List synDL = [], synWL = [], synCL = []
        //List comDL = [], comWL = [], comCL = []
        //List speciesDL = [], speciesWL = [], speciesCL = []
        String countSqlStr='', synCountSqlStr='';
        //CHK: does this query handle synonyms also ... do synonyms have hierarchy? or shd we put status=accepted condition in this query?
        countSqlStr = "select t.position, t.status, count(*) as count \
                    from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+
                    "s.path like '"+parentId+"%' and "+
                    "t.status = '" +NameStatus.ACCEPTED+"' and "+
                    "t.rank in ("+ranksToFetch.join(',')+ ") and "+
                    "t.position in ('"+positionsToFetch.join("','")+ "')  and t.is_deleted = false "+
                    "group by t.position, t.status ";

        synCountSqlStr = "select s.position, s.status, count(*) as count from taxonomy_definition s, accepted_synonym acsy where s.id = acsy.synonym_id and acsy.accepted_id in ( select t.id from taxonomy_registry s, taxonomy_definition t where s.taxon_definition_id = t.id and "+ (classSystem?"s.classification_id = :classSystem and ":"") + "s.path like '"+parentId+"%' and t.is_deleted = false and " + "t.rank in ("+ranksToFetch.join(',') + ")) and "+"s.rank in ("+ranksToFetch.join(',') + ") and s.position in ('"+positionsToFetch.join("','") + "') group by s.position, s.status";


        try {
            if(!parentId) {
                 sqlStr = "select "+exportFields+
                     "from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+
                    "t.status = '" +NameStatus.ACCEPTED+"' and t.is_deleted = false and "+
                    "t.rank = 0 order by s.path, t.name";

                //ALways fetch from IBP Taxonomy Hierarchy
                //def fieldsConfig = grailsApplication.config.speciesPortal.fields
                //def IBPclassification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
                 rs = sql.rows(sqlStr, [classSystem:classSystem])
            } else {
                 sqlStr = "select "+exportFields+
                    "from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+
                    "s.path like '"+parentId+"%' and " +
                    "t.status = '" +NameStatus.ACCEPTED+"' and "+
                    "t.rank in ("+ranksToFetch.join(',')+ ") and t.is_deleted = false and "+
                    "t.position in ('"+positionsToFetch.join("','")+ "') "+
                    " order by s.path, t.name asc limit :limit offset :offset";

                //ALways fetch from IBP Taxonomy Hierarchy
                //def fieldsConfig = grailsApplication.config.speciesPortal.fields
                //def IBPclassification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
                log.debug "======================+++++++"
                log.debug sqlStr;
                log.debug "parentId ${parentId}, classSystem ${classSystem} ranksToFetch ${ranksToFetch} statusToFetch ${statusToFetch} positionsToFetch ${positionsToFetch} limit ${limit} offset ${offset}"

                rs = sql.rows(sqlStr, [classSystem:classSystem, limit:limit, offset:offset]);
           }

            log.debug "total result size === " + rs.size()

            def countRs = sql.rows(countSqlStr, [classSystem:classSystem])
            countRs.each {
                instanceTotal += it.count;
                if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.RAW.value())){
                    dirtyListCount += it.count;
                }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                    workingListCount += it.count;
                }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.CLEAN.value())){
                    cleanListCount += it.count;
                }

                switch(it.status.toLowerCase()) {
                    case NameStatus.ACCEPTED.value().toLowerCase() : acceptedCount += it.count; break;
                    case NameStatus.SYNONYM.value().toLowerCase() : synonymCount += it.count; break;
                }
            }

            if(statusToFetch.contains(NameStatus.SYNONYM.value().toUpperCase())) {
                def synCountRs = sql.rows(synCountSqlStr, [classSystem:classSystem])
                synCountRs.each {
                    instanceTotal += it.count;
                    if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.RAW.value())){
                        dirtyListCount += it.count;
                    }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
                        workingListCount += it.count;
                    }else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.CLEAN.value())){
                        cleanListCount += it.count;
                    }

                    switch(it.status.toLowerCase()) {
                        case NameStatus.ACCEPTED.value().toLowerCase() : acceptedCount += it.count; break;
                        case NameStatus.SYNONYM.value().toLowerCase() : synonymCount += it.count; break;
                    }
                }
            }

            ///////////////////////////////
            rs.each {
                if(statusToFetch.contains(NameStatus.ACCEPTED.value().toUpperCase())) {
                    namesList << it;
                }
                //NOT SENDING PATH
                //SENDING IDS as taxonid for synonyms and common names
                if(statusToFetch.contains(NameStatus.SYNONYM.value().toUpperCase())) {
                    println "GETTING SYNONYMS FOR TAXON ${it.taxonid}"
                    sql = new Sql(dataSource)
                    //FIX:limit is not applied on synonyms query

                    def s1 = "select "+synExportFields+" from taxonomy_definition t, accepted_synonym acsy where t.id = acsy.synonym_id and acsy.accepted_id = :taxonId  and t.is_deleted = false and " + "t.rank in ("+ranksToFetch.join(',') + ") and  t.position in ('"+positionsToFetch.join("','") + "') order by t.name";
                    println s1
                    def q1 = sql.rows(s1, [taxonId:it.taxonid])
                    q1.each {
                        println "==========TAXA IDS======= " + it.taxonid
                        namesList << it
                    }
                }

                /*if(statusToFetch.contains(NameStatus.COMMON.value())) {
                    sql = new Sql(dataSource)
                    def s2 = "select c.id as taxonid, ${it.rank} as rank, c.name as name , ${classSystem} as classificationid, position as position, status as status\
                    from common_names c where c.taxon_concept_id = :taxonId order by c.name";

                    def q2 = sql.rows(s2, [taxonId:it.taxonid])
                    q2.each {
                    }
                }*/

            }

            return [namesList:namesList, statusToFetch:statusToFetch, positionsToFetch:positionsToFetch, ranksToFetch:ranksToFetch, instanceTotal:instanceTotal, dirtyListCount:dirtyListCount, workingListCount:workingListCount, cleanListCount:cleanListCount, acceptedCount:acceptedCount, synonymCount:synonymCount, limit:limit, offset:offset]
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private List getRanksToFetch(def parentRank, def rF) {
        List ranksToFetch = [parentRank];
        
        rF.each {
            int rank = Integer.parseInt(it);
            if(rank > parentRank) ranksToFetch << rank;
        }
        return ranksToFetch;
    }

	public def getNameDetails(params){
        def taxon = TaxonomyDefinition.read(params.taxonId.toLong());
        NameStatus nameType = taxon.status;
        boolean isTaxonEditor = speciesPermissionService.isTaxonContributor(taxon, springSecurityService.currentUser, [SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR]);
        def result;
        switch(nameType) {
            case NameStatus.ACCEPTED:
            def taxonDef = taxon;
            def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()), taxonDef);
            result = taxonDef.fetchGeneralInfo()
            result['taxonId'] = params.taxonId;
            result['isTaxonEditor'] = isTaxonEditor;

            if(taxonReg) {
                result['taxonRegId'] = taxonReg.id?.toString()
                taxonReg.path.tokenize('_').each { taxonDefinitionId ->
                    def td = TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId));
                    result.put(TaxonomyRank.getTRFromInt(td.rank).value().toLowerCase(), [td.id,td.name]);
                }
            }
            result['synonymsList'] = getSynonymsOfTaxon(taxonDef);
            result['commonNamesList'] = getCommonNamesOfTaxon(taxonDef);
            /*def counts = getObvCKLCountsOfTaxon(taxonDef);
            result['countObv'] = counts['countObv'];
            result['countCKL'] = counts['countCKL'];
            result['countSp'] = getSpeciesCountOfTaxon(taxonDef);
            println "=========COUNTS============= " + counts
             */
            println "----------- "  + result
            return result;
            case NameStatus.SYNONYM:
            println "----------------------____"
            //if(params.choosenName && params.choosenName != '') {
                //taxonId here is id of synonyms table
                def syn = SynonymsMerged.read(params.taxonId.toLong());
                result = syn.fetchGeneralInfo();
                result[result['rank']] = params.choosenName;
                result['acceptedNamesList'] = getAcceptedNamesOfSynonym(syn);
                result['isTaxonEditor'] = isTaxonEditor;
                println "========SYNONYMS NAME DETAILS ===== " + result

                def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()), syn);
                if(taxonReg) {
                    result['taxonRegId'] = taxonReg.id?.toString()
                    taxonReg.path.tokenize('_').each { taxonDefinitionId ->
                        def td = TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId));
                        result.put(TaxonomyRank.getTRFromInt(td.rank).value().toLowerCase(), [td.id,td.name]);
                    }
                }

                return result
            //}
            case NameStatus.COMMON :
            //if(params.choosenName && params.choosenName != '') {
                //taxonId here is id of common names table
                def com = CommonNames.read(params.taxonId.toLong());
                result = com.fetchGeneralInfo()
                result[result['rank']] = params.choosenName;
                result['isTaxonEditor'] = isTaxonEditor;
                result['acceptedNamesList'] = getAcceptedNamesOfCommonNames(params.choosenName);
                println "========SYNONYMS NAME DETAILS ===== " + result
                return result
            //}
        }
	}

	
	public static void clearSessionNewNames(){
		NEW_NAME_IN_SESSION.clear()
	}
	
	public static void addNewNameInSession(TaxonomyDefinition td){
		if(!td || !td.id)
			return
			
		NEW_NAME_IN_SESSION.put(td.canonicalForm + "##" + td.rank , td.id)
	}
	
	public static TaxonomyDefinition getNewNameInSession(String canonicalForm, int rank){
		if(!canonicalForm)
			return
			
		def tdId = NEW_NAME_IN_SESSION.get(canonicalForm + "##" + rank)
		return TaxonomyDefinition.get(tdId)
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////  Name list API ///////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean changeAccToSyn(long oldId, long newId){
		ScientificName oldName = TaxonomyDefinition.get(oldId)
		ScientificName newName = TaxonomyDefinition.get(newId)
		
		if(oldName && (oldName.status == NamesMetadata.NameStatus.SYNONYM)){
			log.debug "Name is already synonym... nothing to do... RETURNING " + oldId
			return true
		}
		
		if(!oldName || !newName){
			log.debug "Null id is given for the names  old id " + oldId + " new name " + newName
			return false
		}
		
		if(newName.status != NamesMetadata.NameStatus.ACCEPTED){
			log.debug "Status of new name  is not acceepted aborting... new name id " + newId
			return false
		}
		
		_mergeAccepted(oldName, newName, false)
		//cache is cleared so reading name again
		oldName.refresh()// = TaxonomyDefinition.get(oldId)
		
		updateStatusAndClass(oldName,  NamesMetadata.NameStatus.SYNONYM)
		utilsService.clearCache("defaultCache")
		utilsService.cleanUpGorm()
		
		newName = TaxonomyDefinition.get(newId)
		oldName = SynonymsMerged.get(oldId)

		//adding this synonym to new accepted name
		newName.addSynonym(oldName)
		return true
	}
	
	public boolean changeSynToAcc(long oldId, Map hirMap=null){
		ScientificName oldName = TaxonomyDefinition.get(oldId)
		
		if(!oldName){
			log.debug "Null id is given for the names  old id " + oldId
			return false
		}
		
		if(oldName && (oldName.status == NamesMetadata.NameStatus.ACCEPTED)){
			log.debug "Name is already accepted... nothing to do... RETURNING " + oldId
			return true
		}
		
		//removing this name as synonym from all other accepted name
		oldName.removeAsSynonym()
		updateStatusAndClass(oldName,  NamesMetadata.NameStatus.ACCEPTED)
		
		//TODO have to add ibp hir fir this changed name
		return true
	}
	
	
	public boolean updateNamePosition(long oldId, String position, Map hirMap=null){
		TaxonomyDefinition oldName = TaxonomyDefinition.get(oldId)
		NamesMetadata.NamePosition newPosition = NamesMetadata.NamePosition.getEnum(position)
		
		if(!oldName || !newPosition){
			log.debug "Null id is given for the names  old id " + oldId + " Or position is wrong " + position
			return false
		}

		TaxonomyDefinition.executeUpdate( "update TaxonomyDefinition set position = :newPosition where id = :id",[newPosition:newPosition, id:oldName.id])
		return true
	}
	
	public boolean deleteName(long id){
		ScientificName name = TaxonomyDefinition.get(id)
		if(!name || name.isDeleted){
			log.debug "Null id is given for the name or name is already deleted... ABORTING " + id
			return false
		}
		
		if(name.status == NamesMetadata.NameStatus.SYNONYM){
			return mergeSynonym(name, null)
		}
		
		//its an accepted name
		Classification ibpClassification = Classification.fetchIBPClassification()
		int tCount = TaxonomyRegistry.countByParentTaxonDefinitionAndClassification(name, ibpClassification)
		if(tCount > 0){
			log.debug "Given name has children in ibp classification... Aborting delete ... child count  " +  tCount
			return false
		}
		
		//every thing passed now deleting the accepted name
		return _mergeAccepted(name, null)
	}
	
	
	public boolean mergeNames(long oldId, long newId){
		ScientificName oldName = TaxonomyDefinition.get(oldId)
		ScientificName newName = TaxonomyDefinition.get(newId)
		
		if(!oldName || !newName){
			log.debug "Null id is given for the names  old id " + oldId + " new name " + newName
			return false
		}
		
		
		if(oldName.status != newName.status){
			log.debug "Status is not same for  the name " + oldName.status + " new name " + newName.status
			return false
		}
		
		if(oldName.status == NamesMetadata.NameStatus.SYNONYM){
			return mergeSynonym(oldName, newName)
		}
		
		//status is accepted
		return mergeAccepted(oldName, newName)
		
	}
	
	
	private void updateStatusAndClass(ScientificName sciName, NameStatus status) {
		//sciName.relationship = (status == NameStatus.SYNONYM)? ScientificName.RelationShip.SYNONYM.toString():null
		sciName.status = status
		if(!sciName.save(flush:true)){
			sciName.errors.allErrors.each { log.error it }
		}

		Map m = [id:sciName.id]
		m['class'] = (status == NameStatus.SYNONYM) ? SynonymsMerged.class.canonicalName: TaxonomyDefinition.class.canonicalName
		m['relationship'] = (status == NameStatus.SYNONYM)? ScientificName.RelationShip.SYNONYM.toString():''
		m['status'] = status.toString()

		String query = "update taxonomy_definition set class = :class,relationship = :relationship,status = :status where id = :id";
		def sql = sessionFactory.getCurrentSession().createSQLQuery(query)
		sql.setProperties(m).executeUpdate()
	}
	
	private boolean mergeAccepted(oldName, newName){
		if(oldName.rank != newName.rank){
			log.debug "Rank is not same... Aborting merge " + oldName.rank + "  new name " + newName.rank
			return false
		 }
		
		
		if(oldName.fetchRootId() != newName.fetchRootId()){
			log.debug "Kingdom is not same... Aborting merge " + oldName.fetchRootId() + "  new name " + newName.fetchRootId()
			return false
		 }
		
		log.debug "All validation passed Merging names..." + oldName + " new name " + newName
		
		return _mergeAccepted(oldName, newName)
	}

	private boolean _mergeAccepted(oldName, newName, boolean flagAsDelete = true) {
		//moving synonym, common names and document sci name
		moveOtherNamesReference(oldName, newName)

		//move recommendation and observation
		moveRecoAndObv(oldName, newName)

		//move species content and resources
		moveSpeciesContent(oldName, newName)


		//flag the name as deleted
		if(flagAsDelete){
			oldName.isDeleted = true
			log.debug"======= for delete " + oldName
			if(!oldName.save(flush:true)){
				oldName.errors.allErrors.each { log.error it }
				return false
			}
		}

		//moving existing taxon reg and deleteing ibp hir
		moveTaxonReg(oldName, newName)

		utilsService.clearCache("defaultCache")

		return true
	}
	
	
	
	private boolean mergeSynonym(SynonymsMerged oldName, SynonymsMerged newName){

		//moving synonym
		def oldEntries = AcceptedSynonym.findAllBySynonym(oldName);
		oldEntries.each { e ->
			TaxonomyDefinition acName = e.accepted
			acName.removeSynonym(oldName)
			if(newName){
				acName.addSynonym(newName)
			}
		}

		moveSpeciesContent(oldName, newName)

		//XXX save is not updating so using hiberet call to set isdelete flag
		SynonymsMerged.executeUpdate( "update SynonymsMerged set isDeleted = true where id = (:id) ", [id:oldName.id])
		
		utilsService.clearCache("defaultCache")
		return true
	}


	private moveSpeciesContent(oldName, newName){
		Species oldSpecies = Species.get(oldName.findSpeciesId())
		if(!oldSpecies){
			log.debug "Species page is not there... nothing to move " + oldName
			return
		}
		
		//old species page is there but new name is not given which means want to delete species content
		if(!newName){
			oldSpecies.deleteSpecies(null)
			return
		}
		
		Species newSpecies = Species.get(newName.findSpeciesId())
		
		//updating species for names
		if(!newSpecies && oldSpecies){
			oldSpecies.taxonConcept = newName
			if(!oldSpecies.save(flush:true)){
				oldSpecies.errors.allErrors.each { log.error it }
			}
			log.debug "  new speices not available"
		}
		
		if(newSpecies && oldSpecies){
			//move sfield
			SpeciesField.findAllBySpecies(oldSpecies).each {sf ->
				sf.species = newSpecies
				if(!sf.save(flush:true)){
					sf.errors.allErrors.each { log.error it }
				}
			}
			
			//move resources
			def ress = oldSpecies.resources.collect { it}
			ress.each { res ->
				log.debug "Removing resource " + res
				newSpecies.addToResources(res)
				oldSpecies.removeFromResources(res)
			}
			
			//add hyper link for redirect
			ResourceRedirect.addLink(oldSpecies, newSpecies)
			
			//saving new species
			if(!newSpecies.save(flush:true)){
				newSpecies.errors.allErrors.each { log.error it }
			}
			
			//deleting speices
			oldSpecies.deleteSpecies(null)
		}
	}
	
	
	private moveRecoAndObv(oldName, newName){
		def newReco = Recommendation.findByTaxonConcept(newName)
		def reco = Recommendation.findByTaxonConcept(oldName)
		if(reco){
			RecommendationVote.findAllByRecommendationOrCommonNameReco(reco, reco).each { r ->
				println " saving reco vote  " + r
				if(r.recommendation == reco){
					r.recommendation = newReco
				}
				if(r.commonNameReco == reco){
					r.commonNameReco = newReco
				}
				if(!r.save(flush:true)){
					r.errors.allErrors.each { log.error it }
				}
			}
			Observation.findAllByMaxVotedReco(reco).each { obv ->
				obv.maxVotedReco = newReco
				if(!obv.save(flush:true)){
					obv.errors.allErrors.each { log.error it }
				}
				
			}
			println "========= deleting reco " + reco
			reco.delete(flush:true)
		}
	}
	
	private moveOtherNamesReference(oldName, newName){
		//moving synonym
		oldName.fetchSynonyms().each {
			log.debug "================ synonym move " + it
			oldName.removeSynonym(it)
			if(newName) newName.addSynonym(it)
			
		}
		
		//moving common names
		List cns = CommonNames.findAllByTaxonConcept(oldName)
		cns.each { CommonNames cn ->
			log.debug "================ common name update " + cn
			if(newName)
				cn.taxonConcept = newName
			else
				cn.isDeleted = true
				
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
		
		//moving docsciname
		List dcs = DocSciName.findAllByTaxonConcept(oldName)
		dcs.each {DocSciName  cn ->
			log.debug "================ Doc sci name " + cn
			if(newName)
				cn.taxonConcept = newName
			else
				cn.isDeleted = true
				
			if(!cn.save(flush:true)){
				cn.errors.allErrors.each { log.error it }
			}else{
				cn.delete(flush:true)
			}
		}
	}
	
	//moving children and deleting ibp hir
	private moveTaxonReg(oldName, newName){
		Classification ibpClassification = Classification.fetchIBPClassification()
		TaxonomyRegistry tr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(oldName, ibpClassification)
		if(!tr){
			log.debug "Does not have ibp hir so not moving any thing " + oldName
			return
		}
		//updating new parent of all the children of old name
		if(newName){
			TaxonomyRegistry ntr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(newName, ibpClassification)
			TaxonomyRegistry.executeUpdate( "update TaxonomyRegistry set parentTaxon = :newParentTaxon, parentTaxonDefinition = :newParentTaxonDef where parentTaxon = :oldParentTaxon", [newParentTaxon:ntr, newParentTaxonDef :newName, oldParentTaxon:tr])
//			TaxonomyRegistry.findAllByParentTaxon(tr).each { TaxonomyRegistry t ->
//				t.parentTaxon = ntr
//				t.parentTaxonDefinition = newName
//				if(!t.save(flush:true)){
//					t.errors.allErrors.each { log.error it }
//			   }
//			}
		}
		
		//deleteing ibp hir of old name
		if(!tr.delete(flush:true)){
			tr.errors.allErrors.each { log.error it }
		}
		
		//moving all children to new ibp path
		if(newName){
			List sqlStrings = newName.fetchUpdateTaxonRegSql(oldName.id)
			Sql sql = new Sql(dataSource)
			sqlStrings.each { String s ->
			log.debug " Path update query " + s
				try{
					int updateCount = sql.executeUpdate(s);
					log.debug " updated path count  " + 	updateCount
				}catch(e){
					e.printStackTrace()
				}
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////


    def export(params, dl){
        if(!dl)
            return null

            File downloadDir = new File(grailsApplication.config.speciesPortal.namelist.downloadDir)
            if(!downloadDir.exists()){
                downloadDir.mkdirs()
            }
        log.debug "export type " + dl.type 
        if(dl.type == DownloadLog.DownloadType.CSV) {
            return exportAsCSV(downloadDir, dl, params.webaddress)
        } /*else if(dl.type == DownloadLog.DownloadType.KML) {
        return exportAsKML(downloadDir, dl, userGroupWebaddress)
        } else {
        return exportAsDW(downloadDir, dl, userGroupWebaddress)
        }*/

    }

    private File exportAsCSV(File downloadDir, DownloadLog dl, String userGroupWebaddress){
        String folderName = "taxon_"+ + new Date().getTime()
        String parent_dir = downloadDir.getAbsolutePath() + File.separator + folderName+File.separator + folderName
        File csvFile = new File(parent_dir, "taxon_" + new Date().getTime() + ".csv")

        def writer = utilsService.getCSVWriter(csvFile.getParent(), csvFile.getName())

        boolean headerAdded = false
        ResourceFetcher rf = new ResourceFetcher(TaxonomyDefinition.class.canonicalName, dl, userGroupWebaddress, dl.offsetParam.intValue());
        try {
            int total = 0;
            while (total < EXPORT_BATCH_SIZE && rf.hasNext()) {
                def obvList = rf.next();
                total += obvList.size();
                obvList.each { obv ->
                    log.debug "Writting " + obv
                    Map m = obv//.fetchExportableValue(dl.author)
                    if(!headerAdded){
                        def header = []
                        for(entry in m){
                            header.add(entry.getKey())
                        }
                        TaxonomyRank.list().each {
                            header.add(it.value());
                        }
                        writer.writeNext(header.toArray(new String[0]))
                        headerAdded = true
                    }
                    String[] array = new String[m.values().size()+TaxonomyRank.list().size()];
                    int index = 0;
                    int taxonIndex = m.values().size();
                    for (entry in m) {
                        if(entry.getKey().equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY) && entry.getValue()) {
                            JSON.parse(entry.getValue()). each {
                                array[taxonIndex + it.rank] = it.name;
                            }
                        } else {
                            array[index] = getExportableValue(entry.getKey(), (String) entry.getValue());
                        }
                        index++;
                    }
                    
                    writer.writeNext(array)
                }
            }
        } catch(e) {
            e.printStackTrace();
        }
        writer.flush()
        writer.close()

        return csvFile; //archive(downloadDir.getAbsolutePath(), folderName, csvFile, f )
        //return csvFile
    }

}
