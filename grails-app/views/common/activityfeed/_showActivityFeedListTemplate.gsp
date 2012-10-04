<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.participation.Comment"%>
<%@page import="species.participation.Observation"%>

<g:each var="feedInstance" in="${feeds}">
	<%
		def tmpliclass = (feedInstance.rootHolderType)?(feedInstance.rootHolderType + feedInstance.rootHolderId) : (ActivityFeedService.OTHER + feedInstance.id)
		if(feedType != ActivityFeedService.SPECIFIC && feedInstance.rootHolderType != Observation.class.getCanonicalName()){
			tmpliclass = (feedInstance.subRootHolderType)?(feedInstance.subRootHolderType + feedInstance.subRootHolderId) : (ActivityFeedService.OTHER + feedInstance.id)
		}
	%>
	<li class="activity_post ${tmpliclass}"><feed:showActivityFeed
			model="['feedInstance':feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" /></li>
</g:each>
