package speciespage

import species.search.SearchIndexManager;

class SearchService {

    static transactional = false
	
	def grailsApplication
	
	private SearchIndexManager searchIndexManager = new SearchIndexManager();
	
	def search(query) {
		return searchIndexManager.search(query)
    }
	
	def terms(query) {
		return searchIndexManager.terms(query)
	}

}
