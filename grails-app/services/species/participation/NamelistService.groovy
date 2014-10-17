package species.participation

import org.apache.commons.logging.LogFactory;

import species.TaxonomyDefinition;
import species.Synonyms;
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.util.XmlParser

class NamelistService {
   
    private static final String COL_SITE = 'http://www.catalogueoflife.org'
	private static final String COL_URI = '/annual-checklist/2014/webservice'
	
	private static final int BATCH_SIZE = 100
	private static final log = LogFactory.getLog(this);

	private static final String ACCEPTED_NAME = "accepted name"
	private static final String SYNONYM = "synonym"
	private static final String PROV_ACCEPTED_NAME = "provisionally accepted name"
	private static final String COMMON_NAME = "common name"
	private static final String AMBI_SYN_NAME = "ambiguous synonym"
	private static final String MIS_APP_NAME = "misapplied name"

	List searchCOL(String input, String searchBy){
		//http://www.catalogueoflife.org/col/webservice?name=Tara+spinosa

		def http = new HTTPBuilder()
        http.request( COL_SITE, GET, TEXT ) { req ->
            uri.path = COL_URI
            if(searchBy == 'name') {
                uri.query = [ name:input, response:'full', format:'xml']
            } else if(searchBy == 'id') {
                uri.query = [ id:input, response:'full', format:'xml']
            }
            //headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            headers.Accept = 'text/xml'

            response.success = { resp, reader ->
                assert resp.statusLine.statusCode == 200
                println "Got response: ${resp.statusLine}"
                println "Content-Type: ${resp.headers.'Content-Type'}"
                def xmlText =  reader.text
                //println xmlText
                return responseAsMap(xmlText, searchBy);
            }
            response.'404' = { println 'Not found' }
        }
    }

    List responseAsMap(String xmlText, String searchBy){
        List finalResult = []
        def results = new XmlParser().parseText(xmlText)
        println results.'@total_number_of_results'
        println results.'@number_of_results_returned'
        println results.'@error_message'
        println results.'@version'

        int i = 0
        results.result.each { r ->
            Map temp = new HashMap()
            println " Starting result ======= ${++i}"
            println r.id.text().getClass()
            temp['externalId'] = r.id.text()
            println r.name.text()
            temp['name'] = r.name.text()
            println r.rank.text()
            temp['rank'] = r.rank.text().toLowerCase()
            temp[r.rank.text().toLowerCase()] = r.name.text()
            println r.name_status.text()
            temp['nameStatus'] = r.name_status.text().tokenize(' ')[0]
            println r.author.text()
            temp['authorString'] = r.author.text()
            println r.source_database.text()
            temp['sourceDatabase'] = r.source_database.text()
            println r.source_database_url.text()
            println r.url.text()
            println "==GETTING GROUP==" 
            temp['group'] = r.classification.taxon[0].name.text()
            println r.classification.taxon[0].name.text()

            if(searchBy == 'id') {
                println "============= references  "
                r.references.reference.each { ref ->
                    println ref.author.text()
                    println ref.source.text()
                }

                println "============= higher taxon  "
                r.classification.taxon.each { t ->
                    println t.rank.text() + " == " + t.name.text()
                    temp[t.rank.text().toLowerCase()] = t.name.text()
                }

                println "============= child taxon  "
                r.child_taxa.taxon.each { t ->
                    println t.name.text()
                    println t.author.text()
                }

                println "============= synonyms  "
                r.synonyms.synonym.each { s ->
                    println s.rank.text() + " == " + s.name.text()
                    println "============= references  "
                    s.references.reference.each { ref ->
                        println ref.author.text()
                        println ref.source.text()
                    }
                }

            }
            finalResult.add(temp);
            println "============End result========"
        }
        return finalResult
    }

}
