package content

import content.fileManager.UFileService
import grails.converters.JSON
import content.Location
import grails.plugins.springsecurity.Secured



class ProjectController {

	static allowedMethods = [save: "POST", update: "POST", delete: "GET"]

	def projectService
	def observationService
	def projectSearchService
	def uFileService = new UFileService()

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		log.debug params
		
		def model = getProjectList(params)	
		render (view:"list", model:model)
		return;
		
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def create = {
		def projectInstance = new Project()
		projectInstance.properties = params
		return [projectInstance: projectInstance]
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def save = {
		log.debug ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		log.debug params
		def projectInstance = projectService.createProject(params)

		def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
		if (projectInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])}"
			projectInstance.setTags(tags);

			params.sourceHolderId = projectInstance.id
			params.sourceHolderType = projectInstance.class.getCanonicalName()
			//TODO: set source to files
			//def uFiles = uFileService.updateUFiles(params)
			redirect(action: "show", id: projectInstance.id)
		}
		else {
			projectInstance.errors.allErrors.each { log.error it }

			render(view: "create", model: [projectInstance: projectInstance])
		}
	}

	def show = {
		def projectInstance = Project.get(params.id)
		if (!projectInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
			redirect(action: "list")
		}
		else {
			[projectInstance: projectInstance]
		}
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def edit = {
		def projectInstance = Project.get(params.id)
		if (!projectInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
			redirect(action: "list")
		}
		else {
			render(view: "create", model: [projectInstance: projectInstance])
		}
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def update = {
		def projectInstance = Project.get(params.id)
		if (projectInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (projectInstance.version > version) {

					projectInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'project.label', default: 'Project')] as Object[], "Another user has updated this Project while you were editing")
					render(view: "edit", model: [projectInstance: projectInstance])
					return
				}
			}
			
			projectService.updateProject(params, projectInstance)
			
			if (!projectInstance.hasErrors() && projectInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])}"
				redirect(action: "show", id: projectInstance.id)
			}
			else {
				render(view: "create", model: [projectInstance: projectInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def delete = {
		def projectInstance = Project.get(params.id)
		if (projectInstance) {
			try {
				projectInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
			redirect(action: "list")
		}
	}

	def tags = {
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

	def locationSites = {
		def sitesFound = Location.withCriteria {
			ilike 'siteName', params.term + '%'
		}.siteName
		render (sitesFound as JSON)
	}

	def locationCorridors = {
		def corridorsFound = Location.withCriteria {
			ilike 'corridor', params.term + '%'
		}.corridor
		render (corridorsFound as JSON)
	}

	def search = {
		
		[projectInstanceList: Project.list(params), projectInstanceTotal: Project.count()]		
	}
	
	def tagcloud = {
		render (view:"tagcloud")
	}
	
	protected def getProjectList(params) {
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredProject = projectService.getFilteredProjects(params, max, offset)
		def projectInstanceList = filteredProject.projectInstanceList
		def queryParams = filteredProject.queryParams
		def activeFilters = filteredProject.activeFilters
		activeFilters.put("append", true);//needed for adding new page proj ids into existing session["proj_ids_list"]
		
		def totalProjectInstanceList = projectService.getFilteredProjects(params, -1, -1).projectInstanceList
		def count = totalProjectInstanceList.size()
		
		//storing this filtered proj ids list in session for next and prev links
		//http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/1.8.2/org/codehaus/groovy/runtime/DefaultGroovyMethods.java
		//returns an arraylist and invalidates prev listing result
		if(params.append?.toBoolean()) {
			session["proj_ids_list"].addAll(projectInstanceList.collect {it.id});
		} else {
			session["proj_ids_list_params"] = params.clone();
			session["proj_ids_list"] = projectInstanceList.collect {it.id};
		}
		
		log.debug "Storing all project ids list in session ${session['proj_ids_list']} for params ${params}";
		return [totalProjectInstanceList:totalProjectInstanceList, projectInstanceList: projectInstanceList, projectInstanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}
	
}
