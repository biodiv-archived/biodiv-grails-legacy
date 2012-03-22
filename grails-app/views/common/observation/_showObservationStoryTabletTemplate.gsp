<g:javascript>
    $(document).ready(function(){
        $(".observation_story").hover(function(){
                $('.more_info', this).slideDown('fast'); 
            },
            function(){
                $('.more_info', this).slideUp('fast'); 
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
		
		<a href=/biodiv/SUser/show/${observationInstance.author.id}>  
		<img class="species_group_icon"
			src="${observationInstance.icon()}"
			title="${observationInstance.author.username}" />
		</a>
		
		<div class="prop tablet">
			<span class="name tablet">Species Name</span>
			<div class="value tablet">
				<g:set var= "sName" value="${observationInstance.maxVotedSpeciesName}" />
					<g:if test="${sName == 'Unknown'}"> 
						${sName} <a href="#">Help identify</a>  
					</g:if>
				<g:else>
					${sName}
				</g:else>
			</div>
		</div>	
		
		<div class="prop tablet">
			<span class="name tablet">Created on</span>
			<div class="value tablet">
				<g:formatDate format="MMMMM dd, yyyy"
					date="${observationInstance.createdOn}" />
			</div>
		</div>

		<div class="prop tablet">
			<span class="name tablet">Place name</span>
			<div class="value tablet">
				${observationInstance.placeName}
			</div>
		</div>
		
<%--		<div class="prop tablet">--%>
<%--			<span class="name tablet">Recommendations</span>--%>
<%--			<div class="value tablet">--%>
<%--				${observationInstance.getRecommendationCount()}--%>
<%--			</div>--%>
<%--		</div>--%>
		
		<div class="prop tablet">
			<span class="name tablet">Last Updated</span>
			<div class="value tablet">
				${observationInstance.daysAfterLastUpdate()} days before
			</div>
		</div>
		
		<div class="prop tablet">
			<span class="name tablet">Visit Count</span>
			<div class="value tablet">
				${observationInstance.getPageVisitCount()}
			</div>
		</div>
		
       <br/>		

		<div class="more_info" style="position:absolute; display:none;background-color: #fbfbfb;width: 200px;z-index:2;box-shadow:0 8px 6px -6px black;">
		    <obv:showTagsSummary model="['observationInstance':observationInstance]" />
        </div>
</div>


