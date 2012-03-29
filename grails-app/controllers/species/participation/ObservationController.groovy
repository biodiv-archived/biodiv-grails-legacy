package species.participation

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine
import groovy.util.Node

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.plugins.springsecurity.Secured
import species.participation.RecommendationVote.ConfidenceType
import species.sourcehandler.XMLConverter
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.Habitat

class ObservationController {

	private static final String OBSERVATION_ADDED = "observationAdded";
	private static final String SPECIES_RECOMMENDED = "speciesRecommended";
	private static final String SPECIES_AGREED_ON = "speciesAgreedOn";


	def grailsApplication;
	def observationService;
	def springSecurityService;
	def mailService;

	static allowedMethods = [update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def filteredList = {
		def result = getObservationList(params);
		render (template:"/common/observation/showObservationListTemplate", model:result);
	}

	def list = { getObservationList(params); }

	protected def getObservationList(params) {
		def max = Math.min(params.max ? params.int('max') : 10, 100)

		def offset = params.offset ? params.int('offset') : 0

		def filteredObservation = observationService.getFilteredObservations(params, max, offset)
		def observationInstanceList = filteredObservation.observationInstanceList
		def queryParams = filteredObservation.queryParams
		def activeFilters = filteredObservation.activeFilters

		def totalObservationInstanceList = observationService.getFilteredObservations(params, -1, -1).observationInstanceList
		def count = totalObservationInstanceList.size()

		[totalObservationInstanceList:totalObservationInstanceList, observationInstanceList: observationInstanceList, observationInstanceTotal: count, queryParams: queryParams, activeFilters:activeFilters]
	}

	@Secured(['ROLE_USER'])
	def create = {
		def observationInstance = new Observation()
		observationInstance.properties = params
		return [observationInstance: observationInstance, 'springSecurityService':springSecurityService]
	}

	@Secured(['ROLE_USER'])
	def save = {
		log.debug params;
		if(request.method == 'POST') {
			//TODO:edit also calls here...handle that wrt other domain objects

			params.author = springSecurityService.currentUser;
			def observationInstance;
			try {
				observationInstance =  observationService.createObservation(params);

				if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
					//flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					log.debug "Successfully created observation : "+observationInstance

					params.obvId = observationInstance.id

					def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();

					observationInstance.setTags(tags);

					sendNotificationMail(OBSERVATION_ADDED, observationInstance, request);

					redirect(action: 'addRecommendationVote', params:params);
				} else {
					observationInstance.errors.allErrors.each { log.error it }
					render(view: "create", model: [observationInstance: observationInstance])
				}
			} catch(e) {
				e.printStackTrace();
				flash.message = "${message(code: 'error')}";
				render(view: "create", model: [observationInstance: observationInstance])
			}
		} else {
			redirect(action: "create")
		}
	}
	@Secured(['ROLE_USER'])
	def update = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		def observationInstance = Observation.get(params.id.toLong())
		if(observationInstance)	{
			try {
				observationService.updateObservation(params, observationInstance);

				if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					log.debug "Successfully updated observation : "+observationInstance

					params.obvId = observationInstance.id
					def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
					observationInstance.setTags(tags);

					//redirect(action: "show", id: observationInstance.id)
					redirect(action: 'addRecommendationVote', params:params);
				} else {
					observationInstance.errors.allErrors.each { log.error it }
					render(view: "create", model: [observationInstance: observationInstance])
				}
			} catch(e) {
				e.printStackTrace();
				flash.message = "${message(code: 'error')}";
				render(view: "create", model: [observationInstance: observationInstance])
			}
		}else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	def show = {
		def observationInstance = Observation.findWhere(id:params.id.toLong(), isDeleted:false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
		else {
			observationInstance.incrementPageVisit();
			[observationInstance: observationInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def edit = {
		def observationInstance = Observation.findWhere(id:params.id.toLong(), isDeleted:false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
		else {
			render(view: "create", model: [observationInstance: observationInstance, 'springSecurityService':springSecurityService])
		}
	}

	@Secured(['ROLE_ADMIN'])
	def delete = {
		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			try {
				observationInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def upload_resource = {
		log.debug params;

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				def rs = [:]
				Utils.populateHttpServletRequestParams(request, rs);
				def resourcesInfo = [];
				def rootDir = grailsApplication.config.speciesPortal.observations.rootDir
				File obvDir;
				def message;

				if(!params.resources) {
					message = g.message(code: 'no.file.attached', default:'No file is attached')
				}

				params.resources.each { f ->
					log.debug "Saving observation file ${f.originalFilename}"

					// List of OK mime-types
					//TODO Move to config
					def okcontents = [
						'image/png',
						'image/jpeg',
						'image/gif',
						'image/jpg'
					]

					if (! okcontents.contains(f.contentType)) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							okcontents,
							f.originalFilename
						])
					}
					else if(f.size > grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE/1024,
							f.originalFilename,
							f.size/1024
						], default:'File size cannot exceed ${104857600/1024}KB');
					}
					else if(f.empty) {
						message = g.message(code: 'file.empty.message', default:'File cannot be empty');
					}
					else {
						if(!obvDir) {
							if(!params.obvDir) {
								obvDir = new File(rootDir);
								if(!obvDir.exists()) {
									obvDir.mkdir();
								}
								obvDir = new File(obvDir, UUID.randomUUID().toString());
								obvDir.mkdir();
							} else {
								obvDir = new File(params.obvDir);
							}
						}

						File file = new File(obvDir, Utils.cleanFileName(f.originalFilename));
						f.transferTo( file );
						ImageUtils.createScaledImages(file, obvDir);
						resourcesInfo.add([fileName:file.name, size:f.size]);
					}
				}
				log.debug resourcesInfo
				// render some XML markup to the response
				if(obvDir && resourcesInfo) {
					render(contentType:"text/xml") {
						observations {
							dir(obvDir.absolutePath.replace(rootDir, ""))
							resources {
								for(r in resourcesInfo) {
									image('fileName':r.fileName, 'size':r.size){}
								}
							}
						}
					}
				} else {
					response.setStatus(500)
					message = [error:message]
					render message as JSON
				}
			} else {
				response.setStatus(500)
				def message = [error:g.message(code: 'no.file.attached', default:'No file is attached')]
				render message as JSON
			}
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			def message = [error:g.message(code: 'error', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addRecommendationVote = {
		log.debug params;

		params.author = springSecurityService.currentUser;

		if(params.obvId) {
			//Saves recommendation if its not present
			def recommendationVoteInstance = getRecommendationVote(params);
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if(!recommendationVoteInstance){
					//saving max voted species name for observation instance needed when observation created without species name
					observationInstance.calculateMaxVotedSpeciesName();
					redirect(action: "show", id: observationInstance.id);
				}else if(!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance

					//saving max voted species name for observation instance
					observationInstance.calculateMaxVotedSpeciesName();

					//sending mail to user
					sendNotificationMail(SPECIES_RECOMMENDED, observationInstance, request);

					redirect(action: "show", id: observationInstance.id);
				}
				else {
					recommendationVoteInstance.errors.allErrors.each { log.error it }
					render (view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance])
				}
			} catch(e) {
				e.printStackTrace()
				render(view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance])
			}
		} else {
			flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
			log.error flash.message;
			redirect(action: "list")
		}
	}

	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addAgreeRecommendationVote = {
		log.debug params;

		params.author = springSecurityService.currentUser;
		boolean success = false;

		if(params.obvId) {
			//Saves recommendation if its not present
			def recommendationVoteInstance = getRecommendationVote(params);
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if(!recommendationVoteInstance){
					def result = ['votes':params.int('currentVotes')];
					render result as JSON;
				}else if(recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					success = true;

					observationInstance.calculateMaxVotedSpeciesName();

					//sending mail to user
					sendNotificationMail(SPECIES_AGREED_ON, observationInstance, request);

					def result = ['votes':++params.int('currentVotes')];
					render result as JSON;

				}
				else {
					recommendationVoteInstance.errors.allErrors.each { log.error it }

				}
			} catch(e) {
				e.printStackTrace();
			}
		} else {
			flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
		}
		if(!success) {
			def result = ['votes':params.int('currentVotes')];
			render result as JSON;
		}
	}

	/**
	 * 
	 */
	def getRecommendationVotes = {
		log.debug params;
		params.max = Math.min(params.max ? params.int('max') : 1, 10)
		params.offset = params.offset ? params.long('offset'): 0

		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			try {
				def results = observationInstance.getRecommendationVotes(params.max, params.offset);
				log.debug results;
				if(results?.recoVotes.size() > 0) {
					def html =  g.render(template:"/common/observation/showObservationRecosTemplate", model:['observationInstance':observationInstance, 'result':results.recoVotes, 'totalVotes':results.totalVotes]);
					def result = ['html':html, 'max':params.max]
					render result as JSON;
				} else {
					response.setStatus(500);
					def message = "";
					if(params.offset > 0) {
						message = [info: g.message(code: 'recommendations.nomore.message', default:'No more recommendations made. Please suggest')];
					} else {
						message = [info:g.message(code: 'recommendations.zero.message', default:'No recommendations made. Please suggest')];
					}
					render message as JSON
				}
			} catch(e){
				e.printStackTrace();
				response.setStatus(500);
				def message = ['error' : g.message(code: 'error', default:'Error while processing the request.')];
				render message as JSON
			}
		}
		else {
			response.setStatus(500)
			def message = ['error':g.message(code: 'error', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	/**
	 * 
	 */
	def voteDetails = {
		log.debug params;
		def votes = RecommendationVote.findAll("from RecommendationVote as recoVote where recoVote.recommendation.id = :recoId and recoVote.observation.id = :obvId order by recoVote.votedOn desc", [recoId:params.long('recoId'), obvId:params.long('obvId')]);
		render (template:"/common/voteDetails", model:[votes:votes]);
	}

	/**
	 * 
	 */

	def listRelated = {
		log.debug params;
		def relatedObv = getRelatedObservations(params);
		
		def model = [observationInstanceList: relatedObv.observations.observation, observationInstanceTotal: relatedObv.count, queryParams: [:], activeFilters:[:]]
		render (view:'listRelated', model:model)
	}
	
	/**
	 * 
	 */
	def getRelatedObservation = {
		log.debug params;
		def relatedObv = getRelatedObservations(params);

		if(relatedObv.observations) {
			relatedObv.observations = observationService.createUrlList2(relatedObv.observations);
		}
		render relatedObv as JSON
	}

	/**
	 * 
	 * @param params
	 * @return
	 */

	protected Map getRelatedObservations(params) {
		log.debug params;
		def max = Math.min(params.limit ? params.int('limit') : 3, 100)

		def offset = params.offset ? params.int('offset') : 0

		def relatedObv
		if(params.filterProperty == "speciesName") {
			relatedObv = observationService.getRelatedObservationBySpeciesName(params.long('id'), max, offset)
		} else if(params.filterProperty == "speciesGroup"){
			relatedObv = observationService.getRelatedObservationBySpeciesGroup(params.long('filterPropertyValue'),  max, offset)
		}else if(params.filterProperty == "user"){
			relatedObv = observationService.getRelatedObservationByUser(params.long('filterPropertyValue'), max, offset, params.sort)
		}else if(params.filterProperty == "nearBy"){
			relatedObv = observationService.getNearbyObservations(params.id, 5)
		}else{
			relatedObv = observationService.getRelatedObservation(params.filterProperty, params.long('id'), max, offset)
		}
		return relatedObv
	}


	def tags = {
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

	@Secured(['ROLE_USER'])
	def flagDeleted = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		def observationInstance = Observation.findWhere(id:params.id.toLong(), author: params.author)
		if (observationInstance) {
			try {
				observationInstance.isDeleted = true;
				observationInstance.save(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

	def snippet = {
		def observationInstance = Observation.get(params.id)

		render (template:"/common/observation/showObservationSnippetTabletTemplate", model:[observationInstance:observationInstance]);
	}

	private sendNotificationMail(String notificationType, Observation obv, request){
		//(commented / recommended a species name/ agreed on a species suggested)

		if(!obv.author.sendNotification){
			log.debug "Not sending any notification mail for user " + obv.author.id
			return
		}

		def conf = SpringSecurityUtils.securityConfig
		def obvUrl = generateLink("observation", "show", ["id": obv.id], request)
		def userProfileUrl = generateLink("SUser", "show", ["id": obv.author.id], request)

		def templateMap = [user: obv.author, obvUrl:obvUrl, userProfileUrl:userProfileUrl]

		def mailSubject = ""
		def body = ""

		if(notificationType == OBSERVATION_ADDED){
			mailSubject = conf.ui.addObservation.emailSubject
			body = conf.ui.addObservation.emailBody
		}else if (notificationType == SPECIES_RECOMMENDED){
			mailSubject = "Species name suggested"
			body = conf.ui.addRecommendationVote.emailBody
			templateMap["currentUser"] = springSecurityService.currentUser
			templateMap["currentActivity"] = "recommended a species name"
		}else{
			mailSubject = "Species name suggested"
			body = conf.ui.addRecommendationVote.emailBody
			templateMap["currentUser"] = springSecurityService.currentUser
			templateMap["currentActivity"] = "agreed on a species suggested"
		}
		if (body.contains('$')) {
			body = evaluate(body, templateMap)
		}

		mailService.sendMail {
			to obv.author.email
			from conf.ui.notification.emailFrom
			subject mailSubject
			html body.toString()
		}
	}

	private String generateLink( String controller, String action, linkParams, request) {
		createLink(base: "$request.scheme://$grailsApplication.config.speciesPortal.domain$request.contextPath",
				controller:controller, action: action,
				params: linkParams)
	}

	private String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}
	
	/**
	*
	* @param params
	* @return
	*/
   private RecommendationVote getRecommendationVote(params) {
	   def observation = params.observation?:Observation.get(params.obvId);
	   def author = params.author;
	   
	   def reco;
	   if(params.recoId)
		   reco = Recommendation.get(params.long('recoId'));
	   else
		   reco = observationService.getRecommendation(params.recoName, params.canName);
	   
	   ConfidenceType confidence = observationService.getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());
	   
	   RecommendationVote existingRecVote = RecommendationVote.findByAuthorAndObservation(author, observation);
	   RecommendationVote newRecVote = new RecommendationVote(observation:observation, recommendation:reco, author:author, confidence:confidence);
	   
	   if(!reco){
		   log.debug "Not a valid recommendation"
		   return null
	   }else{
		   if(!existingRecVote){
			   log.debug " Adding (first time) recommendation vote for user " + author.id +  " reco name " + reco.name
			   flash.message = "${message(code: 'recommendations.added.message', args: [reco.name])}"
			   return newRecVote
		   }else{
			   if(existingRecVote.recommendation.id == reco.id){
				   log.debug " Same recommendation already made by user " + author.id +  " reco name " + reco.name + " leaving as it is"
				   return null
			   }else{
			   	   log.debug " Overwrting old recommendation vote for user " + author.id +  " new reco name " + reco.name + " old reco name " + existingRecVote.recommendation.name
				   flash.message = "${message(code: 'recommendations.overwrite.message', args: [existingRecVote.recommendation.name, reco.name])}"
				   if(!existingRecVote.delete(flush: true)){
					   existingRecVote.errors.allErrors.each { log.error it }
				   }
				   return newRecVote;
			   }
		   }
	   }
   }
   private addMsgToFlash(msg){
	   flash.message = msg
   }   
}
