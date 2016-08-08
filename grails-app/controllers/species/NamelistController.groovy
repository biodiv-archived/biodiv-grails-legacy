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
import species.CommonNames;
import species.utils.Utils


import species.participation.NamePermission.Permission

class NamelistController {
    
    def index() { }

    def namelistService
    def utilsService
    def springSecurityService;
    def speciesService;
    def observationService;
    def documentService;
    def speciesPermissionService;
	def namePermissionService;

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
            /*if(params.nameType?.equalsIgnoreCase(NameStatus.ACCEPTED.value())) {
                instance = TaxonomyDefinition.read(params.taxonId.toLong())
            } else if(params.nameType?.equalsIgnoreCase(NameStatus.SYNONYM.value())) {
                instance = SynonymsMerged.read(params.taxonId.toLong())
            } else if(params.nameType?.equalsIgnoreCase(NameStatus.COMMON.value())) {
                //TODO
            }*/
            def td = TaxonomyDefinition.read(params.taxonId.toLong());
            if(td instanceof species.TaxonomyDefinition) {
                instance = td
            } else if(td instanceof species.SynonymsMerged) {                
                instance = SynonymsMerged.read(params.taxonId.toLong())
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
        def sourceAcceptedIds = params?.sourceAcceptedId.split(',');
        sourceAcceptedIds.each{ sourceAcceptedId ->
            res.status = namelistService.changeAccToSyn(sourceAcceptedId.toLong(), params.targetAcceptedId.toLong())
        }
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
        def delIds = params?.ids.split(',');
        println "==============delIds================"
        println delIds
        res.msg = ''
        delIds.each{ id ->
            boolean isParent = TaxonomyDefinition.read(id.toLong()).isParent()
            if(isParent){
              res.msg += "\n Taxon id " +id+ " has children "              
              //render  res as JSON;  
              //return
            }else{
              namelistService.deleteName(id.toLong())
              res.msg += "\n Taxon id " +id+ " deleted"
              res.status = true
              //render  res as JSON;
            }
        }
        render  res as JSON; 
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
        def ids = params?.ids.split(',');
        res.msg=''
        ids.each{ id ->
            namelistService.updateNamePosition(id.toLong(), params.position, params.hirMap)
            res.msg += "\n Position for Taxon " +id+ " Updated"
            res.status = true
	
        }
        render  res as JSON;
    }


