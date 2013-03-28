<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<div class="yj-thread-replies-container">
	<div class="yj-message-container">
		<div class="yj-avatar">
			<g:link controller="SUser" action="show"
				id="${commentInstance.author.id}">
				<img class="small_profile_pic"
					src="${commentInstance.author.icon(ImageType.SMALL)}"
					title="${commentInstance.author.name}" />
			</g:link>
		</div>
		<b> ${commentInstance.author.name} :
		</b>
		<comment:showCommentContext
			model="['commentInstance' : commentInstance]" />
		<div class="yj-message-body">
			<pre>${Utils.linkifyYoutubeLink(commentInstance.body)}</pre>
		</div>
		<div class="yj-attributes">
			<time class="timeago" datetime="${commentInstance.lastUpdated.getTime()}"></time>
			${commentInstance.author }
			<sUser:ifOwns model="['user':commentInstance.author]">
				<a href="#"
					onclick="deleteComment(${commentInstance.id}, '${createLink(controller:'comment', action:'removeComment')}'); return false;">
					<span class="deleteFlagIcon"><i class="icon-trash"></i></span>
				</a>
			</sUser:ifOwns>
		</div>
	</div>
</div>
