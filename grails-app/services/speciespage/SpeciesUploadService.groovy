package speciespage

import java.util.List;

import java.util.List

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList


import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;

import org.apache.commons.logging.LogFactory
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.hibernate.exception.ConstraintViolationException;

import grails.converters.JSON
import species.Contributor;
import species.Field
import species.Resource;
import species.Species
import species.SpeciesField;
import species.GeographicEntity;
import species.TaxonomyDefinition;
import species.Language;
import species.formatReader.SpreadsheetReader
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.NewSimpleSpreadsheetConverter
import species.sourcehandler.SourceConverter;
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter
import species.utils.Utils;

import java.text.DateFormat
import java.text.SimpleDateFormat;

import species.sourcehandler.exporter.DwCAExporter

import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;

import species.auth.SUser
import species.formatReader.SpreadsheetReader;
import species.formatReader.SpreadsheetWriter;
import species.namelist.NameInfo
import species.participation.Featured
import species.participation.Recommendation
import species.participation.SpeciesBulkUpload
import species.CommonNames
import species.Synonyms
import species.TaxonomyRegistry
import species.SpeciesPermission
import species.groups.SpeciesGroupMapping
import species.groups.UserGroup
import org.codehaus.groovy.grails.web.json.JSONObject;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import species.participation.NamesReportGenerator
import species.participation.NamelistService
import species.participation.UploadLog

class SpeciesUploadService {

    private static def log = LogFactory.getLog(this);
    private FileAppender fa;

	static transactional = false

    //prototype - A new service is created every time it is injected into another class
    static scope = "prototype"

    def utilsService;
	def grailsApplication;
	def groupHandlerService;
	def namesLoaderService;
	def sessionFactory;
	def externalLinksService;
	def speciesSearchService;
	def springSecurityService
	def speciesPermissionService;
    //def namelistService;

    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	static int BATCH_SIZE = 1;
	//int noOfFields = Field.count();
    String contentRootDir = config.speciesPortal.content.rootDir

	/**
	 * 
	 * @return
	 */
	def loadData() {
		int noOfInsertions = 0;
		return noOfInsertions;
	}
	
	
	Map searchAndValidateName(params){
		if(!params.xlsxFileUrl){
			return ['msg': 'File not found !!!' ]
		}
		
		File speciesDataFile = saveModifiedSpeciesFile(params)
		log.debug "THE FILE BEING UPLOADED " + speciesDataFile
		
		if(!speciesDataFile.exists()){
			return ['msg': 'File not found !!!' ]
		}
		
		def namesReportGenEntry = NamesReportGenerator.create(springSecurityService.currentUser, new Date(), null, speciesDataFile.getAbsolutePath())
		
		return ['msg': 'Names search in progress. Please visit your profile page to view status.' ,namesReportGenEntry:namesReportGenEntry]
	}

	
	Map basicUploadValidation(params){
		if(!params.xlsxFileUrl){
			return ['msg': 'File not found !!!' ]
		}
		
		File speciesDataFile = saveModifiedSpeciesFile(params)
		log.debug "THE FILE BEING UPLOADED " + speciesDataFile
		
		if(!speciesDataFile.exists()){
			return ['msg': 'File not found !!!' ]
		}
		
		SpeciesBulkUpload sBulkUploadEntry = createRollBackEntry(new Date(), null, speciesDataFile.getAbsolutePath(), params.imagesDir, params.notes, params.uploadType)
		
		if(!validateUserSheetForName(sBulkUploadEntry)){
			return  ['msg': 'Name validation failed. Please visit your profile page to view status.!!!', 'sBulkUploadEntry': sBulkUploadEntry ]
		}
		
		return ['msg': 'Bulk upload in progress. Please visit your profile page to view status.', 'sBulkUploadEntry': sBulkUploadEntry]
		
	}
	
	boolean validateUserSheetForName(SpeciesBulkUpload sbu){
		return MappedSpreadsheetConverter.validateUserSheetForName(sbu)
	}
	
