package content.eml

import grails.plugin.springsecurity.annotation.Secured

import grails.converters.JSON
import org.grails.taggable.*
import species.AbstractObjectController;
import speciespage.search.DocumentSearchService;

class DocumentController extends AbstractObjectController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def documentService
	def springSecurityService
	def userGroupService
	def SUserService
	def activityFeedService
	def observationService
	def documentSearchService
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

		log.debug "params in document save "+ params

		params.author = springSecurityService.currentUser;
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
			observationService.sendNotificationMail(activityFeedService.DOCUMENT_CREATED, documentInstance, request, params.webaddress);
			documentSearchService.publishSearchIndex(documentInstance, true)
			redirect(action: "show", id: documentInstance.id)
		}
		else {
			documentInstance.errors.allErrors.each { log.error it }
			render(view: "create", model: [documentInstance: documentInstance])
		}
	}

	def show() {
		def documentInstance = Document.get(params.id)
		if (!documentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "browser")
		}
		else {
			[documentInstance: documentInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def edit() {
		def documentInstance = Document.get(params.id)
		if (!documentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "browser")
		} else if(SUserService.ifOwns(documentInstance.author)) {
			render(view: "create", model: [documentInstance: documentInstance])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"document", id:documentInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	@Secured(['ROLE_USER'])	
	def update() {
		log.debug "Params in Document update >> "+ params
		def documentInstance = Document.get(params.id)
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
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
				redirect(action: "show", id: documentInstance.id)
			}
			else {
				render(view: "create", model: [documentInstance: documentInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "browser")
		}
	}

	@Secured(['ROLE_USER'])	
	def delete() {
		def documentInstance = Document.get(params.id)
		if (documentInstance) {
			try {
				userGroupService.removeDocumentFromUserGroups(documentInstance, documentInstance.userGroups.collect{it.id})
				documentInstance.delete(flush: true, failOnError:true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
				redirect(action: "browser")
			}
			catch (Exception e) {
				log.debug e.printStackTrace()
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "browser")
		}
	}

	
	def browser = {
		log.debug params
		
		def model = getDocumentList(params)
		if(params.loadMore?.toBoolean()){
			render(template:"/document/documentListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"browser", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/document/documentListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvFilterMsgHtml:obvFilterMsgHtml, obvListHtml:obvListHtml]
			render result as JSON
			return;
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
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%", [max:10])*.name as JSON
	}

	
	def tagcloud = { render (view:"tagcloud") }

	//// SEARCH //////
	/**
	 * 	
//	 */
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
	
	@Secured(['ROLE_USER'])
	def bulkUpload(){
		log.debug params
		documentService.processBatch(params)
		render " done "
	}

}
