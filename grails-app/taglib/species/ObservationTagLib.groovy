package species

class ObservationTagLib {
	static namespace = "obv"
	
	//def observationService
	def grailsApplication
	
	def create = {attrs, body ->
		out << render(template:"/common/editObservationTemplate", model:attrs.model);
	}
	
	def show = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/showObservationTemplate", model:attrs.model);
		}
	}
	
	def showSnippet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/showObservationSnippetTemplate", model:attrs.model);
		}
	}
	
	def showStory = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/showObservationStoryTemplate", model:attrs.model);
		}
	}
}
