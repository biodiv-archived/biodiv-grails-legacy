<g:javascript>
    $(document).ready(function(){
        $(".observation_story").hover(function(){
                $('.more_info', this).show(); 
            },
            function(){
                $('.more_info', this).hide(); 
            });
    });

</g:javascript>

<div class="observation_story tablet">
		<img class="species_group_icon"
			src="${createLinkTo(file: observationInstance.group.icon()?.fileName?.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
			title="${observationInstance.group?.name}" />

		<g:if test="${observationInstance.habitat}">
			<img class="habitat_icon species_group_icon"
				src="${createLinkTo(dir: 'group_icons', file:'All.png', base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="${observationInstance.habitat}" />
		</g:if>

		<div class="prop tablet">
			<span class="name tablet">By </span>
			<div class="value tablet">
				<g:link controller="sUser" action="show"
					id="${observationInstance.author.id}">
					${observationInstance.author.username}
				</g:link>
			</div>
		</div>

		<div class="prop tablet">
			<span class="name tablet">Observed on</span>
			<div class="value tablet">
				<g:formatDate format="MMMMM dd, yyyy"
					date="${observationInstance.observedOn}" />
			</div>
		</div>

		<div class="prop tablet">
			<span class="name tablet">Place name</span>
			<div class="value tablet">
				${observationInstance.placeName}
			</div>
		</div>
                <br/>


                <div class="more_info" style="position:absolute; display:none;background-color: #ffffff;width: 200px;z-index:2;box-shadow:0 8px 6px -6px black;">
		    <obv:showTagsSummary model="['observationInstance':observationInstance]" />
                </div>
</div>


