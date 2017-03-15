package species

import species.auth.SUser;
import species.SpeciesPermission;
import species.CommonNames;
import species.participation.ActivityFeed;

class SpeciesTagLib {
	
	static namespace = "s"

    def springSecurityService;
    def speciesService;
	def speciesPermissionService;

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
				out << "<a href=\"http://www.eol.org/pages/${extLink.eolId}\" title=\"View on Encyclopedia of Life\" target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/eol.png', absolute:true)}\"/></a>" ;
				break;
			case "gbifId" : 
				out << "<a href=\"http://www.gbif.org/species/${extLink.gbifId}\" title=\"View on GBIF\" target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/gbif.png', absolute:true)}\"/></a>";
				 break; 
			case "iucnId" : 
				out << "<a href=\"http://www.iucnredlist.org/apps/redlist/details/${extLink.iucnId?.replace('IUCN-', '')}\" title=\"View on IUCN Red List\" target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/iucn.png', absolute:true)}\"/></a>";
				break;
			case "colId" : 
				out << "<a href=\"http://www.catalogueoflife.org/annual-checklist/2010/details/species/id/${extLink.colId}\" title=\"View on COL\"  target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/col.png', absolute:true)}\"/></a>";
				break;
			case "itisId" : 
				out << "http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=${extLink.itisId}\" title=\"View on ITIS\"  target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/itis.png', absolute:true)}\"/></a>";
				break;
			case "ncbiId" : 
				out << "<a href=\"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=${extLink.ncbiId}\" title=\"View on NCBI\"  target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/ncbi.png', absolute:true)}\"/></a>";
				break;
			case "uBio" : 
				out << "<a href=\"http://www.ubio.org/browser/search.php?search_all=${attrs.model.taxonConcept.binomialForm}\" title=\"View on uBio\" target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/uBio.png', absolute:true)}\"/></a>";
				break;
			case "wikipedia" :
				out << "<a href=\"http://en.wikipedia.org/wiki/${attrs.model.taxonConcept.binomialForm?:attrs.model.taxonConcept.name}\" title=\"View on Wikipedia\"  target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/wiki.png', absolute:true)}\"/></a>";
				break;
			case "frlhtUrl" :
				out << "<a href=\"${extLink.frlhtUrl}\" title=\"View on FRLHT's ENVIS\"  target=\"_blank\"><img class=\"group_icon pull_left\" src=\"${assetPath(src: '/all/icons/externalLinks/FRLHT32x32.gif', absolute:true)}\"/></a>";
				break;

		}
	}
	
	/**
	 *  
	 */
	def showThreatenedStatus = {attrs, body ->
		switch(attrs.model.threatenedStatus) {
			case "NT" : 
				out << "<img class=\"species_group_icon\" title=\"Near Threatened\" src=\"${assetPath(src: '/all/icons/externalLinks/NT.png', absolute:true)}\"/>";
				break;
			case "EX" : 
				out << "<img class=\"species_group_icon\" title=\"Extinct\" src=\"${assetPath(src: '/all/icons/externalLinks/EX.png', absolute:true)}\"/>";
				break;
			case "LR/cd" :
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${assetPath(src: '/all/icons/externalLinks/LC.png', absolute:true)}\"/>";
				break;
			case "EN" :
				out << "<img class=\"species_group_icon\" title=\"Endangered\" src=\"${assetPath(src: '/all/icons/externalLinks/EN.png', absolute:true)}\"/>";
				break;
			case "CR" :
				out << "<img class=\"species_group_icon\" title=\"Critically Endangered\" src=\"${assetPath(src: '/all/icons/externalLinks/CR.png', absolute:true)}\"/>";
				break;
			case "LR/lc" : 
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${assetPath(src: '/all/icons/externalLinks/LC.png', absolute:true)}\"/>";
				break;
			case "LR/nt" : 
				out << "<img class=\"species_group_icon\" title=\"Near Threatened\" src=\"${assetPath(src: '/all/icons/externalLinks/NT.png', absolute:true)}\"/>";
				break;
			case "LC" : 
				out << "<img class=\"species_group_icon\" title=\"Least Concern\" src=\"${assetPath(src: '/all/icons/externalLinks/LC.png', absolute:true)}\"/>";
				break;
			case "DD" :
				out << "<img class=\"species_group_icon\" title=\"Data Deficient\" src=\"${assetPath(src: '/all/icons/externalLinks/DD.png', absolute:true)}\"/>";
				break;
			case "EW" :
				out << "<img class=\"species_group_icon\" title=\"Extinct in the Wild\" src=\"${assetPath(src: '/all/icons/externalLinks/EW.png', absolute:true)}\"/>";
				break;
			case "VU" :
				out << "<img class=\"species_group_icon\" title=\"Vulnerable\" src=\"${assetPath(src: '/all/icons/externalLinks/VU.png', absolute:true)}\"/>";
				break;
			case "NE" :
				out << "<img class=\"species_group_icon\" title=\"Not Evaluated\" src=\"${assetPath(src: '/all/icons/externalLinks/NE.png', absolute:true)}\"/>";
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

	def showSnippet = {attrs, body->
		if(attrs.model.speciesInstance) {
			out << render(template:"/species/showSpeciesSnippetTemplate", model:attrs.model);
		}
	}

	def showSpeciesExternalLink = {attrs, body->
		out << render(template:"/species/showSpeciesExternalLinkTemplate", model:attrs.model);
	}
	
	def showDownloadAction = {attrs, body->
		out << render(template:"/species/showDownloadAction", model:attrs.model);
	}
	
	def rollBackTable = {attrs, body->
		out << render(template:"/species/speciesBulkUploadTableTemplate", model:attrs.model);
	}

	def namesReportTable = {attrs, body->
		out << render(template:"/species/namesReportTableTemplate", model:attrs.model);
	}

	
    def hasPermission = {attrs, body ->
        SpeciesField speciesFieldInstance = attrs.model.speciesFieldInstance;
        Field fieldInstance = attrs.model.fieldInstance;
        SUser currentUser = springSecurityService.currentUser;

        if((speciesFieldInstance && speciesFieldInstance.description) || speciesPermissionService.isContributor(speciesFieldInstance, fieldInstance, currentUser)) {
            out << body();
        }
    }

    def isSpeciesContributor = {attrs, body -> 
        Species speciesInstance = attrs.model.speciesInstance;
        SUser currentUser = springSecurityService.currentUser;

        if(speciesPermissionService.isSpeciesContributor(speciesInstance, currentUser)) {
            out << body();
        }
    }

    def isSpeciesFieldContributor = {attrs, body ->
        SpeciesField speciesFieldInstance = attrs.model.speciesFieldInstance;
        SUser currentUser = springSecurityService.currentUser;

        if(speciesPermissionService.isSpeciesFieldContributor(speciesFieldInstance, currentUser)) {
            out << body();
        }
    }

    def isCurator = {attrs, body ->
        SpeciesField speciesFieldInstance = attrs.model.speciesFieldInstance;
        SUser currentUser = springSecurityService.currentUser;

        if(speciesPermissionService.isCurator(speciesFieldInstance, currentUser)) {
            out << body();
        }
    }

    def hasContent = {attrs, body ->
        def map = attrs.model.map;
        if(map instanceof Map && (map.hasContent || map.isContributor)) {
            out << body();
        }
    }

    def updatereference = {attrs, body->
        println "--------------------------------"
        println attrs.model
    	def result = speciesService.updateReference(attrs.model.referenceId, attrs.model.speciesId, attrs.model.fieldId, attrs.model.speciesFieldId, attrs.model.value);
		out << body();
	}

	def noOfContributedSpecies={attrs,body->
		def noOfSpecies=speciesService.totalContributedSpecies(attrs.model.user)

		out << "<td class=countvaluecontributed>"+noOfSpecies+"</td>"

	}
	
		def contriutedSnippet={attrs,body->
		def noOfSpecies=speciesService.totalContributedSpeciesSnippet(attrs.model.user)

		out << "<td class=countvalue>"+noOfSpecies+"</td>"

	}

	def showNoOfOrganizedSpecies={attrs,body->
		String[] activityType=["Added common name","Updated Common Name","Added hierarchy","Posted resource","Added synonym","Updated synonym","Deleted synonym","Updated species gallery","Added species field","Updated species field","Deleted species field"]
		def totalNoOfOrganizedSpecies=0;
		activityType.each{
			def noOfOrganizedSpecies=ActivityFeed.findAllByAuthorAndRootHolderTypeAndActivityType(attrs.model.user,attrs.model.rootHolderType,it);
			//println "========="+it+"===================="+noOfOrganizedSpecies.size();
			totalNoOfOrganizedSpecies=noOfOrganizedSpecies.size()+totalNoOfOrganizedSpecies
		}
		out << "<td class=countvalue>"+totalNoOfOrganizedSpecies+"</td>";
	}
	def showContributedSpecies={attrs,body->
		def species=speciesService.getuserContributionList(attrs.model.user.toInteger())
		out << render(template:"/species/contributedSpeciesTemplate", model:['user':attrs.model,'contributedSpecies':species]);

	}

}
