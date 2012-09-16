<%@page import="species.participation.ActivityFeedService"%>
<g:each var="feedInstance" in="${feeds}">
	<li class="${(feedInstance.rootHolderType)?(feedInstance.rootHolderType + feedInstance.rootHolderId) : (ActivityFeedService.OTHER + feedInstance.id)}"><feed:showActivityFeed
			model="['feedInstance':feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" /></li>
</g:each>
