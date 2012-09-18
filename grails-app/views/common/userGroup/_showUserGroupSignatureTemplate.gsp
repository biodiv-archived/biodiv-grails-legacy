<g:link controller="userGroup" action="show" id="${userGroup.id}">
	<img class="logo" src="${userGroup.mainImage()?.fileName}"
		title="${userGroup.name}" alt="${userGroup.name}" />
</g:link>