    def singleNameUpdate(){

        println params;  
        List errors = [];
        Language languageInstance = utilsService.getCurrentLanguage(request);
        Map result = [success: true,msg: "",userLanguage:languageInstance, errors:errors];

        if(params.int('taxonId')){
            TaxonomyDefinition td = TaxonomyDefinition.get(params.int('taxonId'));
            if(td){
                 boolean checkHir = true 
                 def pathGen
                // Validate Hierarchy 
            /*    if(params?.newPath){
                    Map list = params.taxonRegistry?:[:];
                    def pathGenArr = []                                                      
                    list.each { key, value ->
                        if(value && key < params.rank && checkHir){
                            def taxonDef = TaxonomyDefinition.findByName(value);
                            if(taxonDef){
                                pathGenArr.add(taxonDef.id);
                            }else{
                              checkHir = false
                              result['msg'] = "\n Not Available in Our Hierarchy "+value
                            }
                        }
                    }
                    pathGen = pathGenArr.join('_');                
                }
            println checkHir
            println pathGen
            if(!checkHir){
                result['success']=false;
                withFormat {
                    html { }
                    json { render result as JSON }
                    xml { render result as XML }
                } 
            } */


            // Editing Species Name only
            if(td.name != params.page){
                 List<String> givenNames = [params.page]
                 NamesParser namesParser = new NamesParser();
                 List<TaxonomyDefinition> parsedNames = namesParser.parse(givenNames);

                 if(parsedNames){
                    td.canonicalForm = parsedNames[0].canonicalForm;
                    td.normalizedForm = parsedNames[0].normalizedForm;
                    td.italicisedForm = parsedNames[0].italicisedForm;
                    td.binomialForm = parsedNames[0].binomialForm;
                    td.authorYear = parsedNames[0].authorYear;
                    td.name = parsedNames[0].name;
                        if(td.save(flush:true)){
                            println "Taxon Updated successFully !";
                            result['msg'] +="\n Name taxon updated"
                            Species sp = Species.findByTaxonConcept(td);
                            if(sp){
                                sp.title = td.italicisedForm;
                                if(sp.save(flush:true)){
                                    println "Species Title Updated successFully !"+sp;
                                    result['msg'] +="\n Name updated"
                                }
                            }
                        }
                 }
            }else{
                println "No change in Names"
                result['msg'] += '\n No change in Names';
            }

            // hir Change
           /* if(params?.newPath){
                if(checkHir){
                    def fieldsConfig = grailsApplication.config.speciesPortal.fields
                    def classification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);                  
                    if(speciesService.updateHierarchy(pathGen,params?.taxonId.toLong(),classification.id)){
                        result['msg'] += '\n Hierarchy updated';
                    }                
                }
            }*/

            // Changing position              
            if(params.position && td.position != NamesMetadata.NamePosition.getEnum(params.position)){                                          
                if(params.position.capitalize() == NamePosition.WORKING.value() || params.position.capitalize() == NamePosition.RAW.value() || params.position.capitalize() == NamePosition.CLEAN.value()){
                  println "Prev postion changing from "+td.position+" to "+params.position.toUpperCase()
                  def r = namelistService.updateNamePosition(params.taxonId.toLong(), params.position, params.hirMap)
                  result['msg'] +="\n Position changed to "+params.position
                }             
            }else{
                result['msg'] +="\n No change in position"
                println "No change in current position ="+td.position+" params position"+params.position.toUpperCase();
            }                
        
            //Changing status
            if(params.status && td.status != NamesMetadata.NameStatus.getEnum(params.status)){  
                println "Prev status changing from "+td.status+" to "+params.status.toUpperCase()
                   if(params.status.capitalize() == NameStatus.ACCEPTED.value()){                        
                        println "needed hir updates"
                        // Needed hir check
                        def r = namelistService.changeSynToAcc(params.taxonId.toLong(), null); 
                        result['msg'] +="\n Status changed to "+params.status                       
                    }else if(params.status.capitalize() == NameStatus.SYNONYM.value() ){
                         println "Prev status changing from "+td.id+" to "+params.status
                         if(params.newRecoId.toLong()){
                            def reco = Recommendation.read(params.newRecoId.toLong());
                            if(reco){
                                    def r = namelistService.changeAccToSyn(td.id, reco.taxonConcept.id);
                                    println "Accepted to synonym success";
                                    result['msg'] +="\n Status changed to "+params.status
                                }else{
                                    println "newRecoId is null After reading";  
                                }
                            
                          }else{
                            println "newRecoId is null while";
                          }
                    }                
                }else{
                    result['msg'] +="\n No change in status"
                    println "No Change in Current status ="+td.status+" params status"+params.status
                }

           // hir Change
           if(params?.newPath){
                Map list = params.taxonRegistry?:[:];
                List hirNameList = [];
                String speciesName;
                int rank;
                list.each { key, value -> 
                    if(value) {
                        hirNameList.putAt(Integer.parseInt(key).intValue(), value);
                     }
                } 

                speciesName = params.page
                rank = params.int('rank');
                hirNameList.putAt(rank, speciesName);
                println hirNameList;           
                def language = utilsService.getCurrentLanguage(request);
                def result1 = speciesService.createName(speciesName,rank,hirNameList,null,language,td);
                if(result1.success){
                    result['msg'] = "\n "+result1['msg'];
                }else{
                    result['msg'] = "\n "+result1['msg'];
                }
            }
            
        withFormat {
            html { }
            json { render result as JSON }
            xml { render result as XML }
        } 
     } //if ends td
    } // if ends taxonId            
}

    def test(){
        println speciesService.updateHierarchy('1_2_3_28_627_628',629,6);
        println "aaa"
       render "Its Working"
    }

	
	////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Names Permission///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	@Secured(['ROLE_ADMIN'])
	def addPermission(params){
		log.debug params
		def res = [:]
		res.status = namePermissionService.addPermission(params)
		render  res as JSON;
	}

	@Secured(['ROLE_ADMIN'])
	def removePermission(params){
		log.debug params
		def res = [:]
		res.status = namePermissionService.removePermission(params)
		render  res as JSON;
	}
	
	def tt(){
        //2998_33364_33366_3035_3542_5273_5275

		def p = Permission.getPermissionFromStr("ADMIN")
		def p1 = Permission.ADMIN
		
//        def td = TaxonomyDefinition.get(5275)
//        td.rank = 9
//        td.save(flush:true)
//        println "============= " + td.rank
        
		Map m = ['user':'1188', 'taxon':'5275', permission:'EDITOR', 'moveToClean' : 'false']
		
		def res = namePermissionService.hasPermission(m)
		//res = namePermissionService.addPermission(m)
        //namePermissionService.removePermission(['user':'1188','taxon':'3035'])
        //res = namePermissionService.getAllPermissions(m)
		render 'done  ' + res // as JSON
	}
}
