package species

import grails.converters.JSON;
import species.participation.UsersResource;

import grails.plugin.springsecurity.annotation.Secured
class ResourceController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def resourcesService;
    def springSecurityService;

    def index = {
        redirect(action: "list", params: params)
    }

	def list() {
		log.debug params
		
		def model = getResourceList(params);
		if(params.loadMore?.toBoolean()){
			render(template:"/resource/showResourceListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/resource/showResourceListTemplate", model:model);

			def result = [obvListHtml:obvListHtml, instanceTotal:model.instanceTotal]
			render result as JSON
			return;
		}
	}

	protected def getResourceList(params) {
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredResource = resourcesService.getFilteredResources(params, max, offset, false)
		def resourceInstanceList = filteredResource.resourceInstanceList
		def queryParams = filteredResource.queryParams
		def activeFilters = filteredResource.activeFilters
		activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]
		def count = Resource.count()
		return [resourceInstanceList: resourceInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}
	

    def create() {
        def resourceInstance = new Resource()
        resourceInstance.properties = params
        return [resourceInstance: resourceInstance]
    }

    def save() {
        def resourceInstance = new Resource(params)
        if (resourceInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'resource.label', default: 'Resource'), resourceInstance.id])}"
            redirect(action: "show", id: resourceInstance.id)
        }
        else {
            render(view: "create", model: [resourceInstance: resourceInstance])
        }
    }

    def show() {
        def resourceInstance = Resource.get(params.id)
        if (!resourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
        else {
            [resourceInstance: resourceInstance]
        }
    }

    @Secured("ROLE_USER")
    def edit() {
        def resourceInstance = Resource.get(params.id)
        if (!resourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [resourceInstance: resourceInstance]
        }
    }

    @Secured("ROLE_USER")
    def update() {
        def resourceInstance = Resource.get(params.id)
        if (resourceInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (resourceInstance.version > version) {
                    
                    resourceInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'resource.label', default: 'Resource')] as Object[], "Another user has updated this Resource while you were editing")
                    render(view: "edit", model: [resourceInstance: resourceInstance])
                    return
                }
            }
            resourceInstance.properties = params
            if (!resourceInstance.hasErrors() && resourceInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'resource.label', default: 'Resource'), resourceInstance.id])}"
                redirect(action: "show", id: resourceInstance.id)
            }
            else {
                render(view: "edit", model: [resourceInstance: resourceInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
    }

    @Secured("ROLE_ADMIN")
    def delete() {
        def resourceInstance = Resource.get(params.id)
        if (resourceInstance) {
            try {
                resourceInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
    }

    def rate(){
        def resourceInstance = Resource.get(params.id)
        if (!resourceInstance) {
				def message = ['status':'error', 'msg':g.message(code: 'error', default:'Error while processing the request.')];
				render message as JSON
        }
        else {
            int rate = params.int('rate');
            if(rate) {
                resourceInstance.rating = rate;
                if (resourceInstance.save(flush: true)) {
                    def message = ['status':'success', 'rating':rate, 'msg':'Successfully added rating'];
				    render message as JSON
                }
            }
        }
    }
    
    def createResource(){
        def user = springSecurityService.currentUser;
        List<Resource> resources = resourcesService.createResource(params, user);
        resources.each{
            def flag = resourcesService.createUsersRes(user, it, UsersResource.UsersResourceStatus.NOT_USED)
        }
        def res = [status:true]
        render res as JSON
    }

}
