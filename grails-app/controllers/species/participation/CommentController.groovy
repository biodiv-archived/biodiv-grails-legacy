package species.participation

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*;

class CommentController {

	def springSecurityService;
	def commentService;
	def observationService;
	def utilsService;

	@Secured(['ROLE_USER'])
	def addComment() {
		params.author = springSecurityService.currentUser;
		params.locale_language = utilsService.getCurrentLanguage(request);
		
		def result = [:]
		//XXX on ajax pop up login post request is not sending all params 
		// in such cases checking params and handling gracefully 
		if(params.commentBody && params.commentBody.trim().length() > 0){
			def commentRes = commentService.addComment(params);
			if(!commentRes['success']){
				result['msg'] = commentRes['msgCode']? "${message(code: commentRes['msgCode'])}":'Error in saving'
				result["status"] = 'Error'
			}else{
				result = getResultForResponse(params, request);
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
	def removeComment() {
		if(commentService.removeComment(params)){
			render (['success:true']as JSON);
		}else{
			//XXX handle appropriately here
			log.error "Error in deleting comment " +  params.commentId
			render (['success':false, msg:"Error in deleting comment "] as JSON);
		}
	}
	
	def getAllNewerComments = {
		render getResultForResponse(params, request) as JSON;
	}

	def getComments = {
		def comments = commentService.getComments(params);
		def olderTimeRef = (comments) ? (comments.last().lastUpdated.time.toString()) : null
		def remainingCommentCount = (comments) ? getRemainingCommentCount(comments.last().lastUpdated.time.toString(), params) : 0
		def result = [olderTimeRef:olderTimeRef, remainingCommentCount:remainingCommentCount]
		result['showCommentListHtml'] = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments]);
        result['instanceList'] = comments
        def model = utilsService.getSuccessModel('', null, OK.value(), result);
        withFormat {
            json { render model as JSON }
            xml { render model as XML }
		}
	}
	
	def getCommentByType = {
		commentService.getCommentByType(params)
	}

	@Secured(['ROLE_USER'])
	def likeComment() {
		params.author = springSecurityService.currentUser;
		render commentService.likeComment(params)
	}
	
	@Secured(['ROLE_USER'])
	def editComment() {
		render "To do edit"
	}
	
	private getResultForResponse(params, request){
		def result = ["success":true];
		def comments = _getAllNewerComments(params);
		if(!comments.isEmpty()){
			result.putAll([newerTimeRef:comments.first().lastUpdated.time.toString(), newlyAddedCommentCount:comments.size()]);
			if(params.format?.equalsIgnoreCase("json")) {
				result['commentList'] = comments
			}else{
				def userLanguage = utilsService.getCurrentLanguage(request);
				result['showCommentListHtml'] = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments, userLanguage:userLanguage]);
			}
		}	
		return result
	}
	
	private _getAllNewerComments(params){
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
