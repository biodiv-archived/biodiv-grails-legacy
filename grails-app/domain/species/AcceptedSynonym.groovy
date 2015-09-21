package species

class AcceptedSynonym {

    TaxonomyDefinition accepted;
    SynonymsMerged synonym;
    
    static constraints = {
        accepted(unique: ['synonym']);
    }

    static createEntry(TaxonomyDefinition accepted, SynonymsMerged synonym) {
        //AcceptedSynonym.withNewSession {
        def ent1 = AcceptedSynonym.findWhere(accepted: accepted, synonym: synonym)
        if(ent1) return;
        def ent = new AcceptedSynonym(accepted: accepted, synonym: synonym)
        if(!ent.save()) {
            ent.errors.allErrors.each { log.error it }
        }
        //}
    }
    
    static List<SynonymsMerged> fetchSynonyms(TaxonomyDefinition accepted) {
        def res = AcceptedSynonym.findAllByAccepted(accepted);
        List<SynonymsMerged> synonyms = [];
        res.each {
            synonyms.add(it.synonym);
        }
        return synonyms;
    }

    static List<TaxonomyDefinition> fetchAcceptedNames(SynonymsMerged synonym) {
        def res = AcceptedSynonym.findAllBySynonym(synonym);
        List<TaxonomyDefinition> acceptedNames = [];
        res.each {
            acceptedNames.add(it.accepted);
        }
        return acceptedNames;
    }

    static removeEntry(TaxonomyDefinition accepted, SynonymsMerged synonym) {
        AcceptedSynonym.withNewSession {
            def ent = AcceptedSynonym.findWhere(accepted: accepted, synonym: synonym)
            if(ent) {
                try {
                    println "deleting"
                    if(!ent.delete(flush:true,failOnError:true)){
                        ent.errors.allErrors.each { log.error it }
                    }
                }catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

}
