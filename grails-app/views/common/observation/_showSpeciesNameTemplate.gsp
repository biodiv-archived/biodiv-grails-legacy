<div class="species_title ellipsis multiline">
<g:set var="sName" value="${observationInstance.maxVotedSpeciesName}" />
<g:if test="${sName == 'Unknown'}">
	<i title="${sName}">${sName}</i>
	<a href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}">Help identify</a>
</g:if>
<g:else>
	<i>${sName}</i>
</g:else>
</div>