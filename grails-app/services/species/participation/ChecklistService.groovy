package species.participation

import java.text.SimpleDateFormat
import java.util.Iterator;
import java.util.List;

import species.participation.curation.UnCuratedCommonNames

import org.springframework.transaction.annotation.Transactional;


import species.auth.SUser
import species.Species;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.CommonNames;
import species.License;
import species.Reference;
import groovy.sql.Sql;
import species.participation.curation.UnCuratedVotes;
import species.utils.Utils;
import species.formatReader.SpreadsheetReader;

import au.com.bytecode.opencsv.CSVReader

class ChecklistService {
	static transactional = false

	def grailsApplication
	def observationService
	def sessionFactory
	def curationService
	
	static final String SN_NAME = "scientific_name"
	static final String CN_NAME = "common_name"

	String connectionUrl =  "jdbc:postgresql://localhost/ibp";
	String userName = "postgres";
	String password = "postgres123";

	def dateFormatStrings =  Arrays.asList("yyyy-MM-dd'T'HH:mm:ss")

	def migrateChecklist(){
		def startDate = new Date()
		def sql = Sql.newInstance(connectionUrl, userName, password, "org.postgresql.Driver");
		int i=0;
		sql.eachRow("select nid, vid, title from node where type = 'checklist' order by nid asc ") { row ->
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
		if(snColumnOrder && snVal){
			
			snVal = Utils.getCanonicalForm(snVal);
			
			if(sciNameSet.contains(snVal)){
				println "========================== duplicate sn ==============================" + snVal
				cleanUpGorm()
				sciNameSet.clear()
				commonNameSet.clear()
			}
			sciNameSet.add(snVal)
			
			
			if(cn){
				cn =  Utils.cleanName(cn);
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
			cl.addToRow(new ChecklistRowData(key:snKey, value:snVal, rowId:rowId, reco:reco, columnOrderId:snColumnOrder))
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
		SpreadsheetReader.readSpreadSheet("/tmp/checklist_location_byThomas.xls").get(0).each{ m ->
			//println "title === " + m.title + "||||| lat === $m.lat  ||||| long === " + m.long
			def cl = Checklist.findByTitle(m.title.trim())
			if(cl){
				cl.latitude = m.lat.toFloat()
				cl.longitude = m.long.toFloat() 
				if(!cl.save(flush:true)){
					cl.errors.allErrors.each { log.error it }
				}
			}else{
				//cl = Checklist.read(m.id.toFloat().toLong())
				log.error " no cheklist $m.id === $m.title   ||| cl ==" + cl?.title 
				
				
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
		
		def cnReco = observationService.findReco(originalCnName, false, null, null)
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
*/