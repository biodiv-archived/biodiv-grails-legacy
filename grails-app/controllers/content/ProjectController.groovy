package content


import grails.converters.JSON
import content.Location
import grails.plugins.springsecurity.Secured
import org.grails.taggable.*

class ProjectController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def projectService
	def observationService
	def projectSearchService
	def springSecurityService
	def documentService

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		log.debug params

		def model = getProjectList(params)
		if(params.loadMore?.toBoolean()){
			render(template:"/document/documentListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/project/projectListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvFilterMsgHtml:obvFilterMsgHtml, obvListHtml:obvListHtml]
			render result as JSON
			return;
		}
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def create = {
		def projectInstance = new Project()
		projectInstance.properties = params
		return [projectInstance: projectInstance]
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def save = {
		log.debug params

		params.author = springSecurityService.currentUser;

		def projectInstance = projectService.createProject(params)

		def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
		if (projectInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])}"
			projectInstance.setTags(tags);

			params.sourceHolderId = projectInstance.id
			params.sourceHolderType = projectInstance.class.getCanonicalName()
			projectInstance.updateDocuments()
			//def uFiles = UFileService.updateUFiles(params)
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
			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			if (!projectInstance.hasErrors() && projectInstance.save(flush: true)) {
				projectInstance.setTags(tags);
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
				//userGroupService.removeDocumentFromUserGroups(documentInstance, documentInstance.userGroups.collect{it.id})
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
		render Tag.findAllByNameIlike("${params.term}%", [max:10])*.name as JSON
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

//	/**
//	 *
//	 */
//	def search = {
//		log.debug params;
//		def model = projectService.search(params)
//		model['isSearch'] = true;
//
//		if(params.loadMore?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render(template:"/project/projectListTemplate", model:model);
//			return;
//		} else if(!params.isGalleryUpdate?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render (view:"list", model:model)
//			return;
//		} else {
//			log.debug "going to gallery update in controller"
//			params.remove('isGalleryUpdate');
//			def obvListHtml =  g.render(template:"/project/projectListTemplate", model:model);
//			model.resultType = "project"
//			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
//
//			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
//
//			render (result as JSON)
//			return;
//		}
//	}

	def terms = {
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		List result = projectService.nameTerms(params)
		render result.value as JSON;
	}


	def tagcloud = { render (view:"tagcloud") }

	/**
	 * From the params passed filter teh projects and pass the model for view
	 * @param params
	 * @return
	 */
	protected def getProjectList(params) {
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredProject = projectService.getFilteredProjects(params, max, offset)
		
		def projectInstanceList = filteredProject.projectInstanceList
		def queryParams = filteredProject.queryParams
		def activeFilters = filteredProject.activeFilters
		activeFilters.put("append", true);//needed for adding new page proj ids into existing session["proj_ids_list"]

		def count = projectService.getFilteredProjects(params, -1, -1).projectInstanceList.size()
		
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
		return [projectInstanceList: projectInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, , resultType:'project']
	}

}
