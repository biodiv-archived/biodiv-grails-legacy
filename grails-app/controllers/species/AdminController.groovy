package species

import org.hibernate.exception.ConstraintViolationException;

import species.participation.Recommendation;

class AdminController {

    def index = { }
	
	def namesLoaderService;
	def namesIndexerService;
	def groupHandlerService;
	def sessionFactory;
	
	def names = {
		
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
		
		redirect(action: "names")
	}
	
	def reloadNamesIndex = {
		try {
			namesIndexerService.rebuild();
			flash.message = "Successfully created names index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "names")
	}
	
	def updateGroups = {
		try {
			groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);
			flash.message = "Successfully updated all taxonconcept group associations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "names")
	}
}
