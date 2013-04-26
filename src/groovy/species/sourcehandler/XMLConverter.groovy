package species.sourcehandler

import java.util.List
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

import species.Classification
import species.CommonNames
import species.Contributor
import species.Country
import species.Field
import species.GeographicEntity
import species.Language
import species.License
import species.NamesParser
import species.Reference
import species.Resource
import species.Species
import species.SpeciesField
import species.Synonyms
import species.TaxonomyDefinition
import species.TaxonomyRegistry
import species.License.LicenseType
import species.Resource.ResourceType
import species.SpeciesField.AudienceType
import species.Synonyms.RelationShip
import species.TaxonomyDefinition.TaxonomyRank
import species.groups.SpeciesGroup;
import species.utils.HttpUtils
import species.utils.ImageUtils
import species.utils.Utils
import org.apache.log4j.Logger; 
import org.apache.log4j.FileAppender;

//import org.grails.plugins.sanitizer.MarkupSanitizerResult


class XMLConverter extends SourceConverter {


    //protected static SourceConverter instance;
    private static def log = LogFactory.getLog(this);
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def fieldsConfig = config.speciesPortal.fields
    NamesParser namesParser;
    String resourcesRootDir = config.speciesPortal.resources.rootDir;

    private Species s;

    def groupHandlerService;
    //def markupSanitizerService;

    public enum SaveAction {
        MERGE("merge"),
        OVERWRITE("overwrite"),
        IGNORE("ignore");

        private String value;

