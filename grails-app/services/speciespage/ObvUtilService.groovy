package speciespage

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.MarkupBuilder;
import groovy.xml.XmlUtil;

import java.util.Map;
import java.io.File ;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.log4j.Level;
import species.sourcehandler.XMLConverter;
import species.sourcehandler.importer.AbstractObservationImporter;
import species.sourcehandler.importer.FileObservationImporter;
import species.sourcehandler.importer.DwCObservationImporter;
import species.dataset.DataTable;
import species.participation.UploadLog

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
import species.participation.Observation.BasisOfRecord;
import species.participation.Observation.ProtocolType;
import species.License;
import species.License.LicenseType;
import species.Metadata.DateAccuracy;
import species.participation.Flag;
import species.participation.Flag.FlagType;
import species.NamesParser;
import groovy.sql.Sql
import species.dataset.Dataset;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import content.eml.UFile;

class ObvUtilService {

	static transactional = false
    public static final int IMPORT_BATCH_SIZE = 50;

	static final String IMAGE_PATH = "image file"
	static final String IMAGE_FILE_NAMES = "filename"
	static final String SPECIES_GROUP = "group"
	static final String HABITAT = "habitat"
	static final String OBSERVED_ON   = "observed on"
	static final String TO_DATE   = "to date"
	static final String DATE_ACCURACY   = "date accuracy"
	static final String CN    = "common name"
	static final String LANGUAGE    = "language"
	static final String SN    = "scientific name"
	static final String GEO_PRIVACY   = "Geoprivacy enabled"
	static final String LOCATION   = "location title"
	static final String LOCATION_SCALE   = "location scale"
	static final String LONGITUDE    = "longitude"
	static final String LATITUDE   = "latitude"
	static final String TOPOLOGY   = "topology"
	static final String LICENSE   = "license"
	static final String COMMENT   = "comment"
	static final String NOTES  = "notes"
	static final String USER_GROUPS  = "post to user groups"
	static final String HELP_IDENTIFY  = "help identify?"
	static final String TAGS   = "tags"
	static final String AUTHOR_EMAIL   = "user email"
	static final String AUTHOR_URL   = "user"
	static final String AUTHOR_NAME   = "user name"
	static final String ORIGINAL_AUTHOR_NAME   = "original user name"
	static final String OBSERVATION_ID   = "id"
	static final String CREATED_ON   = "created on"
	static final String UPDATED_ON   = "updated on"
	static final String OBSERVATION_URL   = "observation url"
	static final String EXTERNAL_ID   = "externalId"
	static final String NUM_IDENTIFICATION_AGREEMENT   = "no. of identification agreements"
	static final String NUM_IDENTIFICATION_DISAGREEMENT   = "no. of identification disagreements"
	static final String IDENTIFIED_BY   = "identifiedBy"
	static final String DATE_IDENTIFIED   = "dateIdentified"
	static final String COLLECTION_ID   = "collectionId"
	static final String COLLECTION_CODE   = "collectionCode"
	static final String BASISOFRECORD   = "basisOfRecord"
	static final String PROTOCOL   = "protocol"
	static final String EXTERNAL_DATASET_KEY   = "externalDatasetKey"
	static final String LAST_CRAWLED   = "lastCrawled"
	static final String CATALOG_NUMBER   = "catalogNumber"
	static final String PUBLISHING_COUNTRY   = "publishingCountry"
	static final String ACCESS_RIGHTS = "accessRights"
	static final String INFORMATION_WITHHELD   = "informationWitheld"
 
	//task related
	static final String  SUCCESS = "Success";
	static final String  FAILED = "Failed";
	static final String  SCHEDULED = "Scheduled";
	static final String  EXECUTING = "Executing";
	
	private static final int BATCH_SIZE = 50

    def utilsService
	def observationService
	def springSecurityService
	def grailsApplication
	def activityFeedService
	def observationsSearchService
	def commentService
    def messageSource;
    def sessionFactory;
    def factService;
    def userGroupService;

    SpeciesGroup defaultSpeciesGroup;
    Habitat defaultHabitat;

