<h5 class="nav-header">Groups of Interest</h5>
<ul class="nav  block-tagadelic">	
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li class="pull-right"><g:link controller="userGroup"
			action="list" style="display:inline;">More ...</g:link></li>
</ul>


