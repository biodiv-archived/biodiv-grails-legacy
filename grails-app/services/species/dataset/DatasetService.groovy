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
import species.SpeciesPermission;
import content.eml.Contact;
import org.apache.commons.io.FilenameUtils;

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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;
import static org.springframework.http.HttpStatus.*;
import species.ScientificName.TaxonomyRank;

import species.NamesMetadata.NameStatus;
import species.dataset.Dataset.DatasetType;

class DatasetService extends AbstractMetadataService {

    static transactional = false

    public static final int IMPORT_BATCH_SIZE = 50;

    def messageSource;
    def activityFeedService
    def obvUtilService;
    def observationService;
    def observationsSearchService;
    def dataSource;
    def grailsApplication;
    def dataTableService;
    def dataPackageService;
    def userGroupService;

    Dataset1 create(params) {
        //return super.create(Dataset.class, params);
        def instance = Dataset1.class.newInstance();
        instance = update(instance, params)
        return instance;
    }

    Dataset1 update(Dataset1 instance, params) {
        instance.properties = params;

        instance.clearErrors();

        if(params.dataPackage) {
            DataPackage dp = params.dataPackage instanceof DataPackage ? params.dataPackage : DataPackage.read(params.long('dataPackage'));
            if(dp)
                instance.dataPackage = dp;
            else { 
                instance.dataPackage = null;
                log.warn "NO DATAPACKAGE WITH ID ${params.dataPackage}"
            }
        }

        //setting ufile and uri
        def config = Holders.config
        String contentRootDir = config.speciesPortal.content.rootDir
        if(!instance.uFile) {
            String uploadDir = "datasets/"+ UUID.randomUUID().toString()	
            try{
                File destinationFile = new File(contentRootDir, uploadDir);
                if(!destinationFile.exists()) {
                    destinationFile.mkdir();
                } 
                UFile f = new UFile()
                f.size = destinationFile.length()
                f.path = destinationFile.getAbsolutePath().replaceFirst(contentRootDir, "");
                if(f.save()) {
                    instance.uFile = f
                }
                log.debug "============== " + f.path
            }catch(e){
                e.printStackTrace()
            }
        }

        instance.initParams(params);

       return instance;
    }

    def save(params, sendMail) {
        def result;

        params.uploader = springSecurityService.currentUser;

        Dataset1 dataset;
        def feedType, feedAuthor;
        if(params.id) {
            dataset = Dataset1.get(Long.parseLong(params.id));
            params.uploader = dataset.uploader;
            dataset = update(dataset, params);
            feedType = activityFeedService.INSTANCE_UPDATED;
            feedAuthor = dataset.author
        } else {
            dataset = create(params);
            feedType = activityFeedService.INSTANCE_CREATED;
            feedAuthor = dataset.author
        }
      
        dataset.lastRevised = new Date();
        
        if(hasPermission(dataset, springSecurityService.currentUser)) {
            result = save(dataset, params, true, feedAuthor, feedType, null);
        } else {
            result = utilsService.getErrorModel("The logged in user doesnt have permissions to save ${dataset}", dataset, OK.value(), errors);
        }
        return result;
    }

