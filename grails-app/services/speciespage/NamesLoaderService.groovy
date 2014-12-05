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
    static final int BATCH_SIZE = 50;
    /**
     * 
     * @param cleanAndUpdate
     */
    int syncNamesAndRecos(boolean cleanAndUpdate) {
        log.info "Synching names and recommendations"
        int noOfNames = 0;
        noOfNames += syncRecosFromTaxonConcepts(3, 3, cleanAndUpdate);
        noOfNames += syncSynonyms();
        noOfNames += syncCommonNames();

        log.info "No of names synched : "+noOfNames
        return noOfNames;
    }

    /**
     * 
     * @param minRankToImport : the names of all taxon names above which are available for suggestions
     * @param minRankToSpread : the names of higher rank get same suggestion of the concept at this level
     */
    int syncRecosFromTaxonConcepts(int minRankToImport, int minRankToSpread, boolean cleanAndUpdate) {
        log.info "Importing existing taxon definitions into recommendations"

        if(cleanAndUpdate) {
            //TODO:Handle cascading delete recommendations
            recommendationService.deleteAll();
        }
        updateTaxOnConceptForReco();
        
        int limit = BATCH_SIZE, offset = 0, noOfNames = 0;
        def recos = new ArrayList<Recommendation>();
        def conn = new Sql(dataSource)
        def tmpTableName = "tmp_taxon_concept"
        try {
            conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as select t.name as name, t.canonical_Form as canonicalForm, t.normalized_Form as normalizedForm, t.binomial_Form as binomialForm, t.id as id from Taxonomy_Definition as t left outer join recommendation r on t.id = r.taxon_concept_id where r.name is null order by t.id ");
        } finally {
            conn.close();
        }

        while(true) {
            def taxonConcepts;
            try {
                try {
                    conn = new Sql(dataSource)
                       taxonConcepts = conn.rows("select name, canonicalForm, normalizedForm, binomialForm, id from " + tmpTableName + " order by id limit " + limit + " offset " + offset);
                } finally {
                    conn.close();
                }

                TaxonomyDefinition.withNewTransaction {
                    //def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonConcept where taxonConcept not in (select reco.taxonConcept from Recommendation as reco) and taxonConcept.rank >= :minRank", [minRank:minRankToImport])
                    //def taxonConcepts = TaxonomyDefinition.findAllByRankGreaterThanEquals(minRankToImport);
                    //TODO: select returning different loop in every iteration so using order by. find the reaosn and remove this

                    taxonConcepts.each { taxonConcept ->
                        if(taxonConcept.name) {
                            def taxonObj = TaxonomyDefinition.read(taxonConcept.id)
                            recos.add(new Recommendation(name:taxonConcept.canonicalform, taxonConcept:taxonObj));
                            //					if(!taxonConcept.canonicalform.equals(taxonConcept.name))
                            //						recos.add(new Recommendation(name:taxonConcept.canonicalform, taxonConcept:taxonObj));
                            //					if(!taxonConcept.canonicalform.equals(taxonConcept.normalizedform))
                            //						recos.add(new Recommendation(name:taxonConcept.normalizedform, taxonConcept:taxonObj));
                            //					if(taxonConcept.binomialform && !taxonConcept.binomialform.equals(taxonConcept.canonicalform))
                            //						recos.add(new Recommendation(name:taxonConcept.binomialform, taxonConcept:taxonObj));
                            // TODO giving species subspecies options to parent taxonEntries
                            //noOfNames++;
                        }
                    }
                    noOfNames += recommendationService.save(recos);
                    recos.clear();
                    offset = offset + limit;
                }
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

    private int updateTaxOnConceptForReco(){
        String taxonDefQuery = "select r.id as recoid, t.id as taxonid from recommendation as r, taxonomy_definition as t where r.name ilike t.canonical_form and r.taxon_concept_id is null and r.is_scientific_name = true";
        String synonymQuery = "select r.id as recoid, s.taxon_concept_id as taxonid from recommendation as r, synonyms as s where r.name ilike s.canonical_form and r.taxon_concept_id is null and r.is_scientific_name = true";
        String commnonNameQuery = """
        select r.id as recoid,  t.id as taxonid, r.language_id as rl, c.language_id as c_lang from recommendation as r, taxonomy_definition as t, common_names as c where 
        r.name ilike c.name and 
        ((r.language_id is null and c.language_id is null) or (r.language_id is not null and c.language_id is not null and r.language_id = c.language_id ) or (r.language_id = c.language_id )) and 
        c.taxon_concept_id = t.id and c.taxon_concept_id is not null and
        r.taxon_concept_id is null and r.is_scientific_name = false;
        """;
        def queryList = [taxonDefQuery, synonymQuery, commnonNameQuery]
        int limit = BATCH_SIZE, noOfNames = 0

        queryList.each{ query ->
            int offset = 0
            def recos = new ArrayList<Recommendation>();
            def conn = new Sql(dataSource)
            def conn1;
            def tmpTableName = "tmp_table_update_taxonconcept"
            try {
                try {
                    conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as " + query);
                } finally {
                    conn.close();
                }
                while(true) {
                    try {
                        def recommendationList;
                        try {
                            conn1 = new Sql(dataSource)
                            recommendationList = conn1.rows("select recoid, taxonid from " + tmpTableName + " order by recoid limit " + limit + " offset " + offset);
                        } finally {
                            conn1.close();
                        }

                        Recommendation.withNewTransaction {

                            recommendationList.each { r ->
                                Recommendation rec = Recommendation.read(r.recoid);
                                rec.taxonConcept = TaxonomyDefinition.read(r.taxonid);
                                recos.add(rec);
                                //noOfNames++;
                            }
                            noOfNames += recommendationService.save(recos);
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
        }
        log.info "Total number of updated recommendations are $noOfNames"
        return noOfNames
    }


    /**
     * 
     * @return
     */
    int syncSynonyms() {
        log.info "Importing synonyms into recommendations"
        def recos = new ArrayList<Recommendation>();
        int offset = 0, noOfNames = 0, limit = BATCH_SIZE;
        def conn = new Sql(dataSource)

        def tmpTableName = "tmp_synonyms"
        try {
            conn.executeUpdate("CREATE TABLE " + tmpTableName +  " as select n.canonical_form as canonical_form, n.taxon_concept_id as taxonConcept from synonyms n left outer join recommendation r on n.canonical_form = r.name and n.taxon_concept_id = r.taxon_concept_id where r.name is null and n.canonical_form is not null group by n.canonical_form, n.taxon_concept_id order by n.taxon_concept_id");
            conn.executeUpdate("ALTER TABLE " + tmpTableName +  " ADD COLUMN id SERIAL PRIMARY KEY");
        } finally {
            conn.close();
        }
        while(true) {
            def synonyms
            try {
                conn = new Sql(dataSource);
                synonyms = conn.rows("select canonical_form, taxonConcept from " + tmpTableName + " order by id limit " + limit + " offset " + offset);

                Recommendation.withNewTransaction {
                   //def synonyms = Synonyms.findAll("from Synonyms as synonym left join Recommendation as recommendation with synonym.name = recommendation.name and synonym.taxonConcept = recommendation.taxonConcept where r.name is null)", [max:NAME_BATCH_TO_LOAD, offset:offset]);

                    synonyms.each { synonym ->
                        recos.add(new Recommendation(name:synonym.canonical_form, taxonConcept:TaxonomyDefinition.read(synonym.taxonconcept)));
                        //noOfNames++
                    }

                    offset = offset + limit;		
                    noOfNames += recommendationService.save(recos);
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
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
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
    def syncCommonNames() {
        log.info "Importing common names into recommendations"
        def recos = new ArrayList<Recommendation>();
        int offset = 0, noOfNames = 0, limit=BATCH_SIZE;
        def conn = new Sql(dataSource)
        def tmpTableName = "tmp_common_names"
        def selectQuery = """
        select n.name as name, n.taxon_concept_id as taxonConcept, n.language_id as language from common_names n left outer join recommendation r on 
        n.name ilike r.name and 
        ((n.taxon_concept_id is null and r.taxon_concept_id is null) or (n.taxon_concept_id is not null and r.taxon_concept_id is not null and n.taxon_concept_id = r.taxon_concept_id) or (n.taxon_concept_id = r.taxon_concept_id)) and
        ((r.language_id is null and n.language_id is null) or (r.language_id is not null and n.language_id is not null and r.language_id = n.language_id ) or (r.language_id = n.language_id ))
        where r.name is null 
        group by n.name, n.taxon_concept_id, n.language_id, n.id order by n.taxon_concept_id
        """
        try {
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
                        recos.add(new Recommendation(name:cName.name, isScientificName:false, languageId:cName.language, taxonConcept:TaxonomyDefinition.read(cName.taxonconcept)));
                        //noOfNames++
                    }
                    noOfNames += recommendationService.save(recos);
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
        //recommendationService.save(recos);
        try {
            conn.executeUpdate("DROP TABLE IF EXISTS " + tmpTableName);
        } finally {
            conn.close();
        }
        log.info "Imported common names into recommendations : "+noOfNames
        return noOfNames
    }


}
