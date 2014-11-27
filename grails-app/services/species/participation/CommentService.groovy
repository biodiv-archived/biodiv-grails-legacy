package species.participation

import species.auth.SUser;
import species.participation.Follow;
import species.participation.ActivityFeed;
import org.springframework.web.multipart.MultipartHttpServletRequest

class CommentService {

	static transactional = false
	
	def grailsApplication
    def springSecurityService
	def activityFeedService
    def utilsService
	def userGroupService
	
	def addComment(params){
		log.debug "Adding comment ${params}"
		validateParams(params);
		
		if(!validatePermission(params)){
			return ['success': false, 'msgCode': 'default.comment.not.permitted.message']
		}
		
		// String UserIds with comma seperation to make it as list
		List<String> tagUserIds;
		if(params.tagUserId){
			tagUserIds = Arrays.asList(params.tagUserId.split("\\s*,\\s*"));
		}
		Comment c;
		if(params.commentId){
			c 	= Comment.findById(params.commentId?.toLong());			
			c.body 	= params.commentBody.trim();
			c.language 	=	params.locale_language;
			c.lastUpdated = new Date();
			
		}else{
			
			c = new Comment(author:params.author, body:params.commentBody.trim(), commentHolderId:params.commentHolderId, \
							commentHolderType:params.commentHolderType, rootHolderId:params.rootHolderId, rootHolderType:params.rootHolderType, \
							parentId:params.parentId, mainParentId:params.mainParentId, subject:params.commentSubject?.trim(), language:params.locale_language);
			

			if(params.dateCreated) {
				c.dateCreated = params.dateCreated
				c.lastUpdated = new Date();
			}
			if(params.lastUpdated) {
				c.lastUpdated = params.lastUpdated;
			}

		}
		
		if(!c.save(flush:true)){
			c.errors.allErrors.each { log.error it }
			return ['success': false]
		}else{
			def domainObject = activityFeedService.getDomainObject(c.rootHolderType, c.rootHolderId)
			def feedInstance;
			if(!params.commentId){				
				feedInstance = activityFeedService.addActivityFeed(domainObject, c, c.author, activityFeedService.COMMENT_ADDED)
				utilsService.sendNotificationMail(activityFeedService.COMMENT_ADDED, domainObject, null, params.webaddress, feedInstance);
			}else{
				feedInstance = ActivityFeed.findByActivityHolderIdAndActivityHolderType(c.id,c.class.getCanonicalName());
			}
			if(tagUserIds){
				userTagNofity(tagUserIds,domainObject,feedInstance,params.webaddress);
			}
			return ['success': true, 'commentObj' :c]
		}
	}

	def removeComment(params){
		Comment comment = Comment.get(params.commentId?.toLong());
		try{
            if(comment){
			    comment.deleteComment();
			    return true
            } 
		}catch(e) {
			e.printStackTrace()
		}
		return false
	}


	def getComments(params){
		setDefaultRange(params)
		def rootHolder = getDomainObject(params.rootHolderType, params.rootHolderId)
		def commentHolder = null
		if(params.commentType && (params.commentType == "context")){
			commentHolder = getDomainObject(params.commentHolderType, params.commentHolderId)
		}
		return getComments(commentHolder, rootHolder, params.max, params.refTime, params.timeLine)
	}

	def getComments(commentHolder, rootHolder, max, refTime, timeLine){
		return Comment.fetchComments(commentHolder, rootHolder, max, refTime, timeLine)
	}

	def getCount(params){
		def rootHolder = getDomainObject(params.rootHolderType, params.rootHolderId)
		def commentHolder = null
		if(params.commentType && (params.commentType == "context")){
			commentHolder = getDomainObject(params.commentHolderType, params.commentHolderId)
		}
		return getCount(commentHolder, rootHolder, params.refTime, params.timeLine)
	}

	def getCount(commentHolder, rootHolder, refTime, timeLine){
		return Comment.fetchCount(commentHolder, rootHolder, refTime, timeLine)
	}

	def getCountByUser(author){
		return Comment.countByAuthor(author)
	}

	def likeComment(params){
		Comment comment = Comment.get(params.commentId.toLong());
		comment.addToLikes(params.author)
		if(!comment.save(flush:true)){
			comment.errors.allErrors.each { log.error it }
			return false
		}else{
			return true
		}
	}

	def getCommentByType(params){
		return Comment.findAllByRootHolderType(Observation.getClass().getCanonicalName(),[sort: "lastUpdated", order: "desc"])
	}

	def getDomainObject(className, id){
		return activityFeedService.getDomainObject(className, id)
	}

	private validateParams(params){
		if(params.parentId){
			Comment parentComment = Comment.read(params.parentId.toLong());
			params.mainParentId = parentComment.mainParentId ?: parentComment.id
			params.commentHolderId = parentComment.id
			params.commentHolderType = parentComment.getClass().getCanonicalName()
			params.rootHolderId = parentComment.rootHolderId
			params.rootHolderType = parentComment.rootHolderType
		} 
	}
	
	
	private boolean validatePermission(params){
		//if comment thread
		def object 
		if(params.parentId){
			object = getDomainObject(params.rootHolderType, params.rootHolderId)
		}else{
			object = getDomainObject(params.commentHolderType, params.commentHolderId)
		}
		
		if (!grailsApplication.isDomainClass(object.getClass())){
			return true
		}
		return userGroupService.hasPermissionAsPerGroup(object, 'allowNonMembersToComment', org.springframework.security.acls.domain.BasePermission.WRITE)
	}
	
	private setDefaultRange(params){
		params.max = params.max ? params.max.toInteger() : 3
		params.offset = params.offset ? params.offset.toLong() : 0
	}

    def addRecoComment(commentHolder, rootHolder, recoComment){
        recoComment = (recoComment?.trim()?.length() > 0)? recoComment.trim():null;
        if(recoComment){
            def m = [author:springSecurityService.currentUser, commentBody:recoComment, commentHolderId:commentHolder.id, \
                commentHolderType:commentHolder.class.getCanonicalName(), rootHolderId:rootHolder.id, rootHolderType:rootHolder.class.getCanonicalName(),locale_language:utilsService.getCurrentLanguage()]
                addComment(m);
        }
    }

	private userTagNofity(tagUserIds,domainObject,feedInstance,webaddress){
		def tu =[];			
		tagUserIds.each(){
			def tagUser = SUser.read(it);
			tu.add(tagUser);				
			Follow.addFollower(domainObject, tagUser);
		}			
		def otherParams = ['taggedUsers' : tu];
		utilsService.sendNotificationMail("COMMENT_ADD_USER_TAG", domainObject, null, webaddress, feedInstance,otherParams);

	}


}
