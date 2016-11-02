package species.trait

import grails.converters.JSON;
import grails.converters.XML;

import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import grails.util.Holders;
import static org.springframework.http.HttpStatus.*;
import species.Field;
import species.utils.Utils;
import species.utils.ImageUtils;
import species.utils.ImageType;

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
            model.hackTohideTraits = true;
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
                    model.model.hackTohideTraits = true;
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
        def traitInstance = Trait.findById(params.id)
        def coverage = traitInstance.taxon
        def traitValue = [];
        Field field;
        traitValue = TraitValue.findAllByTrait(traitInstance);
        field = Field.findById(traitInstance.fieldId);
            def model = getList(params);
             model['traitInstance'] = traitInstance
             model['coverage'] = coverage.name
             model['traitValue'] = traitValue
             model['field'] = field.concept
            withFormat {
            html {
                    render (view:"show", model:model)
                 }
        json { render utilsService.getSuccessModel('', traitInstance, OK.value()) as JSON }
        xml { render utilsService.getSuccessModel('', traitInstance, OK.value()) as XML }
            }
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
            result = speciesService.getMatchingSpeciesList(params);
            result.resultType = 'species';
            result.hideId = true;
            result.instanceTotal = result.totalCount;//matchingSpeciesList.size();
            //result.totalCount = result.instanceTotal;
            params.action = 'list';
            result['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:result);
 
            if(result.matchingSpeciesList.size() > 0) {
                result.putAll([status:'success', msg:'success']);
           } else {
                def message = "";
                if(params.offset  > 0) {
                    message = g.message(code: 'recommendations.nomore.message', default:'No more distinct species. Please contribute');
                } else {
                    message = g.message(code: 'recommendations.zero.message', default:'No species. Please contribute');
                }
                result.putAll([msg:message]);
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

    @Secured(['ROLE_ADMIN'])
    def migarteIcons(){
        def rs = [:];
        def resourcesInfo = [];
        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
        File usersDir;
        def message;

        def trait_values = TraitValue.list();
        if(!usersDir) {
            if(!params.dir) {
                usersDir = new File(rootDir);
                if(!usersDir.exists()) {
                    usersDir.mkdir();
                }
                usersDir = new File(usersDir, UUID.randomUUID().toString()+File.separator+"resources");
                usersDir.mkdirs();
            } else {
                usersDir = new File(rootDir, params.dir);
                usersDir.mkdir();
            }
        }
        trait_values.each { f ->
            if(f.icon){                
                boolean iconPresent = (new File(rootDir+'/200_'+f.icon)).exists();
                if(!iconPresent){
                    println "Not Inserted file ="+f.id
                }else{
                    File file = utilsService.getUniqueFile(usersDir, Utils.generateSafeFileName("200_"+f.icon));
                    File fi = new File(rootDir+"/200_"+f.icon);
                    (new AntBuilder()).copy(file: fi, tofile: file)
                    //fi.transferTo( file );
                    ImageUtils.createScaledImages(file, usersDir,true);
                    def file_name = file.name.toString();
                    
                    f.icon = usersDir.absolutePath.replace(rootDir, "")+'/'+file_name;
                    f.save(flush:true);
                }
                //resourcesInfo.add([fileName:file.name, size:f.size]);
            }        
        }
    }
}
