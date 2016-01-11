package species.dataset;

import species.sourcehandler.importer.DwCObservationImporter;
import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.taggable.TagLink;
import species.Classification;
import content.eml.UFile;

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
    def observationsSearchService;
    def datasourceService;

    Dataset create(params) {
        //return super.create(Dataset.class, params);
        def instance = Dataset.class.newInstance();
        instance = update(instance, params)
        return instance;
    }

    Dataset update(Dataset instance, params) {
        instance.properties = params;

        instance.clearErrors();

        if(params.author)  {
            instance.author = params.author;
        }

        String licensesStr = params.license_0?:params.license
        if(licensesStr) {
            log.debug "Setting license to ${licenseStr}"
            instance.license = (new XMLConverter()).getLicenseByType(licenseStr, false)
        } else {
            log.debug "Setting license to ${LicenseType.CC_BY}"
            instance.license = (new XMLConverter()).getLicenseByType(LicenseType.CC_BY, false)
        }

        instance.language = params.locale_language;
        instance.dataLanguage = params.locale_language;
        
        instance.externalId = params.externalId;
        instance.externalUrl = params.externalUrl;
        instance.viaId = params.viaId;
        instance.viaCode = params.viaCode;

        if(params.datasource) {
            Datasource ds = params.datasource instanceof Datasource ? params.datasource : Datasource.read(params.long('datasource'));
            if(ds)
                instance.datasource = ds;
            else 
                log.warn "NO DATASOURCE WITH ID ${params.datasource}"
        }

       return instance;
    }
