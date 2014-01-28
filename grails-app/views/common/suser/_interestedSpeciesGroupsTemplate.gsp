<g:if test="${userInstance.speciesGroups}">
<g:each in="${userInstance.speciesGroups}" var="speciesGroup">
		<button class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
			id="${"group_" + speciesGroup.id}" value="${speciesGroup.id}"
			title="${speciesGroup.name}"></button>
</g:each>
</g:if>
<g:else>

</g:else>
