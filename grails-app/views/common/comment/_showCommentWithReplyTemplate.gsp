<%@page import="species.utils.ImageType"%>
<div class="yj-message-container">
	<div class="yj-avatar">
		<g:link controller="SUser" action="show"
			id="${feedInstance.author.id}">
			<img class="small_profile_pic"
				src="${feedInstance.author.icon(ImageType.SMALL)}"
				title="${feedInstance.author.name}" />
		</g:link>
	</div>
	<b> ${feedInstance.author.name} :
	<g:if test="${commentInstance.isMainThread()}">
		<span class="yj-context"> ${feedInstance.activityType} </span>
	</g:if>
	<g:else>	
		<span class="yj-context" title="${commentInstance.fetchParentText()}">${feedInstance.activityType} in reply to</span><g:link controller="SUser" action="show"
			id="${commentInstance.fetchParentCommentAuthor()?.id}"> ${commentInstance.fetchParentCommentAuthor()?.name} </g:link>
	</g:else>
	</b>
	<div class="feedActivityHolderContext yj-message-body">
		<pre>${commentInstance.body}</pre>
	</div>
	<g:if test="${feedPermission != 'readOnly' && commentInstance}">
		<sUser:ifOwns model="['user':commentInstance.author]">
			<div class="reco-comment-close" value="close" title="delete comment"
				onclick="deleteCommentActivity(this, ${commentInstance.id}, '${createLink(controller:'comment', action:'removeComment')}'); return false;">
				<i class="icon-remove"></i>
			</div>
		</sUser:ifOwns>
	</g:if>
	<div class="comment-reply">
		<a data-toggle="dropdown" href="#" title="reply on comment" onclick='$(this).siblings(".commnet-reply-popup").show();return false'>Reply</a>
		<div class="commnet-reply-popup popup-form" style="display: none">
			<div class="popup-form-close" value="close" onclick='$(this).parent().hide(); return false;'>
				<i class="icon-remove"></i>
			</div>
			<textarea name="commentBody" class="comment-textbox" placeholder="Reply on comment"></textarea>
			<a href="#" class="btn btn-mini pull-right" title="post comment" onclick='replyOnComment($(this), ${commentInstance.id}, "${createLink(controller:'comment', action:'addComment')}"); $(this).parent().hide();return false;'>Post</a>	
		</div>
	</div>
	<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
</div>
