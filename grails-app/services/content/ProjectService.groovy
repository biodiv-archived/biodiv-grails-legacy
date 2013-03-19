package content

import java.util.Date;
import java.util.List;

import content.fileManager.UFile;
import content.fileManager.UFileService
class ProjectService {

    static transactional = true
	def uFileService;

    def createProject(params) {
		
		//def projectInstance = new Project(params)
		//uFileService = new UFileService()
		//def uFiles = uFileService.saveUFiles(params)
		

		
		def projectParams = params
		projectParams.grantFrom = parseDate(params.grantFrom)
		projectParams.grantTo = parseDate(params.grantTo)
		
		log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		log.debug projectParams
		
		def projectInstance = new Project(projectParams)
		
		
		/*
		projectInstance.title = params.title
		projectInstance.granteeURL = params.granteeURL
		projectInstance.granteeName = params.granteeName
	
		projectInstance.grantFrom = parseDate(params.granteeFrom)
		projectInstance.grantTo = parseDate(params.granteeTo)
		projectInstance.grantedAmount = params.int('grantedAmount')
		projectInstance.projectProposal = params.proposal
	
		projectInstance.projectReport = params.report

		projectInstance.dataContributionIntensity = params.dataContributionIntensity
	
		projectInstance.analysis = params.analysis
			
		projectInstance.misc = params.misc
			*/
		 
		
		return projectInstance;
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
