package content

import groovy.sql.Sql;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import content.fileManager.UFile;
import content.fileManager.UFileService
class ProjectService {

	static transactional = true
	def uFileService;

	String connectionUrl =  "jdbc:postgresql://localhost/ibp";
	String userName = "postgres";
	String password = "postgres123";


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

	def migrateProjects() {
		def startDate = new Date()
		def sql = Sql.newInstance(connectionUrl, userName, password, "org.postgresql.Driver");
		int i=0;
		sql.eachRow("select nid, vid, title from node where type = 'project' order by nid asc") { row ->
			log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
			try{
				Project project = createProject(row, sql)
			}catch (Exception e) {
				println "=============================== EXCEPTION in create project ======================="
				println " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
				e.printStackTrace()
				println "====================================================================================="
			}
			i++
		}
		println "================= start date " + startDate
		println "================================= finish time " + new Date()
	}

	
	@Transactional
	def Project createProject(nodeRow, Sql sql){

		String query = "select * from content_type_project where nid = $nodeRow.nid and vid = $nodeRow.vid"
		def row = sql.firstRow(query)		
		
		Project proj = new Project();

		proj.title = nodeRow.title;
		proj.summary = row.field_project_summary_value
		
		proj.grantedAmount = row.field_project_amount_value
		proj.granteeName = row.field_grantee_email_email
		proj.granteeURL = row.field_grantee_email_email
		
		proj.grantFrom = new Date()
		proj.grantTo = new Date()
	
		proj.projectProposal = row.field_project_proposal_value
	
		proj.projectReport = row.field_midterm_assessment_value
	
		proj.dataContributionIntensity = row.field_data_contribution_value
	
		proj.analysis = row.field_analysis_results_value
			
		proj.misc = row.field_miscellaneous_value
		
		//Date dateCreated;

		
		

		if(!proj.save(flush:true)){
			proj.errors.allErrors.each { log.error it }
			return null
		}else{

			println " ****************** Project Saved *********************"

			return proj
		}
	}
}
