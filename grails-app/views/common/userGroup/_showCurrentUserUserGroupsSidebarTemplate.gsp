<ul class="nav">
	<!-- li class="nav-header">My Groups</li-->
	<li style="clear: both;" />
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li class="pull-right"><g:link controller="userGroup" action="list" params="['user':2]"
		style="display:inline;">More ...</g:link></li>
</ul>
