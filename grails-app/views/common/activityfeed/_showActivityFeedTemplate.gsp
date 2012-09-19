<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.participation.Observation"%>
<div class="activityFeed-container">
<g:if test="${feedType == ActivityFeedService.SPECIFIC}">
	<feed:showSpecificFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" />
</g:if>

<g:elseif test="${feedType == ActivityFeedService.GROUP_SPECIFIC}">
	<g:if test="${feedInstance.rootHolderType == Observation.class.getCanonicalName()}">
	<feed:showAggregateFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" />
	</g:if>
	
	<g:else>
	<feed:showSpecificFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" />
	</g:else>
</g:elseif>

<g:else>
	<feed:showAggregateFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType,'feedPermission':feedPermission]" />
</g:else>
</div>
