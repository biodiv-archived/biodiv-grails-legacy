package species.participation

import grails.plugin.springsecurity.annotation.Secured

import grails.converters.JSON
import grails.converters.XML
import org.grails.taggable.*
import species.AbstractObjectController;
import static org.springframework.http.HttpStatus.*;

class DiscussionController extends AbstractObjectController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def discussionService
	def documentService
	def springSecurityService
	def userGroupService
	def activityFeedService
	//def utilsService
	def discussionSearchService
	//def observationService
    def messageSource

	def index = {
		redirect(action: "list", params: params)
	}

	@Secured(['ROLE_USER'])
	def create() {
		def discussionInstance = new Discussion()
		discussionInstance.properties = params
		return [discussionInstance: discussionInstance]
	}

	@Secured(['ROLE_USER'])
	def save() {

		log.debug "params in discussion save "+ params

		params.author = springSecurityService.currentUser;
		params.locale_language = utilsService.getCurrentLanguage(request);
		Discussion discussionInstance = discussionService.createDiscussion(params)

		log.debug( "discussion instance with params assigned >>>>>>>>>>>>>>>>: "+ discussionInstance)
		if (discussionInstance.save(flush: true)) {

			flash.message = "${message(code: 'default.created.message', args: [message(code: 'discussion.label', default: 'discussion'), discussionInstance.id])}"
			def af = activityFeedService.addActivityFeed(discussionInstance, null, discussionInstance.author, activityFeedService.DISCUSSION_CREATED);

			def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
			
			discussionInstance.setTags(tags)
			if(params.groupsWithSharingNotAllowed) {
				discussionService.setUserGroups(discussionInstance, [
					params.groupsWithSharingNotAllowed
				]);
			} else {
				if(params.userGroupsList) {
					def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();

					discussionService.setUserGroups(discussionInstance, userGroups);
				}
			}
			utilsService.sendNotificationMail(activityFeedService.DISCUSSION_CREATED, discussionInstance, request, params.webaddress, af);
			//discussionSearchService.publishSearchIndex(discussionInstance, true)
			
			def model = utilsService.getSuccessModel(flash.message, discussionInstance, OK.value());
			withFormat {
				html {
					redirect(action: "show", id: discussionInstance.id)
				}
				json { render model as JSON }
				xml { render model as XML }
			}
		}
		else {   
				def errors = [];
                discussionInstance.errors.allErrors .each {
                    def formattedMessage = messageSource.getMessage(it, null);
                    errors << [field: it.field, message: formattedMessage]
                }
				
				def model = utilsService.getErrorModel("Failed to save discussion", discussionInstance, OK.value(), errors);
				withFormat {
					html {
						discussionInstance.errors.allErrors.each { log.error it }
						render(view: "create", model: [discussionInstance:discussionInstance])
					}
					json { render model as JSON }
					xml { render model as XML }
				}
		}
	}

	def show() {
		//cache "content"
        params.id = params.long('id');

		def discussionInstance = params.id ? Discussion.get(params.id):null;
		if (!params.id || !discussionInstance) {
				def model = utilsService.getErrorModel("${message(code: 'default.not.found.message', args: [message(code: 'discussion.label', default: 'Discussion'), params.id])}", null, OK.value());
	
				withFormat {
					html {
						flash.message = model.msg;
						redirect(action: "list")
					}
					json { render model as JSON }
					xml { render model as XML }
				}
		}
		else {
			discussionInstance.incrementPageVisit()
			def model = utilsService.getSuccessModel("", discussionInstance, OK.value())
			withFormat {
				html {
					def userLanguage = utilsService.getCurrentLanguage(request);
					model = [discussionInstance:discussionInstance, userLanguage: userLanguage]
				}
				json { render model as JSON }
				xml { render model as XML }
			}
			return model;
		}
             
	}

	@Secured(['ROLE_USER'])
	def edit() {
		def discussionInstance = Discussion.get(params.id)
		if (!discussionInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'discussion.label', default: 'discussion'), params.id])}"
			redirect(action: "list")
		} else if(utilsService.ifOwns(discussionInstance.author)) {
			render(view: "create", model: [discussionInstance: discussionInstance])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"discussion", id:discussionInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}

    @Secured(['ROLE_USER'])	
    def delete() {

        if(!params.id) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'discussion.label', default: 'discussion'), params.id])}"
            if(params.format?.equalsIgnoreCase("json")) {
                render ([success:false, msg:flash.message] as JSON);
                return;
            }


            redirect(action: "list")

        } else {
            try {
                def discussionInstance = Discussion.get(params.long('id'))
                if (discussionInstance) {
                    userGroupService.removeDiscussionFromUserGroups(discussionInstance, discussionInstance.userGroups.collect{it.id})
                    discussionInstance.delete(flush: true, failOnError:true)

                    flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'discussion.label', default: 'discussion'), params.id])}"
                    if(params.format?.equalsIgnoreCase("json")) {
                        render ([success:true, msg:flash.message] as JSON);
                        return;
                    }

                    redirect(action: "list")
                }
                else {
                    flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'discussion.label', default: 'discussion'), params.id])}"
                    if(params.format?.equalsIgnoreCase("json")) {
                        render ([success:false, msg:flash.message] as JSON);
                        return;
                    }


                    redirect(action: "list")
                }
            }
            catch (Exception e) {
                log.debug e.printStackTrace()
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'discussion.label', default: 'discussion'), params.id])}"
                if(params.format?.equalsIgnoreCase("json")) {
                    render ([success:false, msg:flash.message, errors:e.getMessage()] as JSON);
                    return;
                }


                redirect(action: "show", id: params.id)
            }

        }
    }

 	def list() {
		 def model = getDiscussionList(params)
		 model.userLanguage = utilsService.getCurrentLanguage(request);
		 if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
			 model['resultType'] = 'discussion'
			 model['obvListHtml'] =  g.render(template:"/discussion/discussionListTemplate", model:model);
			 model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
		 }
 
		 model = utilsService.getSuccessModel("Success in executing ${actionName} of ${params.controller}", null, OK.value(), model)
 		 withFormat {
			 html {
				 if(params.loadMore?.toBoolean()){
					 render(template:"/discussion/discussionListTemplate", model:model.model);
					 return;
				 } else if(!params.isGalleryUpdate?.toBoolean()){
					 render (view:"list", model:model.model)
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

	protected def getDiscussionList(params) {
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filtereddiscussion = discussionService.getFilteredDiscussions(params, max, offset)
		
		def discussionInstanceList = filtereddiscussion.discussionInstanceList
		def queryParams = filtereddiscussion.queryParams
		def activeFilters = filtereddiscussion.activeFilters
		def canPullResource = filtereddiscussion.canPullResource
		
		def count = discussionService.getFilteredDiscussions(params, -1, -1).discussionInstanceList.size()
		if(params.append?.toBoolean()) {
            session["doc_ids_list"].addAll(discussionInstanceList.collect {it.id});
        } else {
            session["doc_ids_list_params"] = params.clone();
            session["doc_ids_list"] = discussionInstanceList.collect {it.id};
        }

		log.debug "Storing all doc ids list in session ${session['doc_ids_list']} for params ${params}";

		return [discussionInstanceList: discussionInstanceList, instanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, resultType:'discussion', canPullResource:canPullResource]

	}
	
	
	
	@Secured(['ROLE_USER'])
	def update() {
		log.debug params
		def msg = "";
		def discussionInstance = Discussion.get(params.id)
		if (discussionInstance) {
			params.locale_language = utilsService.getCurrentLanguage(request);
			discussionService.updateDiscussion(discussionInstance, params)
			if (!discussionInstance.hasErrors() && discussionInstance.save(flush: true)) {
				def af = activityFeedService.addActivityFeed(discussionInstance, null, springSecurityService.currentUser, activityFeedService.DISCUSSION_UPDATED);
				def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
				discussionInstance.setTags(tags)
				if(params.groupsWithSharingNotAllowed) {
					discussionService.setUserGroups(discussionInstance, [
						params.groupsWithSharingNotAllowed
					]);
				} else {
					if(params.userGroupsList) {
						def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
	
						discussionService.setUserGroups(discussionInstance, userGroups);
					}
				}
				utilsService.sendNotificationMail(activityFeedService.DISCUSSION_UPDATED, discussionInstance, request, params.webaddress, af);
	
				msg = "${message(code: 'default.updated.message', args: [message(code: 'discussion.label', default: 'Discussion'), discussionInstance.id])}"
				def model = utilsService.getSuccessModel(msg, discussionInstance, OK.value());
				withFormat {
					html {
						flash.message = msg;
						redirect(action: "show", id: discussionInstance.id)
					}
					json { render model as JSON }
					xml { render model as XML }
				}
			}
			else {
					def errors = [];

					discussionInstance.errors.allErrors .each {
						def formattedMessage = messageSource.getMessage(it, null);
						errors << [field: it.field, message: formattedMessage]
					}
					
					def model = utilsService.getErrorModel("Failed to update discussion",  discussionInstance, OK.value(), errors);
					withFormat {
						html {
							render(view: "create", model: [discussionInstance: discussionInstance])
						}
						json { render model as JSON }
						xml { render model as XML }
					}
					return;
			}
		}
		else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'discussion.label', default: 'Discussion'), params.id])}"
			def model = utilsService.getErrorModel(msg, null, OK.value(), errors);
			withFormat {
				html {
					flash.message = msg;
					redirect(action: "list")
				}
				json { render model as JSON }
				xml { render model as XML }
			}
		}
	}

	

	def tags = {
		//render Tag.findAllByNameIlike("${params.term}%", [max:10])*.name as JSON
		render documentService.getFilteredTagsByUserGroup(params.webaddress, 'discussion') as JSON
	}

	
	def tagcloud = { render (view:"tagcloud") }

	//// SEARCH //////
	/**
	 * 	
	 */
//	def search = {
//		log.debug params;
//		def model = discussionService.search(params)
//		model['isSearch'] = true;
//
//		if(params.loadMore?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render(template:"/discussion/discussionListTemplate", model:model);
//			return;
//
//		} else if(!params.isGalleryUpdate?.toBoolean()){
//			params.remove('isGalleryUpdate');
//			render (view:"browser", model:model)
//			return;
//		} else {
//			params.remove('isGalleryUpdate');
//			def obvListHtml =  g.render(template:"/discussion/discussionListTemplate", model:model);
//			model.resultType = "discussion"
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
		List result = discussionService.nameTerms(params)
		render result.value as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def bulkUpload(){
		log.debug params
		discussionService.processBatch(params)
		render " done "
	}
	
	@Secured(['ROLE_ADMIN'])
	def migrate(){
		discussionService.migrate()
		render "=============== done"
	}

	@Secured(['ROLE_USER'])
    def updateOraddTags(){
        log.debug params
        def discussionInstance =  Discussion.read(params.instanceId);        
        def result = documentService.updateTags(params,discussionInstance)
        def model = utilsService.getSuccessModel('success', discussionInstance, OK.value(),result);
        render model as JSON
        return;

    }


}
