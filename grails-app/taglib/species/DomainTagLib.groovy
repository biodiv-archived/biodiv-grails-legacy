package species

import species.groups.UserGroup;

class DomainTagLib {
	static namespace = "domain"

	def showSiteHeader = { attrs ->
		out << render(template:"/domain/${grailsApplication.config.speciesPortal.app.siteCode}HeaderTemplate", model:attrs.model);
	}

	def showSiteFooter = { attrs ->
		out << render(template:"/domain/${grailsApplication.config.speciesPortal.app.siteCode}FooterTemplate");
	}
	
	def showHeader = { attrs ->
		out << render(template:"/domain/headerTemplate", model:attrs.model);
	}

}
