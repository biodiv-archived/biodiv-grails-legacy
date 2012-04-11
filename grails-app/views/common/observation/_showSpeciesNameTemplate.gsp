<g:set var="sName" value="${observationInstance.maxVotedSpeciesName}" />
<g:if test="${sName == 'Unknown'}">
	<i>${sName}</i>
	<a href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}">Help identify</a>
</g:if>
<g:else>
	<i>${sName}</i>
</g:else>
