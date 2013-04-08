package content


import org.springframework.transaction.annotation.Transactional;

import content.fileManager.UFile;


class ProjectService {

	static transactional = true

	def createProject(params) {


		def projectInstance = new Project()

		updateProject(params, projectInstance)

		return projectInstance;
	}

	def updateProject(params, Project project) {
		log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		log.debug params

		def projectParams = params
		projectParams.grantFrom = parseDate(params.grantFrom)
		projectParams.grantTo = parseDate(params.grantTo)

		project.properties = projectParams
		
		// delete Locations that are marked for removal
		def _toBeDeletedLocations = project.locations.findAll{it?.deleted || !it}
		
		if(_toBeDeletedLocations) {
			project.locations.removeAll(_toBeDeletedLocations)
		}
		
		// delete DataLinks that are marked for removal
		def _toBeDeletedDataLinks = project.dataLinks.findAll{it?.deleted || !it}
		
		if(_toBeDeletedDataLinks) {
			project.DataLinks.removeAll(_toBeDeletedDataLinks)
		}
		
		//remove ufiles that are marked for delete

		
	}

	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
			print e.toString();
		}
		return null;
	}




	
}