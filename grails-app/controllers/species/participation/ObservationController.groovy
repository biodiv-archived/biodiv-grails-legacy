package species.participation

import org.grails.taggable.*
import groovy.util.Node

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest

import grails.converters.JSON;
import grails.plugins.springsecurity.Secured
import species.sourcehandler.XMLConverter
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;

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
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : grailsApplication.config.speciesPortal.group.ALL
		//params.userName = springSecurityService.currentUser.username;
		
		def query = "select obv from Observation obv "
		def queryParams = [:]
		def filterQuery = ""
		
		if(params.sGroup){
			params.sGroup = params.sGroup.toLong()
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			}else{

				filterQuery += " where obv.group.id = :groupId "
				queryParams["groupId"] = groupId
			}
		}
		
		if(params.tag){
			query = "select obv from Observation obv,  TagLink tagLink "
			(filterQuery == "")? (filterQuery += "  where ") : (filterQuery += "  and ")
			filterQuery +=  " obv.id = tagLink.tagRef and tagLink.type like :tagType and tagLink.tag.name like :tag "
			
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'observation'
		}
		
		
		if(params.habitat && (params.habitat != grailsApplication.config.speciesPortal.group.ALL)){
			(filterQuery == "")? (filterQuery += " where obv.habitat like :habitat ") : (filterQuery += " and obv.habitat like :habitat ")
			queryParams["habitat"] = params.habitat
		}
		
		if(params.userId){
			(filterQuery == "")? (filterQuery += " where ") : (filterQuery += " and ")
			filterQuery += " obv.author.id = :userId "
			queryParams["userId"] = params.userId.toInteger()
		}

		def orderByClause = "order by obv." + (params.sort ? params.sort : "createdOn") +  " desc"

		query += filterQuery + orderByClause
		def count = Observation.executeQuery(query, queryParams).size()
		queryParams["max"] = params.max

		if(params.offset) {
			queryParams["offset"] = params.offset.toInteger()
		}
		//log.error("===============query =======" + query)
		//log.error("===============params =====" + queryParams)	
		def observationInstanceList = Observation.executeQuery(query, queryParams)
		//log.error("================= result == " +observationInstanceList  )
		//log.error("================= size  == " +observationInstanceList.size()  )
		
		[observationInstanceList: observationInstanceList, observationInstanceTotal: count]
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
			log.debug params;
			//TODO:edit also calls here...handle that wrt other domain objects

			params.author = springSecurityService.currentUser;

			try {
				def observationInstance =  observationService.createObservation(params);

				if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
					//flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
					log.debug "Successfully created observation : "+observationInstance

					params.obvId = observationInstance.id

					def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();

					observationInstance.setTags(tags);

					redirect(action: 'addRecommendationVote', params:params);
				} else {
					observationInstance.errors.allErrors.each { log.error it }
					redirect(view: "create", model: [observationInstance: observationInstance])
				}
			} catch(e) {
				e.printStackTrace();
				flash.message = "${message(code: 'error')}";
				redirect(view: "create")
			}
		} else {
			redirect(view: "create")
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

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				def rs = [:]
				Utils.populateHttpServletRequestParams(request, rs);
				println "^^^^ : "+rs;
				def resourcesInfo = [];
				def rootDir = grailsApplication.config.speciesPortal.observations.rootDir
				File obvDir;
				def message;

				if(!params.resources) {
					message = "${message(code: 'no.file.attached', default:'No file is attached')}"
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
					else if(f.size > grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE) {
						message = "${message(code: 'resource.file.invalid.extension.message', args: [grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE/1024, f.originalFilename,f.size/1024 ], default:'File size cannot exceed ${104857600/1024}KB')}";
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
				def message = [error:"${message(code: 'no.file.attached', default:'No file is attached')}"]
				render message as JSON
			}
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			def message = [error:"${message(code: 'error', default:'Error while processing the request.')}"]
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
			def recommendationVoteInstance = observationService.createRecommendationVote(params)
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if (!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
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
			def recommendationVoteInstance = observationService.createRecommendationVote(params)
			def observationInstance = Observation.get(params.obvId);
			log.debug params;
			try {
				if (recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					success = true;
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
				log.debug "======="
				log.debug results;
				if(results?.recoVotes.size() > 0) {
					def html =  g.render(template:"/common/observation/showObservationRecosTemplate", model:['observationInstance':observationInstance, 'result':results.recoVotes, 'totalVotes':results.totalVotes]);
					def result = ['html':html, 'max':params.max]
					render result as JSON;
				} else {
					response.setStatus(500)
					def message = ['info' : "${message(code: 'recommendations.zero.message', default:'No recommendations made. Please suggest')}"];
					render message as JSON
				}
			} catch(e){
				e.printStackTrace();
				response.setStatus(500)
				def message = ['error' : "${message(code: 'error', default:'Error while processing the request.')}"];
				render message as JSON
			}
		}
		else {
			response.setStatus(500)
			def message = ['error':"${message(code: 'error', default:'Error while processing the request.')}"]
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

	def getRelatedObservation = {
		log.debug params;
		def relatedObv;
		if(params.filterProperty == "speciesName"){
			relatedObv = observationService.getRelatedObservationBySpeciesName(params)
		}else if(params.filterProperty == "speciesGroup"){
			relatedObv = observationService.getRelatedObservationBySpeciesGroup(params)
		}else if(params.filterProperty == "user"){
			relatedObv = observationService.getRelatedObservationByUser(params)
		}else{
			relatedObv = observationService.getRelatedObservation(params)
		}
		render relatedObv as JSON
	}


	def tags = {
		log.debug params;
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

}
