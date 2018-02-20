package species

import grails.converters.JSON;
import grails.converters.XML;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import static org.springframework.http.HttpStatus.*;

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
    def grailsApplication;
    def utilsService;
    static defaultAction = "select"

    /**
    *
    */
    def select () {
        def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
        params['userLangauge'] = utilsService.getCurrentLanguage(request);

        def model = biodivSearchService.select(params);

        model['userLanguage'] = params.userLanguage;
        model.remove('responseHeader');

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model['resultType'] = 'search result'
            model['obvListHtml'] =  g.render(template:"/search/showSearchResultsListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

            model['filterPanel'] = g.render(template:"/search/sidebar", model:[modules:model.objectTypes, sGroups:model.sGroups, tags:model.tags, contributors:model.contributors]);

            model['obvListHtml'] = model['obvListHtml'].replaceAll(/\n|\t|\s+/,' ');
            model.remove('instanceList');
        }

        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    params.remove('isGalleryUpdate');
                    render(template:"/search/showSearchResultsListTemplate", model:model.model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    params.remove('isGalleryUpdate');
                    render (view:"select", model:model.model)
                    return;
                } else {
                    return;
                }
            }
            json { render model as JSON }
            xml { render model as XML }
        }
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

    /**
    *
    */
    def search() {
        NamedList paramsList = new NamedList()
        params.each {key,value ->
            paramsList.add(key, value);
        }
        def result = biodivSearchService.search(paramsList)

        println result;
        println "++++++++++++++++++++++++++++++++++++++++++"
/*        def facetResults = [];
        if(result.getFacetField(params['facet.field'])) {
        List objectTypeFacets = result.getFacetField(params['facet.field'])?.getValues()
        if(objectTypeFacets) {
        objectTypeFacets.each {
            //TODO: sort on name
            facetResults <<  [name:it.getName(), count:it.getCount()]

        }
        }
        }
*/
        render result as JSON
    }

}
