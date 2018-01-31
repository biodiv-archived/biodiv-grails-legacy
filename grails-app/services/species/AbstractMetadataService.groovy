package species;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.grails.tagcloud.TagCloudUtil
import groovy.sql.Sql

import content.eml.Document
import species.groups.SpeciesGroup;
import species.Habitat
import species.TaxonomyDefinition;

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;

import species.participation.Observation;
import content.eml.Document.DocumentType;
import species.utils.Utils;
import species.License
import species.License.LicenseType
import content.Project

import species.sourcehandler.XMLConverter
import species.AbstractObjectService;
import static org.springframework.http.HttpStatus.*;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import static speciespage.ObvUtilService.*;
import java.nio.file.Files;
import species.formatReader.SpreadsheetReader;
import species.auth.SUser;
import species.groups.UserGroup;
import static java.nio.file.StandardCopyOption.*
import java.nio.file.Paths;
import species.participation.Discussion;

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.GET
import speciespage.ObvUtilService;

import java.net.URL;
import java.lang.Boolean;
import species.NamesParser;
import species.Metadata
import species.Metadata.DateAccuracy;
import species.Classification;
import species.participation.Flag;
import species.participation.Flag.FlagType;

import org.springframework.context.i18n.LocaleContextHolder as LCH;

class AbstractMetadataService extends AbstractObjectService {

    private static final String GBIF_SITE = 'http://api.gbif.org'
	private static final String GBIF_DWCA_VALIDATOR = 'http://tools.gbif.org/dwca-validator/validatews.do'
 
    def create(klass, params) {
        def instance = klass.newInstance();
        instance = update(instance, params, klass)
        return instance;
    }

    def update(instance, params, klass = null, update=true) {
        if(klass && params.externalId) {
            instance = klass.findByExternalId(params.externalId);
            if(!instance)
                instance = klass.newInstance();
            else if(!update) {
                //return if you think there is nothing to update
                return instance;
            }
        }

        instance.properties = params;

        //instance.clearErrors();

        if(params.author)  {
            instance.author = params.author;
        }

        if(params.originalAuthor) {
            instance.originalAuthor = params.originalAuthor;
        }

        if(params.group instanceof String || params.group_id)
            instance.group = params.group ?: SpeciesGroup.get(params.group_id);
        if(params.habitat instanceof String || params.habitat_id)
            instance.habitat = params.habitat?:Habitat.get(params.habitat_id);

        def dateAccuracy =  Metadata.DateAccuracy.getEnum(params.dateAccuracy)
        if(dateAccuracy) {
            switch(dateAccuracy) {
                case DateAccuracy.UNKNOWN : 
                instance.fromDate = new Date(0);
                //TODO:add flag
                break;
                case DateAccuracy.APPROXIMATE : 
                //TODO:add flag
                break;
            }
            instance.dateAccuracy = dateAccuracy?:(params.toDate?Metadata.DateAccuracy.APPROXIMATE:Metadata.DateAccuracy.ACCURATE); 
        }

        if( params.fromDate != ""){
            if(params.fromDate && (params.fromDate instanceof Date)) {
                instance.fromDate = params.fromDate;
            } else if(params.fromDate) {
                instance.fromDate = parseDate(params.fromDate);
            }
            log.debug "Parsing date ${params.fromDate}"
            log.debug "got ${instance.fromDate}"
  
            if(params.toDate && (params.toDate instanceof Date)) {
                if(params.toDate == new Date(0)) {
                    instance.toDate = instance.fromDate;
                } else {
                    instance.toDate = params.toDate;
                }
            } else if(params.toDate) {
                instance.toDate = parseDate(params.toDate);
            } else {
                instance.toDate = instance.fromDate;
            }
       }
       

        String licenseStr = params.license_0?:params.license
        if(licenseStr) {
            log.debug "Setting license to ${licenseStr}"
            instance.license = (new XMLConverter()).getLicenseByType(licenseStr, false)
        } else {
            log.debug "Setting license to ${LicenseType.CC_BY}"
            instance.license = (new XMLConverter()).getLicenseByType(LicenseType.CC_BY, false)
        }
        instance.language = params.locale_language;
        
        instance.externalId = params.externalId;
        instance.externalUrl = params.externalUrl;
        instance.viaId = params.viaId;
        instance.viaCode = params.viaCode;

        instance.placeName = params.placeName//?:instance.reverseGeocodedName;
        instance.reverseGeocodedName = params.reverse_geocoded_name?:instance.placeName

        //XXX remove this line and column from domain class and database after all migration in wikwio and bhutan
        instance.locationAccuracy = params.location_accuracy?:params.locationAccuracy;

        def locScale =  Metadata.LocationScale.getEnum(params.locationScale)
        instance.locationScale = locScale?:Metadata.LocationScale.APPROXIMATE
        instance.geoPrivacy = params.geoPrivacy ? (params.geoPrivacy.trim().toLowerCase().toBoolean()):false;

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        //        if(params.latitude && params.longitude) {
        //            instance.topology = geometryFactory.createPoint(new Coordinate(params.longitude?.toFloat(), params.latitude?.toFloat()));
        //        } else 
        if(params.topology) {
            instance.topology = params.topology;
        } else if(params.areas) {
            log.debug "Setting topology ${params.areas}"
            WKTReader wkt = new WKTReader(geometryFactory);
            try {
                Geometry geom = wkt.read(params.areas);
                instance.topology = geom;
            } catch(ParseException e) {
                log.error "Error parsing polygon wkt : ${params.areas}"
            }
        }

        if(params.dataTable && instance.metaClass.hasProperty(instance, 'dataTable') ) {
            instance.dataTable = params.dataTable;
        }

       return instance;
    }

