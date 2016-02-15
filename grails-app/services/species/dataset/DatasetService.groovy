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
import species.NamesParser;
import grails.converters.JSON

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
        File directory = zipF.getParentFile();
        File metadataFile;
        if(FilenameUtils.getExtension(zipF.getName()).equals('zip')) {
            def ant = new AntBuilder().unzip( src: zipFile,
            dest: destDir, overwrite:true)
            directory = new File(destDir, FilenameUtils.removeExtension(zipF.getName()));
            if(!directory.exists()) {
                directory = destDir;
            }
            metadataFile = new File(directory, "metadata.xml");
        }

        
        File uploadLog = new File(destDir, 'upload.log');
        if(uploadLog.exists()) uploadLog.delete();

        Date startTime = new Date();
        if(directory) {
            if(metadataFile) {
                uploadLog << "\nUploading dataset in DwCA format present at : ${zipF.getAbsolutePath()}";
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
            }

            params['author'] = springSecurityService.currentUser; 
            params['type'] = DatasetType.OBSERVATIONS;
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
                if(params.datasource.title.contains('Global Biodiversity Information Facility')) {
                    importGBIFObservations(dataset, directory, uploadLog)
                } else {
                    importDWCObservations(dataset, directory, uploadLog);
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
/*            uploadLog << "\nCreating base table for ${occurencesFileName}";

            conn.execute('''
            drop table  if exists '''+tmpBaseDataTable+''';
            create table '''+tmpBaseDataTable+'''(gbifID text, abstract text, accessRights text, accrualMethod text, accrualPeriodicity text, accrualPolicy text, alternative text, audience text, available text, bibliographicCitation text, conformsTo text, contributor text, coverage text, created text, creator text, date text, dateAccepted text, dateCopyrighted text, dateSubmitted text, description text, educationLevel text, extent text, format text, hasFormat text, hasPart text, hasVersion text, identifier text, instructionalMethod text, isFormatOf text, isPartOf text, isReferencedBy text, isReplacedBy text, isRequiredBy text, isVersionOf text, issued text, language text, license text, mediator text, medium text, modified text, provenance text, publisher text, references1 text, relation text, replaces text, requires text, rights text, rightsHolder text, source text, spatial text, subject text, tableOfContents text, temporal text, title text, type text, valid text, acceptedNameUsage text, acceptedNameUsageID text, associatedres text, associatedReferences text, associatedSequences text, associatedTaxa text, basisOfRecord text, bed text, behavior text, catalogNumber text, class text, collectionCode text, collectionID text, continent text, countryCode text, county text, dataGeneralizations text, datasetID text, datasetName text, dateIdentified text, day text, decimalLatitude text, decimalLongitude text, disposition text, dynamicProperties text, earliestAgeOrLowestStage text, earliestEonOrLowestEonothem text, earliestEpochOrLowestSeries text, earliestEraOrLowestErathem text, earliestPeriodOrLowestSystem text, endDayOfYear text, establishmentMeans text, eventDate text, eventID text, eventRemarks text, eventTime text, family text, fieldNotes text, fieldNumber text, footprintSRS text, footprintSpatialFit text, footprintWKT text, formation text, genus text, geologicalContextID text, georeferencedDate text, georeferenceProtocol text, georeferenceRemarks text, georeferenceSources text, georeferenceVerificationStatus text, georeferencedBy text, group1 text, habitat text, higherClassification text, higherGeography text, higherGeographyID text, highestBiostratigraphicZone text, identificationID text, identificationQualifier text, identificationReferences text, identificationRemarks text, identificationVerificationStatus text, identifiedBy text, individualCount text, individualID text, informationWithheld text, infraspecificEpithet text, institutionCode text, institutionID text, island text, islandGroup text, kingdom text, latestAgeOrHighestStage text, latestEonOrHighestEonothem text, latestEpochOrHighestSeries text, latestEraOrHighestErathem text, latestPeriodOrHighestSystem text, lifeStage text, lithostratigraphicTerms text, locality text, locationAccordingTo text, locationID text, locationRemarks text, lowestBiostratigraphicZone text, materialSampleID text, maximumDistanceAboveSurfaceInMeters text, member text, minimumDistanceAboveSurfaceInMeters text, month text, municipality text, nameAccordingTo text, nameAccordingToID text, namePublishedIn text, namePublishedInID text, namePublishedInYear text, nomenclaturalCode text, nomenclaturalStatus text, occurrenceID text, occurrenceRemarks text, occurrenceStatus text, order1 text, originalNameUsage text, originalNameUsageID text, otherCatalogNumbers text, ownerInstitutionCode text, parentNameUsage text, parentNameUsageID text, phylum text, pointRadiusSpatialFit text, preparations text, previousIdentifications text, recordNumber text, recordedBy text, reproductiveCondition text, samplingEffort text, samplingProtocol text, scientificName varchar(2055), scientificNameID text, sex text, specificEpithet text, startDayOfYear text, stateProvince text, subgenus text, taxonConceptID text, taxonID text, taxonRank text, taxonRemarks text, taxonomicStatus text, typeStatus text, verbatimCoordinateSystem text, verbatimDepth text, verbatimElevation text, verbatimEventDate text, verbatimLocality text, verbatimSRS text, verbatimTaxonRank text, vernacularName text, waterBody text, year text, datasetKey text, publishingCountry text, lastInterpreted text, coordinateAccuracy text, elevation text, elevationAccuracy text, depth text, depthAccuracy text, distanceAboveSurface text, distanceAboveSurfaceAccuracy text, issue text, mediaType text, hasCoordinate text, hasGeospatialIssues text, taxonKey text, kingdomKey text, phylumKey text, classKey text, orderKey text, familyKey text, genusKey text, subgenusKey text, speciesKey text, species text, genericName text, typifiedName text, protocol text, lastParsed text, lastCrawled text ) with (fillfactor=50);
            copy gbifdata from '''+"'"+occurencesFileName+"'"+'''  with null '';
            delete from '''+tmpBaseDataTable+''' where gbifid='gbifID';
            alter table '''+tmpBaseDataTable+''' alter column gbifID type bigint using gbifID::bigint, add constraint gbifid_pk primary key(gbifid);

            alter table '''+tmpBaseDataTable+''' add column clean_sciName text, add column canonicalForm text, add column observation_id bigint, add column recommendation_id bigint, add column commonname_reco_id bigint, add column external_url text, add column eventDate1 timestamp without time zone, add column lastCrawled1 timestamp without time zone, add column lastInterpreted1 timestamp without time zone, add column dateIdentified1 timestamp without time zone, add column place_name text, add column group_id bigint, add column habitat_id bigint, add column topology geometry, alter column  decimallongitude type numeric USING NULLIF(decimallongitude, '')::numeric, alter column decimallatitude type numeric USING NULLIF(decimallatitude, '')::numeric, add column license1 bigint;

            update '''+tmpBaseDataTable+''' set eventDate1=to_date(eventDate, 'yyyy-MM-ddTHH:miZ'), lastCrawled1=to_date(lastCrawled, 'yyyy-MM-ddTHH:miZ'), lastInterpreted1=to_date(lastInterpreted, 'yyyy-MM-ddTHH:miZ'), dateIdentified1=to_date(dateIdentified, 'yyyy-MM-ddTHH:miZ'), external_url = 'http://www.gbif.org/occurrence/'|| gbifId, observation_id=nextval('observation_id_seq'), place_name=concat_ws(', ', locality, stateProvince, county), topology=CASE WHEN decimallatitude is not null and decimallongitude is not null THEN ST_SetSRID(ST_MakePoint(decimallongitude, decimallatitude), 4326) ELSE NULL END, basisOfRecord=CASE WHEN basisOfRecord IS null THEN 'HUMAN_OBSERVATION' ELSE basisOfRecord END, protocol= CASE WHEN protocol IS null THEN 'DWC_ARCHIVE' ELSE protocol END;
            
            update '''+tmpBaseDataTable+''' set license1= CASE WHEN rights like '%/publicdomain/%' THEN 821 WHEN rights like '%/by/%' THEN 822 WHEN rights like '%/by-sa/%' THEN 823 WHEN rights like '%/by-nc/%' or rights='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN 825 WHEN rights like '%/by-nc-sa/%' THEN 826 WHEN rights like '%/by-nc-nd/%' THEN 827 WHEN rights like '%/by-nd/%' THEN 824 ELSE 822 END;
            
            drop table  if exists '''+tmpNewBaseDataTable+''';
            create table '''+tmpNewBaseDataTable+''' as select g.*,a.data from '''+tmpBaseDataTable+''' g join  (select gbifID, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifID as gbifID, abstract, accessRights, accrualMethod, accrualPeriodicity, accrualPolicy, alternative, audience, available, bibliographicCitation, conformsTo, contributor, coverage, created, creator, date, dateAccepted, dateCopyrighted, dateSubmitted, description, educationLevel, extent, format, hasFormat, hasPart, hasVersion, identifier, instructionalMethod, isFormatOf, isPartOf, isReferencedBy, isReplacedBy, isRequiredBy, isVersionOf, issued, language, license, mediator, medium, modified, provenance, publisher, references1 as references, relation, replaces, requires, rights, rightsHolder, source, spatial, subject, tableOfContents, temporal, title, type, valid, acceptedNameUsage, acceptedNameUsageID, associatedres, associatedReferences, associatedSequences, associatedTaxa, basisOfRecord, bed, behavior, catalogNumber, class, collectionCode, collectionID, continent, countryCode, county, dataGeneralizations, datasetID, datasetName, dateIdentified, day, decimalLatitude, decimalLongitude, disposition, dynamicProperties, earliestAgeOrLowestStage, earliestEonOrLowestEonothem, earliestEpochOrLowestSeries, earliestEraOrLowestErathem, earliestPeriodOrLowestSystem, endDayOfYear, establishmentMeans, eventDate, eventID, eventRemarks, eventTime, family, fieldNotes, fieldNumber, footprintSRS, footprintSpatialFit, footprintWKT, formation, genus, geologicalContextID, georeferencedDate, georeferenceProtocol, georeferenceRemarks, georeferenceSources, georeferenceVerificationStatus, georeferencedBy, group1 as group, habitat, higherClassification, higherGeography, higherGeographyID, highestBiostratigraphicZone, identificationID, identificationQualifier, identificationReferences, identificationRemarks, identificationVerificationStatus, identifiedBy, individualCount, individualID, informationWithheld, infraspecificEpithet, institutionCode, institutionID, island, islandGroup, kingdom, latestAgeOrHighestStage, latestEonOrHighestEonothem, latestEpochOrHighestSeries, latestEraOrHighestErathem, latestPeriodOrHighestSystem, lifeStage, lithostratigraphicTerms, locality, locationAccordingTo, locationID, locationRemarks, lowestBiostratigraphicZone, materialSampleID, maximumDistanceAboveSurfaceInMeters, member, minimumDistanceAboveSurfaceInMeters, month, municipality, nameAccordingTo, nameAccordingToID, namePublishedIn, namePublishedInID, namePublishedInYear, nomenclaturalCode, nomenclaturalStatus, occurrenceID, occurrenceRemarks, occurrenceStatus, order1 as order, originalNameUsage, originalNameUsageID, otherCatalogNumbers, ownerInstitutionCode, parentNameUsage, parentNameUsageID, phylum, pointRadiusSpatialFit, preparations, previousIdentifications, recordNumber, recordedBy, reproductiveCondition, samplingEffort, samplingProtocol, scientificName, scientificNameID, sex, specificEpithet, startDayOfYear, stateProvince, subgenus, taxonConceptID, taxonID, taxonRank, taxonRemarks, taxonomicStatus, typeStatus, verbatimCoordinateSystem, verbatimDepth, verbatimElevation, verbatimEventDate, verbatimLocality, verbatimSRS, verbatimTaxonRank, vernacularName, waterBody, year,'http://www.gbif.org/dataset/'||datasetKey as datasetKey, publishingCountry, lastInterpreted, coordinateAccuracy, elevation, elevationAccuracy, depth, depthAccuracy, distanceAboveSurface, distanceAboveSurfaceAccuracy, issue, mediaType, hasCoordinate, hasGeospatialIssues, taxonKey, kingdomKey, phylumKey, classKey, orderKey, familyKey, genusKey, subgenusKey, speciesKey, species, genericName, typifiedName, protocol, lastParsed, lastCrawled ) d))::text as data from gbifdata) a on g.gbifid=a.gbifid order by g.gbifid;
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
*/

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
            noOfSciNames = conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name,taxon_concept_id, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.canonicalform, 't', t.taxonId, lower(t.canonicalForm), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on lower(t.canonicalForm) = r.lowercase_name and (t.taxonId=r.taxon_concept_id or (t.taxonId is null and r.taxon_concept_id is null)) and r.is_scientific_name='t' where r.name is null and t.canonicalForm is not null group by t.canonicalForm, t.taxonId");

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
            conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " set recommendation_id = r.id from recommendation r where r.lowercase_name = canonicalform and taxonId = r.taxon_concept_id");
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
            conn.executeUpdate("insert into observation (id, version, access_rights, agree_terms, author_id, basis_of_record, catalog_number, checklist_annotations, created_on, dataset_id, external_dataset_key, external_id, external_url, feature_count, flag_count, from_date, geo_privacy, group_id, habitat_id, information_withheld, is_checklist, is_deleted, is_locked, is_showable, language_id, last_crawled, last_interpreted, last_revised, latitude, license_id, location_accuracy, location_scale, longitude, max_voted_reco_id, notes, original_author, place_name, protocol, publishing_country, rating, reverse_geocoded_name, search_text, source_id, to_date, topology, via_code, via_id, visit_count) select observation_id, 0, accessRights, 't', "+currentUser.id+", basisOfRecord, catalogNumber, data, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, "+dataset.id+", datasetKey, gbifID, external_url, 0, 0, eventDate1, 'f', COALESCE(group_id, "+SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id+"), COALESCE(habitat_id, "+Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id+" ), informationWithheld, 'f', 'f', 'f', 'f', "+Language.getLanguage().id+", lastCrawled1, lastInterpreted1, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, decimalLatitude, license1, 'Approximate', 'APPROXIMATE', decimalLongitude, recommendation_id, null, recordedBy, place_name, protocol, publishingCountry, 0, place_name, null, null, eventDate1, topology, collectionCode, collectionID, 0 from "+tmpNewBaseDataTable+" where decimallatitude is not null and decimallongitude is not null and eventDate1 is not null and decimallatitude>=6.74678 and decimallatitude<=35.51769 and decimallongitude>=68.03215 and decimallongitude<=97.40238 order by gbifId");
            //, [datasetId:dataset.id, languageId:Language.getLanguage().id, defaultHabitatId:Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id, defaultSpeciesGroupId:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id]);
            conn.executeUpdate("ALTER TABLE observation ENABLE TRIGGER ALL ;");
            println "Inserted observations "

            conn.executeUpdate("update observation set protocol=CASE WHEN protocol='TAPIR' or protocol='DIGIR_MANIS' THEN 'OTHER' ELSE protocol END");

            conn.executeUpdate("insert into recommendation_vote select nextval('hibernate_sequence'), 0, 1, 'CERTAIN', observation_id, recommendation_id, 0, COALESCE(dateIdentified1, '"+((new Date()).format('yyyy-MM-dd HH:mm:ss.SSS'))+"'), null, commonname_reco_id, identifiedby from "+tmpNewBaseDataTable+", observation where recommendation_id is not null and observation_id is not null and observation_id=id");
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

            alter table '''+tmpBaseDataTable_multimedia+''' add column annotations text, add column type1 text, add column license1 bigint;

            update '''+tmpBaseDataTable_multimedia+''' set type1= CASE WHEN type='StillImage' THEN 'IMAGE'  WHEN type='MovingImage' THEN 'VIDEO' WHEN type='SOUND' THEN 'AUDIO' ELSE 'IMAGE' END, license1=CASE WHEN license like '%/publicdomain/%' THEN 821 WHEN license like '%/by/%' THEN 822 WHEN license like '%/by-sa/%' THEN 823 WHEN license like '%/by-nc/%' or license='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN 825 WHEN license like '%/by-nc-sa/%' THEN 826 WHEN license like '%/by-nc-nd/%' THEN 827 WHEN license like '%/by-nd/%' THEN 824 ELSE 822 END, identifier= CASE WHEN identifier IS NULL THEN '''+"'"+grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg"+"'"+''' ELSE identifier END;

            update '''+tmpBaseDataTable_multimedia+''' set annotations = g.data from (select id as xid, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifId as gbifId, type, identifier, format, license, references1 as references, rightsHolder, title, publisher, source, description, created, creator, contributor, audience) d))::text as data from gbifdata_multimedia) as  g where g.xid=id;

        alter table resource add column gbifID bigint;
        insert into resource (id, version,description,file_name,mime_type,type,url,rating,upload_time,uploader_id,context,language_id,access_rights,annotations,gbifID,license_id) select nextval('hibernate_sequence'), 0,title,'i',format,type1,identifier,0,'''+"'"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'"+'''::timestamp,'''+currentUser.id+''','OBSERVATION','''+Language.getLanguage().id+''',license,annotations,gbifID,license1  from '''+tmpBaseDataTable_multimedia+''' where identifier is not null;

        insert into observation_resource(observation_id, resource_id) select o.id, r.id from observation o, resource r where cast(o.external_id as integer)=r.gbifID;

        insert into contributor(id, name) select nextval('hibernate_sequence') as id, rightsholder from '''+tmpBaseDataTable_multimedia+''' where rightsholder is not null and rightsholder not in (select distinct(name) from contributor) group by rightsholder;

        insert into resource_contributor(resource_contributors_id,contributor_id) select r.id, c.id from '''+tmpBaseDataTable_multimedia+''' o, resource r, contributor c where o.gbifId=r.gbifID and o.rightsholder=c.name;

        update observation set is_showable='t' where id in (select o.observation_id from observation_resource o, resource r where o.resource_id=r.id and r.gbifid is not null);

        ''');

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
