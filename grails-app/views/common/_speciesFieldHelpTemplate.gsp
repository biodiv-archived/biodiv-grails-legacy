
<g:if test="${speciesFieldInstance?.field.description}">
	<div class="helpContent collapse">
		<div>
		${speciesFieldInstance?.field.description.encodeAsHTML()}
		</div>
	</div>
</g:if>
