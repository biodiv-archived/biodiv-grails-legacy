package speciespage

import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.utils.Utils;
import utils.Newsletter;

class NewsletterService {

	static transactional = false
	def newsletterSearchService
	def grailsApplication;

	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = newsletterSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];

		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Pages"]);
		}
		return result;
	}

	def search(params) {
		def result;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
		def queryParams = [:]
		def activeFilters = [:]

		NamedList paramsList = new NamedList();
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";

		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap) {
			params.aq.each { key, value ->
				queryParams["aq."+key] = value;
				activeFilters["aq."+key] = value;
				if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
					if(i++ == 0) {
						aq = key + ': ('+value+')';
					} else {
						aq = aq + " AND " + key + ': ('+value+')';
					}
				}
			}
		}
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', params['offset']?:"0");
		paramsList.add('rows', params['max']?:"10");
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(sort.indexOf(' desc') == -1) {
			sort += " desc";

		}
		paramsList.add('sort', sort);

		paramsList.add('fl', params['fl']?:"id");
		paramsList.add('hl', true);
		paramsList.add('hl.fl', 'text');
		paramsList.add('hl.fragsize', 300);
//		if(params.tag) {
//			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
//			queryParams["tag"] = params.tag
//			queryParams["tagType"] = 'observation'
//			activeFilters["tag"] = params.tag
//		}
//		if(params.user){
//			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
//			queryParams["user"] = params.user.toLong()
//			activeFilters["user"] = params.user.toLong()
//		}
		
		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}

		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = newsletterSearchService.search(paramsList);
			List<Newsletter> instanceList = new ArrayList<Newsletter>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def instance = Newsletter.get(doc.getFieldValue("id"));
				if(instance)
					instanceList.add(instance);
			}
			println "------------"
println queryResponse.getHighlighting();
			//queryParams = queryResponse.responseHeader.params
			result = [queryParams:queryParams, activeFilters:activeFilters, total:queryResponse.getResults().getNumFound(), instanceList:instanceList, snippets:queryResponse.getHighlighting()]
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, total:0, instanceList:[]];
		return result;
	}
}
