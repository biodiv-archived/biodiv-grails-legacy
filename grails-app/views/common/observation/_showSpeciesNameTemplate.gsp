<div class="species_title">
	<%
		def commonName = observationInstance.isChecklist ? observationInstance.title :observationInstance.fetchSuggestedCommonNames()
		def speciesId = observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId();
		def speciesLink = " "
		if(speciesId && !isHeading){
			speciesLink += '<a class="species-page-link" style="font-style: normal;" href="' + uGroup.createLink(controller:'species', action:'show', id:speciesId, 'userGroupWebaddress':params?.webaddress, absolute:true) + '">' + "<i class='icon-info-sign' style='margin-right: 1px; margin-left: 10px;'></i>See species page" + "</a>"
		} 
		if(observationInstance.id != observationInstance.sourceId && !isHeading){
			speciesLink += '<a class="species-page-link" title="source checklist" style="font-style: normal;" href="' + uGroup.createLink(controller:'checklist', action:'show', id:observationInstance.sourceId, 'userGroupWebaddress':params?.webaddress, absolute:true) + '">' + "<i class='icon-info-sign' style='margin-right: 1px; margin-left: 10px;'></i>See checklist" + "</a>"
		}
	%>
	<g:set var="sName" value="${observationInstance.fetchFormattedSpeciesCall()}" />
	<g:set var="sNameTitle" value="${observationInstance.fetchSpeciesCall()}" />
	<g:if test="${observationInstance.isChecklist}">
		<div class="ellipsis" title="${commonName}">
			${commonName}
		</div>
	</g:if>
	<g:else>
	<g:if test="${sName == 'Unknown'}">
		<div class="sci_name ellipsis" title="${sNameTitle}">
			${raw(sName)} <a
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress) }">Help
				identify</a>
		</div>
	</g:if>
	<g:elseif test="${isListView}">
		<g:if test="${commonName}">
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:elseif test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sNameTitle}">
				 ${raw(sName)}
			</div>
		</g:elseif>
		<g:else>
                        <div class="ellipsis" title="${sNameTitle}">
                            ${raw(sName)}
                        </div>
		</g:else>
	</g:elseif>
	<g:else>
		<g:if test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sNameTitle }">
                ${raw(sName)} ${speciesLink}
			</div>
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:else>
			<div class="ellipsis" title="${sNameTitle}">
                ${raw(sName)} ${speciesLink}
			</div>
		</g:else>
	</g:else>
	</g:else>
</div>
