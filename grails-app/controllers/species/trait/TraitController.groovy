package species.trait

import grails.converters.JSON;
import grails.converters.XML;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import species.Language;
import species.AbstractObjectController;
import grails.plugin.springsecurity.annotation.Secured
import species.participation.UploadLog;
import grails.util.Holders;
import static org.springframework.http.HttpStatus.*;
import species.Field;
import species.participation.Recommendation;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import species.utils.Utils;
import species.utils.ImageUtils;
import species.TaxonomyDefinition;
import species.utils.ImageType;
import groovy.sql.Sql
import species.trait.Trait.DataTypes;

class TraitController extends AbstractObjectController {

    def traitService;
    def speciesService;
    def messageSource;
    def dataSource;

    static allowedMethods = [show:'GET', index:'GET', list:'GET', edit:"GET" ,  save: "POST", delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"],update:"POST",create:"GET"]
    static defaultAction = "list"

    def index = {
        redirect(action: "list", params: params, namespace: null)
    }

    def list() {
        def model = getList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);
        if(params.displayAny) model.displayAny = params.displayAny?.toBoolean();
        else model.displayAny = true;
        if(params.editable) model.editable = params.editable?.toBoolean();
        else model.editable = false;
        if(params.filterable) model.filterable = params.filterable?.toBoolean();
        else model.filterable = true;
        if(params.fromObservationShow) model.fromObservationShow = params.fromObservationShow;
        if(params.ifOwns) model.ifOwns = params.ifOwns.toBoolean();
        //HACK
        if(params.trait) {
            model.queryParams = ['trait':[:]];
            params.trait.each { t,v ->
                model.queryParams.trait[Long.parseLong(t)] = v
            }
        }
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

    @Secured(['ROLE_USER'])
    def create() {
        def traitInstance = new Trait() 
        traitInstance.properties = params;
        return [traitInstance: traitInstance]
    }

    @Secured(['ROLE_USER'])
    def edit() {
        def traitInstance = Trait.findByIdAndIsDeleted(params.id,false)       ;
        if (!traitInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'trait.label', default: 'Trait'), params.id])}"
            redirect(uGroup.createLink(action: "list", controller:"trait", 'userGroupWebaddress':params.webaddress))
        }  else {
            Language userLanguage = utilsService.getCurrentLanguage(request);
            Field field;
            field = Field.findByConnectionAndLanguage(traitInstance.fieldId,userLanguage);
            render(view: "create", model: [traitInstance: traitInstance , userLanguage:userLanguage,field: field.concept+'|'+field.category])
        }
        return;
    }

    @Secured(['ROLE_USER'])
    def update(){
        def msg = "";
        Trait traitInstance;
        Language languageInstance = utilsService.getCurrentLanguage();
        if(params.id){
            traitInstance=Trait.findById(params.id)
        }
        else{traitInstance=new Trait();
            traitInstance.traitTypes=Trait.fetchTraitTypes(params.traittype);
            traitInstance.dataTypes=Trait.fetchDataTypes(params.datatype);
        }
        params.isNotObservationTrait = (params.isNotObservationTrait)?true:false;
        params.isParticipatory = (params.isParticipatory)?true:false;
        params.showInObservation = (params.showInObservation)?true:false;
        params.languageInstance = languageInstance
        
        traitInstance.properties = params;
        def speciesField=params.fieldid.replaceAll(">", "|").trim()
        def fieldInstance=traitService.getField(speciesField,languageInstance)
        traitInstance.field=Field.get(fieldInstance.connection);
        traitInstance?.taxon?.clear()       
        
        if(params.taxonName){
            def taxonId
            params.taxonName = !String.isCase(params.taxonName)?params.taxonName:[params.taxonName];
            params.taxonName.each{
                taxonId=it
                taxonId = taxonId.substring(taxonId.indexOf("(") + 1);                    
                taxonId = taxonId.substring(0, taxonId.indexOf("-"));                
                TaxonomyDefinition taxon = TaxonomyDefinition.findById(taxonId);
                traitInstance.addToTaxon(taxon);
            }
        }        
        if (!traitInstance.hasErrors() && traitInstance.save(flush: true)) {
            def traitTransInstance= TraitTranslation.findByTraitAndLanguage(traitInstance,params.languageInstance);
                if(!traitTransInstance)
                    traitTransInstance = new TraitTranslation();
            traitTransInstance.name = traitInstance.name
            traitTransInstance.description = traitInstance.description
            traitTransInstance.source = traitInstance.source
            traitTransInstance.language=languageInstance
            traitTransInstance.trait=traitInstance
            traitTransInstance.save(flush: true)
            msg = "${message(code: 'default.updated.message', args: [message(code: 'trait.label', default: 'Trait'), traitInstance.id])}"
            def model = utilsService.getSuccessModel(msg, traitInstance, OK.value());
            if(params.action=='update'){
                traitService.createTraitValue(traitInstance,params)
            }
            withFormat {
                html {
                    flash.message = msg;
                    redirect(url: uGroup.createLink(controller:"trait" , action: "show", id: traitInstance.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
            return
        }
        else {
            def errors = [];
            traitInstance.errors.allErrors .each {
                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
            }

            def model = utilsService.getErrorModel("Failed to update trait", traitInstance, OK.value(), errors);
            withFormat {
                html {
                    render(view: "create", model: [traitInstance: traitInstance])
                }
                json { render model as JSON }
                xml { render model as XML }
            }
            return;
        }

    }

    @Secured(['ROLE_USER'])
    def flagDeleted() {
        def result = traitService.delete(params)
        result.remove('url')
        String url = result.url;
        withFormat {
            html {
                flash.message = result.message
                redirect (url:url)
            }
            json { render result as JSON }
            xml { render result as XML }
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
        return [instanceList: instanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:params.controller, 'factInstance':filteredList.traitFactMap, instance:filteredList.object, numericTraitMinMax:filteredList.numericTraitMinMax];
    }

    def show() {
        Trait traitInstance = Trait.findByIdAndIsDeleted(params.id,false)
        Language userLanguage = utilsService.getCurrentLanguage(request);
        String msg;
        if(traitInstance) {
            def coverage = traitInstance.taxon
            def traitValue = [];
            Field field;
            def traitTrans = TraitTranslation.findByTraitAndLanguage(traitInstance,userLanguage);
                traitInstance.name = (traitTrans?.name)?:'';
                traitInstance.description = (traitTrans?.description)?:'';
                traitInstance.source = (traitTrans?.source)?:'';
            traitValue = TraitValue.findAllByTraitAndIsDeleted(traitInstance,false);
            def traitValueRes = []
            traitValue.each{ tv ->
            def traitValTrans = TraitValueTranslation.findByTraitValueAndLanguage(tv,userLanguage);
                if(traitValTrans){
                    tv.value = (traitValTrans?.value)?:'';
                    tv.description = (traitValTrans?.description)?:'';
                    tv.source = (traitValTrans?.source)?:'';
                    traitValueRes << tv;
                }
            }
            field = Field.findById(traitInstance.fieldId);
            def model = getList(params);
            model['traitInstance'] = traitInstance
            model['coverage'] = coverage*.name
            model['traitValue'] = traitValueRes
            model['field'] = field.concept
            model['userLanguage']=userLanguage

            //HACK
            if(params.trait) {
                model.queryParams = ['trait':[:]];
                params.trait.each { t,v ->
                    model.queryParams.trait[Long.parseLong(t)] = v
                }
            }
            if(traitInstance.dataTypes == DataTypes.NUMERIC) {
                Sql sql = Sql.newInstance(dataSource);
                List numericTraitMinMax =  sql.rows("""
                select min(f.value::float)::integer,max(f.to_value::float)::integer,t.id from fact f,trait t where f.trait_id = t.id and t.data_types='NUMERIC' and t.id=:traitId group by t.id;
                """, [traitId:traitInstance.id]);
                model['numericTraitMinMax'] = numericTraitMinMax[0];
            }
            withFormat {
                html {
                    render (view:"show", model:model)
                }
                json { render utilsService.getSuccessModel('', traitInstance, OK.value()) as JSON }
                xml { render utilsService.getSuccessModel('', traitInstance, OK.value()) as XML }
            }
        } else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'trait.label', default: 'Trait'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
                    flash.message = msg;
                    redirect (url:uGroup.createLink(action:'list', controller:"trait", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
        
        return
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

    def updateTraitValue(){
        TraitValue traitValueInstance;
        Language userLanguage = utilsService.getCurrentLanguage(request);
        if(params.traitValueId){
            traitValueInstance = TraitValue.findById(params.traitValueId);
            traitValueInstance.value = params.value
            traitValueInstance.description = params.description
            traitValueInstance.source = params.source
            traitValueInstance.icon = traitService.getTraitIcon(params.icon)
        }
        else{
            traitValueInstance = new TraitValue();
            traitValueInstance.value = params.value
            traitValueInstance.description = params.description
            traitValueInstance.source = params.source
            Trait traitInstance = Trait.findById(params.traitInstance)
            traitValueInstance.trait = traitInstance
        }
        if (!traitValueInstance.hasErrors() && traitValueInstance.save(flush: true)) {
            def msg = "Trait Value Added Successfully"
            traitService.updateTraitValueTranslation(traitValueInstance,userLanguage);
            flash.message=msg
            return
        }
        else{
            def errors = [];
            traitValueInstance.errors.allErrors .each {
                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
                return
            }
        }
    }

    @Secured(['ROLE_USER'])
    def upload_resource() {
        try {
            if(ServletFileUpload.isMultipartContent(request)) {
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                def rs = [:]
                Utils.populateHttpServletRequestParams(request, rs);
                def resourcesInfo = [];
                def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
                File usersDir;
                def message;

                if(!params.resources) {
                    message = g.message(code: 'no.file.attached', default:'No file is attached')
                }

                params.resources.each { f ->
                    log.debug "Saving user file ${f.originalFilename}"

                    // List of OK mime-types
                    //TODO Move to config
                    def okcontents = [
                    'image/png',
                    'image/jpeg',
                    'image/pjpeg',
                    'image/gif',
                    'image/jpg'
                    ]

                    if (! okcontents.contains(f.contentType)) {
                        message = g.message(code: 'resource.file.invalid.extension.message', args: [
                        okcontents,
                        f.originalFilename
                        ])
                    }
                    else if(f.size > grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE) {
                        message = g.message(code: 'resource.file.invalid.max.message', args: [
                        grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE/1024,
                        f.originalFilename,
                        f.size/1024
                        ], default:"File size cannot exceed ${grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE/1024}KB");
                    }
                    else if(f.empty) {
                        message = g.message(code: 'file.empty.message', default:'File cannot be empty');
                    }
                    else {
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

                        File file = utilsService.getUniqueFile(usersDir, Utils.generateSafeFileName(f.originalFilename));
                        f.transferTo( file );
                        ImageUtils.createScaledImages(file, usersDir, true);
                        resourcesInfo.add([fileName:file.name, size:f.size]);
                    }
                }
                log.debug resourcesInfo
                // render some XML markup to the response
                if(usersDir && resourcesInfo) {
                    withFormat {
                        json { 
                            def res = [];
                            for(r in resourcesInfo) {
                                res << ['fileName':r.fileName, 'size':r.size]
                            }
                            def model = utilsService.getSuccessModel("", null, OK.value(), [users:[dir:usersDir.absolutePath.replace(rootDir, ""), resources : res]])
                            render model as JSON
                        }

                        xml { 
                            render(contentType:'text/xml') {
                                response {
                                    success(true)
                                    status(OK.value())
                                    msg('Successfully uploaded the resource')
                                    model {
                                        dir(usersDir.absolutePath.replace(rootDir, ""))
                                        resources {
                                            for(r in resourcesInfo) {
                                                'image'('fileName':r.fileName, 'size':r.size){}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    response.setStatus(500);
                    def model = utilsService.getErrorModel(message, null, INTERNAL_SERVER_ERROR.value())
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML }
                    }
                }
            } else {
                def model = utilsService.getErrorModel(g.message(code: 'no.file.attached', default:'No file is attached'), null, INTERNAL_SERVER_ERROR.value())
                withFormat {
                    json { render model as JSON }
                    xml { render model as XML }
                }
            }
        } catch(e) {
            e.printStackTrace();
            def model = utilsService.getErrorModel(g.message(code: 'file.upload.fail', default:'Error while processing the request.'), null, INTERNAL_SERVER_ERROR.value())
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }

    def deleteValue(){
        def traitValueInstance=TraitValue.findById(params.id);
        traitValueInstance.isDeleted=true
        if (!traitValueInstance.hasErrors() && traitValueInstance.save(flush: true)) {
            def msg = "Trait Value deleted Successfully"
            flash.message=msg
            return
        }
        else{
            def errors = [];
            traitValueInstance.errors.allErrors .each {
                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
                return
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
    def taxonTags = {
        def taxon  = TaxonomyDefinition.findAllByNameIlike("${params.term}%")
        def taxonList = [];
            taxon.each {
                taxonList << it.name+' ('+it.id+'-'+it.status+'-'+it.position+')'
            }
        render taxonList as JSON
    }

}
