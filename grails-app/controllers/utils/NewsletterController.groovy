package utils

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.springframework.security.acls.domain.BasePermission;

import species.groups.UserGroup;
import species.utils.Utils;
import grails.converters.JSON;
import grails.plugin.springsecurity.annotation.Secured;
import species.Language;

class NewsletterController {

	static allowedMethods = [save: "POST", update: "POST"]
	def userGroupService
	def newsletterSearchService;
	def newsletterService
	def aclUtilService
	def springSecurityService
    def observationService
    def utilsService;
	public static final boolean COMMIT = true;

	def index = {
		redirect url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages", params: params)
	}

	def list() {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		redirect url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages", params: params)
	}

	@Secured(['ROLE_USER'])
	def create() {
		log.debug params
		boolean permitted = false;
		if(params.webaddress||params.userGroup) { 
			def userGroupInstance = (params.userGroup)?params.userGroup:UserGroup.findByWebaddress(params.webaddress);
			params.userGroup = userGroupInstance;
			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION) || utilsService.isAdmin(springSecurityService.currentUser)) {
				permitted = true
			} else {
				flash.message = "${message(code: 'default.not.permitted.message', args: ['add new', message(code: 'page.label', default: 'page'), ''])}"
				redirect  url: uGroup.createLink(mapping:'userGroup', action: "pages", userGroup:userGroupInstance)
				return;			
			}
		} else {
			if(utilsService.isAdmin(springSecurityService.currentUser)) {
				permitted = true;
			} else {
				redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
				return;
			}
		}
		
		if(permitted) {
            def newsletterInstance = new Newsletter()
			newsletterInstance.properties = params
            return [newsletterInstance: newsletterInstance,userLanguage:utilsService.getCurrentLanguage(request)]
		}
		
	}

	@Secured(['ROLE_USER'])
	def save() {
		log.debug params
		def newsletterInstance;
		params.language = params.language?Language.get(params.language):Language.findByThreeLetterCode('eng');
		if(params.userGroup) {
			def userGroupInstance = UserGroup.findByWebaddress(params.userGroup);
			params.userGroup = userGroupInstance;
			if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION) || utilsService.isAdmin(springSecurityService.currentUser)) {
				newsletterInstance = new Newsletter(params)
				userGroupInstance.addToNewsletters(newsletterInstance);
	
				newsletterInstance.title = newsletterInstance.title.capitalize()
				if (newsletterInstance.save() && userGroupInstance.save(flush: true)) {
					postProcessNewsletter(newsletterInstance);
					flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.title])}"
					//redirect url: uGroup.createLink(mapping:'userGroupPageShow', params:['webaddress':newsletterInstance.userGroup.webaddress, 'newsletterId':newsletterInstance.id])
					redirect url:uGroup.createLink(controller:"userGroup", action: "pages", 'userGroup':userGroupInstance, params:['newsletterId':newsletterInstance.id])
				}
				else {
					render(view: "create", model: ['userGroup':userGroupInstance, newsletterInstance: newsletterInstance])
				}
			} else {
				flash.message = "${message(code: 'default.not.permitted.message', args: ['add new', message(code: 'page.label', default: 'page'), ''])}"
				redirect  url: uGroup.createLink(mapping:'userGroup', action: "pages", userGroup:userGroupInstance)
			}
		} else {
			if(utilsService.isAdmin(springSecurityService.currentUser)) {
				newsletterInstance = new Newsletter(params)
				if (newsletterInstance.save(flush: true)) {
					postProcessNewsletter(newsletterInstance);
					flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
					redirect(controller:"userGroup", action: "page", params:['newsletterId':newsletterInstance.id])
				}
	
				else {
					render(view: "create", model: [newsletterInstance: newsletterInstance])
				}
			} else {
				flash.message = "${message(code: 'default.not.permitted.message', args: ['add new', message(code: 'page.label', default: 'page'), ''])}"
				redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
			}
		}
        def ug = observationService.getUserGroup(params)
        def resNL = Newsletter.withCriteria{
            if(!ug){
                isNull('userGroup')
            }
            else{
                and{
                    eq('userGroup', ug) 
                }
            }
            maxResults(1)
            order("displayOrder", "desc")
        }
        def disOrder = 0
        if(resNL.size() != 0){
            disOrder = resNL.get(0).displayOrder + 1
        }
        newsletterInstance.displayOrder = disOrder
        if(!newsletterInstance.save(flush:true)){
            newsletterInstance.errors.allErrors.each { log.error it }      
        }
	}

	def show() {
		log.debug params
        def userLanguage = utilsService.getCurrentLanguage(request);
		def newsletterInstance = Newsletter.get(params.id)
		if (!newsletterInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
		else {
			if(newsletterInstance.userGroup) {
				['userGroupInstance':newsletterInstance.userGroup, 'newsletterInstance': newsletterInstance, 'userLanguage':userLanguage]
			}
			else {
				[newsletterInstance: newsletterInstance, userLanguage:userLanguage]
			}
		}
	}

	@Secured(['ROLE_USER'])
	def edit() {
		def newsletterInstance = Newsletter.get(params.id)
		if (!newsletterInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
		else {
			if(newsletterInstance.userGroup) {
				if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), newsletterInstance.userGroup, BasePermission.ADMINISTRATION) || utilsService.isAdmin(springSecurityService.currentUser)) {
					[userGroupInstance:newsletterInstance.userGroup, newsletterInstance: newsletterInstance,userLanguage:utilsService.getCurrentLanguage(request)]
				} else {
					flash.message = "${message(code: 'edit.denied.message')}"
					redirect url:uGroup.createLink(controller:"userGroup", action: "pages", 'userGroup':newsletterInstance.userGroup, params:['newsletterId':newsletterInstance.id])
				}
			}
			else if(utilsService.isAdmin(springSecurityService.currentUser)) {
				[newsletterInstance: newsletterInstance,userLanguage:utilsService.getCurrentLanguage(request)]
			} else {
				flash.message = "${message(code: 'edit.denied.message')}"
				redirect(controller:"userGroup", action: "page", params:['newsletterId':newsletterInstance.id])
			}
		} 
	}

	@Secured(['ROLE_USER'])
	def update() {
		def newsletterInstance = Newsletter.get(params.id)
		params.language = params.language?Language.get(params.language):Language.findByThreeLetterCode('eng');

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
			//XXX removing user group info from cloned params as its coming as string
			Map validMap = new HashMap(params)
			validMap.remove("userGroup")
			validMap.language = params.language;
			newsletterInstance.properties = validMap
			if(params.userGroup) {
				def userGroupInstance = UserGroup.findByWebaddress(params.userGroup);
				if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), userGroupInstance, BasePermission.ADMINISTRATION) || utilsService.isAdmin(springSecurityService.currentUser)) {
					userGroupInstance.addToNewsletters(newsletterInstance);
					if (userGroupInstance.save(flush: true) && !newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
						postProcessNewsletter(newsletterInstance);
						flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
						redirect url:uGroup.createLink(controller:"userGroup", action: "page", 'userGroup':userGroupInstance, params:['newsletterId':newsletterInstance.id])
					} else {

                        userGroupInstance.errors.allErrors.each { log.error it } 
                        newsletterInstance.errors.allErrors.each { log.error it } 
						flash.message = "${message(code: 'update.denied.message')}"
						redirect url:uGroup.createLink(controller:"userGroup", action: "page", 'userGroup':userGroupInstance, params:['newsletterId':newsletterInstance.id])
					}
				} else {
					render(view: "edit", model: [userGroupInstance:userGroupInstance, newsletterInstance: newsletterInstance])
				}
			} else {
                if(utilsService.isAdmin(springSecurityService.currentUser)) {
                    if (!newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
						postProcessNewsletter(newsletterInstance);
						flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
						redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "page", id: newsletterInstance.id)
					}
					else {
						render(view: "edit", model: [newsletterInstance: newsletterInstance])
					}
				} else {
					flash.message = "${message(code: 'update.denied.message')}"
					redirect(controller:"userGroup", action: "page", params:['newsletterId':newsletterInstance.id])
				}
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
	}

	@Secured(['ROLE_USER'])
	def delete() {
		def newsletterInstance = Newsletter.get(params.id)
		if (newsletterInstance) {
			boolean permitted = false;
			if(newsletterInstance.userGroup) {
				if(aclUtilService.hasPermission(springSecurityService.getAuthentication(), newsletterInstance.userGroup, BasePermission.ADMINISTRATION) || utilsService.isAdmin(springSecurityService.currentUser)) {
					permitted = true;
				} else {
					flash.message = "${message(code: 'default.not.permitted.message', args: ['delete', message(code: 'page.label', default: 'page'), ''])}"
					redirect url:uGroup.createLink(controller:"group", action: "page", 'userGroup':newsletterInstance.userGroup, params:['newsletterId':newsletterInstance.id])
				}
			}
			else {
				if(utilsService.isAdmin(springSecurityService.currentUser)) {
					permitted = true;	
				} else {
					flash.message = "${message(code: 'default.not.permitted.message', args: ['delete', message(code: 'page.label', default: 'page'), ''])}"
					redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "page", id: newsletterInstance.id)
				}			
			}
			if(permitted) {
				try {
					newsletterInstance.delete(flush: true)
					newsletterSearchService.delete(newsletterInstance.id);
					flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
					if(newsletterInstance.userGroup) {
						redirect  url: uGroup.createLink(mapping:'userGroup', action: "pages", userGroupWebaddress:newsletterInstance.userGroup.webaddress)
						return;
					}
					redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
					if(params.userGroup) {
						redirect url: uGroup.createLink(mapping:'userGroupPageShow', userGroupWebaddress: params.userGroup, params:['newsletterId':params.id])
						return;
					}
					redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "page", id: params.id)
				}
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			if(params.userGroup) {
				redirect  url: uGroup.createLink(mapping:'userGroup', action: "pages", userGroupWebaddress:newsletterInstance.userGroup.webaddress)
				return;
			}
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
	}


	def listPage(){
    	
		def userGroupInstance = findInstance(null, params.webaddress, false)
		//if (!userGroupInstance) return;
		println userGroupInstance
		def currentLanguage = utilsService.getCurrentLanguage(request);
		def newsletters = userGroupService.getNewsLetters(userGroupInstance, params.max, params.offset, params.sort, params.order,currentLanguage);		 

		def model = ['userGroupInstance':userGroupInstance, 'newsletters':newsletters]

		render model as JSON
    }



	private UserGroup findInstance(id=null, String webaddress='', boolean redirectToList=true) {
		def userGroup
		
		if(id) {
			if(id instanceof String) {
                try {
				    id = Long.parseLong(id);
                } catch(e) {
                    id = null;
                    e.printStackTrace();
                }
			}
            if(id)
			    userGroup = UserGroup.get(id)
		} 
		if(webaddress) {
			userGroup = UserGroup.findByWebaddress(webaddress)
		}
		
		if (!userGroup && redirectToList) {
            def model = utilsService.getErrorModel( "${message(code: 'default.not.found.message', args: ['UserGroup', id?:webaddress])}", userGroup, NOT_FOUND.value())
                withFormat {
                    html {
                        flash.message = model.msg
                        redirect url: uGroup.createLink(controller:'userGroup', action:'list')
                        return;
                    }
                    json { render model as JSON }
                    
                }
                return;
		}
		return userGroup
	}

	def postProcessNewsletter(newsletterInstance) {
		try {
			newsletterSearchService.publishSearchIndex(newsletterInstance, COMMIT);
		} catch(e) {
			e.printStackTrace()
		}
	}

	/**
	 *
	 */
	def search = {
		log.debug params;

		def model = newsletterService.search(params)

		if(SpringSecurityUtils.isAjax(request)) {
			render(template:'/newsletter/searchResultsTemplate', model:model);
			return;
		} else {
			return model;
		}
	}

	def terms = {
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";

		List result = newsletterService.nameTerms(params)

		render result.value as JSON;
	}

    private void swapDisplayOrder(nl1, nl2){
        def temp = nl1.displayOrder
        nl1.displayOrder = nl2.displayOrder
        nl2.displayOrder = temp
        return
    }

    def fetchNewsLetter(params){
        def nlIns = Newsletter.get(params.instanceId.toLong())
        def disOrder = nlIns.displayOrder;
        def newDisOrder = (params.typeOfChange == "up")?(disOrder+1):(disOrder-1)
        def ug = observationService.getUserGroup(params)
        def resNL = Newsletter.withCriteria{
            if(!params.webaddress){
                isNull('userGroup')
            }
            else{
                and{
                    eq('userGroup', ug) 
                }
            }
            and{
                if(params.typeOfChange == "up"){
                    gt('displayOrder', disOrder)
                }
                else{
                    lt('displayOrder', disOrder)
                }
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
        def nlIns = Newsletter.get(params.instanceId.toLong())
        def disOrder = nlIns.displayOrder;
        def otherNewsLetter = fetchNewsLetter(params); //how to fetch its group specific
        def msg
        def success
        if(otherNewsLetter){
            swapDisplayOrder(nlIns, otherNewsLetter);
            if(!nlIns.save(flush:true)){
                nlIns.errors.each { log.error it }        
            }
            if(!otherNewsLetter.save(flush:true)){
                otherNewsLetter.errors.each { log.error it } 
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

    

}
