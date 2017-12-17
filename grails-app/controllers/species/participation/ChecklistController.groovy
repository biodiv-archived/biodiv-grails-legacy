package species.participation

import grails.converters.JSON
import species.License;
import species.groups.SpeciesGroup;
import grails.plugin.springsecurity.annotation.Secured;
import species.Resource.ResourceType;
import species.Habitat;

class ChecklistController {
	
	def userGroupService;
	def activityFeedService;
	def springSecurityService;
	def checklistService;
	def grailsApplication
	def checklistUtilService
	def observationService
	def chartService
    def utilsService;
	
	def index = {
        params.isChecklist = true;
		redirect(controller:'dataTable', action:'list', params: params)
	}

	def list() {
		params.isChecklistOnly = "" + true
		redirect(controller:'observation', action:'list', params: params)
	}

	def show() {
		if(params.id) {
			def checklistInstance = Observation.findByIdAndIsDeleted(params.id.toLong(), false)
			if (!checklistInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'checklist.label', default: 'Checklist'), params.id])}"
				redirect (url:uGroup.createLink(action:'list', controller:"checklist", 'userGroupWebaddress':params.webaddress))
			}else{
				//if this instance is not checklists instance then redirecting to observation
				if(!checklistInstance.instanceOf(Checklists)){
					redirect(controller:'observation', action:'show', params: params)
					return
				}
				//refetching checklist and  all observation in one query
				//checklistInstance = Checklists.findByIdAndIsDeleted(params.id.toLong(), false, [fetch: [observations: 'join']])
				def userLanguage = utilsService.getCurrentLanguage(request);
				checklistInstance.incrementPageVisit()
				def userGroupInstance;
				params.max = params.max?params.max.toInteger():50 
				if(params.webaddress) {
					userGroupInstance = userGroupService.get(params.webaddress);
				}
				if(params.pos) {
					int pos = params.int('pos');
					def obsController = new ObservationController()
					def prevNext = obsController.getPrevNextObservations(pos, params.webaddress);
					//def prevNext = getPrevNextChecklists(pos, params.webaddress);
					if(prevNext) {
						[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, prevObservationId:prevNext.prevObservationId, nextObservationId:prevNext.nextObservationId, lastListParams:prevNext.lastListParams,userLanguage:userLanguage]
					} else {
						[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress,userLanguage:userLanguage]
					}
				} else {
					[checklistInstance: checklistInstance, 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress,userLanguage:userLanguage]
				}
			}
		} else {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'checklist.label', default: 'Checklist'), params.id])}"
				redirect (url:uGroup.createLink(action:'list', controller:"checklist", 'userGroupWebaddress':params.webaddress))
        }
	}

	def snippet = {
		def checklistInstance = Checklists.read(params.id)
		render (template:"/common/checklist/showChecklistSnippetTabletTemplate", model:[checklistInstance:checklistInstance, 'userGroupWebaddress':params.webaddress]);
	}
	
	private getChecklistCount(speciesGroup, userGroupInstance){
		return Checklists.withCriteria(){
			projections {
				count('id')
			}
			and{
				eq('isDeleted', false)
				if(speciesGroup){
					eq('group', speciesGroup)
				}
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
		}[0]
	}
	
	def count() {
		render chartService.getChecklistCount(params)
	}
	
	@Secured(['ROLE_USER'])
	def create() {
		def checklistInstance = new Checklists(license:License.findByName(License.LicenseType.CC_BY))
		checklistInstance.properties = params;
		checklistInstance.habitat = Habitat.findByName(Habitat.HabitatType.ALL.value())
		def filePickerSecurityCodes = utilsService.filePickerSecurityCodes();
		return [observationInstance: checklistInstance, 'policy' : filePickerSecurityCodes.policy, 'signature': filePickerSecurityCodes.signature]
	}
	
	@Secured(['ROLE_USER'])
	def save() {
		if(request.method == 'POST') {
			def result = saveAndRender(params)
            if(result.success){
                redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:result.checklistInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
            }else{
                //flash.error = result.msg;//"${message(code: 'error')}";
                render(view: "create", model: [observationInstance: result.checklistInstance, msg:result.msg, checklistData:params.checklistData.encodeAsJSON(), checklistColumns:params.checklistColumns.encodeAsJSON(), sciNameColumn:params.sciNameColumn, commonNameColumn:params.commonNameColumn, latitude:params.latitude, longitude:params.longitude, obvDate:params.obvDate])
            }
		} else {
			redirect (url:uGroup.createLink(action:'create', controller:"checklist", 'userGroupWebaddress':params.webaddress))
		}
		
	}

	private saveAndRender(params, sendMail=true){
		params.locale_language = utilsService.getCurrentLanguage(request);
		updateParams(params)
		return checklistService.saveChecklist(params, sendMail=true)
	}
	
	private updateParams(params){
		params.checklistData = JSON.parse(params.checklistData)
		params.checklistColumns = JSON.parse(params.checklistColumns)
		List columnList = params.checklistColumns.collect { it.name }
		
		if(columnList.contains(ChecklistService.MEDIA_COLUMN)){
			columnList.remove(ChecklistService.MEDIA_COLUMN);
		}
		
		if(params.action == 'edit' ||params.action == 'update'){
			if(columnList.indexOf(ChecklistService.OBSERVATION_COLUMN) == 0){
				columnList.remove(0);
			}
		}
		
		//getting sn and cn column in front of checklist
		if(params.commonNameColumn){
			columnList.remove(params.commonNameColumn)
			columnList.add(0, params.commonNameColumn)
		}
		
		if(params.sciNameColumn){
			columnList.remove(params.sciNameColumn)
			columnList.add(0, params.sciNameColumn)
		}
		
		
		def validColumnList = []
		columnList.each{
			def colName =  it.trim()
			if(colName != "" && !validColumnList.contains(colName)){
				validColumnList << colName
			}
		}
		params.columns = validColumnList
	} 
	
	@Secured(['ROLE_USER'])
	def edit() {
		def observationInstance = Checklists.findByIdAndIsDeleted(params.id?.toLong(), false)
		if (!observationInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
		} else if(utilsService.ifOwns(observationInstance.author)) {
            def checklist = getChecklistData(params.id.toLong());
			render(view: "create", model: [observationInstance: observationInstance, 'springSecurityService':springSecurityService, sciNameColumn:observationInstance.sciNameColumn, commonNameColumn:observationInstance.commonNameColumn])
		} else {
			flash.message = "${message(code: 'edit.denied.message')}"
			redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:observationInstance.id, 'userGroupWebaddress':params.webaddress))
		}
	}
	
	@Secured(['ROLE_USER'])
	def update() {
		def observationInstance = Checklists.findByIdAndIsDeleted(params.id?.toLong(), false)
		if(observationInstance)	{
			def result = saveAndRender(params, false)
            if(result.success){
                redirect (url:uGroup.createLink(action:'show', controller:"checklist", id:result.checklistInstance.id, 'userGroupWebaddress':params.webaddress, postToFB:(params.postToFB?:false)))
            }else{
                flash.error = result.msg;//"${message(code: 'error')}";
                render(view: "create", model: [observationInstance: result.checklistInstance, 'msg':result.msg, checklistData:params.checklistData, checklistColumns:params.checklistColumns, sciNameColumn:params.sciNameColumn, commonNameColumn:params.commonNameColumn])
            }

		}else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'observation.label', default: 'Observation'), params.id])}"
			redirect (url:uGroup.createLink(action:'list', controller:"observation", 'userGroupWebaddress':params.webaddress))
		}
	}

    private getChecklistData(Long id) {
        if(!id) return [];

		String obv_id = ChecklistService.OBSERVATION_COLUMN
		Checklists cl = Checklists.findByIdAndIsDeleted(id, false, [fetch: [observations: 'join']])
        if(cl) {
            def obvData = []
            cl.observations.each {Observation obv ->
                def tMap = [:]
                tMap[ChecklistService.OBSERVATION_COLUMN] = obv.id
                if(obv.resource) {
                    tMap[ChecklistService.MEDIA_COLUMN] = [obv.resource.size()];
                    Iterator iterator = obv.resource?.iterator();
                    int index = 0;
                    String obvDir;
                    while(iterator.hasNext()) {
                        def res = iterator.next();
                        def r = new HashMap()
                        r['file'] = res.fileName;
                        r['thumbnail'] = res.thumbnailUrl();
                        r['url'] = res.url;
                        r['license'] = res.license.name;
                        r['type'] = res.type.name();
                        r['rating'] = res.rating;
                        if(res.type != ResourceType.VIDEO) {
                            obvDir = res.fileName.split('/')[1];
                        }
                        tMap[ChecklistService.MEDIA_COLUMN][index] = r
                        index++
                    }
                    if(obvDir)
                        tMap['obvDir'] = obvDir;
                }
                obv.fetchChecklistAnnotation().each { ann ->
                    tMap[ann.key] = ann.value
                }
                obvData.add(tMap)
            }
            
            List columns = [obv_id]
            cl.fetchColumnNames().each { columns.add(it) }
            
            return [columns: columns, data :obvData, res:'checklist', sciNameColumn:cl.sciNameColumn, commonNameColumn:cl.commonNameColumn]
        }
        return ['error':"Couldn't find checklist with this id"];
    }

	def getObservationGrid =  {
		log.debug params
		render getChecklistData(params.id?.toLong()) as JSON
	}
	
	@Secured(['ROLE_USER'])
	def flagDeleted() {
		def result = observationService.delete(params)
		flash.message = result.message
		redirect (url:result.url)
	}
	
	def observationData = {
        if(!params.id) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'checklist.label', default: 'Checklist'), params.id])}"
            redirect (url:uGroup.createLink(action:'list', controller:"checklist", 'userGroupWebaddress':params.webaddress))
        }
		def observations = checklistService.getObservationData(params.id, params)
        def checklistInstance = Checklists.read(params.id.toLong());
		def model =[observations:observations, checklistInstance:checklistInstance, observationsCount:checklistInstance.speciesCount]
		render(template:"/common/checklist/showChecklistDataTemplate", model:model);
	}

	def search() {
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		def model = checklistService.search(params);
		
		model['isSearch'] = true;
		
		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/common/checklist/showChecklistListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
			return;
		} else {
			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/common/checklist/showChecklistListTemplate", model:model);
			model.resultType = "checklist"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
			
			render (result as JSON)
			return;
		}
	}

	def terms() {
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		
		List result = checklistService.nameTerms(params)

		render result.value as JSON;
	}
	
	@Secured(['ROLE_ADMIN'])
	def test() {
		checklistService.serializeClData()
		render " done  "
	}

	@Secured(['ROLE_ADMIN'])
    def migrateChecklistToDataTable() {
        checklistService.migrateChecklistToDataTable();
    }
}
