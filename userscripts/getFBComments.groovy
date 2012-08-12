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
def commentService = ctx.getBean("commentService");

int offset = 0;
while(flag) {
	int i = 0;
	String links = "[";
	Observation.list(max:50, offset:offset, sort:"id").each { obv ->
		//String link = "http://thewesternghats.in/biodiv/observation/show/${obv.id}";
		String link = "http://indiabiodiversity.org/biodiv/observation/show/${obv.id}";
		links += '{"method":"GET", "relative_url":"comments/?ids='+link+'"},'
		i++;
	}
	if(i) {
		links = links[0..-2];
		links += "]";
		println "sending request ${offset}"
		http.request( "https://graph.facebook.com/" , Method.POST, ContentType.JSON) {
			uri.query = [ access_token:'327308053982589|b29Nk-MeVi8IljGDzMVxEgm7vv0', batch:links ]
			response.success = { resp, json ->
				if(resp.isSuccess()) {
					//println "FB comments : "+json

					json.each{set->
						try{
							set.body.each { link, commentsData ->
								def obvId = link[link.lastIndexOf('/')+1..link.length()-1]
								def obv = Observation.get(obvId.toLong());
								commentsData.comments.data.each { comment ->
									try {
										addComment(comment, obv);
										if(comment.comments) {
											comment.comments.data.each { reply ->
												addComment(reply, obv);
											}
										}
									} catch(e) {
										println comment;
										e.printStackTrace();
									}
								}
							}
						} catch(e) {
							println set;
							e.printStackTrace();
						}
					}

				}
			}
			response.failure = { resp ->  println 'failed to fetch FB comments for obv ${obv}' }
		}
	} else {
		break;
	}
	offset = offset + i;
}

def addComment(comment, obv) {
	def commentService = ctx.getBean("commentService");
	def username = comment.from.name
	def fbUserId = comment.from.id
	def message = comment.message
	def createdOn = new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", comment.created_time) //2012-07-15T07:00:43+0000
	def user = FacebookUser.findByUid(fbUserId);
	if(!user) {
		user = SUser.findByUsername(username);
	} else {
		user = user.user;
	}

	if(user) {
		def params = [author:user, commentBody:message, commentHolderId:obv.id, commentHolderType:'species.participation.Observation', rootHolderId:obv.id, rootHolderType:'species.participation.Observation', dateCreated:createdOn, lastUpdated:createdOn];
		println "Adding comment on ${obv}"
		commentService.addComment(params);
	}
}

