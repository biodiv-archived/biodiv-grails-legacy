package species.namelist

import org.apache.commons.logging.LogFactory;

import species.TaxonomyDefinition;
import species.Synonyms;
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.util.XmlParser

class Utils {
	
	private static final String COL_SITE = 'http://www.catalogueoflife.org'
	private static final String COL_URI = '/annual-checklist/2014/webservice'
	
	private static final int BATCH_SIZE = 100000
	private static final log = LogFactory.getLog(this);

	private static final String ACCEPTED_NAME = "accepted name"
	private static final String SYNONYM = "synonym"
	private static final String PROV_ACCEPTED_NAME = "provisionally accepted name"
	private static final String COMMON_NAME = "common name"
	private static final String AMBI_SYN_NAME = "ambiguous synonym"
	private static final String MIS_APP_NAME = "misapplied name"

	static void generateColStats(String sourceDir){
		File f = new File(sourceDir)
		if(!f.exists()){
			println "soruce Dir not exists " + sourceDir
			return
		}
		generateReport(f, TaxonomyDefinition.class)
		generateReport(f, Synonyms.class)
	}
	
	
	private static generateReport(File soruceDir, Class c){
		File taxonSourceDir = new File(soruceDir, c.simpleName)
		if(!taxonSourceDir.exists()){
			println " source dir not exisst " + taxonSourceDir
			return 
		}
		File reportFile = new File(soruceDir, c.simpleName + "_report.csv")
		if(reportFile.exists()){
			reportFile.delete()
			reportFile.createNewFile()
		}
		writeHeader(reportFile)
		
		long offset = 0
		int i = 0
		while(true){
			List tds = c.list(max: BATCH_SIZE, offset: offset, sort: "rank", order: "asc")
			if(tds.isEmpty()){
				break
			}
			offset += BATCH_SIZE
			tds.each {
				println "===== Analysing name " + it.canonicalForm + "  name id " + it.id + "   index >>>>>> " + (++i)
				writeStat(taxonSourceDir, reportFile, it)
			}
		}
		
	}
	
	private static void writeHeader(File f){
		println "writing header  " + f
		f << "Id|Canonical Form|Total Result Found|Col Error Msg|Accepted Names|Prov Accepted Name|Synonyms|Ambiguous synonym|Common Name|Misapplied name\n"
	}

	private static void writeStat(sourceDir, reportFile, taxon){
		File f = new File(sourceDir, "" + taxon.id + ".xml" )
		if(!f.exists()){
			println "========== File not avalibel for taxon " + taxon.id + "   name " + taxon.canonicalForm
			return
		}
		
		def results = new XmlParser().parse(f)
		StringBuilder sb = new StringBuilder()
		sb.append(taxon.id + "|") 
		sb.append(taxon.canonicalForm + "|")
		sb.append(results.'@total_number_of_results' + "|")
		sb.append(results.'@error_message' + "|")

		StringBuilder accName = new StringBuilder()
		StringBuilder synonyms = new StringBuilder()
		StringBuilder provAccName = new StringBuilder()
		StringBuilder commonName = new StringBuilder()
		StringBuilder ambiSynonym = new StringBuilder()
		StringBuilder misAppliedName = new StringBuilder()
		results.result.each { r ->
			String status = r.name_status.text()
			if(status.equalsIgnoreCase(ACCEPTED_NAME))
				accName.append(r.rank.text() + "-" + r.name.text() + ", ")
			else if(status.equalsIgnoreCase(PROV_ACCEPTED_NAME))
				provAccName.append(r.rank.text() + "-" + r.name.text() + ", ")  
			else if(status.equalsIgnoreCase(SYNONYM))
				synonyms.append(r.rank.text() + "-" + r.name.text() + ", ")
			else if(status.equalsIgnoreCase(AMBI_SYN_NAME))
				ambiSynonym.append(r.rank.text() + "-" + r.name.text() + ", ")    
			else if(status.equalsIgnoreCase(COMMON_NAME))
				commonName.append(r.name.text() + ", ")
			else if(status.equalsIgnoreCase(MIS_APP_NAME))
				misAppliedName.append(r.name.text() + ", ")    
		}
		sb.append(accName.toString() + "|")
		sb.append(provAccName.toString() + "|")
		sb.append(synonyms.toString() + "|")
		sb.append(ambiSynonym.toString() + "|")
		sb.append(commonName.toString() + "|")
		sb.append(misAppliedName.toString())
		
		reportFile << sb.toString() + "\n"
	}
		
	static void downloadColXml(String sourceDir){
		println "================ strating "
		
		File f = new File(sourceDir)
		if(!f.exists()){
			f.mkdirs()
		}
		
		saveInBatch(new File(f, TaxonomyDefinition.class.simpleName), TaxonomyDefinition.class)
		saveInBatch(new File(f, Synonyms.class.simpleName), Synonyms.class)

	}

