package species.participation

import grails.converters.JSON
import species.groups.SpeciesGroup;

class ChecklistController {

	def activityFeedService;
	def springSecurityService;

	def index = {
		redirect(action:list, params: params)
	}

	def list = {
		log.debug params
		def model = getFilteredChecklist(params)
		[checklistInstanceList:model.checklistInstanceList, checklistMapInstanceList:model.checklistMapInstanceList, checklistInstanceTotal:model.checklistInstanceTotal, 'userGroupWebaddress':params.webaddress]
	}


	def show = {
		log.debug params
		[checklistInstance:Checklist.read(params.id.toLong())]
	}

	def snippet = {
		log.debug params
		def checklistInstance = Checklist.read(params.id)
		render (template:"/common/checklist/showChecklistSnippetTabletTemplate", model:[checklistInstance:checklistInstance, 'userGroupWebaddress':params.webaddress]);
	}

	private getFilteredChecklist(params){
		def allGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL);
		def speciesGroup = params.sGroup ? SpeciesGroup.get(params.sGroup.toLong()) : allGroup
		def max = Math.min(params.max ? params.int('max') : 15, 100);
		def offset = params.offset ? params.int('offset') : 0
		if(speciesGroup == allGroup){
			return [checklistInstanceList:Checklist.list([max:max, offset:offset]), checklistInstanceTotal:Checklist.count(), checklistMapInstanceList:Checklist.list()]
		}else{
			return [checklistInstanceList:Checklist.findAllBySpeciesGroup(speciesGroup, [max:max, offset:offset]), checklistInstanceTotal:Checklist.countBySpeciesGroup(speciesGroup), checklistMapInstanceList:Checklist.findAllBySpeciesGroup(speciesGroup)]
		}
	}

	
	/*
	 * 
	 def filteredMapBasedChecklistList = {
	 log.debug params
	 def model = getFilteredChecklist(params)
	 def mapViewHtml = g.render(template:"/common/checklist/showChecklistMultipleLocationTemplate", model:[checklistInstanceList:model.checklistMapInstanceList]);
	 def result = [mapViewHtml:mapViewHtml]
	 render result as JSON
	 }
	 def filter ={
	 log.debug "====================" +  params
	 params.max = Math.min(params.max ? params.int('max') : 5, 100)
	 render(template:"/common/checklist/showFilteredChecklistTemplate" ,model:[ checklistInstanceList: Checklist.list(params), checklistInstanceTotal:Checklist.count() ])
	 }
	 */
}
