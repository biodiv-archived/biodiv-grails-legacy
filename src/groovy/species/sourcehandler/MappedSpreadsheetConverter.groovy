package species.sourcehandler

import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;
import org.apache.log4j.Logger; 
import org.apache.log4j.FileAppender;

class MappedSpreadsheetConverter extends SourceConverter {

	//protected static SourceConverter _instance;
	private static def log = LogFactory.getLog(this);
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields
	
	public List<Map> imagesMetaData;
	public List<Map> mappingConfig;
	
	//to keep track of current species index. used for reporting error.
	private int currentRowIndex = 1;
	private StringBuffer summary; 
	public MappedSpreadsheetConverter() {
		imagesMetaData = [];
		summary = new StringBuffer()
	}

	public List<Species> convertSpecies(String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo, int imageMetaDataSheetNo, String imagesDir="") {
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);				
		if(imageMetaDataSheetNo && imageMetaDataSheetNo  >= 0) {
			imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetaDataSheetNo, 0);
		}
		return convertSpecies(content, mappingConfig, imagesMetaData, imagesDir);
	}

//	public List<Species> convertSpecies(List<Map> content, List<Map> mappingConfig, List<Map> imagesMetaData) {
//		List<Species> species = new ArrayList<Species>();
//		
//		XMLConverter converter = new XMLConverter();
//		
//		for(Map speciesContent : content) {
//			Node speciesElement = createSpeciesXML(content, mappingConfig);
//			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
//			//log.debug speciesElement;
//			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
//			Species s = converter.convertSpecies(speciesElement)
//			if(s)
//				species.add(s);
//		}
//		return species;
//	}
	
	public Node createSpeciesXML(Map speciesContent, String imagesDir="") {
		if(!mappingConfig) {
			log.error "No mapping config";
			return;
		}
		
		NodeBuilder builder = NodeBuilder.newInstance();
		int i=0;
		
			//log.debug speciesContent;
			Node speciesElement = builder.createNode("species", ['rowIndex':currentRowIndex++]);
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
					if (mappedField.get("category")?.equalsIgnoreCase("images")) {
						Node images = getImages(imagesMetaData, fieldName, 'images', customFormat, delimiter, speciesContent, speciesElement, imagesDir);
					} else if (category.text().equalsIgnoreCase("icons")) {
						Node icons = getImages(imagesMetaData, fieldName, 'icons', customFormat, delimiter, speciesContent, speciesElement, imagesDir);
					} else if (category.text().equalsIgnoreCase("audio")) {
						//						Node images = getAudio(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, audio);
					} else if (category.text().equalsIgnoreCase("video")) {
						//						Node images = getVideo(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, video);
					} else if (concept.text().equalsIgnoreCase((String)fieldsConfig.INFORMATION_LISTING) && field.category.text().equalsIgnoreCase((String)fieldsConfig.REFERENCES)) {
                        Node data = createDataNode(field, speciesContent.get(fieldName), speciesContent, mappedField);
						createReferences(data, speciesContent, mappedField);
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
			return speciesElement
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

	private Node getImages(List<Map> imagesMetaData, String fieldName, String fieldType, String customFormat, String delimiter, Map speciesContent, Node speciesElement, String imagesDir) {
        log.debug "Getting images"
		Node images = new Node(speciesElement, fieldType);
		def result = getCustomFormat(customFormat);
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()):-1
		int location = result.get("location") ? Integer.parseInt(result.get("location")?.toString())-1:-1
		int source = result.get("source") ? Integer.parseInt(result.get("source")?.toString())-1:-1
		int caption = result.get("caption") ? Integer.parseInt(result.get("caption")?.toString())-1:-1
		int attribution = result.get("attribution") ? Integer.parseInt(result.get("attribution")?.toString())-1:-1
		int contributor = result.get("contributor") ? Integer.parseInt(result.get("contributor")?.toString())-1:-1
		int license = result.get("license") ? Integer.parseInt(result.get("license")?.toString())-1:-1
		int name = result.get("name") ? Integer.parseInt(result.get("name")?.toString())-1:-1
		boolean incremental = result.get("incremental") ? new Boolean(result.get("incremental")) : false
		//String imagesmetadatasheet = result.get("imagesmetadatasheet") ?: null
        
		if(imagesMetaData) {
			//TODO:This is getting repeated for every row in spreadsheet costly
			fieldName.split(",").eachWithIndex { t, index ->
				String txt = speciesContent.get(t);
                if(delimiter) {
                    txt.split(delimiter).each { loc ->
                        if(loc) {
                            createImages(images, loc, imagesMetaData, imagesDir);
                        }
                    }
                } else {
						createImages(images, txt, imagesMetaData, imagesDir);
                }
			}
		} else {
			List<String> groupValues = new ArrayList<String>();
			fieldName.split(",").eachWithIndex { t, index ->
				try{
				String txt = speciesContent.get(t.trim());
				if (index != 0 && index % group == 0) {
					populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental, imagesDir);
					groupValues = new ArrayList<String>();
				}
				groupValues.add(txt);
				}catch(e) {
					e.printStackTrace()
				}
			}
			populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental, imagesDir);
		}
		return images;
	}

	private void populateImageNode(Node images, List<String> groupValues, String delimiter, int location, int source, int caption, int attribution, int contributor, int license, int name, boolean incremental, String imagesDir) {
		if(location != -1 && groupValues.get(location)) {
			String locationStr = groupValues.get(location);
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			String uploadDir = imagesDir;
			if(locationStr) {
				if(delimiter) {
					locationStr.split(delimiter).each { loc ->
						createImageNode(images, groupValues, loc, uploadDir, source, caption, attribution, contributor, license, name, incremental);
					}
				} else {
					createImageNode(images, groupValues, locationStr, uploadDir, source, caption, attribution, contributor, license, name, incremental);
				}
			}
		}
	}

	private void createImageNode(Node images, List<String> groupValues, String loc, String uploadDir, int source, int caption, int attribution, int contributor, int license, int name, boolean incremental) {
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
				if(contributor != -1 && groupValues.get(contributor)) new Node(image, "contributor", groupValues.get(contributor));
				if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
			}
		} else if(imagesLocation.exists()){
			Node image = new Node(images, "image");
			new Node(image, "refKey", loc);
			new Node(image, "fileName", imagesLocation.getAbsolutePath());
			if(source != -1 && groupValues.get(source)) new Node(image, "source", groupValues.get(source));
			if(caption != -1 && groupValues.get(caption)) new Node(image, "caption", groupValues.get(caption));
			if(attribution != -1 && groupValues.get(attribution)) new Node(image, "attribution", groupValues.get(attribution));
			if(contributor != -1 && groupValues.get(contributor)) new Node(image, "contributor", groupValues.get(contributor));
			if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
		} else {
            log.error "Image is not present at ${imagesLocation}"
        }
	}

    void setLogAppender(FileAppender fa) {
        if(fa) {
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }
	
	def addToSummary(String str){
		if(str){
			summary.append(str+ System.getProperty("line.separator"))
		}
	}
	
	def String getSummary(){
		return summary.toString()
	}
}
