<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.ActivityFeedService"%>
<g:if test="${feedInstance.activityType == ActivityFeedService.COMMENT_ADDED}">
	<comment:showCommentWithReply model="['feedInstance' : feedInstance, 'feedPermission':feedPermission]" />
</g:if>
<g:else>
	<div class="yj-message-container">
		<div class="yj-avatar">
			<g:link controller="SUser" action="show"
				id="${feedInstance.author.id}">
				<img class="small_profile_pic"
					src="${feedInstance.author.icon(ImageType.SMALL)}"
					title="${feedInstance.author.name}" />
			</g:link>
		</div>
		<b> ${feedInstance.author.name} :<span class="yj-context"> ${feedInstance.activityType}</span></b>
		<feed:showActivity model="['feedInstance' : feedInstance, 'feedPermission':feedPermission]" />
		<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
	</div>
</g:else>
