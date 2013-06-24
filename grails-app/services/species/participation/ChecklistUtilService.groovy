package species.participation

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import org.springframework.transaction.annotation.Transactional;
import groovy.sql.Sql
import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List;

//csv related
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter;


import species.auth.SUser
import species.Species;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.CommonNames;
import species.Contributor
import species.Habitat;
import species.Language;
import species.License;
import species.Reference;

import species.participation.curation.UnCuratedVotes;
import species.utils.Utils;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.curation.UnCuratedCommonNames

import static species.participation.ChecklistService.*
/**
 * @author sandeept
 * Service created to hold old code of migration from drupal to grails checklist and 
 * old checklist to new checklists in grails
 *
 */
class ChecklistUtilService {

    static transactional = false
	
	def grailsApplication
	def observationService
	def sessionFactory
	def curationService
	def recommendationService
	def checklistSearchService;
	def obvUtilService;
	def activityFeedService;
	
	
	String connectionUrl =  "jdbc:postgresql://localhost/ibp";
	String userName = "postgres";
	String password = "postgres123";

	def dateFormatStrings =  Arrays.asList("yyyy-MM-dd'T'HH:mm:ss")
	def flushImmediately  = false // grailsApplication.config.speciesPortal.flushImmediately
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// create observation from checklist row //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	def migrateObservationFromChecklist(){
		def startTime = new Date()
		
		int created =0, error =0,  ignored =0
		def ignoreset = []
		def errorset = []
		//Checklist.withTransaction(){
		log.debug "   start time $startTime "
		