/*
    Dataset update(String datasetEmlXmlFile, String occurenceFile) {
        String datasetEmlXmlStr = new File(datasetEmlXmlFile).text;

        def datasetEmlXml = new XmlParser().parseText(datasetEmlXmlStr);
        Dataset dataset = new Dataset();
        Map params = readEML(datasetEmlXml);
    }

    Map readEML(def datasetEmlXml) {
        def dataset = datasetEmlXml.dataset;
        params['alternateIdentifiers'] = [];
        dataset.alternateIdentifiers.each {
            params['alternateIdentifiers'] << it.text()
        }
        params['title'] = dataset.title;
        params['creator'] = [];
        dataset.creator.each {
            params['creator'] << createContact(it);
        }

        params['metadataProvider'] = [];
        dataset.metadataProvider.each {
            params['metadataProvider'] << createContact(it);
        }

        params['associatedParty'] = [];
        dataset.associatedParty.each {
            params['associatedParty'] << createContact(it);
        }

        params['createdOn'] = dataset.pubDate;
        params['language'] = Language.getLanguage(dataset.language);

        params['description'] = dataset.abstract.para;

        params['keywords'] = [];
        dataset.keywordSet.keyword.each {
            params['keywords'] << it.text();
        }

        params['rights'] = dataset.intellectualRights.text();

        params['geographicDescription'] = dataset.coverage.geographicCoverage.geographicCoverage;
        params['geographicCoverages'] = [];
        dataset.coverage.geographicCoverage.each {
            params['geographicCoverages'] << [description:it.geographicDescription, minLatitude:it.boundingCoordinates.southBoundingCoordinate, maxLatitude:it.boundingCoordinates.northBoundingCoordinate, minLongitude:it.boundingCoordinates.westBoundingCoordinate, maxLongitude:it.boundingCoordinates.eastBoundingCoordinate]; 
        }

        params['temporalCoverages'] = [];
        dataset.coverage.temporalCoverage.each {
            params['temporalCoverages'] << [fromDate:it.rangeOfDates.beginDate.calendarDate, toDate:it.rangeOfDates.endDate.calendarDate, singleDateTime:it.singleDateTime.calendarDate];
        }

        params['taxonomicCoverages'] = [];
        dataset.coverage.taxonomicCoverage.each {
            List t = [];
            it.taxonomicClassification.each { x ->
                x.taxonRankValue.split(',').each { n->
                    t << [rank:TaxonomyRank.getTaxonRank(x.taxonRankName), scientificName:n.trim()];
                }
            }
            params['taxonomicCoverages'] << [generalTaxonomicCoverage:it.generalTaxonomicCoverage, taxonomicCoverage:t];
        }

        params['contact'] = [];
        dataset.contact.each {
            params['contact'] << createContact(it);
        }

        params['author'] = springSecurityService.currentUser;

        params['externalId'] = dataset.attribute('packageId'); 
        params['externalUrl'] = 'GBIF'

        params['type'] = DatasetType.OBSERVATIONS; 

        return params;       
    }

    private Contact createContact(rP) {
        Map params = [:];
        params['role'] = rP.role?:ContactType.POINT_OF_CONTACT;  
        params['firstName'] = rP.individialName.givenName;
        params['lastName'] = rP.individialName.surName;
        params['description'] = ''; 
        params['deliveryPoint'] = rP.address.deliveryPoint;
        params['city'] = rP.address.city;
        params['state'] = rP.address.administrativeArea;
        params['country'] = rP.address.country?:'IN';
        params['postalCode'] = rP.address.postalCode;

        params['phone'] = [];
        rP.phone.each {
            params['phone'] << it;
        }

        params['email'] = []
        rP.electronicMailAddress.each {
            params['email'] << it;
        }

        params['onlineUrl'] = []
        rP.onlineUrl.each {
            params['onlineUrl'] << it;
        }

        params['position'] =  rP.positionName;
        params['organization'] = rP.organizationName;
       
        return new Contact(params);
    }
*/

    def save(params, sendMail) {
        return uploadDwCDataset(params);
    }

    Map uploadDwCDataset(Map params) {
        def resultModel = [:]
        utilsService.benchmark('uploadDwcDataset') {
        String zipFile = params.path?:params.uFile?.path;
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        zipFile = config.speciesPortal.content.rootDir + zipFile;

        boolean r = true;//validateDwCA(zipFile);
        if(!r) {
            return [success:false, msg:'Invalid DwC-A file']
        }

        File zipF = new File(zipFile);
        File destDir = zipF.getParentFile();
        /*new File(zipF.getParentFile(),  zipF.getName())
        if(!destDir.exists()) {
            destDir.mkdir()
        }*/

        def ant = new AntBuilder().unzip( src: zipFile,
            dest: destDir, overwrite:true)


        File directory = new File(destDir, FilenameUtils.removeExtension(zipF.getName()));
        File metadataFile = new File(directory, "metadata.xml");
        
        File uploadLog = new File(destDir, 'upload.log');
        if(uploadLog.exists()) uploadLog.delete();

        Date startTime = new Date();
        if(directory && metadataFile) {
            uploadLog << "\nUploading dataset in DwCA format present at : ${zipF.getAbsolutePath()}";
            uploadLog << "\nDataset upload start time : ${startTime}"
            String datasetMetadataStr = metadataFile.text;

            def datasetMetadata = new XmlParser().parseText(datasetMetadataStr);
            params['title'] = params.title?:datasetMetadata.dataset.title.text()
            params['description'] = params.description?:datasetMetadata.dataset.abstract.para.text();
            params['author'] = springSecurityService.currentUser; 
            params['externalId'] = datasetMetadata.attributes().packageId;
            params['externalUrl'] = 'http://doi.org/'+params['externalId'];
            params['type'] = DatasetType.OBSERVATIONS;
            params['rights'] = datasetMetadata.dataset.intellectualRights.para.text();
            params['language'] = datasetMetadata.dataset.language.text();
            params['datasource'] = Datasource.read(params.long('datasource'));
            UFile f = new UFile()
            f.size = zipF.length()
            f.path = params.uFile.path;//zipF.getAbsolutePath().replaceFirst(contentRootDir, "")
            if(f.save()) {
                params['uFile'] = f
            }
            //params['uFile'] = params.uFile; 
    //        params['originalAuthor'] = createContact() 
            Dataset dataset;
            def feedType;
            if(params.id) {
                dataset = Dataset.get(params.long('id'));
                dataset = update(dataset, params);
                feedType = activityFeedService.INSTANCE_UPDATED;
            } else {
                dataset = create(params);
                feedType = activityFeedService.INSTANCE_CREATED;
            }


            Dataset.withTransaction {
                resultModel = save(dataset, params, true, null, feedType, null);
            } 

            if(resultModel.success) {
                DwCObservationImporter dwcImporter = DwCObservationImporter.getInstance();
                Map o = dwcImporter.importObservationData(directory.getAbsolutePath(), uploadLog);
                int noOfUploadedObv=0, noOfFailedObv=0;

                List obvParamsList = dwcImporter.next(o.mediaInfo, IMPORT_BATCH_SIZE)
                boolean flushSingle = false;
                while(obvParamsList) {
                    List resultObv = [];
                    int tmpNoOfUploadedObv = 0, tmpNoOfFailedObv= 0;
                    try {
                        obvParamsList.each { obvParams ->
                            if(flushSingle) {
                                log.info "Retrying batch obv with flushSingle"
                                uploadLog << "\n Retrying batch obv with flushSingle"
                            }
                            obvParams['observation url'] = 'http://www.gbif.org/occurrence/'+obvParams['externalId'];
                            obvParams['dataset'] = dataset;
                            uploadLog << "\n\n----------------------------------------------------------------------";
                            uploadLog << "\nUploading observation with params ${obvParams}"
                            try {
                                if(obvUtilService.uploadObservation(null, obvParams, resultObv, uploadLog)) {
                                    tmpNoOfUploadedObv++;
                                } else {
                                    tmpNoOfFailedObv++;
                                }
                            } catch(Exception e) {
                                tmpNoOfFailedObv++;
                                if(flushSingle) { 
                                    utilsService.cleanUpGorm(true)
                                    uploadLog << "\n"+e.getMessage()
                                }
                                else
                                    throw e;
                            }
                        }

                        def obvs = resultObv.collect { Observation.read(it) }
                        try {
                            observationsSearchService.publishSearchIndex(obvs, true);
                        } catch (Exception e) {
                            log.error e.printStackTrace();
                        }

                        noOfUploadedObv += tmpNoOfUploadedObv;
                        noOfFailedObv += tmpNoOfFailedObv;
                        log.debug "Saved observations : noOfUploadedObv : ${noOfUploadedObv} noOfFailedObv : ${noOfFailedObv}";
                        obvParamsList = dwcImporter.next(o.mediaInfo, IMPORT_BATCH_SIZE)
                        flushSingle = false;
                    } catch (Exception e) {
                        log.error "error in creating observation."
                        if(uploadLog) uploadLog << "\nerror in creating observation ${e.getMessage()}." 
                        e.printStackTrace();
                        flushSingle = true;
                    }
                    utilsService.cleanUpGorm(true)
                    resultObv.clear();
                }
                log.debug "Total number of observations saved for dataset ${dataset} are : ${noOfUploadedObv}";
                
                uploadLog << "\n\n----------------------------------------------------------------------";
                uploadLog << "\nTotal number of observations saved for dataset (${dataset}) are : ${noOfUploadedObv}";
                uploadLog << "\nTotal number of observations failed in loading for dataset (${dataset}) are : ${noOfFailedObv}";
                uploadLog << "\nTotal time taken for dataset upload ${((new Date()).getTime() - startTime.getTime())/1000} sec"
                dwcImporter.closeReaders();
            } else {
                log.error "Error while saving dataset ${resultModel}";
            }
        } else {
            resultModel = [success:false, msg:'Invalid file']
        }
        uploadLog <<  "\nUpload result while saving dataset ${resultModel}";
        }
        return resultModel
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
        query += " from Dataset obv "

        def filterQuery = " where obv.isDeleted = :isDeleted "
        
        //TODO: check logic
        if(params.featureBy == "false") {
            featureQuery = ", Featured feat "
            query += featureQuery;
            filterQuery += " and obv.id != feat.objectId and feat.objectType = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Observation.class.getCanonicalName();

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

      
        
		def allDatasetCountQuery = "select count(*) from Dataset obv " +((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
        orderByClause = " order by " + orderByClause;

        return [query:query, allDatasetCountQuery:allDatasetCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

    }


} 
