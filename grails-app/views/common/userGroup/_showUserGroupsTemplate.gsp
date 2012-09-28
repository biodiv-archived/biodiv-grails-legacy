<div class="row section">
	<b>${title}</b>
	<ul class="nav">
		<g:each in="${userGroupInstanceList}" var="${userGroup}">
			<li class="span3"><uGroup:showUserGroupSignature  model="[ 'userGroup':userGroup]" /></li>
		</g:each>
	</ul>
</div>