package speciespage


import java.util.List
import java.text.DateFormat
import org.apache.solr.common.util.DateUtil;
import org.apache.commons.logging.LogFactory
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.hibernate.exception.ConstraintViolationException;
import grails.plugin.springsecurity.SpringSecurityUtils;

import species.License.LicenseType;
import species.Contributor;
import species.Field
import species.Resource;
import species.Resource.ResourceType;
import species.participation.Observation;
import species.Species;
import species.License;
import species.Reference;
import species.SpeciesField;
import species.SpeciesField.AudienceType;
import species.SpeciesField.Status;
import species.TaxonomyDefinition;
import species.formatReader.SpreadsheetReader
import species.groups.SpeciesGroup;
import species.ScientificName
import species.Synonyms;
import species.SynonymsMerged;
import species.CommonNames;
import species.Language;
import species.Classification;
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.NewSimpleSpreadsheetConverter
import species.sourcehandler.SourceConverter;
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter
import species.utils.Utils;
import species.auth.SUser;
import java.text.SimpleDateFormat;
import species.sourcehandler.exporter.DwCAExporter
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;
import species.participation.DownloadLog;
import species.groups.UserGroup;
import species.AbstractObjectService;
import species.TaxonomyRegistry;
import species.ScientificName.TaxonomyRank;
import org.hibernate.FetchMode;
import grails.converters.JSON;
import species.participation.ActivityFeedService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;

import species.NamesMetadata.NameStatus;
import content.eml.DocSciName;
import content.eml.Document;
import species.AcceptedSynonym;
import species.sourcehandler.exporter.DwCSpeciesExporter
import java.io.File ;
import species.participation.NamelistService

class SpeciesService extends AbstractObjectService  {

    private static log = LogFactory.getLog(this);

    static transactional = false

    def groupHandlerService;
    def namesLoaderService;
    def externalLinksService;
    def speciesSearchService;
    def namesIndexerService;
    def speciesPermissionService;
    def taxonService;
    def activityFeedService;
    def messageSource;
	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm aaa")
    static int BATCH_SIZE = 10;
    def request;
    //static int noOfFields = Field.count();

    def nameTerms(params) {
        List result = new ArrayList();
        def queryResponse = speciesSearchService.terms(params.term, params.field, params.max);
        if(queryResponse) {
            NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
            for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
                Map.Entry tag = (Map.Entry) iterator.next();
                result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Species Pages"]);
            }
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
        if(params.aq instanceof GrailsParameterMap || params.aq instanceof Map) {
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
            lastRevisedEndDate = dateFormatter.format(e);
        }

        if(lastRevisedStartDate && lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            aq += " "+searchFieldsConfig.UPDATED_ON+":["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;
        } else if(lastRevisedStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPDATED_ON+":["+lastRevisedStartDate+" TO NOW]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
        } else if (lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPDATED_ON+":[ * "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;
        }

        if(params.query && aq) {
            params.query = params.query + " AND "+aq
        } else if (aq) {
            params.query = aq;
        }

        def offset = params.offset ? params.long('offset') : 0

        paramsList.add('q', Utils.cleanSearchQuery(params.query));
        paramsList.add('start', offset);
        def max = Math.min(params.max ? params.int('max') : 12, 100)
        paramsList.add('rows', max);
        params['sort'] = params['sort']?:"score"
        String sort = params['sort'].toLowerCase();
        if(isValidSortParam(sort)) {
            if(sort.indexOf(' desc') == -1 && sort.indexOf(' asc') == -1 ) {
                sort += " desc";
            }
            paramsList.add('sort', sort);
        }
        queryParams["max"] = max
        queryParams["offset"] = offset

        paramsList.add('fl', params['fl']?:"id");

        if(params.sGroup) {
            params.sGroup = params.sGroup.toLong()
            def groupId = getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            } else{
                paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        //		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
        //			paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
        //			queryParams["habitat"] = params.habitat
        //			activeFilters["habitat"] = params.habitat
        //		}
        //		if(params.tag) {
        //			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
        //			queryParams["tag"] = params.tag
        //			queryParams["tagType"] = 'species'
        //			activeFilters["tag"] = params.tag
        //		}
        //		if(params.user){
        //			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
        //			queryParams["user"] = params.user.toLong()
        //			activeFilters["user"] = params.user.toLong()
        //		}
        if(params.name) {
            paramsList.add('fq', searchFieldsConfig.NAME+":"+params.name);
            queryParams["name"] = params.name
            activeFilters["name"] = params.name
        }

        if(params.uGroup) {
            if(params.uGroup == "THIS_GROUP") {
                String uGroup = params.webaddress
                if(uGroup) {
                    //AS we dont have selecting species for group ... we are ignoring this filter
                    //paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
                }
                queryParams["uGroup"] = params.uGroup
                activeFilters["uGroup"] = params.uGroup
            } else {
                queryParams["uGroup"] = "ALL"
                activeFilters["uGroup"] = "ALL"
            }
        }

        if(params.query && params.startsWith && params.startsWith != "A-Z"){
            params.query = params.query + " AND "+searchFieldsConfig.TITLE+":"+params.startsWith+"*"
            //paramsList.add('fq', searchFieldsConfig.TITLE+":"+params.startsWith+"*");
            queryParams["startsWith"] = params.startsWith
            activeFilters["startsWith"] = params.startsWith
        }
        log.debug "Along with faceting params : "+paramsList;
        try {
            def queryResponse = speciesSearchService.search(paramsList);
            List<Species> speciesInstanceList = new ArrayList<Species>();
            Iterator iter = queryResponse.getResults().listIterator();
            while(iter.hasNext()) {
                def doc = iter.next();
                Long id = (doc.getFieldValue("id").tokenize("_")[1]).toLong()
                def speciesInstance = Species.get(id);
                if(speciesInstance)
                    speciesInstanceList.add(speciesInstance);
            }

            //queryParams = queryResponse.responseHeader.params
            result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), speciesInstanceList:speciesInstanceList, snippets:queryResponse.getHighlighting()]
            return result;
        } catch(SolrException e) {
            e.printStackTrace();
        }

