<g:each in="${userInstance.habitats}" var="habitat">
		<button class="btn habitats_sprites ${habitat.iconClass()} active"
			id="${"habitat_" + habitat.id}" value="${habitat.id}"
			title="${habitat.name}"
			data-content="${message(code: 'habitat.definition.' + habitat.name)}"
			rel="tooltip" data-original-title="A Title"></button>
</g:each>