<ul class="nav">
	<li class="nav-header">My Groups</li>
	<li style="clear: both;" />
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>

</ul>

<div class="pull-right nav-header" style="clear: both;">
	<g:link controller="userGroup" action="list" params="['user':2]"
		style="display:inline;">More ...</g:link>
</div>