package species

class RecommendationTagLib {

	static namespace = "reco"
	
	def recommendationService
	def grailsApplication
	
	def create = {attrs, body ->
		out << render(template:"/common/editRecommendationTemplate", model:attrs.model);
	}
	
	def show = {attrs, body->
		if(attrs.model.recommendationInstance) {
			out << render(template:"/common/showRecommendationTemplate", model:attrs.model);
		}
	}
}
