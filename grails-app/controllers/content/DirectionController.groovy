package content

import grails.plugin.springsecurity.Secured


class DirectionController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [strategicDirectionInstanceList: StrategicDirection.list(params), strategicDirectionInstanceTotal: StrategicDirection.count()]
    }

	@Secured(['ROLE_CEPF_ADMIN'])
    def create = {
        def strategicDirectionInstance = new StrategicDirection()
        strategicDirectionInstance.properties = params
        return [strategicDirectionInstance: strategicDirectionInstance]
    }

	@Secured(['ROLE_CEPF_ADMIN'])	
    def save = {
        def strategicDirectionInstance = new StrategicDirection(params)
        if (strategicDirectionInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), strategicDirectionInstance.id])}"
            redirect(action: "show", id: strategicDirectionInstance.id)
        }
        else {
            render(view: "create", model: [strategicDirectionInstance: strategicDirectionInstance])
        }
    }

    def show = {
        def strategicDirectionInstance = StrategicDirection.get(params.id)
        if (!strategicDirectionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
            redirect(action: "list")
        }
        else {
            [strategicDirectionInstance: strategicDirectionInstance]
        }
    }

	@Secured(['ROLE_CEPF_ADMIN'])	
    def edit = {
        def strategicDirectionInstance = StrategicDirection.get(params.id)
        if (!strategicDirectionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [strategicDirectionInstance: strategicDirectionInstance]
        }
    }
	
	@Secured(['ROLE_CEPF_ADMIN'])
    def update = {
        def strategicDirectionInstance = StrategicDirection.get(params.id)
        if (strategicDirectionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (strategicDirectionInstance.version > version) {
                    
                    strategicDirectionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'strategicDirection.label', default: 'StrategicDirection')] as Object[], "Another user has updated this StrategicDirection while you were editing")
                    render(view: "edit", model: [strategicDirectionInstance: strategicDirectionInstance])
                    return
                }
            }
            strategicDirectionInstance.properties = params
            if (!strategicDirectionInstance.hasErrors() && strategicDirectionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), strategicDirectionInstance.id])}"
                redirect(action: "show", id: strategicDirectionInstance.id)
            }
            else {
                render(view: "edit", model: [strategicDirectionInstance: strategicDirectionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
            redirect(action: "list")
        }
    }

	@Secured(['ROLE_CEPF_ADMIN'])	
    def delete = {
        def strategicDirectionInstance = StrategicDirection.get(params.id)
        if (strategicDirectionInstance) {
            try {
                strategicDirectionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'strategicDirection.label', default: 'StrategicDirection'), params.id])}"
            redirect(action: "list")
        }
    }
}
