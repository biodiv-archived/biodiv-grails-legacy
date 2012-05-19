package species

class SearchTagLib {

	static namespace = "search"
	
	def searchBox = {attrs, body ->
		out << render(template:"/common/search/searchBoxTemplate", model:attrs.model);
	}
}
