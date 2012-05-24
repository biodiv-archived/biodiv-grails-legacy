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
}
