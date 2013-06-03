package species

class ResourceTagLib {
	
    static namespace = "rc"
	
	def grailsApplication
	
    def showResourceList = {attrs, body->
		out << render(template:"/resource/showResourceListTemplate", model:attrs.model);
	}
	
	def rating = {attrs, body-> 
		out << render(template:"/common/ratingTemplate", model:attrs.model);
	}
}

