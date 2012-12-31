<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<div class="observation_story" style="overflow:auto;">
	<h5>
		<a class="ellipsis"
			href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			${userInstance.name} </a>
	</h5>


	<div class="span8" style="padding-bottom: 10px;">
		<g:if test="${userInstance.location}">
			<div class="prop">
				<span class="name"><i class="icon-map-marker"></i>Location</span>
				<div class="value">
				${userInstance.location}
				</div>
			</div>
		</g:if>

		<g:if test="${userInstance.website}">
			<div class="prop">
				<span class="name"><i class="icon-road"></i>Website</span>
				<div class="value">
				<g:if test="${Utils.isURL(userInstance.website) }">
					<a target="_blank" href="${userInstance.getWebsiteLink()}"> ${fieldValue(bean: userInstance, field: 'website')}
					</a>
				</g:if>
				<g:else>
					${fieldValue(bean: userInstance, field: 'website')}
				</g:else>
				</div>
			</div>
		</g:if>

		<div class="prop">
			<span class="name"><i class="icon-time"></i>Member since </span>
			<div class="value">
				<time class="timeago"
					datetime="${userInstance.dateCreated.getTime()}"></time>
			</div>
		</div>
		<div class="prop">
			<span class="name"><i class="icon-time"></i>Last visited </span>
			<div class="value">
				<time class="timeago"
					datetime="${userInstance.lastLoginDate.getTime()}"></time>
			</div>
		</div>

	</div>
	<div class="story-footer">
		<span class="footer-item" title="No of Observations"> <i
			class="icon-screenshot"></i> <obv:showNoOfObservationsOfUser
				model="['user':userInstance]" /> </span> <span class="footer-item"
			title="No of Tags"> <i class="icon-tags"></i> <obv:showNoOfTagsOfUser
				model="['userId':userInstance.id]" /> </span> <span class="footer-item"
			title="No of Identifications"> <i class="icon-check"></i> <obv:showNoOfRecommendationsOfUser
				model="['user':userInstance]" /> </span>
	</div>

</div>





