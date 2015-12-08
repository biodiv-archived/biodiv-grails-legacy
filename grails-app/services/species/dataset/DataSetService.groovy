package species.dataset;

import species.sourcehandler.importer.DwCObservationImporter;
import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.taggable.TagLink;
import species.Classification;

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

class DataSetService extends AbstractMetadataService {

    static transactional = false

    def messageSource;
    def activityFeedService
    def obvUtilService;
    def observationsSearchService;

    DataSet create(params) {
        return super.create(DataSet.class, params);
    }

    DataSet update(DataSet dataSet, params) {
        params.remove('latitude')
        params.remove('longitude')
        def locationScale = params.remove('locationScale')

        super.update(dataSet, params);

        document.group = null
        document.habitat = null

        document.notes = params.description? params.description.trim() :null
        /*document.contributors = params.contributors? params.contributors.trim() :null
        document.attribution = params.attribution? params.attribution.trim() :null

        document.speciesGroups = []
        params.speciesGroup.each {key, value ->
        log.debug "Value: "+ value
        document.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
        }

        document.habitats  = []
        params.habitat.each {key, value ->
        document.addToHabitats(Habitat.read(value.toLong()));
        }*/
        return dataSet;
    }

    DataSet update(String dataSetEmlXmlFile, String occurenceFile) {
        String dataSetEmlXmlStr = new File(dataSetEmlXmlFile).text;

        def dataSetEmlXml = new XmlParser().parseText(dataSetEmlXmlStr);
        DataSet dataSet = new DataSet();
        Map params = readEML(dataSetEmlXml);

        /*        List
        params['publisher'] = 
        params['dataObjects'] = 
         */
    }

    Map readEML(def dataSetEmlXml) {
        def dataset = dataSetEmlXml.dataset;
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

        params['type'] = DataSetType.OBSERVATIONS; 

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

    void uploadDwCDataset(String directory) {
        /*String dataSetEmlXmlStr = new File(dataSetEmlXmlFile).text;

        def dataSetEmlXml = new XmlParser().parseText(dataSetEmlXmlStr);
        Map params = readEML(dataSetEmlXml);
        
        DataSet dataSet = new DataSet(params);
*/
        DwCObservationImporter dwcImporter = DwCObservationImporter.getInstance();
        List obvParamsList = dwcImporter.importObservationData(directory);
        //TODO:BATCH CONVERT AND CREATE OF OBSERVATION
        List resultObv = [];
        obvParamsList.each { obvParams ->
            obvParams['observation url'] = 'GBIF';
            obvUtilService.uploadObservation(null, obvParams, resultObv);
        }
        def obvs = resultObv.collect { Observation.read(it) }
        try {
            observationsSearchService.publishSearchIndex(obvs, true);
        } catch (Exception e) {
            log.error e.printStackTrace();
        }


/*        resultObv.each {
            dataSet.addToDataObjects(it);
        }

        try {
            if(!dataSet.hasErrors() && dataSet.save(flush:true)) {
                log.debug "Successfully created dataset : "+dataSet

                activityFeedService.addActivityFeed(dataSet, null, dataSet.author, activityFeedService.DATASET_CREATED);
            } else {
                dataSet.errors.allErrors.each { log.error it }
            }
        }catch(e) {
            log.error "error in creating dataset"
            e.printStackTrace();
        }
        return dataSet;
  */  }
} 
