import species.CommonNames;
import species.Language;
import species.participation.ChecklistService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;
import speciespage.*

def checklistService = ctx.getBean("checklistService");

//checklistService.updateUncuratedVotesTable()

//checklistService.updateLocation()

//checklistService.changeCnName()

//checklistService.mCn()
//
//def cl = Checklist.get(174)
//println  cl
////
//checklistService.createObservationFromChecklist(cl)

//checklistService.migrateObservationFromChecklist()
//checklistService.addFollow()
checklistService.addRefObseravtionToChecklist()


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
	def checklistService = ctx.getBean("checklistService");
	deleteChecklist(deleteId)
	checklistService.migrateChecklist(migrateId)
}

//correctRow()
//correctChecklist(41, 1277 ) //Scientific Name
//correctChecklist(62, 1298) //scientific_names
