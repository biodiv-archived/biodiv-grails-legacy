package species.namelist

import org.apache.commons.logging.LogFactory;

import species.ScientificName.TaxonomyRank
import species.TaxonomyDefinition;
import species.Synonyms;
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import groovy.util.XmlParser
import species.participation.Recommendation;
import species.participation.Observation;
import species.Species;
import species.NamesMetadata;

class Utils {

	private static final String COL_SITE = 'http://www.catalogueoflife.org'
	private static final String COL_URI = '/annual-checklist/2015/webservice'
	
	private static final int BATCH_SIZE = 100
	private static final log = LogFactory.getLog(this);

	private static final String ACCEPTED_NAME = "accepted name"
	private static final String SYNONYM = "synonym"
	private static final String PROV_ACCEPTED_NAME = "provisionally accepted name"
	private static final String COMMON_NAME = "common name"
	private static final String AMBI_SYN_NAME = "ambiguous synonym"
	private static final String MIS_APP_NAME = "misapplied name"
	
    private static final int MAX_TO_DOWNLOAD = BATCH_SIZE * 2;


	static void generateColStats(String sourceDir){
		File f = new File(sourceDir)
		if(!f.exists()){
			println "soruce Dir not exists " + sourceDir
			return
		}
		generateReport(f, TaxonomyDefinition.class)
		//generateReport(f, Synonyms.class)
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
        def sortBy = "rank"
        if(c == Synonyms.class) {
            sortBy = 'id'
        }
		writeHeader(reportFile, c)
		
		long offset = 0
		int i = 0
		while(true){
            def cri = TaxonomyDefinition.createCriteria()
            List tds = cri.list (max: BATCH_SIZE , offset:offset) {
                and {
                    //lt('id', 293748L)
                    eq('position', NamesMetadata.NamePosition.DIRTY)
                    //eq('isDeleted', false)//isNull('matchId')
                }
                order('rank','asc')
                order('id','asc')                    
            }
			//List tds = c.list(max: BATCH_SIZE, offset: offset, sort: sortBy, order: "asc")
			if(tds.isEmpty()){
				break
			}
			offset += BATCH_SIZE
			tds.each {
				println "===== Analysing name " + it.canonicalForm + "  name id " + it.id + "   index >>>>>> " + (++i)
				writeStat(taxonSourceDir, reportFile, it, c)
			}
		}
		
	}
	
	private static void writeHeader(File f, Class c){
		println "writing header  " + f
        if(c == Synonyms.class) {
		    f << "Taxon ID|Accepted name Species Id|IBP varbatim|IBP Canonical Form|Accepted name IBP rank|Accepted name Varbatim|IBP author year|IBP status|Has species page|Percent Info|Num of Obv|Total Result Found|Col Error Msg|COL canonical|COL verbatim|COL rank|COL ID|COL Name Status|COL Group|Synonym for accepted name|Accepted Names|Prov Accepted Name|Synonyms|Ambiguous synonym|Common Name|Misapplied name\n"
        } else {
		    f << "Taxon ID|Species Id|IBP varbatim|IBP Canonical Form|IBP rank|IBP author year|IBP status|Has species page|Percent Info|Num of Obv|Total Result Found|Col Error Msg|COL canonical|COL verbatim|COL rank|COL ID|COL Name Status|COL Group|Accepted Names|Prov Accepted Name|Synonyms|Ambiguous synonym|Common Name|Misapplied name\n"
        }
    }

