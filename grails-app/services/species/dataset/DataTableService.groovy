package species.dataset;

import species.sourcehandler.importer.*;
import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import grails.util.Holders
import org.grails.taggable.TagLink;
import species.Classification;
import content.eml.UFile;
import species.NamesParser;
import grails.converters.JSON
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import au.com.bytecode.opencsv.CSVWriter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import groovy.io.FileType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

import species.Resource;
import species.Habitat;
import species.Language;
import species.License;
import species.License.LicenseType;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.participation.Featured;
import species.groups.SpeciesGroup;
import species.participation.ActivityFeed;
import species.participation.Comment;
import species.participation.Follow;
import species.participation.Observation;
import species.participation.Checklists;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import species.participation.Flag.FlagType
import species.participation.RecommendationVote.ConfidenceType;
import species.participation.Annotation
import species.participation.UploadLog
import species.sourcehandler.XMLConverter;
import species.utils.ImageType;
import species.utils.Utils;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import java.beans.Introspector;
import species.CommonNames;
import species.Language;
import species.Species;
import species.Metadata
import species.UploadJob;
import species.SpeciesPermission;
import content.eml.Contact;
import org.apache.commons.io.FilenameUtils;
import species.dataset.DataTable;
//import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import java.net.URLDecoder;
import org.apache.solr.common.util.DateUtil;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import content.eml.Coverage;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import species.groups.UserGroupController;
import species.groups.UserGroup;
import species.AbstractMetadataService;
import species.participation.UsersResource;
import species.participation.BulkUpload;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;
import static org.springframework.http.HttpStatus.*;
import species.ScientificName.TaxonomyRank;

import species.NamesMetadata.NameStatus;
import species.dataset.Dataset.DatasetType;
import species.dataset.DataPackage.DataTableType;
import species.participation.Observation.ProtocolType;

class DataTableService extends AbstractMetadataService {

    static transactional = false

    public static final int IMPORT_BATCH_SIZE = 50;

    def messageSource;
    def activityFeedService
    def observationsSearchService;
    def observationService;
    def speciesUploadService
    def dataSource;
    def grailsApplication;
    def factService;
    def traitService;
    def userGroupService;
    def documentService;
    def dataPackageService;

    DataTable create(params) {
        def instance = DataTable.class.newInstance();
        instance = update(instance, params)
        return instance;
    }

