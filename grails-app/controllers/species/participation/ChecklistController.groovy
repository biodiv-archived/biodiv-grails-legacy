package species.participation

import grails.converters.JSON
import species.groups.SpeciesGroup;
import grails.plugins.springsecurity.Secured;

class ChecklistController {
	
	def userGroupService;
	def activityFeedService;
	def springSecurityService;
	def checklistService;
	def grailsApplication
	def checklistUtilService
	
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
				checklistInstance = Checklists.findByIdAndIsDeleted(params.id.toLong(), false, [fetch: [observations: 'join']])
				checklistInstance.incrementPageVisit()
				def userGroupInstance;
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
			render Checklists.count();
		}
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
	
}
