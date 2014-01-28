package species

class NewsletterTagLib {
	static namespace = "newsletter"
	
	def searchResults = {attrs, body->
		out << render(template:"/newsletter/searchResultsTemplate", model:attrs.model);
	}
}
