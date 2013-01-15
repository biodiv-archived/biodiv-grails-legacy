package species.participation

class CommentService {

	static transactional = false
	def grailsApplication

	def addComment(params){
		validateParams(params);
		Comment c = new Comment(author:params.author, body:params.commentBody.trim(), commentHolderId:params.commentHolderId, \
						commentHolderType:params.commentHolderType, rootHolderId:params.rootHolderId, rootHolderType:params.rootHolderType, \
						parentId:params.parentId, mainParentId:params.mainParentId);

		if(params.dateCreated) {
			c.dateCreated = params.dateCreated
			c.lastUpdated = new Date();
		}
		if(params.lastUpdated) {
			c.lastUpdated = params.lastUpdated;
		}


		if(!c.save(flush:true)){
			c.errors.allErrors.each { log.error it }
			return null
		}else{
			try {
				getDomainObject(params.commentHolderType, params.commentHolderId).onAddComment(c)
			}catch (MissingMethodException e) {
				//e.printStackTrace();
			}
			return c
		}
	}

	def removeComment(params){
		Comment comment = Comment.get(params.commentId.toLong());
		try{
			comment.deleteComment();
			return true
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
		if(!className || className.trim() == ""){
			return null
		}
		id = id.toLong()
		return grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
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
	
	private setDefaultRange(params){
		params.max = params.max ? params.max.toInteger() : 3
		params.offset = params.offset ? params.offset.toLong() : 0
	}

}
