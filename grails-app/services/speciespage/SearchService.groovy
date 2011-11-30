package speciespage

import species.Species;
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

	def publishSearchIndex(List<Species> species) {
		return searchIndexManager.publishSearchIndex(species);
	}
}
