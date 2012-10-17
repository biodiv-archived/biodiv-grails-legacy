<ul class="dropdown-menu">
	<g:if test="${userGroups}">
		<g:each in="${userGroups}" var="userGroup">
			<li><uGroup:showUserGroupSignature
					model="['userGroup':userGroup]" /></li>
		</g:each>
		<li><small>Groups is in Beta. We would like you to
				provide valuable feedback, suggestions and interest in using the
				groups functionality. </small></li>
	</g:if>
	<li class="pull-right"><g:link mapping="userGroupGeneric"
			action="list" absolute='true'>All Groups ...</g:link>
	</li>
</ul>