import species.CommonNames;
import species.Language;
import species.participation.ChecklistService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;

def checklistService = ctx.getBean("checklistService");

//checklistService.updateUncuratedVotesTable()

//checklistService.migrateChecklist()

//checklistService.updateLocation()

//checklistService.changeCnName()

//checklistService.mCn()
//
//def cl = Checklist.get(174)
//println  cl
////
//checklistService.createObservationFromChecklist(cl)
//checklistService.udpateObv(cl)

checklistService.migrateObv()