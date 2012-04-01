
<%@page import="species.utils.ImageType"%>
<div class="observation_story">
	<div class="observation-icons">
		<img class="group_icon"
        	title="${observationInstance.group?.name}"  
			style="background: url('${createLinkTo(dir:'images', file: observationInstance.group.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 -100px; width: 50px; height: 50px; margin-left: 6px;"/>

		<g:if test="${observationInstance.habitat}">
			<img class="habitat_icon group_icon"
                title="${observationInstance.habitat.name}" 
             	style="background: url('${createLinkTo(dir: 'images', file:observationInstance.habitat.icon(ImageType.SMALL)?.fileName?.trim(), absolute:true)}') no-repeat; background-position: 0 -100px; width: 50px; height: 50px; margin-left: 6px;"/>
		</g:if>
	</div>

	<div class="prop">
		<span class="name">Species Name</span>
		<div class="value">
			<obv:showSpeciesName model="['observationInstance':observationInstance]" />
		</div>
	</div>


	<div class="prop">
		<span class="name">Place name</span>
		<div class="value">
			${observationInstance.placeName}
		</div>
	</div>

	<div class="prop">
		<span class="name">Lat/Long</span>
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
		<span class="name">Last Update</span>
		<obv:showDate
		model="['observationInstance':observationInstance, 'propertyName':'lastRevised']" />
	</div>

	<div class="prop">
		<span class="name">Visit Count</span>
		<div class="value">
			${observationInstance.getPageVisitCount()}
		</div>
	</div>

	<div class="prop">
		<span class="name">Comments</span>
		<div class="value">
			<fb:comments-count href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:grailsApplication.config.grails.domainServerURL)}"></fb:comments-count>
		</div>
	</div>

	<div class="prop">
		<span class="name">Likes</span>
		<div class="value">
			<fb:like layout="button_count" href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:grailsApplication.config.grails.domainServerURL)}" width="450" show_faces="true"></fb:like>
		</div>
	</div>
	
					
	<sUser:showUserTemplate model="['userInstance':observationInstance.author]"/>

	<obv:showTagsSummary
		model="['observationInstance':observationInstance]" />
</div>
