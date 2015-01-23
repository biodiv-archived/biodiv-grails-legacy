package species.participation

import java.util.List;
import java.util.Map;

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder;
import groovy.xml.StreamingMarkupBuilder;
import groovy.xml.XmlUtil;
import org.codehaus.groovy.grails.web.json.JSONObject;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import grails.plugin.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.converters.XML;

import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.Flag.FlagType
import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.Habitat;
import species.Species;
import species.Resource;
import species.BlockedMails;
import species.Resource.ResourceType;
import species.auth.SUser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import species.participation.Featured
import species.AbstractObjectController;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import species.TaxonomyDefinition.TaxonomyRank;
import species.participation.ActivityFeedService;
import static org.springframework.http.HttpStatus.*;

class ObservationController extends AbstractObjectController {
	
	public static final boolean COMMIT = true;

	//def observationService; //injected in AbstractObjectController
	def springSecurityService;
	def mailService;
	def observationsSearchService;
	def namesIndexerService;
	def userGroupService;
	def activityFeedService;
	def obvUtilService;
    def chartService;
    def messageSource;
    def commentService;
    def speciesService;
    def setupService;
    def springSecurityFilterChain
 
	static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"]]
    static defaultAction = "list"

	def index = {
		redirect(action: "list", params: params)
	}

