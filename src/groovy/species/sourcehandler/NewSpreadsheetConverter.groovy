package species.sourcehandler

import groovy.util.Node

import java.util.List
import java.util.Map

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import species.Species
import species.formatReader.SpreadsheetReader


class NewSpreadsheetConverter extends SourceConverter {
	protected static SourceConverter _instance;
	private static final Log log = LogFactory.getLog(this);

	private SpreadsheetConverter() {
	}

	//TODO:should be synchronized
	public static NewSpreadsheetConverter getInstance() {
		if(!_instance) {
			_instance = new NewSpreadsheetConverter();
		}
		return _instance;
	}

	public List<Species> convertSpecies(String file, String imagesDir="") {
		List<List<Map>> content = SpreadsheetReader.readSpreadSheet(file);
		convertSpecies(content, imagesDir);
	}

	public List<Species> convertSpecies(List<List<Map>> sheetContent, String imagesDir="") {
		List<Species> species = new ArrayList<Species>();
		List<Node> speciesElements = createSpeciesXML(sheetContent);
		XMLConverter converter = new XMLConverter();
		for(Node speciesElement : speciesElements) {			
			Species s = converter.convertSpecies(speciesElement, imagesDir)
			if(s)
				species.add(s);
		}
		return species;		
	}
	
	public List<Node> createSpeciesXML(List<List<Map>> sheetContent, String imagesDir="") {
		
		List<Node> speciesElements = new ArrayList<Node>();
		NodeBuilder builder = NodeBuilder.newInstance();
		Node speciesElement = builder.createNode("species");

		Map<String, Map> references = new HashMap<String, Map>();
		Map<String, Map> attributions = new HashMap<String, Map>();

		Iterator iter = sheetContent.get(5).iterator();
		while(iter.hasNext()) {
			println iter;
			Map row = iter.next();
			references.put(row.get("id"), row);
		}

		iter = sheetContent.get(6).iterator();
		while(iter.hasNext()) {
			Map row = iter.next();
			attributions.put(row.get("id"), row);
		}

		iter = sheetContent.get(0).iterator();
		while(iter.hasNext()) {
			Map row = iter.next();
			Node field = new Node(speciesElement, "field");
			Node concept = new Node(field, "concept", row.get("concept"));
			Node category = new Node(field, "category", row.get("category"));
			Node subcategory = new Node(field, "subcategory", row.get("subcategory"));
			if(row.get('category')?.equalsIgnoreCase('Synonyms')) {
				createSynonyms(field, sheetContent.get(2), attributions, references);
			} else if(row.get('category')?.equalsIgnoreCase('Common Name')) {
				createCommonNames(field, sheetContent.get(3), attributions, references);
			} else if(row.get('subcategory')?.equalsIgnoreCase('Global Distribution Geographic Entity')) {
				createCountryGeoEntity(field,  sheetContent.get(7), attributions, references);
			} else if(row.get('subcategory')?.equalsIgnoreCase('Indian Distribution Geographic Entity')) {
				createCountryGeoEntity(field,  sheetContent.get(8), attributions, references);
			} else if(row.get('subcategory')?.equalsIgnoreCase('Global Endemicity Geographic Entity')) {
				createCountryGeoEntity(field,  sheetContent.get(9), attributions, references);
			} else if(row.get('subcategory')?.equalsIgnoreCase('Indian Endemicity Geographic Entity')) {
				createCountryGeoEntity(field,  sheetContent.get(10), attributions, references);
			} else {
				createDataNode(field, getDescription(row), row, attributions, references);
			}
		}
		createImages(speciesElement, sheetContent.get(1), imagesDir);

		log.debug speciesElement;
		if(speciesElement) {
			speciesElements.add(speciesElement);
		}
		
		return speciesElements;
	}

	private Node createDataNode(Node field, String text, Map speciesContent, Map attributions, Map references) {
		//if(!text) return;

		Node data = new Node(field, "data", text);
		attachMetadata(data, speciesContent, new HashMap(), attributions, references);
		return data;
	}

