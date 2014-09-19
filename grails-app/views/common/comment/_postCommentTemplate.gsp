<%@page import="species.participation.Recommendation"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.ActivityFeedService"%>
<div class="post-comment">
    <g:set var="postCommentUrl" value="${uGroup.createLink(controller:'comment', action:'addComment')}"/>
    <form class="form-horizontal" onSubmit="return postComment(this, '${postCommentUrl}')">
		<%
			boolean isGroupDisccusionThread = params.webaddress && (params.controller == 'activityFeed' || params.action == 'activity')
			def commentPlaceHolder = "Write comment"
			def commentHolderClass = ActivityFeedService.getType(commentHolder)
			if(commentHolderClass == Observation.class.getName())
				commentPlaceHolder += " on observation"
			if(commentHolderClass == Recommendation.class.getName())
				commentPlaceHolder += " on species call"
			if(isGroupDisccusionThread)
				commentPlaceHolder = "Message"
		%>
		<g:if test="${isGroupDisccusionThread}">
			<textarea name="commentSubject" class="comment-subjectbox" placeholder="Subject"></textarea>
		</g:if>
		<textarea name="commentBody" class="comment-textbox" placeholder="${commentPlaceHolder}" ></textarea>
		<span  style="color:#B84A48; display:none;">Please write comment</span>
		<input type="hidden" name='commentHolderId' value="${commentHolder.id}" />
		<input type="hidden" name='commentHolderType' value="${commentHolderClass}" />
		<input type="hidden" name='rootHolderId' value="${rootHolder.id}" />
		<input type="hidden" name='rootHolderType' value="${rootHolder.class.getCanonicalName()}" />
		<input type="hidden" name='commentType' value="${commentType}" />
		<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
		<input type="hidden" name='commentPostUrl' value="${uGroup.createLink(controller:'comment', action:'addComment')}"/>
		<input type="submit" value="Post" class="btn comment-post-btn " style="float:right;"/>
	</form>
</div>