		def m = GrailsDomainBinder.getMapping(ActivityFeed.class)
		m.autoTimestamp = false
		//[Checklist.get(4)].each { Checklist cl ->
		//def cids = [12, 13]
		def cids = Checklist.listOrderById().collect { it.id}
		cids.each { id ->
			//loading all rows in one query
			Checklist cl = Checklist.findById(id, [fetch: [row: 'join']])
			if(!cl.latitude || !cl.longitude || !cl.placeName){
				log.debug " ingnoring cheklist $cl.id   $cl.title"
				ignored++
				ignoreset << cl.id
			}else{
				log.debug " =============================================  STARTING cheklist $cl.id   $cl.title"
				log.debug " =============================================  observations " +  cl.speciesCount
				try{
					def newChecklist = Checklists.get(cl.id)
					createObservationFromChecklist(cl, newChecklist)
					log.debug " =============================================  FINISH cheklist $cl.id   $cl.title"
					created++
					if(!newChecklist.save(flush:flushImmediately)){
						newChecklist.errors.allErrors.each { log.error it }
					}
					
				}catch (Exception e) {
					log.debug " ERROR cheklist $cl.id   $cl.title ================"
					e.printStackTrace()
					error++
					errorset << cl.id
				}
				cleanUpGorm(true)
			}
		}
		m.autoTimestamp = true
		log.debug "   start time $startTime    endTime " + new Date()
		//}
		println "========================= $created   $ignored  $error"
		println "============ ingnore set " + ignoreset
		println "============ error set " + errorset
	}
	
	def migrateChecklistAsObs(){
		def startTime = new Date()
		int created =0, error =0,  ignored =0
		def ignoreset = []
		def errorset = []
		def cids = Checklist.listOrderById().each{Checklist cl ->
			if(!cl.latitude || !cl.longitude || !cl.placeName){
				log.debug " ingnoring cheklist $cl.id   $cl.title"
				ignored++
				ignoreset << cl.id
			}else{
				log.debug " =============================================  STARTING cheklist $cl.id   $cl.title"
				log.debug " =============================================  observations " +  cl.speciesCount
				try{
					createMetaObservation(cl)
					created++
				}catch (Exception e) {
					log.debug " ERROR cheklist $cl.id   $cl.title ================"
					e.printStackTrace()
					error++
					errorset << cl.id
				}
			}
		}
		cleanUpGorm(true)
		log.debug "   start time $startTime    endTime " + new Date()
		println "========================= $created   $ignored  $error"
		println "============ ingnore set " + ignoreset
		println "============ error set " + errorset
	}
	
	
	
	@Transactional
	private Checklists createMetaObservation(Checklist cl){
		Checklists newChecklist = new Checklists()
		updateMetaObservation(getBasicObvData(cl), cl, newChecklist)
		newChecklist.isChecklist = true
		
		//XXX commnet this when going on production
		//observationInstance.createdOn = observationInstance.lastRevised = new Date()
		if(!newChecklist.hasErrors() && newChecklist.save(flush:flushImmediately)) {
			log.debug "saved observation $newChecklist "
			//activityFeedService.addActivityFeed(observationInstance, null, observationInstance.author, activityFeedService.OBSERVATION_CREATED);
		}else{
			newChecklist.errors.allErrors.each { log.error it }
			throw new RuntimeException("Error during checklist meta observation save");
		}
		return newChecklist
	}
	
	@Transactional
	def createObservationFromChecklist(Checklist cl, Checklists newChecklist){
		int prevRowId = -1
		def rowData = []
		cl.row.each { ChecklistRowData r ->
			if(prevRowId == -1){
				prevRowId = r.rowId
			}
			
			if(prevRowId != r.rowId){
				createObservationFromRow(rowData, cl, newChecklist)
				rowData = []
				prevRowId = r.rowId
			}
			rowData.add(r)
		}
		//create obs from last row
		createObservationFromRow(rowData, cl, newChecklist)
	}
	
	
	private createObservationFromRow(List rowData, Checklist cl, Checklists newChecklist){
		def basicData = getBasicObvData(cl)
		def observationInstance = observationService.createObservation(basicData)
		observationInstance.createdOn = observationInstance.lastRevised = observationInstance.fromDate
		observationInstance.sourceId = newChecklist.id
		
		if(!observationInstance.hasErrors() && observationInstance.save(flush:flushImmediately)) {
			log.debug "saved observation $observationInstance.id"
			addActivityFeed(observationInstance, null, observationInstance.author, activityFeedService.OBSERVATION_CREATED, observationInstance.fromDate);
			//saving recommendation
			saveRecoVote(observationInstance, rowData)
			//saveMetaData
			saveMetaData(observationInstance, rowData)
			//XXX: uncomment this
			//Follow.addFollower(observationInstance, observationInstance.author)
			//adding observation to checklist replacing checklist row
			newChecklist.addToObservations(observationInstance)
			
		}else{
			observationInstance.errors.allErrors.each { log.error it }
			throw new RuntimeException("Error during observation save");
		}
	}
	
	private saveRecoVote(Observation obv, List rowData){
		ChecklistRowData snData, cnData
		rowData.each {ChecklistRowData r ->
			if(r.key.equalsIgnoreCase(ChecklistService.SN_NAME)){
				snData = r
			}else if (r.key.equalsIgnoreCase(ChecklistService.CN_NAME)){
				cnData = r
			}
		}
		
		if(snData.reco){
			def cnReco
			if(cnData){
				def languageId = Language.getLanguage(null).id;
				cnReco = recommendationService.findReco(cnData.value, false, languageId, null);
			}
			//println " ============ saving    $snData.value     $snData.reco  $obv   $snData"
			ConfidenceType confidence = observationService.getConfidenceType(ConfidenceType.CERTAIN.name());
			RecommendationVote recommendationVoteInstance = new RecommendationVote(observation:obv, recommendation:snData.reco, commonNameReco:cnReco, author:obv.author, confidence:confidence, votedOn:obv.fromDate);
			if(!recommendationVoteInstance.hasErrors() && recommendationVoteInstance.save(flush:flushImmediately)) {
				log.debug "Successfully added reco vote : "+recommendationVoteInstance.id
				addActivityFeed(obv, recommendationVoteInstance, recommendationVoteInstance.author, activityFeedService.SPECIES_RECOMMENDED, new Date(obv.fromDate.getTime() + 1));
				//saving max voted species name for observation instance
				obv.maxVotedReco = snData.reco
			}else{
				recommendationVoteInstance.errors.allErrors.each { log.error it }
				throw new RuntimeException("Error during reco vote save");
			}
		}
	}
	
	private saveMetaData(obv, List rowData){
		rowData.each { ChecklistRowData r ->
			observationService.addAnnotation(['key': r.key, 'value': r.value, 'columnOrder':r.columnOrderId, 'sourceType': Checklists.class.getCanonicalName()], obv)
		}
		if(!obv.hasErrors() && obv.save(flush:flushImmediately)) {
			log.debug "saved observation meta data $obv"
		}else{
			obv.errors.allErrors.each { log.error it }
			throw new RuntimeException("Error during meta data save");
		}
	}
	
	private getBasicObvData(Checklist cl){
		def observation = [:]
		observation.author = cl.author
		observation.group_id= cl.speciesGroups.iterator().next().id
		observation.notes = cl.description
		observation.observedOn = parseDate(cl);
		observation.placeName = cl.placeName;
		observation.reverse_geocoded_name = cl.placeName;
		observation.latitude = '' + cl.latitude;
		observation.longitude = '' +  cl.longitude;
		observation.location_accuracy = 'Approximate'
		observation.habitat_id = Habitat.findByName("All").id
		observation.agreeTerms = 'on'
		return observation
	}
	
	
	private updateMetaObservation(params, Checklist oldCl, Checklists observation){
		if(params.author)  {
			observation.author = params.author;
		}

		if(params.url) {
			observation.url = params.url;
		}
		observation.group = SpeciesGroup.get(params.group_id);
		observation.notes = params.notes;
		
		observation.reverseGeocodedName = params.reverse_geocoded_name;
		observation.placeName = params.place_name?:observation.reverseGeocodedName;
		observation.location = 'POINT(' + params.longitude + ' ' + params.latitude + ')'
		observation.latitude = params.latitude.toFloat();
		observation.longitude = params.longitude.toFloat();
		observation.locationAccuracy = params.location_accuracy;
		observation.geoPrivacy = false;
		observation.habitat = Habitat.get(params.habitat_id);
		observation.agreeTerms = (params.agreeTerms?.equals('on'))?true:false;
		
		
		def newChecklist = observation
		newChecklist.id =  oldCl.id
		
		newChecklist.title =  oldCl.title
		newChecklist.speciesCount =  oldCl.speciesCount
		newChecklist.license =  oldCl.license
		newChecklist.refText =  oldCl.refText
		newChecklist.sourceText =  oldCl.sourceText
		newChecklist.rawChecklist =  oldCl.rawChecklist
		newChecklist.columnNames =  oldCl.columnNames
		newChecklist.fromDate =  oldCl.fromDate
		newChecklist.toDate =  oldCl.toDate
		newChecklist.publicationDate =  oldCl.publicationDate
		newChecklist.reservesValue =  oldCl.reservesValue
		observation.fromDate = observation.fromDate ?: parseDate1(params.observedOn);
		observation.createdOn = observation.lastRevised = observation.fromDate
		
		if(oldCl.attribution){
			def contributor = new Contributor(name:oldCl.attribution)
			contributor.save()
			newChecklist.addToAttributions(contributor)
		}
		
		if(oldCl.state) {
			oldCl.state.each { newChecklist.addToStates(it) }
		}
		if(oldCl.district) {
			oldCl.district.each { newChecklist.addToDistricts(it) }
		}
		if(oldCl.taluka) {
			oldCl.taluka.each { newChecklist.addToTalukas(it) }
		}
	}
	
	private parseDate(Checklist cl){
		def date = (cl.fromDate ?:(cl.toDate?:cl.lastUpdated))
		return date.format("dd/MM/yyyy")
	}
	
	private Date parseDate1(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	//XXX this method is to add activity on back date only for checklist to observation migration
	private addActivityFeed(rootHolder, activityHolder, author, activityType, date){
		//to support discussion on comment thread
		def subRootHolderType = rootHolder?.class?.getCanonicalName()
		def subRootHolderId = rootHolder?.id
		if(activityHolder?.class?.getCanonicalName() == Comment.class.getCanonicalName()){
			subRootHolderType = activityHolder.class.getCanonicalName()
			subRootHolderId = (activityHolder.isMainThread())? activityHolder.id : activityHolder.fetchMainThread().id
		}
		
		ActivityFeed af = new ActivityFeed(author:author, activityHolderId:activityHolder?.id, \
						activityHolderType:activityHolder?.class?.getCanonicalName(), \
						rootHolderId:rootHolder?.id, rootHolderType:rootHolder?.class?.getCanonicalName(), \
						activityType:activityType, subRootHolderType:subRootHolderType, subRootHolderId:subRootHolderId,
						dateCreated :date, lastUpdated:date);
					
		if(!af.save(flush:flushImmediately)){
			af.errors.allErrors.each { log.error it }
			return null
		}
		//Follow.addFollower(rootHolder, author)
		return af
	}
	
	def addFollow(){
		def admin = SUser.findByUsername('admin')
		Observation.findByAuthor(admin).each { obv ->
			Follow.addFollower(obv, admin)
		}
	}
	
	def addRefObseravtionToChecklist(){
		Observation.findAllByIsChecklist(true).each { Observation obv ->
			Checklist cl = activityFeedService.getDomainObject(obv.sourceType, obv.sourceId)
			cl.refObservation = obv
			if(!cl.save(flush:true)){
				cl.errors.allErrors.each { log.error it }
			}
			
		}
	}
	
	//	@Transactional
	//	def udpateObv(Checklist cl){
	//		Observation.findAllBySourceTypeAndSourceId(cl.class.getCanonicalName(), cl.id).each { Observation obv ->
	//			obv.calculateMaxVotedSpeciesName()
	//		}
	//	}
	

	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Dropal to Grails checklist ///////////////////
	////////////////////////////////////////////////////////////////////////////
	
	def migrateNewChecklist(params){
		def startId = params.startId
		if(!startId || !startId.isNumber()){
			log.debug "Please enter valid start id"
			return
		}
		migrateChecklist(startId.toLong())
	}

	def migrateChecklist(startOffset){
		def startDate = new Date()
		def sql = Sql.newInstance(connectionUrl, userName, password, "org.postgresql.Driver");
		int i=0;
		sql.eachRow("select nid, vid, title from node where type = 'checklist' and nid = $startOffset") { row ->
			log.debug " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
			try{
				Checklist checklist = createCheckList(row, sql)
			}catch (Exception e) {
				println "=============================== EXCEPTION in create checklist ======================="
				println " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>     title ===  $i  $row.title  nid == $row.nid , vid == $row.vid"
				e.printStackTrace()
				println "====================================================================================="
			}
			i++
		}
		println "================= start date " + startDate
		println "================================= finish time " + new Date()
	}

	@Transactional
	def Checklist createCheckList(nodeRow, Sql sql){
		String query = "select * from content_type_checklist where nid = $nodeRow.nid and vid = $nodeRow.vid"
		def row = sql.firstRow(query)
		
		Checklist cl = new Checklist()

		cl.title = nodeRow.title
		cl.speciesCount = row.field_numentities_value
		cl.description = row.field_clinfo_value
		cl.attribution = row.field_attribution_value


		cl.license = getLicense(row.field_cclicense_value.toInteger())
		addSpeciesGroup(cl, nodeRow.nid, nodeRow.vid, cl.title, sql)
		cl.author = SUser.findByUsername("admin")
		cl.refText = row.field_references_value
		cl.sourceText = row.field_source_value
		//addReferences(cl, row.link)

		//write one file in web server location
		saveRawFile(cl, row.field_rawchecklist_value)
		
		//location related
		cl.placeName = row.field_place_value
		updateLocation(cl, nodeRow.nid, nodeRow.vid, sql)
		// date related
		cl.fromDate = getDate(row.field_fromdate_value)
		cl.toDate = getDate(row.field_todate_value)
		cl.publicationDate = getDate(row.field_publicationdate_value)
		cl.lastUpdated = getDate(row.field_updateddate_value)

		addGroup(cl, nodeRow.nid, sql)

		//others
		cl.reservesValue = row.field_checklist_reserves_value
		
		if(!cl.save(flush:true)){
			cl.errors.allErrors.each { log.error it }
			return null
		}else{
			//get actual data
			fillData(cl, row.field_rawchecklist_value)
			//log.debug "saved successfully  >>>>>>> " + cl
			return cl
		}
	}

	private addGroup(cl, nid, sql){
		def query = "select gid from domain_access where nid = $nid"
		def row = sql.firstRow(query)
		
		//XXX change to point wgp group here gid 2 represent checklist belongs to wgp group
		if(row.gid == 2){
			cl.addToUserGroups(UserGroup.read(1))
		}
		
		if(nid == 1959 || 1960){
			cl.addToUserGroups(UserGroup.read(2))
		}
	}

	private saveRawFile(cl, String rawText){
		def rootDir = grailsApplication.config.speciesPortal.checklist.rootDir
		
		def checklistRootDir = new File(rootDir);
		if(!checklistRootDir.exists()) {
			checklistRootDir.mkdir();
		}

		def currentChecklistDir = new File(checklistRootDir, UUID.randomUUID().toString());
		currentChecklistDir.mkdir();

		File file = new File(currentChecklistDir, "rawFile.csv");
		file.createNewFile()
		file << rawText
		log.debug "saved in file " + file.getAbsolutePath()
		cl.rawChecklist = file.getAbsolutePath()
	}

	private fillData(cl, String rawText){
		
		Set commonNameSet = new HashSet()
		Set sciNameSet = new HashSet()
		
		def formatType = detectFormat(rawText)
		
		

		if(formatType == "CSV"){
			parseCSV(cl,rawText, commonNameSet, sciNameSet)
		}else{
			parseTSV(cl, rawText, commonNameSet, sciNameSet)
		}

		if(!cl.save(flush:true)){
			cl.errors.allErrors.each { log.error it }
		}
		//log.debug "saved data as well " + cl

	}

	private String detectFormat(String txt){
		Scanner scanner = new Scanner(txt);
		String[] keys = scanner.nextLine().split(",");
		scanner.close()

		if(keys.length > 1){
			return "CSV"
		}
		return "TSV"
	}

	private parseCSV(cl, String rawText, commonNameSet, sciNameSet){
		CSVReader csvReader = new CSVReader(new StringReader(rawText))
		List arrayList = csvReader.readAll()

		//log.debug " total size of rows " + arrayList.size()

		int i = 0
		def keyNames = null
		for(ar in arrayList){
			if(!keyNames){
				keyNames = ar
			}else{
				populateData(cl, keyNames, ar, i++, commonNameSet, sciNameSet)
			}
		}
		csvReader.close()

		//Arrays.sort(keyNames)
		cl.columnNames = keyNames.join("\t");
	}

	private parseTSV(cl, String txt, commonNameSet, sciNameSet){
		Scanner scanner = new Scanner(txt);
		String[] keyNames = scanner.nextLine().split("\t");

		
		int i = 0
		while (scanner.hasNextLine()) {
			populateData(cl, keyNames, scanner.nextLine().split("\t"), ++i, commonNameSet, sciNameSet)
		}
		scanner.close()

		//Arrays.sort(keyNames)
		cl.columnNames = keyNames.join("\t");
	}




	private populateData(cl, String[] keys, String[] values, rowId, HashSet commonNameSet, HashSet sciNameSet){
		def snVal, snKey, snColumnOrder, cn
		for (int i = 0; i < keys.length; i++) {
			def key = keys[i].trim()
			def value = null
			if(i < values.length){
				value = values[i]
			}

			if(key.equalsIgnoreCase(SN_NAME) && value && (value.trim() != "") && (value.trim() != "?")){
				snVal = value
				snKey = key
				snColumnOrder = i
			}

			if(key.equalsIgnoreCase(CN_NAME)){
				cn = (value && (value.trim() != "") && (value.trim() != "No common name found"))? value.trim() : null
			}

			//storing all the key value pair except scientific name
			if(i != snColumnOrder){
				def clr = new ChecklistRowData(key:key, value:value, rowId:rowId, columnOrderId:i)
				cl.addToRow(clr);
			}
		}
		
		
		

		//handling scientific name infrastructre
		if(snColumnOrder != null && snVal){
			snVal = Utils.getCanonicalForm(snVal);
			if(sciNameSet.contains(snVal)){
				println "========================== duplicate sn ==============================" + snVal
				cleanUpGorm()
				sciNameSet.clear()
				commonNameSet.clear()
			}
			sciNameSet.add(snVal)
			
			
			if(cn){
				cn =  Utils.getTitleCase(Utils.cleanName(cn));
				if(commonNameSet.contains(cn)){
					println "========================== duplicate cn =======  $cn ================ for sn =======" + snVal
					cleanUpGorm()
					sciNameSet.clear()
					commonNameSet.clear()
				}
				sciNameSet.add(snVal)
				commonNameSet.add(cn)
			}
			
			
			Recommendation reco = observationService.getRecommendation([recoName:snVal, canName:snVal, commonName:cn, refObject:cl]).mainReco
//			log.debug "===================== reco info ========================" + reco
//			log.debug " species id " + reco.taxonConcept?.findSpeciesId()
//			log.debug " cannonical form " + reco.taxonConcept?.canonicalForm
			cl.addToRow(new ChecklistRowData(key:SN_NAME_KEY, value:snVal, rowId:rowId, reco:reco, columnOrderId:snColumnOrder))
		}
	}

	private addReferences(cl, linkText){
		//XXX needs to do more processing
		cl.addToReference(new Reference(url:linkText))
	}

	private License getLicense(licId){
		switch (licId) {
			case 1:
				return License.read(827)
			case 2:
				return License.read(826)
			case 3:
				return License.read(825)
			case 4:
				return License.read(824)
			case 5:
				return License.read(823)
			case 6:
				return License.read(822)
			default:
				return null;
		}
	}


	private addSpeciesGroup(cl, nid, vid, title, Sql sql){
		String query = "select common_name as cn from ibpcl_taxa as ita where ita.id in (" + sql.rows("select field_taxa_value as tt from content_field_taxa as t1 where t1.nid = $nid and t1.vid = $vid").collect{ rr -> rr.tt}.join(", ") + ")"
		sql.rows(query).each{ row ->
			cl.addToSpeciesGroups(resolveSpeciesGroup(row.cn, title))
		}
	}
	
	private SpeciesGroup resolveSpeciesGroup(taxaName, title){
		if(taxaName.equalsIgnoreCase("Insects")){
			taxaName = "Arthropods"
		}else if(taxaName.equalsIgnoreCase("Fungi including lichens")){
			taxaName = "Fungi"
		}else if("Algae".equalsIgnoreCase(taxaName)){
			taxaName = "Others"
		}else if(taxaName.equalsIgnoreCase("Higher Plants")){
			taxaName = "Plants"
		}else if(taxaName.equalsIgnoreCase("Viruses") ||taxaName.equalsIgnoreCase("Bacteria and Protozoans")){
			taxaName = "Others"
		}
		
		
		def sg = SpeciesGroup.findByName(taxaName)
		if(!sg){
				// in this case group name is invertebrates
				title = title.trim()
				if(title == "Checklist of Annelids of Punjab" || title ==  "Checklist of Protozoans of Punjab" || title == "Checklist of Nematodes of Punjab" || title == "Checklist of Platyhelminthes of Punjab" || title == "Checklist of Crustaceans of Punjab"){
					sg = SpeciesGroup.findByName("Others")
				}else if(title == "Checklist of Molluscs of Punjab"){
					sg = SpeciesGroup.findByName("Molluscs")
				}else if(title == "Checklist of Thrips of Punjab" || title == "Checklist of Dipterans of Punjab"){
					sg = SpeciesGroup.findByName("Arthropods")
				}else{
					sg = SpeciesGroup.findByName("Arthropods")
				}
			
		}
		if(!sg){
			sg = SpeciesGroup.findByName("Others")
		}
		return sg
	}


	private updateLocation(Checklist cl, nid, vid, Sql sql){
		def point = sql.firstRow("select ST_AsText(ST_Centroid(__mlocate__topology)) as tt from  public.lyr_210_india_checklists where ibp_node = " + nid)?.get("tt")
		if(point){
			//println "=================<<<<<<<<<<< $point >>>>>>>>>>>>>>>>>>>>> "
			parsePoint(cl, point)
		}

		//		def ss =  sql.rows("select field_taluks_value as tt from content_field_taluks where nid = $nid and vid = $vid").size()
		//		if(ss > 1){
		//			println "========================== more that one taluka " + ss
		//		}

		def talukas =  sql.rows("select field_taluks_value as tt from content_field_taluks where nid = $nid and vid = $vid").collect { it.get("tt") }
		//println "   taluea " + talukas
		if(talukas){
			talukas.each { taluka ->
				if(taluka){
					def res = sql.firstRow("select ST_AsText(ST_Centroid(__mlocate__topology)) as tt, tahsil, district, state from lyr_115_india_tahsils where __mlocate__id = " + taluka)
					//def point = res.get("tt")
					cl.addToTaluka(res.get("tahsil"))
					cl.addToDistrict(res.get("district"))
					cl.addToState(res.get("state"))
					//parsePoint(cl, point)
				}

			}
			return
		}
		//		ss = sql.rows("select field_districts_value as tt from content_field_districts where nid = $nid and vid = $vid").size()
		//		if(ss > 1){
		//			println "========================== more that one distrcit " + ss
		//		}
		def districts = sql.rows("select field_districts_value as tt from content_field_districts where nid = $nid and vid = $vid").collect { it.get("tt") }
		//println "   district " + district
		if(districts){
			districts.each{district ->
				if(district){
					def res = sql.firstRow("select ST_AsText(ST_Centroid(__mlocate__topology)) as tt, district, state from lyr_105_india_districts where __mlocate__id = " + district)
					//def point = res.get("tt")
					cl.addToDistrict(res.get("district"))
					cl.addToState(res.get("state"))
					//parsePoint(cl, point)
				}
			}
			return
		}

		//		ss = sql.rows("select field_states_value as tt from content_field_states where nid = $nid and vid = $vid").size()
		//		if(ss > 1){
		//			println "========================== more that one state " + ss
		//		}


		def states = sql.rows("select field_states_value as tt from content_field_states where nid = $nid and vid = $vid").collect { it.get("tt") }
		//println " state " + state
		if(states){
			states.each{state ->
				if(state){
					def res = sql.firstRow("select ST_AsText(ST_Centroid(__mlocate__topology)) as tt, state from lyr_116_india_states where __mlocate__id = " + state)
					//def point = res.get("tt")
					cl.addToState(res.get("state"))
					//parsePoint(cl, point)
				}
			}
		}

	}

	private parsePoint(Checklist cl, pointStr){
		String[] ar = pointStr.substring(pointStr.indexOf("(") + 1, pointStr.indexOf(")")).split(" ")
		cl.longitude = ar[0].trim().toFloat().floatValue()
		cl.latitude = ar[1].trim().toFloat().floatValue()
	}

	private Date getDate(String dateString){
		if(!dateString || dateString.trim() == '')
			return null
		for (String formatString : dateFormatStrings){
			try
			{
				def d = new SimpleDateFormat(formatString).parse(dateString.trim());
				return d
			}
			catch (Exception e){
				log.debug " failed in format " + formatString
			}
		}
		return null;
	}
	
	
	def updateUncuratedVotesTable(){
		UnCuratedVotes.list().each { UnCuratedVotes uv ->
			if(uv.obv){
				uv.refId = uv.obv.id
				uv.refType = uv.obv.getClass().getCanonicalName()
				if(!uv.save(flush:true)){
					log.error "Error during UnCuratedVotes save === "
					uv.errors.allErrors.each { log.error it }
				}
			}
		}
	}
	
	def updateLocation(){
		SpreadsheetReader.readSpreadSheet("/tmp/checklist_locations.xls").get(0).each{ m ->
			def cl = Checklist.get(m.id.toString().trim().toLong())
			if(cl){
				cl.latitude = m.lat.trim().toFloat()
				cl.longitude = m.long.trim().toFloat()
				cl.placeName = m.place.trim()
				
				if(!cl.save(flush:true)){
					cl.errors.allErrors.each { log.error it }
				}
				log.debug "saving $cl"
			}
		}
	}
	
	private void cleanUpGorm(){
		cleanUpGorm(true)
	}
	
	private void cleanUpGorm(boolean clearSession) {
		
				def hibSession = sessionFactory?.getCurrentSession();
		
				if(hibSession) {
					log.debug "Flushing and clearing session"
					try {
						hibSession.flush()
					} catch(Exception e) {
						e.printStackTrace()
					}
					if(clearSession){
						hibSession.clear()
					}
				}
			}
	
	
	
	def changeCnName(){
		Recommendation.withTransaction(){
			
			CommonNames.list().each { CommonNames cn ->
				cn.name = Utils.getTitleCase(cn.name)
				cn.save()
			}
			log.debug " done common names"
			
			Recommendation.findAllByIsScientificName(false).each{ Recommendation r ->
				r.name = Utils.getTitleCase(r.name)
				r.save()
			}
			log.debug " done recommendation names"
			
			UnCuratedCommonNames.list().each{ UnCuratedCommonNames uncn ->
				uncn.name = Utils.getTitleCase(uncn.name)
				uncn.save()
			}
			log.debug " done un curated common names"
			
		}
		
	}
	
	def mCn(){
		def flushImmediately  = grailsApplication.config.speciesPortal.flushImmediately
		
		Date startTime = new Date()
		println "stat time ====== " + startTime
		
		def clIdList = []
		Checklist.listOrderById(order: "asc").each{ Checklist cl ->
				clIdList.add(cl.id)
		}
		
		println "=== total ids $clIdList.size()"
		
		clIdList.each{ id ->
			Checklist cl = Checklist.get(id)
			println "================ starting checklist === " + cl
			def clPairs = getPairs(cl.row)
			println "== got paris " + clPairs.size()
			updateCl(clPairs, cl)
			if(!flushImmediately){
				cleanUpGorm(true)
			}
			println "================ done checklist === " + cl
		}
		Date finishTime = new Date()
		println "  start time " + startTime + "   and finish time $finishTime"
		println ">>>>>>>>>>> finish time period ======  hours " +  finishTime.getHours() - startTime.getHours() + "   mintes " +  finishTime.getMinutes() - startTime.getMinutes()
	}
	
	
	private void updateCl(List snCnPairs, cl){
		Set recoIdSet = new HashSet()
		Set cnSet = new HashSet()
		def user = SUser.read(1)
		snCnPairs.each { pair ->
			updateCNSN(pair[0], pair[1], cl, user, cnSet, recoIdSet)
		}
	}
	
	private updateCNSN(snReco, cnName, cl, user, Set cnSet, Set recoIdSet){
		if(!cnName && !snReco){
			return
		}
		def originalCnName = cnName
		
		def flushImmediately  = grailsApplication.config.speciesPortal.flushImmediately
		if(!flushImmediately){
			// creating batches for optimization
			if(snReco){
				if(recoIdSet.contains(snReco.id)){
					println "========================== duplicate sn =============================="
					cleanUpGorm(false)
					recoIdSet.clear()
					cnSet.clear()
				}
				recoIdSet.add(snReco.id)
			}
			
			
			if(cnName){
				cnName = Utils.getTitleCase(Utils.cleanName(cnName))
				if(cnSet.contains(cnName)){
					println "========================== duplicate cn =======  $cnName ================"
					cleanUpGorm(false)
					recoIdSet.clear()
					cnSet.clear()
				}
				if(snReco){
					recoIdSet.add(snReco.id)
				}
				cnSet.add(cnName)
			}
		}
		
		def cnReco = recommendationService.findReco(originalCnName, false, null, null)
		curationService.add(snReco, cnReco, cl, user);
	}
	
	private getPairs(clDataRows){
		def res = []
		def preRowNo = -1
		def snReco = null
		def cName = null
		clDataRows.each { ChecklistRowData crd ->
			def currentRowNo = crd.rowId
			//row is changed
			if(currentRowNo != preRowNo){
				res.add([snReco, cName])
				preRowNo = currentRowNo
				snReco = null
				cName = null
				
			}
			if(crd.key.equalsIgnoreCase(SN_NAME)){
				snReco = crd.reco
			}else if(crd.key.equalsIgnoreCase(CN_NAME)){
				def value = crd.value
				cName = (value && (value.trim() != "") && (value.trim() != "No common name found"))? value.trim() : null
			}
		}
		return res
	}

}

