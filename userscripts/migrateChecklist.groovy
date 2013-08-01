import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;

import com.vividsolutions.jts.geom.PrecisionModel;
import species.CommonNames;
import species.Language;
//import species.participation.checklistUtilService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;
import speciespage.*
import com.vividsolutions.jts.geom.*
import content.eml.Coverage
import content.eml.Document
import species.groups.UserGroup
import grails.converters.JSON

//def checklistUtilService = ctx.getBean("checklistUtilService");

//checklistUtilService.updateUncuratedVotesTable()

//checklistUtilService.updateLocation()

//checklistUtilService.changeCnName()

//checklistUtilService.mCn()
//
//def cl = Checklist.get(174)
//println  cl
////
//checklistUtilService.createObservationFromChecklist(cl)


//checklistUtilService.addFollow()
//checklistUtilService.addRefObseravtionToChecklist()


def correctRow(){
	def snVal = 'Aethopyga vigorsii'
	def observationService = ctx.getBean("observationService");
	def reco = observationService.getRecommendation([recoName:snVal, canName:snVal, commonName:null]).mainReco
	def row = new ChecklistRowData(key:'scientific_name', value:snVal, rowId:25, columnOrderId:2, reco:reco)
	def cl = Checklist.get(23).addToRow(row)
	if(!cl.save(flush:true)){
		cl.errors.allErrors.each { println  it }
	}
}

def deleteChecklist(id)  {
	try{
		Checklist.get(id).delete(flush:true)
	}catch (Exception e) {
		e.printStackTrace()
	}
}

def correctChecklist(deleteId, migrateId){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	deleteChecklist(deleteId)
	checklistUtilService.migrateChecklist(migrateId)
}

//correctRow()
//correctChecklist(41, 1277 ) //Scientific Name
//correctChecklist(62, 1298) //scientific_names



 	
//checklistUtilService.migrateChecklistAsObs()
//checklistUtilService.migrateObservationFromChecklist()




def migrateObvLocation() {
	GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
	Observation.withTransaction(){
	   Observation.findAllByTopologyIsNull().each{obv->
	        obv.topology = geometryFactory.createPoint(new Coordinate(obv.longitude, obv.latitude));
			obv.placeName = obv.placeName?:obv.reverseGeocodedName;
	        if(!obv.save(flush:true)) {
			    obv.errors.allErrors.each { println  it }
	        }
	   }
	}
	
	//or use
	//update observation set topology = ST_GeomFromText('SRID=4326;POINT(' || logitude ||  latitude)|| ')');
}
//migrateObvLocation();



def migrateDocLocation() {
	GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
	Document.withTransaction(){
	   Coverage.findAllByTopologyIsNull().each{obv->
			obv.topology = geometryFactory.createPoint(new Coordinate(obv.longitude, obv.latitude));
			obv.placeName = obv.placeName?:obv.reverseGeocodedName;
			if(!obv.save(flush:true)) {
				obv.errors.allErrors.each { println  it }
			}
	   }
	}
	
	//or use
	//update observation set topology = ST_GeomFromText('SRID=4326;POINT(' || logitude ||  latitude)|| ')');
}



def postChecklistToWGPGroup(){
	def userGroupInstance = UserGroup.findByName('The Western Ghats')
	def oldcls = Checklist.withCriteria(){
		and{
			if(userGroupInstance){
				userGroups{
					eq('id', userGroupInstance.id)
				}
			}
		}
		order 'id', 'asc'
	}
	Checklists.withTransaction {
		oldcls.each { Checklist oldCl ->
			println "============= adding  $oldCl" 
			userGroupInstance.addToObservations(Checklists.get(oldCl.id));
		}
	}
}


def serachInList(l1){
	List snList =  ["scientific_names", "Scientific Name", "scientific_name", "scientific_name "]
	for(sn in snList){ 
		if(l1.contains(sn)){
			return sn
		}
	}
	return null 
}

