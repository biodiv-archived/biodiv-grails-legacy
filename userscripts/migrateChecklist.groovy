import species.participation.ChecklistService
import species.participation.curation.*
import species.participation.*


def checklistService = ctx.getBean("checklistService");

//checklistService.updateUncuratedVotesTable()

//checklistService.migrateChecklist()

//checklistService.updateLocation()

checklistService.changeCnName()

//checklistService.mCn()


def getWrongCommonName(){
	def shared = 0
	
	Set checklistIds = new HashSet()
	Set badRecoIds = new HashSet()
	Set recoVoteCommonNamesIds = new HashSet(RecommendationVote.list().collect(){
		it.commonNameReco?.id
	});
	
	def ucvList = UnCuratedVotes.findAllWhere(refType:Checklist.class.getCanonicalName()).each{ ucv ->
		UnCuratedCommonNames ucn = ucv.commonName
		if(ucn &&  !ucn.reco.isScientificName ){
			if(recoVoteCommonNamesIds.contains(ucn.reco.id)){
				shared++
			}else{
				checklistIds.add(ucv.refId)
				badRecoIds.add(ucn.reco.id)
			}
			//println "=== bad reco " +  ucn.reco
		}
	}
	
	def d = new Date(112, 11, 31)
	println "=== date " + d
	def newRecosSize = Recommendation.findAllByIsScientificNameAndLastModifiedGreaterThanEquals(false, d).size()
	
	println "=============================================================="
	println "total bad common names " + badRecoIds.size()
	println "shared common name " + shared
	println "new recos size " + newRecosSize
	println "total checklist has common names " + checklistIds.size()
	println "useful common name reco " + recoVoteCommonNamesIds.size()
	println "=============================================================="
	
}

//getWrongCommonName()
// select * from un_curated_votes where ref_type = 'species.participation.Checklist' and voted_on >= '2012-12-30 00:00:00' order by voted_on asc;



