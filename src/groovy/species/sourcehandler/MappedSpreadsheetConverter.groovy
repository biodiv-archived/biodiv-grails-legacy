package species.sourcehandler

import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;
import species.formatReader.SpreadsheetWriter;
import org.apache.log4j.Logger; 
import org.apache.log4j.FileAppender;
import species.utils.Utils;

class MappedSpreadsheetConverter extends SourceConverter {

	//protected static SourceConverter _instance;
	private static def log = LogFactory.getLog(this);
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields
	
	public List<Map> imagesMetaData;
	public List<Map> mappingConfig;
	
	
	public MappedSpreadsheetConverter() {
		imagesMetaData = [];
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
			addToSummary("Creating XML for row " + currentRowIndex++)
			Node speciesElement = builder.createNode("species", ['rowIndex':currentRowIndex]);
			for(Map mappedField : mappingConfig) {
				String fieldName = mappedField.get("field name(s)")
				Map delimiterMap = getCustomDelimiterMap(mappedField.get("content delimiter"));
				Map customFormatMap = getCustomFormat(mappedField.get("content format"));
				if(fieldName && (customFormatMap || speciesContent.get(fieldName.toLowerCase()))) {
					myPrint("================== PROCESSING FILED NAME == " + fieldName + "  and raw text == " + speciesContent.get(fieldName.toLowerCase()))
					fieldName = fieldName.toLowerCase();
					//String delimiter = delimiterMap.get(fieldName)
					//Map customFormat = customFormatMap.get(fieldName)
					Node field = new Node(speciesElement, "field");
					Node concept = new Node(field, "concept", mappedField.get("concept"));
					Node category = new Node(field, "category", mappedField.get("category"));
					Node subcategory = new Node(field, "subcategory", mappedField.get("subcategory"));
					if (mappedField.get("category")?.equalsIgnoreCase("images")) {
						Node images = getImages(imagesMetaData, fieldName, 'images', customFormatMap, delimiterMap, speciesContent, speciesElement, imagesDir);
					} else if (category.text().equalsIgnoreCase("icons")) {
						Node icons = getImages(imagesMetaData, fieldName, 'icons', customFormatMap, delimiterMap, speciesContent, speciesElement, imagesDir);
					} else if (category.text().equalsIgnoreCase("audio")) {
						//						Node images = getAudio(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, audio);
					} else if (category.text().equalsIgnoreCase("video")) {
						//						Node images = getVideo(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, video);
					} else if (concept.text().equalsIgnoreCase((String)fieldsConfig.INFORMATION_LISTING) && field.category.text().equalsIgnoreCase((String)fieldsConfig.REFERENCES)) {
						fieldName.split(SpreadsheetWriter.FIELD_SEP).each { fieldNameToken -> 
							fieldNameToken = fieldNameToken.trim().toLowerCase()
							String delimiter = delimiterMap.get(fieldNameToken);
							String text = speciesContent.get(fieldNameToken);
							myPrint(">>>>>>>>>>>>>>>> ZZZZ <<<<<<<<<<<<<<<<<<< =========== delimerte map " + delimiterMap)
							if(text){
								if(delimiter){
									text.split(delimiter).each { part ->
										if(part) {
											part = part.trim();
											myPrint(">>>>>>>>>>>>>>>> <<<<<<<<<<<<<<<<<<< ======= createing data aaaaaaaaa node for ref " + fieldNameToken + "  actula text " + part)
											Node data = createDataNode(field, part , speciesContent, mappedField);
											//myPrint(">>>>>>>>>>>>>>>> <<<<<<<<<<<<<<<<<<< ============ after ref creeate node " + data)
											createReferences(data, speciesContent, mappedField);
										}
									}
								}else{
									Node data = createDataNode(field, text , speciesContent, mappedField);
									//myPrint("=====ELSE PART======= after ref creeate node " + data)
									createReferences(data, speciesContent, mappedField);
								}
							}
						}
					} else if(ignoreCustomFormat(mappedField)) {
						// Here honouring delimiter if given but ingnoring custom format 
						fieldName.split(SpreadsheetWriter.FIELD_SEP).each { fieldNameToken -> 
							fieldNameToken = fieldNameToken.trim().toLowerCase()
							String text = speciesContent.get(fieldNameToken);
							def delimiter = delimiterMap.get(fieldNameToken);
							if(text) {
								boolean isCommonName = category.text().equalsIgnoreCase("common name")
								if(delimiter){
									for(String part : text.split(delimiter)) {
										if(part) {
											part = part.trim();
											if(isCommonName){
												createCommonNameNode(part, field, speciesContent, mappedField)
											} else {
												createDataNode(field, part, speciesContent, mappedField);
											}
										}
									}
								}else{
									if(isCommonName){
										createCommonNameNode(text, field, speciesContent, mappedField)
									}else{
										createDataNode(field, text, speciesContent, mappedField);
									}
								}
							}
						}
					} else {
						String text = getCustomFormattedText(mappedField.get("field name(s)"), customFormatMap, delimiterMap, speciesContent);
						createDataNode(field, text, speciesContent, mappedField);
					}
				}
			}
			return speciesElement
	}

