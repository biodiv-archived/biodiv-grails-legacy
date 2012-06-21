package species.sourcehandler

import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;

class MappedSpreadsheetConverter extends SourceConverter {

	protected static SourceConverter _instance;
	private static final log = LogFactory.getLog(this);

	private MappedSpreadsheetConverter() {
	}

	//should be synchronized
	public static MappedSpreadsheetConverter getInstance() {
		if(!_instance) {
			_instance = new MappedSpreadsheetConverter();
		}
		return _instance;
	}

	public List<Species> convertSpecies(String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo) {
		List<Map> mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);

		List<Species> species = new ArrayList<Species>();

		NodeBuilder builder = NodeBuilder.newInstance();
		int i=0;
		for(Map speciesContent : content) {
			//log.debug speciesContent;
			Node speciesElement = builder.createNode("species");
			for(Map mappedField : mappingConfig) {
				String fieldName = mappedField.get("field name(s)")
				String delimiter = mappedField.get("content delimiter");
				String customFormat = mappedField.get("content format");
				if(fieldName && (customFormat || speciesContent.get(fieldName.toLowerCase()))) {
					fieldName = fieldName.toLowerCase();
					Node field = new Node(speciesElement, "field");
					Node concept = new Node(field, "concept", mappedField.get("concept"));
					Node category = new Node(field, "category", mappedField.get("category"));
					Node subcategory = new Node(field, "subcategory", mappedField.get("subcategory"));
					if (customFormat && mappedField.get("category")?.equalsIgnoreCase("images")) {
						Node images = getImages(file, fieldName, customFormat, delimiter, speciesContent, speciesElement);
					} else if (customFormat && category.text().equalsIgnoreCase("icons")) {
						//						Node images = getIcons(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, icons);
					} else if (customFormat && category.text().equalsIgnoreCase("audio")) {
						//						Node images = getAudio(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, audio);
					} else if (customFormat && category.text().equalsIgnoreCase("video")) {
						//						Node images = getVideo(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, video);
					} else if(customFormat) {
						String text = getCustomFormattedText(mappedField.get("field name(s)"), customFormat, speciesContent);
						createDataNode(field, text, speciesContent, mappedField);
					} else if(delimiter) {
						String text = speciesContent.get(fieldName);
						if(text) {
							for(String part : text.split(delimiter)) {
								if(part) {
									part = part.trim();
									if(category.text().equalsIgnoreCase("common name")) {
										String[] commonNames = part.split(":");
										if(commonNames.length == 2) {
											commonNames[1].split(",|;").each {
												Node data = createDataNode(field, it, speciesContent, mappedField);
												Node language = new Node(data, "language");
												Node name = new Node(language, "name", commonNames[0]);
											}
										} else {
											commonNames[0].split(",|;").each {
												createDataNode(field, it, speciesContent, mappedField);
											}
										}
									} else {
										createDataNode(field, part, speciesContent, mappedField);
									}
								}
							}
						}
					} else {
						createDataNode(field, speciesContent.get(fieldName), speciesContent, mappedField);
					}
				}
			}
			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			//log.debug speciesElement;
			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
			XMLConverter converter = new XMLConverter();
			Species s = converter.convertSpecies(speciesElement)
			if(s)
				species.add(s);
			//if(i==0)break;
			i++
		}
		return species;
	}

	private Node createDataNode(Node field, String text, Map speciesContent, Map mappedField) {
		if(!text) return;

		Node data = new Node(field, "data", text);
		attachMetadata(data, speciesContent, mappedField);
		return data;
	}

	private void attachMetadata(Node data, Map speciesContent, Map mappedField) {

		String contributorFields = mappedField.get("contributor");
		if(contributorFields) {
			contributorFields.split(",").each { contributorField ->
				String contributors = speciesContent.get(contributorField.toLowerCase())
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				contributors?.split(delimiter).each {
					new Node(data, "contributor", it);
				}
			}
		}

		String attributionFields = mappedField.get("attributions");
		if(attributionFields) {
			attributionFields.split(",").each { attributionField ->
				String attribution = speciesContent.get(attributionField.toLowerCase())
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				attribution?.split(delimiter).each {
					new Node(data, "attribution", it);
				}
			}
		}

		String licenseFields = mappedField.get("license");
		if(licenseFields) {
			licenseFields.split(",").each { licenseField ->
				String licenses = speciesContent.get(licenseField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
				licenses?.split(delimiter).each {
					new Node(data, "license", it);
				}
			}
		}

		String audienceTypeFields = mappedField.get("audience");
		if(audienceTypeFields) {
			audienceTypeFields.split(",").each { audienceTypeField ->
				String audience = speciesContent.get(audienceTypeField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
				audience?.split(delimiter).each {
					new Node(data, "audienceType", it);
				}
			}
		}

		String referenceFields = mappedField.get("references");
		if(referenceFields) {
			referenceFields.split(",").each { referenceField ->
				String references = speciesContent.get(referenceField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				references?.split(delimiter).each {
					Node refNode = new Node(data, "reference");
					getReferenceNode(refNode, it);
				}
			}
		}

		String imagesField = mappedField.get("images");
		if(imagesField) {
			imagesField.split(",").each { imageField ->
				String images = speciesContent.get(imageField.toLowerCase());
				if(images) {
					def imagesNode = data;
					imagesNode = new Node(data, "images");
					String delimiter = mappedField.get("content delimiter") ?: "\n|\\s{3,}";
					images.split(delimiter).each {
						String loc = cleanLoc(it)
						new Node(imagesNode, "image", loc);
					}
				}
			}
		}
	}

	private Map getCustomFormat(String customFormat) {
		return customFormat.split(';').inject([:]) { map, token ->
			token = token.toLowerCase();
			token.split('=').with {
				map[it[0]] = it[1];
			}
			map
		}
	}

	private getCustomFormattedText(String fieldName, String customFormat, Map speciesContent) {
		def result = getCustomFormat(customFormat);
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()) : -1;
		boolean includeHeadings = result.get("includeheadings") ? Boolean.parseBoolean(result.get("includeheadings")?.toString()).booleanValue() : false;
		String con = "";
		fieldName.split(",").eachWithIndex { t, index ->
			String txt = speciesContent?.get(t.toLowerCase().trim());
			if(txt) {
				if (index%group == 0) {

					if(group > 1)
						txt = "<h6>"+txt+"</h6>";
					else {
						txt = "<p>"+txt+"</p>";
						if(includeHeadings) txt = "<h6>"+t.trim()+"</h6>"+txt;
					}
					if(con)
						con += txt;
					else con = txt;

				} else {
					txt = "<p>"+txt+"</p>";
					if(includeHeadings) txt = "<h6>"+t.trim()+"</h6>"+txt;
					con += txt;
				}
			}
		}
		return con;
	}

	private Node getImages(String file, String fieldName, String customFormat, String delimiter, Map speciesContent, Node speciesElement) {
		Node images = new Node(speciesElement, "images");

		def result = getCustomFormat(customFormat);
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()):-1
		int location = result.get("location") ? Integer.parseInt(result.get("location")?.toString())-1:-1
		int source = result.get("source") ? Integer.parseInt(result.get("source")?.toString())-1:-1
		int caption = result.get("caption") ? Integer.parseInt(result.get("caption")?.toString())-1:-1
		int attribution = result.get("attribution") ? Integer.parseInt(result.get("attribution")?.toString())-1:-1
		int license = result.get("license") ? Integer.parseInt(result.get("license")?.toString())-1:-1
		int name = result.get("name") ? Integer.parseInt(result.get("name")?.toString())-1:-1
		boolean incremental = result.get("incremental") ? new Boolean(result.get("incremental")) : false
		int imagesMetaDataSheet = result.get("imagesmetadatasheet") ? Integer.parseInt(result.get("imagesmetadatasheet")?.toString()):-1;
		if(imagesMetaDataSheet != -1) {
			//TODO:This is getting repeated for every row in spreadsheet costly
			List<Map> imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imagesMetaDataSheet, 0);
			fieldName.split(",").eachWithIndex { t, index ->
				String txt = speciesContent.get(t);
				txt.split(delimiter).each { loc ->
					if(loc) {
						createImages(images, loc, imagesMetaData);
					}
				}
			}
		} else {
			List<String> groupValues = new ArrayList<String>();
			fieldName.split(",").eachWithIndex { t, index ->
				String txt = speciesContent.get(t);
				if (index != 0 && index % group == 0) {
					populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, license, name, incremental);
					groupValues = new ArrayList<String>();
				}
				groupValues.add(txt);
			}
			populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, license, name, incremental);
		}
		return images;
	}

	private void populateImageNode(Node images, List<String> groupValues, String delimiter, int location, int source, int caption, int attribution, int license, int name, boolean incremental) {
		if(location != -1 && groupValues.get(location)) {
			String locationStr = groupValues.get(location);
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			String uploadDir = config.speciesPortal.images.uploadDir;
			if(locationStr) {
				if(delimiter) {
					locationStr.split(delimiter).each { loc ->
						createImageNode(images, groupValues, loc, uploadDir, source, caption, attribution, license, name, incremental);
					}
				} else {
					createImageNode(images, groupValues, locationStr, uploadDir, source, caption, attribution, license, name, incremental);
				}
			}
		}
	}

	private void createImageNode(Node images, List<String> groupValues, String loc, String uploadDir, int source, int caption, int attribution, int license, int name, boolean incremental) {
		String refKey = loc;
		loc = cleanLoc(loc);
		File imagesLocation = new File(uploadDir, loc);

		if(imagesLocation.isDirectory()) {
			imagesLocation.listFiles().eachWithIndex { file, index ->
				Node image = new Node(images, "image");
				new Node(image, "refKey", loc);
				new Node(image, "fileName", file.getAbsolutePath());
				if(source != -1 && groupValues.get(source)) new Node(image, "source", groupValues.get(source));
				if(caption != -1 && groupValues.get(caption)) new Node(image, "caption", groupValues.get(caption));
				if(attribution != -1 && groupValues.get(attribution)) new Node(image, "attribution", groupValues.get(attribution));
				if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
			}
		} else if(imagesLocation.exists()){
			Node image = new Node(images, "image");
			new Node(image, "refKey", loc);
			new Node(image, "fileName", imagesLocation.getAbsolutePath());
			if(source != -1 && groupValues.get(source)) new Node(image, "source", groupValues.get(source));
			if(caption != -1 && groupValues.get(caption)) new Node(image, "caption", groupValues.get(caption));
			if(attribution != -1 && groupValues.get(attribution)) new Node(image, "attribution", groupValues.get(attribution));
			if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
		}
	}

	private void createImages(Node images, String imageId, List<Map> imageMetaData) {
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String uploadDir = config.speciesPortal.images.uploadDir;
		imageMetaData.each { imageData ->
			String refKey = imageData.get("id");
			if(refKey.trim().equals(imageId.trim())) {
				Node image = new Node(images, "image");
				String loc = imageData.get("imageno.");
				File file = new File(uploadDir, cleanLoc(loc));
				new Node(image, "refKey", refKey);
				new Node(image, "fileName", file.getAbsolutePath());
				new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", imageData.get("possiblecaption"));
				new Node(image, "attribution", imageData.get("attribution"));
				new Node(image, "license", imageData.get("license"));
			}
		}
	}

	private String cleanLoc(String loc) {
		return loc.replaceAll("\\\\", File.separator);
	}

	private getReferenceNode(Node refNode, String text) {
		if(text.startsWith("http://")) {
			new Node(refNode, "url", text);
		} else {
			new Node(refNode, "title", text);
		}
	}
}