	private static saveInBatch(File sourceDir, domainClass){
		if(!sourceDir.exists()){
			sourceDir.mkdirs()
		}

		long offset = 0
		int i = 0
		while(true){ 
			List tds = domainClass.list(max: BATCH_SIZE, offset: offset, sort: "rank", order: "asc")
			tds.each {
				println it.rank +  "    " + it.id + "   " +  it.canonicalForm 
			}
			if(tds.isEmpty()){
				break
			}
			offset += BATCH_SIZE
			tds.each { 
				println "===== Searching name " + it.canonicalForm + "   index >>>>>> " + (++i)  	
				saveFile(sourceDir, it)
			}
		}
	}
	
	private static saveFile(File sourceDir, taxon){
		def name = taxon.canonicalForm
		def id = taxon.id
		def http = new HTTPBuilder()
		http.request( COL_SITE, GET, TEXT ) { req ->
			uri.path = COL_URI
			uri.query = [ name:name, response:'full', format:'xml']
			//headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
			headers.Accept = 'text/xml'

			response.success = { resp, reader ->
				assert resp.statusLine.statusCode == 200
				println "Got response: ${resp.statusLine}"
				//println "Content-Type: ${resp.headers.'Content-Type'}"
				def xmlText =  reader.text
				try{
					File f = new File(sourceDir, "" + id + ".xml")
					if(f.exists()){
						f.delete()
						f.createNewFile()
					}
					println "-- Writing to file " + f.getAbsolutePath() 
					f.write(xmlText)
				}catch(Exception e){
					println e.message
				}
			}
			response.'404' = { println 'Not found' }
		}
	}

	static void searchCol(String name){
		//http://www.catalogueoflife.org/col/webservice?name=Tara+spinosa

		def http = new HTTPBuilder()
		http.request( COL_SITE, GET, TEXT ) { req ->
			uri.path = COL_URI
			uri.query = [ name:name, response:'full', format:'xml']
			//headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
			headers.Accept = 'text/xml'

			response.success = { resp, reader ->
				assert resp.statusLine.statusCode == 200
				println "Got response: ${resp.statusLine}"
				println "Content-Type: ${resp.headers.'Content-Type'}"
				def xmlText =  reader.text
				//println xmlText
				printResponse(xmlText)
			}
			response.'404' = { println 'Not found' }
		}
	}

	static printResponse(String xmlText){
		def results = new XmlParser().parseText(xmlText)
		println results.'@total_number_of_results'
		println results.'@number_of_results_returned'
		println results.'@error_message'
		println results.'@version'
		
		int i = 0
		results.result.each { r ->
			println " Starting result ======= ${++i}"   
			println r.name.text()
			println r.rank.text()
			println r.name_status.text()
			println r.author.text()
			println r.source_database.text()
			println r.source_database_url.text()
			println r.url.text()

			println "============= references  "
			r.references.reference.each { ref ->
				println ref.author.text()
				println ref.source.text()
			}

			println "============= higher taxon  "
			r.classification.taxon.each { t ->
				println t.rank.text() + " == " + t.name.text()
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

			println "============End result========"
		}
	}
	
	static void searchCol1(String genus, String species){
		def http = new HTTPBuilder()
		//http://www.catalogueoflife.org/webservices/status/query/key/30554fc5008829b418639e58b66eac1b/genus/mangifera/species/indica

		String queryPath = (genus) ? "/genus/"+genus : ""
		queryPath = (species) ? queryPath + "/species/"+species : queryPath
		
		http.request( COL_SITE, GET, TEXT ) { req ->
			uri.path = '/webservices/synonyms/query/key/30554fc5008829b418639e58b66eac1b' + queryPath
			//uri.query = [ v:'1.0', q: 'Calvin and Hobbes' ]
			//headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
			headers.Accept = 'text/xml'

			response.success = { resp, reader ->
				assert resp.statusLine.statusCode == 200
				println "Got response: ${resp.statusLine}"
				println "Content-Type: ${resp.headers.'Content-Type'}"
				def xmlText =  reader.text
				//println xmlText
				printResponse(xmlText)
			}
			response.'404' = { println 'Not found' }
		}
	}

	static printResponse1(String xmlText){
		def xp = new XmlParser().parseText(xmlText).response
		def accpetedNames = xp.accepted_name
		println "accepted name size " + accpetedNames.size()
		accpetedNames.each { an ->
			println "===================== start accepted name "
			println an.'@id'
			println an.kingdom.text()
			println an.phylum.text()
			println an.author.text()

			def synonyms = an.synonyms.synonym
			println " synonums size " + synonyms.size()
			println "start synonyms==== "
			synonyms.each { sn ->
				println sn.species.text()
				println sn.author.text()
			}
			println "=== end synonyms"

			println "--------------End--------------------"
		}
		
	}
}
