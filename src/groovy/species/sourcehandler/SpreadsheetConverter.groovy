package species.sourcehandler

import groovy.util.Node;

import java.awt.ImageMediaEntry;
import java.util.List;
import java.util.Map;

import species.Contributor;
import species.Country;
import species.DOMWriter;
import species.Field;
import species.GeographicEntity;
import species.Language;
import species.License;
import species.License.LicenseType;
import species.Resource;
import species.Reference;
import species.Species;
import species.CommonNames;
import species.SpeciesField;
import species.Resource.ResourceType;
import species.SpeciesField.AudienceType;
import species.formatReader.SpreadsheetReader;


class SpreadsheetConverter extends SourceConverter {
	protected static SourceConverter _instance;
	
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields
	
	private SpreadsheetConverter() {
	}

	//should be synchronized
	public static SpreadsheetConverter getInstance() {
		if(!_instance) {
			_instance = new SpreadsheetConverter();
		}
		return _instance;
	}

	public List<Species> convertSpecies(String file, int contentSheetNo, int contentHeaderRowNo, int imageMetadataSheetNo, int imageMetaDataHeaderRowNo, String imagesDir="") {
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		List<Map> imageMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetadataSheetNo, imageMetaDataHeaderRowNo);
		convertSpecies(content, imagesDir, imageMetaData);
	}

	public List<Species> convertSpecies(List<Map> content, List<Map> imageMetaData, String imagesDir="") {

		List<Species> species = new ArrayList<Species>();

		NodeBuilder builder = NodeBuilder.newInstance();
		Node speciesElement = builder.createNode("species");

		Iterator iter = content.iterator();
		while(iter.hasNext()) {
			Map row = iter.next();
			Node field = new Node(speciesElement, "field");
			Node concept = new Node(field, fieldsConfig.CONCEPT, row.get(fieldsConfig.CONCEPT));
			Node category = new Node(field, fieldsConfig.CATEGORY, row.get(fieldsConfig.CATEGORY));
			Node subcategory = new Node(field, fieldsConfig.SUBCATEGORY, row.get(fieldsConfig.SUBCATEGORY));
			if(row.get(fieldsConfig.CATEGORY)?.equalsIgnoreCase(fieldsConfig.SYNONYMS)) {
				if(row.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase('Name(s)')) {
					Map relationshipsRow = iter.next();
					createSynonyms(field, getDescription(row), row, relationshipsRow);
				}
			} else if(row.get(fieldsConfig.CATEGORY)?.equalsIgnoreCase('Common Name')) {
				createCommonNames(field, getDescription(row), row);
			} else if(row.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase('Global Distribution Geographic Entity')) {
				createCountryGeoEntity(field, getDescription(row), row);
			} else if(row.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase('Indian Distribution Geographic Entity')) {
				createCountryGeoEntity(field, getDescription(row), row);
			} else if(row.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase('Global Endemicity Geographic Entity')) {
				createCountryGeoEntity(field, getDescription(row), row);
			} else if(row.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase('Indian Endemicity Geographic Entity')) {
				createCountryGeoEntity(field, getDescription(row), row);
			} else {
				createDataNode(field, getDescription(row), row);
			}
		}
		createImages(speciesElement, imageMetaData, imagesDir);

		XMLConverter converter = new XMLConverter();
		Species s = converter.convertSpecies(speciesElement)
		if(s)
			species.add(s);
		return species;
	}

	private Node createDataNode(Node field, String text, Map speciesContent) {
		//if(!text) return;

		Node data = new Node(field, "data", text);
		attachMetadata(data, speciesContent, new HashMap());
		return data;
	}

	private String getDescription(Map row) {
		return row.get("entries")?:"";
	}

	private void createReferences(Node data, Map map) {
		List<String> attrs = getAttributionsList(map.get("attribution"));
		String refs = map.get("reference url");
		if(refs && !refs.equals("")) {
			int i=0;
			for(String ref : refs.split("\\\n")) {
				//TODO : remove other protocols as well if present
				if(!ref.startsWith("http://") && ref.indexOf("http://") != -1) {
					ref = ref.substring(ref.indexOf("http://"));
				}

				if(ref.startsWith("http://")) {
					def refNode = new Node(data, "reference")
					new Node(refNode, "title", attrs?attrs.get(i):"");
					new Node(refNode, "url", ref);
				}
				i++;
			}
		}
	}

	private List<String> getAttributionsList(Map map) {
		String attrs = map.get("attribution");
		if(attrs && !attrs.equals("")) {

			List<String> newAttrsList = new ArrayList();
			attrs.split("\\\n").each {
				if(it.matches("^\\d+\\..*")) {
					it = it.split("^\\d+\\.", 2);
					newAttrsList.add(it[1].trim());
				} else if(!it.trim().equals("")){
					newAttrsList.add(it.trim());
				}
			}
			return newAttrsList;
		}
	}

	private void createAttributions(Node data, Map map) {

	}
}
