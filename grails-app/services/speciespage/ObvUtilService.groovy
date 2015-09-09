package speciespage

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.MarkupBuilder;
import groovy.xml.XmlUtil;

import java.util.Map;
import java.io.File ;

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
import species.ResourceFetcher;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import content.eml.Coverage;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import species.sourcehandler.exporter.DwCObservationExporter; 
import species.sourcehandler.exporter.DwCSpeciesExporter
import org.codehaus.groovy.grails.web.json.JSONObject;
import grails.converters.JSON;
import org.springframework.context.i18n.LocaleContextHolder as LCH;

class ObvUtilService {

	static transactional = false

	static final String IMAGE_PATH = "image file"
	static final String IMAGE_FILE_NAMES = "filename"
	static final String SPECIES_GROUP = "group"
	static final String HABITAT = "habitat"
	static final String OBSERVED_ON   = "observed on"
	static final String CN    = "common name"
	static final String LANGUAGE    = "language"
	static final String SN    = "scientific name"
	static final String GEO_PRIVACY   = "Geoprivacy enabled"
	static final String LOCATION   = "location title"
	static final String LOCATION_SCALE   = "location scale"
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
	static final String AUTHOR_NAME   = "user name"
	static final String OBSERVATION_ID   = "id"
	static final String CREATED_ON   = "created on"
	static final String UPDATED_ON   = "updated on"
	static final String OBSERVATION_URL   = "observation url"
	static final String NUM_IDENTIFICATION_AGREEMENT   = "no. of identification agreements"
	static final String NUM_IDENTIFICATION_DISAGREEMENT   = "no. of identification disagreements"
	
	//task related
	static final String  SUCCESS = "Success";
	static final String  FAILED = "Failed";
	static final String  SCHEDULED = "Scheduled";
	static final String  EXECUTING = "Executing";
	
	private static final int BATCH_SIZE = 50

    def utilsService
	def userGroupService
	def observationService
	def springSecurityService
	def grailsApplication
	def activityFeedService
	def observationsSearchService
	def commentService
    def messageSource;

	///////////////////////////////////////////////////////////////////////
	/////////////////////////////// Export ////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	
	private final static int EXPORT_BATCH_SIZE = 5000;

	def requestExport(params){
        log.debug "creating download request"
        DownloadLog dl;
        def res = [];
        int instanceTotal = params.instanceTotal?params.int('instanceTotal'):0
        for( int i=0; i<instanceTotal/EXPORT_BATCH_SIZE; i++) {
            int offset = (i * EXPORT_BATCH_SIZE);
            dl = DownloadLog.createLog(springSecurityService.currentUser, params.filterUrl, params.downloadType, params.notes, params.source, params, offset);

            def r = [:];
            r['offset'] = offset;
            if(!dl.hasErrors()) {
                r['success'] = true;
                r['msg']= messageSource.getMessage('observation.download.requsted',null,'Processing... You will be notified by email when it is completed. Login and check your user profile for download link.', LCH.getLocale())
            } else {
                r['success'] = false;
                r['msg'] = 'Error in creating download log.' 
                def errors = [];
                dl.errors.allErrors.each {
                    def formattedMessage = messageSource.getMessage(it, LCH.getLocale());
                    errors << ['field': it.field, 'message': formattedMessage]
                }
                r['errors'] = errors 
                println dl.errors.allErrors
            }
            res << r;
        }
        return res;
	}
	
	def export(params, dl){
        if(params.downloadFrom && params.downloadFrom == 'uniqueSpecies') {
            def max = Math.min(params.max ? params.int('max') : 10, 100)
            def offset = params.offset ? params.int('offset') : 0
            //TODO: distinct reco can be from search also - not handled (ref - obvCont -line -1610)
            def distinctRecoListResult = observationService.getDistinctRecoList(params, BATCH_SIZE, offset);
            def totalRecoCount = distinctRecoListResult.totalCount;
            def completeResult = distinctRecoListResult.distinctRecoList;
            offset = offset + BATCH_SIZE
            while(offset <= totalRecoCount) {
                Observation.withNewTransaction {
                    completeResult.addAll(observationService.getDistinctRecoList(params, BATCH_SIZE, offset).distinctRecoList);
                    offset = offset + BATCH_SIZE
                }
            }
            distinctRecoListResult.distinctRecoListResult = completeResult;
            return exportUniqueSpeciesList(distinctRecoListResult); 
        }else {
//            def observationInstanceList = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, params.webaddress, dl.offset).getAllResult()
            return exportObservation(dl);
//            return exportObservation(observationInstanceList, dl.type, dl.author, dl.id, dl.filterUrl )
            //return DwCObservationExporter.getInstance().exportObservationData(downloadDir, list_final, reqUser, dl.id, params.filterUrl );

        }
    }
	
