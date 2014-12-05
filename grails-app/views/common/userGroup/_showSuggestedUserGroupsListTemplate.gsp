
<g:if test="${userGroups}">
	<g:each in="${userGroups}"
		var="userGroup">
		<li class="usergrouplist"><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" /></li>
	</g:each>
</g:if>
<g:if test="${userGroups.size() >= 20}">
<li style="display:none;">
	<a href="#" class="btn btn-mini load_more_usergroup " onclick="loadSuggestedGroups($(this).parent().parent(), '${uGroup.createLink(controller:'userGroup', action:'suggestedGroups')}',20);return false;" >Load More</a>
</li>
</g:if>	
	
