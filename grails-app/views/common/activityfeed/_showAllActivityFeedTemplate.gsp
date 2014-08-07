<%@page import="species.groups.UserGroup"%>

<div class="activityfeed activityfeed${feedType}">

	<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
	<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
	<input type="hidden" name='feedType' value="${feedType}"/>
	<input type="hidden" name='feedCategory' value="${feedCategory}"/>
	<input type="hidden" name='feedClass' value="${feedClass}"/>
	<input type="hidden" name='feedOrder' value="${feedOrder}"/>
	<input type="hidden" name='feedPermission' value="${feedPermission}"/>
	<input type="hidden" name='refreshType' value="${refreshType}"/>
	<input type="hidden" name='rootHolderId' value="${rootHolder?.id}"/>
	<input type="hidden" name='rootHolderType' value="${rootHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='activityHolderId' value="${activityHolder?.id}"/>
	<input type="hidden" name='activityHolderType' value="${activityHolder?.class?.getCanonicalName()}"/>
	<input type="hidden" name='feedUrl' value="${uGroup.createLink(controller:'activityFeed', action: 'getFeeds')}"/>
	<input type="hidden" name='webaddress' value="${(rootHolder instanceof UserGroup) ? rootHolder.webaddress: null}"/>
	<input type="hidden" name='user' value="${user}"/>
	<input type="hidden" name='userGroupFromUserProfile' value="${userGroup?.id}"/>
	
	<input type="hidden" name='isCommentThread' value="${isCommentThread}"/>
	<input type="hidden" name='subRootHolderId' value="${subRootHolderId}"/>
	<input type="hidden" name='subRootHolderType' value="${subRootHolderType}"/>
	<input type="hidden" name='feedHomeObjectId' value="${rootHolder?.id}"/>
	<input type="hidden" name='feedHomeObjectType' value="${rootHolder?.class?.getCanonicalName()}"/>

    <g:if test="${!hideList}">
		<a class="activiyfeednewermsg yj-thread-replies-container yj-show-older-replies" style="display:none;" href="#" title="load new feeds" onclick='loadNewerFeedsInAjax($(this).closest(".activityfeed${feedType}"), false);return false;'>Click to see new feeds</a>
		<g:if test="${feedOrder == 'oldestFirst'}">	
			<g:if test="${refreshType == 'manual' && remainingFeedCount > 0}" >
				<a class="activiyfeedoldermsg yj-thread-replies-container yj-show-older-replies" href="#" title="show feeds" onclick='loadOlderFeedsInAjax($(this).closest(".activityfeed${feedType}"));return false;'>Show ${(feedType != 'GroupSpecific')?remainingFeedCount:''} older feeds >></a>
			</g:if>
			<ul>
				<feed:showActivityFeedList model="['feeds':feeds, 'feedType':feedType, 'feedPermission':feedPermission, feedHomeObject:rootHolder, 'userGroup':userGroup]" />
			</ul>
		</g:if>
		<g:else>
			<ul>
				<feed:showActivityFeedList model="['feeds':feeds, 'feedType':feedType, 'feedPermission':feedPermission, feedHomeObject:rootHolder , 'userGroup':userGroup]" />
			</ul>
			<g:if test="${refreshType == 'manual' && remainingFeedCount > 0}" >
				<a class="activiyfeedoldermsg yj-thread-replies-container yj-show-older-replies" href="#" title="show feeds" onclick='loadOlderFeedsInAjax($(this).closest(".activityfeed${feedType}"));return false;'>Show ${(feedType != 'GroupSpecific')?remainingFeedCount:''} older feeds >></a>
			</g:if>
	    </g:else>
	    <g:if test="${refreshType == 'auto'}">
	    	<span class="activiyfeedNoMoreFeedmsg yj-thread-replies-container yj-show-older-replies" style="display:none;">No more feeds</span>
	    </g:if>
	    
    </g:if>
</div>
<r:script>
	$(document).ready(function(){
		setUpFeed("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
	});
</r:script>
