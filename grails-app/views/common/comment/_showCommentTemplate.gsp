<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<div class="yj-thread-replies-container">
	<div class="yj-message-container">
		<div class="yj-avatar">
			<g:link controller="SUser" action="show"
				id="${commentInstance.author.id}">
				<img class="small_profile_pic"
					src="${commentInstance.author.profilePicture(ImageType.SMALL)}"
					title="${commentInstance.author.name}" />
			</g:link>
		</div>
		<b> ${commentInstance.author.name} :
		</b>
		<comment:showCommentContext
			model="['commentInstance' : commentInstance,'userLanguage' : userLanguage]" />
		<div class="yj-message-body">
			${raw(Utils.linkifyYoutubeLink(commentInstance.body?.replaceAll("\\n",'<br/>')))}
		</div>
		<div class="yj-attributes">
			<time class="timeago" datetime="${commentInstance.lastUpdated.getTime()}"></time>
			${commentInstance.author }
			<sUser:ifOwns model="['user':commentInstance.author]">
				<a href="javascript:void(0);" onclick="editCommentActivity(this.parentNode, ${commentInstance.id}); return false;">
					<span class="editFlagIcon"><i class="icon-edit"></i></span>
				</a>
				<a href="#"
					onclick="deleteComment(${commentInstance.id}, '${createLink(controller:'comment', action:'removeComment')}'); return false;">
					<span class="deleteFlagIcon"><i class="icon-trash"></i></span>
				</a>
			</sUser:ifOwns>
		</div>
	</div>
</div>
