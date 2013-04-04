package speciespage

import java.util.List;

import java.util.List

import org.apache.commons.logging.LogFactory
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.hibernate.exception.ConstraintViolationException;

import species.Classification
import species.CommonNames;
import species.Contributor;
import species.Country
import species.Field
import species.Habitat;
import species.Language
import species.Resource;
import species.Species
import species.SpeciesField;
import species.Synonyms;
import species.TaxonomyDefinition;
import species.License.LicenseType
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader
import species.participation.Observation;
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.NewSimpleSpreadsheetConverter
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter
import species.utils.Utils;

import species.sourcehandler.exporter.DwCAExporter

class SpeciesService {

	private static final log = LogFactory.getLog(this);

	static transactional = false

	def grailsApplication;
	def groupHandlerService;
	def namesLoaderService;
	def sessionFactory;
	def externalLinksService;
	def speciesSearchService;
	def namesIndexerService;
	def observationService;
	def springSecurityService

	static int BATCH_SIZE = 10;
	int noOfFields = Field.count();

	/**
	 * 
	 * @return
	 */
	def loadData() {
		int noOfInsertions = 0;

		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Dung_beetle_Species_pages_IBP_v13.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/dungbeetles_mapping.xlsx", 0, 0, 0, 0);
		//
		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Trees_descriptives_prabha_final_6.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/ifp_tree_mapping_v2.xlsx", 0, 0, 0, 2);

		//		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Bats/WG_bats_account_01Nov11_sanjayMolur.xls", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/WG_bats_account_01Nov11_sanjayMolurspecies_mapping_v2.xlsx", 0, 0, 0, 0);
		//
		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages";
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages/species accounts188_v2.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/speciesaccount188_mapping_v1.xlsx", 0, 0, 0, 0);

		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone";
		//		String mappingFile = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone/keystone_mapping_v1.xlsx";
		//		noOfInsertions += uploadKeyStoneData("jdbc:mysql://localhost:3306/ezpz", "sravanthi", "sra123", mappingFile, 0, 0);

		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango";
		//		noOfInsertions += uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango/MangoMangifera_indica_prabha_v4 (copy).xlsx", 0, 0, 1, 4);
		//
		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin";
		//		noOfInsertions += uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin/GreyFrancolin_v4.xlsx", 0, 0, 1, 4);
		//
		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/images";
		//		noOfInsertions += uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/RufousWoodepecker_v4_1.xlsm");
		//
		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/png ec";
		//		noOfInsertions += uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/EurasianCurlew_v4_2.xlsm");
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/primates.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/primates_mappingfile.xls", 0, 0, 0, 0);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/1.5/photos_bryo/";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/1.5/abctrust_mosses.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/1.5/abctrust_mosses_mappingfile.xls", 0, 0, 0, 0);

		//		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ranwa/uploadready/";
		//		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ranwa/uploadready/plant_speciesimages.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ranwa/uploadready/plant_speciesimages_mapping.xlsx", 0, 0, 0, 0);

		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/mcccollege/uploadready/Database_on_Diots_of_Western_Ghats_1.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/mcccollege/uploadready/Database_on_Diots_of_Western_Ghats_mappingfile.xls", 0, 0, 0, 0);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/climbers_images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/climbers.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/climbers_mapping.xlsx", 0, 0, 0, 0,1);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/epi_saprophytes_images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/epi_saprophytes.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/epi_saprophytes_mapping.xlsx", 0, 0, 0, 0,1);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/herbs_images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/herbs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/herbs_mapping.xlsx", 0, 0, 0, 0,1);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/shrubs_images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/shrubs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/shrubs_mapping.xlsx", 0, 0, 0, 0,1);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/trees_images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/trees.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/trees_mapping.xlsx", 0, 0, 0, 0,1);

		return noOfInsertions;
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
	int uploadMappedSpreadsheet (String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo, int imageMetaDataSheetNo = -1) {
		log.info "Uploading mapped spreadsheet : "+file;

		List<Species> species = new ArrayList<Species>();
		MappedSpreadsheetConverter mappedSpreadsheetConverter = MappedSpreadsheetConverter.getInstance();


		def startTime = System.currentTimeMillis()

		List<Map> mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		List<Map> imagesMetaData;
		if(imageMetaDataSheetNo && imageMetaDataSheetNo  >= 0) {
			imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetaDataSheetNo, 0);
		}

		int noOfInsertions = 0;
		def speciesElements = [];
		for(int i=0; i<content.size(); i++) {
			if(speciesElements.size() == BATCH_SIZE) {
				noOfInsertions += saveSpeciesElements(speciesElements);
				speciesElements.clear();
				cleanUpGorm();
			}

			Map speciesContent = content.get(i);
			Node speciesElement = mappedSpreadsheetConverter.createSpeciesXML(speciesContent, mappingConfig, imagesMetaData);
			if(speciesElement)
				speciesElements.add(speciesElement);
		}
		if(speciesElements.size() > 0) {
			noOfInsertions += saveSpeciesElements(speciesElements);
			speciesElements.clear();
			cleanUpGorm();
		}

		log.info "Total time taken to save : "+(( System.currentTimeMillis()-startTime)/1000) + "(sec)"
		log.info "Total number of species that got added : ${noOfInsertions}"
		//List<Species> species = MappedSpreadsheetConverter.getInstance().convertSpecies(file, mappingFile, mappingSheetNo, mappingHeaderRowNo, contentSheetNo, contentHeaderRowNo);
		return noOfInsertions;
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
	int uploadSpreadsheet (String file, int contentSheetNo, int contentHeaderRowNo, int imageMetadataSheetNo, int imageMetaDataHeaderRowNo) {
		log.info "Uploading spreadsheet : "+file;
		List<Species> species = SpreadsheetConverter.getInstance().convertSpecies(file, contentSheetNo, contentHeaderRowNo, imageMetadataSheetNo, imageMetaDataHeaderRowNo);
		return saveSpecies(species);
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	int uploadNewSpreadsheet (String file) {
		log.info "Uploading new spreadsheet : "+file;
		List<Species> species = NewSpreadsheetConverter.getInstance().convertSpecies(file);
		return saveSpecies(species);
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	int uploadNewSimpleSpreadsheet (String file) {
		log.info "Uploading new simple spreadsheet : "+file;
		List<Species> species = NewSimpleSpreadsheetConverter.getInstance().convertSpecies(file);
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

	private int saveSpeciesElements(List speciesElements) {
		XMLConverter converter = new XMLConverter();
		List<Species> species = new ArrayList<Species>();

		int noOfInsertions = 0;
		try {
			Species.withTransaction { status ->
				for(Node speciesElement : speciesElements) {
					Species s = converter.convertSpecies(speciesElement)
					if(s)
						species.add(s);
				}
				noOfInsertions += saveSpecies(species);
				species.clear();
			}
		}catch (org.springframework.dao.OptimisticLockingFailureException e) {
			log.error "OptimisticLockingFailureException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			noOfInsertions = 0
		}catch (org.springframework.dao.DataIntegrityViolationException e) {
			log.error "OptimisticLockingFailureException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			noOfInsertions = 0
		} catch(ConstraintViolationException e) {
			log.error "ConstraintViolationException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
			noOfInsertions = 0
		}
		return noOfInsertions;
	}

	/** 
	 * 
	 * @param species
	 * @return
	 */
	int saveSpecies(List species) {
		log.info "Saving species : "+species.size()
		int noOfInsertions = 0;

		def startTime = System.currentTimeMillis()
		def addedSpecies = []
		List <Species> batch =[]
		species.each {
			batch.add(it);
			if(batch.size() > BATCH_SIZE){
				def newlyAddedSpecies = saveSpeciesBatch(batch);
				noOfInsertions += newlyAddedSpecies.size();
				addedSpecies.addAll(newlyAddedSpecies);
				batch.clear();
				return
			}
		}
		if(batch.size() > 0) {
			def newlyAddedSpecies = saveSpeciesBatch(batch);
			noOfInsertions += newlyAddedSpecies.size();
			addedSpecies.addAll(newlyAddedSpecies);
			batch.clear();
		}

		log.info "Time taken to save : "+(( System.currentTimeMillis()-startTime)/1000) + "(sec)"
		log.info "Number of species that got added : ${noOfInsertions}"
		//log.debug "Publishing to search index"

		postProcessSpecies(addedSpecies);

		try {
			speciesSearchService.publishSearchIndex(addedSpecies);
		} catch(e) {
			e.printStackTrace()
		}



		return noOfInsertions;
	}

	/**
	 * 
	 * @param batch
	 * @return
	 */
	private List saveSpeciesBatch(List<Species> batch) {
		//int noOfInsertions = 0;
		List<Species> addedSpecies = [];



		for(Species s in batch) {
			try {
				externalLinksService.updateExternalLinks(s.taxonConcept);
			} catch(e) {
				e.printStackTrace()
			}

			s.percentOfInfo = calculatePercentOfInfo(s);


			//			if(s != null && s.id) {
			//				if(!s.isAttached()) {
			//					//merging only if it was already a persistent object
			//					s = s.merge();
			//				}
			//			} else {
			//				log.debug "Saving new object"
			//			}

			if(!s.save()) {
				s.errors.allErrors.each { log.error it }
			} else {
				//noOfInsertions++;
				addedSpecies.add(s);
			}

		}


		//log.debug "Saved species batch with insertions : "+noOfInsertions
		//TODO : probably required to clear hibernate cache
		//Reference : http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/
		return addedSpecies;
	}

	/**
	 *
	 */
	def createSpeciesStub(TaxonomyDefinition taxonConcept) {
		if(!taxonConcept) return;

		XMLConverter converter = new XMLConverter();

		Species s = new Species();
		s.taxonConcept = taxonConcept
		s.title = s.taxonConcept.italicisedForm;
		s.guid = converter.constructGUID(s);

		return s;
	}


	/**
	 * 
	 */
	def postProcessSpecies(List<Species> species) {
		//TODO: got to move this to the end of taxon creation
		try{
			groupHandlerService.updateGroups(species, false);
		} catch(e) {
			e.printStackTrace()
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
	protected float calculatePercentOfInfo(Species s) {
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
		int richness = s.fields?.size()?:0 + s.globalDistributionEntities?.size()?:0 + s.globalEndemicityEntities?.size()?:0 + s.indianDistributionEntities?.size()?:0 + s.indianEndemicityEntities?.size()?:0;
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
			hibSession.clear()
		}
	}

	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = speciesSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Species Pages"]);
		}
		return result;
	}

	def search(params) {
		def result;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
		def queryParams = [:]
		def activeFilters = [:]

		NamedList paramsList = new NamedList();
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";

		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap) {
			params.aq.each { key, value ->
				queryParams["aq."+key] = value;
				activeFilters["aq."+key] = value;
				if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
					if(i++ == 0) {
						aq = key + ': ('+value+')';
					} else {
						aq = aq + " AND " + key + ': ('+value+')';
					}
				}
			}
		}
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}

		def offset = params.offset ? params.long('offset') : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1 && sort.indexOf(' asc') == -1 ) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		queryParams["max"] = max
		queryParams["offset"] = offset

		paramsList.add('fl', params['fl']?:"id");

		if(params.sGroup) {
			params.sGroup = params.sGroup.toLong()
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			} else{
				paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}

		//		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
		//			paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
		//			queryParams["habitat"] = params.habitat
		//			activeFilters["habitat"] = params.habitat
		//		}
		//		if(params.tag) {
		//			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
		//			queryParams["tag"] = params.tag
		//			queryParams["tagType"] = 'species'
		//			activeFilters["tag"] = params.tag
		//		}
		//		if(params.user){
		//			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
		//			queryParams["user"] = params.user.toLong()
		//			activeFilters["user"] = params.user.toLong()
		//		}
		if(params.name) {
			paramsList.add('fq', searchFieldsConfig.NAME+":"+params.name);
			queryParams["name"] = params.name
			activeFilters["name"] = params.name
		}

		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					//AS we dont have selecting species for group ... we are ignoring this filter
					//paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}

		if(params.query && params.startsWith && params.startsWith != "A-Z"){
			params.query = params.query + " AND "+searchFieldsConfig.TITLE+":"+params.startsWith+"*"
			//paramsList.add('fq', searchFieldsConfig.TITLE+":"+params.startsWith+"*");
			queryParams["startsWith"] = params.startsWith
			activeFilters["startsWith"] = params.startsWith
		}
		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = speciesSearchService.search(paramsList);
			List<Species> speciesInstanceList = new ArrayList<Species>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def speciesInstance = Species.get(doc.getFieldValue("id"));
				if(speciesInstance)
					speciesInstanceList.add(speciesInstance);
			}

			//queryParams = queryResponse.responseHeader.params
			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), speciesInstanceList:speciesInstanceList, snippets:queryResponse.getHighlighting()]
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}

	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase('lastrevised'))
			return true;
		return false;
	}
	/**
	 * export species data
	 */
	def exportSpeciesData(String directory) {
		DwCAExporter.getInstance().exportSpeciesData(directory)
	}

	/**
	 * export species data
	 */
	def exportSpeciesData(String directory, List<Species> species) {
		DwCAExporter.getInstance().exportSpeciesData(directory, species)
	}

	def updateContributor(long contributorId, long speciesFieldId, def value, String type) {
		if(!value) {
			return [success:false, msg:"Field content cannot be empty"]
		}

		Contributor oldContrib = Contributor.read(contributorId);
		if(!oldContrib) {
			return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
		} else if(oldContrib.name == value) {
			return [success:true, msg:"Nothing to change"]
		}

		SpeciesField speciesField = SpeciesField.get(speciesFieldId);
		if(!speciesField) {
			return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
		}

		SpeciesField.withTransaction { status ->
			Contributor c = (new XMLConverter()).getContributorByName(value, true);
			if(!c) {
				return [success:false, msg:"Error while updating ${type}"]
			} else {
				if(type == 'contributor') {
					speciesField.removeFromContributors(oldContrib);
					speciesField.addToContributors(c);
				} else if (type == 'attributor') {
					speciesField.removeFromAttributors(oldContrib);
					speciesField.addToAttributors(c);
				}
				if(!speciesField.save()) {
					speciesField.errors.each { log.error it }
					return [success:false, msg:"Error while updating ${type}"]
				}
				return [success:true, msg:""]
			}
		}
	}

	def updateDescription(long id, def value) {
		if(!value) {
			return [success:false, msg:"Field content cannot be empty"]
		}

		SpeciesField c = SpeciesField.get(id)
		if(!c) {
			return [success:false, msg:"SpeciesField with id ${id} is not found"]
		} else {
			SpeciesField.withTransaction {
				c.description = value.trim()
				if (!c.save()) {
					c.errors.each { log.error it }
					return [success:false, msg:"Error while updating species field name"]
				}
			}
			return [success:true, msg:""]
		}
	}

	private def createImagesXML(params) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node images = new Node(resources, "images");
		List files = [];
		List titles = [];
		List licenses = [];
		params.each { key, val ->
			int index = -1;
			if(key.startsWith('file_')) {
				index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));

			}
			if(index != -1) {
				files.add(val);
				titles.add(params.get('title_'+index));
				licenses.add(params.get('license_'+index));
			}
		}
		files.eachWithIndex { file, key ->
			Node image = new Node(images, "image");
			if(file) {
				File f = new File(uploadDir, file);
				new Node(image, "fileName", f.absolutePath);
				//new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", titles.getAt(key));
				new Node(image, "contributor", params.author.username);
				new Node(image, "license", licenses.getAt(key));
			} else {
				log.warn("No reference key for image : "+key);
			}
		}
		return resources;
	}

	private def createVideoXML(params) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node videos = new Node(resources, "videos");

		Node video = new Node(videos, "video");
		new Node(video, 'fileName', 'video')
		new Node(video, "source", params.video);
		new Node(video, "caption", params.description);
		new Node(video, "contributor", springSecurityService.currentUser.name);
		new Node(video, "attributor", params.attributor);
		new Node(video, "license", "CC BY");

		return resources;
	}

	/**
	 * 
	 */
	private List<Resource> saveResources(Node resourcesXML, String relImagesContext) {
		XMLConverter converter = new XMLConverter();
		converter.setResourcesRootDir(grailsApplication.config.speciesPortal.resources.rootDir);
		return converter.createMedia(resourcesXML, relImagesContext);
	}
}
