<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.participation.Comment"%>
<%@page import="species.groups.UserGroup"%>

<g:each var="feedInstance" in="${feeds}">
	<%
		def tmpliclass = (feedInstance.rootHolderType)?(feedInstance.rootHolderType + feedInstance.rootHolderId) : (ActivityFeedService.OTHER + feedInstance.id)
		if(feedType != ActivityFeedService.SPECIFIC && feedInstance.rootHolderType == UserGroup.class.getCanonicalName()){
			tmpliclass = (feedInstance.subRootHolderType)?(feedInstance.subRootHolderType + feedInstance.subRootHolderId) : (ActivityFeedService.OTHER + feedInstance.id)
		}
	%>
    <li class="${tmpliclass} activity_post">
    <feed:showActivityFeed
    model="['feedInstance':feedInstance, 'feedType':feedType, 'feedPermission':feedPermission, feedHomeObject:feedHomeObject]" />
    </li>
</g:each>
