package species.dataset;

import java.util.List;
import java.util.Map;

import grails.converters.JSON;
import grails.converters.XML;
import species.AbstractObjectController;

import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*;
import species.participation.Observation;
import species.dataset.DataPackage;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.grails.taggable.*
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;
import species.utils.ImageUtils;
import species.utils.Utils;

class DataPackageController extends AbstractObjectController {
	
	def springSecurityService;
	def mailService;
	def messageSource;

    def dataPackageService;

	static allowedMethods = [show:'GET', index:'GET', list:'GET',  update: ["POST","PUT"], delete: ["POST", "DELETE"], flagDeleted: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}
    
    @Secured(['ROLE_ADMIN'])
	def create() {
		def dataPackageInstance = new DataPackage()
		
        dataPackageInstance.properties = params;
        return [dataPackageInstance: dataPackageInstance]
	}

    @Secured(['ROLE_ADMIN'])
	def edit() {
		def dataPackageInstance = DataPackage.findWhere(id:params.id?.toLong(), isDeleted:false)
		if (!dataPackageInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dataPackage.label', default: 'DataPackage'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"dataPackage", 'userGroupWebaddress':params.webaddress))
			//redirect(action: "list")
		} else if(utilsService.ifOwns(dataPackageInstance.author)) {
			render(view: "create", model: [dataPackageInstance: dataPackageInstance, 'springSecurityService':springSecurityService])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"dataPackage", id:dataPackageInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	@Secured(['ROLE_ADMIN'])
	def save() {
	    saveAndRender(params, false)
	}

	@Secured(['ROLE_ADMIN'])
	def update() {
		def dataPackageInstance = DataPackage.get(params.long('id'))
        def msg;
		if(dataPackageInstance)	{
			saveAndRender(params, true)
		} else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataPackage.label', default: 'DataPackage'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
                    flash.message = msg;
			        redirect (url:uGroup.createLink(action:'list', controller:"dataPackage"))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}
		
	private saveAndRender(params, sendMail=true){
		params.locale_language = utilsService.getCurrentLanguage(request);
		def result = dataPackageService.save(params, sendMail)
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
			        render(view: "create", model: [dataPackageInstance: result.instance])
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
			def dataPackageInstance = DataPackage.findByIdAndIsDeleted(params.id, false)
			if (!dataPackageInstance) {
                msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataPackage.label', default: 'DataPackage'), params.id])}"
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    html {
				        flash.message = model.msg;
				        redirect (url:uGroup.createLink(action:'list', controller:"dataPackage", 'userGroupWebaddress':params.webaddress))
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
			else {
				//dataPackageInstance.incrementPageVisit()
				def userLanguage = utilsService.getCurrentLanguage(request);   

                def model = utilsService.getSuccessModel("", dataPackageInstance, OK.value());

                withFormat {
                    html {
                            return [dataPackageInstance: dataPackageInstance, 'userLanguage':userLanguage, max:10]
                    } 
                    json  { render model as JSON }
                    xml { render model as JSON }
                }
			}
		} else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'dataPackage.label', default: 'DataPackage'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
			        redirect (url:uGroup.createLink(action:'list', controller:"dataPackage", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
	}

	def list() {
		def model = getDataPackageList(params);
        model.userLanguage = utilsService.getCurrentLanguage(request);

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model.resultType = 'dataPackage'
            //model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
            model['obvListHtml'] =  g.render(template:"/dataPackage/showDataPackageListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('dataPackageInstanceList');
        }
        
        model = utilsService.getSuccessModel('', null, OK.value(), model);

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/dataPackage/showDataPackageListTemplate", model:model.model);
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

	protected def getDataPackageList(params) {
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
        def filteredDataPackage = dataPackageService.getFilteredDataPackages(params, max, offset, false)
        def instanceList = filteredDataPackage.instanceList

        def queryParams = filteredDataPackage.queryParams
        def activeFilters = filteredDataPackage.activeFilters
        def count = filteredDataPackage.instanceTotal	

        activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]
        return [instanceList: instanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'dataPackage']
	}

}
