<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<div class="yj-message-container">
	<div class="yj-avatar">
		<a href="${uGroup.createLink(controller:'SUser', action:'show', id:feedInstance.author.id, userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}">
			<img class="small_profile_pic"
				src="${feedInstance.author.profilePicture(ImageType.SMALL)}"
				title="${feedInstance.author.name}" />
		</a>
	</div>
	<b> ${feedInstance.author.name} :
	<g:if test="${commentInstance.isMainThread()}">
		<span class="yj-context"> ${raw(commentContext)} </span>
	</g:if>
	<g:else>	
		<span class="yj-context" title="${commentInstance.fetchParentText()}"><g:message code="msg.reply.to" /></span><g:link controller="SUser" action="show"
			id="${commentInstance.fetchParentCommentAuthor()?.id}"> ${commentInstance.fetchParentCommentAuthor()?.name} </g:link>
	</g:else>
	</b>
	<div class="feedActivityHolderContext yj-message-body">
		${raw(Utils.linkifyYoutubeLink(commentInstance.body?.replaceAll("\\n",'<br/>')))}
	</div>
	<g:if test="${feedPermission != 'readOnly' && commentInstance}">
		<sUser:ifOwns model="['user':commentInstance.author]">
			<div class="reco-comment-close" value="close" title="delete comment"
				onclick="deleteCommentActivity(this, ${commentInstance.id}, '${uGroup.createLink(controller:'comment', action:'removeComment',  userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}'); return false;">
				<i class="icon-remove"></i>
			</div>
		</sUser:ifOwns>
	</g:if>
	<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
	
	<div class="comment-reply">
		<a data-toggle="dropdown" href="#" title="reply on comment" onclick='$(this).hide();$(this).siblings(".commnet-reply-popup").show();return false'><g:message code="msg.Reply" /></a>
		<div class="commnet-reply-popup clearfix" style="display: none;position:relative;">
			<div class="popup-form-close" value="close" onclick='$(this).parent().hide().prev().show(); return false;'>
				<i class="icon-remove"></i>
			</div>
			<textarea name="commentBody" class="comment-textbox" placeholder="Reply on comment"></textarea>
			<a href="#" class="btn btn-mini pull-right" title="post comment" onclick='replyOnComment($(this), ${commentInstance.id}, "${uGroup.createLink(controller:'comment', action:'addComment',  userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}"); return false;'><g:message code="msg.Post" /></a>	
		</div>
	</div>
	
</div>
