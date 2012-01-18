package speciespage

import org.apache.commons.logging.LogFactory;

import species.Classification
import species.Country
import species.DataLoader
import species.Field
import species.Language
import species.License.LicenseType
import species.formatReader.SpreadsheetReader
import species.groups.SpeciesGroup
import species.sourcehandler.XMLConverter

class SetupService {

	private static final log = LogFactory.getLog(this);

	static transactional = false

	def grailsApplication;
	def groupHandlerService;
	def taxonService;
	def speciesService;

	/**
	 *
	 */
	def setupDefs() {
		uploadFields(grailsApplication.config.speciesPortal.data.rootDir+"/templates/DefinitionsAbridged_prabha.xlsx");
		uploadLanguages(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2.csv");
		uploadCountries(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Countries_ISO-3166-1.csv");
		uploadClassifications(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Classifications.xlsx", 0, 0);
		uploadLicences();

		def allGroup = new SpeciesGroup(name:"All");
		allGroup.save(flush:true, failOnError:true);
		def othersGroup = new SpeciesGroup(name:"Others", parentGroup:allGroup);
		othersGroup.save(flush:true, failOnError:true);
		groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);

		//speciesService.loadData();
		//taxonService.loadTaxon();
	}

	/**
	 *
	 * @param fieldDefinitionsFile
	 */
	void uploadFields(String fieldDefinitionsFile) {
		log.debug "Uploading field definitions"
		populateFields(fieldDefinitionsFile, 0, 0);
	}

	/**
	 *
	 * @param file
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 */
	void populateFields(String file, int contentSheetNo, int contentHeaderRowNo) {
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		for (Map row : content) {
			String concept = row.get("concept");
			String category = row.get("category");
			String subCategory = row.get("subcategory");
			String description = row.get("description");
			int displayOrder = Math.round(Float.parseFloat(row.get("s.no.")));

			def fieldCriteria = Field.createCriteria();

			Field field = fieldCriteria.get {
				and {
					eq("concept", concept);
					category ? eq("category", category) : isNull("category");
					subCategory ? eq("subCategory", subCategory) : isNull("subCategory");
				}
			}

			if(!field) {
				field = new Field(concept:concept, category:category, subCategory:subCategory, displayOrder:displayOrder, description:description);
				if(!field.save(flush:true, failOnError: true)) {
					field.errors.each { log.error it }
				}
			}
		}
	}

	/**
	 *
	 * @param languagesFile
	 */
	void uploadLanguages (String languagesFile) {
		log.debug "Uploading languages"
		new File(languagesFile).splitEachLine("\\t") {
			def fields = it;
			def lang = new Language (threeLetterCode:fields[0].replaceAll("\"",""), twoLetterCode:fields[1].replaceAll("\"",""), name:fields[2].replaceAll("\"",""));
			if(!lang.save(flush:true))
				lang.errors.each { log.error it; }
		}
	}

	/**
	 *
	 * @param countriesFile
	 */
	void uploadCountries (String countriesFile) {
		log.debug "Uploading countries"
		new File(countriesFile).splitEachLine("\\t") {
			def fields = it;
			def country = new Country(countryName:fields[1].replaceAll("\"",""), twoLetterCode:fields[0].replaceAll("\"",""));
			if(!country.save(flush:true))
				country.errors.each { log.error it; }
		}
	}

	/**
	 *
	 * @param classificationsFile
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 */
	void uploadClassifications (String classificationsFile, int contentSheetNo, int contentHeaderRowNo) {
		log.debug "Uploading classifications"
		List<Map> content = SpreadsheetReader.readSpreadSheet(classificationsFile, contentSheetNo, contentHeaderRowNo);
		for (Map row : content) {
			def classification = Classification.findByName(row.get('name'));
			if(!classification) {
				classification = new Classification(name : row.get("name"), citation:row.get('citation'));
			} else {
				classification.citation = row.get('citation');
			}
			if(!classification.save(flush:true))
				classification.errors.each { log.error it; }
		}
	}

	/**
	 *
	 */
	void uploadLicences () {
		XMLConverter converter = new XMLConverter();
		LicenseType.toList().each { licType ->
			converter.getLicenseByType(licType, true);
		}
	}
}