    def save(instance, params, boolean sendMail, SUser feedAuthor, String feedType, searchService, String feedDesc=null) {
        log.debug( "saving instance with params assigned >>>>>>>>>>>>>>>>: "+ instance)

        instance.clearErrors();

        if (!instance.hasErrors() && instance.save(flush: true)) {
            //mailSubject = messageSource.getMessage("info.share.observation", null, LCH.getLocale())
            //String msg = messageSource.getMessage("instance.label", [instance.id], LCH.getLocale())
            activityFeedService.addActivityFeed(instance, null, instance.getAuthor(), feedType, feedDesc);
            setAssociations(instance, params, sendMail);
            if(sendMail)
                utilsService.sendNotificationMail(feedType, instance, null, params.webaddress);
            if(searchService)
                searchService.publishSearchIndex(instance, true);
            def model = utilsService.getSuccessModel("Saved successfully", instance, OK.value());
            return model
        }
        else {
            println "error in saving instance"
            def errors = [];

             instance.errors.allErrors .each {
                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
            }
            println errors;
            def model = utilsService.getErrorModel("Failed to save ${instance}", instance, OK.value(), errors);
            return model;
        }
    }

    def setAssociations(instance, params, sendMail) {
        log.debug "setAssociations tags"
        def tags = (params.tags != null) ? ((params.tags instanceof List) ? params.tags : Arrays.asList(params.tags)) : new ArrayList();
        if(tags) { 
            instance.setTags(tags)
        }
        if(instance.metaClass.hasProperty(instance, "userGroups")) {
            log.debug "setAssociations userGroups"
            if(params.groupsWithSharingNotAllowed) {
                setUserGroups(instance, [params.groupsWithSharingNotAllowed], sendMail);
            } else {
                List validUserGroups = getValidUserGroups(instance, params.userGroupsList);
                if(validUserGroups)
                    setUserGroups(instance, validUserGroups, sendMail);
            }
        }

        /*if(instance.metaClass.hasProperty(instance, "dateAccuracy")) {
            switch(instance.dateAccuracy) {
                case DateAccuracy.UNKNOWN:
                case DateAccuracy.APPROXIMATE : 
                    flagIt(instance, FlagType.DATE_INAPPROPRIATE, "Date is "+dateAccuracy.value());
                    break;
           }
        } else {
        }*/

    }

