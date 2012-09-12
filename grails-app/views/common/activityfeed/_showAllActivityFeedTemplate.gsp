<div class="activityfeed activityfeed${feedType}">
	<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
	<ul>
		<feed:showActivityFeedList model="['feeds':feeds, 'feedType':feedType, 'feedPermission':feedPermission]" />
	</ul>
	<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
	<input type="hidden" name='feedType' value="${feedType}"/>
	<input type="hidden" name='feedPermission' value="${feedPermission}"/>
	<input type="hidden" name='refreshType' value="${refreshType}"/>
	<input type="hidden" name='rootHolderId' value="${rootHolder?.id}"/>
	<input type="hidden" name='rootHolderType' value="${rootHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='activityHolderId' value="${activityHolder?.id}"/>
	<input type="hidden" name='activityHolderType' value="${activityHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='feedUrl' value="${createLink(controller:'activityFeed', action: 'getFeeds')}"/>
	
	<g:if test="${refreshType == 'manual' && remainingFeedCount > 0}" >
		<a class="yj-thread-replies-container yj-show-older-replies" href="#" title="show feeds" onclick='loadOlderFeedsInAjax($(this).closest(".activityfeed${feedType}"), "${createLink(controller:'activityFeed', action: 'getFeeds')}", "${feedType}");return false;'>Show ${remainingFeedCount} older feeds >></a>
	</g:if>
	
</div>
<r:script>
	$(document).ready(function(){
		setUpFeed();
	});
</r:script>