	private createCommonNameNode(String part, Node field, Map speciesContent, Map mappedField) {
		myPrint("=========== creating common names " + part)
		String[] commonNames = part.split(":");

		if(commonNames.length == 2) {
			myPrint("=========== creating common names " + part)
			commonNames[1].split(",|;").each {
				myPrint("=========== lang " + it  + " val " + commonNames[0])
				Node data = createDataNode(field, it, speciesContent, mappedField);
				Node language = new Node(data, "language");
				Node name = new Node(language, "name", commonNames[0]);
			}
		} else {
			commonNames[0].split(",|;").each {
				createDataNode(field, it, speciesContent, mappedField);
			}
		}
	}

	protected void createReferences(Node dataNode, Map speciesContent, Map mappedField) {
        log.debug "Creating References"
        Map delimiterMap = getCustomDelimiterMap(mappedField.get("content delimiter"));
        def referenceFields = mappedField.get("field name(s)");		
        if(referenceFields) {
            referenceFields.split(SpreadsheetWriter.FIELD_SEP).each { referenceField ->
                String references = speciesContent.get(referenceField.toLowerCase().trim());
                String delimiter  = delimiterMap.get(referenceField.toLowerCase().trim()) ?: "\n";
                createReferences(dataNode, references, delimiter);
            }
        }
    }

	protected Map getCustomFormat(String customFormat) {
		if(!customFormat) return [:];
		def fMap = [:]
		customFormat.split(SpreadsheetWriter.COLUMN_SEP).each { columnsInfo ->
			def colMetaData = columnsInfo.split(SpreadsheetWriter.KEYVALUE_SEP)
			def colName = colMetaData[0]
			def metaData = colMetaData[1]
			def m = [:]
			def attrList = metaData.split(';')
				attrList.each{ attrInfo ->
				def attr = attrInfo.split('=')
				if(attr.size() > 1){
					m[attr[0]] = attr[1]
				}
			}
			fMap.put(colName, m)
		}
		return fMap
	}

	/**
	 * Ignore custom format for following field
	 * @param mappedFieldString
	 * @return
	 */
	private boolean ignoreCustomFormat(Map mappedFieldString){
		String concept = mappedFieldString.get("concept").trim()
		String category = mappedFieldString.get("category").trim()

		boolean ignore = (concept.equalsIgnoreCase((String)fieldsConfig.INFORMATION_LISTING))
		ignore = (ignore || concept.equalsIgnoreCase((String)fieldsConfig.NOMENCLATURE_AND_CLASSIFICATION))
	    ignore = (ignore || ( concept.equalsIgnoreCase((String)fieldsConfig.OVERVIEW) && category.equalsIgnoreCase("SubSpecies Varieties Races")))
				
		return ignore
	}
	
	private Map getCustomDelimiterMap(String text){
		Map m = new HashMap<String, String>()
		if(!text) return m;
		
		text.split(SpreadsheetWriter.COLUMN_SEP).each { colInfo ->
			def delimiterInfo = colInfo.split(SpreadsheetWriter.KEYVALUE_SEP)
			if(delimiterInfo.size() > 1 && delimiterInfo[1] ){
				m.put(delimiterInfo[0].trim(), getSafeDelimiter(delimiterInfo[1].trim()))
			}
		}
		
		return m
	}

