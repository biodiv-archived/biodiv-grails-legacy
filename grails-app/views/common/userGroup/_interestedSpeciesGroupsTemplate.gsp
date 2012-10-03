<g:each in="${userGroupInstance.speciesGroups}" var="speciesGroup">
	<g:link controller="userGroup" action="list"
		params="['sGroup':speciesGroup.id]">
		<button class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
			id="${"group_" + speciesGroup.id}" value="${speciesGroup.id}"
			title="${speciesGroup.name}"></button>
	</g:link>
</g:each>