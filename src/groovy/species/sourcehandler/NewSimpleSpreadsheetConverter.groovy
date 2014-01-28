package species.sourcehandler

import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;

class NewSimpleSpreadsheetConverter extends SourceConverter {

	protected static SourceConverter _instance;
	private static final log = LogFactory.getLog(this);
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields
	
	private NewSimpleSpreadsheetConverter() {
	}

	//should be synchronized
	public static NewSimpleSpreadsheetConverter getInstance() {
		if(!_instance) {
			_instance = new NewSimpleSpreadsheetConverter();
		}
		return _instance;
	}

	public List<Species> convertSpecies(String file, String imagesDir = "") {
		List<List<String>> content = SpreadsheetReader.readSpreadSheet(file, 0);
		List<Map> imageMetaData = SpreadsheetReader.readSpreadSheet(file, 1, 0);
		return convertSpecies(content, imageMetaData, imagesDir);
	}

	public List<Species> convertSpecies(List<List<String>> content, List<Map> imageMetaData, String imagesDir="") {

		List<Species> species = new ArrayList<Species>();

		NodeBuilder builder = NodeBuilder.newInstance();
		int fieldsCount = content.get(0).size()-1;
		
		//reading first concept, category and subcategory but ignoring descriptionfrom simple spreadsheet
		String[] concepts = new String[fieldsCount];
		String[] categories = new String[fieldsCount];
		String[] subcategories = new String[fieldsCount];
		for(int index = 1; index < fieldsCount+1; index++) {
			concepts[index-1] = content.get(0).get(index)?.toLowerCase().trim();
			if(index < content.get(1).size())
				categories[index-1] = content.get(1).get(index)?.toLowerCase().trim();
			if(index < content.get(2).size())
				subcategories[index-1] = content.get(2).get(index)?.toLowerCase().trim();
		}
		//done reading
		
		List<String> contributors, attributions,  licenses,  audiences,  status, imageIds;
		
		int i=0;
		for(int index = 4; index < content.size(); index++) {
            contributors = [];
            attributions = [];
            licenses = [];
            audiences = [];
            status = [];
            imageIds = [];
			List<String> speciesContent = content.get(index);
			//log.debug speciesContent;
			Node speciesElement = builder.createNode("species");
			
			for(int j=0; j<fieldsCount; j++) {
				String fieldContent = speciesContent[j+1];
				if(fieldContent) {
					fieldContent = fieldContent.trim();
					Node field = new Node(speciesElement, "field");
					if(concepts[j])
						new Node(field, "concept", concepts[j]);
					if(categories[j])
						new Node(field, "category", categories[j]);
					if(subcategories[j])
						new Node(field, "subcategory", subcategories[j]);
					
					log.debug "Reading $field"
					if(field.category.text().equalsIgnoreCase((String)fieldsConfig.COMMON_NAME)) {
						createCommonNames(field, fieldContent)
					} else if (field.category.text().equalsIgnoreCase((String)fieldsConfig.SYNONYMS)) {
						createSynonyms(field, fieldContent)
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.IMAGES)) {
						imageIds = getImages(fieldContent)
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.CONTRIBUTOR)) {
						contributors = getContributors(fieldContent);
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.ATTRIBUTIONS)) {
						attributions = getAttributions(fieldContent);
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.LICENSE)) {
						licenses = getLicenses(fieldContent);
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.AUDIENCE)) {
						audiences = getAudience(fieldContent);
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.STATUS)) {
						status = getStatus(fieldContent);
					} else if (field.concept.text().equalsIgnoreCase((String)fieldsConfig.INFORMATION_LISTING) && field.category.text().equalsIgnoreCase((String)fieldsConfig.REFERENCES)) {
						Node data = new Node(field, "data", '');
						createReferences(data, fieldContent);
					} else {
						createDataNode(field , fieldContent);
					}
				}
			}
			
			for(field in speciesElement.field) {
				for (data in field.data) {
					attachMetadata(data, contributors, attributions, licenses, audiences, status);
				}
			}
			
			createImages(speciesElement, imageIds, imageMetaData, imagesDir);
			log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			log.debug speciesElement;
			log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			XMLConverter converter = new XMLConverter();
			Species s = converter.convertSpecies(speciesElement)
			if(s) {
                println "Species Title: ${s.title}   ${s.taxonConcept.name}"
				species.add(s);
            }
//			if(i==0)break;
//			i++
		}
	
        return species;
	}
}
