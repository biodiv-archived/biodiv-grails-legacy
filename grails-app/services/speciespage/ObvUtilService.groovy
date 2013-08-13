package speciespage

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.MarkupBuilder;
import groovy.xml.XmlUtil;

import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import species.auth.SUser
import species.groups.SpeciesGroup;
import species.groups.UserGroup
import species.participation.*
import species.participation.RecommendationVote.ConfidenceType
import species.Habitat
import species.utils.Utils;
import species.formatReader.SpreadsheetReader;
import species.utils.ImageType;
import species.utils.ImageUtils

class ObvUtilService {

	static transactional = false

	static final String IMAGE_PATH = "image file"
	static final String IMAGE_FILE_NAMES = "filename"
	static final String SPECIES_GROUP = "group"
	static final String HABITAT = "habitat"
	static final String OBSERVED_ON   = "observed on"
	static final String CN    = "common name"
	static final String SN    = "scientific name"
	static final String LOCATION   = "location title"
	static final String LONGITUDE    = "longitude"
	static final String LATITUDE   = "latitude"
	static final String LICENSE   = "license"
	static final String COMMENT   = "comment"
	static final String NOTES  = "notes"
	static final String USER_GROUPS  = "post to user groups"
	static final String HELP_IDENTIFY  = "help identify?"
	static final String TAGS   = "tags"
	static final String AUTHOR_EMAIL   = "user email"
	static final String AUTHOR_URL   = "user"
	static final String AUTHOR   = "user username"
	static final String AUTHOR_NAME   = "user name"
	//task related
	static final String  SUCCESS = "Success";
	static final String  FAILED = "Failed";
	static final String  SCHEDULED = "Scheduled";
	static final String  EXECUTING = "Executing";
	
	static final int MAX_EXPORT_SIZE = 5000;
	
	
	def userGroupService
	def observationService
	def springSecurityService
	def grailsApplication
	def activityFeedService
	def observationsSearchService
	
	
	
	///////////////////////////////////////////////////////////////////////
	/////////////////////////////// Export ////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	
	
	def requestExport(params){
		log.debug(params)
		log.debug "creating download request"
		DownloadLog.createLog(springSecurityService.currentUser, params.filterUrl, params.downloadType, params.notes, params.source, params)
	}
	
	def export(params, dl){
		log.debug(params)
		def observationInstanceList = getObservationList(params, dl)
		log.debug " Obv total $observationInstanceList.size " 
		return exportObservation(observationInstanceList, dl.type)
	}
	
	
	private getObservationList(params, dl){
		String action = new URL(dl.filterUrl).getPath().split("/")[2]
		if("search".equalsIgnoreCase(action)){
			//getting result from solr
			def idList = observationService.getFilteredObservationsFromSearch(params, MAX_EXPORT_SIZE, 0, false).totalObservationIdList
			def res = []
			idList.each { obvId ->
				res.add(Observation.read(obvId))
			}
			return res
		}else if(params.webaddress){
			def userGroupInstance =	userGroupService.get(params.webaddress)
			if (!userGroupInstance){
				log.error "user group not found for id  $params.id  and webaddress $params.webaddress"
				return []
			}
			return userGroupService.getUserGroupObservations(userGroupInstance, params, MAX_EXPORT_SIZE, 0).observationInstanceList;
		}
		else{
			return observationService.getFilteredObservations(params, MAX_EXPORT_SIZE, 0, false).observationInstanceList
			
		}
	}
	
