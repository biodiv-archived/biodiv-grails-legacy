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
import org.springframework.context.ApplicationContext
import species.utils.Utils;

import java.text.SimpleDateFormat;

//import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;

import java.net.URLDecoder;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;

import org.springframework.beans.factory.annotation.Autowired;

class BiodivSearchService extends AbstractSearchService {

    static transactional = false

    def observationsSearchService
    def speciesSearchService
    def documentSearchService
    def SUserSearchService
    //def newsletterSearchService
    def userGroupSearchService
    def resourceSearchService
    def newsletterSearchService
    def projectSearchService



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

        log.info "===INDEXING SP========"
        speciesSearchService.INDEX_DOCS = INDEX_DOCS
        speciesSearchService.publishSearchIndex();


        log.info "===INDEXING OBV======== "
        observationsSearchService.INDEX_DOCS = INDEX_DOCS
        observationsSearchService.publishSearchIndex();

        log.info "===INDEXING DOC========"
        documentSearchService.INDEX_DOCS = INDEX_DOCS
        documentSearchService.publishSearchIndex()

        log.info "===INDEXING USER========"
        SUserSearchService.INDEX_DOCS = INDEX_DOCS
        SUserSearchService.publishSearchIndex()

        //log.info "===NEWSLETTER========"
        //def f5 = newsletterSearchService.publishSearchIndex(newsletters, commit)
        log.info "=====INDEXING USER GROUP==== "
        userGroupSearchService.INDEX_DOCS = INDEX_DOCS
        userGroupSearchService.publishSearchIndex()

        log.info "=====INDEXING NEWSLETTER==== "
        newsletterSearchService.INDEX_DOCS = INDEX_DOCS
        newsletterSearchService.publishSearchIndex()

        log.info "=====INDEXING RESOURCE==== "
        resourceSearchService.INDEX_DOCS = INDEX_DOCS
        resourceSearchService.publishSearchIndex()

        log.info "=====INDEXING PROJECT==== "
        projectSearchService.INDEX_DOCS = INDEX_DOCS
        projectSearchService.publishSearchIndex()

        log.info "=====INDEXING UserGroup==== "
        userGroupSearchService.INDEX_DOCS = INDEX_DOCS
        userGroupSearchService.publishSearchIndex()

