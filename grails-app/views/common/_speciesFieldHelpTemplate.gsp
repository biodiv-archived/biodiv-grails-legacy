		<g:if test="${speciesFieldInstance?.field.description}">
			<div class="dropdown pull-right">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" title="About field"><i class="  icon-question-sign"></i></a>			
			
				<ul class="dropdown-menu span6 helpContent">
					<li>${speciesFieldInstance?.field.description.encodeAsHTML()}</li>
				</ul>
			</div>
		</g:if>
