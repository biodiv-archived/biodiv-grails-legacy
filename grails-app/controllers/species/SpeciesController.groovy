package species

import java.sql.ResultSet;

import species.TaxonomyDefinition.TaxonomyRank;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.auth.SUser;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.sourcehandler.SpreadsheetConverter;
import species.sourcehandler.XMLConverter;
import grails.converters.JSON;
import grails.converters.XML;
import groovy.sql.GroovyRowResult;
import groovy.sql.Sql
import groovy.xml.MarkupBuilder;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import species.NamesParser
import org.hibernate.FetchMode;
import species.SpeciesPermission.PermissionType;

import species.utils.Utils;
import grails.plugin.springsecurity.annotation.Secured
import com.grailsrocks.emailconfirmation.PendingEmailConfirmation;
import species.participation.UserToken;

class SpeciesController extends AbstractObjectController {

	def dataSource
	def speciesSearchService;
	def namesIndexerService;
    def speciesUploadService;
	def speciesService;
	def speciesPermissionService;
	def observationService;
	def userGroupService;
	def springSecurityService;
    def taxonService;
    def activityFeedService;

    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    
    String contentRootDir = config.speciesPortal.content.rootDir

	def index() {
		redirect(action: "list", params: params)
	}

	def list() {
		def model = speciesService.getSpeciesList(params, 'list');
		model.canPullResource = userGroupService.getResourcePullPermission(params)
		params.controller="species"
		params.action="list"

		if(params.loadMore?.toBoolean()){
			render(template:"/species/showSpeciesListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
            if(params.webaddress)
			    model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
			def obvListHtml =  g.render(template:"/species/showSpeciesListTemplate", model:model);
			model.resultType = "species"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]

			render (result as JSON)
			return;
		}
	}