	def filteredMapBasedObservationsList = {
		def model;
		//TODO: Dirty hack to feed results through solr if the request is from search
		if(params.action == 'search') {
			model = observationService.getObservationsFromSearch(params)
		} else {
			 model = getObservationList(params);
		}
		if(params.loadMore?.toBoolean()){
			render(template:"/common/observation/showObservationListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
			def tagsHtml = "";
			if(model.showTags) {
//				def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
//				tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			}
//			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);
			
			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, instanceTotal:model.instanceTotal]
			render result as JSON
			return;
		}
	}

	def list() {
		
		def model = runLastListQuery(params);
        withFormat {
            html {
                model.resultType = 'observation'
                def userLanguage = utilsService.getCurrentLanguage(request); 
                if(params.loadMore?.toBoolean()){
                    render(template:"/common/observation/showObservationListTemplate", model:model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    model['width'] = 300;
                    model['height'] = 200;
                    model['userLanguage'] = userLanguage;
                    render (view:"list", model:model)
                    return;
                } else {
                    model['userGroupInstance'] = UserGroup.findByWebaddress(params.webaddress);
                    def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
                    def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
                    def tagsHtml = "";
                    if(model.showTags) {
        //				def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
        //				tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
                    }
        //			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);
        /*	        def chartModel = model.speciesGroupCountList
                    chartModel['width'] = 300;
                    chartModel['height'] = 270;
        */
                    
                    def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, instanceTotal:model.instanceTotal,userLanguage:userLanguage]

                    render result as JSON
                    return;
                }
            }
            json { render utilsService.getSuccessModel("", null, OK.value(), model) as JSON }
            xml { render utilsService.getSuccessModel("", null, OK.value(), model) as XML }
        }
	}

	def listJSON = {
		def model = getObservationList(params);
        model.queryParams.remove('userGroup');
		render model as JSON
	}

	protected def getObservationList(params) {
        try { 
            params.max = params.max?Integer.parseInt(params.max.toString()):24 
        } catch(NumberFormatException e) { 
            params.max = 24 
        }
        try { 
            params.offset = params.offset?Integer.parseInt(params.offset.toString()):0; 
        } catch(NumberFormatException e) { 
            params.offset = 0 
        }
        println "=========ID ID ================ " + params.id?.toLong()
        params['parentId'] = params.parentId?.toLong()
        params['maxNearByRadius'] = 200;
		def max = Math.min(params.max ? params.int('max') : 24, 100)
		def offset = params.offset ? params.int('offset') : 0
		def filteredObservation = observationService.getFilteredObservations(params, max, offset, false)
        println "=======FILTERED OBSERVATION LIST========== " + filteredObservation
		def observationInstanceList = filteredObservation.observationInstanceList
        
        //Because returning source Ids instead of actual obv ins
        if(params.filterProperty == 'speciesName') {
            def results = []
            def cklCount = 0;
            observationInstanceList.each {
                def obv = Observation.read(it);
                if(obv.isChecklist) {
                    cklCount += 1;
                }
                results.add(obv)
            }
            filteredObservation.observationInstanceList = results;
            observationInstanceList = results;
            filteredObservation.checklistCount = cklCount;
        }

		def queryParams = filteredObservation.queryParams
		def activeFilters = filteredObservation.activeFilters
		activeFilters.put("append", true);//needed for adding new page obv ids into existing session["obv_ids_list"]
		
//		def queryResult = observationService.getFilteredObservations(params, -1, -1, false)
//		def count = queryResult.observationInstanceList.size()
        def checklistCount =  filteredObservation.checklistCount
		def allObservationCount =  filteredObservation.allObservationCount
		
		//storing this filtered obvs ids list in session for next and prev links
		//http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/1.8.2/org/codehaus/groovy/runtime/DefaultGroovyMethods.java
		//returns an arraylist and invalidates prev listing result
        if(max != -1) {
            if(params.append?.toBoolean() && session["obv_ids_list"]) {
                session["obv_ids_list"].addAll(observationInstanceList.collect {
                    params.fetchField?it[0]:it.id
                }); 
            } else {
                session["obv_ids_list_params"] = params.clone();
                session["obv_ids_list"] = observationInstanceList.collect {
                    params.fetchField?it[0]:it.id
                };
            }
        }
		log.debug "Storing all observations ids list in session ${session['obv_ids_list']} for params ${params}";
		return [observationInstanceList: observationInstanceList, instanceTotal: allObservationCount, checklistCount:checklistCount, observationCount: allObservationCount-checklistCount, speciesGroupCountList:filteredObservation.speciesGroupCountList, queryParams: queryParams, activeFilters:activeFilters, resultType:'observation', geoPrivacyAdjust:Utils.getRandomFloat(), canPullResource:userGroupService.getResourcePullPermission(params)]
	}
	
	def occurrences() {
		def result = observationService.getObservationOccurences(params)
        def model = utilsService.getSuccessModel('', null, OK.value(), result);
        withFormat {
            json { render model as JSON }
            xml { render model as XML }
        }
	}

	@Secured(['ROLE_USER'])
	def create() {
		def observationInstance = new Observation()
		observationInstance.properties = params;
		def author = springSecurityService.currentUser;
		def lastCreatedObv = Observation.find("from Observation as obv where obv.author=:author and obv.isDeleted=:isDeleted order by obv.createdOn desc ",[author:author, isDeleted:false]);
		def filePickerSecurityCodes = utilsService.filePickerSecurityCodes();
        return [observationInstance: observationInstance, 'lastCreatedObv':lastCreatedObv, 'springSecurityService':springSecurityService, 'policy' : filePickerSecurityCodes.policy, 'signature': filePickerSecurityCodes.signature]
	}

	@Secured(['ROLE_USER'])
	def save() {
		if(request.method == 'POST') {
			//TODO:edit also calls here...handle that wrt other domain objects
			saveAndRender(params, false)
		} else {
			msg = "Method Not Allowed"
            def model = utilsService.getErrorModel(msg, null, METHOD_NOT_ALLOWED.value());
            withFormat {
                html {
                    flash.message = msg;
			        redirect (url:uGroup.createLink(action:'create', controller:"observation", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

    @Secured(['ROLE_USER'])
	def flagDeleted() {
		def result = observationService.delete(params)
        if(params.format?.equalsIgnoreCase("json")) {
            result.remove('url')
            render result as JSON;
            return;
        }
		flash.message = result.message
		redirect (url:result.url)
	}

	@Secured(['ROLE_USER'])
	def update() {
		def observationInstance = Observation.get(params.id?.toLong())
        def msg;
		if(observationInstance)	{
			saveAndRender(params, true)
		} else {
			msg = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
                    flash.message = msg;
			        redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }

		}
	}
	
	private saveAndRender(params, sendMail=true){
		params.locale_language = utilsService.getCurrentLanguage(request);
		def result = observationService.saveObservation(params, sendMail)
		if(result.success){
            //if(params.format?.equalsIgnoreCase("json") || params.format?.equalsIgnoreCase("xml") ) 
            //    params.isMobileApp = true;
			forward (action: 'addRecommendationVote', params:params);
		} else {
            withFormat {
                html {
                    //flash.message = "${message(code: 'error')}";
			        render(view: "create", model: [observationInstance: result.instance, lastCreatedObv:null])
                }
                json {
                    result.remove('instance');
                    render result as JSON 
                }
                xml {
                    result.remove('instance');
                    render result as XML
                }
            }
		}
	}

	def show() {
        params.id = params.long('id');
        def msg;
        if(params.id) {
			def observationInstance = Observation.findByIdAndIsDeleted(params.id, false)
			if (!observationInstance) {
                msg = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    html {
				        flash.message = model.msg;
				        redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
			else {
    			if(observationInstance.instanceOf(Checklists)){
                    msg = messageSource.getMessage("default.checklist.id", [params.id] as Object[], RCU.getLocale(request))
                    def model = utilsService.getErrorModel(msg, null, OK.value());
                    withFormat {
                        html {
					        redirect(controller:'checklist', action:'show', params: params)
                        } 
                        json { render model as JSON }
                        xml { render model as XML }
                    }
					return
				}

				observationInstance.incrementPageVisit()
				def userLanguage = utilsService.getCurrentLanguage(request);   
                int pos = params.pos?params.int('pos'):0;
				def prevNext = getPrevNextObservations(pos, params.webaddress);

                def model = utilsService.getSuccessModel("", observationInstance, OK.value());
                withFormat {
                    html {
                        if(prevNext) {
                            model = [observationInstance: observationInstance, prevObservationId:prevNext.prevObservationId, nextObservationId:prevNext.nextObservationId, lastListParams:prevNext.lastListParams,'userLanguage':userLanguage]
                        } else {
                            model = [observationInstance: observationInstance,'userLanguage':userLanguage]
                        }
                    } 
                    json  { render model as JSON }
                    xml { render model as JSON }
                }
			}
		} else {
            msg = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
            def model = utilsService.getErrorModel(msg, null, OK.value());
            withFormat {
                html {
			        redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
	}

	/**
	 * 
	 * @param pos
	 * @return
	 */
	def getPrevNextObservations(int pos, String userGroupWebaddress) {
		if(pos == null) pos = 0;
		String listKey = "obv_ids_list";
		String listParamsKey = "obv_ids_list_params"
		if(userGroupWebaddress) {
			listKey = userGroupWebaddress + listKey;
			listParamsKey = userGroupWebaddress + listParamsKey;
		}
		def lastListParams = session[listParamsKey]?.clone();
        
		if(lastListParams) {
			if(!session[listKey]) {
				log.debug "Fetching observations list as its not present in session "
				runLastListQuery(lastListParams);
			}
	
			long noOfObvs = session[listKey].size();
			
			log.debug "Current ids list in session ${session[listKey]} and position ${pos}";
			def nextObservationId = (pos+1 < session[listKey].size()) ? session[listKey][pos+1] : null;
			if(nextObservationId == null) {			
				lastListParams.put("append", true);
				def max = Math.min(lastListParams.max ? lastListParams.int('max') : 12, 100)
				def offset = lastListParams.offset ? lastListParams.int('offset') : 0
				lastListParams.offset = offset + session[listKey].size();
				log.debug "Fetching new page of observations using params ${lastListParams}";
				runLastListQuery(lastListParams);
				lastListParams.offset = offset;
				nextObservationId = (pos+1 < session[listKey].size()) ? session[listKey][pos+1] : null;
			}
			def prevObservationId = pos > 0 ? session[listKey][pos-1] : null;
			lastListParams.remove('isGalleryUpdate');
			lastListParams.remove("append");
			lastListParams['max'] = noOfObvs;
			lastListParams['offset'] = 0;
			return ['prevObservationId':prevObservationId, 'nextObservationId':nextObservationId, 'lastListParams':lastListParams];
		}
	}
	
	private def runLastListQuery(Map params) {
		if(params.webaddress) {
			def userGroupController = new UserGroupController();
			return userGroupController.getUserGroupObservationsList(params)
		} else if(params.action == 'search') {
			return observationService.getObservationsFromSearch(params);
		} else {
			return getObservationList(params);
		}
	}
	
	@Secured(['ROLE_USER'])
	def edit() {
		def observationInstance = Observation.findWhere(id:params.id?.toLong(), isDeleted:false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
			//redirect(action: "list")
		} else if(utilsService.ifOwns(observationInstance.author)) {
			render(view: "create", model: [observationInstance: observationInstance, 'springSecurityService':springSecurityService])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"observation", id:observationInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	@Secured(['ROLE_CEPF_ADMIN'])
	def delete() {
        if(!params.id) return;
		def observationInstance = Observation.get(params.id)
		if (observationInstance) {
			try {
				observationInstance.delete(flush: true)
				observationsSearchService.delete(observationInstance.id);
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
				//redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
				redirect (url:uGroup.createLink(action:'show', controller:"observation", id:params.id, 'userGroupWebaddress':params.webaddress))
				//redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
			//redirect(action: "list")
		}
	}

	@Secured(['ROLE_USER'])
	def upload_resource() {
		def message;
		def msg;
		if(params.ajax_login_error == "1") {
			msg = messageSource.getMessage("default.login.continue", null, RCU.getLocale(request))
            message = [status:401, error:msg]
			render message as JSON 
			return;
		} else if(!params.resources && !params.videoUrl) {
			message = g.message(code: 'no.file.attached', default:'No file is attached')
			response.setStatus(500)
			message = [error:message]
			render message as JSON
			return;
		}
		
		try {
				def rs = [:]
                if(ServletFileUpload.isMultipartContent(request)) {
                    MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                    Utils.populateHttpServletRequestParams(request, rs);
                }
				def resourcesInfo = [];
                String rootDir
                switch(params.resType) {
                    case [Observation.class.name,Checklists.class.name]:
                        rootDir = grailsApplication.config.speciesPortal.observations.rootDir
                    break;

                    case Species.class.name:
                        rootDir = grailsApplication.config.speciesPortal.resources.rootDir
                    break;

                    case SUser.class.name:
                        rootDir = grailsApplication.config.speciesPortal.usersResource.rootDir
                    break;
                }
                File obvDir 
				if(!params.resources && !params.videoUrl) {
					message = g.message(code: 'no.file.attached', default:'No file is attached')
				}
				
				if(params.resources instanceof String) {
						params.resources = [params.resources]
				}
				params.resources.each { f ->					
                    String mimetype,filename;
                    if(f instanceof String) {
                        f = JSON.parse(f);
                        if(f.size instanceof String) {
                            f.size = Integer.parseInt(f.size)
                        }
                        mimetype = f.mimetype
                        filename = f.filename
                        log.debug "Saving observation file ${f.filename}"
                    } else {
                        mimetype = f.contentType
                        filename = f.originalFilename
					    log.debug "Saving user file ${f.originalFilename}"
                    }

					// List of OK mime-types
					//TODO Move to config

					def resourcetype = mimetype.substring(0, mimetype.indexOf('/')).toLowerCase()
					def resourceTypeAudio = ResourceType.AUDIO.value().toLowerCase()					
					def resourceTypeImage = ResourceType.IMAGE.value().toLowerCase()		
					
					def okcontents
					def maxSizeAllow
					if(resourcetype == resourceTypeImage){
						okcontents = [
										'image/png',
										'image/jpeg',
										'image/pjpeg',
										'image/gif',
										'image/jpg'
										]
						maxSizeAllow = grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE

					} else if(resourcetype == resourceTypeAudio){						
						okcontents = [
										'audio/mp4','audio/m4a',
										'audio/mpeg','audio/mp1','audio/mp2','audio/mp3','audio/mpg',
										'audio/ogg','audio/oga',
										'audio/wav',
										'audio/webm',

										]
						maxSizeAllow = grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE

					}
					
					if (!okcontents.contains(mimetype)) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							okcontents,
							filename
						])
					}
					else if(f.size > maxSizeAllow) {
						message = g.message(code: 'resource.file.invalid.max.message', args: [
							grailsApplication.config.speciesPortal.observations.MAX_IMAGE_SIZE/1024,
							filename,
							f.size/1024
						], default:'File size cannot exceed ${104857600/1024}KB');
					}
//					else if(f.empty) {
//						message = g.message(code: 'file.empty.message', default:'File cannot be empty');
//					}
					else {
						if(params.resType == SUser.class.name){
                            obvDir = null;
                        }
                        if(!obvDir) {
							if(!params.obvDir) {
                                obvDir = new File(rootDir);
								if(!obvDir.exists()) {
									obvDir.mkdir();
								}
								obvDir = new File(obvDir, UUID.randomUUID().toString());
								obvDir.mkdir();
							} else {
								obvDir = new File(rootDir, params.obvDir);
								obvDir.mkdir();
							}
						}

				
						File file = utilsService.getUniqueFile(obvDir, Utils.generateSafeFileName(filename));

                        if(f instanceof org.codehaus.groovy.grails.web.json.JSONObject) {
						    download(f.url, file );						
                        } else {
						    f.transferTo( file );
                        }
						String obvDirPath = obvDir.absolutePath.replace(rootDir, "")
						def thumbnail
						def type
                        def pi
						if(resourcetype == resourceTypeImage){
								pi = ProcessImage.createLog(file.getAbsolutePath(), obvDir.toString());
                                //ImageUtils.createScaledImages(new File(pi.filePath), new File(pi.directory));
								def res = new Resource(fileName:obvDirPath+"/"+file.name, type:ResourceType.IMAGE);
		                        //context specific baseUrl for location picker script to work
								def baseUrl = Utils.getDomainServerUrlWithContext(request) + rootDir.substring(rootDir.lastIndexOf("/") , rootDir.size())
								thumbnail = res.thumbnailUrl(baseUrl, null, ImageType.LARGE);	
								type = ResourceType.IMAGE	
														
						}else if(resourcetype == resourceTypeAudio){
								thumbnail = grailsApplication.config.grails.serverURL+"/images/audioicon.png"
								type = ResourceType.AUDIO	
								

						}
                        if(pi)
						    resourcesInfo.add([fileName:obvDirPath+"/"+file.name, url:'', thumbnail:thumbnail ,type:type, jobId:pi.id]);
                        else 
						    resourcesInfo.add([fileName:obvDirPath+"/"+file.name, url:'', thumbnail:thumbnail ,type:type]);
					}
				}
				
				if(params.videoUrl) {
					//TODO:validate url;
					def videoUrl = params.videoUrl;
					if(videoUrl && Utils.isURL(videoUrl)) {
						String videoId = Utils.getYouTubeVideoId(videoUrl);
						if(videoId) {
						def res = new Resource(fileName:'v', type:ResourceType.VIDEO);		
						res.setUrl(videoUrl);				
						resourcesInfo.add([fileName:'v', url:res.url, thumbnail:res.thumbnailUrl(), type:res.type]);
						} else {
						message = messageSource.getMessage("default.valid.video.url", ['youtube'] as Object[] , RCU.getLocale(request))
						}
					} else {
						message = messageSource.getMessage("default.valid.video.url", [''] as Object[] , RCU.getLocale(request))
					}
				}

			
				log.debug resourcesInfo
				// render some XML markup to the response
				if(resourcesInfo) {
                    if(params.format?.equalsIgnoreCase("json")) {
                        def resourcesList = [];
                        for(r in resourcesInfo) {
                            def res = ['fileName':r.fileName, 'url':r.url,'thumbnail':r.thumbnail, type:r.type, 'jobId':r.jobId]
                            resourcesList << res
                        }
                        render ([observations:['dir':(obvDir?obvDir.absolutePath.replace(rootDir, ""):''), resources:resourcesList]] as JSON)
                    } else {
                        render(contentType:"text/xml") {
                            observations {							
                                dir(obvDir?obvDir.absolutePath.replace(rootDir, ""):'')							
                                resources {
                                    for(r in resourcesInfo) {
                                        res('fileName':r.fileName, 'url':r.url,'thumbnail':r.thumbnail, type:r.type, 'jobId':r.jobId){}
                                    }
                                }
                            }
                        }
                    }
				} else {
					response.setStatus(500)
					message = [success:false, error:message]
					render message as JSON
				}
			/*} else {
				response.setStatus(500)
				def message = [success:false, error:g.message(code: 'no.file.attached', default:'No file is attached')]
				render message as JSON
			}*/
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			message = [success:false, error:g.message(code: 'file.upload.fail', default:'Error while processing the request.')]
			render message as JSON
		}
	}
	
	private def download(url, File file)
	{
		def out = new BufferedOutputStream(new FileOutputStream(file))
		out << new URL(url).openStream()
		out.close()
	}


	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addRecommendationVote() {
		params.author = springSecurityService.currentUser;
        //boolean isMobileApp = params.format?.equalsIgnoreCase("json") || params.isMobileApp; 
        String msg; 
        try {
            params.obvId = params.obvId?.toLong()?:(params.id?.toLong());
        } catch(e) {
            e.printStackTrace();
            params.obvId = null;
        }

		if(params.obvId) {
			boolean canMakeSpeciesCall = true//getSpeciesCallPermission(params.obvId)
			//Saves recommendation if its not present
			def recVoteResult, recommendationVoteInstance
			if(canMakeSpeciesCall){
				recVoteResult = getRecommendationVote(params.long('obvId'), params.author, params.confidence, params.recoId?params.long('recoId'):null, params.recoName, params.canName, params.commonName, params.languageName);
				recommendationVoteInstance = recVoteResult?.recVote;
				msg = recVoteResult?.msg;
			}

			def observationInstance = Observation.get(params.obvId);
			def mailType
			try {
				if(!recommendationVoteInstance) {
					msg = msg ?: "No reco instance"
                    log.debug msg
					//saving max voted species name for observation instance needed when observation created without species name
					//observationInstance.calculateMaxVotedSpeciesName();
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
					if(params["createNew"] && (params.oldAction == "save" || params.oldAction == "bulkSave")) {
						mailType = utilsService.OBSERVATION_ADDED;
						utilsService.sendNotificationMail(mailType, observationInstance, request, params.webaddress);
					}

					if(!params["createNew"]){
                        def model = utilsService.getErrorModel(msg, null, OK.value());
                        withFormat {
                            html {
                                redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, msg:msg, canMakeSpeciesCall:canMakeSpeciesCall])
                            }
                            json { render model as JSON }
                            xml { render model as XML }
                        }
					} else {
                        if(params.oldAction != "bulkSave"){
                            def model = utilsService.getErrorModel(msg, null, OK.value());
                            withFormat {
                                html {
						            redirect (url:uGroup.createLink(action:'show', controller:"observation", id:observationInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
                                }
                                json { render model as JSON }
                                xml { render model as XML }
                            }
                        } else {
                            //def output = [:]
                            def miniObvCreateHtml = g.render(template:"/observation/miniObvCreateTemplate", model:[observationInstance: observationInstance]);
                            def model = utilsService.getErrorModel(msg, null, OK.value(), ['miniObvCreateHtml':miniObvCreateHtml]);
                            //output = [statusComplete : true, 'miniObvCreateHtml':miniObvCreateHtml]
                            withFormat {
                                html {
                            		//redirect(action: "show", id: observationInstance.id, params:[postToFB:(params.postToFB?:false)]);
                                }
                                json { render model as JSON }
                                xml { render model as XML}
                            }
                        }
					}
					return
				} else if(!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush: true)) {
					msg = msg ?: "Successfully added reco vote : "+recommendationVoteInstance
                    log.debug msg;
					//saving max voted species name for observation instance
					observationInstance.calculateMaxVotedSpeciesName();
					def activityFeed = activityFeedService.addActivityFeed(observationInstance, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED, activityFeedService.getSpeciesNameHtmlFromReco(recommendationVoteInstance.recommendation, null));
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
		            
                    //just updates species time stamp on recommendation
                    recommendationVoteInstance.updateSpeciesTimeStamp();			
					
                    //sending email
					if( params["createNew"] && ( params.oldAction == "save" || params.oldAction == "bulkSave" ) ) {
						mailType = utilsService.OBSERVATION_ADDED;
					} else {
						mailType = utilsService.SPECIES_RECOMMENDED;
					}
					utilsService.sendNotificationMail(mailType, observationInstance, request, params.webaddress, activityFeed);
					commentService.addRecoComment(recommendationVoteInstance.recommendation, observationInstance, params.recoComment);
					
                    if(!params["createNew"]) {
                        def model = utilsService.getSuccessModel(msg, recommendationVoteInstance, OK.value());
                        withFormat {
                            html {
        						redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, msg:msg, canMakeSpeciesCall:canMakeSpeciesCall])
                            }
                            json { render model as JSON }
                            xml { render model as XML }
                        }
					} else {
                        if(params.oldAction != "bulkSave"){
                            def model = utilsService.getSuccessModel(msg, recommendationVoteInstance, OK.value());
                            withFormat {
                                html {
    						        redirect (url:uGroup.createLink(action:'show', controller:"observation", id:observationInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
                                } 
                                json { render model as JSON }
                                xml { render model as XML }
                            }
                        } else {
                            //def output = [:]
                            def miniObvCreateHtml = g.render(template:"/observation/miniObvCreateTemplate", model:[observationInstance: observationInstance]);
                            def model = utilsService.getSuccessModel(msg, recommendationVoteInstance, OK.value(), ['miniObvCreateHtml':miniObvCreateHtml]);
                            //output = [statusComplete : true, 'miniObvCreateHtml':miniObvCreateHtml]
                            //render output as JSON
                            withFormat {
                                html {
                                    //redirect(action: "show", id: observationInstance.id, params:[postToFB:(params.postToFB?:false)]);
                                }
                                json { render model as JSON }
                                xml { render model as XML}
                            }
                        }
					}
					return
				} else {
                    msg = msg ?: messageSource.getMessage("default.recommendation.vote.failed", null, RCU.getLocale(request))
                    log.debug msg
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);

                    if(!params["createNew"]) {
                        def model = utilsService.getErrorModel(msg, recommendationVoteInstance, OK.value());
                        withFormat {
                            json { render model as JSON }
                            xml { render model as XML }
                        }
                    }
                    if(params.oldAction != "bulkSave"){
                        def model = utilsService.getErrorModel(msg, recommendationVoteInstance, OK.value());
                        withFormat {
                            html {
                                render (view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance], params:[postToFB:(params.postToFB?:false)])
                            }
                            json { render model as JSON }
                            xml { render model as XML }
                        }
                    } else {
                        //def output = [:]
                        def miniObvCreateHtml = g.render(template:"/observation/miniObvCreateTemplate", model:[observationInstance: observationInstance]);
                        //output = [statusComplete : true, 'miniObvCreateHtml':miniObvCreateHtml]
                        //render output as JSON
                        def model = utilsService.getErrorModel(msg, recommendationVoteInstance, OK.value(), ['miniObvCreateHtml':miniObvCreateHtml]);
                        //output = [statusComplete : true, 'miniObvCreateHtml':miniObvCreateHtml]
                        //render output as JSON
                        withFormat {
                            html {
                                //redirect(action: "show", id: observationInstance.id, params:[postToFB:(params.postToFB?:false)]);
                            }
                            json { render model as JSON }
                            xml { render model as XML}
                        }

                    }

				}
			} catch(e) {
                e.printStackTrace();
                msg  = e.getMessage();
                log.error msg;

                if(params["createNew"]){
                    if(params.oldAction != "bulkSave"){
                        def model = utilsService.getErrorModel(msg, null, OK.value());
                        withFormat {
                            html {
                                render(view: "show", model: [observationInstance:observationInstance, recommendationVoteInstance: recommendationVoteInstance], params:[postToFB:(params.postToFB?:false)])
                            }
                            json { render model as JSON; } 
                            xml { render model as XML; } 
                        }
                    } else {
                        def miniObvCreateHtml = g.render(template:"/observation/miniObvCreateTemplate", model:[observationInstance: observationInstance]);
                        def model = utilsService.getErrorModel(msg, recommendationVoteInstance, OK.value(), ['miniObvCreateHtml':miniObvCreateHtml]);
                        withFormat {
                            json { render model as JSON }
                            xml { render model as XML }
                        }
                    }
                }
                else {
                    def model = utilsService.getErrorModel(msg, null, OK.value());
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML }
                    }

                }
			}
		} else {
			msg  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
			log.error msg;
            def model = utilsService.getErrorModel(msg, null, OK.value());
            if(params['createNew']) {
                withFormat {
                    html {
                        flash.message = msg;
                        redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
            } else {
                withFormat {
                    json { render model as JSON }
                    xml { render model as XML }
                }
            }
		}
	}

	/**
	 * adds a recommendation and 1 vote to it attributed to the logged in user
	 * saves recommendation if it doesn't exist
	 */
	@Secured(['ROLE_USER'])
	def addAgreeRecommendationVote() {
		def msg;
		params.author = springSecurityService.currentUser;
 
        try {
            params.obvId = params.obvId?.toLong()?:(params.id?.toLong());
        } catch(e) {
            e.printStackTrace();
            params.obvId = null;
        }


		if(params.obvId) {
			//Saves recommendation if its not present
			boolean canMakeSpeciesCall = true//getSpeciesCallPermission(params.obvId)
			def recVoteResult, recommendationVoteInstance
			if(canMakeSpeciesCall){
				recVoteResult = getRecommendationVote(params.long('obvId'), params.author, params.confidence, params.recoId?params.long('recoId'):null, params.recoName, params.canName, params.commonName, params.languageName);
				recommendationVoteInstance = recVoteResult?.recVote;
				msg = recVoteResult?.msg;
			}
			
			def observationInstance = Observation.get(params.obvId);
			try {
				if(!recommendationVoteInstance){
					def result = ['votes':params.int('currentVotes')];
					def r = [
						status : OK.value(),
						success : 'true',
						msg:msg,
						canMakeSpeciesCall:canMakeSpeciesCall]
                        withFormat {
                            json { render r as JSON }
                            xml { render r as XML }
                        }
					//render r as JSON
					//redirect(action:getRecommendationVotes, id:params.obvId, params:[ max:3, offset:0, msg:msg])
					return
				} else if(recommendationVoteInstance.save(flush: true)) {
					log.debug "Successfully added reco vote : "+recommendationVoteInstance
					observationInstance.calculateMaxVotedSpeciesName();
					def activityFeed = activityFeedService.addActivityFeed(observationInstance, recommendationVoteInstance, recommendationVoteInstance.author, ActivityFeedService.SPECIES_AGREED_ON, activityFeedService.getSpeciesNameHtmlFromReco(recommendationVoteInstance.recommendation, null));
					observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
				
                    //just updates species time stamp on recommendation
                    recommendationVoteInstance.updateSpeciesTimeStamp();			

					//sending mail to user
					utilsService.sendNotificationMail(ActivityFeedService.SPECIES_AGREED_ON, observationInstance, request, params.webaddress, activityFeed);
					def r = [
						status : OK.value(),
						success : 'true',
						msg:msg,
						canMakeSpeciesCall:canMakeSpeciesCall]
                    withFormat {
                            json { render r as JSON }
                            xml { render r as XML }
                        }
					//redirect(action:getRecommendationVotes, id:params.obvId, params:[max:3, offset:0, msg:msg])
					return
				}
				else {
                    msg = messageSource.getMessage("default.recommendation.vote.failed", null, RCU.getLocale(request))
                    def model = utilsService.getErrorModel(msg, recommendationVoteInstance, OK.value());
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML }
                    }
				}
			} catch(e) {
				e.printStackTrace();
                msg = messageSource.getMessage("default.error.adding.vote", [e.getMessage()] as Object[], RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
        } else {
            flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
            log.error flash.message;
            def model = utilsService.getErrorModel(flash.message, null, OK.value());
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
	}
	
	/**
	* Deletes recommendation vote
	*/
   @Secured(['ROLE_USER'])
   def removeRecommendationVote() {

	   def author = springSecurityService.currentUser;
 
       try {
           params.obvId = params.obvId?.toLong()?:(params.id?.toLong());
       } catch(e) {
           e.printStackTrace();
           params.obvId = null;
       }
	
	   if(params.obvId) {
		   def observationInstance = Observation.get(params.obvId);
		   def recommendationVoteInstance = RecommendationVote.findWhere(recommendation:Recommendation.read(params.recoId.toLong()), author:author, observation:observationInstance)
           if(!observationInstance || !recommendationVoteInstance) {
            	   def r = [
				   status : OK.value(),
				   success : 'false',
				   msg:"${message(code: 'default.not.found.message', args: ['Recommendation', params.recoId])}"]
                   withFormat {
                       json { render r as JSON }
                       xml { render r as XML }
                   }
			   return

           }
		   try {
			   recommendationVoteInstance.delete(flush: true, failOnError:true)
			   log.debug "Successfully deleted reco vote : "+recommendationVoteInstance
			   observationInstance.calculateMaxVotedSpeciesName();
			   def activityFeed = activityFeedService.addActivityFeed(observationInstance, observationInstance, author, activityFeedService.RECOMMENDATION_REMOVED, activityFeedService.getSpeciesNameHtmlFromReco(recommendationVoteInstance.recommendation, null));
			   observationsSearchService.publishSearchIndex(observationInstance, COMMIT);
		
                //just updates species time stamp on recommendation
                recommendationVoteInstance.updateSpeciesTimeStamp();				
	
               //sending mail to user
			   utilsService.sendNotificationMail(activityFeedService.RECOMMENDATION_REMOVED, observationInstance, request, params.webaddress, activityFeed);
			   def r = [
				   status : OK.value(),
				   success : 'true',
				   msg:"${message(code: 'recommendations.deleted.message', args: [recommendationVoteInstance.recommendation.name])}"]
                   withFormat {
                       json { render r as JSON }
                       xml { render r as XML }
                   }
               return
		   } catch(e) {
			   e.printStackTrace();
               def msg = "Error while adding agree vote ${e.getMessage()}"
               def model = utilsService.getErrorModel(msg, null, OK.value());
               withFormat {
                   json { render r as JSON }
                   xml { render r as XML }
               }
		   }
	   } else {
           flash.message  = "${message(code: 'observation.invalid', default:'Invalid observation')}"
           log.error flash.message;
           def model = utilsService.getErrorModel(flash.message, null, OK.value());
           withFormat {
               json { render r as JSON }
               xml { render r as XML }
           }
	   }
   }


	/**
	 * 
	 */
	def getRecommendationVotes = {
		params.max = params.max ? params.int('max') : 1
		params.offset = params.offset ? params.long('offset'): 0
        String msg;

        if(!params.id) {
            def model = utilsService.getErrorModel (g.message(code: 'error', default:'Invalid observation id'), null, OK.value())
            withFormat {
                html {}
                json { render model as JSON }
                xml { render model as XML }
            }
            return;
        }

        try {
            def observationInstance = Observation.get(params.id.toLong())
            if (observationInstance) {
                def results = observationInstance.getRecommendationVotes(params.max, params.offset);
                def html =  g.render(template:"/common/observation/showObservationRecosTemplate", model:['observationInstance':observationInstance, 'result':results.recoVotes, 'totalVotes':results.totalVotes, 'uniqueVotes':results.uniqueVotes, 'userGroupWebaddress':params.userGroupWebaddress]);
                def speciesNameHtml =  g.render(template:"/common/observation/showSpeciesNameTemplate", model:['observationInstance':observationInstance]);
                def speciesExternalLinkHtml =  g.render(template:"/species/showSpeciesExternalLinkTemplate", model:['speciesInstance':Species.read(observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId())]);

                html = html.replaceAll('\\n|\\t','');
                def result = [
                'status' : 'success',
                recoVotes:results.recoVotes?:'',
                canMakeSpeciesCall:params.canMakeSpeciesCall,
                recoHtml:html?:'',
                totalVotes:results.totalVotes?:'',
                uniqueVotes:results.uniqueVotes?:'',
                msg:params.msg?:'',
                speciesNameTemplate:speciesNameHtml?:'',
                speciesExternalLinkHtml:speciesExternalLinkHtml?:'',
                speciesName:observationInstance.fetchSpeciesCall()?:'']

                if(results?.recoVotes.size() > 0) {
                    def model = utilsService.getSuccessModel('', null, OK.value(), result);
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML}
                    }
                    return
                } else {
                    //response.setStatus(500);
                    def message = "";
                    if(params.offset > 0) {
                        message = g.message(code: 'recommendations.nomore.message', default:'No more recommendations made. Please suggest');
                    } else {
                        message = g.message(code: 'recommendations.zero.message', default:'No recommendations made. Please suggest');
                    }
                    result["msg"] = message
                    def model = utilsService.getSuccessModel(message, null, OK.value(), result);
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML}
                    }
                    return
                }

            } else {
                //response.setStatus(500)
                msg = g.message(code: 'error', default:"No observation found with ${params.id}")
                def model = utilsService.getErrorModel(msg, null, OK.value());
                withFormat {
                    json { render model as JSON }
                    xml { render model as XML}
                }

            }
        } catch(e){
            e.printStackTrace();
            //response.setStatus(500);
            msg = g.message(code: 'error', default:"Error while processing the request : ${e.getMessage()}");
            def model = utilsService.getErrorModel(msg, null, OK.value(), [e.getMessage()]);
            withFormat {
                json { render model as JSON }
                xml { render model as XML}
            }
        }
	}

	/**
	 * 
	 */
	def voteDetails = {
		def votes = RecommendationVote.findAll("from RecommendationVote as recoVote where recoVote.recommendation.id = :recoId and recoVote.observation.id = :obvId order by recoVote.votedOn desc", [recoId:params.long('recoId'), obvId:params.long('obvId')]);
		render (template:"/common/voteDetails", model:[votes:votes]);
	}

	/**
	 * 
	 */

	def listRelated = {
    	log.debug params;
println "================LIST RELATED CALLED========="
        Long parentId = params.id?params.long('id'):null;
        def result = observationService.getRelatedObservations(params);

        def activeFilters = new HashMap(params);
        activeFilters.remove('userGroupInstance');

        def model = [ queryParams: [max:result.max  ], activeFilters:activeFilters, filterProperty:params.filterProperty];
        model['parentId'] = parentId;

        def inGroupMap = [:]
        result.relatedObv.observations.each { m-> 
            inGroupMap[(m.observation.id)] = m.inGroup == null ?'false':m.inGroup
        }

        model['observationInstanceList'] = result.relatedObv.observations.observation
        model['inGroupMap'] = inGroupMap
        model['instanceTotal'] = result.relatedObv.count

        render (view:'listRelated', model:model)
    }

	/**
	 * 
	 */
	def tags = {
		render Tag.findAllByNameIlike("${params.term}%")*.name as JSON
	}

		
	@Secured(['ROLE_USER'])
	def deleteRecoVoteComment() {
		def recoVote = RecommendationVote.get(params.id.toLong());
		recoVote.comment = null;
		try {
			recoVote.save(flush:true)
			def msg =  [success:g.message(code: 'observation.recoVoteComment.success.onDelete', default:'Comment deleted successfully')]
			render msg as JSON
			return;
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			def message = [error: g.message(code: 'observation.recoVoteComment.error.onDelete', default:'Error on deleting recommendation vote comment')];
			render message as JSON
		}
	}

	def snippet = {
        def observationInstance
        if(params.id){
		    observationInstance = Observation.get(params.id)
        }
        if(observationInstance){
		    render (template:"/common/observation/showObservationSnippetTabletTemplate", model:[observationInstance:observationInstance, 'userGroupWebaddress':params.webaddress]);
        }
	}

	

//	def participants = {
//		render getParticipants(Observation.read(params.long('id')))
//	}
	
	def unsubscribeToIdentificationMail = {
		log.debug "$params"
		def user
		if(params.userId){
			user = SUser.get(params.userId.toLong())
		}
		BlockedMails bm = new BlockedMails(email:params.email);
		if(bm){
			user = SUser.findByEmail(bm.email)
			if(!bm.save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
		}
		if(user){
			user.allowIdentifactionMail = false;
			if(!user.save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
		}
		render "${message(code: 'user.unsubscribe.identificationMail', args: [params.email])}"
	}

	/*
	 * @param params
     * obvId
     * author
     * confidence
     * recoId
     * recoName
    * canName
    * commonName
    * languageName
	 * @return
	 */
	private Map getRecommendationVote(Long obvId, SUser author, String conf, Long recoId, String recoName, String canName, String commonName, String languageName) {
		def observation = params.observation?:Observation.get(obvId);
		
		ConfidenceType confidence = observationService.getConfidenceType(conf?:ConfidenceType.CERTAIN.name());
		RecommendationVote existingRecVote = RecommendationVote.findByAuthorAndObservation(author, observation);
		
		def reco, commonNameReco, isAgreeRecommendation = false;
		if(recoId) {
			//user presses on agree button so getting reco from id and creating new recoVote without additional common name
			reco = Recommendation.get(recoId);
			isAgreeRecommendation = true
		} else {
			//add recommendation used so taking both reco and common name reco if available
			def recoResultMap = observationService.getRecommendations(params.recoName, params.canName, params.commonName, params.languageName)
			reco = recoResultMap.mainReco;
			commonNameReco =  recoResultMap.commonNameReco;
		}
		
		RecommendationVote newRecVote = new RecommendationVote(observation:observation, recommendation:reco, commonNameReco:commonNameReco, author:author, confidence:confidence);

		if(!reco){
			def msg = "Not a valid recommendation"
			return [recoVote:null, msg:msg]
		} else {
			if(!existingRecVote){
				log.debug " Adding (first time) recommendation vote for user " + author.id +  " reco name " + reco.name
				def msg = "${message(code: 'recommendations.added.message', args: [reco.name])}"
				return [recVote:newRecVote, msg:msg]
			}else{
				/**
				 *  if old recommendation is same as new recommendation then
				 *  case 1 : user might want to update(add new common name or delete existing one) the common name
				 *  case 2 : if user gave sn and cn earlier and the by mistake clicks on agree then this case should not affect any thing on recovote
				 *  		so added boolean flag before updating common name				   
				 *  if old reco and new reco not same then deleting old reco vote 
				 */
				if(existingRecVote.recommendation.id == reco.id){
					log.debug " Same recommendation already made by user " + author.id +  " reco name " + reco.name + " leaving as it is"
					def msg = "${message(code: 'reco.vote.duplicate.message', args: [reco.name])}"
					if(!isAgreeRecommendation && existingRecVote.commonNameReco != commonNameReco){
						log.debug "Updating recoVote as common name changed"
						existingRecVote.commonNameReco = commonNameReco;
						return [recVote:existingRecVote, msg:msg]
						/*
						if(!existingRecVote.save(flush:true)){
							existingRecVote.errors.allErrors.each { log.error it }
						}
						commentService.addRecoComment(existingRecVote.recommendation, observation, params.recoComment);
						*/
					}
					return [recVote:null, msg:msg]
				}else{
					log.debug " Overwriting old recommendation vote for user " + author.id +  " new reco name " + reco.name + " old reco name " + existingRecVote.recommendation.name
					def msg = "${message(code: 'recommendations.overwrite.message', args: [existingRecVote.recommendation.name, reco.name])}"
					try{
                        observation.removeFromRecommendationVote(existingRecVote);
						existingRecVote.delete(flush: true, failOnError:true)
					}catch (Exception e) {
						e.printStackTrace();
					}
					return [recVote:newRecVote, msg:msg]
				}
			}
		}
        return;
	}
   
    /**
	 * Count   
	 */
	def count = {
		render chartService.getObservationCount(params)
	}

	@Secured(['ROLE_USER'])
	def sendIdentificationMail() {
		def currentUserMailId = springSecurityService.currentUser?.email;
		Map emailList = getUnBlockedMailList(params.userIdsAndEmailIds, request);
		if(emailList.isEmpty()){
			log.debug "No valid email specified for identification."
		}else if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
			def conf = SpringSecurityUtils.securityConfig
			def mailSubject = params.mailSubject
			for(entry in emailList.entrySet()){
				def body = observationService.getIdentificationEmailInfo(params, request, entry.getValue(), params.sourceController?:'observation', params.sourceAction?:'show').mailBody
				try {
					mailService.sendMail {
						to entry.getKey()
	                    bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
						//bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com", "thomas.vee@gmail.com", "sandeept@strandls.com"
						from grailsApplication.config.grails.mail.default.from
						replyTo currentUserMailId
						subject mailSubject
						html body.toString()
					}
					log.debug " mail sent for identification "
				}catch(all)  {
				    log.error all.getMessage()
				}
			}
		}
		render (['success:true']as JSON);
	}

	private Map getUnBlockedMailList(String userIdsAndEmailIds, request) {
		Map result = new HashMap();
		userIdsAndEmailIds.split(",").each{
			String candidateEmail = it.trim();
			if(candidateEmail.isNumber()){
				SUser user = SUser.get(candidateEmail.toLong());
				candidateEmail = user.email.trim();
				if(user.allowIdentifactionMail){
					result[candidateEmail] = utilsService.generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail, userId:user.id], request) ;
				}else{
					log.debug "User $user.id has unsubscribed for identification mail."
				}
			}else{
				if(BlockedMails.findByEmail(candidateEmail)){
					log.debug "Email $candidateEmail is unsubscribed for identification mail."
				}else{
					result[candidateEmail] = utilsService.generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail], request) ;
				}
			}
		}
		return result;
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	def search = {
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		def model = observationService.getObservationsFromSearch(params);
		
		if(params.append?.toBoolean() && session["obv_ids_list"]) {
			session["obv_ids_list"].addAll(model.totalObservationIdList);
		} else {
			session["obv_ids_list_params"] = params.clone();
			session["obv_ids_list"] = model.totalObservationIdList;
		}
		model.remove('totalObservationIdList');
		model['isSearch'] = true;
		log.debug "Storing all observations ids list in session ${session['obv_ids_list']}";
		params.action = 'search'
		params.controller = 'observation'
		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/common/observation/showObservationListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
            model['width'] = 300;
            model['height'] = 200;
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
			return;
		} else {
			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/common/observation/showObservationListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
			def tagsHtml = "";
			if(model.showTags) {
//				def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
//				tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			}
//			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);
/*			def chartModel = model.speciesGroupCountList
            chartModel['width'] = 300;
            chartModel['height'] = 270;
*/
            def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml, tagsHtml:tagsHtml, instanceTotal:model.instanceTotal]
			render result as JSON
			return;
		}
	}

	/**
	 *
	 */
	def terms = {
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        if(params.field == searchFieldsConfig.CONTRIBUTOR) {
            params.field = searchFieldsConfig.USERNAME +"_exact"
        }
		
		List result = observationService.nameTerms(params)

		render result.value as JSON;
	}

	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// SEARCH END /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	def getFilteredLanguage = {
		render species.Language.filteredList() 
	}
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// json API ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	@Secured(['ROLE_USER'])
	def getObv() {
		render Observation.read(params.id.toLong()) as JSON
	} 
	
	@Secured(['ROLE_USER'])
	def getList() {
		def result = getObservationList(params)
        render result as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getHabitatList() {
		def res = new HashMap()
			Habitat.list().each {
			res[it.id] = it.name
		}
		render res as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getGroupList() {
		def res = new HashMap()
		SpeciesGroup.list().each {
			res[it.id] = it.name
		
		}
		render res as JSON
	}
	
	@Secured(['ROLE_USER'])
	def getThumbObvImage() {
		def baseUrl = grailsApplication.config.speciesPortal.observations.serverURL
		def mainImage = Observation.read(params.id.toLong()).mainImage()
		def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null
		render baseUrl + imagePath 
	}
	
	@Secured(['ROLE_USER'])
	def getFullObvImage() {
        if(!params.id ) {
            render '';
            return
        }
		def baseUrl = grailsApplication.config.speciesPortal.observations.serverURL
		def mainImage = Observation.read(params.id.toLong()).mainImage()
		def gallImagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix):null
		render baseUrl + gallImagePath
	}
	
	@Secured(['ROLE_USER'])
	def getUserImage() {
		render SUser.read(params.id.toLong()).icon() 
	}
	
	@Secured(['ROLE_USER'])
	def getUserInfo() {
		def res = new HashMap()
		def u = SUser.read(params.id.toLong())
		res["id"] = u.id
		res["aboutMe"] = u.aboutMe
		res["dateCreated"] =  u.dateCreated
		res["email"] = u.email
		res["lastLoginDate"] = u.lastLoginDate
		res["location"] = u.location 
		res["name"] = u.name
		res["username"] = u.username
		res["website"] = u.website
		render res as JSON
	}
	
	private getSpeciesCallPermission(obvId){
		return customsecurity.hasPermissionToMakeSpeciesCall([id:obvId, className:species.participation.Observation.class.getCanonicalName(), permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
	}
	
	@Secured(['ROLE_ADMIN'])
	def batchUpload() {
		obvUtilService.batchUpload(request, params)
		render "== done"
	}
	
	@Secured(['ROLE_USER'])
	def requestExport() {
		obvUtilService.requestExport(params)
		def r = [:]
		r['msg']= "${message(code: 'observation.download.requsted', default: 'Processing... You will be notified by email when it is completed. Login and check your user profile for download link.')}"
		render r as JSON
	}
	
	@Secured(['ROLE_USER'])
	def downloadFile() {
		log.debug(params)
		def dl = DownloadLog.read(params.id.toLong())
		if(dl && dl.author == springSecurityService.currentUser){
			File file = new File(dl.filePath)
			response.contentType  = 'text/csv' 
			response.setHeader("Content-disposition", "filename=${file.getName()}")
			response.outputStream << file.getBytes()
			response.outputStream.flush()
		}
	}

    def locations = {
        def locations = observationService.locations(params);
        render locations as JSON
    }

    /**
    */
    def distinctReco() {
        def max = Math.min(params.max ? params.int('max') : 10, 100)
        def offset = params.offset ? params.int('offset') : 0
        Map result = [:];
        try {
            def distinctRecoListResult;
		    if(params.actionType == 'search') {
                distinctRecoListResult = observationService.getDistinctRecoListFromSearch(params, max, offset);
            } else {
                distinctRecoListResult = observationService.getDistinctRecoList(params, max, offset);
            }

            if(distinctRecoListResult.distinctRecoList.size() > 0) {
                result = [distinctRecoList:distinctRecoListResult.distinctRecoList, totalRecoCount:distinctRecoListResult.totalCount, status:'success', msg:'success', next:offset+max]
                
            } else {
                def message = "";
                if(params.offset  > 0) {
                    message = g.message(code: 'recommendations.nomore.message', default:'No more distinct species. Please contribute');
                } else {
                    message = g.message(code: 'recommendations.zero.message', default:'No species. Please contribute');
                }
                result = [msg:message]
            }

            def model = utilsService.getSuccessModel(result.msg, null, OK.value(), result);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }

        } catch(e) {
            e.printStackTrace();
            log.error e.getMessage();
            String msg = g.message(code: 'error', default:'Error while processing the request.');
            def model = utilsService.getErrorModel(msg, null, OK.value(), [e.getMessage()]);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }

    /**
    */
    def speciesGroupCount() {
        Map result = [:];
        try {
            def speciesGroupCountListResult;
		    if(params.actionType == 'search') {
                speciesGroupCountListResult = observationService.getSpeciesGroupCountFromSearch(params);
            } else {
                speciesGroupCountListResult = observationService.getSpeciesGroupCount(params);
            }

            if(speciesGroupCountListResult.speciesGroupCountList.size() > 0) {
                result = ['speciesGroupCountList':speciesGroupCountListResult.speciesGroupCountList, status:'success', msg:'success']
                
            } else {
                def message = g.message(code: 'speciesGroup.count.zero.message', default:'No data');
                result = [msg:message]
            }
            def model = utilsService.getSuccessModel(result.msg, null, OK.value(), result);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        } catch(e) {
            e.printStackTrace();
            log.error e.getMessage();
            String msg = g.message(code: 'error', default:'Error while processing the request.');
             def model = utilsService.getErrorModel(msg, null, OK.value(), [e.getMessage()]);
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
        }
    }
    
    @Secured(['ROLE_ADMIN','ROLE_SPECIES_ADMIN'])
    def lock() {
        def msg = ""
        def obv = Observation.get(params.id.toLong());
        def reco = Recommendation.get(params.recoId.toLong());
        def currentUser = springSecurityService.currentUser;
        def mailType = '';
        def activityFeed;
        if(params.lockType == "Lock"){
            //current user & reco
            def recVo = RecommendationVote.findWhere(observation:obv, author: currentUser);
            def newRecVo;
            if(recVo){
                if(reco != recVo.recommendation) {
                    recVo.delete(flush: true, failOnError:true)
                }
                newRecVo = new RecommendationVote(recommendation: reco, observation:obv, author: currentUser )
                if(!newRecVo.save(flush:true)){
                    newRecVo.errors.allErrors.each { log.error it } 
                }
            }
            if(!recVo){
                newRecVo = new RecommendationVote(recommendation: reco, observation:obv, author: currentUser )
                if(!newRecVo.save(flush:true)){
                    newRecVo.errors.allErrors.each { log.error it } 
                }
            }
            obv.maxVotedReco = reco; 
            obv.isLocked = true;
            msg = messageSource.getMessage("default.observation.locked", null, RCU.getLocale(request))
            mailType = utilsService.OBV_LOCKED;
            activityFeed = activityFeedService.addActivityFeed(obv, newRecVo, currentUser, mailType, activityFeedService.getSpeciesNameHtmlFromReco(newRecVo.recommendation, null));
        }else{
            obv.removeResourcesFromSpecies()
            obv.isLocked = false;
            obv.calculateMaxVotedSpeciesName()
            msg = messageSource.getMessage("default.observation.unlocked", null, RCU.getLocale(request))
            mailType = utilsService.OBV_UNLOCKED;
            def recommendationVoteInstance =  RecommendationVote.findWhere(recommendation: reco, observation:obv, author: currentUser)
            activityFeed = activityFeedService.addActivityFeed(obv, recommendationVoteInstance, currentUser, mailType, activityFeedService.getSpeciesNameHtmlFromReco(recommendationVoteInstance.recommendation, null));
        }
        if(!obv.save(flush:true)){
            obv.errors.allErrors.each { log.error it } 
        }
		utilsService.sendNotificationMail(mailType, obv, request, params.webaddress, activityFeed);

        def result = ['msg': msg]
        render result as JSON
    }

    /*def getRecommendationListJSON(LinkedHashMap result){

    	if(result){	    	
	   		def test = ['result' : result];
	   		return test; // as JSON;
   		}

	}*/ 
   
    @Secured(['ROLE_USER'])
    def bulkCreate(){
        def observationInstance = new Observation()
		observationInstance.properties = params;
		def author = springSecurityService.currentUser;
		def lastCreatedObv = Observation.find("from Observation as obv where obv.author=:author and obv.isDeleted=:isDeleted order by obv.createdOn desc ",[author:author, isDeleted:false]);
		def filePickerSecurityCodes = utilsService.filePickerSecurityCodes();
		return [observationInstance: observationInstance, 'lastCreatedObv':lastCreatedObv, 'springSecurityService':springSecurityService, 'userInstance':author, 'policy' : filePickerSecurityCodes.policy, 'signature': filePickerSecurityCodes.signature] 
    }

    @Secured(['ROLE_USER'])
    def bulkSave(){
        log.debug params;
        if(request.method == 'POST') {
            //TODO:edit also calls here...handle that wrt other domain objects
            params.locale_language = utilsService.getCurrentLanguage(request);
            def result = observationService.saveObservation(params, false)
            if(result.success){
                forward(action: 'addRecommendationVote', params:params);
            } else {
                def output = [:]
                def miniObvCreateHtml = g.render(template:"/observation/miniObvCreateTemplate", model:[observationInstance: result.observationInstance]);
                output = [miniObvCreateHtml: miniObvCreateHtml, observationInstance: result.observationInstance, lastCreatedObv:null, statusComplete: false]
                render output as JSON
            }
        } else {
            redirect (url:uGroup.createLink(action:'bulkCreate', controller:"observation", 'userGroupWebaddress':params.webaddress))
        }
    }

    def getProcessedImageStatus = {
        if(!(params.jobId)) {
            return;
        }
        def pi = ProcessImage.get(params.jobId?.toLong());
        def output = [:];
        output = ['imageStatus':pi.status];
        render output as JSON
        return;
    }

    def testy(){
	    //setupService.uploadFields("/tmp/FrenchDefinitions.xlsx");
	    println speciesService.checking();
    	//return false;
        //render springSecurityFilterChain
    }

    def filePickerSecurityCodes() {
        utilsService.filePickerSecurityCodes();
    }
}
