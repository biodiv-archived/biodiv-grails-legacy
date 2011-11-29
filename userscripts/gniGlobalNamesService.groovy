import groovyx.net.http.HTTPBuilder;
import static groovyx.net.http.ContentType.*;
import static groovyx.net.http.Method.*;

def names = ["Stagonospora polyspora M.T. Lucas & Sousa da Câmara 1934"]
def http = new HTTPBuilder()
        http.request(  'http://gni.globalnames.org', POST, JSON) { 
            uri.path = 'parsers.json'
            body = [ names : names.join("|") ] 
            
            response.success = { resp, json ->
                if(resp.isSuccess()) {
                    println json.scientificName.details.species.basionymAuthorTeam.year[0][0]
                }
            }            
            response.failure = { resp ->  
                println 'request failed'
            }
        } 