//postChecklistToWGPGroup()

def serializeChecklist(){
	def clIdList = []
	Checklist.listOrderById(order: "asc").each{ Checklist cl ->
			clIdList.add(cl.id)
	}
	clIdList.each {  id ->
		Checklists.withTransaction(){
			def cl = Checklists.findByIdAndIsDeleted(id, false)
			//if(!cl.columns){
				println cl
				List cns = new ArrayList(Arrays.asList(cl.columnNames.split("\t"))).collect{ it}
				if(cns.contains("common_name")){
					cl.commonNameColumn = "common_name"
					cns.remove("common_name")
					cns.add(0, "common_name")
				}
				def snCol = serachInList(cns)
				if(snCol){
					cl.sciNameColumn = snCol
					cns.remove(snCol)
					cns.add(0, snCol)
				}
				cl.columns = cns as JSON
				if(!cl.save(flush:true)){
					cl.errors.allErrors.each { println it }
				}
			//}
		}
	}
}


def addFeedToChecklist(){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	
	def m = GrailsDomainBinder.getMapping(ActivityFeed.class)
	m.autoTimestamp = false
	
	def clIdList = []
	Checklist.listOrderById(order: "asc").each{ Checklist cl ->
			clIdList.add(cl.id)
	}
	clIdList.each {  id ->
		Checklists.withTransaction(){
			def cl = Checklists.findByIdAndIsDeleted(id, false)
			println cl
			checklistUtilService.addActivityFeed(cl, cl, cl.author, ActivityFeedService.CHECKLIST_CREATED, cl.fromDate)
			def ugs = cl.userGroups 
			if(ugs){
				ugs.each { ug ->
					println "============= user group " + "   " +ug.name  +  ug.class  
					def tDate = new Date(cl.fromDate.getTime() + 10)
					checklistUtilService.addActivityFeed(cl, ug, cl.author, ActivityFeedService.CHECKLIST_POSTED_ON_GROUP, tDate)
				}
			}
			if(!cl.save(flush:true)){
				cl.errors.allErrors.each { println it }
			}
		}
	}
	m.autoTimestamp = true
	
}


def corChecklist(){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	def clIdList = [20, 72, 129, 221, 267, 304, 305]
	clIdList.each {  id ->
			def cl = Checklists.findById(id, [fetch: [observations: 'join']])
			println cl
			Checklists.withTransaction(){
				cl.observations.each { obv ->
					def m = [:]
					obv.fetchChecklistAnnotation().each { a ->
						if(a.value){
							m.put(a.key.trim(), a.value.trim())
						}
					}
					m.put(cl.sciNameColumn.trim(), obv.fetchSpeciesCall())
					if(cl.commonNameColumn){
						def cnReco = RecommendationVote.findByAuthorAndObservation(obv.author, obv).commonNameReco
						if(cnReco){
							m.put(cl.commonNameColumn.trim(), cnReco.name)
						}
					}
					
					obv.checklistAnnotations = m as JSON
					if(!obv.save(flush:true)){
						obv.errors.allErrors.each { println it }
					}
				}
				println "=== done observations "
			}
			
			checklistUtilService.cleanUpGorm(true)
			
			Checklists.withTransaction(){
				cl =Checklists.get(id)
				println "== cl before " +  cl.columns
				cl.columns = new ArrayList(cl.fetchColumnNames()).collect {it.trim() } as JSON
				println  " colsss  " + cl.columns
				cl.sciNameColumn = cl.sciNameColumn.trim()
				cl.commonNameColumn =  cl.commonNameColumn ? cl.commonNameColumn.trim() : null
				if(!cl.save(flush:true)){
					cl.errors.allErrors.each { println it }
				}
				println  "after save " + cl.columns 
			}
		
	}
}


//addFeedToChecklist()
//serializeChecklist()
corChecklist()
println "================ done "
