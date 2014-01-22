package species

class TaxonomyDefinitionController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [taxonomyDefinitionInstanceList: TaxonomyDefinition.list(params), taxonomyDefinitionInstanceTotal: TaxonomyDefinition.count()]
    }

    def create() {
        def taxonomyDefinitionInstance = new TaxonomyDefinition()
        taxonomyDefinitionInstance.properties = params
        return [taxonomyDefinitionInstance: taxonomyDefinitionInstance]
    }

    def save() {
        def taxonomyDefinitionInstance = new TaxonomyDefinition(params)
        if (taxonomyDefinitionInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), taxonomyDefinitionInstance.id])}"
            redirect(action: "show", id: taxonomyDefinitionInstance.id)
        }
        else {
            render(view: "create", model: [taxonomyDefinitionInstance: taxonomyDefinitionInstance])
        }
    }

    def show() {
        def taxonomyDefinitionInstance = TaxonomyDefinition.get(params.id)
        if (!taxonomyDefinitionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
            redirect(action: "list")
        }
        else {
            [taxonomyDefinitionInstance: taxonomyDefinitionInstance]
        }
    }

    def edit() {
        def taxonomyDefinitionInstance = TaxonomyDefinition.get(params.id)
        if (!taxonomyDefinitionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [taxonomyDefinitionInstance: taxonomyDefinitionInstance]
        }
    }

    def update() {
        def taxonomyDefinitionInstance = TaxonomyDefinition.get(params.id)
        if (taxonomyDefinitionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (taxonomyDefinitionInstance.version > version) {
                    
                    taxonomyDefinitionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition')] as Object[], "Another user has updated this TaxonomyDefinition while you were editing")
                    render(view: "edit", model: [taxonomyDefinitionInstance: taxonomyDefinitionInstance])
                    return
                }
            }
            taxonomyDefinitionInstance.properties = params
            if (!taxonomyDefinitionInstance.hasErrors() && taxonomyDefinitionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), taxonomyDefinitionInstance.id])}"
                redirect(action: "show", id: taxonomyDefinitionInstance.id)
            }
            else {
                render(view: "edit", model: [taxonomyDefinitionInstance: taxonomyDefinitionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete() {
        def taxonomyDefinitionInstance = TaxonomyDefinition.get(params.id)
        if (taxonomyDefinitionInstance) {
            try {
                taxonomyDefinitionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition'), params.id])}"
            redirect(action: "list")
        }
    }
}
