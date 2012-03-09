
<div class="grid_10 observation_story">
	<div>
        <img class="species_group_icon" src="${createLinkTo(dir: 'images', file: observationInstance.group.icon()?.fileName?.trim(), absolute:true)}" title="${observationInstance.group?.name}"/>
        
        <g:if test="${observationInstance.habitat}">
        	<img class="habitat_icon species_group_icon" src="${resource(dir:'images/group_icons',file:observationInstance.habitat+'.png', absolute:true)}" title="${observationInstance.habitat}"/>
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
			<span class="name">Observed on</span> <div class="value"><g:formatDate
					format="MMMMM dd, yyyy" date="${observationInstance.observedOn}" />
			</div>
		</div>
		
		<div class="prop readmore">
			<span class="name">Description </span> <div class="value"> ${observationInstance.notes}
			</div>
		</div>

		<div class="view_tags">
			<ul name="tags">
				<g:each in="${observationInstance.tags}">
					<li>
						${it}
					</li>
				</g:each>
			</ul>
		</div>
	</div>
</div>
