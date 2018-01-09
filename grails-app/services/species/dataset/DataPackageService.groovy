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

import grails.converters.JSON;
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
import species.dataset.DataPackage;

class DataPackageService extends AbstractMetadataService {

    static transactional = false

    def messageSource;
    def activityFeedService
    def obvUtilService;
    def springSecurityService;
    def customFieldService;

    DataPackage create(params) {
        //return super.create(DataPackage.class, params);
        def instance = DataPackage.class.newInstance();
        instance = update(instance, params)
        return instance;
    }

    DataPackage update(DataPackage instance, params) {
        instance.properties = params;

        instance.clearErrors();

        if(params.author)  {
            instance.author = params.author;
        } else {
            instance.author = springSecurityService.currentUser;
        }

        if(params.uploaderUserIds)  {
           instance.uploaderIds = params.uploaderUserIds;
        } 

       return instance;
    }

    def save(params, sendMail) {
        def result;

        DataPackage dataPackage;
        def feedType;
        if(params.id) {
            dataPackage = DataPackage.get(Long.parseLong(params.id));
            dataPackage = update(dataPackage, params);
            feedType = activityFeedService.INSTANCE_UPDATED;
        } else {
            dataPackage = create(params);
            feedType = activityFeedService.INSTANCE_CREATED;
        }
      
        def customFieldMapList = [];
        if(params.customFieldMapList) {
            customFieldMapList = JSON.parse(params.customFieldMapList);
        }

        if(params.supportingModule) {
            Map s = [:];
            params.supportingModule.each { sm,v ->
                List x = [];
                customFieldMapList.each { m ->
                    if(m['supportingModule'] == sm) {
                        x << m;
                    }
                }
                s[Integer.parseInt(sm)] = x;
            }
            dataPackage.supportingModules = s as JSON;
        }

        if(params.allowedDataTableType) {
            List s = [];
            params.allowedDataTableType.each { it
                s << Integer.parseInt(it.key);
            }
            dataPackage.allowedDataTableTypes = s as JSON;
        }

        dataPackage.lastRevised = new Date();

        DataPackage.withTransaction {
            result = save(dataPackage, params, true, null, feedType, null);
            if(result.success) {
			    //customFieldService.addToDataPackage(params.customFieldMapList, dataPackage)
            }
        } 
        return result;
    }
    
    Map getFilteredDataPackages(def params, max, offset, isMapView = false) {

        def queryParts = getFilteredDataPackageFilterQuery(params) 
        String query = queryParts.query;
        long allDataPackageCount = 0;

        query += queryParts.filterQuery + queryParts.orderByClause
        
        log.debug "query : "+query;
        log.debug "allDataPackageCountQuery : "+queryParts.allDataPackageCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        def allDataPackageCountQuery = sessionFactory.currentSession.createQuery(queryParts.allDataPackageCountQuery)

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
        def dataPackageInstanceList = hqlQuery.list();

        allDataPackageCountQuery.setProperties(queryParts.queryParams)
        allDataPackageCount = allDataPackageCountQuery.list()[0]

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
        return [instanceList:dataPackageInstanceList, instanceTotal:allDataPackageCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }

    def getFilteredDataPackageFilterQuery(params) {
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
        query += " from DataPackage obv "

        def filterQuery = " where obv.isDeleted = :isDeleted and title != 'Checklist' "
        
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

      
        
		def allDataPackageCountQuery = "select count(*) from DataPackage obv " +((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
        orderByClause = " order by " + orderByClause;

        return [query:query, allDataPackageCountQuery:allDataPackageCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

    }

    boolean hasPermission(DataPackage dataPackage, SUser user) {
        
        if(!user || !dataPackage) return false;

        log.debug "Testing if ${user} has permissions on ${dataPackage}"

        if(utilsService.isAdmin(user))
            return true;

        if(dataPackage.hasRoleUserAllowed && springSecurityService.currentUser) 
            return true;

        boolean isPermitted = false;
        if(dataPackage.uploaderIds) {
            dataPackage.uploaderIds.split(',').each { uId ->
                if(Long.parseLong(uId) == user.id) {
                    isPermitted = true;
                }
            }
        }
        return isPermitted;
    }
} 
