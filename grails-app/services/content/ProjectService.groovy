package content


import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;



import content.fileManager.UFile;
import content.fileManager.UFileService


class ProjectService {

	static transactional = false
	UFileService uFileService = new UFileService()

	def createProject(params) {

		def projectInstance = new Project()

		updateProject(params, projectInstance)

		return projectInstance;
	}

	def updateProject(params, Project project) {
		log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		log.debug params

		def projectParams = params
		projectParams.grantFrom = parseDate(params.grantFrom)
		projectParams.grantTo = parseDate(params.grantTo)

		project.properties = projectParams
		def uFiles = uFileService.updateUFiles(params)

		// delete Locations that are marked for removal
		def _toBeDeletedLocations = project.locations.findAll{it?.deleted || !it}

		if(_toBeDeletedLocations) {
			project.locations.removeAll(_toBeDeletedLocations)
		}

		// delete DataLinks that are marked for removal
		def _toBeDeletedDataLinks = project.dataLinks.findAll{it?.deleted || !it}

		if(_toBeDeletedDataLinks) {
			project.dataLinks.removeAll(_toBeDeletedDataLinks)
		}

		def _toBeDeletedProposalFiles = project.proposalFiles.findAll{it?.deleted || !it}

		log.debug "Proposal Files marked for delete.."
		log.debug _toBeDeletedProposalFiles

		if(_toBeDeletedProposalFiles) {
			project.proposalFiles.removeAll(_toBeDeletedProposalFiles)
		}

		def _toBeDeletedReportFiles = project.reportFiles.findAll{it?.deleted || !it}

		log.debug "Report Files marked for delete.."
		log.debug _toBeDeletedReportFiles

		if(_toBeDeletedReportFiles) {
			project.reportFiles.removeAll(_toBeDeletedReportFiles)
		}


		def _toBeDeletedMiscFiles = project.miscFiles.findAll{it?.deleted || !it}

		log.debug "Report Files marked for delete.."
		log.debug _toBeDeletedReportFiles

		if(_toBeDeletedMiscFiles) {
			project.miscFiles.removeAll(_toBeDeletedMiscFiles)
		}
	}

	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
			print e.toString();
		}
		return null;
	}

	/**
	 * Handle the filtering on projects
	 * @param params
	 * @param max
	 * @param offset
	 * @return
	 */
	Map getFilteredProjects(params, max, offset) {

		def queryParts = getProjectsFilterQuery(params)
		String query = queryParts.query;


		query += queryParts.filterQuery + queryParts.orderByClause
		if(max != -1)
			queryParts.queryParams["max"] = max
		if(offset != -1)
			queryParts.queryParams["offset"] = offset


		log.debug "Project Query >>>>>>>>>"+ query + " >>>>>params " + queryParts.queryParams
		def projectInstanceList = Project.executeQuery(query, queryParts.queryParams)

		return [projectInstanceList:projectInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	/**
	 * Prepare database wuery based on paramaters
	 * @param params
	 * @return
	 */
	def getProjectsFilterQuery(params) {

		def query = "select proj from Project proj "
		def queryParams = [:]
		def activeFilters = [:]
		def filterQuery = "where proj.id is not NULL "  //Dummy stmt


		if(params.tag){
			query = "select proj from Project proj,  TagLink tagLink "
			//TODO - 
			filterQuery += " and proj.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Project.class)
			activeFilters["tag"] = params.tag
		}

		if(params.keywords) {
			query = "select proj from Project proj,  TagLink tagLink "
			//TODO - contains
			filterQuery += " and proj.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :keywords "
			queryParams["keywords"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Project.class)
			activeFilters["keywords"] = params.tag
		}
		
		/*
		if(params.sitename) {
			query = "select proj from Project proj,  Location location"
			
			filterQuery += " and location.siteName = :sitename and location in proj.locations"
			queryParams["sitename"] = params.sitename
			activeFilters["sitename"] = params.sitename
		}
		*/
		if(params.title) {
			filterQuery += " and proj.title like :title "
			queryParams["title"] = '%' + params.title + '%' 
			activeFilters["title"] = params.title
		}
		
		if(params.grantee) {
			filterQuery += " and proj.granteeOrganization like :grantee "
			queryParams["grantee"] = '%' +params.grantee + '%'
			activeFilters["grantee"] = params.grantee
		}
		
		

		def sortBy = params.sort ? params.sort : " lastUpdated "

		def sortOrder = (sortBy=='granteeOrganization')?" asc ":" desc "

		def orderByClause = " order by proj." + sortBy +  sortOrder +", proj.id asc"

		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

	}


	
	def getProjectsFromSearch(params) {
		def max = Math.min(params.max ? params.max.toInteger() : 12, 100)
		def offset = params.offset ? params.offset.toLong() : 0
 
		def model;
		
		try {
			model = getFilteredProjectsFromSearch(params, max, offset);
		} catch(SolrException e) {
			e.printStackTrace();
		}
		return model;
	}
	
	
	Map getFilteredProjectsFromSearch(params, max, offset){
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		def queryParams = [:]
		
		def activeFilters = [:]
		
		
		NamedList paramsList = new NamedList();
		
		//params.userName = springSecurityService.currentUser.username;
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

		
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}
		
	
		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		//options
		paramsList.add('start', offset);
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		
		paramsList.add('fl', params['fl']?:"id");
		
		//Facets
		params["facet.field"] = params["facet.field"] ?: searchFieldsConfig.TAG;
		paramsList.add('facet.field', params["facet.field"]);
		paramsList.add('facet', "true");
		params["facet.limit"] = params["facet.limit"] ?: 50;
		paramsList.add('facet.limit', params["facet.limit"]);
		params["facet.offset"] = params["facet.offset"] ?: 0;
		paramsList.add('facet.offset', params["facet.offset"]);
		paramsList.add('facet.mincount', "1");
		
		//Filters
		if(params.sGroup) {
			params.sGroup = params.sGroup.toLong()
			def groupId = getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			} else{
				paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}
		
		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
			queryParams["habitat"] = params.habitat
			activeFilters["habitat"] = params.habitat
		}
		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'observation'
			activeFilters["tag"] = params.tag
		}
		if(params.user){
			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
		}
		if(params.name && (params.name != grailsApplication.config.speciesPortal.group.ALL)) {
			paramsList.add('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.name);
			queryParams["name"] = params.name
			activeFilters["name"] = params.name
		}
		if(params.isFlagged && params.isFlagged.toBoolean()){
			paramsList.add('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
		}

		if(params.bounds){
			def bounds = params.bounds.split(",")
			 def swLat = bounds[0]
			 def swLon = bounds[1]
			 def neLat = bounds[2]
			 def neLon = bounds[3]
			 paramsList.add('fq', searchFieldsConfig.LATLONG+":["+swLat+","+swLon+" TO "+neLat+","+neLon+"]");
			 activeFilters["bounds"] = params.bounds
		}
		
		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}
		log.debug "Along with faceting params : "+paramsList;
		
		if(isMapView) {
			//query = mapViewQuery + filterQuery + orderByClause
		} else {
			//query += filterQuery + orderByClause
			queryParams["max"] = max
			queryParams["offset"] = offset
		}

		List<Project> instanceList = new ArrayList<Project>();
		def totalObservationIdList = [];
		def facetResults = [:], responseHeader
		long noOfResults = 0;
		if(paramsList.get('q')) {
			def queryResponse = observationsSearchService.search(paramsList);
			
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def instance = Observation.read(Long.parseLong(doc.getFieldValue("id")+""));
				if(instance) {
					totalObservationIdList.add(Long.parseLong(doc.getFieldValue("id")+""));
					instanceList.add(instance);
				}
			}
			
			List facets = queryResponse.getFacetField(params["facet.field"]).getValues()
			
			facets.each {
				facetResults.put(it.getName(),it.getCount());
			}
			
			responseHeader = queryResponse?.responseHeader;
			noOfResults = queryResponse.getResults().getNumFound()
		}
		/*if(responseHeader?.params?.q == "*:*") {
			responseHeader.params.remove('q');
		}*/
		
		return [responseHeader:responseHeader, projectInstanceList:instanceList, instanceTotal:noOfResults, queryParams:queryParams, activeFilters:activeFilters, tags:facetResults, totalObservationIdList:totalObservationIdList]
	}
	


}