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
import species.utils.ImageType


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
    def activityFeedService;
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
    @Secured(['ROLE_USER'])
    def changeAccToSyn(params){
        log.debug params
        def res = ['msg':'']
		def sourceAcceptedIds = params?.sourceAcceptedId.split(',');
		def user = springSecurityService.currentUser
        sourceAcceptedIds.each{ sourceAcceptedId ->
			def m = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":sourceAcceptedId + "," + params.targetAcceptedId, "moveToClean":'false']))
			if(namePermissionService.hasPermissionOnAll(m)){
				res.status = namelistService.changeAccToSyn(sourceAcceptedId.toLong(), params.targetAcceptedId.toLong())
			}else{
				res.msg += "\n You do not have permission to change accepted name: " + TaxonomyDefinition.read(sourceAcceptedId.toLong())?.name + "(" + sourceAcceptedId + ") to Synonym of: " + TaxonomyDefinition.read(params.targetAcceptedId.toLong())?.name + "(" + params.targetAcceptedId + ")" 
			}
        }
        render  res as JSON;
    }

    @Secured(['ROLE_USER'])
    def changeSynToAcc(params){
        log.debug params
		def res = ['msg':'']
		def user = springSecurityService.currentUser
		boolean hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":params.oldId, "moveToClean":'false']))
		if(hasPerm){
			res.status = namelistService.changeSynToAcc(params.oldId.toLong(), null)
		}else{
			res.status = false
			res.msg = "\n You do not have permission to change Synonym: " + TaxonomyDefinition.read(params.oldId.toLong())?.name + "(" + params.oldId + ") to Accepted name"
		}
        render  res as JSON;
    }

    @Secured(['ROLE_USER'])
    def deleteName(params){
        log.debug params
		def res = ['msg':'']
		def user = springSecurityService.currentUser
        def delIds = params?.ids.split(',');
        delIds.each{ id ->
			boolean hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":id, "moveToClean":'false']))
			if(!hasPerm){
                res.msg += "\n You do not have permission to delete name: " + TaxonomyDefinition.read(id.toLong())?.name + "(" + id + ")"
                res.status = false
				return
			}
            boolean isParent = TaxonomyDefinition.read(id.toLong()).isParent()
			if(isParent){
              res.msg += "\n Name id: " + TaxonomyDefinition.read(id.toLong())?.name + "(" + id + ")" + " has child taxa. Please delete child first"
            }else{
              res.status = namelistService.deleteName(id.toLong())
              res.msg += "\n Name id: " + TaxonomyDefinition.read(id.toLong())?.name + "(" + id + ")" + " deleted"
            }
        }
        render res as JSON; 
    }

    @Secured(['ROLE_USER'])
    def mergeNames(params){
        log.debug params
        def res = ['msg':'']
		def user = springSecurityService.currentUser
		def m = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":params.sourceId + "," + params.targetId, "moveToClean":'false']))
		if(namePermissionService.hasPermissionOnAll(m)){
			res.status = namelistService.mergeNames(params.sourceId.toLong(), params.targetId.toLong())
            res.msg = "Successfully merged"
		}else{
			res.status = false
			res.msg = "\n You do not have permission to merge name: " + TaxonomyDefinition.read(params.sourceId.toLong())?.name + "(" + params.sourceId + ") to target name: " +  TaxonomyDefinition.read(params.targetId.toLong())?.name + "(" + params.targetId + ")"
		}
        render  res as JSON;
    }

    @Secured(['ROLE_USER'])
    def updatePosition(params){
        log.debug params
		def res = ['msg':'']
		def user = springSecurityService.currentUser
		def ids = params?.ids.split(',');
		boolean moveToClean = (NamesMetadata.NamePosition.getEnum(params.position) == NamesMetadata.NamePosition.CLEAN)
        ids.each{ id ->
			boolean hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":id, "moveToClean":'' + moveToClean]))
			if(hasPerm){
				res.msg += "\n Position for name: " + TaxonomyDefinition.read(id.toLong())?.name + "(" + id + ")"+id+ " Updated"
				res.status = namelistService.updateNamePosition(id.toLong(), params.position, params.hirMap)
			}else{
				res.msg += "\n You do not have permission to update position for name: " + TaxonomyDefinition.read(id.toLong())?.name + "(" + id + ")"
			}
        }
        render  res as JSON;
    }

	@Secured(['ROLE_USER'])
    def singleNameUpdate(){
        log.debug params;
        List errors = [];
		def user = springSecurityService.currentUser
        Language languageInstance = utilsService.getCurrentLanguage(request);
        Map result = [success: true,msg: "",userLanguage:languageInstance, errors:errors];  
		boolean hasPerm = false
        if(params.int('taxonId')){
            TaxonomyDefinition td = TaxonomyDefinition.get(params.int('taxonId'));
            if(td){
	            // Editing Species Name only
                def chkStatus =false;
                def activityMsg = '';
				hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":'' + td.id, "moveToClean":'false']))
				if((td.name != params.page) && hasPerm ){
	                 NamesParser namesParser = new NamesParser();
	                 TaxonomyDefinition pn = new NamesParser().parse([params.page])?.get(0);
                     if(pn){
                        activityMsg +='Taxon name updated : '+td.name 
                        Map m = [name:pn.name, canonicalForm:pn.canonicalForm, normalizedForm:pn.normalizedForm, italicisedForm:pn.normalizedForm,  binomialForm:pn.binomialForm, authorYear:pn.authorYear, id:td.id]
                        TaxonomyDefinition.executeUpdate( "update TaxonomyDefinition set name = :name, canonicalForm = :canonicalForm, normalizedForm = :normalizedForm,  italicisedForm = :italicisedForm, binomialForm = :binomialForm, authorYear = :authorYear where id = :id", m)
                        println "Taxon Updated successFully !";
                        chkStatus = true;
                        activityMsg +=' to '+pn.name;
                        result['msg'] +="\n Name taxon updated";
                        Species sp = Species.findByTaxonConcept(td);
                        if(sp){
                            sp.title = td.italicisedForm;
                            if(sp.save(flush:true)){
                                println "Species Title Updated successFully "+ sp;
                                result['msg'] +="\n Name updated"
                            }
                        }
                    }    
	            }else{
	                log.debug "No change in Names"
					if(!hasPerm)
	                	result['msg'] += '\n You do not have permission to change the name attributes for ' + td.name + "(" + td.id + ")";
					else
						result['msg'] += '\n No change in name' + td.name + "(" + td.id + ")";
	            }          
	
	            // Changing position
				def tmpPos = NamesMetadata.NamePosition.getEnum(params.position)
				boolean moveToClean = (tmpPos == NamesMetadata.NamePosition.CLEAN)
				hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":'' + td.id, "moveToClean":''+moveToClean]))
				if(params.position && (td.position != tmpPos) && hasPerm){                                          
	                  println "Prev postion changing from "+td.position+" to "+params.position.toUpperCase()
                      activityMsg +='| Taxon position updated : '+td.position +" to "+ params.position.toUpperCase()
                      chkStatus = true;
	                  def r = namelistService.updateNamePosition(params.taxonId.toLong(), params.position, params.hirMap)
	                  result['msg'] +="\n Position changed to "+params.position
	            }else{
					if(!hasPerm)
						result['msg'] += '\n You do not have permission to change the position for ' + td.name + "(" + td.id + ")"  + ' to ' + params.position
					else
	                	result['msg'] +="\n No change in position"
						
	                log.debug "No change in current position ="+td.position+" params position"+params.position.toUpperCase();
	            }                
	        
	            //Changing status
				if(params.status && (td.status != NamesMetadata.NameStatus.getEnum(params.status))){  
					log.debug "Prev status changing from "+td.status+" to "+params.status.toUpperCase()
					hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":'' + td.id, "moveToClean":'false']))
					if(hasPerm && (params.status.capitalize() == NameStatus.ACCEPTED.value())){                        
						println "needed hir updates"
                        // Needed hir check
						def r = namelistService.changeSynToAcc(params.taxonId.toLong(), null);
                        activityMsg +='| Taxon status updated : '+td.status.capitalize() +" to "+ params.status.capitalize()
                        chkStatus = true;
                        result['msg'] +="\n Status changed to "+params.status                       
					}else if(hasPerm && (params.status.capitalize() == NameStatus.SYNONYM.value())){
                         println "Prev status changing from "+td.id+" to "+params.status
                         if(params.newRecoId.toLong()){
							 def reco = Recommendation.read(params.newRecoId.toLong());
							 if(reco){
								 def r = namelistService.changeAccToSyn(td.id, reco.taxonConcept.id);
                                 activityMsg +='| Taxon status updated : '+td.status.capitalize() +" to "+ params.status.capitalize()
                                 chkStatus = true;
								 println "Accepted to synonym success";
								 result['msg'] +="\n Status changed to "+params.status
							 }else{
							 	println "newRecoId is null After reading";  
							 }
						 }else{
                            println "newRecoId is null while";
                         }
                    }else if(!hasPerm){
						result['msg'] += '\n You do not have permission to change the status of name ' + td.name + "(" + td.id + ")";
					}                
	            }else{
						result['msg'] +="\n No change in status"
						println "No Change in Current status ="+td.status+" params status"+params.status
				}
	
	           // hir Change
				hasPerm = namePermissionService.hasPermission(namePermissionService.populateMap(["user":'' + user.id, "taxon":'' + td.id, "moveToClean":'false']))
				if(hasPerm && (params.newPath)){
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
	                params.taxonHirMatch[params.rank+'.ibpId'] = params.taxonId;
	
	                def result1 = speciesService.createName(speciesName,rank,hirNameList,null,language,params.taxonId.toLong(), params.taxonHirMatch);
	                if(result1.success){
                        activityMsg +='| Taxon Hierarchy updated : '
                        chkStatus = true;
	                    result['msg'] += "\n "+result1['msg'];
                        result['msg'] += "\n \n Note: Changes will not reflect in IBP Taxonomy Hierarchy unless executed by an Admin. \n Please contact admin at support@indiabiodiversity.org with details"
	                }else{
	                    result['msg'] += "\n "+result1['msg'];
	                }
	            }

                if(chkStatus){
                    def domainObject = activityFeedService.getDomainObject('species.TaxonomyDefinition', td.id)
                    println "========================"
                    domainObject
                    activityFeedService.addActivityFeed(domainObject, td, user, activityFeedService.TAXON_NAME_UPDATED,activityMsg)
        
                }
	            
		        withFormat {
		            html { }
		            json { render result as JSON }
		            xml { render result as XML }
		        } 
			} //if ends td
		} // if ends taxonId            
	}

