//import speciespage.SetupService
//
//def setupService = ctx.getbean("setupService");
//setupService.updateLanguageRegion(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2_withRegion.csv");

import species.*
import species.participation.*

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

def updateStandardRecoTest(){
	println "=========== updating standard recommendation"
	Recommendation.findAllByTaxonConceptIsNotNull(max:10).each{ reco ->
		String name = reco.name
		if(TaxonomyDefinition.findByCanonicalFormIlike(name) || Synonyms.findByCanonicalFormAndTaxonConcept(name, reco.taxonConcept )){
			updateReco(true, null, reco);
		}else{
			CommonNames cn = CommonNames.findWhere(name:name, taxonConcept:reco.taxonConcept, language:(reco.languageId?Language.read(reco.languageId):null));
			if(!cn){
				println "============== Error  Error reco $reco not mapping from known database"
			}
			//updating existing reco
			updateReco(false, cn.language?.id, reco);
		}
	}
}

def updateStandardReco(){
	println "=========== updating standard recommendation"
	Recommendation.findAllByTaxonConceptIsNotNull().each{ reco ->
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
				Recommendation r = new Recommendation(name:it.name, taxonConcept:TaxonomyDefinition.get(it.taxonConcept));
				updateReco(false, it.language?.id, reco);
			}			 
		}
	}
}

def updateNonStandardReco(){
	println "=========== updating non-standard recommendation"
	Recommendation.findAllByTaxonConceptIsNull().each{ reco ->
		updateReco(false, Language.getLanguage(null).id, reco);
	}
}

def migrate(){
	updateStandardReco()
	//updateStandardRecoTest()
	updateNonStandardReco()
}

migrate()