	private File exportObservation(List obvList, exportType){
		if(! obvList)
			return null
		
		//removing checklist
		def obvWithoutChecklist = []	
		obvList.each { obv ->
			if(!obv.isChecklist)
				obvWithoutChecklist << obv
		}
		obvList = obvWithoutChecklist
		
		if(obvList.isEmpty())
			return null
		
		File downloadDir = new File(grailsApplication.config.speciesPortal.observations.observationDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		log.debug "export type " + exportType 
		if(exportType == DownloadLog.DownloadType.CSV){
			return exportAsCSV(downloadDir, obvList)
		}else{
			return exportAsKML(downloadDir, obvList)
		}
	}
	
	private File exportAsCSV(downloadDir, obvList){
		File csvFile = new File(downloadDir, "obv_" + new Date().getTime() + ".csv")
		CSVWriter writer = getCSVWriter(csvFile.getParent(), csvFile.getName())
		
		boolean headerAdded = false
		obvList.each { obv ->
			log.debug "Writting " + obv
			Map m = obv.fetchExportableValue()
			if(!headerAdded){
				def header = []
				for(entry in m){
					header.add(entry.getKey())
				}
				writer.writeNext(header.toArray(new String[0]))
				headerAdded = true
			}
			writer.writeNext(m.values().toArray(new String[0]))
		}
		writer.flush()
		writer.close()
		
		return csvFile
	}
	
	def CSVWriter getCSVWriter(def directory, def fileName) {
		//char separator = '\t'
		File dir =  new File(directory)
		if(!dir.exists()){
			dir.mkdirs()
		}
		return new CSVWriter(new FileWriter("$directory/$fileName"))//, separator);
	}
	
	def exportAsKML(downloadDir, obvList){
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String iconBasePath = config.speciesPortal.observations.serverURL
		
		def builder = new StreamingMarkupBuilder()
		builder.encoding = 'UTF-8'
		def books = builder.bind {
			mkp.xmlDeclaration()
			namespaces << ['':'http://www.opengis.net/kml/2.2']
			kml() {
				Document(){
					Style(id:"displayName-value") {
						BalloonStyle() {
							text() {
								mkp.yield '''
<a href='$[Observation]' style="height:150px" ><img src='$[Image]' title='$[Species Name]'/></a>
<table>
<tr><td><b>Species Name</b></td><td>$[Species Name]</td></tr>
<tr><td><b>Common Name</b></td><td>$[Common Name]</td></tr>
<tr><td><b><b>Place</b></td><td>$[Place]</td></tr>
<tr><td><b>Observed On</b></td><td>$[Observed on]</td></tr>
<tr><td><b>Species Group</b></b></td><td>$[Species Group]</td></tr>
<tr><td><b>Habitat</td><td>$[Habitat]</td></tr>
<tr><td><b>By</b></td><td><a href='$[AuthorProfile]'>$[Author]</a></td></tr>
<tr><td><b>Posted to User Groups</b></td><td>$[UserGroups]</td></tr>
<tr><td><b>Notes</b></td><td>$[Notes]</td></tr>
</table>
											'''
							}
						}
					
					IconStyle() {
					   color('ff00ff00')
					   colorMode('random')
					   scale('1.1')
					   Icon() {
						  href('http://maps.google.com/mapfiles/kml/pal3/icon21.png')
					   }
					}
					}
					for ( obv in obvList) {
						log.debug "writing $obv"
						def mainImage = obv.mainImage()
						def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null
						def commonName = obv.fetchSuggestedCommonNames()
						def base = grailsApplication.config.speciesPortal.observations.serverURL
						PhotoOverlay() {
							name(obv.fetchSpeciesCall())
							styleUrl('#displayName-value')
//							def snippet = g.render (template:"/common/observation/showObservationSnippetTabletTemplate", model:[observationInstance:obv, 'userGroupWebaddress':params.webaddress])
//							description () {
//								 mkp.yield "$snippet"
//							}
							ExtendedData() {
								Data(name:'Observation') {
									value("${createHardLink('observation', 'show', obv.id)}")
									//value("${userGroupService.userGroupBasedLink(controller:'observation', action:'show', id:obv.id, 'userGroupWebaddress':params.webaddress, absolute:true) }")
								}
								Data(name:'Image') {
									if(imagePath) {
										value("${'' + base + imagePath}")
										//value("${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath, absolute:true)}")
									} else {
										value()
									}
								}
								Data(name:'Species Name') {
									value(obv.fetchSpeciesCall());
								}
								
								Data(name:'Common Name') {
										
										value(commonName)
									
								}
								Data(name:'Place') {
									value(obv.reverseGeocodedName)
								}
								Data(name:'Observed on') {
									value(String.format('%tA %<te %<tB %<ty', obv.fromDate))
								}
								Data(name:'Notes') {
									value() {
										 mkp.yield obv.notes
									}
								}
								Data(name:'Species Group') {
									value(obv.group?.name)
								}
								Data(name:'Habitat') {
									value(obv.habitat?.name)
								}
								Data(name:'Author') {
									value(obv.author.name)
								}
								Data(name:'AuthorProfile') {
									value("${createHardLink('user', 'show', obv.author.id)}")
									//value("${userGroupService.userGroupBasedLink('controller':'SUser', action:'show', id:obv.author.id,  'userGroupWebaddress':params.webaddress, absolute:true)}")
								}
								Data(name:'UserGroups') {
									value(obv.userGroups*.name.join(','))
								}
							}
							Camera() {
								longitude(obv.longitude)
								latitude(obv.latitude)
								altitude(5)
								roll(0)
								altitudeMode('relativeToGround')
							}
							ImagePyramid() {
								maxWidth(512)
								maxHeight(512)
							}
							Icon() {
								href('' + base + imagePath)
								//href(createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath, absolute:true))
							}
							Point() {
								coordinates(obv.longitude+","+obv.latitude)
							}
							Style() {
								IconStyle() {
									Icon() {
										href('http://maps.google.com/mapfiles/ms/micons/green-dot.png')
									}
								}
							}
						}
					}
				}
			}
		}
		File kmlFile = new File(downloadDir, "obv_" + new Date().getTime() + ".kml")
		kmlFile <<  XmlUtil.serialize(books)
		return kmlFile
	}
	
	public static createHardLink(controller, action, id){
		return "" + Utils.getIBPServerDomain() + "/" + controller + "/" + action + "/" + id 
	}

	////////////////////////////////// End export//////////////////////////////
	
	
	
	def batchUpload(request, params){
		/*
		File dir = new File("/home/sandeept/obvUpload")
		dir.listFiles().each { File obvDir ->
			log.debug "Processing Dir ======== $obvDir"
			processBatch(obvDir)
		}
		*/
		processBatch(params)
	}

	private processBatch(params){
		/*
		def spreadSheets =  obvDir.listFiles(new FileFilter(){
					boolean accept(File pathname){
						return pathname.getName().endsWith("xls")
					}
				})
		if(spreadSheets.length == 0){
			log.error "Main file not found. Aborting upload"
			return
		}
		*/
		File imageDir = new File(params.imageDir)
		if(!imageDir.exists()){
			log.error "Image dir not found. Aborting upload"
			return
		}
		
		File spreadSheet = new File(params.batchFileName)
		if(!spreadSheet.exists()){
			log.error "Main batch file not found. Aborting upload"
			return
		}
		
		SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each{ m ->
			uploadObservation(imageDir, m)
		}
	}
	
	private uploadObservation(imageDir, Map m){
		Map obvParams = uploadImageFiles(imageDir, m[IMAGE_FILE_NAMES].trim().split(","), ("cc " + m[LICENSE]).toUpperCase())
		if(obvParams.isEmpty()){
			log.error "No image file .. aborting this obs upload with params " + m
		}else{
			populateParams(obvParams, m)
			saveObv(obvParams)
			log.debug "observation saved"
		}
	}
	
	private populateParams(Map obvParams, Map m){
		
		//mandatory
		obvParams['group_id'] = (SpeciesGroup.findByName(m[SPECIES_GROUP].trim())? SpeciesGroup.findByName(m[SPECIES_GROUP].trim()).id : SpeciesGroup.findByName('Others').id)
		obvParams['habitat_id'] = (Habitat.findByName(m[HABITAT].trim())? Habitat.findByName(m[HABITAT].trim()).id :  Habitat.findByName('Others').id )
		obvParams['longitude'] = (m[LONGITUDE] ?:"76.658279")
		obvParams['latitude'] = (m[LATITUDE] ?: "12.32112")
		obvParams['location_accuracy'] = 'Approximate'
		obvParams['placeName'] = m[LOCATION]
		obvParams['reverse_geocoded_name'] = (m[LOCATION] ?: "National Highway 6, Maharashtra, India")
		
		//reco related
		obvParams['recoName'] = m[SN]
		obvParams['commonName'] = m[CN] 
		obvParams['recoComment'] = m[COMMENT]
		
		//tags, grouplist, notes
		obvParams['notes'] = m[NOTES]
		obvParams['tags'] = (m[TAGS] ? m[TAGS].trim().split(",").collect { it.trim() } : null)
		obvParams['userGroupsList'] = getUserGroupIds(m[USER_GROUPS])
		
		obvParams['fromDate'] = m[OBSERVED_ON]
		
		//author
		obvParams['author'] = SUser.findByEmail(m[AUTHOR_EMAIL].trim())
 	}
	
	private getUserGroupIds(String names){
		if(!names || names.trim() == "")
			return null
		
		List gIds = [] 
		names = names.split(",").each { name -> 
			def ug = UserGroup.findByName(name.trim())
			if(ug){
				gIds.add(ug.id)
			}
		}
		
		if(gIds.isEmpty()){
			return null
		}
		
		return gIds.join(",")
	}
	
	private saveObv(Map params){
		if(!params.author){
			log.error "Author not found for params $params"
			return
		}
		
		def observationInstance;
		try {
			observationInstance =  observationService.createObservation(params);

			if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
				log.debug "Successfully created observation : "+observationInstance

				params.obvId = observationInstance.id
				activityFeedService.addActivityFeed(observationInstance, null, observationInstance.author, activityFeedService.OBSERVATION_CREATED);
				addReco(params, observationInstance)
				println "==============   tags " + params.tags 
				def tags = (params.tags != null) ? new ArrayList(params.tags) : new ArrayList();
				println "==============after    tags " + tags
				observationInstance.setTags(tags);

				if(params.groupsWithSharingNotAllowed) {
					observationService.setUserGroups(observationInstance, [params.groupsWithSharingNotAllowed]);
				}else {
					if(params.userGroupsList) {
						def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
						observationService.setUserGroups(observationInstance, userGroups);
					}
				}
				if(!observationInstance.save(flush:true)){
					observationInstance.errors.allErrors.each { log.error it }
				}
			}else {
					observationInstance.errors.allErrors.each { log.error it }
			}
		}catch(e) {
				log.error "error in creating observation"
				e.printStackTrace();
		}
	}
	
