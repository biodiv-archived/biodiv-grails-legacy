<div class="trait" data-toggle="buttons-radio">
<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
	def traitValue;
		<g:if test = "${fromSpeciesShow!=true}">
			traitValue = factInstance[trait]
		</g:if>
		<g:else>
			traitValue = trait.values();
		</g:else>
<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':factInstance[trait],'displayAny':true]"/>
</div>