	def listXML() {
		//cache "taxonomy_results"
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		def speciesList = Species.list(params) as XML;
		def writer = new StringWriter ();
		def result = new MarkupBuilder(writer);
		result.response() {
			numspecies (Species.count())
			result.mkp.yieldUnescaped (speciesList.toString() - "<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
		}
		render(contentType: "text/xml", text:writer.toString())
	}

	@Secured(['ROLE_USER'])
	def create() {
		def speciesInstance = new Species()
		speciesInstance.properties = params
		return [speciesInstance: speciesInstance]
	}

    @Secured(['ROLE_USER'])
    def save() {
        List errors = [];
        Map result = [errors:errors];
        if(params.page && params.rank) {
            Map list = params.taxonRegistry?:[:];
            List t = [];
            String speciesName;
            int rank;
            list.each { key, value -> 
                if(value) {
                    t.putAt(Integer.parseInt(key).intValue(), value);
                 }
            } 

            speciesName = params.page
            rank = params.int('rank');
            t.putAt(rank, speciesName);

            //t.putAt(TaxonomyRank.SPECIES.ordinal(), params.species);

            try {
                result = speciesService.createSpecies(speciesName, rank, t);
                result.errors = result.errors ? ' : '+result.errors : '';
                if(!result.success) {
                    if(result.status == 'requirePermission') {
                        //flash.message = "${message(code: 'species.contribute.not.permitted.message', args: ['contribute to', message(code: 'species.label', default: 'Species'), params.id])}"
                        flash.message = "Sorry, you don't have permission to contribute to this species ${params.id?speciesName+(params.id):''}. Please request for permission below."

                        def url = uGroup.createLink(controller:"species", action:"contribute");
                        redirect url: url
                        return;
                    } else {
                    flash.message = result.msg ? result.msg+result.errors:"Error while creating page"+result.errors
			        render(view: "create", model: result)
                    return;
                    }
                }

                Species speciesInstance = result.speciesInstance;
                if(speciesInstance.taxonConcept) {

                    if (!speciesInstance.hasErrors() && speciesInstance.save(flush:true)) {
                        //Saving current user as contributor for the species
                        //if(!speciesPermissionService.addContributorToSpecies(springSecurityService.currentUser, speciesInstance)){
                            //flash.message = "Successfully created species. But there was a problem in adding current user as contributor."
                        //} else {
                            flash.message = "Successfully created species."
                            speciesUploadService.postProcessSpecies([speciesInstance]);
                        //}
                        
                        def feedInstance = activityFeedService.addActivityFeed(speciesInstance, null, springSecurityService.currentUser, activityFeedService.SPECIES_CREATED);

                        observationService.sendNotificationMail(activityFeedService.SPECIES_CREATED, speciesInstance, request, params.webaddress, feedInstance, ['info':activityFeedService.SPECIES_CREATED]);


                        redirect(action: "show", id: speciesInstance.id, params:['editMode':true])
                        return;
                    } else {
                        flash.message = result.msg ? result.msg + result.errors : "Error while saving species " + result.errors
                    }
                }
                else {
                    flash.message = result.msg ? result.msg + result.errors : "Error while saving species " + result.errors
                }
            } catch(e) {
                e.printStackTrace();
                result.errors << e.getMessage();
                flash.message = result.msg ? result.msg+result.errors : "Error while saving species "+result.errors
            }
        }
        render(view: "create", model:result)
    }

	def show() {
		//cache "content"
		def speciesInstance = Species.get(params.long('id'))
		if (!speciesInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
			redirect(action: "list")
		}
		else {
            if(params.editMode) {
                if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
			        //flash.message = "${message(code: 'species.contribute.not.permitted.message', args: ['contribute to', message(code: 'species.label', default: 'Species'), params.id])}"
                    flash.message = "Sorry, you don't have permission to contribute to this species ${params.id?speciesInstance.title+' ( '+params.id+' )':''}. Please request for permission below."
                    def url = uGroup.createLink(controller:"species", action:"contribute");
                    redirect url: url
            		return;
                }
            }

			def c = Field.createCriteria();
			def fields = c.list(){
				and{ order('displayOrder','asc') }
			};
			Map map = getTreeMap(speciesInstance, fields);
			map = mapSpeciesInstanceFields(speciesInstance, speciesInstance.fields, map);
			def relatedObservations = observationService.getRelatedObservationByTaxonConcept(speciesInstance.taxonConcept.id, 1,0);
			def observationInstanceList = relatedObservations?.observations?.observation
			def instanceTotal = relatedObservations?relatedObservations.count:0

			def result = [speciesInstance: speciesInstance, fields:map, totalObservationInstanceList:[:], observationInstanceList:observationInstanceList, instanceTotal:instanceTotal, queryParams:[max:1, offset:0], 'userGroupWebaddress':params.webaddress]
            if(springSecurityService.currentUser) {
                SpeciesField newSpeciesFieldInstance = speciesService.createNewSpeciesField(speciesInstance, fields[0], '');
                result['newSpeciesFieldInstance'] = newSpeciesFieldInstance
            }
            return result
		}
	}

	private Map getTreeMap(Species speciesInstance, List fields) {
        def user = springSecurityService.currentUser;

		Map map = new LinkedHashMap();
		for(Field field : fields) {
			Map finalLoc;
			Map conceptMap, categoryMap, subCategoryMap;
			if(field.concept && !field.concept.equals("")) {
				if(map.containsKey(field.concept)) {
					conceptMap = map.get(field.concept);
				} else {
					conceptMap = new LinkedHashMap();
					map.put(field.concept, conceptMap);
				}
				finalLoc = conceptMap;

				if(field.category && !field.category.equals("")) {
					if(conceptMap.containsKey(field.category)) {
						categoryMap = conceptMap.get(field.category);
					} else {
						categoryMap = new LinkedHashMap();
						conceptMap.put(field.category, categoryMap);
					}
					finalLoc = categoryMap;

					if(field.subCategory && !field.subCategory.equals("")) {
						if(categoryMap.containsKey(field.subCategory)) {
							subCategoryMap = categoryMap.get(field.subCategory);
						} else {
							subCategoryMap = new LinkedHashMap();
							categoryMap.put(field.subCategory, subCategoryMap);
						}
						finalLoc = subCategoryMap;
					}
				}
				finalLoc.put ("field", field);
                if(user && speciesPermissionService.isSpeciesContributor(speciesInstance, user)) {
                    finalLoc.put('isContributor', 1);
                }
			}
		}

		return map;
	}

