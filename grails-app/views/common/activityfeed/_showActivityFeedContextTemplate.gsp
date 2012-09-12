<%@page import="species.participation.Observation"%>
<%@page import="species.groups.UserGroup"%>

<%@page import="species.participation.ActivityFeedService"%>
<div class="activityFeedContext" >
	<div class="feedParentContext">
		<g:if test="${feedInstance.rootHolderType ==  Observation.class.getCanonicalName()}" >
			<obv:showSnippet model="['observationInstance':feedParentInstance]"></obv:showSnippet>
		</g:if>
		<g:elseif test="${feedInstance.rootHolderType ==  UserGroup.class.getCanonicalName()}" >
			<uGroup:showSnippet model="['userGroupInstance':feedParentInstance]"></uGroup:showSnippet>
		</g:elseif>
		<g:else>
			${feedInstance.rootHolderType}
		</g:else>
	</div>
	
	<feed:showAllActivityFeeds model="['rootHolder':feedParentInstance, 'feedType':ActivityFeedService.SPECIFIC, 'refreshType':ActivityFeedService.MANUAL]" />
</div>