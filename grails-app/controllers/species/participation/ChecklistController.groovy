package species.participation

import grails.converters.JSON
import species.groups.SpeciesGroup;
import grails.plugins.springsecurity.Secured;


import species.groups.UserGroup
import species.auth.SUser
import org.springframework.security.acls.domain.BasePermission;


class ChecklistController {
	
	def userGroupService;
	def activityFeedService;
	def springSecurityService;
	def checklistService;
	
	def index = {
		redirect(action:list, params: params)
	}

	def list = {
		log.debug params
		def model = getFilteredChecklist(params)
		
		if(params.loadMore?.toBoolean()){
			render(template:"/common/checklist/showChecklistListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def checklistListHtml =  g.render(template:"/common/checklist/showChecklistListTemplate", model:model);
			def checklistMsgtHtml =  g.render(template:"/common/checklist/showChecklistMsgTemplate", model:model);
			def checklistMapHtml =  g.render(template:"/common/checklist/showChecklistMultipleLocationTemplate", model:model);
			
			def result = [obvListHtml:checklistListHtml, obvFilterMsgHtml:checklistMsgtHtml, mapViewHtml:checklistMapHtml]
			render result as JSON
			return;
		}
	}


	def show = {
		log.debug params
		if(params.id){
			def checklistInstance = Checklist.read(params.id.toLong())
			if (!checklistInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'checklist.label', default: 'Checklist'), params.id])}"
				redirect (url:uGroup.createLink(action:'list', controller:"checklist", 'userGroupWebaddress':params.webaddress))
			}
			else {
				def userGroupInstance;
				if(params.webaddress) {
					userGroupInstance = userGroupService.get(params.webaddress);
				}
				if(params.pos) {
					int pos = params.int('pos');
					def prevNext = getPrevNextChecklists(pos, params.webaddress);
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
		def checklistInstance = Checklist.read(params.id)
		render (template:"/common/checklist/showChecklistSnippetTabletTemplate", model:[checklistInstance:checklistInstance, 'userGroupWebaddress':params.webaddress]);
	}
	
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
		def max = Math.min(params.max ? params.int('max') : 50, 100);
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
		return Checklist.withCriteria(){
			and{
				if(speciesGroup){
					speciesGroups{
						eq('id', speciesGroup.id)
					}
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
			
			order 'fromDate', 'desc'
		}
	}
	
	private getChecklistCount(speciesGroup, userGroupInstance){
		return Checklist.withCriteria(){
			projections {
				count('id')
			}
			and{
				if(speciesGroup){
					speciesGroups{
						eq('id', speciesGroup.id)
					}
				}
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
		}[0]
	}
	

/*
		
	def test = {
		def perm = BasePermission.WRITE
		UserGroup wgpGroup = UserGroup.read(1)
		render " sandee " + wgpGroup.hasPermission(SUser.read(1188), perm) + " other " +  wgpGroup.hasPermission(SUser.read(716), perm)
	}
	
	
	def test1 ={
		def wgpUserDate = new Date(111, 7, 8)
		def aa = []
		SUser.findAllByDateCreatedGreaterThanEquals(wgpUserDate).each{ user ->
			aa.add(user.id)
		}
		render aa
	}

	
	def test = {
		def wgpGroup = UserGroup.read(2)
		wgpGroup.addMember(SUser.read(2963))
		render "======== done "
		
	}
	
	def addPermissionToWgp = {
		UserGroup wgpGroup = UserGroup.read(1)
		def wgpUserDate = new Date(111, 7, 8)
		
		SUser.findAllByDateCreatedGreaterThanEquals(wgpUserDate).each{ user ->
			if(wgpGroup.isMember(user) && !wgpGroup.isFounder(user)){
				wgpGroup.addMember(user)
				log.debug "added permission $user"
			}
		}
		wgpGroup.addFounder(SUser.read(797));
		log.debug "added founder"
		render "=== Done"
	}
	
*/
	/*
	@Secured(['ROLE_ADMIN'])
	def migrateTable = {
		checklistService.updateUncuratedVotesTable()
		render "=== Done"
	}
	
	@Secured(['ROLE_ADMIN'])
	def migrateChecklist = {
		checklistService.migrateChecklist()
		render "Done ==== " 
	}
	*/
}