/*

drop table checklist_district ; drop table checklist_state; drop table checklist_taluka; drop table checklist_user_group;drop table checklist_reference; drop table checklist_row_data; drop table checklist CASCADE ;


delete from un_curated_votes where id > 3287;
delete from un_curated_scientific_names_un_curated_common_names where un_curated_common_names_id > 1235 or un_curated_scientific_names_common_names_id > 1119;
delete from un_curated_common_names where id > 1235;
delete from un_curated_scientific_names where id > 1119;
delete from recommendation where id > 369304 and id < 380677 and is_scientific_name = false and id not in(select common_name_reco_id from recommendation_vote where common_name_reco_id  > 369304 and common_name_reco_id < 380677);

update suser set last_login_date = null where last_login_date <= date_created;

select count(r.id) from recommendation as r where r.is_scientific_name = false and (select count(*) from recommendation as r1 where r1.name = r.name and r1.taxon_concept_id = r.taxon_concept_id and ((r1.language_id is null and r.language_id is null)) and r.id != r1.id ) > 0;
387438, 387437, 387436, 387435, 387427

delete from un_curated_votes where voted_on >= '2013-01-11 08:05:55.705'
delete from un_curated_scientific_names_un_curated_common_names where un_curated_scientific_names_common_names_id in (11890, 11892, 11894, 11895, 11895,  11896);
delete from un_curated_scientific_names where id in (11890, 11892, 11894, 11895, 11895,  11896);
delete from recommendation where id in (387438, 387437, 387436, 387435, 387427);

*/

