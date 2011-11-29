		<g:if test="${speciesFieldInstance?.field.description}">
			<div class="help">
				<span class="ui-icon ui-icon-control ui-icon-help" title="Show help"
					style="float: right;"></span>
				<div class="grid_10 ui-corner-all toolbarIconContent helpContent"
					style="display: none;">
					<a class="ui-icon ui-icon-close" style="float: right;"></a>
					${speciesFieldInstance?.field.description.encodeAsHTML()}

				</div>
			</div>
		</g:if>
