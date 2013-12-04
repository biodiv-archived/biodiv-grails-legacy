package species.participation

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import java.util.List;

class MapController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "show", params: params)
    }

    def show = {
        render (view:'show');
    }
}
