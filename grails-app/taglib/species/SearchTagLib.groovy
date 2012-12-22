package species

class SearchTagLib {

	static namespace = "search"
	
	def searchBox = {attrs, body ->
		if(!attrs.model) attrs.model = [:]
		if(params.controller=='species'||params.controller=='observation'||params.controller=='newsletter'||params.controller=='members') {
			attrs.model.controller= params.controller
		} else if (params.controller=='userGroup' && (params.action=='species'||params.action=='observation'||params.action=='newsletter'||params.action=='members')) {
			attrs.model.controller= params.action
		} else {
			attrs.model.controller= "species"
		}
		out << render(template:"/common/search/searchBoxTemplate", model:attrs.model);
	}
	
	def searchResultsHeading = {attrs, body ->
		out << render(template:"/common/search/searchResultsHeadingTemplate", model:attrs.model);
	} 
	
	def noSearchResults =  {attrs, body ->
		out << render(template:"/common/search/noSearchResultsTemplate", model:attrs.model);
	}
	
	def advSearch =  {attrs, body ->
		def model = attrs.model;
	
		if(params.controller == "species" || (params.controller == 'userGroup' && params.action == 'species')) {
			out << render(template:"/species/advSearchTemplate", model:attrs.model);
		} else if(params.controller == "observation" || (params.controller == 'userGroup' && params.action == 'observation')) {
			out << render(template:"/observation/advSearchTemplate", model:attrs.model);
		} else if(params.controller == "newsletter"  || (params.controller == 'userGroup' && params.action == 'newsletter')) {
			out << render(template:"/newsletter/advSearchTemplate", model:attrs.model);
		}
	}
}
