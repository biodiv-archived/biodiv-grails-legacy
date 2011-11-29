package species

import java.util.List;

import org.apache.commons.logging.LogFactory;

import species.formatReader.SpreadsheetReader;
import species.sourcehandler.KeyStoneDataConverter;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.sourcehandler.NewSpreadsheetConverter;
import species.sourcehandler.SpreadsheetConverter;
import species.search.SearchIndexManager;

class DataLoader {

	private static final log = LogFactory.getLog(this);

	void uploadFields(String fieldDefinitionsFile) {
		log.debug "Uploading field definitions"
		populateFields(fieldDefinitionsFile, 0, 0);
		log.debug "Uploading field definitions done"
	}

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
				field.save(flush:true, failOnError: true);
				field.errors.each { log.error it }
			}
		}
	}

	void uploadLanguages (String languagesFile) {
		log.debug "Uploading languages"
		new File(languagesFile).splitEachLine("\\t") {
			def fields = it;
			def lang = new Language (threeLetterCode:fields[0].replaceAll("\"",""), twoLetterCode:fields[1].replaceAll("\"",""), name:fields[2].replaceAll("\"",""));
			lang.save(flush:true);
			lang.errors.each { log.error it; }
		}
		log.debug "Uploading languages done"
	}

	void uploadCountries (String countriesFile) {
		log.debug "Uploading countries"
		new File(countriesFile).splitEachLine("\\t") {
			def fields = it;
			def country = new Country(countryName:fields[1].replaceAll("\"",""), twoLetterCode:fields[0].replaceAll("\"",""));
			country.save(flush:true);
			country.errors.each { log.error it; }
		}
		log.debug "Uploading countries done"
	}
	
	void uploadClassifications (String classificationsFile, int contentSheetNo, int contentHeaderRowNo) {
		log.debug "Uploading classifications"
		List<Map> content = SpreadsheetReader.readSpreadSheet(classificationsFile, contentSheetNo, contentHeaderRowNo);
		for (Map row : content) {
			def classification = new Classification(name : row.get("name"));
			classification.save(flush:true);
			classification.errors.each { log.error it; }
		}
		log.debug "Uploading classifications done"
	}

	void uploadMappedSpreadsheet (String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo) {
		log.debug "Uploading mapped spreadsheet : "+file;
		List<Species> species = MappedSpreadsheetConverter.getInstance().convertSpecies(file, mappingFile, mappingSheetNo, mappingHeaderRowNo, contentSheetNo, contentHeaderRowNo);
		saveSpecies(species);
	}

	void uploadSpreadsheet (String file, int contentSheetNo, int contentHeaderRowNo, int imageMetadataSheetNo, int imageMetaDataHeaderRowNo) {
		log.debug "Uploading spreadsheet : "+file;
		List<Species> species = SpreadsheetConverter.getInstance().convertSpecies(file, contentSheetNo, contentHeaderRowNo, imageMetadataSheetNo, imageMetaDataHeaderRowNo);
		saveSpecies(species);
	}

	void uploadNewSpreadsheet (String file) {
		log.debug "Uploading new spreadsheet : "+file;
		List<Species> species = NewSpreadsheetConverter.getInstance().convertSpecies(file);
		saveSpecies(species);
	}

	void uploadKeyStoneData (String connectionUrl, String userName, String password, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo) {
		log.debug "Uploading keystone data";
		List<Species> species = KeyStoneDataConverter.getInstance().convertSpecies(connectionUrl, userName, password, mappingFile, mappingSheetNo, mappingHeaderRowNo);
		saveSpecies(species);
	}

	boolean saveSpecies(List species) {
		log.debug "Saving species : "+species.size()
		def startTime = System.nanoTime()
		List <Species> batch =[]
		species.each {
			batch.add(it);
			if(batch.size()>10){
				saveSpeciesBatch(batch);
				batch.clear();
				return
			}
		}
		if(batch.size() > 0) {
			saveSpeciesBatch(batch);
			batch.clear();
		}
		def endTime =  System.nanoTime()
		def diff = (endTime-startTime)/1000000000
		log.debug "Time taken to save : "+diff + "(sec)"

		log.debug "Publishing to search index"
		//def searchIndexManager = new SearchIndexManager();
		//searchIndexManager.publishSearchIndex(species);
	}

	void saveSpeciesBatch(List<Species> batch) {
		Species.withTransaction {
			for(Species s in batch) {
				if(!s.save()) {
					s.errors.allErrors.each { log.error it }
				}
			}
		}
		log.debug "Saved batch"
		//TODO : probably required to clear hibernate cache	
		//Reference : http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/
		
	}
}