	Map upload(SpeciesBulkUpload sBulkUploadEntry){
		def speciesDataFile = sBulkUploadEntry.filePath
		def imagesDir = sBulkUploadEntry.imagesDir
		sBulkUploadEntry.updateStatus(UploadLog.Status.RUNNING)
		
		def res = uploadMappedSpreadsheet(speciesDataFile, speciesDataFile, 2,0,0,0, imagesDir?1:-1, imagesDir, sBulkUploadEntry)
		
		//writing log after upload
		def mylog = (!res.success) ?  "ERROR WHILE UPLOADING SPECIES "  : ""
		mylog += "Start Date  " + sBulkUploadEntry.startDate + "   End Date " + sBulkUploadEntry.endDate + "\n\n " 
		//mylog += "  \n\n    Name assigned \n\n" + res.idSummary + "\n\n " 
		mylog += "  \n\n    Developer log \n\n" + res.log
		File errorFile = utilsService.createFile("ErrorLog.txt" , "species", contentRootDir);
		errorFile.write(mylog)
		
		sBulkUploadEntry.errorFilePath = errorFile.getAbsolutePath()
		
		if(!sBulkUploadEntry.save(flush:true)){
			sBulkUploadEntry.errors.allErrors.each { log.error it }
		}
		
		//Every thing is fine then sending mail
		String link
		try{
			if(res.success){
				def otherParams = [:]
				def usersMailList = []
				usersMailList = speciesPermissionService.getSpeciesAdmin()
				log.debug "user mail list " + usersMailList
				def sp = new Species()
				
				def linkParams = [:]
				linkParams["daterangepicker_start"] = SpeciesService.DATE_FORMAT.format(sBulkUploadEntry.startDate) 
				linkParams["daterangepicker_end"] = SpeciesService.DATE_FORMAT.format(new Date(sBulkUploadEntry.endDate.getTime() + 60*1000))  
				linkParams["sort"] = "lastUpdated"
				linkParams["user"] = springSecurityService.currentUser?.id
				link = utilsService.generateLink("species", "list", linkParams)
				otherParams["link"] = link
				usersMailList.each{ user ->
					def uml =[]
					uml.add(user)
					otherParams["curator"] = user.name
					otherParams["usersMailList"] = uml
					utilsService.sendNotificationMail(utilsService.SPECIES_CURATORS,sp,null,null,null,otherParams)
				}
				
				//sending mail to species contributor
				otherParams["uploadCount"] = res.uploadCount?res.uploadCount:""
				otherParams["speciesCreated"] = sBulkUploadEntry.speciesCreated
				otherParams["speciesUpdated"] = sBulkUploadEntry.speciesUpdated
				otherParams["stubsCreated"] = sBulkUploadEntry.stubsCreated
				utilsService.sendNotificationMail(utilsService.SPECIES_CONTRIBUTOR,sp,null,null,null,otherParams)
			}
		}catch(e){
			log.error "Error while sending mail"
			e.printStackTrace()
		}
			
		
		def msg = ""
		if(sBulkUploadEntry.status == UploadLog.Status.UPLOADED){
			msg = "Species uploaded Successfully. Please visit your profile page to view summary."
		}else{
			msg = "Species upload " + sBulkUploadEntry.status.value().toLowerCase() + ". Please visit your profile page to view summary."
		}
		return [success:res.success, downloadFile:speciesDataFile, filterLink: link, msg:msg]
	}
	
	
	Map runNamesMapper( NamesReportGenerator nr) {
		println "---------------- running ---- "
		
		def speciesDataFile = nr.filePath
		
		nr.updateStatus(UploadLog.Status.RUNNING)
		List nameInfoList = MappedSpreadsheetConverter.getNames(speciesDataFile, speciesDataFile)
		
		println "------------------ name info list " + nameInfoList
		File f = NameInfo.writeNamesMapperSheet(nameInfoList, new File(speciesDataFile))  
		
		nr.updateStatus(UploadLog.Status.SUCCESS)
		
		if(!nr.save(flush:true)){
			nr.errors.allErrors.each { log.error it }
		}
		
		def msg = "Please visit your profile page to view summary."
		return [success:true, downloadFile:f, msg:msg] 
	}
	
