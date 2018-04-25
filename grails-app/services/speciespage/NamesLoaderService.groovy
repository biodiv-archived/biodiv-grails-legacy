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
    def dataSource

    static transactional = false

    private static final log = LogFactory.getLog(this);

    static final RECO_BATCH_TO_SAVE = 1000
    static final NAME_BATCH_TO_LOAD = 1000
    static final int BATCH_SIZE = 100;
    /**
     * 
     * @param cleanAndUpdate
     */
    int syncNamesAndRecos(boolean cleanAndUpdate, boolean addToTree = false) {
        log.info "Synching names and recommendations"
        int noOfNames = 0;
//        int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
        try {
//            dataSource.setUnreturnedConnectionTimeout(500);
			noOfNames += syncRecosFromTaxonConcepts(3, 3, cleanAndUpdate, addToTree);
            noOfNames += syncSynonyms(addToTree);
            noOfNames += syncCommonNames(addToTree);
        } catch(Exception e) {
            log.error e.printStackTrace();
        } finally {
//            log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
//            dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
        }
        log.info "No of names synched : "+noOfNames
        return noOfNames;
    }

    /**
     * 
     * @param minRankToImport : the names of all taxon names above which are available for suggestions
     * @param minRankToSpread : the names of higher rank get same suggestion of the concept at this level
     */
    int syncRecosFromTaxonConcepts(int minRankToImport, int minRankToSpread, boolean cleanAndUpdate, boolean addToTree) {
        log.info "Importing existing taxon definitions into recommendations"

        if(cleanAndUpdate) {
            //TODO:Handle cascading delete recommendations
            recommendationService.deleteAll();
        }
        updateTaxonConceptForReco(addToTree);
        
        int limit = BATCH_SIZE, offset = 0, noOfNames = 0;
  
        def conn = new Sql(dataSource)
        def tmpTableName = "tmp_taxon_concept"
		def viewQuery = """
			SELECT t.name AS name,
			       t.canonical_form AS canonicalForm,
			       t.normalized_form AS normalizedForm,
			       t.binomial_form AS binomialForm,
			       t.id AS id
			FROM Taxonomy_Definition AS t
			LEFT OUTER JOIN recommendation r ON t.id = r.taxon_concept_id
			AND t.lowercase_match_name = r.lowercase_name
			WHERE r.lowercase_name IS NULL
			  AND t.status = 'ACCEPTED'
			  AND t.is_deleted = false
			ORDER BY t.id;
		"""
        try {
			conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
            conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as " + viewQuery);
        } finally {
            conn.close();
        }
		def taxonConcepts
        while(true) {
            try {
                try {
                    conn = new Sql(dataSource);
                    taxonConcepts = conn.rows("select name, canonicalForm, normalizedForm, binomialForm, id from " + tmpTableName + " order by id limit " + limit + " offset " + offset);
                } finally {
                    conn.close();
                }
                noOfNames += syncRecosFromTaxonConcepts(taxonConcepts, addToTree);
                offset = offset + limit;

            } catch (Exception e) {
                e.printStackTrace();
                break;
            } 
            if(!taxonConcepts) break; //no more results;
        }

        conn = new Sql(dataSource);
        try {
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);	
        } finally {
            conn.close();
        }
        return noOfNames;
    }

    int syncRecosFromTaxonConcepts(taxonConcepts, boolean addToTree=false) {
        List recos = [];
        int noOfNames = 0;
        TaxonomyDefinition.withNewTransaction {
            taxonConcepts.each { taxonConcept ->
                if(taxonConcept.name) {
                    def taxonObj = TaxonomyDefinition.read(taxonConcept.id)
                    recos.add(new Recommendation(name:taxonConcept.canonicalForm, taxonConcept:taxonObj, acceptedName:taxonObj));
                }
            }
            noOfNames += recommendationService.save(recos, addToTree);
        }   
        return noOfNames;
    }

    private int updateTaxonConceptForReco(boolean addToTree){
        String taxonDefQuery = """
			SELECT r.id AS recoid,
			       t.id AS taxonid,
			       t.id AS acceptedid
			FROM recommendation AS r,
			     taxonomy_definition AS t
			WHERE r.lowercase_name = t.lowercase_match_name
			  AND r.taxon_concept_id IS NULL
			  AND r.is_scientific_name = TRUE
			  AND t.is_deleted = false
			  AND t.status = 'ACCEPTED' """
		
		String synonymQuery = """
			SELECT r.id AS recoid,
			       asyn.synonym_id AS taxonid,
			       asyn.accepted_id AS acceptedid
			FROM recommendation AS r,
			     taxonomy_definition AS t,
			     accepted_synonym AS asyn
			WHERE t.id = asyn.synonym_id
			  AND r.lowercase_name = t.lowercase_match_name
			  AND r.taxon_concept_id IS NULL
			  AND r.is_scientific_name = TRUE
			  AND t.is_deleted = false
			  AND t.status = 'SYNONYM'
			  AND asyn.synonym_id IN
			    (SELECT synonym_id
			     FROM accepted_synonym
			     GROUP BY synonym_id
			     HAVING count(*) = 1) """

		String commnonNameQuery = """
			SELECT r.id AS recoid,
			       c.taxon_concept_id AS taxonid,
			       c.taxon_concept_id AS acceptedid
			FROM recommendation AS r,
			     common_names AS c,
				 taxonomy_definition AS t
			WHERE r.lowercase_name = c.lowercase_name
			  AND ((r.language_id IS NULL
			        AND c.language_id IS NULL)
			       OR (r.language_id IS NOT NULL
			           AND c.language_id IS NOT NULL
			           AND r.language_id = c.language_id))
			  AND r.taxon_concept_id IS NULL
			  AND r.is_scientific_name = FALSE
			  AND t.id = c.taxon_concept_id
			  AND t.status = 'ACCEPTED'
			  AND t.is_deleted = false
			  AND c.is_deleted = false
		"""
		
		String commnonNameQuerySyn = """
			SELECT r.id AS recoid,
			       c.taxon_concept_id AS taxonid,
			       asyn.accepted_id AS acceptedid
			FROM recommendation AS r,
			     common_names AS c,
				 taxonomy_definition AS t,
				 accepted_synonym AS asyn
			WHERE r.lowercase_name = c.lowercase_name
			  AND ((r.language_id IS NULL
			        AND c.language_id IS NULL)
			       OR (r.language_id IS NOT NULL
			           AND c.language_id IS NOT NULL
			           AND r.language_id = c.language_id))
			  AND asyn.synonym_id = c.taxon_concept_id
			  AND r.taxon_concept_id IS NULL
			  AND r.is_scientific_name = FALSE
			  AND t.id = c.taxon_concept_id
			  AND t.status = 'SYNONYM'
			  AND t.is_deleted = false
			  AND c.is_deleted = false
			  AND asyn.synonym_id IN
			    (SELECT synonym_id
			     FROM accepted_synonym
			     GROUP BY synonym_id
			     HAVING count(*) = 1)
		"""
	

        def queryList = [taxonDefQuery, synonymQuery, commnonNameQuery, commnonNameQuerySyn]
        int limit = BATCH_SIZE, noOfNames = 0
        queryList.each{ query ->
            int offset = 0
            def recos = new ArrayList<Recommendation>();
            def conn = new Sql(dataSource);
            def conn1;
            def tmpTableName = "tmp_table_update_taxonconcept"
            try {
                try {
					conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
                    conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as " + query);
                } finally {
                    conn.close();
                }
                while(true) {
                    try {
                        def recommendationList;
                        try {
                            conn1 = new Sql(dataSource)
                            recommendationList = conn1.rows("select recoid, taxonid, acceptedid from " + tmpTableName + " order by recoid limit " + limit + " offset " + offset);
                        } finally {
                            conn1.close();
                        }

                        Recommendation.withNewTransaction {
                             recommendationList.each { r ->
								println "${r.recoid} ${r.taxonid} ${r.acceptedid}"
                                Recommendation rec = Recommendation.get(r.recoid);
                                rec.taxonConcept = TaxonomyDefinition.get(r.taxonid);
								rec.acceptedName = TaxonomyDefinition.get(r.acceptedid);
                                recos.add(rec);
                                //noOfNames++;
                            }
                            noOfNames += recommendationService.save(recos, addToTree);
                            recos.clear();
                            offset = offset + limit;
                        }
                        if(!recommendationList) break; //no more results;
                    } catch(Exception e) {
                        e.printStackTrace();
                        break;
                    } 
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            } finally{
                conn = new Sql(dataSource)
                try {
                conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
                } finally {
                    conn.close()
                }
            }	
            log.info "Synched recommendations for query ${query}"
        }
        log.info "Total number of updated recommendations are $noOfNames"
        return noOfNames
    }


    /**
     * 
     * @return
     */
    int syncSynonyms(boolean addToTree) {
        log.info "Importing synonyms into recommendations"
        def recos = new ArrayList<Recommendation>();
        int offset = 0, noOfNames = 0, limit = BATCH_SIZE;
        def conn = new Sql(dataSource)

        def tmpTableName1 = "tmp_synonyms_1"
		def tmpTableName2 = "tmp_synonyms_2"
		
		def query1 = """
			SELECT t.lowercase_match_name AS lowercase_match_name,
			       t.canonical_form AS canonical_form,
			       asyn.synonym_id AS taxonconcept,
			       asyn.accepted_id AS acceptedname
			FROM accepted_synonym AS asyn,
			     taxonomy_definition AS t
			WHERE t.id = asyn.synonym_id
			  AND t.status = 'SYNONYM'
			  AND t.is_deleted = false
		"""
		
		def query2 = """
			SELECT n.canonical_form AS canonical_form,
			       n.taxonconcept AS taxonconcept,
			       n.acceptedname AS acceptedname
			FROM tmp_synonyms_1 AS n
			LEFT OUTER JOIN recommendation r ON n.lowercase_match_name = r.lowercase_name
			AND n.taxonconcept = r.taxon_concept_id
			WHERE r.name IS NULL
			  AND n.canonical_form IS NOT NULL
			GROUP BY n.canonical_form,
			         n.taxonconcept,
			         n.acceptedname
			ORDER BY n.taxonconcept
		"""
		
        try {
			conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName2);
			conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName1);
            conn.executeUpdate("CREATE TABLE " + tmpTableName1 +  " as " + query1);   
			conn.executeUpdate("CREATE TABLE " + tmpTableName2 +  " as " + query2);
 			conn.executeUpdate("ALTER TABLE " + tmpTableName2 +  " ADD COLUMN id SERIAL PRIMARY KEY");
        } finally {
            conn.close();
        }
        while(true) {
            def synonyms
            try {
                conn = new Sql(dataSource);
                synonyms = conn.rows("select canonical_form, taxonconcept, acceptedname from " + tmpTableName2 + " order by id limit " + limit + " offset " + offset);
				
                Recommendation.withNewTransaction {
					synonyms.each { synonym ->
                        recos.add(new Recommendation(name:synonym.canonical_form, taxonConcept:TaxonomyDefinition.get(synonym.taxonconcept), acceptedName:TaxonomyDefinition.get(synonym.acceptedname)));
                    }

                    offset = offset + limit;		
                    noOfNames += recommendationService.save(recos, addToTree);
                    recos.clear();
                }
            } catch(Exception e) {
                e.printStackTrace();
                break;
            } finally {
                conn.close();
            }
            if(!synonyms) break;
        }
        conn = new Sql(dataSource);
        try {
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName2);
			conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName1);
        } finally {
            conn.close();
        }
        log.info "Imported synonyms into recommendations : "+noOfNames
        return noOfNames;
    }

    /**
     * 
     * @return
     */
    def syncCommonNames(boolean addToTree) {
        log.info "Importing common names into recommendations"
        def recos = new ArrayList<Recommendation>();
        int offset = 0, noOfNames = 0, limit=BATCH_SIZE;
        def conn = new Sql(dataSource)
        def tmpTableName = "tmp_common_names"
        def selectQuery = """
			SELECT n.name AS name,
			       n.taxon_concept_id AS taxonConcept,
			       n.language_id AS LANGUAGE
			FROM common_names n
			LEFT OUTER JOIN recommendation r ON n.lowercase_name = r.lowercase_name
			AND ((n.taxon_concept_id IS NULL
			      AND r.taxon_concept_id IS NULL)
			     OR (n.taxon_concept_id IS NOT NULL
			         AND r.taxon_concept_id IS NOT NULL
			         AND n.taxon_concept_id = r.taxon_concept_id))
			AND ((r.language_id IS NULL
			      AND n.language_id IS NULL)
			     OR (r.language_id IS NOT NULL
			         AND n.language_id IS NOT NULL
			         AND r.language_id = n.language_id))
			WHERE r.name IS NULL
			  AND r.is_scientific_name = FALSE
			  AND n.is_deleted = false

			GROUP BY n.name,
			         n.taxon_concept_id,
			         n.language_id,
			         n.id
			ORDER BY n.taxon_concept_id
        """
        try {
			conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
            conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as " + selectQuery );
            conn.executeUpdate("ALTER TABLE " + tmpTableName +  " ADD COLUMN id SERIAL PRIMARY KEY");
        } finally {
            conn.close();
        }

        while(true) {
            def commonNames
            try {
                conn = new Sql(dataSource);
                commonNames = conn.rows("select name, taxonConcept, language from " + tmpTableName + " order by id limit "+limit+" offset "+offset)

                Recommendation.withNewTransaction {
                    commonNames.each { cName ->
						def td = TaxonomyDefinition.read(cName.taxonconcept)
                        recos.add(new Recommendation(name:cName.name, isScientificName:false, languageId:cName.language, taxonConcept:td, acceptedName:TaxonomyDefinition.fetchAccepted(td)));
                    }
                    noOfNames += recommendationService.save(recos, addToTree);
                    recos.clear();
                    offset =  offset + commonNames.size();
                }
            } catch(Exception e) {
                e.printStackTrace();
                break;
            } finally {
                conn.close();
            }
            if(!commonNames) break;
        }
        try {
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
        } finally {
            conn.close();
        }
        log.info "Imported common names into recommendations : "+noOfNames
        return noOfNames
    }


}
