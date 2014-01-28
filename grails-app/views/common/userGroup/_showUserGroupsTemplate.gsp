<div class="row section">
	<h6>${title}</h6>
	<ul class="nav">
		<g:each in="${userGroupInstanceList}" var="${userGroup}">
			<li class="span4"><uGroup:showUserGroupSignature  model="[ 'userGroup':userGroup]" /></li>
		</g:each>
	</ul>
</div>
