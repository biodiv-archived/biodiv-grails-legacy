package species.sourcehandler

import java.util.Map;
import grails.util.Environment;
import org.apache.commons.logging.LogFactory;

import species.Field;
import species.License.LicenseType;
import species.formatReader.SpreadsheetWriter;
import species.auth.SUser;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.Language;

class SourceConverter {
    protected Map licenseUrlMap;
    protected static final log = LogFactory.getLog(this);
	
	//to keep track of current species index. used for reporting error.
	public int currentRowIndex = 1;
	private StringBuilder summary;
	private StringBuilder shortSummary;

    protected fieldsMap;

    protected SourceConverter() {
        licenseUrlMap = new HashMap();
        licenseUrlMap.put(LicenseType.CC_PUBLIC_DOMAIN, "http://creativecommons.org/licenses/publicdomain/");
        licenseUrlMap.put(LicenseType.CC_BY, "http://creativecommons.org/licenses/by/3.0/");
        licenseUrlMap.put(LicenseType.CC_BY_NC, "http://creativecommons.org/licenses/by-nc/3.0/");
        licenseUrlMap.put(LicenseType.CC_BY_ND, "http://creativecommons.org/licenses/by-nd/3.0/");
        licenseUrlMap.put(LicenseType.CC_BY_NC_ND, "http://creativecommons.org/licenses/by-nc-nd/3.0/");
        licenseUrlMap.put(LicenseType.CC_BY_NC_SA, "http://creativecommons.org/licenses/by-nc-sa/3.0/");
        licenseUrlMap.put(LicenseType.CC_BY_SA, "http://creativecommons.org/licenses/by-sa/3.0/ ");
		
		summary = new StringBuilder()
		shortSummary = new StringBuilder()
    }

    protected Node createFieldNode(Field field) {
        NodeBuilder builder = NodeBuilder.newInstance();

        Node fieldNode = builder.createNode("field");
        new Node(fieldNode, "fieldInstance", field);
        return fieldNode;
    }

    protected Node createFieldNode(String concept, String category, String subcategory) {
        NodeBuilder builder = NodeBuilder.newInstance();

        Node field = builder.createNode("field");

        updateFieldNode(field, concept, category, subcategory);
        return field;
    }

    protected void updateFieldNode(Node field, String concept, String category, String subcategory) {
        if(concept)
            new Node(field, "concept", concept);
        if(category)
            new Node(field, "category", category);
        if(subcategory)
            new Node(field, "subcategory", subcategory);
    }

    protected Node createDataNode(Node field, String text) {
        if(!text) return;

        Node data = new Node(field, "data", text);
        return data;
    }

    protected Node createDataNode(Node field, String text, List<String> contributors, List<String> attributions, List<String> licenses, List<String> audiences, List<String> status) {
        if(!field) return;

        Node data = new Node(field, "data", text);
        attachMetadata(data, contributors, attributions, licenses, audiences, status);

        return data;
    }

    protected Node createDataNode(Node field, String text, Map speciesContent, Map mappedField) {
        if(!text) return;

        Node data = new Node(field, "data", text);
        attachMetadata(data, speciesContent, mappedField);
        return data;
    }

    public List<Node> createTaxonRegistryNodes(List names, String classification, SUser contributor, Language language) {
        NodeBuilder builder = NodeBuilder.newInstance();
        List nodes = [];
        names.eachWithIndex { name, index ->
            if(name) {
                Node field = builder.createNode("field");
                new Node(field, "category", classification);
                new Node(field, "subcategory", TaxonomyRank.list()[index].value());
                new Node(field, "language", language);

                Node data = new Node(field, "data", name);
                new Node(data, "contributor", contributor.email);
                
                nodes << field;
            }
        }
        return nodes;
    }

    protected void attachMetadata(Node data, List<String> contributors, List<String> attributions, List<String> licenses, List<String> audiences, List<String> status) {
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
    }

    protected void createImages(Node speciesElement, List<Map> imageMetaData, String imagesDir="") {
        Node images = new Node(speciesElement, "images");
        imageMetaData.each { imageData ->
            Node image = new Node(images, "image");
            String refKey = imageData.get("imageno.");
            if(refKey) {
                File file = new File(imagesDir, refKey);
                new Node(image, "refKey", refKey);
                new Node(image, "fileName", file.getAbsolutePath());
                new Node(image, "source", imageData.get("source"));
                new Node(image, "caption", imageData.get("possiblecaption"));
                new Node(image, "attribution", imageData.get("attribution"));
                new Node(image, "license", imageData.get("license"));
            } else {
                log.warn("No reference key for image : "+imageData);
				addToSummary("No reference key for image : "+imageData)
            }
        }
    }//

