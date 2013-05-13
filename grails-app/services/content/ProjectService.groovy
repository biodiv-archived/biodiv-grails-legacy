package content


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;
import species.utils.Utils;



import content.eml.Document;
import content.eml.UFile;
import content.eml.DocumentService;


class ProjectService {

	static transactional = false
	def grailsApplication;
	
	def projectSearchService
	
	def documentService
	def userGroupService
	
	def createProject(params) {

		def projectInstance = new Project()

		updateProject(params, projectInstance)

		return projectInstance;
	}

	def updateProject(params, Project project) {
		log.debug " >>>>>>>>>>>>>>>>>>>>ccc>>>>>>>>>>>>>>>>>>" + params
		def projectParams = params
		projectParams.grantFrom = parseDate(params.grantFrom)
		projectParams.grantTo = parseDate(params.grantTo)
		projectParams.grantedAmount = (params.grantedAmount && params.grantedAmount != '') ? params.grantedAmount?.toInteger() : 0
		project.properties = projectParams
		def documents = documentService.updateDocuments(params)

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

		
		def _toBeDeletedProposalFiles = project.proposalFiles.findAll{it?.deleted || !it }

		log.debug "Proposal Files marked for delete.." + project.proposalFiles?.dump()

		if(_toBeDeletedProposalFiles) {
			project.proposalFiles.removeAll(_toBeDeletedProposalFiles)
			deleteDocumentsFromProject(_toBeDeletedProposalFiles)
		}

		def _toBeDeletedReportFiles = project.reportFiles.findAll{it?.deleted || !it}

		log.debug "Report Files marked for delete.." + _toBeDeletedReportFiles

		if(_toBeDeletedReportFiles) {
			project.reportFiles.removeAll(_toBeDeletedReportFiles)
			deleteDocumentsFromProject(_toBeDeletedReportFiles)
		}


		def _toBeDeletedMiscFiles = project.miscFiles.findAll{it?.deleted || !it}

		log.debug "Report Files marked for delete.." + _toBeDeletedReportFiles

		if(_toBeDeletedMiscFiles) {
			project.miscFiles.removeAll(_toBeDeletedMiscFiles)
			deleteDocumentsFromProject(_toBeDeletedMiscFiles)
		}
		
		log.debug "Project object after updating with params: "+ project.dump()
	}

	
	private deleteDocumentsFromProject(docs){
		docs.each { it->
			it.delete(flush:true)
		}
	}
	
	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):null;
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
		if(!params.aq){
			return getProjectListFromDB(params, max, offset)
		}
		
		//get result from solr search
		return search(params)
	}
	
	private getProjectListFromDB(params, max, offset){
		def queryParts = getProjectsFilterQuery(params)
		String query = queryParts.query;


		query += queryParts.filterQuery + queryParts.orderByClause
		if(max != -1)
			queryParts.queryParams["max"] = max
		if(offset != -1)
			queryParts.queryParams["offset"] = offset


		log.debug "Project Query "+ query + " params " + queryParts.queryParams
		def projectInstanceList = Project.executeQuery(query, queryParts.queryParams)

		return [projectInstanceList:projectInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	/**
	 * Prepare database query based on parameters
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

//		if(params.keywords) {
//			query = "select proj from Project proj,  TagLink tagLink "
//			//TODO - contains
//			filterQuery += " and proj.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :keywords "
//			queryParams["keywords"] = params.tag
//			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Project.class)
//			activeFilters["keywords"] = params.tag
//		}
//		
//		
//		if(params.sitename) {
//			query = "select proj from Project proj,  Location location"
//			
//			filterQuery += " and location.siteName = :sitename and location in proj.locations"
//			queryParams["sitename"] = params.sitename
//			activeFilters["sitename"] = params.sitename
//		}
//		
//		if(params.title) {
//			filterQuery += " and proj.title like :title "
//			queryParams["title"] = '%' + params.title + '%' 
//			activeFilters["title"] = params.title
//		}
//		
//		if(params.grantee) {
//			filterQuery += " and proj.granteeOrganization like :grantee "
//			queryParams["grantee"] = '%' +params.grantee + '%'
//			activeFilters["grantee"] = params.grantee
//		}
		
		def sortBy = params.sort ? params.sort : " lastUpdated "
		def sortOrder = (sortBy=='granteeOrganization')?" asc ":" desc "
		def orderByClause = " order by proj." + sortBy +  sortOrder +", proj.id asc"

		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]
	}


	
	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = projectSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Project Pages"]);
		}
		return result;
	}

	
	/**
	 * 
	 * @param params
	 * @return
	 */
	def search(params) {
		def result;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
		def queryParams = [:]
		def activeFilters = [:]

		NamedList paramsList = new NamedList();
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";

		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap) {
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

		def offset = params.offset ? params.long('offset') : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1 && sort.indexOf(' asc') == -1 ) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		queryParams["max"] = max
		queryParams["offset"] = offset

		paramsList.add('fl', params['fl']?:"id");

		//filters
		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Project.class)
			activeFilters["tag"] = params.tag
		}
	
//		if(params.name) {
//			paramsList.add('fq', searchFieldsConfig.NAME+":"+params.name);
//			queryParams["name"] = params.name
//			activeFilters["name"] = params.name
//		}
//
//		if(params.uGroup) {
//			if(params.uGroup == "THIS_GROUP") {
//				String uGroup = params.webaddress
//				if(uGroup) {
//					//AS we dont have selecting species for group ... we are ignoring this filter
//					//paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
//				}
//				queryParams["uGroup"] = params.uGroup
//				activeFilters["uGroup"] = params.uGroup
//			} else {
//				queryParams["uGroup"] = "ALL"
//				activeFilters["uGroup"] = "ALL"
//			}
//		}
//
//		if(params.query && params.startsWith && params.startsWith != "A-Z"){
//			params.query = params.query + " AND "+searchFieldsConfig.TITLE+":"+params.startsWith+"*"
//			//paramsList.add('fq', searchFieldsConfig.TITLE+":"+params.startsWith+"*");
//			queryParams["startsWith"] = params.startsWith
//			activeFilters["startsWith"] = params.startsWith
//		}

		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = projectSearchService.search(paramsList);
			List<Project> projectInstanceList = new ArrayList<Project>();
			log.debug "query response: "+ queryResponse.getResults()
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				log.debug "doc : "+ doc
				def projectInstance = Project.get(doc.getFieldValue("id"));
				if(projectInstance)
					projectInstanceList.add(projectInstance);
			}

			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), projectInstanceList:projectInstanceList, snippets:queryResponse.getHighlighting()]
			log.debug "result returned from search: "+ result
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}
	
	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("grantee") || sortParam.equalsIgnoreCase('lastUpdated'))
			return true;
		return false;
	}




}