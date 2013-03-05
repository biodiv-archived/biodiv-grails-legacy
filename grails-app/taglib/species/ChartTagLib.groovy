package species

class ChartTagLib {
	static namespace = "chart"
	
	def chartService
	
	def showStats = {attrs, body->
		out << render(template:"/chart/genericStatTemplate", model:attrs.model);
	}
	
	def showActivityStats = {attrs, body->
		out << render(template:"/chart/activityStatTemplate", model:attrs.model);
	}
}
