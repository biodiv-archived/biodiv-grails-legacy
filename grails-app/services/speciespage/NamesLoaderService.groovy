package speciespage

import groovy.sql.Sql;

import org.apache.commons.logging.LogFactory

import species.CommonNames
import species.Synonyms
import species.TaxonomyDefinition
import species.participation.Recommendation

class NamesLoaderService {

	def grailsApplication
	def sessionFactory
	def recommendationService;

	static transactional = false

	private static final log = LogFactory.getLog(this);

	static final RECO_BATCH_TO_SAVE = 1000
	static final NAME_BATCH_TO_LOAD = 1000

	/**
	 * 
	 * @param cleanAndUpdate
	 */
	int syncNamesAndRecos(boolean cleanAndUpdate) {
		log.debug "Synching names and recommendations"
		int noOfNames = 0;
		noOfNames += syncRecosFromTaxonConcepts(3, 3, cleanAndUpdate);
		noOfNames += syncSynonyms();
		noOfNames += syncCommonNames();
		log.debug "No of names synched : "+noOfNames
		return noOfNames;
	}

	/**
	 *
	 * @param minRankToImport : the names of all taxon names above which are available for suggestions
	 * @param minRankToSpread : the names of higher rank get same suggestion of the concept at this level
	 */
	int syncRecosFromTaxonConcepts(int minRankToImport, int minRankToSpread, boolean cleanAndUpdate) {
		log.debug "Importing existing taxon definitions into recommendations"

		if(cleanAndUpdate) {
			//TODO:Handle cascading delete recommendations
			recommendationService.deleteAll();
		}

		int offset = 0;
		int noOfNames = 0;
		def recos = new ArrayList<Recommendation>();
		def conn = new Sql(sessionFactory.currentSession.connection())
		//while(true) {
		//def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonConcept where taxonConcept not in (select reco.taxonConcept from Recommendation as reco) and taxonConcept.rank >= :minRank", [minRank:minRankToImport])
		//def taxonConcepts = TaxonomyDefinition.findAllByRankGreaterThanEquals(minRankToImport);
		def taxonConcepts = conn.rows("select t.name as name, t.canonical_Form as canonicalForm, t.normalized_Form as normalizedForm, t.binomial_Form as binomialForm, t.id as id from Taxonomy_Definition as t left outer join recommendation r on t.id = r.taxon_concept_id where r.name is null");
		//if(!taxonConcepts) break; //no more results;

		taxonConcepts.each { taxonConcept ->
			 
			if(taxonConcept.name) {
				recos.add(new Recommendation(name:taxonConcept.name, taxonConcept:TaxonomyDefinition.get(taxonConcept.id)));
				if(!taxonConcept.name.equals(taxonConcept.canonicalform))
					recos.add(new Recommendation(name:taxonConcept.canonicalform, taxonConcept:TaxonomyDefinition.get(taxonConcept.id)));
				if(!taxonConcept.name.equals(taxonConcept.normalizedform))
					recos.add(new Recommendation(name:taxonConcept.normalizedform, taxonConcept:TaxonomyDefinition.get(taxonConcept.id)));
				if(taxonConcept.binomialform && !taxonConcept.binomialform.equals(taxonConcept.canonicalform))
					recos.add(new Recommendation(name:taxonConcept.binomialform, taxonConcept:TaxonomyDefinition.get(taxonConcept.id)));
				// TODO giving species subspecies options to parent taxonEntries
				noOfNames++;
			}
			if(recos.size() >= RECO_BATCH_TO_SAVE) {
				recommendationService.save(recos);
				recos.clear();
			}
		}
		offset = offset + taxonConcepts.size();
		//}

		//		while(true) {
		//			def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonConcept where taxonConcept not in (select reco.taxonConcept from Recommendation as reco) and taxonConcept.rank < :minRank", [minRank:minRankToSpread, max:NAME_BATCH_TO_LOAD, offset:offset])
		//			//def taxonConcepts = TaxonomyDefinition.findAllByRankGreaterThanEquals(minRankToImport, [max:NAME_BATCH_TO_LOAD, offset:offset]);
		//
		//			if(!taxonConcepts) break; //no more results;
		//
		//			taxonConcepts.each { taxonConcept ->
		//				//find all its children among all hierarchies > minRankToSpread and add it as reco
		//				//select r.*, t.rank from taxonomy_registry as r, taxonomy_definition as t where path similar to '(^820!_%|%!_820!_%|%!_820$)' escape '!' and r.taxon_definition_id = t.id ;
		//			}
		//			offset = offset + taxonConcepts.size();
		//		}

		recommendationService.save(recos);
		return noOfNames;
	}

	/**
	 * 
	 * @return
	 */
	int syncSynonyms() {
		log.debug "Importing synonyms into recommendations"
		def recos = new ArrayList<Recommendation>();
		int offset = 0;
		int noOfNames = 0;
		def conn = new Sql(sessionFactory.currentSession.connection())
		//while(true) {
		def synonyms = conn.rows("select n.name as name, n.taxon_concept_id as taxonConcept from synonyms n left outer join recommendation r on n.name = r.name and n.taxon_concept_id = r.taxon_concept_id where r.name is null")
		//def synonyms = Synonyms.findAll("from Synonyms as synonym left join Recommendation as recommendation with synonym.name = recommendation.name and synonym.taxonConcept = recommendation.taxonConcept where r.name is null)", [max:NAME_BATCH_TO_LOAD, offset:offset]);
		//if(!synonyms) break;
		synonyms.each { synonym ->
			recos.add(new Recommendation(name:synonym.name, taxonConcept:TaxonomyDefinition.get(synonym.taxonconcept)));
			if(recos.size() >= RECO_BATCH_TO_SAVE) {
				recommendationService.save(recos);
				recos.clear();
			}
			noOfNames++
		}

		offset = offset + synonyms.size();
		//}
		recommendationService.save(recos);
		log.debug "Imported synonyms into recommendations : "+noOfNames
		return noOfNames;
	}

	/**
	 * 
	 * @return
	 */
	def syncCommonNames() {
		log.debug "Importing common names into recommendations"
		def recos = new ArrayList<Recommendation>();
		int offset = 0, noOfNames = 0;
		def conn = new Sql(sessionFactory.currentSession.connection())
		//while(true) {
		def commonNames = conn.rows("select n.name as name, n.taxon_concept_id as taxonConcept from common_names n left outer join recommendation r on n.name = r.name and n.taxon_concept_id = r.taxon_concept_id where r.name is null group by n.name, n.taxon_concept_id")
		//if(!commonNames) break;

		commonNames.each { cName ->
			recos.add(new Recommendation(name:cName.name, taxonConcept:TaxonomyDefinition.get(cName.taxonconcept)));
			if(recos.size() >= RECO_BATCH_TO_SAVE) {
				recommendationService.save(recos);
				recos.clear();
			}
			noOfNames++
		}

		offset =  offset + commonNames.size();
		//}
		recommendationService.save(recos);
		log.debug "Imported common names into recommendations : "+noOfNames
		return noOfNames
	}

}
