
<div class="observation_story">
		<div>
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
		</div>
			
		<div class="prop">
			<span class="name">Species Name</span>
			<div class="value">
				<g:set var= "sName" value="${observationInstance.maxVotedSpeciesName}" />
					<g:if test="${sName == 'Unknown'}"> 
						${sName} <a href="#">Help identify</a>  
					</g:if>
				<g:else>
					${sName}
				</g:else>
			</div>
		</div>	
			
<%--		<div class="prop">--%>
<%--			<span class="name">Observed on</span>--%>
<%--			<div class="value">--%>
<%--				<g:formatDate format="MMMMM dd, yyyy"--%>
<%--					date="${observationInstance.observedOn}" />--%>
<%--			</div>--%>
<%--		</div>--%>
<%--		--%>

		<div class="prop">
			<span class="name">Created on</span>
			<div class="value">
				<g:formatDate format="MMMMM dd, yyyy"
					date="${observationInstance.createdOn}" />
			</div>
		</div>
		<div class="prop">
			<span class="name">Place name</span>
			<div class="value">
				${observationInstance.placeName}
			</div>
		</div>
		
<%--		<div class="prop">--%>
<%--			<span class="name">Recommendations</span>--%>
<%--			<div class="value">--%>
<%--				${observationInstance.getRecommendationCount()}--%>
<%--			</div>--%>
<%--		</div>--%>
		
		<div class="prop">
			<span class="name">Last Updated</span>
			<div class="value">
				${observationInstance.daysAfterLastUpdate()} days ago
			</div>
		</div>
		
		<div class="prop">
			<span class="name">Visit Count</span>
			<div class="value">
				${observationInstance.getPageVisitCount()}
			</div>
		</div>
		
		<obv:showTagsSummary model="['observationInstance':observationInstance]" />
</div>