        log.info "===DONE INDEXING========"
        println "===DONE INDEXING========"
        return true;
    }

    def nameTerms(params) {
        List result = new ArrayList();
        def queryResponse = terms(params.term, params.field, params.max);
        if(queryResponse) {
            /*NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
            for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
                Map.Entry tag = (Map.Entry) iterator.next();
                result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"General"]);
            }*/
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
        } catch(Exception e) {
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
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields
        def queryParts = getFilteredObjectsQueryFromSearch(params, max, offset, isMapView);
        def paramsList = queryParts.paramsList
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
        List objectTypes = [], uGroups = [], sGroups = [], tags= [], contributors = [];

        if(paramsList) {
                def queryResponse = search(params);
                def instance;
                queryResponse.eData.each { doc ->
                    Long instanceId = Long.parseLong(doc.id);
                    String className = doc.type
                    Class clazz = grailsApplication.domainClasses.find { it.clazz.simpleName.equalsIgnoreCase(className) }?.clazz
                    if(clazz) {
                        instance = clazz.read(instanceId);
                    }

                    def containers = doc.container;
                    List containerInstances = [];


                    if(instance) {
                        if(params.format?.equalsIgnoreCase("json")) {
                            instanceList.add([id:instanceId, object_type:className, title:instance.title(), summary:instance.summary(params.userLanguage), containers:containerInstances]);
                        } else {
                            instanceList.add([id:instanceId, object_type:className, instance:instance, containers:containerInstances]);
                        }
                    } else {
                        log.error "${doc} has invalid id in search index. May be out of sync from db"
                    }
                }
                noOfResults = queryResponse.count;

                queryResponse.docCount.each {
                    println it;
                    it.each {key,value ->
                     objectTypes << [name:key, count:value]
                   }
                }

            // if(queryResponse) {
            //     if(!params.loadMore?.toBoolean()){
            //         List objectTypeFacets = queryResponse.getFacetField(searchFieldsConfig.OBJECT_TYPE)?.getValues()?:[]
            //         objectTypeFacets.each {
            //             //TODO: sort on name
            //             objectTypes << [name:it.getName(), count:it.getCount()]
            //         }
            //
            //         List uGroupFacets = queryResponse.getFacetField(searchFieldsConfig.USER_GROUP)?.getValues()?:[]
            //         uGroupFacets.each {
            //             //TODO: sort on name
            //             uGroups << [name:it.getName(), count:it.getCount()]
            //         }
            //     }
            //
            //     responseHeader = queryResponse.responseHeader;
            //     org.apache.solr.common.SolrDocumentList results = queryResponse.getResults();
            //     def instance;
            //     results.each { doc ->
            //         Long instanceId = Long.parseLong(doc.getFieldValue(searchFieldsConfig.ID).split('_')[1])
            //         String className = doc.getFieldValue(searchFieldsConfig.OBJECT_TYPE)
            //         Class clazz = grailsApplication.domainClasses.find { it.clazz.simpleName == className }?.clazz
            //         if(clazz) {
            //             instance = clazz.read(instanceId);
            //         }
            //
            //         def containers = doc.getFieldValue(searchFieldsConfig.CONTAINER)
            //         List containerInstances = [];
            //         containers.each { container ->
            //             container = container.split('_')
            //             if(container) {
            //                 Long containerId = Long.parseLong(container[1])
            //                 className = container[0]
            //                 clazz = grailsApplication.domainClasses.find { it.clazz.simpleName == className }?.clazz
            //                 if(clazz) {
            //                     containerInstances << clazz.read(containerId);
            //                 }
            //             }
            //         }
            //
            //         if(instance) {
            //             if(params.format?.equalsIgnoreCase("json")) {
            //                 instanceList.add([id:instanceId, object_type:className, title:instance.title(), summary:instance.summary(params.userLanguage), containers:containerInstances]);
            //             } else {
            //                 instanceList.add([id:instanceId, object_type:className, instance:instance, containers:containerInstances]);
            //             }
            //         } else {
            //             log.error "${doc} has invalid id in search index. May be out of sync from db"
            //         }
            //     }
            //     noOfResults = queryResponse.getResults().getNumFound();
            // }
        }

        return [responseHeader:responseHeader, queryParams:queryParams, activeFilters:activeFilters, instanceTotal:noOfResults, instanceList:instanceList, objectTypes:objectTypes, uGroups:uGroups]

    }

    private Map getFilteredObjectsQueryFromSearch(params, max, offset, isMapView) {
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields
        def config = grails.util.Holders.config;
        params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(config.speciesPortal.group.ALL).id
        params.habitat = (params.habitat)? params.habitat : Habitat.findByName(config.speciesPortal.group.ALL).id
        params.habitat = params.habitat.toLong()
        def queryParams = [isDeleted : false, isShowable:true]

        def activeFilters = [:]


        Map paramsList = [:];

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
                if(key.equalsIgnoreCase(searchFieldsConfig.OBJECT_TYPE)) {
                    if(!value.equalsIgnoreCase('All')) {
                        paramsList.put('fq', key+":"+value)
                    }
                } else if(key.equalsIgnoreCase(searchFieldsConfig.LICENSE) || key.equalsIgnoreCase(searchFieldsConfig.TAG) || key.equalsIgnoreCase(searchFieldsConfig.USER_GROUP_WEBADDRESS) || key.equalsIgnoreCase(searchFieldsConfig.CONTEXT) || key.equalsIgnoreCase(searchFieldsConfig.CONTAINER) || key.equalsIgnoreCase(searchFieldsConfig.DOC_TYPE)) {
                    paramsList.put('fq', key+':"'+value+'"')
                } else if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
                    if(i++ == 0) {
                        aq = key + ':('+value+')';
                    } else {
                        aq = aq + " AND " + key + ':('+value+')';
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
            aq += " "+searchFieldsConfig.UPLOADED_ON+":["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;

        } else if(lastRevisedStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPLOADED_ON+":["+lastRevisedStartDate+" TO NOW]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
        } else if (lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPLOADED_ON+":[ * "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;
        }

        String observedOnStartDate = '';
        String observedOnEndDate = '';
        if(params.observedon_start) {
            Date s = DateUtil.parseDate(params.observedon_start, ['dd/MM/yyyy']);
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(s)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MINUTE, 0);
            s = new Date(cal.getTimeInMillis())
            //StringWriter str1 = new StringWriter();
            observedOnStartDate = dateFormatter.format(s)
            //DateUtil.formatDate(s, cal, str1)
            //println str1
            //lastRevisedStartDate = str1;

        }

        if(params.observedon_end) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            Date e = DateUtil.parseDate(params.observedon_end, ['dd/MM/yyyy']);
            cal.setTime(e)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            e = new Date(cal.getTimeInMillis())
            //			StringWriter str2 = new StringWriter();
            //			DateUtil.formatDate(e, cal, str2)
            //			println str2
            observedOnEndDate = dateFormatter.format(e);
        }

        if(observedOnStartDate && observedOnEndDate) {
            if(i > 0) aq += " AND";
            aq += " "+searchFieldsConfig.OBSERVED_ON+":["+observedOnStartDate+" TO "+observedOnEndDate+"]";
            queryParams['observedon_start'] = params.observedon_start;
            queryParams['observedon_end'] = params.observedon_end;
            activeFilters['observedon_start'] = params.observedon_start;
            activeFilters['observedon_end'] = params.observedon_end;

        } else if(observedOnStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.OBSERVED_ON+":["+observedOnStartDate+" TO NOW]";
            queryParams['observedon_start'] = params.observedon_start;
            activeFilters['observedon_start'] = params.observedon_end;
        } else if (observedOnEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.OBSERVED_ON+":[ * "+observedOnEndDate+"]";
            queryParams['observedon_end'] = params.observedon_end;
            activeFilters['observedon_end'] = params.observedon_end;
        }


        if(params.query && aq) {
            params.query = params.query + " AND "+aq
        } else if (aq) {
            params.query = aq;
        }

        String cleanSearchQuery = Utils.cleanSearchQuery(params.query);
        if(!cleanSearchQuery) cleanSearchQuery = "id:*"
        paramsList.put('q', cleanSearchQuery);
        //options
        if(offset>= 0)
            paramsList.put('start', offset);
        if(max >= 0)
            paramsList.put('rows', max);
        params['sort'] = params['sort']?:"score"
        String sort = params['sort'].toLowerCase();
        if(isValidSortParam(sort)) {
            if(sort.indexOf(' desc') == -1) {
                sort += " desc";
            }
            paramsList.put('sort', sort);
        }

        paramsList.put('fl', params['fl']?:searchFieldsConfig.ID+","+searchFieldsConfig.OBJECT_TYPE+","+searchFieldsConfig.CONTAINER);

        //Filters
        if(params.sGroup) {
            params.sGroup = params.sGroup.toLong()
            def groupId = utilsService.getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            } else{
                paramsList.put('fq', searchFieldsConfig.SGROUP+":"+groupId);
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        if(params.habitat && (params.habitat != Habitat.findByName(config.speciesPortal.group.ALL).id)){
            paramsList.put('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
            queryParams["habitat"] = params.habitat
            activeFilters["habitat"] = params.habitat
        }

        if(params.tag) {
            paramsList.put('fq', searchFieldsConfig.TAG+":"+params.tag);
            queryParams["tag"] = params.tag
            activeFilters["tag"] = params.tag
        }

        if(params.user){
            paramsList.put('fq', searchFieldsConfig.USER+":"+params.user);
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if(params.speciesName && (params.speciesName != config.speciesPortal.group.ALL)) {
            paramsList.put('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.speciesName);
            queryParams["name"] = params.name
            activeFilters["name"] = params.name
        }

        if(params.isFlagged && params.isFlagged.toBoolean()){
            paramsList.put('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
            paramsList.put('fq', searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean());
            activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
        }

        if(params.bounds){
            def bounds = params.bounds.split(",")
            def swLat = bounds[0]
            def swLon = bounds[1]
            def neLat = bounds[2]
            def neLon = bounds[3]
            paramsList.put('fq', searchFieldsConfig.LATLONG+":["+swLat+","+swLon+" TO "+neLat+","+neLon+"]");
            activeFilters["bounds"] = params.bounds
        }

        if(params.uGroup) {
            if (params.uGroup) {
                paramsList.put('fq', searchFieldsConfig.USER_GROUP+":("+params.uGroup+")");
                queryParams["uGroup"] = params.uGroup
                activeFilters["uGroup"] = params.uGroup
            } else {
                queryParams["uGroup"] = "ALL"
                activeFilters["uGroup"] = "ALL"
            }
        }

        if(params.object_type && !params.object_type.equalsIgnoreCase('All')){
            paramsList.put('fq', searchFieldsConfig.OBJECT_TYPE+":("+params.object_type.capitalize()+")");
            queryParams["object_type"] = params.object_type
            activeFilters["object_type"] = params.object_type
        }

        if(params.contributor){
            paramsList.put('fq', searchFieldsConfig.CONTRIBUTOR+":"+params.contributor);
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
