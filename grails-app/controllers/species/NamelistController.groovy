package species

import grails.converters.JSON;
import grails.converters.XML;
import species.TaxonomyRegistry;
import species.Classification;
import species.TaxonomyDefinition;
import grails.plugin.springsecurity.annotation.Secured;
import species.NamesParser;
import species.sourcehandler.XMLConverter;
import species.participation.NamelistService
import species.participation.Recommendation;
import species.participation.Observation;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.NamePosition;
import content.eml.Document;
import species.Species;

class NamelistController {
    
    def index() { }

    def namelistService
    def utilsService
    def springSecurityService;
    def speciesService;
    def observationService;
    def documentService;
    def speciesPermissionService;

	/**
	 * input : taxon id ,classification id of ibp 
	 * @return A map which contain keys as dirty, clean and working list. Values of this key is again a LIST of maps with key as name and id
	 * 
	 */
	def getNamesFromTaxon(){
        //input in params.taxonId
        def res = namelistService.getNamesFromTaxon(params)
		//def res = [dirtyList:[[name:'aa', id:11, classificationId:params.classificationId], [name:'bb', id:29585, classificationId:params.classificationId]], workingList:[[name:'aa', id:11, classificationId:params.classificationId], [name:'bb', id:22, classificationId:params.classificationId]]]
        def result;
        if(res) {
            res['isAdmin'] = utilsService.isAdmin();
            result = [success:true, model:res]
        }
        else result = [success:false, msg:'Error while fetching names']
        withFormat {
            json { render result as JSON }
            xml { render result as XML } 
        }
	}
	
