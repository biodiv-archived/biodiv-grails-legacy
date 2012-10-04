package utils

import species.groups.UserGroup;
import grails.plugins.springsecurity.Secured;

class NewsletterController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[newsletterInstanceList: Newsletter.list(params), newsletterInstanceTotal: Newsletter.count()]
	}

	@Secured(['ROLE_USER'])
	def create = {
		def newsletterInstance = new Newsletter()
		newsletterInstance.properties = params
		return [newsletterInstance: newsletterInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params
		def newsletterInstance = new Newsletter(params)
		if(params.userGroupId) {
			def userGroupInstance = UserGroup.get(params.long('userGroupId'));
			userGroupInstance.addToNewsletters(newsletterInstance);
			if (userGroupInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
				redirect url: createLink(mapping:'userGroupPageShow', params:['id':newsletterInstance.userGroup.id, 'newsletterId':newsletterInstance.id])
			}
			else {
				render(view: "create", model: [userGroupInstance:userGroupInstance, newsletterInstance: newsletterInstance])
			}
		} else {
			if (newsletterInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
				redirect(controller:"userGroup", action: "page", params:['newsletterId':newsletterInstance.id])
			}

			else {
				render(view: "create", model: [newsletterInstance: newsletterInstance])
			}
		}
	}

	def show = {
		def newsletterInstance = Newsletter.get(params.id)
		if (!newsletterInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect(action: "list")
		}
		else {
			if(newsletterInstance.userGroup) {
				[userGroupInstance:newsletterInstance.userGroup, newsletterInstance: newsletterInstance]	
			}
			else {
				[newsletterInstance: newsletterInstance]
			}
		}
	}

	@Secured(['ROLE_USER'])
	def edit = {
		def newsletterInstance = Newsletter.get(params.id)
		if (!newsletterInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [newsletterInstance: newsletterInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def update = {
		def newsletterInstance = Newsletter.get(params.id)

		if (newsletterInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (newsletterInstance.version > version) {

					newsletterInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'newsletter.label', default: 'Newsletter')]
					as Object[], "Another user has updated this Newsletter while you were editing")
					render(view: "edit", model: [newsletterInstance: newsletterInstance])
					return
				}
			}
			newsletterInstance.properties = params
			if(params.userGroupId) {
				def userGroupInstance = UserGroup.get(params.long('userGroupId'));
				userGroupInstance.addToNewsletters(newsletterInstance);
				if (userGroupInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
					redirect url: createLink(mapping:'userGroupPageShow', params:['id':newsletterInstance.userGroup.id, 'newsletterId':newsletterInstance.id, userGroupInstance:userGroupInstance])
				}
				else {
					render(view: "edit", model: [userGroupInstance:userGroupInstance, newsletterInstance: newsletterInstance])
				}
			} else {
				if (!newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"

					redirect(action: "show", id: newsletterInstance.id)
				}
				else {
					render(view: "edit", model: [newsletterInstance: newsletterInstance])
				}
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def delete = {
		def newsletterInstance = Newsletter.get(params.id)
		if (newsletterInstance) {
			try {
				newsletterInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
				if(newsletterInstance.userGroup) {
					redirect(controller:'userGroup', action: "pages", id:newsletterInstance.userGroup.id)
					return;
				}
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
				if(params.userGroupId) {
					redirect url: createLink(mapping:'userGroupPageShow', params:['id':params.userGroupId, 'newsletterId':params.id])
					return;
				}
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			if(params.userGroupId) {
				redirect url: createLink(mapping:'userGroupPageShow', params:['id':params.userGroupId, 'newsletterId':params.id])
				return;
			}
			redirect(action: "list")
		}
	}
}
