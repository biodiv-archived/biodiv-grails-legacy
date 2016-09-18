package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured


class TraitController extends AbstractObjectController {

    def traitService;
    

    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}

    def list() {
        render (view:'list', model:['traitList' : traitService.listTraits(params)]);
    }

    def show() {
        render(view:'show', model:traitService.showTrait(params.id))
    }

    @Secured(['ROLE_ADMIN'])
    def uploadFacts () {

    }

    @Secured(['ROLE_ADMIN'])
    def saveFacts() {
        params.locale_language = utilsService.getCurrentLanguage(request);
        def result = traitService.saveFacts(params)
        if(result.success){
            withFormat {
                html {
                    redirect(controller:'species', action: "facts")
                }
                json {
                    render result as JSON 
                }
                xml {
                    render result as XML
                }
            }

        } else {
            withFormat {
                html {
                    //flash.message = "${message(code: 'error')}";
                    render(controller:'species', view: "uploadFacts", model: [])
                }
                json {
                    result.remove('instance');
                    render result as JSON 
                }
                xml {
                    result.remove('instance');
                    render result as XML
                }
            }
        }

    }

    def facts() {
        if(params.id) {
            render (view:'facts', model:['factsList' : traitService.listFacts(params.id.toLong(), params.trait, params.traitValue), 'traitsList':traitService.listTraits()]);
        } else {
            render (view:'facts', model:['factsList' : traitService.listFacts(null, params.trait, params.traitValue), 'traitsList':traitService.listTraits()]);
        }

    }

    @Secured(['ROLE_ADMIN'])
    def testTraitDefinition(){
        Language languageInstance = utilsService.getCurrentLanguage(request);
        traitService.loadTraitDefinitions("${grailsApplication.config.speciesPortal.app.rootDir}/parent.tsv", languageInstance);
    }

    @Secured(['ROLE_ADMIN'])
    def testTraitValue(){
        Language languageInstance = utilsService.getCurrentLanguage(request);
        traitService.loadTraitValues("${grailsApplication.config.speciesPortal.app.rootDir}/traitvalue.tsv", languageInstance);
    }

    @Secured(['ROLE_ADMIN'])
    def testFacts() {
        Language languageInstance = utilsService.getCurrentLanguage(request);
        traitService.loadFacts("${grailsApplication.config.speciesPortal.app.rootDir}/traitfacts.xlsx", languageInstance);
    }
}
