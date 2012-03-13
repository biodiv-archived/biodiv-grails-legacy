
<div class="observation_story">
		<img class="species_group_icon"
			src="${createLinkTo(file: observationInstance.group.icon()?.fileName?.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
			title="${observationInstance.group?.name}" />

		<g:if test="${observationInstance.habitat}">
			<img class="habitat_icon species_group_icon"
				src="${createLinkTo(dir: 'group_icons', file:'All.png', base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="${observationInstance.habitat}" />
		</g:if>

		<div class="prop">
			<span class="name">By </span>
			<div class="value">
				<g:link controller="sUser" action="show"
					id="${observationInstance.author.id}">
					${observationInstance.author.username}
				</g:link>
			</div>
		</div>

		<div class="prop">
			<span class="name">Observed on</span>
			<div class="value">
				<g:formatDate format="MMMMM dd, yyyy"
					date="${observationInstance.observedOn}" />
			</div>
		</div>

		<div class="prop">
			<span class="name">Place name</span>
			<div class="value">
				${observationInstance.placeName}
			</div>
		</div>

		<obv:showTagsSummary model="['observationInstance':observationInstance]" />
</div>
