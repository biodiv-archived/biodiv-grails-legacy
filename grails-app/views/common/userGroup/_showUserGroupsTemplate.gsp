<div class="row section">
	<b>${title}</b>
	<ul class="thumbnails">
		<g:each in="${userGroupInstanceList}" var="${userGroup}">
			<li class="thumbnail">
				<g:link controller="userGroup" action="show" id="${userGroup.id}"><img class="logo" src="${userGroup.icon().fileName}" title="${userGroup.name}" alt="${userGroup.name}"></g:link>
			</li>
		</g:each>
	</ul>
</div>