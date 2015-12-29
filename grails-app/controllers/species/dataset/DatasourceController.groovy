package species.dataset;

import java.util.List;
import java.util.Map;

import grails.converters.JSON;
import grails.converters.XML;
import species.AbstractObjectController;

import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*;
import species.participation.Observation;
import species.dataset.Datasource;

class DatasourceController extends AbstractObjectController {
	
	def springSecurityService;
	def mailService;
	def messageSource;

    def datasourceService;

	static allowedMethods = [show:'GET', index:'GET', list:'GET',  update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}
    
    @Secured(['ROLE_ADMIN'])
	def create() {
		def datasourceInstance = new Datasource()
		
        datasourceInstance.properties = params;
		def author = springSecurityService.currentUser;
        
        return [datasourceInstance: datasourceInstance]
	}

	@Secured(['ROLE_ADMIN'])
	def save() {
	    saveAndRender(params, false)
	}

	@Secured(['ROLE_ADMIN'])
	def update() {
		def datasourceInstance = Datasource.get(params.long('id'))
        def msg;
		if(datasourceInstance)	{
			saveAndRender(params, true)
		} else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'datasource.label', default: 'Datasource'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
                    flash.message = msg;
			        redirect (url:uGroup.createLink(action:'list', controller:"datasource"))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}
		
	private saveAndRender(params, sendMail=true){
		params.locale_language = utilsService.getCurrentLanguage(request);
		def result = datasourceService.save(params, sendMail)
		if(result.success){
            withFormat {
                html {
			        redirect(action: "show", id: result.instance.id)
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
			        render(view: "create", model: [datasourceInstance: result.instance])
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

	def show() {
        params.id = params.long('id');
        def msg;
        if(params.id) {
			def datasourceInstance = Datasource.findByIdAndIsDeleted(params.id, false)
			if (!datasourceInstance) {
                msg = "${message(code: 'default.not.found.message', args: [message(code: 'datasource.label', default: 'Datasource'), params.id])}"
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    html {
				        flash.message = model.msg;
				        redirect (url:uGroup.createLink(action:'list', controller:"datasource", 'userGroupWebaddress':params.webaddress))
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
			else {
				//datasourceInstance.incrementPageVisit()
				def userLanguage = utilsService.getCurrentLanguage(request);   

                def model = utilsService.getSuccessModel("", datasourceInstance, OK.value());

                withFormat {
                    html {
                            return [datasourceInstance: datasourceInstance, 'userLanguage':userLanguage, max:10]
                    } 
                    json  { render model as JSON }
                    xml { render model as JSON }
                }
			}
		} else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'datasource.label', default: 'Datasource'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
			        redirect (url:uGroup.createLink(action:'list', controller:"datasource", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
	}

	def list() {
		def model = getDatasourceList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = 'datasource'
            //model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
            model['obvListHtml'] =  g.render(template:"/datasource/showDatasourceListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('datasourceInstanceList');
        }
        
        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/datasource/showDatasourceListTemplate", model:model.model);
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

	protected def getDatasourceList(params) {
        try { 
            params.max = params.max?Integer.parseInt(params.max.toString()):24 
        } catch(NumberFormatException e) { 
            params.max = 24 
        }
        try { 
            params.offset = params.offset?Integer.parseInt(params.offset.toString()):0; 
        } catch(NumberFormatException e) { 
            params.offset = 0 
        }

        def max = Math.min(params.max ? params.int('max') : 24, 100)
        def offset = params.offset ? params.int('offset') : 0
        def filteredDatasource = datasourceService.getFilteredDatasources(params, max, offset, false)
        def instanceList = filteredDatasource.instanceList

        def queryParams = filteredDatasource.queryParams
        def activeFilters = filteredDatasource.activeFilters
        def count = filteredDatasource.instanceTotal	

        activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]

        if(params.append?.toBoolean() && session["obv_ids_list"]) {
            session["datasource_ids_list"].addAll(instanceList.collect {
                params.fetchField?it[0]:it.id
            }); 
        } else {
            session["datasource_ids_list_params"] = params.clone();
            session["datasource_ids_list"] = instanceList.collect {
                params.fetchField?it[0]:it.id
            };
        }
        log.debug "Storing all datasource ids list in session ${session['datasource_ids_list']} for params ${params}";
        return [instanceList: instanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'datasource']
	}

}
