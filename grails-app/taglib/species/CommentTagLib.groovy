package species

class CommentTagLib {
	static namespace = "comment"
	
	def commentService
	
	def showComment = {attrs, body->
		out << render(template:"/common/comment/showCommentTemplate", model:attrs.model);
	}
	
	def postComment = {attrs, body->
		def model = attrs.model
		out << render(template:"/common/comment/postCommentTemplate", model:attrs.model);
	}
	
	def showCommentList = {attrs, body->
		out << render(template:"/common/comment/showCommentListTemplate", model:attrs.model);
	}
	
	def showCommentContext = {attrs, body->
		out << render(template:"/common/comment/showCommentContextTemplate", model:attrs.model);
	}
	
	def showCommentPopup = {attrs, body->
		def model = attrs.model
		model.rootHolder = model.rootHolder?:model.commentHolder
		model.commentType = model.commentType ?: "context"
		if(model.commentType == "context"){
			model.totalCount = commentService.getCount(model.commentHolder, model.rootHolder, null, null)
		}else{
			model.totalCount = commentService.getCount(null, model.rootHolder, null, null)
		}
		out << render(template:"/common/comment/showCommentPopupTemplate", model:attrs.model);
	}
	
	def showAllComments = {attrs, body->
		def model = attrs.model
		model.rootHolder = model.rootHolder?:model.commentHolder
		model.commentType = model.commentType ?: "context"
		model.canPostComment = (model.canPostComment != null)? model.canPostComment : true
		if(model.commentType == "context"){
			model.comments = commentService.getComments(model.commentHolder, model.rootHolder, 3, new Date().time.toString(), null)
			model.totalCount = commentService.getCount(model.commentHolder, model.rootHolder, null, null)
		}else{
			model.comments =  commentService.getComments(null, model.rootHolder, 3, new Date().time.toString(), null)
			model.totalCount = commentService.getCount(null, model.rootHolder, null, null)
		}
		model.newerTimeRef = (model.comments) ? (model.comments.first().lastUpdated.time.toString()) : null
		model.olderTimeRef = (model.comments) ? (model.comments.last().lastUpdated.time.toString()) : null
		out << render(template:"/common/comment/showAllCommentsTemplate", model:model);
	}
	
	///////////////////////////////// FEEDS ////////////////////////////
	
	def showAllFeeds = {attrs, body->
		def model = attrs.model
		model.feeds = commentService.getCommentByType(null)
		out << render(template:"/common/feeds/showAllFeedsTemplate", model:model);
	}
	
	def showFeed = {attrs, body->
		out << render(template:"/common/feeds/showFeedTemplate", model:attrs.model);
	}
	
	def showFeedContext = {attrs, body->
		def model = attrs.model
		model.observationInstance = Observation.read(model.feedInstance.rootHolderId)
		out << render(template:"/common/feeds/showFeedContextTemplate", model:attrs.model);
	}
}
