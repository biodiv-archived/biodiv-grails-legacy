<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<div class="toolbar">

		<g:showSpeciesFieldHelp model="['speciesFieldInstance':speciesFieldInstance]" />	
		
		<!-- div class="edit">
			<span class="ui-icon ui-icon-control ui-icon-edit" title="Edit Content"
				style="float: right;"></span>
			<div class="grid_10 ui-corner-all toolbarIconContent editContent"
				style="display: none;">
				<a class="ui-icon ui-icon-close" style="float: right;"></a>				
				<editField model="['speciesFieldInstance':speciesFieldInstance]"/>
			</div>
		</div-->
		<g:showSpeciesFieldAttribution model="['speciesFieldInstance':speciesFieldInstance]" />	
	</div>
</g:if>