<div class="species_title multiline">
	<g:set var="sName" value="${observationInstance.fetchSpeciesCall()}" />
	<g:if test="${sName == 'Unknown'}">
		<div class="sci_name">
			<i title="${sName}"> ${sName}
			</i> <a
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress) }">Help
				identify</a>
		</div>
	</g:if>
	<g:else>
		<div class="sci_name">
			<g:if test="${observationInstance.maxVotedReco.isScientificName}">
			  	<i>
					${sName}
				</i>
				<div class="common_name">
					${observationInstance.fetchSuggestedCommonNames()}
				</div>
			</g:if>
			<g:else>
			    ${sName}
			</g:else>
		</div>
	</g:else>
</div>