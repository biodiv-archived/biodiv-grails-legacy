<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<div class="toolbar">

		
			<g:if test="${speciesFieldInstance?.contributors}">
				<span class="name" style="color: #b1b1b1; margin-top: 10px;"><i
					class="icon-user"></i> by </span>
				<g:each in="${ speciesFieldInstance?.contributors}"
					var="contributor">
					${contributor.name}
				</g:each>
			</g:if>
			<div class="pull-right">
				<g:if test="${speciesFieldInstance?.licenses.size() > 0}">
					<g:each in="${speciesFieldInstance?.licenses}" var="license">
						<a class="license" href="${license?.url}" target="_blank"><img
							class="icon" 
							src="${createLinkTo(dir:'images/license', file: license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
							alt="${license?.name.value()}" /> </a>
					</g:each>
				</g:if>
			</div>
		
		<g:showSpeciesFieldHelp
			model="['speciesFieldInstance':speciesFieldInstance]" />

		<!-- div class="edit">
			<span class="ui-icon ui-icon-control ui-icon-edit" title="Edit Content"
				style="float: right;"></span>
			<div class="grid_10 ui-corner-all toolbarIconContent editContent"
				style="display: none;">
				<a class="ui-icon ui-icon-close" style="float: right;"></a>				
				<editField model="['speciesFieldInstance':speciesFieldInstance]"/>
			</div>
		</div-->
		<g:showSpeciesFieldAttribution
			model="['speciesFieldInstance':speciesFieldInstance]" />
	</div>
</g:if>