<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
	<div style="clear:both;"></div>
	<div class="toolbar">

		<div class="span6" style="height:30px;margin-left:0px;">
			<g:if test="${speciesFieldInstance?.contributors}">
				<ul class="tagit" style="list-style:none;display:inline-block;margin-left:0px;">
					<li><span class="name" style="color: #b1b1b1;"><i
					class="icon-user"></i> by </span></li>
					<g:each in="${ speciesFieldInstance?.contributors}"
						var="contributor">
						<li class="contributor_ellipsis" title="${contributor.name.trim()}">${contributor.name.trim()}</li>
					</g:each>
				</ul>
			</g:if>
		</div>
		<div class="pull-right span3">
			<g:if test="${speciesFieldInstance?.licenses.size() > 0}">
				<g:each in="${speciesFieldInstance?.licenses}" var="license">
					<a class="license" href="${license?.url}" target="_blank"><img
						class="icon"
						src="${createLinkTo(dir:'images/license', file: license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
						alt="${license?.name.value()}" /> </a>
				</g:each>
			</g:if>
			<button type="button" class="btn btn-link pull-right"
				data-toggle="collapse-next" data-target=".helpContent"
				title="About field" style="padding: 2px 2px;">
				<i class="icon-question-sign"></i>
			</button>
			<button type="button" class="btn btn-link pull-right"
				data-toggle="collapse-next" data-target=".attributionContent"
				title="Show details" style="padding: 2px 2px;">
				<i class=" icon-info-sign"></i>
			</button>
			
		</div>
		<div class="clearfix"></div>
		<g:showSpeciesFieldAttribution
			model="['speciesFieldInstance':speciesFieldInstance]" />
		<g:showSpeciesFieldHelp
			model="['speciesFieldInstance':speciesFieldInstance]" />
	</div>
</g:if>