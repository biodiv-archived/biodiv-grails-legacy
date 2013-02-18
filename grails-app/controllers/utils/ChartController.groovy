package utils

import grails.converters.JSON

class ChartController {

	def chartService
	
    def index = { 
		redirect (action:show, params:params)
	}
	
	def test = {
		//render chartService.getObservationStats(params, null) as JSON
		render chartService.getSpeciesPageStats(params) as JSON
	}
	
	def show = {
		log.debug params
		[obvData:chartService.getObservationStats(params, null), speciesData: chartService.getSpeciesPageStats(params)]
	}
}