	private void attachMetadata(Node data, Map speciesContent, Map mappedField, Map attributions, Map references) {

		String contributorField = "contributor";
		if(contributorField) {
			String contributors = getDescription(speciesContent, contributorField.toLowerCase())
			String delimiter = mappedField.get("content delimiter") ?: "\n";
			contributors.split(delimiter).each {
				new Node(data, "contributor", it);
			}
		}

		//		String attributionField = "attribution";
		//		if(attributionField) {
		//			String attribution = getDescription(speciesContent, attributionField.toLowerCase())
		//			String delimiter = mappedField.get("content delimiter") ?: "\n";
		//			attribution.split(delimiter).each {
		//				new Node(data, "attribution", it);
		//			}
		//		}

		String licenseField = "license";
		if(licenseField) {
			String licenses = getDescription(speciesContent, licenseField.toLowerCase());
			String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
			licenses.split(delimiter).each {
				new Node(data, "license", it);
			}
		}

		String audienceTypeField = "audience";
		if(audienceTypeField) {
			String audience = getDescription(speciesContent, audienceTypeField.toLowerCase());
			String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
			audience.split(delimiter).each {
				new Node(data, "audienceType", it);
			}
		}

		createReferences(data, speciesContent, references);

		String imagesField = "images";
		if(imagesField) {
			String images = getDescription(speciesContent, imagesField.toLowerCase());
			if(images) {
				def imagesNode = new Node(data, "images");
				images.split(",").each { imageField ->
					new Node(imagesNode, "image", imageField);
				}
			}
		}

		String iconsField = "icons";
		if(iconsField) {
			String icons = getDescription(speciesContent, iconsField.toLowerCase());
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

	private String getDescription(Map row, String field) {
		return row.get(field)?:"";
	}

	protected void createReferences(Node data, Map speciesContent, Map references) {
		String refs = speciesContent.get("references");
		if(refs && !refs.equals("")) {
			for(String ref : refs.split(",")) {
				ref = ref.trim();
				Map referenceRow = references.get(ref);
				if(referenceRow) {
					def refNode = new Node(data, "reference")
					new Node(refNode, "title", referenceRow.get("title"));
					new Node(refNode, "url", referenceRow.get("url"));
				}
			}
		}
	}

	protected void createAttributions(Node data, Map map, Map attributions) {

	}

	protected void createSynonyms(Node field, List<Map> content, Map attributions, Map references) {
		log.debug '#### Creating Synonyms'
		content.each { row ->
			log.debug row;
			String syn = getDescription(row, "synonym");
			String rel = getDescription(row, "relationship");
			log.debug syn;
			if(syn) {
				Node dataNode = createDataNode(field, syn, row, attributions, references);
				if(rel) {
					new Node(dataNode, "relationship", rel);
				}
			}
		}
		log.debug field;

	}

	protected void createCommonNames(Node field, List<Map> content, Map attributions, Map references) {
		content.each { row ->
			String cmnName = getDescription(row, "common name");
			String lang = getDescription(row, "language name");
			String code = getDescription(row, "language code");
			if(cmnName) {
				Node dataNode = createDataNode(field, cmnName, row, attributions, references);
				if(lang || code) {
					Node langNode = new Node(dataNode, "language");
					if(code) new Node(langNode, "threeLetterCode", code.trim());
					if(lang) new Node(langNode, "name", lang)

				}
			}
		}

	}

	protected void createCountryGeoEntity(Node field, List<Map> content, Map attributions, Map references) {
		content.each { row ->
			String countryName = getDescription(row, "country");
			String twoLetterCode = getDescription(row, "two letter code");
			if(countryName) {
				Node dataNode = createDataNode(field, "", row)
				if(dataNode) {
					Node country = new Node(dataNode, "country");
					new Node(country, "name", countryName);
					if(twoLetterCode) new Node(country, "twoLetterCode", twoLetterCode);
				}
			}
		}
	}

}
