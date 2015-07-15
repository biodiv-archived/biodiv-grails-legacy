import content.eml.DocSciName;
import species.TaxonomyDefinition;

DocSciName.list().each { docSciNameInstance ->
    DocSciName.withTransaction {
        if(docSciNameInstance.canonicalForm) {
            docSciNameInstance.taxonConcept = TaxonomyDefinition.findByCanonicalForm(docSciNameInstance.canonicalForm);
            if (!docSciNameInstance.save(flush: true)) {
                docSciNameInstance.errors.each {
                }
            }
        }
    }

}
