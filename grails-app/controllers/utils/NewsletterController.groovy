package utils

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

    @Secured(['ROLE_ADMIN'])
    def create = {
        def newsletterInstance = new Newsletter()
        newsletterInstance.properties = params
        return [newsletterInstance: newsletterInstance]
    }
    
    @Secured(['ROLE_ADMIN'])
    def save = {
    
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        println params
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        def newsletterInstance = new Newsletter(params)
        if (newsletterInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
            redirect(action: "show", id: newsletterInstance.id)
        }
        else {
            render(view: "create", model: [newsletterInstance: newsletterInstance])
        }
    }

    def show = {
        def newsletterInstance = Newsletter.get(params.id)
        if (!newsletterInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
            redirect(action: "list")
        }
        else {
            [newsletterInstance: newsletterInstance]
        }
    }

    @Secured(['ROLE_ADMIN'])
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

    @Secured(['ROLE_ADMIN'])
    def update = {
        def newsletterInstance = Newsletter.get(params.id)
        if (newsletterInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (newsletterInstance.version > version) {
                    
                    newsletterInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'newsletter.label', default: 'Newsletter')] as Object[], "Another user has updated this Newsletter while you were editing")
                    render(view: "edit", model: [newsletterInstance: newsletterInstance])
                    return
                }
            }
            newsletterInstance.properties = params
            if (!newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
                redirect(action: "show", id: newsletterInstance.id)
            }
            else {
                render(view: "edit", model: [newsletterInstance: newsletterInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_ADMIN'])
    def delete = {
        def newsletterInstance = Newsletter.get(params.id)
        if (newsletterInstance) {
            try {
                newsletterInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
            redirect(action: "list")
        }
    }
}
