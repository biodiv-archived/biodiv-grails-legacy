package species.dataset;

import java.util.List;
import java.util.Map;

import grails.converters.JSON;
import grails.converters.XML;
import species.AbstractObjectController;

import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*;
import species.participation.Observation;

class DatasetController extends AbstractObjectController {
	
	def springSecurityService;
	def mailService;
	def messageSource;

    def datasetService;

	static allowedMethods = [show:'GET', index:'GET', list:'GET',  update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}
    
    @Secured(['ROLE_USER'])
	def create() {
		def datasetInstance = new Dataset()
		
        datasetInstance.properties = params;
		datasetInstance.group = SpeciesGroup.findByName(SpeciesGroup.SpeciesGroupType.ALL.value())
		datasetInstance.habitat = Habitat.findByName(Habitat.HabitatType.ALL.value())

		def author = springSecurityService.currentUser;
        
        return [datasetInstance: datasetInstance]
	}

	@Secured(['ROLE_USER'])
	def save() {
	    saveAndRender(params, false)
	}

	@Secured(['ROLE_USER'])
	def update() {
		def datasetInstance = Dataset.get(params.long('id'))
        def msg;
		if(datasetInstance)	{
			saveAndRender(params, true)
		} else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'Dataset'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
                    flash.message = msg;
			        redirect (url:uGroup.createLink(action:'list', controller:"dataset"))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}
		
	private saveAndRender(params, sendMail=true){
		params.locale_language = utilsService.getCurrentLanguage(request);
		def result = datasetService.save(params, sendMail)
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
			        render(view: "create", model: [datasetInstance: result.instance])
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
			def datasetInstance = Dataset.findByIdAndIsDeleted(params.id, false)
			if (!datasetInstance) {
                msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'Dataset'), params.id])}"
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
				//datasetInstance.incrementPageVisit()
				def userLanguage = utilsService.getCurrentLanguage(request);   

                def model = utilsService.getSuccessModel("", datasetInstance, OK.value());
                model['observations'] = Observation.findAllByDataset(datasetInstance, [max:10, offset:0]);
                model['observationsCount'] = Observation.countByDataset(datasetInstance);

                withFormat {
                    html {
                            return [datasetInstance: datasetInstance, observations:model.observations, observationsCount:model.observationsCount, 'userLanguage':userLanguage, max:10]
                    } 
                    json  { render model as JSON }
                    xml { render model as JSON }
                }
			}
		} else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'Dataset'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
			        redirect (url:uGroup.createLink(action:'list', controller:"dataset", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
	}

	def observationData = {
        if(!params.id) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dataset.label', default: 'Dataset'), params.id])}"
            redirect (url:uGroup.createLink(action:'list', controller:"dataset", 'userGroupWebaddress':params.webaddress))
        }

        Dataset datasetInstance = Dataset.read(params.id.toLong());
		List observations =  Observation.findAllByDataset(datasetInstance, [max:params.int('max'), offset:params.int('offset')]);
        int observationsCount = Observation.countByDataset(datasetInstance);
		def model = ['observations':observations, 'observationsCount':observationsCount, 'checklistInstance':datasetInstance, 'max':10]
		render(template:"/common/checklist/showChecklistDataTemplate", model:model);
	}

	def list() {
		def model = getDatasetList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = 'dataset'
            //model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
            model['obvListHtml'] =  g.render(template:"/dataset/showDatasetListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('datasetInstanceList');
        }
        
        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/dataset/showDatasetListTemplate", model:model.model);
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

	protected def getDatasetList(params) {
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
		def filteredDataset = datasetService.getFilteredDatasets(params, max, offset, false)
		def datasetInstanceList = filteredObservation.datasetInstanceList
        		
        def queryParams = filteredObservation.queryParams
		def activeFilters = filteredObservation.activeFilters
        def canPullResource = filteredDocument.canPullResource
	    def count = filteredDocument.instanceTotal	

		activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]
        
            if(params.append?.toBoolean() && session["obv_ids_list"]) {
                session["dataset_ids_list"].addAll(datasetInstanceList.collect {
                    params.fetchField?it[0]:it.id
                }); 
            } else {
                session["dataset_ids_list_params"] = params.clone();
                session["dataset_ids_list"] = datasetInstanceList.collect {
                    params.fetchField?it[0]:it.id
                };
            }
		log.debug "Storing all dataset ids list in session ${session['dataset_ids_list']} for params ${params}";
		return [datasetInstanceList: datasetInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'dataset', canPullResource:userGroupService.getResourcePullPermission(params)]
	}

}