	private static void writeStat(sourceDir, reportFile, taxon, Class c){
		File f = new File(sourceDir, "" + taxon.id + ".xml" )
		if(!f.exists()){
			println "========== File not available for taxon " + taxon.id + "   name " + taxon.canonicalForm
			return
		}
        def results ;
		try {
		    results = new XmlParser().parse(f)
        } catch(Exception e) {return;}
        /*StringBuilder sb = new StringBuilder()
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
		*/
        results.result.each { r ->
            StringBuilder sb = new StringBuilder()
            def q,t,rr;
            def accNameTaxon;
            if(c == Synonyms.class) {
                accNameTaxon = TaxonomyDefinition.read(taxon.taxonConcept?.id)//find accepted name of this synonym
                q = Species.findByTaxonConcept(accNameTaxon);
                rr = TaxonomyRank.getTRFromInt(accNameTaxon.rank).value();
            } else {
                q = Species.findByTaxonConcept(taxon);
                rr = TaxonomyRank.getTRFromInt(taxon.rank).value();
            }
            t = q?q.id:"No sp id"
            def hasSpPage = q?"True":"False"
            def percInfo = q?q.percentOfInfo:"NAN"
            def numOfObv = q?getRelatedObservationByTaxonConcept(taxon.id,0,0L)?.count:"Not a species" 
            if(c == Synonyms.class) {
                sb.append(taxon.taxonConcept?.id + "|")
            } else {
                sb.append(taxon.id + "|")
            }
            sb.append(t + "|") //sp id
            sb.append(taxon.name + "|") //ibp ver
            sb.append(taxon.canonicalForm + "|") //ibp can
            sb.append(rr + "|") // rank
            if(c == Synonyms.class) {
                sb.append(accNameTaxon.name + "|")  //add accepted names verbatim
            }
            sb.append(taxon.authorYear + "|") //author
            sb.append(taxon.status.value() + "|") // status
            sb.append(hasSpPage + "|") // HasSPPage
            sb.append(percInfo + "|") // percentinfo
            sb.append(numOfObv + "|") // no of obv
            sb.append(results.'@total_number_of_results' + "|")
            sb.append(results.'@error_message' + "|")
            sb.append(r.name.text() + "|") //canonical
            sb.append(r.name.text() + " " + r.author?.text() + "|") //verbatim
            sb.append(r.rank?.text() + "|") //rank
            sb.append(r.id.text() + "|") //ID
            sb.append(r.name_status?.text() + "|") //Name status
            sb.append(r.classification?.taxon[0]?.name?.text() + "|") //Group
            if(c == Synonyms.class) {
                def accNamesSTR = '';
                if(r.name_status?.text() == 'synonym') {
                    r.accepted_name.each {
                        accNamesSTR += "<i>" +it.name_html.i.text()+"</i> "+ it.name_html.text()+' *** '
                    }
                } else {
                    accNamesSTR = 'N.A.'    
                }
                sb.append(accNamesSTR + "|")  //add accepted names verbatim
            }
            StringBuilder accName = new StringBuilder()
            StringBuilder synonyms = new StringBuilder()
            StringBuilder provAccName = new StringBuilder()
            StringBuilder commonName = new StringBuilder()
            StringBuilder ambiSynonym = new StringBuilder()
            StringBuilder misAppliedName = new StringBuilder()
			String status = r.name_status.text()
			if(status.equalsIgnoreCase(ACCEPTED_NAME))
				accName.append(r.rank.text() + "-" + r.name.text() /*+ ", "*/)
			else if(status.equalsIgnoreCase(PROV_ACCEPTED_NAME))
				provAccName.append(r.rank.text() + "-" + r.name.text())  
			else if(status.equalsIgnoreCase(SYNONYM))
				synonyms.append(r.rank.text() + "-" + r.name.text())
			else if(status.equalsIgnoreCase(AMBI_SYN_NAME))
				ambiSynonym.append(r.rank.text() + "-" + r.name.text())    
			else if(status.equalsIgnoreCase(COMMON_NAME))
				commonName.append(r.name.text())
			else if(status.equalsIgnoreCase(MIS_APP_NAME)) {
				misAppliedName.append(r.name.text())  
            }
            sb.append(accName.toString() + "|")
            sb.append(provAccName.toString() + "|")
            sb.append(synonyms.toString() + "|")
            sb.append(ambiSynonym.toString() + "|")
            sb.append(commonName.toString() + "|")
            sb.append(misAppliedName.toString())
            reportFile << sb.toString() + "\n"

		}
        /*
		sb.append(accName.toString() + "|")
		sb.append(provAccName.toString() + "|")
		sb.append(synonyms.toString() + "|")
		sb.append(ambiSynonym.toString() + "|")
		sb.append(commonName.toString() + "|")
		sb.append(misAppliedName.toString())

		reportFile << sb.toString() + "\n"
        */
	}
		
