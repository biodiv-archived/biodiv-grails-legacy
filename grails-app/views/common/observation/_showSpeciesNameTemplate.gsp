<div class="species_title">
	<%
		def commonName = observationInstance.fetchSuggestedCommonNames()
		def speciesId = observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId();
		def speciesLink = " "
		if(speciesId && !isHeading){
			speciesLink += '<a href="' + uGroup.createLink(controller:'species', action:'show', id:speciesId, 'userGroupWebaddress':params?.webaddress, absolute:true) + '">' + "<i class='icon-info-sign' style='margin-right: 0px; margin-left: 10px;'></i>know more" + "</a>"
		}
	%>
	<g:set var="sName" value="${observationInstance.fetchSpeciesCall()}" />
	<g:if test="${sName == 'Unknown'}">
		<div class="sci_name ellipsis" title="${sName}">
			${sName} <a
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
			<div class="sci_name ellipsis" title="${sName}">
				 ${sName}
			</div>
		</g:elseif>
		<g:else>
				<div class="ellipsis" title="${sName}">
					${sName}
				</div>
		</g:else>
	</g:elseif>
	<g:else>
		<g:if test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sName }">
				${sName + speciesLink}
			</div>
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:else>
<%--			<s:showHeadingAndSubHeading model="['heading':sName, 'headingClass':headingClass]"/>--%>
			<div class="ellipsis" title="${sName}">
				${sName + speciesLink}
			</div>
		</g:else>
	</g:else>
</div>