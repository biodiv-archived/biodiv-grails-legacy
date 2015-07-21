import species.Species;
import species.SpeciesField;

import javax.sql.DataSource
import groovy.sql.Sql
import groovyx.net.http.HTTPBuilder;
import static groovyx.net.http.ContentType.*;
import static groovyx.net.http.Method.*;


def checkSearchSpeciesHitRatio() {
    def dataSource =  ctx.getBean("dataSource");
    def sql =  Sql.newInstance(dataSource);
    int[] a = [0,0,0,0,0,0,0,0,0,0];
    int noHit = 0;

    def http = new HTTPBuilder()

    sql.eachRow("select s.id as id, t.name as name from species s, taxonomy_definition t where s.taxon_concept_id=t.id and is_deleted=false limit 10" ) { s ->
        println s;
        http.request(  'http://indiabiodiversity.org/api/search/', GET, JSON) { 
            uri.path = 'select'
            //headers.'X-Auth-Token' = 'nstjbfui4s3rfmnsndjkekbnntresknd'//local
            headers.'X-Auth-Token' = 'i3tg4tvqb7q068hcqhl2nmcsjptbqnc3'//kk
            uri.query = [ 'query': s.name.replaceAll('<.*>',''), 'max':10, 'aq.object_type':'Species', resultType:'json', format:'json'] 

            response.success = { resp, json ->
                if(resp.isSuccess()) {
                    boolean found = false;
                    json.instanceList.eachWithIndex { instance, index ->
                        if(instance.id == s.id) {
                            found = true; 
                            a[index] ++;
                        }
                    }
                    if(!found) noHit++;
                }
            }            
        response.failure = { resp ->  
            println '----request failed'
            println resp;
        }
        } 
    }
    println "HITS : "+a
    println "NO HIT : "+noHit;
}

checkSearchSpeciesHitRatio();


