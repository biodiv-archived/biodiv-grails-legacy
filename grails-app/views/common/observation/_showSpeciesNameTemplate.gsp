<div class="species_title multiline">
	<g:set var="sName" value="${observationInstance.fetchSpeciesCall()}" />
	<g:if test="${sName == 'Unknown'}">
		<g:if test="${userGroup}">
			<g:set var="url" value="${createLink(mapping:'userGroupModule', controller:'observation', action:'show', id:observationInstance.id, params:['webaddress':userGroup.webaddress]) }"/>
		</g:if>
		<g:elseif test="${userGroupWebaddress }">
			<g:set var="url" value="${createLink(mapping:'userGroupModule', controller:'observation', action:'show', id:observationInstance.id, params:['webaddress':userGroupWebaddress]) }"/>
		</g:elseif>
		<g:else>
			<g:set var="url" value="${createLink( controller:'observation', action:'show', id:observationInstance.id) }"/>
		</g:else>
		<div class="sci_name">
			<i title="${sName}"> ${sName}
			</i> <a
				href="${url}">Help
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