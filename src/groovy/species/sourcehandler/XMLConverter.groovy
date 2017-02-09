package species.sourcehandler

import groovy.sql.Sql
import groovy.util.Node;

import java.util.List

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.FetchMode;

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
import species.SynonymsMerged
import species.Language
import species.TaxonomyDefinition
import species.TaxonomyRegistry
import species.License.LicenseType
import species.Resource.ResourceType
import species.SpeciesField.AudienceType
import species.ScientificName
import species.ScientificName.RelationShip
import species.ScientificName.TaxonomyRank
import species.groups.SpeciesGroup;
import species.utils.HttpUtils
import species.utils.ImageUtils
import species.utils.Utils
import species.auth.SUser
import species.NamesMetadata;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.NamePosition;
import species.namelist.NameInfo
import species.participation.NamelistService;

import org.apache.log4j.Logger; 
import org.apache.log4j.FileAppender;
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.context.MessageSource
import org.springframework.web.servlet.support.RequestContextUtils as RCU;

//import org.grails.plugins.sanitizer.MarkupSanitizerResult


class XMLConverter extends SourceConverter {


    //protected static SourceConverter instance;
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def fieldsConfig = config.speciesPortal.fields
    NamesParser namesParser;
    String resourcesRootDir = config.speciesPortal.resources.rootDir;

    private Species s;

    def groupHandlerService;
 	
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
	
	
	
