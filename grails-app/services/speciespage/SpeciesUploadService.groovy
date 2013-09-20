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

import species.Contributor;
import species.Field
import species.Resource;
import species.Species
import species.SpeciesField;
import species.TaxonomyDefinition;
import species.formatReader.SpreadsheetReader
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.NewSimpleSpreadsheetConverter
import species.sourcehandler.SourceConverter;
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter
import species.utils.Utils;
import java.text.SimpleDateFormat;
import species.sourcehandler.exporter.DwCAExporter
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;

class SpeciesUploadService {

    private static def log = LogFactory.getLog(this);
    private FileAppender fa;

	static transactional = false

    //prototype - A new service is created every time it is injected into another class
    static scope = "prototype"

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

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/climbers_images";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/climbers.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/climbers_mapping.xlsx", 0, 0, 0, 0,1);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/epi_saprophytes_images";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/epi_saprophytes.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/epi_saprophytes_mapping.xlsx", 0, 0, 0, 0,1);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/herbs_images";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/herbs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/herbs_mapping.xlsx", 0, 0, 0, 0,1);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/shrubs_images";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/shrubs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/shrubs_mapping.xlsx", 0, 0, 0, 0,1);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/rawdata/forest_plants_HTML/trees_images";
		//noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/trees.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/keystone/inprocess/trees_mapping.xlsx", 0, 0, 0, 0,1);

        //grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/careearth/1.0";
        //noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/careearth/1.0/species accounts188.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/careearth/1.0/speciesaccount188_mapping.xlsx", 0, 0, 0, 0, 1);

     /*   grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/aparnawatve/uploadready/images";
        noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/aparnawatve/uploadready/aparnawatve.xlsx");

        grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/thomas/uploadready/bird_images";
        noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/thomas/uploadready/BirdsspeciesPages.xlsx");

        grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/odonates.xls";
        noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/odonates.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/odonates_mapping.xls", 0, 0, 0, 0,1);


        grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills";
        noOfInsertions += speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/associate_plants_of_grasslands_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills_mapping.xlsx", 0, 0, 0, 0,-1);

        grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/grasses_of_palni_hills";
        noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/grasses_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/PHCC/uploadready/grasses_of_palni_hills_mapping.xlsx", 0, 0, 0, 0,-1);
*/
        //noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/thomas/1.6/MammalsspeciesPages.xlsx",grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/thomas/1.6/images");

      //  noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/Northeast Butterflies-RG4.xlsx",grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/NE_Butterflies_RG4");


        noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/1.6/indian_molluscs_asr_cr1.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/1.6/molluscs_images");
        noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/1.6/indian_molluscs_asr_cr2.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/1.6/molluscs_images");
        noOfInsertions += uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.6/NortheastButterflies-RG25.xlsx",grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.6/NE_Butterflies_RG25");

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
	int uploadMappedSpreadsheet (String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo, int imageMetaDataSheetNo = -1, String imagesDir="") {
        log.info "Uploading mapped spreadsheet : "+file;

		List<Species> species = new ArrayList<Species>();
		MappedSpreadsheetConverter converter = new MappedSpreadsheetConverter();


		converter.mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		List<Map> imagesMetaData;
		if(imageMetaDataSheetNo && imageMetaDataSheetNo  >= 0) {
			converter.imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetaDataSheetNo, 0);
		}

		return saveSpecies(converter, content, imagesDir);
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

	int saveSpecies(SourceConverter converter, List content, String imagesDir="") {
        converter.setLogAppender(fa);
		def startTime = System.currentTimeMillis()
		int noOfInsertions = 0;
		def speciesElements = [];
		int noOfSpecies = content.size();
		for(int i=0; i<noOfSpecies; i++) {
			if(speciesElements.size() == BATCH_SIZE) {
				noOfInsertions += saveSpeciesElements(speciesElements);
				speciesElements.clear();
				cleanUpGorm();
			}

			def speciesContent = content.get(i);
			Node speciesElement = converter.createSpeciesXML(speciesContent, imagesDir);
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
		return noOfInsertions;
	}

	private int saveSpeciesElements(List speciesElements) {
		XMLConverter converter = new XMLConverter();
        converter.setLogAppender(fa);

		List<Species> species = new ArrayList<Species>();


		int noOfInsertions = 0;
		try {
			List<Species> addedSpecies = new ArrayList<Species>();
			//Species.withTransaction { status ->
				for(Node speciesElement : speciesElements) {
					Species s = converter.convertSpecies(speciesElement)
					if(s)
						species.add(s);
				}
				noOfInsertions += saveSpecies(species);
				//addedSpecies = saveSpeciesBatch(species);
				//noOfInsertions += addedSpecies.size()
			//}

		}catch (org.springframework.dao.OptimisticLockingFailureException e) {
			log.error "OptimisticLockingFailureException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
		}catch (org.springframework.dao.DataIntegrityViolationException e) {
			log.error "DataIntegrityViolationException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
		} catch(ConstraintViolationException e) {
			log.error "ConstraintViolationException : $e.message"
			log.error "Trying to add species in the batch are ${species*.taxonConcept*.name.join(' , ')}"
			e.printStackTrace()
		}




		return noOfInsertions;
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
		
		Species.withTransaction { status ->		
			noOfInsertions += saveSpeciesBatch(species);			
		}
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
                //def taxonConcept = TaxonomyDefinition.get(s.taxonConcept.id);
                if(externalLinksService.updateExternalLinks(s.taxonConcept)) {
                    s.taxonConcept = TaxonomyDefinition.get(s.taxonConcept.id);
//                    if(!s.taxonConcept.isAttached())
//                        s.taxonConcept.attach();
                }

				//externalLinksService.updateExternalLinks(taxonConcept);
			} catch(e) {
				e.printStackTrace()
			}

			s.percentOfInfo = calculatePercentOfInfo(s);

			if(!s.save()) {
				s.errors.allErrors.each { log.error it }
			} else {
				noOfInsertions++;
				//addedSpecies.add(s);
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
			//TaxonomyDefinition.withTransaction { status ->
			for(Species s : species) {
				def taxonConcept = s.taxonConcept;
				if(!taxonConcept.isAttached()) {
					taxonConcept.attach();
				}
				groupHandlerService.updateGroup(taxonConcept);
			}
			//}
		} catch(e) {
			log.error "$e.message"
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

}
