package species.participation

import org.apache.commons.logging.LogFactory;

import species.TaxonomyDefinition;
import species.Synonyms;
import species.NamesMetadata;
import species.TaxonomyRegistry
import species.Classification;
import species.ScientificName.TaxonomyRank

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.sql.Sql
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

	def dataSource
	
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
            Map id_details = new HashMap()
            println " Starting result ======= ${++i}"
            println r.id.text().getClass()
            temp['externalId'] = r.id.text()
            println r.name.text()
            temp['name'] = r.name.text()
            println r.rank.text()
            temp['rank'] = r.rank.text().toLowerCase()
            temp[r.rank.text().toLowerCase()] = r.name.text()
            println r.name_status.text()
            id_details[r.name.text()] = r.id.text();
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
                    id_details[t.name.text()] = t.id.text()
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
            temp['id_details'] = id_details
            finalResult.add(temp);
            println "============End result========"
        }
        return finalResult
    }
	
	
	def getNamesFromTaxon(params){
		log.debug params
		def sql = new Sql(dataSource)
		def sqlStr, rs
		def classSystem = params.classificationId.toLong()
		def parentId = params.parentId
		def limit = params.limit ? params.limit.toInteger() : 1000
		def offset = params.offset ? params.limit.toLong() : 0
		if(!parentId) {
			sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name, s.path as path, ${classSystem} as classificationid, position as position \
				from taxonomy_registry s, \
				taxonomy_definition t \
				where \
				s.taxon_definition_id = t.id and "+
				(classSystem?"s.classification_id = :classSystem and ":"")+
				"t.rank = 0";
			rs = sql.rows(sqlStr, [classSystem:classSystem])
		} else {
			sqlStr = "select t.id as taxonid, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classificationid, position as position \
				from taxonomy_registry s, \
				taxonomy_definition t \
				where \
				s.taxon_definition_id = t.id and "+
				(classSystem?"s.classification_id = :classSystem and ":"")+
				"s.path like '"+parentId+"%' " +
				"order by t.rank, t.name asc limit :limit offset :offset";
			rs = sql.rows(sqlStr, [classSystem:classSystem, limit:limit, offset:offset])
		}
		
		println "total result size === " + rs.size()
		
		def dirtyList = []
		def workingList = []
		def cleanList = []
		
		rs.each {
			if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.DIRTY.value())){
				dirtyList << it
			}else if(it.position.equalsIgnoreCase(NamesMetadata.NamePosition.WORKING.value())){
				workingList << it
			}else{
				cleanList << it
			}
		}		
		return [dirtyList:dirtyList, workingList:workingList, cleanList:cleanList]	
	}
	
	def getNameDetails(params){
		log.debug params
		def taxonDef = TaxonomyDefinition.read(params.taxonId.toLong())
		def taxonReg = TaxonomyRegistry.findByClassificationAndTaxonDefinition(Classification.read(params.classificationId.toLong()), taxonDef);
		def result = taxonDef.fetchGeneralInfo()
        result['taxonId'] = params.taxonId;
		if(taxonReg) {
			result['taxonRegId'] = taxonReg.id?.toString()
			taxonReg.path.tokenize('_').each { taxonDefinitionId ->
				def td = TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId));
				result.put(TaxonomyRank.getTRFromInt(td.rank).value().toLowerCase(), td.name);
			}
		}
		return result
	}

}
