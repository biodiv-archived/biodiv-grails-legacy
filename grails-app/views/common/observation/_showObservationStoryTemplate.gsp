
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
		<span class="name"><i class="icon-share-alt"></i>Species Name</span>
		<div class="value">
			<obv:showSpeciesName model="['observationInstance':observationInstance]" />
                        <i class="icon-ok"></i>
		</div>
	</div>


	<div class="prop">
		<span class="name"><i class="icon-map-marker"></i>Place</span>
		<div class="value">
                    <g:if test="${observationInstance.placeName == ''}">
                        ${observationInstance.reverseGeocodedName}
                    </g:if>
                    <g:else>
                        ${observationInstance.placeName}
                    </g:else>
                    <br/>
                    Lat: <g:formatNumber number="${observationInstance.latitude}"
				type="number" maxFractionDigits="2" />
			,
			Long: <g:formatNumber number="${observationInstance.longitude}"
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
		<span class="name"><i class="icon-time"></i>Submitted</span>
	 <obv:showDate 
			model="['observationInstance':observationInstance, 'propertyName':'createdOn']" />
                        
	</div>

	<div class="prop">
		<span class="name"><i class="icon-time"></i>Updated</span>
		<obv:showDate
		model="['observationInstance':observationInstance, 'propertyName':'lastRevised']" />
	</div>

	<sUser:showUserTemplate model="['userInstance':observationInstance.author]"/>

	<obv:showTagsSummary
		model="['observationInstance':observationInstance]" />

        <div class="story-footer">
            <div class="footer-item"><i class="icon-eye-open"></i>${observationInstance.getPageVisitCount()} views</div>
                
            <div class="footer-item"><i class="icon-comment"></i><fb:comments-count href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:grailsApplication.config.grails.domainServerURL)}"></fb:comments-count> comments</div>
            <div class="footer-item" style="width:50px;"><fb:like layout="button_count" href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:grailsApplication.config.grails.domainServerURL)}" width="450" show_faces="true"></fb:like></div>
        </div>

</div>
