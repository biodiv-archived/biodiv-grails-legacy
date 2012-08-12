import grails.converters.JSON;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.ContentType;
import groovyx.net.http.Method;
import species.TaxonomyDefinition;
import species.auth.FacebookUser;
import species.auth.SUser;
import species.participation.Observation;

def http = new HTTPBuilder()
boolean flag = true;
def service = ctx.getBean("commentService");

int offset = 0;
while(flag) {
	int i = 0;
	Observation.list(max:1, offset:offset).each { obv ->
		http.request( "https://graph.facebook.com/comments/" , Method.GET, ContentType.JSON) {
			//uri.path = taxon.canonicalForm+'.json'
			String link = 'http://indiabiodiversity.org/biodiv/observation/show/269018';
			uri.query = [ ids:link ]
			response.success = { resp, json ->
				if(resp.isSuccess()) {
					println "FB comments : "+json
					
						json."${link}".comments.data.each { comment ->
							addComment(comment, obv);
							if(comment.comments) {
								comment.comments.data.each { reply ->
									addComment(reply, obv);
								}
							}
						}
					
				}
			}
			response.failure = { resp ->  println 'failed to fetch FB comments for obv ${obv}' }
		}
		//i++;
		i=0
	}
	if(!i) flag = false;
	offset = offset + i;
}

def addComment(comment, obv) {
	def username = comment.from.name
	def fbUserId = comment.from.id
	def message = comment.message
	def createdOn = new Date().parse("yyyy-mm-dd'T'HH:mm:ssZ", comment.created_time) //2012-07-15T07:00:43+0000
	def user = FacebookUser.findByUid(fbUserId);
	if(!user) {
		user = SUser.findByUsername(username);
	}
	
	if(user) {
		service.addComent([author:user, commentBody:message, commentHolderId:obv.id, commentHolderType:'species.participation.Observation', rootHolderId:obv.id, rootHolderType:'species.participation.Observation', dateCreated:createdOn]);
	}
	
}