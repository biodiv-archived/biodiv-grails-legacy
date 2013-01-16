
<g:if test="${speciesFieldInstance?.field.description}">
	<div class="helpContent span11"  style="display:none;">
		<div>
		${speciesFieldInstance?.field.description.encodeAsHTML()}
		</div>
	</div>
</g:if>