	private addReco(params, Observation observationInstance){
		def recoResultMap = observationService.getRecommendation(params);
		def reco = recoResultMap.mainReco;
		def commonNameReco =  recoResultMap.commonNameReco;
		ConfidenceType confidence = observationService.getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());
		
		def recommendationVoteInstance
		if(reco){
			recommendationVoteInstance = new RecommendationVote(observation:observationInstance, recommendation:reco, commonNameReco:commonNameReco, author:params.author, confidence:confidence); 
		}
		
		if(recommendationVoteInstance && !recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
			log.debug "Successfully added reco vote : "+recommendationVoteInstance
			observationService.addRecoComment(recommendationVoteInstance.recommendation, observationInstance, params.recoComment);
			observationInstance.lastRevised = new Date();
			//saving max voted species name for observation instance
			observationInstance.calculateMaxVotedSpeciesName();
			def activityFeed = activityFeedService.addActivityFeed(observationInstance, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED);
		}
		try{
			observationsSearchService.publishSearchIndex(observationInstance, ObservationController.COMMIT);
		}catch (Exception e) {
			log.error "Error in publishing ===== "
		}	
	}

	private uploadImageFiles(imageDir, imagePaths, license){
		def resourcesInfo = [:];
		def rootDir = grailsApplication.config.speciesPortal.observations.rootDir
		File obvDir
		int index = 1
		imagePaths.each{ path ->
			File f = new File(imageDir, path.trim())
			if(f.exists()){
				log.debug "uploading file $f"
				if(f.length()  > grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE) {
					log.debug 'File size cannot exceed ${104857600/1024}KB'
				}else if(f.length() == 0) {
					log.debug 'File cannot be empty'
				}else {
					if(!obvDir) {

						obvDir = new File(rootDir);
						if(!obvDir.exists()) {
							obvDir.mkdir();
						}
						obvDir = new File(obvDir, UUID.randomUUID().toString());
						obvDir.mkdir();
					}

					File file = observationService.getUniqueFile(obvDir, Utils.generateSafeFileName(f.getName()));
					file << f.bytes
					ImageUtils.createScaledImages(file, obvDir);
					resourcesInfo.put("file_" + index, file.getAbsolutePath().replace(rootDir, ""))
					resourcesInfo.put("license_" + index, license)
					resourcesInfo.put("type_" + index, "image")
					index++
				}
			}
		}
		log.debug resourcesInfo
		return resourcesInfo
	}
	
	

}
