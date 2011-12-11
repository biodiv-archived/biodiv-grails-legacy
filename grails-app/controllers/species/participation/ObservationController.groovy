package species.participation

import groovy.util.Node

import org.springframework.web.multipart.MultipartHttpServletRequest

import grails.converters.JSON;
import grails.plugins.springsecurity.Secured
import species.sourcehandler.XMLConverter
import species.utils.ImageUtils

class ObservationController {

	def grailsApplication;
	def observationService;
	def springSecurityService;

	static allowedMethods = [update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[observationInstanceList: Observation.list(params), observationInstanceTotal: Observation.count()]
	}

	@Secured(['ROLE_USER'])
	def create = {
		def observationInstance = new Observation()
		observationInstance.properties = params
		return [observationInstance: observationInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {
		if(request.method == 'POST') {
			log.debug params;
			//TODO:edit also calls here...handle that wrt other domain objects

			params.author = springSecurityService.currentUser;

			def observationInstance =  observationService.createObservation(params);

			if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
				//flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
				log.debug "Successfully created observation : "+observationInstance

				params.obvId = observationInstance.id

				redirect(action: 'addRecommendationVote', params:params);
			} else {
				render(view: "create", model: [observationInstance: observationInstance])
			}
		} else {
			render(view: "create")
		}
	}

	def show = {
		def observationInstance = Observation.get(params.id)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
		else {
			[observationInstance: observationInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def edit = {
		def observationInstance = Observation.get(params.id)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [observationInstance: observationInstance]
		}
	}

	@Secured(['ROLE_USER'])
	def update = {
		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (observationInstance.version > version) {

					observationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'observation.label', default: 'Observation')]
					as Object[], "Another user has updated this Observation while you were editing")
					render(view: "edit", model: [observationInstance: observationInstance])
					return
				}
			}
			observationInstance.properties = params
			try {
				if (!observationInstance.hasErrors() && observationInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					redirect(action: "show", id: observationInstance.id)
				}
				else {
					render(view: "edit", model: [observationInstance: observationInstance])
				}}catch(e) {
				render(view: "edit", model: [observationInstance: observationInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
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

		if(request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

			def resourcesInfo = [];
			def rootDir = grailsApplication.config.speciesPortal.observations.rootDir
			File obvDir;
			String message;

			if(!params.resources) {
				message = "${message(code: 'resource.attachment.missing.message')}";
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
					message = "${message(code: 'resource.file.invalid.extension.message', args: [okcontents, f.originalFilename])}"
				}
				else if(f.size > 104857600) { //100MB
					message = "${message(code: 'resource.file.invalid.extension.message', args: [104857600/1024, f.originalFilename,f.size/1024 ], default:'File size cannot exceed ${104857600/1024}KB')}";
				}
				else if(f.empty) {
					message = "${message(code: 'file.empty.message', default:'File cannot be empty')}";
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

					File file = new File(obvDir, f.originalFilename);
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
				render message
			}
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
			def recommendationVoteInstance = observationService.createRecommendationVote(params)
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if (recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					redirect(action: "show", id: observationInstance.id);
				}
				else {
					recommendationVoteInstance.errors.allErrors.each { log.error it }
					render(view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance])
				}
			} catch(e) {
				render(view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance])
			}
		} else {
			flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def voteDetails = {
		log.debug params;
		def votes = RecommendationVote.findAll("from RecommendationVote as recoVote where recoVote.recommendation.id = :recoId and recoVote.observation.id = :obvId order by recoVote.votedOn desc", [recoId:params.long('recoId'), obvId:params.long('obvId')]);
		render (template:"/common/voteDetails", model:[votes:votes]);
	}
}
