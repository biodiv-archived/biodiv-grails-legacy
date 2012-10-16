<g:if test="${userGroups}">
		<ul class="dropdown-menu">
			<g:each in="${userGroups}" var="userGroup">
				<li><uGroup:showUserGroupSignature
						model="['userGroup':userGroup]" /></li>
			</g:each>
			<li><small>Groups is in Beta. We would like you to provide
			valuable feedback, suggestions and interest in using the groups
			functionality. </small></li>
			<li class="pull-right"><g:link mapping="userGroupGeneric"
					action="list" style="display:inline;">More ...</g:link>
			</li>
		</ul>
		
</g:if>