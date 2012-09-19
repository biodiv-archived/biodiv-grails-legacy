<g:if test="${feedType != 'Specific'}">
	<feed:showFeedFilter model="[feedCategory:feedCategory]" />
</g:if>
<feed:showAllActivityFeeds model="['rootHolder':rootHolder, feedType:feedType, refreshType:refreshType, feedPermission:feedPermission, feedCategory:feedCategory]"/>
