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

	public List<Species> convertSpecies(String file) {
		List<List<String>> content = SpreadsheetReader.readSpreadSheet(file, 0);
		List<Map> imageMetaData = SpreadsheetReader.readSpreadSheet(file, 1, 0);
		return convertSpecies(content, imageMetaData);
	}

	public List<Species> convertSpecies(List<List<String>> content, List<Map> imageMetaData) {

		List<Species> species = new ArrayList<Species>();

		NodeBuilder builder = NodeBuilder.newInstance();
		int fieldsCount = content.get(0).size()-1;
		
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
		List<String> contributors, attributions,  licenses,  audiences,  status, imageIds;
		Node references = new Node(null, '');
		int i=0;
		for(int index = 4; index < content.size(); index++) {
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
						createReferences(references, fieldContent);
					} else {
						createDataNode(field , fieldContent);
					}
				}
			}
			
			for(field in speciesElement.field) {
				for (data in field.data) {
					attachMetadata(data, contributors, attributions, licenses, audiences, status, references);
				}
			}
			
			createImages(speciesElement, imageIds, imageMetaData);
			log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			log.debug speciesElement;
			log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			XMLConverter converter = new XMLConverter();
			Species s = converter.convertSpecies(speciesElement)
			if(s)
				species.add(s);
			//if(i==0)break;
			//i++
		}
		return species;
	}

	private Node createDataNode(Node field, String text) {
		if(!text) return;

		Node data = new Node(field, "data", text);
		return data;
	}

	private void attachMetadata(Node data, List<String> contributors, List<String> attributions, List<String> licenses, List<String> audiences, List<String> status, Node references) {
		log.debug "Attaching metadata $contributors $attributions $licenses $audiences $status"
		for(contributor in contributors) {
			new Node(data, "contributor", contributor);
		}
		for(attribution in attributions) {
			new Node(data, "attribution", attribution);
		}
		for(license in licenses) {
			new Node(data, "license", license);
		}
		for(audience in audiences) {
			new Node(data, "audienceType", audience);
		}
		for(s in status) {
			new Node(data, "status", s);
		}
		
		for(r in references.reference) {
			data.append(r)
		}
	}
	
	private List<String> getContributors(String text, String delimiter="\n") {
		log.debug "Creating contributors ${text}"
		List<String> contributors = []; 
		if(text) {
			text.split(delimiter).each {
				if(it.matches("^\\d+\\..*")) {
					it = it.split("^\\d+\\.", 2);
					contributors << it[1].trim();
				} else if(!it.trim().equals("")){
					contributors << it.trim();
				}
			}
		}
		return contributors;
	}

	private List<String> getAttributions(String text, String delimiter="\n") {
		log.debug "Creating attributions ${text}"
		List<String> attributions = []; 
		if(text) {
			text.split(delimiter).each {
				if(it.matches("^\\d+\\..*")) {
					it = it.split("^\\d+\\.", 2);
					attributions << it[1].trim();
				} else if(!it.trim().equals("")){
					attributions << it.trim();
				}
			}
		}
		return attributions;
	}
	
	private List<String> getLicenses(String text, String delimiter=",|;|\n") {
		log.debug "Creating licenses ${text}"
		List<String> licenses = [];
		if(text) {
			text.split(delimiter).each {
				if(it) {
					if(!it.startsWith("CC")) {
						licenses << "CC "+it.trim()
					} else {
						licenses << it.trim()
					}
				}
			}
		}
		return licenses;
	}

	private List<String> getAudience(String text, String delimiter=",|;|\n") {
		log.debug "Creating audience ${text}"
		List<String> audiences = [];
		if(text) {
			text.split(delimiter).each {
				if(it)
					audiences << it.trim()
			}
		}
		return audiences;
	}
	
	private List<String> getImages(String text, String delimiter=",|;|\n|\\s{3,}") {
		log.debug "Creating images ${text}"
		List<String> imageIds = [];
		if(text) {
			text.split(delimiter).each {
				if(it) 
					imageIds << it.trim()
			}
		}
		return imageIds;
	}

	private void createCommonNames(Node field, String text, String delimiter="\n") {
		log.debug "Creating commonNames ${text}"
		if(text) {
			for(String part : text.split(delimiter)) {
				if(part) {
					String[] commonNames = part.split(":");
					if(commonNames.length == 2) {
						commonNames[1].split(",|;").each {
							Node data = createDataNode(field, it.trim());
							createLanguage(data, commonNames[0]);
						}
					} else {
						commonNames[0].split(",|;").each {
							createDataNode(field, it.trim());
						}
					}
					
				}
			}
		}
	}
	
	private void createLanguage(Node dataNode, String s) {
		log.debug "Getting language $s"
		if(!s || s.equals("")) return null;
		String[] str = s.split("\\(|\\)");
		Node langNode = new Node(dataNode, "language");
		if(str.length > 1)
			new Node(langNode, "threeLetterCode", str[1]?.trim());
		new Node(langNode, "name", str[0]?.trim())
	}
	
	
	private void createSynonyms(Node field, String text, String delimiter="\n|\\s{3,}") {
		log.debug "Creating synonyms ${text}"
		if(text) {
			for(String part : text.split(delimiter)) {
				if(part) {
					part = part.trim();
					createDataNode(field, part);
				}
			}
		}
	}
	
	
	private void createImages(Node speciesElement, List<String> imageIds, List<Map> imageMetaData) {
		log.debug "Creating images ${imageIds}"
		println imageMetaData
		if(!imageIds) return;
		
		Node images = new Node(speciesElement, "images");
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String uploadDir = config.speciesPortal.images.uploadDir;
		imageMetaData.each { imageData ->
			String refKey = imageData.get("id");
			if(imageIds.contains(refKey)) {
				Node image = new Node(images, "image");		
				String imagePath =  imageData.get("image")?:refKey;
				File file = new File(uploadDir, imagePath);
				new Node(image, "refKey", refKey);
				new Node(image, "fileName", file.getAbsolutePath());
				new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", imageData.get("possiblecaption"));
				new Node(image, "attribution", imageData.get("attribution"));
				new Node(image, "license", imageData.get("license"));
			}
		}
		log.debug images
	}

	
	private void createReferences(Node field, String text) {
		log.debug "Creating references ${text}"
		List<String> attrs = getAttributionsList(text);
		if(text && !text.equals("")) {
			int i=0;
			for(String ref : text.split("\\\n")) {
				//TODO : remove other protocols as well if present
				if(!ref.startsWith("http://") && ref.indexOf("http://") != -1) {
					ref = ref.substring(ref.indexOf("http://"));
				}

				if(ref.startsWith("http://")) {
					def refNode = new Node(field, "reference")
					new Node(refNode, "title", attrs?attrs.get(i):"");
					new Node(refNode, "url", ref);
				} else {
					def refNode = new Node(field, "reference")
					new Node(refNode, "title", attrs?attrs.get(i):"");
				}
				i++;
			}
		}
	}
	private List<String> getAttributionsList(String text) {
		
		if(text && !text.equals("")) {

			List<String> newAttrsList = new ArrayList();
			text.split("\\\n").each {
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
}
