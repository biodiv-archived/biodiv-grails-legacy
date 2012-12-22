package species

import java.sql.ResultSet;

import species.TaxonomyDefinition.TaxonomyRank;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
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

import species.utils.Utils;
import grails.plugins.springsecurity.Secured

class SpeciesController {

	def dataSource
	def grailsApplication
	def speciesSearchService;
	def namesIndexerService;
	def speciesService;

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		log.debug params
		
		def model = getSpeciesList(params);
		if(params.loadMore?.toBoolean()){
			render(template:"/species/showSpeciesListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/species/showSpeciesListTemplate", model:model);
			model.resultType = "species page"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
			
			render (result as JSON)
			return;
		}
	}
	
	private def getSpeciesList(params) {
		//cache "taxonomy_results"
		params.startsWith = params.startsWith?:"A-Z"
		def allGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL);
		params.sGroup = params.sGroup ?: allGroup.id+""
		params.max = Math.min(params.max ? params.int('max') : 40, 100);
		params.offset = params.offset ? params.int('offset') : 0
		params.sort = params.sort?:"percentOfInfo"
		params.order = params.sort.equals("percentOfInfo")?"desc":params.sort.equals("title")?"asc":"asc"

		log.debug params
		def groupIds = params.sGroup.tokenize(',')?.collect {Long.parseLong(it)}

		int count = 0;
		if (params.startsWith && params.sGroup) {
			String query, countQuery;

			if(groupIds.size() == 1 && groupIds[0] == allGroup.id) {
				if(params.startsWith == "A-Z") {
					query = "select s from Species s order by s.${params.sort} ${params.order}";
					countQuery = "select count(*) as count from Species s"
				} else {
					query = "select s from Species s where s.title like '<i>${params.startsWith}%' order by s.${params.sort} ${params.order}";
					countQuery = "select count(*) as count from Species s where s.title like '<i>${params.startsWith}%'"
				}
			} else {
				if(params.startsWith == "A-Z") {
					query = "select s from Species s, TaxonomyDefinition t where s.taxonConcept = t and t.group.id  in (:sGroup) order by s.${params.sort} ${params.order}"
					countQuery = "select count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t and t.group.id  in (:sGroup)";
				} else {
					query = "select s from Species s, TaxonomyDefinition t where title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup) order by s.${params.sort} ${params.order}"
					countQuery = "select count(*) as count from Species s, TaxonomyDefinition t where s.title like '<i>${params.startsWith}%' and s.taxonConcept = t and t.group.id  in (:sGroup)";
				}
			}

			def speciesInstanceList;
			if(groupIds.size() == 1 && groupIds[0] == allGroup.id) {
				speciesInstanceList = Species.executeQuery(query, [max:params.max, offset:params.offset]);
				def rs = Species.executeQuery(countQuery)
				count = rs[0];
			} else {
				speciesInstanceList = Species.executeQuery(query, [sGroup:groupIds], [max:params.max, offset:params.offset]);
				def rs = Species.executeQuery(countQuery,[sGroup:groupIds]);
				count = rs[0];
			}
			return [speciesInstanceList: speciesInstanceList, instanceTotal: count, 'userGroupWebaddress':params.webaddress]
		} else {
			//Not being used for now
			params.max = Math.min(params.max ? params.int('max') : 40, 100)
			return [speciesInstanceList: Species.list(params), instanceTotal: Species.count(),  'userGroupWebaddress':params.webaddress]
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
		def speciesInstance = Species.get(params.id)
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
			[speciesInstance: speciesInstance, fields:map]
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
				finalLoc.put('speciesFieldInstance', sField);
				/*
				 def sfList;
				 if(!(sfList = finalLoc.get('speciesFieldInstance'))) {
				 sfList = new ArrayList(); 
				 } 
				 sfList.add(sField);
				 finalLoc.put('speciesFieldInstance', sfList);
				 */
			}
		}

		//remove empty information hierarchy
		Map newMap = new LinkedHashMap();
		for(concept in map) {
			//log.debug "Concept : "+concept
			Map newConceptMap = new LinkedHashMap();
			if(hasContent(concept.value.get('speciesFieldInstance'))) {
				newConceptMap.put('speciesFieldInstance', concept.value.get('speciesFieldInstance'));
			}
			for(category in concept.value) {
				Map newCategoryMap = new LinkedHashMap();
				//log.debug "Category : "+category
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
					newCategoryMap.put('speciesFieldInstance', category.value.get('speciesFieldInstance'));
				}
				for(subCategory in category.value) {

					//log.debug "subCategory : "+subCategory;
					if(subCategory.key.equals("field") || subCategory.key.equals("speciesFieldInstance")) continue;

					if((subCategory.key.equals(config.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.globalDistributionEntities.size()>0)  ||
					(subCategory.key.equals(config.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.globalEndemicityEntities.size()>0)||
					(subCategory.key.equals(config.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY) && speciesInstance.indianDistributionEntities.size()>0) ||
					(subCategory.key.equals(config.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY) && speciesInstance.indianEndemicityEntities.size()>0)||
					hasContent(subCategory.value.get('speciesFieldInstance'))) {

						newCategoryMap.put(subCategory.key, subCategory.value)
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

	@Secured(['ROLE_USER'])
	def edit = {
		if(params.id) {
			def speciesInstance = Species.get(params.id)
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
	def update = {
		def speciesInstance = Species.get(params.id)
		if (speciesInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (speciesInstance.version > version) {

					speciesInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'species.label', default: 'Species')]
					as Object[], "Another user has updated this Species while you were editing")
					render(view: "edit", model: [speciesInstance: speciesInstance])
					return
				}
			}
			speciesInstance.properties = params
			if (!speciesInstance.hasErrors() && speciesInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'species.label', default: 'Species'), speciesInstance.id])}"
				redirect(action: "show", id: speciesInstance.id)
			}
			else {
				render(view: "edit", model: [speciesInstance: speciesInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
			redirect(action: "list")
		}
	}

	@Secured(['ROLE_ADMIN'])
	def delete = {
		def speciesInstance = Species.get(params.id)
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
		log.debug params;
		def model = speciesService.search(params)
		model['isSearch'] = true;
		
		if(params.loadMore?.toBoolean()){
			render(template:"/species/searchResultsTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"search", model:model)
			return;
		} else {
			def obvListHtml =  g.render(template:"/species/searchResultsTemplate", model:model);
			model.resultType = "species page"
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
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		List result = speciesService.nameTerms(params)
		render result.value as JSON;
	}
}
