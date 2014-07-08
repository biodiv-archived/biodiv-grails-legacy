package speciespage

import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine

import org.grails.taggable.TagLink;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import species.Resource;
import species.Habitat;
import species.Language;
import species.utils.Utils;
import species.TaxonomyDefinition;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Follow;
import species.utils.ImageType;
import species.utils.Utils;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;

class ResourcesService {

	static transactional = false

	def grailsApplication;
	def dataSource;
	def springSecurityService;
    def observationService;

    /**
	 * Filter resources by group, habitat, tag, user, species
	 * max: limit results to max: if max = -1 return all results
	 * offset: offset results: if offset = -1 its not passed to the 
	 * executing query
	 */
	Map getFilteredResources(params, max, offset, isMapView) {

		def queryParts = getFilteredResourcesFilterQuery(params) 
		String query = queryParts.query;
		
		if(isMapView) {
			query = queryParts.mapViewQuery + queryParts.filterQuery + queryParts.orderByClause
		} else {
			query += queryParts.filterQuery + queryParts.orderByClause
			if(max != -1)
				queryParts.queryParams["max"] = max
			if(offset != -1)
				queryParts.queryParams["offset"] = offset
		}
		
		def resourceInstanceList = Resource.executeQuery(query, queryParts.queryParams)
		if(params.daterangepicker_start){
			queryParts.queryParams["daterangepicker_start"] = params.daterangepicker_start
		}
		if(params.daterangepicker_end){
			queryParts.queryParams["daterangepicker_end"] =  params.daterangepicker_end
		}
		
		return [resourceInstanceList:resourceInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	def getFilteredResourcesFilterQuery(params) {
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;

		def query = "select res from Resource res "
		def mapViewQuery = "select res.id, res.latitude, res.longitude from Resource res "
		def queryParams = [:];//[isDeleted : false]
		def filterQuery = ""//"  where res.isDeleted = :isDeleted "
		def activeFilters = [:]

		if(params.sGroup){
			params.sGroup = params.sGroup.toLong()
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			}else{
				filterQuery += " and res.group.id = :groupId "
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}

		if(params.tag){
			query = "select res from Resource res,  TagLink tagLink "
			mapViewQuery = "select res.id, res.latitude, res.longitude from Resource res, TagLink tagLink "
			filterQuery +=  " and res.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Resource.class);
			activeFilters["tag"] = params.tag
		}


		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			filterQuery += " and res.habitat.id = :habitat "
			queryParams["habitat"] = params.habitat
			activeFilters["habitat"] = params.habitat
		}

		if(params.user){
			filterQuery += " and res.author.id = :user "
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
		}

		if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
			filterQuery += " and res.maxVotedReco is null "
			//queryParams["speciesName"] = params.speciesName
			activeFilters["speciesName"] = params.speciesName
		}

		if(params.isFlagged && params.isFlagged.toBoolean()){
			filterQuery += " and res.flagCount > 0 "
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
		}
		
		if(params.daterangepicker_start && params.daterangepicker_end){
			def df = new SimpleDateFormat("dd/MM/yyyy")
			def startDate = df.parse(params.daterangepicker_start)
			def endDate = df.parse(params.daterangepicker_end)
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
		
		if(params.bounds){
			def bounds = params.bounds.split(",")

			def swLat = bounds[0]
			def swLon = bounds[1]
			def neLat = bounds[2]
			def neLon = bounds[3]

			filterQuery += " and res.latitude > " + swLat + " and  res.latitude < " + neLat + " and res.longitude > " + swLon + " and res.longitude < " + neLon
			activeFilters["bounds"] = params.bounds
		}
		
		def orderByClause = " order by res." + (params.sort ? params.sort : "id") +  " desc, res.id asc"

		return [query:query, mapViewQuery:mapViewQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

	}
	
	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
}	
