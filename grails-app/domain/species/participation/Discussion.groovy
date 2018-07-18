package species.participation

import java.util.Date;
import java.util.List;

import species.auth.SUser;
import species.groups.UserGroup;

import org.grails.taggable.Taggable;
import org.grails.rateable.*

import species.participation.Flag;
import species.participation.Follow;
import species.participation.Featured;
import species.Language;
import species.Resource;

class Discussion implements Taggable, Rateable,  Serializable {
	
	def springSecurityService;
	def commentService;
	def grailsApplication;
	def discussionService;
	
	String subject;
	String body;
	
	//to hold non html form of body
	String plainText;
	
	
	int flagCount = 0;
    int featureCount = 0;
	long visitCount = 0;
	
	SUser author;
    Language language;
	
	Date createdOn = new Date();
	Date lastRevised = createdOn;

	boolean isDeleted = false
	boolean agreeTerms = false
	
	static constraints = {
		body nullable:false, blank:false
		plainText nullable:false, blank:false
		subject nullable:false, blank:false
	}
	
	static hasMany = [userGroups:UserGroup]
	static belongsTo = [SUser, UserGroup]
	
	static mapping = {
		subject type:"text"
		body type:"text"
		plainText type:"text"
	}

	def beforeUpdate(){
		if(isDirty() && !isDirty('visitCount')){
			this.lastRevised = new Date();
		}
	}
	
	Resource mainImage() {
		String reprImage = "Discussion.jpg"
		String name = (new File(grailsApplication.config.speciesPortal.content.rootDir + "/" + reprImage)).getName()
		return new Resource(fileName: "discussions"+File.separator+name, type:Resource.ResourceType.IMAGE, context:Resource.ResourceContext.DISCUSSION, baseUrl:grailsApplication.config.speciesPortal.content.serverURL)
	 }

	
    List fetchAllFlags(){
        def fList = Flag.findAllWhere(objectId:this.id,objectType:this.class.getCanonicalName());
        return fList;
	}

    boolean fetchIsFollowing(SUser user=springSecurityService.currentUser){
		return Follow.fetchIsFollowing(this, user)
	}

	List featuredNotes() {
		return Featured.featuredNotes(this, null);
	}
	
	String fetchSpeciesCall(){
		return subject;
	}
	
	long getPageVisitCount(){
		return visitCount
	}
	
	int fetchCommentCount(){
		return commentService.getCount(null, this, null, null)
	}
	
	int activeUserCount(){
		return ActivityFeed.withCriteria() {
			projections {
				countDistinct('author')
			}
			and{
				eq('rootHolderType', this.class.canonicalName)
				eq('rootHolderId', this.id)
			}
		}[0] 
	}
	
	def incrementPageVisit(){
		visitCount++
	}
	
	String summary(Language userLanguage = null) {
		return this.body?:'';
	}

	def onAddActivity(af, flushImmidiatly){
		lastRevised = new Date();
		if(!save(flush:flushImmidiatly)){
			errors..allErrors.each { log.error it }
		}
	}
	
	def fetchList(params, max, offset){
		return discussionService.getFilteredDiscussions(params, max, offset)
	}
	
	def fetchSpeciesGroup(){
		return null
	}
	
	def notes(){
		return body
	}
	
    static long countDiscussions() {
        def c = Discussion.createCriteria();
        def count = c.count {
            cache true;
        }
        return count;
    }
}
