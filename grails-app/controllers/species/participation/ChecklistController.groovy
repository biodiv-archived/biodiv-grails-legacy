package species.participation

import grails.converters.JSON
import species.License;
import species.groups.SpeciesGroup;
import grails.plugins.springsecurity.Secured;
import species.Resource.ResourceType;

class ChecklistController {
	
	def userGroupService;
	def activityFeedService;
	def springSecurityService;
	def checklistService;
	def grailsApplication
	def checklistUtilService
	def observationService
	def SUserService
	
	def index = {
		redirect(action:list, params: params)
	}

	def list = {
		log.debug params
		params.isChecklistOnly = "" + true
		redirect(controller:'observation', action:list, params: params)
	}


	def show = {
		log.debug params
		if(params.id){
			def checklistInstance = Observation.findByIdAndIsDeleted(params.id.toLong(), false)
			if (!checklistInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'checklist.label', default: 'Checklist'), params.id])}"
				redirect (url:uGroup.createLink(action:'list', controller:"checklist", 'userGroupWebaddress':params.webaddress))
			}else{
				//if this instance is not checklists instance then redirecting to observation
				if(!checklistInstance.instanceOf(Checklists)){
					redirect(controller:'observation', action:show, params: params)
					return
				}
				//refetching checklist and  all observation in one query
				//checklistInstance = Checklists.findByIdAndIsDeleted(params.id.toLong(), false, [fetch: [observations: 'join']])
				checklistInstance.incrementPageVisit()
				def userGroupInstance;
				params.max = params.max?params.max.toInteger():50 
				if(params.webaddress) {
					userGroupInstance = userGroupService.get(params.webaddress);
				}
				if(params.pos) {
					int pos = params.int('pos');
					def obsController = new ObservationController()
					def prevNext = obsController.getPrevNextObservations(pos, params.webaddress);
					//def prevNext = getPrevNextChecklists(pos, params.webaddress);
					if(prevNext) {
						[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, prevObservationId:prevNext.prevObservationId, nextObservationId:prevNext.nextObservationId, lastListParams:prevNext.lastListParams]
					} else {
						[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress]
					}
				} else {
					[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress]
				}
			}
		}
	}

	def snippet = {
		log.debug params
		def checklistInstance = Checklists.read(params.id)
		render (template:"/common/checklist/showChecklistSnippetTabletTemplate", model:[checklistInstance:checklistInstance, 'userGroupWebaddress':params.webaddress]);
	}
	
	private getChecklistCount(speciesGroup, userGroupInstance){
		return Checklists.withCriteria(){
			projections {
				count('id')
			}
			and{
				eq('isDeleted', false)
				if(speciesGroup){
					eq('group', speciesGroup)
				}
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
		}[0]
	}
	
	def count = {
		log.debug params
		def userGroup
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			render getChecklistCount(null, userGroup)
		}else{
			render Checklists.countByIsDeleted(false);
		}
	}
	
	@Secured(['ROLE_USER'])
	def create = {
		log.debug params
		def checklistInstance = new Checklists(license:License.findByName(License.LicenseType.CC_BY))
		checklistInstance.properties = params;
		return [observationInstance: checklistInstance]
	}
	
	@Secured(['ROLE_USER'])
	def save = {
		log.debug params;
		if(request.method == 'POST') {
			def result = saveAndRender(params)
            if(result.success){
                redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:result.checklistInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
            }else{
                //flash.message = "${message(code: 'error')}";
                render(view: "create", model: [observationInstance: result.checklistInstance, checklistData:params.checklistData.encodeAsJSON(), checklistColumns:params.checklistColumns, sciNameColumn:params.sciNameColumn, commonNameColumn:params.commonNameColumn])
            }
		} else {
			redirect (url:uGroup.createLink(action:'create', controller:"checklist", 'userGroupWebaddress':params.webaddress))
		}
		
	}

	private saveAndRender(params, sendMail=true){
		updateParams(params)
		return checklistService.saveChecklist(params, sendMail=true)
	}
	
	private updateParams(params){
		params.checklistData = JSON.parse(params.checklistData)
		params.checklistColumns = JSON.parse(params.checklistColumns)
		List columnList = params.checklistColumns.collect { it.name }
		
		if(columnList.contains(ChecklistService.MEDIA_COLUMN)){
			columnList.remove(ChecklistService.MEDIA_COLUMN);
		}
		
		if(params.action == 'edit' ||params.action == 'update'){
			if(columnList.indexOf(ChecklistService.OBSERVATION_COLUMN) == 0){
				columnList.remove(0);
			}
		}
		
		//getting sn and cn column in front of checklist
		if(params.commonNameColumn){
			columnList.remove(params.commonNameColumn)
			columnList.add(0, params.commonNameColumn)
		}
		
		if(params.sciNameColumn){
			columnList.remove(params.sciNameColumn)
			columnList.add(0, params.sciNameColumn)
		}
		
		params.columnNames =  columnList.join("\t")
		params.columns =  columnList.collect { it.trim() }
		
		//params.sciNameColumn = params.sciNameColumn ?: "scientific_name"
		//params.commonNameColumn = params.commonNameColumn ?: "common_name"
		
		//params.group_id = "841" //params.group?:SpeciesGroup.get(params.group_id);
		//params.placeName = "honey valley " 
		//params.location_accuracy = params.locationAccuracy  = "Accurate" 
		
		//params.habitat_id = "267836";
		//params.agreeTerms = 'on'
		//params.latitude = "" + 23.314
		//params.longitude = "" + 77.74
		
		//params.rawChecklist =  "checklist raw file" //params.rawChecklist
		//params.title =  "cl title" 
		//params.refText =  "ref text " //params.refText
		//params.sourceText =  "source text " // params.sourceText
		
		//params.publicationDate =  null //params.publicationDate ? observationService.parseDate(params.publicationDate) : null
		//params.reservesValue =  null //params.reservesValue
	} 
	
	@Secured(['ROLE_USER'])
	def edit = {
        log.debug params;
		def observationInstance = Checklists.findByIdAndIsDeleted(params.id?.toLong(), false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
		} else if(SUserService.ifOwns(observationInstance.author)) {
            def checklist = getChecklistData(params.id.toLong());
			render(view: "create", model: [observationInstance: observationInstance, 'springSecurityService':springSecurityService, sciNameColumn:observationInstance.sciNameColumn, commonNameColumn:observationInstance.commonNameColumn])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:observationInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}
	
	@Secured(['ROLE_USER'])
	def update = {
		log.debug params;
		def observationInstance = Checklists.findByIdAndIsDeleted(params.id?.toLong(), false)
		if(observationInstance)	{
			def result = saveAndRender(params, false)
            if(result.success){
                redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:result.checklistInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
            }else{
                //flash.message = "${message(code: 'error')}";
                render(view: "create", model: [observationInstance: result.checklistInstance, checklistData:params.checklistData, checklistColumns:params.checklistColumns, sciNameColumn:params.sciNameColumn, commonNameColumn:params.commonNameColumn])
            }

		}else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
		}
	}

    private getChecklistData(Long id) {
        if(!id) return [];

		String obv_id = ChecklistService.OBSERVATION_COLUMN
		Checklists cl = Checklists.findByIdAndIsDeleted(id, false, [fetch: [observations: 'join']])
        if(cl) {
            def obvData = []
            cl.observations.each {Observation obv ->
                def tMap = [:]
                tMap[ChecklistService.OBSERVATION_COLUMN] = obv.id
                if(obv.resource) {
                    tMap[ChecklistService.MEDIA_COLUMN] = [obv.resource.size()];
                    Iterator iterator = obv.resource?.iterator();
                    int index = 0;
                    String obvDir;
                    while(iterator.hasNext()) {
                        def res = iterator.next();
                        def r = new HashMap()
                        r['file'] = res.fileName;
                        r['thumbnail'] = res.thumbnailUrl();
                        r['url'] = res.url;
                        r['license'] = res.licenses.collect { it.name }.join(',');
                        r['type'] = res.type.name();
                        r['rating'] = res.rating;
                        if(res.type != ResourceType.VIDEO) {
                            obvDir = res.fileName.split('/')[1];
                        }
                        tMap[ChecklistService.MEDIA_COLUMN][index] = r
                        index++
                    }
                    if(obvDir)
                        tMap['obvDir'] = obvDir;
                }
                obv.fetchChecklistAnnotation().each { ann ->
                    tMap[ann.key] = ann.value
                }
                obvData.add(tMap)
            }
            
            List columns = [obv_id]
            cl.fetchColumnNames().each { columns.add(it) }
            
            return [columns: columns, data :obvData, sciNameColumn:cl.sciNameColumn, commonNameColumn:cl.commonNameColumn]
        }
        return ['error':"Couldn't find checklist with this id"];
    }

	def getObservationGrid =  {
		log.debug params
		render getChecklistData(params.id?.toLong()) as JSON
	}
	
	@Secured(['ROLE_USER'])
	def flagDeleted = {
		def result = observationService.delete(params)
		flash.message = result.message
		redirect (url:result.url)
	}
	
	
	def observationData = {
		log.debug params
		def observations = checklistService.getObservationData(params.id, params)
		def model =[observations:observations, checklistInstance:Checklists.read(params.id.toLong())]
		render(template:"/common/checklist/showChecklistDataTemplate", model:model);
	}
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search = {
		log.debug params;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		def model = checklistService.search(params);
		
		model['isSearch'] = true;
		
		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/common/checklist/showChecklistListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
			return;
		} else {
			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/common/checklist/showChecklistListTemplate", model:model);
			model.resultType = "checklist"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
			
			render (result as JSON)
			return;
		}
	}

	/**
	 *
	 */
	def terms = {
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		
		List result = checklistService.nameTerms(params)

		render result.value as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def test = {
		checklistService.serializeClData()
		render " done  "
	}
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH END /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	/*
	private def getPrevNextChecklists(int pos, String userGroupWebaddress) {
		String listKey = "checklist_ids_list";
		String listParamsKey = "checklist_ids_list_params"
		
		if(userGroupWebaddress) {
			listKey = userGroupWebaddress + listKey;
			listParamsKey = userGroupWebaddress + listParamsKey;
		}
		def lastListParams = session[listParamsKey]?.clone();
		if(lastListParams) {
			if(!session[listKey]) {
				log.debug "Fetching checklist list as its not present in session "
				runLastListQuery(lastListParams);
			}
			long noOfChecklist = session[listKey].size();
			log.debug "Current ids list in session ${session[listKey]} and position ${pos}";
			def nextId = (pos+1 < session[listKey].size()) ? session[listKey][pos+1] : null;
			if(nextId == null) {
				def max = Math.min(lastListParams.max ? lastListParams.int('max') : 50, 100)
				def offset = lastListParams.offset ? lastListParams.int('offset') : 0
				lastListParams.offset = offset + max;
				log.debug "Fetching new list of checklist using params ${lastListParams}";
				runLastListQuery(lastListParams);
				lastListParams.offset = offset;
				nextId = (pos+1 < session[listKey].size()) ? session[listKey][pos+1] : null;
			}
			
			def prevId = pos > 0 ? session[listKey][pos-1] : null;
			lastListParams.remove('isGalleryUpdate');
			lastListParams.remove("append");
			lastListParams.remove("loadMore");
			lastListParams['max'] = noOfChecklist;
			lastListParams['offset'] = 0;
			return ['prevObservationId':prevId, 'nextObservationId':nextId, 'lastListParams':lastListParams];
		}
	}
	
	private void runLastListQuery(Map params) {
		log.debug params;
		getFilteredChecklist(params)
	}
	
	protected getFilteredChecklist(params){
		def allGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL);
		def speciesGroup = params.sGroup ? SpeciesGroup.get(params.sGroup.toLong()) : allGroup
		def max = Math.min(params.max ? params.int('max') : 12, 100);
		def offset = params.offset ? params.int('offset') : 0
		def userGroupInstance = userGroupService.get("" + params.webaddress);
		
		speciesGroup = (speciesGroup != allGroup) ? speciesGroup : null
		def checklistInstanceList = getChecklist(speciesGroup, userGroupInstance, max, offset)
		def checklistInstanceTotal = getChecklistCount(speciesGroup, userGroupInstance)
		def checklistMapInstanceList = getChecklist(speciesGroup, userGroupInstance, null, null)
		
		def queryParams = [sGroup : params.sGroup, max:max, offset:offset]
		
		def activeFilters = [sGroup : params.sGroup]
		//storing in session for prev <-> next checklist
		String webAddress = (userGroupInstance)? userGroupInstance.webaddress : ""
		session[webAddress + "checklist_ids_list_params"] = params.clone();
		session[webAddress + "checklist_ids_list"] = checklistInstanceList.collect {it.id};
		return [checklistInstanceList:checklistInstanceList, instanceTotal:checklistInstanceTotal, checklistMapInstanceList:checklistMapInstanceList, 'userGroupWebaddress':params.webaddress, activeFilters:activeFilters, queryParams:queryParams]
	}

	private getChecklist(speciesGroup, userGroupInstance, max, offset){
		return Checklists.withCriteria(){
			and{
				if(speciesGroup){
					eq('group', speciesGroup)
				}
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
			if(max){
				maxResults max
			}
			if(offset){
				firstResult offset
			}
			
			order 'id', 'desc'
		}
	}
	*/
	

	
//	def create = {
//		log.debug params;
//	}
	
	
//	@Secured(['ROLE_ADMIN'])
//	def breakChecklist = {
//		log.debug params
//		checklistUtilService.migrateObservationFromChecklist()
//		render "=== done "
//	}
	
	//	@Secured(['ROLE_ADMIN'])
	//	def correctCn = {
	//		checklistService.mCn()
	//		render "=== done "
	//	}
	//
	//	@Secured(['ROLE_ADMIN'])
	//	def migrateNewChecklist = {
	//		log.debug params
	//		checklistService.migrateNewChecklist(params)
	//		render "=== done "
	//	}
	//
		/*
		@Secured(['ROLE_USER'])
		def test = {
			userGroupService.migrateUserPermission()
			render "=== done "
		}
	
		@Secured(['ROLE_USER'])
		def addSpecialFounder = {
			userGroupService.addSpecialFounder()
			render "=== done "
		}
		*/

    /*
    def createWizardFlow = {
        main {
            on('createChecklist').to "createChecklist"
        }

        createChecklist {
			on('submit') {
                def result = saveAndRender(params)
                if(result.success) show()
                else createFlow()
            }
            on('show').to 'show'
            on('createFlow').to 'createChecklist'
        }
    }*/
}
