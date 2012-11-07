<%@page import="species.participation.Recommendation"%>
<%@page import="species.participation.Observation"%>
<div class="post-comment">

	<form class="form-horizontal" onSubmit='return postComment(this, "${uGroup.createLink(controller:'comment', action:'addComment')}")'>
		<%
			def commentPlaceHolder = "Write comment"
			if(commentHolder.class.getName() == Observation.class.getName())
				commentPlaceHolder += " on observation"
			if(commentHolder.class.getName() == Recommendation.class.getName())
				commentPlaceHolder += " on species call"
		%>
		<textarea name="commentBody" class="comment-textbox" placeholder="${commentPlaceHolder}" ></textarea>
		<span  style="color:#B84A48; display:none;">Please write comment</span>
		<input type="hidden" name='commentHolderId' value="${commentHolder.id}" />
		<input type="hidden" name='commentHolderType' value="${commentHolder.class.getCanonicalName()}" />
		<input type="hidden" name='rootHolderId' value="${rootHolder.id}" />
		<input type="hidden" name='rootHolderType' value="${rootHolder.class.getCanonicalName()}" />
		<input type="hidden" name='commentType' value="${commentType}" />
		<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
		<input type="hidden" name='commentPostUrl' value="${uGroup.createLink(controller:'comment', action:'addComment')}"/>
		<input type="submit" value="Post" class="btn comment-post-btn " style="float:right;"/>
	</form>
</div>
