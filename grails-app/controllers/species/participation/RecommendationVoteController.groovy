package species.participation

import grails.plugin.springsecurity.Secured

class RecommendationVoteController {

	def grailsApplication;
	def springSecurityService;
	
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [recommendationVoteInstanceList: RecommendationVote.list(params), recommendationVoteInstanceTotal: RecommendationVote.count()]
    }

	@Secured(['ROLE_USER'])
    def create = {
        def recommendationVoteInstance = new RecommendationVote()
        recommendationVoteInstance.properties = params
        return [recommendationVoteInstance: recommendationVoteInstance]
    }

	@Secured(['ROLE_USER'])
    def save = {
		params.author = springSecurityService.currentUser;
		
        def recommendationVoteInstance = new RecommendationVote(params)
		
        if (recommendationVoteInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), recommendationVoteInstance.id])}"
            redirect(controller:'observation', action: "show", id: params.obvId);
        }
        else {
			def observationInstance = Observation.get(params.obvId);
            render(controller: "observation", view: "create", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance])
        }
    }

    def show = {
        def recommendationVoteInstance = RecommendationVote.get(params.id)
        if (!recommendationVoteInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
            redirect(action: "list")
        }
        else {
            [recommendationVoteInstance: recommendationVoteInstance]
        }
    }

	@Secured(['ROLE_USER'])
    def edit = {
        def recommendationVoteInstance = RecommendationVote.get(params.id)
        if (!recommendationVoteInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [recommendationVoteInstance: recommendationVoteInstance]
        }
    }

	@Secured(['ROLE_USER'])
    def update = {
        def recommendationVoteInstance = RecommendationVote.get(params.id)
        if (recommendationVoteInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (recommendationVoteInstance.version > version) {
                    
                    recommendationVoteInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'recommendationVote.label', default: 'RecommendationVote')] as Object[], "Another user has updated this RecommendationVote while you were editing")
                    render(view: "edit", model: [recommendationVoteInstance: recommendationVoteInstance])
                    return
                }
            }
            recommendationVoteInstance.properties = params
            if (!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), recommendationVoteInstance.id])}"
                redirect(action: "show", id: recommendationVoteInstance.id)
            }
            else {
                render(view: "edit", model: [recommendationVoteInstance: recommendationVoteInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
            redirect(action: "list")
        }
    }

	@Secured(['ROLE_ADMIN'])
    def delete = {
        def recommendationVoteInstance = RecommendationVote.get(params.id)
        if (recommendationVoteInstance) {
            try {
                recommendationVoteInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendationVote.label', default: 'RecommendationVote'), params.id])}"
            redirect(action: "list")
        }
    }
}
