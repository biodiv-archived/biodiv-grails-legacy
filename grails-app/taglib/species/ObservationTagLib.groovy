package species

class ObservationTagLib {
	static namespace = "obv"
	
	def observationService
	def grailsApplication
	
	def create = {attrs, body ->
		out << render(template:"/common/observation/editObservationTemplate", model:attrs.model);
	}
	
	def show = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationTemplate", model:attrs.model);
		}
	}
	
	def showSnippet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationSnippetTemplate", model:attrs.model);
		}
	}

        def showSnippetTablet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationSnippetTabletTemplate", model:attrs.model);
		}
	}

	
	def showStory = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStoryTemplate", model:attrs.model);
		}
	}

	def showStoryTablet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStoryTabletTemplate", model:attrs.model);
		}
	}

	def showRelatedStory = {attrs, body->
			out << render(template:"/common/observation/showObservationRelatedStoryTemplate", model:attrs.model);
	}
	
	def showGroupFilter = {attrs, body->
			out << render(template:"/common/speciesGroupFilterTemplate", model:attrs.model);
	}
	
	
	def showRating = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationRatingTemplate", model:attrs.model);
		}
	}

	def showLocation = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationLocationTemplate", model:attrs.model);
		}
	}
	def showTagsSummary = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showTagsSummaryTemplate", model:attrs.model);
		}
	}
	
	def showTags = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationTagsTemplate", model:attrs.model);
		}
	}

	def showObvStats = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStatsTemplate", model:attrs.model);
		}
	}
	
	def showTagsList = {attrs, body->
			def tags = observationService.findAllTagsSortedByObservationCount(50);
			out << render(template:"/common/observation/showTagsListTemplate", model:[tags:tags]);
	}
	
	def showTagsCloud = {attrs, body->
		out << render(template:"/common/observation/showTagsCloudTemplate", model:attrs.model);
	}
	
	def showAllTags = {attrs, body->
		out << render(template:"/common/observation/showAllTagsTemplate", model:attrs.model);
	}

	def showGroupList = {attrs, body->
		out << render(template:"/common/observation/showGroupListTemplate", model:attrs.model);
	}
	
	def showObservationsLocation = {attrs, body->
		out << render(template:"/common/observation/showObservationMultipleLocationTemplate", model:attrs.model);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Tag List added by specific User ///////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	def showAllTagsOfUser = {attrs, body->
		def tags = observationService.getAllTagsOfUser(attrs.model.userId.toLong());
		out << render(template:"/common/observation/showTagsListTemplate", model:[tags:tags]);
	}
	def showUserAddedTags = {attrs, body->
		out << render(template:"/common/observation/showAllTagsTemplate", model:attrs.model);
		//out << render(template:"/common/observation/showUserAddedTagsTemplate", model:attrs.model);
	}
	
	
}