        result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
        return result;
    }

    private boolean isValidSortParam(String sortParam) {
        if(sortParam.equalsIgnoreCase(grailsApplication.config.speciesPortal.searchFields.SCORE) || sortParam.equalsIgnoreCase(grailsApplication.config.speciesPortal.searchFields.UPDATED_ON))
            return true;
        return false;
    }

    /**
    * Add Species Field
    */
    def addSpeciesField(long speciesId, long fieldId, params) {
        
        if(!fieldId || !speciesId) {
            return [success:false, msg:messageSource.getMessage("info.cannot.empty", null, LCH.getLocale())]
        }
        XMLConverter converter = new XMLConverter();

        Species speciesInstance = Species.get(speciesId);
        Field field = Field.read(fieldId);

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission.add", null, LCH.getLocale())]
        }

        if(!field) {
            return [success:false, msg:messageSource.getMessage("info.invalid.field", null, LCH.getLocale())]
        }
        try {
            SpeciesField speciesFieldInstance = createNewSpeciesField(speciesInstance, field, null);
            if(speciesFieldInstance) {
                //updating metadata for the species field
                def result = updateSpeciesFieldInstance(speciesFieldInstance, params);
                def errors = result.errors;
                if(result.success) {
                    speciesInstance.addToFields(speciesFieldInstance);

                    //TODO:make sure this is run in only one user updates this species at a time
                    Species.withTransaction {
                        if(!speciesInstance.save()) {
                            speciesInstance.errors.each { errors << it; log.error it }
                            return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale()), errors:errors]
                        }
                    }

                    List sameFieldSpeciesFieldInstances =  speciesInstance.fields.findAll { it.field.id == field.id} as List
                    sortAsPerRating(sameFieldSpeciesFieldInstances);
                    addMediaInSpField(params, speciesFieldInstance);
                    return [success:true, msg:messageSource.getMessage("info.success.added", null, LCH.getLocale()), id:field.id, content:sameFieldSpeciesFieldInstances, speciesId:speciesInstance.id, errors:errors, speciesFieldInstance:speciesFieldInstance, speciesInstance:speciesInstance, activityType:ActivityFeedService.SPECIES_FIELD_CREATED, activityDesc:ActivityFeedService.SPECIES_FIELD_CREATED+" : "+field, mailType:ActivityFeedService.SPECIES_FIELD_CREATED]
                } else {
                    return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale()), errors:errors]
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale())]
        }
        return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale())]
    }       

    SpeciesField createNewSpeciesField(Species speciesInstance, Field fieldInstance, String value) {
            def newSpeciesFieldInstance = (new XMLConverter()).createSpeciesField(speciesInstance, fieldInstance, value, [springSecurityService.currentUser.email], [], [LicenseType.CC_BY.value()], [SpeciesField.AudienceType.GENERAL_PUBLIC.value()], [SpeciesField.Status.UNDER_VALIDATION.value()]);
            newSpeciesFieldInstance.species = speciesInstance;
            newSpeciesFieldInstance.field = fieldInstance;
            return newSpeciesFieldInstance;
    }

    /**
     * Update Species Field
     */
    def updateSpeciesField(SpeciesField speciesField, params) {
        
        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }

        try {
            def result;
            SpeciesField.withTransaction { status ->
                result = updateSpeciesFieldInstance(speciesField, params); 
                if(result.success  == false || !speciesField.save(flush:true)) { 
                    speciesField.errors.each { result.errors << it }
                    return [success:false, msg:messageSource.getMessage("info.update.speciesfield", null, LCH.getLocale()), errors:result.errors]
                }
                addMediaInSpField(params, speciesField);
            }
            log.debug "Successfully updated species field";
            return [success:true, msg:messageSource.getMessage("info.success.update", null, LCH.getLocale()), errors:result.errors, content:speciesField, speciesFieldInstance:speciesField, speciesInstance:speciesField.species, activityType:ActivityFeedService.SPECIES_FIELD_UPDATED, activityDesc:ActivityFeedService.SPECIES_FIELD_UPDATED+" : "+speciesField.field, mailType:ActivityFeedService.SPECIES_FIELD_UPDATED]
        } catch(Exception e) {
            e.printStackTrace();
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = e.getMessage();
            return [success:false, msg:messageSource.getMessage("info.error.updating", messagesourcearg, LCH.getLocale())]
        }
    }

    private def updateSpeciesFieldInstance(SpeciesField speciesField, params) {
        List errors = [];

        if(!params.description) {
            return [success:false, msg:messageSource.getMessage("info.about.description", null, LCH.getLocale()), errors:[messageSource.getMessage("info.about.description", null, LCH.getLocale())]]
        }

        String msg;
        if(!params.contributor) {
            params.contributor = springSecurityService.currentUser.id+'';
        }

        // Language
        speciesField.language = params.locale_language;

        //contributors
        speciesField.contributors.clear();

        List contributors = Utils.getUsersList(params.contributor);
        contributors.each { c ->
            speciesField.addToContributors(c);
        }
        /*
        params.contributor.split("\\r?\\n|,").each { l ->
            l = l.trim()
            if(l) {
                SUser c = SUser.findByEmail(l.trim());
                if(!c) {
                    errors <<  "No registered user with email ${l} found"
                } else {
                    speciesField.addToContributors(c);
                }
            }
        } */

        //attributions
        speciesField.attributors?.clear();
        params.attribution?.split("\\r?\\n").each { l ->
            l = l.trim()
            if(l) {
                Contributor c = (new XMLConverter()).getContributorByName(l.trim(), true);
                if(!c) {
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = l;
                    errors <<  messageSource.getMessage("info.error.updating.license", messagesourcearg, LCH.getLocale())
                } else {
                    speciesField.addToAttributors(c);
                }
            }
        }   

        //reference
        if(speciesField.references?.size() > 0) {
            /* HACK:As reference has all-delete-orphan... a clear on this collection is failing 
             * java.lang.UnsupportedOperationException: queued clear cannot be used with orphan delete
             * This is fixed in grails 2 http://jira.grails.org/browse/GRAILS-6734
             * For now doing hack by executing basic sql
             */
            SpeciesField.executeUpdate('delete Reference r where r.speciesField = :speciesField', ['speciesField':speciesField]);
            //            speciesField.references?.clear();
        }
        params.reference?.split("\\r?\\n").each { l ->
            l = l.trim(); 

            if(l && l.trim()) {
                speciesField.addToReferences(new Reference(title:l.trim()));
            }
        }   

        //license
        speciesField.licenses.clear();
        params.license.split("\\r?\\n|,").each { l ->
            l = l.trim();
            if(l) {
                License c = (new XMLConverter()).getLicenseByType(l, false);
                if(!c) { 
                    errors << messageSource.getMessage("info.error.updating.license", null, LCH.getLocale())
                } else {
                    speciesField.addToLicenses(c);
                }
            }
        }

        //audienceType
        speciesField.audienceTypes.clear();
        params.audienceType?.split("\\r?\\n|,").each { l ->
            l = l.trim();
            if(l) {
                AudienceType c = (new XMLConverter()).getAudienceTypeByType(l);
                if(!c) {
                    errors << messageSource.getMessage("info.error.updating.audience", null, LCH.getLocale())
                } else {
                    speciesField.addToAudienceTypes(c);
                }
            }
        }

        //description
        speciesField.description = params.description;
        
        
        log.warn errors
        return [success:true, errors:errors]
    }


    /**
    * Delete species field
    */
    def deleteSpeciesField(long id) {
        SpeciesField speciesField = SpeciesField.get(id);
        
        if(!speciesField) {
            return [success:false, msg:messageSource.getMessage("info.speciesfield.notfound", null, LCH.getLocale())]
        } else if(speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            def speciesInstance = speciesField.species;
            def field = speciesField.field;
            try {
                SpeciesField.withTransaction {
                    speciesInstance.removeFromFields(speciesField);
                    speciesField.delete(failOnError:true);
                }
                //List sameFieldSpeciesFieldInstances =  speciesInstance.fields.findAll { it.field.id == field.id} as List
                //sortAsPerRating(sameFieldSpeciesFieldInstances);
                //return [success:true, msg:"Successfully deleted species field", id:field.id, content:sameFieldSpeciesFieldInstances, speciesId:speciesInstance.id]
                def newSpeciesFieldInstance = createNewSpeciesField(speciesInstance, field, '');
                return [success:true, msg:messageSource.getMessage("info.speciefield.deleted", null, LCH.getLocale()), id:field.id, content:newSpeciesFieldInstance, speciesFieldInstance:speciesField, speciesInstance:speciesInstance, activityType:ActivityFeedService.SPECIES_FIELD_DELETED, activityDesc:ActivityFeedService.SPECIES_FIELD_DELETED+" : "+speciesField.field, mailType:ActivityFeedService.SPECIES_FIELD_DELETED]
            } catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = e.getMessage();
                return [success:false, msg:messageSource.getMessage("info.speciesfield.deleting", messagesourcearg, LCH.getLocale())]
            }
        } else {
            return [success:false, msg:messageSource.getMessage("info.no.permission.delete", null, LCH.getLocale())]
        }
    }

    /**
    * Update methods for individual metadata fields
    */
    def updateContributor(contributorId, long speciesFieldId, def value, String type) {
        
        if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        SUser oldContrib;
        if(contributorId) {
            oldContrib = SUser.read(contributorId);

            if(!oldContrib) {
                 def messagesourcearg = new Object[2];
                messagesourcearg[0] = type.capitalize();
                messagesourcearg[1] = contributorId;
                return [success:false, msg:messageSource.getMessage("info.id.not.found", messagesourcearg, LCH.getLocale())]
            } else if(oldContrib.email == value) {
                return [success:true, msg:messageSource.getMessage("info.nothing.change", null, LCH.getLocale())]
            }
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }

        SpeciesField.withTransaction { status ->
            SUser c = SUser.findByEmail(value);
            if(!c) {
                def messagesourcearg = new Object[2];
                messagesourcearg[0] = type;
                messagesourcearg[1] = value;
                return [success:false, msg:messageSource.getMessage("info.error.no.user", messagesourcearg, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                if(oldContrib)
                    speciesField.removeFromContributors(oldContrib);
                speciesField.addToContributors(c);
                msg = messageSource.getMessage("info.success.adding.contributor", null, LCH.getLocale());
                content = speciesField.contributors;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    def messagesourcearg = new Object[1];
                    messagesourcearg[0] = type;
                    return [success:false, msg:messageSource.getMessage("info.error.while.updating", messagesourcearg, LCH.getLocale())]
                }
                return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
            }
        }
    }

    def updateAttributor(contributorId, long speciesFieldId, def value, String type) {
        
        if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        Contributor oldContrib;
        if(contributorId) {
            oldContrib = Contributor.read(contributorId);

            if(!oldContrib) {
                def messagesourcearg = new Object[2];
                messagesourcearg[0] = type.capitalize();
                messagesourcearg[1] = contributorId;
                return [success:false, msg:messageSource.getMessage("info.id.not.found", messagesourcearg, LCH.getLocale())]
            } else if(oldContrib.name == value) {
                return [success:true, msg:messageSource.getMessage("info.nothing.change", null, LCH.getLocale())]
            }
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }

        SpeciesField.withTransaction { status ->
            Contributor c = (new XMLConverter()).getContributorByName(value, true);
            if(!c) {
                def messagesourcearg = new Object[1];
                    messagesourcearg[0] = type;
                return [success:false, msg:messageSource.getMessage("info.error.while.updating", messagesourcearg, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                   if(oldContrib)
                        speciesField.removeFromAttributors(oldContrib);
                    speciesField.addToAttributors(c);
                    msg = messageSource.getMessage("info.success.adding.attribution", null, LCH.getLocale());
                    content = speciesField.attributors;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }

                    def messagesourcearg = new Object[1];
                    messagesourcearg[0] = type;
                return [success:false, msg:messageSource.getMessage("info.error.while.updating", messagesourcearg, LCH.getLocale())]
                }
                return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
            }
        }
    }

    def updateReference(referenceId, long speciesId, long fieldId, speciesFieldId, def value) {
         if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        if(speciesId && fieldId){
            return updateSpeciesReference(referenceId, speciesId, fieldId, value);
        }
    }


    private def updateSpeciesReference(referenceId, long speciesId, long fieldId, def value) {
        String msg = '';
        def content;
        def count_chk = ['success_count' : 0 ,'failure_count' : 0];
        SpeciesField speciesField,speciesFields;
        Species speciesInstance = Species.get(speciesId);
        if(speciesId && !referenceId){               
            if(!speciesInstance){
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = referenceId;
                return [success:false, msg:messageSource.getMessage("info.reference.id.not.found", messagesourcearg, LCH.getLocale())]
            }

            Field field = Field.read(fieldId);       
            speciesField = SpeciesField.findByFieldAndSpeciesAndDescription(field,speciesInstance,value);
            if(!speciesField){
                speciesField = createNewSpeciesField(speciesInstance, field, "dummy");
            }
            def references = [];        
            value?.split("\\r?\\n").each { l ->
                l = l.trim(); 
                if(l && l.trim()) {                 
                    def chk = is_exist_reference(speciesInstance, l);
                    if(chk){
                        Reference reference;
                        if(l.startsWith("http://")) {
                            reference = new Reference(url:l); 
                        }else{
                            reference = new Reference(title:l); 
                        }   
                        speciesField.addToReferences(reference);
                        references.push(reference);
                        count_chk.success_count =  count_chk.success_count +1;
                    }else{
                        count_chk.failure_count =  count_chk.failure_count +1;
                    }    

                }
            }

            if(!speciesField.save(flush:true)){
                speciesField.errors.allErrors.each { log.error it }
            }
            msg = messageSource.getMessage("info.success.adding.reference", null, LCH.getLocale());
            content = references;
        }else{
            if(!referenceId) {
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = referenceId;
                return [success:false, msg:messageSource.getMessage("info.reference.id.not.found", messagesourcearg, LCH.getLocale())]
            }
            def chk = is_exist_reference(speciesInstance, value); 
            if(!chk){
                return [success:false, msg:messageSource.getMessage("info.reference.id.duplicate.found", null, LCH.getLocale())]

            }
            Reference reference = Reference.get(referenceId);            
            speciesField = reference.speciesField;
            if(value.startsWith("http://")) {
                reference.url= value;
            }else{
                reference.title= value;
            }

            if(!reference.save(flush:true)){
                reference.errors.allErrors.each { log.error it }
            }
            content = reference;
            msg = messageSource.getMessage("info.success.adding.reference", null, LCH.getLocale());
        }

        return [success:true, id:speciesField.id, type:'reference', msg:msg, content:content, count_chk:count_chk]
    }

    private def is_exist_reference(Species speciesInstance, def title){
        SpeciesField speciesFields = SpeciesField.findBySpecies(speciesInstance); 
        def rf_chk;
        for( sf in speciesFields){
            rf_chk = Reference.findAllBySpeciesFieldAndTitle(sf,title);
            if(rf_chk){
                return false;                
            }
        }  
        return true;   
    }

    def addDescription(long speciesId, long fieldId, String value) {
     
        if(!value || !fieldId || !speciesId) {
            return [success:false, msg:messageSource.getMessage("info.cannot.empty", null, LCH.getLocale())]
        }
        XMLConverter converter = new XMLConverter();

        Species speciesInstance = Species.get(speciesId);
        Field field = Field.read(fieldId);

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission.add", null, LCH.getLocale())]
        }

        if(!field) {
            return [success:false, msg:"Invalid field"]
        }
        try {
            SpeciesField speciesFieldInstance = converter.createSpeciesField(speciesInstance, field, value, [springSecurityService.currentUser.email], [], [LicenseType.CC_BY.value()], [SpeciesField.AudienceType.GENERAL_PUBLIC.value()], [SpeciesField.Status.UNDER_VALIDATION.value()]);
            if(speciesFieldInstance) {
                speciesInstance.addToFields(speciesFieldInstance);
                //TODO:make sure this is run in only one user updates this species at a time
                Species.withTransaction {
                    if(!speciesInstance.save()) {
                        speciesInstance.errors.each { log.error it }
                        return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale())]
                    }
                }
                List sameFieldSpeciesFieldInstances =  speciesInstance.fields.findAll { it.field.id == field.id} as List
                sortAsPerRating(sameFieldSpeciesFieldInstances);
                return [success:true, msg:messageSource.getMessage("info.success.update", null, LCH.getLocale()), id:field.id, type:'newdescription', content:sameFieldSpeciesFieldInstances, 'speciesInstance':speciesInstance, speciesId:speciesInstance.id]
            }
        } catch(Exception e) {
            e.printStackTrace();
            return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale())]
        }
        return [success:false, msg:messageSource.getMessage("info.error.adding", null, LCH.getLocale())]
    }

    def updateDescription(long id, String value) {
        
        if(!value || !id) {
            return [success:false, msg:messageSource.getMessage("info.cannot.empty", null, LCH.getLocale())]
        }
        SpeciesField c = SpeciesField.get(id);
        return updateSpeciesFieldDescription(c, value);
    }
    
    def updateSpeciesFieldDescription(SpeciesField c, String value) {
        
        if(!c) {
            return [success:false, msg:messageSource.getMessage("info.speciesfield.notfound", null, LCH.getLocale())]
        } else if(!speciesPermissionService.isSpeciesFieldContributor(c, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        } else {
            SpeciesField.withTransaction {
                c.description = value.trim()
                if (!c.save()) {
                    c.errors.each { log.error it }
                    return [success:false, msg:messageSource.getMessage("info.error.updating.field.name", null, LCH.getLocale())]
                }
            }
            return [success:true, msg:""]
        }
    }

    def updateLicense(long speciesFieldId, def value) {
        
        if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
           def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }

        SpeciesField.withTransaction { status ->
            License c = (new XMLConverter()).getLicenseByType(value, false);
            if(!c) {
                return [success:false, msg:messageSource.getMessage("info.error.updating.license", null, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                speciesField.licenses.clear();
                speciesField.addToLicenses(c);
                msg = messageSource.getMessage("info.success.adding.license", null, LCH.getLocale());
                content = speciesField.licenses;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:messageSource.getMessage("info.error.updating.license", null, LCH.getLocale())]
                }
                return [success:true, id:speciesFieldId, msg:msg, content:content]
            }
        }
    }

    def updateAudienceType(long speciesFieldId, String value) {
        
        if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }


        SpeciesField.withTransaction { status ->
            AudienceType c = (new XMLConverter()).getAudienceTypeByType(value);
            if(!c) {
                return [success:false, msg:messageSource.getMessage("info.error.updating.audience", null, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                speciesField.audienceTypes.clear();
                speciesField.addToAudienceTypes(c);
                msg = messageSource.getMessage("info.success.adding.audience", null, LCH.getLocale());
                content = speciesField.audienceTypes;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:messageSource.getMessage("info.error.updating.audience", null, LCH.getLocale())]
                }
                return [success:true, id:speciesFieldId, msg:msg, content:content]
            }
        }
    }

    def updateStatus(long speciesFieldId, String value) {
        
        if(!value) {
            return [success:false, msg:messageSource.getMessage("info.field.cannot.empty", null, LCH.getLocale())]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
            
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }


        SpeciesField.withTransaction { status ->
            SpeciesField.Status c = getStatus(value);
            if(!c) {
                return [success:false, msg:messageSource.getMessage("info.error.updating.status", null, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                speciesField.status = c;
                msg = messageSource.getMessage("info.success.adding.status", null, LCH.getLocale());
                content = speciesField.status;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:messageSource.getMessage("info.error.updating.status", null, LCH.getLocale())]
                }
                return [success:true, id:speciesFieldId, msg:msg, content:content]
            }
        }
    }

    private Status getStatus(String value) {
        for(Status l : Status){
			if(l.value().equalsIgnoreCase(value))
				return l
		}
    }

    def updateSynonym(def synonymId, def speciesId, String relationship, String value, otherParams = null) {
        println "=====parameters========== " + synonymId +"========== "+ speciesId+ "======== "+relationship +"========= " + value+"============ " + otherParams
        //if(request == null) request = RequestContextHolder.currentRequestAttributes().request
        if(!value || !relationship) {
            return [success:false, msg:messageSource.getMessage("info.synonym.non.empty", null, LCH.getLocale())]
        }
        Species speciesInstance = null; 
        if(!otherParams) {
            speciesInstance = Species.get(speciesId);

            if(!speciesInstance) {
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            	return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
            }
        }

        if(speciesInstance && !speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }
        println "=====2========"
        SynonymsMerged oldSynonym;
        if(synonymId) {
            oldSynonym = SynonymsMerged.read(synonymId);

            if(!oldSynonym) {
        println "=====a========"
                //return [success:false, msg:"Synonym with id ${synonymId} is not found"]
            } else if(oldSynonym.name == value && oldSynonym.relationship.value().equals(relationship)) {
                return [success:true, msg:messageSource.getMessage("info.nothing.change", null, LCH.getLocale())]
            } else if(!oldSynonym.isContributor()) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
            }
        }

        println "=====3========"
        Species.withTransaction { status ->
            TaxonomyDefinition taxonConcept;
            if(otherParams) {
                taxonConcept = TaxonomyDefinition.get(otherParams['taxonId'].toLong()); 
            } else {
                taxonConcept = speciesInstance.taxonConcept;
            }
            if(oldSynonym) {
                def result
                if(otherParams) {
                    println "===========HERE HERE ======= " + oldSynonym +"===== "+ taxonConcept
                    result = deleteSynonym(oldSynonym, null, taxonConcept);
                } else {
                    result = deleteSynonym(oldSynonym, speciesInstance);
                }
                if(!result.success) {
                    def messagesourcearg = new Object[1];
                    messagesourcearg[0] = result.msg;
                    return [success:false, msg:messageSource.getMessage("info.error.updating.synonym", messagesourcearg, LCH.getLocale())]
                }
            } 
            println "====4========="
            XMLConverter converter = new XMLConverter();

            NodeBuilder builder = NodeBuilder.newInstance();
            def synonym = builder.createNode("synonym");
            Node data = new Node(synonym, 'data', value)
            new Node(data, "relationship", relationship);
            def email = (springSecurityService.currentUser)?springSecurityService.currentUser.email:"admin@strandls.com";
            new Node(data, "contributor", email);
            if(otherParams) {
                new Node(data, "viaDatasource", otherParams['source']);
            }
            List<SynonymsMerged> synonyms = converter.createSynonyms(synonym, taxonConcept);
            
            if(!synonyms) {
                return [success:false, msg:messageSource.getMessage("info.error.update.synonym", null, LCH.getLocale())]
            } else {
                synonyms.each {
                    taxonConcept.addSynonym(it);
                }
                String msg = '';
                def content;
                msg = messageSource.getMessage("info.success.update.synonym", null, LCH.getLocale());
                //content = Synonyms.findAllByTaxonConcept(taxonConcept) ;
                content = taxonConcept ? taxonConcept.fetchSynonyms() :  null;
                String activityType, mailType, description;
                if(oldSynonym) {
                    description = ActivityFeedService.SPECIES_SYNONYM_UPDATED+" : "+oldSynonym.name+" changed to "+synonyms[0].name
                    activityType = mailType = ActivityFeedService.SPECIES_SYNONYM_UPDATED
                } else {
                    description =  ActivityFeedService.SPECIES_SYNONYM_CREATED+" : "+synonyms[0].name
                    activityType = mailType = ActivityFeedService.SPECIES_SYNONYM_CREATED
                }
                if(otherParams) {
                    println "========SYNONYMS========== " + synonyms
                    return [success:true,/* id:speciesId,*/ msg:msg, type:'synonym', content:content,taxonConcept:taxonConcept,dataInstance:synonyms[0], activityType:activityType, mailType:mailType, activityDesc:description]  
                }
                return [success:true, id:speciesId, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, activityType:activityType, mailType:mailType, activityDesc:description]
            }
        }
    }

    def updateCommonname(def cnId, def speciesId, String language, String value, otherParams = null) {
        
        if(!value || !language) {
            return [success:false, msg:messageSource.getMessage("info.name.language.no.empty", null, LCH.getLocale())]
        }
        Species speciesInstance = null;

        if(!otherParams) {
            speciesInstance = Species.get(speciesId);

            if(!speciesInstance) {
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesId;
                return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
            }
        }
        if(speciesInstance && !speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }

        CommonNames oldCommonname;
        Language lang = Language.getLanguage(language);
        if(cnId) {
            oldCommonname = CommonNames.read(cnId);

            if(!oldCommonname) {
                //return [success:false, msg:"Commonname with id ${cnId} is not found"]
            } else if(oldCommonname.name == value && oldCommonname.language.equals(lang)) {
                return [success:true, msg:messageSource.getMessage("info.nothing.change", null, LCH.getLocale())]
            } else if(!oldCommonname.isContributor()) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
            }
        }

        Species.withTransaction { status ->
            TaxonomyDefinition taxonConcept;
            if(otherParams) {
                taxonConcept = TaxonomyDefinition.get(otherParams['taxonId'].toLong()); 
            } else {
                taxonConcept = speciesInstance.taxonConcept;
            }

            if(oldCommonname) {
                oldCommonname.delete();
            } 
            XMLConverter converter = new XMLConverter();

            NodeBuilder builder = NodeBuilder.newInstance();
            def cn = builder.createNode("commonname");
            Node data = new Node(cn, 'data', value)
            Node l = new Node(data, "language");
            new Node(l, 'name', language);
            new Node(data, "contributor", springSecurityService.currentUser.email);
            if(otherParams) {
                new Node(data, "viaDatasource", otherParams['source']);
            }
            List<CommonNames> commonnames = converter.createCommonNames(cn, taxonConcept);
            
            if(!commonnames) {
                return [success:false, msg:messageSource.getMessage("info.error.updating.commonname", null, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                msg = messageSource.getMessage("info.succes.update.commonname", null, LCH.getLocale());
                content = CommonNames.findAllByTaxonConcept(taxonConcept) ;
                String activityType, mailType, description;
                if(oldCommonname) {
                    description = ActivityFeedService.SPECIES_COMMONNAME_UPDATED+" : "+oldCommonname.name+" changed to "+commonnames[0].name
                    mailType =  activityType = ActivityFeedService.SPECIES_COMMONNAME_UPDATED
                } else {
                    description = ActivityFeedService.SPECIES_COMMONNAME_CREATED+" : "+commonnames[0].name
                    mailType = activityType =  ActivityFeedService.SPECIES_COMMONNAME_CREATED
                }
                if(otherParams) {
                    println "========COMMON NAME========== " + commonnames
                    return [success:true,/* id:speciesId,*/ msg:msg, type:'commonname', content:content,taxonConcept:taxonConcept,dataInstance:commonnames[0], activityType:activityType, mailType:mailType, activityDesc:description]  
                }

                return [success:true, id:speciesId, msg:msg, type:'commonname', content:content, speciesInstance:speciesInstance, activityType:activityType, mailType :mailType, activityDesc:description]
            }
        }
    }

    /**
    * Delete methods for individual metadata fields
    */
    def deleteContributor(contributorId, long speciesFieldId, String type) {
        
        SUser oldContrib;
        if(contributorId) {
            oldContrib = SUser.read(contributorId);
        }
        if(!oldContrib) {
             def messagesourcearg = new Object[2];
                messagesourcearg[0] = type.capitalize();
                messagesourcearg[1] = contributorId;
                return [success:false, msg:messageSource.getMessage("info.id.not.found", messagesourcearg, LCH.getLocale())]
            
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
           def messagesourcearg = new Object[1];
                messagesourcearg[0] = type;
            return [success:false, msg:messageSource.getMessage("info.no.permission.to.delete", messagesourcearg, LCH.getLocale())]

        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromContributors(oldContrib);
            if(speciesField.contributors.size() == 0) {
                msg = messageSource.getMessage("info.atleast.one.contributor", null, LCH.getLocale());
                return [success:false, msg:msg]
            } else {
                msg = messageSource.getMessage("info.success.removed.contributor", null, LCH.getLocale());
                content = speciesField.contributors;
            }
            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                def messagesourcearg = new Object[1];
                    messagesourcearg[0] = type;
                    return [success:false, msg:messageSource.getMessage("info.error.while.updating", messagesourcearg, LCH.getLocale())]
            }
            return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
        }
    }

    def deleteAttributor(contributorId, long speciesFieldId, String type) {
        
        Contributor oldContrib;
        if(contributorId) {
            oldContrib = Contributor.read(contributorId);
        }
        if(!oldContrib) {
           def messagesourcearg = new Object[2];
                messagesourcearg[0] = type.capitalize();
                messagesourcearg[1] = contributorId;
                return [success:false, msg:messageSource.getMessage("info.id.not.found", messagesourcearg, LCH.getLocale())]
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = type;
            return [success:false, msg:messageSource.getMessage("info.no.permission.to.delete", messagesourcearg, LCH.getLocale())]
        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromAttributors(oldContrib);
            msg = messageSource.getMessage("info.success.removed.attribution", null, LCH.getLocale());
            content = speciesField.attributors;

            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
               def messagesourcearg = new Object[1];
                    messagesourcearg[0] = type;
                    return [success:false, msg:messageSource.getMessage("info.error.while.updating", messagesourcearg, LCH.getLocale())]
            }
            return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
        }
    }

    def deleteReference(referenceId, long speciesFieldId) {
        
        Reference oldReference;
        if(referenceId) {
            oldReference = Reference.read(referenceId);
        }
        if(!oldReference) {
            def messagesourcearg = new Object[1];
                    messagesourcearg[0] = referenceId;
                return [success:false, msg:messageSource.getMessage("info.reference.id.not.found", messagesourcearg, LCH.getLocale())]
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
           def messagesourcearg = new Object[1];
                messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }
        
        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission.delete.reference", null, LCH.getLocale())]
        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromReferences(oldReference);
            msg = messageSource.getMessage("info.success.remove.reference", null, LCH.getLocale());
            content = speciesField.references;

            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                return [success:false, msg:messageSource.getMessage("info.error.updating.reference", null, LCH.getLocale())]
            }
            return [success:true, id:speciesFieldId, type:'reference', msg:msg, content:content]
        }
    }

    def deleteDescription(long id) {
        return deleteSpeciesField(id);
    }

    def deleteSynonym(long synonymId, def speciesId = null, def taxonId = null) {
        SynonymsMerged oldSynonym;
        if(synonymId) {
            oldSynonym = SynonymsMerged.read(synonymId);
        }
        Species speciesInstance = null;
        if(speciesId)
            speciesInstance = Species.get(speciesId);

        TaxonomyDefinition taxonConcept = null;
        if(taxonId)
            taxonConcept = TaxonomyDefinition.get(taxonId);

        return deleteSynonym(oldSynonym, speciesInstance, taxonConcept);
    }
    
    def deleteSynonym(SynonymsMerged oldSynonym, Species speciesInstance = null, TaxonomyDefinition taxonConcept = null) {
       println oldSynonym; 
        if(!oldSynonym) {
            def messagesourcearg = new Object[1];
            messagesourcearg[0] = oldSynonym.id;
            return [success:false, msg:messageSource.getMessage("info.synonym.id.not.found", messagesourcearg, LCH.getLocale())]
        } 

        if(speciesInstance) {
            if(!oldSynonym.isContributor()) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
            }

            if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.delete.synonym", null, LCH.getLocale())]
            }
        }

        SynonymsMerged.withTransaction { status ->
            String msg = '';
            def content;
            try{
                oldSynonym.removeFromContributors(springSecurityService.currentUser);
                taxonConcept = speciesInstance ? speciesInstance.taxonConcept : oldSynonym.taxonConcept;
                taxonConcept.removeSynonym(oldSynonym);
                if(oldSynonym.contributors.size() == 0) {
                    oldSynonym.delete(failOnError:true) //should not delete synonym entry
                } else {
                    if(!oldSynonym.save()) {
                        oldSynonym.errors.each { log.error it }
                        return [success:false, msg:messageSource.getMessage("info.error.deleting.synonym", null, LCH.getLocale())]
                    }
                }
                msg = messageSource.getMessage("info.success.remove.synonym", null, LCH.getLocale());
                //content = taxonConcept ? Synonyms.findAllByTaxonConcept(taxonConcept) :  null;
                content = taxonConcept ? taxonConcept.fetchSynonyms() :  null;
                return [success:true, id:speciesInstance?.id, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, taxonConcept:taxonConcept, activityType:ActivityFeedService.SPECIES_SYNONYM_DELETED, activityDesc:ActivityFeedService.SPECIES_SYNONYM_DELETED+" : "+oldSynonym.name, mailType:ActivityFeedService.SPECIES_SYNONYM_DELETED]
            } 
            catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = e.getMessage();
                return [success:false, msg:messageSource.getMessage("info.error.synonym.deletion", messagesourcearg, LCH.getLocale())]
            }
        }
    }
  
    //taxonId comes from curation interface only 
    def deleteCommonname(def cnId, def speciesId = null, def taxonId = null) {
        CommonNames oldCommonname;
        if(cnId) {
            oldCommonname = CommonNames.read(cnId);
        }

        Species speciesInstance = (speciesId) ? Species.get(speciesId):null;
        TaxonomyDefinition taxonConcept = (taxonId)?TaxonomyDefinition.get(taxonId.toLong()):null;
        return deleteCommonname(oldCommonname, speciesInstance, taxonConcept);
    }

    //taxonConcept comes from curation interface only 
    def deleteCommonname(CommonNames oldCommonname, Species speciesInstance = null, TaxonomyDefinition taxonConcept = null) {
        if(!taxonConcept) taxonConcept = speciesInstance?.taxonConcept
        if(!oldCommonname) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = cnId;
            return [success:false, msg:messageSource.getMessage("info.common.name.id.not.found", messagesourcearg, LCH.getLocale())]
        }
        //Permission check only if its from species show page
        if(speciesInstance) {
            if(!oldCommonname.isContributor()) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
            }

            if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.delete.commonname", null, LCH.getLocale())]
            }
        }
        CommonNames.withTransaction { status ->
            String msg = '';
            def content;
            try{
                oldCommonname.removeFromContributors(springSecurityService.currentUser);
                
                if(oldCommonname.contributors.size() == 0) {
                    oldCommonname.delete(failOnError:true)
                } else {
                    if(!oldCommonname.save()) {
                        oldCommonname.errors.each { log.error it }
                        return [success:false, msg:messageSource.getMessage("info.error.deleting.commonname", null, LCH.getLocale())]
                    }
                }

                msg = messageSource.getMessage("info.success.remove.commonname", null, LCH.getLocale());
                content = CommonNames.findAllByTaxonConcept(taxonConcept) ;
                if(speciesInstance) {
                    return [success:true, id:speciesInstance.id, msg:msg, type:'commonname', content:content, speciesInstance:speciesInstance,activityType:ActivityFeedService.SPECIES_COMMONNAME_DELETED, activityDesc:ActivityFeedService.SPECIES_COMMONNAME_DELETED+" : "+oldCommonname.name, mailType:ActivityFeedService.SPECIES_COMMONNAME_DELETED]
                } else {
                    return [success:true, msg:msg, type:'commonname', content:content, taxonConcept:taxonConcept, activityType:ActivityFeedService.SPECIES_COMMONNAME_DELETED, activityDesc:ActivityFeedService.SPECIES_COMMONNAME_DELETED+" : "+oldCommonname.name, mailType:ActivityFeedService.SPECIES_COMMONNAME_DELETED]
                }
            } 
            catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = e.getMessage();
                return [success:false, msg:messageSource.getMessage("info.error.commonname.deletion", messagesourcearg, LCH.getLocale())]
            }
        }
    }
    
    /**
    * Create Species given species name and atleast one taxon hierarchy
    */
    def createSpecies(String speciesName, int rank, List taxonRegistryNames, Language language) {
        
        def speciesInstance = new Species();
        List<TaxonomyRegistry> taxonRegistry;
        List errors = [];
        Map result = [requestParams:[speciesName:speciesName, rank:rank, taxonRegistryNames:taxonRegistryNames], errors:errors];

        XMLConverter converter = new XMLConverter();
        speciesInstance.taxonConcept = converter.getTaxonConceptFromName(speciesName, rank);
		 if(speciesInstance.taxonConcept) {
			speciesInstance.title = speciesInstance.taxonConcept.italicisedForm;
            //taxonconcept is being used as guid
            speciesInstance.guid = converter.constructGUID(speciesInstance);

            //a species page with guid as taxon concept is considered as duplicate
            Species existingSpecies = converter.findDuplicateSpecies(speciesInstance);
            if(existingSpecies) {
				existingSpecies.clearBasicContent()
                speciesInstance = existingSpecies;
            }

            if(!taxonService.validateHierarchy(taxonRegistryNames)) {
                if(!speciesInstance.fetchTaxonomyRegistry()) {
                    result['success'] = false;
                    result['msg'] = messageSource.getMessage("info.message.missing", null, LCH.getLocale())
                    return result
                }
                result['success'] = false;
                result['msg'] = messageSource.getMessage("info.message.missing", null, LCH.getLocale())
                return result
            }

            Classification classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
            //CHK if current user has permission to add details to the species
            if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
                def taxonRegistryNodes = converter.createTaxonRegistryNodes(taxonRegistryNames, classification.name, springSecurityService.currentUser, language);

                List<TaxonomyRegistry> tR = converter.getClassifications(taxonRegistryNodes, speciesName, false).taxonRegistry;
                def tD = tR.taxonDefinition
                if(!speciesPermissionService.isTaxonContributor(tD, springSecurityService.currentUser)) {
                    result['success'] = false;
                    result['status'] = 'requirePermission';
                    result['msg'] = 'Please request for permission to contribute.'
                    //result['errors'] = errors
                    return result
                }
            }

            //save taxonomy hierarchy
            Map result1 = taxonService.addTaxonHierarchy(speciesName, taxonRegistryNames, classification, springSecurityService.currentUser, language); 
            result.putAll(result1);
            result.speciesInstance = speciesInstance;
			speciesInstance.taxonConcept.postProcess()
			result.taxonRegistry = taxonRegistry;
            result.errors.addAll(errors);
			
        }
       return result;
    }

    /**
    * Create resources XML
    */
    private def createImagesXML(params) {
        NodeBuilder builder = NodeBuilder.newInstance();
        XMLConverter converter = new XMLConverter();
        def resources = builder.createNode("resources");
        Node images = new Node(resources, "images");
        List files = [];
        List titles = [];
        List licenses = [];
        params.each { key, val ->
            int index = -1;
            if(key.startsWith('file_')) {
                index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));

            }
            if(index != -1) {
                files.add(val);
                titles.add(params.get('title_'+index));
                licenses.add(params.get('license_'+index));
            }
        }
        files.eachWithIndex { file, key ->
            Node image = new Node(images, "image");
            if(file) {
                File f = new File(uploadDir, file);
                new Node(image, "fileName", f.absolutePath);
                //new Node(image, "source", imageData.get("source"));
                new Node(image, "caption", titles.getAt(key));
                new Node(image, "contributor", params.author.username);
                new Node(image, "license", licenses.getAt(key));
            } else {
                log.warn("No reference key for image : "+key);
            }
        }
        return resources;
    }

    private def createVideoXML(params) {
        NodeBuilder builder = NodeBuilder.newInstance();
        XMLConverter converter = new XMLConverter();
        def resources = builder.createNode("resources");
        Node videos = new Node(resources, "videos");

        Node video = new Node(videos, "video");
        new Node(video, 'fileName', 'video')
        new Node(video, "source", params.video);
        new Node(video, "caption", params.description);
        new Node(video, "contributor", springSecurityService.currentUser.name);
        new Node(video, "attributor", params.attributor);
        new Node(video, "license", "CC BY");

        return resources;
    }

    private List<Resource> saveResources(Node resourcesXML, String relImagesContext) {
        XMLConverter converter = new XMLConverter();
        converter.setResourcesRootDir(grailsApplication.config.speciesPortal.resources.rootDir);
        return converter.createMedia(resourcesXML, relImagesContext);
    }

    ///////////////////////////////////////////////////////////////////////
    /////////////////////////////// Export ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /*def requestExport(params){
        log.debug(params)
        log.debug "creating species download request"
        DownloadLog.createLog(springSecurityService.currentUser, params.filterUrl, params.downloadType, params.notes, params.source, params)
    }*/

    def export(params, dl){
/*        String action = new URL(dl.filterUrl).getPath().split("/")[2]
        def speciesInstanceList = getSpeciesList(params, action).speciesInstanceList
        log.debug " Species total $speciesInstanceList.size "
        List<String> list_final=[] ;


        speciesInstanceList.each {
            println it 
            def it_next= it as JSON ; 
            def it_final=JSON.parse(""+it_next)
            list_final.add(it_final)

        }
*/
          return DwCSpeciesExporter.getInstance().exportSpecieData( null, dl, params.webaddress)
        //return exportSpeciesData(speciesInstanceList, null, dl)

    }

    def getSpeciesList(params, String action){
        if(Utils.isSearchAction(params, action)){
            return search(params)
        }else{
            return _getSpeciesList(params)
        }
    }

    /**
     * export species data
     */
