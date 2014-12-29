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
import org.springframework.web.servlet.support.RequestContextUtils as RCU;

class SpeciesController extends AbstractObjectController {

	def dataSource
	def speciesSearchService;
	def namesIndexerService;
    def speciesUploadService;
	def speciesService;
	def speciesPermissionService;
	def userGroupService;
	def springSecurityService;
    def taxonService;
    def activityFeedService;
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def messageSource;

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
        model.userLanguage = utilsService.getCurrentLanguage(request);
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
        Language languageInstance = utilsService.getCurrentLanguage(request);
        Map result = [userLanguage:languageInstance, errors:errors];

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
                result = speciesService.createSpecies(speciesName, rank, t, languageInstance);
                result.errors = result.errors ? ' : '+result.errors : '';
                if(!result.success) {
                    if(result.status == 'requirePermission') {
                        def tmp_var   = params.id?speciesName+(params.id):''
                        flash.message = "${message(code: 'species.contribute.not.permitted.message', args: ['contribute to', message(code: 'species.label', default: 'Species'), tmp_var])}"
                        //flash.message = "Sorry, you don't have permission to contribute to this species ${params.id?speciesName+(params.id):''}. Please request for permission below."

                        def url = uGroup.createLink(controller:"species", action:"contribute");
                        redirect url: url
                        return;
                    } else {
                    flash.message = result.msg ? result.msg+result.errors:${message(code: 'default.species.error.Create',null)}+result.errors
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
                            flash.message =  messageSource.getMessage("default.species.success.Create", null, RCU.getLocale(request))
                            speciesUploadService.postProcessSpecies([speciesInstance]);
                        //}
                        
                        def feedInstance = activityFeedService.addActivityFeed(speciesInstance, null, springSecurityService.currentUser, activityFeedService.SPECIES_CREATED);

                        utilsService.sendNotificationMail(activityFeedService.SPECIES_CREATED, speciesInstance, request, params.webaddress, feedInstance, ['info':activityFeedService.SPECIES_CREATED]);


                        redirect(action: "show", id: speciesInstance.id, params:['editMode':true])
                        return;
                    } else {
                        flash.message = result.msg ? result.msg + result.errors : messageSource.getMessage("default.species.error.species", null, RCU.getLocale(request)) +" "+ result.errors
                    }
                }
                else {
                    flash.message = result.msg ? result.msg + result.errors : messageSource.getMessage("default.species.error.species", null, RCU.getLocale(request)) +" "+ result.errors
                }
            } catch(e) {
                e.printStackTrace();
                result.errors << e.getMessage();
                flash.message = result.msg ? result.msg+result.errors : messageSource.getMessage("default.species.error.species", null, RCU.getLocale(request)) +" "+ result.errors
            }
        }
        render(view: "create", model:result)
    }

	def show() {
		//cache "content"
        params.id = params.long('id');

		def speciesInstance = params.id ? Species.get(params.id):null;
		if (!params.id || !speciesInstance) {
            if(request.getHeader('X-Auth-Token')) {
                render (['success':false, 'msg':"Coudn't find species with id ${params.id}"] as JSON)
                return
            } else {
			    flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'species.label', default: 'Species'), params.id])}"
			    redirect(action: "list")
            }
		}
		else {
            if(params.editMode) {
                if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
                	def tmp_var   = params.id?speciesInstance.title+' ( '+params.id+' )':''
			        flash.message = "${message(code: 'species.contribute.not.permitted.message', args: ['contribute to', message(code: 'species.label', default: 'Species'), tmp_var])}"
                   // flash.message = "Sorry, you don't have permission to contribute to this species ${}. Please request for permission below."
                    if(request.getHeader('X-Auth-Token')) {
                        render (['success':false, 'msg':flash.message] as JSON)
                        return
                    } else {
                        def url = uGroup.createLink(controller:"species", action:"contribute");
                        redirect url: url
            		    return;
                    }
                }
            }
            
            if(request.getHeader('X-Auth-Token')) {
                render speciesInstance as JSON;
                return;
            } 

            def userLanguage = utilsService.getCurrentLanguage(request);
			def c = Field.createCriteria();
			def fields = c.list(){
				eq('language', userLanguage)
				and{ 
                        order('displayOrder','asc')
					 
					}
			};

			Map map;
            
            utilsService.benchmark('getTreeMap') {
                map = getTreeMap(speciesInstance, fields, userLanguage);
            }
            def converter = new XMLConverter()
            Map fieldFromName = [                
                summary : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.SUMMARY,2,userLanguage),
                occurrenceRecords : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS,2,userLanguage),
                references : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.REFERENCES,2,userLanguage),
                brief : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.BRIEF,2,userLanguage),
                gdge : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY,3,userLanguage),
                gege : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY,3,userLanguage) ,
                idge : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY,3,userLanguage), 
                iege : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY,3,userLanguage),
                tri  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.TAXONRECORDID,1,userLanguage),
                gui  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBALUNIQUEIDENTIFIER,1,userLanguage),
                nc  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION,1,userLanguage),
                md  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.META_DATA,1,userLanguage),
                overview  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.OVERVIEW,1,userLanguage),
                acth  : grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY,
                trn: converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME,1,userLanguage),
                sn: converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME,1,userLanguage),
                sn3: converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME,3,userLanguage),
                gsn3:converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME,3,userLanguage)
            ]

            utilsService.benchmark('mapSpeciesInstanceFields') {
			    map = mapSpeciesInstanceFields(speciesInstance, speciesInstance.fields, map.map, map.fieldsConnectionArray,fieldFromName);
            }

           
			//def relatedObservations = observationService.getRelatedObservationByTaxonConcept(speciesInstance.taxonConcept.id, 1,0);
			//def observationInstanceList = relatedObservations?.observations?.observation
			//def instanceTotal = relatedObservations?relatedObservations.count:0
			
			def result = [speciesInstance: speciesInstance, fields:map, totalObservationInstanceList:[:], queryParams:[max:1, offset:0], 'userGroupWebaddress':params.webaddress, 'userLanguage': userLanguage,fieldFromName:fieldFromName]

             if(springSecurityService.currentUser) {
                SpeciesField newSpeciesFieldInstance = speciesService.createNewSpeciesField(speciesInstance, fields[0], '');
                result['newSpeciesFieldInstance'] = newSpeciesFieldInstance
            }
            return result
		}
	}

	private Map getTreeMap(Species speciesInstance, List fields, Language userLanguage) {
        def user = springSecurityService.currentUser;

		Map map = new LinkedHashMap();
		ArrayList fieldsConnectionArray = new ArrayList(fields.size());
        boolean isSpeciesContributor = speciesPermissionService.isSpeciesContributor(speciesInstance, user)
        ArrayList fieldsArray = new ArrayList(fields.size());
		for(Field field : fields) {
			Map finalLoc;
			Map conceptMap, categoryMap, subCategoryMap;
			if(field.concept && !field.concept.equals("")) {
				if(map.containsKey(field.concept)) {

                   // println "=======language========"+userLanguage.id;
                    //println "=======fieldId========="+field.language.id;
                    //println "=======fieldConnection========="+field.connection;
                    //println "=======field.concept========="+field.concept;
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
				fieldsConnectionArray.putAt(field.connection, finalLoc);
                if(user && isSpeciesContributor) {
                    finalLoc.put('isContributor', 1);
                }
			}
		}

		return [map:map, fieldsConnectionArray:fieldsConnectionArray];
	}

	private Map mapSpeciesInstanceFields(Species speciesInstance, Collection speciesFields, Map map, ArrayList fieldsConnectionArray,Map config) {
		
		//def config = grailsApplication.config.speciesPortal.fields
        SUser user = springSecurityService.currentUser;

        utilsService.benchmark('grouping sFields') {
		for (SpeciesField sField : speciesFields) {
			Map finalLoc;
            Language lang;
            //concept
           // println "species fields ============"+sField.language;
			if(map.containsKey(sField.field.concept) || fieldsConnectionArray[sField.field.connection]) {

				finalLoc = map.get(sField.field.concept)?:fieldsConnectionArray[sField.field.connection];

              //  println "===============finalLoc===================="+finalLoc;

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
                finalLoc.put("lang", sField.language);
			}
			if(finalLoc && finalLoc.containsKey('field')) { 
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
        }


        utilsService.benchmark('remove empty info hierarchy') {
        //remove empty information hierarchy
		for(concept in map.clone()) {
            if(concept.value.get('speciesFieldInstance')) {
                speciesService.sortAsPerRating(map.get(concept.key).get('speciesFieldInstance'));
			}
			for(category in concept.value.clone()) {
				if(category.key.equals("field") || category.key.equals("speciesFieldInstance") ||category.key.equals("hasContent") ||category.key.equals("isContributor") || category.key.equals("lang") || category.key.equalsIgnoreCase('Species Resources'))  {
					continue;
				} else if(category.key.equals(config.occurrenceRecords) || category.key.equals(config.references) ) {
					boolean show = false;
					if(category.key.equals(config.references)) {
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
					if(subCategory.key.equals("field") || subCategory.key.equals("speciesFieldInstance") || subCategory.key.equals('hasContent') ||subCategory.key.equals("isContributor") || subCategory.key.equals("lang") ) continue;

					if((subCategory.key.equals(config.gdge) && speciesInstance.globalDistributionEntities.size()>0)  ||
					(subCategory.key.equals(config.gege) && speciesInstance.globalEndemicityEntities.size()>0)||
					(subCategory.key.equals(config.idge) && speciesInstance.indianDistributionEntities.size()>0) ||
					(subCategory.key.equals(config.iege) && speciesInstance.indianEndemicityEntities.size()>0)) {

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

	/*@Secured(['ROLE_USER'])
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
	}*/

	@Secured(['ROLE_USER'])
    def update() {
    	def msg;
        def userLanguage;
        def paramsForObvSpField = params.paramsForObvSpField?JSON.parse(params.paramsForObvSpField):null
        def paramsForUploadSpField =  params.paramsForUploadSpField?JSON.parse(params.paramsForUploadSpField):null
        
        if(!(params.name && params.pk)) {
        	msg=messageSource.getMessage("default.species.error.fieldOrname", null, RCU.getLocale(request))
            render ([success:false, msg:msg] as JSON)
            return;
        }
        try {
            def result;
            long speciesFieldId = params.pk ? params.long('pk'):null;
            def value = params.value;
            userLanguage = utilsService.getCurrentLanguage(request);
            params.locale_language = userLanguage;

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
                Long sid = params.sid?params.long('sid'):null;
                String relationship = params.relationship?:null;

                if(params.act == 'delete') {
                    result = speciesService.deleteSynonym(sid, speciesFieldId);
                } else {
                    result = speciesService.updateSynonym(sid, speciesFieldId, relationship, value);
                }
                break;
                case 'commonname':
                Long cid = params.cid?params.long('cid'):null;
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
                println ")))))))"
                println result.content
                result.content.each {sf ->
                    boolean isSpeciesFieldContributor = speciesPermissionService.isSpeciesFieldContributor(sf, springSecurityService.currentUser);
                    html << g.render(template:'/common/speciesFieldTemplate', model:['speciesInstance':sf.species, 'speciesFieldInstance':sf, 'speciesId':sf.species.id, 'fieldInstance':sf.field, 'isSpeciesFieldContributor':isSpeciesFieldContributor,'userLanguage':userLanguage]);
                }
                result.content = html.join();
                
                break;
                default :
                msg=messageSource.getMessage("default.species.incorrect.datatype", null, RCU.getLocale(request))
                result=['success':false, msg:msg];
            }
 
            if(result.success) {
                def feedInstance;
                if(result.activityType)
                    feedInstance = activityFeedService.addActivityFeed(result.speciesInstance, result.speciesFieldInstance, springSecurityService.currentUser, result.activityType);
                if(result.mailType) {
                    def otherParams = ['info':result.activityType]
                    def spIns = result.speciesFieldInstance
                    if(spIns) {
                        def des = spIns.description
                        des = des.replaceAll("<(.|\n)*?>", '');
                        des = des.replaceAll("&nbsp;", '');
                        if(des.length() > 150) {
                            otherParams['spFDes'] = des[0..147] + "...";
                        } else {
                            otherParams['spFDes'] = des
                        }
                    }
                    utilsService.sendNotificationMail(result.mailType, result.speciesInstance, request, params.webaddress, feedInstance, otherParams);
                }
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
		def msg;
		if(!params.id) {
			msg=messageSource.getMessage("default.species.id.missing", null, RCU.getLocale(request))
			render ([success:false, errors:[msg:msg]] as JSON)
			return;
		}

		def result;
		long speciesInstanceId = params.long('id');
		def speciesInstance = Species.get(speciesInstanceId);

		if(!speciesInstance) {
			msg=messageSource.getMessage("default.species.id.notFound", null, RCU.getLocale(request))
			render ([success:false, errors:[msg:msg]] as JSON)
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
				msg=messageSource.getMessage("default.species.no.resource", null, RCU.getLocale(request))
				render ([success:false, errors:[msg:msg]] as JSON)
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
					msg = messageSource.getMessage("default.species.error.message", null, RCU.getLocale(request))
					render ([success:false, errors:[msg:msg]]) as JSON
					return;
				}


			}
		} catch(e) {
			msg = messageSource.getMessage("default.species.error.message.add", null, RCU.getLocale(request))
			render ([success:false, errors:[msg:msg]] as JSON)
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
	/*def search() {
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
	}*/



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
            Language languageInstance = utilsService.getCurrentLanguage(request);
            params.locale_language = languageInstance;
            log.debug  "Choosen languauge is ${languageInstance}"
			def res = speciesUploadService.basicUploadValidation(params)
			log.debug "Starting bulk upload"
			res = speciesUploadService.upload(res.sBulkUploadEntry)
			render(text:res as JSON, contentType:'text/html')
		}
	}
	
	def getDataColumns() {
        Language languageInstance = utilsService.getCurrentLanguage(request);

        List res = speciesUploadService.getDataColumns(languageInstance);
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
        	def msg = messageSource.getMessage("default.species.error.request", null, RCU.getLocale(request))
            render (['success':false, 'statusComplete':false, 'shortMsg':msg, 'msg':messageSource.getMessage("default.species.info.selectNode",null,RCU.getLocale(request))] as JSON)
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
                utilsService.sendNotificationMail(utilsService.NEW_SPECIES_PERMISSION, taxonConcept, null, null, null, ['permissionType':invitetype, 'taxonConcept':taxonConcept, 'user':user]);
                def conf = PendingEmailConfirmation.findByConfirmationToken(params.confirmationToken);
                if(conf) {
                    log.debug "Deleting confirmation code and usertoken params";
                    conf.delete();
                    UserToken.get(params.tokenId.toLong())?.delete();
                }
                flash.message=messageSource.getMessage("default.species.success.added.userInviteTaxon", [user,invitetype,taxonConcept.name] as Object[], RCU.getLocale(request))
            } else {
                flash.error=messageSource.getMessage("default.species.error.added.userInviteTaxon", [user,invitetype,taxonConcept.name] as Object[], RCU.getLocale(request))            
            }
        }else{
            flash.error=messageSource.getMessage("default.species.error.added.userInviteTaxon", [params.userId, params.invitetype, params.taxonConcept] as Object[], RCU.getLocale(request))           
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
        	def msg = messageSource.getMessage("default.species.error.request", null, RCU.getLocale(request))
            render (['success':false, 'statusComplete':false, 'shortMsg':msg, 'msg':messageSource.getMessage("default.species.info.selectNode",null,RCU.getLocale(request))] as JSON)
         }
		return
    } 

    @Secured(['ROLE_USER', 'RUN_AS_ADMIN'])
    def confirmPermissionInviteRequest() {
        if(params.userId && params.taxonConcept){
            SUser user = SUser.get(params.userId.toLong())
            TaxonomyDefinition taxonConcept = TaxonomyDefinition.get(params.taxonConcept.toLong())
            String invitetype = params.invitetype;
            boolean success = false
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
                def conf = PendingEmailConfirmation.findByConfirmationToken(params.confirmationToken);
                if(conf) {
                    log.debug "Deleting confirmation code and usertoken params";
                    conf.delete();
                    UserToken.get(params.tokenId.toLong())?.delete();
                }

                flash.message=messageSource.getMessage("default.species.success.added.userInviteTaxon", [user,invitetype,taxonConcept.name] as Object[], RCU.getLocale(request))
                utilsService.sendNotificationMail(utilsService.NEW_SPECIES_PERMISSION, taxonConcept, null, null, null, ['permissionType':invitetype, 'taxonConcept':taxonConcept, 'user':user]);
            } else{
                flash.error=messageSource.getMessage("default.species.error.added.userInviteTaxon", [user,invitetype,taxonConcept.name] as Object[], RCU.getLocale(request))            
            }
        } else{
            flash.error=messageSource.getMessage("default.species.error.added.userInviteTaxon", [user,invitetype,taxonConcept.name] as Object[], RCU.getLocale(request))           
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
    
    //ADDS/EDITS/DELETES media in species
    @Secured(['ROLE_USER'])
    def setResources() {
        log.debug params
        //pass that same species
        Language userLanguage = utilsService.getCurrentLanguage(request);
        params.locale_language = userLanguage;
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
    def getRelatedObvForSpecies() {
        if(!params.speciesId) {
            log.error "NO SPECIES ID TO FETCH RELATED OBV";
            def result = [addPhotoHtml: '', relatedObvCount: 0]
            render result as JSON
        }
        params.offset = (params.offset)?params.offset:0
        def spInstance = Species.read(params.speciesId.toLong())
        def relatedObvMap = observationService.getRelatedObvForSpecies(spInstance, 4, params.offset.toInteger())
        def relatedObv = relatedObvMap.resList
        def relatedObvCount = relatedObvMap.count
        def obvLinkList = relatedObvMap.obvLinkList
        def addPhotoHtml = g.render(template:"/observation/addPhoto", model:[observationInstance: spInstance, resList: relatedObv, obvLinkList: obvLinkList, resourceListType: params.resourceListType, offset:(params.offset.toInteger() + relatedObvCount)]);
        def result = [addPhotoHtml: addPhotoHtml, relatedObvCount: relatedObvCount]
        render result as JSON
    }
/*
    @Secured(['ROLE_USER'])
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

    @Secured(['ROLE_USER'])
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
*/
    @Secured(['ROLE_USER'])
    def validate() {
/*        List hierarchy = [];
        if(params.taxonRegistry) {
            hierarchy = taxonService.getTaxonHierarchyList(params.taxonRegistry);
        }
*/
		def msg;
        def result = [requestParams:[taxonRegistry:params.taxonRegistry?:[:]]];
        if(params.page && params.rank) {
            try {
                int rank = params.rank?params.int('rank'):null;
                Map r = getTaxon(params.page, rank);
                if(r.success) {
                    TaxonomyDefinition taxon = r.taxon;
                    if(!taxon) {
                    	msg = messageSource.getMessage("default.species.error.NameValidate.message", null, RCU.getLocale(request))
                        result = ['success':true, 'msg':msg, rank:rank, requestParams:[taxonRegistry:params.taxonRegistry]]
                    } else {
                        //CHK if a species page exists for this concept
                        Species species = Species.findByTaxonConcept(taxon);
                        def taxonRegistry = taxon.parentTaxonRegistry();
                        if(species) {
                        	msg = messageSource.getMessage("default.species.error.already", null, RCU.getLocale(request))
                            result = ['success':true, 'msg':msg, id:species.id, name:species.title, rank:taxon.rank, requestParams:[taxonRegistry:params.taxonRegistry]];
                        } else {
                        	msg = messageSource.getMessage("default.species.addExisting.taxon", null, RCU.getLocale(request))
                            result = ['success':true, 'msg':msg, rank:taxon.rank, requestParams:[taxonRegistry:params.taxonRegistry]];
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
                msg = messageSource.getMessage("default.species.error.validate", null, RCU.getLocale(request))
                result = ['success':false, 'msg':msg, requestParams:[taxonRegistry:params.taxonRegistry]]
            }

        } else {
        	msg = messageSource.getMessage("default.species.not.validName",  [' '] as Object[], RCU.getLocale(request))
            result = ['success':false, 'msg':msg, requestParams:[taxonRegistry:params.taxonRegistry]]
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
            	msg = messageSource.getMessage("default.species.not.validName", [name] as Object[], RCU.getLocale(request))
                result = ['success':false, 'msg':msg]
            } else {
                result = ['success':true, 'taxon':taxon];        
            }
        } else {
        	msg = messageSource.getMessage("default.species.not.validName", [name] as Object[], RCU.getLocale(request))
            result = ['success':false, 'msg':msg]
        }
        return result;
    }

    def saveModifiedSpeciesFile () {
        Language userLanguage = utilsService.getCurrentLanguage(request);
        params.locale_language = userLanguage;
        File file = speciesUploadService.saveModifiedSpeciesFile(params);
        if(!file) {    
            return render(text: [success:false, downloadFile: ""] as JSON, contentType:'text/html')
        }
        return render(text: [success:true, downloadFile: file.getAbsolutePath()] as JSON, contentType:'text/html')
        /*
        if (f.exists()) {
            println "here here===================="
            //log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${f.name}")
            response.outputStream << f.readBytes()
            response.outputStream.flush()
            println "==YAHAN HUN == " 
            return render(text: [success:true] as JSON, contentType:'text/html')
        } else {
            println "in else================"
            def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], RCU.getLocale(request))
            log.error msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }
        */
    }
    
    @Secured(['ROLE_USER'])
    def getSpeciesFieldMedia() {
        def resList = []
        def obvLinkList = []
        def resCount = 0
        def offset = 0
        def result
        if(params.speciesId){
            def spInstance = Species.read(params.speciesId?.toLong())
            resList = speciesService.getSpeciesFieldMedia(params.spFieldId)
            def addPhotoHtml = g.render(template:"/observation/addPhoto", model:[observationInstance: spInstance, resList: resList, resourceListType: params.resourceListType, obvLinkList:obvLinkList, resCount:resCount, offset:offset]);
            result = [statusComplete:true, addPhotoHtml: addPhotoHtml]
        } else {
            log.debug params  
            result = [statusComplete:false]
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def pullObvMediaInSpField(){
        log.debug params  
        //pass that same species
        Language userLanguage = utilsService.getCurrentLanguage(request);
        params.locale_language = userLanguage; 
        def speciesField = SpeciesField.get(params.speciesFieldId.toLong())
        def out = speciesService.updateSpecies(params, speciesField)
        def result
        if(out){
            result = ['success' : true]
        }
        else{
            result = ['success'  : false]
        }    
    }

    @Secured(['ROLE_USER'])
    def uploadMediaInSpField(){
        Language userLanguage = utilsService.getCurrentLanguage(request);
        params.locale_language = userLanguage;
        def speciesField = SpeciesField.get(params.speciesFieldId.toLong())
        def out = speciesService.updateSpecies(params, speciesField)
        def result
        if(out){
            result = ['success' : true]
        }
        else{
            result = ['success'  : false]
        }    

    }
}
