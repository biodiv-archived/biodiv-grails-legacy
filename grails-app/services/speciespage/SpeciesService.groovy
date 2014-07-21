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
import species.Synonyms;
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
import org.hibernate.FetchMode;

class SpeciesService extends AbstractObjectService  {

    private static log = LogFactory.getLog(this);

    static transactional = false

    def groupHandlerService;
    def namesLoaderService;
    def externalLinksService;
    def speciesSearchService;
    def namesIndexerService;
    def observationService;
    def speciesPermissionService;
    def taxonService;
    def activityFeedService;

	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm aaa")
    static int BATCH_SIZE = 10;
    //static int noOfFields = Field.count();

    def nameTerms(params) {
        List result = new ArrayList();
        def queryResponse = speciesSearchService.terms(params.term, params.field, params.max);
        NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
        for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
            Map.Entry tag = (Map.Entry) iterator.next();
            result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Species Pages"]);
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
            def groupId = observationService.getSpeciesGroupIds(params.sGroup)
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
                def speciesInstance = Species.get(doc.getFieldValue("id"));
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
        if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase('lastrevised'))
            return true;
        return false;
    }

    /**
    * Add Species Field
    */
    def addSpeciesField(long speciesId, long fieldId, params) {
        if(!fieldId || !speciesId) {
            return [success:false, msg:"Id or value cannot be empty"]
        }
        XMLConverter converter = new XMLConverter();

        Species speciesInstance = Species.get(speciesId);
        Field field = Field.read(fieldId);

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to add"]
        }

        if(!field) {
            return [success:false, msg:"Invalid field"]
        }
        try {
            SpeciesField speciesFieldInstance = createNewSpeciesField(speciesInstance, field, null);
            if(speciesFieldInstance) {
                //updating metadata for the species field
                def result = updateSpeciesFieldInstance(speciesFieldInstance, params);
                def errors = result.errors;
                speciesInstance.addToFields(speciesFieldInstance);

                //TODO:make sure this is run in only one user updates this species at a time
                Species.withTransaction {
                    if(!speciesInstance.save()) {
                        speciesInstance.errors.each { errors << it; log.error it }
                        return [success:false, msg:"Error while adding species field", errors:errors]
                    }
                }

                List sameFieldSpeciesFieldInstances =  speciesInstance.fields.findAll { it.field.id == field.id} as List
                sortAsPerRating(sameFieldSpeciesFieldInstances);
                return [success:true, msg:"Successfully added species field", id:field.id, content:sameFieldSpeciesFieldInstances, speciesId:speciesInstance.id, errors:errors, speciesFieldInstance:speciesFieldInstance, speciesInstance:speciesInstance, activityType:activityFeedService.SPECIES_FIELD_CREATED+" : "+field, mailType:activityFeedService.SPECIES_FIELD_CREATED]
            }
        } catch(Exception e) {
            e.printStackTrace();
            return [success:false, msg:"Error while adding species field"]
        }
        return [success:false, msg:"Error while adding species field"]
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
            return [success:false, msg:"You don't have permission to update"]
        }

        try {
            def result;
            SpeciesField.withTransaction { status ->
                result = updateSpeciesFieldInstance(speciesField, params); 
                if(!speciesField.save()) {
                    speciesField.errors.each { result.errors << it }
                    return [success:false, msg:"Error while updating species field", errors:result.errors]
                } 
            }
            log.debug "Successfully updated species field";
            return [success:true, msg:"Successfully updated species field", errors:result.errors, content:speciesField, speciesFieldInstance:speciesField, speciesInstance:speciesField.species, activityType:activityFeedService.SPECIES_FIELD_UPDATED+" : "+speciesField.field, mailType:activityFeedService.SPECIES_FIELD_UPDATED]
        } catch(Exception e) {
            e.printStackTrace();
            return [success:false, msg:"Error while updating species field : ${e.getMessage()}"]
        }
    }

    private def updateSpeciesFieldInstance(SpeciesField speciesField, params) {
        if(!params.description) {
            return [success:false, msg:"Description cant be empty. Please delete the field if you dont want to have any description"]
        }

        List errors = [];
        String msg;
        if(!params.contributor) {
            params.contributor = springSecurityService.currentUser.id+'';
        }

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
                    errors <<  "Error while adding attribution ${l}"
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
                    errors << "Error while updating license"
                } else {
                    speciesField.addToLicenses(c);
                }
            }
        }

        //audienceType
        speciesField.audienceTypes.clear();
        params.audienceType.split("\\r?\\n|,").each { l ->
            l = l.trim();
            if(l) {
                AudienceType c = (new XMLConverter()).getAudienceTypeByType(l);
                if(!c) {
                    errors << "Error while updating audience type"
                } else {
                    speciesField.addToAudienceTypes(c);
                }
            }
        }

        //description
        speciesField.description = params.description;

        log.warn errors
        return [errors:errors]
    }


    /**
    * Delete species field
    */
    def deleteSpeciesField(long id) {
        SpeciesField speciesField = SpeciesField.get(id);
        if(!speciesField) {
            return [success:false, msg:"SpeciesField not found"]
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
                return [success:true, msg:"Successfully deleted species field", id:field.id, content:newSpeciesFieldInstance, speciesFieldInstance:speciesField, speciesInstance:speciesInstance, activityType:activityFeedService.SPECIES_FIELD_DELETED+" : "+speciesField.field, mailType:activityFeedService.SPECIES_FIELD_DELETED]
            } catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                return [success:false, msg:"Error while deleting field : ${e.getMessage()}"]
            }
        } else {
            return [success:false, msg:"You don't have persmission to delete this field"]
        }
    }

    /**
    * Update methods for individual metadata fields
    */
    def updateContributor(contributorId, long speciesFieldId, def value, String type) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        SUser oldContrib;
        if(contributorId) {
            oldContrib = SUser.read(contributorId);

            if(!oldContrib) {
                return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
            } else if(oldContrib.email == value) {
                return [success:true, msg:"Nothing to change"]
            }
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }

        SpeciesField.withTransaction { status ->
            SUser c = SUser.findByEmail(value);
            if(!c) {
                return [success:false, msg:"Error while updating ${type}. No registered user with email ${value} found"]
            } else {
                String msg = '';
                def content;
                if(oldContrib)
                    speciesField.removeFromContributors(oldContrib);
                speciesField.addToContributors(c);
                msg = 'Successfully added contributor';
                content = speciesField.contributors;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:"Error while updating ${type}"]
                }
                return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
            }
        }
    }

    def updateAttributor(contributorId, long speciesFieldId, def value, String type) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        Contributor oldContrib;
        if(contributorId) {
            oldContrib = Contributor.read(contributorId);

            if(!oldContrib) {
                return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
            } else if(oldContrib.name == value) {
                return [success:true, msg:"Nothing to change"]
            }
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }

        SpeciesField.withTransaction { status ->
            Contributor c = (new XMLConverter()).getContributorByName(value, true);
            if(!c) {
                return [success:false, msg:"Error while updating ${type}"]
            } else {
                String msg = '';
                def content;
                   if(oldContrib)
                        speciesField.removeFromAttributors(oldContrib);
                    speciesField.addToAttributors(c);
                    msg = 'Successfully added attribution';
                    content = speciesField.attributors;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:"Error while updating ${type}"]
                }
                return [success:true, id:speciesFieldId, type:type, msg:msg, content:content]
            }
        }
    }

    def updateReference(referenceId, long speciesFieldId, def value) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        Reference oldReference;
        if(referenceId) {
            oldReference = Reference.read(referenceId);

            if(!oldReference) {
                return [success:false, msg:"Reference with id ${referenceId} is not found"]
            } else if(oldReference.title == value) {
                return [success:true, msg:"Nothing to change"]
            }
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }


        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            if(oldReference)
                speciesField.removeFromReferences(oldReference);
            speciesField.addToReferences(new Reference(title:value));
            msg = 'Successfully added reference';
            content = speciesField.references;

            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                return [success:false, msg:"Error while updating reference"]
            }
            return [success:true, id:speciesFieldId, type:'reference', msg:msg, content:content]
        }
    }

    def addDescription(long speciesId, long fieldId, String value) { 
        if(!value || !fieldId || !speciesId) {
            return [success:false, msg:"Id or value cannot be empty"]
        }
        XMLConverter converter = new XMLConverter();

        Species speciesInstance = Species.get(speciesId);
        Field field = Field.read(fieldId);

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to add"]
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
                        return [success:false, msg:"Error while adding species field"]
                    }
                }
                List sameFieldSpeciesFieldInstances =  speciesInstance.fields.findAll { it.field.id == field.id} as List
                sortAsPerRating(sameFieldSpeciesFieldInstances);
                return [success:true, msg:"Successfully updated speciesField", id:field.id, type:'newdescription', content:sameFieldSpeciesFieldInstances, 'speciesInstance':speciesInstance, speciesId:speciesInstance.id]
            }
        } catch(Exception e) {
            e.printStackTrace();
            return [success:false, msg:"Error while adding species field"]
        }
        return [success:false, msg:"Error while adding species field"]
    }

    def updateDescription(long id, String value) {
        if(!value || !id) {
            return [success:false, msg:"Id or value cannot empty"]
        }
        SpeciesField c = SpeciesField.get(id);
        return updateSpeciesFieldDescription(c, value);
    }
    
    def updateSpeciesFieldDescription(SpeciesField c, String value) {
        if(!c) {
            return [success:false, msg:"SpeciesField not found"]
        } else if(!speciesPermissionService.isSpeciesFieldContributor(c, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        } else {
            SpeciesField.withTransaction {
                c.description = value.trim()
                if (!c.save()) {
                    c.errors.each { log.error it }
                    return [success:false, msg:"Error while updating species field name"]
                }
            }
            return [success:true, msg:""]
        }
    }

    def updateLicense(long speciesFieldId, def value) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }

        SpeciesField.withTransaction { status ->
            License c = (new XMLConverter()).getLicenseByType(value, false);
            if(!c) {
                return [success:false, msg:"Error while updating license"]
            } else {
                String msg = '';
                def content;
                speciesField.licenses.clear();
                speciesField.addToLicenses(c);
                msg = 'Successfully added license';
                content = speciesField.licenses;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:"Error while updating license"]
                }
                return [success:true, id:speciesFieldId, msg:msg, content:content]
            }
        }
    }

    def updateAudienceType(long speciesFieldId, String value) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }


        SpeciesField.withTransaction { status ->
            AudienceType c = (new XMLConverter()).getAudienceTypeByType(value);
            if(!c) {
                return [success:false, msg:"Error while updating audience type"]
            } else {
                String msg = '';
                def content;
                speciesField.audienceTypes.clear();
                speciesField.addToAudienceTypes(c);
                msg = 'Successfully added audience type';
                content = speciesField.audienceTypes;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:"Error while updating audience type"]
                }
                return [success:true, id:speciesFieldId, msg:msg, content:content]
            }
        }
    }

    def updateStatus(long speciesFieldId, String value) {
        if(!value) {
            return [success:false, msg:"Field content cannot be empty"]
        }

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }


        SpeciesField.withTransaction { status ->
            SpeciesField.Status c = getStatus(value);
            if(!c) {
                return [success:false, msg:"Error while updating status"]
            } else {
                String msg = '';
                def content;
                speciesField.status = c;
                msg = 'Successfully added status';
                content = speciesField.status;

                if(!speciesField.save()) {
                    speciesField.errors.each { log.error it }
                    return [success:false, msg:"Error while updating status"]
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

    def updateSynonym(def synonymId, def speciesId, String relationship, String value) {
        if(!value || !relationship) {
            return [success:false, msg:"Synonym value or relationship content cannot be empty"]
        }
        Species speciesInstance = Species.get(speciesId);
   
        if(!speciesInstance) {
            return [success:false, msg:"Species with id ${speciesId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }

        Synonyms oldSynonym;
        if(synonymId) {
            oldSynonym = Synonyms.read(synonymId);

            if(!oldSynonym) {
                //return [success:false, msg:"Synonym with id ${synonymId} is not found"]
            } else if(oldSynonym.name == value && oldSynonym.relationship.value().equals(relationship)) {
                return [success:true, msg:"Nothing to change"]
            } else if(!oldSynonym.isContributor()) {
                return [success:false, msg:"You don't have permission to update as you are not a contributor."]
            }
        }

        Species.withTransaction { status ->
            if(oldSynonym) {
                def result = deleteSynonym(oldSynonym, speciesInstance);
                if(!result.success) {
                    return [success:false, msg:"Error while updating synonym. Error: ${result.msg})"]
                }
            } 
            XMLConverter converter = new XMLConverter();

            NodeBuilder builder = NodeBuilder.newInstance();
            def synonym = builder.createNode("synonym");
            Node data = new Node(synonym, 'data', value)
            new Node(data, "relationship", relationship);
            new Node(data, "contributor", springSecurityService.currentUser.email);
 
            List<Synonyms> synonyms = converter.createSynonyms(synonym, speciesInstance.taxonConcept);
            
            if(!synonyms) {
                return [success:false, msg:"Error while updating synonym"]
            } else {
                String msg = '';
                def content;
                msg = 'Successfully updated synonym';
                content = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                String activityType, mailType;
                if(oldSynonym) {
                    activityType = activityFeedService.SPECIES_SYNONYM_UPDATED+" : "+oldSynonym.name+" changed to "+synonyms[0].name
                    mailType = activityFeedService.SPECIES_SYNONYM_UPDATED
                } else {
                    activityType = activityFeedService.SPECIES_SYNONYM_CREATED+" : "+synonyms[0].name
                    mailType = activityFeedService.SPECIES_SYNONYM_CREATED
                }

                return [success:true, id:speciesId, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, activityType:activityType, mailType:mailType]
            }
        }
    }

    def updateCommonname(def cnId, def speciesId, String language, String value) {
        if(!value || !language) {
            return [success:false, msg:"Common name or language content cannot be empty"]
        }
        Species speciesInstance = Species.get(speciesId);
   
        if(!speciesInstance) {
            return [success:false, msg:"Species with id ${speciesId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to update"]
        }

        CommonNames oldCommonname;
        Language lang = Language.getLanguage(language);
        if(cnId) {
            oldCommonname = CommonNames.read(cnId);

            if(!oldCommonname) {
                //return [success:false, msg:"Commonname with id ${cnId} is not found"]
            } else if(oldCommonname.name == value && oldCommonname.language.equals(lang)) {
                return [success:true, msg:"Nothing to change"]
            } else if(!oldCommonname.isContributor()) {
                return [success:false, msg:"You don't have permission to update as you are not a contributor."]
            }
        }

        Species.withTransaction { status ->
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
 
            List<CommonNames> commonnames = converter.createCommonNames(cn, speciesInstance.taxonConcept);
            
            if(!commonnames) {
                return [success:false, msg:"Error while updating common name"]
            } else {
                String msg = '';
                def content;
                msg = 'Successfully updated common name';
                content = CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                String activityType, mailType;
                if(oldCommonname) {
                    activityType = activityFeedService.SPECIES_COMMONNAME_UPDATED+" : "+oldCommonname.name+" changed to "+commonnames[0].name
                    mailType = activityFeedService.SPECIES_COMMONNAME_UPDATED
                } else {
                    activityType = activityFeedService.SPECIES_COMMONNAME_CREATED+" : "+commonnames[0].name
                    mailType = activityFeedService.SPECIES_COMMONNAME_CREATED
                }


                return [success:true, id:speciesId, msg:msg, type:'commonname', content:content, speciesInstance:speciesInstance, activityType:activityType, mailType :mailType]
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
            return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to delete this ${type}"]
        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromContributors(oldContrib);
            if(speciesField.contributors.size() == 0) {
                msg = 'There should be atleast one contributor';
                return [success:false, msg:msg]
            } else {
                msg = 'Successfully removed contributor';
                content = speciesField.contributors;
            }
            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                return [success:false, msg:"Error while updating ${type}"]
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
            return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to delete this ${type}"]
        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromAttributors(oldContrib);
            msg = 'Successfully removed attribution';
            content = speciesField.attributors;

            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                return [success:false, msg:"Error while updating ${type}"]
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
            return [success:false, msg:"Reference with id ${referenceId} is not found"]
        } 

        SpeciesField speciesField = SpeciesField.get(speciesFieldId);
        if(!speciesField) {
            return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
        }

        if(!speciesPermissionService.isSpeciesFieldContributor(speciesField, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to delete this reference"]
        }

        SpeciesField.withTransaction { status ->
            String msg = '';
            def content;
            speciesField.removeFromReferences(oldReference);
            msg = 'Successfully removed reference';
            content = speciesField.references;

            if(!speciesField.save()) {
                speciesField.errors.each { log.error it }
                return [success:false, msg:"Error while updating reference"]
            }
            return [success:true, id:speciesFieldId, type:'reference', msg:msg, content:content]
        }
    }

    def deleteDescription(long id) {
        return deleteSpeciesField(id);
    }

    def deleteSynonym(long synonymId, long speciesId) {
        Synonyms oldSynonym;
        if(synonymId) {
            oldSynonym = Synonyms.read(synonymId);
        }
        Species speciesInstance = Species.get(speciesId);

        return deleteSynonym(oldSynonym, speciesInstance);
    }
    
    def deleteSynonym(Synonyms oldSynonym, Species speciesInstance) {
        if(!oldSynonym) {
            return [success:false, msg:"Synonym with id ${synonymId} is not found"]
        } 

        if(!oldSynonym.isContributor()) {
            return [success:false, msg:"You don't have permission to update as you are not a contributor."]
        }

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to delete synonym"]
        }

        Synonyms.withTransaction { status ->
            String msg = '';
            def content;
            try{
                oldSynonym.removeFromContributors(springSecurityService.currentUser);
                
                if(oldSynonym.contributors.size() == 0) {
                    oldSynonym.delete(failOnError:true)
                } else {
                    if(!oldSynonym.save()) {
                        oldSynonym.errors.each { log.error it }
                        return [success:false, msg:"Error while deleting synonym"]
                    }
                }
                msg = 'Successfully removed synonym';
                content = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                return [success:true, id:speciesInstance.id, msg:msg, type:'synonym', content:content, speciesInstance:speciesInstance, activityType:activityFeedService.SPECIES_SYNONYM_DELETED+" : "+oldSynonym.name, mailType:activityFeedService.SPECIES_SYNONYM_DELETED]
            } 
            catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                return [success:false, msg:"Error while deleting synonym: ${e.getMessage()}"]
            }
        }
    }

    def deleteCommonname(def cnId, def speciesId) {
        CommonNames oldCommonname;
        if(cnId) {
            oldCommonname = CommonNames.read(cnId);
        }

        Species speciesInstance = Species.get(speciesId);
        return deleteCommonname(oldCommonname, speciesInstance);
    } 
    
    def deleteCommonname(CommonNames oldCommonname, Species speciesInstance) {
        if(!oldCommonname) {
            return [success:false, msg:"Common name with id ${cnId} is not found"]
        } 

        if(!oldCommonname.isContributor()) {
            return [success:false, msg:"You don't have permission to update as you are not a contributor."]
        }

        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
            return [success:false, msg:"You don't have permission to delete commonname"]
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
                        return [success:false, msg:"Error while deleting common name"]
                    }
                }

                msg = 'Successfully removed common name';
                content = CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept) ;
                return [success:true, id:speciesInstance.id, msg:msg, type:'commonname', content:content, speciesInstance:speciesInstance, activityType:activityFeedService.SPECIES_COMMONNAME_DELETED+" : "+oldCommonname.name, mailType:activityFeedService.SPECIES_COMMONNAME_DELETED]
            } 
            catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
                return [success:false, msg:"Error while deleting common name: ${e.getMessage()}"]
            }
        }
    }
    
    /**
    * Create Species given species name and atleast one taxon hierarchy
    */
    def createSpecies(String speciesName, int rank, List taxonRegistryNames) {
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
                    result['msg'] = 'Mandatory level(s) is/are missing in the hierarchy';
                    return result
                }
                result['success'] = false;
                result['msg'] = 'Mandatory level(s) is/are missing in the hierarchy';
                return result
            }

            Classification classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