    DataTable update(DataTable instance, params) {
        //rollback any changes to instance on error
        DataTable.withTransaction {
            instance.properties = params;

            instance.clearErrors();

            instance.initParams(params);

            if(params.dataset) {
                Dataset1 ds = params.dataset instanceof Dataset1 ? params.dataset : Dataset1.read(params.long('dataset'));
                if(ds)
                    instance.dataset = ds;
                else { 
                    instance.dataset = null;
                    log.warn "NO DATASET WITH ID ${params.dataset}"
                }
            }

            //setting ufile and uri
            if(params.uFile.path) {
                def config = Holders.config
                String contentRootDir = config.speciesPortal.content.rootDir
                try{
                    File destinationFile = new File(contentRootDir, params.uFile.path);
                    if(destinationFile.exists()) {
                        UFile f = new UFile()
                        f.size = destinationFile.length()
                        f.path = destinationFile.getAbsolutePath().replaceFirst(contentRootDir, "");
                        log.debug "DATATABLE FILE PATH ============== " + f.path
                        if(f.save()) {
                            instance.uFile = f
                        }
                    }
                    File imagesFile;
                    if(params.imagesFilePath) {
                        imagesFile = new File(params.imagesFilePath);
                    } else if(params.imagesFile.path) {
                        imagesFile = new File(contentRootDir, params.imagesFile.path);
                    }
                    println  imagesFile

                    if(imagesFile && imagesFile.exists()) {
                        if(FilenameUtils.getExtension(imagesFile.getName()).equals('zip')) {
                            File destDir = destinationFile.getParentFile();
                            def ant = new AntBuilder().unzip( src: imagesFile, dest: destDir, overwrite:true)
                            imagesFile = destDir;
                        }

                        UFile f = new UFile()
                        f.size = imagesFile.length()
                        f.path = imagesFile.getAbsolutePath().replaceFirst(contentRootDir, "");
                        log.debug "IMAGES DIR============== " + f.path
                        if(f.save()) {
                            instance.imagesFile = f
                        }
                    }

                } catch(e){
                    e.printStackTrace()
                }
            }


            if(params.dataTableType) {
                instance.dataTableType = DataTableType.values()[params.int('dataTableType')];
            }

            def config = Holders.config
            String contentRootDir = config.speciesPortal.content.rootDir
            if(instance.uFile) {
                File dataTableFile = new File(contentRootDir, instance.uFile.path);

                File mappingFile = new File(dataTableFile.getParentFile(), 'mappingFile.tsv');
                //if mapping already exists .. chk if there has been any diff in marking
                List<String[]> oldColMapping = [];

                if(mappingFile.exists()) {
                    mappingFile.eachLine { line, count ->
                        //skipping first line [Field, Column, Order]
                        if(count > 1)
                            oldColMapping << line.split("\t");
                    }
                }
                List<String[]> newColMapping = FileObservationImporter.getInstance().getObservationMapping(params, null);

                FileObservationImporter.getInstance().saveObservationMapping(newColMapping, mappingFile, null, null);
                log.info "Saved new column marking."
                List columns = [];
                boolean isMarkingDirty = false;
              


                Map changedCols = [:]
                if(params.columns) {
                    params.columns.split(',').each {
                        String url = "";
                        String order = '100000';
                        String desc = '';
                        newColMapping.each {colM ->
                            if(colM[1]==it) {
                                url = colM[0];
                                order = colM[2];
                            }
                        }
                        boolean isNew = true;
                        oldColMapping.each {colM ->
                            println colM
                            if(colM[1]==it) {
                                isNew = false;
                                println colM[0]+"   "+url
                                if(colM[0] != url) {
                                    isMarkingDirty = true;
                                    println "Marking changed for col ${colM[1]}"
                                    changedCols[it]= ['newMarking':url, 'oldMarking':colM[0]];
                                }
                            }
                        }
                        if(isNew && url) {
                            changedCols[it] = ['newMarking':url];
                            isMarkingDirty = true;
                            println "New col marking was made ${it}"
                        } 
                        if(params.descColumn && params.descColumn[it]) {
                            desc = params.descColumn[it];
                        }
                        columns << [url,it,order,desc];
                    }
                }
                println "-------------------------------------------------------------"
                println "-------------------------------------------------------------"
                println columns;

                println "\n\nOLDCOLMAPPING : ${oldColMapping}"
                println "\n\nNEWCOLMAPPING : ${newColMapping}"


                if(isMarkingDirty) { 
                    instance.isMarkingDirty = isMarkingDirty;
                    instance.changedCols = changedCols;
                    log.debug "######### Reuploading datatable as marking is changed. Changed columns are ${changedCols}"
                } else {
                    log.debug "######### Ignoring updating data objects as marking was not changed"
                }
                println "-------------------------------------------------------------"
                println "-------------------------------------------------------------"
                instance.columns = columns as JSON;
            }
        }
       return instance;
    } 