    def setUserGroups(instance, List userGroupIds, boolean sendMail = true) {
		if(!instance) return;
        userGroupIds = userGroupIds?.collect {(it instanceof Long) ? it : Long.parseLong(it)}
		def instanceInUserGroups = instance.userGroups.collect { it.id }
		def toRemainInUserGroups =  instanceInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			log.debug 'removing instance from usergroups'
			userGroupService.removeResourceOnGroups(instance, instanceInUserGroups, sendMail);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
            println "Adding resources to ${userGroupIds}"
			userGroupService.addResourceOnGroups(instance, userGroupIds, sendMail);
			instanceInUserGroups.removeAll(toRemainInUserGroups)
            println "Removing from ${instanceInUserGroups}"
			userGroupService.removeResourceOnGroups(instance, instanceInUserGroups, sendMail);
		}
	}

    protected List getValidUserGroups(instance, String userGroupsList) {
        List validUserGroups = [];
        HashSet userGroupIds = new HashSet();
        if(userGroupsList) {
            userGroupsList.split(',').each {
                if(it) userGroupIds << Long.parseLong(it);
            }
        }
        List<UserGroup> userGroupsWithFilterRule = UserGroup.findAllByFilterRuleIsNotNull();
        userGroupIds.addAll(userGroupsWithFilterRule.collect {it.id})

        //if instance is part of dataTable then all datatable usergroups
        if(instance.metaClass.hasProperty(instance, 'dataTable') && instance.dataTable) {
            userGroupIds.addAll(instance.dataTable.userGroups.collect {it.id});
            if(instance.dataTable.dataset) {
                userGroupIds.addAll(instance.dataTable.dataset.userGroups.collect {it.id});
            }
        }

        return new ArrayList(userGroupIds);

        /*boolean hasValidUserGroup = instance.metaClass.respondsTo(instance, "isUserGroupValidForPosting");
        if(hasValidUserGroup) {
            //these are groups with filter rule that means autoPull = on
            List<UserGroup> userGroupsWithFilterRule = UserGroup.findAllByFilterRuleIsNotNull();
            userGroupsWithFilterRule.each { uGroup ->
                if(instance.isUserGroupValidForPosting(uGroup))
                    validUserGroups << uGroup.id;
                else 
                    userGroupIds.remove(uGroup.id);
            }
            if(userGroupIds) {
                validUserGroups.addAll(userGroupIds);
            }
        }
        return validUserGroups;
        */
        
    }

    Date parseDate(date){
		return utilsService.parseDate(date)
    }

    boolean validateDwCA(String zipFile) {
        
        def http = new HTTPBuilder()
        http.request( GBIF_DWCA_VALIDATOR, GET, JSON ) { req ->
            uri.query = [ archiveUrl:zipFile]
            headers.Accept = 'application/json'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def jsonText =  reader.text
                println "========GBIF DWCA Validator Result====== " + jsonText
                return jsonText?jsonText.valid:false
            }
            response.'404' = { println 'Not found' }
        }
    }

    public void flagIt(instance, FlagType flagType, String notes=null) {
        log.info "Flagging instance ${instance}"
        def flagInstance = Flag.findWhere(author: instance.author, objectId: instance.id, objectType:instance.class.getCanonicalName());
        if (!flagInstance) {
            try {
                flagInstance = new Flag(objectId: instance.id, objectType: instance.class.getCanonicalName(), author: instance.author, flag:flagType, notes:notes)
                if(!flagInstance.save(flush:true)){
                    flagInstance.errors.allErrors.each { println it }
                    return null
                }
                log.info "Saving flagCount"
                instance.flagCount++
                instance.save(flush:true)
                log.info "Adding flagged activity"
                def activityNotes = flagInstance.flag.value() + ( flagInstance.notes ? " \n" + flagInstance.notes : "")
                def act = activityFeedService.addActivityFeed(instance, flagInstance, flagInstance.author, activityFeedService.OBSERVATION_FLAGGED, activityNotes); 
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                e.printStackTrace();
            }
        } else {
            log.error "object ${instance} is already flagged by author"
        }
    }
}
