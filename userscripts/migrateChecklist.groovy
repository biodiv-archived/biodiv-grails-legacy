import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;

import species.CommonNames;
import species.Language;
//import species.participation.checklistUtilService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;
import speciespage.*
import com.vividsolutions.jts.geom.*
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
	Observation.withTransaction(){
	   Observation.findAllByTopologyIsNull().each{obv->
	        GeometryFactory geometryFactory = new GeometryFactory();
	        obv.topology = geometryFactory.createPoint(new Coordinate(obv.latitude, obv.longitude));
			obv.placeName = obv.placeName?:obv.reverseGeocodedName;
	        if(!obv.save(flush:true)) {
			    obv.errors.allErrors.each { println  it }
	        }
	   }
	}
}
migrateObvLocation();


println "================ done "

