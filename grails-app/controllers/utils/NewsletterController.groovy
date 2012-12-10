package utils

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;

import species.groups.UserGroup;
import species.utils.Utils;
import grails.plugins.springsecurity.Secured;

class NewsletterController {

	static allowedMethods = [save: "POST", update: "POST"]
	def userGroupService
	def newsletterSearchService;
	public static final boolean COMMIT = true;
	
	def index = {
		redirect url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		redirect url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages", params: params)
	}

	@Secured(['ROLE_USER'])
	def create = {
		log.debug params
		def newsletterInstance = new Newsletter()
		newsletterInstance.properties = params
		return [newsletterInstance: newsletterInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params
		def newsletterInstance;
		if(params.userGroup) {
			def userGroupInstance = UserGroup.findByWebaddress(params.userGroup);
			params.userGroup = userGroupInstance;
			newsletterInstance = new Newsletter(params)
			userGroupInstance.addToNewsletters(newsletterInstance);
			
			if (newsletterInstance.save() && userGroupInstance.save(flush: true)) {
				postProcessNewsletter(newsletterInstance);
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.title])}"
				//redirect url: uGroup.createLink(mapping:'userGroupPageShow', params:['webaddress':newsletterInstance.userGroup.webaddress, 'newsletterId':newsletterInstance.id])
				redirect url:uGroup.createLink(controller:"userGroup", action: "page", 'userGroup':userGroupInstance, params:['newsletterId':newsletterInstance.id])
			}
			else {
				render(view: "create", model: ['userGroup':userGroupInstance, newsletterInstance: newsletterInstance])
			}
		} else {
			newsletterInstance = new Newsletter(params)
			if (newsletterInstance.save(flush: true)) {
				postProcessNewsletter(newsletterInstance);
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
				redirect(controller:"userGroup", action: "page", params:['newsletterId':newsletterInstance.id])
			}

			else {
				render(view: "create", model: [newsletterInstance: newsletterInstance])
			}
		}
	}

	def show = {
		log.debug params
		def newsletterInstance = Newsletter.get(params.id)
		if (!newsletterInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
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
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
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
			//XXX removing user group info from cloned params as its coming as string
			Map validMap = new HashMap(params)
			validMap.remove("userGroup")
			newsletterInstance.properties = validMap
			if(params.userGroup) {
				def userGroupInstance = UserGroup.findByWebaddress(params.userGroup);
				userGroupInstance.addToNewsletters(newsletterInstance);
				if (userGroupInstance.save(flush: true) && !newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
					postProcessNewsletter(newsletterInstance);
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"
					redirect url: uGroup.createLink(mapping:'userGroupPageShow', userGroupWebaddress:newsletterInstance.userGroup.webaddress, params:['newsletterId':newsletterInstance.id, userGroupInstance:userGroupInstance])
				}else {
					render(view: "edit", model: [userGroupInstance:userGroupInstance, newsletterInstance: newsletterInstance])
				}
			} else {
				if (!newsletterInstance.hasErrors() && newsletterInstance.save(flush: true)) {
					postProcessNewsletter(newsletterInstance);
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), newsletterInstance.id])}"

					redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "page", id: newsletterInstance.id)
				}
				else {
					render(view: "edit", model: [newsletterInstance: newsletterInstance])
				}
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
	}

	@Secured(['ROLE_USER'])
	def delete = {
		def newsletterInstance = Newsletter.get(params.id)
		if (newsletterInstance) {
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
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'newsletter.label', default: 'Newsletter'), params.id])}"
			if(params.userGroup) {
				redirect url: uGroup.createLink(mapping:'userGroupPageShow', userGroupWebaddress:params.userGroup,  params:['newsletterId':params.id])
				return;
			}
			redirect  url: uGroup.createLink(mapping:'userGroupGeneric', action: "pages")
		}
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
	   def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

	   if(params.query) {
		   NamedList paramsList = new NamedList();
		   paramsList.add('q', Utils.cleanSearchQuery(params.query));
		   params.remove('query');
		   paramsList.add('start', params['start']?:"0");
		   paramsList.add('rows', params['rows']?:"10");
		   paramsList.add('sort', params['sort']?params['sort']+" desc":"score desc");
		   paramsList.add('fl', params['fl']?:"id, name");
//		   paramsList.add('facet', "true");
//		   paramsList.add('facet.limit', "-1");
//		   paramsList.add('facet.mincount', "1");
	
		   
		   log.debug "Along with faceting params : "+paramsList;
		   try {
			   def queryResponse = newsletterSearchService.search(paramsList);
			   List<Newsletter> instanceList = new ArrayList<Newsletter>();
			   Iterator iter = queryResponse.getResults().listIterator();
			   while(iter.hasNext()) {
				   def doc = iter.next();
				   def instance = Newsletter.get(doc.getFieldValue("id"));
				   if(instance)
					   instanceList.add(instance);
			   }
			   
			   [responseHeader:queryResponse.responseHeader, total:queryResponse.getResults().getNumFound(), instanceList:instanceList, snippets:queryResponse.getHighlighting()];
		   } catch(SolrException e) {
			   e.printStackTrace();
			   [params:params, instanceList:[]];
		   }
	   } else {
		   [params:params, instanceList:[]];
	   }
   }

}
