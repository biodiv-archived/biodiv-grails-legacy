//import speciespage.SetupService
//
//def setupService = ctx.getbean("setupService");
//setupService.updateLanguageRegion(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2_withRegion.csv");

import java.text.Normalizer.Form;

import species.utils.Utils;
import species.*
import species.participation.*

import  species.participation.curation.CurationService;
import speciespage.search.ObservationsSearchService


def updateReco(boolean isSciName, Long langId, Recommendation reco){
	reco.isScientificName = isSciName
	reco.languageId = langId

	Recommendation.withTransaction(){
		if(!reco.save(flush:true)){
			reco.errors.each { println it; }
		}
	}
	println "== Saved success $reco"
}


def updateStandardReco(){
	println "=========== updating standard recommendation "
	List<Recommendation> recoList = Recommendation.findAllByTaxonConceptIsNotNull()
	println "=========== Toltal recos to update $recoList.size()"

	int size = recoList.size()

	for(int i=0; i < size; i++){
		Recommendation reco = recoList.get(i);
		String name = reco.name
		if(TaxonomyDefinition.findByCanonicalFormIlike(name) || Synonyms.findByCanonicalFormAndTaxonConcept(name, reco.taxonConcept )){
			updateReco(true, null, reco);
		}else{
			//list will be returned because lang_id is not considered for uniquness
			List<CommonNames> cnList = CommonNames.findAllByNameAndTaxonConcept(name, reco.taxonConcept);
			if(!cnList || cnList.isEmpty()){
				println "============== Error  Error reco $reco not mapping from known database"
			}
			//updating existing reco
			CommonNames cn = cnList.remove(0);
			updateReco(false, cn.language?.id, reco);

			//inserting new recos with different language id
			cnList.each{
				Recommendation r = new Recommendation(name:it.name, taxonConcept:it.taxonConcept);
				println "=== saving extra common name reco $r"
				updateReco(false, it.language?.id, reco);
			}
		}
	}
}

def updateNonStandardReco(languagesFile){
	def cs = ctx.getBean("curationService");
	def ss = ctx.getBean("observationsSearchService");
	
	Set processedRecoIds = new HashSet()
	Set storedObvId = new HashSet()
	Set deleteCandidate = new HashSet()
	
	println "=========== updating non-standard recommendation"
	def splittedlineList = []
	
	new File(languagesFile).splitEachLine("\\t"){
		splittedlineList << it
	}
	
	for(line in splittedlineList) {
		def fields = line;
		
		println line
		
		def obvId = fields[0]?.replaceAll("\"","")?.trim()
		
		if(obvId != ""){
			storedObvId.add(obvId.toLong())
		}
		
		def recoId = fields[1].replaceAll("\"","").trim().toLong();
		if(processedRecoIds.contains(recoId)){
			continue
		}

		processedRecoIds.add(recoId)
		
		def oldName = fields[2]?.replaceAll("\"","")?.trim()
		
		def sciName = fields[4]?.replaceAll("\"","")?.trim()
		sciName = (sciName && sciName != "") ?  Utils.cleanName(sciName) : null
		
		def commonName = fields[3]?.replaceAll("\"","")?.trim()
		commonName = (commonName && commonName != "") ? Utils.cleanName(commonName) : null

		def comment = fields[5]?.replaceAll("\"","")?.trim()
		comment = (comment && comment != "") ? comment : null
		
		
		Recommendation oldReco = Recommendation.get(recoId)
		
		if(!sciName && !commonName){
			println "=========== error both cant be null for reco $recoId"
			continue
		}

		Recommendation sciReco, commonNameReco
		
		if(commonName){
			commonNameReco = findReco(commonName, false)
		}
		
		if(sciName){
			sciReco = findReco(sciName, true)
			updateRecoVoteReferances(sciReco, commonNameReco, oldReco, cs, comment);
		}else{
			updateRecoVoteReferances(commonNameReco, commonNameReco, oldReco, cs, comment);
		}
		
	}
	
	Observation.findAllByIsDeleted(false).each{
		def observationInstance = Observation.get(it)
		//update species call
		observationInstance.calculateMaxVotedSpeciesName();
		
		//update solr
		//ss.publishSearchIndex(observationInstance, true);	
	}
}

def findReco(name, isSciName){
	def reco = Recommendation.findByNameIlikeAndIsScientificName(name, isSciName);
	if(!reco){
		def langId = (isSciName) ? null : Language.getLanguage(null).id
		reco = new Recommendation(name:name, isScientificName:isSciName, languageId:langId)
		if(!reco.save(flush:true)){
			reco.errors.each { println it; }
		}
	}
	return reco;
}

def updateRecoVoteReferances(Recommendation sciReco, Recommendation commonNameReco, Recommendation oldReco, cs, comment){
	List<RecommendationVote> rVotes = RecommendationVote.findAllByRecommendation(oldReco)
	
	rVotes.each{ rVote ->
		rVote.recommendation = sciReco
		rVote.commonNameReco = commonNameReco
		rVote.comment = comment
		if(!rVote.save(flush:true)){
			rVote.errors.each { println it; }
		}
		//adding it to curation
		cs.add((sciReco.isScientificName)? sciReco : null, commonNameReco, rVote.observation ,rVote.author)
	}
}

def migrate(){
	//updateStandardReco()
	updateNonStandardReco("/home/sandeept/dbmig/3.txt")
}

migrate()

//
//def updateStandardRecoTest(){
//	println "=========== updating standard recommendation"
//	Recommendation.findAllByTaxonConceptIsNotNull(max:10).each{ reco ->
//		String name = reco.name
//		if(TaxonomyDefinition.findByCanonicalFormIlike(name) || Synonyms.findByCanonicalFormAndTaxonConcept(name, reco.taxonConcept )){
//			updateReco(true, null, reco);
//		}else{
//			CommonNames cn = CommonNames.findWhere(name:name, taxonConcept:reco.taxonConcept, language:(reco.languageId?Language.read(reco.languageId):null));
//			if(!cn){
//				println "============== Error  Error reco $reco not mapping from known database"
//			}
//			//updating existing reco
//			updateReco(false, cn.language?.id, reco);
//		}
//	}
//}
//
//def updateNonStandardRecoTest(){
//	println "=========== updating non-standard recommendation"
//	Recommendation.findAllByTaxonConceptIsNull().each{ reco ->
//		updateReco(false, Language.getLanguage(null).id, reco);
//	}
//}


