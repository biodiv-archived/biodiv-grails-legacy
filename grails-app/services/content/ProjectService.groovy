package content

import groovy.sql.Sql;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import content.fileManager.UFile;
import content.fileManager.UFileService

//import de.ailis.pherialize
import org.lorecraft.phparser.SerializedPhpParser;

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
				//Project project = createProject(row, sql)
				migrateFiles(sql,'content_field_project_proposal_files',row.nid, 'field_project_proposal_files_fid', 'field_project_proposal_files_data')
				migrateFiles(sql,'content_field_data_contribution_files',row.nid, 'field_data_contribution_files_fid', 'field_data_contribution_files_data')
				migrateFiles(sql,'content_field_miscellaneous_files',row.nid, 'field_data_contribution_files_fid', 'field_data_contribution_files_data')
				migrateFiles(sql,'content_field_midterm_assessment_files',row.nid, 'field_midterm_assessment_files_fid', 'field_midterm_assessment_files_data')

			}catch (Exception e) {
				println "=============================== EXCEPTION in create project ======================="
				println " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
				e.printStackTrace()
				println "====================================================================================="
				throw new Exception()
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

		int direction_nid = row.field_strategic_direction_nid

		proj.granteeOrganization = row.field_project_grantee_org_value
		proj.granteeContact = row.field_grantee_name_value
		proj.granteeEmail = row.field_grantee_email_email

		proj.grantFrom = row.field_grantterm_value
		proj.grantTo = row.field_grantterm_value2
		proj.grantedAmount = row.field_project_amount_value

		proj.projectProposal = row.field_project_proposal_value

		proj.projectReport = row.field_midterm_assessment_value

		proj.dataContributionIntensity = row.field_data_contribution_value

		//proj.analysis = row.field_analysis_results_value

		proj.misc = row.field_miscellaneous_value

		if(!proj.save(flush:true)){
			proj.errors.allErrors.each { log.error it }
			return null
		}else{

			println " ****************** Project Saved *********************"

			return proj
		}
	}

	def migrateFiles( sql,field_table, nid, fid_field, data_field) {
		String query = "select $fid_field as fid, $data_field as metadata from " + field_table + " where nid=" +nid
		println query

		def files

		sql.eachRow(query) { row ->
			if(row.fid) {

				def filedata = sql.firstRow("select * from files where fid = $row.fid")

				UFile file = new UFile();

				file.name = filedata.filename
				file.path = filedata.filepath
				file.size = filedata.filesize
				file.mimetype = filedata.filemime

				def metadata = row.metadata

				if(row.metadata) {
					println "metadata is "+ row.metadata

					SerializedPhpParser serializedPhpParser = new SerializedPhpParser(row.metadata);

					Object result = serializedPhpParser.parse();


					file.name = result.description;
					file.description = result.shortnote.body
					//file.setTags(result.tags.body)

				}
			}
		}


	}
}
