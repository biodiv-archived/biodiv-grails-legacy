<div
	class="speciesField${speciesFieldInstance[0]?.field.id} speciesField"
	style="display: none">
	<div class="header">
		<h3>
			${speciesFieldInstance[0]?.field?.concept}
		</h3>
		<h1>
			${speciesFieldInstance[0]?.field?.category}
		</h1>
	</div>
	<g:each in="${speciesFieldInstances}" var="${speciesFieldInstance}">
		<g:showSpeciesField
			model="['speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesId]" />
	</g:each>
</div>

