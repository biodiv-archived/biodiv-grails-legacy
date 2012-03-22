
<div class="observation_story">
	<div class="observation-icons">
		<img class="species_group_icon"
			src="${createLinkTo(file: observationInstance.group.icon()?.fileName?.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
			title="${observationInstance.group?.name}" />

		<g:if test="${observationInstance.habitat}">
			<img class="habitat_icon species_group_icon"
				src="${createLinkTo(dir: 'group_icons', file:'All.png', base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="${observationInstance.habitat}" />
		</g:if>
	</div>

	<div class="prop">
		<span class="name">Species Name</span>
		<div class="value">
			<g:set var="sName" value="${observationInstance.maxVotedSpeciesName}" />
			<g:if test="${sName == 'Unknown'}">
				${sName}
				<a href="#">Help identify</a>
			</g:if>
			<g:else>
				${sName}
			</g:else>
		</div>
	</div>


	<div class="prop">
		<span class="name">Place name</span>
		<div class="value">
			${observationInstance.placeName}
		</div>
	</div>

	<div class="prop">
		<span class="name">Latitude/Longitude</span>
		<div class="value">
			<g:formatNumber number="${observationInstance.latitude}"
				type="number" maxFractionDigits="2" />
			,
			<g:formatNumber number="${observationInstance.longitude}"
				type="number" maxFractionDigits="2" />
		</div>
	</div>

	<%--		<div class="prop">--%>
	<%--			<span class="name">Recommendations</span>--%>
	<%--			<div class="value">--%>
	<%--				${observationInstance.getRecommendationCount()}--%>
	<%--			</div>--%>
	<%--		</div>--%>

	<div class="prop">
		<span class="name">Created on</span>
		<obv:showDate
			model="['observationInstance':observationInstance, 'propertyName':'createdOn']" />
	</div>

	<div class="prop">
		<span class="name">Last Updated</span>
		<obv:showDate
			model="['observationInstance':observationInstance, 'propertyName':'lastUpdated']" />
	</div>

	<div class="prop">
		<span class="name">Visit Count</span>
		<div class="value">
			${observationInstance.getPageVisitCount()}
		</div>
	</div>

	<div class="prop">
		<div class="user-icon">
			<a href=/biodiv/SUser/show/${observationInstance.author.id}> <img
				src="${createLinkTo(file: observationInstance.author.icon()?.fileName?.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="${observationInstance.author.username}" /> </a>
		</div>
		<div class="username-value">
			<a href=/biodiv/SUser/show/${observationInstance.author.id}> ${observationInstance.author.username}
			</a>
		</div>
	</div>




	<obv:showTagsSummary
		model="['observationInstance':observationInstance]" />
</div>
