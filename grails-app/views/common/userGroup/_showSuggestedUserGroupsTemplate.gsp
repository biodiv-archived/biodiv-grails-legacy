<ul class="nav">
	<li class="nav-header">Other groups that might interest you</li>
	<li style="clear: both;" />
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" /></li>
	</g:each>

</ul>
<div class="pull-right nav-header" style="clear: both;">
	<g:link controller="userGroup" action="list" style="display:inline;">More ...</g:link>
</div>

