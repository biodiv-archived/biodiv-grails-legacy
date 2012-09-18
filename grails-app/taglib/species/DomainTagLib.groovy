package species

class DomainTagLib {
	static namespace = "domain"

	def showWGPHeader = { attrs ->
		out << render(template:"/domain/wgpHeaderTemplate");
	}

	def showWGPFooter = { attrs ->
		out << render(template:"/domain/wgpFooterTemplate");
	}

	def showIBPHeader = { attrs ->
		out << render(template:"/domain/ibpHeaderTemplate");
	}

	def showIBPFooter = { attrs ->
		out << render(template:"/domain/ibpFooterTemplate");
	}
	
	def showHeader = { attrs ->
		out << render(template:"/domain/headerTemplate");
	}

}
