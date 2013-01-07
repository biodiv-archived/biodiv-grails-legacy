package species

class SpeciesTagLib {
	
	static namespace = "s"

	def showSpeciesImages = { attrs, body->
		out << render(template:"/common/speciesImagesTemplate", model:attrs.model);
	}
	
	def showSpeciesIcons = { attrs, body ->
		out << render(template:"/common/speciesIconsTemplate", model:attrs.model);
	}
	
	def showExternalLink = { attrs, body ->
		ExternalLinks extLink = attrs.model.externalLinks;
		
		switch(attrs.model.key) {
			case "eolId" : 
				out << "<a href=\"http://www.eol.org/pages/${extLink.eolId}\" title=\"View on Encyclopedia of Life\" target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'eol.png', absolute:true)}\"/></a>" ;
				break;
			case "gbifId" : 
				out << "<a href=\"http://data.gbif.org/species/${extLink.gbifId}\" title=\"View on GBIF\" target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'gbif.png', absolute:true)}\"/></a>";
				 break; 
			case "iucnId" : 
				out << "<a href=\"http://www.iucnredlist.org/apps/redlist/details/${extLink.iucnId?.replace('IUCN-', '')}\" title=\"View on IUCN Red List\" target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'iucn.png', absolute:true)}\"/></a>";
				break;
			case "colId" : 
				out << "<a href=\"http://www.catalogueoflife.org/annual-checklist/2010/details/species/id/${extLink.colId}\" title=\"View on COL\"  target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'col.png', absolute:true)}\"/></a>";
				break;
			case "itisId" : 
				out << "http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=${extLink.itisId}\" title=\"View on ITIS\"  target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'itis.png', absolute:true)}\"/></a>";
				break;
			case "ncbiId" : 
				out << "<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=${extLink.ncbiId}\" title=\"View on NCBI\"  target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'ncbi.png', absolute:true)}\"/></a>";
				break;
			case "uBio" : 
				out << "<a href=\"http://www.ubio.org/browser/search.php?search_all=${attrs.model.taxonConcept.binomialForm}\" title=\"View on uBio\" target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'uBio.png', absolute:true)}\"/></a>";
				break;
			case "wikipedia" :
				out << "<a href=\"http://en.wikipedia.org/wiki/${attrs.model.taxonConcept.binomialForm?:attrs.model.taxonConcept.name}\" title=\"View on Wikipedia\"  target=\"_blank\"><img class=\"group_icon\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'wiki.png', absolute:true)}\"/></a>";
				break;
		}
	}
	
	/**
	 *  
	 */
	def showThreatenedStatus = {attrs, body ->
		switch(attrs.model.threatenedStatus) {
			case "NT" : 
				out << "<img class=\"species_group_icon\" title=\"Near Threatened\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'NT.png', absolute:true)}\"/>";
				break;
			case "EX" : 
				out << "<img class=\"species_group_icon\" title=\"Extinct\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'EX.png', absolute:true)}\"/>";
				break;
			case "LR/cd" :
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'LC.png', absolute:true)}\"/>";
				break;
			case "EN" :
				out << "<img class=\"species_group_icon\" title=\"Endangered\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'EN.png', absolute:true)}\"/>";
				break;
			case "CR" :
				out << "<img class=\"species_group_icon\" title=\"Critically Endangered\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'CR.png', absolute:true)}\"/>";
				break;
			case "LR/lc" : 
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'LC.png', absolute:true)}\"/>";
				break;
			case "LR/nt" : 
				out << "<img class=\"species_group_icon\" title=\"Near Threatened\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'NT.png', absolute:true)}\"/>";
				break;
			case "LC" : 
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'LC.png', absolute:true)}\"/>";
				break;
			case "DD" :
				out << "<img class=\"species_group_icon\" title=\"Data Deficient\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'DD.png', absolute:true)}\"/>";
				break;
			case "EW" :
				out << "<img class=\"species_group_icon\" title=\"Extinct in the Wild\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'EW.png', absolute:true)}\"/>";
				break;
			case "VU" :
				out << "<img class=\"species_group_icon\" title=\"Vulnerable\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'VU.png', absolute:true)}\"/>";
				break;
			case "NE" :
				out << "<img class=\"species_group_icon\" title=\"Not Evaluated\" src=\"${createLinkTo(dir: 'images/icons/externalLinks', file:'NE.png', absolute:true)}\"/>";
				break;
		}
	}
	
	def chooseLanguage = { attrs, body->
		out << render(template:"/common/chooseLanguageTemplate", model:attrs.model);
	}

	def showSubmenuTemplate = {attrs, body->
		out << render(template:"/species/speciesSubmenuTemplate", model:attrs.model);
	}
	
	def searchResults = {attrs, body->
		out << render(template:"/species/searchResultsTemplate", model:attrs.model);
	}
	
	def speciesFilter = {attrs, body->
		out << render(template:"/species/speciesFilterTemplate", model:attrs.model);
	}
	def showSpeciesList = {attrs, body->
		out << render(template:"/species/showSpeciesListTemplate", model:attrs.model);
	}
	def showHeadingAndSubHeading = {attrs, body->
		out << render(template:"/common/headingAndSubHeading", model:attrs.model);
	}
}
