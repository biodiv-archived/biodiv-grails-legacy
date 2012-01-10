package species

class SpeciesTagLib {
	
	static namespace = "s"

	def showSpeciesImages = { attrs, body->
		out << render(template:"/common/speciesImagesTemplate", model:attrs.model);
	}
}
