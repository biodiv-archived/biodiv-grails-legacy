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
	<feed:showActivityFeedContext
		model="['feedInstance' : feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" />
</div>
