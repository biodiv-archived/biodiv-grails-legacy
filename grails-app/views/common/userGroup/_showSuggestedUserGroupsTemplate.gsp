<ul class="nav">
	<li class="nav-header">Other groups of interest</li>
	<li style="clear: both;" />
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li class="pull-right"><g:link controller="userGroup"
			action="list" style="display:inline;">More ...</g:link></li>
</ul>


