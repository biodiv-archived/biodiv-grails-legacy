package speciespage

import java.util.List;

import java.util.List

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.hibernate.exception.ConstraintViolationException;

import species.Classification
import species.CommonNames;
import species.Country
import species.Field
import species.Language
import species.Species
import species.SpeciesField;
import species.Synonyms;
import species.TaxonomyDefinition;
import species.License.LicenseType
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter

class SpeciesService {

	private static final log = LogFactory.getLog(this);

	static transactional = false

	def grailsApplication;
	def groupHandlerService;
	def namesLoaderService;
	def sessionFactory;
	def externalLinksService;
	def searchService;

	static int BATCH_SIZE = 10;
	int noOfFields = Field.count();

	/**
	 * 
	 * @return
	 */
	def loadData() {
		int noOfInsertions = 0;

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Dung_beetle_Species_pages_IBP_v13.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/dungbeetles_mapping.xlsx", 0, 0, 0, 0);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Trees_descriptives_prabha_final_6.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/ifp_tree_mapping_v2.xlsx", 0, 0, 0, 2);

		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Bats/WG_bats_account_01Nov11_sanjayMolur.xls", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/WG_bats_account_01Nov11_sanjayMolurspecies_mapping_v2.xlsx", 0, 0, 0, 0);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages";
		noOfInsertions += uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages/species accounts188_v2.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/speciesaccount188_mapping_v1.xlsx", 0, 0, 0, 0);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone";
		String mappingFile = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone/keystone_mapping_v1.xlsx";
		noOfInsertions += uploadKeyStoneData("jdbc:mysql://localhost:3306/ezpz", "sravanthi", "sra123", mappingFile, 0, 0);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango";
		noOfInsertions += uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango/MangoMangifera_indica_prabha_v4 (copy).xlsx", 0, 0, 1, 4);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin";
		noOfInsertions += uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin/GreyFrancolin_v4.xlsx", 0, 0, 1, 4);

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/images";
		noOfInsertions += uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/RufousWoodepecker_v4_1.xlsm");

		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/png ec";
		noOfInsertions += uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/EurasianCurlew_v4_2.xlsm");

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
	int uploadMappedSpreadsheet (String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo) {
		log.info "Uploading mapped spreadsheet : "+file;
		List<Species> species = MappedSpreadsheetConverter.getInstance().convertSpecies(file, mappingFile, mappingSheetNo, mappingHeaderRowNo, contentSheetNo, contentHeaderRowNo);
		return saveSpecies(species);
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

	/**
	 * 
	 * @param species
	 * @return
	 */
	int saveSpecies(List species) {
		log.info "Saving species : "+species.size()
		int noOfInsertions = 0;
		def startTime = System.currentTimeMillis()
		List <Species> batch =[]
		species.each {
			batch.add(it);
			if(batch.size() > BATCH_SIZE){
				noOfInsertions += saveSpeciesBatch(batch);
				batch.clear();
				return
			}
		}
		if(batch.size() > 0) {
			noOfInsertions += saveSpeciesBatch(batch);
			batch.clear();
		}

		log.info "Time taken to save : "+(( System.currentTimeMillis()-startTime)/1000) + "(sec)"

		//log.debug "Publishing to search index"

		try {
			searchService.publishSearchIndex(species);
		} catch(e) {
			e.printStackTrace()
		}

		cleanUpGorm();

		//postProcessSpecies(species);

		return noOfInsertions;
	}

	/**
	 * 
	 * @param batch
	 * @return
	 */
	private int saveSpeciesBatch(List<Species> batch) {
		int noOfInsertions = 0;
		Species.withTransaction {
			for(Species s in batch) {
				//externalLinksService.updateExternalLinks(s.taxonConcept);
				s.percentOfInfo = calculatePercentOfInfo(s);
				if(!s.save()) {
					s.errors.allErrors.each { log.error it }
				} else {
					noOfInsertions++;
				}
			}
		}
		log.debug "Saved batch with insertions : "+noOfInsertions
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
			groupHandlerService.updateGroups(species);
		} catch(e) {
			e.printStackTrace()
		}

		try{
			namesLoaderService.syncNamesAndRecos(false);
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
		
		int noOfMultimedia = s.resources.size();
		//int diffSources = 
		//TODO: int reviewedFields = 
		int richness = s.fields.size() + s.globalDistributionEntities.size() + s.globalEndemicityEntities.size() + s.indianDistributionEntities.size() + s.indianEndemicityEntities.size();
		richness += noOfMultimedia;
		richness += textSize;
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

}
