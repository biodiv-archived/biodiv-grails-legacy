<div class="activityFeed-container">
<%@page import="species.participation.ActivityFeedService"%>
<g:if test="${feedType == ActivityFeedService.SPECIFIC}">
	<feed:showSpecificFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType, 'feedPermission':feedPermission]" />
</g:if>
<g:else>
	<feed:showAggregateFeed
			model="['feedInstance' : feedInstance, 'feedType':feedType,'feedPermission':feedPermission]" />
</g:else>
</div>