println "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
            //CHK if current user has permission to add details to the species
            if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
                println "checking permissions +++++++++++++++++++++++++++++++++++++++++"
                def taxonRegistryNodes = converter.createTaxonRegistryNodes(taxonRegistryNames, classification.name, springSecurityService.currentUser);
                println taxonRegistryNodes

                List<TaxonomyRegistry> tR = converter.getClassifications(taxonRegistryNodes, speciesName, false);
                println tR
                println "tR: .... +++++++++++++++++++++++++++++++++++"
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
            Map result1 = taxonService.addTaxonHierarchy(speciesName, taxonRegistryNames, classification, springSecurityService.currentUser); 
            result.putAll(result1);
            result.speciesInstance = speciesInstance;
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

    def requestExport(params){
        log.debug(params)
        log.debug "creating species download request"
        DownloadLog.createLog(springSecurityService.currentUser, params.filterUrl, params.downloadType, params.notes, params.source, params)
    }

    def export(params, dl){
        log.debug(params)
        String action = new URL(dl.filterUrl).getPath().split("/")[2]
        def speciesInstanceList = getSpeciesList(params, action).speciesInstanceList
        log.debug " Species total $speciesInstanceList.size "
        return exportSpeciesData(speciesInstanceList, null)
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
    def exportSpeciesData(String directory) {
        return DwCAExporter.getInstance().exportSpeciesData(directory)
    } 

    /**
     * export species data
     */
    def exportSpeciesData(List<Species> species, String directory) {
        return DwCAExporter.getInstance().exportSpeciesData(species, directory)
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

        def queryParams = [:]
        def activeFilters = [:]
        queryParams.max = Math.min(params.max ? params.max.toInteger() : 42, 100);
        queryParams.offset = params.offset ? params.offset.toInteger() : 0

        if(queryParams.max < 0 ) {
            queryParams.max = 42 
        }

        if(queryParams.offset < 0) {
            queryParams.offset = 0
        }

        queryParams.sort = params.sort?:"lastrevised"
        if(queryParams.sort.equals('lastrevised')) {
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
            } else {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  is null "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  is null ";
				queryParams["startsWith"] = params.startsWith
            }
            queryParams['sGroup']  = groupIds
        } else {
            if(params.startsWith == "A-Z") {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and s.taxonConcept = t and t.group.id  in (:sGroup) "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.taxonConcept = t and t.group.id  in (:sGroup)  ";

            } else {
                query = "select s from Species s, TaxonomyDefinition t "
                filterQuery += " and title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup) "
                countQuery = "select s.percentOfInfo, count(*) as count from Species s, TaxonomyDefinition t "
                countFilterQuery += " and s.title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup)  ";
				queryParams["startsWith"] = params.startsWith
            }
            queryParams['sGroup']  = groupIds
        }

        if(params.featureBy == "true" ) {
            params.userGroup = observationService.getUserGroup(params)
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
            }
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
        countQuery += countFilterQuery + " group by s.percentOfInfo"
		
        return [query:query, countQuery:countQuery, queryParams:queryParams]


    }

    /**
    * get species list 
    */
    private _getSpeciesList(params) {
        //cache "taxonomy_results"
        def queryParts = _getSpeciesListQuery(params)
        def hqlQuery = sessionFactory.currentSession.createQuery(queryParts.query)
        def hqlCountQuery = sessionFactory.currentSession.createQuery(queryParts.countQuery)
        def queryParams = queryParts.queryParams
        if(queryParams.max > -1){
            hqlQuery.setMaxResults(queryParams.max);
        }
        if(queryParams.offset > -1) {
            hqlQuery.setFirstResult(queryParams.offset);
        } 
        hqlQuery.setProperties(queryParams);
        hqlCountQuery.setProperties(queryParams);
        
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
        return [speciesInstanceList: speciesInstanceList, instanceTotal: count, speciesCountWithContent:speciesCountWithContent, 'userGroupWebaddress':params.webaddress, queryParams: queryParams]
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
        if(params.resourceListType == "ofSpecies"){
            def resourcesXML = createResourcesXML(params);
            resources = saveResources(species, resourcesXML);
            species.resources?.clear();
        }
        else if(params.resourceListType == "fromRelatedObv" || params.resourceListType == "fromSpeciesField"){
            def resId = []
            params.each { key, val ->
                int index = -1;
                if(key.startsWith('pullImage_')) {
                    index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));
                }
                if(index != -1) {
                    resId.add(params.get('resId_'+index));    
                }
            }
            resId.each{
                resources.add(Resource.get(it.toLong()))
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

        resources.each { resource ->
            resource.saveResourceContext(species)
            species.addToResources(resource);
        }
        if(!species.save(flush:true)){
            species.errors.allErrors.each { log.error it }
            return false
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
}
