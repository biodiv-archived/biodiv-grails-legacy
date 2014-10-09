package speciespage.search

import species.participation.Observation
import species.Species
import species.Habitat
import species.groups.SpeciesGroup;
import content.eml.Document
import species.auth.SUser;
import utils.Newsletter;
import content.Project
import species.groups.UserGroup;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
import org.springframework.context.ApplicationContext
import species.utils.Utils;

import java.text.SimpleDateFormat;

//import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import java.net.URLDecoder;
import org.apache.solr.common.util.DateUtil;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.apache.solr.common.SolrDocument

import org.springframework.beans.factory.annotation.Autowired;

class BiodivSearchService extends AbstractSearchService {
    
    @Autowired
    def observationsSearchService
    @Autowired
    def speciesSearchService
    @Autowired
    def documentSearchService
    @Autowired
    def SUserSearchService
    //def newsletterSearchService
    @Autowired
    def userGroupSearchService
    //ApplicationContext applicationContext
/*
    def getObservationsSearchServiceBean() {
        println "=======HELLO==========="
        if(!observationsSearchServiceBean) { 
            println "=================="
            observationsSearchServiceBean = applicationContext.getBean("observationsSearchService");
        }
        return observationsSearchServiceBean;
    }
    
    def getSpeciesSearchServiceBean() {
        if(!speciesSearchServiceBean) { 
            speciesSearchServiceBean = applicationContext.getBean("speciesSearchService");
        }
        return speciesSearchServiceBean;
    
    }
    
    def getDocumetSearchServiceBean() {
        if(!documentSearchServiceBean) { 
            documentSearchServiceBean = applicationContext.getBean("documentSearchService");
        }
        return documentSearchServiceBean;
    
    }
    
    def getSUserSearchServiceBean() {
        if(!SUserSearchServiceBean) { 
            SUserSearchServiceBean = applicationContext.getBean("SUserSearchService");
        }
        return SuserSearchServiceBean;
    
    }
    
    def getNewsletterSearchServiceBean() {
        if(!newsletterSearchServiceBean) { 
            newsletterSearchServiceBean = applicationContext.getBean("newsletterSearchService");
        }
        return newsletterSearchServiceBean;
    }
    
    def getUserGroupSearchServiceBean() {
        if(!userGroupSearchServiceBean) { 
            userGroupSearchServiceBean = applicationContext.getBean("userGroupSearchService");
        }
        return userGroupSearchServiceBean;
    }
*/  
    /**
     * 
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to biodiv search index"

        //TODO: change limit
        int limit = BATCH_SIZE//Observation.count()+1, 
        int offset = 0;

        def startTime = System.currentTimeMillis()
        //while(true) {
        if(!publishSearchIndex(null, true)) {
            log.error "FAILED to publish biodiv search index"
            return
        }
        cleanUpGorm()
        //}

        log.info "Time taken to publish biodiv search index is ${System.currentTimeMillis()-startTime}(msec)";
    }


    def publishSearchIndex(List dummy, boolean commit=true) {
        log.info "=======STARTING======== "
        log.info "Initializing publishing to biodiv search index : ";

        log.info "===INDEXING OBV======== "
        observationsSearchService.publishSearchIndex();
        log.info "===INDEXING SP========"
        speciesSearchService.publishSearchIndex();
        log.info "===INDEXING DOC========"
        documentSearchService.publishSearchIndex()
        log.info "===INDEXING USER========"
        SUserSearchService.publishSearchIndex()
        //log.info "===NEWSLETTER========"
        //def f5 = newsletterSearchService.publishSearchIndex(newsletters, commit)
        log.info "=====INDEXING USER GROUP==== "
        userGroupSearchService.publishSearchIndex()

        log.info "===DONE INDEXING========"
        println "===DONE INDEXING========"
        return true;
    }

    def nameTerms(params) {
        List result = new ArrayList();
        def queryResponse = terms(params.term, params.field, params.max);
        if(queryResponse) {
            NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
            for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
                Map.Entry tag = (Map.Entry) iterator.next();
                result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"General"]);
            }
        }
        return result;
    }

    /**
     *
     * @param params
     * @return
     */
    def select(params) {
        def max = Math.min(params.max ? params.max.toInteger() : 12, 100)
        def offset = params.offset ? params.offset.toLong() : 0

        def model;

        try {
            model = getFilteredObjectsFromSearch(params, max, offset, false);
        } catch(SolrException e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * Filter objects by group, habitat, tag, user, species
     * max: limit results to max: if max = -1 return all results
     * offset: offset results: if offset = -1 its not passed to the
     * executing query
     */
    Map getFilteredObjectsFromSearch(params, max, offset, isMapView){
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        def queryParts = getFilteredObjectsQueryFromSearch(params, max, offset, isMapView);
        NamedList paramsList = queryParts.paramsList
        def queryParams = queryParts.queryParams
        def activeFilters = queryParts.activeFilters

        if(isMapView) {
            //query = mapViewQuery + filterQuery + orderByClause
        } else {
            //query += filterQuery + orderByClause
            queryParams["max"] = max
            queryParams["offset"] = offset
        }

        List instanceList = new ArrayList();
        def responseHeader
        long noOfResults = 0;
        def speciesGroupCountList = [];
        List objectTypes = [], sGroups = [], tags= [], contributors = [];

        if(paramsList) {
            //Facets
            paramsList.add('facet', "true");
            paramsList.add('facet.mincount', "1");
            paramsList.add('facet.field', searchFieldsConfig.OBJECT_TYPE);
//            paramsList.add('facet.field', searchFieldsConfig.SGROUP);
//            paramsList.add('facet.field', searchFieldsConfig.TAG);
//            paramsList.add('f.'+searchFieldsConfig.TAG+'.facet.limit', max);
//            paramsList.add('f.'+searchFieldsConfig.TAG+'.facet.offset', 0);

//            paramsList.add('facet.field', searchFieldsConfig.CONTRIBUTOR+"_exact");
//            paramsList.add('f.'+searchFieldsConfig.CONTRIBUTOR+"_exact"+'.facet.limit', max);
//            paramsList.add('f.'+searchFieldsConfig.CONTRIBUTOR+"_exact"+'.facet.offset', 0);


            def queryResponse = search(paramsList);

            if(!params.loadMore?.toBoolean()){
                List objectTypeFacets = queryResponse.getFacetField(searchFieldsConfig.OBJECT_TYPE).getValues()
                objectTypeFacets.each {
                    //TODO: sort on name
                    objectTypes << [name:it.getName(), count:it.getCount()]
                }

/*                List sGroupFacets = queryResponse.getFacetField(searchFieldsConfig.SGROUP).getValues()
                sGroupFacets.each {
                    //TODO: sort on name
                    sGroups << [name:it.getName(), count:it.getCount()]
                }

                List tagFacets = queryResponse.getFacetField(searchFieldsConfig.TAG).getValues()
                tagFacets.each {
                    //TODO: sort on name
                    tags << [name:it.getName(), count:it.getCount()]
                }

                List contributorFacets = queryResponse.getFacetField(searchFieldsConfig.CONTRIBUTOR+"_exact").getValues()
                contributorFacets.each {
                    //TODO: sort on name
                    contributors << [name:it.getName(), count:it.getCount()]
                }
*/ 
            }

            responseHeader = queryResponse?.responseHeader;
            if(queryResponse) {
                org.apache.solr.common.SolrDocumentList results = queryResponse.getResults();
                def instance;
                println queryResponse
                results.each { doc ->
                    Long instanceId = Long.parseLong(doc.getFieldValue(searchFieldsConfig.ID).split('_')[1])
                    String className = doc.getFieldValue(searchFieldsConfig.OBJECT_TYPE)
                    Class clazz = grailsApplication.domainClasses.find { it.clazz.simpleName == className }?.clazz
                    if(clazz) {
                        instance = clazz.read(instanceId);
                    }
/*                    switch(doc.getFieldValue(searchFieldsConfig.OBJECT_TYPE)) {
                        case Species.simpleName:
                            instance = Species.read(instanceId);
                            break;
                        case Observation.simpleName:
                            instance = Observation.read(instanceId);
                            break;
                        case Document.simpleName:
                            instance = Species.read(instanceId);
                            break;
                        case SUser.simpleName:
                            break;
                        case Newsletter.simpleName:
                            break;
                        case UserGroup.simpleName:
                            break;
                        default:
                            log.debug "Not a valid object ${doc}"
                    }
*/
                    if(instance) {
                        instanceList.add(instance);
                    } else {
                        log.error "${doc} has invalid id in search index. May be out of sync from db"
                    }
                }
            }
            noOfResults = queryResponse.getResults().getNumFound();

        }

        return [responseHeader:responseHeader, instanceList:instanceList, instanceTotal:noOfResults, objectTypes:objectTypes, queryParams:queryParams, activeFilters:activeFilters]

    }

    private Map getFilteredObjectsQueryFromSearch(params, max, offset, isMapView) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config;
        params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(config.speciesPortal.group.ALL).id
        params.habitat = (params.habitat)? params.habitat : Habitat.findByName(config.speciesPortal.group.ALL).id
        params.habitat = params.habitat.toLong()
        def queryParams = [isDeleted : false, isShowable:true]

        def activeFilters = [:]


        NamedList paramsList = new NamedList();

        //params.userName = springSecurityService.currentUser.username;
        queryParams["query"] = params.query
        activeFilters["query"] = params.query
        params.query = params.query ?: "";

        String aq = "";
        int i=0;

        if(params.aq instanceof GrailsParameterMap || params.aq instanceof Map) {
            params.aq.each { key, value ->
                queryParams["aq."+key] = value;
                activeFilters["aq."+key] = value;
                if(key.equalsIgnoreCase(searchFieldsConfig.OBJECT_TYPE) && value.equalsIgnoreCase('All')) {
                } else if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
                    if(i++ == 0) {
                        aq = key + ': ('+value+')';
                    } else {
                        aq = aq + " AND " + key + ': ('+value+')';
                    }
                }
            }
        }
        
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String lastRevisedStartDate = '';
        String lastRevisedEndDate = '';
        if(params.daterangepicker_start) {
            Date s = DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']);
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(s)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MINUTE, 0);
            s = new Date(cal.getTimeInMillis())
            //StringWriter str1 = new StringWriter();
            lastRevisedStartDate = dateFormatter.format(s)
            //DateUtil.formatDate(s, cal, str1)
            //println str1
            //lastRevisedStartDate = str1;

        }

