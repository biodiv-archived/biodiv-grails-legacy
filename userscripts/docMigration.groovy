import content.eml.DocSciName;
import species.TaxonomyDefinition;


import javax.sql.DataSource
import groovy.sql.Sql
import groovyx.net.http.HTTPBuilder;
import static groovyx.net.http.ContentType.*;
import static groovyx.net.http.Method.*;
import content.eml.Document;

def saveDocSciNameTaxonConcept() {
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
}

def runGNRDService() {
    def dataSource =  ctx.getBean("dataSource");
    def sql =  Sql.newInstance(dataSource);

    def dS = ctx.getBean("documentService");
    def docList = [];

    sql.eachRow(" select id from document where id in (select doc_id from document_token_url where status != 'Success')") { s->
        docList << Document.read(s.id);
    } 
    println docList
    println dS.runDocuments(docList);
}


//saveDocSciNameTaxonConcept();
runGNRDService();
