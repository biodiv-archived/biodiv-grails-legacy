package utils

import grails.converters.JSON
import grails.plugin.springsecurity.Secured

class ChartController {

	def chartService
	
    def index = { 
		redirect (action:show, params:params)
	}
	
	@Secured(['ROLE_ADMIN'])
	def test = {
		//render chartService.getObservationStats(params, null) as JSON
		//render chartService.getSpeciesPageStats(params) as JSON
		//render chartService.getPortalActivityStatsByDay(params) as JSON
		[]
	}
	//@Secured(['ROLE_ADMIN'])
	def show = {
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
}
