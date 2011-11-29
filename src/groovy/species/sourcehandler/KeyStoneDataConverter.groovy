package species.sourcehandler

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;
import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;

/**
 * Data importer for keystone data
 * @author sravanthi
 *
 */
class KeyStoneDataConverter extends SourceConverter {

	protected static SourceConverter _instance;
	private static final log = LogFactory.getLog(this);
	
	private KeyStoneDataConverter() {
	}

	//should be synchronized
	public static KeyStoneDataConverter getInstance() {
		if(!_instance) {
			_instance = new KeyStoneDataConverter();
		}
		return _instance;
	}

	/**
	 * 
	 * @param connectionUrl
	 * @param userName
	 * @param password
	 * @param mappingFile
	 * @param mappingSheetNo
	 * @param mappingHeaderRowNo
	 * @return
	 */
	public List<Species> convertSpecies(String connectionUrl, String userName, String password, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo) {
		List<Map> mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);
		
		def sql = Sql.newInstance(connectionUrl, userName,
				password, "com.mysql.jdbc.Driver");		
		
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search
		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
			
		List<Species> species = new ArrayList<Species>();
		
		NodeBuilder builder = NodeBuilder.newInstance();
		
		int i=0;
		sql.eachRow("select * from wp_posts where post_status='publish'") { row ->
			if(validRow(row, sql)) {
				log.debug "Reading post : "+row.post_title;
				Node speciesElement = builder.createNode("species");
				
				List metadataRows = sql.rows("select * from wp_postmeta where post_id="+row.id +" and meta_value != ''");
				
				for(Map mappedField : mappingConfig) {
					String fieldName = mappedField.get("field name(s)")
					String delimiter = mappedField.get("content delimiter");
					String customFormat = mappedField.get("content format");
					
					if(fieldName) {
						fieldName = fieldName.toLowerCase();
						Node field = new Node(speciesElement, "field");
						Node concept = new Node(field, fieldsConfig.CONCEPT, mappedField.get(fieldsConfig.CONCEPT));
						Node category = new Node(field, fieldsConfig.CATEGORY, mappedField.get(fieldsConfig.CATEGORY));
						Node subcategory = new Node(field, fieldsConfig.SUBCATEGORY, mappedField.get(fieldsConfig.SUBCATEGORY));
						if(mappedField.get(fieldsConfig.SUBCATEGORY)?.equalsIgnoreCase(fieldsConfig.SCIENTIFIC_NAME)) {
							createDataNode(field, row.post_title, row.id, mappedField);
						} else if (customFormat && mappedField.get(fieldsConfig.CATEGORY)?.equalsIgnoreCase(fieldsConfig.IMAGES)) {
							//Node images = getImages(file, fieldName, customFormat, delimiter, speciesContent, speciesElement);
						} else if (customFormat && category.text().equalsIgnoreCase(fieldsConfig.ICONS)) {
							//Node images = getIcons(fieldName, customFormat, speciesContent);
							//new Node(speciesElement, icons);
						} else if (customFormat && category.text().equalsIgnoreCase(fieldsConfig.AUDIO)) {
							//Node images = getAudio(fieldName, customFormat, speciesContent);
							//new Node(speciesElement, audio);
						} else if (customFormat && category.text().equalsIgnoreCase(fieldsConfig.VIDEO)) {
							//Node images = getVideo(fieldName, customFormat, speciesContent);
							//new Node(speciesElement, video);
						} else if (customFormat && category.text().equalsIgnoreCase(fieldsConfig.COMMON_NAME)) {
							fieldName.split(",").eachWithIndex { t, index ->
								String txt = getContent(metadataRows, t.toLowerCase().trim());
								if(txt) {
								txt.split(",|;").each {
									Node data = createDataNode(field, it, row.id, mappedField);
									Node language = new Node(data, "language");
									Node name = new Node(language, "name", t.toLowerCase().trim());
								}
								}
							}
						} else if(customFormat) {
							String text = getCustomFormattedText(mappedField.get("field name(s)"), customFormat, metadataRows);
							createDataNode(field, text, row.id, mappedField);
						} else if(delimiter) {
							String text = getContent(metadataRows, fieldName);
							if(text) {
								for(String part : text.split(delimiter)) {
									if(part) {
										part = part.trim();
										createDataNode(field, part, row.id, mappedField);
									}
								}
							}
						} else {
							createDataNode(field, getContent(metadataRows, fieldName), row.id, mappedField);
						}
					}
				}
				createImages(speciesElement, sql, row.id);
				log.debug speciesElement;
				XMLConverter converter = new XMLConverter();
				Species s = converter.convertSpecies(speciesElement);
				if(s)
					species.add(s);
			} else {
				log.debug "NOT A VALID ROW : "+row;
			}			
		}
		return species;
	}
	
	/**
	 * 
	 * @param metadataRows
	 * @param fieldName
	 * @return
	 */
	private String getContent(List<GroovyRowResult> metadataRows, String fieldName) {
		String content;
		metadataRows.each { metadataRow ->
			String key = metadataRow.meta_key;
			if(key.equalsIgnoreCase(fieldName)) {
				content = metadataRow.meta_value;
			}
		}
		return content;
	}
	
	/**
	 * 
	 * @param row
	 * @return
	 */
	private boolean validRow(row, sql) {
		def result = sql.firstRow("select count(*) as count from wp_postmeta where post_id="+row.id+" and meta_key='Scientific'");
		log.debug result;
		if(result.count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param field
	 * @param text
	 * @param metadataRows
	 * @param mappedField
	 * @return
	 */
	private Node createDataNode(Node field, String text, int postId, Map mappedField) {
		if(!text) return;

		Node data = new Node(field, "data", text);
		attachMetadata(data, postId, mappedField);
		return data;
	}

	/**
	 * 
	 * @param data
	 * @param speciesContent
	 * @param mappedField
	 */
	private void attachMetadata(Node data, postId, Map mappedField) {
		new Node(data, "contributor", "opendata@keystone-foundation.org");
		new Node(data, "attribution", "Keystone Foundation");
		new Node(data, "license", "BY-NC");
	}

	/**
	 * 
	 * @param customFormat
	 * @return
	 */
	private Map getCustomFormat(String customFormat) {
		return customFormat.split(';').inject([:]) { map, token ->
			token = token.toLowerCase();
			token.split('=').with {
				map[it[0]] = it[1];
			}
			map
		}
	}

	/**
	 * 
	 * @param fieldName
	 * @param customFormat
	 * @param metadataRows
	 * @return
	 */
	private getCustomFormattedText(String fieldName, String customFormat, List<GroovyRowResult> metadataRows) {
		def result = getCustomFormat(customFormat);
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()) : -1;
		boolean includeHeadings = result.get("includeheadings") ? Boolean.parseBoolean(result.get("includeheadings")?.toString()).booleanValue() : false;
		String con = "";
		fieldName.split(",").eachWithIndex { t, index ->
			String txt = getContent(metadataRows, t.toLowerCase().trim());
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

	private void createImages(Node speciesElement, sql, postId) {
		Node images = new Node(speciesElement, "images");
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		
		List imagesData = sql.rows("select post_content from wp_posts where id="+postId+" and post_status = 'publish'");
		def xmlParser = new XmlParser();
		def set = new HashSet();
		imagesData.each { imageData ->
			def text = "<post>" + imageData.post_content + "</post>";
			log.debug text;
			def xml = xmlParser.parseText(text);
			log.debug xml
			def imgNodes = xml.'**'.findAll{it.name() == 'img'}
			log.debug imgNodes;
			imgNodes.each { imgNode ->
				set.add(imgNode.@src.replaceAll(/-\d+x\d+/, ""));
			}
		}
		log.debug "Found following image paths : "+set;
		
		
		if(set) {
			set.each { imagePath ->
				Node image = new Node(images, "image");
				new Node(image, "source", imagePath);
			}
		}		
	}
}
