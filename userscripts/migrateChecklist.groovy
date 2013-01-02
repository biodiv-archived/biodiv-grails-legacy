import species.participation.ChecklistService

def checklistService = ctx.getBean("checklistService");

//checklistService.updateUncuratedVotesTable()

//checklistService.migrateChecklist()

checklistService.updateLocation()
