package species

import grails.converters.JSON;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import species.utils.Utils;


/**
 * 
 * @author sravanthi
 *
 */
class SearchController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def speciesSearchService;
	def namesIndexerService;
	def observationService;
	def speciesService;
	def SUserService;
	def userGroupService;
	
	/**
	 * Default action : select
	 */
	def index = {
		render (view:"select", controller:"observation");
	}

	def nameTerms = {
		log.debug params;
		params.field = params.field?:"autocomplete";
		params.max = Math.min(params.max ? params.int('max') : 5, 10)
		List suggestions = new ArrayList();
		def namesLookupResults = namesIndexerService.suggest(params);
		
		suggestions.addAll(namesLookupResults);
		suggestions.addAll(speciesService.nameTerms(params));
		suggestions.addAll(observationService.nameTerms(params));
		suggestions.addAll(userGroupService.nameTerms(params));
		suggestions.addAll(SUserService.nameTerms(params));
		render suggestions as JSON 
	}
	
}
