<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<div class="toolbar">


		<g:if test="${speciesFieldInstance?.contributors}">
			<span class="name" style="color: #b1b1b1; margin-top: 10px;"><i
				class="icon-user"></i> by </span>
			<g:each in="${ speciesFieldInstance?.contributors}" var="contributor">
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
			<button type="button" class="btn btn-link"
				data-toggle="collapse-next" data-target=".attributionContent"
				title="Show details" style="padding:2px 2px;">
				<i class=" icon-info-sign"></i>
			</button>
			<button type="button" class="btn btn-link"
				data-toggle="collapse-next" data-target=".helpContent"
				title="About field" style="padding:2px 2px;">
				<i class="icon-question-sign"></i>
			</button>
		</div>

		<g:showSpeciesFieldAttribution
			model="['speciesFieldInstance':speciesFieldInstance]" />
		<g:showSpeciesFieldHelp
			model="['speciesFieldInstance':speciesFieldInstance]" />
	</div>
</g:if>