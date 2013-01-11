<div class="species_title">
	<g:set var="sName" value="${observationInstance.fetchSpeciesCall()}" />
	<g:if test="${sName == 'Unknown'}">
		<div class="sci_name" title="${sName}">
			${sName} <a
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress) }">Help
				identify</a>
		</div>
	</g:if>
	<g:elseif test="${isListView}">
		<%def commonName = observationInstance.fetchSuggestedCommonNames() %>

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
		<%def commonName = observationInstance.fetchSuggestedCommonNames() %>
		<g:if test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sName }">
				${sName}
			</div>
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:else>
<%--			<s:showHeadingAndSubHeading model="['heading':sName, 'headingClass':headingClass]"/>--%>
			<div class="ellipsis" title="${sName}">
				${sName}
			</div>
		</g:else>
	</g:else>
</div>