/*    private def exportSpeciesData(String directory) {
        return DwCAExporter.getInstance().exportSpeciesData(directory)
    } 
*/
    /**
     * export species data
     */
    private def exportSpeciesData(List<Species> species, String directory, DownloadLog dl) {
		if(!species || species.isEmpty())
			return null
		
		File downloadDir = new File(directory?:grailsApplication.config.speciesPortal.species.speciesDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		log.debug "export type " + exportType 
		if(exportType == DownloadLog.DownloadType.DWCA) {
            return DwCAExporter.getInstance().exportSpeciesData(species, directory)
        } else {
            log.warn "Not a valid export type"
        }
    }

    /**
    * get species list query
    */
    def _getSpeciesListQuery(params) {
        params.startsWith = params.startsWith?:"A-Z"
        def allGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL);
        def othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS);
        params.sGroup = params.sGroup ?: allGroup.id+""
        
        int count = 0;
        String query, countQuery;
        String filterQuery = " where s.id is not null " //dummy statement
        String countFilterQuery = " where s.id is not null " //dummy statement
        String speciesCountQuery = "select s.taxonConcept.rank, count(*) as count from Species s "
        String speciesCountFilterQuery = '';
        String speciesStatusCountQuery = "select s.taxonConcept.status, count(*) as count from Species s "
        String speciesStatusCountFilterQuery = '';


        def queryParams = [:]
        def activeFilters = [:]
        queryParams.max = Math.min(params.max ? params.max.toInteger() : 40, 100);
        queryParams.offset = params.offset ? params.offset.toInteger() : 0

        if(queryParams.max < 0 ) {
            queryParams.max = 40 
        }

        if(queryParams.offset < 0) {
            queryParams.offset = 0
        }

        queryParams.sort = params.sort?:"lastrevised"
        if(queryParams.sort.equals('lastrevised') || queryParams.sort.equals('lastupdated')) {
            queryParams.sort = 'lastUpdated'

        } else if(queryParams.sort.equals('percentofinfo') || queryParams.sort.equals('score')) {
            queryParams.sort = 'percentOfInfo'
        }
        queryParams.order = (queryParams.sort.equals("percentOfInfo")||queryParams.sort.equals("lastUpdated"))?"desc":queryParams.sort.equals("title")?"asc":"asc"

        def groupIds = params.sGroup.tokenize(',')?.collect {Long.parseLong(it)}

        if(groupIds.size() == 1 && groupIds[0] == allGroup.id) {
            if(params.startsWith == "A-Z") {
                query = "select s from Species s ";
                countQuery = "select s.percentOfInfo, count(*) as count from Species s "

            } else {
                query = "select s from Species s "
                filterQuery += " and s.title like '<i>${params.startsWith}%' ";
                countQuery = "select s.percentOfInfo, count(*) as count from Species s "
                countFilterQuery += " and s.title like '<i>${params.startsWith}%' "


				queryParams["startsWith"] = params.startsWith
            }
        } else if(groupIds.size() == 1 && groupIds[0] == othersGroup.id) {
            if(params.startsWith == "A-Z") {
                query = "select s from Species s, TaxonomyDefinition t " 
                filterQuery += " and s.taxonConcept = t and t.group.id  is null "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.taxonConcept = t and t.group.id  is null ";
                speciesCountQuery = "select s.taxonConcept.rank, count(*) as count from Species s, TaxonomyDefinition t "
                speciesStatusCountQuery = "select s.taxonConcept.status, count(*) as count from Species s, TaxonomyDefinition t "
            } else {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  is null "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  is null ";
                speciesCountQuery = "select s.taxonConcept.rank, count(*) as count from Species s, TaxonomyDefinition t "
                speciesStatusCountQuery = "select s.taxonConcept.status, count(*) as count from Species s, TaxonomyDefinition t "
				queryParams["startsWith"] = params.startsWith
            }
            queryParams['sGroup']  = groupIds
            queryParams['groupId']  = groupIds[0]
        } else {
            if(params.startsWith == "A-Z") {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and s.taxonConcept = t and t.group.id  in (:sGroup) "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.taxonConcept = t and t.group.id  in (:sGroup)  ";

                speciesCountQuery = "select s.taxonConcept.rank, count(*) as count from Species s, TaxonomyDefinition t "
                speciesStatusCountQuery = "select s.taxonConcept.status, count(*) as count from Species s, TaxonomyDefinition t "

            } else {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup) "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup)  ";

                speciesCountQuery = "select s.taxonConcept.rank, count(*) as count from Species s, TaxonomyDefinition t "
                speciesStatusCountQuery = "select s.taxonConcept.status, count(*) as count from Species s, TaxonomyDefinition t "
				queryParams["startsWith"] = params.startsWith
            }
            queryParams['sGroup']  = groupIds
            queryParams['groupId']  = groupIds[0]
        }

        if(params.featureBy == "true" ) {
            params.userGroup = utilsService.getUserGroup(params)
            // def featureQuery = ", Featured feat "
            //query += featureQuery;
            //countQuery += featureQuery
            if(params.userGroup == null) {
                def featureQuery = " and s.featureCount > 0"
                countFilterQuery += featureQuery
                filterQuery += featureQuery
                //String str = "feat.userGroup is null "
                //filterQuery += str
                //countFilterQuery += str
            }else {
                String featureQuery = ", Featured feat "
                query += featureQuery
                countQuery += featureQuery
                String str = " and s.id = feat.objectId and feat.objectType =:featType and feat.userGroup.id = :userGroupId "
                filterQuery += str
                countFilterQuery += str
                queryParams["userGroupId"] = params.userGroup?.id
            }   
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Species.class.getCanonicalName();
        }

        if(params.hasMedia) {
            switch(params.hasMedia) {
                case "true" :
                    filterQuery += " and s.hasMedia = true "            
                    countFilterQuery += " and s.hasMedia = true "            
                break
                case "false" :
                    filterQuery += " and s.hasMedia = false "            
                    countFilterQuery += " and s.hasMedia = false "
                break
                default:
                break
            }
        }

        if(params.daterangepicker_start && params.daterangepicker_end){
			def startDate = DATE_FORMAT.parse(URLDecoder.decode(params.daterangepicker_start))
            def endDate = DATE_FORMAT.parse(URLDecoder.decode(params.daterangepicker_end))
            filterQuery += " and ( last_updated between :daterangepicker_start and :daterangepicker_end) "
            countFilterQuery += " and ( last_updated between :daterangepicker_start and :daterangepicker_end) "
            queryParams["daterangepicker_start"] =  startDate   
            queryParams["daterangepicker_end"] =  endDate
        }

        if(params.webaddress) {
            def userGroupInstance = UserGroup.findByWebaddress(params.webaddress)
            if(userGroupInstance){
                queryParams['userGroup'] = userGroupInstance
                query += " join s.userGroups userGroup "
                filterQuery += " and userGroup=:userGroup "
                countQuery += " join s.userGroups userGroup "
                countFilterQuery += " and userGroup=:userGroup "
                speciesCountQuery += " join s.userGroups userGroup "
                speciesStatusCountQuery += " join s.userGroups userGroup "
            }
        }

        if(params.taxon) {
            def taxon = TaxonomyDefinition.read(Long.parseLong(params.taxon))
            if(taxon){
                queryParams['taxon'] = taxon.id
                activeFilters['taxon'] = taxon.id
                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification))
                if(!classification)
                    classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

                queryParams['classification'] = classification.id 
                activeFilters['classification'] = classification.id
                query += " join s.taxonConcept.hierarchies as reg "
                filterQuery += " and reg.classification.id=:classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!')";
                countQuery += " join s.taxonConcept.hierarchies as reg "
                countFilterQuery += " and reg.classification.id=:classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!')";

                speciesCountQuery += " join s.taxonConcept.hierarchies as reg "
                speciesStatusCountQuery += " join s.taxonConcept.hierarchies as reg "
                
            }
        }

        if(params.taxonRank) {
            queryParams['taxonRank'] = Integer.parseInt(params.taxonRank)
            activeFilters['taxonRank'] = queryParams['taxonRank']
            filterQuery += " and s.taxonConcept.rank=:taxonRank";
            countFilterQuery += " and s.taxonConcept.rank=:taxonRank";
        }

        if(params.status) {
            def st;
            NameStatus.list().each { s ->
                if(s.value().equalsIgnoreCase(params.status)) {
                    st = s;
                    return
                }
            }
            queryParams['status'] = st;
            activeFilters['status'] = st;
            filterQuery += " and s.taxonConcept.status=:status";
            countFilterQuery += " and s.taxonConcept.status=:status";
        }

