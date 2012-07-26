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
		def c = commentService.addComment(params);
		def comments = getAllNewerComments(params);
		def showCommentListHtml = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments]);
		def result = [showCommentListHtml:showCommentListHtml, newerTimeRef:comments.first().lastUpdated.time.toString()]
		render result as JSON
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

	def getComments = {
		log.debug params;
		def comments = getComments(params)
		def showCommentListHtml = g.render(template:"/common/comment/showCommentListTemplate", model:[comments:comments]);
		def olderTimeRef = (comments) ? (comments.last().lastUpdated.time.toString()) : null
		def result = [showCommentListHtml:showCommentListHtml, olderTimeRef:olderTimeRef]
		render result as JSON
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
	
	private List getComments(params){
		def comments;
		if(params.commentType && (params.commentType == "context")){
			comments = commentService.getComments(params)
		}else{
			comments = commentService.getSuperComments(params)
		}
		return comments
	}
	
	private getAllNewerComments(params){
		params.max = 100
		params.timeLine = "newer"
		params.refTime = params.refTime ?: new Date().previous().time.toString()
		return getComments(params)
	}
}
