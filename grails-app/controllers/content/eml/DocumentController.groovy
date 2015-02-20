package content.eml

import grails.plugin.springsecurity.annotation.Secured

import grails.converters.JSON
import grails.converters.XML
import org.grails.taggable.*
import species.AbstractObjectController;
import speciespage.search.DocumentSearchService;
import static groovyx.net.http.Method.*;
import static groovyx.net.http.ContentType.*
import groovyx.net.http.*
import content.eml.DocSciName;
import static org.springframework.http.HttpStatus.*;

class DocumentController extends AbstractObjectController {

    static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"]]
    static defaultAction = "list"

	def documentService
	def springSecurityService
	def userGroupService
	def activityFeedService
	def documentSearchService
	//def observationService
    def messageSource

	def index = {
		redirect(action: "browser", params: params)
	}

	@Secured(['ROLE_USER'])
	def create() {
		def documentInstance = new Document()
		documentInstance.properties = params
		return [documentInstance: documentInstance]
	}

	@Secured(['ROLE_USER'])
	def save() {
		params.author = springSecurityService.currentUser;
		params.locale_language = utilsService.getCurrentLanguage(request);
		def documentInstance = documentService.createDocument(params)

		log.debug( "document instance with params assigned >>>>>>>>>>>>>>>>: "+ documentInstance)
		if (documentInstance.save(flush: true)) {

			flash.message = "${message(code: 'default.created.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
			activityFeedService.addActivityFeed(documentInstance, null, documentInstance.author, activityFeedService.DOCUMENT_CREATED);

			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			
			documentInstance.setTags(tags)
			if(params.groupsWithSharingNotAllowed) {
				documentService.setUserGroups(documentInstance, [
					params.groupsWithSharingNotAllowed
				]);
			} else {
				if(params.userGroupsList) {
					def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();

					documentService.setUserGroups(documentInstance, userGroups);
				}
			}
			if (params.tokenUrl != '') {
				DocumentTokenUrl.createLog(documentInstance,params.tokenUrl)
			}
			utilsService.sendNotificationMail(activityFeedService.DOCUMENT_CREATED, documentInstance, request, params.webaddress);
			documentSearchService.publishSearchIndex(documentInstance, true)

            def model = utilsService.getSuccessModel(flash.message, documentInstance, OK.value());
            withFormat {
                html {
			        redirect(action: "show", id: documentInstance.id)
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
		else {
            def errors = [];

            documentInstance.errors.allErrors .each {
                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
            }

            def model = utilsService.getErrorModel("Failed to save document", documentInstance, OK.value(), errors);
            withFormat {
                html {
                    documentInstance.errors.allErrors.each { log.error it }
                    render(view: "create", model: [documentInstance: documentInstance])
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

	def show() {
		//cache "content"
        params.id = params.long('id');

		def documentInstance = params.id ? Document.get(params.id):null;
		if (!params.id || !documentInstance) {
            def model = utilsService.getErrorModel("${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}", null, OK.value());

            withFormat {
                html {
			        flash.message = model.msg;
                    redirect(action: "browser")
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
		else {
            def model = utilsService.getSuccessModel("", documentInstance, OK.value())
            withFormat {
                html {
			        def userLanguage = utilsService.getCurrentLanguage(request);
			        model = [documentInstance: documentInstance, userLanguage: userLanguage]
                }
                json { render model as JSON }
                xml { render model as XML }
            }
            return model;
		}
	}

	@Secured(['ROLE_USER'])
	def edit() {
		def documentInstance = Document.get(params.id)
		if (!documentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "browser")
		} else if(utilsService.ifOwns(documentInstance.author)) {
			render(view: "create", model: [documentInstance: documentInstance])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"document", id:documentInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	@Secured(['ROLE_USER'])	
	def update() {
		def documentInstance = Document.get(params.id)
        def msg = "";
		if (documentInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (documentInstance.version > version) {

					documentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'document.label', default: 'Document')] as Object[], "Another user has updated this Document while you were editing")
					render(view: "create", model: [documentInstance: documentInstance])
					return
				}
			}
			if(params.tokenUrl != '') {
				List docSciNames = DocSciName.findAllByDocument(documentInstance);
				docSciNames.each{ 
	 				it.delete(flush: true)
		  		}
				DocumentTokenUrl.createLog(documentInstance, params.tokenUrl)
			}
			params.locale_language = utilsService.getCurrentLanguage(request);
			//documentInstance.properties = params
			documentService.updateDocument(documentInstance, params)
			if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {
				activityFeedService.addActivityFeed(documentInstance, null, springSecurityService.currentUser, activityFeedService.DOCUMENT_UPDATED);
				def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
				
				documentInstance.setTags(tags)
				
				if(params.groupsWithSharingNotAllowed) {
					documentService.setUserGroups(documentInstance, [params.groupsWithSharingNotAllowed]);
				} else {
					if(params.userGroupsList) {
						def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
						
						documentService.setUserGroups(documentInstance, userGroups);
					}
				}
				documentSearchService.publishSearchIndex(documentInstance, true) 
				msg = "${message(code: 'default.updated.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
                def model = utilsService.getSuccessModel(msg, documentInstance, OK.value());
                withFormat {
                    html {
                        flash.message = msg;
				        redirect(action: "show", id: documentInstance.id)
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
            else {
                    def errors = [];
                    documentInstance.errors.allErrors .each {
                        def formattedMessage = messageSource.getMessage(it, null);
                        errors << [field: it.field, message: formattedMessage]
                    }

                    def model = utilsService.getErrorModel("Failed to update document", documentInstance, OK.value(), errors);
                    withFormat {
                        html {
                            render(view: "create", model: [documentInstance: documentInstance])
                        }
                        json { render model as JSON }
                        xml { render model as XML }
                    }
                    return;
            }
		}
		else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value(), errors);
            withFormat {
                html {
			        flash.message = msg;
			        redirect(action: "browser")
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

    @Secured(['ROLE_USER'])	
    def delete() {
        def msg;
        if(!params.id) {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value()); 
            withFormat {
                html {
                    flash.message = msg;
                    redirect(action: "browser")
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        } else {
            try {
                def documentInstance = Document.get(params.long('id'))
                if (documentInstance) {
                    userGroupService.removeDocumentFromUserGroups(documentInstance, documentInstance.userGroups.collect{it.id})
				    documentService.documentDelete(documentInstance)
                    documentInstance.delete(flush: true, failOnError:true)

                    msg = "${message(code: 'default.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"

                    def model = utilsService.getSuccessModel(msg, null, OK.value()); 
                    withFormat {
                        html {
                            flash.message = msg;
                            redirect(action: "browser")
                        }
                        json { render model as JSON }
                        xml { render model as XML }
                    }
                }
                else {
                    msg = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
                    def model = utilsService.getErrorModel(msg, null, OK.value()); 
                    withFormat {
                        html {
                            flash.message = msg;
                            redirect(action: "browser")
                        }
                        json { render model as JSON }
                        xml { render model as XML }
                    }
                }
            }
            catch (Exception e) {
                log.debug e.printStackTrace()
                msg = "${message(code: 'default.not.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
                def model = utilsService.getErrorModel(msg, null, OK.value(), [e.getMessage()]); 
                withFormat {
                    html {
                        flash.message = msg;
                        redirect(action: "show", id: params.id)
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }

            }
        }
    }

    def list() {
		redirect(action: "browser", params: params)
    }
	
	def browser() {
        def model = getDocumentList(params)
        model.userLanguage = utilsService.getCurrentLanguage(request);
        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model['resultType'] = 'document'
            model['obvListHtml'] =  g.render(template:"/document/documentListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
            model.remove('documentInstanceList');
        }

        model = utilsService.getSuccessModel("Success in executing ${actionName} of ${params.controller}", null, OK.value(), model) 

        withFormat {
            html {
                if(params.loadMore?.toBoolean()){
                    render(template:"/document/documentListTemplate", model:model.model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    render (view:"browser", model:model.model)
                    return;
                } else{
                     /*def result = [obvFilterMsgHtml:obvFilterMsgHtml, obvListHtml:obvListHtml]
                    render result as JSON
                    */return;
                }
            }
            json { render model as JSON }
            xml { render model as XML }
        }
	}

	protected def getDocumentList(params) {
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredDocument = documentService.getFilteredDocuments(params, max, offset)
		
		def documentInstanceList = filteredDocument.documentInstanceList
		def queryParams = filteredDocument.queryParams
		def activeFilters = filteredDocument.activeFilters
		def canPullResource = filteredDocument.canPullResource
		
		def count = documentService.getFilteredDocuments(params, -1, -1).documentInstanceList.size()
		if(params.append?.toBoolean()) {
            session["doc_ids_list"].addAll(documentInstanceList.collect {it.id});
        } else {
            session["doc_ids_list_params"] = params.clone();
            session["doc_ids_list"] = documentInstanceList.collect {it.id};
        }

		log.debug "Storing all doc ids list in session ${session['doc_ids_list']} for params ${params}";

		return [documentInstanceList: documentInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'document', canPullResource:canPullResource]

	}

	def tags = {
		//render Tag.findAllByNameIlike("${params.term}%", [max:10])*.name as JSON
		render documentService.getFilteredTagsByUserGroup(params.webaddress, 'document') as JSON
	}

	
	def tagcloud = { render (view:"tagcloud") }

	private void swapDisplayOrder(docSciIns1, docSciIns2){
	 	def temp = docSciIns1.displayOrder
       	docSciIns1.displayOrder = docSciIns2.displayOrder
       	docSciIns2.displayOrder = temp;
        return
    }

	def fetchDocSciName(params){
		def docSciNameInstance = DocSciName.get(params.instanceId.toLong())
        def disOrder = docSciNameInstance.displayOrder;
        def docIns = Document.read(params.parentInsId.toLong())
        def resNL = DocSciName.withCriteria{
        	eq('document',docIns)
                if(params.typeOfChange == "up"){
                    gt('displayOrder', disOrder)
                }
                else{
                    lt('displayOrder', disOrder)
                }
            
            maxResults(1)
            if(params.typeOfChange == "up"){
                order("displayOrder", "asc")
            }
            else{
                order("displayOrder", "desc")
            }
        }
        if(resNL.size() != 0){
            return resNL.get(0)
        }
        else{
            return null
        }
    }

	def changeDisplayOrder = {
		def docSciNameInstance = DocSciName.get(params.instanceId.toLong())
        def disOrder = docSciNameInstance.displayOrder;
        //println "========DIS ORDER===== " + disOrder;
        def otherDocSciName = fetchDocSciName(params);
        //println "========otherDocSciName ===== " + otherDocSciName;
        def msg
        def success
        if(otherDocSciName){
            swapDisplayOrder(docSciNameInstance, otherDocSciName);
            if(!docSciNameInstance.save(flush:true)){
                docSciNameInstance.errors.each { log.error it }        
            }
            if(!otherDocSciName.save(flush:true)){
                otherDocSciName.errors.each { log.error it }        
            }
            success = true
            msg = "order changed"
        }
        else{
            msg = "Its already at the extreme"
            success = false
        }
                def result = [success:success, msg:msg]
        render result as JSON
    }

	

	//// SEARCH //////
	/**
	 * 	
	 */
//	def search = {
//		log.debug params;
//		def model = documentService.search(params)
//		model['isSearch'] = true;
//
//		if(params.loadMore?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render(template:"/document/documentListTemplate", model:model);
//			return;
//
//		} else if(!params.isGalleryUpdate?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render (view:"browser", model:model)
//			return;
//		} else {
//			params.remove('isGalleryUpdate');
//			def obvListHtml =  g.render(template:"/document/documentListTemplate", model:model);
//			model.resultType = "document"
//			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
//
//			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
//
//			render (result as JSON)
//			return;
//		}
//	}
	
	
	def terms = {
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		List result = documentService.nameTerms(params)
		render result.value as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def bulkUpload(){
		log.debug params
		documentService.processBatch(params)
		render " done "
	}

	def runAllDocuments() {
			
			documentService.runAllDocuments();
		}
	

}
