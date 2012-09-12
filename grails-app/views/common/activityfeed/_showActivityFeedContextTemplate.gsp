<%@page import="species.participation.Observation"%>
<%@page import="species.participation.ActivityFeedService"%>
<div class="activityFeedContext" >
	<div class="feedParentContext">
		<g:if test="${feedInstance.rootHolderType ==  Observation.class.getName()}" >
			<obv:showSnippet model="['observationInstance':feedParentInstance]"></obv:showSnippet>
		</g:if>
		<g:else>
			${feedInstance.rootHolderType}
		</g:else>
	</div>
	
	<feed:showAllActivityFeeds model="['rootHolder':feedParentInstance, 'feedType':ActivityFeedService.SPECIFIC, 'refreshType':ActivityFeedService.MANUAL]" />
</div>