	private Map mapSpeciesInstanceFields(Species speciesInstance, Collection speciesFields, Map map) {

		def config = grailsApplication.config.speciesPortal.fields
        SUser user = springSecurityService.currentUser;

		for (SpeciesField sField : speciesFields) {
			Map finalLoc;
            //concept
			if(map.containsKey(sField.field.concept)) {
				finalLoc = map.get(sField.field.concept);
                if(speciesService.hasContent(sField) || finalLoc.get('hasContent')) {
                    finalLoc.put('hasContent', true);
                }
                if(finalLoc.get('isContributor') && isContentContributor(sField) && !sField.field.category) {
                        finalLoc.put('isContributor', 2)
                }
                //category
                
                if(finalLoc.containsKey(sField.field.category)) {
                    finalLoc = finalLoc.get(sField.field.category);
                    if(speciesService.hasContent(sField) || finalLoc.get('hasContent')) {
                            map.get(sField.field.concept).put('hasContent', true);
                            finalLoc.put('hasContent', true);
                    }
                    if(finalLoc.get('isContributor') && isContentContributor(sField) && !sField.field.subCategory) {
                        finalLoc.put('isContributor', 2)
                    }

                    //subcategory
					if(sField.field.subCategory && finalLoc.containsKey(sField.field.subCategory)) {
						finalLoc = finalLoc.get(sField.field.subCategory);
                        if(speciesService.hasContent(sField) || finalLoc.get('hasContent')) {
                            map.get(sField.field.concept).put('hasContent', true);
                            map.get(sField.field.concept).get(sField.field.category).put('hasContent', true);
                            finalLoc.put('hasContent', true);
                        }
                        if(finalLoc.get('isContributor') && isContentContributor(sField)){
                            finalLoc.put('isContributor', 2)
                            /*if(!map.get(sField.field.concept).containsKey('isContributor'))
                                map.get(sField.field.concept).put('isContributor', 1);
                            if(!map.get(sField.field.concept).get(sField.field.category).containsKey('isContributor'))

                                map.get(sField.field.concept).get(sField.field.category).put('isContributor', 1);
                            */
                        }
					}

				}
			}
			if(finalLoc.containsKey('field')) { 
				def t = finalLoc.get('speciesFieldInstance');
				if(!t) {
					t = [];
					finalLoc.put('speciesFieldInstance', t);
				} 
				t.add(sField); 
                //TODO:do an insertion sort instead of sorting collection again and again
            //    speciesService.sortAsPerRating(t);
			}
		}
        //remove empty information hierarchy
		for(concept in map.clone()) {
            if(concept.value.get('speciesFieldInstance')) {
                speciesService.sortAsPerRating(map.get(concept.key).get('speciesFieldInstance'));
			}
			for(category in concept.value.clone()) {
				if(category.key.equals("field") || category.key.equals("speciesFieldInstance") ||category.key.equals("hasContent") ||category.key.equals("isContributor") || category.key.equalsIgnoreCase('Species Resources'))  {
					continue;
				} else if(category.key.equals(config.OCCURRENCE_RECORDS) || category.key.equals(config.REFERENCES) ) {
					boolean show = false;
					if(category.key.equals(config.REFERENCES)) {
						for(f in speciesInstance.fields) {
							if(f.references) {
								show = true;
								break;
							}
						}
					} else {
						show = true;
					}
					if(show) {
                            map.get(concept.key).get(category.key).put('hasContent', true);
                            map.get(concept.key).put('hasContent', true);
					}
				} else if(category.value.get('speciesFieldInstance')) {
					    speciesService.sortAsPerRating(map.get(concept.key).get(category.key).get('speciesFieldInstance'));
				}

                if(category.value.get('hasContent')) {
                    map.get(concept.key).get(category.key).put('hasContent', true);
                    map.get(concept.key).put('hasContent', true);
                }
                if(category.value.get('isContributor')) {
                    int val = category.value.get('isContributor')
                    if(!map.get(concept.key).containsKey('isContributor'))
                        map.get(concept.key).put('isContributor', 1);
                }


				for(subCategory in category.value.clone()) {
					if(subCategory.key.equals("field") || subCategory.key.equals("speciesFieldInstance") || subCategory.key.equals('hasContent') ||subCategory.key.equals("isContributor")  ) continue;

					if((subCategory.key.equals(config.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.globalDistributionEntities.size()>0)  ||
					(subCategory.key.equals(config.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.globalEndemicityEntities.size()>0)||
					(subCategory.key.equals(config.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.indianDistributionEntities.size()>0) ||
					(subCategory.key.equals(config.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.indianEndemicityEntities.size()>0)) {

                        if(subCategory.value.get('speciesFieldInstance')) {
                            speciesService.sortAsPerRating(map.get(concept.key).get(category.key).get(subCategory.key).get('speciesFieldInstance'));
                        }
					}

                    if(subCategory.value.get('hasContent')) { 
                        map.get(concept.key).get(category.key).put('hasContent', true);
                        map.get(concept.key).put('hasContent', true);
                    }

                    if(subCategory.value.get('isContributor')) { 
                        int val = subCategory.value.get('isContributor')

                        if(!map.get(concept.key).get(category.key).containsKey('isContributor'))
                            map.get(concept.key).get(category.key).put('isContributor', 1);
                        if(!map.get(concept.key).containsKey('isContributor'))
                            map.get(concept.key).put('isContributor', 1);
                    }
				}
			}
		}

		return map;
	}

    private boolean isContentContributor(SpeciesField sField) {
        if(springSecurityService.currentUser == null) return false;
                
        for(c1 in sField.contributors) {
            if(c1?.id == springSecurityService.currentUser.id) {
                return true;
            }
        }
        return false;
    }

	@Secured(['ROLE_USER'])
	def edit() {
		if(params.id) {
			def speciesInstance = Species.get(params.long('id'))
			if (!speciesInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
				redirect(action: "list")
			}
			else {
				return [speciesInstance: speciesInstance]
			}
		} else {
			//Not being used for now
			params.max = Math.min(params.max ? params.int('max') : 10, 100)
			return [speciesInstanceList: Species.list(params), instanceTotal: Species.count()]
		}
	}

	@Secured(['ROLE_USER'])
    def update() {
        if(!(params.name && params.pk)) {
            render ([success:false, msg:'Either field name or field id is missing'] as JSON)
            return;
        }
        try {
            def result;
            long speciesFieldId = params.pk ? params.long('pk'):null;
            def value = params.value;

            switch(params.name) {
                case "contributor":
                long cid = params.cid?params.long('cid'):null;
                if(params.act == 'delete') {
                    result = speciesService.deleteContributor(cid, speciesFieldId, params.name);
                } else {
                    result = speciesService.updateContributor(cid, speciesFieldId, value, params.name);
                }
                break;
                case "attribution":
                long cid = params.cid?params.long('cid'):null;
                if(params.act == 'delete') {
                    result = speciesService.deleteAttributor(cid, speciesFieldId, params.name);
                } else {
                    result = speciesService.updateAttributor(cid, speciesFieldId, value, params.name);
                }
                break;
                case "description":
                if(params.act == 'delete') {
                    result = speciesService.deleteDescription(speciesFieldId);
                } else {
                    result = speciesService.updateDescription(speciesFieldId, value);
                }
                break;
                case "newdescription":
                long speciesId = params.speciesid? params.long('speciesid') : null;
                long fieldId = speciesFieldId;
                result = speciesService.addDescription(speciesId, fieldId, value);
                def html = [];
                if(result.speciesInstance) {
                    boolean isSpeciesContributor = speciesPermissionService.isSpeciesContributor(result.speciesInstance, springSecurityService.currentUser);

                    result.content.each {sf ->
                        boolean isSpeciesFieldContributor = speciesPermissionService.isSpeciesFieldContributor(sf, springSecurityService.currentUser);
                        html << g.render(template:'/common/speciesFieldTemplate', model:['speciesInstance':sf.species, 'speciesFieldInstance':sf, 'speciesId':sf.species.id, 'fieldInstance':sf.field, 'isSpeciesContributor':isSpeciesContributor, 'isSpeciesFieldContributor':isSpeciesFieldContributor]);
                    }
                    result.content = html.join(' ');
                }
                break;
                case 'license':
                result = speciesService.updateLicense(speciesFieldId, value);
                break;
                case 'audienceType':
                result = speciesService.updateAudienceType(speciesFieldId, value);
                break;
                case 'status':
                result = speciesService.updateStatus(speciesFieldId, value);
                break;
                case "reference":
                long cid = params.cid?params.long('cid'):null;
                if(params.act == 'delete') {
                    result = speciesService.deleteReference(cid, speciesFieldId);
                } else {
                    result = speciesService.updateReference(cid, speciesFieldId, value);
                }
                break;
                case 'synonym':
                long sid = params.sid?params.long('sid'):null;
                String relationship = params.relationship?:null;

                if(params.act == 'delete') {
                    result = speciesService.deleteSynonym(sid, speciesFieldId);
                } else {
                    result = speciesService.updateSynonym(sid, speciesFieldId, relationship, value);
                }
                break;
                case 'commonname':
                long cid = params.cid?params.long('cid'):null;
                String language = params.language?:null;

                if(params.act == 'delete') {
                    result = speciesService.deleteCommonname(cid, speciesFieldId);
                } else {
                    result = speciesService.updateCommonname(cid, speciesFieldId, language, value);
                }

                break;
                case 'speciesField':
                Species speciesInstance;
                SpeciesField speciesFieldInstance;
                if(params.act == 'delete') {
                    result = speciesService.deleteSpeciesField(speciesFieldId);
                } else if(params.act == 'add') {
                    long speciesId = params.speciesid? params.long('speciesid') : null;
                    long fieldId = speciesFieldId;
                    result = speciesService.addSpeciesField(speciesId, fieldId, params);
                } else {
                    speciesFieldInstance = SpeciesField.get(speciesFieldId);
                    if(!speciesFieldInstance) {
                        return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
                    }
                    result = speciesService.updateSpeciesField(speciesFieldInstance, params);
                }
                result['act'] = params.act;
                List html = [];
                result.content.each {sf ->
                    boolean isSpeciesFieldContributor = speciesPermissionService.isSpeciesFieldContributor(sf, springSecurityService.currentUser);
                    html << g.render(template:'/common/speciesFieldTemplate', model:['speciesInstance':sf.species, 'speciesFieldInstance':sf, 'speciesId':sf.species.id, 'fieldInstance':sf.field, 'isSpeciesFieldContributor':isSpeciesFieldContributor]);
                }
                result.content = html.join();
                
                break;
                default :
                result=['success':false, msg:'Incorrect datatype'];
            }
 
            if(result.success) {
                def feedInstance;
                if(result.activityType)
                    feedInstance = activityFeedService.addActivityFeed(result.speciesInstance, result.speciesFieldInstance, springSecurityService.currentUser, result.activityType);
                if(result.mailType) 
                    observationService.sendNotificationMail(result.mailType, result.speciesInstance, request, params.webaddress, feedInstance, ['info':result.activityType]);
                result.remove('speciesInstance');
                result.remove('speciesFieldInstance');
                result.remove('activityType');
            }

            render result as JSON
            return;
        } catch(Exception e) {
            e.printStackTrace();
            log.error e.getMessage();
            render ([success:false, msg:e.getMessage()] as JSON)
            return;
        }

    }

	@Secured(['ROLE_SPECIES_ADMIN'])
	def addResource() {
		if(!params.id) {
			render ([success:false, errors:[msg:'Species id is missing']] as JSON)
			return;
		}

		def result;
		long speciesInstanceId = params.long('id');
		def speciesInstance = Species.get(speciesInstanceId);

		if(!speciesInstance) {
			render ([success:false, errors:[msg:'Species instance with id not found']] as JSON)
			return;
		}

		try {
			def resourcesXML;
			if(params.image) {
				resourcesXML = speciesService.createImagesXML(params);
			} else if (params.video) {
				resourcesXML = speciesService.createVideoXML(params);
			} else {
				log.error "No resource is given in the parameters"
				render ([success:false, errors:[msg:'No resource is given in the parameters']] as JSON)
				return;
			}

			log.debug resourcesXML;
			if(resourcesXML) {
				def resources = speciesService.saveResources(resourcesXML,  speciesInstance.taxonConcept.canonicalForm);
				log.debug resources;
				resources.each { resource ->
					speciesInstance.addToResources(resource);
				}


				if(!speciesInstance.hasErrors() && speciesInstance.save(flush:true)) {
					render ([success:true, id:resources[0].id, msg:""]) as JSON
					return;
				} else {
					speciesInstance.errors.each { log.error it }
					render ([success:false, errors:[msg:"Error while updating species "]]) as JSON
					return;
				}


			}
		} catch(e) {
			render ([success:false, errors:[msg:'Error adding resource: $e.message']] as JSON)
		}

	} 

	@Secured(['ROLE_ADMIN'])
	def delete() {
		def speciesInstance = Species.get(params.long('id'))
		if (speciesInstance) {
			try {
				speciesInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
			redirect(action: "list")
		}
	}

	def count() {
		//cache "search_results"
		render Species.count();
	}

	def countSpeciesWithRichness() {
		//cache "search_results"
		render Species.countByPercentOfInfoGreaterThan(0);
	}

	def taxonBrowser() {
	}

	def contribute() {
		//render (view:"contribute");
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search() {
		def model = speciesService.getSpeciesList(params, 'search')
		model.canPullResource = userGroupService.getResourcePullPermission(params)
		model['isSearch'] = true;

		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/species/searchResultsTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
			return;
		} else {
            if(params.webaddress)
			    model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);

			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/species/searchResultsTemplate", model:model);
			model.resultType = "specie"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]

			render (result as JSON)
			return;
		}
	}



	/**
	 *
	 */
	def terms() {
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		List result = speciesService.nameTerms(params)
		render result.value as JSON;
	}


	//	def getRelatedObservations = {
	//
	//		def speciesInstance = Species.get(params.long('id'))
	//		if (speciesInstance) {
	//			params.limit = Math.min(params.max ? params.int('limit') : 10, 100);
	//			params.offset = params.offset ? params.int('offset') : 0
	//			params.filterProperty = 'taxonConcept'
	//			params.filterPropertyValue = speciesInstance.taxonConcept;
	//
	//			def relatedObv = observationService.getRelatedObservations(params).relatedObv;
	//
	//			if(relatedObv.observations) {
	//				relatedObv.observations = observationService.createUrlList2(relatedObv.observations);
	//			}
	//
	//			render relatedObv as JSON
	//		}
	//		else {
	//			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
	//			render (['success':false, msg:'No species id'] as JSON)
	//		}
	//
	//
	//	}

	@Secured(['ROLE_ADMIN'])
	def requestExport() {
		log.debug "Export of species requested" + params
		speciesService.requestExport(params)
		def r = [:]
		r['msg']= "${message(code: 'species.download.requsted', default: 'Processing... You will be notified by email when it is completed. Login and check your user profile for download link.')}"
		render r as JSON
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////Online upload //////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////

	@Secured(['ROLE_SPECIES_ADMIN'])
	def upload() {
		log.debug params.xlsxFileUrl
		if(params.xlsxFileUrl){
			def res = speciesUploadService.basicUploadValidation(params)
			log.debug "Starting bulk upload"
			res = speciesUploadService.upload(res.sBulkUploadEntry)
			render(text:res as JSON, contentType:'text/html')
		}
	}
	
	def getDataColumns() {
        List res = speciesUploadService.getDataColumns();
        render res as JSON
    }
    
    @Secured(['ROLE_SPECIES_ADMIN', 'ROLE_ADMIN'])
	def rollBackUpload() {
		log.debug params
		def m = [success:true, msg:speciesUploadService.rollBackUpload(params)]
		render m as JSON
	}

	@Secured(['ROLE_SPECIES_ADMIN', 'ROLE_ADMIN'])
	def abortBulkUpload() {
		log.debug params
		def m = [success:true, msg:speciesUploadService.abortBulkUpload(params)]
		render m as JSON
	}
	
    @Secured(['ROLE_USER'])
    def requestPermission() {
        def selectedNodes = params.selectedNodes
        if(selectedNodes) {
            List members = [springSecurityService.currentUser];
            def msg = speciesPermissionService.sendPermissionRequest(selectedNodes, members, Utils.getDomainName(request), params.invitetype, params.message)
            render (['success':true, 'statusComplete':true, 'shortMsg':'Sent request', 'msg':msg] as JSON)
        } else {
            render (['success':false, 'statusComplete':false, 'shortMsg':'Error while sending request.', 'msg':'Please select a node'] as JSON)
        }
		return
    }

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
    def confirmPermissionRequest () {
        if(params.userId && params.taxonConcept){
            SUser user = SUser.get(params.userId.toLong())
            TaxonomyDefinition taxonConcept = TaxonomyDefinition.get(params.taxonConcept.toLong())
            String invitetype = params.invitetype;
            boolean success = false;
            switch (invitetype) {
                case 'curator':
                success = speciesPermissionService.addCurator(user, taxonConcept)
                break;
 
                case 'contributor':
                success = speciesPermissionService.addContributorToTaxonConcept(user, taxonConcept)
                break;
                default: log.error "No invite type"
            }

            if(success) {
                observationService.sendNotificationMail(observationService.NEW_SPECIES_PERMISSION, taxonConcept, null, null, null, ['permissionType':invitetype, 'taxonConcept':taxonConcept, 'user':user]);
                def conf = PendingEmailConfirmation.findByConfirmationToken(params.confirmationToken);
                if(conf) {
                    log.debug "Deleting confirmation code and usertoken params";
                    conf.delete();
                    UserToken.get(params.tokenId.toLong())?.delete();
                }
                flash.message="Successfully added ${user} as a ${invitetype} to ${taxonConcept.name}"
            } else {
                flash.error="Couldn't add ${user} as ${invitetype} to ${taxonConcept.name} because of missing information."            
            }
        }else{
            flash.error="Couldn't add ${user} as ${invitetype} to ${taxonConcept.name} because of missing information."            
        }
        def url = uGroup.createLink(controller:"species", action:"taxonBrowser");
        redirect url: url
		return;
    }

	@Secured(['ROLE_SPECIES_ADMIN', 'ROLE_ADMIN'])
    def invite () {
        log.debug " inviting curators " + params
        List members = Utils.getUsersList(params.userIds);
        def selectedNodes = params.selectedNodes
        if(selectedNodes) {
            def msg = speciesPermissionService.sendPermissionInvitation(selectedNodes, members, Utils.getDomainName(request), params.invitetype, params.message)
            render (['success':true, 'statusComplete':true, 'shortMsg':'Sent request', 'msg':msg] as JSON)
        } else {
            render (['success':false, 'statusComplete':false, 'shortMsg':'Error while sending request.', 'msg':'Please select a node'] as JSON)
         }
		return
    } 

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
    def confirmPermissionInviteRequest() {
        if(params.userId && params.taxonConcept){
            SUser user = SUser.get(params.userId.toLong())
            TaxonomyDefinition taxonConcept = TaxonomyDefinition.get(params.taxonConcept.toLong())
            String invitetype = params.invitetype;
            switch (invitetype) {
                case 'curator':
                speciesPermissionService.addCurator(user, taxonConcept)
                break;
 
                case 'contributor':
                speciesPermissionService.addContributorToTaxonConcept(user, taxonConcept)
                break;
                default: log.error "No invite type"
            }

            def conf = PendingEmailConfirmation.findByConfirmationToken(params.confirmationToken);
            if(conf) {
                log.debug "Deleting confirmation code and usertoken params";
                conf.delete();
                UserToken.get(params.tokenId.toLong())?.delete();
            }
            flash.message="Successfully added ${user} as a ${invitetype} to ${taxonConcept.name}"
        }else{
            flash.error="Couldn't add ${user} as ${invitetype} to ${taxonConcept.name} because of missing information."            
        }
        def url = uGroup.createLink(controller:"species", action:"taxonBrowser");
        redirect url: url
		return;
    }

    @Secured(['ROLE_SPECIES_ADMIN', 'ROLE_ADMIN'])
    def searchPermission() {
        if(params.rank && params.page) {
            int rank = params.rank?params.int('rank'):null;
            try {
                Map r = getTaxon(params.page, rank);
                if(r.success) {
                    if(r.taxon) {
                        render speciesPermissionService.getUsers(r.taxon, PermissionType.ROLE_CONTRIBUTOR) as JSON
                        return;
                    }
                }
            } catch(e) {
                e.printStackTrace();
            }
        } else if(params.user) {
            List<SUser> users = Utils.getUsersList(params.userIdsAndEmailIds);
            def result = [];
            users.each { user -> 
                result[user.id] = speciesPermissionService.contributorFor(user);
            }
            render (result as JSON)
            return;
        } 
        render '';
        return;
    }

    def uploadImage() {
        log.debug params
        //pass that same species
        def species = Species.get(params.speciesId.toLong())
        def out = speciesService.updateSpecies(params, species)
        def result
        if(out){
            result = ['success' : true]
        }
        else{
            result = ['success'  : false]
        }
        render result as JSON
    }

    def getRelatedObvForSpecies() {
        log.debug params
        def spInstance = Species.get(params.speciesId.toLong())
        def relatedObvMap = observationService.getRelatedObvForSpecies(spInstance, 4, params.offset.toInteger())
        def relatedObv = relatedObvMap.resList
        def relatedObvCount = relatedObvMap.count
        def obvLinkList = relatedObvMap.obvLinkList
        def addPhotoHtml = g.render(template:"/observation/addPhoto", model:[observationInstance: spInstance, resList: relatedObv, obvLinkList: obvLinkList, resourceListType: params.resourceListType, offset:params.offset.toInteger() ]);
        def result = [addPhotoHtml: addPhotoHtml, relatedObvCount: relatedObvCount]
        render result as JSON
    }

    def pullObvImage() {
        log.debug params  
        //pass that same species
        def species = Species.get(params.speciesId.toLong())
        def out = speciesService.updateSpecies(params, species)
        def result
        if(out){
            result = ['success' : true]
        }
        else{
            result = ['success'  : false]
        }   
        render result as JSON
    }

    def pullSpeciesFieldImage() {
        log.debug params
        def species = Species.get(params.speciesId.toLong())
        def out = speciesService.updateSpecies(params, species)
        def result
        if(out){
            result = ['success' : true]
        }
        else{
            result = ['success'  : false]
        }   
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def validate() {
/*        List hierarchy = [];
        if(params.taxonRegistry) {
            hierarchy = taxonService.getTaxonHierarchyList(params.taxonRegistry);
        }
*/

        def result = [requestParams:[taxonRegistry:params.taxonRegistry?:[:]]];
        if(params.page && params.rank) {
            try {
                int rank = params.rank?params.int('rank'):null;
                Map r = getTaxon(params.page, rank);
                if(r.success) {
                    TaxonomyDefinition taxon = r.taxon;
                    if(!taxon) {
                        result = ['success':true, 'msg':"Name validated and no match was found with existing species names on the portal. Please fill in the taxonomic hierarchy so that a species page can be created. Taxa marked with * are compulsory fields.", rank:rank, requestParams:[taxonRegistry:params.taxonRegistry]]
                    } else {
                        //CHK if a species page exists for this concept
                        Species species = Species.findByTaxonConcept(taxon);
                        def taxonRegistry = taxon.parentTaxonRegistry();
                        if(species) {
                            result = ['success':true, 'msg':'Already a species page exists with this name. ', id:species.id, name:species.title, rank:taxon.rank, requestParams:[taxonRegistry:params.taxonRegistry]];
                        } else {
                            result = ['success':true, 'msg':"Adding a new species page for an existing concept ${taxon.name}", rank:taxon.rank, requestParams:[taxonRegistry:params.taxonRegistry]];
                        }
                        result['taxonRegistry'] = [:];
                        taxonRegistry.each {classification, h ->
                            if(!result['taxonRegistry'][classification.name])
                                result['taxonRegistry'][classification.name] = [];
                            result['taxonRegistry'][classification.name] << h
                        }
                    }
                } else {
                    result.putAll(r);
                }
            } catch(e) {
                e.printStackTrace();
                result = ['success':false, 'msg':"Error while validating : ${e.getMessage()}", requestParams:[taxonRegistry:params.taxonRegistry]]
            }

        } else {
            result = ['success':false, 'msg':'Not a valid name.', requestParams:[taxonRegistry:params.taxonRegistry]]
        }
        render result as JSON
    }
    
    private Map getTaxon(String name, int rank) {
        def result = [:];
        if(!name || rank == null) return result;

        NamesParser namesParser = new NamesParser();
        List<TaxonomyDefinition> names = namesParser.parse([name]);
        TaxonomyDefinition page = names[0];
        if(page && page.canonicalForm) {
            def taxonCriteria = TaxonomyDefinition.createCriteria();
            TaxonomyDefinition taxon = taxonCriteria.get {
                eq("rank", rank);
                ilike("canonicalForm", page.canonicalForm);
            }
            if(rank == TaxonomyRank.SPECIES.ordinal() && !page.binomialForm) { //TODO:check its not uninomial
                result = ['success':false, 'msg':"Not a valid name ${name}."]
            } else {
                result = ['success':true, 'taxon':taxon];        
            }
        } else {
            result = ['success':false, 'msg':"Not a valid name ${name}."]
        }
        return result;
    }
}
