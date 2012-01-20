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
	def searchService;
	def namesIndexerService;
	/**
	 * Default action : select
	 */
	def index = {
		render (view:"select");
	}

	/**
	 * 
	 */
	def select = {
		log.debug params;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		if(params.query) {
			params.q = Utils.cleanSearchQuery(params.query);
			params.remove('query');
			params['start'] = params['start']?:"0";
			params['rows'] = params['rows']?:"10";
			params['sort'] = params['sort']?:"score desc";
			params['fl'] = params['fl']?:"id, name";
			params['facet'] = "true";
			params['facet.limit'] = "-1";
			params['facet.mincount'] = "1";
			NamedList paramsList = new NamedList();
			paramsList.addAll(params);
			paramsList.add('facet.field', searchFieldsConfig.NAME_EXACT);
			paramsList.add('facet.field', searchFieldsConfig.CANONICAL_NAME_EXACT);
			paramsList.add('facet.field', searchFieldsConfig.COMMON_NAME_EXACT);
			paramsList.add('facet.field', searchFieldsConfig.UNINOMIAL_EXACT)
			paramsList.add('facet.field', searchFieldsConfig.GENUS)
			paramsList.add('facet.field', searchFieldsConfig.SPECIES)
			paramsList.add('facet.field', searchFieldsConfig.AUTHOR)
			paramsList.add('facet.field', searchFieldsConfig.YEAR)

			log.debug "Along with faceting params : "+paramsList;
			try {
				def queryResponse = searchService.search(paramsList);
				List<Species> speciesInstanceList = new ArrayList<Species>();
				Iterator iter = queryResponse.getResults().listIterator();
				while(iter.hasNext()) {
					def doc = iter.next();
					def speciesInstance = Species.get(doc.getFieldValue("id"));
					if(speciesInstance)
						speciesInstanceList.add(speciesInstance);
				}
				log.debug(queryResponse.getFacetFields());
				[responseHeader:queryResponse.responseHeader, total:queryResponse.getResults().getNumFound(), speciesInstanceList:speciesInstanceList, snippets:queryResponse.getHighlighting(), facets:queryResponse.getFacetFields()];
			} catch(SolrException e) {
				e.printStackTrace();
				[params:params, speciesInstanceList:[]];
			}
		} else {
			[params:params, speciesInstanceList:[]];
		}
	}

	/**
	 * 
	 */
	def advSelect = {
		log.debug params;
		String query  = "";
		def newParams = [:]
		for(field in params) {
			if(!(field.key ==~ /action|controller|sort|fl|start|rows/) && field.value ) {
				if(field.key.equalsIgnoreCase('name')) {
					newParams[field.key] = field.value;
					query = query + " " +field.value;
				} else {
					newParams[field.key] = field.value;
					query = query + " " + field.key + ': "'+field.value+'"';
				}
			}
		}
		if(query) {
			newParams['query'] = query;
			redirect (action:"select", params:newParams);
		}
		render (view:'advSelect', params:newParams);
	}

	def nameTerms = {
		log.debug params;
		params.field = params.field?:"autocomplete";
		List result = new ArrayList();

		def namesLookupResults = namesIndexerService.suggest(params)
		result.addAll(namesLookupResults);

				def queryResponse = searchService.terms(params);
				NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		
				for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
					Map.Entry tag = (Map.Entry) iterator.next();
					result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"General"]);
				}
		render result as JSON;

	}

	/**
	 * 
	 */
	def terms = {
		log.debug params;
		params.field = params.field?:"autocomplete";
		List result = new ArrayList();

		if(params.field == "autocomplete" || params.field == 'name') {
			def namesLookupResults = namesIndexerService.suggest(params)
			result.addAll(namesLookupResults);
		} else {

			def queryResponse = searchService.terms(params);
			NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];

			for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
				Map.Entry tag = (Map.Entry) iterator.next();
				result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"General"]);
			}
		}
		render result as JSON;
	}
}
