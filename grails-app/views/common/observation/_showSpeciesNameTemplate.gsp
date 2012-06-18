<div class="species_title ellipsis multiline">
<g:set var="sName" value="${observationInstance.maxVotedSpeciesName}" />
<g:if test="${sName == 'Unknown'}">
	<g:if test="${isTitle}">
		<h1><i title="${sName}">${sName}</i>
		<a href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}">Help identify</a></h1>
	</g:if>
	<g:else>
		<i title="${sName}">${sName}</i>
		<a href="${createLink(controller:'observation', action:'show', id:observationInstance.id)}">Help identify</a>
	</g:else>
</g:if>
<g:else>
	<g:if test="${isTitle}">
		<h1><i>${sName}</i></h1>
		<h4>${observationInstance.fetchSuggestedCommonNames()}</h4>
	</g:if>
	<g:else>
		<i>${sName}</i><br>
		${observationInstance.fetchSuggestedCommonNames()}
	</g:else>
</g:else>
</div>