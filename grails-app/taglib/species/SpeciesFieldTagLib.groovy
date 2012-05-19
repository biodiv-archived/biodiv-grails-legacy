package species

class SpeciesFieldTagLib {
	def showSpeciesField = {attrs, body->

		def speciesInstance = attrs.model.speciesInstance
		def speciesFieldInstance = attrs.model.speciesFieldInstance
		if(speciesFieldInstance) {
			def field = speciesFieldInstance.field;
			if(field.concept.equals("Nomenclature and Classification")){
				if(field.category.equals("uBio Taxomonic Search")) {
					out << render(template:"/common/uBioTaxonomyFieldTemplate", model:attrs.model);
				}  else {
					out << render(template:"/common/speciesFieldTemplate", model:attrs.model);
				}
			} 
			/*  else if(field.concept.equals("Taxonomy _ ID")) {
				out << render(template:"/common/uBioTaxonomyFieldTemplate", model:attrs.model);
			}*/  else {
				out << render(template:"/common/speciesFieldTemplate", model:attrs.model);
			}
		} else {
			//println "speciesFieldInstance is null"
		}
	}

	def showSpeciesConcept = {attrs, body ->
		out << render(template:"/common/speciesConceptTemplate", model:attrs.model);
	}
	
	def showSpeciesFieldAttribution = {attrs, body ->
		out << render(template:"/common/speciesFieldAttributionTemplate", model:attrs.model);
	}
	
	def showSpeciesFieldHelp = {attrs, body ->
		out << render(template:"/common/speciesFieldHelpTemplate", model:attrs.model);
	}
	
	def showSpeciesFieldToolbar = {attrs, body ->
		out << render(template:"/common/speciesFieldToolbarTemplate", model:attrs.model);
	}
	
	def editField = {attrs, body ->
		out << render(template:"/common/editFieldTemplate", model:attrs.model);
	}
	
	def imageAttribution = {attrs, body ->
		out << render(template:"/common/imageAttributionTemplate", model:attrs.model);
	}
	
}
