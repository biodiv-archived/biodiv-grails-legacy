<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Observation"%>
<div class="span3 prop tablet">
		<div class="figure">
		<a href=/biodiv/SUser/show/${userInstance.id}> <img
			style="float: left;" src="${userInstance.icon(ImageType.SMALL)}"
			class="small_profile_pic" title="${userInstance.name}" /></a>
		</div>
		<div class="story" style="margin-left:60px">
			<a href=/biodiv/SUser/show/${userInstance.id}> ${userInstance.name} </a>
			<g:if test="${userInstance.location}">
				<div>
					<i class="icon-map-marker"></i>
					${userInstance.location}
				</div>
			</g:if>

			<div class="story-footer" style="position:static;">
				<div class="footer-item" title="No of Observations">
					<i class="icon-screenshot"></i>
					${Observation.countByAuthor(userInstance)}
				</div>

				<div class="footer-item" title="No of Tags">
					<i class="icon-tags"></i>
					<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />
				</div>
			</div>

		</div>

	</div>