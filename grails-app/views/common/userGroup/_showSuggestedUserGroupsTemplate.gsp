<ul class="nav">
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li><g:link controller="userGroup" action="list"
			style="background-color: transparent;display:inline;">More ...</g:link>
	</li>
</ul>

