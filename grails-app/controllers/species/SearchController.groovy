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

    def namesIndexerService;
    def biodivSearchService;
    def grailsApplication
    static defaultAction = "select"

    def select () {
        def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

        def model = biodivSearchService.select(params);

        if(params.loadMore?.toBoolean()){
            println "111======================="
            params.remove('isGalleryUpdate');
            render(template:"/search/showSearchResultsListTemplate", model:model);
            return;
        } else if(request.getHeader('X-Auth-Token') || params.resultType?.equalsIgnoreCase("json")) {
            render model as JSON
        } else if(!params.isGalleryUpdate?.toBoolean()){
            println "222======================="
            params.remove('isGalleryUpdate');
            println model
            println "result=================++++"
            render (view:"select", model:model)
            return;
        } else {
            println "333======================="
            params.remove('isGalleryUpdate');
            model['resultType'] = 'search result'
            def listHtml =  g.render(template:"/search/showSearchResultsListTemplate", model:model);
            def filterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

            listHtml = listHtml.replaceAll(/\n|\t|\s+/,' ');
            def filterPanel = g.render(template:"/search/sidebar", model:[modules:model.objectTypes, sGroups:model.sGroups, tags:model.tags, contributors:model.contributors]);
            def result = [obvListHtml:listHtml, obvFilterMsgHtml:filterMsgHtml, filterPanel:filterPanel,  instanceTotal:model.instanceTotal]
            render result as JSON
            return;
        }
        return;
    }

    /**
     *
     */
    def nameTerms()  {
        params.field = params.field?:"autocomplete";
        params.max = Math.min(params.max ? params.int('max') : 5, 10)
        List suggestions = new ArrayList();
        def namesLookupResults = namesIndexerService.suggest(params);

        suggestions.addAll(namesLookupResults);
        suggestions.addAll(biodivSearchService.nameTerms(params));
        render suggestions as JSON 
    }

}
