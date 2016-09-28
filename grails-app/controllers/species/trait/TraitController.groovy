package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import grails.util.Holders;

class TraitController extends AbstractObjectController {

    def traitService;
    

    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

    def index = {
        redirect(action: "list", params: params)
    }

    def list() {
        //render (view:'list', model:['traitList' : traitService.listTraits(params)]);
        def model = [:];
        model['traitInstanceList'] = traitService.listTraits(params);
        println "model============"+model
        model['obvListHtml'] = g.render(template:"/trait/traitListTemplate", model:model);
        model = utilsService.getSuccessModel('', null, 200, model);

        withFormat {
            html {
                render(view:"list", model:model.model);

            }
            json {
                render model as JSON 
            }
            xml {
                render model as XML
            }
        }
    }

    def show() {
        render(view:'show', model:traitService.showTrait(params))
    }

    @Secured(['ROLE_USER'])
    def upload() {
        File contentRootDir = new File(Holders.config.speciesPortal.content.rootDir + File.separator + params.controller);          
        if(!contentRootDir.exists()) {
            contentRootDir.mkdir();
        }
        
        params.file = contentRootDir.getAbsolutePath() + File.separator + params.file;
        params.tvFile = contentRootDir.getAbsolutePath() + File.separator + params.tvFile;

        def r = traitService.upload(params);
        redirect(action: "list")
    }
}