	/**
	 * 
	 * @param file
	 * @param mappingFile
	 * @param mappingSheetNo
	 * @param mappingHeaderRowNo
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 * @return
	 */
	Map uploadMappedSpreadsheet (String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo, int imageMetaDataSheetNo = -1, String imagesDir="", SpeciesBulkUpload sBulkUploadEntry=null) {
		Map result = ['success':false]
		MappedSpreadsheetConverter converter
		def uploadCount = 0
		try {
			log.info "Uploading mapped spreadsheet : "+file;
	
			List<Species> species = new ArrayList<Species>();
			converter = new MappedSpreadsheetConverter();
	
			converter.mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);
			List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
			converter.initCurationInfo(file)
			
			List<Map> imagesMetaData;
			if(imageMetaDataSheetNo && imageMetaDataSheetNo  >= 0) {
				converter.imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetaDataSheetNo, 0);
				converter.imagesDir = imagesDir
				
			}
			uploadCount = saveSpecies(converter, content, imagesDir, sBulkUploadEntry)
			result['success'] = true
		} catch (Exception e) {
			log.error e.message
			e.printStackTrace()
		}
		
		result['uploadCount'] = uploadCount
		result['summary'] = converter ? converter.getSummary():""
		result['log'] = converter ? converter.getLogs() : ""
		//result['idSummary'] = converter ? converter.idSummaryLog : "\n"
		return result
	}

	/**
	 * 
	 * @param file
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 * @param imageMetadataSheetNo
	 * @param imageMetaDataHeaderRowNo
	 * @return
	 */
	int uploadSpreadsheet (String file, int contentSheetNo, int contentHeaderRowNo, int imageMetadataSheetNo, int imageMetaDataHeaderRowNo, String imagesDir="") {
		log.info "Uploading spreadsheet : "+file;
		List<Species> species = SpreadsheetConverter.getInstance().convertSpecies(file, contentSheetNo, contentHeaderRowNo, imageMetadataSheetNo, imageMetaDataHeaderRowNo, imagesDir);
		return saveSpecies(species);
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	int uploadNewSpreadsheet (String file, String imagesDir="") {
		log.info "Uploading new spreadsheet : "+file;
		def converter = NewSpreadsheetConverter.getInstance();
		List<List<Map>> content = SpreadsheetReader.readSpreadSheet(file);
		List<Node> species = NewSpreadsheetConverter.getInstance().convertSpecies(content, imagesDir);
		return saveSpecies(species);
		
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	int uploadNewSimpleSpreadsheet (String file, String imagesDir="") {
		log.info "Uploading new simple spreadsheet : "+file;
		List<Species> species = NewSimpleSpreadsheetConverter.getInstance().convertSpecies(file, imagesDir);
		return saveSpecies(species);
	}

	/**
	 * 
	 * @param connectionUrl
	 * @param userName
	 * @param password
	 * @param mappingFile
	 * @param mappingSheetNo
	 * @param mappingHeaderRowNo
	 * @return
	 */
	int uploadKeyStoneData (String connectionUrl, String userName, String password, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo) {
		log.info "Uploading keystone data";
		List<Species> species = KeyStoneDataConverter.getInstance().convertSpecies(connectionUrl, userName, password, mappingFile, mappingSheetNo, mappingHeaderRowNo);
		return saveSpecies(species);
	}

	int saveSpecies(SourceConverter converter, List content, String imagesDir="", SpeciesBulkUpload sBulkUploadEntry=null) {
		converter.setLogAppender(fa);
		def startTime = System.currentTimeMillis()
		int noOfInsertions = 0;
		List speciesElements = [];
		int noOfSpecies = content.size();
		
		log.info " CONTENT SIZE " + noOfSpecies
		int processNameCount = 0
		boolean isAborted = false
		List contentSubLists = content.collate(BATCH_SIZE)
		contentSubLists.each { contentSubList ->
			if(isAborted || shouldAbortUpload(sBulkUploadEntry)){
				isAborted = true
				log.debug "Aborting bulk upload"
			}
			
			if(!isAborted){
				contentSubList.each { speciesContent ->
					Node speciesElement = converter.createSpeciesXML(speciesContent, imagesDir);
					if(speciesElement){
						speciesElements.add(speciesElement);
					}
				}
				def res = saveSpeciesElements(speciesElements, sBulkUploadEntry)
				speciesElements.clear()
				noOfInsertions += res.noOfInsertions;
				converter.addToSummary(res.summary);
				converter.addToSummary(res.species.collect{it.fetchLogSummary()}.join("\n"))
				converter.addToSummary("======================== FINISHED BATCH =============================\n")
				sBulkUploadEntry?.writeLog(res.idSummary)
				
			}
			
			processNameCount += contentSubList.size()
			if(processNameCount%5 == 0){
				println "------------------- processNameCount " + processNameCount
				cleanUpGorm();
			}
		}
		
		//Species.withNewTransaction { status ->
			sBulkUploadEntry.updateStatus(isAborted ? UploadLog.Status.ABORTED : UploadLog.Status.UPLOADED)
		//}
		
		log.info "================================ LOG ============================="
		println  converter.getLogs()
		log.info "=================================================================="
		
		log.info "Total time taken to save : "+(( System.currentTimeMillis()-startTime)/1000) + "(sec)"
		log.info "Total number of species that got added : ${noOfInsertions}"
		return noOfInsertions;
	}

	
	private boolean shouldAbortUpload(SpeciesBulkUpload sbu){
		sbu.refresh()
		log.debug "Current status " + sbu.status 
		return (sbu.status == UploadLog.Status.ABORTED)
	}
	
	private Map saveSpeciesElementsWrapper(List speciesElements, SpeciesBulkUpload sBulkUploadEntry=null) {
		def res = saveSpeciesElements(speciesElements, sBulkUploadEntry)
		if(res.noOfInsertions == speciesElements.size()){
			return res
		}
		
		//batch fail now running 1 by 1 for each species
		List<Species> species = new ArrayList<Species>();
		int noOfInsertions = 0;
		speciesElements.each { sEle ->
			def tmpRes = saveSpeciesElements([sEle], sBulkUploadEntry)
			noOfInsertions += tmpRes.noOfInsertions
			species.addAll(tmpRes.species)
		}
		
		return ['noOfInsertions':noOfInsertions, 'species':species];
	}
	
	private Map saveSpeciesElements(List speciesElements, SpeciesBulkUpload sBulkUploadEntry=null) {
		XMLConverter converter = new XMLConverter();
        converter.setLogAppender(fa);
		
		List species = []
		//StringBuilder sb = new StringBuilder()
		int noOfInsertions = 0;
		try {
			if(sBulkUploadEntry && (sBulkUploadEntry.uploadType == "namesUpload")){
				for(Node speciesElement : speciesElements) {
					String currSpeciesName = converter.fetchSpeciesName(speciesElement)
					currSpeciesName = currSpeciesName ?: "Name Not found in Species XML Skipping"
					//Species.withNewTransaction { status ->
						def s = converter.convertName(speciesElement)
						if(s){
							species.add(s)
							noOfInsertions++;
						}
					//}
				}
			}else{
				for(Node speciesElement : speciesElements) {
					//Species.withNewTransaction { status ->
						Species s = converter.convertSpecies(speciesElement)
						if(s)
							species.add(s);
					//}
				}
				noOfInsertions += saveSpecies(species);
			}
		}catch (org.springframework.dao.OptimisticLockingFailureException e) {
			//log.error "OptimisticLockingFailureException : $e.message"
			//log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			converter.addToSummary(e)
		}catch (org.springframework.dao.DataIntegrityViolationException e) {
			//log.error "DataIntegrityViolationException : $e.message"
			//log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			converter.addToSummary(e)
		} catch(ConstraintViolationException e) {
			//log.error "ConstraintViolationException : $e.message"
			//log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			converter.addToSummary(e)
		}catch(Exception e) {
			//log.error "Exception : $e.message"
			//log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			converter.addToSummary(e)
		}

		return ['noOfInsertions':noOfInsertions, 'species':species, 'summary': converter.getSummary(), 'log':converter.getLogs(), 'idSummary':converter.individualNameSumm];
	}

	/** 
	 * 
	 * @param species
	 * @return
	 */	
	int saveSpecies(List<Species> species) {
		log.info "Saving species : "+species.size()
		int noOfInsertions = saveSpeciesToDB(species);
		//All species should be saved before updating any group information or publishing it to search index
		postProcessSpecies(species);

		try {
			speciesSearchService.publishSearchIndex(species);
		} catch(e) {
			e.printStackTrace()
	 	}
		return noOfInsertions;
	} 
	
	
	private int saveSpeciesToDB(List<Species> species) {
		int noOfInsertions = 0;
		long startTime = System.currentTimeMillis();
		
		//Species.withNewTransaction { status ->		
			noOfInsertions += saveSpeciesBatch(species);			
		//}
		log.info "Time taken to save : "+(( System.currentTimeMillis()-startTime)/1000) + "(sec)"
		log.info "Number of species that got added : ${noOfInsertions}"
		return noOfInsertions;
	}

	/**
	 * 
	 * @param batch
	 * @return
	 */
	private int saveSpeciesBatch(List<Species> batch) {
		int noOfInsertions = 0;
		//List<Species> addedSpecies = [];

		for(Species s in batch) {
			try {
//                //def taxonConcept = TaxonomyDefinition.get(s.taxonConcept.id);
//                if(externalLinksService.updateExternalLinks(s.taxonConcept)) {
//                    s.taxonConcept = TaxonomyDefinition.get(s.taxonConcept.id);
////                    if(!s.taxonConcept.isAttached())
////                        s.taxonConcept.attach();
//                }
//
				//externalLinksService.updateExternalLinks(taxonConcept);
				
				s.percentOfInfo = calculatePercentOfInfo(s);
				if(!s.save()) {
					s.errors.allErrors.each {
						log.error it
						s.appendLogSummary(it)
					}
				} else {
					noOfInsertions++;
					//addedSpecies.add(s);
				}
				
			} catch(e) {
				s.appendLogSummary(e)
				e.printStackTrace()
			}
            
		}

		//log.debug "Saved species batch with insertions : "+noOfInsertions
		//TODO : probably required to clear hibernate cache
		//Reference : http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/
        return noOfInsertions;
	}

	/**
	 *
	 */
	def Species createSpeciesStub(TaxonomyDefinition taxonConcept) {
		if(!taxonConcept) return;
		return taxonConcept.createSpeciesStub()
	}


	/**
	 * 
	 */
	def postProcessSpecies(List<Species> species) {
		//TODO: got to move this to the end of taxon creation
		for(Species s : species) {
			try{
				s.afterInsert();
				def taxonConcept = s.taxonConcept;
				groupHandlerService.updateGroup(taxonConcept);
				def rCount = s.fetchResourceCount();
	            def rsfCount = s.fetchSpeciesFieldResourceCount();
	            if(rCount.size() > 0 || rsfCount > 0) {
					s.updateHasMediaValue(true);
	            }
				
				//this fucntion may time out some times
				externalLinksService.updateExternalLinks(s.taxonConcept)
				
				log.info "post processed spcecies ${s}"
			}
			catch(e) {
				log.error "$e.message"
				e.printStackTrace()
			}
		} 

		try{
			//namesLoaderService.syncNamesAndRecos(false);
		} catch(e) {
			e.printStackTrace()
		}

	}

	/**
	 *
	 */
	def computeInfoRichness() {
		log.info "Computing information richness"
		int limit=Species.count(), offset = 0, noOfUpdations = 0;
		def species;
		def startTime = System.currentTimeMillis()
		while(true) {
			species = Species.list(max:limit, offset:offset);
			if(!species) break;
			noOfUpdations += saveSpeciesBatch(species);
			species.clear();
			offset += limit;
		}
		log.info "Time taken to update info richness for species ${noOfUpdations} is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 * EOL automatically calculates some statistics about its pages. These statistics recalculate every day or two.
	 Richness Score
	 Richness Score is a composite of many different factors:
	 how much text a page has
	 how many multimedia or map files are available
	 how many different topics are covered
	 how many different sources contribute information
	 whether information has been reviewed or not
	 */
	float calculatePercentOfInfo(Species s) {
		//		int synonyms = Synonyms.countByTaxonConcept(s.taxonConcept);
		//		int commonNames = CommonNames.countByTaxonConcept(s.taxonConcept);
		//		def authClassification = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)
		//		int taxaHierarchies = TaxonomyRegistry.countByTaxonDefinitionAndClassification(s.taxonConcept, authClassification);
		//TODO: int occRecords =
		//TODO: observations =
		int textSize = 0;
		s.fields.each { field ->
			textSize += field.description?.length();
		}
		int noOfMultimedia = s.resources?.size()?:0;
		//int diffSources =
		//TODO: int reviewedFields =
		int richness = (s.fields?.size()?:0 )+ (s.globalDistributionEntities?.size()?:0 )+ (s.globalEndemicityEntities?.size()?:0) + (s.indianDistributionEntities?.size()?:0) + (s.indianEndemicityEntities?.size()?:0);
		richness += noOfMultimedia;
		//richness += textSize;
		return richness;

	}

	/**
	 *
	 */
	private void cleanUpGorm() {

		def hibSession = sessionFactory?.getCurrentSession();

		if(hibSession) {
			log.debug "Flushing and clearing session"
			try {
				hibSession.flush()
			} catch(ConstraintViolationException e) {
				e.printStackTrace()
			}
			//hibSession.clear()
		}
	}

    void setLogAppender(FileAppender fa) {
        if(fa) {
            this.fa = fa;
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }

    /**
    *
    **/
    FileAppender getLogFileAppender(String uploadDescriptor) {
        
        uploadDescriptor = Utils.cleanName(uploadDescriptor).replaceAll("\\\\s+","_");
        
        String user = springSecurityService.currentUser ?: 'script'
        String timestamp = String.format('%tF', new Date())
        String filename = "/tmp/logs/${user}_${uploadDescriptor}_${timestamp}.log";

        return new FileAppender(new PatternLayout("%d %m%n"), filename, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////Online upload related //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    def List getDataColumns(Language languageInstance){
    	List columnList = []
    	Field.findAllByLanguageAndCategoryNotEqual(languageInstance, "Catalogue of Life Taxonomy Hierarchy", [sort:"displayOrder", order:"asc"]).each {
			def tmpList = []
    		tmpList << it.concept
			if(it.category)
    			tmpList << it.category
    		if(it.subCategory)
    			tmpList << it.subCategory

    		columnList <<  tmpList.join("|")
    	}
    	return columnList
    }

    def getDataColumnsMap(Language languageInstance){
        def columns = [:]
        Field.findAllByLanguageAndCategoryNotEqual(languageInstance, "Catalogue of Life Taxonomy Hierarchy", [sort:"displayOrder", order:"asc"]).each {
            def tmpList = []
            tmpList << it.concept
            if(it.category)
                tmpList << it.category
                if(it.subCategory)
                    tmpList << it.subCategory

                    columns[it.id] = tmpList.join(" ")
        }
        return columns
    }

    File saveModifiedSpeciesFile(params){
        try{
        def gData //= JSON.parse(params.gridData)
        def headerMarkers = JSON.parse(params.headerMarkers)
        def orderedArray = JSON.parse(params.orderedArray);

        //storing current locale language information
        //this will be available in mappingConfig for each field
        if(headerMarkers) {
            def newHeaders = new JSONObject();
            headerMarkers.each {
                it.value.language = params.locale_language.id.toString();
                newHeaders.put(it.key, it.value);
            }
            headerMarkers = newHeaders;
        }

        String fileName = "speciesSpreadsheet"
        String uploadDir = "species"
        def ext = params.xlsxFileUrl.split("\\.")[-1];
        println "=========PARAMS XLSXURL  on which split ============= " + params.xlsxFileUrl
        println "=========THE SPLITED LIST ================ " + ext
        String xlsxFileUrl = params.xlsxFileUrl.replace("\"", "").trim().replaceFirst(config.speciesPortal.content.serverURL, config.speciesPortal.content.rootDir);
        String writeContributor = params.writeContributor.replace("\"","").trim();
        println "======= INITIAL UPLOADED XLSX FILE URL ======= " + xlsxFileUrl;
        fileName = fileName + "."+ext;
        println "===FILE NAME CREATED ================ " + fileName
        File file = utilsService.createFile(fileName , uploadDir, contentRootDir);
		println "=== NEW MODIFIED SPECIES FILE === " + file
        String contEmail = springSecurityService.currentUser.email;
        InputStream input = new FileInputStream(xlsxFileUrl);
        SpreadsheetWriter.writeSpreadsheet(file, input, gData, headerMarkers, writeContributor, contEmail, orderedArray);
        return file
        } catch(Exception e) {
            e.printStackTrace();
            log.error e.getMessage();
        }
    }



	//////////////////////////////////////// ROLL BACK //////////////////////////////
	def createRollBackEntry(Date startDate, Date endDate, String filePath, String imagesDir, String notes = null, String uploadType = null){
		return SpeciesBulkUpload.create(springSecurityService.currentUser, startDate, endDate, filePath, imagesDir, notes, uploadType)
		
	}
	
	def String abortBulkUpload(params){
		SpeciesBulkUpload sbu = SpeciesBulkUpload.read(params.id.toLong())
		
		if((sbu.author != springSecurityService.currentUser) && (!utilsService.isAdmin(springSecurityService.currentUser?.id))){
			log.error "Authentication failed for user " + springSecurityService.currentUser
			return "Authentication failed for user. Please login."
		}
		
		if(sbu.status == UploadLog.Status.RUNNING){
			log.debug "Aborting in progress..." + sbu
			sbu.updateStatus(UploadLog.Status.ABORTED)
			return "Sent request for abort."
		}else{
			log.error "Already uploaded or aborted or roll backed " + sbu
			return "Nothing to abort."
		}
	}
	
	def String rollBackUpload(params){
		SpeciesBulkUpload sbu = SpeciesBulkUpload.read(params.id.toLong())
		
		if((sbu.author != springSecurityService.currentUser) && (!utilsService.isAdmin(springSecurityService.currentUser?.id))){
			log.error "Authentication failed for user " + springSecurityService.currentUser
			return "Authentication failed for user. Please login."
		}
		
		if(sbu.status == UploadLog.Status.ROLLBACK){
			log.error "Already roll backed " + sbu
			return "Already rolled back." 
		}
		
		if(sbu.status == UploadLog.Status.RUNNING){
			log.error "Roll back in progress..." + sbu
			return "Rollback in progress..."
		}
		
		sbu.updateStatus(UploadLog.Status.RUNNING)
		log.debug "Changed to running status"
		
		SUser user = sbu.author
		Date start = sbu.startDate
		Date end = sbu.endDate
		
		//based on time stamp and user contributor get all the species fields and delete one by one
		List sFields = SpeciesField.withCriteria(){
			and{
				eq('uploader', user)
				between("lastUpdated", start, end)
			}
		}
		log.debug "Affected sFields " + sFields
		
		//XXX: Assuming no parallel upload by same user.
		List tRegistries = TaxonomyRegistry.withCriteria(){
			and{
				eq('uploader', user)
				between("uploadTime", start, end)
			}
		}
		log.debug "Affected taxonomy registries " + tRegistries
		
		List resourceList = Resource.withCriteria(){
			and{
				eq('uploader', user)
				between("uploadTime", start, end)
			}
		}
		if(!sFields && !tRegistries && !resourceList){
			log.debug "Nothing to rollback"
			sbu.updateStatus(UploadLog.Status.ROLLBACK)
			return "Nothing to rollback."
		}
		
		Collection<Species> sList = getAffectedSpecies(sFields, tRegistries)
		log.debug "Affected species list " + sList

		//XXX:remvoing species which going to be delted from usergroup first. 
		//This is done to avaoid cascade resaving of object. better way requried.
		Species.withTransaction{   
			sList.each { s->
				unpostFromUserGroup(s, sFields, user, sbu)
			}
		}
		
		Species.withTransaction{   
			sList.each { s->
				rollBackSpeciesUpdate(s, sFields, resourceList, user, sbu)
			}
			sbu.updateStatus(UploadLog.Status.ROLLBACK)
		}

		
		if(sbu.status == UploadLog.Status.ROLLBACK){
			log.debug "Successfully Rolled back."
			return "Successfully Rolled back."
		}else{
			sbu.updateStatus(UploadLog.Status.FAILED)
			log.debug "Rollback failed..."
			return "Rollback failed..."
		}
	}
	
	private Collection<Species> getAffectedSpecies(List sFields, List tRegs){
		def sList = sFields.collect{it.species}
		tRegs.each { TaxonomyRegistry tr ->
			def s = Species.get(tr.taxonDefinition.findSpeciesId())
			if(s)
				sList << s
		}
		
		return sList.unique() 
	}
 
 	boolean unpostFromUserGroup(Species s, List sFields, SUser user, SpeciesBulkUpload sbu) throws Exception {
        List specificSFields = SpeciesField.findAllBySpecies(s).collect{it} .unique()
		List sFieldToDelete = specificSFields.intersect(sFields)
		
		List taxonReg = TaxonomyRegistry.withCriteria(){
			and{
				eq('taxonDefinition', s.taxonConcept)
				if(user) eq('uploader', user)
				if(sbu) between("uploadTime", sbu.startDate, sbu.endDate)
			}
		}
        boolean canDelete = specificSFields.minus(sFieldToDelete).isEmpty() && TaxonomyRegistry.findAllByTaxonDefinition(s.taxonConcept).minus(taxonReg).isEmpty() ;
		if(canDelete){
			try{
				Featured.deleteFeatureOnObv(s, user)
				if(s.userGroups) {
					List ugIds = s.userGroups.collect {it.id}
					ugIds.each { ugId ->
						def ug = UserGroup.get(ugId) 
						log.debug "Removing species $s from userGroup  " + ug
						ug.removeFromSpecies(s)
						ug.save(flush:true, failOnError:true)
					}
				}
				if(!s.save(flush:true)) {
					s.errors.allErrors.each { log.error it }
                    return false
				}
                return true;
			}
			catch (Exception e) {
				log.error "Error in unposting from usergroup"
				e.printStackTrace()
				throw e
			}
		}
        log.debug "Can't delete species"
        return false
 	}
	
	private void rollBackSpeciesUpdate(Species s, List sFields, List resources, SUser user, SpeciesBulkUpload sbu) throws Exception {
		List specificSFields = SpeciesField.findAllBySpecies(s).collect{it} .unique()
		List sFieldToDelete = specificSFields.intersect(sFields)
		
		log.debug "Removing species field from species ${s} following sFields  ${sFieldToDelete}"
		sFieldToDelete.each { sf ->
			s.removeFromFields(sf)
			def ge = GeographicEntity.read(sf.id) 
			if(ge){
				s.removeFromGlobalDistributionEntities(ge)
				s.removeFromGlobalEndemicityEntities(ge)
				s.removeFromIndianDistributionEntities(ge)
				s.removeFromIndianEndemicityEntities(ge)
			}
		}
		
		//delete species field only when species bulk upload object given to roll back
		if(sbu){
			sFieldToDelete.each { sf ->
				sf.refresh()
				log.info "Deleting ${sf}"
				sf.delete(flush:true)
			}
		}
		
		sFields.removeAll(sFieldToDelete)
	
        s.taxonConcept.speciesId = null;
        s.taxonConcept.save();
		//removing taxonomy hirarchy if added due to this upload
		List taxonReg = TaxonomyRegistry.withCriteria(){
			and{
				eq('taxonDefinition', s.taxonConcept)
				if(user) eq('uploader', user)
				if(sbu) between("uploadTime", sbu.startDate, sbu.endDate)
			}
		}
		
		if(sbu){
			log.debug "Deleting taxon registry because rollback is called " + sbu
			log.debug "Taxonomy registry to be deleted as  " + taxonReg
			taxonReg.each { tr ->
				log.debug "Deleting  $tr"
				tr.delete(flush:true)
			}
		}
	
        def tempRes = s.resources.intersect(resources);
		if(sbu){
	        tempRes.each { res ->
	            log.debug "Removing resource " + res
	            s.removeFromResources(res)
	        }
		}
		
		boolean canDelete = specificSFields.minus(sFieldToDelete).isEmpty() && TaxonomyRegistry.findAllByTaxonDefinition(s.taxonConcept).minus(taxonReg).isEmpty() ;
		if(canDelete){
			log.debug "Deleting species ${s} "
			deleteSpecies(s, sbu)
			return
		}

		if(!s.save(flush:true)){
			s.errors.allErrors.each { log.error it }
		}
				
	}
	
	private boolean deleteSpecies(Species s, SpeciesBulkUpload sbu) throws Exception { 
		try{
//			Recommendation.findAllByTaxonConcept(s.taxonConcept).each { reco ->
//				reco.taxonConcept = null
//				reco.save(flush:true)
//			}
//			CommonNames.findAllByTaxonConcept(s.taxonConcept).each { cn ->
//				cn.delete()
//			}
//			Synonyms.findAllByTaxonConcept(s.taxonConcept).each { sn ->
//				sn.delete()
//			}
//			TaxonomyRegistry.findAllByTaxonDefinition(s.taxonConcept).each { tr ->
//				tr.delete(flush:true)
//			}
//			SpeciesGroupMapping.findAllByTaxonConcept(s.taxonConcept).each { sgm ->
//				sgm.delete()
//			}
			
			if(sbu){
				SpeciesPermission.findAllByTaxonConcept(s.taxonConcept).each { sp ->
					sp.delete(flush:true)
				}
			}
			
			if(sbu && s.resources){
				def ids = s.resources.collect { it.id}
				ids.each { 
					def r = Resource.get(it)
					s.removeFromResources(r)
				}
			}
			log.debug "Reverting changes of species before delete $s ===="
			if(sbu){
				if(s.delete(flush:true)) {
					log.debug "Deleted ${s}"
				}
			}else{
				log.debug "Flagging species as deleted"
				s.isDeleted = true
				if(!s.save(flush:true)){
					s.errors.allErrors.each { log.error it }
				}
			}
			speciesSearchService.delete(s.id);
			return true
		}catch (Exception e) {
			log.error "Error in Delete/Reverting ${s}"
			e.printStackTrace()
			throw e
		}
		return false
	}
	
	
	boolean deleteSpeciesWrapper(Species s, SUser user){
		List sFields = SpeciesField.findAllBySpecies(s).collect{it} .unique()
		boolean success = false
		
		try{	
			Species.withTransaction{
				success = unpostFromUserGroup(s, sFields, user, null);
			}
			Species.withTransaction{
				if(success){
					rollBackSpeciesUpdate(s, sFields, s.resources.collect{it}, user, null)
				}
			}
		}catch(e){
			log.error e.printStackTrace()
			return false
		}
		return true	
	}		
}
