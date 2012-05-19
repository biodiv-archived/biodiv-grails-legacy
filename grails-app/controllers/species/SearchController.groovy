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
	/**
	 * Default action : select
	 */
	def index = {
		render (view:"select", controller:"observation");
	}

	
}
