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

def checklistUtilService = ctx.getBean("checklistUtilService");

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

//postChecklistToWGPGroup()

def serializeChecklist(){
	def clIdList = []
	Checklist.listOrderById(order: "asc").each{ Checklist cl ->
			clIdList.add(cl.id)
	}
	clIdList.each {  id ->
		Checklists.withTransaction(){
			def cl = Checklists.findByIdAndIsDeleted(id, false, [fetch: [observations: 'join']])
			println cl
			def cns = Arrays.asList(cl.columnNames.split("\t"))
			if(cns.contains("scientific_name")){
				cl.sciNameColumn = "scientific_name"
			}
			if(cns.contains("common_name")){
				cl.sciNameColumn = "common_name"
			}
			
			cl.columns = cns as JSON
			cl.save(flush:true)
		}
	}
}
serializeChecklist()
println "================ done "