	protected populateIBPCOLMatch(Node species, Map speciesMap, Map hirMap, Map synMap){
		if(!speciesMap || !hirMap )
			return 
		
		removeInvalidNode(species);
		
		Node node = getNodeFromSubCategory(species, fieldsConfig.SCIENTIFIC_NAME);
		String sName = getData((node && node.data)?node.data[0]:null);
		
		def index = species.attribute('rowIndex')
		
		//updating species node here for ibp and col match
		String k = index + KEY_SEP + sName + KEY_SEP + sName
		updateNode(node, speciesMap, k)
		
		//updating taxon hir here for ibp and col match
		List taxonNodes = getNodesFromCategory(species.children(), fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
		
		taxonNodes.each { tn ->
			if(tn && tn.data){
				def tmpHirName = getData(tn.data[0])
				if(tmpHirName){
					k = index + KEY_SEP + sName + KEY_SEP + getData((tn && tn.data)?tn.data[0]:null)
					String rankSuffix = KEY_SEP + tn.subcategory.text()?.trim().toLowerCase();
					updateNode(tn, hirMap, k + rankSuffix)
				}
			}
		}
		
		//updating synonyms here for ibp and col match
		if(synMap){
			taxonNodes = getNodesFromCategory(species.children(), "synonyms");
			taxonNodes.each { tn ->
				if(tn && tn.data){
					tn.data.each { s ->
						def synName = getData(s)
						if(synName){
							k = index + KEY_SEP + sName + KEY_SEP + synName
							updateNode(s, synMap, k)
						}
					}
				}
			}
		}
	}
	
	
	private updateNode(Node node, Map completeMap, String k){
		def m = completeMap.get(k)
		
		new Node(node, "matchStatus", m['status']);
		new Node(node, "rank", m['rank']);
				
		String targetPosition = m['target position']
		if(targetPosition){
			new Node(node, "position", targetPosition);
		}
		
		String targetStatus = m['target status']
		
		if(targetStatus){
			new Node(node, "status", targetStatus);
		}
		
		
		String matchSource = m['match found']?.trim()?.toLowerCase()
		if("new".equals(matchSource) || "ignore".equals(matchSource)){
			new Node(node, "nameRunningStatus", matchSource);
			return
		}
		
		String id = m?.id
		if(!id)
			return
		
		if("ibp".equalsIgnoreCase(matchSource))
			new Node(node, "ibpId", id);
		else if("col".equalsIgnoreCase(matchSource))	
			new Node(node, "colId", id);
		
	}
	
	
	public NameInfo populateNameDetail(Node species, int index){
		removeInvalidNode(species);
		
		String speciesName = getNodeDataFromSubCategory(species, fieldsConfig.SCIENTIFIC_NAME);
		int rank = getTaxonRank(getNodeDataFromSubCategory(species, fieldsConfig.RANK));
		
		NameInfo n = new NameInfo(speciesName, rank, index)
		//getting hir
		List taxonNodes = getNodesFromCategory(species.children(), fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
		//println "====== taxon =====>>>>>>>>>>======= " + taxonNodes
		taxonNodes.each { tn ->
			rank = getTaxonRank(tn.subcategory.text())
			def hirNodeData = getData((tn && tn.data)?tn.data[0]:null)
			if(hirNodeData){
				def tmp = new NameInfo(hirNodeData, rank, index)
				tmp.sourceName = speciesName
				//println "--- hir " + tmp
				n.addToHir(tmp)
			}
		}
		
		taxonNodes = getNodesFromCategory(species.children(), "synonyms");
		//println "=======synonyms=========== " + taxonNodes
		taxonNodes.each { tn ->
			if(tn && tn.data){
				tn.data.each { s ->
					def synName = getData(s)
					if(synName){
						def tmp = new NameInfo(synName,index)
						tmp.sourceName = speciesName
						//println "--- syn " + tmp
						n.addToSyn(tmp)
					}
				}
			}
		}
		
		//println "  final node  " + n
		return n
	}
	
	private String getNodeDataFromSubCategory(species, subCat){
		Node node =  getNodeFromSubCategory(species, subCat)
		return getData((node && node.data)?node.data[0]:null);
	}

	private Node getNodeFromSubCategory(species, subCat){
		Language language
		Node node = species.field.find {
			language = it.language[0]?.value();
			it.subcategory.text().equalsIgnoreCase(getFieldFromName(subCat, 3, language));
		}
		return node
	}
	
	private boolean addScNameNode(Node species, Node nameNode, String rank){
		String nameRunningStatusText = getData(nameNode.nameRunningStatus)
		
		//println "------------------- name node " + nameNode
		println "--------------------rank ---------- " + rank
		
		if("ignore".equals(nameRunningStatusText)){
			//println "Name is in ignore status so not doing any thing  " + nameNode
			return false
		}	
		
		Node taxonLastNode = getNodeFromSubCategory(species, rank);
		
		if(taxonLastNode){
			Node parentNode = taxonLastNode.parent()
			parentNode.remove(taxonLastNode)
		}
		
		
		String targetClassName = getTargetClassificationName(species)
		
		//creating new node having all info as scientific name
		Node field = new Node(species, "field");
		Node concept = new Node(field, "concept", fieldsConfig.NOMENCLATURE_AND_CLASSIFICATION);
		Node category = new Node(field, "category", targetClassName);
		Node subcategory = new Node(field, "subcategory", rank);
		
		field.append(nameNode.language)
		field.append(nameNode.data)
		
		field.append(nameNode.matchStatus)
		
		if(nameNode.ibpId){
			field.append(nameNode.ibpId)
		}
		
		if(nameNode.colId){
			field.append(nameNode.colId)
		}
		
		if(nameNode.nameRunningStatus){
			field.append(nameNode.nameRunningStatus)
		}
		
		if(nameNode.position){
			field.append(nameNode.position)
		}
		if(nameNode.status){
			field.append(nameNode.status)
		}
		
		return true
	}
	
	
	private String getTargetClassificationName(Node species){
		String res
		Classification.list().each {
			if(res){
				return
			}
			
			List taxonNodes = getNodesFromCategory(species.children(), it.name);
			if(taxonNodes){
				res = it.name
			}
		}
		
		return res //fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY
	}
	
    public Species convertSpecies(Node species) {
        //TODO default action to be merge
        convertSpecies(species, SaveAction.MERGE);
    }

    public Species convertSpecies(Node species, SaveAction defaultSaveAction) {
        if(!species) return null;
        try {
            log.info "Creating/Updating species"
            s = new Species();
			
			TaxonomyDefinition taxonConcept = convertName(species)
			//sciName is must for the species to be populated
			if(taxonConcept){
				Language language;
            	
				s.taxonConcept = taxonConcept
				s.title = s.taxonConcept.italicisedForm;
                //taxonconcept is being used as guid
                s.guid = constructGUID(s);
                //a species page with guid as taxon concept is considered as duplicate
                Species existingSpecies = findDuplicateSpecies(s);

                //either overwrite or merge if an existing species exists
                if(existingSpecies) {
                    if(defaultSaveAction == SaveAction.OVERWRITE || existingSpecies.percentOfInfo == 0){
                        log.info "Cleraring old version of species : "+existingSpecies.id;
                        try {
                            existingSpecies.clearBasicContent()
                            s = existingSpecies;
                        }
                        catch(org.springframework.dao.DataIntegrityViolationException e) {
                            e.printStackTrace();
                            log.error "Could not clear species ${existingSpecies.id} : "+e.getMessage();
                            addToSummary("Could not clear species ${existingSpecies.id} : "+e.getMessage())
                            addToSummary(e);
                            return;
                        }
                    } else if(defaultSaveAction == SaveAction.MERGE){
                        log.info "Merging with already existing species information : "+existingSpecies.id;
                        //mergeSpecies(existingSpecies, s);
                        s = existingSpecies;
						//XXX: not removing resources so if same spreadsheet uploaded multiple times will see duplicate images
                        //s.resources?.clear();
                    } else {
                        log.warn "Ignoring species as a duplicate is already present : "+existingSpecies.id;
                        addToSummary("Ignoring species as a duplicate is already present : "+existingSpecies.id)
                        return;
                    }
                }
				List<Resource> resources = createMedia(species, s.taxonConcept.canonicalForm);
                log.debug "Resources ${resources}"
                resources.each { 
                    it.saveResourceContext(s)
                    s.addToResources(it); 
                }

                for(Node fieldNode : species.children()) {
                    if(fieldNode.name().equals("field")) {
                        if(!isValidField(fieldNode)) {
                            log.warn "NOT A VALID FIELD : "+fieldNode;
                            addToSummary("NOT A VALID FIELD : "+fieldNode)
                            continue;
                        }

                        String concept = fieldNode.concept?.text()?.trim();
                        String category = fieldNode.category?.text()?.trim();
                        String subcategory = fieldNode.subcategory?.text()?.trim();

                        language = fieldNode.language[0].value();

                        if(category && category.equalsIgnoreCase(getFieldFromName(fieldsConfig.COMMON_NAME, 2, language))) {
							//log.debug "Cammon name added at the time of name update" 
                        } else if(category && category.equalsIgnoreCase(getFieldFromName(fieldsConfig.SYNONYMS, 2, language))) {
							//log.debug "Synonym added at the time of name update"	
                        }
                        else if(subcategory && subcategory.equalsIgnoreCase(getFieldFromName(fieldsConfig.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY, 3, language))) {
                            List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                            countryGeoEntities.each {
                                if(it.species == null) {
                                    s.addToGlobalDistributionEntities(it);
                                }
                            }
                        } else if(subcategory && subcategory.equalsIgnoreCase(getFieldFromName(fieldsConfig.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY, 3, language))) {
                            List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                            countryGeoEntities.each {
                                if(it.species == null) {
                                    s.addToGlobalEndemicityEntities(it);
                                }
                            }
                        }  else if(subcategory && subcategory.equalsIgnoreCase(getFieldFromName(fieldsConfig.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY, 3, language))) {
                            List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                            countryGeoEntities.each {
                                if(it.species == null) {
                                    s.addToIndianDistributionEntities(it);
                                }
                            }
                        } else if(subcategory && subcategory.equalsIgnoreCase(getFieldFromName(fieldsConfig.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY, 3, language))) {
                            List<GeographicEntity> countryGeoEntities = getCountryGeoEntity(s, fieldNode);
                            countryGeoEntities.each {
                                if(it.species == null) {
                                    s.addToIndianEndemicityEntities(it);
                                }
                            }

                        } else if(category && ( category.toLowerCase().endsWith(fieldsConfig.TAXONOMIC_HIERARCHY.toLowerCase()) ||  category.toLowerCase().startsWith("Hiérarchie Taxonomique".toLowerCase()))) {
                            //log.debug "Added Hirerachy at the time of name update" 
                        } else {
                            List<SpeciesField> speciesFields = createSpeciesFields(s, fieldNode, SpeciesField.class, species.images[0], species.icons[0], species.audio[0], species.video[0], s.taxonConcept.fetchSynonyms());
                            speciesFields.each {
                                if(it.species == null) { // if its already associated this field will be populated
                                    log.debug "Adding new fields to species ${s}"
                                    s.addToFields(it);
                                }
                            }
                        }
                    }
                }
			}
			return s 
        } catch(Exception e) {
            log.error "ERROR CONVERTING SPECIES : "+e.getMessage();
            e.printStackTrace();
            addToSummary(e);
        }
    }

	
	public TaxonomyDefinition convertName(Node species) {
		if(!species) return null;
		
		try {
			StringBuilder sb = new StringBuilder()
			individualNameSumm = ""
			log.info "Creating/Updating names"
			//Thread.dumpStack()
			removeInvalidNode(species);

			Language language;
			//sciName is must for the species to be populated
			Node speciesNameNode = species.field.find {
				language = it.language[0].value();
				it.subcategory.text().equalsIgnoreCase(getFieldFromName(fieldsConfig.SCIENTIFIC_NAME, 3, language));
			}

			
			//XXX: sending just the first element need to decide on this if list has multiple elements
			def speciesName = getData((speciesNameNode && speciesNameNode.data)?speciesNameNode.data[0]:null);
			int rank = getTaxonRank(getNodeDataFromSubCategory(species, fieldsConfig.RANK));
			String rankStr = TaxonomyRank.getTRFromInt(rank).value()
			addToSummary("<<< NAME >>> "  + speciesName + "  <<< Rank >>> " + rank)
			
			String nameRunningStatusText = getData(speciesNameNode.nameRunningStatus)?.trim()
			
			String nameMatchStatus = getData(speciesNameNode.matchStatus)
			String targetStatus = getData(speciesNameNode.status)
			targetStatus = targetStatus ?: nameMatchStatus

			
			if("synonym".equalsIgnoreCase(targetStatus)){
				return addNameAsSynonym(speciesNameNode)
			}
			
			//adding scientific name as last node in author contribute hir so that author year and other info picked from scientific name column
			if(speciesName && addScNameNode(species, speciesNameNode, rankStr)){
				
				//getting classification hierarchies and saving these taxon definitions
				List<TaxonomyRegistry> taxonHierarchy = getClassifications(species.children(), speciesName, true).taxonRegistry;

				//taxonConcept is being taken from taxonomy hierarchy
				TaxonomyDefinition taxonConcept = getTaxonConcept(taxonHierarchy);

				// if the author contributed taxonomy hierarchy is not specified
				// then the taxonConept is null and sciName of species is saved as concept and is used to create the page
				taxonConcept = taxonConcept ?: getTaxonConceptFromName(speciesName, rank, true, speciesNameNode);
				
				List tmpTaxonList = []
				if(taxonConcept) {
					//adding taxonomy classifications
					TaxonomyRegistry latestHir
					taxonHierarchy.each { th ->
						//th = th.merge()
						th.save();
						if(th.taxonDefinition == taxonConcept){
							latestHir = th
						}else{
							th.taxonDefinition.updateNameSignature(getUserContributors(speciesNameNode.data))
						}
						
						tmpTaxonList.add(th.taxonDefinition)
					}
					
					//println " latest hir -------convertname------------ <<<<<<<<<>>>>>>>>>>>>>>> " + latestHir
					//taxonConcept.updateNameStatus(targetStatus)
					taxonConcept.updatePosition(speciesNameNode?.position?.text(), getNameSourceInfo(species), latestHir)
					updateUserPrefForColCuration(taxonConcept, speciesNameNode)
					taxonConcept.postProcess()
					taxonConcept.updateNameSignature(getUserContributors(speciesNameNode.data))
					
					List<SynonymsMerged> synonyms = [];
					List<CommonNames> commNames = []
					
					for(Node fieldNode : species.children()) {
						if(fieldNode.name().equals("field")) {
							if(!isValidField(fieldNode)) {
								log.warn "NOT A VALID FIELD : "+fieldNode;
								addToSummary("NOT A VALID FIELD : "+fieldNode)
								continue;
							}

							String concept = fieldNode.concept?.text()?.trim();
							String category = fieldNode.category?.text()?.trim();
							String subcategory = fieldNode.subcategory?.text()?.trim();

							language = fieldNode.language[0].value();

							if(category && category.equalsIgnoreCase(getFieldFromName(fieldsConfig.SYNONYMS, 2, language))) {
								synonyms = createSynonyms(fieldNode, taxonConcept);
								synonyms.each {taxonConcept.addSynonym(it); }
							}else if (category && category.equalsIgnoreCase(getFieldFromName(fieldsConfig.COMMON_NAME, 2, language))) {
								commNames = createCommonNames(fieldNode, taxonConcept);
							}  
						}
					}
					
					//writing name summary
					def s = taxonConcept
					sb.append(s.name + "|" + s.id+ "|" + s.status + "|" + s.position + "|" + s.rank + "|" + s.matchId)
					sb.append("|" + tmpTaxonList.collect { it.id + ":" + it.name }.join(">"))
					sb.append("|" + synonyms.collect { it.name }.join("#"))
					sb.append("|" + commNames.collect { it.name }.join("#"))
					individualNameSumm = sb.toString()
					
					addToSummary("TaxonConcept id  >>>   " + taxonConcept.id)
					return taxonConcept;
				} else {
					log.error "TaxonConcept is not found"
					addToSummary("TaxonConcept is not found")
				}
			} else {
				log.error "IGNORING SPECIES AS SCIENTIFIC NAME WAS NOT FOUND OR IN IGNORED STATUS: "+speciesName;
				addToSummary("IGNORING SPECIES AS SCIENTIFIC NAME WAS NOT FOUND OR IN IGNORED STATUS: "+speciesName)
			}
		} catch(Exception e) {
			log.error "ERROR CONVERTING Name : "+e.getMessage();
			e.printStackTrace();
			addToSummary(e);
		}
	}
	
	private void updateUserPrefForColCuration(TaxonomyDefinition taxonConcept, Node nameNode){
		String nameRunningStatusText = getData(nameNode.nameRunningStatus)?.trim()
		if("new".equals(nameRunningStatusText)){
			println "Name is forcefully created as new so setting flag to avoid col curation  " //+ nameNode
			taxonConcept.doColCuration = false
		}
	}
	
	private TaxonomyDefinition addNameAsSynonym(Node nameNode){
		String nameRunningStatusText = getData(nameNode.nameRunningStatus)
		
		if("ignore".equals(nameRunningStatusText)){
			println "Name is in ignore status so not doing any thing  " + nameNode
			return null
		}
		
		
		if(nameNode.ibpId){
			println "Ibp id is given for this synonym so returning the same " + nameNode
			return SynonymsMerged.read(Long.parseLong(nameNode.ibpId.text()))
		}
		
		if(!nameNode.colId){
			println "Col Id is not given for this synonym so ignoring it  " + nameNode
			return null
		}
		try {
			def colId = nameNode.colId.text()
			return ApplicationHolder.getApplication().getMainContext().getBean("namelistService").createNameFromColId(colId)
		}catch(e){
			e.printStackTrace()
		}
	}
	
	private Map getNameSourceInfo(Node species){
		Map res = [:]
		res.put(fieldsConfig.NAME_SOURCE, getNodeDataFromSubCategory(species, fieldsConfig.NAME_SOURCE))
		res.put(fieldsConfig.VIA_SOURCE, getNodeDataFromSubCategory(species, fieldsConfig.VIA_SOURCE))
		res.put(fieldsConfig.NAME_SOURCE_ID, getNodeDataFromSubCategory(species,fieldsConfig.NAME_SOURCE_ID))
		return  res 
	}
	
	private String fetchSpeciesName(Node species){
		def language
		Node speciesNameNode = species.field.find {
			language = it.language[0].value();
			it.subcategory.text().equalsIgnoreCase(getFieldFromName(fieldsConfig.SCIENTIFIC_NAME, 3, language));
		}

		def speciesName = getData((speciesNameNode && speciesNameNode.data)?speciesNameNode.data[0]:null);
		return speciesName
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
                    addToSummary("NOT A VALID FIELD. IGNORING : "+fieldNode)
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
    */
    public SpeciesField createSpeciesField(Species s, Field field, String text, List<String> contributors, List<String> attributions, List<String> licenses, List<String> audiences, List<String> status) {
        Node fieldNode = createFieldNode(field);
        createDataNode(fieldNode, text, contributors, attributions, licenses, audiences, status); 

        return createSpeciesFields(s, fieldNode, SpeciesField.class, null, null, null, null, []) [0]
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
    private List<SpeciesField> createSpeciesFields(Species s, Node fieldNode, Class sFieldClass, Node imagesNode, Node iconsNode, Node audiosNode, Node videosNode, List synonyms) {
        //log.debug "Creating species field from node : =========== "+fieldNode;
        List<SpeciesField> speciesFields = new ArrayList<SpeciesField>();
        Field field = getField(fieldNode, false);
        if(field == null) {
            log.warn "NO SUCH FIELD : "+field?.name;
            addToSummary("NO SUCH FIELD : "+field?.name)
            return;
        }
      
        List sFields = [];
		if(s.isAttached() && field) {
             sFields = SpeciesField.withCriteria() {
                eq("field", field)
                eq('species', s)
                fetchMode 'references', FetchMode.JOIN
                fetchMode 'contributors', FetchMode.JOIN
                fetchMode 'licenses', FetchMode.JOIN
                fetchMode 'audienceTypes', FetchMode.JOIN
                fetchMode 'resources', FetchMode.JOIN
                fetchMode 'attributors', FetchMode.JOIN
            }
        }
        Language language = field.language;

        for(Node dataNode : fieldNode.data) {
            String data = getData(dataNode);
            data = cleanData(data, s.taxonConcept, synonyms);
            List<SUser> contributors = getUserContributors(dataNode);
            List<License> licenses = getLicenses(dataNode, false);
            List<AudienceType> audienceTypes = getAudienceTypes(dataNode, true);
            List<Resource> resources = getResources(dataNode, imagesNode, iconsNode, audiosNode, videosNode);
            List<Reference> references = getReferences(dataNode, true,s.taxonConcept, synonyms);
            List<Contributor> attributors = getAttributions(dataNode, true);
            SpeciesField speciesField;

            def temp = []
			
			String dt = data.replaceAll("\n","<br />");
            //TODO:HACK just for keystone
            dt = data.replaceAll("</?p>","");
            for (sField in sFields) {
                if(isDuplicateSpeciesField(sField, contributors, attributors,  dt)) {
                    log.debug "Found already existing species fields for field ${field} ${sField}"
                    temp << sField;
                }
            }

            log.debug "Found duplicate fields ${temp}"

            if(dataNode.action?.text()?.trim() == "merge") {
                for(sField in temp) {

                    //HACK for deleting keystone fields with different content and contributor 
                    /*if(dt.equals(sField.description.replaceAll("</?p>",""))) {
                      println "Deleting same description field"
                      s.removeFromFields(sField);
                      sField.delete();

                      } else*/ if(sField.description.contains(dt)){
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
                log.debug "Adding new field to species ${s} ===  " + "  field " + field + "  data " + data
                //XXX giving default uploader now. At the time of actual save updating this with logged in user.
                if(field.connection != 81){
                	speciesField = sFieldClass.newInstance(field:field, description:data);
                }else{
                	speciesField =  sFieldClass.newInstance(field:field, description:"dummy");                
                }

            } else {
                log.debug "Overwriting existing ${speciesField}. Removing all metadata associate with previous field."
                speciesField.description = data;
                //TODO: Will have to clean up orphaned entries from following tables
                speciesField.contributors?.clear()
                speciesField.licenses?.clear()
                speciesField.audienceTypes?.clear()
                speciesField.attributors?.clear()
                speciesField.resources?.clear()
                if(field.connection != 81 ){ // TODO: No need for Reference fields
                	speciesField.references?.clear()
                }
            }
            
            if(speciesField && contributors) {
                contributors.each {speciesField.addToContributors(it); }
                licenses.each { speciesField.addToLicenses(it); }
                audienceTypes.each { speciesField.addToAudienceTypes(it); }
                attributors.each {  speciesField.addToAttributors(it); }
                resources.each {  it.saveResourceContext(speciesField); speciesField.addToResources(it); }
                if(field.connection == 81 ){
					def ref = new Reference(title:data);
                    speciesField.addToReferences(ref);                	
            	}else{
					references.each { speciesField.addToReferences(it); }            		
            	}
            
                speciesField.language = language;
                speciesFields.add(speciesField);
            } else {
                log.error "IGNORING SPECIES FIELD AS THERE ARE NO CONTRIBUTORS FOR SPECIESFIELD ${speciesField}"
                addToSummary("IGNORING SPECIES FIELD AS THERE ARE NO CONTRIBUTORS FOR SPECIESFIELD ${speciesField}")
            }           
        }

        return speciesFields;
    } 

    private String getData(NodeList dataNodes) {
        //log.info "It should be one node only but got list of nodes " + dataNodes
        if(!dataNodes) return "";
        return getData(dataNodes[0]);
    }
    
    private String getData(Node dataNode) {
        if(!dataNode) return "";
        //sanitize the html text
        return dataNode.text()?:"";
    }

    /**
    * A field is duplicate if contributor and attributor are exactly same.
    * 
    **/
    private boolean isDuplicateSpeciesField(SpeciesField sField, contributors, attributors, data) {
		boolean a =  (new HashSet(sField.contributors).equals(new HashSet(contributors)))
		boolean c = (new HashSet(sField.attributors.collect{it.id}).equals(new HashSet(attributors.collect {it.id})))
		return  (a && c)
    }

    private String cleanData(String text, TaxonomyDefinition taxon, List synonyms) {
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
        //  log.error result.errorMessages
        //  return ''
        //}
    }

    /**
     * 
     * @param fieldNode
     * @param createNew
     * @return
     */
    private Field getField(Node fieldNode, boolean createNew) {
        if(fieldNode.fieldInstance) {
            return fieldNode.fieldInstance[0].value();
        }

        String concept = fieldNode.concept?.text()?.trim();
        String category = fieldNode.category?.text()?.trim();
        String subCategory = fieldNode.subcategory?.text()?.trim();
        Language language = fieldNode.language[0]?.value();
        def fieldCriteria = Field.createCriteria();

        Field field = fieldCriteria.get {
            and {
                ilike("concept", concept);
                category ? ilike("category", category) : isNull("category");
                subCategory ? ilike("subCategory", subCategory) : isNull("subCategory");
                eq("language", language);
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
                addToSummary("NOT A VALID CONTIBUTOR : "+contributorName)
            }
        }
        return contributors;
    }
    
    private List<SUser> getUserContributors(NodeList dataNodes ) {
        //log.info "It should be one node only but got list of nodes " + dataNodes
        return getUserContributors(dataNodes[0])
    }
    
    private List<SUser> getUserContributors(Node dataNode) {
        List<SUser> contributors = new ArrayList<SUser>();
        dataNode.contributor.each {
            String contributorEmail = it.text()?.trim();
            SUser contributor = SUser.findByEmail(contributorEmail)
            if(contributor) {
                contributors.add(contributor);
            } else {
                log.warn "NOT A VALID CONTIBUTOR : "+contributorEmail;
                addToSummary("NOT A VALID CONTIBUTOR : "+contributorEmail)
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
    public static Contributor getContributorByName(String contributorName, boolean createNew) {
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
                addToSummary("NOT A SUPPORTED LICENSE TYPE: "+licenseType)
            }
        }

        if(!licenses) {
            licenses.add(getLicenseByType(LicenseType.UNSPECIFIED, createNew));
        }

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

        LicenseType type = License.fetchLicenseType(licenseType)

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
                addToSummary("NOT A SUPPORTED AUDIENCE TYPE: "+audienceType)
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
            resources.addAll(createResourceByType(audiosNode[0], ResourceType.AUDIO, relResFolder));
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
                        //TODO done because each image in bulk upload goes to separate folder.
                        if(resourcesRootDir == config.speciesPortal.usersResource.rootDir){
                            def relFolder = it.fileName?.getAt(0)?.text()?.replace(resourcesRootDir.toString(), "")?:""
                            relResFolder = new File(relFolder).getParent();
                        }
						
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
                    //resourceNode?.audio.each { if(!it?.id) resources.add(createAudio(it)); }
                resourceNode?.audio.each {
                    if(!it?.id) {
                        def resource = createImage(it, relResFolder, ResourceType.AUDIO);
                        if(resource) {
                            resources.add(resource);
                        }
                    }
                }
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
        println ";;;;;;;;;;;;;;;::::"
        println imageNode
        File tempFile = getImageFile(imageNode, relImagesFolder);
        def sourceUrl = imageNode.source?.text() ? imageNode.source?.text() : "";
        def absUrl = imageNode.url?.text() ? imageNode.url?.text() : "";
        def rate = imageNode.rating?.text() ? imageNode.rating?.text() : "";
        
        log.debug "Creating image resource : ${tempFile}  or from absUrl : ${absUrl}";
        if((tempFile && tempFile.exists()) || absUrl) {
            
            Resource res;
            String path;

			if(tempFile && tempFile.exists()) {
                //copying file
                relImagesFolder = relImagesFolder.trim();
                File root = new File(resourcesRootDir , relImagesFolder);
                if(!root.exists() && !root.mkdirs()) {
                    log.error "COULD NOT CREATE DIR FOR SPECIES : "+root.getAbsolutePath();
                    addToSummary("COULD NOT CREATE DIR FOR SPECIES : "+root.getAbsolutePath())
                }
                log.debug "in dir : "+root.absolutePath;
                File imageFile = new File(root, Utils.cleanFileName(tempFile.getName()));
                if(!imageFile.exists()) {
                    try {
                        Utils.copy(tempFile, imageFile);
                        if( resourceType.toString() == "IMAGE"){
                            ImageUtils.createScaledImages(imageFile, imageFile.getParentFile());
                        }  
                    } catch(FileNotFoundException e) {
                        log.error "File not found : "+tempFile.absolutePath;
                        addToSummary("File not found : "+tempFile.absolutePath)
                    }
                }
                path = imageFile.absolutePath.replace(resourcesRootDir, "");
                res = Resource.findByFileNameAndType(path, resourceType);
            } else if(absUrl) {
                path = 'u';
                res = Resource.findByUrlAndType(absUrl, resourceType);
            }

            if(!res) {
                log.debug "Creating new resource"
                res = new Resource(type : resourceType, fileName:path, description:imageNode.caption?.text(), mimeType:imageNode.mimeType?.text(),language:imageNode.language[0]?.value());
                res.url = absUrl?:sourceUrl
                if(rate) res.rating = Integer.parseInt(rate);
                for(Contributor con : getContributors(imageNode, true)) {
                    println con
                    res.addToContributors(con);
                }
                println "-----------------------"
                for(Contributor con : getAttributions(imageNode, true)) {
                    res.addToAttributors(con);
                }
                for(License l : getLicenses(imageNode, true)) {
                    res.license = l;
                }
                if(imageNode.annotations?.text()) {
                    res.annotations = imageNode.annotations?.text()
                }
            } else {
                log.debug "Updating resource metadata"
                res.url = absUrl ?: sourceUrl
                if(rate) res.rating = Integer.parseInt(rate);
                res.description = imageNode.caption?.text();
                res.language    = imageNode.language[0]?.value();
                res.license = null;//?.clear()
                res.contributors?.clear()
                res.attributors?.clear();
                println "-----------------------"
                for(Contributor  con : getContributors(imageNode, true)) {
                    println con
                    res.addToContributors(con);
                }
                for(Contributor con : getAttributions(imageNode, true)) {
                    res.addToAttributors(con);
                }
                for(License l : getLicenses(imageNode, true)) {
                    println "=====LICENSE on EXISTING RES!!!======== " + l + "===RES== " + res
                    res.license = l
                }
                if(imageNode.annotations?.text()) {
                    println "###################################SAVING Annotations"
                    res.annotations = imageNode.annotations?.text()
                }

                //res.merge();
                //res.refresh();

                //removing flush:true as in bulk upload this flush is causing observation version to change. We shd flush when parent object is saved
            }
            if(!res.save(/*flush:true*/)){
                res.errors.allErrors.each { println it;log.error it }
            }
            //res.refresh();

            //s.addToResources(res);
            imageNode.appendNode("resource", res);
            log.debug "Successfully created resource " + res;
            return res;
        } else {
            log.error "File not found : "+tempFile?.absolutePath;
            addToSummary("File not found : "+tempFile?.absolutePath)
        }
    }

    /**
     * imageNode has image metadata and absolute path for the file
     * @param imageNode
     * @return
     */
    File getImageFile(Node imageNode, String imagesDir="") {
        String fileName = imageNode?.fileName?.text()?.trim();
        String sourceUrl = imageNode.source?.text();
        if(!fileName && !sourceUrl) return;

        File tempFile;
        if(fileName) {
            tempFile = new File(fileName);
            if(!tempFile.exists()) {
                tempFile = new File(fileName+".jpg");
                if(!tempFile.exists()) {
                    tempFile = new File(fileName+".png");
                }
            }
        } 
        if(!tempFile.exists()) {
            if(sourceUrl) {
                //downloading from web
                def tempdir = new File(config.speciesPortal.content.rootDir+File.separator+"species"+File.separator+imagesDir?:'', "images");
                if(!tempdir.exists()) {
                    tempdir.mkdirs();
                }
                try {
                    tempFile = HttpUtils.download(sourceUrl, tempdir, false);
                } catch (Exception e) {
                    log.error e.getMessage();
                    tempFile = null;
                }
            } else {
                tempFile = null;
            }
        }
        return tempFile;
    }

    //  private Resource createIcon(Node iconNode) {
    //      String fileName = iconNode.text()?.trim();
    //      log.debug "Creating icon : "+fileName;
    //
    //      def l = getLicenseByType(LicenseType.CC_PUBLIC_DOMAIN, false);
    //      def res = new Resource(type : ResourceType.ICON, fileName:fileName);
    //      res.addToLicenses(l);
    //      if(!res.save(flush:true)) {
    //          res.errors.each { log.error it }
    //      }
    //      iconNode.appendNode("resource", res);
    //      return res;
    //  }

    private Resource createAudio(Node audioNode) {
         log.debug "Creating video from data $audioNode"
        def sourceUrl = audioNode.source?.text() ? audioNode.source?.text() : "";
        def rate = audioNode.rating?.text() ? audioNode.rating?.text() : "";

        if(!sourceUrl) return;

        def res = Resource.findByUrlAndType(sourceUrl, ResourceType.AUDIO);

        if(!res) {
            
            def attributors = getAttributions(audioNode, true);
            String description = (audioNode.caption?.text()) ? (audioNode.caption?.text()) : "";
            res = new Resource(type : ResourceType.AUDIO, fileName:audioNode.get("fileName")?.text(), description:description, license:getLicenses(audioNode, true), contributor:getContributors(audioNode, true));
            res.url = sourceUrl;
            if(rate) res.rating = Integer.parseInt(rate);

            for(Contributor con : getContributors(audioNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(audioNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(audioNode, true)) {
                res.addToLicenses(l);
            }
        } else {
            res.fileName = audioNode.get("fileName")?.text(); 
            res.url = sourceUrl
            if(rate) res.rating = Integer.parseInt(rate);
            res.description = (audioNode.caption?.text()) ? (audioNode.caption?.text()) : "";
            res.license = null//?.clear()
            res.contributors?.clear()
            res.attributors?.clear();
            for(Contributor con : getContributors(audioNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(audioNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(audioNode, true)) {
                res.addToLicenses(l);
            }

        }

        //s.addToResources(res);
        audioNode.appendNode("resource", res);
        log.debug "Successfully created resource";
        return res;
    }

    private Resource createVideo(Node videoNode) {
        log.debug "Creating video from data $videoNode"
        def sourceUrl = videoNode.source?.text() ? videoNode.source?.text() : "";
        def rate = videoNode.rating?.text() ? videoNode.rating?.text() : "";

        if(!sourceUrl) return;

        def res = Resource.findByUrlAndType(sourceUrl, ResourceType.VIDEO);

        if(!res) {
            
            def attributors = getAttributions(videoNode, true);
            String description = (videoNode.caption?.text()) ? (videoNode.caption?.text()) : "";
            res = new Resource(type : ResourceType.VIDEO, fileName:videoNode.get("fileName")?.text(), description:description, contributor:getContributors(videoNode, true),language:videoNode.language[0]?.value());
            res.url = sourceUrl;
            if(rate) res.rating = Integer.parseInt(rate);

            for(Contributor con : getContributors(videoNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(videoNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(videoNode, true)) {
                res.license = l;
            }
        } else {
            res.fileName = videoNode.get("fileName")?.text(); 
            res.url = sourceUrl
            if(rate) res.rating = Integer.parseInt(rate);
            res.description = (videoNode.caption?.text()) ? (videoNode.caption?.text()) : "";
            res.language    = videoNode.language[0]?.value()

            res.license = null;
            res.contributors?.clear()
            res.attributors?.clear();
            for(Contributor con : getContributors(videoNode, true)) {
                res.addToContributors(con);
            }
            for(Contributor con : getAttributions(videoNode, true)) {
                res.addToAttributors(con);
            }
            for(License l : getLicenses(videoNode, true)) {
                res.license = l;
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
            myPrint("========== image node  " + imageNode);

            if(imageNode) {
                def res;
                if(imageNode.resource && imageNode.resource[0].value()) {
                    res = imageNode.resource[0].value();
                }

                if(res) {
                    resources.add(res);
                } else {
                    log.error "IMAGE NOT FOUND : "+imageNode
                    addToSummary("IMAGE NOT FOUND : "+imageNode)
                }

            } else {
                File tempFile = getImageFile(it);
                //if(tempFile) {
                    //check if the fileName is a physical file on disk and create a resource from it
                    def resource = createImage(it, s.taxonConcept.canonicalForm, ResourceType.IMAGE);
                    if(resource) {
                        resources.add(resource)
                    }
                /*if(tempFile) {
                    //just for logging
                } else {
                    log.error "COULD NOT FIND REFERENCE TO THE IMAGE ${fileName}"
                    addToSummary("COULD NOT FIND REFERENCE TO THE IMAGE ${fileName}")
                }*/
            }
        }
        return resources;
    }

    //  private List<Resource> getIcons(Node dataNode, Node iconsNode) {
    //      List<Resource> resources = new ArrayList<Resource>();
    //
    //      if(!dataNode) return resources;
    //
    //      dataNode.icons.icon.each {
    //          String fileName = it.text();
    //          def fieldCriteria = Resource.createCriteria();
    //          def res = fieldCriteria.get {
    //              and {
    //                  eq("fileName", fileName);
    //                  eq("type", ResourceType.ICON);
    //              }
    //          }
    //
    //          if(!res) {
    //              log.debug "Creating icon : "+fileName
    //              res = new Resource(type : ResourceType.ICON, fileName:fileName);
    //              def l = getLicenseByType(LicenseType.CC_PUBLIC_DOMAIN, false);
    //              res.addToLicenses(l);
    //              if(!res.save(flush:true)) {
    //                  res.errors.each { log.error it }
    //              }
    //          }
    //
    //          resources.add(res);
    //          s.addToResources(res);
    //      }
    //      return resources;
    //  }

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
    private List<Reference> getReferences(Node dataNode, boolean createNew, TaxonomyDefinition taxon, List synonyms) {
    	
        List<Reference> references = new ArrayList<Reference>();

        NodeList refs = dataNode.reference;
        if(refs) {
            println "Adding references from dataNode : ${refs}"
            refs.each {
                String title = cleanData(it?.title?.text().trim(), taxon, synonyms);
                String url = it?.url?.text().trim();
                if(title || url) {
                    def ref = new Reference(title:title, url:url);
                    references.add(ref);
                }
            }

            log.debug "Got ${references.size()} references for ${taxon.name}"
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
				eq('isDeleted', false)
            }

            if(!sfield) {
                log.debug "Saving common name :"+n.text();
                sfield = new CommonNames();
                sfield.name = cleanName;
                sfield.taxonConcept = taxonConcept;
                sfield.status = NameStatus.COMMON;
                sfield.viaDatasource = n.viaDatasource?.text() 
                if(lang)
                    sfield.language = lang;
                else {
                    log.warn "NOT A SUPPORTED LANGUAGE: " + n.language;
                    addToSummary("NOT A SUPPORTED LANGUAGE: " + n.language)
                }
                
                if(!sfield.save(flush:true)) {
                    sfield.errors.each { log.error it }
                }
            }
            
            //adding contributors to common name
            sfield.updateContributors(getUserContributors(n))
            
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
    List<SynonymsMerged> createSynonyms(Node fieldNode, TaxonomyDefinition taxonConcept) {
        log.debug "Creating synonyms";
		def taxonContributors = taxonConcept.contributors
        List<SynonymsMerged> synonyms = new ArrayList<SynonymsMerged>();
        //List<SpeciesField> sfields = createSpeciesFields(fieldNode, Synonyms.class, null, null, null, null,null);
        fieldNode.data.eachWithIndex { n, index ->
            RelationShip rel = getRelationship(n.relationship?.text());
            if(rel) {
				try{
	                def cleanName = Utils.cleanName(n.text()?.trim());
	                def parsedNames = namesParser.parse([cleanName]);
	                def viaDatasource = null;
	                if(n.viaDatasource) {
	                    //println "=======SOURCE HAI == == " + n.viaDatasource.text();
	                    viaDatasource = n.viaDatasource.text();
	                }
	                def sfield = saveSynonym(parsedNames[0], rel, taxonConcept, viaDatasource, n, taxonContributors);
	                if(sfield) {
	                    //adding contributors
						//println "--------------------- contribtors to be added for " + sfield + "  contr " + getUserContributors(n)
	                    sfield.updateContributors(getUserContributors(n))
	                    synonyms.add(sfield);
	                }
				}catch(Exception e){
					log.error  e.printStackTrace()
				}
            } else {
                log.warn "NOT A SUPPORTED RELATIONSHIP: "+n.relationship?.text();
                addToSummary("NOT A SUPPORTED RELATIONSHIP: "+n.relationship?.text())
            }
        }
        return synonyms;
    }

    private SynonymsMerged saveSynonym(TaxonomyDefinition parsedName, RelationShip rel, TaxonomyDefinition taxonConcept, viaDatasource, Node dataNode, List taxonContributors) {

        SynonymsMerged sfield = null;
		if(parsedName && parsedName.canonicalForm) {
			List res = searchIBP(parsedName, -1, false, true, dataNode)
	        def taxon = res ? res[0] : null   
	        if(!taxon) {
	            log.debug "Saving synonym : "+parsedName.name;
	            sfield = new SynonymsMerged();
	            sfield.name = parsedName.name;
	            sfield.relationship = rel;
	            //sfield.taxonConcept = taxonConcept;
	            sfield.rank = taxonConcept.rank;
	            sfield.canonicalForm = parsedName.canonicalForm;
	            sfield.normalizedForm = parsedName.normalizedForm;;
	            sfield.italicisedForm = parsedName.italicisedForm;;
	            sfield.binomialForm = parsedName.binomialForm;;
	            sfield.status = NameStatus.SYNONYM
	            if(viaDatasource){
	                sfield.viaDatasource = viaDatasource
	            }
	            sfield.uploadTime = new Date();
				sfield.matchDatabaseName = taxonConcept.matchDatabaseName
				
				
//				taxonContributors = taxonContributors ?: taxonConcept.contributors
//				taxonContributors.each { userContributor ->
//					sfield.addToContributors(userContributor)
//				}
	            if(!sfield.save(flush:true)) {
	                sfield.errors.each { log.error it }
	            }
	        } else {
	            println "Looking at existing name : ${taxon}"
	            if(taxon.status == NameStatus.ACCEPTED) {
					//if existing name is acceted then ignoring XXX need to be reported to user
	                //sfield = ApplicationHolder.getApplication().getMainContext().getBean("namelistService").changeAcceptedToSynonym(taxon, [acceptedNamesList:[['taxonConcept':taxonConcept]]]);
					log.error "Ignoring synonym taxon entry as the name existing name is ACCEPTED : "+parsedName.name
					addToSummary("Ignoring synonym taxon entry as the name existing name is ACCEPTED : "+parsedName.name)
					return
	            } else {
	                sfield = taxon as SynonymsMerged;
	            }
	        }
			
			if(dataNode){
				sfield.updatePosition(dataNode?.position?.text())
			}
            //println "======== Synonym ============= " + sfield
            return sfield;
        } else {
            log.error "Ignoring synonym taxon entry as the name is not parsed : "+parsedName.name
            addToSummary("Ignoring synonym taxon entry as the name is not parsed : "+parsedName.name)
        }

    }

    /**
     * 
     * @param rel
     * @return
     */
    public static RelationShip getRelationship(String rel) {
        if(rel) {
            for(RelationShip type : RelationShip) {
                if(type.value().equalsIgnoreCase(rel)) {
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
                    addToSummary("NOT A SUPPORTED COUNTRY: "+c?.country?.name?.text())
                }
            } else {
                log.error " NO COUNTRY IS SPECIFIED in $c"
                addToSummary(" NO COUNTRY IS SPECIFIED in $c")
            }
        }
        return geographicEntities;
    }

    /**
     * Creating the given classification entries hierarchy.
     * Saves any new taxondefinition found 
     */
     def getClassifications(List speciesNodes, String scientificName, boolean saveHierarchy = true, boolean abortOnNewName = false, boolean fromCOL = false, otherParams= null) {
        //log.debug "Getting classifications for ${scientificName}"
        def classifications = Classification.list();
        List<TaxonomyRegistry> taxonHierarchies = new ArrayList<TaxonomyRegistry>();
        String spellCheckMsg = ''
        classifications.each {
            List taxonNodes = getNodesFromCategory(speciesNodes, it.name);
            if(!taxonNodes){
            	return
            }
            def getTaxonHierarchyRes = getTaxonHierarchy(taxonNodes, it, scientificName, saveHierarchy, abortOnNewName, fromCOL ,otherParams)
            def t = getTaxonHierarchyRes.taxonRegistry;
            spellCheckMsg = getTaxonHierarchyRes.spellCheckMsg;
            if(t) {
            	//cleanUpGorm();
                taxonHierarchies.addAll(t);
            }
        }
		return ['taxonRegistry':taxonHierarchies, 'spellCheckMsg':spellCheckMsg];
    }

    private List<Node> getNodesFromCategory(List speciesNodes, String category) {
        def result = new ArrayList();
        for(Node fieldNode : speciesNodes) {
            if(fieldNode.name().equals("field")) {
                String cat = fieldNode.category?.text()?.trim().toLowerCase();
                Language language = fieldNode.language[0].value();
                if(cat && (cat.equalsIgnoreCase(category) || cat.equalsIgnoreCase(getFieldFromName(category,2,language))) ) {
                    result.add(fieldNode);
                }
            }
        }
        return result;
    }

    List<TaxonomyRegistry> getTaxonHierarchyOld(List fieldNodes, Classification classification, String scientificName, boolean saveTaxonHierarchy=true) {
        //log.debug "Getting classification hierarchy : "+classification.name;

        List<TaxonomyRegistry> taxonEntities = new ArrayList<TaxonomyRegistry>();

        List<String> names = new ArrayList<String>();
        List<TaxonomyDefinition> parsedNames;
        List<TaxonomyDefinition> sortedFieldNodes = new ArrayList<TaxonomyDefinition>();;

        fieldNodes.each { fieldNode ->
            String name = getData(fieldNode.data);
            int rank = getTaxonRank(fieldNode?.subcategory?.text());
            Language language = fieldNode.language[0].value();
            //if(classification.name.equalsIgnoreCase(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY) && rank == TaxonomyRank.SPECIES.ordinal()) {
            //    def cleanSciName = Utils.cleanSciName(scientificName);
            //    name = cleanSciName
            //} else 
            if(name) {
                name = Utils.cleanSciName(name);
            }
            if(name) {
                names.putAt(rank, name);
            }
            sortedFieldNodes.putAt(rank, fieldNode)
        }
        parsedNames = namesParser.parse(names);
        fieldNodes = sortedFieldNodes;

        int i=0;
        fieldNodes.each { fieldNode ->
            if(fieldNode) {
                log.debug "Adding taxonomy registry from node: "+fieldNode;
                int rank = getTaxonRank(fieldNode?.subcategory?.text());
                String name = getData(fieldNode.data);

                log.debug "Taxon : "+name+" and rank : "+rank;
                if(name && rank >= 0) {
                    //TODO:HACK to populate sciName in species level of taxon hierarchy
                    //              if(classification.name.equalsIgnoreCase(getFieldFromName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY))) {// && rank == TaxonomyRank.SPECIES.ordinal() {
                    //                  def cleanSciName = cleanSciName(scientificName);
                    //                  name = cleanSciName
                    //              }

                    def parsedName = parsedNames.get(i++);
                    //log.debug "Parsed name ${parsedName?.canonicalForm}"
                    if(parsedName?.canonicalForm) {
                        //TODO: IMP equality of given name with the one in db should include synonyms of taxonconcepts
                        //i.e., parsedName.canonicalForm == taxonomyDefinition.canonicalForm or Synonym.canonicalForm
                        def taxonCriteria = TaxonomyDefinition.createCriteria();
                        TaxonomyDefinition taxon = taxonCriteria.get {
                            eq("rank", rank);
                            ilike("canonicalForm", parsedName.canonicalForm);
							eq('isDeleted', false)
                        }

                        if(!taxon && saveTaxonHierarchy) {
                            log.debug "Saving taxon definition"
                            taxon = parsedName;
                            taxon.rank = rank;
                            if(!taxon.save()) {
                                taxon.errors.each { log.error it }
                            }

                            taxon.updateContributors(getUserContributors(fieldNode.data))
                        } else if(saveTaxonHierarchy && taxon && parsedName && taxon.name != parsedName.name) {
                            /*def synonym = saveSynonym(parsedName, getRelationship(null), taxon);
                            if(synonym)
                                synonym.updateContributors(getUserContributors(fieldNode.data))
                            */
                        }

                        def ent = new TaxonomyRegistry();
                        ent.taxonDefinition = taxon
                        ent.classification = classification;
                        ent.parentTaxon = getParentTaxon(taxonEntities, rank);
                        ent.parentTaxonDefinition = ent.parentTaxon.taxonDefinition;
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
                            //log.debug "Taxon registry already exists : "+registry;
                            if(saveTaxonHierarchy)
                                registry.updateContributors(getUserContributors(fieldNode.data))
                                taxonEntities.add(registry);
                        } else if(saveTaxonHierarchy) {
                            //log.debug "Saving taxon registry entity : "+ent;
                            if(!ent.save()) {
                                ent.errors.each { log.error it }
                            } else {
                                log.debug "Saved taxon registry entity : "+ent;
                            }
                            ent.updateContributors(getUserContributors(fieldNode.data))
                            taxonEntities.add(ent);
                        }

                    } else {
                        log.error "Ignoring taxon entry as the name is not parsed : "+parsedName
                        addToSummary("Ignoring taxon entry as the name is not parsed : "+parsedName)
                    }
                }
            }
        }
        //      if(classification.name.equalsIgnoreCase(getFieldFromName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY))) {
        //          updateSpeciesGroup(taxonEntities);
        //      }
        return taxonEntities;
    }
    
    /**
     * 
     * @param fieldNodes
     * @param classification
     * @param scientificName
     * @return
     */
    def getTaxonHierarchy(List fieldNodes, Classification classification, String scientificName, boolean saveTaxonHierarchy=true ,boolean abortOnNewName=false, boolean fromCOL = false, otherParams = null) {
        //XXX: IBP hir will be added automaitcally on post process of taxon def where it checks for duplicacy as well
        //ibp hir should not be added from anywhere else
        println "Adding hir for classification " + classification?.name
            if(classification == Classification.fetchIBPClassification()){
                return
            }
        //TODO: BREAK HIERARCHY FROM UI ID RAW LIST NAME IN BETWEEN HIERARCHY
        boolean newNameSaved = false;
        List<TaxonomyRegistry> taxonEntities = new ArrayList<TaxonomyRegistry>();

        List<String> names = new ArrayList<String>();
        List<TaxonomyDefinition> parsedNames;
        List<TaxonomyDefinition> sortedFieldNodes = new ArrayList<TaxonomyDefinition>();;
        fieldNodes.each { fieldNode ->
            String name = getData(fieldNode.data);
            int rank = getTaxonRank(fieldNode?.subcategory?.text());
            Language language = fieldNode.language[0].value();
            if(name) {
                name = Utils.cleanSciName(name);
            }
            if(name) {
                //println "===NAME=== " + name + "  ===rank=== " + rank
                names.putAt(rank, name);
            }
            sortedFieldNodes.putAt(rank, fieldNode)
        }
        //println "=Input PARSING NAMES====== " + names
        parsedNames = namesParser.parse(names);
        fieldNodes = sortedFieldNodes;
        String spellCheckMsg = ''
        int i=0;
        boolean flag = true;
        fieldNodes.each { fieldNode ->
            if(flag && fieldNode){
                //log.debug "Adding taxonomy registry from node: "+fieldNode;
                int rank = getTaxonRank(fieldNode?.subcategory?.text());
                String name = getData(fieldNode.data);

                println "Taxon : "+name+" and rank : "+rank;
                if(!name || rank < 0) {
                    //return only continue the loop
                    return
                }

                def parsedName = parsedNames.get(i++);
                def authorYear = parsedName.authorYear;
                //log.debug "Parsed name ${parsedName?.canonicalForm}"
                if(parsedName?.canonicalForm) {
                    //TODO: IMP equality of given name with the one in db should include synonyms of taxonconcepts
                    //i.e., parsedName.canonicalForm == taxonomyDefinition.canonicalForm or Synonym.canonicalForm

                    //TODO: how to get status in each case?
                    boolean searchInNull = false;
                    boolean useAuthorYear = (otherParams?true:false)

                    def searchIBPResult = searchIBP(parsedName, rank, searchInNull, useAuthorYear, fieldNode)
                    TaxonomyDefinition taxon = null;

                    //if from curation
                    //search results is single - choose it
                    //if multiple -  and if last node - read that taxon from curatingTaxonId
                    //if its accepted pick that as taxon & flag it
                    //else if its synonym pick 1st result and flag that synonym
                    if(otherParams) {
                        //println "============= otherParams  " + otherParams
                        //DOING THIS BECAUSE IT DIDNT FIND NEWLY MOVED NAME FROM SYNONYM TO ACCEPTED
                        if(fieldNode == fieldNodes.last()) {
                            if(otherParams.curatingTaxonId) {
                                TaxonomyDefinition sciName = TaxonomyDefinition.get(otherParams.curatingTaxonId.toLong());
                                println "=============++++++ 1111 ${sciName.status} ............${otherParams.curatingTaxonStatus}"
                                //if(otherParams.curatingTaxonStatus == NameStatus.ACCEPTED) {
                                //couldnot use contains()
                                boolean isPresent = false;
                                searchIBPResult.each {
                                    if(it.id == sciName.id){
                                        isPresent = true;
                                    }
                                }
                                if(!isPresent) {
                                    searchIBPResult.add(sciName);
                                }
                                //}
                            }
                        }
                        if(searchIBPResult.size() == 1) {
                            taxon = searchIBPResult[0];
                        }
                        if(searchIBPResult.size() > 1 ){
                            if(fieldNode == fieldNodes.last()) {
                                println "Field node is last node"
                                if(otherParams.curatingTaxonId) {
                                    TaxonomyDefinition sciName = TaxonomyDefinition.get(otherParams.curatingTaxonId.toLong());
                                    println "1=============++++++ 1111 ${sciName.status} ............${otherParams.curatingTaxonStatus}"
                                    if(sciName.status == NameStatus.ACCEPTED) {
                                        println "==========sci name status  ${sciName.status} ............${otherParams.curatingTaxonStatus}"
                                        println "############==== Flagging accepted name " + sciName;
                                        taxon = sciName;
                                        taxon.isFlagged = true;
                                        String flaggingReason = "The name clashes with an existing name on the portal.IDs- ";
                                        searchIBPResult.each {
                                            flaggingReason = flaggingReason + it.id.toString() + ", ";
                                        }
                                        println "########### Flagging in XML CONVERTER becoz : ${flaggingReason}==============" + taxon
                                        taxon.flaggingReason = taxon.flaggingReason + " ### " + flaggingReason;
                                        //taxon = taxon.merge();
                                        if(!taxon.save()) {
                                            taxon.errors.each { log.error it }
                                        }
                                    } else {
                                        println "############==== Flagging synonym " + sciName;
                                        //Pick any working list name if available
                                        def workingTaxon = null
                                        def dirtyTaxon = null
                                        searchIBPResult.each {
                                            if(it.position == NamePosition.WORKING && !workingTaxon) {
                                                workingTaxon = it;
                                            }
                                            if(it.position == NamePosition.RAW && !dirtyTaxon) {
                                                dirtyTaxon = it;
                                            }
                                        }
                                        if(workingTaxon) {
                                            taxon = workingTaxon
                                        } else if(dirtyTaxon) {
                                            taxon = dirtyTaxon;
                                        } else {
                                            taxon = searchIBPResult[0];
                                        }
                                        sciName.isFlagged = true;
                                        String flaggingReason = "The accepted name for this is a system default.Multiple potential matches exist.IDs- ";
                                        searchIBPResult.each {
                                            flaggingReason = flaggingReason + it.id.toString() + ", ";
                                        }
                                        sciName.flaggingReason = sciName.flaggingReason + " ### " + flaggingReason;
                                        if(!sciName.save()) {
                                            sciName.errors.each { log.error it }
                                        }
                                    }
                                } else {
                                    println "==========No curation taxon id"
                                }
                            } else {
                                println "Field node is not last node"
                                //Pick any working list name if available
                                //then dirty
                                //then null
                                def workingTaxon = null
                                def dirtyTaxon = null
                                searchIBPResult.each {
                                    if(it.position == NamePosition.WORKING && !workingTaxon) {
                                        workingTaxon = it;
                                    }
                                    if(it.position == NamePosition.RAW && !dirtyTaxon) {
                                        dirtyTaxon = it;
                                    }
                                }
                                if(workingTaxon) {
                                    taxon = workingTaxon
                                } else if(dirtyTaxon) {
                                    taxon = dirtyTaxon;
                                } else {
                                    taxon = searchIBPResult[0];
                                }
                            }    
                        } else {
                            println "searchIBPResult.....${searchIBPResult.size()}";
                        }
                    } else {
                        if(searchIBPResult.size() > 0) {
                            taxon = searchIBPResult[0];
                        }
                    }
                    if(newNameSaved && abortOnNewName) {
                        //abort
                        println "==========ABORTING============  "
                        flag = false;
                        return;
                    }
                    if(!fromCOL && taxon && (taxon.position != NamePosition.WORKING )) {
                        //println " =========== got taxon and reusing it  " + taxon
                        //taxon = null;
                    }
                    boolean addNewNameToSession = (taxon && taxon.id)?false:true
                    if(!taxon && saveTaxonHierarchy) {
                        log.debug "Saving taxon definition"
                        taxon = parsedName;
                        taxon.rank = rank;
                        taxon.uploadTime = new Date();
                        if(fieldNode == fieldNodes.last()){
                            taxon.matchDatabaseName = otherParams?.metadata?otherParams.metadata.source:"";
                            taxon.viaDatasource = otherParams?.metadata?otherParams.metadata.via:"";
                            taxon.authorYear = otherParams?.metadata?otherParams.metadata.authorString:"";
                        }
                        if(fromCOL) {
                            println "=========COL SE REQUIRED==============="
                            taxon.matchDatabaseName = "CatalogueOfLife";
                            taxon.colNameStatus = NamesMetadata.COLNameStatus.ACCEPTED
                            //get its data from col and save
                            println "=======NAME======== " + name
                            if(otherParams.id_details && otherParams.id_details[taxon.canonicalForm+ "#" + taxon.rank]) {
                                println "========UPDATING NEW TAXON ======= " + otherParams.id_details[taxon.canonicalForm + "#" + taxon.rank] + "=======TAXON CANONICAL === " + taxon.canonicalForm + "  -- id " + taxon.id
                                taxon.matchId = otherParams.id_details[taxon.canonicalForm+ "#" + taxon.rank];
                            }
                            //println "=========EXTERNAL ID===== " + externalId
                            String nameStatus = otherParams.nameStatus?:NameStatus.ACCEPTED //?: (namelistService.searchCOL(externalId, 'id')[0]).nameStatus;
                            println "=========NAMESTATUS===== " + nameStatus
                            println "=========NAME STATUS============ " + nameStatus +"======= " + nameStatus.getClass();
                            def finalNameStatus;
                            switch(nameStatus) {
                                case ["accepted","provisionally"] :
                                finalNameStatus = NameStatus.ACCEPTED;
                                break
                                /*
                                case "provisionally":
                                finalNameStatus = NameStatus.PROV_ACCEPTED;
                                break
                                 */

                                case ["synonym", "ambiguous", "misapplied"]:
                                finalNameStatus = null  //NameStatus.SYNONYM;
                                break

                                case "common" :
                                finalNameStatus = null  //NameStatus.COMMON;
                                break

                                default:
                                finalNameStatus = null //""
                                break
                            }
                            println "=======FINAL NAME STATUS======= " + finalNameStatus;
                            taxon.status = finalNameStatus; 
                            taxon.position = NamePosition.WORKING;
                            newNameSaved = false;
                        }
                        // else search COL
                        // if single acc name take in
                        // if single synonym take its acc name and show msg, change return or this function,
                        //if multiple reject save name with null status.
                        else {
                            //TODO: chk commenting curate here
                            //namelistService.curateName(taxon)
                            if(!taxon.id){
                                //No name with null status
                                //taxon.status = null;
                                //even no proper match from COL
                                newNameSaved = true;
                            }else {
                                //because new name saved from COL details
                                newNameSaved = false;
                            }
                        }

                        if(!taxon.save(flush:true)) {
                            taxon.errors.each { log.error it }
                        }
                        if(fromCOL) {
                            //taxon = namelistService.updateAttributes(taxon, res);
                        }
                    } else if(taxon && fromCOL && !NamePosition.CLEAN) {
                        if(fieldNode == fieldNodes.last()){
                            taxon.matchDatabaseName = otherParams?.metadata?otherParams.metadata.source:"";
                            taxon.viaDatasource = otherParams?.metadata?otherParams.metadata.via:"";
                            taxon.authorYear = otherParams?.metadata?otherParams.metadata.authorString:"";
                        }

                        taxon.position = NamePosition.WORKING
                        taxon.matchDatabaseName = "CatalogueOfLife";
                        if(otherParams.id_details && otherParams.id_details[taxon.canonicalForm+ "#" + taxon.rank]) {
                            println "========UPDATING EXISTING TAXON ======= " + otherParams.id_details[taxon.canonicalForm+ "#" + taxon.rank] + "=======TAXON CANONICAL === " + taxon.canonicalForm + "  -- id " + taxon.id
                            taxon.matchId = otherParams.id_details[taxon.canonicalForm+ "#" + taxon.rank];
                        }
                        if(!taxon.save(flush:true)) {
                            taxon.errors.each { println it; log.error it }
                        }
                    }else if(otherParams && taxon && otherParams.spellCheck && fieldNode == fieldNodes.last()) {
                        def oldTaxon = TaxonomyDefinition.get(otherParams.oldTaxonId.toLong());
                        spellCheckMsg = 'Edit of ' + oldTaxon.name + '('+oldTaxon.id +') to ' + taxon.name +'('+oldTaxon.id +') causes a clash with ' + taxon.name + '('+taxon.id +'). Edit saved and flagged for attention of admin.'
                        taxon = oldTaxon;
                    } else if(saveTaxonHierarchy && taxon && parsedName && taxon.name != parsedName.name) {
                        //println "=====TAXON WAS THERE================================== "
                        /*
                        def synonym = saveSynonym(parsedName, getRelationship(null), taxon);
                        if(synonym)
                        synonym.updateContributors(getUserContributors(fieldNode.data))
                         */
                    }
                    //Moving name to Working list, so all names should be in working list,
                    //even if a single name in hierarchy is in dirty list
                    //abort process
                    if(otherParams && otherParams.moveToWKG) {
                        if(taxon.position == NamePosition.RAW) {
                            newNameSaved = true;
                        }
                    }

                    //newNameSaved true becoz now this taxon cant be used in hierarchy 
                    //of a lower level as its status is not accepted 
                    newNameSaved = newNameSaved || (taxon.status != NameStatus.ACCEPTED)
                    
                    //updating contributors
                    taxon.updateContributors(getUserContributors(fieldNode.data))
                    //updating name status given in sheet
                    taxon.updateNameStatus(fieldNode?.status?.text())
                    //saving taxon new position
                    taxon.updatePosition(fieldNode?.position?.text(), null, null, parsedName)

                    def nameRunningStatus = fieldNode?.nameRunningStatus?.text();
                     
                    if("new".equalsIgnoreCase(nameRunningStatus)){                     
                       taxon.authorYear=authorYear;
                       if(!taxon.save(flush:true)) {
                            taxon.errors.each { println it; log.error it }
                        }
                    }
                    
                    if(addNewNameToSession){
                        NamelistService.addNewNameInSession(taxon)
                    }

                    //saving taxon registry
                    def parentTaxon = getParentTaxon(taxonEntities, rank);
                    def path = (parentTaxon ? parentTaxon.path+"_":"") + taxon.id;
                    def criteria = TaxonomyRegistry.createCriteria()
                    TaxonomyRegistry ent = criteria.get {
                        and{
                            eq("taxonDefinition", taxon);
                            eq("path", path);
                            eq("classification", classification);
                        }
                    }
                    ent = ent?:new TaxonomyRegistry();
                    ent.taxonDefinition = taxon
                    ent.classification = classification;
                    ent.parentTaxon = parentTaxon;
                    ent.parentTaxonDefinition = ent.parentTaxon?.taxonDefinition
                    ent.path = path;
                    if(saveTaxonHierarchy) {
                        //log.debug "Saving Taxon registry : " +  ent;
                        if(!ent.save(flush:true)) {
                            ent.errors.each { log.error it }
                        } else {
                            //log.debug "Saved taxon registry entity : "+ent;
                            ent.updateContributors(getUserContributors(fieldNode.data))
                            taxonEntities.add(ent);
                        }
                    }
                } else {
                    log.error "Ignoring taxon entry as the name is not parsed : "+parsedName
                    addToSummary("Ignoring taxon entry as the name is not parsed : "+parsedName)
                }
            }
        }
        return ['taxonRegistry' : taxonEntities, 'spellCheckMsg' : spellCheckMsg];
    }




    /**
     * This method first look at the name node and if any ibpid or colid is given then return(if not present then create first)
     * if namenode is null then doing normal ibp search
     * @param parsedName
     * @param rank
     * @param searchInNull
     * @param useAuthorYear
     * @param nameNode
     * @return
     */
    private List searchIBP(TaxonomyDefinition parsedName, rank, searchInNull, useAuthorYear, nameNode, status = null){
		def ibpId = nameNode?.ibpId?.text();
		def colId = nameNode?.colId?.text();
		def nameRunningStatus = nameNode?.nameRunningStatus?.text();
		
		
		// to force creation of new node
		if("new".equalsIgnoreCase(nameRunningStatus)){
			def taxon = NamelistService.getNewNameInSession(parsedName.canonicalForm, rank)
			if(taxon)
				return [taxon]
				
			return []
		}
		
		//println "----------------- ibp id  " +  ibpId + "  and col id " + colId
		if(ibpId){
			ibpId = Long.parseLong(ibpId.trim());
			return [TaxonomyDefinition.get(ibpId)]
		}
		
		if(colId){
			def taxon = TaxonomyDefinition.findByMatchId(colId)
			if(!taxon)
				taxon =  ApplicationHolder.getApplication().getMainContext().getBean("namelistService").createNameFromColId(colId)
				
			return [taxon]
		}
		
		
		
		return NamelistService.searchIBP(parsedName.canonicalForm, parsedName.authorYear, status, rank, searchInNull, parsedName.normalizedForm, useAuthorYear)
	}
	
    /**
     * 
     * @param rankStr
     * @return
     */
    static int getTaxonRank(String rankStr) {
		return ScientificName.TaxonomyRank.getTaxonRank(rankStr);
        /* // moved to TaxonRank
        MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
        def request = null;
        try {
            request = RequestContextHolder.currentRequestAttributes().request
        } catch (e) {
            //log.debug "No thread bound request"
        }

        for(TaxonomyRank type : TaxonomyRank) {
            String message = request ? messageSource.getMessage(type.getCodes()[0], null, RCU.getLocale(request)) : type.value()
            if(type.value().equalsIgnoreCase(rankStr) || message.equalsIgnoreCase(rankStr)) {
                return type.ordinal();
            }
        }
        return -1;
        */
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
    TaxonomyDefinition getTaxonConceptFromName(String sciName, int rank, boolean createNew = true, Node nameNode = null) {
		def cleanSciName = Utils.cleanSciName(sciName);

        if(cleanSciName) {
			TaxonomyDefinition taxon
			List name = namesParser.parse([cleanSciName])
			//If ibp or colid is given inside the node then using this method
			if(nameNode){
				List res = searchIBP(name, rank, false, true, nameNode)
				if(res){
					return res[0]
				}
			}
			
            if(name[0].normalizedForm) {
				List taxonList = NamelistService.searchIBP(name[0].canonicalForm, name[0].authorYear, null, rank, false, name[0].normalizedForm)
				if(taxonList.size() > 1){
					log.error '############  ' + "IBP search returning mulitiple result: should not happen " + taxonList
				}
				if(taxonList){
					taxon =  taxonList[0]
				}
				if(!taxon && createNew) {
                    taxon = name[0];
                    taxon.rank = rank;
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
        def species = Species.withCriteria() {
                eq("guid", s.guid);
                fetchMode 'userGroups', FetchMode.JOIN
                fetchMode 'resources', FetchMode.JOIN
                fetchMode 'fields', FetchMode.JOIN
                fetchMode 'globalDistributionEntities', FetchMode.JOIN
                fetchMode 'globalEndemicityEntities', FetchMode.JOIN
                fetchMode 'indianDistributionEntities', FetchMode.JOIN
                fetchMode 'indianEndemicityEntities', FetchMode.JOIN
        //      if(!species.isAttached()) {
        //          species.attach();
        //      }
        }
        if(species)
            return species[0]
    }

    /**
     * 
     * @param existingSpecies
     * @param newSpecies
     */
    void mergeSpecies(Species existingSpecies, Species newSpecies) {
        //              newSpecies.fields.each { field ->
        //                  existingSpecies.addToFields(field);
        //              }
        //              newSpecies.synonyms.each { field ->
        //                  existingSpecies.addToSynonyms(field);
        //              }
        //              newSpecies.commonNames.each { field ->
        //                  existingSpecies.addToFields(field);
        //              }
        //              newfields,
        //              synonyms, commonNames, globalDistributionEntities, globalEndemicityEntities,
        //              taxonomyRegistry;

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
            try {
                hibSession.flush()
            } catch(ConstraintViolationException e) {
                e.printStackTrace()
            }
			//hibSession.clear()
        }
    }


    void setLogAppender(FileAppender fa) {
        if(fa) {
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }

    List<Synonyms> createSynonymsOld(Node fieldNode, TaxonomyDefinition taxonConcept) {
        log.debug "Creating synonyms";
        List<Synonyms> synonyms = new ArrayList<Synonyms>();
        //List<SpeciesField> sfields = createSpeciesFields(fieldNode, Synonyms.class, null, null, null, null,null);
        fieldNode.data.eachWithIndex { n, index ->
            RelationShip rel = getRelationship(n.relationship?.text());
            if(rel) {
                def cleanName = Utils.cleanName(n.text()?.trim());
                def parsedNames = namesParser.parse([cleanName]);
                def sfield = saveSynonymOld(parsedNames[0], rel, taxonConcept);
                if(sfield) {
                    //adding contributors
                    sfield.updateContributors(getUserContributors(n))
                    synonyms.add(sfield);
                }
            } else {
                log.warn "NOT A SUPPORTED RELATIONSHIP: "+n.relationship?.text();
                addToSummary("NOT A SUPPORTED RELATIONSHIP: "+n.relationship?.text())
            }
        }
        return synonyms;
    }

    private Synonyms saveSynonymOld(TaxonomyDefinition parsedName, RelationShip rel, TaxonomyDefinition taxonConcept) {

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
            addToSummary("Ignoring synonym taxon entry as the name is not parsed : "+parsedName.name)
        }

    }
}
