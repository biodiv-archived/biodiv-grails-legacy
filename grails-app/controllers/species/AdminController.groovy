package species

import grails.plugins.springsecurity.Secured;

@Secured(['ROLE_ADMIN'])
class AdminController {
	
	def setupService;
	def speciesService;
	def taxonService;
	def searchService;
	def namesLoaderService;
	def namesIndexerService;
	def groupHandlerService;
	def sessionFactory;
	def externalLinksService;

	/**
	 * 
	 */
	def index = {
	}

	/**
	 * 
	 */
	def setup = {
		try {
			setupService.setupDefs();
			flash.message = "Successfully loaded all definitions"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def loadData = {
		int noOfInsertions = 0;
		try {
			noOfInsertions = speciesService.loadData();
			flash.message = "Added ${noOfInsertions} records"
		} catch(e) {
			e.printStackTrace();
			flash.message = "Inserted ${noOfInsertions} records. Error while doing so ${e.getMessage()}"
		}
		redirect(action: "index")
	}

	def loadNames = {
		try {
			taxonService.loadTaxon(true);
			flash.message = "Finished loading names"
		} catch(e) {
			e.printStackTrace();
			flash.message = "Error ${e.getMessage()}"
		}
		
		redirect (action:"index");
	}

	def reloadNames = {
		try {
			log.debug "Syncing names into recommendations"
			namesLoaderService.syncNamesAndRecos(false);
			flash.message = "Successfully loaded all names into recommendations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}

		redirect(action: "index")
	}

	def reloadSearchIndex = {
		try {
			searchService.publishSearchIndex();
			flash.message = "Successfully created search index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def reloadNamesIndex = {
		try {
			namesIndexerService.rebuild();
			flash.message = "Successfully created names index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def updateGroups = {
		int noOfUpdations = 0;
		try {
			noOfUpdations = groupHandlerService.updateGroups();
			flash.message = "Successfully updated group associations for taxonConcepts ${noOfUpdations}"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}
	
	def updateExternalLinks = {
		try {
			int noOfUpdations = externalLinksService.updateExternalLinks();
			flash.message = "Successfully updated externalLinks for taxonConcepts ${noOfUpdations}"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def recomputeInfoRichness = {
		try {
			speciesService.computeInfoRichness();
			flash.message = "Successfully updated species information richness"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}
}