    protected void attachMetadata(Node data, Map speciesContent, Map mappedField) {

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
                    String delimiter = mappedField.get("content delimiter") ?: "\n|\\s{3,}|,|;";
                    images.split(delimiter).each {
                        String loc = cleanLoc(it)
                        new Node(imagesNode, "image", loc);
                    }
                }
            }
        }

        String iconsField = mappedField.get("icons");
        if(iconsField) {
            iconsField.split(",").each { iconField ->
                String icons = speciesContent.get(iconField.toLowerCase());
                if(icons) {
                    def iconsNode = data;
                    iconsNode = new Node(data, "icons");
                    String delimiter = mappedField.get("content delimiter") ?: "\n|\\s{3,}|,|;";
                    icons.split(delimiter).each {
                        String loc = cleanLoc(it)
                        new Node(iconsNode, "icon", loc);
                    }
                }
            }
        }

        String customFormat = mappedField.get("content format");
        if(customFormat) {
            def format = getCustomFormat(customFormat);
            String action = format.get("action") ?:null;
            if(action) {
                new Node(data, "action", action);
            }
        }
    }

	
	protected Map getCustomFormat(String customFormat) {
		if(!customFormat) return [:];
		return customFormat.split(';').inject([:]) { map, token ->
			token = token.toLowerCase();
			token.split('=').with {
				map[it[0]] = it[1];
			}
			map
		}
	}
	

	protected void createImages(Node speciesElement, List<String> imageIds, List<Map> imageMetaData, String imagesDir="", Language language="") {
		log.debug "Creating images ${imageIds}"
		
		if(!imageIds){
			addToSummary("In ImageMetadata sheet either 'id' header missing or values are blank within the column ")
			return;
		}
		
		
		Node images = new Node(speciesElement, "images");
		imageMetaData.each { imageData ->
			String refKey = imageData.get("id");
			if(imageIds.contains(refKey)) {
				Node image = new Node(images, "image");		
				String imagePath =  imageData.get("image")?:refKey;
				File file = new File(imagesDir, imagePath);
				myPrint("Absolute file path for image " + file.getAbsolutePath())
				new Node(image, "refKey", refKey);
				new Node(image, "fileName", file.getAbsolutePath());
				new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", imageData.get("caption"));
				new Node(image, "language", language);
				
				List<String> contributors = getContributors(imageData.get("contributor"));
				for(c in contributors) {
					new Node(image, "contributor", c);
				}
				
				List<String> attributions = getAttributions(imageData.get("attribution"));
				for(a in attributions) {
					new Node(image, "attribution", a);
				}
				
				List<String> licenses = getLicenses(imageData.get('license'))
				for (l in licenses) {
					new Node(image, "license", l);
				}
			}
		}
		log.debug images
	}

	protected void createImages(Node images, String imageId, List<Map> imageMetaData, String imagesDir="") {
		log.debug "Creating images ${imageId}"
		if(!imageId){
			addToSummary("In ImageMetadata sheet either 'id' header missing or values are blank within the column ")
			return
		}
		imageMetaData.each { imageData ->
			String refKey = imageData.get("id");
			if(refKey && refKey.trim().equals(imageId.trim())) {
				Node image = new Node(images, "image");
				String loc = imageData.get("imageno.")?:imageData.get("image")?:imageData.get("id");
				File file = new File(imagesDir, cleanLoc(loc));
				new Node(image, "refKey", refKey);
				myPrint(" image location " + loc)
				myPrint("Absolute file path for image " + file.getAbsolutePath())
				new Node(image, "fileName", file.getAbsolutePath());
				new Node(image, "source", imageData.get("source")?:imageData.get("url"));
				new Node(image, "caption", imageData.get("possiblecaption")?:imageData.get("caption"));
				new Node(image, "attribution", imageData.get("attribution"));
				new Node(image, "contributor", imageData.get("contributor"));
				new Node(image, "license", imageData.get("license"));
			}
		}
	}

	protected String cleanLoc(String loc) {
		return loc.replaceAll("\\\\", File.separator);
	}


    protected List<String> getContributors(String text, String delimiter="\n") {
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

    protected List<String> getAttributions(String text, String delimiter="\n") {
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

    protected List<String> getLicenses(String text, String delimiter=",|;|\n") {
        log.debug "Creating licenses ${text}"
        List<String> licenses = [];
        if(text) {
            text.split(delimiter).each {
                if(it) {
                    licenses << it.trim()
                }
            }
        }
        return licenses;
    }

    protected List<String> getAudience(String text, String delimiter=",|;|\n") {
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

    protected List<String> getImages(String text, String delimiter=",|;|\n|\\s{3,}") {
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

    protected void createCommonNames(Node field, String text, Map speciesContent) {
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

    protected void createCommonNames(Node field, String text, String delimiter="\n") {
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

    protected void createLanguage(Node dataNode, String s) {
        log.debug "Getting language $s"
        if(!s || s.equals("")) return null;
        String[] str = s.split("\\(|\\)");
        Node langNode = new Node(dataNode, "language");
        if(str.length > 1)
            new Node(langNode, "threeLetterCode", str[1]?.trim());
        new Node(langNode, "name", str[0]?.trim())
    }

    protected void createSynonyms(Node field, String text, String delimiter="\n|\\s{3,}") {
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

    protected void createSynonyms(Node field, String text, Map speciesContent, Map relationshipsRow) {
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

    protected void createReferences(Node field, String text, String delimiter="\\\n") {
        log.debug "Creating references delimiter ${delimiter}   === text == ${text}"
        List<String> attrs = getAttributionsList(text, delimiter);
        if(text && !text.equals("")) {
            int i=0;
            for(String ref : text.split(delimiter)) {
                if(!ref.trim().equals("")) {
                    //TODO : remove other protocols as well if present
                    //				if(!ref.startsWith("http://") && ref.indexOf("http://") != -1) {
                    //					ref = ref.substring(ref.indexOf("http://"));
                    //				}

                    //				if(ref.startsWith("http://")) {
                    //					def refNode = new Node(field, "reference")
                    //					new Node(refNode, "title", attrs?attrs.get(i):"");
                    //					new Node(refNode, "url", ref);
                    //				} else {
                    def refNode = new Node(field, "reference")
                    getReferenceNode(refNode, attrs?attrs.get(i):"");
                    //				}
                    i++;
                }
            }
        }
    }

    protected List<String> getAttributionsList(String text, String delimiter="\\\n") {

        if(text && !text.equals("")) {
            List<String> newAttrsList = new ArrayList();
            text.split(delimiter).each {
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

    protected void createReferences(Node dataNode, Map speciesContent, Map mappedField) {
        log.debug "Creating References"
        def referenceFields = mappedField.get("field name(s)");		
        if(referenceFields) {
            referenceFields.split(SpreadsheetWriter.FIELD_SEP).each { referenceField ->
                String references = speciesContent.get(referenceField.toLowerCase());
                String delimiter = mappedField.get("content delimiter") ?: "\n";
                createReferences(dataNode, references, delimiter);
            }
        }
    }

    private getReferenceNode(Node refNode, String text) {
        if(text.startsWith("http://")) {
            new Node(refNode, "url", text);
        } else {
            new Node(refNode, "title", text);
        }
    }

    protected void createCountryGeoEntity(Node field, String text, Map speciesContent) {
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

	//////////////////////// custom log related ////////////////////	
	public void addToSummary(def str){
		if(str){
			String shortSumm 
			String summ 
			if(str instanceof Exception){
				StringWriter errors = new StringWriter();
				str.printStackTrace(new PrintWriter(errors));
				summ = errors.toString()
				shortSumm = str.getMessage()
			}
			summary.append(summ?:str + System.getProperty("line.separator"))
			shortSummary.append(shortSumm?:str + System.getProperty("line.separator"))
		}
	}
	
	public void addToLogs(String str){
		if(str)
			summary.append(str + System.getProperty("line.separator"))
	}
	
	public String getSummary(){
		return shortSummary.toString()
	}

	public String getLogs(){
		return summary.toString()
	}

	def myPrint(str){
		if(!Environment.getCurrent().getName().equalsIgnoreCase("pamba")){
			println str
		}
	}

    private static class FieldsMapHolder {
            private static Map<String, Field> fieldsMap = null;
            private static Map<Integer, Field> connectionMap = null;
            
            private static void init() {
                if (fieldsMap == null || connectionMap == null) {
                    synchronized(Field.class) {
                        if(fieldsMap == null || connectionMap == null) {
                            fieldsMap = new HashMap<String, Field>();
                            connectionMap = new HashMap<Integer, Field>();
                            def fields = Field.list(sort:'id');

                            def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
                            def fieldsConfig = config.speciesPortal.fields

                            for(Field field in fields) {
                                //HACK
 //                               if(field.category?.equalsIgnoreCase(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)) fieldsMap.put(field.category, field);
                                if(field.concept) fieldsMap.put(field.concept, field);
                                if(field.category) fieldsMap.put(field.category, field);
                                if(field.subCategory) fieldsMap.put(field.subCategory, field);

                                List t;
                                if(!connectionMap.get(field.connection)) {
                                    t = [];
                                    connectionMap.put(field.connection, t);
                                } else {
                                    t = connectionMap.get(field.connection);
                                }
                                t << field;
                            }
                        }
                    }
                }
            }

            public static Map<String, Field> getFieldsMap() {
               init();
               return fieldsMap;
            }

            public static Map<Integer, Field> getConnectionMap() {
               init();
               return connectionMap;
            }
    }

    String getFieldFromName(String fieldName, int level, String languageName) {
        return getFieldFromName(fieldName, level, Language.getLanguage(languageName)); 
    }
    
    String getFieldFromName(String fieldName, int level, Language language) {
        println "+++++++++++++"
        println fieldName
        println level
        println language;
        Field field = FieldsMapHolder.getFieldsMap().get(fieldName);
        println field
        if(field) {
            def t = FieldsMapHolder.getConnectionMap().get(field.connection)
            println t
            if(language) {
                t.each {
                    println language.id +" "+ it.language.id
                    if(it.language.id == language.id) { field = it; return;}
                }
            } else {
                field = t[0];
            }
            println field
            if(level == 1) return field.concept;
            if(level == 2) return field.category;
            if(level == 3) return field.subCategory;
        }
        return null;
    }
}

