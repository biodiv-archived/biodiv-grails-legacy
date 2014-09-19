package species.participation


import groovy.sql.Sql
import grails.converters.JSON

import java.io.File;
import java.text.SimpleDateFormat
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.springframework.transaction.annotation.Transactional;


import species.License;
import species.Reference;
import species.Species;
import species.Metadata;
import species.Contributor;
import species.participation.RecommendationVote.ConfidenceType;
import species.utils.Utils;
import species.sourcehandler.XMLConverter;
import species.formatReader.SpreadsheetReader;

//pdf related
import au.com.bytecode.opencsv.CSVWriter
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image

import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class ChecklistService {

	static transactional = false

	static final String SN_NAME_KEY = "scientific_name"
	static final String SN_NAME = "scientific_name" //"scientific_names" //"Scientific Name" //"scientific_name"
	static final String CN_NAME = "common_name"
	static final String OBSERVATION_COLUMN = "Id"
	static final String MEDIA_COLUMN = "Media"
	static final String SPECIES_TITLE_COLUMN = "speciesTitle"
	static final String SPECIES_ID_COLUMN = "speciesId"
	def grailsApplication
	def observationService
	def checklistSearchService;
	def obvUtilService;
	def springSecurityService;
	def activityFeedService;
	def observationsSearchService;
	def dataSource;
	def utilsService
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Create ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	
	Checklists createChecklist(params) {
		Checklists checklist = new Checklists();
		updateChecklist(params, checklist);
		return checklist
	}
	
	void updateChecklist(params, Checklists checklist){
		//removing time component from date
		params.fromDate = observationService.parseDate(params.fromDate)?.format("dd/MM/yyyy");
		params.toDate =  observationService.parseDate(params.toDate)?.format("dd/MM/yyyy");
		
		observationService.updateObservation(params, checklist)
		
		checklist.title =  params.title
		checklist.license = License.findByName(License.fetchLicenseType(params.license_0))  
		checklist.refText =  params.refText
		checklist.sourceText =  params.sourceText
		checklist.rawChecklist =  params.rawChecklist ?:checklist.rawChecklist
		checklist.publicationDate =  params.publicationDate ? observationService.parseDate(params.publicationDate) : null
		checklist.reservesValue =  params.reservesValue
		checklist.sciNameColumn =  params.sciNameColumn
		checklist.commonNameColumn =  params.commonNameColumn
		checklist.columns =  params.columns?params.columns as JSON:checklist.columns
		
		checklist.isChecklist = true
	}
	
	Map saveChecklist(params, sendMail=true){
		params.author = springSecurityService.currentUser;
		def checklistInstance, feedType, feedAuthor, mailType, isGlobalUpdate = false;
		try {
			
			if(params.action == "save"){
				//create new checklist 
				checklistInstance = createChecklist(params);
				feedType = activityFeedService.CHECKLIST_CREATED
				feedAuthor = checklistInstance.author
				mailType = feedType 
			}else{
				//updates old checklist
				checklistInstance = Checklists.get(params.id.toLong())
				params.author = checklistInstance.author;
				updateChecklist(params, checklistInstance)
				feedType = activityFeedService.CHECKLIST_UPDATED
				feedAuthor = springSecurityService.currentUser
				//so that original author of checklist should not change
				//to say if all obv needs to be updated because some change in MetaData properties
				isGlobalUpdate = isGlobalUpdateForObv(checklistInstance)
                mailType = feedType
			}
			
            // ignoring saving when there is no valid observation
            boolean validObvPresent = false
            for(Map m in params.checklistData) {
                def oldObvId = m.OBSERVATION_COLUMN
                if(isValidObservation(m, oldObvId, checklistInstance)){
                    validObvPresent = true;
                    break;
                }
            }
            if(!validObvPresent) {
				return ['success' : false, 'msg':'No valid observation present. Ignoring saving checklist', checklistInstance:checklistInstance]
            }

			if(validObvPresent && !checklistInstance.hasErrors() && checklistInstance.save(flush:true)) {
				log.debug "Successfully created checklistInstance : "+checklistInstance
				activityFeedService.addActivityFeed(checklistInstance, null, feedAuthor, feedType);
				
				saveAttributions(params, checklistInstance)
				observationService.saveObservationAssociation(params, checklistInstance)
				
				if(sendMail)
					observationService.sendNotificationMail(mailType, checklistInstance, null, params.webaddress);
				
				saveObservationFromChecklist(params, checklistInstance, isGlobalUpdate)
				observationsSearchService.publishSearchIndex(checklistInstance, true);
					
				return ['success' : true, 'msg':'Successfully saved checklist.', checklistInstance:checklistInstance]
			} else {
				checklistInstance.errors.allErrors.each { log.error it }
				return ['success' : false, 'msg':checklistInstance.errors, checklistInstance:checklistInstance]
			}
		} catch(e) {
			e.printStackTrace();
			return ['success' : false, 'msg':"Error in saving checklist : ${e.getMessage()}", checklistInstance:checklistInstance]
		}
	}
	
	@Transactional
	private saveAttributions(params, checklist){
		if(params.attributions){
			checklist.attributions?.clear()
			def contributor =  XMLConverter.getContributorByName(params.attributions.trim(), true)
			checklist.addToAttributions(contributor)
		}
	}
	
	@Transactional
	private saveObservationFromChecklist(params, checklistInstance, boolean isGlobalUpdate){
	    if(!params.checklistData && !isGlobalUpdate)
			return
            
		//Checklists.withTransaction() {
			
			checklistInstance = Checklists.get(checklistInstance.id)
			log.debug "adding observation to checklist " + checklistInstance.title
			Set updatedObv = new HashSet()
			Set newObv = new HashSet()
			def commonObsParams = getParamsForObv(params, checklistInstance)
			
			//Each entry in checklistData represent one observatoin
			// if it has observation id column then those observation needs to be updated else new observation will be created
			// checklist Edit page will return only dirty list that needs to be updated + new rows that will create new observation
			// checklist save page will have all new rows that will create new observation
			params.checklistData.each {  Map m ->
				def oldObvId = m.remove(OBSERVATION_COLUMN)
				if(isValidObservation(m, oldObvId, checklistInstance)){
					def media = m.remove(MEDIA_COLUMN);
					m.remove(SPECIES_TITLE_COLUMN);
					m.remove(SPECIES_ID_COLUMN);

	                Map obsParams = new HashMap(commonObsParams);
	                if(media) {
	                    media.eachWithIndex{ item, index ->
	                        item.each { key, value ->
	                            obsParams.put(key+'_'+index, value);
	                        }
	                    }
	                }
	
					// for old observation
					if(oldObvId){
						obsParams.action = "update"
						obsParams.id = oldObvId
						updatedObv.add(oldObvId)
					}else{
						obsParams.action = "save"
					}
					
					obsParams.checklistAnnotations =  getSafeAnnotation(m, checklistInstance.fetchColumnNames())
					def res = observationService.saveObservation(obsParams, false)
					Observation observationInstance = res.observationInstance
					saveReco(observationInstance, m, checklistInstance)
					
					if(!oldObvId){
						checklistInstance.addToObservations(observationInstance)
						newObv.add(observationInstance.id)
					}
				}
			}
			//if any global thing (ie. species group, habitat) changes then updating all the observation
			if(isGlobalUpdate){
				commonObsParams.action = "update"
				commonObsParams.checklistAnnotations = null
				checklistInstance.observations.each { obv ->
					if(!updatedObv.contains(obv.id) && !(newObv.contains(obv.id))){
						commonObsParams.id = obv.id
						observationService.saveObservation(commonObsParams, false, false)
					}
				}
			}
			//updating obv count
			checklistInstance.speciesCount = (checklistInstance.observations) ? checklistInstance.observations.size() : 0
			if(!checklistInstance.save(flush:true) || checklistInstance.hasErrors()){
				checklistInstance.errors.allErrors.each { log.error it }
			}
		//}
			
		log.debug "saved checklist observations"
	}
	
	private boolean isValidObservation(m, oldObvId, cl){
		if(oldObvId){
			return true
		}
		def media = m[MEDIA_COLUMN]
		def snCol = m[cl.sciNameColumn]
		def cnCol = m[cl.commonNameColumn]
		
		return snCol || cnCol || media
	}
	
	private getSafeAnnotation(Map m, List validColumns){
		def newMap = [:]
		m.each { k, v ->
			if(validColumns.contains(k.trim()) && v && !m.isNull(k)){
				newMap.put(k.trim(), v.trim())
			}
		}
		return newMap as JSON
	}
	
	private Map getParamsForObv(params, checklistInstance){
		Map obvParams = new HashMap(params)
		obvParams.sourceId = checklistInstance.id
		
		obvParams.remove("checklistData")
		obvParams.remove("checklistColumns")
		
		obvParams.remove("tags")
		obvParams.remove("userGroupsList")
		obvParams.remove("groupsWithSharingNotAllowed")
		
		return obvParams
	}
	
	private saveReco(Observation obv, Map m, Checklists cl){
		def safeMap = new HashMap()
		m.each { k, v ->
			if(k.trim() && v && !m.isNull(k)){
				safeMap.put(k.trim(), v.trim())
			}
		}
		
		m = safeMap
		
		def res = observationService.getRecommendation([recoName:m[cl.sciNameColumn], commonName: m[cl.commonNameColumn]])
		
		if(!isNewReco(obv,res))
			return
		
		def reco = res.mainReco
		def cnReco = res.commonNameReco
		
		ConfidenceType confidence = observationService.getConfidenceType(ConfidenceType.CERTAIN.name());
		RecommendationVote recommendationVoteInstance = new RecommendationVote(observation:obv, recommendation:reco, commonNameReco:cnReco, author:obv.author, confidence:confidence, votedOn:obv.fromDate);
		
		def user = obv.author;
		def oldRecoVote = RecommendationVote.findWhere(observation:obv, author:user)
		if(oldRecoVote){
			oldRecoVote.delete(flush:true)
		}
		if(!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush:true)) {
			log.debug "Successfully added reco vote : "+recommendationVoteInstance.id
			activityFeedService.addActivityFeed(obv, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED);
			//saving max voted species name for observation instance
			obv.maxVotedReco = reco
		}else{
			recommendationVoteInstance.errors.allErrors.each { log.error it }
			throw new RuntimeException("Error during reco vote save");
		}
	}
	
	/**
	 * 
	 * @param obv
	 * @param res
	 * @return 
	 * To check if any thing got changed in marked column of grid . if yes then saving new recoVote
	 */
	private boolean isNewReco(Observation obv, Map res){
		def reco = res.mainReco
		def cnReco = res.commonNameReco
		
		if(!reco){
			return false
		}
		
		if(obv.fetchSpeciesCall() != reco.name)
			return true
		
		def user = obv.author;
		def oldRecoVote = RecommendationVote.findWhere(observation:obv, author:user)
		
		if(!oldRecoVote || oldRecoVote.recommendation.name != reco.name || oldRecoVote.commonNameReco?.name != cnReco?.name)
			return true
		
		return false
	}
	
	/**
	 * 
	 * @param cl
	 * @return
	 * Flag to tell if some thing got changes in checklist which must be update on all of its observation
	 */
	
	private boolean isGlobalUpdateForObv(Checklists cl){
		if(!cl.isDirty()){
			return false
		}
		List dirtyPropList = Checklists.fetchDirtyFields()
		for (String prop : dirtyPropList) {
			if(cl.isDirty(prop)){
				return true
			}
		}
		return false
	}
	
	/**
	 * 
	 * @param obv
	 * @param m
	 * @param columns
	 * @return
	 * this method will store fresh annotations .clear any old annotation if exist
	 * XXX have to change this method to store serialize map for checklist row data
	 */
	
	private saveObservationAnnotation(obv, Map m, List columns){
		obv.checklistAnnotations = m
		/*
		obv.annotations = []
		obv.save(flush:true)
		
		m.each { k, v ->
			observationService.addAnnotation(['key': k, 'value': v, 'columnOrder':columns.indexOf(k), 'sourceType': Checklists.class.getCanonicalName()], obv)
		}
		if(!obv.hasErrors() && obv.save(flush:true)) {
			log.debug "saved observation meta data $obv"
		}else{
			obv.errors.allErrors.each { log.error it }
			throw new RuntimeException("Error during meta data save");
		}
		*/
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Search related /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = checklistSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Checklists"]);
		}
		return result;
	}

	def search(params) {
		def result;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
		def queryParams = [:]
		def activeFilters = [:]

		NamedList paramsList = new NamedList();
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";

		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap) {
			params.aq.each { key, value ->
				queryParams["aq."+key] = value;
				activeFilters["aq."+key] = value;
				if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
					if(i++ == 0) {
						aq = key + ': ('+value+')';
					} else {
						aq = aq + " AND " + key + ': ('+value+')';
					}
				}
			}
		}

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}

		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.long('offset') : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1 && sort.indexOf(' asc') == -1 ) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		queryParams["max"] = max
		queryParams["offset"] = offset

		paramsList.add('fl', params['fl']?:"id");

		if(params.sGroup) {
			params.sGroup = params.sGroup.toLong()
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			} else{
				paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}

		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'species'
			activeFilters["tag"] = params.tag
		}
		if(params.user){
			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
		}

		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					//AS we dont have selecting species for group ... we are ignoring this filter
					//paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}

		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = checklistSearchService.search(paramsList);
			List<Species> checklistInstanceList = new ArrayList<Species>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def checklistInstance = Checklist.get(doc.getFieldValue("id"));
				if(checklistInstance)
					checklistInstanceList.add(checklistInstance);
			}

			//queryParams = queryResponse.responseHeader.params
			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), checklistInstanceList:checklistInstanceList, snippets:queryResponse.getHighlighting()]
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}

	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("score"))
			return true
		else
			return false;
	}

	
	def List getObservationData(id, params=[:]){
        //Done because of java melody error - junk coming with offset value
        params.offset = params.offset ? params.offset.tokenize("/?")[0] : 0;
		params.max = params.max ? params.max.toInteger() :50
		params.offset = params.offset ? params.offset.toInteger() :0
		def sql =  Sql.newInstance(dataSource);
		def query = "select observation_id  as obv_id from checklists_observation where checklists_observations_id = " + id + " order by observations_idx limit " + params.max + " offset " + params.offset;
		def res = []
		sql.rows(query).each{
			res << Observation.read(it.getProperty("obv_id"));
		}
		return res 
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////  Export ////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	def export(params, DownloadLog dlog){
		log.debug(params)
		def cl = Checklists.read(params.downloadObjectId.toLong())
		if(!cl){
			return null
		}

		File downloadDir = new File(grailsApplication.config.speciesPortal.checklist.checklistDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		if(dlog.type == DownloadLog.DownloadType.CSV){
			return exportAsCSV(cl, downloadDir)
		}else{
			return exportAsPDF(cl, downloadDir)
		}
	}

	private File exportAsPDF(Checklists cl, downloadDir){
		log.debug "Writing pdf checklist" + cl
		File pdfFile = new File(downloadDir, "checklist_" + new Date().getTime() + ".pdf")
		Document document = new Document()
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile))

		document.open()
		Map m = cl.fetchExportableValue(true)


		//adding site banner
		Image image2 = Image.getInstance(grailsApplication.config.speciesPortal.app.rootDir + "/sites/all/themes/ibp/images/map-logo.gif");
		//image2.scaleToFit(120f, 120f);
		document.add(image2);

		//writing meta data
		com.itextpdf.text.List list = new com.itextpdf.text.List(10);
		for(item in m[cl.META_DATA]){
			list.add(new ListItem(item.join("  ")));
		}
		document.add(list)

		//writing data
		def tmpColumnNames = cl.fetchColumnNames()
		def columnNames = ["s.no"]
		for(c in tmpColumnNames){
			if(c.equalsIgnoreCase(cl.sciNameColumn) || c.equalsIgnoreCase(cl.commonNameColumn)){
				columnNames.add(c)
			}
		}
		columnNames.add("notes")
		PdfPTable t = new PdfPTable(columnNames.size())
		t.setSpacingBefore(25);
		t.setSpacingAfter(25);

		//writing header
		for(c in columnNames){
			t.addCell(new PdfPCell(new Phrase(c)))
		}
		//t.setHeaderRows(1)
		//writing actual data
		for(item in m[cl.DATA]){
			for(obj in item){
				t.addCell(new PdfPCell(new Phrase(obj?:"")))
			}
		}
		document.add(t)

		document.close()
		return pdfFile
	}

	private File exportAsCSV(Checklists cl, downloadDir){
		File csvFile = new File(downloadDir, "checklist_" + new Date().getTime() + ".csv")
		CSVWriter writer = obvUtilService.getCSVWriter(csvFile.getParent(), csvFile.getName())
		log.debug "Writing csv checklist" + cl

		Map m = cl.fetchExportableValue()

		for(item in m[cl.META_DATA]){
			writer.writeNext(item.toArray(new String[0]))
		}

		writer.writeNext("## Checklist Data:")
		writer.writeNext(cl.fetchColumnNames().toArray(new String[0]))
		for(item in m[cl.DATA]){
			writer.writeNext(item.toArray(new String[0]))
		}

		writer.flush()
		writer.close()

		return csvFile
	}
	
	//////////////////////////////////// Migrate related ////////////////////////////////////////
	def serializeClData(){
		List  clIdList = Checklist.listOrderById(order: "asc").collect{it.id}
		clIdList.removeAll( [ 20, 72, 129, 221, 267, 304, 305])
		clIdList.each {  id ->
			def cl = Checklists.findById(id, [fetch: [observations: 'join']])
			if(!cl.observations.iterator().next().checklistAnnotations){
				Checklists.withTransaction(){
					cl.observations.each { obv ->
						def m = [:]
						obv.fetchChecklistAnnotation().each { a ->
							if(a.value){
								m.put(a.key.trim(), a.value.trim())
							}
						}
						obv.checklistAnnotations = m as JSON
						obv.save(flush:true)
					}
					
				}
				
				utilsService.cleanUpGorm(true)
				
				Checklists.withTransaction(){ 
					cl = Checklists.get(id)
					cl.columns = new ArrayList(cl.fetchColumnNames()).collect { it.trim() } as JSON
					cl.sciNameColumn = cl.sciNameColumn.trim()
					cl.commonNameColumn = cl.commonNameColumn ? cl.commonNameColumn.trim() : null
					if(!cl.save(flush:true)){
						cl.errors.allErrors.each { println it }
					}
				}
			}	
		}
	}
	
}
