package species

import species.auth.FacebookAuthToken;

class SUserTagLib {
	static namespace = "sUser"

	def springSecurityService
	def SUserService;
    def utilsService;
    def observationService
    def datasetService;
    def dataTableService;
    def dataPackageService;
	/**
	 * 
	 */
	def renderProfileLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << showUserTemplate(model:['userInstance':currentUser, 'hideDetails':attrs.model?.hideDetails]);
		}
	}

	def renderProfileHyperLink = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << createLink(controller:"user", action:"show", id:springSecurityService.currentUser.id);
		}
	}

	def renderCurrentUserId = { attrs ->
		def currentUser = springSecurityService.getCurrentUser()
		if(currentUser) {
			out << currentUser.id
		}
	}
	
	/**
	 * Renders the body if the authenticated user owns this page.
	 */
 	def ifOwns = { attrs, body ->
		if (utilsService.ifOwns(attrs.model.user)) {
			out << body()
		}
	}

    /**
    *checks the permission for lock/unlock of observation
    */
    def hasObvLockPerm = { attrs, body ->
        if(observationService.hasObvLockPerm(attrs.model.obvId, attrs.model.recoId)) {
            out << body()
        } 
    }
	
    /**
    *
    */
    def permToReorderPages = { attrs, body ->
        if(utilsService.permToReorderPages(attrs.model.userGroupInstance)){
            out<<body()
        }
    
    }

    def permToReorderDocNames = { attrs, body ->
    	if(utilsService.permToReorderDocNames(attrs.model.documentInstance)){
    		out<<body()
    	}
    }
    /**
	 * 
	 */
	def ifOwnsOrIsPublic = { attrs, body ->
		if (utilsService.ifOwns(attrs.model.user) || attrs.model.isPublic) {
			out << body()
		}
	}

	/**
	 * 
	 */
	def showUserTemplate = { attrs ->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserTemplate", model:attrs.model);
		}
	}

	/**
	 *
	 */
	def showUserSnippet = { attrs ->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserSnippetTemplate", model:attrs.model);
		}
	}

	/**
	 * 
	 */
	def showUserSnippetTablet = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserSnippetTabletTemplate", model:attrs.model);
		}
	}


	/**
	 *
	 */
	def showUserList = { attrs, body->
		if(attrs.model.userInstanceList) {
			out << render(template:"/common/suser/showUserListTemplate", model:attrs.model);
		}
	}

	/**
	 *
	 */
	def showUserListWrapper = { attrs, body->
		out << render(template:"/common/suser/showUserListWrapperTemplate", model:attrs.model);
	}

	/**
	 *
	 */
	def showUserStory = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserStoryTemplate", model:attrs.model);
		}
	}

	/**
	 * 
	 */
	def showUserStoryTablet = { attrs, body->
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showUserStoryTabletTemplate", model:attrs.model);
		}
	}
	/**
	 * 
	 */
	def showDate = {
		if(attrs.model.userInstance) {
			out << render(template:"/common/suser/showDateTemplate", model:attrs.model);
		}
	}

	def isFBUser = { attrs, body->
		if(springSecurityService.getAuthentication() instanceof FacebookAuthToken) {
			out<< body();
		}
	}
	
	def selectUsers = { attrs, body->
		out << render(template:"/common/suser/selectUsersTemplate", model:attrs.model);
	}

	/**
	 * Renders the body if the authenticated user owns this page.
	 */
	def isAdmin = { attrs, body ->
		def user = attrs.model ? attrs.model.user : null;
		user = user?:springSecurityService.getCurrentUser()
		if (utilsService.isAdmin(user)) {
			out << body()
		}
	}
	
	def interestedSpeciesGroups = {attrs, body ->
		out<<render(template:"/common/suser/interestedSpeciesGroupsTemplate", model:attrs.model);
	}

	def interestedHabitats = {attrs, body ->
		out<<render(template:"/common/suser/interestedHabitatsTemplate", model:attrs.model);
	}
	
	def isCEPFAdmin = { attrs, body ->
		def user = attrs.model ? attrs.model.user : null;
		user = user?:springSecurityService.getCurrentUser()
		if (utilsService.isCEPFAdmin(user?.id)) {
			out << body()
		}
	}

 	def ifOwnsDataset = { attrs, body ->
		if (datasetService.hasPermission(attrs.model.dataset, springSecurityService.currentUser)) {
			out << body()
		}
	}
 	
    def ifOwnsDataTable = { attrs, body ->
		if (dataTableService.hasPermission(attrs.model.dataTable, springSecurityService.currentUser)) {
			out << body()
		}
	}
 	
    def hasPermissionForDataset = { attrs, body ->
		if (dataPackageService.hasPermission(attrs.model.dataPackage, springSecurityService.currentUser)) {
			out << body()
		}
	}
}
