package species

class DomainTagLib {
	static namespace = "domain"

	def showWGPHeader = { attrs ->
		out << render(template:"/domain/wgpHeaderTemplate");
	}

	def showIBPHeader = { attrs ->
		out << render(template:"/domain/ibpHeaderTemplate");
	}

}