	static void downloadColXml(String sourceDir){
		println "================ strating "
	    List errors = [];	
		File f = new File(sourceDir)
		if(!f.exists()){
			f.mkdirs()
		}
		
		saveInBatch(new File(f, TaxonomyDefinition.class.simpleName), TaxonomyDefinition.class, errors)
		//saveInBatch(new File(f, Synonyms.class.simpleName), Synonyms.class)
        println "========ERRORS IN DOWNLOAD ========= " + errors; 

	}

	private static saveInBatch(File sourceDir, domainClass, List errors){
		if(!sourceDir.exists()){
			sourceDir.mkdirs()
		}

		long offset = 0
		int i = 0
        def sortBy = 'id'
        println "=========DOMAIN CLASS ==== " + domainClass
        println "=========Synonyms class ==== " + Synonyms.class.simpleName

        if(domainClass == Synonyms.class) {
            sortBy = 'id'
        }
	
        println "========= STARTING HERE #######  ====== " 
        List tds = [];
        while(true){ 
            tds = domainClass.list(max: BATCH_SIZE, offset: offset, sort: sortBy, order: "asc")
            //tds.add(domainClass.get(158685L));//max: BATCH_SIZE, offset: offset, sort: sortBy, order: "asc")
            println "========= OFFSET  ====== " + offset
            tds.each {
                println (domainClass != Synonyms.class)?it.rank:"No Rank for ${domainClass}" +  "    " + it.id + "   " +  it.canonicalForm 
            }
            offset += BATCH_SIZE
            tds.each { 
                domainClass.withNewTransaction { status ->
                    println "===== Searching name " + it.canonicalForm + "   index >>>>>> " + (++i) 
                    saveFile(sourceDir, it, errors)
                }
            }
            if(tds.isEmpty()){
                println "=========#####  EMPTY ==============="
		break; 
            }
		}
	}
	
	private static saveFile(File sourceDir, taxon, List errors){
		def name = taxon.canonicalForm
        def id = taxon.id
        File f11 = new File(sourceDir, "" + id + ".xml")
        if(f11.exists()){
            println ">>>>> FILE ALREADY EXISTS ======== " + f11
            return
            //f.delete()
            //f.createNewFile()
        }
        try { 
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
        } catch(Exception e) {
            def temp = [:];
            temp.id = id;
            temp.name = name;
            temp.errorMsg = e.message;
            errors.add(temp); 
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

    static void testObv() {
        def numOfObv = "=============" + getRelatedObservationByTaxonConcept(5275L,0, 0L);    //observationService.related(pp).relatedObv
        println "=========NUM OF======= " + numOfObv
    }
    
    private static List<Recommendation> searchRecoByTaxonConcept(taxonConcept){
		if(!taxonConcept) return;
		
		def c = Recommendation.createCriteria();
		def recoList = c.list {
			eq('taxonConcept', taxonConcept)
		}
		
		if(!recoList || recoList.isEmpty()){
			return null
		}
		
		return recoList;
	}

    private static Map getRelatedObservationByTaxonConcept(long taxonConceptId, int limit, long offset){
        def taxonConcept = TaxonomyDefinition.read(taxonConceptId);
        if(!taxonConcept) return ['observations':[], 'count':0]

        List<Recommendation> scientificNameRecos = searchRecoByTaxonConcept(taxonConcept);
        if(scientificNameRecos) {
            def criteria = Observation.createCriteria();
            def observations = criteria.list (max: limit, offset: offset) {
                and {
                    'in'("maxVotedReco", scientificNameRecos)
                        eq("isDeleted", false)
                        eq("isShowable", true)
                }
                order("lastRevised", "desc")
            }
            def count = observations.totalCount;
            def result = [];
            def iter = observations.iterator();
            while(iter.hasNext()){
                def obv = iter.next();
                result.add(['observation':obv, 'title':obv.fetchSpeciesCall()]);
            }
            println "=========COUNT OF RELATED OBV ======== " + count
            return ['observations':result, 'count':count]
        } else {
            return ['observations':[], 'count':0]
        }
    }

}
