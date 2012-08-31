<g:each in="${userGroupInstance.habitats}" var="habitat">
	<g:link controller="userGroup" action="list"
		params="['habitat':habitat.id]">
		<button class="btn habitats_sprites ${habitat.iconClass()}"
			id="${"habitat_" + habitat.id}" value="${habitat.id}"
			title="${habitat.name}"
			data-content="${message(code: 'habitat.definition.' + habitat.name)}"
			rel="tooltip" data-original-title="A Title"></button>
	</g:link>
</g:each>