    private File exportUniqueSpeciesList(Map distinctRecoListResult) {
        if(!distinctRecoListResult) {
            return null;
        } 
        if(distinctRecoListResult.totalCount == 0) {
            return null;
        }
        File downloadDir = new File(grailsApplication.config.speciesPortal.observations.observationDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		} 
		return exportDistinctRecoAsCSV(downloadDir, distinctRecoListResult);
    }
	
    private File exportDistinctRecoAsCSV(downloadDir, Map distinctRecoListResult){
        File csvFile = new File(downloadDir, "UniqueSpecies_" + new Date().getTime() + ".csv")
        CSVWriter writer = getCSVWriter(csvFile.getParent(), csvFile.getName())
        
        def header = ['Species Name', 'Count', 'URL'];
        writer.writeNext(header.toArray(new String[0]))
        def distinctRecoList = distinctRecoListResult.distinctRecoList;
        def dataToWrite = []
        distinctRecoList.each {
            log.debug "Writting " + it
            def temp = []
            temp.add("" + it[0]);
            temp.add("" + it[2]);
            temp.add("" + it[3]);
            dataToWrite.add(temp.toArray(new String[0]))
        }
        writer.writeAll(dataToWrite);
        writer.flush()
        writer.close()

        return csvFile
    }
	