    Map save(params, sendMail=true){
        def result;

        params.uploader = springSecurityService.currentUser;

        DataTable dataTable;
        String feedType;
        SUser feedAuthor;
        String feedDesc;

        if(params.id) { 
            dataTable = DataTable.get(Long.parseLong(params.id));
            params.uploader = dataTable.uploader;
            dataTable = update(dataTable, params);
            feedType = activityFeedService.INSTANCE_UPDATED;
            feedAuthor = dataTable.author
            def p = params.clone();
            p.remove('description')
            p.remove('summary')
            p.remove('uFile.path')
            p.remove('uFile.size')
            feedDesc = '';//p;
            if(dataTable.isMarkingDirty) {
                feedDesc += "Marking got changed ... so reuploading the sheet for new marking";
                feedDesc += "\n Changed Marking : ${dataTable.changedCols}"
            }
            //HACK to reupload species on edit
            switch(dataTable.dataTableType.ordinal()) {
                case DataTableType.SPECIES.ordinal(): 
                dataTable.isMarkingDirty = true;
                break;
            }

        } else {
            dataTable = create(params);
            feedType = activityFeedService.INSTANCE_CREATED;
            feedAuthor = dataTable.author
            //on creation of datatable ... we shd not use dataTableObservationImporter
            dataTable.isMarkingDirty = false;
        }

        dataTable.lastRevised = new Date();

        if(hasPermission(dataTable, springSecurityService.currentUser)) {
            result = save(dataTable, params, true, feedAuthor, feedType, null, feedDesc);

            if(result.success && dataTable.dataset) {
                log.debug "Posting dataTable to all user groups that dataset is part of"
                HashSet uGs = new HashSet();
                uGs.addAll(dataTable.dataset.userGroups);
                log.debug uGs
                userGroupService.addResourceOnGroups(dataTable, uGs.collect{it.id}, false);
            }

            //resave dataObjects only if requested for.
            if(result.success && (dataTable.isMarkingDirty || feedType == activityFeedService.INSTANCE_CREATED)) {
                if(params.id && params.replaceDataObjects) {
                    //TODO:delete all existing objects and reupload sheet
                    switch(dataTable.dataTableType.ordinal()) {
                        case DataTableType.OBSERVATIONS.ordinal():
                        log.info "Deleting all observations from ${dataTable}"
                        dataTable.deleteAllObservations();
                        break;
                        case DataTableType.SPECIES.ordinal(): 
                        break;
                        case DataTableType.FACTS.ordinal():
                        log.info "Deleting all facts from ${dataTable}"
                        dataTable.deleteAllFacts();
                        break;
                        case DataTableType.TRAITS.ordinal():
                        break;
                        case DataTableType.DOCUMENTS.ordinal():
                        log.info "Deleting all documents from ${dataTable}"
                        dataTable.deleteAllDocuments();
                        break;
                     }

                 }

                def config = Holders.config
                String contentRootDir = config.speciesPortal.content.rootDir
                File dataTableFile = new File(contentRootDir, dataTable.uFile.path);
                File imagesDir = new File(contentRootDir, dataTable.imagesFile.path);
                if(dataTableFile.exists() && !dataTableFile.isDirectory()) {
                    File mappingFile = new File(dataTableFile.getParentFile(), 'mappingFile.tsv');

                    Map paramsToPropagate = DataTable.getParamsToPropagate(dataTable);

                    switch(dataTable.dataTableType.ordinal()) {

                        case DataTableType.OBSERVATIONS.ordinal() :
                        Map p = ['file':dataTableFile.getAbsolutePath(), 'mappingFile':mappingFile.getAbsolutePath(), 'imagesDir':imagesDir?.getAbsolutePath(), 'uploadType':UploadJob.OBSERVATION_LIST, 'dataTable':dataTable.id, 'isMarkingDirty':dataTable.isMarkingDirty, 'changedCols':dataTable.changedCols];
                        p.putAll(paramsToPropagate);
                        def r = observationService.upload(p);
                        if(r.success) {
                            dataTable.uploadLog = r.uploadLog;
                        } else {
                            log.error "Error in scheduling observations upload job"
                        }
                        break;

                        case DataTableType.SPECIES.ordinal(): 
                        def res = speciesUploadService.basicUploadValidation(['xlsxFileUrl':params.xlsxFileUrl, 'imagesDir':imagesDir?.getAbsolutePath(), 'notes':params.notes, 'uploadType':params.uploadType, 'writeContributor':params.writeContributor, 'locale_language':params.locale_language, 'orderedArray':params.orderedArray, 'headerMarkers':params.headerMarkers, 'dataTable':dataTable, 'isMarkingDirty':dataTable.isMarkingDirty, 'dataDir':dataTableFile.getParentFile().getAbsolutePath()]);

                        if(res.sBulkUploadEntry) {
                            println "Saving upload log entry"
                            dataTable.uploadLog = res.sBulkUploadEntry;
                            //dataTable.uFile.path = res.sBulkUploadEntry.filePath.replace(contentRootDir,''); 
                            //dataTable.uFile.save();
                        } else {
                            log.error "Error in scheduling species upload job. Msg: ${res.msg}"
                        }

                        break;

                        case DataTableType.FACTS.ordinal():
                        String xlsxFileUrl = params.xlsxFileUrl.replace("\"", "").trim().replaceFirst(config.speciesPortal.content.serverURL, config.speciesPortal.content.rootDir);
                        def fFileValidation = factService.validateFactsFile(xlsxFileUrl, new UploadLog());
                        if(fFileValidation.success) {
                            log.debug "Validation of fact file is done. Proceeding with upload"
                            Map p = ['file':xlsxFileUrl, 'notes':params.notes, 'uploadType':UploadJob.FACT, 'dataTable':dataTable.id, 'isMarkingDirty':dataTable.isMarkingDirty];
                            p.putAll(paramsToPropagate);
                            def r = factService.upload(p);
                            if(r.success) {
                                dataTable.uploadLog = r.uploadLog;
                            } else {
                                log.error "Error in scheduling facts upload job"
                            }
                        } else {
                            log.error "Facts file is not valid.  So not scheduling for upload. ${fFileValidation}"
                        }
                        break;

                        case DataTableType.TRAITS.ordinal():

                        String tFile = params.uFile ? contentRootDir + File.separator + params.uFile.path : null;
                        String tvFile = params.tvFile ? contentRootDir + File.separator + params.tvFile.path : null;

                        def tFileValidation = traitService.validateTraitDefinitions(tFile, new UploadLog());
                        def tvFileValidation = traitService.validateTraitValues(tvFile, new UploadLog());

                        if(tFileValidation.success || tvFileValidation.success) {
                            log.debug "Validation of trait file and traitvalue file is done. Proceeding with upload"

                            Map p = ['file':tFile, 'tFile':tFile, 'tvFile':params.traitValueFile, 'iconsFile':imagesDir?.getAbsolutePath(), 'notes':params.notes, 'uploadType':UploadJob.TRAIT, 'dataTable':dataTable.id, 'isMarkingDirty':dataTable.isMarkingDirty];
                            p.putAll(paramsToPropagate);

                            def r = traitService.upload(p);
                            if(r.success) {
                                dataTable.uploadLog = r.uploadLog;
                            } else {
                                log.error "Error in scheduling traits upload job"
                            }
                        } else {
                            log.error "Traits file is not valid.  So not scheduling for upload. ${tFileValidation} ${tvFileValidation}"
                        }
                        break;
                        case DataTableType.DOCUMENTS.ordinal():
                        //String xlsxFileUrl = params.xlsxFileUrl.replace("\"", "").trim().replaceFirst(config.speciesPortal.content.serverURL, config.speciesPortal.content.rootDir);
                        Map p = ['file':dataTableFile.getAbsolutePath(), 'uploadType':UploadJob.DOCUMENT, 'dataTable':dataTable.id, 'locale_language':params.locale_language, 'isMarkingDirty':dataTable.isMarkingDirty];
                        p.putAll(paramsToPropagate);
                        def r = documentService.upload(p);
                        if(r.success) {
                            dataTable.uploadLog = r.uploadLog;
                        } else {
                            log.error "Error in scheduling observations upload job"
                        }
                        break;


                    }
                    if(!dataTable.save()) {
                        log.error "Error while saving datatable";
                    }
                }
             }
        } else {
            result = utilsService.getErrorModel("The logged in user doesnt have permissions to save ${dataset}", dataset, OK.value(), errors);
        } 


        return result;
    }

