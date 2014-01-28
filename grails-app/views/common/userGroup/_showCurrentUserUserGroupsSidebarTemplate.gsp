<ul class="nav span3" style="overflow:hidden;margin-bottom:0px;margin-left:40px;">
	<!-- li class="nav-header">My Groups</li-->
	<li style="clear: both;" />
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li style="float:right;"><g:link mapping="userGroupGeneric" action="list" absolute='true' params="['user':sUser.renderCurrentUserId()]"
		style="display:inline;">More ...</g:link></li>
</ul>
