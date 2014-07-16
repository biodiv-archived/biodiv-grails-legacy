package utils

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

class ChartController {
    
    static defaultAction = "show"
       
       def chartService
       
    def index(){ 
               redirect (params:params)
       }
	
	@Secured(['ROLE_ADMIN'])
	def test() {
		//render chartService.getObservationStats(params, null) as JSON
		//render chartService.getSpeciesPageStats(params) as JSON
		//render chartService.getPortalActivityStatsByDay(params) as JSON
		[]
	}
	//@Secured(['ROLE_ADMIN'])
	def show() {
		log.debug params
		[obvData:chartService.getObservationStats(params, null, request), speciesData: chartService.getSpeciesPageStats(params, request), userData:chartService.activeUserStats(params, request),  activityData:chartService.getPortalActivityStatsByDay(params)]
	}
	
	def homePageStat = {
		log.debug params
		[obvData:chartService.getObservationStats(params, null, request), speciesData: chartService.getSpeciesPageStats(params, request), userData:chartService.activeUserStats(params, request),  activityData:chartService.getPortalActivityStatsByDay(params), combineData:chartService.combineStats(params, request) ]
		//render(template:"/chart/homePageStatTemplate", model:[activityData:chartService.getPortalActivityStatsByDay(params)])
		//[obvData:chartService.getObservationStats(params, null), speciesData: chartService.getSpeciesPageStats(params), userData:chartService.activeUserStats(params),  activityData:chartService.getPortalActivityStatsByDay(params)]
	}
	
	def smallStat = {
		log.debug params
		params.days = "" + 30
		render template:"/chart/homePageStatTemplate", model:[activityData:chartService.getPortalActivityStatsByDay(params)]
	}

	/**
	* Will return basic dynamic stats i.e. observationCount, speciesCount
	*/
	def basicStat = {
		log.debug params
		def retMap = [observationCount:chartService.getObservationCount(params), speciesCount:chartService.getSpeciesCount(params), checklistsCount:chartService.getChecklistCount(params), documentCount:chartService.getDocumentCount(params), userCount:chartService.getUserCount(params)]
		render retMap as JSON
	}  

    def topContributors = {        
	    render chartService.activeUserStats(params, request) as JSON
    }
}
