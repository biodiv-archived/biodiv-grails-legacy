<%@page import="species.ScientificName.TaxonomyRank"%>
<g:if test="${speciesInstance.taxonConcept.rank == TaxonomyRank.SPECIES.ordinal()}">
<ul>
<g:each in="${speciesInstance.fetchInfraSpecies()}" var="infraSpecies">
<li><a href="${uGroup.createLink(controller:'species', action:'show', id:infraSpecies.id)}">${raw(infraSpecies.taxonConcept.italicisedForm)}</a></li>
</g:each>
</ul>
</g:if>
