package species.participation

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.auth.SUser
import species.Resource

class Comment{

	def activityFeedService
	
	//comment basic info
	String body;
	Date dateCreated;
	Date lastUpdated;
	//main comment thread may have subject (i.e will be shown in group discussion foram)
	String subject; 

	//comment holder (i.e recoVote, image)
	Long commentHolderId;
	String commentHolderType;

	//root holder(i.e observation, group)
	Long rootHolderId;
	String rootHolderType;

	//to store immediate parent comment 
	Long parentId;
	//to store main comment thread
	Long mainParentId;
	
	
	static hasMany = [likes:SUser, attachments:Resource];
	static belongsTo = [author:SUser];

	static constraints = {
		body blank:false;
		parentId nullable:true;
		mainParentId nullable:true;
		subject nullable:true;
	}

	static mapping = {
		version : false;
		body type:'text';
		
		//fething this right away
		author fetch: 'join'
		
		rootHolderId index: 'rootHolderId_Index'
		rootHolderType index: 'rootHolderType_Index'
		lastUpdated index: 'lastUpdated_Index'
		
		commentHolderId index: 'commentHolderId_Index'
		commentHolderType index: 'commentHolderType_Index'
	}

	static int fetchCount(commentHolder, rootHolder, refTime, timeLine){
		timeLine = (timeLine)?:"older"
		refTime = getValidDate(refTime)
		if(commentHolder || rootHolder){
			return Comment.withCriteria(){
				projections {
					count('id')
				}
				and{
					if(commentHolder){
						eq('commentHolderId', commentHolder.id)
						eq('commentHolderType', ActivityFeedService.getType(commentHolder))
					}
					if(rootHolder){
						eq('rootHolderId', rootHolder.id)
						eq('rootHolderType', ActivityFeedService.getType(rootHolder))
					}
					if(refTime){
						(timeLine == "older") ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
					}
				}
			}[0]
		}else{
			return 0;
		}
	}

	static int fetchSuperCount(rootHolder, refTime, timeLine){
		return fetchCount(null, rootHolder, refTime, timeLine)
	}
	

	static fetchComments(commentHolder, rootHolder, max, refTime, timeLine){
		timeLine = (timeLine)?:"older"
		refTime = getValidDate(refTime)
		if(!refTime){
			return Collections.EMPTY_LIST
		}
		return Comment.withCriteria(){
			and{
				if(commentHolder){
					eq('commentHolderId', commentHolder.id)
					eq('commentHolderType', ActivityFeedService.getType(commentHolder))
				}
				if(rootHolder){
					eq('rootHolderId', rootHolder.id)
					eq('rootHolderType', ActivityFeedService.getType(rootHolder))
				}
				(timeLine == "older") ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
			}
			maxResults max
			order 'lastUpdated', 'desc'
		}
	}

	static fetchSuperComments(rootHolder, max, refTime, timeLine){
		return fetchComments(null, rootHolder, max, refTime, timeLine)
	}
	
	private static getValidDate(String timeIn){
		if(!timeIn){
			return null
		}
		
		try{
			return new Date(timeIn.toLong())
		}catch (Exception e) {
			e.printStackTrace()
		}
		return null
	}
	
	def isMainThread(){
		return mainParentId == null;
	}
	
	
	def deleteComment(){
		this.delete(flush:true, failOnError:true)
		if(isMainThread()){
			deleteAllChild()
		}else{
			setParentToNull()
		}
	}
	
	def afterInsert(){
		//activityFeedService.addActivityFeed(activityFeedService.getDomainObject(rootHolderType, rootHolderId), this, author, activityFeedService.COMMENT_ADDED)
	}
	
	def beforeDelete(){
		activityFeedService.deleteFeed(this);
	
	}
	
	def deleteAllChild(){
		def commentList = Comment.findAllByMainParentId(this.id)
		commentList.each{ Comment c ->
			try{
				//Comment.withNewSession {
					c.delete(flush:true, failOnError:true)
				//} 
			}catch(Exception e){
				e.printStackTrace()
			}
		}
	}
	
	def setParentToNull(){
		def commentList = Comment.findAllByParentId(this.id)
		commentList.each{ Comment c ->
			try{
				//Comment.withNewSession {
					c.parentId = null
					c.save(flush:true)
				//}
			}catch(Exception e){
				e.printStackTrace()
			}
		}
	}
	
	def fetchParent(){
		return Comment.read(parentId)
	}
	
	def fetchMainThread(){
		return Comment.read(mainParentId)
	}
	
	def fetchParentText(){
		def parentComment = fetchParent()
		if(parentComment){
			return parentComment.body
		}
		return "Parent comment has been deleted"
	}
	
	def fetchParentCommentAuthor(){
		def parentComment = fetchParent()
		if(parentComment){
			return parentComment.author
		}
		return null
	}
	
	def onAddComment(Comment comment){
		try {
			activityFeedService.getDomainObject(comment.rootHolderType, comment.rootHolderId).onAddComment(comment)
		}catch (MissingMethodException e) {
			//e.printStackTrace();
		}
	}
}
