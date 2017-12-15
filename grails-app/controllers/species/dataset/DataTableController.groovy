package species.dataset;

import java.util.List;
import java.util.Map;

import grails.converters.JSON;
import grails.converters.XML;
import species.AbstractObjectController;

import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*;
import species.participation.Observation;
import species.participation.Checklists;
import species.dataset.DataPackage.DataTableType;
import species.dataset.DataTable;

class DataTableController extends AbstractObjectController {
	
	def springSecurityService;
	def mailService;
	def messageSource;

    def dataTableService;

	static allowedMethods = [show:'GET', index:'GET', list:'GET',  update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}
    
    @Secured(['ROLE_USER'])
	def create() {
        Dataset1 datasetInstance;
        if(params.dataset) {
            datasetInstance = Dataset1.read(params.long('dataset'));
        } else {
            datasetInstance = new Dataset1();
            datasetInstance.dataPackage = DataPackage.findByTitle('Checklist');
        }
        
        if(datasetInstance) {
            DataTable dataTableInstance = new DataTable()
            dataTableInstance.dataset = datasetInstance;
            dataTableInstance.properties = params;
            return [dataTableInstance: dataTableInstance]
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'Dataset'), params.id])}"
            redirect (url:uGroup.createLink(action:'list', controller:"dataset", 'userGroupWebaddress':params.webaddress))
        }
    }

	@Secured(['ROLE_USER'])
	def save() {
	    saveAndRender(params, false)
	}

    @Secured(['ROLE_USER'])
	def edit() {
		def dataTableInstance = DataTable.findWhere(id:params.id?.toLong(), isDeleted:false)
		if (!dataTableInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'DataTable'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"dataset", 'userGroupWebaddress':params.webaddress))
			//redirect(action: "list")
		} else if(utilsService.ifOwns(dataTableInstance.author)) {
            String dir = (new File(grailsApplication.config.speciesPortal.content.rootDir + dataTableInstance.uFile.path).parentFile).getAbsolutePath().replace(grailsApplication.config.speciesPortal.content.rootDir, '');
            String multimediaFile = dir + '/multimediaFile.tsv';
            String mappingFile = dir + '/mappingFile.tsv';
            String multimediaMappingFile = dir +'/multimediaMappingFile.tsv';
			render(view: "create", model: [dataTableInstance: dataTableInstance, multimediaFile:multimediaFile, mappingFile:mappingFile, multimediaMappingFile:multimediaMappingFile, 'springSecurityService':springSecurityService])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"dataset", id:dataTableInstance.dataset.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	@Secured(['ROLE_USER'])
	def update() {
		def dataTableInstance = DataTable.get(params.long('id'))
        def msg;
		if(dataTableInstance)	{
			saveAndRender(params, true)
		} else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'DataTable'), params.id])}"
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
		def result = dataTableService.save(params, sendMail)
        log.debug "#######DATATABLE SAVE RESULT######"
        log.debug result;
        log.debug "################################"
		if(result.success){
            withFormat {
                html {
			        redirect(controller:'datatable', action: "show", id: result.instance.id)
                }
                json {
                    if(result.instance.dataset) {
                        result.url = uGroup.createLink(action:'show', controller:"dataset", id:result.instance.dataset.id, fragment:result.instance.id, 'userGroupWebaddress':params.webaddress);
                    }
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
			        render(view: "create", model: [dataTableInstance: result.instance])
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
			def dataTableInstance = DataTable.findByIdAndIsDeleted(params.id, false)
			if (!dataTableInstance) {
                msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'DataTable'), params.id])}"
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    html {
				        flash.message = model.msg;
				        redirect (url:uGroup.createLink(action:'list', controller:"dataset", 'userGroupWebaddress':params.webaddress))
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
			else {
				//dataTableInstance.incrementPageVisit()
				def userLanguage = utilsService.getCurrentLanguage(request);   

                def model = utilsService.getSuccessModel("", dataTableInstance, OK.value());
                model['observations'] = dataTableService.getObservationData(params.id, params)
                model['observationsCount'] = Observation.countByDataTableAndIsDeleted(dataTableInstance, false);

                withFormat {
                    html {
                            return [dataTableInstance: dataTableInstance, observations:model.observations, observationsCount:model.observationsCount, 'userLanguage':userLanguage, max:10]
                    } 
                    json  { render model as JSON }
                    xml { render model as JSON }
                }
			}
		} else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'DataTable'), params.id])}"
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
		def model = getDataTableList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = 'datatable'
            //model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
            model['obvListHtml'] =  g.render(template:"/dataTable/showDataTableListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('dataTableInstanceList');
        }
        
        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/dataTable/showDataTableListTemplate", model:model.model);
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

	protected def getDataTableList(params) {
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
        def filteredDataTable = dataTableService.getFilteredDataTables(params, max, offset, false)
        def instanceList = filteredDataTable.instanceList

        def queryParams = filteredDataTable.queryParams
        def activeFilters = filteredDataTable.activeFilters
        def count = filteredDataTable.instanceTotal	

        activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]

        if(params.append?.toBoolean() && session["obv_ids_list"]) {
            session["dataset_ids_list"].addAll(instanceList.collect {
                params.fetchField?it[0]:it.id
            }); 
        } else {
            session["dataset_ids_list_params"] = params.clone();
            session["dataset_ids_list"] = instanceList.collect {
                params.fetchField?it[0]:it.id
            };
        }
        log.debug "Storing all dataset ids list in session ${session['dataset_ids_list']} for params ${params}";
        return [instanceList: instanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'dataset']
	}

    @Secured(['ROLE_USER'])
	def delete() {
		def result = dataTableService.delete(params)
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

	def observationData = {
        if(!params.id) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'default.dataTable.label', default: 'DataTable'), params.id])}"
            redirect (url:uGroup.createLink(action:'list', controller:"dataTable", 'userGroupWebaddress':params.webaddress))
        }
		def observations = dataTableService.getObservationData(params.id, params)
        def dataTableInstance = DataTable.read(params.id.toLong());
        int instanceCount = Observation.countByDataTableAndIsDeleted(dataTableInstance, false);
		def model =[observations:observations, dataTableInstance:dataTableInstance, observationsCount:instanceCount];
		render(template:"/dataTable/showDataTableDataTemplate", model:model);
	}

}
