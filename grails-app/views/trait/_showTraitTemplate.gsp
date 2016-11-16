<div class="trait" data-toggle="buttons-radio">
<%
	def traitValue
	def displayAny
		if(fromSpeciesShow){
			traitValue = factInstance[trait]
			displayAny = true
		}
		else{
			traitValue = trait.values()
			displayAny = false
		}
 %>
<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny]"/>
</div>