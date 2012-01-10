package species.auth

class SUserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [SUserInstanceList: SUser.list(params), SUserInstanceTotal: SUser.count()]
    }

    def create = {
        def SUserInstance = new SUser()
        SUserInstance.properties = params
        return [SUserInstance: SUserInstance]
    }

    def save = {
        def SUserInstance = new SUser(params)
        if (SUserInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'SUser.label', default: 'SUser'), SUserInstance.id])}"
            redirect(action: "show", id: SUserInstance.id)
        }
        else {
            render(view: "create", model: [SUserInstance: SUserInstance])
        }
    }

    def show = {
        def SUserInstance = SUser.get(params.id)
        if (!SUserInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
            redirect(action: "list")
        }
        else {
            [SUserInstance: SUserInstance]
        }
    }

    def edit = {
        def SUserInstance = SUser.get(params.id)
        if (!SUserInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [SUserInstance: SUserInstance]
        }
    }

    def update = {
        def SUserInstance = SUser.get(params.id)
        if (SUserInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (SUserInstance.version > version) {
                    
                    SUserInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'SUser.label', default: 'SUser')] as Object[], "Another user has updated this SUser while you were editing")
                    render(view: "edit", model: [SUserInstance: SUserInstance])
                    return
                }
            }
            SUserInstance.properties = params
            if (!SUserInstance.hasErrors() && SUserInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'SUser.label', default: 'SUser'), SUserInstance.id])}"
                redirect(action: "show", id: SUserInstance.id)
            }
            else {
                render(view: "edit", model: [SUserInstance: SUserInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def SUserInstance = SUser.get(params.id)
        if (SUserInstance) {
            try {
                SUserInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
            redirect(action: "list")
        }
    }
}