    private def exportObservation(DownloadLog dl){
    	if(!dl)
			return null
		
		File downloadDir = new File(grailsApplication.config.speciesPortal.observations.observationDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		log.debug "export type " + dl.type 
		if(dl.type == DownloadLog.DownloadType.CSV) {
			return exportAsCSV(downloadDir, dl)
		} else if(dl.type == DownloadLog.DownloadType.KML) {
			return exportAsKML(downloadDir, dl)
		} else {
			return exportAsDW(downloadDir, dl)
		}
    }

/*    private def exportObservation(List obvList, exportType, reqUser, dl_id, params_filterUrl ){
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
			return exportAsCSV(downloadDir, obvList, reqUser, dl_id , params_filterUrl)
		}else if(exportType == DownloadLog.DownloadType.KML) {
			return exportAsKML(downloadDir, obvList, reqUser, dl_id, params_filterUrl)
		} else {
			return exportAsDW(downloadDir, obvList, reqUser, dl_id, params_filterUrl)
		}
	}
*/
	private File exportAsCSV(File downloadDir, DownloadLog dl){
		String folderName = "obv_"+ + new Date().getTime()
		String parent_dir = downloadDir.getAbsolutePath() + File.separator + folderName+File.separator + folderName
		File csvFile = new File(parent_dir, "obv_" + new Date().getTime() + ".csv")
		
		CSVWriter writer = getCSVWriter(csvFile.getParent(), csvFile.getName())
		
		boolean headerAdded = false
        ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, null, dl.offsetParam.intValue());
        int total = 0;
        while (total < EXPORT_BATCH_SIZE && rf.hasNext()) {
            def obvList = rf.next();
            total += obvList.size();
            obvList.each { obv ->
			    if(obv.isChecklist) return;

                log.debug "Writting " + obv
                Map m = obv.fetchExportableValue(dl.author)
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
        }
		writer.flush()
		writer.close()

		File f = DwCObservationExporter.getInstance().returnMetaData_EML(parent_dir, dl.author, dl.id, dl.filterUrl)
		return archive(downloadDir.getAbsolutePath(), folderName, csvFile, f )
		//return csvFile
	}
/*	
	private File exportAsCSV(downloadDir, obvList, reqUser, dl_id , params_filterUrl){
		String folderName = "obv_"+ + new Date().getTime()
		String parent_dir=downloadDir.getAbsolutePath() +File.separator+folderName+File.separator+ folderName
		File csvFile = new File(parent_dir, "obv_" + new Date().getTime() + ".csv")
		
		CSVWriter writer = getCSVWriter(csvFile.getParent(), csvFile.getName())
		
		boolean headerAdded = false
		obvList.each { obv ->
			log.debug "Writting " + obv
			Map m = obv.fetchExportableValue(reqUser)
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

		File f=DwCObservationExporter.getInstance().returnMetaData_EML(parent_dir,reqUser , dl_id , params_filterUrl)
		return archive(downloadDir.getAbsolutePath(), folderName, csvFile, f )
		//return csvFile
	}
*/
    def  archive(directory, folderName,  file_name1,  file_name2 ) {
        String f = File.separator

        File folder= new File(folderName)
        if(!folder.exists()){
            folder.mkdirs()
        }

        def HOME = directory + f + folderName

        def zipFile = new File(HOME + ".zip")
        def deploymentFiles = [ folderName + f + file_name1.getName(), folderName+f+file_name2.getName() ]
        new AntBuilder().zip( basedir: HOME,
        destFile: zipFile.absolutePath,
        includes: deploymentFiles.join( ' ' ))

        return zipFile
    }

    def exportAsDW(File downloadDir, DownloadLog dl){
        List<String> list_final=[] ;

/*        ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, null, 0);
        while(rf.hasNext()) {
            def obvList = rf.next(); 
            obvList.each {
                def it_observation = it
                def next = it_observation as JSON ; 
                def it_final = JSON.parse(""+next)
                list_final.add(it_final)
            }
        }
*/
        DwCObservationExporter.getInstance().exportObservationData(downloadDir.getAbsolutePath(), dl);
        //DwCSpeciesExporter.getInstance().exportSpecieData(downloadDir, list_final, reqUser , dl_id , params_filterUrl) 
    }

    def CSVWriter getCSVWriter(def directory, def fileName) {
        //char separator = '\t'
        File dir =  new File(directory)
        if(!dir.exists()){
            dir.mkdirs()
        }
        return new CSVWriter(new FileWriter("$directory/$fileName")) //, separator );
    }

/*    def exportAsDW__specie(downloadDir, obvList, reqUser, dl_id, params_filterUrl){
        List<String> list_final=[] ;
        //File dwFile = new File(downloadDir, "obv_" + new Date().getTime() + ".dw")
        //CSVWriter writer = getCSVWriter(dwFile.getParent(), dwFile.getName())

        obvList.each {
            def it_observation =it
            def next = it_observation as JSON ; 
            def it_final=JSON.parse(""+next)
            list_final.add(it_final)
        }

        DwCObservationExporter.getInstance().exportObservationData(downloadDir.getAbsolutePath(), list_final, reqUser, dl_id, params_filterUrl );
        //DwCSpeciesExporter.getInstance().exportSpecieData(downloadDir, list_final, reqUser , dl_id , params_filterUrl) 
    }
*/	
    def exportAsKML(File downloadDir, DownloadLog dl){
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

                    ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, null, dl.offsetParam.intValue());
                    int total = 0;
                    while(rf.hasNext() && total < EXPORT_BATCH_SIZE) {
                        def obvList = rf.next();
                        total += obvList.size();
                        for ( obv in obvList) {
                            log.debug "writing $obv"
                            def mainImage = obv.mainImage()
                            def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null
                            def commonName = obv.fetchSuggestedCommonNames()
                            def base = grailsApplication.config.speciesPortal.observations.serverURL
                            def geoPrivacyAdjust = obv.fetchGeoPrivacyAdjustment(dl.author)
                            def obvLongitude = obv.longitude + geoPrivacyAdjust
                            def obvLatitude = obv.latitude + geoPrivacyAdjust
                            PhotoOverlay() {
                                name(obv.fetchSpeciesCall())
                                styleUrl('#displayName-value')
                                //							def snippet = g.render (template:"/common/observation/showObservationSnippetTabletTemplate", model:[observationInstance:obv, 'userGroupWebaddress':params.webaddress])
                                //							description () {
                                //								 mkp.yield "$snippet"
                                //							}
                                ExtendedData() {
                                    Data(name:'Observation') {
                                        value("${utilsService.createHardLink('observation', 'show', obv.id)}")
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
                                        value("${utilsService.createHardLink('user', 'show', obv.author.id)}")
                                        //value("${userGroupService.userGroupBasedLink('controller':'SUser', action:'show', id:obv.author.id,  'userGroupWebaddress':params.webaddress, absolute:true)}")
                                    }
                                    Data(name:'UserGroups') {
                                        value(obv.userGroups*.name.join(','))
                                    }
                                    Data(name:'Geoprivacy enabled') {
                                        value("" + obv.geoPrivacy)
                                    }
                                }
                                Camera() {
                                    longitude(obvLongitude)
                                    latitude(obvLatitude)
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
                                    coordinates(obvLongitude+","+obvLatitude)
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
        }


        String  file_name = "obv_" + new Date().getTime() + ".kml"
        String folderName = "obv_kml" + new Date().getTime()
        String parent_dir = downloadDir.getAbsolutePath() + File.separator+folderName + File.separator + folderName

        File dir =  new File(parent_dir)
        if(!dir.exists()){
            dir.mkdirs()
        }

        File kmlFile = new File ( dir,  file_name )
        if(!kmlFile.exists()){
            kmlFile.createNewFile()
        }

        kmlFile <<  XmlUtil.serialize(books)

        File eml = DwCObservationExporter.getInstance().returnMetaData_EML(parent_dir, dl.author, dl.id, dl.filterUrl)

        return archive(downloadDir.getAbsolutePath(), folderName, kmlFile, eml)
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
		
		def resultObv = [] 
		int i = 0;
		SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each{ m ->
			if(m[IMAGE_FILE_NAMES].trim() != ""){
				uploadObservation(imageDir, m,resultObv)
				i++
				if(i > BATCH_SIZE){
					utilsService.cleanUpGorm(true)
					def obvs = resultObv.collect { Observation.read(it) }
					try{
						observationsSearchService.publishSearchIndex(obvs, true);
					}catch (Exception e) {
						log.error e.printStackTrace();
					}
					resultObv.clear();
					i = 0;
				}
			}
		}
	}
	
	private uploadObservation(imageDir, Map m, resultObv){
		Map obvParams = uploadImageFiles(imageDir, m[IMAGE_FILE_NAMES].trim().split(","), ("cc " + m[LICENSE]).toUpperCase())
		if(obvParams.isEmpty()){
			log.error "No image file .. aborting this obs upload with params " + m
		}else{
			populateParams(obvParams, m)
			saveObv(obvParams, resultObv)
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
		obvParams['locationScale'] = m[LOCATION_SCALE]
		obvParams['placeName'] = m[LOCATION]
		obvParams['reverse_geocoded_name'] = (m[LOCATION] ?: "National Highway 6, Maharashtra, India")
		
		//reco related
		obvParams['recoName'] = m[SN]
		obvParams['commonName'] = m[CN] 
		obvParams['languageName'] = m[LANGUAGE]
		obvParams['recoComment'] = m[COMMENT]
		
		//tags, grouplist, notes
		obvParams['notes'] = m[NOTES]

		obvParams['tags'] = (m[TAGS] ? m[TAGS].trim().split(",").collect { it.trim() } : null)
		obvParams['userGroupsList'] = getUserGroupIds(m[USER_GROUPS])
		
		obvParams['fromDate'] = m[OBSERVED_ON]
		
		//author
		obvParams['author'] = SUser.findByEmail(m[AUTHOR_EMAIL].trim())
		
		obvParams['geoPrivacy'] = m["geoprivacy"]

        obvParams['resourceListType'] = "ofObv"

		obvParams['agreeTerms'] = "on"
        
        obvParams['locale_language'] = utilsService.getCurrentLanguage();
		
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        if(obvParams.latitude && obvParams.longitude) {
            obvParams.areas = Utils.GeometryAsWKT(geometryFactory.createPoint(new Coordinate(obvParams.longitude?.toFloat(), obvParams.latitude?.toFloat())));
        } 
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
	
	private saveObv(Map params, result){
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
				def tags = (params.tags != null) ? new ArrayList(params.tags) : new ArrayList();
				observationInstance.setTags(tags);

				if(params.groupsWithSharingNotAllowed) {
					observationService.setUserGroups(observationInstance, [params.groupsWithSharingNotAllowed], false);
				}else {
					if(params.userGroupsList) {
						def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
						observationService.setUserGroups(observationInstance, userGroups, false);
					}
				}
				if(!observationInstance.save(flush:true)){
					observationInstance.errors.allErrors.each { log.error it }
				}
				result.add(observationInstance.id)
				
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
			log.debug "Successfully added reco vote : " + recommendationVoteInstance
			commentService.addRecoComment(recommendationVoteInstance.recommendation, observationInstance, params.recoComment, params.author);
			observationInstance.lastRevised = new Date();
			//saving max voted species name for observation instance
			observationInstance.calculateMaxVotedSpeciesName();
			def activityFeed = activityFeedService.addActivityFeed(observationInstance, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED);
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

					File file = utilsService.getUniqueFile(obvDir, Utils.generateSafeFileName(f.getName()));
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
