<a href="#"
	onclick="$('#selectedGroupList').modal('show'); return false;"
	title="Protected to group experts/members. Need to join any of the user groups this observation belongs to inorder to add a species call"
	class="btn btn-primary btn-small pull-right">Join Groups</a>
	
<g:if test="${!showOnlyLink}">
	<uGroup:showUserGroupsListInModal
		model="['userGroupInstanceList':userGroupInstanceList]" />
</g:if>