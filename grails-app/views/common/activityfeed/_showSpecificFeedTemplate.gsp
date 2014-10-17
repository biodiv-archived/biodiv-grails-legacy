<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.ActivityFeedService"%>
<g:if test="${feedInstance.activityType == ActivityFeedService.COMMENT_ADDED}">
	<comment:showCommentWithReply model="['feedInstance' : feedInstance, 'feedPermission':feedPermission, 'userLanguage':userLanguage]" />
</g:if>
<g:else>
	<div class="yj-message-container">
		<div class="yj-avatar">
			<a href="${uGroup.createLink(controller:'SUser', action:'show', id:feedInstance.author.id, userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}">
				<img class="small_profile_pic"
					src="${feedInstance.author.profilePicture(ImageType.SMALL)}"
					title="${feedInstance.author.name}" />
			</a>
		</div>
		<feed:showActivity model="['feedInstance' : feedInstance, 'feedPermission':feedPermission]" />
		<div>
			<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
		</div>
	</div>
</g:else>