	/**
	 * input : taxon id, classification id of ibp
	 * @return All detail like kingdom, order etc
	 */
	def getNameDetails(){
        //input in params.taxonId
		//[name:'aa', kingdom:'kk', .....]
		def userLanguage = utilsService.getCurrentLanguage(request);   
        def instance;

        def res = [:];
        try {
            if(params.nameType?.equalsIgnoreCase(NameStatus.ACCEPTED.value())) {
                instance = TaxonomyDefinition.read(params.taxonId.toLong())
            } else if(params.nameType?.equalsIgnoreCase(NameStatus.SYNONYM.value())) {
                instance = SynonymsMerged.read(params.taxonId.toLong())
            } else if(params.nameType?.equalsIgnoreCase(NameStatus.COMMON.value())) {
                //TODO
            }


            if(instance) {
                //def feedCommentHtml = g.render(template:"/common/feedCommentTemplate", model:[instance: instance, userLanguage:userLanguage]);
                res = namelistService.getNameDetails(params);
                res['success'] = true;
				res['rootHolderType'] = instance.class.canonicalName
				res['rootHolderId'] = instance.id
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
                //res['feedCommentHtml'] = raw(feedCommentHtml.replaceAll('"',"'"))


                Species.withNewTransaction {
                    //speciesModel will be  [speciesInstanceList: speciesInstanceList, instanceTotal: count, speciesCountWithContent:speciesCountWithContent, 'userGroupWebaddress':params.webaddress, queryParams: queryParams]
                    def speciesModel = speciesService.getSpeciesList([taxon:instance.id+"", max:1], 'list');
                    res['speciesInstanceTotal'] = speciesModel.instanceTotal;
                }

                Observation.withNewTransaction {
                    //observationModel will be [observationInstanceList:observationInstanceList, allObservationCount:allObservationCount, checklistCount:checklistCount, speciesGroupCountList:speciesGroupCountList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
                    def observationModel = observationService.getFilteredObservations([taxon:instance.id+""], 1, 0, false);
                    res['observationInstanceTotal'] = observationModel.allObservationCount;
                    res['checklistInstanceTotal'] = observationModel.checklistCount;
                }

                Document.withNewTransaction {
                    //documentModel will be  [documentInstanceList:documentInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
                    def documentModel = documentService.getFilteredDocuments([taxon:instance.id+""], 1, 0);
                    res['documentInstanceTotal'] = documentModel.instanceTotal;
                }
            } else {
                res['success'] = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            res['success'] = false;
            res['msg'] = e.getMessage();
        }
        render res as JSON
        return;
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
        //println "========RES DETAILS====== " + res[0]  
        //Sending 0th index as only one result as its queried on id
        def finRes = [:];
        if(res) {
            finRes = res[0]
        }
        render finRes as JSON
    }
    
    def searchIBP() {
        List<String> givenNames = [params.name]
        NamesParser namesParser = new NamesParser();
        List<TaxonomyDefinition> parsedNames = namesParser.parse(givenNames);
        println "===========PARSED NAMES========= " + parsedNames[0].canonicalForm
        params.name = parsedNames[0].canonicalForm
        List res = []
        res = speciesService.searchIBP(params.name); 
        println "========RES ======= " + res
        render res as JSON
    }
  
    @Secured(['ROLE_USER'])
    def curateName() {
        def res = [:]
        try{
            /*if(!utilsService.isAdmin(springSecurityService.currentUser?.id)) {
                res['msg'] = "This action is temporarily disabled while feedback on the curation interface is being collected . Please leave comment below with details of correction required so that admins can implement it."
                render res as JSON
                return;
            }*/

            if(!params.acceptedMatch) {
                res['msg'] = "No acceptedMatch"
                render res as JSON
                return;
            }

            def acceptedMatch = JSON.parse(params.acceptedMatch);

            if(!acceptedMatch.taxonId) {
                res['msg'] = "No name selected"
                render res as JSON
                return;
            }


            acceptedMatch.parsedRank =  XMLConverter.getTaxonRank(acceptedMatch.rank);
            def name;
            if(acceptedMatch.isOrphanName == "true"){
                name = Recommendation.get(acceptedMatch.recoId?.toLong());
            } else {
                name = TaxonomyDefinition.get(acceptedMatch.taxonId.toLong());
            }

            if(!name) {
                res['msg'] = "Not a valid name."
                render res as JSON
                return;
            }


            boolean moveToRaw = acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.RAW.toString()):false;
            boolean moveToWKG =  acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.WORKING.toString()):false;
            boolean moveToClean =  acceptedMatch.position?acceptedMatch.position.equalsIgnoreCase(NamePosition.CLEAN.toString()):false;

            if((moveToWKG || moveToRaw) && !(speciesPermissionService.isTaxonContributor(name, springSecurityService.currentUser, [SpeciesPermission.PermissionType.ROLE_TAXON_CURATOR, SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR]) || utilsService.isAdmin(springSecurityService.currentUser))
            ) {
                def link = """<a href="${uGroup.createLink(controller:'species', action:'taxonBrowser')}" class="btn btn-primary" target="_blank">${message(code:"link.request")} </a>"""
                res['msg'] = "Only user with Taxon curator/editor permission can edit raw names and move to working list. Please request for permission ${link}"
                render res as JSON
                return;
            }

            //TODO:remove from clean shd be one by user with editor rights only
            if(moveToClean && !(speciesPermissionService.isTaxonContributor(name, springSecurityService.currentUser, [SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR])  || utilsService.isAdmin(springSecurityService.currentUser) )
            ) {
                def link = """<a href="${uGroup.createLink(controller:'species', action:'taxonBrowser')}" class="btn btn-primary" target="_blank">${message(code:"link.request")} </a>"""
                res['msg'] = "Only user with Taxon editor permission can edit working names and move to clean list. Please request for permission ${link}"
                render res as JSON
                return;
            }

            if(moveToClean && name.position != NamesMetadata.NamePosition.WORKING) {
                res['msg'] = "Only names in working list can be moved to clean list."
                render res as JSON
                return;

            }

            if(acceptedMatch.isOrphanName == "true"){
                Recommendation reco = name;
                res = namelistService.processRecoName(reco, acceptedMatch);
            } else {
                ScientificName sciName = name;
                res = namelistService.processDataFromUI(sciName, acceptedMatch)
            }
        } catch(Exception e) {
            e.printStackTrace();
            res = [success:false, msg:e.getMessage()] 
        }
        render res as JSON
    }

    def getOrphanRecoNames() {
        def res = namelistService.getOrphanRecoNames();
        render res as JSON
    }

    def saveAcceptedName() {
        def res = [:];
        if(params.acceptedMatch) {
            def acceptedMatch = JSON.parse(params.acceptedMatch);
            acceptedMatch.parsedRank =  XMLConverter.getTaxonRank(acceptedMatch.rank);
            ScientificName sciName = null;
            sciName = namelistService.saveAcceptedName(acceptedMatch);   
            println "SCIEN NAME =========== " + sciName
            res['acceptedNameId'] = sciName?sciName.id:"";
        }
        render res as JSON;
    }
	
	
	/////////////////////////////// Name list API /////////////////////////////////
	@Secured(['ROLE_ADMIN'])
	def changeAccToSyn(params){
		log.debug params
		def res = [:]
		res.status = namelistService.changeAccToSyn(params.sourceAcceptedId.toLong(), params.targetAcceptedId.toLong())
		render  res as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def changeSynToAcc(params){
		log.debug params
		def res = [:]
		res.status = namelistService.changeSynToAcc(params.oldId.toLong(), null)
		render  res as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def deleteName(params){
		log.debug params
		def res = [:]
		boolean isParent = TaxonomyDefinition.read(params.id.toLong()).isParent()
        if(isParent){
          res.msg = "Taxon name has children "
          res.success = true
          render  res as JSON;  
          return
        }else{
		  res.status = namelistService.deleteName(params.id.toLong())
		  render  res as JSON;
        }
	}
	
	@Secured(['ROLE_ADMIN'])
	def mergeNames(params){
		log.debug params
		def res = [:]
		res.status = namelistService.mergeNames(params.sourceId.toLong(), params.targetId.toLong())
		render  res as JSON;
	}

	@Secured(['ROLE_ADMIN'])
	def updatePosition(params){
		log.debug params
		def res = [:]
		res.status = namelistService.updateNamePosition(params.id.toLong(), params.position, params.hirMap)
		render  res as JSON;
	}

}
