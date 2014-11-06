package content

import groovy.sql.Sql;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import content.eml.UFile;
import content.eml.Document
import content.eml.Document.DocumentType

import species.License
import species.utils.Utils
import species.auth.SUser
import species.groups.UserGroup
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
//import org.lorecraft.phparser.SerializedPhpParser;

class MigrationService {

    static transactional = false
    def utilsService;
	def grailsApplication
	
	String connectionUrl =  "jdbc:postgresql://localhost/ibp";
	String userName = "postgres";
	String password = "postgres123";
	
	
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	String contentRootDir = config.speciesPortal.content.rootDir

	HashMap directionsMap = new HashMap();

def migrateProjects() {
		println "Migrating projects from drupal to grails"
		def startDate = new Date()
		def sql = Sql.newInstance(connectionUrl, userName, password, "org.postgresql.Driver");
		migrateDirections(sql)
		int i=0
		sql.eachRow("select nid, vid, title, created, changed from node where type = 'project' order by nid asc") { row ->
			log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
			try{
				Project project = createProjectFromRow(row, sql)
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

	def migrateDirections(sql) {

		String query = "select * from node where type like 'strategic_direction'"

		sql.eachRow(query) { row ->
			log.debug "Importing direction :" + row.nid
			def direction = sql.firstRow("select title, body from node_revisions where nid=$row.nid")

			log.debug "Direction Title: "+ direction.title
			log.debug "Direction Body: "+ direction.body

			def sd = new StrategicDirection()
			sd.title = direction.title
			sd.strategy = direction.body
			if(!sd.save(flush:true)) {
				sd.errors.allErrors.each { log.error it }
				return null
			} else {
				directionsMap.put(row.nid,sd.id)
			}
		}
	}


	@Transactional
	def Project createProjectFromRow(nodeRow, Sql sql){

		String query = "select * from content_type_project where nid = $nodeRow.nid and vid = $nodeRow.vid"
		def row = sql.firstRow(query)

		Project proj = new Project();

		proj.title = nodeRow.title;
		proj.summary = row.field_project_summary_value?row.field_project_summary_value:null

		int direction_nid = row.field_strategic_direction_nid

		proj.direction = StrategicDirection.get(directionsMap.get(direction_nid))
		proj.granteeOrganization = row.field_project_grantee_org_value
		proj.granteeContact = row.field_grantee_name_value
		proj.granteeEmail = row.field_grantee_email_email

		proj.grantFrom = parseDBDate(row.field_grantterm_value)
		proj.grantTo = parseDBDate(row.field_grantterm_value2)
		proj.grantedAmount = row.field_project_amount_value?row.field_project_amount_value:0

		proj.projectProposal = row.field_project_proposal_value

		proj.projectReport = row.field_midterm_assessment_value

		// Migrate Data Contribution Intensity to misc
		proj.misc= row.field_data_contribution_value
		
		String projectDir = contentRootDir + "/projects/"+ "project-"+UUID.randomUUID().toString()
		

		proj.proposalFiles = migrateFiles(sql,'content_field_project_proposal_files',nodeRow.nid, 'field_project_proposal_files_fid', 'field_project_proposal_files_data', DocumentType.Proposal, projectDir)
		proj.reportFiles = migrateFiles(sql,'content_field_midterm_assessment_files',nodeRow.nid, 'field_midterm_assessment_files_fid', 'field_midterm_assessment_files_data', DocumentType.Report, projectDir)
		
		proj.miscFiles =  migrateFiles(sql,'content_field_miscellaneous_files',nodeRow.nid, 'field_miscellaneous_files_fid', 'field_miscellaneous_files_data', DocumentType.Miscellaneous, projectDir)
		
		
		//TODO Migrate DataContrib files also to miscFiles
		def dataContribFiles =  migrateFiles(sql,'content_field_data_contribution_files',nodeRow.nid, 'field_data_contribution_files_fid', 'field_data_contribution_files_data', DocumentType.Miscellaneous, projectDir)

		for(dataContribFile in dataContribFiles)
			proj.addToMiscFiles(dataContribFile)
		

		def analysisFiles =  migrateFiles(sql,'content_field_analysis_results_files',nodeRow.nid, 'field_analysis_results_files_fid', 'field_analysis_results_files_data', DocumentType.Miscellaneous, projectDir)
		for(analysisFile in analysisFiles)
			proj.addToMiscFiles(analysisFile)	
		
		//locations
		List locations  = new ArrayList()

		String locationsQuery = "select s.nid as nid, s.field_project_sitename_value as sitename, c.field_project_corridor_value as corridor from content_field_project_sitename s, content_field_project_corridor c where s.nid=c.nid and s.vid=c.vid and s.delta=c.delta and s.vid=$nodeRow.vid";

		sql.eachRow(locationsQuery) { lrow ->
			Location location = new Location()
			
			location.siteName = lrow.sitename?lrow.sitename:null
			location.corridor = lrow.corridor?lrow.corridor:null
			
			if(location.siteName || location.corridor)
				locations.add(location)
		}

		proj.locations = locations


		//proj.analysis = row.field_analysis_results_value

		proj.misc = row.field_miscellaneous_value


		//set author for the project
		//TODO: Get CEPF RIT member id while migration
		proj.author = SUser.get("1107")
		
		changeTimestamping(proj, false)
		proj.dateCreated = getDateFromTimestamp(nodeRow.created)
		proj.lastUpdated = getDateFromTimestamp(nodeRow.changed)


		log.debug " ######## Exporting project: "+ proj.dump()

		
		if(!proj.save(flush:true)){
			proj.errors.allErrors.each { log.error it }
			return null
		}else{
			changeTimestamping(proj, true)
		
			String tagsQuery = "select term_data.name from term_data, term_node where term_data.tid=term_node.tid and term_node.nid=$nodeRow.nid and term_node.vid=$nodeRow.vid"

			def tagsRows = sql.rows(tagsQuery)

			proj.setTags(tagsRows.name)
			println " ****************** Project Saved *********************" + proj

			setSourceToProjectDocuments(proj)

			return proj
		}
	}

  private void changeTimestamping(Object domainObjectInstance, boolean shouldTimestamp) {
        def m = GrailsDomainBinder.getMapping(domainObjectInstance.getClass())
        m.autoTimestamp = shouldTimestamp
    }

	/**
	 * Copy files and metadata from drupal tables to grails
	 * @param sql
	 * @param field_table
	 * @param nid
	 * @param fid_field
	 * @param data_field
	 * @param type
	 * @return
	 */
	def migrateFiles( sql,field_table, nid, fid_field, data_field, type, projectDir) {
		String query = "select $fid_field as fid, $data_field as metadata from " + field_table + " where nid=" +nid
		println query


		List docs = new ArrayList()

		sql.eachRow(query) { row ->
			if(row.fid) {

				def filedata = sql.firstRow("select * from files where fid = $row.fid")

				Document document = new Document()
				
				String copiedFileName = transferProjectFileToGrails(filedata.filepath, projectDir)
				document.uFile = new UFile();
				document.type = type
				//document.title = filedata.filename?filedata.filename:copiedFileName
				document.uFile.path = projectDir.replace(contentRootDir, "") + "/"+copiedFileName 				
				document.uFile.size = filedata.filesize
				document.uFile.mimetype = filedata.filemime

				document.license = License.findByName(License.LicenseType.CC_BY)
				
				document.agreeTerms = true			
				document.author = SUser.get("1107")
				
				def metadata = row.metadata

				
				if(row.metadata) {
					println "metadata is "+ row.metadata

	/*				SerializedPhpParser serializedPhpParser = new SerializedPhpParser(row.metadata);
					Object result = serializedPhpParser.parse();
					
					String title =""
					if(result.description) {
						title = result.description
					} else {
						title = copiedFileName
					}
					document.title = title
					
					document.description = result.shortnote.body
					//before setting tags object should be saved
					if(!document.save(flush:true)){
						document.errors.allErrors.each { log.error it }
						return new Exception()
					}else{
						//get tags list by splitting string by comma and stripping whitespace
						List tags = Arrays.asList(result.tags.body.split("\\s*,\\s*"));

						println "tags of file are "+ tags
						document.setTags(tags)
					}
*/

				}

				log.info "##Document to be added : "+ document.dump()
				docs.add(document)
			}
		}

		return docs.size()>0?docs:null;
	}
	
	/**
	 * In drupal files all project fiels are strored in common directory. Move them to projects directory in grails.
	 * @param filePath
	 * @param projectDir
	 * @return fileName in project folder
	 */
	private String transferProjectFileToGrails(filePath, projectDirPath) {
		log.info "Copying file " + filePath + "to "+ projectDirPath
		int idx = filePath.lastIndexOf("/");
		String fileName = idx >= 0 ? filePath.substring(idx + 1) : filePath;
		
		File projectDir = new File(projectDirPath)
		if(!projectDir.exists())
			projectDir.mkdirs()
		File dst = utilsService.getUniqueFile(projectDir, Utils.generateSafeFileName(fileName));
		
		//TODO : get domain url 
		File src = new File("/data/augmentedmaps/"+filePath)
		if(!src.exists()) {
			log.error " src file "+src.getPath()+" does not exist"
			return "Empty File"
		}	
				
		if(!Utils.copy(src, dst)) {
			log.error "Tranferring project file "+ src.getPath() +" from drupal to " + dst.getName()+ " failed"
			throw new Exception();
		}
		
		return dst.getName()
		
	}

	def setSourceToProjectDocuments(Project proj)
	{

		def userGroup = UserGroup.findByName('The Western Ghats')
		
		for( file in proj.miscFiles) {
			file.setSource(proj)
			file.contributors = proj.granteeOrganization
			file.attribution = proj.granteeOrganization
			
			userGroup.addToDocuments(file);
			
					
			if(!file.save(flush:true)){
				throw new Exception()
			}
		}

		for( file in proj.reportFiles) {
			file.setSource(proj)
			file.contributors = proj.granteeContact
			file.attribution = proj.granteeContact
			
			userGroup.addToDocuments(file);
			
			
			if(!file.save(flush:true)){
				throw new Exception()
			}
		}

		for( file in proj.proposalFiles) {
			file.setSource(proj)
			file.contributors = proj.granteeContact
			file.attribution = proj.granteeContact
			

			userGroup.addToDocuments(file);
			
			if(!file.save(flush:true)){
				throw new Exception()
			}
		}
	}
	
	private Date parseDBDate(date){
		try {			
			return date? Date.parse("yyyy-mm-dd'T'HH:mm:ss", date):null;
		} catch (Exception e) {
			// TODO: handle exception
			print e.toString();
			throw e;
		}
		return null;
	}
	
	private Date getDateFromTimestamp(timestamp) {
		Date date =  new Date(new Long(timestamp)*1000)
		return date
	}
}

