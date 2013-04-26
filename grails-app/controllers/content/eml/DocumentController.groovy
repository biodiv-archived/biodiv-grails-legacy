package content.eml

import grails.plugins.springsecurity.Secured


class DocumentController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def documentService
	def springSecurityService

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[documentInstanceList: Document.list(params), documentInstanceTotal: Document.count()]
	}

	@Secured(['ROLE_USER'])
	def create = {
		def documentInstance = new Document()
		documentInstance.properties = params
		return [documentInstance: documentInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {

		log.debug "params in document save "+ params

		params.author = springSecurityService.currentUser;
		def documentInstance = documentService.createDocument(params)

		log.debug( "document instance with params assigned >>>>>>>>>>>>>>>>: "+ documentInstance)
		if (documentInstance.save(flush: true)) {
			//In creation parent is set to uFile after document creation
			documentInstance.uFile.setSource(documentInstance)
			if(!documentInstance.uFile.save(flush:true)) {
				log.error "Error saving ufile insatnce after setting document parent. Document:  "+ documentInstance.id+ " UFile ID: "+ documentInstance.uFile.id
			} 
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
			redirect(action: "show", id: documentInstance.id)

			if(params.groupsWithSharingNotAllowed) {
				documentService.setUserGroups(observationInstance, [
					params.groupsWithSharingNotAllowed
				]);
			} else {
				if(params.userGroupsList) {
					def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();

					documentService.setUserGroups(documentInstance, userGroups);
				}
			}
		}
		else {
			render(view: "create", model: [documentInstance: documentInstance])
		}
	}

	def show = {
		def documentInstance = Document.get(params.id)
		if (!documentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "list")
		}
		else {
			[documentInstance: documentInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def edit = {
		def documentInstance = Document.get(params.id)
		if (!documentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "list")
		}
		else {
			render(view: "create", model: [documentInstance: documentInstance])
		}
	}

	def update = {
		def documentInstance = Document.get(params.id)
		if (documentInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (documentInstance.version > version) {

					documentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'document.label', default: 'Document')] as Object[], "Another user has updated this Document while you were editing")
					render(view: "edit", model: [documentInstance: documentInstance])
					return
				}
			}
			documentInstance.properties = params
			if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
				redirect(action: "show", id: documentInstance.id)
			}
			else {
				render(view: "edit", model: [documentInstance: documentInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def documentInstance = Document.get(params.id)
		if (documentInstance) {
			try {
				documentInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(action: "list")
		}
	}



}