//    def test(){
//        println speciesService.updateHierarchy('1_2_3_28_627_628',629,6);
//        println "aaa"
//       render "Its Working"
//    }

	
	////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Names Permission///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	@Secured(['ROLE_ADMIN'])
	def addPermission(params){
		log.debug params
		def res = [:]
        def taxonIds = (params.selectedNodes)?params.selectedNodes.split(','):[];
        def userIds  = (params.userIds)?params.userIds.split(','):[];
        if(taxonIds.length > 0 && userIds.length > 0 && params.invitetype != ''){
            taxonIds.each{ taxon -> 
                userIds.each{ userId ->
                    Map m = [user: userId,taxon: taxon,permission:params.invitetype.toString()];
    		      res.statusComplete = namePermissionService.addPermission(namePermissionService.populateMap(m))
                  res.msg ="Successfully added!"
                }
            }
        }else{
            res.statusComplete = false;
            res.msg = 'User or Taxon cannot be null'
        }
		render  res as JSON;
	}

	@Secured(['ROLE_ADMIN'])
	def removePermission(params){
		log.debug params
		def res = [:]
		res.status = namePermissionService.removePermission(namePermissionService.populateMap(params))
		render  res as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def tt(){
        //2998_33364_33366_3035_3542_5273_5275
		/*
		Map m = [user:"1" ,permission:"ADMIN"];
		namePermissionService.addPermission(namePermissionService.populateMap(m)) */
        def td = TaxonomyDefinition.get(126L);
        def user = springSecurityService.currentUser
        def domainObject = activityFeedService.getDomainObject('species.TaxonomyDefinition', td.id)
        activityFeedService.addActivityFeed(domainObject, td, user, activityFeedService.TAXON_NAME_UPDATED,"This is test description")
		
//		m = [user:"1426" ,permission:"ADMIN"];
//		namePermissionService.addPermission(namePermissionService.populateMap(m))
//		
//		m = [user:"1117" ,permission:"ADMIN"];
//		namePermissionService.addPermission(namePermissionService.populateMap(m))
//

//		
//        def getAllPermissions= namePermissionService.getAllPermissions([taxon:393]);
//        def users=[]
//            getAllPermissions.each { nP ->
//                println nP.user
//                users << [id:nP.user.id,profile_pic:nP.user.profilePicture(ImageType.SMALL)];
//            }
//            
        render "Working  "


	}
}
