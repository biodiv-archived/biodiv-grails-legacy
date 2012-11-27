package species.participation

import grails.converters.JSON

class ChecklistController {

	def activityFeedService;
	def springSecurityService;
	
	def index = {
		redirect(action:list, params: params)
	}
	
	def list = {
		log.debug params
		[checklistList:Checklist.list(max:20)]
		//['feedType':params.feedType, 'feedCategory':params.feedCategory]
	}
	
	def show = {
		log.debug params
		[checklistInstance:Checklist.read(params.id.toLong())]
	}
}
