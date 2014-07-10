package species

import grails.converters.JSON;
import species.groups.SpeciesGroup;

class SpeciesGroupController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
        render SpeciesGroup.list() as JSON
	}

	def tags = {
		render SpeciesGroup.list().collect{it.name} as JSON
	}
	
	def create = {
		def speciesGroupInstance = new SpeciesGroup()
		speciesGroupInstance.properties = params
		return [speciesGroupInstance: speciesGroupInstance]
	}

	def save = {
		def speciesGroupInstance = new SpeciesGroup(params)
		if (speciesGroupInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), speciesGroupInstance.id])}"
			redirect(action: "show", id: speciesGroupInstance.id)
		}
		else {
			render(view: "create", model: [speciesGroupInstance: speciesGroupInstance])
		}
	}

	def show = {
		def speciesGroupInstance = SpeciesGroup.get(params.id)
		def groupsConfig = grailsApplication.config.speciesPortal.group

		if (!speciesGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
			redirect(action: "list")
		} else if (speciesGroupInstance.name.equalsIgnoreCase(groupsConfig.ALL)) {
			TaxonomyDefinition.list().each {
				speciesGroupInstance.addToTaxonConcept(it);
			}
		} else if (speciesGroupInstance.name.equalsIgnoreCase(groupsConfig.OTHERS)) {
			TaxonomyDefinition.findAllByGroupIsNull().each {
				speciesGroupInstance.addToTaxonConcept(it);
			}
		}

		[speciesGroupInstance: speciesGroupInstance]
	}

	def edit = {
		def speciesGroupInstance = SpeciesGroup.get(params.id)
		if (!speciesGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [speciesGroupInstance: speciesGroupInstance]
		}
	}

	def update = {
		def speciesGroupInstance = SpeciesGroup.get(params.id)
		if (speciesGroupInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (speciesGroupInstance.version > version) {

					speciesGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'speciesGroup.label', default: 'SpeciesGroup')]
					as Object[], "Another user has updated this SpeciesGroup while you were editing")
					render(view: "edit", model: [speciesGroupInstance: speciesGroupInstance])
					return
				}
			}
			speciesGroupInstance.properties = params
			if (!speciesGroupInstance.hasErrors() && speciesGroupInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), speciesGroupInstance.id])}"
				redirect(action: "show", id: speciesGroupInstance.id)
			}
			else {
				render(view: "edit", model: [speciesGroupInstance: speciesGroupInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def speciesGroupInstance = SpeciesGroup.get(params.id)
		if (speciesGroupInstance) {
			try {
				speciesGroupInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesGroup.label', default: 'SpeciesGroup'), params.id])}"
			redirect(action: "list")
		}
	}
}
