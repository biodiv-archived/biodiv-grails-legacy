package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import grails.util.Holders;
import static org.springframework.http.HttpStatus.*;

class TraitController extends AbstractObjectController {

    def traitService;
    def speciesService;    

    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

    def index = {
        redirect(action: "list", params: params)
    }

    def list() {
        def model = getList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = params.controller;
            //model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
            model['obvListHtml'] =  g.render(template:"/${params.controller}/show${params.controller.capitalize()}ListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('instanceList');
        }

        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/${params.controller}/show${params.controller.capitalize()}ListTemplate", model:model.model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    model.model['width'] = 300;
                    model.model['height'] = 200;
                    render (view:"list", model:model.model)
                    return;
                } else {

                    return;
                }
            }
            json { render model as JSON }
            xml { render model as XML }
        }
    }

    protected def getList(params) {
        try { 
            params.max = params.max?Integer.parseInt(params.max.toString()):100;
        } catch(NumberFormatException e) { 
            params.max = 100;
        }

        try { 
            params.offset = params.offset?Integer.parseInt(params.offset.toString()):0; 
        } catch(NumberFormatException e) { 
            params.offset = 0 
        }

        def max = Math.min(params.max ? params.int('max') : 100, 100)
        def offset = params.offset ? params.int('offset') : 0
        def filteredList = traitService.getFilteredList(params, max, offset)
        def instanceList = filteredList.instanceList

        def queryParams = filteredList.queryParams
        def activeFilters = filteredList.activeFilters
        def count = filteredList.instanceTotal	

        activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]

        if(params.append?.toBoolean() && session["${params.controller}_ids_list"]) {
            session["${params.controller}_ids_list"].addAll(instanceList.collect {
                params.fetchField?it[0]:it.id
            }); 
        } else {
            session["${params.controller}_ids_list_params"] = params.clone();
            session["${params.controller}_ids_list"] = instanceList.collect {
                params.fetchField?it[0]:it.id
            };
        }
        log.debug "Storing all ${params.controller} ids list in session ${session[params.controller+'_ids_list']} for params ${params}";
        return [instanceList: instanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:params.controller]
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

    def matchingSpecies() {
        Map result = [:];
        try {
            def matchingSpeciesListResult;
            matchingSpeciesListResult = speciesService.getMatchingSpeciesList(params);
            if(matchingSpeciesListResult.matchingSpeciesList.size() > 0) {
                matchingSpeciesListResult.putAll([status:'success', msg:'success']);
                result = matchingSpeciesListResult;
                
            } else {
                def message = "";
                if(params.offset  > 0) {
                    message = g.message(code: 'recommendations.nomore.message', default:'No more distinct species. Please contribute');
                } else {
                    message = g.message(code: 'recommendations.zero.message', default:'No species. Please contribute');
                }
                result = [msg:message]
            }
            def model = utilsService.getSuccessModel(result.msg, null, OK.value(), result);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }

        } catch(e) {
            e.printStackTrace();
            log.error e.getMessage();
            String msg = g.message(code: 'error', default:'Error while processing the request.');
            def model = utilsService.getErrorModel(msg, null, OK.value(), [e.getMessage()]);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }
}
