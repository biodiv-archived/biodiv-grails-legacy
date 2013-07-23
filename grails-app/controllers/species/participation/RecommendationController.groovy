package species.participation

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import java.util.List;

import org.apache.solr.common.util.NamedList

import species.Species;
import species.TaxonomyDefinition;
import species.participation.Recommendation;

class RecommendationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def namesIndexerService;
	def recommendationService;
	def searchFieldsConfig = grailsApplication.config.speciesPortal.nameSearchFields

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [recommendationInstanceList: Recommendation.list(params), recommendationInstanceTotal: Recommendation.count()]
    }

	@Secured(['ROLE_USER'])
    def create = {
        def recommendationInstance = new Recommendation()
        recommendationInstance.properties = params
        return [recommendationInstance: recommendationInstance]
    }

	@Secured(['ROLE_USER'])
    def save = {
        def recommendationInstance = new Recommendation(params)
        if (recomendationService.save(recommendationInstance)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), recommendationInstance.id])}"
            redirect(action: "show", id: recommendationInstance.id)
        }
        else {
            render(view: "create", model: [recommendationInstance: recommendationInstance])
        }
    }

    def show = {
        def recommendationInstance = Recommendation.get(params.id)
        if (!recommendationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
            redirect(action: "list")
        }
        else {
            [recommendationInstance: recommendationInstance]
        }
    }

	@Secured(['ROLE_USER'])
    def edit = {
        def recommendationInstance = Recommendation.get(params.id)
        if (!recommendationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [recommendationInstance: recommendationInstance]
        }
    }

	@Secured(['ROLE_USER'])
    def update = {
        def recommendationInstance = Recommendation.get(params.id)
        if (recommendationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (recommendationInstance.version > version) {
                    
                    recommendationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'recommendation.label', default: 'Recommendation')] as Object[], "Another user has updated this Recommendation while you were editing")
                    render(view: "edit", model: [recommendationInstance: recommendationInstance])
                    return
                }
            }
			def oldRecommendationInstance = recommendationInstance; 
            recommendationInstance.properties = params
			recommendationInstance.lastModified = new Date();
            if (!recommendationInstance.hasErrors() && recommendationService.update(oldRecommendationInstance, recommendationInstance)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), recommendationInstance.id])}"
                redirect(action: "show", id: recommendationInstance.id)
            }
            else {
                render(view: "edit", model: [recommendationInstance: recommendationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
            redirect(action: "list")
        }
    }

	@Secured(['ROLE_ADMIN'])
    def delete = {
        def recommendationInstance = Recommendation.get(params.id)
        if (recommendationInstance) {
            try {
                recommendationService.delete(recommendationInstance)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'recommendation.label', default: 'Recommendation'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
    * suggest recommendations
    */
	def suggest = {
		render namesIndexerService.suggest(params) as JSON;
	}
 
    /**
    * parse names and find out a matching a reco
    */
    def getRecos = {
        log.debug params;
        def names = params.names;
        def result = recommendationService.getRecosForNames(params.names)
        render result as JSON
    }

    def p = {
        render "asd"
    }
}