    Map getFilteredDatasets(def params, max, offset, isMapView = false) {

        def queryParts = getFilteredDatasetFilterQuery(params) 
        String query = queryParts.query;
        long allDatasetCount = 0;

        query += queryParts.filterQuery + queryParts.orderByClause
        
        log.debug "query : "+query;
        log.debug "allDatasetCountQuery : "+queryParts.allDatasetCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        def allDatasetCountQuery = sessionFactory.currentSession.createQuery(queryParts.allDatasetCountQuery)

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
        def datasetInstanceList = hqlQuery.list();

        allDatasetCountQuery.setProperties(queryParts.queryParams)
        allDatasetCount = allDatasetCountQuery.list()[0]

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
        return [instanceList:datasetInstanceList, instanceTotal:allDatasetCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }

    def getFilteredDatasetFilterQuery(params) {
        def allSGroupId = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id.toString();
        params.sGroup = (params.sGroup)? params.sGroup : allSGroupId;

        //params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        //params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        //params.habitat = params.habitat.toLong()
		//params.isMediaFilter = (params.isMediaFilter) ?: 'true'
        //params.userName = springSecurityService.currentUser.username;

        def queryParams = [isDeleted : false]
        def activeFilters = [:]

        String userGroupQuery = "";
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
        query += " from Dataset1 obv "

        def filterQuery = " where obv.isDeleted = :isDeleted "
        
        //TODO: check logic
        if(params.featureBy == "false") {
            featureQuery = ", Featured feat "
            query += featureQuery;
            filterQuery += " and obv.id != feat.objectId and feat.objectType = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Dataset.class.getCanonicalName();
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

      if(params.sGroup){

            List  groupIdList=[];
            params.sGroup.split(',').each{ sGroupId->
                if(sGroupId) {
                    try {
                        sGroupId = sGroupId.toLong();
                        if(sGroupId != allSGroupId) {
                            println sGroupId;
                            def sGId = getSpeciesGroupIds(sGroupId);
                            if(sGId) {
                                groupIdList << sGId+'';
                            }
                        }
                    } catch(NumberFormatException e) {
                        println e.getMessage();
                    }
                }
            }


            if(!groupIdList){
                log.debug("No valid groups for id " + groupIdList);
                }else{
                    filterQuery += " and obv.taxonomicCoverage.groupIds like :sGroup "
                    queryParams["sGroup"] = '%'+groupIdList[0]+'%'
                    activeFilters["sGroup"] = groupIdList[0]
                }

        }

        if(params.dataPackage){
            filterQuery += " and obv.dataPackage.id = :dataPackage "
            queryParams["dataPackage"] = params.dataPackage.toLong()
            activeFilters["dataPackage"] = params.dataPackage.toLong()
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


		def allDatasetCountQuery = "select count(*) from Dataset1 obv " +((userGroupQuery)?userGroupQuery:'') +((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
        orderByClause = " order by " + orderByClause;

        return [query:query, allDatasetCountQuery:allDatasetCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

    }

    def delete(params){
        String messageCode;
        String url = utilsService.generateLink(params.controller, 'list', []);
        String label = Utils.getTitleCase(params.controller?:'Dataset')
        def messageArgs = [label, params.id]
        def errors = [];
        boolean success = false;
        if(!params.id) {
            messageCode = 'default.not.found.message'
        } else {
            try {
                def datasetInstance = Dataset1.get(params.id.toLong())
                if (datasetInstance) {
                    //datasetInstance.removeResourcesFromSpecies()
                    boolean isFeatureDeleted = Featured.deleteFeatureOnObv(datasetInstance, springSecurityService.currentUser, utilsService.getUserGroup(params))
                    if(isFeatureDeleted && hasPermission(datasetInstance, springSecurityService.currentUser)) {
                        def mailType = activityFeedService.INSTANCE_DELETED
                        try {
                            datasetInstance.isDeleted = true;

                            DataTable.findAllByDataset(datasetInstance).each {
                                Map r = dataTableService.delete(['id':it.id])
                                r.errors.each {
                                    errors << it;
                                }
                            }

                            if(!datasetInstance.hasErrors() && datasetInstance.save(flush: true)){
                                utilsService.sendNotificationMail(mailType, datasetInstance, null, params.webaddress);
                                //observationsSearchService.delete(observationInstance.id);
                                messageCode = 'default.deleted.message'
                                url = utilsService.generateLink(params.controller, 'list', [])
                                ActivityFeed.updateIsDeleted(datasetInstance)
                                success = true;
                            } else {
                                messageCode = 'default.not.deleted.message'
                                url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                                datasetInstance.errors.allErrors.each { log.error it }
                                datasetInstance.errors.allErrors .each {
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
                            log.warn "${datasetInstance.author} doesn't own dataset to delete"
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

    boolean hasPermission(Dataset1 dataset, SUser user) {
        if(!user || !dataset) return false;
        boolean isPermitted = false;
        if(dataPackageService.hasPermission(dataset.dataPackage, user) && (utilsService.isAdmin(springSecurityService.currentUser) ||dataset.getAuthor().id == user.id || dataset.uploader.id == user.id)) {
            isPermitted = true;
        }
        return isPermitted;
    }
} 
