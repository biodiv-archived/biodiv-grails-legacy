<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
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
				<g:if test="${Utils.isURL(userInstance.website) }">
					<a target="blank"
						href="${fieldValue(bean: userInstance, field: 'website')}"> ${fieldValue(bean: userInstance, field: 'website')}
					</a>
				</g:if><g:else>
					${fieldValue(bean: userInstance, field: 'website')}
				</g:else>
			</div>
		</g:if>

	</div>

	<div class="story-footer">
		<div class="footer-item" title="No of Observations">
			<i class="icon-screenshot"></i>
			<obv:showNoOfObservationsOfUser model="['user':userInstance]"/>
		</div>

		<div class="footer-item" title="No of Tags">
			<i class="icon-tags"></i>
			<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />
		</div>
	</div>
</div>


