package species

class CommentTagLib {
	static namespace = "comment"
	
	def commentService
	def activityFeedService
	
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
	
	def showCommentWithReply = {attrs, body->
		def model = attrs.model
//		def showAlways = (model.showAlways != null)?model.showAlways:false
//		 
//		if(showAlways || model.feedInstance.showComment()){
		model.commentInstance = commentService.getDomainObject( model.feedInstance.activityHolderType, model.feedInstance.activityHolderId)
		//this is for checklist row and species field comment
		model.commentContext = activityFeedService.COMMENT_ADDED  + activityFeedService.getCommentContext(model.commentInstance, params)
		out << render(template:"/common/comment/showCommentWithReplyTemplate", model:attrs.model);
//		}
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
		model.showCommentList = (model.showCommentList == null)? true: model.showCommentList
		model.commentType = model.commentType ?: "context"
		model.canPostComment = (model.canPostComment != null)? model.canPostComment : true
		if(model.showCommentList){
			if(model.commentType == "context"){
				model.comments = commentService.getComments(model.commentHolder, model.rootHolder, 3, new Date().time.toString(), null)
				model.totalCount = commentService.getCount(model.commentHolder, model.rootHolder, null, null)
			}else{
				model.comments =  commentService.getComments(null, model.rootHolder, 3, new Date().time.toString(), null)
				model.totalCount = commentService.getCount(null, model.rootHolder, null, null)
			}
			model.newerTimeRef = (model.comments) ? (model.comments.first().lastUpdated.time.toString()) : null
			model.olderTimeRef = (model.comments) ? (model.comments.last().lastUpdated.time.toString()) : null
		}else{
			model.newerTimeRef = new Date().time.toString()
		}
		out << render(template:"/common/comment/showAllCommentsTemplate", model:model);
	}
}
