package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import grails.util.Holders;
import static org.springframework.http.HttpStatus.*;

class FactController extends AbstractObjectController {

    def factService;


    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

    def index = {
        redirect(action: "list", params: params)
    }

    def list() {
        //render (view:'list', model:['traitList' : traitService.listTraits(params)]);
        def model = [:];
        model['factInstanceList'] = factService.list(params);
        println "model============"+model
        model['obvListHtml'] = g.render(template:"/fact/factListTemplate", model:model);
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
        render(view:'show', model:factService.show(params.id))
    }

    @Secured(['ROLE_ADMIN'])
    def save() {
        params.locale_language = utilsService.getCurrentLanguage(request);
        def result = factService.save(params)
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

/*    def facts() {
        if(params.id) {
            render (view:'facts', model:['factsList' : factService.list(params.id.toLong(), params.trait, params.traitValue), 'traitsList':traitService.listTraits()]);
        } else {
            render (view:'facts', model:['factsList' : factService.list(null, params.trait, params.traitValue), 'traitsList':traitService.listTraits()]);
        }
    }
*/
    @Secured(['ROLE_USER'])
    def upload() {
        File contentRootDir = new File(Holders.config.speciesPortal.content.rootDir+File.separator+params.controller);          
        if(!contentRootDir.exists()) {
            contentRootDir.mkdir();
        }
        
        params.file = contentRootDir.getAbsolutePath()+File.separator+params.file;
        def r = factService.upload(params);
        redirect(action: "list")
    }

}
