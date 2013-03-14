package content

class LocationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [locationInstanceList: Location.list(params), locationInstanceTotal: Location.count()]
    }

    def create = {
        def locationInstance = new Location()
        locationInstance.properties = params
        return [locationInstance: locationInstance]
    }

    def save = {
        def locationInstance = new Location(params)
        if (locationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'location.label', default: 'Location'), locationInstance.id])}"
            redirect(action: "show", id: locationInstance.id)
        }
        else {
            render(view: "create", model: [locationInstance: locationInstance])
        }
    }

    def show = {
        def locationInstance = Location.get(params.id)
        if (!locationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        }
        else {
            [locationInstance: locationInstance]
        }
    }

    def edit = {
        def locationInstance = Location.get(params.id)
        if (!locationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [locationInstance: locationInstance]
        }
    }

    def update = {
        def locationInstance = Location.get(params.id)
        if (locationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (locationInstance.version > version) {
                    
                    locationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'location.label', default: 'Location')] as Object[], "Another user has updated this Location while you were editing")
                    render(view: "edit", model: [locationInstance: locationInstance])
                    return
                }
            }
            locationInstance.properties = params
            if (!locationInstance.hasErrors() && locationInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'location.label', default: 'Location'), locationInstance.id])}"
                redirect(action: "show", id: locationInstance.id)
            }
            else {
                render(view: "edit", model: [locationInstance: locationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def locationInstance = Location.get(params.id)
        if (locationInstance) {
            try {
                locationInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        }
    }
}
