
<div class="grid_8 observation_story">
	<div>
                <img class="group_icon" src="${grailsApplication.config.grails.serverURL+'/images/group_icons/'}${observationInstance.group?.name?.trim().replaceAll(/ /, '_')}.png" title="${observationInstance.group?.name}"/>
		<p class="prop">
			<span class="name">By </span>
			<div class="value">
				<g:link controller="sUser" action="show"
					id="${observationInstance.author.id}">
					${observationInstance.author.username}
				</g:link>
			</div>
		</p>
		<p class="prop">
			<span class="name">Habitat </span>
			<div class="value">${observationInstance.habitat}</div>
		</p>
		<p class="prop">
			<span class="name">Observed on</span> <div class="value"><g:formatDate
					format="MMMMM dd, yyyy" date="${observationInstance.observedOn}" />
			</div>
		</p>
		<!--p class="prop">
			<span class="name">Group</span> <div class="value"><g:link
					controller="speciesGroup" action="show" id="${observationInstance.group?.id }">${observationInstance.group?.name }</g:link>
			</div>
		</p-->
		<p class="prop readmore">
			<span class="name">Description </span> <div class="value"> ${observationInstance.notes}
			</div>
		</p>
	</div>
</div>
