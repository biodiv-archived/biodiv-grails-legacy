package species.participation

import grails.converters.JSON
import grails.plugins.springsecurity.Secured

class CommentController {

	def springSecurityService;
	def commentService;

	@Secured(['ROLE_USER'])
	def addComment = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		
		
		def result = [:]
		//XXX on ajax pop up login post request is not sending all params 
		// in such cases checking params and handling gracefully 
		if(params.commentBody && params.commentBody.trim().length() > 0){
			def commentRes = commentService.addComment(params);
			if(!commentRes['success']){
				result['msg'] = commentRes['msg']
				result["status"] = 'Error'
			}else{
				result = getResultForResponse(params);
				result["clearForm"] = true;
			}
		} else if(params.ajax_login_error == "1") {
			result["success"] = true;
			result["status"] = 401;
		} else{
			result["success"] = true;
		}
		render result as JSON;
	}

	@Secured(['ROLE_USER'])
	def removeComment = {
		log.debug params;
		if(commentService.removeComment(params)){
			render (['success:true']as JSON);
		}else{
			//XXX handle appropriately here
			log.error "Error in deleting comment " +  params.commentId
			render (['success:false']as JSON);
		}
	}
	
	def getAllNewerComments = {
		log.debug params;
		render getResultForResponse(params) as JSON;
	}

	def getComments = {
		log.debug params;
		def comments = commentService.getComments(params);
		def showCommentListHtml = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments]);
		def olderTimeRef = (comments) ? (comments.last().lastUpdated.time.toString()) : null
		def remainingCommentCount = (comments) ? getRemainingCommentCount(comments.last().lastUpdated.time.toString(), params) : 0
		def result = [showCommentListHtml:showCommentListHtml, olderTimeRef:olderTimeRef, remainingCommentCount:remainingCommentCount]
		render result as JSON
	}
	
	def getCommentByType = {
		commentService.getCommentByType(params)
	}
	

	@Secured(['ROLE_USER'])
	def likeComment = {
		log.debug params;
		params.author = springSecurityService.currentUser;
		render commentService.likeComment(params)
	}
	
	@Secured(['ROLE_USER'])
	def editComment = {
		log.debug params;
		render "To do edit"
	}
	
	private getResultForResponse(params){
		def result = ["success":true];
		def comments = getAllNewerComments(params);
		if(!comments.isEmpty()){
			def showCommentListHtml = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments]);
			result.putAll([showCommentListHtml:showCommentListHtml, newerTimeRef:comments.first().lastUpdated.time.toString(), newlyAddedCommentCount:comments.size()]);
		}	
		return result
	}
	
	private getAllNewerComments(params){
		params.max = 100
		params.timeLine = "newer"
		params.refTime = params.newerTimeRef ?: new Date().previous().time.toString()
		return commentService.getComments(params)
	}
	
	private getRemainingCommentCount(String refTime, params){
		params.refTime = refTime
		return commentService.getCount(params)
	}
}