	private String getSafeDelimiter(String str){
		if(str == '|' || str == '$'){
			return "\\" + str
		}
		return str
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param customFormatMap
	 * @param delimiterMap
	 * @param speciesContent
	 * @return 
	 * 
	 * TODO : Handle delimiter for each field
	 */
	
    private getCustomFormattedText(String fieldName, Map customFormatMap, Map delimiterMap, Map speciesContent) {
		String con = "";
		fieldName.split(SpreadsheetWriter.FIELD_SEP).eachWithIndex { t, index ->
			t = t.toLowerCase().trim()
			String txt = speciesContent?.get(t);
			myPrint("    >>>>>>>>>>    field name ==== " + t + " and TEXT " + txt)
			def customFormat = customFormatMap.get(t)
			myPrint("    >>>>>>>>>>    field name and customFormatMap ==== " + customFormatMap)
			int group = customFormat.get("group") ? Integer.parseInt(result.get("group")?.toString()) : -1;
			boolean includeHeadings = customFormat.get("includeheadings") ? Boolean.parseBoolean(customFormat.get("includeheadings")?.toString()).booleanValue() : false;
			boolean noCustomFormating = (group == -1 && !includeHeadings)
			//String delimiter = delimiterMap.get(t)
			if(txt) {
				if(noCustomFormating){
					con += (txt + "<br>")
				}else {
					if (index%group == 0) {
	
						if(group > 1)
							txt = "<h6>"+txt+"</h6>";
						else {
							txt = "<p>"+txt+"</p>";
							if(includeHeadings) txt = "<h6>"+Utils.getTitleCase(t.trim())+"</h6>"+txt;
						}
						if(con)
							con += txt;
						else con = txt;
	
					} else {
						txt = "<p>"+txt+"</p>";
						if(includeHeadings) txt = "<h6>"+Utils.getTitleCase(t.trim())+"</h6>"+txt;
						con += txt;
					}
				}
			}
		}
		return con;
	}

	private Node getImages(List<Map> imagesMetaData, String fieldName, String fieldType, Map customFormatMap, Map delimiterMap, Map speciesContent, Node speciesElement, String imagesDir) {
        log.debug "Getting images"
		Node images = new Node(speciesElement, fieldType);
		//def result = getCustomFormat(customFormat);
		//String imagesmetadatasheet = result.get("imagesmetadatasheet") ?: null
        def customFormat, delimiter, group, location, source, caption, attribution, contributor, license, name, incremental
		
		if(imagesMetaData) {
			//TODO:This is getting repeated for every row in spreadsheet costly
			fieldName.split(SpreadsheetWriter.FIELD_SEP).eachWithIndex { t, index ->
				String txt = speciesContent.get(t);
				if(txt && txt.trim()){
					customFormat =  customFormatMap.get(t.trim().toLowerCase());
					delimiter = delimiterMap.get(t.trim().toLowerCase());
					group = customFormat.get("group") ? Integer.parseInt(customFormat.get("group")?.toString()):-1
					location = customFormat.get("location") ? Integer.parseInt(customFormat.get("location")?.toString())-1:-1
					source = customFormat.get("source") ? Integer.parseInt(customFormat.get("source")?.toString())-1:-1
					caption = customFormat.get("caption") ? Integer.parseInt(customFormat.get("caption")?.toString())-1:-1
					attribution = customFormat.get("attribution") ? Integer.parseInt(customFormat.get("attribution")?.toString())-1:-1
					contributor = customFormat.get("contributor") ? Integer.parseInt(customFormat.get("contributor")?.toString())-1:-1
					license = customFormat.get("license") ? Integer.parseInt(customFormat.get("license")?.toString())-1:-1
					name = customFormat.get("name") ? Integer.parseInt(customFormat.get("name")?.toString())-1:-1
					incremental = customFormat.get("incremental") ? new Boolean(customFormat.get("incremental")) : false
		
				
                	if(delimiter) {
                    	txt.split(delimiter).each { loc ->
							createImages(images, loc, imagesMetaData, imagesDir);
                    	}
                	} else {
						createImages(images, txt, imagesMetaData, imagesDir);
                	}
				}else{
					addToSummary("In data sheet values are blank within marked image column ")
				}
			}
		} else {
			List<String> groupValues = new ArrayList<String>();
			fieldName.split(SpreadsheetWriter.FIELD_SEP).eachWithIndex { t, index ->
				customFormat =  customFormatMap.get(t.trim().toLowerCase());
				delimiter = delimiterMap.get(t.trim().toLowerCase());
				
				group = customFormat.get("group") ? Integer.parseInt(customFormat.get("group")?.toString()):-1
				location = customFormat.get("location") ? Integer.parseInt(customFormat.get("location")?.toString())-1:-1
				source = customFormat.get("source") ? Integer.parseInt(customFormat.get("source")?.toString())-1:-1
				caption = customFormat.get("caption") ? Integer.parseInt(customFormat.get("caption")?.toString())-1:-1
				attribution = customFormat.get("attribution") ? Integer.parseInt(customFormat.get("attribution")?.toString())-1:-1
				contributor = customFormat.get("contributor") ? Integer.parseInt(customFormat.get("contributor")?.toString())-1:-1
				license = customFormat.get("license") ? Integer.parseInt(customFormat.get("license")?.toString())-1:-1
				name = customFormat.get("name") ? Integer.parseInt(customFormat.get("name")?.toString())-1:-1
				incremental = customFormat.get("incremental") ? new Boolean(customFormat.get("incremental")) : false
		
				try{
				String txt = speciesContent.get(t.trim());
				if(txt && txt.trim()){
					if (index != 0 && index % group == 0) {
						populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental, imagesDir);
						groupValues = new ArrayList<String>();
					}
					groupValues.add(txt);
				}else{
					addToSummary("In data sheet values are blank within marked image column")
				}
				}catch(e) {
					e.printStackTrace()
				}
			}
			if(!groupValues.isEmpty()){
				populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental, imagesDir);
		}	}
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
			addToSummary("Image is not present at ${imagesLocation}")
        }
	}

    void setLogAppender(FileAppender fa) {
        if(fa) {
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }
	
	protected void attachMetadata(Node data, Map speciesContent, Map mappedField) {
		addMetaAttibute("contributor", data, speciesContent, mappedField, "contributor", ",|;|\n")
		addMetaAttibute("attributions", data, speciesContent, mappedField, "attribution", "\n")
		addMetaAttibute("license", data, speciesContent, mappedField, "license", ",|;|\n")
		addMetaAttibute("audience", data, speciesContent, mappedField, "audienceType", ",|;|\n")
		addMetaAttibute("references", data, speciesContent, mappedField, "reference", "\n")
		addMetaAttibute("images", data, speciesContent, mappedField, "image", "\n|\\s{3,}|,|;")
	}
	
	private void addMetaAttibute(String fieldName, Node data, Map speciesContent, Map mappedField, String resultNodeName, String defaultDelimiter="\n"){
		String fields = mappedField.get(fieldName)
		boolean doProcess = false
		Map fMap = [:]
		if(fields){
			fields.split(SpreadsheetWriter.COLUMN_SEP).each { columnsInfo ->
				def colMetaData = columnsInfo.split(SpreadsheetWriter.KEYVALUE_SEP)
				if(colMetaData.size() > 1){
					def colName = colMetaData[0]
					def infoColList = colMetaData[1].split(',').collect { it.trim().toLowerCase() }
					fMap.put(colName, infoColList)
					doProcess = true
				}
			}
		}
		
		if(doProcess){
			List processedInfoColumn = []
			//myPrint("=======ADD meta attribute  for field " + fieldName + "  fields " + fields + " fMap " + fMap + " content delim  " + mappedField.get("content delimiter"))
			fMap.keySet().each { key ->
				//if column has text then only proceed
				if(speciesContent.get(key)){
					def infoColList = fMap.get(key)
					infoColList.each { infoCol ->
						String text = speciesContent.get(infoCol)
						String delimiter =  defaultDelimiter;
						//if meta attribute has text and that column is not processed then only adding
						if(text &&  !processedInfoColumn.contains(infoCol)){
							processedInfoColumn << infoCol
							if(resultNodeName == 'image'){
								def imagesNode = data;
								imagesNode = new Node(data, "images");
								text.split(delimiter).each {
									String loc = cleanLoc(it)
									new Node(imagesNode, "image", loc);
								}
							}else{
								myPrint("========= <<<<<<<<<<<<<<<<  meta data >>>>>>>>>>>>>>> ==== " + text + "   fieldname " +  fieldName + " delimiter " + delimiter)
								text.split(delimiter).each {
									if(resultNodeName == 'reference'){
										Node refNode = new Node(data, "reference");
										getReferenceNode(refNode, it);
									}else{
										new Node(data, resultNodeName, it);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
}
