package species

class ResourceController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [resourceInstanceList: Resource.list(params), resourceInstanceTotal: Resource.count()]
    }

    def create = {
        def resourceInstance = new Resource()
        resourceInstance.properties = params
        return [resourceInstance: resourceInstance]
    }

    def save = {
        def resourceInstance = new Resource(params)
        if (resourceInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'resource.label', default: 'Resource'), resourceInstance.id])}"
            redirect(action: "show", id: resourceInstance.id)
        }
        else {
            render(view: "create", model: [resourceInstance: resourceInstance])
        }
    }

    def show = {
        def resourceInstance = Resource.get(params.id)
        if (!resourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
        else {
            [resourceInstance: resourceInstance]
        }
    }

    def edit = {
        def resourceInstance = Resource.get(params.id)
        if (!resourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'resource.label', default: 'Resource'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [resourceInstance: resourceInstance]
        }
    }

    def update = {
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

    def delete = {
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
}
