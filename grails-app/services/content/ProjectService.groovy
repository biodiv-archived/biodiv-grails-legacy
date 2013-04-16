package content


import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils


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
			query = "select proj from Project proj, Location loc "
			
		}
		*/
		if(params.title) {
			filterQuery += " and proj.title = :title "
			queryParams["title"] = params.title 
			activeFilters["title"] = params.title
		}
		
		if(params.grantee) {
			filterQuery += " and proj.granteeOrganization like :grantee "
			queryParams["grantee"] = params.grantee
			activeFilters["grantee"] = params.grantee
		}
		
		

		def sortBy = params.sort ? params.sort : " lastUpdated "

		def sortOrder = (sortBy=='granteeOrganization')?" asc ":" desc "

		def orderByClause = " order by proj." + sortBy +  sortOrder +", proj.id asc"

		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

	}




}