//		XXX: to be corrected		
//		if(params.user){
//			def userInstance = params.user.toLong()
//			if(userInstance){
//				queryParams['user'] = userInstance
//				query += " join s.fields as f "
//				filterQuery += " and f.uploader.id=:user "
//				countQuery += " join s.fields as f "
//				countFilterQuery += " and f.uploader.id=:user "
//			}
//		}

        query += filterQuery + " order by s.${queryParams.sort} ${queryParams.order}"

        speciesCountFilterQuery = countFilterQuery +" group by s.taxonConcept.rank having s.taxonConcept.rank in :ranks ";
        queryParams['ranks'] = [TaxonomyRank.SPECIES.ordinal(), TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()]

        speciesStatusCountFilterQuery = countFilterQuery +" group by s.taxonConcept.status having s.taxonConcept.status in :statuses ";
        queryParams['statuses'] = [NameStatus.ACCEPTED, NameStatus.SYNONYM]

        speciesCountQuery = speciesCountQuery + speciesCountFilterQuery
        speciesStatusCountQuery = speciesStatusCountQuery + speciesStatusCountFilterQuery
		
        countQuery += countFilterQuery + " group by s.percentOfInfo"

        return [query:query, countQuery:countQuery, speciesCountQuery:speciesCountQuery,  speciesStatusCountQuery:speciesStatusCountQuery, queryParams:queryParams]


    }

    /**
    * get species list 
    */
    private _getSpeciesList(params) {
        //cache "taxonomy_results"
        def queryParts = _getSpeciesListQuery(params)
        println queryParts
        def hqlQuery = sessionFactory.currentSession.createQuery(queryParts.query)
        def hqlCountQuery = sessionFactory.currentSession.createQuery(queryParts.countQuery)
        def hqlSpeciesCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesCountQuery)
        def hqlSpeciesStatusCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesStatusCountQuery)

        def queryParams = queryParts.queryParams
        if(queryParams.max > -1){
            hqlQuery.setMaxResults(queryParams.max);
        }
        if(queryParams.offset > -1) {
            hqlQuery.setFirstResult(queryParams.offset);
        } 
        hqlQuery.setProperties(queryParams);
        hqlCountQuery.setProperties(queryParams);
        hqlSpeciesCountQuery.setProperties(queryParams);
        hqlSpeciesStatusCountQuery.setProperties(queryParams);
        
        log.debug "Species list query :${queryParts.query} with params ${queryParams}"
        def speciesInstanceList = hqlQuery.list();

        log.debug "Species list count query :${queryParts.countQuery} with params ${queryParams}"
        def rs = hqlCountQuery.list();

        def speciesCountWithContent = 0;
        int count = 0
        for(c in rs) {
            count += c[1];
            if (c[0] >0)
                speciesCountWithContent += c[1];
        }
        if(params.daterangepicker_start){
            queryParts.queryParams["daterangepicker_start"] = params.daterangepicker_start
        }
        if(params.daterangepicker_end){
            queryParts.queryParams["daterangepicker_end"] =  params.daterangepicker_end
        }

        log.debug "No of species count query :${queryParts.speciesCountQuery} with params ${queryParams}"
        def speciesCounts = hqlSpeciesCountQuery.list();
        def speciesCount;
        def subSpeciesCount;
        speciesCounts.each {s->
            if(s[0]==TaxonomyRank.SPECIES.ordinal()) 
                speciesCount = s[1];
            else if(s[0]==TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()) 
                subSpeciesCount = s[1];

        }

        log.debug "No of species status count query :${queryParts.speciesStatusCountQuery} with params ${queryParams}"
        def speciesStatusCounts = hqlSpeciesStatusCountQuery.list();
        def acceptedSpeciesCount;
        def synonymSpeciesCount;
        speciesStatusCounts.each { s ->
            if(s[0] == NameStatus.ACCEPTED) acceptedSpeciesCount = s[1]
            else if(s[0] == NameStatus.SYNONYM) synonymSpeciesCount = s[1]
        }



        return [speciesInstanceList: speciesInstanceList, instanceTotal: count, speciesCountWithContent:speciesCountWithContent, speciesCount:speciesCount, subSpeciesCount:subSpeciesCount, acceptedSpeciesCount:acceptedSpeciesCount, synonymSpeciesCount:synonymSpeciesCount, 'userGroupWebaddress':params.webaddress, queryParams: queryParams]
        //else {
        //Not being used for now
        //return [speciesInstanceList: Species.list(params), instanceTotal: Species.count(),  'userGroupWebaddress':params.webaddress]
        //}
    }

    /**
    *
    */
    private sortAsPerRating(List fields) {
        if(!fields) return;
        fields.sort( { a, b -> 
            if (a.averageRating < b.averageRating) {
                return -1;
            } else if (a.averageRating > b.averageRating) {
                return 1;
            } else {
                return a.lastUpdated <=> b.lastUpdated;
            }
        } as Comparator )
    } 

    /**
    *
    */
    boolean hasContent(speciesFieldInstances) {
        if(!speciesFieldInstances) return false;
        if(speciesFieldInstances.instanceOf(SpeciesField)) {
            if(speciesFieldInstances.description)
                return true;
        }
		for(speciesFieldInstance in speciesFieldInstances) {
			if(speciesFieldInstance.description) {
				return true
			}
		}
		return false;
	}
    
    def updateSpecies(params, species){
        def resources = []
        def speciesRes = species.resources
        if(params.resourceListType == "ofSpecies" || params.resourceListType == "fromSingleSpeciesField"){
            def resourcesXML = createResourcesXML(params);
            resources = saveResources(species, resourcesXML);
            resources.each{
                //if(it){
                  //  it.refresh()
                //}
                /*
                if(!it.save(flush:true)){
                    it.errors.allErrors.each { log.error it }
                    return false
                }*/
            }
            def resourcesFileName = resources.collect{it.fileName}
            params.each { key, val ->
                if(key.startsWith('file_')) {
                    if(!resourcesFileName.contains(params.get(key))){
                        def res = Resource.findByFileNameAndType(params.get(key), ResourceType.IMAGE);
                        res?.refresh()
                        if(res && !resources.contains(res)){
                            resources.add(res)
                        }
                    }
                }
            }
            species.resources?.clear();
        }
        else if(params.resourceListType == "fromRelatedObv" || params.resourceListType == "fromSpeciesField"){
            def resId = []
            def captions = []
            params.each { key, val ->
                int index = -1;
                if(key.startsWith('pullImage_')) {
                    index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));
                }
                if(index != -1) {
                    resId.add(params.get('resId_'+index));
                    captions.add(params.get('title_'+index))
                }
            }
            int index = 0;
            resId.each{
                def r = Resource.get(it.toLong())
                r.description = captions[index]
                index++;
                if(speciesRes && !speciesRes.contains(r)){    
                    resources.add(r)
                } else if (!speciesRes){
                    resources.add(r)
                }
            }

            if(params.resourceListType == "fromRelatedObv"){
                resId.each{
                    def rid = it
                    def obv = Observation.withCriteria(){
                        resource{
                            eq("id", rid.toLong())
                        }
                    }
                    if(obv.size() == 1 ){
                        def obvIns = obv.get(0)
                        if(obvIns.isLocked == false){
                            obvIns.isLocked = true
                        }
                        if(!obvIns.save(flush:true)){
                            obvIns.errors.allErrors.each { log.error it } 
                        }
                    }
                }
            }
        }
        //species.refresh();
        resources.each { resource ->
            if(params.resourceListType == "ofSpecies" || params.resourceListType == "fromSingleSpeciesField") {
                if(!resource.save(flush:true)){
                    resource.errors.allErrors.each { log.error it }
                }
                if(!resource.context){
                    resource.saveResourceContext(species)
                }
            }
            species.addToResources(resource);
        }
        //species.merge();
        if(resources.size() > 0) {
            if(species.instanceOf(Species)) {
                species.updateHasMediaValue(true)
            }
            if(species.instanceOf(SpeciesField)) {
                species.species.updateHasMediaValue(true)
            }
        } else {
            if(species.instanceOf(Species)) {
                species.updateHasMediaValue(false)
            }
            if(species.instanceOf(SpeciesField)) {
                species.species.updateHasMediaValue(false)
            }
        }

        if(!species.save(flush:true)){
            species.errors.allErrors.each { log.error it }
            return false
        }
        if(species.instanceOf(Species)) {
            def otherParams = [:]
            def resURLs = []
            resources.each {
                def basePath = '';
                if(it?.context?.value() == Resource.ResourceContext.OBSERVATION.toString()){
                    basePath = grailsApplication.config.speciesPortal.observations.serverURL
                }
                else if(it?.context?.value() == Resource.ResourceContext.SPECIES.toString() || it?.context?.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
                    basePath = grailsApplication.config.speciesPortal.resources.serverURL
                }
                def imagePath = '';
                imagePath = it.thumbnailUrl(basePath);
                //URL encoding required
                imagePath = imagePath.replaceAll(' ','%20');
                resURLs.add(imagePath);
            }
            otherParams['resURLs'] = resURLs
            otherParams['spId'] = species.id
            //ADD FEED AND SEND
            def feedInstance = activityFeedService.addActivityFeed(species, species, springSecurityService.currentUser, ActivityFeedService.SPECIES_UPDATED);
            utilsService.sendNotificationMail(ActivityFeedService.SPECIES_UPDATED, species, null, "", feedInstance, otherParams);
        }
        return true
    }
    
     def getLatestUpdatedSpecies(webaddress, sortBy, max, offset ){
        def p = [:]
        p.webaddress = webaddress
        p.sort = sortBy
        p.max = max.toInteger()
        p.offset = offset.toInteger()
        def result = _getSpeciesList(p).speciesInstanceList
        def res = []
        result.each{
            res.add(["observation":it, 'title':it.title])
        }
        return ['observations':res]
    }

    def getSpeciesFieldMedia(spFieldId){
        def spF = SpeciesField.get(spFieldId.toLong())
        return spF?spF.resources:[]
    }

    def addMediaInSpField(params, speciesField){
        if(params.runForImages == "true"){
            def paramsForObvSpField = params.paramsForObvSpField?JSON.parse(params.paramsForObvSpField):null
            def paramsForUploadSpField =  params.paramsForUploadSpField?JSON.parse(params.paramsForUploadSpField):null
            Map<String, String> p1 = new HashMap<String, String>();
            Iterator<String> keysItr1 = paramsForObvSpField.keys();
            while(keysItr1.hasNext())
            {
                String key = keysItr1.next();
                String value = paramsForObvSpField.get(key);
                p1.put(key, value);
            }
                
            Map<String, String> p2 = new HashMap<String, String>();
            Iterator<String> keysItr2 = paramsForUploadSpField.keys();
            while(keysItr2.hasNext())
            {
                String key = keysItr2.next();
                String value = paramsForUploadSpField.get(key);
                p2.put(key, value);
            }
            p1.locale_language = params.locale_language
            p2.locale_language = params.locale_language
            def out2 = updateSpecies(p2, speciesField)
            def out1 = updateSpecies(p1, speciesField)
        }
    }

