<div class="activityfeed activityfeed${feedType}">
	<a class="activiyfeednewermsg yj-thread-replies-container yj-show-older-replies" style="display:none;" href="#" title="load new feeds" onclick='loadNewerFeedsInAjax($(this).closest(".activityfeed${feedType}"), false);return false;'>Click to see new feeds</a>
	<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
	<ul>
		<feed:showActivityFeedList model="['feeds':feeds, 'feedType':feedType, 'feedPermission':feedPermission]" />
	</ul>
	<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
	<input type="hidden" name='feedType' value="${feedType}"/>
	<input type="hidden" name='feedCategory' value="${feedCategory}"/>
	<input type="hidden" name='feedClass' value="${feedClass}"/>
	<input type="hidden" name='feedPermission' value="${feedPermission}"/>
	<input type="hidden" name='refreshType' value="${refreshType}"/>
	<input type="hidden" name='rootHolderId' value="${rootHolder?.id}"/>
	<input type="hidden" name='rootHolderType' value="${rootHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='activityHolderId' value="${activityHolder?.id}"/>
	<input type="hidden" name='activityHolderType' value="${activityHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='feedUrl' value="${createLink(controller:'activityFeed', action: 'getFeeds')}"/>
	
	<input type="hidden" name='isCommentThread' value="${isCommentThread}"/>
	<input type="hidden" name='subRootHolderId' value="${subRootHolderId}"/>
	<input type="hidden" name='subRootHolderType' value="${subRootHolderType}"/>
	
	<g:if test="${refreshType == 'manual' && remainingFeedCount > 0}" >
		<a class="activiyfeedoldermsg yj-thread-replies-container yj-show-older-replies" href="#" title="show feeds" onclick='loadOlderFeedsInAjax($(this).closest(".activityfeed${feedType}"));return false;'>Show ${remainingFeedCount} older feeds >></a>
	</g:if>
</div>
<r:script>
	$(document).ready(function(){
		setUpFeed("${createLink(controller:'activityFeed', action:'getServerTime')}");
	});
</r:script>