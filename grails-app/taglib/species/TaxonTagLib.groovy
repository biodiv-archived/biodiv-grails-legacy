package species

class TaxonTagLib {

	static namespace = "t"
	
	def showTaxonBrowser = { attrs, body->
		out << render(template:"/common/taxonBrowserTemplate", model:attrs.model);
	}
}
