package species

import java.sql.ResultSet;

import species.TaxonomyDefinition.TaxonomyRank;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.sourcehandler.SpreadsheetConverter;
import species.sourcehandler.XMLConverter;
import grails.converters.JSON;
import grails.converters.XML;
import grails.web.JSONBuilder;
import groovy.sql.GroovyRowResult;
import groovy.sql.Sql
import groovy.xml.MarkupBuilder;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import species.utils.Utils;
import grails.plugins.springsecurity.Secured

class SpeciesController extends AbstractObjectController {

	def dataSource
	def grailsApplication
	def speciesSearchService;
	def namesIndexerService;
    def speciesUploadService;
	def speciesService;
	def observationService;
	def userGroupService
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
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
			model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
			def obvListHtml =  g.render(template:"/species/showSpeciesListTemplate", model:model);
			model.resultType = "species"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]

			render (result as JSON)
			return;
		}
	}

	def listXML = {
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
	def create = {
		def speciesInstance = new Species()
		speciesInstance.properties = params
		return [speciesInstance: speciesInstance]
	}

	@Secured(['ROLE_USER'])
	def save = {
		def speciesInstance = new Species(params)
		if (speciesInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'species.label', default: 'Species'), speciesInstance.id])}"
			redirect(action: "show", id: speciesInstance.id)
		}
		else {
			render(view: "create", model: [speciesInstance: speciesInstance])
		}
	}

	def show = {
		//cache "content"
		def speciesInstance = Species.get(params.long('id'))
		if (!speciesInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
			redirect(action: "list")
		}
		else {
			def c = Field.createCriteria();
			def fields = c.list(){
				and{ order('displayOrder','asc') }
			};
			Map map = getTreeMap(fields);
			map = mapSpeciesInstanceFields(speciesInstance, speciesInstance.fields, map);
			def relatedObservations = observationService.getRelatedObservationByTaxonConcept(speciesInstance.taxonConcept.id, 1,0);
			def observationInstanceList = relatedObservations?.observations?.observation
			def instanceTotal = relatedObservations?relatedObservations.count:0
			[speciesInstance: speciesInstance, fields:map, totalObservationInstanceList:[:], observationInstanceList:observationInstanceList, instanceTotal:instanceTotal, queryParams:[max:1, offset:0], 'userGroupWebaddress':params.webaddress]
		}
	}

	private Map getTreeMap(List fields) {
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
			}
		}

		return map;
	}

	private Map mapSpeciesInstanceFields(Species speciesInstance, Collection speciesFields, Map map) {

		def config = grailsApplication.config.speciesPortal.fields

		for (SpeciesField sField : speciesFields) {
			Map finalLoc;
			if(map.containsKey(sField.field.concept)) {
				finalLoc = map.get(sField.field.concept);
				if(finalLoc.containsKey(sField.field.category)) {
					finalLoc = finalLoc.get(sField.field.category);
					if(sField.field.subCategory && finalLoc.containsKey(sField.field.subCategory)) {
						finalLoc = finalLoc.get(sField.field.subCategory);
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
			}
		}

		//remove empty information hierarchy
		Map newMap = new LinkedHashMap();
		for(concept in map) {
			//log.debug "Concept : "+concept
			Map newConceptMap = new LinkedHashMap();
			if(hasContent(concept.value.get('speciesFieldInstance'))) {
				newConceptMap.put('speciesFieldInstance', sortAsPerRating(concept.value.get('speciesFieldInstance')));
			}
			for(category in concept.value) {
				Map newCategoryMap = new LinkedHashMap();
				//				log.debug "Category : "+category
				if(category.key.equals("field") || category.key.equals("speciesFieldInstance") || category.key.equalsIgnoreCase('Species Resources'))  {
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
						newConceptMap.put(category.key, category.value);
					}
				} else if(hasContent(category.value.get('speciesFieldInstance'))) {
					newCategoryMap.put('speciesFieldInstance',  sortAsPerRating(category.value.get('speciesFieldInstance')));
				}
				for(subCategory in category.value) {

					//					log.debug "subCategory : "+subCategory;
					if(subCategory.key.equals("field") || subCategory.key.equals("speciesFieldInstance")) continue;

					if((subCategory.key.equals(config.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.globalDistributionEntities.size()>0)  ||
					(subCategory.key.equals(config.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.globalEndemicityEntities.size()>0)||
					(subCategory.key.equals(config.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.indianDistributionEntities.size()>0) ||
					(subCategory.key.equals(config.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.indianEndemicityEntities.size()>0)||
					hasContent(subCategory.value.get('speciesFieldInstance'))) {
						//						log.debug "Putting distribution entities ${subCategory.key}"
                        sortAsPerRating(subCategory.value.get('speciesFieldInstance'));
						newCategoryMap.put(subCategory.key,  subCategory.value)
					}
				}
				//log.debug 'NSC : '+newCategoryMap

				if(newCategoryMap.size() != 0) {
					newConceptMap.put(category.key, newCategoryMap)
				}
				//log.debug "NC : "+newConceptMap;
			}
			if(newConceptMap.size() != 0) {
				newMap.put(concept.key, newConceptMap)
			}
		}
		//log.debug newMap;
		return newMap;
		//return map;
	}

	private boolean hasContent(speciesFieldInstances) {
		for(speciesFieldInstance in speciesFieldInstances) {
			if(speciesFieldInstance.description) {
				return true
			}
		}
		return false;
	}

    private sortAsPerRating(List fields) {
        if(!fields) return;
        fields.sort( { a, b -> b.averageRating <=> a.averageRating } as Comparator )
    }

	@Secured(['ROLE_USER'])
	def edit = {
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

	@Secured(['ROLE_SPECIES_ADMIN'])
	def update = {
		if(!params.name || !params.pk) {
			render ([success:false, msg:'Either field name or field id is missing'] as JSON)
			return;
		}

		def result;
		long speciesFieldId = params.pk ? params.long('pk'):null;
		def value = params.value;

		switch(params.name) {
			case "contributor":
				long cid = params.cid?params.long('cid'):null;
				result = speciesService.updateContributor(cid, speciesFieldId, value, params.name);
				break;
			case "attributor":
				long cid = params.cid?params.long('cid'):null;
				result = speciesService.updateContributor(cid, speciesFieldId, value, params.name);
				break;
			case "description":
				result = speciesService.updateDescription(speciesFieldId, value);
				break;
		}

		render result as JSON
	}

	@Secured(['ROLE_SPECIES_ADMIN'])
	def addResource = {
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
	def delete = {
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

	def count = {
		//cache "search_results"
		render Species.count();
	}

	def countSpeciesWithRichness = {
		//cache "search_results"
		render Species.countByPercentOfInfoGreaterThan(0);
	}

	def taxonBrowser = {
		render (view:"taxonBrowser");
	}

	def contribute = {
		render (view:"contribute");
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search = {
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
	def terms = {
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

	@Secured(['ROLE_SPECIES_ADMIN'])
	def upload = {
        /*List contributors;
        if(!params.contributorIds) {
			contributors = Utils.getUsersList(params.contributorIds);
        } else {
            contributors << springSecurityService.currentUser
        }
        */

        if(params.uFile) {
            String contentRootDir = grailsApplication.config.speciesPortal.content.rootDir
            File speciesDataFile = new File(contentRootDir, params.uFile.path[0])
            if(/*contributors && */speciesDataFile.exists()) {
                if(params.uFile.path[1]) {
                    File mappingFile = new File(contentRootDir, params.uFile.path[1])
                    speciesUploadService.uploadMappedSpreadsheet(speciesDataFile.getAbsolutePath(), mappingFile.getAbsolutePath(), 0,0,0,0,params.imagesDir?1:-1, params.imagesDir);
					render "Done mapped species upload"
                } else {
					//grailsApplication.config.speciesPortal.images.uploadDir = params.imagesDir
                    speciesUploadService.uploadNewSimpleSpreadsheet(speciesDataFile.getAbsolutePath(), params.imagesDir);
					render "Done simple species upload"
                }
            }
        }

	}
	
	@Secured(['ROLE_ADMIN'])
	def requestExport = {
		log.debug "Export of species requested" + params
		speciesService.requestExport(params)
		def r = [:]
		r['msg']= "${message(code: 'species.download.requsted', default: 'Processing... You will be notified by email when it is completed. Login and check your user profile for download link.')}"
		render r as JSON
	}
}
