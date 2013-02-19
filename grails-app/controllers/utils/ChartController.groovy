package utils

import grails.converters.JSON
import grails.plugins.springsecurity.Secured

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
	@Secured(['ROLE_ADMIN'])
	def show = {
		log.debug params
		[obvData:chartService.getObservationStats(params, null), speciesData: chartService.getSpeciesPageStats(params), userData:chartService.activeUserStats(params),  activityData:chartService.getPortalActivityStatsByDay(params)]
	}
}
