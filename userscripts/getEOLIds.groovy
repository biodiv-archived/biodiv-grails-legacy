import grails.converters.JSON;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.ContentType;
import groovyx.net.http.Method;
import species.TaxonomyDefinition;

File data = new File("/home/cepf/EOLIds_exactmatch_v1.tsv");

def http = new HTTPBuilder()
boolean flag = true;

int offset = 0;
while(flag) {
	int i = 0;
	TaxonomyDefinition.findAllByRank(8, [max:10, offset:offset]).each { taxon ->
		http.request( "http://eol.org/api/search/1.0" , Method.GET, ContentType.JSON) {
			uri.path = taxon.canonicalForm+'.json'
			uri.query = [ exact:1 ]
			response.success = { resp, json ->
				if(resp.isSuccess()) {
					println "EOL search result for : "+json
					if(!json.results)
						data << taxon.id+"\t"+taxon.canonicalForm+"\t"+taxon.rank+"\n"

					json.results.each { r ->
						data << taxon.id+"\t"+taxon.canonicalForm+"\t"+taxon.rank+"\t"+r.title  + "\t" + r.id + "\t" + r.link+"\n";
					}
				}
			}
			response.failure = { resp ->  println 'EOL search request failed for taxon : '+taxon }
		}
		i++;
	}
	if(!i) flag = false;
	offset = offset + i;
}