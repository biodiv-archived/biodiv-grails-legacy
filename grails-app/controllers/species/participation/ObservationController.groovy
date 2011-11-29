package species.participation

import species.participation.Observation;

class ObservationController {

	def grailsApplication;
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[observationInstanceList: Observation.list(params), observationInstanceTotal: Observation.count()]
	}

	def create = {
		def observationInstance = new Observation()
		observationInstance.properties = params
		return [observationInstance: observationInstance]
	}

	def save = {
		def observationInstance = new Observation(params)
		if (observationInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
			redirect(action: "show", id: observationInstance.id)
		}
		else {
			render(view: "create", model: [observationInstance: observationInstance])
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
			if (!observationInstance.hasErrors() && observationInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
				redirect(action: "show", id: observationInstance.id)
			}
			else {
				render(view: "edit", model: [observationInstance: observationInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect(action: "list")
		}
	}

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

	def upload = {
		def f = request.getFile('imageFile')
		if(!f.empty) {
			//TODO: if multiple requests with same file name are gng on ||ly this file will get corrupted.
			String dir = grailsApplication.config.speciesPortal.images.rootDir + File.separator + "observations"
			
			def obvDir = new File(dir);
			if(!obvDir.exists()) {
				obvDir.mkdir();
			}
			
			File file = new File(obvDir, UUID.randomUUID());
			f.transferTo( file );
			flash.message = 'Image uploaded'
			response.sendError(200,'Done');
		}
		else {
			flash.message = 'file cannot be empty'
			render(view:'uploadForm')
		}
	}
}
