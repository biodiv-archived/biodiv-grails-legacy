<%@page import="species.participation.Observation"%>
<div class="observation_story tablet">
	<h5>
		<a href=/biodiv/SUser/show/${userInstance.id}> ${userInstance.name}
		</a>
	</h5>
	<div class="icons-bar">
		<div class="observation-icons"></div>
	</div>

	<div class="story">
		<g:if test="${userInstance.location}">
			<div>
				<i class="icon-map-marker"></i>
				${userInstance.location}
			</div>
		</g:if>

		<g:if test="${userInstance.website}">
			<div>
				<i class="icon-road"></i>
				<a target="blank"
					href="${fieldValue(bean: userInstance, field: 'website')}"> ${fieldValue(bean: userInstance, field: 'website')}
				</a>
			</div>
		</g:if>

	</div>

	<div class="story-footer">
		<div class="footer-item" title="No of Observations">
			<i class="icon-camera"></i>
			${Observation.countByAuthor(userInstance)}
		</div>

		<div class="footer-item" title="No of Tags">
			<i class="icon-tags"></i>
			<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />
		</div>
	</div>
</div>


