package species

import org.springframework.security.access.annotation.Secured;

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
			log.debug "Reloading all names into recommendations"
			namesLoaderService.syncNamesAndRecos(true);
			flash.message = "Successfully loaded all names into recommendations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}

		redirect(action: "index")
	}

	def reloadSearchIndex = {
		try {
			searchService.publishSearchIndex(Species.list());
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
		try {
			groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);
			flash.message = "Successfully updated all taxonconcept group associations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

}
