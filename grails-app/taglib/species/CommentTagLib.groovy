package species

import species.participation.Comment;

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
		out << render(template:"/common/comment/showCommentPopupTemplate", model:attrs.model);
	}
	
	def showAllComments = {attrs, body->
		def model = attrs.model
		model.rootHolder = model.rootHolder?:model.commentHolder
		model.commentType = model.commentType ?: "context"
		if(model.commentType == "context"){
			model.comments = Comment.fetchComments(model.commentHolder, model.rootHolder, 3, new Date().time.toString(), null)
			model.totalCount = Comment.fetchCount(model.commentHolder, model.rootHolder, null, null)
		}else{
			model.comments =  Comment.fetchSuperComments(model.rootHolder, 3, new Date().time.toString(), null)
			model.totalCount = Comment.fetchSuperCount(model.rootHolder, null, null)
		}
		model.newerTimeRef = (model.comments) ? (model.comments.first().lastUpdated.time.toString()) : null
		model.olderTimeRef = (model.comments) ? (model.comments.last().lastUpdated.time.toString()) : null
		out << render(template:"/common/comment/showAllCommentsTemplate", model:model);
	}
}