        if(params.daterangepicker_end) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            Date e = DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']);
            cal.setTime(e)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            e = new Date(cal.getTimeInMillis())
            //			StringWriter str2 = new StringWriter();
            //			DateUtil.formatDate(e, cal, str2)
            //			println str2
            lastRevisedEndDate = dateFormatter.format(e);
        }

        if(lastRevisedStartDate && lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            aq += " lastrevised:["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;

        } else if(lastRevisedStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " lastrevised:["+lastRevisedStartDate+" TO NOW]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
        } else if (lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " lastrevised:[ * "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;
        }

        if(params.query && aq) {
            params.query = params.query + " AND "+aq
        } else if (aq) {
            params.query = aq;
        }

        paramsList.add('q', Utils.cleanSearchQuery(params.query));
        //options
        if(offset>= 0)
            paramsList.add('start', offset);
        if(max >= 0)
            paramsList.add('rows', max);    
        params['sort'] = params['sort']?:"score"
        String sort = params['sort'].toLowerCase(); 
        if(isValidSortParam(sort)) {
            if(sort.indexOf(' desc') == -1) {
                sort += " desc";
            }
            paramsList.add('sort', sort);
        }

        paramsList.add('fl', params['fl']?:searchFieldsConfig.ID+","+searchFieldsConfig.OBJECT_TYPE);
        
        //Filters
        if(params.sGroup) {
            params.sGroup = params.sGroup.toLong()
            def groupId = utilsServiceBean.getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            } else{
                paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        if(params.habitat && (params.habitat != Habitat.findByName(config.speciesPortal.group.ALL).id)){
            paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
            queryParams["habitat"] = params.habitat
            activeFilters["habitat"] = params.habitat
        }

        if(params.tag) {
            paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
            queryParams["tag"] = params.tag
            activeFilters["tag"] = params.tag
        }

        if(params.user){
            paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if(params.speciesName && (params.speciesName != config.speciesPortal.group.ALL)) {
            paramsList.add('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.speciesName);
            queryParams["name"] = params.name
            activeFilters["name"] = params.name
        }

        if(params.isFlagged && params.isFlagged.toBoolean()){
            paramsList.add('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
            paramsList.add('fq', searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean());
            activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
        }

        if(params.bounds){
            def bounds = params.bounds.split(",")
            def swLat = bounds[0]
            def swLon = bounds[1]
            def neLat = bounds[2]
            def neLon = bounds[3]
            paramsList.add('fq', searchFieldsConfig.LATLONG+":["+swLat+","+swLon+" TO "+neLat+","+neLon+"]");
            activeFilters["bounds"] = params.bounds
        }

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

        if(params.object_type && !params.object_type.equalsIgnoreCase('All')){
            paramsList.add('fq', searchFieldsConfig.OBJECT_TYPE+":"+params.object_type);
            queryParams["object_type"] = params.object_type
            activeFilters["object_type"] = params.object_type
        }
        
        if(params.contributor){
            paramsList.add('fq', searchFieldsConfig.CONTRIBUTOR+":"+params.contributor);
            queryParams["contributor"] = params.contributor
            activeFilters["contributor"] = params.contributor
        }

        log.debug "Along with faceting params : "+paramsList;
        return [paramsList:paramsList, queryParams:queryParams, activeFilters:activeFilters];	
    }

    private boolean isValidSortParam(String sortParam) {
        if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase("visitCount")  || sortParam.equalsIgnoreCase("createdOn") || sortParam.equalsIgnoreCase("lastRevised") )
            return true;
        return false;
    }

}
