
<%@page import="species.utils.ImageType"%>
<div class="observation_info">

	<div class="icons-bar">
		<div class="observation-icons">
			<img class="group_icon" title="${observationInstance.group?.name}"
				src='${createLinkTo(dir:'images', file: observationInstance.group.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}' />
			<g:if test="${observationInstance.habitat}">
				<img class="habitat_icon group_icon"
					title="${observationInstance.habitat.name}"
					src='${createLinkTo(dir: 'images', file:observationInstance.habitat.icon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}' />
			</g:if>

		</div>

		<div class="user-icon">
			<a href=/biodiv/SUser/show/${observationInstance.author.id}> <img
				src="${observationInstance.author.profilePicture()}" class="small_profile_pic"
				title="${observationInstance.author.username}" /> </a>
		</div>
	</div>
	<div style="clear: both;"></div>
	<h5>
		<obv:showSpeciesName
			model="['observationInstance':observationInstance, 'userGroup':userGroup]" />
	</h5>

	<!--div class="prop tablet">
                    <span class="name tablet">Species Name</span>
                    <div class="value tablet">
                            <obv:showSpeciesName model="['observationInstance':observationInstance]" />
                    </div>
            </div-->


	<div class="prop tablet">
		<!--span class="name tablet">Place name</span-->
		<div class="value tablet highlighted">
			<i class="icon-map-marker"></i>
			<g:if test="${observationInstance.placeName == ''}">
				${observationInstance.reverseGeocodedName}
			</g:if>
			<g:else>
				${observationInstance.placeName}
			</g:else>
		</div>
	</div>

	<!--div class="prop tablet">
                    <span class="name tablet">Lat/Long</span>
                    <div class="value tablet highlighted">
                            <g:formatNumber number="${observationInstance.latitude}"
                                    type="number" maxFractionDigits="2" />
                            ,
                            <g:formatNumber number="${observationInstance.longitude}"
                                    type="number" maxFractionDigits="2" />
                    </div>
            </div-->
	<%--		<div class="prop tablet">--%>
	<%--			<span class="name tablet">Recommendations</span>--%>
	<%--			<div class="value tablet">--%>
	<%--				${observationInstance.getRecommendationCount()}--%>
	<%--			</div>--%>
	<%--		</div>--%>

	<div class="prop tablet">
		<span class="name tablet"><i class="icon-time"></i>Submitted</span>
		<obv:showDate
			model="['observationInstance':observationInstance, 'propertyName':'createdOn']" />
	</div>


	<div class="prop tablet">
		<span class="name tablet"><i class="icon-time"></i>Updated</span>
		<obv:showDate
			model="['observationInstance':observationInstance, 'propertyName':'lastRevised']" />
	</div>


	<%-- <obv:showTagsSummary model="['observationInstance':observationInstance]" /> --%>

	<div class="value tablet">
		<%--  <fb:like layout="button_count" href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}" send="true" width="450" show_faces="true"></fb:like> --%>
	</div>



	<%-- <sUser:showUserTemplate model="['userInstance':observationInstance.author]"/> --%>


	<div class="btn btn-primary view-button">
		<a href="${uGroup.createLink(action:"show", controller:"observation",
			id:observationInstance.id)}">View</a>
	</div>


</div>


