<div class="species_title ellipsis multiline">
<g:set var="sName" value="${observationInstance.maxVotedSpeciesName}" />
<g:if test="${sName == 'Unknown'}">
	<div class="sci_name"><i title="${sName}">${sName}</i>
	<a href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}">Help identify</a></div>
</g:if>
<g:else>
	<div class="sci_name"><i>${sName}</i></div>
	<div class="common_name">${observationInstance.fetchSuggestedCommonNames()}</div>
</g:else>
</div>