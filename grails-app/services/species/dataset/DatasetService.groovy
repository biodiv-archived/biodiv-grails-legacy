package species.dataset;

import species.sourcehandler.importer.*;
import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.ConfigurationHolder
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
    def datasourceService;
    def dataSource;
    def grailsApplication;

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
        String file = params.path?:params.uFile?.path;
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        file = config.speciesPortal.content.rootDir + file;

        File f = new File(file);
        File destDir = f.getParentFile();
        /*new File(f.getParentFile(),  f.getName())
        if(!destDir.exists()) {
            destDir.mkdir()
        }*/
        boolean isDwC = false;
        File directory = f.getParentFile();
        File metadataFile;
        if(FilenameUtils.getExtension(f.getName()).equals('zip')) {
            def ant = new AntBuilder().unzip( src: file,
            dest: destDir, overwrite:true)
            directory = new File(destDir, FilenameUtils.removeExtension(f.getName()));
            if(!directory.exists()) {
                directory = destDir;
            }
            isDwC = true;//validateDwCA(file);
            if(!isDwC) {
                return [success:false, msg:'Invalid DwC-A file']
            } else {
                metadataFile = new File(directory, "metadata.xml");
            }
        }

        
        File uploadLog = new File(destDir, 'upload.log');
        if(uploadLog.exists()) uploadLog.delete();

        Date startTime = new Date();
        if(directory) {
            params['author'] = springSecurityService.currentUser; 
            params['type'] = DatasetType.OBSERVATIONS;
            params['datasource'] = Datasource.read(params.long('datasource'));
 
            if(metadataFile) {
                uploadLog << "\nUploading dataset in DwCA format present at : ${f.getAbsolutePath()}";
                uploadLog << "\nDataset upload start time : ${startTime}"
                String datasetMetadataStr = metadataFile.text;

                def datasetMetadata = new XmlParser().parseText(datasetMetadataStr);
                params['title'] = params.title?:datasetMetadata.dataset.title.text()
                params['description'] = params.description?:datasetMetadata.dataset.abstract.para.text();
                params['externalId'] = datasetMetadata.attributes().packageId;
                params['externalUrl'] = 'http://doi.org/'+params['externalId'];
                params['rights'] = datasetMetadata.dataset.intellectualRights.para.text();
                params['language'] = datasetMetadata.dataset.language.text();
                params['publicationDate'] = utilsService.parseDate(datasetMetadata.dataset.pubDate.text());
            } else {
                params['externalUrl'] = params.externalUrl ?: params['datasource']?.website;
            }

            UFile f1 = new UFile()
            f1.size = f.length()
            f1.path = params.uFile.path;//zipF.getAbsolutePath().replaceFirst(contentRootDir, "")
            if(f1.save()) {
                params['uFile'] = f1
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


            resultModel = save(dataset, params, true, null, feedType, null);

            if(resultModel.success) {
                    if(params.datasource.title.contains('Global Biodiversity Information Facility')) {
                        importGBIFObservations(dataset, directory, uploadLog)
                    } else {
                        if(isDwC) {
                            importDWCObservations(dataset, directory, uploadLog);
                        } else {
                            def request = WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest()    
                            def rs = [:]
                            if(ServletFileUpload.isMultipartContent(request)) {
                                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                                Utils.populateHttpServletRequestParams(request, rs);
                            } 
 
                            def multimediaF = params.multimediaFile?:params.multimediaFileUpload;
                            def mF = params.mappingFile?:params.mappingFileUpload;
                            def mMF = params.multimediaMappingFile?:params.multimediaMappingFileUpload;
                            File multimediaFile, mappingFile, multimediaMappingFile;
                            
                            if(multimediaF instanceof String) {
                                multimediaFile = new File(config.speciesPortal.content.rootDir, multimediaF );
                            } else {
                                multimediaFile = new File(directory, 'multimediaFile.tsv');
                                multimediaF.transferTo(multimediaFile);
                            }
                            
                            if(mF instanceof String) {
                                mappingFile = new File(config.speciesPortal.content.rootDir, mF );
                            } else {
                                mappingFile = new File(directory, 'mappingFile.tsv');
                                mF.transferTo(mappingFile);
                            }

                            if(mMF instanceof String) {
                                multimediaMappingFile = new File(config.speciesPortal.content.rootDir, mMF );
                            } else {
                                multimediaMappingFile = new File(directory, 'multimediaMappingFile.tsv');
                                mMF.transferTo(multimediaMappingFile);
                            }

                            File observationsFile = f;//new File(directory, 'occurence.txt');
                            //File multimediaFile = params.multimediaFile;//new File(directory, 'multimedia.txt');
                            importObservations(dataset, observationsFile, multimediaFile, mappingFile, multimediaMappingFile, uploadLog);
                        }
                    }
            } else {
                log.error "Error while saving dataset ${resultModel}";
            }
        } else {
            resultModel = [success:false, msg:'Invalid file']
        }
        uploadLog <<  "\nUpload result while saving dataset ${resultModel}";
        return resultModel
    }

    private void importDWCObservations(Dataset dataset, File directory, File uploadLog) {
        DwCObservationImporter importer = DwCObservationImporter.getInstance();
        Map o = importer.importData(directory.getAbsolutePath(), uploadLog);
        importObservations(dataset, directory, importer, uploadLog);
    }

    private void importObservations(Dataset dataset, File observationsFile, File multimediaFile, File mappingFile, File multimediaMappingFile, File uploadLog) {
        FileObservationImporter importer = FileObservationImporter.getInstance();
        Map o = importer.importData(observationsFile, multimediaFile, mappingFile, multimediaMappingFile, uploadLog);
        importObservations(dataset, observationsFile.getParentFile(), importer, o.mediaInfo, uploadLog);
    }

    private void importObservations(Dataset dataset, File directory, AbstractObservationImporter importer, Map mediaInfo, File uploadLog) {
        List obvParamsList = importer.next(mediaInfo, IMPORT_BATCH_SIZE, uploadLog)
        int noOfUploadedObv=0, noOfFailedObv=0;
        boolean flushSingle = false;
        Date startTime = new Date();
        int i=0;
        while(obvParamsList) {
            List resultObv = [];
            int tmpNoOfUploadedObv = 0, tmpNoOfFailedObv= 0;
            try {
                obvParamsList.each { obvParams ->
                    if(flushSingle) {
                        log.info "Retrying batch obv with flushSingle"
                        uploadLog << "\n Retrying batch obv with flushSingle"
                    }
                    //obvParams['observation url'] = 'http://www.gbif.org/occurrence/'+obvParams['externalId'];
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
                obvParamsList = importer.next(mediaInfo, IMPORT_BATCH_SIZE, uploadLog)
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
        importer.closeReaders();
    }

    //FIX: This code is subjected to SQL INJECTION. Please make sure your data is sanitized
    private void importGBIFObservations(Dataset dataset, File directory, File uploadLog) {

        uploadLog << "Starting import of GBIF Observations data";
        def conn = new Sql(dataSource)

        int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
        dataSource.setUnreturnedConnectionTimeout(0);
	

        def tmpBaseDataTable = "gbifdata";
        def tmpNewBaseDataTable = "gbifdata_new";
        def tmpBaseDataTable_multimedia = tmpBaseDataTable+"_multimedia";
        def tmpBaseDataTable_parsedNamess = tmpBaseDataTable+"_parsed_names";
        def tmpBaseDataTable_namesList = tmpBaseDataTable+"_namesList";

        String occurencesFileName = (new File(directory, 'occurrence.txt')).getAbsolutePath(); 
        String multimediaFileName = (new File(directory, 'multimedia.txt')).getAbsolutePath(); 
        String namesFileName = (new File(directory, 'gbif_names_all_with_idswithoutspchar.csv')).getAbsolutePath(); 
        Date startTime = new Date();
         try {
            uploadLog << "\nCreating base table for ${occurencesFileName}";

            conn.execute('''
            drop table  if exists '''+tmpBaseDataTable+''';
            create table '''+tmpBaseDataTable+'''(gbifID text, abstract text, accessRights text, accrualMethod text, accrualPeriodicity text, accrualPolicy text, alternative text, audience text, available text, bibliographicCitation text, conformsTo text, contributor text, coverage text, created text, creator text, date text, dateAccepted text, dateCopyrighted text, dateSubmitted text, description text, educationLevel text, extent text, format text, hasFormat text, hasPart text, hasVersion text, identifier text, instructionalMethod text, isFormatOf text, isPartOf text, isReferencedBy text, isReplacedBy text, isRequiredBy text, isVersionOf text, issued text, language text, license text, mediator text, medium text, modified text, provenance text, publisher text, references1 text, relation text, replaces text, requires text, rights text, rightsHolder text, source text, spatial text, subject text, tableOfContents text, temporal text, title text, type text, valid text, institutionID text, collectionID text, datasetID text, institutionCode text, collectionCode text, datasetName text, ownerInstitutionCode text, basisOfRecord text, informationWithheld text, dataGeneralizations text, dynamicProperties text, occurrenceID text, catalogNumber text, recordNumber text, recordedBy text, individualCount text, organismQuantity text, organismQuantityType text, sex text, lifeStage text, reproductiveCondition text, behavior text, establishmentMeans text, occurrenceStatus text, preparations text, disposition text, associatedReferences text, associatedSequences text, associatedTaxa text, otherCatalogNumbers text, occurrenceRemarks text, organismID text, organismName text, organismScope text, associatedOccurrences text, associatedOrganisms text, previousIdentifications text, organismRemarks text, materialSampleID text, eventID text, parentEventID text, fieldNumber text, eventDate text, eventTime text, startDayOfYear text, endDayOfYear text, year text, month text, day text, verbatimEventDate text, habitat text, samplingProtocol text, samplingEffort text, sampleSizeValue text, sampleSizeUnit text, fieldNotes text, eventRemarks text, locationID text, higherGeographyID text, higherGeography text, continent text, waterBody text, islandGroup text, island text, countryCode text, stateProvince text, county text, municipality text, locality text, verbatimLocality text, verbatimElevation text, verbatimDepth text, minimumDistanceAboveSurfaceInMeters text, maximumDistanceAboveSurfaceInMeters text, locationAccordingTo text, locationRemarks text, decimalLatitude text, decimalLongitude text, coordinateUncertaintyInMeters text, coordinatePrecision text,  pointRadiusSpatialFit text, verbatimCoordinateSystem text, verbatimSRS text, footprintWKT text, footprintSRS text, footprintSpatialFit text, georeferencedBy text, georeferencedDate text, georeferenceProtocol text, georeferenceSources text, georeferenceVerificationStatus text, georeferenceRemarks text, geologicalContextID text, earliestEonOrLowestEonothem text, latestEonOrHighestEonothem text, earliestEraOrLowestErathem text, latestEraOrHighestErathem text, earliestPeriodOrLowestSystem text, latestPeriodOrHighestSystem text, earliestEpochOrLowestSeries text, latestEpochOrHighestSeries text, earliestAgeOrLowestStage text, latestAgeOrHighestStage text, lowestBiostratigraphicZone text, highestBiostratigraphicZone text, lithostratigraphicTerms text, group1 text, formation text, member text, bed text, identificationID text, identificationQualifier text, typeStatus text, identifiedBy text, dateIdentified text, identificationReferences text, identificationVerificationStatus text, identificationRemarks text, taxonID text, scientificNameID text, acceptedNameUsageID text, parentNameUsageID text, originalNameUsageID text, nameAccordingToID text, namePublishedInID text, taxonConceptID text, scientificName text, acceptedNameUsage text, parentNameUsage text, originalNameUsage text, nameAccordingTo text, namePublishedIn text, namePublishedInYear text, higherClassification text, kingdom text, phylum text, class text, order1 text, family text, genus text, subgenus text, specificEpithet text, infraspecificEpithet text, taxonRank text, verbatimTaxonRank text, vernacularName text, nomenclaturalCode text, taxonomicStatus text, nomenclaturalStatus text, taxonRemarks text, datasetKey text, publishingCountry text, lastInterpreted text, elevation text, elevationAccuracy text, depth text, depthAccuracy text, distanceAboveSurface text, distanceAboveSurfaceAccuracy text, issue text, mediaType text, hasCoordinate text, hasGeospatialIssues text, taxonKey text, kingdomKey text, phylumKey text, classKey text, orderKey text, familyKey text, genusKey text, subgenusKey text, speciesKey text, species text, genericName text, typifiedName text, protocol text, lastParsed text, lastCrawled text, repatriated text) with (fillfactor=50);

            copy gbifdata from '''+"'"+occurencesFileName+"'"+'''  with null '';
            delete from '''+tmpBaseDataTable+''' where gbifid='gbifID';
            alter table '''+tmpBaseDataTable+''' alter column gbifID type bigint using gbifID::bigint, add constraint gbifid_pk primary key(gbifid);

            alter table '''+tmpBaseDataTable+''' add column clean_sciName text, add column canonicalForm text, add column observation_id bigint, add column recommendation_id bigint, add column commonname_reco_id bigint, add column external_url text, add column eventDate1 timestamp without time zone, add column lastCrawled1 timestamp without time zone, add column lastInterpreted1 timestamp without time zone, add column dateIdentified1 timestamp without time zone, add column place_name text, add column group_id bigint, add column habitat_id bigint, add column topology geometry, alter column  decimallongitude type numeric USING NULLIF(decimallongitude, '')::numeric, alter column decimallatitude type numeric USING NULLIF(decimallatitude, '')::numeric, add column license1 bigint, to_update boolean;

            update '''+tmpBaseDataTable+''' set eventDate1=to_date(eventDate, 'yyyy-MM-ddTHH:miZ'), lastCrawled1=to_date(lastCrawled, 'yyyy-MM-ddTHH:miZ'), lastInterpreted1=to_date(lastInterpreted, 'yyyy-MM-ddTHH:miZ'), dateIdentified1=to_date(dateIdentified, 'yyyy-MM-ddTHH:miZ'), external_url = 'http://www.gbif.org/occurrence/'|| gbifId, place_name=concat_ws(', ', locality, stateProvince, county), topology=CASE WHEN decimallatitude is not null and decimallongitude is not null THEN ST_SetSRID(ST_MakePoint(decimallongitude, decimallatitude), 4326) ELSE NULL END, basisOfRecord=CASE WHEN basisOfRecord IS null THEN 'HUMAN_OBSERVATION' ELSE basisOfRecord END, protocol= CASE WHEN protocol IS null THEN 'DWC_ARCHIVE' ELSE protocol END;

            update '''+tmpBaseDataTable+''' set to_update = 't', observation_id=o.id from observation o where o.external_id::bigint = gbifId;
            update '''+tmpBaseDataTable+''' set observation_id=nextval('observation_id_seq') where to_update != 't';

            update '''+tmpBaseDataTable+''' set license1= CASE WHEN rights like '%/publicdomain/%' THEN '''+License.findByName('CC_PUBLIC_DOMAIN').id+''' WHEN rights like '%/by/%' THEN '''+License.findByName('CC_BY').id+'''  WHEN rights like '%/by-sa/%' THEN '''+License.findByName('CC_BY_SA').id+'''  WHEN rights like '%/by-nc/%' or rights='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN '''+License.findByName('CC_BY_NC').id+'''  WHEN rights like '%/by-nc-sa/%' THEN '''+License.findByName('CC_BY_NC_SA').id+'''  WHEN rights like '%/by-nc-nd/%' THEN '''+License.findByName('CC_BY_NC_ND').id+''' WHEN rights like '%/by-nd/%' THEN '''+License.findByName('CC_BY_ND').id+'''  ELSE '''+License.findByName('CC_BY').id+''' END;


            drop table  if exists '''+tmpNewBaseDataTable+''';
            create table '''+tmpNewBaseDataTable+''' as select g.*,a.data from '''+tmpBaseDataTable+''' g join  (select gbifID, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifID as gbifID, abstract, accessRights, accrualMethod, accrualPeriodicity, accrualPolicy, alternative, audience, available, bibliographicCitation, conformsTo, contributor, coverage, created, creator, date, dateAccepted, dateCopyrighted, dateSubmitted, description, educationLevel, extent, format, hasFormat, hasPart, hasVersion, identifier, instructionalMethod, isFormatOf, isPartOf, isReferencedBy, isReplacedBy, isRequiredBy, isVersionOf, issued, language, license, mediator, medium, modified, provenance, publisher, references1 as references, relation, replaces, requires, rights, rightsHolder, source, spatial, subject, tableOfContents, temporal, title, type, valid, institutionID, collectionID, datasetID, institutionCode, collectionCode, datasetName, ownerInstitutionCode, basisOfRecord, informationWithheld, dataGeneralizations, dynamicProperties, occurrenceID, catalogNumber, recordNumber, recordedBy, individualCount, organismQuantity, organismQuantityType, sex, lifeStage, reproductiveCondition, behavior, establishmentMeans, occurrenceStatus, preparations, disposition, associatedReferences, associatedSequences, associatedTaxa, otherCatalogNumbers, occurrenceRemarks, organismID, organismName, organismScope, associatedOccurrences, associatedOrganisms, previousIdentifications, organismRemarks, materialSampleID, eventID, parentEventID, fieldNumber, eventDate, eventTime, startDayOfYear, endDayOfYear, year, month, day, verbatimEventDate, habitat, samplingProtocol, samplingEffort, sampleSizeValue, sampleSizeUnit, fieldNotes, eventRemarks, locationID, higherGeographyID, higherGeography, continent, waterBody, islandGroup, island, countryCode, stateProvince, county, municipality, locality, verbatimLocality, verbatimElevation, verbatimDepth, minimumDistanceAboveSurfaceInMeters, maximumDistanceAboveSurfaceInMeters, locationAccordingTo, locationRemarks, decimalLatitude, decimalLongitude,  coordinateUncertaintyInMeters, coordinatePrecision, pointRadiusSpatialFit, verbatimCoordinateSystem, verbatimSRS, footprintWKT, footprintSRS, footprintSpatialFit, georeferencedBy, georeferencedDate, georeferenceProtocol, georeferenceSources, georeferenceVerificationStatus, georeferenceRemarks, geologicalContextID, earliestEonOrLowestEonothem, latestEonOrHighestEonothem, earliestEraOrLowestErathem, latestEraOrHighestErathem, earliestPeriodOrLowestSystem, latestPeriodOrHighestSystem, earliestEpochOrLowestSeries, latestEpochOrHighestSeries, earliestAgeOrLowestStage, latestAgeOrHighestStage, lowestBiostratigraphicZone, highestBiostratigraphicZone, lithostratigraphicTerms, group1 as group, formation, member, bed, identificationID, identificationQualifier, typeStatus, identifiedBy, dateIdentified, identificationReferences, identificationVerificationStatus, identificationRemarks, taxonID, scientificNameID, acceptedNameUsageID, parentNameUsageID, originalNameUsageID, nameAccordingToID, namePublishedInID, taxonConceptID, scientificName, acceptedNameUsage, parentNameUsage, originalNameUsage, nameAccordingTo, namePublishedIn, namePublishedInYear, higherClassification, kingdom, phylum, class, order1 as order, family, genus, subgenus, specificEpithet, infraspecificEpithet, taxonRank, verbatimTaxonRank, vernacularName, nomenclaturalCode, taxonomicStatus, nomenclaturalStatus, taxonRemarks, datasetKey, publishingCountry, lastInterpreted, elevation, elevationAccuracy, depth, depthAccuracy, distanceAboveSurface, distanceAboveSurfaceAccuracy, issue, mediaType, hasCoordinate, hasGeospatialIssues, taxonKey, kingdomKey, phylumKey, classKey, orderKey, familyKey, genusKey, subgenusKey, speciesKey, species, genericName, typifiedName, protocol, lastParsed, lastCrawled, repatriated ) d))::text as data from gbifdata) a on g.gbifid=a.gbifid order by g.gbifid;

            alter table '''+tmpNewBaseDataTable+''' alter column gbifID type bigint using gbifID::bigint, add constraint gbifid_new_pk primary key(gbifid);
            alter table '''+tmpNewBaseDataTable+''' add column key text;
            update '''+tmpNewBaseDataTable+''' set key=concat(scientificname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
            ''');
            
            uploadLog << "\nCreating distinct sciName table for parsing";
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_parsedNamess);
            conn.executeUpdate("CREATE TABLE "+tmpBaseDataTable_parsedNamess+"(id serial primary key, sciName text, clean_sciName text, canonicalForm text, species text, genus text, family text, order1 text, class text, phylum text, kingdom text, commonName text, taxonrank text, taxonId bigint, acceptedId bigint, recommendation_id bigint)");
            conn.executeInsert("INSERT INTO "+ tmpBaseDataTable_parsedNamess +  " (sciName, species, genus, family, order1, class, phylum, kingdom, commonname, taxonrank) select scientificname, species, genus, family, order1, class, phylum, kingdom, vernacularname,taxonrank from "+tmpNewBaseDataTable + " group by scientificname, species, genus, family, order1, class,phylum,kingdom, vernacularname,taxonrank");
            conn.execute('''
            alter table '''+tmpBaseDataTable_parsedNamess+''' add column key text;
            update '''+tmpBaseDataTable_parsedNamess+''' set key=concat(sciname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
            ''')


            uploadLog << "\nTime taken for creating annotations ${((new Date()).getTime() - startTime.getTime())/1000} sec"

            uploadLog << "\nPopulating with canonicalform and taxonIds";
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_namesList);
            conn.executeUpdate("CREATE TABLE "+tmpBaseDataTable_namesList+"(id bigint, sciName text, clean_sciName text, canonicalForm text, commonname text, species text, genus text, family text, order1 text, class text, phylum text, kingdom text, taxonId bigint, acceptedId bigint, taxonrank varchar(255))");
            conn.execute("copy "+tmpBaseDataTable_namesList+" from "+"'"+namesFileName+"'"+"  with null '' delimiter '\t' csv header");

conn.execute('''
alter table '''+tmpBaseDataTable_namesList+''' add column key text;
update '''+tmpBaseDataTable_namesList+''' set key=concat(sciname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
''')

            conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " as x set canonicalForm = n.canonicalForm, taxonId = n.taxonId, acceptedId = n.acceptedId from "+tmpBaseDataTable_namesList+" n where n.key=x.key");

            uploadLog << "\nTime taken for creating annotations ${((new Date()).getTime() - startTime.getTime())/1000} sec"


        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for creating tables ${((new Date()).getTime() - startTime.getTime())/1000} sec"

        ///////////////////////////
        //Parsing Names
        ///////////////////////////
        uploadLog << "\nStarting parsing distinct sciNames";
        NamesParser namesParser = new NamesParser();
        SUser currentUser = springSecurityService.currentUser;
        List resultObv = [];
        int limit = 5000, offset 
        def noOfSciNames, noOfCommonNames;
        
        Date s = new Date();
        Date t_date = new Date();
/*        while(true) {
            s = new Date();

            try {
                conn = new Sql(dataSource);
                resultObv = conn.rows("select * from " + tmpBaseDataTable_parsedNamess + " order by id limit " + limit + " offset " + offset);
            } finally {
                conn.close();
            }

            if(!resultObv) break;

            uploadLog << "\n\n-----------------------------------------------"
            uploadLog << "\n limit : ${limit}, offset : ${offset}";

            def names = resultObv.collect { it.sciName };
            def parsedNames;
            Date n = new Date();
            try {
                parsedNames = namesParser.parse(names)
            } catch (Exception e) {
                uploadLog << "\n"+e.getMessage();
                log.error e.printStackTrace();
            }
            uploadLog << "\nTime taken for parsing ${limit} names ${((new Date()).getTime() - n.getTime())/1000} sec"

            uploadLog << "\nUpdating each distinct name with canonicalForm";
            n = new Date();
            try {
                conn = new Sql(dataSource);
                resultObv.eachWithIndex { t, index ->
                    conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " set canonicalForm = :canonicalForm, clean_sciName=:clean_sciName where sciName = :sciName and species = :species and genus=:genus and family=:family and order1=:order1 and class=:class and phylum=:phylum and kingdom=:kingdom and commonname=:commonname and taxonrank=:taxonrank", [canonicalForm:parsedNames[index]?.canonicalForm, clean_sciName:parsedNames[index]?.name, sciName:t.sciName, species:t.species, genus:t.genus, family:t.family, order1:t.order1, class:t.class, phylum:t.phylum, kingdom:t.kingdom, commonname:t.commonname, taxonrank:t.taxonrank]);
                }
            } finally {
                conn.close();
            }
            uploadLog << "\nTime taken for updating ${limit} canonicalForms ${((new Date()).getTime() - n.getTime())/1000} sec"

            resultObv.clear();
            offset = offset + limit;
            uploadLog << "\nTime taken for parsing and updating ${limit} distinct names ${((new Date()).getTime() - s.getTime())/1000} sec"
        }
        uploadLog << "\nTime taken for parsing and updating canonical forms for distinct names is ${((new Date()).getTime() - t_date.getTime())/1000} sec"
*/
        uploadLog << "\nInserting new names into recommendations";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            uploadLog << "\nInserting new sci names into recommendations";
            noOfSciNames = conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name,taxon_concept_id, accepted_name_id, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.canonicalform, 't', t.taxonId, t.acceptedId, lower(t.canonicalForm), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on lower(t.canonicalForm) = r.lowercase_name and (t.taxonId=r.taxon_concept_id or (t.taxonId is null and r.taxon_concept_id is null)) and r.is_scientific_name='t' where r.name is null and t.canonicalForm is not null group by t.canonicalForm, t.taxonId,t.acceptedId");

            //handling canonical form null by taking in sciname as is
            noOfSciNames += conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name,taxon_concept_id, accepted_name_id, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.sciname, 't', t.taxonId, t.acceptedId, lower(t.sciname), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on t.sciName=r.name where r.name is null and t.canonicalForm is null and t.sciname is not null");

            println noOfSciNames;
            uploadLog << "\nInserting new common names into recommendations";
            noOfCommonNames = conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.commonname, 'f', lower(t.commonname), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on lower(t.commonname) = r.lowercase_name and (r.is_scientific_name='f' or r.is_scientific_name is null) and r.language_id=:defaultLanguageId where r.name is null and t.commonname is not null group by t.commonname", [defaultLanguageId:Language.getLanguage().id]);
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for inserting new recommendations ${noOfSciNames.size()} and ${noOfCommonNames.size()} ${((new Date()).getTime() - s.getTime())/1000} sec"

        uploadLog << "\nUpdating all sciNames with their recommendaitonIds";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //FIX:sciName could be repeated in parsed_names table
            conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " set recommendation_id = r.id from recommendation r where ((canonicalform is not null and r.lowercase_name = lower(canonicalform)) or (sciname is not null and r.lowercase_name = lower(sciname))) and ((taxonId is null and r.taxon_concept_id is null) or (taxonId = r.taxon_concept_id)) and ((acceptedId is null and r.accepted_name_id is null) or (acceptedId = r.accepted_name_id))");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all canonicalForms ${((new Date()).getTime() - s.getTime())/1000} sec"


/*        uploadLog << "\nUpdating all sciNames with their canonicalForm";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //FIX:sciName could be repeated in parsed_names table
            conn.executeUpdate("update " + tmpNewBaseDataTable + " set canonicalForm = t1.canonicalForm, clean_sciName = t1.clean_sciName from " + tmpBaseDataTable_parsedNamess + " t1 where t1.sciName = scientificname");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all canonicalForms ${((new Date()).getTime() - s.getTime())/1000} sec"
*/

        uploadLog << "\nUpdating all sciNames with their reco ids";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            conn.executeUpdate("update " + tmpNewBaseDataTable + " as g set recommendation_id = t1.recommendation_id, group_id=t2.group_id, habitat_id=:defaultHabitatId from "+tmpBaseDataTable_parsedNamess+" t1 join taxonomy_definition t2 on t1.taxonid is not null and t1.taxonid = t2.id where t1.key=g.key",  [defaultHabitatId:Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id]) ;

            //handling taxonid null case
            conn.executeUpdate("update " + tmpNewBaseDataTable +"  as g set recommendation_id = t1.recommendation_id, group_id=:defaultSpeciesGroupId,habitat_id=:defaultHabitatId from gbifdata_parsed_names  t1 where g.key=t1.key and t1.taxonid is null and t1.recommendation_id is not null and g.scientificname is not null",  ['defaultSpeciesGroupId':SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id, 'defaultHabitatId':Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id]);

            conn.executeUpdate("update " + tmpNewBaseDataTable + " set commonname_reco_id = t1.id from recommendation t1 where t1.name = vernacularname;") ;
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all recoids ${((new Date()).getTime() - s.getTime())/1000} sec"


        uploadLog << "\nInserting observation and creating recovotes";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //TODO: this is risky as any other obv creation during this time will happen without constraints
            conn.executeUpdate("ALTER TABLE observation DISABLE TRIGGER ALL ;");
            conn.executeUpdate("insert into observation (id, version, access_rights, agree_terms, author_id, basis_of_record, catalog_number, checklist_annotations, created_on, dataset_id, external_dataset_key, external_id, external_url, feature_count, flag_count, from_date, geo_privacy, group_id, habitat_id, information_withheld, is_checklist, is_deleted, is_locked, is_showable, language_id, last_crawled, last_interpreted, last_revised, latitude, license_id, location_accuracy, location_scale, longitude, max_voted_reco_id, notes, original_author, place_name, protocol, publishing_country, rating, reverse_geocoded_name, search_text, source_id, to_date, topology, via_code, via_id, visit_count) select observation_id, 0, accessRights, 't', "+currentUser.id+", basisOfRecord, catalogNumber, data, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, "+dataset.id+", datasetKey, gbifID, external_url, 0, 0, eventDate1, 'f', COALESCE(group_id, "+SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id+"), COALESCE(habitat_id, "+Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id+" ), informationWithheld, 'f', 'f', 'f', 'f', "+Language.getLanguage().id+", lastCrawled1, lastInterpreted1, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, decimalLatitude, license1, 'Approximate', 'APPROXIMATE', decimalLongitude, recommendation_id, null, recordedBy, place_name, 'DWC_ARCHIVE', publishingCountry, 0, place_name, null, null, eventDate1, topology, collectionCode, collectionID, 0 from "+tmpNewBaseDataTable+" where decimallatitude is not null and decimallongitude is not null and eventDate1 is not null and decimallatitude>=26.647 and decimallatitude<=28.280 and decimallongitude>=88.692 and decimallongitude<=92.170 and to_update != 't' order by gbifId");

            println "Inserted observations "

            conn.executeUpdate("update observation set access_rights = tmp.access_rights, set agree_terms=tmp.agree_terms, set author_id=tmp.author_id, set basis_of_record=tmp.basis_of_record, set catalog_number=tmp.catalog_number, set checklist_annotations=tmp.checklist_annotations, set created_on=tmp.created_on, set dataset_id=tmp.dataset_id, set external_dataset_key=tmp.external_dataset_key, set external_id=tmp.external_id, set external_url=tmp.external_url, set feature_count=tmp.feature_count, set flag_count=tmp.flag_count, set from_date= tmp.from_date, set geo_privacy=tmp.geo_privacy, set group_id=tmp.group_id, set habitat_id=tmp.habitat_id, set information_withheld=tmp.information_withheld, set is_checklist=tmp.is_checklist, set is_deleted=tmp.is_deleted, set is_locked=tmp.is_locked, set is_showable=tmp.is_showable, set language_id=tmp.language_id, set last_crawled=tmp.last_crawled, set last_interpreted=tmp.last_interpreted, set last_revised=tmp.last_revised, set latitude=tmp.latitude, set license_id=tmp.license_id, set location_accuracy=tmp.location_accuracy, set location_scale=tmp.location_scale, set longitude=tmp.longitude, set max_voted_reco_id=tmp.max_voted_reco_id, set notes=tmp.notes, set original_author=tmp.original_author, set place_name=tmp.place_name, set protocol=tmp.protocol, set publishing_country=tmp.publishingCountry, set rating=tmp.rating, set reverse_geocoded_name=tmp.reverse_geocoded_name, set search_text=tmp.search_text, set source_id=tmp.source_id, set to_date=tmp.to_date, set topology=tmp.topology, set via_code=tmp.via_code, set via_id=tmp.via_id, set visit_count=tmp.visit_count) select accessRights as access_rights, 't' as agree_terms, "+currentUser.id+" as author_id, basisOfRecord as basis_of_record, catalogNumber as catalog_number, data as checklist_annotations, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp as created_on, "+dataset.id+" as dataset_id, datasetKey as external_dataset_key, gbifID as external_id, external_url as external_url, 0 as feature_count, 0 as flag_count, eventDate1 as from_date, 'f' as geo_privacy, COALESCE(group_id, "+SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id+") as group_id, COALESCE(habitat_id, "+Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id+" ) as habitat_id, informationWithheld as information_withheld, 'f' as is_checklist, 'f' as is_deleted, 'f' as is_locked, 'f' as is_showable, "+Language.getLanguage().id+" as language_id, lastCrawled1 as last_crawled, lastInterpreted1 as last_interpreted, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp as last_revised, decimalLatitude as latitude, license1 as license_id, 'Approximate' as location_accuracy, 'APPROXIMATE' as location_scale, decimalLongitude as longitude, recommendation_id as max_voted_reco_id, null as notes, recordedBy as original_author, place_name as place_name, 'DWC_ARCHIVE' as protocol, publishingCountry as publishing_country, 0 as rating, place_name as reverse_geocoded_name, null as search_text, null as source_id, eventDate1 as to_date, topology, collectionCode as via_code, collectionID as via_id, 0 as visit_count from "+tmpNewBaseDataTable+" where decimallatitude is not null and decimallongitude is not null and eventDate1 is not null and decimallatitude>=26.647 and decimallatitude<=28.280 and decimallongitude>=88.692 and decimallongitude<=92.170 and to_update = 't' order by gbifId");
            //, [datasetId:dataset.id, languageId:Language.getLanguage().id, defaultHabitatId:Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id, defaultSpeciesGroupId:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id]);
            conn.executeUpdate("ALTER TABLE observation ENABLE TRIGGER ALL ;");
            println "updated old observations "

            //conn.executeUpdate("update observation set protocol=CASE WHEN protocol='TAPIR' or protocol='DIGIR_MANIS' THEN 'OTHER' ELSE protocol END");

            conn.executeUpdate("delete from recommendation_vote where observation_id = id from "+tmpNewBaseDataTable+", observation where to_update='t' and recommendation_id is not null and observation_id is not null and observation_id=id");

            conn.executeUpdate("insert into recommendation_vote(id, version, author_id, confidence, observation_id, recommendation_id, user_weight, voted_on, comment, common_name_reco_id, given_sci_name, given_common_name, original_author) select nextval('hibernate_sequence'), 0, "+currentUser.id+", 'CERTAIN', observation_id, recommendation_id, 0, COALESCE(dateIdentified1, '"+((new Date()).format('yyyy-MM-dd HH:mm:ss.SSS'))+"'), null, commonname_reco_id,  scientificname as given_sci_name, vernacularname as given_common_name, identifiedby as original_author from "+tmpNewBaseDataTable+", observation where recommendation_id is not null and observation_id is not null and observation_id=id");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for creating observations and recovote ${((new Date()).getTime() - s.getTime())/1000} sec"


        uploadLog << "\nInserting resources";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            conn.execute('''
            drop table if exists '''+tmpBaseDataTable_multimedia+''';
            alter table resource drop column IF EXISTS gbifID;
            create table '''+tmpBaseDataTable_multimedia+'''(id serial primary key, gbifID   text, type     text,  format   text,  identifier   text,  references1 text, title  text,  description   text, created   text, creator   text, contributor   text, publisher   text,   audience    text,   source  text, license text, rightsHolder text);

            copy '''+tmpBaseDataTable_multimedia+'''(gbifID,type,format,identifier,references1,title,description,created,creator,contributor,publisher,audience,source,license,rightsHolder) from '''+"'"+multimediaFileName+"';"+''' ;
            delete from '''+tmpBaseDataTable_multimedia+''' where gbifID='gbifID';
            alter table '''+tmpBaseDataTable_multimedia+''' alter column gbifID type bigint using gbifID::bigint;

            alter table '''+tmpBaseDataTable_multimedia+''' add column annotations text, add column type1 text, add column license1 bigint, add column to_update boolean, resource_id bigint;

            update '''+tmpBaseDataTable_multimedia+''' set type1= CASE WHEN type='StillImage' THEN 'IMAGE'  WHEN type='MovingImage' THEN 'VIDEO' WHEN type='SOUND' THEN 'AUDIO' ELSE 'IMAGE' END, license1=CASE WHEN license like '%/publicdomain/%' THEN '''+License.findByName('CC_PUBLIC_DOMAIN').id+''' WHEN license like '%/by/%' THEN '''+License.findByName('CC_BY').id+'''  WHEN license like '%/by-sa/%' THEN '''+License.findByName('CC_BY_SA').id+'''  WHEN license like '%/by-nc/%' or license='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN '''+License.findByName('CC_BY_NC').id+'''  WHEN license like '%/by-nc-sa/%' THEN '''+License.findByName('CC_BY_NC_SA').id+'''  WHEN license like '%/by-nc-nd/%' THEN '''+License.findByName('CC_BY_NC_ND').id+''' WHEN license like '%/by-nd/%' THEN '''+License.findByName('CC_BY_ND').id+'''  ELSE '''+License.findByName('CC_BY').id+''' END, identifier= CASE WHEN identifier IS NULL THEN '''+"'"+grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg"+"'"+''' ELSE identifier END;

            update '''+tmpBaseDataTable_multimedia+''' set annotations = g.data from (select id as xid, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifId as gbifId, type, identifier, format, license, references1 as references, rightsHolder, title, publisher, source, description, created, creator, contributor, audience) d))::text as data from gbifdata_multimedia) as  g where g.xid=id;

            update '''+tmpBaseDataTable_multimedia+''' set to_update = 't', set resource_id = r.id from resource r where r.gbifid = gbifID;
            
            delete from resource_contributor where resource_contributors_id in (select resource_id from '''+tmpBaseDataTable_multimedia+''' where to_update = 't');
            delete from observation_resource where resource_id in (select resource_id from '''+tmpBaseDataTable_multimedia+''' where to_update = 't');

            delete from resource where gbifid = gbifID from '''+tmpBaseDataTable_multimedia+''';

            insert into resource (id, version,description,file_name,mime_type,type,url,rating,upload_time,uploader_id,context,language_id,access_rights,annotations,gbifID,license_id) select nextval('hibernate_sequence'), 0,title,'i',format,type1,identifier,0,'''+"'"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'"+'''::timestamp,'''+currentUser.id+''','OBSERVATION','''+Language.getLanguage().id+''',license,annotations,gbifID,license1  from '''+tmpBaseDataTable_multimedia+''' where identifier is not null;

            insert into observation_resource(observation_id, resource_id) select o.id, r.id from observation o, resource r where cast(o.external_id as integer)=r.gbifID;

            insert into contributor(id, name) select nextval('hibernate_sequence') as id, rightsholder from '''+tmpBaseDataTable_multimedia+''' where rightsholder is not null and rightsholder not in (select distinct(name) from contributor) group by rightsholder;

        insert into resource_contributor(resource_contributors_id,contributor_id) select r.id, c.id from '''+tmpBaseDataTable_multimedia+''' o, resource r, contributor c where o.gbifId=r.gbifID and o.rightsholder=c.name;

        ALTER TABLE observation DISABLE TRIGGER ALL;
        update observation set is_showable='t' where id in (select o.observation_id from observation_resource o, resource r where o.resource_id=r.id and r.gbifid is not null);

        ''');

           conn.execute("delete from observation as o1 where o1.id in (select id from observation left outer join observation_resource on id=observation_id where external_id is not null and max_voted_reco_id is null and resource_id is null)");

           conn.execute('''
           update observation set no_of_images = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='IMAGE' group by observation_id) g where g.observation_id = id;
           update observation set no_of_videos = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='VIDEO' group by observation_id) g where g.observation_id = id;
           update observation set no_of_audio = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='AUDIO' group by observation_id) g where g.observation_id = id;

           create table tmp as select observation_id, count(*) as count from recommendation_vote group by observation_id;

           update observation set no_of_identifications = g.count from (select * from tmp) g where g.observation_id=id;

           drop table tmp;
                    
            create table tmp as select resource_id, observation_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from observation_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='resource' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref order by observation_id asc, avg desc, resource_id asc;

            update observation set repr_image_id = g.resource_id from (select b.observation_id,b.resource_id from (select observation_id, max(avg) as max_avg from tmp group by observation_id) a inner join tmp b on a.observation_id=b.observation_id where b.avg=a.max_avg) g where g.observation_id=id;

            drop table tmp;


           '''
           );
        conn.executeUpdate("ALTER TABLE observation ENABLE TRIGGER ALL ;");

        } finally {
        conn.close();
        }
        uploadLog << "\nTime taken for resources ${((new Date()).getTime() - s.getTime())/1000} sec"
         


        /*uploadLog << "\n Publishing search index"
        d = new Date();
        try {
        utilsService.logSql {
        observationsSearchService.publishSearchIndex(obvs, true);
        }
        uploadLog << "\nTime taken for search index commit : ${((new Date()).getTime() - d.getTime())/1000} sec"
        } catch (Exception e) {
        log.error e.printStackTrace();
        }*/


        try {
            //conn = new Sql(dataSource);
            //conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);	
            //conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_parsedNamess);	
        } finally {
            //conn.close();
            log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
            dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
        }

        uploadLog << "\n\n----------------------------------------------------------------------";
        uploadLog << "\nTotal time taken for uploading ${((new Date()).getTime() - startTime.getTime())/1000} sec"
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

      
        
		def allDatasetCountQuery = "select count(*) from Dataset obv " +((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
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
                def datasetInstance = Dataset.get(params.id.toLong())
                if (datasetInstance) {
                    //datasetInstance.removeResourcesFromSpecies()
                    boolean isFeatureDeleted = Featured.deleteFeatureOnObv(datasetInstance, springSecurityService.currentUser, utilsService.getUserGroup(params))
                    if(isFeatureDeleted && utilsService.ifOwns(datasetInstance.author)) {
                        def mailType = activityFeedService.INSTANCE_DELETED
                        try {
                            datasetInstance.isDeleted = true;

                            Observation.findAllByDataset(datasetInstance).each {
                                it.isDeleted = true; 
                                if(!it.save(flush:true)){
                                    it.errors.allErrors.each { log.error it } 
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

} 