def checking(){
    Field field = Field.read(81L);
    
    int limit = 500, offset = 0, insert_check = 0,exist_check =0;
    while(true){
     println "offset=================="+offset +"==========================limit"+limit;  
    def sf = SpeciesField.createCriteria()  
    def speciesFieldInstancesList = sf.list (max: limit , offset:offset) {        
            eq("field", field)             
    }  
   
    for ( speciesFieldInstances in speciesFieldInstancesList ) {
         SpeciesField.withNewTransaction{
            if(!speciesFieldInstances?.description){
                speciesFieldInstances.description = "dummy";
                speciesFieldInstances.save();                
            }else{
                Reference reference = Reference.findBySpeciesField(speciesFieldInstances);                            
                if(!reference){
                     speciesFieldInstances?.description?.split("\\r?\\n").each { l ->
                        l = l.trim(); 
                       if(l && l != "dummy" ) {
                            println "SpecieField ID ====="+speciesFieldInstances.id;
                            speciesFieldInstances.addToReferences(new Reference(title:l.trim()));
                            speciesFieldInstances.description = "dummy";
                            speciesFieldInstances.save();                            
                            insert_check+= 1;
                            println "Inserted "+insert_check; 
                        }else{
                            println "NOT Inserted"; 
                        }
                    }                      
                }else{
                    println "Passed Existed!"
                }
            }
        }
    }
    offset = offset+limit; 
    utilsService.cleanUpGorm(true); 
    if(!speciesFieldInstancesList) break;  
    } 

   return "Passed!" 
}

    def updateSynonymOld(def synonymId, def speciesId, String relationship, String value) {
        if(!value || !relationship) {
            return [success:false, msg:messageSource.getMessage("info.synonym.non.empty", null, LCH.getLocale())]
        }
        Species speciesInstance = Species.get(speciesId);

        if(!speciesInstance) {
            def messagesourcearg = new Object[1];
            messagesourcearg[0] = speciesFieldId;
            return [success:false, msg:messageSource.getMessage("info.fieldid.not.found", messagesourcearg, LCH.getLocale())]
        }

        /*if(!speciesPermissionService.isSpeciesContributor(speciesInstance, SUser.read(1L))) {
            return [success:false, msg:messageSource.getMessage("info.no.permission", null, LCH.getLocale())]
        }*/
        def currentUser
        Synonyms oldSynonym;
        if(synonymId) {
            println "=====SYN ID HAI== " + synonymId
            oldSynonym = Synonyms.read(synonymId);
            println "=====OLD SYN == " + oldSynonym
            if(oldSynonym) {
                println "====CONTRIBUTOR=== " +  oldSynonym.contributors[0]
                currentUser = oldSynonym.contributors[0]
            }
            if(!oldSynonym) {
                //return [success:false, msg:"Synonym with id ${synonymId} is not found"]
            } else if(oldSynonym.name == value && oldSynonym.relationship.value().equals(relationship)) {
                return [success:true, msg:messageSource.getMessage("info.nothing.change", null, LCH.getLocale())]
            } /*else if(!oldSynonym.isContributor()) {
                return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
            }*/
        } else {
            println "====CONTRIBUTOR=== " + SUser.read(1L)
            currentUser = SUser.read(1L)
        }

        Species.withTransaction { status ->
            if(oldSynonym) {
                def result = deleteSynonymOld(oldSynonym, speciesInstance);
                if(!result.success) {
                    def messagesourcearg = new Object[1];
                    println "====FAILED DELETE IN UPDATE===="
                    messagesourcearg[0] = result.msg;
                    return [success:false, msg:messageSource.getMessage("info.error.updating.synonym", messagesourcearg, LCH.getLocale())]
                }
            } 
            XMLConverter converter = new XMLConverter();
            
            NodeBuilder builder = NodeBuilder.newInstance();
            def synonym = builder.createNode("synonym");
            Node data = new Node(synonym, 'data', value)
            new Node(data, "relationship", relationship);
            new Node(data, "contributor", currentUser.email);

            List<Synonyms> synonyms = converter.createSynonymsOld(synonym, speciesInstance.taxonConcept);

            if(!synonyms) {
                return [success:false, msg:messageSource.getMessage("info.error.update.synonym", null, LCH.getLocale())]
            } else {
                String msg = '';
                def content;
                msg = messageSource.getMessage("info.success.update.synonym", null, LCH.getLocale());
                content = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                String activityType, mailType, description;
                if(oldSynonym) {
					description = ActivityFeedService.SPECIES_SYNONYM_UPDATED+" : "+oldSynonym.name+" changed to "+synonyms[0].name
                    activityType = mailType = ActivityFeedService.SPECIES_SYNONYM_UPDATED
                } else {
				    description = ActivityFeedService.SPECIES_SYNONYM_CREATED+" : "+synonyms[0].name
                    activityType = mailType = ActivityFeedService.SPECIES_SYNONYM_CREATED
                }

                return [success:true, id:speciesId, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, activityType:activityType, mailType:mailType, activityDesc:description]
            }
        }
    }

    def deleteSynonymOld(long synonymId, long speciesId) {
        Synonyms oldSynonym;
        if(synonymId) {
            oldSynonym = Synonyms.read(synonymId);
        }
        Species speciesInstance = Species.get(speciesId);

        return deleteSynonymOld(oldSynonym, speciesInstance);
    }
    
    def deleteSynonymOld(Synonyms oldSynonym, Species speciesInstance) {
        def currentUser
        if(!oldSynonym) {
            def messagesourcearg = new Object[1];
                messagesourcearg[0] = synonymId;
            return [success:false, msg:messageSource.getMessage("info.synonym.id.not.found", messagesourcearg, LCH.getLocale())]
        }

        /*if(!oldSynonym.isContributor()) {
            return [success:false, msg:messageSource.getMessage("info.no.permission.update", null, LCH.getLocale())]
        }*/

        currentUser = oldSynonym.contributors[0];
        /*if(!speciesPermissionService.isSpeciesContributor(speciesInstance, currentUser)) {
            return [success:false, msg:messageSource.getMessage("info.no.permission.delete.synonym", null, LCH.getLocale())]
        }*/

        Synonyms.withTransaction { status ->
            String msg = '';
            def content;
            try{
                oldSynonym.removeFromContributors(currentUser);
                
                if(oldSynonym.contributors.size() == 0) {
                    oldSynonym.delete(failOnError:true)
                } else {
                    if(!oldSynonym.save()) {
                        oldSynonym.errors.each { log.error it }
                        return [success:false, msg:messageSource.getMessage("info.error.deleting.synonym", null, LCH.getLocale())]
                    }
                }
                msg = messageSource.getMessage("info.success.remove.synonym", null, LCH.getLocale());
                content = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                return [success:true, id:speciesInstance.id, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, activityType:ActivityFeedService.SPECIES_SYNONYM_DELETED, activityDesc:ActivityFeedService.SPECIES_SYNONYM_DELETED+" : "+oldSynonym.name, mailType:ActivityFeedService.SPECIES_SYNONYM_DELETED]
            } 
            catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = e.getMessage();
                return [success:false, msg:messageSource.getMessage("info.error.synonym.deletion", messagesourcearg, LCH.getLocale())]
            }
        }
    }

    List<Document> getRelatedDocuments(Species speciesInstance) {
        List<SynonymsMerged> synonyms = AcceptedSynonym.fetchSynonyms(speciesInstance.taxonConcept);

        List<String> canonicalForms = [];        

        canonicalForms << speciesInstance.taxonConcept.canonicalForm;
        synonyms.each { syn ->
            canonicalForms << syn.canonicalForm;
        }

        def docSciNames = DocSciName.executeQuery("from DocSciName dsn where  dsn.scientificName in :canonicalForms", ['canonicalForms':canonicalForms]);
        return docSciNames.document.unique();

    }
	
	/////////////////////////// Online edit and bulk upload //////////////////////////
	ScientificName searchIBP(def parsedName, int rank, NameStatus status =  NameStatus.ACCEPTED ){
		if(parsedName instanceof String){
			parsedName = new TaxonomyDefinition(canonicalForm:parsedName.trim())
		}
		List taxonList = NamelistService.searchIBP( parsedName.canonicalForm, null, status, rank, false, parsedName.normalizedForm)
		if(taxonList.isEmpty())
			return null
		
		if(taxonList.size() > 1){
			log.error '############  ' + "IBP serch returning mulitiple result: should not happen " + taxonList
		}
		
		return taxonList[0]
	}
	
	
}
