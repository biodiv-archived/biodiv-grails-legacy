package species

class ChartTagLib {
	static namespace = "chart"
	
	private final static int Y_MAX = 500
	
	def chartService
	
	def showStats = {attrs, body->
		chartService.populateData(attrs.model)
		out << render(template:"/chart/genericStatTemplate", model:attrs.model);
	}
	
	def showActivityStats = {attrs, body->
		attrs.model.max = attrs.model.max ?:Y_MAX
		out << render(template:"/chart/activityStatTemplate", model:attrs.model);
	}
	
	def showHomePageStats = {attrs, body->
		attrs.model.max = attrs.model.max ?:Y_MAX
		out << render(template:"/chart/homePageStatTemplate", model:attrs.model);
	}
}
