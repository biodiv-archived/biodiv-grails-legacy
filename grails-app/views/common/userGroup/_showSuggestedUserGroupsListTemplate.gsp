<g:if test="${userGroups}">

	<g:each in="${userGroups}"
		var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" /></li>
	</g:each>
</g:if>
<li style="float: right;"><g:link mapping="userGroupGeneric"
		action="list" absolute='true'>More ...</g:link>
</li>	
