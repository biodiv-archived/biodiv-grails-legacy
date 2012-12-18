package species

class SearchTagLib {

	static namespace = "search"
	
	def searchBox = {attrs, body ->
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
	
		if(params.controller == "species") {
			out << render(template:"/species/advSearchTemplate", model:attrs.model);
		} else if(params.controller == "observation") {
			out << render(template:"/observation/advSearchTemplate", model:attrs.model);
		} else if(params.controller == "newsletter") {
			out << render(template:"/newsletter/advSearchTemplate", model:attrs.model);
		}
	}
}
