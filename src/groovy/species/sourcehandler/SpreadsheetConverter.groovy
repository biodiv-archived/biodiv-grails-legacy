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

	public List<Species> convertSpecies(String file, int contentSheetNo, int contentHeaderRowNo, int imageMetadataSheetNo, int imageMetaDataHeaderRowNo) {
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		List<Map> imageMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetadataSheetNo, imageMetaDataHeaderRowNo);
		convertSpecies(content, imageMetaData);
	}

	public List<Species> convertSpecies(List<Map> content, List<Map> imageMetaData) {

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
		createImages(speciesElement, imageMetaData);

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

	private void attachMetadata(Node data, Map speciesContent, Map mappedField) {

		String contributorField = "contributor";
		if(contributorField) {
			String contributors = speciesContent.get(contributorField.toLowerCase())
			String delimiter = mappedField.get("content delimiter") ?: "\n";
			contributors.split(delimiter).each {
				new Node(data, "contributor", it);
			}
		}

		//		String attributionField = "attribution";
		//		if(attributionField) {
		//			String attribution = speciesContent.get(attributionField.toLowerCase())
		//			String delimiter = mappedField.get("content delimiter") ?: "\n";
		//			attribution.split(delimiter).each {
		//				new Node(data, "attribution", it);
		//			}
		//		}

		String licenseField = "license";
		if(licenseField) {
			String licenses = speciesContent.get(licenseField.toLowerCase());
			String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
			licenses.split(delimiter).each {
				new Node(data, "license", it);
			}
		}

		String audienceTypeField = "audience";
		if(audienceTypeField) {
			String audience = speciesContent.get(audienceTypeField.toLowerCase());
			String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
			audience.split(delimiter).each {
				new Node(data, "audienceType", it);
			}
		}

		createReferences(data, speciesContent);

		String imagesField = "images";
		if(imagesField) {
			String images = speciesContent.get(imagesField.toLowerCase());
			if(images) {
				def imagesNode = new Node(data, "images");
				images.split(",").each { imageField ->
					new Node(imagesNode, "image", imageField);
				}
			}
		}

		String iconsField = "icons";
		if(iconsField) {
			String icons = speciesContent.get(iconsField.toLowerCase());
			if(icons) {
				def iconsNode = new Node(data, "icons");
				icons.split(",").each { iconField ->
					new Node(iconsNode, "icon", iconField.trim());
				}
			}
		}
	}

	private String getDescription(Map row) {
		return row.get("entries")?:"";
	}

	private void createImages(Node speciesElement, List<Map> imageMetaData) {
		Node images = new Node(speciesElement, "images");
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String uploadDir = config.speciesPortal.images.uploadDir;
		imageMetaData.each{ imageData ->
			Node image = new Node(images, "image");
			String refKey = imageData.get("imageno.");
			File file = new File(uploadDir, refKey);
			new Node(image, "refKey", refKey);
			new Node(image, "fileName", file.getAbsolutePath());
			new Node(image, "source", imageData.get("source"));
			new Node(image, "caption", imageData.get("possiblecaption"));
			new Node(image, "attribution", imageData.get("attribution"));
			new Node(image, "license", imageData.get("license"));
		}
	}

	private void createReferences(Node data, Map map) {
		List<String> attrs = getAttributionsList(map);
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

	private void createSynonyms(Node field, String text, Map speciesContent, Map relationshipsRow) {
		String[] relationships = getDescription(relationshipsRow)?.split("\\\n");
		if(text && !text.equals("")) {
			text.split("\\\n").eachWithIndex { syn, index ->
				Node dataNode = createDataNode(field, syn, speciesContent)
				if(relationships && index < relationships.length) {
					new Node(dataNode, "relationship", relationships[index]);
				}
			}
		}
	}

	private void createCommonNames(Node field, String text, Map speciesContent) {
		if(text && !text.equals("")) {
			text.split("\\\n").each { 
				def commonNames = it.split(":", 2);
				if(commonNames.length == 2) {
					commonNames[1].split(",|;").each {
						Node data = createDataNode(field, it.replaceAll('"', ''), speciesContent);
						if(data)
							createLanguage(data, commonNames[0]);
					}
				} else {
					commonNames[0].split(",|;").each {
						createDataNode(field, it, speciesContent);
					}
				}
			}
		}
	}
	private void createLanguage(Node dataNode, String s) {
		if(!s || s.equals("")) return null; //|| !s.trim().matches(".+\\(.+\\)")
		String[] str = s.split("\\(|\\)");
		Node langNode = new Node(dataNode, "language");
		new Node(langNode, "threeLetterCode", str[1]?.trim());
		new Node(langNode, "name", str[0]?.trim())
	}


	private void createCountryGeoEntity(Node field, String text, Map speciesContent) {
		if(text) {
			text.split("\\\n").each {
				it = it.split("-", 2);
				String[] codes = it[0].split(',');
				String countryName = it[1];
				if(codes[0] && codes[1] && codes[2] && countryName) {
					countryName = countryName.replaceAll('"', '').trim();

					Node dataNode = createDataNode(field, "", speciesContent)
					if(dataNode) {
						Node country = new Node(dataNode, "country");
						new Node(country, "name", countryName);
						new Node(country, "twoLetterCode", codes[0]);
						new Node(country, "threeLetterCode", codes[1]);
						new Node(country, "threeDigitCode", Integer.parseInt(codes[2].trim()));
					}
				}
			}
		}
	}
}
