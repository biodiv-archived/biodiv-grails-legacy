package species

class CommonNamesController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [commonNamesInstanceList: CommonNames.list(params), commonNamesInstanceTotal: CommonNames.count()]
    }

    def create = {
        def commonNamesInstance = new CommonNames()
        commonNamesInstance.properties = params
        return [commonNamesInstance: commonNamesInstance]
    }

    def save = {
        def commonNamesInstance = new CommonNames(params)
        if (commonNamesInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), commonNamesInstance.id])}"
            redirect(action: "show", id: commonNamesInstance.id)
        }
        else {
            render(view: "create", model: [commonNamesInstance: commonNamesInstance])
        }
    }

    def show = {
        def commonNamesInstance = CommonNames.get(params.id)
        if (!commonNamesInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
            redirect(action: "list")
        }
        else {
            [commonNamesInstance: commonNamesInstance]
        }
    }

    def edit = {
        def commonNamesInstance = CommonNames.get(params.id)
        if (!commonNamesInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [commonNamesInstance: commonNamesInstance]
        }
    }

    def update = {
        def commonNamesInstance = CommonNames.get(params.id)
        if (commonNamesInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (commonNamesInstance.version > version) {
                    
                    commonNamesInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'commonNames.label', default: 'CommonNames')] as Object[], "Another user has updated this CommonNames while you were editing")
                    render(view: "edit", model: [commonNamesInstance: commonNamesInstance])
                    return
                }
            }
            commonNamesInstance.properties = params
            if (!commonNamesInstance.hasErrors() && commonNamesInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), commonNamesInstance.id])}"
                redirect(action: "show", id: commonNamesInstance.id)
            }
            else {
                render(view: "edit", model: [commonNamesInstance: commonNamesInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def commonNamesInstance = CommonNames.get(params.id)
        if (commonNamesInstance) {
            try {
                commonNamesInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'commonNames.label', default: 'CommonNames'), params.id])}"
            redirect(action: "list")
        }
    }
}