	///////////////////////////////////////////////////////////////////////
	/////////////////////////////// Export ////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	
	private final static int EXPORT_BATCH_SIZE = 5000;
	private final static int MAX_BATCHES = 1;
	
	
	def requestExport(params){
        log.debug "creating download request"
        DownloadLog dl;
        def res = [];
        int instanceTotal = params.instanceTotal?params.int('instanceTotal'):0

        int x = instanceTotal/EXPORT_BATCH_SIZE;

        if(x == 0 || (params.downloadFrom && params.downloadFrom == 'uniqueSpecies')) {
            x = 1;
        }

		x = Math.min(x, MAX_BATCHES)
			
        for( int i=0; i<x; i++) {
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
            return exportObservation(dl, params.webaddress);
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
	
    private def exportObservation(DownloadLog dl, String userGroupWebaddress){
    	if(!dl)
			return null
		
		File downloadDir = new File(grailsApplication.config.speciesPortal.observations.observationDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		log.debug "export type " + dl.type 
		if(dl.type == DownloadLog.DownloadType.CSV) {
			return exportAsCSV(downloadDir, dl, userGroupWebaddress)
		} else if(dl.type == DownloadLog.DownloadType.KML) {
			return exportAsKML(downloadDir, dl, userGroupWebaddress)
		} else {
			return exportAsDW(downloadDir, dl, userGroupWebaddress)
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
	private File exportAsCSV(File downloadDir, DownloadLog dl, String userGroupWebaddress){
		String folderName = "obv_"+ + new Date().getTime()
		String parent_dir = downloadDir.getAbsolutePath() + File.separator + folderName+File.separator + folderName
		File csvFile = new File(parent_dir, "obv_" + new Date().getTime() + ".csv")
		
		CSVWriter writer = getCSVWriter(csvFile.getParent(), csvFile.getName())
		
		boolean headerAdded = false
        ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, userGroupWebaddress, dl.offsetParam.intValue());
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

    def exportAsDW(File downloadDir, DownloadLog dl, String userGroupWebaddress){
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
        DwCObservationExporter.getInstance().exportObservationData(downloadDir.getAbsolutePath(), dl, userGroupWebaddress);
        //DwCSpeciesExporter.getInstance().exportSpecieData(downloadDir, list_final, reqUser , dl_id , params_filterUrl) 
    }

    def CSVWriter getCSVWriter(def directory, def fileName) {
        return utilsService.getCSVWriter(directory, fileName);
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
    def exportAsKML(File downloadDir, DownloadLog dl, String userGroupWebaddress){
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

                    ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, userGroupWebaddress, dl.offsetParam.intValue());
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
		processBatch(params)
	}

	def processBatch(params){
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
		
		List resultObv = [] 
		int i = 0;
		SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each{ m ->
			if(m[IMAGE_FILE_NAMES].trim() != ""){
                println "Uploading obv for image file " + m[IMAGE_FILE_NAMES].trim()
				if(uploadObservation(imageDir, m, resultObv)){
				    i++
				    if(i > BATCH_SIZE){
					   publishAndGormcleanup(resultObv)
					   i = 0;
				    }
                }
			}
		}
		//last batch
		publishAndGormcleanup(resultObv)
	}
	
	private publishAndGormcleanup(List resultObv){
		utilsService.cleanUpGorm(true)
		def obvs = resultObv.collect { Observation.read(it) }
		try {
			observationsSearchService.publishSearchIndex(obvs, true);
		} catch (Exception e) {
			log.error e.printStackTrace();
		}
		resultObv.clear();
	}
	
	boolean uploadObservation(imageDir, Map m, List resultObv, File uploadLog=null, ProtocolType protocolType = ProtocolType.MULTI_OBSERVATION) {
        Map obvParams = [:];
        boolean result;

        if(!defaultSpeciesGroup) defaultSpeciesGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS);
        if(!defaultHabitat) defaultHabitat = Habitat.findByName(grailsApplication.config.speciesPortal.group.OTHERS);

        utilsService.benchmark ('uploadObv') {
            utilsService.benchmark ('uploadImage') {
                if(m[IMAGE_FILE_NAMES]) {
                    obvParams = uploadImageFiles(imageDir, m[IMAGE_FILE_NAMES].trim().split(","), ("cc " + m[LICENSE]).toUpperCase(), SUser.findByEmail(m[AUTHOR_EMAIL].trim()))
                }
            }

            if(!obvParams && !(protocolType == ProtocolType.BULK_UPLOAD || protocolType == ProtocolType.LIST)){
              log.error "No image file .. aborting this obs upload with params " + m
              return false
            }

            utilsService.benchmark ('populateParams') {
                populateParams(obvParams, m, protocolType)
            }


            log.debug "Populated Obv Params ${obvParams}"
            utilsService.benchmark('saveObv') {
                result = saveObv(obvParams, resultObv, uploadLog, true, protocolType);
            }
        }
        return result;
	}
	
	private void populateParams(Map obvParams, Map m, ProtocolType protocolType){
		
		//mandatory
        SpeciesGroup sG = m[SPECIES_GROUP] ? SpeciesGroup.findByName(m[SPECIES_GROUP].trim()) : null
		obvParams['group_id'] = m[SPECIES_GROUP]?(sG ? sG.id : defaultSpeciesGroup.id):defaultSpeciesGroup.id
        Habitat h = m[HABITAT] ? Habitat.findByName(m[HABITAT].trim()) : null;
        obvParams['habitat_id'] = m[HABITAT] ? ( h ? h.id :  defaultHabitat.id ): defaultHabitat.id 
		obvParams['longitude'] = m[LONGITUDE]
		obvParams['latitude'] = m[LATITUDE]
		obvParams['location_accuracy'] = 'Approximate'
		obvParams['locationScale'] = m[LOCATION_SCALE]
		obvParams['placeName'] = m[LOCATION]
		obvParams['reverse_geocoded_name'] = m[LOCATION]
		obvParams['topology'] = m[TOPOLOGY];
		//reco related
		obvParams['recoName'] = m[SN]
		obvParams['commonName'] = m[CN] 
		obvParams['languageName'] = m[LANGUAGE]
		obvParams['recoComment'] = m[COMMENT]
		obvParams['identifiedBy'] = m[IDENTIFIED_BY]
		obvParams['dateIdentified'] = m[DATE_IDENTIFIED]
		
		//tags, grouplist, notes
		obvParams['notes'] = m[NOTES]

		obvParams['tags'] = (m[TAGS] ? m[TAGS].trim().split(",").collect { it.trim() } : null)
		obvParams['userGroupsList'] = getUserGroupIds(m[USER_GROUPS])
		
		obvParams['fromDate'] = m[OBSERVED_ON]
		obvParams['toDate'] = m[TO_DATE]
		obvParams['dateAccuracy'] = m[DATE_ACCURACY]?:(m[AbstractObservationImporter.ANNOTATION_HEADER]?m[AbstractObservationImporter.ANNOTATION_HEADER][DATE_ACCURACY]:null)
		//author
        if(m[AUTHOR_EMAIL]) {
		    obvParams['author'] = SUser.findByEmail(m[AUTHOR_EMAIL].trim())
        } else {
            obvParams['author'] = springSecurityService.currentUser ?: SUser.read(1L); 
        }
        
        if(m[ORIGINAL_AUTHOR_NAME])
		    obvParams['originalAuthor'] = m[ORIGINAL_AUTHOR_NAME]
		
		obvParams['externalId'] = m[EXTERNAL_ID]
		obvParams['externalUrl'] = m[OBSERVATION_URL]
		obvParams['viaId'] = m[COLLECTION_ID]
		obvParams['viaCode'] = m[COLLECTION_CODE]
        if(m['dataTable']) {
            obvParams['dataTable'] = m['dataTable'];
            obvParams['sourceId'] = m['dataTable'].id;
        }

        if(m['geoprivacy'] || m[GEO_PRIVACY])
    		obvParams['geoPrivacy'] = m["geoprivacy"] ?: m[GEO_PRIVACY];

        obvParams['resourceListType'] = "ofObv"

		obvParams['agreeTerms'] = "on"
        
        obvParams['locale_language'] = utilsService.getCurrentLanguage();
		
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        println "populating topology"
        println "${obvParams.topology}"
        if(!obvParams.topology && obvParams.latitude && obvParams.longitude) {
            println "constructing areas from lat lng ${obvParams.latitude}"
            obvParams.areas = Utils.GeometryAsWKT(geometryFactory.createPoint(new Coordinate(obvParams.longitude?.toFloat(), obvParams.latitude?.toFloat())));
        } 

        if(m[DwCObservationImporter.ANNOTATION_HEADER])
            obvParams['checklistAnnotations'] = m[DwCObservationImporter.ANNOTATION_HEADER] as JSON;
        if(m[DwCObservationImporter.TRAIT_HEADER])
            obvParams['traits'] = m[DwCObservationImporter.TRAIT_HEADER];



		obvParams['basisOfRecord'] = m[BASISOFRECORD] ? BasisOfRecord.getEnum(m[BASISOFRECORD]): BasisOfRecord.HUMAN_OBSERVATION;
		obvParams['protocol'] = m[PROTOCOL]? ProtocolType.getEnum(m[PROTOCOL]) : protocolType;
		obvParams['externalDatasetKey'] = m[EXTERNAL_DATASET_KEY]
		obvParams['lastCrawled'] = m[LAST_CRAWLED]
		obvParams['catalogNumber'] = m[CATALOG_NUMBER]
		obvParams['publishingCountry'] = m[PUBLISHING_COUNTRY]
		obvParams['accessRights'] = m[ACCESS_RIGHTS]
		obvParams['informationWitheld'] = m[INFORMATION_WITHHELD]
        if(m['mediaInfo']) {
            obvParams.putAll(m['mediaInfo']);
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
	
	private boolean saveObv(Map params, List result, File uploadLog=null, boolean newObv=false, ProtocolType protocolType=ProtocolType.MULTI_OBSERVATION) {
        boolean success = false;
	
	
        if(!isValidObservation(params, uploadLog, protocolType)) {
			log.error "Aborting saving this observation as it is not valid for above reasons. Obv : $params"
            if(uploadLog) uploadLog << "\nAborting saving this observation as it is not valid for above reasons. Obv: $params"

			return false
		}	

		def observationInstance;
		try {
			observationInstance =  observationService.create(params);
            observationInstance.protocol = protocolType;
            observationInstance.clearErrors();
            //following line was needed as : image saving in XMLConverter was calling observation before and afterUpdate.... so obv is gng out of sync

			if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
				log.debug "Successfully created observation : "+observationInstance
                if(uploadLog) uploadLog <<  "\nSuccessfully created observation : "+observationInstance
                success=true;

				params.obvId = observationInstance.id
				params.author = observationInstance.author
				activityFeedService.addActivityFeed(observationInstance, null, observationInstance.author, activityFeedService.OBSERVATION_CREATED);
                postProcessObervation(params, observationInstance, newObv, uploadLog);
				result.add(observationInstance.id)
				
            } else {
                if(uploadLog) uploadLog <<  "\nError in observation creation : "+observationInstance
                observationInstance.errors.allErrors.each { 
                    if(uploadLog) uploadLog << "\n"+it; 
                    log.error it;
                }
            }
		} catch(e) {
				log.error "error in creating observation"
                if(uploadLog) uploadLog << "\nerror in creating observation ${e.getMessage()}" 
				e.printStackTrace();
		}
        return success;
	}

    private postProcessObervation(params, observationInstance, boolean newObv=false, File uploadLog=null) {
        params.identifiedBy = params.identifiedBy;
        addReco(params, observationInstance, newObv)
        if(uploadLog) 
            uploadLog <<  "\n======NAME PRESENT IN TAXONCONCEPT : ${observationInstance.externalId} :  "+observationInstance.maxVotedReco?.taxonConcept?.id;
        
		println "======NAME PRESENT IN TAXONCONCEPT : ${observationInstance.externalId} :  "+observationInstance.maxVotedReco?.taxonConcept?.id
        if(observationInstance.dataTable && observationInstance.maxVotedReco?.taxonConcept && observationInstance.maxVotedReco?.taxonConcept.group ) {
            observationService.updateSpeciesGrp(['group_id': observationInstance.maxVotedReco.taxonConcept.group.id], observationInstance, false);
        }

        utilsService.benchmark('settags') {
            def tags = (params.tags != null) ? new ArrayList(params.tags) : new ArrayList();
            observationInstance.setTags(tags);
        }

        utilsService.benchmark('setGroups') {
            if(params.groupsWithSharingNotAllowed) {
                observationService.setUserGroups(observationInstance, [params.groupsWithSharingNotAllowed], false);
            } else {
                def userGroups = observationService.getValidUserGroups(observationInstance, params.userGroupsList);
                if(userGroups)
                    observationService.setUserGroups(observationInstance, userGroups, false);
            }
        }
		
        //customFieldService.updateCustomFields(params, observationInstance.id)
        if(params.traits) {
            utilsService.benchmark('setTraits') {
                println "Saving Traits"
                def traitParams = ['contributor':observationInstance.author.email, 'attribution':observationInstance.author.email, 'license':License.LicenseType.CC_BY.value(), replaceFacts:true];
                traitParams.putAll(params.traits);
                println "---------------------"
                println traitParams;
                factService.updateFacts(traitParams, observationInstance);
            }
        }

        /*switch(observationInstance.dateAccuracy) {
            case DateAccuracy.UNKNOWN:
            case DateAccuracy.APPROXIMATE : 
                observationService.flagIt(observationInstance, FlagType.DATE_INAPPROPRIATE, "Date is "+observationInstance.dateAccuracy.value());
            break;
        }*/

        if(observationInstance.dataTable) {
            log.debug "Posting observation to all user groups that data table is part of"
            HashSet uGs = new HashSet();
            uGs.addAll(observationInstance.dataTable.userGroups);
            if(observationInstance.dataTable.dataset) {
                uGs.addAll(observationInstance.dataTable.dataset.userGroups);
            }
            log.debug uGs
            userGroupService.addResourceOnGroups(observationInstance, uGs.collect{it.id}, false);
        }

        if(!observationInstance.save()){
            if(uploadLog) uploadLog <<  "\nError in updating few properties of observation : "+observationInstance
                observationInstance.errors.allErrors.each { 
                    if(uploadLog) uploadLog << "\n"+it; 
                    log.error it 
                }
        } else {
            println "Successfully saved observation"
        }
    }

	private addReco(params, Observation observationInstance, boolean newObv=false){
		def recoResultMap;
        params.flushImmediately = false;
        recoResultMap = observationService.getRecommendation(params);
		def reco = recoResultMap.mainReco;
		def commonNameReco =  recoResultMap.commonNameReco;
		ConfidenceType confidence = observationService.getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());
		
		def recommendationVoteInstance
        Date dateIdentified = params.dateIdentified ? utilsService.parseDate(params.dateIdentified) : observationInstance.createdOn;
		if(reco){
            utilsService.benchmark('RecommendationVote.findByAuthorAndObservation') {
		        recommendationVoteInstance = RecommendationVote.findByAuthorAndObservation(params.author, observationInstance);
            }
		    if(!recommendationVoteInstance) {
			    recommendationVoteInstance = new RecommendationVote(observation:observationInstance, recommendation:reco, commonNameReco:commonNameReco, author:params.author, originalAuthor:params.identifiedBy?:params.originalAuthor, confidence:confidence, votedOn:dateIdentified, givenSciName:params.recoName, givenCommonName:params.commonName); 
            } else {
                recommendationVoteInstance.givenSciName = params.recoName;
                recommendationVoteInstance.givenCommonName = params.commonName;
                recommendationVoteInstance.author = params.author;
                recommendationVoteInstance.originalAuthor = params.identifiedBy?:params.originalAuthor;
                recommendationVoteInstance.confidence = params.confidence;
                recommendationVoteInstance.votedOn = dateIdentified;
            }
		}
        utilsService.benchmark('savingRecoVote') {
		if(recommendationVoteInstance && !recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save()) {
			log.debug "Successfully added reco vote : " + recommendationVoteInstance
			commentService.addRecoComment(recommendationVoteInstance.recommendation, observationInstance, params.recoComment, recommendationVoteInstance.author);

			observationInstance.lastRevised = dateIdentified;
            utilsService.benchmark('observationInstance.calculateMaxVotedSpeciesName') {
			    //saving max voted species name for observation instance
                //TODO: new obv and from bulk upload set it by default
                if(newObv) {
                    observationInstance.maxVotedReco = recommendationVoteInstance.recommendation;
                } else {
    			    observationInstance.calculateMaxVotedSpeciesName();
                }
            }
			def activityFeed = activityFeedService.addActivityFeed(observationInstance, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED);
           
        } else {
            recommendationVoteInstance?.errors?.allErrors?.each { log.error it }
        }
        }

			
	}

	private Map uploadImageFiles(imageDir, imagePaths, license, author){
		Map resourcesInfo = [:];
		String rootDir = grailsApplication.config.speciesPortal.observations.rootDir
		File obvDir
		int index = 1
		imagePaths.each{ path ->
			File f = new File(imageDir, path.trim())
            if(f.exists()){
				log.debug "uploading file $f"
				if(f.length()  > grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE) {
					log.debug  'File size cannot exceed ${104857600/1024}KB' 
				} else if(f.length() == 0) {
					log.debug 'File cannot be empty'
				} else {
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
					resourcesInfo.put("contributor_" + index, author.username)
					index++
				}
			}else{
                log.error " File not found please check the image folder " + f
            }
		}
		return resourcesInfo
	}
	
    private boolean isValidObservation(Map params, File uploadLog, ProtocolType protocolType) {
        def media = params['file_1']
        def snCol = params['recoName']
        def cnCol = params['commonName']

        boolean isValid = true;
        if(!params.author && !params.originalAuthor) {
			log.error "Author not found for params $params"
            if(uploadLog) uploadLog << "\nAuthor not found for params $params"
            isValid = false;
		}

        switch(protocolType) {
            case(ProtocolType.BULK_UPLOAD) :
            if(media && (snCol	|| cnCol) ) {
            } else {
                log.error "Media and either of sciNameColumn or commonNameColumn is required"
                if(uploadLog) uploadLog << "\n Media and either of sciNameColumn or commonNameColumn or Media is required";
                isValid = false;
            }
            break;
            case(ProtocolType.LIST):
            if(!(snCol	|| cnCol || media) ) {
                log.error "Either of sciNameColumn or commonNameColumn or Media is required";
                if(uploadLog) uploadLog << "\n Either of sciNameColumn or commonNameColumn or Media is required";
                isValid = false;
            }
            break;
            default:
            if(!media) {
                log.error "Atleast one media resource is required"
                if(uploadLog) uploadLog << "\n Atleast one media resource is required";
                isValid = false;
            }
            break;
            //TODO: More validation on obv is reqd here.e.g., on  location col

        }
        return isValid;
    }

    Map upload(String file, Map params, UploadLog dl) {
        dl.writeLog("============================================\n", Level.INFO);            
        File ipFile = new File(params.file);
        File mappingFile = new File(params.mappingFile);
        uploadObservations(DataTable.get(params.dataTable), ipFile, null, mappingFile, null,  new File(dl.logFilePath));  
//        dl.writeLog("\n====================================\nLoaded ${noOfObservationsLoaded} observations\n====================================\n", Level.INFO);
        return ['success':true, 'msg':"Loaded observations."];
    }

    private void uploadDWCObservations(DataTable dataTable, File directory, File uploadLog) {
        DwCObservationImporter importer = DwCObservationImporter.getInstance();
        Map o = importer.importData(directory.getAbsolutePath(), uploadLog);
        uploadObservations(dataTable, directory, importer, uploadLog);
    }

    private void uploadObservations(DataTable dataTable, File observationsFile, File multimediaFile, File mappingFile, File multimediaMappingFile, File uploadLog) {
        FileObservationImporter importer = FileObservationImporter.getInstance();
        importer.separator = ',';
        Map o = importer.importData(observationsFile, multimediaFile, mappingFile, multimediaMappingFile, uploadLog);
        uploadObservations(dataTable, observationsFile.getParentFile(), importer, o.mediaInfo, uploadLog);
    }

    private void uploadObservations(DataTable dataTable, File directory, AbstractObservationImporter importer, Map mediaInfo, File uploadLog) {
        Map paramsToPropagate = DataTable.getParamsToPropagate(dataTable);
        List obvParamsList = importer.next(mediaInfo, IMPORT_BATCH_SIZE, uploadLog)
        int noOfUploadedObv=0, noOfFailedObv=0;
        boolean flushSingle = false;
        Date startTime = new Date();
        int i=0;
        while(obvParamsList) {
            List resultObv = [];
            int tmpNoOfUploadedObv = 0, tmpNoOfFailedObv= 0;
            try {
                obvParamsList.each { obvParams ->
                    if(flushSingle) {
                        log.info "Retrying batch obv with flushSingle"
                        uploadLog << "\n Retrying batch obv with flushSingle"
                    }
                    //obvParams['observation url'] = 'http://www.gbif.org/occurrence/'+obvParams['externalId'];
                    obvParams['dataTable'] = dataTable;
                    DataTable.inheritParams(obvParams, paramsToPropagate);
                    uploadLog << "\n\n----------------------------------------------------------------------";
                    uploadLog << "\nUploading observation with params ${obvParams}"
                    try {
                        if(uploadObservation(null, obvParams, resultObv, uploadLog, ProtocolType.LIST )) {
                            tmpNoOfUploadedObv++;
                        } else {
                            tmpNoOfFailedObv++;
                        }
                    } catch(Exception e) {
                        tmpNoOfFailedObv++;
                        if(flushSingle) { 
                            utilsService.cleanUpGorm(true)
                            uploadLog << "\n"+e.getMessage()
                        }
                        else
                            throw e;
                    }
                }

                def obvs = resultObv.collect { Observation.read(it) }
                try {
                    observationsSearchService.publishSearchIndex(obvs, true);
                } catch (Exception e) {
                    log.error e.printStackTrace();
                }

                noOfUploadedObv += tmpNoOfUploadedObv;
                noOfFailedObv += tmpNoOfFailedObv;
                log.debug "Saved observations : noOfUploadedObv : ${noOfUploadedObv} noOfFailedObv : ${noOfFailedObv}";
                obvParamsList = importer.next(mediaInfo, IMPORT_BATCH_SIZE, uploadLog)
                flushSingle = false;
            } catch (Exception e) {
                log.error "error in creating observation."
                if(uploadLog) uploadLog << "\nerror in creating observation ${e.getMessage()}." 
                    e.printStackTrace();
                flushSingle = true;
            }
            utilsService.cleanUpGorm(true)
            resultObv.clear();
        }
        log.debug "Total number of observations saved for dataTable ${dataTable} are : ${noOfUploadedObv}";

        uploadLog << "\n\n----------------------------------------------------------------------";
        uploadLog << "\nTotal number of observations saved for dataTable (${dataTable}) are : ${noOfUploadedObv}";
        uploadLog << "\nTotal number of observations failed in loading for dataTable (${dataTable}) are : ${noOfFailedObv}";
        uploadLog << "\nTotal time taken for dataTable upload ${((new Date()).getTime() - startTime.getTime())/1000} sec"
        importer.closeReaders();
    }


    Map uploadDwCDataset(Map params) {
        def resultModel = [:]
        String file = params.path?:params.uFile?.path;
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        file = config.speciesPortal.content.rootDir + file;

        File f = new File(file);
        File destDir = f.getParentFile();
        /*new File(f.getParentFile(),  f.getName())
        if(!destDir.exists()) {
            destDir.mkdir()
        }*/
        boolean isDwC = false;
        File directory = f.getParentFile();
        File metadataFile;
        if(FilenameUtils.getExtension(f.getName()).equals('zip')) {
            def ant = new AntBuilder().unzip( src: file,
            dest: destDir, overwrite:true)
            directory = new File(destDir, FilenameUtils.removeExtension(f.getName()));
            if(!directory.exists()) {
                directory = destDir;
            }
            isDwC = true;//validateDwCA(file);
            if(!isDwC) {
                return [success:false, msg:'Invalid DwC-A file']
            } else {
                metadataFile = new File(directory, "metadata.xml");
            }
        }

        
        File uploadLog = new File(destDir, 'upload.log');
        if(uploadLog.exists()) uploadLog.delete();

        Date startTime = new Date();
        if(directory) {
            params['author'] = springSecurityService.currentUser; 
            params['type'] = DatasetType.OBSERVATIONS;
            params['datasource'] = Datasource.read(params.long('datasource'));
 
            if(metadataFile) {
                uploadLog << "\nUploading dataset in DwCA format present at : ${f.getAbsolutePath()}";
                uploadLog << "\nDataset upload start time : ${startTime}"
                String datasetMetadataStr = metadataFile.text;

                def datasetMetadata = new XmlParser().parseText(datasetMetadataStr);
                params['title'] = params.title?:datasetMetadata.dataset.title.text()
                params['description'] = params.description?:datasetMetadata.dataset.abstract.para.text();
                params['externalId'] = datasetMetadata.attributes().packageId;
                params['externalUrl'] = 'http://doi.org/'+params['externalId'];
                params['rights'] = datasetMetadata.dataset.intellectualRights.para.text();
                params['language'] = datasetMetadata.dataset.language.text();
                params['publicationDate'] = utilsService.parseDate(datasetMetadata.dataset.pubDate.text());
            } else {
                params['externalUrl'] = params.externalUrl ?: params['datasource']?.website;
            }

            UFile f1 = new UFile()
            f1.size = f.length()
            f1.path = params.uFile.path;//zipF.getAbsolutePath().replaceFirst(contentRootDir, "")
            if(f1.save()) {
                params['uFile'] = f1
            }
            //params['uFile'] = params.uFile; 
    //        params['originalAuthor'] = createContact() 
            Dataset dataset;
            def feedType;
            if(params.id) {
                dataset = Dataset.get(params.long('id'));
                dataset = update(dataset, params);
                feedType = activityFeedService.INSTANCE_UPDATED;
            } else {
                dataset = create(params);
                feedType = activityFeedService.INSTANCE_CREATED;
            }


            resultModel = save(dataset, params, true, null, feedType, null);

            if(resultModel.success) {
                    if(params.datasource.title.contains('Global Biodiversity Information Facility')) {
                        importGBIFObservations(dataset, directory, uploadLog)
                    } else {
                        if(isDwC) {
                            importDWCObservations(dataset, directory, uploadLog);
                        } else {
                            def request = WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest()    
                            def rs = [:]
                            if(ServletFileUpload.isMultipartContent(request)) {
                                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                                Utils.populateHttpServletRequestParams(request, rs);
                            } 
 
                            def multimediaF = params.multimediaFile?:params.multimediaFileUpload;
                            def mF = params.mappingFile?:params.mappingFileUpload;
                            def mMF = params.multimediaMappingFile?:params.multimediaMappingFileUpload;
                            File multimediaFile, mappingFile, multimediaMappingFile;
                            
                            if(multimediaF instanceof String) {
                                multimediaFile = new File(config.speciesPortal.content.rootDir, multimediaF );
                            } else {
                                multimediaFile = new File(directory, 'multimediaFile.tsv');
                                multimediaF.transferTo(multimediaFile);
                            }
                            
                            if(mF instanceof String) {
                                mappingFile = new File(config.speciesPortal.content.rootDir, mF );
                            } else {
                                mappingFile = new File(directory, 'mappingFile.tsv');
                                mF.transferTo(mappingFile);
                            }

                            if(mMF instanceof String) {
                                multimediaMappingFile = new File(config.speciesPortal.content.rootDir, mMF );
                            } else {
                                multimediaMappingFile = new File(directory, 'multimediaMappingFile.tsv');
                                mMF.transferTo(multimediaMappingFile);
                            }

                            File observationsFile = f;//new File(directory, 'occurence.txt');
                            //File multimediaFile = params.multimediaFile;//new File(directory, 'multimedia.txt');
                            importObservations(dataset, observationsFile, multimediaFile, mappingFile, multimediaMappingFile, uploadLog);
                        }
                    }
            } else {
                log.error "Error while saving dataset ${resultModel}";
            }
        } else {
            resultModel = [success:false, msg:'Invalid file']
        }
        uploadLog <<  "\nUpload result while saving dataset ${resultModel}";
        return resultModel
    }

    //FIX: This code is subjected to SQL INJECTION. Please make sure your data is sanitized
    private void importGBIFObservations(Dataset dataset, File directory, File uploadLog) {

        uploadLog << "Starting import of GBIF Observations data";
        def conn = new Sql(dataSource)

        int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
        dataSource.setUnreturnedConnectionTimeout(0);
	

        def tmpBaseDataTable = "gbifdata";
        def tmpNewBaseDataTable = "gbifdata_new";
        def tmpBaseDataTable_multimedia = tmpBaseDataTable+"_multimedia";
        def tmpBaseDataTable_parsedNamess = tmpBaseDataTable+"_parsed_names";
        def tmpBaseDataTable_namesList = tmpBaseDataTable+"_namesList";

        String occurencesFileName = (new File(directory, 'occurrence.txt')).getAbsolutePath(); 
        String multimediaFileName = (new File(directory, 'multimedia.txt')).getAbsolutePath(); 
        String namesFileName = (new File(directory, 'gbif_names_all_with_idswithoutspchar.csv')).getAbsolutePath(); 
        Date startTime = new Date();
         try {
            uploadLog << "\nCreating base table for ${occurencesFileName}";

            conn.execute('''
            drop table  if exists '''+tmpBaseDataTable+''';
            create table '''+tmpBaseDataTable+'''(gbifID text, abstract text, accessRights text, accrualMethod text, accrualPeriodicity text, accrualPolicy text, alternative text, audience text, available text, bibliographicCitation text, conformsTo text, contributor text, coverage text, created text, creator text, date text, dateAccepted text, dateCopyrighted text, dateSubmitted text, description text, educationLevel text, extent text, format text, hasFormat text, hasPart text, hasVersion text, identifier text, instructionalMethod text, isFormatOf text, isPartOf text, isReferencedBy text, isReplacedBy text, isRequiredBy text, isVersionOf text, issued text, language text, license text, mediator text, medium text, modified text, provenance text, publisher text, references1 text, relation text, replaces text, requires text, rights text, rightsHolder text, source text, spatial text, subject text, tableOfContents text, temporal text, title text, type text, valid text, institutionID text, collectionID text, datasetID text, institutionCode text, collectionCode text, datasetName text, ownerInstitutionCode text, basisOfRecord text, informationWithheld text, dataGeneralizations text, dynamicProperties text, occurrenceID text, catalogNumber text, recordNumber text, recordedBy text, individualCount text, organismQuantity text, organismQuantityType text, sex text, lifeStage text, reproductiveCondition text, behavior text, establishmentMeans text, occurrenceStatus text, preparations text, disposition text, associatedReferences text, associatedSequences text, associatedTaxa text, otherCatalogNumbers text, occurrenceRemarks text, organismID text, organismName text, organismScope text, associatedOccurrences text, associatedOrganisms text, previousIdentifications text, organismRemarks text, materialSampleID text, eventID text, parentEventID text, fieldNumber text, eventDate text, eventTime text, startDayOfYear text, endDayOfYear text, year text, month text, day text, verbatimEventDate text, habitat text, samplingProtocol text, samplingEffort text, sampleSizeValue text, sampleSizeUnit text, fieldNotes text, eventRemarks text, locationID text, higherGeographyID text, higherGeography text, continent text, waterBody text, islandGroup text, island text, countryCode text, stateProvince text, county text, municipality text, locality text, verbatimLocality text, verbatimElevation text, verbatimDepth text, minimumDistanceAboveSurfaceInMeters text, maximumDistanceAboveSurfaceInMeters text, locationAccordingTo text, locationRemarks text, decimalLatitude text, decimalLongitude text, coordinateUncertaintyInMeters text, coordinatePrecision text,  pointRadiusSpatialFit text, verbatimCoordinateSystem text, verbatimSRS text, footprintWKT text, footprintSRS text, footprintSpatialFit text, georeferencedBy text, georeferencedDate text, georeferenceProtocol text, georeferenceSources text, georeferenceVerificationStatus text, georeferenceRemarks text, geologicalContextID text, earliestEonOrLowestEonothem text, latestEonOrHighestEonothem text, earliestEraOrLowestErathem text, latestEraOrHighestErathem text, earliestPeriodOrLowestSystem text, latestPeriodOrHighestSystem text, earliestEpochOrLowestSeries text, latestEpochOrHighestSeries text, earliestAgeOrLowestStage text, latestAgeOrHighestStage text, lowestBiostratigraphicZone text, highestBiostratigraphicZone text, lithostratigraphicTerms text, group1 text, formation text, member text, bed text, identificationID text, identificationQualifier text, typeStatus text, identifiedBy text, dateIdentified text, identificationReferences text, identificationVerificationStatus text, identificationRemarks text, taxonID text, scientificNameID text, acceptedNameUsageID text, parentNameUsageID text, originalNameUsageID text, nameAccordingToID text, namePublishedInID text, taxonConceptID text, scientificName text, acceptedNameUsage text, parentNameUsage text, originalNameUsage text, nameAccordingTo text, namePublishedIn text, namePublishedInYear text, higherClassification text, kingdom text, phylum text, class text, order1 text, family text, genus text, subgenus text, specificEpithet text, infraspecificEpithet text, taxonRank text, verbatimTaxonRank text, vernacularName text, nomenclaturalCode text, taxonomicStatus text, nomenclaturalStatus text, taxonRemarks text, datasetKey text, publishingCountry text, lastInterpreted text, elevation text, elevationAccuracy text, depth text, depthAccuracy text, distanceAboveSurface text, distanceAboveSurfaceAccuracy text, issue text, mediaType text, hasCoordinate text, hasGeospatialIssues text, taxonKey text, kingdomKey text, phylumKey text, classKey text, orderKey text, familyKey text, genusKey text, subgenusKey text, speciesKey text, species text, genericName text, typifiedName text, protocol text, lastParsed text, lastCrawled text, repatriated text) with (fillfactor=50);

            copy gbifdata from '''+"'"+occurencesFileName+"'"+'''  with null '';
            delete from '''+tmpBaseDataTable+''' where gbifid='gbifID';
            alter table '''+tmpBaseDataTable+''' alter column gbifID type bigint using gbifID::bigint, add constraint gbifid_pk primary key(gbifid);

            alter table '''+tmpBaseDataTable+''' add column clean_sciName text, add column canonicalForm text, add column observation_id bigint, add column recommendation_id bigint, add column commonname_reco_id bigint, add column external_url text, add column eventDate1 timestamp without time zone, add column lastCrawled1 timestamp without time zone, add column lastInterpreted1 timestamp without time zone, add column dateIdentified1 timestamp without time zone, add column place_name text, add column group_id bigint, add column habitat_id bigint, add column topology geometry, alter column  decimallongitude type numeric USING NULLIF(decimallongitude, '')::numeric, alter column decimallatitude type numeric USING NULLIF(decimallatitude, '')::numeric, add column license1 bigint, to_update boolean;

            update '''+tmpBaseDataTable+''' set eventDate1=to_date(eventDate, 'yyyy-MM-ddTHH:miZ'), lastCrawled1=to_date(lastCrawled, 'yyyy-MM-ddTHH:miZ'), lastInterpreted1=to_date(lastInterpreted, 'yyyy-MM-ddTHH:miZ'), dateIdentified1=to_date(dateIdentified, 'yyyy-MM-ddTHH:miZ'), external_url = 'http://www.gbif.org/occurrence/'|| gbifId, place_name=concat_ws(', ', locality, stateProvince, county), topology=CASE WHEN decimallatitude is not null and decimallongitude is not null THEN ST_SetSRID(ST_MakePoint(decimallongitude, decimallatitude), 4326) ELSE NULL END, basisOfRecord=CASE WHEN basisOfRecord IS null THEN 'HUMAN_OBSERVATION' ELSE basisOfRecord END, protocol= CASE WHEN protocol IS null THEN 'DWC_ARCHIVE' ELSE protocol END;

            update '''+tmpBaseDataTable+''' set to_update = 't', observation_id=o.id from observation o where o.external_id::bigint = gbifId;
            update '''+tmpBaseDataTable+''' set observation_id=nextval('observation_id_seq') where to_update != 't';

            update '''+tmpBaseDataTable+''' set license1= CASE WHEN rights like '%/publicdomain/%' THEN '''+License.findByName('CC_PUBLIC_DOMAIN').id+''' WHEN rights like '%/by/%' THEN '''+License.findByName('CC_BY').id+'''  WHEN rights like '%/by-sa/%' THEN '''+License.findByName('CC_BY_SA').id+'''  WHEN rights like '%/by-nc/%' or rights='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN '''+License.findByName('CC_BY_NC').id+'''  WHEN rights like '%/by-nc-sa/%' THEN '''+License.findByName('CC_BY_NC_SA').id+'''  WHEN rights like '%/by-nc-nd/%' THEN '''+License.findByName('CC_BY_NC_ND').id+''' WHEN rights like '%/by-nd/%' THEN '''+License.findByName('CC_BY_ND').id+'''  ELSE '''+License.findByName('CC_BY').id+''' END;


            drop table  if exists '''+tmpNewBaseDataTable+''';
            create table '''+tmpNewBaseDataTable+''' as select g.*,a.data from '''+tmpBaseDataTable+''' g join  (select gbifID, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifID as gbifID, abstract, accessRights, accrualMethod, accrualPeriodicity, accrualPolicy, alternative, audience, available, bibliographicCitation, conformsTo, contributor, coverage, created, creator, date, dateAccepted, dateCopyrighted, dateSubmitted, description, educationLevel, extent, format, hasFormat, hasPart, hasVersion, identifier, instructionalMethod, isFormatOf, isPartOf, isReferencedBy, isReplacedBy, isRequiredBy, isVersionOf, issued, language, license, mediator, medium, modified, provenance, publisher, references1 as references, relation, replaces, requires, rights, rightsHolder, source, spatial, subject, tableOfContents, temporal, title, type, valid, institutionID, collectionID, datasetID, institutionCode, collectionCode, datasetName, ownerInstitutionCode, basisOfRecord, informationWithheld, dataGeneralizations, dynamicProperties, occurrenceID, catalogNumber, recordNumber, recordedBy, individualCount, organismQuantity, organismQuantityType, sex, lifeStage, reproductiveCondition, behavior, establishmentMeans, occurrenceStatus, preparations, disposition, associatedReferences, associatedSequences, associatedTaxa, otherCatalogNumbers, occurrenceRemarks, organismID, organismName, organismScope, associatedOccurrences, associatedOrganisms, previousIdentifications, organismRemarks, materialSampleID, eventID, parentEventID, fieldNumber, eventDate, eventTime, startDayOfYear, endDayOfYear, year, month, day, verbatimEventDate, habitat, samplingProtocol, samplingEffort, sampleSizeValue, sampleSizeUnit, fieldNotes, eventRemarks, locationID, higherGeographyID, higherGeography, continent, waterBody, islandGroup, island, countryCode, stateProvince, county, municipality, locality, verbatimLocality, verbatimElevation, verbatimDepth, minimumDistanceAboveSurfaceInMeters, maximumDistanceAboveSurfaceInMeters, locationAccordingTo, locationRemarks, decimalLatitude, decimalLongitude,  coordinateUncertaintyInMeters, coordinatePrecision, pointRadiusSpatialFit, verbatimCoordinateSystem, verbatimSRS, footprintWKT, footprintSRS, footprintSpatialFit, georeferencedBy, georeferencedDate, georeferenceProtocol, georeferenceSources, georeferenceVerificationStatus, georeferenceRemarks, geologicalContextID, earliestEonOrLowestEonothem, latestEonOrHighestEonothem, earliestEraOrLowestErathem, latestEraOrHighestErathem, earliestPeriodOrLowestSystem, latestPeriodOrHighestSystem, earliestEpochOrLowestSeries, latestEpochOrHighestSeries, earliestAgeOrLowestStage, latestAgeOrHighestStage, lowestBiostratigraphicZone, highestBiostratigraphicZone, lithostratigraphicTerms, group1 as group, formation, member, bed, identificationID, identificationQualifier, typeStatus, identifiedBy, dateIdentified, identificationReferences, identificationVerificationStatus, identificationRemarks, taxonID, scientificNameID, acceptedNameUsageID, parentNameUsageID, originalNameUsageID, nameAccordingToID, namePublishedInID, taxonConceptID, scientificName, acceptedNameUsage, parentNameUsage, originalNameUsage, nameAccordingTo, namePublishedIn, namePublishedInYear, higherClassification, kingdom, phylum, class, order1 as order, family, genus, subgenus, specificEpithet, infraspecificEpithet, taxonRank, verbatimTaxonRank, vernacularName, nomenclaturalCode, taxonomicStatus, nomenclaturalStatus, taxonRemarks, datasetKey, publishingCountry, lastInterpreted, elevation, elevationAccuracy, depth, depthAccuracy, distanceAboveSurface, distanceAboveSurfaceAccuracy, issue, mediaType, hasCoordinate, hasGeospatialIssues, taxonKey, kingdomKey, phylumKey, classKey, orderKey, familyKey, genusKey, subgenusKey, speciesKey, species, genericName, typifiedName, protocol, lastParsed, lastCrawled, repatriated ) d))::text as data from gbifdata) a on g.gbifid=a.gbifid order by g.gbifid;

            alter table '''+tmpNewBaseDataTable+''' alter column gbifID type bigint using gbifID::bigint, add constraint gbifid_new_pk primary key(gbifid);
            alter table '''+tmpNewBaseDataTable+''' add column key text;
            update '''+tmpNewBaseDataTable+''' set key=concat(scientificname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
            ''');
            
            uploadLog << "\nCreating distinct sciName table for parsing";
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_parsedNamess);
            conn.executeUpdate("CREATE TABLE "+tmpBaseDataTable_parsedNamess+"(id serial primary key, sciName text, clean_sciName text, canonicalForm text, species text, genus text, family text, order1 text, class text, phylum text, kingdom text, commonName text, taxonrank text, taxonId bigint, acceptedId bigint, recommendation_id bigint)");
            conn.executeInsert("INSERT INTO "+ tmpBaseDataTable_parsedNamess +  " (sciName, species, genus, family, order1, class, phylum, kingdom, commonname, taxonrank) select scientificname, species, genus, family, order1, class, phylum, kingdom, vernacularname,taxonrank from "+tmpNewBaseDataTable + " group by scientificname, species, genus, family, order1, class,phylum,kingdom, vernacularname,taxonrank");
            conn.execute('''
            alter table '''+tmpBaseDataTable_parsedNamess+''' add column key text;
            update '''+tmpBaseDataTable_parsedNamess+''' set key=concat(sciname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
            ''')


            uploadLog << "\nTime taken for creating annotations ${((new Date()).getTime() - startTime.getTime())/1000} sec"

            uploadLog << "\nPopulating with canonicalform and taxonIds";
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_namesList);
            conn.executeUpdate("CREATE TABLE "+tmpBaseDataTable_namesList+"(id bigint, sciName text, clean_sciName text, canonicalForm text, commonname text, species text, genus text, family text, order1 text, class text, phylum text, kingdom text, taxonId bigint, acceptedId bigint, taxonrank varchar(255))");
            conn.execute("copy "+tmpBaseDataTable_namesList+" from "+"'"+namesFileName+"'"+"  with null '' delimiter '\t' csv header");

conn.execute('''
alter table '''+tmpBaseDataTable_namesList+''' add column key text;
update '''+tmpBaseDataTable_namesList+''' set key=concat(sciname,species,genus,family,order1,class,phylum,kingdom,taxonrank);
''')

            conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " as x set canonicalForm = n.canonicalForm, taxonId = n.taxonId, acceptedId = n.acceptedId from "+tmpBaseDataTable_namesList+" n where n.key=x.key");

            uploadLog << "\nTime taken for creating annotations ${((new Date()).getTime() - startTime.getTime())/1000} sec"


        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for creating tables ${((new Date()).getTime() - startTime.getTime())/1000} sec"

        ///////////////////////////
        //Parsing Names
        ///////////////////////////
        uploadLog << "\nStarting parsing distinct sciNames";
        NamesParser namesParser = new NamesParser();
        SUser currentUser = springSecurityService.currentUser;
        List resultObv = [];
        int limit = 5000, offset 
        def noOfSciNames, noOfCommonNames;
        
        Date s = new Date();
        Date t_date = new Date();
/*        while(true) {
            s = new Date();

            try {
                conn = new Sql(dataSource);
                resultObv = conn.rows("select * from " + tmpBaseDataTable_parsedNamess + " order by id limit " + limit + " offset " + offset);
            } finally {
                conn.close();
            }

            if(!resultObv) break;

            uploadLog << "\n\n-----------------------------------------------"
            uploadLog << "\n limit : ${limit}, offset : ${offset}";

            def names = resultObv.collect { it.sciName };
            def parsedNames;
            Date n = new Date();
            try {
                parsedNames = namesParser.parse(names)
            } catch (Exception e) {
                uploadLog << "\n"+e.getMessage();
                log.error e.printStackTrace();
            }
            uploadLog << "\nTime taken for parsing ${limit} names ${((new Date()).getTime() - n.getTime())/1000} sec"

            uploadLog << "\nUpdating each distinct name with canonicalForm";
            n = new Date();
            try {
                conn = new Sql(dataSource);
                resultObv.eachWithIndex { t, index ->
                    conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " set canonicalForm = :canonicalForm, clean_sciName=:clean_sciName where sciName = :sciName and species = :species and genus=:genus and family=:family and order1=:order1 and class=:class and phylum=:phylum and kingdom=:kingdom and commonname=:commonname and taxonrank=:taxonrank", [canonicalForm:parsedNames[index]?.canonicalForm, clean_sciName:parsedNames[index]?.name, sciName:t.sciName, species:t.species, genus:t.genus, family:t.family, order1:t.order1, class:t.class, phylum:t.phylum, kingdom:t.kingdom, commonname:t.commonname, taxonrank:t.taxonrank]);
                }
            } finally {
                conn.close();
            }
            uploadLog << "\nTime taken for updating ${limit} canonicalForms ${((new Date()).getTime() - n.getTime())/1000} sec"

            resultObv.clear();
            offset = offset + limit;
            uploadLog << "\nTime taken for parsing and updating ${limit} distinct names ${((new Date()).getTime() - s.getTime())/1000} sec"
        }
        uploadLog << "\nTime taken for parsing and updating canonical forms for distinct names is ${((new Date()).getTime() - t_date.getTime())/1000} sec"
*/
        uploadLog << "\nInserting new names into recommendations";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            uploadLog << "\nInserting new sci names into recommendations";
            noOfSciNames = conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name,taxon_concept_id, accepted_name_id, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.canonicalform, 't', t.taxonId, t.acceptedId, lower(t.canonicalForm), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on lower(t.canonicalForm) = r.lowercase_name and (t.taxonId=r.taxon_concept_id or (t.taxonId is null and r.taxon_concept_id is null)) and r.is_scientific_name='t' where r.name is null and t.canonicalForm is not null group by t.canonicalForm, t.taxonId,t.acceptedId");

            //handling canonical form null by taking in sciname as is
            noOfSciNames += conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name,taxon_concept_id, accepted_name_id, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.sciname, 't', t.taxonId, t.acceptedId, lower(t.sciname), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on t.sciName=r.name where r.name is null and t.canonicalForm is null and t.sciname is not null");

            println noOfSciNames;
            uploadLog << "\nInserting new common names into recommendations";
            noOfCommonNames = conn.executeInsert("INSERT INTO recommendation(id, last_modified, name, is_scientific_name, lowercase_name, is_flagged) select nextval('hibernate_sequence') as id, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, t.commonname, 'f', lower(t.commonname), 'f' from "+tmpBaseDataTable_parsedNamess+" t left outer join recommendation r on lower(t.commonname) = r.lowercase_name and (r.is_scientific_name='f' or r.is_scientific_name is null) and r.language_id=:defaultLanguageId where r.name is null and t.commonname is not null group by t.commonname", [defaultLanguageId:Language.getLanguage().id]);
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for inserting new recommendations ${noOfSciNames.size()} and ${noOfCommonNames.size()} ${((new Date()).getTime() - s.getTime())/1000} sec"

        uploadLog << "\nUpdating all sciNames with their recommendaitonIds";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //FIX:sciName could be repeated in parsed_names table
            conn.executeUpdate("update " + tmpBaseDataTable_parsedNamess + " set recommendation_id = r.id from recommendation r where ((canonicalform is not null and r.lowercase_name = lower(canonicalform)) or (sciname is not null and r.lowercase_name = lower(sciname))) and ((taxonId is null and r.taxon_concept_id is null) or (taxonId = r.taxon_concept_id)) and ((acceptedId is null and r.accepted_name_id is null) or (acceptedId = r.accepted_name_id))");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all canonicalForms ${((new Date()).getTime() - s.getTime())/1000} sec"


/*        uploadLog << "\nUpdating all sciNames with their canonicalForm";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //FIX:sciName could be repeated in parsed_names table
            conn.executeUpdate("update " + tmpNewBaseDataTable + " set canonicalForm = t1.canonicalForm, clean_sciName = t1.clean_sciName from " + tmpBaseDataTable_parsedNamess + " t1 where t1.sciName = scientificname");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all canonicalForms ${((new Date()).getTime() - s.getTime())/1000} sec"
*/

        uploadLog << "\nUpdating all sciNames with their reco ids";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            conn.executeUpdate("update " + tmpNewBaseDataTable + " as g set recommendation_id = t1.recommendation_id, group_id=t2.group_id, habitat_id=:defaultHabitatId from "+tmpBaseDataTable_parsedNamess+" t1 join taxonomy_definition t2 on t1.taxonid is not null and t1.taxonid = t2.id where t1.key=g.key",  [defaultHabitatId:Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id]) ;

            //handling taxonid null case
            conn.executeUpdate("update " + tmpNewBaseDataTable +"  as g set recommendation_id = t1.recommendation_id, group_id=:defaultSpeciesGroupId,habitat_id=:defaultHabitatId from gbifdata_parsed_names  t1 where g.key=t1.key and t1.taxonid is null and t1.recommendation_id is not null and g.scientificname is not null",  ['defaultSpeciesGroupId':SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id, 'defaultHabitatId':Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id]);

            conn.executeUpdate("update " + tmpNewBaseDataTable + " set commonname_reco_id = t1.id from recommendation t1 where t1.name = vernacularname;") ;
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for updating all recoids ${((new Date()).getTime() - s.getTime())/1000} sec"


        uploadLog << "\nInserting observation and creating recovotes";
        s = new Date();
        try {
            conn = new Sql(dataSource);
            //TODO: this is risky as any other obv creation during this time will happen without constraints
            conn.executeUpdate("ALTER TABLE observation DISABLE TRIGGER ALL ;");
            conn.executeUpdate("insert into observation (id, version, access_rights, agree_terms, author_id, basis_of_record, catalog_number, checklist_annotations, created_on, dataset_id, external_dataset_key, external_id, external_url, feature_count, flag_count, from_date, geo_privacy, group_id, habitat_id, information_withheld, is_checklist, is_deleted, is_locked, is_showable, language_id, last_crawled, last_interpreted, last_revised, latitude, license_id, location_accuracy, location_scale, longitude, max_voted_reco_id, notes, original_author, place_name, protocol, publishing_country, rating, reverse_geocoded_name, search_text, source_id, to_date, topology, via_code, via_id, visit_count) select observation_id, 0, accessRights, 't', "+currentUser.id+", basisOfRecord, catalogNumber, data, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, "+dataset.id+", datasetKey, gbifID, external_url, 0, 0, eventDate1, 'f', COALESCE(group_id, "+SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id+"), COALESCE(habitat_id, "+Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id+" ), informationWithheld, 'f', 'f', 'f', 'f', "+Language.getLanguage().id+", lastCrawled1, lastInterpreted1, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp, decimalLatitude, license1, 'Approximate', 'APPROXIMATE', decimalLongitude, recommendation_id, null, recordedBy, place_name, 'DWC_ARCHIVE', publishingCountry, 0, place_name, null, null, eventDate1, topology, collectionCode, collectionID, 0 from "+tmpNewBaseDataTable+" where decimallatitude is not null and decimallongitude is not null and eventDate1 is not null and decimallatitude>=26.647 and decimallatitude<=28.280 and decimallongitude>=88.692 and decimallongitude<=92.170 and to_update != 't' order by gbifId");

            println "Inserted observations "

            conn.executeUpdate("update observation set access_rights = tmp.access_rights, set agree_terms=tmp.agree_terms, set author_id=tmp.author_id, set basis_of_record=tmp.basis_of_record, set catalog_number=tmp.catalog_number, set checklist_annotations=tmp.checklist_annotations, set created_on=tmp.created_on, set dataset_id=tmp.dataset_id, set external_dataset_key=tmp.external_dataset_key, set external_id=tmp.external_id, set external_url=tmp.external_url, set feature_count=tmp.feature_count, set flag_count=tmp.flag_count, set from_date= tmp.from_date, set geo_privacy=tmp.geo_privacy, set group_id=tmp.group_id, set habitat_id=tmp.habitat_id, set information_withheld=tmp.information_withheld, set is_checklist=tmp.is_checklist, set is_deleted=tmp.is_deleted, set is_locked=tmp.is_locked, set is_showable=tmp.is_showable, set language_id=tmp.language_id, set last_crawled=tmp.last_crawled, set last_interpreted=tmp.last_interpreted, set last_revised=tmp.last_revised, set latitude=tmp.latitude, set license_id=tmp.license_id, set location_accuracy=tmp.location_accuracy, set location_scale=tmp.location_scale, set longitude=tmp.longitude, set max_voted_reco_id=tmp.max_voted_reco_id, set notes=tmp.notes, set original_author=tmp.original_author, set place_name=tmp.place_name, set protocol=tmp.protocol, set publishing_country=tmp.publishingCountry, set rating=tmp.rating, set reverse_geocoded_name=tmp.reverse_geocoded_name, set search_text=tmp.search_text, set source_id=tmp.source_id, set to_date=tmp.to_date, set topology=tmp.topology, set via_code=tmp.via_code, set via_id=tmp.via_id, set visit_count=tmp.visit_count) select accessRights as access_rights, 't' as agree_terms, "+currentUser.id+" as author_id, basisOfRecord as basis_of_record, catalogNumber as catalog_number, data as checklist_annotations, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp as created_on, "+dataset.id+" as dataset_id, datasetKey as external_dataset_key, gbifID as external_id, external_url as external_url, 0 as feature_count, 0 as flag_count, eventDate1 as from_date, 'f' as geo_privacy, COALESCE(group_id, "+SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id+") as group_id, COALESCE(habitat_id, "+Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id+" ) as habitat_id, informationWithheld as information_withheld, 'f' as is_checklist, 'f' as is_deleted, 'f' as is_locked, 'f' as is_showable, "+Language.getLanguage().id+" as language_id, lastCrawled1 as last_crawled, lastInterpreted1 as last_interpreted, '"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'::timestamp as last_revised, decimalLatitude as latitude, license1 as license_id, 'Approximate' as location_accuracy, 'APPROXIMATE' as location_scale, decimalLongitude as longitude, recommendation_id as max_voted_reco_id, null as notes, recordedBy as original_author, place_name as place_name, 'DWC_ARCHIVE' as protocol, publishingCountry as publishing_country, 0 as rating, place_name as reverse_geocoded_name, null as search_text, null as source_id, eventDate1 as to_date, topology, collectionCode as via_code, collectionID as via_id, 0 as visit_count from "+tmpNewBaseDataTable+" where decimallatitude is not null and decimallongitude is not null and eventDate1 is not null and decimallatitude>=26.647 and decimallatitude<=28.280 and decimallongitude>=88.692 and decimallongitude<=92.170 and to_update = 't' order by gbifId");
            //, [datasetId:dataset.id, languageId:Language.getLanguage().id, defaultHabitatId:Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id, defaultSpeciesGroupId:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id]);
            conn.executeUpdate("ALTER TABLE observation ENABLE TRIGGER ALL ;");
            println "updated old observations "

            //conn.executeUpdate("update observation set protocol=CASE WHEN protocol='TAPIR' or protocol='DIGIR_MANIS' THEN 'OTHER' ELSE protocol END");

            conn.executeUpdate("delete from recommendation_vote where observation_id = id from "+tmpNewBaseDataTable+", observation where to_update='t' and recommendation_id is not null and observation_id is not null and observation_id=id");

            conn.executeUpdate("insert into recommendation_vote(id, version, author_id, confidence, observation_id, recommendation_id, user_weight, voted_on, comment, common_name_reco_id, given_sci_name, given_common_name, original_author) select nextval('hibernate_sequence'), 0, "+currentUser.id+", 'CERTAIN', observation_id, recommendation_id, 0, COALESCE(dateIdentified1, '"+((new Date()).format('yyyy-MM-dd HH:mm:ss.SSS'))+"'), null, commonname_reco_id,  scientificname as given_sci_name, vernacularname as given_common_name, identifiedby as original_author from "+tmpNewBaseDataTable+", observation where recommendation_id is not null and observation_id is not null and observation_id=id");
        } finally {
            conn.close();
        }
        uploadLog << "\nTime taken for creating observations and recovote ${((new Date()).getTime() - s.getTime())/1000} sec"


        uploadLog << "\nInserting resources";
        s = new Date();
        try {
            conn = new Sql(dataSource);

            conn.execute('''
            drop table if exists '''+tmpBaseDataTable_multimedia+''';
            alter table resource drop column IF EXISTS gbifID;
            create table '''+tmpBaseDataTable_multimedia+'''(id serial primary key, gbifID   text, type     text,  format   text,  identifier   text,  references1 text, title  text,  description   text, created   text, creator   text, contributor   text, publisher   text,   audience    text,   source  text, license text, rightsHolder text);

            copy '''+tmpBaseDataTable_multimedia+'''(gbifID,type,format,identifier,references1,title,description,created,creator,contributor,publisher,audience,source,license,rightsHolder) from '''+"'"+multimediaFileName+"';"+''' ;
            delete from '''+tmpBaseDataTable_multimedia+''' where gbifID='gbifID';
            alter table '''+tmpBaseDataTable_multimedia+''' alter column gbifID type bigint using gbifID::bigint;

            alter table '''+tmpBaseDataTable_multimedia+''' add column annotations text, add column type1 text, add column license1 bigint, add column to_update boolean, resource_id bigint;

            update '''+tmpBaseDataTable_multimedia+''' set type1= CASE WHEN type='StillImage' THEN 'IMAGE'  WHEN type='MovingImage' THEN 'VIDEO' WHEN type='SOUND' THEN 'AUDIO' ELSE 'IMAGE' END, license1=CASE WHEN license like '%/publicdomain/%' THEN '''+License.findByName('CC_PUBLIC_DOMAIN').id+''' WHEN license like '%/by/%' THEN '''+License.findByName('CC_BY').id+'''  WHEN license like '%/by-sa/%' THEN '''+License.findByName('CC_BY_SA').id+'''  WHEN license like '%/by-nc/%' or license='Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.' THEN '''+License.findByName('CC_BY_NC').id+'''  WHEN license like '%/by-nc-sa/%' THEN '''+License.findByName('CC_BY_NC_SA').id+'''  WHEN license like '%/by-nc-nd/%' THEN '''+License.findByName('CC_BY_NC_ND').id+''' WHEN license like '%/by-nd/%' THEN '''+License.findByName('CC_BY_ND').id+'''  ELSE '''+License.findByName('CC_BY').id+''' END, identifier= CASE WHEN identifier IS NULL THEN '''+"'"+grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg"+"'"+''' ELSE identifier END;

            update '''+tmpBaseDataTable_multimedia+''' set annotations = g.data from (select id as xid, row_to_json((select d from (select 'http://www.gbif.org/occurrence/'||gbifId as gbifId, type, identifier, format, license, references1 as references, rightsHolder, title, publisher, source, description, created, creator, contributor, audience) d))::text as data from gbifdata_multimedia) as  g where g.xid=id;

            update '''+tmpBaseDataTable_multimedia+''' set to_update = 't', set resource_id = r.id from resource r where r.gbifid = gbifID;
            
            delete from resource_contributor where resource_contributors_id in (select resource_id from '''+tmpBaseDataTable_multimedia+''' where to_update = 't');
            delete from observation_resource where resource_id in (select resource_id from '''+tmpBaseDataTable_multimedia+''' where to_update = 't');

            delete from resource where gbifid = gbifID from '''+tmpBaseDataTable_multimedia+''';

            insert into resource (id, version,description,file_name,mime_type,type,url,rating,upload_time,uploader_id,context,language_id,access_rights,annotations,gbifID,license_id) select nextval('hibernate_sequence'), 0,title,'i',format,type1,identifier,0,'''+"'"+(new Date()).format('yyyy-MM-dd HH:mm:ss.SSS')+"'"+'''::timestamp,'''+currentUser.id+''','OBSERVATION','''+Language.getLanguage().id+''',license,annotations,gbifID,license1  from '''+tmpBaseDataTable_multimedia+''' where identifier is not null;

            insert into observation_resource(observation_id, resource_id) select o.id, r.id from observation o, resource r where cast(o.external_id as integer)=r.gbifID;

            insert into contributor(id, name) select nextval('hibernate_sequence') as id, rightsholder from '''+tmpBaseDataTable_multimedia+''' where rightsholder is not null and rightsholder not in (select distinct(name) from contributor) group by rightsholder;

        insert into resource_contributor(resource_contributors_id,contributor_id) select r.id, c.id from '''+tmpBaseDataTable_multimedia+''' o, resource r, contributor c where o.gbifId=r.gbifID and o.rightsholder=c.name;

        ALTER TABLE observation DISABLE TRIGGER ALL;
        update observation set is_showable='t' where id in (select o.observation_id from observation_resource o, resource r where o.resource_id=r.id and r.gbifid is not null);

        ''');

           conn.execute("delete from observation as o1 where o1.id in (select id from observation left outer join observation_resource on id=observation_id where external_id is not null and max_voted_reco_id is null and resource_id is null)");

           conn.execute('''
           update observation set no_of_images = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='IMAGE' group by observation_id) g where g.observation_id = id;
           update observation set no_of_videos = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='VIDEO' group by observation_id) g where g.observation_id = id;
           update observation set no_of_audio = g.count from (select observation_id, count(*) as count from resource r inner join observation_resource or1 on r.id=or1.resource_id and r.type='AUDIO' group by observation_id) g where g.observation_id = id;

           create table tmp as select observation_id, count(*) as count from recommendation_vote group by observation_id;

           update observation set no_of_identifications = g.count from (select * from tmp) g where g.observation_id=id;

           drop table tmp;
                    
            create table tmp as select resource_id, observation_id, rating_ref, (case when avg is null then 0 else avg end) as avg, (case when count is null then 0 else count end) as count from observation_resource o left outer join (select rating_link.rating_ref, avg(rating.stars), count(rating.stars) from rating_link , rating  where rating_link.type='resource' and rating_link.rating_id = rating.id  group by rating_link.rating_ref) c on o.resource_id =  c.rating_ref order by observation_id asc, avg desc, resource_id asc;

            update observation set repr_image_id = g.resource_id from (select b.observation_id,b.resource_id from (select observation_id, max(avg) as max_avg from tmp group by observation_id) a inner join tmp b on a.observation_id=b.observation_id where b.avg=a.max_avg) g where g.observation_id=id;

            drop table tmp;


           '''
           );
        conn.executeUpdate("ALTER TABLE observation ENABLE TRIGGER ALL ;");

        } finally {
        conn.close();
        }
        uploadLog << "\nTime taken for resources ${((new Date()).getTime() - s.getTime())/1000} sec"
         


        /*uploadLog << "\n Publishing search index"
        d = new Date();
        try {
        utilsService.logSql {
        observationsSearchService.publishSearchIndex(obvs, true);
        }
        uploadLog << "\nTime taken for search index commit : ${((new Date()).getTime() - d.getTime())/1000} sec"
        } catch (Exception e) {
        log.error e.printStackTrace();
        }*/


        try {
            //conn = new Sql(dataSource);
            //conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);	
            //conn.executeUpdate("DROP TABLE IF EXISTS " + tmpBaseDataTable_parsedNamess);	
        } finally {
            //conn.close();
            log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
            dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
        }

        uploadLog << "\n\n----------------------------------------------------------------------";
        uploadLog << "\nTotal time taken for uploading ${((new Date()).getTime() - startTime.getTime())/1000} sec"
    }


}