        SaveAction(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public XMLConverter() {
        namesParser = new NamesParser();
        def ctx = ApplicationHolder.getApplication().getMainContext();
        //markupSanitizerService = ctx.getBean("markupSanitizerService");
    }

    //should be synchronized
/*    public static XMLConverter getInstance() {
        if(!instance) {
            instance = new XMLConverter();
        }
        return instance;
    }*/

    public Species convertSpecies(Node species) {
        //TODO default action to be merge
        convertSpecies(species, SaveAction.MERGE);
    }

    public Species convertSpecies(Node species, SaveAction defaultSaveAction) {

        if(!species) return null;

        try {
            log.info "Creating/Updating species"
            log.debug species;
            s = new Species();

            removeInvalidNode(species);

            //sciName is must for the species to be populated
            Node speciesNameNode = species.field.find {it.subcategory.text().equalsIgnoreCase(fieldsConfig.SCIENTIFIC_NAME);}
            def speciesName = getData(speciesNameNode?.data);
            if(speciesName) {
                //getting classification hierarchies and saving these taxon definitions
                List<TaxonomyRegistry> taxonHierarchy = getClassifications(species.children(), speciesName, true);

                //taxonConcept is being taken from only author contributed taxonomy hierarchy
                TaxonomyDefinition taxonConcept = getTaxonConcept(taxonHierarchy);

                // if the author contributed taxonomy hierarchy is not specified
                // then the taxonConept is null and sciName of species is saved as concept and is used to create the page
                s.taxonConcept = taxonConcept ?: getTaxonConceptFromName(speciesName);

                if(s.taxonConcept) {

                    s.title = s.taxonConcept.italicisedForm;

                    //taxonconcept is being used as guid
                    s.guid = constructGUID(s);

                    //a species page with guid as taxon concept is considered as duplicate
                    Species existingSpecies = findDuplicateSpecies(s);

                    //either overwrite or merge if an existing species exists
                    if(existingSpecies) {
                        if(defaultSaveAction == SaveAction.OVERWRITE || existingSpecies.percentOfInfo == 0){
                            log.info "Deleting old version of species : "+existingSpecies.id;
                            try {
                                s.id = existingSpecies.id;
                                if(!existingSpecies.delete(flush:true)) {
                                    existingSpecies.errors.allErrors.each { log.error it }
                                }
                            }
                            catch(org.springframework.dao.DataIntegrityViolationException e) {
                                e.printStackTrace();
                                log.error "Could not delete species ${existingSpecies.id} : "+e.getMessage();
                                return;
                            }
                        } else if(defaultSaveAction == SaveAction.MERGE){
                            log.info "Merging with already existing species information : "+existingSpecies.id;
                            //mergeSpecies(existingSpecies, s);
                            s = existingSpecies;
                            s.resources.clear();
                        } else {
                            log.warn "Ignoring species as a duplicate is already present : "+existingSpecies.id;
                            return;
                        }
                    }

                    List<Resource> resources = createMedia(species, s.taxonConcept.canonicalForm);
                    log.debug "Resources ${resources}"
                    resources.each { s.addToResources(it); }

                    List<Synonyms> synonyms;

                    for(Node fieldNode : species.children()) {
                        if(fieldNode.name().equals("field")) {
                            if(!isValidField(fieldNode)) {
                                log.warn "NOT A VALID FIELD : "+fieldNode;
                                continue;
                            }

                            String concept = fieldNode.concept?.text()?.trim();
                            String category = fieldNode.category?.text()?.trim();
                            String subcategory = fieldNode.subcategory?.text()?.trim();

                            if(category && category.equalsIgnoreCase(fieldsConfig.COMMON_NAME)) {
                                List<CommonNames> commNames = createCommonNames(fieldNode, s.taxonConcept);
                                //commNames.each { s.addToCommonNames(it); }
                            } else if(category && category.equalsIgnoreCase(fieldsConfig.SYNONYMS)) {
                                synonyms = createSynonyms(fieldNode, s.taxonConcept);
                                //synonyms.each { s.addToSynonyms(it); }
                            } else if(subcategory && subcategory.equalsIgnoreCase(fieldsConfig.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY)) {
                                List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                                countryGeoEntities.each {
                                    if(it.species == null) {
                                        s.addToGlobalDistributionEntities(it);
                                    }
                                }
                            } else if(subcategory && subcategory.equalsIgnoreCase(fieldsConfig.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY)) {
                                List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                                countryGeoEntities.each {
                                    if(it.species == null) {
                                        s.addToGlobalEndemicityEntities(it);
                                    }
                                }
                            }  else if(subcategory && subcategory.equalsIgnoreCase(fieldsConfig.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY)) {
                                List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                                countryGeoEntities.each {
                                    if(it.species == null) {
                                        s.addToIndianDistributionEntities(it);
                                    }
                                }
                            } else if(subcategory && subcategory.equalsIgnoreCase(fieldsConfig.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY)) {
                                List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                                countryGeoEntities.each {
                                    if(it.species == null) {
                                        s.addToIndianEndemicityEntities(it);
                                    }
                                }

                            } else if(category && category.toLowerCase().endsWith(fieldsConfig.TAXONOMIC_HIERARCHY.toLowerCase())) {
                                //ignore
                                println "ignoring hierarchy" 
                            } else {
                                List<SpeciesField> speciesFields = createSpeciesFields(s, fieldNode, SpeciesField.class, species.images[0], species.icons[0], species.audio[0], species.video[0], synonyms);
                                speciesFields.each {
                                    if(it.species == null) { // if its already associated this field will be populated
                                        log.debug "Adding new fields to species ${s}"
                                        s.addToFields(it);
                                    }
                                }
                            }
                        }
                    }

                    //adding taxonomy classifications
                    taxonHierarchy.each { s.addToTaxonomyRegistry(it); }

                    //					if(defaultSaveAction == SaveAction.MERGE){
                    //						log.info "Merging with already existing species information : "+existingSpecies.id;
                    //						mergeSpecies(existingSpecies, s);
                    //						s = existingSpecies;
                    //					}

                    return s;
                } else {
                    log.error "TaxonConcept is not found"
                }
            } else {
                log.error "IGNORING SPECIES AS SCIENTIFIC NAME COULD NOT BE PARSED : "+speciesName;
            }
        } catch(Exception e) {
            log.error "ERROR CONVERTING SPECIES : "+e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Removing nodes whose field concept is null
     * @param speciesNodes
     */
    private void removeInvalidNode(Node speciesNodes) {
        for(Node fieldNode : speciesNodes.children()) {
            if(fieldNode.name().equals("field")) {
                if(!isValidField(fieldNode)) {
                    log.warn "NOT A VALID FIELD. IGNORING : "+fieldNode;
                    fieldNode.parent().remove(fieldNode);
                    continue;
                }
            }
        }
    }

    /**
     * Using the taxonConcept as guid
     * @param species
     * @return
     */
    String constructGUID(Species species) {
        return species.taxonConcept?.id;
    }

    /**
     * A node is not valid if its concept node is not defined
     * @param fieldNode
     * @return
     */
    private boolean isValidField(Node fieldNode) {
        return !!fieldNode.concept?.text();
    }

    /**
     * 
     * @param fieldNode
     * @param sFieldClass
     * @param imagesNode
     * @param iconsNode
     * @param audiosNode
     * @param videosNode
     * @return
     */
    private List<SpeciesField> createSpeciesFields(Species s, Node fieldNode, Class sFieldClass, Node imagesNode, Node iconsNode, Node audiosNode, Node videosNode, List<Synonyms> synonyms) {
        log.debug "Creating species field from node : "+fieldNode;
        List<SpeciesField> speciesFields = new ArrayList<SpeciesField>();
        def field = getField(fieldNode, false);
        if(field == null) {
            log.warn "NO SUCH FIELD : "+field;
            return;
        }
      
        List sFields = [];
        
         if(s.isAttached()) {
             sFields = SpeciesField.withCriteria() {
                eq("field", field)
                eq('species', s)
            }
        }

        for(Node dataNode : fieldNode.data) {
            String data = getData(dataNode);
            data = cleanData(data, s.taxonConcept, synonyms);
            List<Contributor> contributors = getContributors(dataNode, true);
            List<License> licenses = getLicenses(dataNode, false);
            List<AudienceType> audienceTypes = getAudienceTypes(dataNode, true);
            List<Resource> resources = getResources(dataNode, imagesNode, iconsNode, audiosNode, videosNode);
            List<Reference> references = getReferences(dataNode, true,s.taxonConcept, synonyms);
            List<Contributor> attributors = getAttributions(dataNode, true);
            SpeciesField speciesField;

            def temp = []
            //TODO:HACK just for keystone
            String dt = data.replaceAll("</?p>","");
            for (sField in sFields) {
                if(isDuplicateSpeciesField(sField,contributors, dt)) {
                    log.debug "Found already existing species fields for field ${field} ${sField}"
                    temp << sField;
                }
            }

            println "Found duplicate fields ${temp}"

                if(dataNode.action?.text()?.trim() == "merge") {
                    for(sField in temp) {

                    //HACK for deleting keystone fields with different content and contributor 
                    /*if(dt.equals(sField.description.replaceAll("</?p>",""))) {
                        println "Deleting same description field"
                        s.removeFromFields(sField);
                        sField.delete();

                    } else */if(sField.description.contains(dt)){
                        log.debug "Field already contains given text"
                    } else {
                        log.debug "Merging description from existing ${sField}. Removing all metadata associate with previous field."
                        data = sField.description + "<br/>" + data
                        speciesField = sField
                    }
                    }
                } else {
                    for(sField in temp) {

                        //HACK for deleting keystone fields with different content and contributor 
                        /*if(!dt.equals(sField.description.replaceAll("</?p>",""))) {
                            println "Deleting different description field & retaining new description field"
                            s.removeFromFields(sField);
                            sField.delete()
                        } else {*/
                            speciesField = sField
                       // }
                    }
                }

            if(!speciesField) {
                log.debug "Adding new field to species ${s}"
                speciesField = sFieldClass.newInstance(field:field, description:data);
            } else {
                log.debug "Overwriting existing ${speciesField}. Removing all metadata associate with previous field."
                speciesField.description = data;
                //TODO: Will have to clean up orphaned entries from following tables
                speciesField.contributors?.clear()
                speciesField.licenses?.clear()
                speciesField.audienceTypes?.clear()
                speciesField.attributors?.clear()
                speciesField.resources?.clear()
                speciesField.references?.clear()
            }

            if(speciesField && contributors) {
                contributors.each { speciesField.addToContributors(it); }
                licenses.each { speciesField.addToLicenses(it); }
                audienceTypes.each { speciesField.addToAudienceTypes(it); }
                attributors.each {  speciesField.addToAttributors(it); }
                resources.each {  speciesField.addToResources(it); }
                references.each {  speciesField.addToReferences(it); }
                speciesFields.add(speciesField);
                println "----${speciesField.field.category}......${speciesField.contributors}"
            } else {
                log.error "IGNORING SPECIES FIELD AS THERE ARE NO CONTRIBUTORS"
            }			
        }
        return speciesFields;
    }

    private String getData(Node dataNode) {
        if(!dataNode) return "";
        //sanitize the html text
        return dataNode.text()?:"";
    }

    /**
    * A field is duplicate if atleast one of the contributors is same.
    * 
    **/
    private boolean isDuplicateSpeciesField(SpeciesField sField, contributors, data) {
        for(c1 in sField.contributors) {
            for(c2 in contributors) {
                if(c1.id == c2.id) {
                    return true;
                }
            }
        }

        /**
        * Hack to find duplicate where conributor is not specified or specified to be dummy
        * Resetting contributor back to the fields contributors.
        * TODO: need to remove
        **/
        i/*for(c1 in sField.contributors) {
            if((c1.name.equals("dummy")||c1.name.equals("pearlsravanthi"))) {
                println 'is dummy node'
                println sField.description
                println data
                def desc = sField.description.replaceAll("</?p>","");
                if( desc.equals(data)) {
                    println "is duplicate"
                    println "resetting contributor ${contributors}"
                    sField.removeFromContributors(c1);
                    contributors.each { sField.addToContributors(it); }
                    sField.save();
                    return true;
                }
            }
        }

        //HACK .. contributors shd never be empty
        def desc = sField.description.replaceAll("</?p>","");
        if(sField.contributors.isEmpty() && desc.equals(data)) {
            println "resetting contributor ${contributors}"
            contributors.each { sField.addToContributors(it); }
            sField.save();
            return true;
        }*/
        return false;
    }

    private String cleanData(String text, TaxonomyDefinition taxon, List<Synonyms> synonyms) {
        //MarkupSanitizerResult result = markupSanitizerService.sanitize(text)
        //if(!result.isInvalidMarkup()) {
        String cleanString = text;//result.cleanString
        cleanString.replaceAll(taxon.name, '<i>'+taxon.name+'</i>');
        if(synonyms) {
            synonyms.each {
                cleanString.replaceAll(it.name, '<i>'+it.name+'</i>');
            }
        }
        cleanString = cleanString.replaceAll('<i>\\s+<i>','<i>').replaceAll('</i>\\s+</i>','</i>')
        return cleanString;
        //} else {
        //	log.error result.errorMessages
        //	return ''
        //}
    }

    /**
     * 
     * @param fieldNode
     * @param createNew
     * @return
     */
    private Field getField(Node fieldNode, boolean createNew) {
        String concept = fieldNode.concept?.text()?.trim();
        String category = fieldNode.category?.text()?.trim();
        String subCategory = fieldNode.subcategory?.text()?.trim();
        def fieldCriteria = Field.createCriteria();

        Field field = fieldCriteria.get {
            and {
                ilike("concept", concept);
                category ? ilike("category", category) : isNull("category");
                subCategory ? ilike("subCategory", subCategory) : isNull("subCategory");
            }
        }

        if(!field && createNew) {
            String description = getData(fieldNode.description);
            int displayOrder = Math.round(Float.parseFloat(fieldNode.displayOrder));

            field = new Field(concept:concept, category:category, subCategory:subCategory, displayOrder:displayOrder, description:description);
            if(!field.save(flush:true, failOnError: true)) {
                field.errors.each { log.error it }
            }
        }
        return field;
    }

    /**
     * 
     * @param dataNode
     * @param createNew
     * @return
     */
    private List<Contributor> getContributors(Node dataNode, boolean createNew) {
        List<Contributor> contributors = new ArrayList<Contributor>();
        dataNode.contributor.each {
            String contributorName = it.text()?.trim();
            Contributor contributor = getContributorByName(contributorName, createNew);
            if(contributor) {
                contributors.add(contributor);
            } else {
                log.warn "NOT A VALID CONTIBUTOR : "+contributorName;
            }
        }
        return contributors;
    }

    /**
     * 
     * @param contributorName
     * @param createNew
     * @return
     */
    public Contributor getContributorByName(String contributorName, boolean createNew) {
        if(!contributorName) return;

        def contributor = Contributor.findByName(contributorName);
        if(!contributor && createNew && contributorName != null) {
            contributor = new Contributor(name:contributorName);
            if(!contributor.save(flush:true)) {
                contributor.errors.each { log.error it }
            }
        }
        return contributor;
    }

    /**
     * 
     * @param dataNode
     * @param createNew
     * @return
     */
    private List<License> getLicenses(Node dataNode, boolean createNew) {
        List<License> licenses = new ArrayList<License>();
        dataNode.license.each {
            String licenseType = it.text()?.trim();
            License license = getLicenseByType(licenseType, createNew);
            if(license) {
                licenses.add(license);
            } else {
                log.warn "NOT A SUPPORTED LICENSE TYPE: "+licenseType;
            }
        }

        if(!licenses) {
            licenses.add(getLicenseByType(LicenseType.CC_BY, createNew));
        }

        println "****returning ${licenses}";
        return licenses;
    }

    /**
     * 
     * @param licenseType
     * @param createNew
     * @return
     */
    License getLicenseByType(licenseType, boolean createNew) {
        if(!licenseType) return null;

        LicenseType type;
        if(licenseType instanceof LicenseType) {
            type = licenseType
        } else {
            licenseType = licenseType?.toString().trim();
            if(!licenseType.startsWith("CC")) {
                licenseType = "CC "+licenseType.trim()
            }
            for(LicenseType t : LicenseType) {
                if(t.value().equalsIgnoreCase(licenseType)) {
                    type = t;
                    break;
                }
            }
        }

        if(!type) return null;

        def license = License.findByName(type);
        if(!license && createNew) {
            license = new License(name:type, url:this.licenseUrlMap.get(type));
            if(!license.save(flush:true)) {
                license.errors.each { log.error it }
            }
        }
        return license;
    }

    /**
     * 
     * @param dataNode
     * @param createNew
     * @return
     */
    private List<AudienceType> getAudienceTypes(Node dataNode, boolean createNew) {
        List<AudienceType> audienceTypes = new ArrayList<AudienceType>();
        dataNode.audienceType.each {
            String audienceTypeType = it.text()?.trim();
            AudienceType audienceType = getAudienceTypeByType(audienceTypeType);
            if(audienceType) {
                audienceTypes.add(audienceType);
            } else {
                log.warn "NOT A SUPPORTED AUDIENCE TYPE: "+audienceType;
            }
        }
        return audienceTypes;
    }

    /**
     * 
     * @param audienceType
     * @return
     */
    private AudienceType getAudienceTypeByType(String audienceType) {
        if(!audienceType) return null;
        for(AudienceType type : AudienceType) {
            if(type.value().equals(audienceType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Creates media instances and associates them with species
     * @param s species domain object that is to be populated
     * @param species an xml having media nodes
     */
    List<Resource> createMedia(resourcesXML, String relResFolder) {
        List<Resource> resources = [];

        if(resourcesXML) {
            //saving media
            def imagesNode = resourcesXML.images;
            def iconsNode = resourcesXML.icons;
            def audiosNode = resourcesXML.audios;
            def videosNode = resourcesXML.videos;

            resources.addAll(createResourceByType(imagesNode[0], ResourceType.IMAGE, relResFolder));
            resources.addAll(createResourceByType(iconsNode[0], ResourceType.ICON, "icons"));
            //resources.addAll(createResourceByType(audiosNode, ResourceType.AUDIO));
            resources.addAll(createResourceByType(videosNode[0], ResourceType.VIDEO, relResFolder));
        }

        return resources;
    }

    /**
     * Creating the resources
     * @param s
     * @param resourceNode xml giving resource details
     * @param resourceType type of the resource
     */
    private List<Resource> createResourceByType(Node resourceNode, ResourceType resourceType, String relResFolder) {

        List<Resource> resources = [];
        if(resourceNode) {
            switch(resourceType) {
                case ResourceType.IMAGE:
                resourceNode?.image.each {
                    if(!it?.id) {
                        def resource = createImage(it, relResFolder, ResourceType.IMAGE);
                        if(resource) {
                            resources.add(resource);
                        }
                    }
                }
                break;
                case ResourceType.ICON:
                resourceNode?.image.each {
                    if(!it?.id) {
                        def resource = createImage(it, relResFolder, ResourceType.ICON);
                        if(resource) {
                            resources.add(resource);
                        }
                    }
                }
                break;
                case ResourceType.AUDIO:
                resourceNode?.audio.each { if(!it?.id) resources.add(createAudio(it)); }
                break;
                case ResourceType.VIDEO:
                resourceNode?.video.each { if(!it?.id) resources.add(createVideo(it)); }
            }
        }
        return resources;
    }

    /**
     * 
     * @param s
     * @param imageNode
     * @return
     */
    private Resource createImage(Node imageNode, String relImagesFolder, resourceType) {
        File tempFile = getImageFile(imageNode);
        def sourceUrl = imageNode.source?.text() ? imageNode.source?.text() : "";

        log.debug "Creating image resource : "+tempFile;

        if(tempFile && tempFile.exists()) {
            //copying file
            relImagesFolder = Utils.cleanFileName(relImagesFolder.trim());

            File root = new File(resourcesRootDir , relImagesFolder);
            if(!root.exists() && !root.mkdirs()) {
                log.error "COULD NOT CREATE DIR FOR SPECIES : "+root.getAbsolutePath();
            }
            log.debug "in dir : "+root.absolutePath;

            File imageFile = new File(root, Utils.cleanFileName(tempFile.getName()));
            if(!imageFile.exists()) {
                try {
                    Utils.copy(tempFile, imageFile);
                    ImageUtils.createScaledImages(imageFile, imageFile.getParentFile());
                } catch(FileNotFoundException e) {
                    log.error "File not found : "+tempFile.absolutePath;
                }
            }

            String path = imageFile.absolutePath.replace(resourcesRootDir, "");

            def res = Resource.findByFileNameAndType(path, resourceType);

            if(!res) {
                log.debug "Creating new resource"
                res = new Resource(type : resourceType, fileName:path, description:imageNode.caption?.text(), mimeType:imageNode.mimeType?.text());
                res.url = sourceUrl
                for(Contributor con : getContributors(imageNode, true)) {
                    res.addToContributors(con);
                }
                for(Contributor con : getAttributions(imageNode, true)) {
                    res.addToAttributors(con);
                }
                for(License l : getLicenses(imageNode, true)) {
                    res.addToLicenses(l);
                }
            } else {
                log.debug "Updating resource metadata"
                res.url = sourceUrl
                res.description = imageNode.caption?.text();
                res.licenses?.clear()
                res.contributors?.clear()
                res.attributors?.clear();
                for(Contributor con : getContributors(imageNode, true)) {
                    res.addToContributors(con);
                }
                for(Contributor con : getAttributions(imageNode, true)) {
                    res.addToAttributors(con);
                }
                for(License l : getLicenses(imageNode, true)) {
                    res.addToLicenses(l);
                }
            }

            //s.addToResources(res);
            imageNode.appendNode("resource", res);
            log.debug "Successfully created resource";
            return res;
        } else {
            log.error "File not found : "+tempFile?.absolutePath;
        }
    }

    /**
     * imageNode has image metadata and absolute path for the file
     * @param imageNode
     * @return
     */
    File getImageFile(Node imageNode) {
        String fileName = imageNode?.fileName?.text()?.trim();
        String sourceUrl = imageNode.source?.text();
        if(!fileName && !sourceUrl) return;

        File tempFile;
        if(!fileName) {
            //downloading from web
            def tempdir = new File(config.speciesPortal.images.uploadDir, "images");
            if(!tempdir.exists()) {
                tempdir.mkdir();
            }
            try {
                tempFile = HttpUtils.download(sourceUrl, tempdir, false);
            } catch (FileNotFoundException e) {
                log.error e.getMessage();
            }
        } else {
            tempFile = new File(fileName);
            if(!tempFile.exists()) {
                tempFile = new File(fileName+".jpg");
                if(!tempFile.exists()) {
                    tempFile = new File(fileName+".png");
                }
            }
        }
        return tempFile;
    }

    //	private Resource createIcon(Node iconNode) {
    //		String fileName = iconNode.text()?.trim();
    //		log.debug "Creating icon : "+fileName;
    //
    //		def l = getLicenseByType(LicenseType.CC_PUBLIC_DOMAIN, false);
    //		def res = new Resource(type : ResourceType.ICON, fileName:fileName);
    //		res.addToLicenses(l);
    //		if(!res.save(flush:true)) {
    //			res.errors.each { log.error it }
    //		}
    //		iconNode.appendNode("resource", res);
    //		return res;
    //	}

    private Resource createAudio(Node audioNode) {
        String fileName = imageNode.get("fileName");

        //		def fieldCriteria = Resource.createCriteria();
        //		def res = fieldCriteria.get {
        //			and {
        //				eq("fileName", fileName);
        //				eq("type", ResourceType.AUDIO);
        //			}
        //		}
        //		if(!res) {
        def attributors = getAttributions(audioNode, true);
        def res = new Resource(type : ResourceType.AUDIO, fileName:audioNode.get("fileName"), description:audioNode.get("caption"), license:getLicenses(audioNode, true), contributor:getContributors(audioNode, true));
        res.url = audioNode.get("source")
        for(Contributor con : attributors) {
            res.addToAttributors(con);
        }
        //		}
        audioNode.appendNode("resource", res);
        return res;
    }

    private Resource createVideo(Node videoNode) {
        log.debug "Creating video from data $videoNode"
        def sourceUrl = videoNode.source?.text() ? videoNode.source?.text() : "";

        if(!sourceUrl) return;

        def res = Resource.findByUrlAndType(sourceUrl, ResourceType.VIDEO);

        if(!res) {
            def attributors = getAttributions(videoNode, true);
            res = new Resource(type : ResourceType.VIDEO, fileName:videoNode.get("fileName")?.text(), description:videoNode.get("caption"), license:getLicenses(videoNode, true), contributor:getContributors(videoNode, true));
            res.url = sourceUrl;
            for(Contributor con : getContributors(videoNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(videoNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(videoNode, true)) {
                res.addToLicenses(l);
            }
        } else {
            res.fileName = videoNode.get("fileName")?.text(); 
            res.url = sourceUrl
            res.description = videoNode.caption?.text();
            res.licenses?.clear()
            res.contributors?.clear()
            res.attributors?.clear();
            for(Contributor con : getContributors(videoNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(videoNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(videoNode, true)) {
                res.addToLicenses(l);
            }

        }

        //s.addToResources(res);
        videoNode.appendNode("resource", res);
        log.debug "Successfully created resource";
        return res;
    }

    private List<Resource> getResources(Node dataNode, Node imagesNode, Node iconsNode, Node audiosNode, Node videosNode) {
        List<Resource> resources = new ArrayList<Resource>();
        List<Resource> res =  getImages(dataNode.images?.image, imagesNode);
        if(res) resources.addAll(res);
        res =  getImages(dataNode.icons?.icon, iconsNode);
        if(res) resources.addAll(res);

        log.debug "Getting resources for dataNode : "+resources;
        return resources;
    }

    private List<Resource> getImages(NodeList imagesRefNode, Node imagesNode) {
        List<Resource> resources = new ArrayList<Resource>();

        if(!imagesRefNode || !imagesNode) return resources;
        imagesRefNode.each {
            String fileName = it?.text()?.trim();

            def imageNode = imagesNode.image.find { it?.refKey?.text()?.trim() == fileName };

            if(imageNode) {
                def res;
                if(imageNode.resource && imageNode.resource[0].value()) {
                    res = imageNode.resource[0].value();
                }

                if(res) {
                    resources.add(res);
                } else {
                    log.error "IMAGE NOT FOUND : "+imageNode
                }

            }
        }
        return resources;
    }

    //	private List<Resource> getIcons(Node dataNode, Node iconsNode) {
    //		List<Resource> resources = new ArrayList<Resource>();
    //
    //		if(!dataNode) return resources;
    //
    //		dataNode.icons.icon.each {
    //			String fileName = it.text();
    //			def fieldCriteria = Resource.createCriteria();
    //			def res = fieldCriteria.get {
    //				and {
    //					eq("fileName", fileName);
    //					eq("type", ResourceType.ICON);
    //				}
    //			}
    //
    //			if(!res) {
    //				log.debug "Creating icon : "+fileName
    //				res = new Resource(type : ResourceType.ICON, fileName:fileName);
    //				def l = getLicenseByType(LicenseType.CC_PUBLIC_DOMAIN, false);
    //				res.addToLicenses(l);
    //				if(!res.save(flush:true)) {
    //					res.errors.each { log.error it }
    //				}
    //			}
    //
    //			resources.add(res);
    //			s.addToResources(res);
    //		}
    //		return resources;
    //	}

    private List<Resource> getAudio(Node dataNode, Node audiosNode) {
        List<Resource> resources = new ArrayList<Resource>();

        if(!dataNode || !audiosNode) return resources;

        dataNode.audios.audio.each {
            String fileName = it.text();
            def resNode = audiosNode.audio.find { it.fileName == fileName };

            if(resNode) {
                def res;
                if(resNode[0].id)
                    res = Resource.get(resNode[0].id);

                if(res)
                    resources.add(res);
                else {
                    log.error "AUDIO NOT FOUND : "+it
                }
            }
        }

        return resources;
    }

    /**
     * 
     * @param dataNode
     * @param videosNode
     * @return
     */
    private List<Resource> getVideo(Node dataNode, Node videosNode) {
        List<Resource> resources = new ArrayList<Resource>();

        if(!dataNode || !videosNode) return resources;

        dataNode.videos.video.each {
            String fileName = it.text();
            def resNode = videosNode.video.find { it.fileName == fileName };

            if(resNode) {
                def res;
                if(resNode[0].id)
                    res = Resource.get(resNode[0].id);

                if(res)
                    resources.add(res);
                else {
                    log.error "VIDEO NOT FOUND : "+it
                }
            }
        }

        return resources;
    }

    /**
     * 
     * @param dataNode
     * @param createNew
     * @return
     */
    private List<Reference> getReferences(Node dataNode, boolean createNew, TaxonomyDefinition taxon, List<Synonyms> synonyms) {
        List<Reference> references = new ArrayList<Reference>();

        NodeList refs = dataNode.reference;
        refs.each {
            String title = cleanData(it?.title?.text().trim(), taxon, synonyms);
            String url = it?.url?.text().trim();
            if(title || url) {
                def ref = new Reference(title:title, url:url);
                references.add(ref);
            }
        }

        return references;
    }

    /**
     * 
     * @param dataNode
     * @param createNew
     * @return
     */
    private List<Contributor> getAttributions(Node dataNode, boolean createNew) {
        List<Contributor> contributors = new ArrayList<Contributor>();
        dataNode.attribution.each {
            def contributor = getContributorByName(it?.text(), createNew)
            if(contributor)
                contributors.add(contributor);
        }
        return contributors;
    }

    /**
     * 
     * @param fieldNode
     * @return
     */
    private List<CommonNames> createCommonNames(Node fieldNode, TaxonomyDefinition taxonConcept) {
        log.debug "Creating common names";
        List<CommonNames> commonNames = new ArrayList<CommonNames>();
        //List<SpeciesField> sfields = createSpeciesFields(fieldNode, CommonNames.class, null, null, null, null,null);
        fieldNode.data.eachWithIndex { n, index ->
            Language lang = getLanguage(n.language?.name?.text(), n.language?.threeLetterCode?.text());

            def criteria = CommonNames.createCriteria();
            String cleanName = Utils.cleanName(n.text().trim()).capitalize();
            CommonNames sfield = criteria.get {
                lang ? eq("language", lang): isNull("language");
                ilike("name", cleanName);
                eq("taxonConcept", taxonConcept);
            }

            if(!sfield) {
                log.debug "Saving common name : "+n.text();
                sfield = new CommonNames();
                sfield.name = cleanName;
                sfield.taxonConcept = taxonConcept;
                if(lang)
                    sfield.language = lang;
                else {
                    log.warn "NOT A SUPPORTED LANGUAGE: " + n.language;
                }
                if(!sfield.save(flush:true)) {
                    sfield.errors.each { log.error it }
                }
            }
            commonNames.add(sfield);
        }
        return commonNames;
    }

    /**
     * 
     * @param name
     * @param threeLetterCode
     * @return
     */
    private Language getLanguage(String name, String threeLetterCode) {
        if(!name) return null;

        name = Utils.cleanName(name.trim());
        threeLetterCode = threeLetterCode.toLowerCase();
        def langCriteria = Language.createCriteria();
        def langs = langCriteria.list {
            if(name) ilike("name", name);
            if(threeLetterCode) eq("threeLetterCode", threeLetterCode);
        }

        if(langs && langs[0]) {
            return langs[0]
        } 

        if(threeLetterCode) {
            return Language.findByThreeLetterCode(threeLetterCode);
        }

        if(name) {
            def lang = Language.findByNameIlike(name);
            if(!lang && name.size() == 3) {
                return Language.findByThreeLetterCode(name.toLowerCase());
            } 
        }

        log.debug "Creating new language ${name} and ${threeLetterCode}"		
        Language lang = Language.getLanguage(name);	
        return lang;
    }

    /**
     * 
     * @param fieldNode
     * @return
     */
    private List<Synonyms> createSynonyms(Node fieldNode, TaxonomyDefinition taxonConcept) {
        log.debug "Creating synonyms";
        List<Synonyms> synonyms = new ArrayList<Synonyms>();
        //List<SpeciesField> sfields = createSpeciesFields(fieldNode, Synonyms.class, null, null, null, null,null);
        fieldNode.data.eachWithIndex { n, index ->
            RelationShip rel = getRelationship(n.relationship?.text());
            if(rel) {
                def cleanName = Utils.cleanName(n.text()?.trim());
                def parsedNames = namesParser.parse([cleanName]);
                def sfield = saveSynonym(parsedNames[0], rel, taxonConcept);
                if(sfield) {
                    synonyms.add(sfield);
                }
            } else {
                log.warn "NOT A SUPPORTED RELATIONSHIP: "+n.relationship?.text();
            }
        }
        return synonyms;
    }

    private Synonyms saveSynonym(TaxonomyDefinition parsedName, RelationShip rel, TaxonomyDefinition taxonConcept) {

        if(parsedName && parsedName.canonicalForm) {
            //TODO: IMP equality of given name with the one in db should include synonyms of taxonconcepts
            //i.e., parsedName.canonicalForm == taxonomyDefinition.canonicalForm or Synonym.canonicalForm
            def criteria = Synonyms.createCriteria();
            Synonyms sfield = criteria.get {
                ilike("canonicalForm", parsedName.canonicalForm);
                eq("relationship", rel);
                eq("taxonConcept", taxonConcept);
            }
            if(!sfield) {
                log.debug "Saving synonym : "+parsedName.name;
                sfield = new Synonyms();
                sfield.name = parsedName.name;
                sfield.relationship = rel;
                sfield.taxonConcept = taxonConcept;

                sfield.canonicalForm = parsedName.canonicalForm;
                sfield.normalizedForm = parsedName.normalizedForm;;
                sfield.italicisedForm = parsedName.italicisedForm;;
                sfield.binomialForm = parsedName.binomialForm;;

                if(!sfield.save(flush:true)) {
                    sfield.errors.each { log.error it }
                }
            }
            return sfield;
        } else {
            log.error "Ignoring synonym taxon entry as the name is not parsed : "+parsedName.name
        }

    }

    /**
     * 
     * @param rel
     * @return
     */
    private RelationShip getRelationship(String rel) {
        if(rel) {
            for(RelationShip type : RelationShip) {
                if(type.value().equals(rel)) {
                    return type;
                }
            }
        }
        return RelationShip.SYNONYM;
    }

    /**
     * 
     * @param fieldNode
     * @return
     */
    private List<GeographicEntity> getCountryGeoEntity(Species s, Node fieldNode) {
        List<GeographicEntity> geographicEntities = new ArrayList<GeographicEntity>();
        List<SpeciesField> sfields = createSpeciesFields(s, fieldNode, GeographicEntity.class, null, null, null, null, null);
        fieldNode.data.eachWithIndex { c, index ->
            if(c?.country) {
                def countryCriteria = Country.createCriteria();
                def countries = countryCriteria.list {
                    if(c?.country?.twoLetterCode?.text()) ilike("twoLetterCode", c.country.twoLetterCode?.text().trim());
                    //if(c?.country?.threeLetterCode?.text()) ilike("threeLetterCode", c.country.threeLetterCode.text().trim());
                    //if(c?.country?.threeDigitCode?.text()) ilike("threeDigitCode", Integer.parseInt(c.threeDigitCode.text().trim()));
                    ilike("countryName", c?.country?.name?.text());
                }

                Country country = countries?countries[0]:null;
                if(country) {
                    def sfield = sfields.get(index);
                    if(sfield) {
                        sfield.description = "";
                        sfield.country = country;
                        geographicEntities.add(sfield);
                    }
                } else {
                    log.warn "NOT A SUPPORTED COUNTRY: "+c?.country?.name?.text();
                }
            } else {
                log.error " NO COUNTRY IS SPECIFIED in $c"
            }
        }
        return geographicEntities;
    }

    /**
     * Creating the given classification entries hierarchy.
     * Saves any new taxondefinition found 
     */
    private List<TaxonomyRegistry> getClassifications(List speciesNodes, String scientificName, boolean saveHierarchy = true) {
        def classifications = Classification.list();
        def taxonHierarchies = new ArrayList();
        classifications.each {
            List taxonNodes = getNodesFromCategory(speciesNodes, it.name);
            def t = getTaxonHierarchy(taxonNodes, it, scientificName, saveHierarchy);
            if(t) {
                cleanUpGorm();
                taxonHierarchies.addAll(t);
            }
        }
        return taxonHierarchies;
    }

    private List<Node> getNodesFromCategory(List speciesNodes, String category) {
        def result = new ArrayList();
        for(Node fieldNode : speciesNodes) {
            if(fieldNode.name().equals("field")) {
                String cat = fieldNode.category?.text()?.trim().toLowerCase();
                if(cat && cat.equalsIgnoreCase(category)) {
                    result.add(fieldNode);
                }
            }
        }
        return result;
    }

    /**
     * 
     * @param fieldNodes
     * @param classification
     * @param scientificName
     * @return
     */
    private List<TaxonomyRegistry> getTaxonHierarchy(List fieldNodes, Classification classification, String scientificName, boolean saveTaxonHierarchy=true) {
        log.debug "Getting classification hierarchy : "+classification.name;

        List<TaxonomyRegistry> taxonEntities = new ArrayList<TaxonomyRegistry>();

        List<String> names = new ArrayList<String>();
        List<TaxonomyDefinition> parsedNames;
        fieldNodes.each { fieldNode ->
            String name = getData(fieldNode.data);
            int rank = getTaxonRank(fieldNode?.subcategory?.text());
            if(classification.name.equalsIgnoreCase(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY) && rank == TaxonomyRank.SPECIES.ordinal()) {
                def cleanSciName = cleanSciName(scientificName);
                name = cleanSciName
            } else {
                name = cleanSciName(name);
            }
            if(name) {
                names.add(name);
            }
        }
        parsedNames = namesParser.parse(names);

        int i=0;
        fieldNodes.each { fieldNode ->
            log.debug "Adding taxonomy registry from node: "+fieldNode;
            int rank = getTaxonRank(fieldNode?.subcategory?.text());
            String name = getData(fieldNode.data);

            log.debug "Taxon : "+name+" and rank : "+rank;
            if(name && rank >= 0) {
                //TODO:HACK to populate sciName in species level of taxon hierarchy
                //				if(classification.name.equalsIgnoreCase(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)) {// && rank == TaxonomyRank.SPECIES.ordinal()) {
                //					def cleanSciName = cleanSciName(scientificName);
                //					name = cleanSciName
                //				}

                def parsedName = parsedNames.get(i++);
                log.debug "Parsed name ${parsedName?.canonicalForm}"
                if(parsedName?.canonicalForm) {
                    //TODO: IMP equality of given name with the one in db should include synonyms of taxonconcepts
                    //i.e., parsedName.canonicalForm == taxonomyDefinition.canonicalForm or Synonym.canonicalForm
                    def taxonCriteria = TaxonomyDefinition.createCriteria();
                    TaxonomyDefinition taxon = taxonCriteria.get {
                        eq("rank", rank);
                        ilike("canonicalForm", parsedName.canonicalForm);
                    }

                    if(!taxon && saveTaxonHierarchy) {
                        log.debug "Saving taxon definition"
                        taxon = parsedName;
                        taxon.rank = rank;
                        if(!taxon.save()) {
                            taxon.errors.each { log.error it }
                        }
                    } else if(taxon.name != parsedName.name) {
                        saveSynonym(parsedName, getRelationship(null), taxon);
                    }


                    def ent = new TaxonomyRegistry();
                    ent.taxonDefinition = taxon
                    ent.classification = classification;
                    ent.parentTaxon = getParentTaxon(taxonEntities, rank);
                    log.debug("Parent Taxon : "+ent.parentTaxon)
                    ent.path = (ent.parentTaxon ? ent.parentTaxon.path+"_":"") + taxon.id;
                    //same taxon at same parent and same path may exist from same classification.
                    def criteria = TaxonomyRegistry.createCriteria()
                    TaxonomyRegistry registry = criteria.get {
                        eq("taxonDefinition", ent.taxonDefinition);
                        eq("path", ent.path);
                        eq("classification", ent.classification);
                    }

                    if(registry) {
                        log.debug "Taxon registry already exists : "+registry;
                        taxonEntities.add(registry);
                    } else if(saveTaxonHierarchy) {
                        log.debug "Saving taxon registry entity : "+ent;
                        if(!ent.save()) {
                            ent.errors.each { log.error it }
                        } else {
                            log.debug "Saved taxon registry entity : "+ent;
                        }
                        taxonEntities.add(ent);
                    }


                } else {
                    log.error "Ignoring taxon entry as the name is not parsed : "+parsedName
                }
            }
        }
        //		if(classification.name.equalsIgnoreCase(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)) {
        //			updateSpeciesGroup(taxonEntities);
        //		}
        return taxonEntities;
    }

    /**
     * 
     * @param rankStr
     * @return
     */
    static int getTaxonRank(String rankStr) {
        for(TaxonomyRank type : TaxonomyRank) {
            if(type.value().equalsIgnoreCase(rankStr)) {
                return type.ordinal();
            }
        }
        return -1;
    }

    /**
     * //ASSUMING THE TAXON REGISTRY ENTRIES ARE INSERTED IN RANK ORDER
     * @param s
     * @param category
     * @param subCategory
     * @param rank
     * @return
     */
    private TaxonomyRegistry getParentTaxon(List taxonEntities, int rank) {
        def parentTaxon;
        def fs = taxonEntities.each { f ->
            def temp
            if(f.taxonDefinition.rank < rank) {
                temp = f;
                if(!parentTaxon) parentTaxon = temp;
                if(temp && temp.taxonDefinition.rank > parentTaxon.taxonDefinition.rank && temp.taxonDefinition.rank < rank) {
                    parentTaxon = temp;
                }
            }
        }
        return parentTaxon;
    }

    TaxonomyDefinition getTaxonConcept(List taxonomyRegistry) {
        def taxonConcept = getTaxonConcept(taxonomyRegistry, Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY));
        if(!taxonConcept) {
            taxonConcept = getTaxonConcept(taxonomyRegistry, Classification.findByName(fieldsConfig.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY));
        }
        if(!taxonConcept) {
            taxonConcept = getTaxonConcept(taxonomyRegistry, Classification.findByName(fieldsConfig.GBIF_TAXONOMIC_HIERARCHY));
        }
        if(!taxonConcept) {
            taxonConcept = getTaxonConcept(taxonomyRegistry, Classification.findByName(fieldsConfig.IUCN_TAXONOMIC_HIERARCHY));
        }
        return taxonConcept;
    }
    /**
     * 
     */
    TaxonomyDefinition getTaxonConcept(List taxonomyRegistry, Classification classification) {
        def taxonConcept, max = 0;

        taxonomyRegistry.each { tReg ->
            if(tReg.classification == classification && tReg.taxonDefinition.rank >= max) {
                taxonConcept = tReg.taxonDefinition;
                max = taxonConcept.rank;
            }
        }
        return taxonConcept;
    }

    /**
     * 
     * @param sciName
     * @param s
     * @return
     */
    TaxonomyDefinition getTaxonConceptFromName(String sciName) {
        def cleanSciName = cleanSciName(sciName);

        if(cleanSciName) {
            List name = namesParser.parse([cleanSciName])
            if(name[0].normalizedForm) {
                def taxonCriteria = TaxonomyDefinition.createCriteria();
                TaxonomyDefinition taxon = taxonCriteria.get {
                    eq("rank", TaxonomyRank.SPECIES.ordinal());
                    ilike("canonicalForm", name[0].canonicalForm);
                }

                if(!taxon) {
                    taxon = name[0];
                    taxon.rank = TaxonomyRank.SPECIES.ordinal();
                    if(!taxon.save(flush:true)) {
                        taxon.errors.each { log.error it }
                    }
                }
                return taxon;
            } else {
                return null;
            }
        }
    }

    /**
     * 
     * @param s
     * @return
     */
    static Species findDuplicateSpecies(s) {
        def species = Species.findByGuid(s.guid);
        //		if(!species.isAttached()) {
        //			species.attach();
        //		}
        return species
    }

    /**
     * 
     * @param existingSpecies
     * @param newSpecies
     */
    void mergeSpecies(Species existingSpecies, Species newSpecies) {
        //				newSpecies.fields.each { field ->
        //					existingSpecies.addToFields(field);
        //				}
        //				newSpecies.synonyms.each { field ->
        //					existingSpecies.addToSynonyms(field);
        //				}
        //				newSpecies.commonNames.each { field ->
        //					existingSpecies.addToFields(field);
        //				}
        //				newfields,
        //				synonyms, commonNames, globalDistributionEntities, globalEndemicityEntities,
        //				taxonomyRegistry;

    }

    private String cleanSciName(String scientificName) {
        def cleanSciName = Utils.cleanName(scientificName);
        if(cleanSciName =~ /s\.\s*str\./) {
            cleanSciName = cleanSciName.replaceFirst(/s\.\s*str\./, cleanSciName.split()[0]);
        }

        if(cleanSciName.indexOf(' ') == -1) {
            cleanSciName = cleanSciName.toLowerCase().capitalize();
        }
        return cleanSciName;
    }

    /**
     * updating species group for the taxon entries
     * ASSUMING TAXONENTITIES ARE IN HIERARCHY ORDER
     * @param taxonEntities
     * @return
     */
    private updateSpeciesGroup(List<TaxonomyRegistry> taxonEntities) {
        def ctx = ApplicationHolder.getApplication().getMainContext();
        groupHandlerService = ctx.getBean("groupHandlerService");

        taxonEntities.each { taxonReg ->
            if(!taxonReg.taxonDefinition.group) {
                //TODO: optimize... getGroup is refetching all saved parent taxons in all hierarchies
                groupHandlerService.updateGroup(taxonReg.taxonDefinition)
                log.debug "Updating species group for taxon ${taxonReg.taxonDefinition.name} as ${taxonReg.taxonDefinition.group?.name}"
            }
        }
    }

    /**
     *
     */
    private void cleanUpGorm() {
        def ctx = ApplicationHolder.getApplication().getMainContext();
        SessionFactory sessionFactory = ctx.getBean("sessionFactory")
        def hibSession = sessionFactory?.getCurrentSession()
        if(hibSession) {
            log.debug "Flushing and clearing session"
            try {
                hibSession.flush()
            } catch(ConstraintViolationException e) {
                e.printStackTrace()
            }
            //		   hibSession.clear()
        }
    }


    void setLogAppender(FileAppender fa) {
        if(fa) {
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }
}
