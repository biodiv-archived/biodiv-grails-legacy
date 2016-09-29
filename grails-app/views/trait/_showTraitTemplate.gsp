<div class="trait" data-toggle="buttons-radio">
<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':trait.values()]"/>
</div>