    Map getFilteredDataTables(params, max, offset, isMapView = false) {

        def queryParts = getFilteredDataTableFilterQuery(params) 
        String query = queryParts.query;
        long allDataTableCount = 0;

        query += queryParts.filterQuery + queryParts.orderByClause
        
        log.debug "query : "+query;
        log.debug "allDataTableCountQuery : "+queryParts.allDataTableCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        def allDataTableCountQuery = sessionFactory.currentSession.createQuery(queryParts.allDataTableCountQuery)

        def hqlQuery = sessionFactory.currentSession.createQuery(query)

        if(max > -1){
            hqlQuery.setMaxResults(max);
            queryParts.queryParams["max"] = max
        }
        if(offset > -1) {
            hqlQuery.setFirstResult(offset);
            queryParts.queryParams["offset"] = offset
        }
        
        hqlQuery.setProperties(queryParts.queryParams);
        def dataTableInstanceList = hqlQuery.list();

        allDataTableCountQuery.setProperties(queryParts.queryParams)
        allDataTableCount = allDataTableCountQuery.list()[0]

        if(params.daterangepicker_start){
            queryParts.queryParams["daterangepicker_start"] = params.daterangepicker_start
        }
        if(params.daterangepicker_end){
            queryParts.queryParams["daterangepicker_end"] =  params.daterangepicker_end
        }

        if(params.observedon_start){
            queryParts.queryParams["observedon_start"] = params.observedon_start
        }
        if(params.observedon_end){
            queryParts.queryParams["observedon_end"] =  params.observedon_end
        }
        println dataTableInstanceList
        return [instanceList:dataTableInstanceList, instanceTotal:allDataTableCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }

    def getFilteredDataTableFilterQuery(params) {
        //params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        //params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        //params.habitat = params.habitat.toLong()
		//params.isMediaFilter = (params.isMediaFilter) ?: 'true'
        //params.userName = springSecurityService.currentUser.username;

        def queryParams = [isDeleted : false]
        def activeFilters = [:]

        def query = "select "

        if(!params.sort || params.sort == 'score') {
            params.sort = "lastRevised"
        }
        def orderByClause = "  obv." + params.sort +  " desc, obv.id asc"

        if(params.fetchField) {
            query += " obv.id as id,"
            params.fetchField.split(",").each { fetchField ->
                if(!fetchField.equalsIgnoreCase('id'))
                    query += " obv."+fetchField+" as "+fetchField+","
            }
            query = query [0..-2];
            queryParams['fetchField'] = params.fetchField
        }else if(params.filterProperty == 'nearByRelated' && !params.bounds) {
            query += " g2 "
        } 
        else {
            query += " obv "
        }
        query += " from DataTable obv "

        String userGroupQuery = "";
        String filterQuery = " where obv.isDeleted = :isDeleted "
        
        //TODO: check logic
        if(params.featureBy == "false") {
            featureQuery = ", Featured feat "
            query += featureQuery;
            filterQuery += " and obv.id != feat.objectId and feat.objectType = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = DataTable.class.getCanonicalName();
        }

        if(params.tag){
            tagQuery = ",  TagLink tagLink "
            query += tagQuery;
            //mapViewQuery = "select obv.topology from Observation obv, TagLink tagLink "
            filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

            queryParams["tag"] = params.tag
            queryParams["tagType"] = GrailsNameUtils.getPropertyName(Observation.class);
            activeFilters["tag"] = params.tag
        }

        if(params.isChecklist && params.isChecklist.toBoolean()){
            filterQuery += " and obv.dataset is null and obv.dataTableType = :dataTableType"
            queryParams["isChecklist"] = params.isChecklist.toBoolean()
            activeFilters["user"] = params.isChecklist.toBoolean()
            queryParams["dataTableType"] = DataTableType.OBSERVATIONS;
            activeFilters["dataTableType"] = DataTableType.OBSERVATIONS;
        }

        if(params.dataTableType || params.type){
            params.dataTableType = params.dataTableType?:params.type
            if(params.dataTableType) {
                params.dataTableType = DataTableType.getEnum(params.dataTableType);
                filterQuery += " and obv.dataTableType = :dataTableType"
                queryParams["dataTableType"] = params.dataTableType;
                activeFilters["dataTableType"] = params.dataTableType;
            }
        }

        if(params.user){
            filterQuery += " and obv.author.id = :user "
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if (params.isFlagged && params.isFlagged.toBoolean()){
            filterQuery += " and obv.flagCount > 0 "
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if( params.daterangepicker_start && params.daterangepicker_end){
            def df = new SimpleDateFormat("dd/MM/yyyy")
            def startDate = df.parse(URLDecoder.decode(params.daterangepicker_start))
            def endDate = df.parse(URLDecoder.decode(params.daterangepicker_end))
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(endDate)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            endDate = new Date(cal.getTimeInMillis())

            filterQuery += " and ( created_on between :daterangepicker_start and :daterangepicker_end) "
            queryParams["daterangepicker_start"] =  startDate   
            queryParams["daterangepicker_end"] =  endDate

            activeFilters["daterangepicker_start"] = params.daterangepicker_start
            activeFilters["daterangepicker_end"] =  params.daterangepicker_end
        }

        if(params.webaddress) {

            def userGroupInstance =	utilsService.getUserGroup(params)
            params.userGroup = userGroupInstance;
        }

        if(params.userGroup) {
            log.debug "Filtering from usergourp : ${params.userGroup}"
            query += " join obv.userGroups userGroup "
            userGroupQuery += " join obv.userGroups userGroup "
            filterQuery += " and userGroup = :userGroup "
            queryParams['userGroup'] = params.userGroup;
        }

        if(params.notInUserGroup) {
            log.debug "Filtering from notInUsergourp : ${params.userGroup}"
            query += " join obv.userGroups userGroup "
            userGroupQuery += " join obv.userGroups userGroup "
            filterQuery += " and userGroup is null "
        }

		def allDataTableCountQuery = "select count(*) from DataTable obv " +((userGroupQuery)?userGroupQuery:'')+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
        orderByClause = " order by " + orderByClause;

        return [query:query, allDataTableCountQuery:allDataTableCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

    }

    def getMapFeatures(DataTable dt) {
        String query = "select t.type as type, t.feature as feature from map_layer_features t where ST_WITHIN('"+dt.geographicalCoverage.topology.toText()+"', t.topology)order by t.type" ;
        log.debug query;
        def features = [:]
        if(!Environment.getCurrent().getName().equalsIgnoreCase("development")) {
            try {
                def sql =  Sql.newInstance(dataSource);
                //sql.in(new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()), obv.topology)

                sql.rows(query).each {
                    switch (it.getProperty("type")) {
                        case "140" : features['Rainfall'] = it.getProperty("feature")+" mm";break;
                        case "138" : features['Soil'] = it.getProperty("feature");break;
                        case "161" : features['Temperature'] = it.getProperty("feature")+" C";break;
                        case "139" : features['Forest Type'] = it.getProperty("feature").toLowerCase().capitalize();break;
                        case "136" : features['Tahsil'] = it.getProperty("feature");break;
                    }
                };
                sql.close();
            } catch(e) {
                e.printStackTrace();
                log.error e.getMessage();
            }
        }
        return features
    }

    def delete(params){
        String messageCode;
        String url = utilsService.generateLink(params.controller, 'list', []);
        String label = Utils.getTitleCase(params.controller?:'DataTable')
        def messageArgs = [label, params.id]
        def errors = [];
        boolean success = false;
        println "+++++++++++++++++++++++++++++++++++"
        println "+++++++++++++++++++++++++++++++++++"
        println "+++++++++++++++++++++++++++++++++++"
        if(!params.id) {
            messageCode = 'default.not.found.message'
        } else {
            try {
                def dataTableInstance = DataTable.get(params.id.toLong())
                if (dataTableInstance) {
                    boolean isFeatureDeleted = Featured.deleteFeatureOnObv(dataTableInstance, springSecurityService.currentUser, utilsService.getUserGroup(params))
                    if(isFeatureDeleted  && hasPermission(dataTableInstance, springSecurityService.currentUser)) {
                        def mailType = utilsService.DATATABLE_DELETED
                        try {
                            dataTableInstance.isDeleted = true;
                            //Delete underlying observations of data table
                            dataTableInstance.deleteAllObservations();
                            dataTableInstance.deleteAllFacts();
                            dataTableInstance.deleteAllDocuments();
                            if(!dataTableInstance.hasErrors() && dataTableInstance.save(flush: true)){
                                utilsService.sendNotificationMail(mailType, dataTableInstance, null, params.webaddress);
                                //TODO: dataTableSearchService.delete(observationInstance.id);
                                messageCode = 'default.deleted.message'
                                url = utilsService.generateLink(params.controller, 'list', [])
                                ActivityFeed.updateIsDeleted(dataTableInstance)
                                success = true;
                            } else {
                                messageCode = 'default.not.deleted.message'
                                url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                                observationInstance.errors.allErrors.each { log.error it }
                                observationInstance.errors.allErrors .each {
                                    def formattedMessage = messageSource.getMessage(it, null);
                                    errors << [field: it.field, message: formattedMessage]
                                }

                            }
                        }
                        catch (org.springframework.dao.DataIntegrityViolationException e) {
                            messageCode = 'default.not.deleted.message'
                            url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                            e.printStackTrace();
                            log.error e.getMessage();
                            errors << [message:e.getMessage()];
                        }
                    } else {
                        if(!isFeatureDeleted) {
                            messageCode = 'default.not.deleted.message'
                            log.warn "Couldnot delete feature"
                        }
                        else {
                            messageArgs.add(0,'delete');
                            messageCode = 'default.not.permitted.message'
                            log.warn "currentUser doesn't own object to delete"
                        }
                    }
                } else {
                    messageCode = 'default.not.found.message'
                    url = utilsService.generateLink(params.controller, 'list', [])
                }
            } catch(e) {
                e.printStackTrace();
                url = utilsService.generateLink(params.controller, 'list', [])
                messageCode = 'default.not.deleted.message'
                errors << [message:e.getMessage()];
            }
        }

        String message = messageSource.getMessage(messageCode, messageArgs.toArray(), Locale.getDefault())

        return [success:success, url:url, msg:message, errors:errors]
    }

    boolean hasPermission(DataTable dataTable, SUser user) {

        if(!user || !dataTable) return false;

        boolean isPermitted = false;
        DataPackage dataPackage = null;
        if(dataTable.dataset == null) {
            dataPackage = DataPackage.findByTitle('Checklist');
        } else {
            dataPackage = dataTable.dataset.dataPackage;
        }

        if(dataPackageService.hasPermission(dataPackage, user) && (utilsService.isAdmin(springSecurityService.currentUser) || dataTable.getAuthor().id == user.id || dataTable.uploader.id == user.id)) {
            isPermitted = true;
        }
        return isPermitted;
    }

} 
